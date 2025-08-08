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

import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;

/**
 * Utility class for assigning the most appropriate shape to use to add detail to terrain. A "cut"
 * refers to the replacing of an existing block while a "fill" refers to the addition of an extra
 * block on top of the existing block.
 *
 * <p>Each shape is represented by an array of length 4. Position 1 represents the top left height,
 * position 2 represents the top right height, position 3 represents the bottom left height, and
 * position 4 represents the bottom right height. "1" corresponds to <i>0.5</i> blocks, "2"
 * corresponds to <i>1.0</i> blocks, etc. In this representation, north would be the top, so the
 * side containing positions 1 and 2.
 */
public final class Shapes {

  /**
   * Shape: Full block, fill
   */
  private static final int[] F_FULL_BLOCK = {
      2, 2, 2, 2
  };

  /**
   * Shape: Slab, fill
   */
  private static final int[] F_SLAB = {
      1, 1, 1, 1
  };

  /**
   * Shape: Slab, cut
   */
  private static final int[] C_SLAB = {
      -1, -1, -1, -1
  };

  /**
   * Shape: Stairs, north, fill
   */
  private static final int[] F_STAIR_N = {
      2, 2, 1, 1
  };

  /**
   * Shape: Stairs, north, cut
   */
  private static final int[] C_STAIR_N = {
      0, 0, -1, -1
  };

  /**
   * Shape: Stairs, east, fill
   */
  private static final int[] F_STAIR_E = {
      1, 2, 1, 2
  };

  /**
   * Shape: Stairs, east, cut
   */
  private static final int[] C_STAIR_E = {
      -1, 0, -1, 0
  };

  /**
   * Shape: Stairs, south, fill
   */
  private static final int[] F_STAIR_S = {
      1, 1, 2, 2
  };

  /**
   * Shape: Stairs, south, cut
   */
  private static final int[] C_STAIR_S = {
      -1, -1, 0, 0
  };

  /**
   * Shape: Stairs, west, fill
   */
  private static final int[] F_STAIR_W = {
      2, 1, 2, 1
  };

  /**
   * Shape: Stairs, west, cut
   */
  private static final int[] C_STAIR_W = {
      0, -1, 0, -1
  };

  /**
   * Shape: Stairs, outer, top left, fill
   */
  private static final int[] F_STAIR_OUTER_TL = {
      2, 1, 1, 1
  };

  /**
   * Shape: Stairs, outer, top left, cut
   */
  private static final int[] C_STAIR_OUTER_TL = {
      0, -1, -1, -1
  };

  /**
   * Shape: Stairs, outer, top right, fill
   */
  private static final int[] F_STAIR_OUTER_TR = {
      1, 2, 1, 1
  };

  /**
   * Shape: Stairs, outer, top right, cut
   */
  private static final int[] C_STAIR_OUTER_TR = {
      -1, 0, -1, -1
  };

  /**
   * Shape: Stairs, outer, bottom left, fill
   */
  private static final int[] F_STAIR_OUTER_BL = {
      1, 1, 2, 1
  };

  /**
   * Shape: Stairs, outer, bottom left, cut
   */
  private static final int[] C_STAIR_OUTER_BL = {
      -1, -1, 0, -1
  };

  /**
   * Shape: Stairs, outer, bottom right, fill
   */
  private static final int[] F_STAIR_OUTER_BR = {
      1, 1, 1, 2
  };

  /**
   * Shape: Stairs, outer, bottom right, cut
   */
  private static final int[] C_STAIR_OUTER_BR = {
      -1, -1, -1, 0
  };

  /**
   * Shape: Stairs, inner, top left, fill
   */
  private static final int[] F_STAIR_INNER_TL = {
      1, 2, 2, 2
  };

  /**
   * Shape: Stairs, inner, top left, cut
   */
  private static final int[] C_STAIR_INNER_TL = {
      -1, 0, 0, 0
  };

  /**
   * Shape: Stairs, inner, top right, fill
   */
  private static final int[] F_STAIR_INNER_TR = {
      2, 1, 2, 2
  };

  /**
   * Shape: Stairs, inner, top right, cut
   */
  private static final int[] C_STAIR_INNER_TR = {
      0, -1, 0, 0
  };

  /**
   * Shape: Stairs, inner, bottom left, fill
   */
  private static final int[] F_STAIR_INNER_BL = {
      2, 2, 1, 2
  };

  /**
   * Shape: Stairs, inner, bottom left, cut
   */
  private static final int[] C_STAIR_INNER_BL = {
      0, 0, -1, 0
  };

  /**
   * Shape: Stairs, inner, bottom right, fill
   */
  private static final int[] F_STAIR_INNER_BR = {
      2, 2, 2, 1
  };

  /**
   * Shape: Stairs, inner, bottom right, cut
   */
  private static final int[] C_STAIR_INNER_BR = {
      0, 0, 0, -1
  };

  /**
   * Shape: Empty
   */
  private static final int[] EMPTY = {
      0, 0, 0, 0
  };

