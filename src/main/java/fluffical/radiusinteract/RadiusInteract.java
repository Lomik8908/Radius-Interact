package fluffical.radiusinteract;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

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
	public BlockPos targetBlockPos;
	public Direction targetDirection;
	public BlockHitResult targetHitResult;

	public KeyMapping registerKeyEvent(KeyMapping key, EndTick endTick) {
		KeyMapping mapping = KeyMappingHelper.registerKeyMapping(key);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (mapping.consumeClick()) {
				if (client.player == null) return;
				endTick.onEndTick(client);
			}
		});
		return mapping;
	}

	public void targetInRange(double range, Vec3 pos3, Level level, Block block) {
		targetBlockPos = null;
		targetDirection = null;
		targetHitResult = null;
		int radius = (int)Math.ceil(range);
		BlockPos center = BlockPos.containing(pos3);
		double closestDist = config.farthestFirst ? 0 : range * range;
		for (BlockPos pos : BlockPos.betweenClosed(
			center.offset(-radius, -radius, -radius),
			center.offset(radius, radius, radius)
		)) {
			Vec3 blockCenter = Vec3.atCenterOf(pos);
			double distSqr = pos3.distanceToSqr(blockCenter);
			if (distSqr <= range * range && config.farthestFirst ? (distSqr >= closestDist) : (distSqr <= closestDist)) {
				String id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
				if (!config.targets.contains(id)) continue;
				Vec3 diff = pos3.subtract(blockCenter);
				Direction dir = Direction.getApproximateNearest(diff);
				BlockHitResult result = level.clip(new ClipContext(pos3, blockCenter.relative(dir, 0.49), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
				if (result.getType() == HitResult.Type.BLOCK && pos.equals(result.getBlockPos())) {
					closestDist = distSqr;
					targetBlockPos = pos.immutable();
					targetDirection = dir;
					targetHitResult = result;
				}
			}
		}
	}

	@Override
	public void onInitialize() {
		config = ConfigManager.loadConfig();
		var category = KeyMapping.Category.register(id("keys"));
		openConfigKey = registerKeyEvent(new KeyMapping("key.radius-interact.config", InputConstants.Type.KEYBOARD, InputConstants.KEY_APOSTROPHE, category), client -> {
			if (client.gui.screen() instanceof ConfigScreen)
				client.gui.setScreen(null);
			else
				client.gui.setScreen(new ConfigScreen());
		});

		registerKeyEvent(new KeyMapping("key.radius-interact.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_G, category), client -> {
			isBreaking = !isBreaking;
			client.player.sendOverlayMessage(getTranslation("enabled."+String.valueOf(isBreaking)));
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal(MOD_ID)
				.then(ClientCommands.literal("clearTargets").executes(context -> {
					config.targets.clear();
					context.getSource().sendFeedback(getTranslation("targets.clear"));
					return 1;
				}))
				.then(ClientCommands.literal("listTargets").executes(context -> {
					if (config.targets.size() > 0)
						context.getSource().sendFeedback(getTranslation("targets.list", String.join(", ", config.targets)));
					else
						context.getSource().sendFeedback(getTranslation("targets.listempty"));
					return 1;
				}))
				.then(ClientCommands.literal("addTarget").then(ClientCommands.argument("block", BlockStateArgument.block(registryAccess)).executes(context -> {
					BlockInput value = context.getArgument("block", BlockInput.class);
					String id = BuiltInRegistries.BLOCK.getKey(value.getState().getBlock()).toString();
					if (!config.targets.contains(id)) {
						config.targets.add(id);
						context.getSource().sendFeedback(getTranslation("targets.add", id));
					} else {
						context.getSource().sendError(getTranslation("targets.added", id));
					}
					return 1;
				})))
				.then(ClientCommands.literal("removeTarget").then(ClientCommands.argument("block", BlockStateArgument.block(registryAccess)).executes(context -> {
					BlockInput value = context.getArgument("block", BlockInput.class);
					String id = BuiltInRegistries.BLOCK.getKey(value.getState().getBlock()).toString();
					if (config.targets.contains(id)) {
						config.targets.remove(id);
						context.getSource().sendFeedback(getTranslation("targets.removed", id));
					} else {
						context.getSource().sendError(getTranslation("targets.isnt", id));
					}
					return 1;
				}))).executes(context -> {
					context.getSource().sendError(getTranslation("config", config.useBlock, config.farthestFirst, config.maxRange));
					return 1;
				})
			);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.level != null && !client.isPaused() && isBreaking) {
				Vec3 eyePos = client.player.getEyePosition();
				double reach = client.player.blockInteractionRange();
				if (config.maxRange > 0)
					reach = Math.min(reach, config.maxRange);
				if (!config.useBlock && targetBlockPos != null && !client.level.getBlockState(targetBlockPos).isAir() && eyePos.distanceToSqr(Vec3.atCenterOf(targetBlockPos)) <= reach * reach) {
					if (canBreak) {
						client.gameMode.continueDestroyBlock(targetBlockPos, targetDirection);
						client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
					}
				}
				else {
					targetInRange(reach, eyePos, client.level, Blocks.DIRT);
					if (targetBlockPos != null && targetDirection != null) {
						if (config.useBlock) {
							InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, targetHitResult);
							if (result.consumesAction())
								client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
						} else {
							canBreak = client.gameMode.startDestroyBlock(targetBlockPos, targetDirection);
							if (canBreak)
								client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
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

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Component getTranslation(String translation) {
		return Component.translatable(MOD_ID+"."+translation);
	}
	public static Component getTranslation(String translation, Object... args) {
		return Component.translatable(MOD_ID+"."+translation, args);
	}
}
