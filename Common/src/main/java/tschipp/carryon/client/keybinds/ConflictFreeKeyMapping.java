package tschipp.carryon.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;

public class ConflictFreeKeyMapping extends KeyMapping
{
	public ConflictFreeKeyMapping(String $$0, int $$1, String $$2)
	{
		super($$0, $$1, $$2);
	}

	public ConflictFreeKeyMapping(String $$0, Type $$1, int $$2, String $$3)
	{
		super($$0, $$1, $$2, $$3);
	}

	@Override
	public boolean same(KeyMapping $$0)
	{
		return false;
	}
}
