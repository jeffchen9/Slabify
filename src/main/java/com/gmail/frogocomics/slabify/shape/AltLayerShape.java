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

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class AltLayerShape extends Shape {

  public static final String NAME = "alt_layer";

  private final Map<String, Material[]> materials = new HashMap<>();
  private final float[] heights = new float[]{0.125f, 0.25f, 0.5f, 0.75f};

  public AltLayerShape() {
    super("Alt Layer", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 1);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    if (selectedOption == Options.ENABLE) {
      List<Matrix> shapes = new ArrayList<>();

      for (int i = 1; i < heights.length; i++) {
        shapes.add((Matrix.of(new float[][]{{heights[i]}})).upscale(resolution / getMinResolution(null)));
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

      Material[] layerMaterials = new Material[heights.length];

      for (int j = 0; j < heights.length; j++) {
        layerMaterials[j] = Material.get(materialName, CQ_LAYER, j + 1);
      }

      materials.put(baseMaterial.name, layerMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
