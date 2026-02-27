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

import static org.pepsoft.minecraft.Constants.MC_LAYERS;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class LayerShape extends Shape {

  public static final String NAME = "layer";
  private final Map<String, Material[]> materials = new HashMap<>();

  public LayerShape() {
    super("Layer", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, true, 1, false, false, false);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    if (selectedOption == Options.ENABLE) {
      List<Matrix> shapes = new ArrayList<>();

      for (int i = 1; i < 8; i++) {
        shapes.add((Matrix.of(new float[][]{{i / 8f}})).upscale(resolution / getMinResolution(null)));
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

      Material[] layerMaterials = new Material[7];

      for (int j = 1; j < 8; j++) {
        layerMaterials[j - 1] = Material.get(materialName, MC_LAYERS, j);
      }

      materials.put(baseMaterial.name, layerMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
