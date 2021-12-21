package tschipp.carryon.common.scripting;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import tschipp.carryon.CarryOn;
import tschipp.carryon.network.client.ScriptReloadPacket;

@EventBusSubscriber(modid = CarryOn.MODID, bus = Bus.FORGE)
public class ScriptReloadListener extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public ScriptReloadListener()
	{
		super(GSON, "carryon/scripts");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler)
	{
		ScriptReader.OVERRIDES.clear();

		objects.forEach((path, jsonElem) -> {
			CarryOnOverride override = new CarryOnOverride(jsonElem, path);
			if(!override.isInvalid)
				ScriptReader.OVERRIDES.put(override.hashCode(), override);
		});

		if (EffectiveSide.get().isServer() && ServerLifecycleHooks.getCurrentServer() != null)
		{
			CarryOn.network.send(PacketDistributor.ALL.noArg(), new ScriptReloadPacket(ScriptReader.OVERRIDES.values()));
		}
	}
	
	@SubscribeEvent
	public static void onDatapackRegister(AddReloadListenerEvent event)
	{
		event.addListener(new ScriptReloadListener());
	}
}
