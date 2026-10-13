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
import java.util.concurrent.ConcurrentHashMap;

import static com.gmail.frogocomics.slabify.Constants.CQ_LAYER;
import static org.pepsoft.minecraft.Constants.MC_FACING;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public final class VerticalSlabShape extends Shape {

  public static final String NAME = "vert_slab";

  private final Map<String, Material[]> materials = new ConcurrentHashMap<>();
  private final Matrix slab1 = Matrix.of(new float[][]{
      {1, 1, 1, 1, 1, 1, 1, 1},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0},
      {0, 0, 0, 0, 0, 0, 0, 0}
  });
  private final Matrix slab2 = Matrix.of(new float[][]{
      {1, 1, 1, 1},
      {0, 0, 0, 0},
      {0, 0, 0, 0},
      {0, 0, 0, 0}
  });
  private final Matrix slab3 = Matrix.of(new float[][]{
      {1, 1},
      {0, 0}
  });
  private final Matrix slab4 = Matrix.of(new float[][]{
      {1, 1, 1, 1},
      {1, 1, 1, 1},
      {1, 1, 1, 1},
      {0, 0, 0, 0}
  });

  public VerticalSlabShape() {
    super("Vertical Slab", NAME, new Options[]{Options.DISABLE, Options.EIGHTHS, Options.QUARTERS,
        Options.HALVES}, false, -1, true, Options.DISABLE);
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
      List<Matrix> shapes = new ArrayList<>();

      switch (selectedOption) {
        case HALVES: {
          Matrix upscaled3 = slab3.upscale(resolution / slab3.getSize());

          addRotations(shapes, upscaled3);
          break;
        }
        case QUARTERS: {
          Matrix upscaled2 = slab2.upscale(resolution / slab2.getSize());
          Matrix upscaled3 = slab3.upscale(resolution / slab3.getSize());
          Matrix upscaled4 = slab4.upscale(resolution / slab4.getSize());

          addRotations(shapes, upscaled3);

          addRotations(shapes, upscaled2);

          addRotations(shapes, upscaled4);
          break;
        }
        case EIGHTHS: {
          Matrix upscaled1 = slab1.upscale(resolution / slab1.getSize());
          Matrix upscaled2 = slab2.upscale(resolution / slab2.getSize());
          Matrix upscaled3 = slab3.upscale(resolution / slab3.getSize());
          Matrix upscaled4 = slab4.upscale(resolution / slab4.getSize());

          addRotations(shapes, upscaled3);

          addRotations(shapes, upscaled2);

          addRotations(shapes, upscaled4);

          addRotations(shapes, upscaled1);
          break;
        }
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

      Material[] slabMaterials = new Material[16];

      slabMaterials[0] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "3");
      slabMaterials[1] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "3");
      slabMaterials[2] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "3");
      slabMaterials[3] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "3");
      slabMaterials[4] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "2");
      slabMaterials[5] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "2");
      slabMaterials[6] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "2");
      slabMaterials[7] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "2");
      slabMaterials[8] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "4");
      slabMaterials[9] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "4");
      slabMaterials[10] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "4");
      slabMaterials[11] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "4");
      slabMaterials[12] = Material.get(materialName, MC_FACING, "east", CQ_LAYER, "1");
      slabMaterials[13] = Material.get(materialName, MC_FACING, "north", CQ_LAYER, "1");
      slabMaterials[14] = Material.get(materialName, MC_FACING, "west", CQ_LAYER, "1");
      slabMaterials[15] = Material.get(materialName, MC_FACING, "south", CQ_LAYER, "1");

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
