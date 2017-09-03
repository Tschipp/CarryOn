package tschipp.carryon.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import tschipp.carryon.CarryOn;
import tschipp.carryon.common.handler.ModelOverridesHandler;
import tschipp.carryon.common.handler.RegistrationHandler;
import tschipp.carryon.common.item.ItemEntity;
import tschipp.carryon.common.item.ItemTile;
import tschipp.carryon.network.client.CarrySlotPacket;

public class CommandCarryOn extends CommandBase implements ICommand
{

	private final List names;

	public CommandCarryOn()
	{
		names = new ArrayList();
		names.add("carryon");
	}

	@Override
	public int compareTo(ICommand o)
	{
		return this.getCommandName().compareTo(o.getCommandName());
	}

	@Override
	public String getCommandName()
	{
		return "carryon";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{

		return "/carryon <mode>";
	}

	@Override
	public List<String> getCommandAliases()
	{
		return this.names;
	}
	
	

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			// Handling clear
			if (args[0].toLowerCase().equals("clear"))
			{
				if (sender instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer) sender;

					int cleared = 0;
					cleared += player.inventory.clearMatchingItems(RegistrationHandler.itemTile, 0, 64, null);
					cleared += player.inventory.clearMatchingItems(RegistrationHandler.itemEntity, 0, 64, null);

					CarryOn.network.sendTo(new CarrySlotPacket(9), (EntityPlayerMP) player);

					if (cleared != 1)
						player.addChatMessage(new TextComponentString("Cleared " + cleared + " Items!"));
					else
						player.addChatMessage(new TextComponentString("Cleared " + cleared + " Item!"));
				}

			}
			// Handling debug
			else if (args[0].toLowerCase().equals("debug"))
			{

				if (sender instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer) sender;
					ItemStack main = player.getHeldItemMainhand();
					if (main != null && main.getItem() == RegistrationHandler.itemTile)
					{
						player.addChatMessage(new TextComponentString("Block: " + ItemTile.getBlock(main)));
						player.addChatMessage(new TextComponentString("BlockState: " + ItemTile.getBlockState(main)));
						player.addChatMessage(new TextComponentString("Meta: " + ItemTile.getMeta(main)));
						player.addChatMessage(new TextComponentString("ItemStack: " + ItemTile.getItemStack(main)));
						
						if(ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(main), ItemTile.getTileData(main)))
							player.addChatMessage(new TextComponentString("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(main), ItemTile.getTileData(main))));
									
						CarryOn.LOGGER.info("Block: " + ItemTile.getBlock(main));
						CarryOn.LOGGER.info("BlockState: " + ItemTile.getBlockState(main));
						CarryOn.LOGGER.info("Meta: " + ItemTile.getMeta(main));
						CarryOn.LOGGER.info("ItemStack: " + ItemTile.getItemStack(main));
						
						if(ModelOverridesHandler.hasCustomOverrideModel(ItemTile.getBlockState(main), ItemTile.getTileData(main)))
							CarryOn.LOGGER.info("Override Model: " + ModelOverridesHandler.getOverrideObject(ItemTile.getBlockState(main), ItemTile.getTileData(main)));					
						
					}
					else if(main != null && main.getItem() == RegistrationHandler.itemEntity)
					{
						player.addChatMessage(new TextComponentString("Entity: " + ItemEntity.getEntity(main, server.getEntityWorld())));
						player.addChatMessage(new TextComponentString("Entity Name: " + ItemEntity.getEntityName(main)));

					
						CarryOn.LOGGER.info("Entity: " + ItemEntity.getEntity(main, server.getEntityWorld()));
						CarryOn.LOGGER.info("Entity Name: " + ItemEntity.getEntityName(main));

					
					}
				}
			}
			else
			{
				throw new WrongUsageException(this.getCommandUsage(sender));
			}

		}
		else
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}

	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}

	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{

		if (args.length > 0)
		{
			if (args.length == 1)
			{
				return CommandBase.getListOfStringsMatchingLastWord(args, "debug", "clear");
			}

			else
			{
				return Collections.<String>emptyList();
			}

		}

		return Collections.<String>emptyList();

	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{

		return false;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	

}
