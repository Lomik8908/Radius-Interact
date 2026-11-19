package fluffical.radiusinteract;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

public class RadiusInteract implements ModInitializer {
	public static final String MOD_ID = "radius-interact";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static KeyMapping openConfigKey;
	public static Config config;

	public static boolean isBreaking = false;
	public boolean canBreak = false;
	public TargetBlock target = new TargetBlock();
	// public BlockPos targetBlockPos;
	// public Direction targetDirection;
	// public BlockHitResult targetHitResult;

	public KeyMapping registerKeyEvent(KeyMapping key, EndTick endTick) {
		KeyMapping mapping = KeyBindingHelper.registerKeyBinding(key);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (mapping.consumeClick()) {
				if (client.player == null) return;
				endTick.onEndTick(client);
			}
		});
		return mapping;
	}

	public boolean isExcluded(BlockPos pos, Player player) {
		switch (config.exclusionMode) {
			case 1:
				return player.blockPosition().below().equals(pos);
			case 2:
				return pos.getY() < player.getY();
			case 3:
				return pos.getY() >= player.getY();
		}
		return false;
	}

	public Direction getDirection() {
		switch (config.sideOnly) {
			case 1:
				return Direction.UP;
			case 2:
				return Direction.DOWN;
			case 3:
				return Direction.NORTH;
			case 4:
				return Direction.WEST;
			case 5:
				return Direction.SOUTH;
			case 6:
				return Direction.EAST;
		}
		return null; //Direction.getApproximateNearest(pos);
	}
	static final double digInside = 1d / 64d;
	public Vec3 getClosestPoint(VoxelShape shape, BlockPos pos, Vec3 eyePos) {
		Vec3 closestPoint = null;
		Vec3 relPos = eyePos.subtract(pos.getX(), pos.getY(), pos.getZ());
		for (AABB box : shape.toAabbs()) {
			Vec3 point = new Vec3(
				Math.clamp(relPos.x, box.minX + digInside, box.maxX - digInside),
				Math.clamp(relPos.y, box.minY + digInside, box.maxY - digInside),
				Math.clamp(relPos.z, box.minZ + digInside, box.maxZ - digInside)
			);
			if (closestPoint == null || relPos.distanceToSqr(point) < relPos.distanceToSqr(closestPoint))
				closestPoint = point;
		}
		return closestPoint == null ? null : closestPoint.add(pos.getX(), pos.getY(), pos.getZ());
	}

	public void targetInRange(double range, Vec3 eyePos, Level level, Player player) {
		target.CanInteract = false;
		int radius = (int)Math.ceil(range);
		BlockPos center = BlockPos.containing(eyePos);
		double closestDist = config.farthestFirst ? 0 : range * range;
		for (BlockPos pos : BlockPos.betweenClosed(
			center.offset(-radius, -radius, -radius),
			center.offset(radius, radius, radius)
		)) {
			if (isExcluded(pos, player) || !level.isInWorldBounds(pos)) continue;

			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			
			String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
			if (!config.targets.contains(id)) continue;

			VoxelShape shape = state.getShape(level, pos);
			if (shape.isEmpty()) return;
			Vec3 blockCenter = getClosestPoint(shape, pos, eyePos);//shape.bounds().getCenter().add(pos.getX(), pos.getY(), pos.getZ());
			// Direction direction = getDirection(eyePos.subtract(blockCenter));

			// if (!level.getBlockState(pos.relative(direction)).isAir()) continue;

			// Vec3 blockDirPos = blockCenter.relative(direction, 0.49);
			double distSqr = eyePos.distanceToSqr(blockCenter);

			if (distSqr <= range * range && config.farthestFirst ? (distSqr >= closestDist) : (distSqr <= closestDist)) {
				BlockHitResult result = level.clip(new ClipContext(eyePos, blockCenter, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
				// if (result.getType() != HitResult.Type.BLOCK)
				// 	result = level.clip(new ClipContext(eyePos, blockCenter, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
				Direction gDirection = getDirection();
				if (config.sideAir && !level.getBlockState(pos.relative(gDirection == null ? result.getDirection() : gDirection)).isAir()) continue;
				if (result.getType() == HitResult.Type.BLOCK && pos.equals(result.getBlockPos()) && (gDirection == null || result.getDirection() == gDirection)) {
					closestDist = distSqr;
					target.BlockPosition = pos.immutable();
					target.Direction = result.getDirection();
					target.HitResult = result;
					target.ClosestPoint = blockCenter;
					target.CanInteract = true;
				}
			}
		}
	}

	@Override
	public void onInitialize() {
		config = ConfigManager.loadConfig();
		openConfigKey = registerKeyEvent(new KeyMapping("key.radius-interact.config", InputConstants.Type.KEYSYM, InputConstants.KEY_H, "key.category.radius-interact.keys"), client -> {
			if (client.screen instanceof ConfigScreen)
				client.setScreen(null);
			else
				client.setScreen(new ConfigScreen());
		});

		registerKeyEvent(new KeyMapping("key.radius-interact.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_G, "key.category.radius-interact.keys"), client -> {
			isBreaking = !isBreaking;
			client.gui.setOverlayMessage(getTranslation("enabled."+String.valueOf(isBreaking)), false);
		});
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.level != null && !client.isPaused() && isBreaking) {
				Vec3 eyePos = client.player.getEyePosition();
				double reach = client.player.blockInteractionRange();
				if (config.maxRange > 0)
					reach = Math.min(reach, config.maxRange);
				if (!config.useBlock && target.CanInteract && !client.level.getBlockState(target.BlockPosition).isAir() && (!config.sideAir || client.level.getBlockState(target.BlockPosition.relative(target.Direction)).isAir()) && eyePos.distanceToSqr(target.ClosestPoint) <= reach * reach) {
					if (canBreak) {
						client.gameMode.continueDestroyBlock(target.BlockPosition, target.Direction);
						client.player.swing(InteractionHand.MAIN_HAND);
					}
				}
				else {
					targetInRange(reach, eyePos, client.level, client.player);
					if (target.CanInteract) {
						if (config.useBlock) {
							InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, target.HitResult);
							if (result.consumesAction()) 
								client.player.swing(InteractionHand.MAIN_HAND);
						} else {
							canBreak = client.gameMode.startDestroyBlock(target.BlockPosition, target.Direction);
							if (canBreak)
								client.player.swing(InteractionHand.MAIN_HAND);
						}
					}
				}
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			isBreaking = false;
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			ConfigManager.saveConfig(config);
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Component getTranslation(String translation) {
		return Component.translatable(MOD_ID+"."+translation);
	}
	public static Component getTranslation(String translation, Object... args) {
		return Component.translatable(MOD_ID+"."+translation, args);
	}
}
