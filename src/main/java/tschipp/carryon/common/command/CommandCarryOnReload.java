package tschipp.carryon.common.command;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import tschipp.carryon.common.config.CarryOnConfig;
import tschipp.carryon.common.scripting.ScriptReader;

public class CommandCarryOnReload extends CommandBase
{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{

		if (CarryOnConfig.settings.useScripts)
		{
			ScriptReader.reloadScripts();
			sender.sendMessage(new TextComponentString("Successfully reloaded scripts!"));
		}
		else
			sender.sendMessage(new TextComponentString("To use custom Carry On scripts, enable them in the config!"));

	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{

		if (args.length > 0)
		{
			if (args.length == 1)
			{
				return CommandBase.getListOfStringsMatchingLastWord(args, "reload");
			}

			else
			{
				return Collections.<String>emptyList();
			}

		}

		return Collections.<String>emptyList();

	}

	@Override
	public String getName()
	{
		return "reloadscripts";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/reloadscripts";
	}
}
