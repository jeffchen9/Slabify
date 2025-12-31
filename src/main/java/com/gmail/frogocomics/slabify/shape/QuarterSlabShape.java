package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;

import java.util.*;

import static com.gmail.frogocomics.slabify.Constants.CQ_LAYER;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class QuarterSlabShape extends Shape {

  public static final String NAME = "quarter";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix quarter1;
  private final Matrix quarter2;
  private final Matrix quarter3;

  public QuarterSlabShape() {
    super("Quarter Slab", NAME, new Options[]{Options.DISABLE, Options.QUARTERS, Options.HALVES}, false, -1);

    quarter1 = new Matrix(new float[][]{
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    });
    quarter2 = new Matrix(new float[][]{
            {1, 1},
            {0, 0}
    });
    quarter3 = new Matrix(new float[][]{
            {3, 3, 3, 3},
            {3, 3, 3, 3},
            {3, 3, 3, 3},
            {0, 0, 0, 0}
    });
  }

  @Override
  public int getMinimumResolution(Options option) {
    if (option == Options.QUARTERS) {
      return 4;
    } else if (option == Options.HALVES) {
      return 2;
    }

    return -1; // Default value that won't be reached
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

    assert resolution >= getMinimumResolution(selectedOption);

    if (selectedOption != Options.DISABLE) {
      // Upscale shapes if needed
      Matrix upscaled1 = quarter1.upscale(resolution / 4);
      Matrix upscaled2 = quarter2.upscale(resolution / 2);
      Matrix upscaled3 = quarter3.upscale(resolution / 4);

      int[] angles = {90, 180, 270};
      List<Matrix> shapes = new ArrayList<>();

      if (selectedOption == Options.HALVES) {
        shapes.add(upscaled2);
        for (int angle : angles) {
          shapes.add(upscaled2.rotate(angle));
        }

        return Optional.of(shapes);
      } else if (selectedOption == Options.QUARTERS) {
        shapes.add(upscaled1);
        for (int angle : angles) {
          shapes.add(upscaled1.rotate(angle));
        }

        shapes.add(upscaled2);
        for (int angle : angles) {
          shapes.add(upscaled2.rotate(angle));
        }

        shapes.add(upscaled3);
        for (int angle : angles) {
          shapes.add(upscaled3.rotate(angle));
        }

        return Optional.of(shapes);
      }
    }

    return Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    if (!materials.containsKey(baseMaterial.name)) {
      // Create materials if does not exist
      String materialName = Shapes.getMaterial(this, baseMaterial.name);

      System.out.println(option);

      if (materialName == null) {
        // Default
        materialName = baseMaterial.name + "_quarter_slab";
      }

      if (option == Options.HALVES) {
        Material[] slabMaterials = new Material[4];
        slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "2", Constants.MC_HALF, "bottom");

        materials.put(baseMaterial.name, slabMaterials);
      } else if (option == Options.QUARTERS) {
        Material[] slabMaterials = new Material[12];

        slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "1", Constants.MC_HALF, "bottom");
        slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "1", Constants.MC_HALF, "bottom");
        slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "1", Constants.MC_HALF, "bottom");
        slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "1", Constants.MC_HALF, "bottom");

        slabMaterials[4] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[5] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[6] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "2", Constants.MC_HALF, "bottom");
        slabMaterials[7] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "2", Constants.MC_HALF, "bottom");

        slabMaterials[8] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "3", Constants.MC_HALF, "bottom");
        slabMaterials[9] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "3", Constants.MC_HALF, "bottom");
        slabMaterials[10] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "3", Constants.MC_HALF, "bottom");
        slabMaterials[11] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "3", Constants.MC_HALF, "bottom");

        materials.put(baseMaterial.name, slabMaterials);
      } else if (option != null) {
        throw new IllegalArgumentException("Invalid option: " + option);
      }
    }

    return materials.get(baseMaterial.name)[i];
  }
}
