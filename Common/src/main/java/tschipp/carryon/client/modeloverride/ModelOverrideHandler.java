package tschipp.carryon.client.modeloverride;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import tschipp.carryon.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelOverrideHandler
{
	private static List<ModelOverride> OVERRIDES = new ArrayList<>();

	public static void initModelOverrides()
	{
		OVERRIDES.clear();

		for(String ov : Constants.CLIENT_CONFIG.modelOverrides)
		{
			addFromString(ov);
		}
	}

	public static Optional<ModelOverride> getModelOverride(BlockState state, @Nullable CompoundTag tag)
	{
		for(ModelOverride ov : OVERRIDES)
		{
			if(ov.matches(state, tag))
				return Optional.of(ov);
		}
		return Optional.empty();
	}

	public static void addFromString(String str)
	{
		DataResult<ModelOverride> res = ModelOverride.of(str);
		if(res.result().isPresent())
		{
			ModelOverride override = res.result().get();
			OVERRIDES.add(override);
		}
		else
		{
			Constants.LOG.debug("Error while parsing ModelOverride: " + res.error().get().message());
		}
	}

}
