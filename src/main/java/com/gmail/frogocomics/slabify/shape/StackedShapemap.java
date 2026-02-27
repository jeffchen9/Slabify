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

public class StackedShapemap implements Shapemap {

  public final int[][][][] map;
  private final int minZ;
  private final int maxZ;

  public StackedShapemap(int[][][][] map, int minZ, int maxZ) {
    this.map = map;
    this.minZ = minZ;
    this.maxZ = maxZ;
  }

  @Override
  public int[] getIndicesAt(int x, int y, int relativeZ) {
    return map[x][y][relativeZ];
  }

  @Override
  public int getMinZ() {
    return minZ;
  }

  @Override
  public int getMaxZ() {
    return maxZ;
  }
}
