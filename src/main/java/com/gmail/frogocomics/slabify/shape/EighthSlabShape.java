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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.pepsoft.minecraft.Constants.MC_FACING;
import static org.pepsoft.minecraft.Constants.MC_HALF;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class EighthSlabShape extends Shape {

  public static final String NAME = "eighth_slab";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix southShape = Matrix.of(new float[][]{
      {1, 0},
      {0, 0}
  });
  private final Matrix eastShape = Matrix.of(new float[][]{
      {0, 1},
      {0, 0}
  });
  private final Matrix northShape = Matrix.of(new float[][]{
      {0, 0},
      {0, 1}
  });
  private final Matrix westShape = Matrix.of(new float[][]{
      {0, 0},
      {1, 0}
  });


  public EighthSlabShape() {
    super("Eighth Slab", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 2);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

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
        return Material.AIR;
      }

      Material[] slabMaterials = new Material[12];

      slabMaterials[0] = Material.get(materialName, MC_FACING, "south", MC_HALF, "bottom");
      slabMaterials[1] = Material.get(materialName, MC_FACING, "east", MC_HALF, "bottom");
      slabMaterials[2] = Material.get(materialName, MC_FACING, "north", MC_HALF, "bottom");
      slabMaterials[3] = Material.get(materialName, MC_FACING, "west", MC_HALF, "bottom");

      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
