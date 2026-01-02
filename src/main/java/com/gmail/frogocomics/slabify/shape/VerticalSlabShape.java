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
public class VerticalSlabShape extends Shape {

  public static final String NAME = "vert_slab";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix slab1;
  private final Matrix slab2;
  private final Matrix slab3;
  private final Matrix slab4;

  public VerticalSlabShape() {
    super("Vertical Slab", NAME, new Options[]{Options.DISABLE, Options.EIGHTHS, Options.QUARTERS,
        Options.HALVES}, false, -1);

    slab1 = new Matrix(new float[][]{
            {8, 8, 8, 8, 8, 8, 8, 8},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
    });
    slab2 = new Matrix(new float[][]{
            {4, 4, 4, 4},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    });
    slab3 = new Matrix(new float[][]{
            {1, 1},
            {0, 0}
    });
    slab4 = new Matrix(new float[][]{
            {4, 4, 4, 4},
            {4, 4, 4, 4},
            {4, 4, 4, 4},
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
      Matrix upscaled1 = resolution >= 8 ? slab1.upscale(resolution / 8): null;
      Matrix upscaled2 = resolution >= 4 ? slab2.upscale(resolution / 4) : null;
      Matrix upscaled3 = resolution >= 2 ? slab3.upscale(resolution / 2) : null;
      Matrix upscaled4 = resolution >= 4 ? slab4.upscale(resolution / 4): null;

      int[] angles = {90, 180, 270};
      List<Matrix> shapes = new ArrayList<>();

      switch (selectedOption) {
        case HALVES:
          shapes.add(upscaled3);
          for (int angle : angles) {
            shapes.add(upscaled3.rotate(angle));
          }
          break;
        case QUARTERS:
          shapes.add(upscaled3);
          for (int angle : angles) {
            shapes.add(upscaled3.rotate(angle));
          }

          shapes.add(upscaled2);
          for (int angle : angles) {
            shapes.add(upscaled2.rotate(angle));
          }

          shapes.add(upscaled4);
          for (int angle : angles) {
            shapes.add(upscaled4.rotate(angle));
          }
          break;
        case EIGHTHS:
          shapes.add(upscaled3);
          for (int angle : angles) {
            shapes.add(upscaled3.rotate(angle));
          }

          shapes.add(upscaled2);
          for (int angle : angles) {
            shapes.add(upscaled2.rotate(angle));
          }

          shapes.add(upscaled4);
          for (int angle : angles) {
            shapes.add(upscaled4.rotate(angle));
          }

          shapes.add(upscaled1);
          for (int angle : angles) {
            shapes.add(upscaled1.rotate(angle));
          }
          break;
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
        materialName = baseMaterial.name + "_vertical_slab";
      }

      if (option == null) {
        throw new IllegalArgumentException("Option cannot be null for a vertical slab");
      }

      Material[] slabMaterials = null;

      switch (option) {
        case HALVES:
          slabMaterials = new Material[4];

          slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "3");

          break;
        case QUARTERS:
          slabMaterials = new Material[12];

          slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "3");

          slabMaterials[4] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "2");
          slabMaterials[5] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "2");
          slabMaterials[6] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "2");
          slabMaterials[7] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "2");
          slabMaterials[8] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "4");
          slabMaterials[9] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "4");
          slabMaterials[10] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "4");
          slabMaterials[11] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "4");

          break;
        case EIGHTHS:
          slabMaterials = new Material[16];

          slabMaterials[0] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "3");

          slabMaterials[4] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "2");
          slabMaterials[5] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "2");
          slabMaterials[6] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "2");
          slabMaterials[7] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "2");
          slabMaterials[8] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "4");
          slabMaterials[9] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "4");
          slabMaterials[10] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "4");
          slabMaterials[11] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "4");

          slabMaterials[12] = Material.get(materialName, Constants.MC_FACING, "east", CQ_LAYER, "1");
          slabMaterials[13] = Material.get(materialName, Constants.MC_FACING, "north", CQ_LAYER, "1");
          slabMaterials[14] = Material.get(materialName, Constants.MC_FACING, "west", CQ_LAYER, "1");
          slabMaterials[15] = Material.get(materialName, Constants.MC_FACING, "south", CQ_LAYER, "1");

          break;
      }

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
