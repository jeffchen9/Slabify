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

import static com.gmail.frogocomics.slabify.Constants.CQ_LAYER;
import static org.pepsoft.minecraft.Constants.MC_FACING;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class VerticalCornerShape extends Shape {

  public static final String NAME = "vert_corner";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix quarter1 = Matrix.of(new float[][]{
      {1, 1, 1, 1, 1, 1, 1, 1},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0},
      {1, 0, 0, 0, 0, 0, 0, 0}
  });
  private final Matrix quarter2 = Matrix.of(new float[][]{
      {1, 1, 1, 1},
      {1, 0, 0, 0},
      {1, 0, 0, 0},
      {1, 0, 0, 0}
  });
  private final Matrix quarter3 = Matrix.of(new float[][]{
      {1, 1},
      {1, 0}
  });
  private final Matrix quarter4 = Matrix.of(new float[][]{
      {1, 1, 1, 1},
      {1, 1, 1, 1},
      {1, 1, 1, 1},
      {1, 1, 1, 0}
  });

  public VerticalCornerShape() {
    super("Vertical Corner", NAME, new Options[]{Options.DISABLE, Options.EIGHTHS, Options.QUARTERS,
        Options.HALVES}, false, -1, true, false, Options.DISABLE);
  }

  @Override
  public int getMinResolution(Options option) {
    if (option == Options.EIGHTHS) {
      return 8;
    } else if (option == Options.QUARTERS) {
      return 4;
    } else if (option == Options.HALVES) {
      return 2;
    }

    return -1; // Default value that won't be reached
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(selectedOption);

    if (selectedOption != Options.DISABLE) {
      // Upscale shapes if needed
      Matrix upscaled1 = quarter1.upscale(resolution / 8);
      Matrix upscaled2 = quarter2.upscale(resolution / 4);
      Matrix upscaled3 = quarter3.upscale(resolution / 2);
      Matrix upscaled4 = quarter4.upscale(resolution / 4);

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
        return Material.AIR;
      }

      if (option == null) {
        throw new IllegalArgumentException("Option cannot be null for a vertical corner");
      }

      Material[] slabMaterials = null;

      switch (option) {
        case HALVES:
          slabMaterials = new Material[4];

          slabMaterials[0] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "3");

          break;
        case QUARTERS:
          slabMaterials = new Material[12];

          slabMaterials[0] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "3");

          slabMaterials[4] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "2");
          slabMaterials[5] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "2");
          slabMaterials[6] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "2");
          slabMaterials[7] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "2");
          slabMaterials[8] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "4");
          slabMaterials[9] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "4");
          slabMaterials[10] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "4");
          slabMaterials[11] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "4");

          break;
        case EIGHTHS:
          slabMaterials = new Material[16];

          slabMaterials[0] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "3");
          slabMaterials[1] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "3");
          slabMaterials[2] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "3");
          slabMaterials[3] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "3");

          slabMaterials[4] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "2");
          slabMaterials[5] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "2");
          slabMaterials[6] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "2");
          slabMaterials[7] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "2");
          slabMaterials[8] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "4");
          slabMaterials[9] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "4");
          slabMaterials[10] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "4");
          slabMaterials[11] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "4");

          slabMaterials[12] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "1");
          slabMaterials[13] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "1");
          slabMaterials[14] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "1");
          slabMaterials[15] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "1");

          break;
      }

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
