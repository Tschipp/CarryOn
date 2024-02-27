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

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import tschipp.carryon.config.neoforge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
public class CarryOnNeoForge {

    public CarryOnNeoForge(IEventBus bus) {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        // Use Forge to bootstrap the Common mod.
        CarryOnCommon.registerConfig();
        bus.addListener(this::setup);
        bus.addListener(this::registerPackets);

        ConfigLoaderImpl.initialize();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    public void registerPackets(final RegisterPayloadHandlerEvent event) {

        final IPayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                .versioned("1.0.0");

        CarryOnCommon.registerServerPackets(registrar);
        CarryOnCommon.registerClientPackets(registrar);
    }

}