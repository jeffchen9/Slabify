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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class FullShape extends Shape {

  public static final String NAME = "full";
  private static final FullShape instance = new FullShape();
  private final Matrix shape = Matrix.of(new float[][]{{1}});

  public FullShape() {
    super(null, NAME, new Options[]{}, true, 1);
  }

  public static FullShape getInstance() {
    return instance;
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {
    return Optional.of(Collections.singletonList(shape.upscale(resolution / getMinResolution(null))));
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    return baseMaterial;
  }
}
