package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Material;

import java.util.*;

import static org.pepsoft.minecraft.Constants.MC_HALF;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class SlabShape extends Shape {

  public static final String NAME = "slab";

  private final Map<String, Material> materials = new HashMap<>();
  private final Matrix shape;

  public SlabShape() {
    super("Slab", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 1);
    shape = Matrix.of(new float[][]{{0.5f}});
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

    return selectedOption == Options.ENABLE ? Optional.of(Collections.singletonList(shape.upscale(resolution / getMinResolution(null)))) : Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    if (materials.containsKey(baseMaterial.name)) {
      return materials.get(baseMaterial.name);
    }

    String materialName = Shapes.getMaterial(this, baseMaterial.name);

    if (materialName == null) {
      return Material.AIR;
    }

    Material newMaterial = Material.get(materialName, MC_HALF, "bottom");
    materials.put(baseMaterial.name, newMaterial);

    return newMaterial;
  }
}
