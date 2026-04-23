package com.gmail.frogocomics.slabify.shape;

public class RaggedStackedShapemap implements Shapemap {
  public final int[][][][] map;
  private final int[][] minZ;
  private final int[][] maxZ;
  private final int[][] range;

  public RaggedStackedShapemap(int[][][][] map, int[][] minZ, int[][] maxZ) {
    this.map = map;
    this.minZ = minZ;
    this.maxZ = maxZ;
    range = new int[minZ.length][minZ[0].length];

    for (int i = 0; i < minZ.length; i++) {
      for (int j = 0; j < minZ[0].length; j++) {
        range[i][j] = maxZ[i][j] - minZ[i][j];
      }
    }
  }

  @Override
  public int[] getIndicesAt(int x, int y, int relativeZ) {
    return map[x][y][relativeZ];
  }

  @Override
  public int getMinZ(int x, int y) {
    return minZ[x][y];
  }

  @Override
  public int getMaxZ(int x, int y) {
    return maxZ[x][y];
  }

  @Override
  public int getRange(int x, int y) {
    return range[x][y];
  }
}
