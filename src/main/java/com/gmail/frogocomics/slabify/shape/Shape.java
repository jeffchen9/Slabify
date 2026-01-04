package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
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

  public Shape(String displayName, String name, Options[] availableOptions, boolean vanilla, int minResolution) {
    this.displayName = displayName;
    this.name = name;
    this.availableOptions = List.of(availableOptions);
    this.vanilla = vanilla;
    this.minResolution = minResolution;
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
   * @return <code>true</code> if the shape is vanilla.
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
