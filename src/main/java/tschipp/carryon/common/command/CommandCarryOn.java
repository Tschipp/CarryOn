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
					return handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()));
				}))

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermission(2)).executes((cmd) -> {
					return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
				})))

		;

		dispatcher.register(builder);

	}

	private static int handleDebug(CommandSource source)
	{
		try
		{
			if (source.getEntityOrException() != null)
			{
				ServerPlayerEntity player = source.getPlayerOrException();

				ItemStack main = player.getMainHandItem();
				if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemTile)
				{
					source.sendSuccess(new StringTextComponent("Block: " + ItemCarryonBlock.getBlock(main)), true);
					source.sendSuccess(new StringTextComponent("BlockState: " + ItemCarryonBlock.getBlockState(main)), true);
					source.sendSuccess(new StringTextComponent("ItemStack: " + ItemCarryonBlock.getItemStack(main)), true);

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main)))
						source.sendSuccess(new StringTextComponent("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main))), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(main)))
						source.sendSuccess(new StringTextComponent("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(main))), true);

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
					source.sendSuccess(new StringTextComponent("Entity: " + ItemCarryonEntity.getEntity(main, player.level)), true);
					source.sendSuccess(new StringTextComponent("Entity Name: " + ItemCarryonEntity.getEntityName(main)), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonEntity.getEntity(main, player.level)))
						source.sendSuccess(new StringTextComponent("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonEntity.getEntity(main, player.level))), true);

					CarryOn.LOGGER.info("Entity: " + ItemCarryonEntity.getEntity(main, player.level));
					CarryOn.LOGGER.info("Entity Name: " + ItemCarryonEntity.getEntityName(main));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonEntity.getEntity(main, player.level)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonEntity.getEntity(main, player.level)));

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
			cleared += player.inventory.clearOrCountMatchingItems(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile, 64, player.inventoryMenu.getCraftSlots()); // TODO
			cleared += player.inventory.clearOrCountMatchingItems(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity, 64, player.inventoryMenu.getCraftSlots());

			CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CarrySlotPacket(9, player.getId()));

			if (cleared != 1)
				source.sendSuccess(new StringTextComponent("Cleared " + cleared + " Items!"), true);
			else
				source.sendSuccess(new StringTextComponent("Cleared " + cleared + " Item!"), true);

			return 1;
		}

		return 0;
	}
}
