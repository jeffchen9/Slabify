package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;

import java.util.*;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class CornerSlabShape extends Shape {

  public static final String NAME = "corner_slab";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix shape;

  public CornerSlabShape() {
    super("Corner Slab", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 2);
    shape = new Matrix(new float[][]{
            {1, 1},
            {1, 0}
    });
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

    assert resolution >= getMinimumResolution(null);

    if (selectedOption == Options.ENABLE) {
      // Upscale shapes if needed
      Matrix upscaledShape = shape.upscale(resolution / 2);

      List<Matrix> shapes = new ArrayList<>();

      int[] angles = {90, 180, 270};

      shapes.add(upscaledShape);
      for (int angle : angles) {
        shapes.add(upscaledShape.rotate(angle));
      }

      return Optional.of(shapes);
    }

    return Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    if (!materials.containsKey(baseMaterial.name)) {
      // Create materials if does not exist
      String materialName = Shapes.getMaterial(this, baseMaterial.name);

      if (materialName == null) {
        // Default
        materialName = baseMaterial.name + "_corner_slab";
      }

      Material[] slabMaterials = new Material[4];

      slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "south", Constants.MC_HALF, "bottom");
      slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_HALF, "bottom");
      slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "north", Constants.MC_HALF, "bottom");
      slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_HALF, "bottom");

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
