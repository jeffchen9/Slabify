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

import static com.gmail.frogocomics.slabify.Constants.CQ_HINGE;
import static org.pepsoft.minecraft.Constants.MC_FACING;
import static org.pepsoft.minecraft.Constants.MC_HALF;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class VerticalCornerSlabShape extends Shape {

  public static final String NAME = "vert_corner_slab";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final Matrix leftStairShape = Matrix.of(new float[][]{
      {2, 0},
      {1, 0}
  });
  private final Matrix rightStairShape = Matrix.of(new float[][]{
      {0, 2},
      {0, 1}
  });

  public VerticalCornerSlabShape() {
    super("Vertical Corner Slab", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 2);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

    if (selectedOption == Options.ENABLE) {
      // Upscale shapes if needed
      Matrix upscaledLeftStairShape = leftStairShape.upscale(resolution / 2);
      Matrix upscaledRightStairShape = rightStairShape.upscale(resolution / 2);

      List<Matrix> shapes = new ArrayList<>();

      int[] angles = {90, 180, 270};

      shapes.add(upscaledLeftStairShape);
      for (int angle : angles) {
        shapes.add(upscaledLeftStairShape.rotate(angle));
      }

      shapes.add(upscaledRightStairShape);
      for (int angle : angles) {
        shapes.add(upscaledRightStairShape.rotate(angle));
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

      Material[] slabMaterials = new Material[8];

      slabMaterials[0] = Material.get(materialName, MC_FACING, "south", CQ_HINGE, "right", MC_HALF, "bottom");
      slabMaterials[7] = Material.get(materialName, MC_FACING, "east", CQ_HINGE, "right", MC_HALF, "bottom");
      slabMaterials[4] = Material.get(materialName, MC_FACING, "north", CQ_HINGE, "right", MC_HALF, "bottom");
      slabMaterials[3] = Material.get(materialName, MC_FACING, "west", CQ_HINGE, "right", MC_HALF, "bottom");

      slabMaterials[2] = Material.get(materialName, MC_FACING, "north", CQ_HINGE, "left", MC_HALF, "bottom");
      slabMaterials[5] = Material.get(materialName, MC_FACING, "west", CQ_HINGE, "left", MC_HALF, "bottom");
      slabMaterials[6] = Material.get(materialName, MC_FACING, "south", CQ_HINGE, "left", MC_HALF, "bottom");
      slabMaterials[1] = Material.get(materialName, MC_FACING, "east", CQ_HINGE, "left", MC_HALF, "bottom");

      // NOrth right / north left
      // East right / east left


      materials.put(baseMaterial.name, slabMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