  /**
   * An array of all the available shapes.\
   */
  private static final int[][] SHAPES = {
      // 1
      F_FULL_BLOCK,
      // 2
      F_SLAB,
      // 3
      C_SLAB,
      // 4
      F_STAIR_N,
      // 5
      C_STAIR_N,
      // 6
      F_STAIR_E,
      // 7
      C_STAIR_E,
      // 8
      F_STAIR_S,
      // 9
      C_STAIR_S,
      // 10
      F_STAIR_W,
      // 11
      C_STAIR_W,
      // 12
      F_STAIR_OUTER_TL,
      // 13
      C_STAIR_OUTER_TL,
      // 14
      F_STAIR_OUTER_TR,
      // 15
      C_STAIR_OUTER_TR,
      // 16
      F_STAIR_OUTER_BL,
      // 17
      C_STAIR_OUTER_BL,
      // 18
      F_STAIR_OUTER_BR,
      // 19
      C_STAIR_OUTER_BR,
      // 20
      F_STAIR_INNER_TL,
      // 21
      C_STAIR_INNER_TL,
      // 22
      F_STAIR_INNER_TR,
      // 23
      C_STAIR_INNER_TR,
      // 24
      F_STAIR_INNER_BL,
      // 25
      C_STAIR_INNER_BL,
      // 26
      F_STAIR_INNER_BR,
      // 27
      C_STAIR_INNER_BR,
      // 28
      EMPTY
  };

  /**
   * <code>true</code> if the shape represents a fill instead of a cut
   */
  private static final boolean[] FILL = {
      // 1
      true,
      // 2
      true,
      // 3
      false,
      // 4
      true,
      // 5
      false,
      // 6
      true,
      // 7
      false,
      // 8
      true,
      // 9
      false,
      // 10
      true,
      // 11
      false,
      // 12
      true,
      // 13
      false,
      // 14
      true,
      // 15
      false,
      // 16
      true,
      // 17
      false,
      // 18
      true,
      // 19
      false,
      // 20
      true,
      // 21
      false,
      // 22
      true,
      // 23
      false,
      // 24
      true,
      // 25
      false,
      // 26
      true,
      // 27
      false,
      // 28
      false
  };

  /**
   * The direction of the block, if applicable
   */
  private final static String[] FACING = {null, null, null, "north", "north", "east", "east",
      "south", "south", "west", "west", "west", "west", "east", "east", "west", "west", "east",
      "east", "east", "east", "west", "west", "east", "east", "west", "west"};

  /**
   * The shape of the block, if applicable
   */
  private final static String[] SHAPE = {null, null, null, "straight", "straight", "straight",
      "straight", "straight", "straight", "straight", "straight", "outer_right", "outer_right",
      "outer_left", "outer_left", "outer_left", "outer_left", "outer_right", "outer_right",
      "inner_right", "inner_right", "inner_left", "inner_left", "inner_left", "inner_left",
      "inner_right", "inner_right"};

  private final static double exponent;

  static {
    String s = System.getProperty("com.gmail.frogocomics.exponent");
    exponent = s == null ? 2 : Double.parseDouble(s); // 2 for MSE, 1 for MAE
  }

  private Shapes() {
    // Prevent instantiation
  }

  /**
   * Find the most appropriate shape given an array representing the difference between the upscaled
   * heights and the original heights.
   *
   * @param differenceMap the difference, represented as an array.
   * @return an integer array wherein each value corresponds to the index of each shape in
   * {@link Shapes#SHAPES}.
   */
  public static int[][] findMostSimilarShapes(float[][] differenceMap) {
    int height = differenceMap.length;
    int width = differenceMap[0].length;

    int[][] shapeMap = new int[height / 2][width / 2];

    for (int x = 0; x < height / 2; x++) {
      for (int y = 0; y < width / 2; y++) {
        shapeMap[x][y] = findMostSimilarShape(
            differenceMap[2 * x][2 * y],
            differenceMap[2 * x + 1][2 * y],
            differenceMap[2 * x][2 * y + 1],
            differenceMap[2 * x + 1][2 * y + 1]
        );
      }
    }

    return shapeMap;
  }

  /**
   * Find the most similar shape.
   *
   * @param difference the difference, represented as a length-4 array.
   * @return the index of the closest shape, wherein the index corresponds to the index of each
   * shape in {@link Shapes#SHAPES};
   */
  public static int findMostSimilarShape(float... difference) {

    float lowestLoss = Float.MAX_VALUE;
    int lowestIdx = 27; // Index for EMPTY

    for (int i = 0; i < SHAPES.length; i++) {
      int[] shape = SHAPES[i];
      float v1 = (float) Math.pow(Math.abs(shape[0] - difference[0]), exponent);
      float v2 = (float) Math.pow(Math.abs(shape[1] - difference[1]), exponent);
      float v3 = (float) Math.pow(Math.abs(shape[2] - difference[2]), exponent);
      float v4 = (float) Math.pow(Math.abs(shape[3] - difference[3]), exponent);
      float currentLoss = v1 + v2 + v3 + v4;

      if (currentLoss < lowestLoss) {
        lowestLoss = currentLoss;
        lowestIdx = i;
      }
    }

    return lowestIdx;
  }

  /**
   * Get whether the shape requires a fill.
   *
   * @param i the index of the shape.
   * @return <code>true</code> if the shape requires a fill.
   */
  public static boolean isFill(int i) {
    return FILL[i];
  }

  /**
   * Get the stair material that corresponds to a base material and a shape.
   *
   * @param baseMaterial the base material.
   * @param i the index of the shape.
   * @return the stair material.
   */
  public static Material getStairMaterial(Material baseMaterial, int i) {

    String namespace = baseMaterial.namespace;
    String simpleName = baseMaterial.simpleName;

    // Assume all slab materials end with "_slab"
    if (simpleName.endsWith("_slab")) {
      simpleName = simpleName.substring(0, simpleName.length() - 5);
    }

    // Assume the stair material will end with "_stairs"
    return Material.get(namespace + ":" + simpleName + "_stairs", Constants.MC_FACING, FACING[i],
        Constants.MC_SHAPE,
        SHAPE[i], Constants.MC_HALF, "bottom");
  }
}
