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

import com.gmail.frogocomics.slabify.utils.Utils;

import java.util.Set;

/**
 * Represents a representation of the closest shapes that match a difference map.
 */
public interface Shapemap {

  /**
   * Get the shape indices at a point.
   *
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param relativeZ the relative z coordinate.
   * @return the closest indices, as an array, from closest to furthest.
   */
  int[] getIndicesAt(int x, int y, int relativeZ);

  /**
   * Get the minimum z value.
   *
   * @param x the x coordinate, if required by the implementation.
   * @param y the y coordinate, if required by the implementation.
   * @return the minimum z value.
   */
  int getMinZ(int x, int y);

  /**
   * Get the maximum z value.
   *
   * @param x the x coordinate, if required by the implementation.
   * @param y the y coordinate, if required by the implementation.
   * @return the maximum z value.
   */
  int getMaxZ(int x, int y);

  /**
   * Get the range between the minimum z value and the maximum z value.
   *
   * @param x the x coordinate, if required by the implementation.
   * @param y the y coordinate, if required by the implementation.
   * @return the range.
   */
  default int getRange(int x, int y) {
    return getMaxZ(x, y) - getMinZ(x, y);
  }

  /**
   * Get the closest allowable shape index at a point.
   *
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param relativeZ the relative z coordinate.
   * @param allowedIndices a set of the allowed shape indices.
   * @return the closest allowable shape index.
   */
  default int getIndexAt(int x, int y, int relativeZ, Set<Integer> allowedIndices) {
    if (allowedIndices.isEmpty()) {
      throw new IllegalArgumentException("allowedIndices must not be empty");
    }

    return Utils.filter(getIndicesAt(x, y, relativeZ), allowedIndices);
  }
}
