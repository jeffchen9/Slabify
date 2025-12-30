package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;

import java.util.*;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class StairShape extends Shape {

  public static final String NAME = "stairs";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix stairShape;
  private final Matrix insideStairShape;
  private final Matrix outsideStairShape;

  public StairShape() {
    super("Stairs", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 2);
    stairShape = new Matrix(new float[][]{
        {2, 2},
        {1, 1}
    });
    insideStairShape = new Matrix(new float[][]{
        {1, 2},
        {2, 2}
    });
    outsideStairShape = new Matrix(new float[][]{
        {2, 1},
        {1, 1}
    });
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

    assert resolution >= getMinimumResolution(null);

    if (selectedOption == Options.ENABLE) {

      // Upscale shapes if needed
      Matrix upscaledStairShape = stairShape.upscale(resolution / 2);
      Matrix upscaledInsideStairShape = insideStairShape.upscale(resolution / 2);
      Matrix upscaledOutsideStairShape = outsideStairShape.upscale(resolution / 2);

      List<Matrix> shapes = new ArrayList<>();

      /*
      Normal stair (N, E, S, W)
      Inside stair (N, E, S, W)
      Outside stair (N, E, S, W)
       */

      int[] angles = {90, 180, 270};

      shapes.add(upscaledStairShape);
      for (int angle : angles) {
        shapes.add(upscaledStairShape.rotate(angle));
      }

      shapes.add(upscaledInsideStairShape);
      for (int angle : angles) {
        shapes.add(upscaledInsideStairShape.rotate(angle));
      }

      shapes.add(upscaledOutsideStairShape);
      for (int angle : angles) {
        shapes.add(upscaledOutsideStairShape.rotate(angle));
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
        materialName = baseMaterial.name + "_stairs";
      }

      Material[] stairMaterials = new Material[12];

      stairMaterials[0] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_SHAPE, "straight", Constants.MC_HALF, "bottom");
      stairMaterials[1] = Material.get(materialName, Constants.MC_FACING, "south", Constants.MC_SHAPE, "straight", Constants.MC_HALF, "bottom");
      stairMaterials[2] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_SHAPE, "straight", Constants.MC_HALF, "bottom");
      stairMaterials[3] = Material.get(materialName, Constants.MC_FACING, "north", Constants.MC_SHAPE, "straight", Constants.MC_HALF, "bottom");

      stairMaterials[4] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_SHAPE, "inner_right", Constants.MC_HALF, "bottom");
      stairMaterials[5] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_SHAPE, "inner_left", Constants.MC_HALF, "bottom");
      stairMaterials[6] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_SHAPE, "inner_right", Constants.MC_HALF, "bottom");
      stairMaterials[7] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_SHAPE, "inner_left", Constants.MC_HALF, "bottom");

      stairMaterials[8] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_SHAPE, "outer_right", Constants.MC_HALF, "bottom");
      stairMaterials[9] = Material.get(materialName, Constants.MC_FACING, "west", Constants.MC_SHAPE, "outer_left", Constants.MC_HALF, "bottom");
      stairMaterials[10] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_SHAPE, "outer_right", Constants.MC_HALF, "bottom");
      stairMaterials[11] = Material.get(materialName, Constants.MC_FACING, "east", Constants.MC_SHAPE, "outer_left", Constants.MC_HALF, "bottom");

      materials.put(baseMaterial.name, stairMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
