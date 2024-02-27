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

package tschipp.carryon;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import tschipp.carryon.config.forge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CarryOnForge {

    public static SimpleChannel network;

    public CarryOnForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        // Use Forge to bootstrap the Common mod.
        CarryOnCommon.registerConfig();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ConfigLoaderImpl.initialize();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        network = ChannelBuilder.named(new ResourceLocation(Constants.MOD_ID, "carryonpackets")).simpleChannel();

        CarryOnCommon.registerServerPackets();
        CarryOnCommon.registerClientPackets();
    }

}