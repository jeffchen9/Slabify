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

import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

public final class SlabCustomLayerSettings implements ExporterSettings {

  private final Slab layer;

  public SlabCustomLayerSettings(Slab layer) {
    this.layer = layer;
  }

  @Override
  public boolean isApplyEverywhere() {
    return false;
  }

  @Override
  public Layer getLayer() {
    return layer;
  }

  @Override
  public ExporterSettings clone() {
    return this;
  }
}