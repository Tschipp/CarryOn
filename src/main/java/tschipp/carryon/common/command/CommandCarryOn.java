package tschipp.carryon.common.command;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.config.Configs.Settings;
import tschipp.carryon.common.handler.CustomPickupOverrideHandler;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.common.scripting.ScriptReader;
import tschipp.carryon.network.client.CarrySlotPacket;
import tschipp.carryon.network.client.ScriptReloadPacket;

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

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.multiplePlayers()).requires(src -> src.hasPermissionLevel(2)).executes((cmd) -> {
					return handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target"));
				})))

				.then(Commands.literal("reload").requires(src -> src.hasPermissionLevel(2)).executes((cmd) -> {
					return handleReload(cmd.getSource());
				}));

		dispatcher.register(builder);

	}

	private static int handleDebug(CommandSource source)
	{
		try
		{
			if (source.assertIsEntity() != null)
			{
				EntityPlayerMP player = source.asPlayer();

				ItemStack main = player.getHeldItemMainhand();
				if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemTile)
				{
					source.sendFeedback(new TextComponentString("Block: " + ItemTile.getBlock(main)), true);
					source.sendFeedback(new TextComponentString("BlockState: " + ItemTile.getBlockState(main)), true);
					source.sendFeedback(new TextComponentString("ItemStack: " + ItemTile.getItemStack(main)), true);

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(main), ItemTile.getTileData(main)))
						source.sendFeedback(new TextComponentString("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(main), ItemTile.getTileData(main))), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemTile.getBlockState(main)))
						source.sendFeedback(new TextComponentString("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemTile.getBlockState(main))), true);

					CarryOn.LOGGER.info("Block: " + ItemTile.getBlock(main));
					CarryOn.LOGGER.info("BlockState: " + ItemTile.getBlockState(main));
					CarryOn.LOGGER.info("ItemStack: " + ItemTile.getItemStack(main));

					if (ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(main), ItemTile.getTileData(main)))
						CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(main), ItemTile.getTileData(main)));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemTile.getBlockState(main)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemTile.getBlockState(main)));

					return 1;
				} else if (!main.isEmpty() && main.getItem() == RegistrationHandler.itemEntity)
				{
					source.sendFeedback(new TextComponentString("Entity: " + ItemEntity.getEntity(main, player.world)), true);
					source.sendFeedback(new TextComponentString("Entity Name: " + ItemEntity.getEntityName(main)), true);

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemEntity.getEntity(main, player.world)))
						source.sendFeedback(new TextComponentString("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemEntity.getEntity(main, player.world))), true);

					CarryOn.LOGGER.info("Entity: " + ItemEntity.getEntity(main, player.world));
					CarryOn.LOGGER.info("Entity Name: " + ItemEntity.getEntityName(main));

					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemEntity.getEntity(main, player.world)))
						CarryOn.LOGGER.info("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemEntity.getEntity(main, player.world)));

					return 1;
				}
			}

		} catch (CommandSyntaxException e)
		{
			return 0;
		}

		return 0;
	}

	private static int handleClear(CommandSource source, Collection<EntityPlayerMP> players)
	{
		for (EntityPlayerMP player : players)
		{
			try
			{
				if (source.assertIsEntity() != null)
				{
					int cleared = 0;
					cleared += player.inventory.clearMatchingItems(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemTile, 64);
					cleared += player.inventory.clearMatchingItems(stack -> !stack.isEmpty() && stack.getItem() == RegistrationHandler.itemEntity, 64);

					CarryOn.network.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) player), new CarrySlotPacket(9, player.getEntityId()));

					if (cleared != 1)
						source.sendFeedback(new TextComponentString("Cleared " + cleared + " Items!"), true);
					else
						source.sendFeedback(new TextComponentString("Cleared " + cleared + " Item!"), true);

					return 1;
				} else
					throw EntityArgument.ONLY_PLAYERS_ALLOWED.create();

			} catch (CommandSyntaxException e)
			{
				return 0;
			}
		}

		return 0;
	}

	private static int handleReload(CommandSource source)
	{
		if (Settings.useScripts.get())
		{
			ScriptReader.reloadScripts();
			CarryOn.network.send(PacketDistributor.ALL.noArg(), new ScriptReloadPacket());

			source.sendFeedback(new TextComponentString("Successfully reloaded scripts!"), true);
		} else
			source.sendErrorMessage(new TextComponentString("To use custom Carry On scripts, enable them in the config!"));

		return 1;
	}
}

