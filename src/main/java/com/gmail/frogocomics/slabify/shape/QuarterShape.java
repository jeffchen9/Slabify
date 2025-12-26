package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.pepsoft.minecraft.Material;

import java.util.List;
import java.util.Optional;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class QuarterShape extends Shape {

  public static final String NAME = "quarter";

  public QuarterShape() {
    super("Quarter", NAME, new Options[]{Options.DISABLE, Options.QUARTERS, Options.HALVES}, false, 4);
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {
    return Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i) {
    // TODO
    return Material.AIR;
  }
}
