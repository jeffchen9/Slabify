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

import static org.pepsoft.minecraft.Constants.MC_TYPE;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class SlabShape extends Shape {

  public static final String NAME = "slab";

  private final Map<String, Material> materials = new HashMap<>();
  private final Matrix shape;

  public SlabShape() {
    super("Slab", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 1, false, false, Options.ENABLE);
    shape = Matrix.of(new float[][]{{0.5f}});
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

    return selectedOption == Options.ENABLE ? Optional.of(Collections.singletonList(shape.upscale(resolution / getMinResolution(null)))) : Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    if (materials.containsKey(baseMaterial.name)) {
      return materials.get(baseMaterial.name);
    }

    String materialName = Shapes.getMaterial(this, baseMaterial.name);

    if (materialName == null) {
      return Material.AIR;
    }

    Material newMaterial = Material.get(materialName, MC_TYPE, "bottom");
    materials.put(baseMaterial.name, newMaterial);

    return newMaterial;
  }
}
