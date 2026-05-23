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

import java.util.Set;

public final class StackedShapemap implements Shapemap {

  public final int[] map;
  private final int minZ;
  private final int vertDiff;
  private final int shapeSize;

  public StackedShapemap(int[] map, int minZ, int maxZ, int shapeSize) {
    this.map = map;
    this.minZ = minZ;
    this.vertDiff = maxZ - minZ;
    this.shapeSize = shapeSize;
  }

  @Override
  public int getIndexAt(int x, int y, int relativeZ, Set<Integer> allowedIndices) {
    if (allowedIndices.isEmpty()) {
      throw new IllegalArgumentException("allowedIndices must not be empty");
    }

    int flatXY = (x << 7) | y;
    int startIndex = (flatXY * vertDiff + relativeZ) * shapeSize;

    for (int i = 0; i < shapeSize; i++) {
      int shapeIdx = map[startIndex + i];
      if (allowedIndices.contains(shapeIdx)) {
        return shapeIdx;
      }
    }

    // This should not happen
    throw new IllegalStateException("None of the values in arr are in allowed");
  }

  @Override
  public int getMinZ(int x, int y) {
    return minZ;
  }

  @Override
  public int getRange(int x, int y) {
    return vertDiff;
  }
}
