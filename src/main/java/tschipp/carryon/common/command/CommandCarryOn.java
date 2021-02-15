package tschipp.carryon.common.command;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemCarryonBlock;
import tschipp.carryon.common.item.ItemCarryonEntity;
import tschipp.carryon.network.client.CarrySlotPacket;

public class CommandCarryOn
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("carryon")

				.then(Commands.literal("debug").executes((cmd) -> {
					return handleDebug(cmd.getSource());
				}))

				.then(Commands.literal("clear").executes((cmd) -> {
					return handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().asPlayer()));
				}))

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermissionLevel(2)).executes((cmd) -> {
					return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
				})))

		;

		dispatcher.register(builder);

	}

	private static int handleDebug(CommandSource source)
	{
		try
		{
			if (source.assertIsEntity() != null)
			{
				ServerPlayerEntity player = source.asPlayer();

				ItemStack main = player.getHeldItemMainhand();
				if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemTile)
				{
					source.sendFeedback(new StringTextComponent("Block: " + ItemCarryonBlock.getBlock(main)), true);
					source.sendFeedback(new StringTextComponent("BlockState: " + ItemCarryonBlock.getBlockState(main)), true);
					source.sendFeedback(new StringTextComponent("ItemStack: " + ItemCarryonBlock.getItemStack(main)), true);

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main)))
						source.sendFeedback(new StringTextComponent("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main))), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(main)))
						source.sendFeedback(new StringTextComponent("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(main))), true);

					CarryOn.LOGGER.info("Block: " + ItemCarryonBlock.getBlock(main));
					CarryOn.LOGGER.info("BlockState: " + ItemCarryonBlock.getBlockState(main));
					CarryOn.LOGGER.info("ItemStack: " + ItemCarryonBlock.getItemStack(main));

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main)))
						CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main)));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(main)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(main)));

					return 1;
				}
				else if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemEntity)
				{
					source.sendFeedback(new StringTextComponent("Entity: " + ItemCarryonEntity.getEntity(main, player.world)), true);
					source.sendFeedback(new StringTextComponent("Entity Name: " + ItemCarryonEntity.getEntityName(main)), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonEntity.getEntity(main, player.world)))
						source.sendFeedback(new StringTextComponent("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonEntity.getEntity(main, player.world))), true);

					CarryOn.LOGGER.info("Entity: " + ItemCarryonEntity.getEntity(main, player.world));
					CarryOn.LOGGER.info("Entity Name: " + ItemCarryonEntity.getEntityName(main));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonEntity.getEntity(main, player.world)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonEntity.getEntity(main, player.world)));

					return 1;
				}
			}

		}
		catch (CommandSyntaxException e)
		{
			return 0;
		}

		return 0;
	}

	private static int handleClear(CommandSource source, Collection<ServerPlayerEntity> players)
	{
		for (ServerPlayerEntity player : players)
		{
			int cleared = 0;
			cleared += player.inventory.func_234564_a_(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile, 64, player.container.func_234641_j_()); // TODO
			cleared += player.inventory.func_234564_a_(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity, 64, player.container.func_234641_j_());

			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(9, player.getEntityId()));

			if (cleared != 1)
				source.sendFeedback(new StringTextComponent("Cleared " + cleared + " Items!"), true);
			else
				source.sendFeedback(new StringTextComponent("Cleared " + cleared + " Item!"), true);

			return 1;
		}

		return 0;
	}
}
