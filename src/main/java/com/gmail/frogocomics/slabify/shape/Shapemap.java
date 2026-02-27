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

public interface Shapemap {

  int[] getIndicesAt(int x, int y, int relativeZ);

  int getMinZ();

  int getMaxZ();

  default int getRange() {
    return getMaxZ() - getMinZ();
  }

  default int getIndexAt(int x, int y, int relativeZ, Set<Integer> allowedIndices) {
    return Utils.filter(getIndicesAt(x, y, relativeZ), allowedIndices);
  }
}
