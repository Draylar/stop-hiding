package draylar.stophiding.mixin;

import draylar.stophiding.StopHidingClient;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow private ClientWorld world;

    @Shadow
    protected static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
    }

    @Inject(
            method = "render",
            at = @At(value = "RETURN", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V", ordinal = 0)
    )
    private void renderCenter(
            MatrixStack matrices,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f matrix4f,
            CallbackInfo ci
    ) {
        Profiler profiler = world.getProfiler();
        profiler.swap("stophiding");

        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        List<BlockPos> expiredOutlines = new ArrayList<>();

        for (BlockPos outlinePos : StopHidingClient.showInventoryMap.keySet()) {
            RenderSystem.disableDepthTest();
            GlStateManager.disableDepthTest();

            Integer currentTime = StopHidingClient.showInventoryMap.get(outlinePos);
            VoxelShape shape = client.world.getBlockState(outlinePos).getOutlineShape(client.world, outlinePos);

            float opacity = 1.0f;
            if(currentTime <= 100) {
                opacity -= 1 - currentTime / 100f;
            }

            drawShapeOutline(
                    matrices,
                    bufferBuilders.getEffectVertexConsumers().getBuffer(StopHidingClient.DEPTH_LINES),
                    shape,
                    outlinePos.getX() - camX, outlinePos.getY() - camY, outlinePos.getZ() - camZ,
                    1.0F, 1.0F, 1.0F, opacity
            );

            // decrement time of this outline by 1
            StopHidingClient.showInventoryMap.replace(outlinePos, currentTime - 1);

            // remove outline if it has expired
            if (StopHidingClient.showInventoryMap.get(outlinePos) <= 0) {
                expiredOutlines.add(outlinePos);
            }

            RenderSystem.enableDepthTest();
        }

        // remove any expired outlines here to avoid CME
        StopHidingClient.showInventoryMap.keySet().removeAll(expiredOutlines);
    }
}
