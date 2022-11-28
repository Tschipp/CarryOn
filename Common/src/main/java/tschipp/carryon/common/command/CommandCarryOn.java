package tschipp.carryon.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.Collection;
import java.util.Collections;

public class CommandCarryOn
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("carryon")

				.then(Commands.literal("debug").executes(cmd -> handleDebug(cmd.getSource())))

				.then(Commands.literal("clear").executes(cmd -> handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()))))

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermission(2)).executes(cmd -> handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target")))))

		;

		dispatcher.register(builder);

	}

	private static int handleDebug(CommandSourceStack source)
	{
		try
		{
			if (source.getEntityOrException() != null)
			{
				ServerPlayer player = source.getPlayerOrException();

				CarryOnData carry = CarryOnDataManager.getCarryData(player);
				if (carry.isCarrying(CarryType.BLOCK))
				{
					log(source,"Block: " + carry.getBlock().getBlock());
					log(source,"BlockState: " + carry.getBlock());
					log(source,"NBT: " + carry.getNbt());

					//TODO
//					if (ModelOverridesHandler.hasCustomOverrideModel(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main)))
//						source.sendSuccess(Component.literal("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemCarryonBlock.getBlockState(main), ItemCarryonBlock.getTileData(main))), true);

//					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonBlock.getBlockState(main)))
//						source.sendSuccess(Component.literal("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonBlock.getBlockState(main))), true);

					return 1;
				}
				else if (carry.isCarrying(CarryType.ENTITY))
				{
					log(source,"Entity: " + carry.getEntity(player.level));
					log(source,"Entity Name: " + carry.getEntity(player.level).getType());
					log(source,"NBT: " + carry.getNbt());

//					if (CustomPickupOverrideHandler.hasSpecialPickupConditions(ItemCarryonEntity.getEntity(main, player.level)))
//						source.sendSuccess(Component.literal("Custom Pickup Condition: " + CustomPickupOverrideHandler.getPickupCondition(ItemCarryonEntity.getEntity(main, player.level))), true);

					return 1;
				}
				else if(carry.isCarrying(CarryType.PLAYER))
				{
					log(source, "Carrying Player.");
				}
			}

		}
		catch (CommandSyntaxException e)
		{
		}

		return 0;
	}

	private static int handleClear(CommandSourceStack source, Collection<ServerPlayer> players)
	{
		int cleared = 0;
		for (ServerPlayer player : players)
		{

			CarryOnData carry = CarryOnDataManager.getCarryData(player);
			carry.clear();
			CarryOnDataManager.setCarryData(player, carry);

			cleared++;
		}

		if (cleared != 1)
			source.sendSuccess(Component.literal("Cleared " + cleared + " Items!"), true);
		else
			source.sendSuccess(Component.literal("Cleared " + cleared + " Item!"), true);

		return 1;
	}

	private static void log(CommandSourceStack source, String toLog)
	{
		source.sendSuccess(Component.literal(toLog), true);
		Constants.LOG.info(toLog);
	}
}
