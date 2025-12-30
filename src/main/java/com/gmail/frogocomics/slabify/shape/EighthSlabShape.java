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
public class EighthSlabShape extends Shape {

  public static final String NAME = "eighth_slab";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix southShape;
  private final Matrix eastShape;
  private final Matrix northShape;
  private final Matrix westShape;


  public EighthSlabShape() {
    super("Eighth Slab", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 2);

    southShape = new Matrix(new float[][]{
            {1, 0},
            {0, 0}
    });
    eastShape = new Matrix(new float[][]{
            {0, 1},
            {0, 0}
    });
    northShape = new Matrix(new float[][]{
            {0, 0},
            {0, 1}
    });
    westShape = new Matrix(new float[][]{
            {0, 0},
            {1, 0}
    });
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

    assert resolution >= getMinimumResolution(null);

    if (selectedOption == Options.ENABLE) {

      // Upscale shapes if needed
      Matrix upscaledEastShape = southShape.upscale(resolution / 2);
      Matrix upscaledSouthShape = eastShape.upscale(resolution / 2);
      Matrix upscaledWestShape = northShape.upscale(resolution / 2);
      Matrix upscaledNorthShape = westShape.upscale(resolution / 2);

      return Optional.of(List.of(upscaledEastShape, upscaledSouthShape, upscaledWestShape, upscaledNorthShape));
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
        materialName = baseMaterial.name + "_eighth_slab";
      }

      Material[] slabMaterials = new Material[12];

      slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "south", Constants.MC_HALF, "bottom");
      slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_HALF, "bottom");
      slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "north", Constants.MC_HALF, "bottom");
      slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_HALF, "bottom");

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
