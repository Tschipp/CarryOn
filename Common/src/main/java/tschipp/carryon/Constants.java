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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tschipp.carryon.common.config.CarryConfig;

public class Constants {

	public static final String MOD_ID = "carryon";
	public static final String MOD_NAME = "Carry On";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final CarryConfig.Common COMMON_CONFIG = new CarryConfig.Common();
	public static final CarryConfig.Client CLIENT_CONFIG = new CarryConfig.Client();

	public static final ResourceLocation PACKET_ID_KEY_PRESSED =  new ResourceLocation(Constants.MOD_ID, "key_pressed");
	public static final ResourceLocation PACKET_ID_START_RIDING =  new ResourceLocation(Constants.MOD_ID, "start_riding");
	public static final ResourceLocation PACKET_ID_SYNC_SCRIPTS =  new ResourceLocation(Constants.MOD_ID, "sync_scripts");

}