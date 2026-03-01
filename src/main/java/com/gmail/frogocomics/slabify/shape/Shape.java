/*
 *     A plugin for WorldPainter that adds additional shape detail to terrain.
 *     Copyright (C) 2026  Jeff Chen
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;

import java.util.List;
import java.util.Optional;

/**
 * Abstract class representing a block "shape" like stairs and slabs; a seperate class is needed to account for the
 * complex shapes provided by Conquest.
 */
public abstract class Shape {

  private final String displayName;
  private final String name;
  private final List<Options> availableOptions;
  private final boolean vanilla;
  private final int minResolution;
  private final boolean supportsStacking;
  private final boolean alwaysActive;
  private final Options defaultOption;

  public Shape(String displayName, String name, Options[] availableOptions, boolean vanilla, int minResolution,
               boolean supportsStacking, boolean alwaysActive, Options defaultOption) {
    this.displayName = displayName;
    this.name = name;
    this.availableOptions = List.of(availableOptions);
    this.vanilla = vanilla;
    this.minResolution = minResolution;
    this.supportsStacking = supportsStacking;
    this.alwaysActive = alwaysActive;
    this.defaultOption = defaultOption;
  }

  /**
   * Get the name (not the display name) of the shape.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the available options as an immutable list.
   *
   * @return the available options.
   */
  public List<Options> getAvailableOptions() {
    return availableOptions;
  }

  /**
   * Check if the shape is vanilla. If not, the shape is available only in Conquest.
   *
   * @return {@code true} if the shape is vanilla.
   */
  public boolean isVanilla() {
    return vanilla;
  }

  /**
   * Get the minimum multiplier needed for upscaling. Vanilla blocks require 2x upscaling. However, some Conquest
   * shapes require more upscaling.
   *
   * @param option the selected option; this is usually not relevant.
   * @return the minimum upscaling needed.
   */
  public int getMinResolution(@Nullable Options option) {
    return minResolution;
  }

  /**
   * Get whether the shape supports stacking. If it does not support stacking, it will only be used for the top shape
   * in a stack.
   *
   * @return {@code true} if the shape supports stacking.
   */
  public boolean supportsStacking() {
    return supportsStacking;
  }

  /**
   * Get whether the shape is always active regardless of whether it is available in the mappings for a material. This
   * could be so if the shape does not rely on the base material.
   *
   * @return {@code true} if the shape is always active.
   */
  public boolean isAlwaysActive() {
    return alwaysActive;
  }

  /**
   * Get the default option.
   *
   * @return the default option.
   */
  public Options getDefaultOption() {
    return defaultOption;
  }

  /**
   * Place the shape in a chunk.
   *
   * @param worldX       the global X coordinate.
   * @param worldY       the global Y coordinate.
   * @param worldZ       the global Z coordinate.
   * @param localX       the chunk X coordinate.
   * @param localZ       the chunk Z coordinate.
   * @param chunk        the chunk.
   * @param material     the material for the shape.
   * @param baseMaterial the base material.
   */
  public void place(int worldX, int worldY, int worldZ, int localX, int localZ, Chunk chunk, Material material, Material baseMaterial) {
    chunk.setMaterial(localX, worldY, localZ, material);
  }

  /**
   * Get the display name of the shape.
   *
   * @return the display name.
   */
  @Override
  public String toString() {
    return displayName;
  }

  /**
   * Get the specific matrices pertaining to the shape.
   *
   * @param selectedOption the selected option.
   * @param resolution     the resolution of the matrices.
   * @return a list of the matrices, if available. Will return {@link Optional#empty()} if {@code selectedOption}
   * equals to {@link Options#DISABLE}.
   */
  public abstract Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution);

  /**
   * Get a material connected to a base material.
   *
   * @param baseMaterial the base material.
   * @param i            the index of the item in the material list for the shape.
   * @param option       the selected option, which is only needed for some shapes.
   * @return the material, which will be {@link Material#AIR} if the material cannot be found.
   */
  public abstract Material getMaterial(Material baseMaterial, int i, @Nullable Options option);

  /**
   * Represents potential options of the shape. For a given shape, not all options may be available. This is provided
   * in {@link Shape#getAvailableOptions()}.
   */
  public enum Options {
    DISABLE("Disable"),
    ENABLE("Enable"),
    EIGHTHS("Eighths"),   // Conquest only
    QUARTERS("Quarters"), // Conquest only
    HALVES("Halves");     // Conquest only

    private final String displayText;

    Options(String displayText) {
      this.displayText = displayText;
    }

    @Override
    public String toString() {
      return displayText;
    }
  }
}
