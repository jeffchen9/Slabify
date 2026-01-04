/*
 *     A plugin for WorldPainter that adds slab and stair detail to terrain.
 *     Copyright (C) 2025  Jeff Chen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify;

import org.pepsoft.minecraft.Material;

public final class Constants {

  public static final String MC_NAMESPACE = "minecraft";
  public static final String CQ_NAMESPACE = "conquest";
  public static final String DEFAULT_BLOCK = Material.STONE.simpleName;
  public static final double LOSS_EXPONENT = 1; // 2 for MSE, 1 for MAE
  public static final int TILE_PADDING = 2;

  public static final String CQ_HINGE = "hinge";
  public static final String CQ_LAYER = "layer";

  private Constants() {
    // Prevent instantiation
  }
}
