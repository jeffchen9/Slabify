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

package com.gmail.frogocomics.slabify.layers;

import org.pepsoft.util.ColourUtils;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.layers.renderers.BitLayerRenderer;
import org.pepsoft.worldpainter.layers.renderers.DimensionAwareRenderer;

public final class SlabCustomLayerRenderer implements BitLayerRenderer, DimensionAwareRenderer {

  private final int color;
  private final float opacity;
  private Dimension dimension;

  public SlabCustomLayerRenderer(int color, float opacity) {
    this.color = color;
    this.opacity = opacity;
  }

  @Override
  public int getPixelColour(int globalX, int globalY, int underlyingColour, boolean b) {
    if (b) {
      float intensity = 255 * opacity;
      double diff =
          dimension.getHeightAt(globalX, globalY) - dimension.getIntHeightAt(globalX, globalY);

      if (diff >= 0 && diff < 0.5) {
        intensity /= 2;
      }

      return ColourUtils.mix(this.color, underlyingColour, (int) intensity);
    } else {
      return underlyingColour;
    }
  }

  @Override
  public void setDimension(Dimension dimension) {
    this.dimension = dimension;
  }
}
