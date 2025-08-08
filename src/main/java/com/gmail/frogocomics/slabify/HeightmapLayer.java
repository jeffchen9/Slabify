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
import java.util.Optional;

/**
 * Represents a WorldPainter {@link org.pepsoft.worldpainter.layers.Layer} that supports a
 * {@link File}, which represents a heightmap.
 */
public interface HeightmapLayer {

  /**
   * Get the heightmap, if available.
   *
   * @return the heightmap.
   */
  Optional<File> getHeightmap();

  /**
   * Set the heightmap.
   *
   * @param map the heightmap, which may be <code>null</code>.
   */
  void setHeightmap(File map);
}