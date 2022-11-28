package tschipp.carryon.scripting;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import tschipp.carryon.Constants;
import tschipp.carryon.common.scripting.ScriptReloadListener;

public class IdentifiableScriptReloadListener extends ScriptReloadListener implements IdentifiableResourceReloadListener
{
	@Override
	public ResourceLocation getFabricId()
	{
		return new ResourceLocation(Constants.MOD_ID, "carryon_scripts");
	}
}
