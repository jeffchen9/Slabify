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
import org.pepsoft.minecraft.Material;

import java.util.*;

import static org.pepsoft.minecraft.Constants.*;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class StairShape extends Shape {

  public static final String NAME = "stairs";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix stairShape = Matrix.of(new float[][]{
      {1, 1},
      {0.5f, 0.5f}
  });
  private final Matrix insideStairShape = Matrix.of(new float[][]{
      {0.5f, 1},
      {1, 1}
  });
  private final Matrix outsideStairShape = Matrix.of(new float[][]{
      {1, 0.5f},
      {0.5f, 0.5f}
  });

  public StairShape() {
    super("Stairs", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 2, true, false);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

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
        return Material.AIR;
      }

      Material[] stairMaterials = new Material[12];

      stairMaterials[0] = Material.get(materialName, MC_FACING, "west", MC_SHAPE, "straight", MC_HALF, "bottom");
      stairMaterials[1] = Material.get(materialName, MC_FACING, "south", MC_SHAPE, "straight", MC_HALF, "bottom");
      stairMaterials[2] = Material.get(materialName, MC_FACING, "east", MC_SHAPE, "straight", MC_HALF, "bottom");
      stairMaterials[3] = Material.get(materialName, MC_FACING, "north", MC_SHAPE, "straight", MC_HALF, "bottom");

      stairMaterials[4] = Material.get(materialName, MC_FACING, "east", MC_SHAPE, "inner_right", MC_HALF, "bottom");
      stairMaterials[5] = Material.get(materialName, MC_FACING, "east", MC_SHAPE, "inner_left", MC_HALF, "bottom");
      stairMaterials[6] = Material.get(materialName, MC_FACING, "west", MC_SHAPE, "inner_right", MC_HALF, "bottom");
      stairMaterials[7] = Material.get(materialName, MC_FACING, "west", MC_SHAPE, "inner_left", MC_HALF, "bottom");

      stairMaterials[8] = Material.get(materialName, MC_FACING, "west", MC_SHAPE, "outer_right", MC_HALF, "bottom");
      stairMaterials[9] = Material.get(materialName, MC_FACING, "west", MC_SHAPE, "outer_left", MC_HALF, "bottom");
      stairMaterials[10] = Material.get(materialName, MC_FACING, "east", MC_SHAPE, "outer_right", MC_HALF, "bottom");
      stairMaterials[11] = Material.get(materialName, MC_FACING, "east", MC_SHAPE, "outer_left", MC_HALF, "bottom");

      materials.put(baseMaterial.name, stairMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
