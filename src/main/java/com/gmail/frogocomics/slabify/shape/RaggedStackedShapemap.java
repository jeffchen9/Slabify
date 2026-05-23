package com.gmail.frogocomics.slabify.shape;

import java.util.Set;

public final class RaggedStackedShapemap implements Shapemap {
  public final int[] map;
  private final int[] minZ;
  private final int[] range;
  private final int[] offsets;
  private final int shapeSize;

  public RaggedStackedShapemap(int[] map, int[] minZ, int[] maxZ, int[] offsets, int shapeSize) {
    this.map = map;
    this.minZ = minZ;
    this.offsets = offsets;
    this.shapeSize = shapeSize;
    range = new int[minZ.length];

    for (int i = 0; i < minZ.length; i++) {
      range[i] = maxZ[i] - minZ[i];
    }
  }

  @Override
  public int getIndexAt(int x, int y, int relativeZ, Set<Integer> allowedIndices) {
    if (allowedIndices.isEmpty()) {
      throw new IllegalArgumentException("allowedIndices must not be empty");
    }

    int flatXY = (x << 7) | y;
    int startIndex = offsets[flatXY] + relativeZ * shapeSize;

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
    return minZ[(x << 7) | y];
  }

  @Override
  public int getRange(int x, int y) {
    return range[(x << 7) | y];
  }
}
