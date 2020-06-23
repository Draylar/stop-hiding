package draylar.stophiding;

import draylar.stophiding.key.KeyBindings;
import draylar.stophiding.mixin.ContainerScreenMixin;
import draylar.stophiding.mixin.RenderPhaseAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;

@Environment(EnvType.CLIENT)
public class StopHidingClient implements ClientModInitializer {

    public static HashMap<BlockPos, Integer> showInventoryMap = new HashMap<>();
    public static String latestItem = "";

    public static final RenderLayer DEPTH_LINES = RenderLayer.of(
            "depth_lines",
            VertexFormats.POSITION_COLOR,
            1,
            256,
            RenderLayer.MultiPhaseParameters
                    .builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .layering(RenderPhaseAccessor.getVIEW_OFFSET_Z_LAYERING())
                    .transparency(RenderPhaseAccessor.getTRANSLUCENT_TRANSPARENCY())
                    .writeMaskState(RenderPhaseAccessor.getCOLOR_MASK())
                    .depthTest(RenderPhaseAccessor.getALWAYS_DEPTH_TEST())
                    .build(false)
    );

    @Override
    public void onInitializeClient() {
        KeyBindings.init();
        registerClientPacketHandlers();
        registerKeybindHandlers();
    }

    private void registerKeybindHandlers() {
        ClientTickCallback.EVENT.register(tick -> {
            if(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindings.STOP_HIDING.getDefaultKey().getCode())) {
                MinecraftClient client = MinecraftClient.getInstance();

                if(client.currentScreen instanceof HandledScreen) {
                    HandledScreen containerScreen = (HandledScreen) client.currentScreen;
                    Slot focusedSlot = ((ContainerScreenMixin) containerScreen).getFocusedSlot();

                    if(client.player.inventory.getCursorStack().isEmpty() && focusedSlot != null && focusedSlot.hasStack()) {
                        PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
                        byteBuf.writeString(Registry.ITEM.getId(focusedSlot.getStack().getItem()).toString());
                        latestItem = new TranslatableText(focusedSlot.getStack().getItem().getTranslationKey()).asString();
                        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(StopHiding.SERVER_PACKET, byteBuf);
                        ClientSidePacketRegistry.INSTANCE.sendToServer(packet);
                    }
                }
            }
        });
    }

    private void registerClientPacketHandlers() {
        // receive packet from server with info on where inventories are
        ClientSidePacketRegistry.INSTANCE.register(StopHiding.CLIENT_PACKET, ((context, buffer) -> {
            int found = buffer.readInt();
            int size = buffer.readInt();
            List<BlockPos> positions = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                positions.add(BlockPos.fromLong(buffer.readLong()));
            }

            positions.forEach(pos -> {
                if(!showInventoryMap.containsKey(pos)) {
                    showInventoryMap.put(pos, StopHiding.CONFIG.highlightTime);
                }
            });

            if(StopHiding.CONFIG.displayResults) {
                if (found > 0) {
                    MinecraftClient.getInstance().player.sendMessage(new LiteralText(String.format("Found %d %ss across %d %s", found, latestItem, size, size > 1 ? "inventories." : "inventory.")), true);
                } else {
                    MinecraftClient.getInstance().player.sendMessage(new LiteralText("Not in any nearby inventories."), true);
                }
            }
        }));
    }
}
