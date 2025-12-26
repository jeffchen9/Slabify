/*
 *     A plugin for WorldPainter that adds slab and stair detail to terrain.
 *     Copyright (C) 2025  Jeff Chen
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify;

import com.gmail.frogocomics.slabify.gui.SlabCustomLayerEditor;
import com.gmail.frogocomics.slabify.layers.Slab;
import com.gmail.frogocomics.slabify.shape.Shapes;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.LayerEditor;
import org.pepsoft.worldpainter.plugins.AbstractPlugin;
import org.pepsoft.worldpainter.plugins.CustomLayerProvider;
import org.pepsoft.worldpainter.plugins.LayerEditorProvider;

import java.util.Collections;
import java.util.List;

import static com.gmail.frogocomics.slabify.Version.VERSION;

/**
 * The main plugin class for Slabify.
 */
@SuppressWarnings("unused") // Instantiated by WorldPainter
public final class SlabifyWPPlugin extends AbstractPlugin implements CustomLayerProvider,
    LayerEditorProvider {

  private static final String NAME = "Slabify Plugin";
  private static final List<Class<? extends CustomLayer>> CUSTOM_LAYERS = Collections.singletonList(
      Slab.class);

  public SlabifyWPPlugin() {
    super(NAME, VERSION);
    Shapes.init();
  }

  @Override
  public List<Class<? extends CustomLayer>> getCustomLayers() {
    return CUSTOM_LAYERS;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <L extends Layer> LayerEditor<L> createLayerEditor(Platform platform, Class<L> layerType) {
    if (layerType == Slab.class) {
      return (LayerEditor<L>) new SlabCustomLayerEditor(platform);
    } else {
      return null;
    }
  }
}
