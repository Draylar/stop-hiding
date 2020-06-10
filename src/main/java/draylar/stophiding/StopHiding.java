package draylar.stophiding;

import draylar.stophiding.config.StopHidingConfig;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StopHiding implements ModInitializer {

	public static final StopHidingConfig CONFIG = AutoConfig.register(StopHidingConfig.class, JanksonConfigSerializer::new).getConfig();

	public static final Identifier SERVER_PACKET = new Identifier("stophiding", "notifyserver");
	public static final Identifier CLIENT_PACKET = new Identifier("stophiding", "notifyclient");

	@Override
	public void onInitialize() {
		registerServerPacketHandlers();
	}

	private void registerServerPacketHandlers() {
		// receive packet sent from client to find nearby inventories with item
		ServerSidePacketRegistry.INSTANCE.register(SERVER_PACKET, (context, packetByteBuf) -> {
			String searchIdentifier = packetByteBuf.readString(32767);

			context.getTaskQueue().execute(() -> {
				Item item = Registry.ITEM.get(new Identifier(searchIdentifier));

				if (item != Items.AIR) {
					ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
					ServerWorld world = (ServerWorld) player.world;
					List<Pair<BlockPos, Integer>> foundPositions = findItemInNearbyInventories(world, player.getBlockPos(), CONFIG.searchRange, item);

					// count total items found
					int totalItems = 0;
					for (Pair<BlockPos, Integer> blockPosIntegerPair : foundPositions) {
						totalItems += blockPosIntegerPair.getRight();
					}

					if (!foundPositions.isEmpty()) {
						player.closeContainer();

						// get long blockpos array
						ArrayList<Long> longFoundPositions = new ArrayList<>();
						foundPositions.forEach(pos -> longFoundPositions.add(pos.getLeft().asLong()));

						// store block positions in bytebuf
						PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
						byteBuf.writeInt(totalItems);
						byteBuf.writeInt(longFoundPositions.size());
						longFoundPositions.forEach(byteBuf::writeLong);

						// send results to server
						CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CLIENT_PACKET, byteBuf);
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
					}
				}
			});
		});
	}

	private List<Pair<BlockPos, Integer>> findItemInNearbyInventories(World world, BlockPos origin, int radius, Item item) {
		List<Pair<BlockPos, Integer>> validPositions = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = new BlockPos(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
					int positionCount = getItemCountAt(world, pos, item);

					if (getItemCountAt(world, pos, item) > 0) {
						validPositions.add(new Pair<>(pos, positionCount));
					}
				}
			}
		}

		return validPositions;
	}

	private int getItemCountAt(World world, BlockPos pos, Item item) {
		BlockEntity blockEntity = world.getBlockEntity(pos);

		// check if block entity can have an inventory
		if (blockEntity instanceof Inventory) {
			Inventory inventory = (Inventory) blockEntity;

			// storing number of items at this position
			int count = 0;

			// iterate over inventory stacks
			for (int i = 0; i < inventory.getInvSize(); i++) {
				ItemStack stack = inventory.getInvStack(i);

				if (stack.getItem() == item) {
					count += stack.getCount();
				}
			}

			return count;
		}

		return 0;
	}
}
