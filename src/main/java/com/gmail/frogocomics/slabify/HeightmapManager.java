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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton manager for storing heightmaps.
 */
public final class HeightmapManager {

  private static final HeightmapManager INSTANCE = new HeightmapManager();

  private static final Logger LOGGER = LoggerFactory.getLogger(HeightmapManager.class);
  // Key: UUID
  // Value: (File, Set<File>)
  //                   Represents all layers where the height map is used.
  private final Map<File, Set<HeightmapLayer>> heightmaps = new HashMap<>();

  private HeightmapManager() {
    // Prevent instantiation
  }

  public static HeightmapManager getInstance() {
    return INSTANCE;
  }

  /**
   * Get all heightmaps.
   *
   * @return the heightmaps.
   */
  public Set<File> getHeightmaps() {
    return heightmaps.keySet();
  }

  /**
   * Register the heightmap.
   *
   * @param heightmap the location of the heightmap, which can be <code>null</code>.
   * @param layer     the layer the heightmap belongs to.
   */
  public void register(File heightmap, HeightmapLayer layer) {
    if (heightmap != null) {
      LOGGER.info("Registering: {}", heightmap.getName());
      if (heightmaps.containsKey(heightmap)) {
        Set<HeightmapLayer> s = heightmaps.get(heightmap);
        s.add(layer);
      } else {
        heightmaps.put(heightmap, new HashSet<>(Collections.singleton(layer)));
      }
    }
  }

  /**
   * Deregister the heightmap. This will deregister the heightmap on all {@link HeightmapLayer}s
   * that use the heightmap.
   *
   * @param heightmap the location of the heightmap.
   */
  public void deregister(File heightmap) {
    if (heightmaps.containsKey(heightmap)) {
      LOGGER.info("Deregistering: {}", heightmap.getName());
      for (HeightmapLayer layer : heightmaps.get(heightmap)) {
        layer.setHeightmap(null);
      }
      heightmaps.remove(heightmap);
    }
  }
}
