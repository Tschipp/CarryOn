/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.Constants;
import tschipp.carryon.client.modeloverride.ModelOverride;
import tschipp.carryon.client.modeloverride.ModelOverrideHandler;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.common.pickupcondition.PickupCondition;
import tschipp.carryon.common.pickupcondition.PickupConditionHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class CommandCarryOn
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("carryon")

				.then(Commands.literal("debug").executes(cmd -> handleDebug(cmd.getSource())))

				.then(Commands.literal("clear").executes(cmd -> handleClear(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()))))

				.then(Commands.literal("clear").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermission(2)).executes(cmd -> handleClear(cmd.getSource(), EntityArgument.getPlayers(cmd, "target")))))

				.then(Commands.literal("place").requires(src -> src.hasPermission(2)).executes(cmd -> handlePlace(cmd.getSource(), Collections.singleton(cmd.getSource().getPlayerOrException()))))

				.then(Commands.literal("place").then(Commands.argument("target", EntityArgument.players()).requires(src -> src.hasPermission(2)).executes(cmd -> handlePlace(cmd.getSource(), EntityArgument.getPlayers(cmd, "target")))))

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
					BlockState block = carry.getBlock();
					log(source,"Block: " + block.getBlock());
					log(source,"BlockState: " + block);
					log(source,"NBT: " + carry.getNbt());

					Optional<ModelOverride> ov = ModelOverrideHandler.getModelOverride(block, carry.getContentNbt());
					if(ov.isPresent())
						log(source, "Override Model: " + ov.get().getRenderObject());

					Optional<PickupCondition> cond = PickupConditionHandler.getPickupCondition(block);
					if(cond.isPresent())
						log(source, "Custom Pickup Condition: " + cond.get().getCondition());


					return 1;
				}
				else if (carry.isCarrying(CarryType.ENTITY))
				{
					Entity entity = carry.getEntity(player.level());
					log(source,"Entity: " + entity);
					log(source,"Entity Name: " + entity.getType());
					log(source,"NBT: " + carry.getNbt());

					Optional<PickupCondition> cond = PickupConditionHandler.getPickupCondition(entity);
					if(cond.isPresent())
						log(source, "Custom Pickup Condition: " + cond.get().getCondition());

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
		int finalCleared = cleared;

		if (cleared != 1) {
			source.sendSuccess(() -> Component.literal("Cleared " + finalCleared + " Items!"), true);
		}
		else {
			source.sendSuccess(() -> Component.literal("Cleared " + finalCleared + " Item!"), true);
		}

		return 1;
	}

	private static int handlePlace(CommandSourceStack source, Collection<ServerPlayer> players)
	{
		int cleared = 0;
		for (ServerPlayer player : players)
		{
			PlacementHandler.placeCarried(player);
			cleared++;
		}
		int finalCleared = cleared;

		if (cleared != 1) {
			source.sendSuccess(() -> Component.literal("Placed " + finalCleared + " Items!"), true);
		}
		else
			source.sendSuccess(() -> Component.literal("Placed " + finalCleared + " Item!"), true);

		return 1;
	}

	private static void log(CommandSourceStack source, String toLog)
	{
		source.sendSuccess(() -> Component.literal(toLog), true);
		Constants.LOG.info(toLog);
	}
}
