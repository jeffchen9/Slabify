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

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.SlabifyWPPlugin;
import com.gmail.frogocomics.slabify.linalg.Matrix;
import com.gmail.frogocomics.slabify.utils.Utils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.javatuples.Pair;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for assigning the most appropriate shape to use to add detail to terrain.
 */
public final class Shapes {
  public static final List<Shape> shapesList = new ArrayList<>();
  public static final List<Shape> stackableShapesList = new ArrayList<>();
  public static final BiMap<Integer, String> shapesMap = HashBiMap.create();

  // Key: block, Values: block for each shape
  private static final Map<String, String[]> mappings = new HashMap<>();

  private Shapes() {
    // Prevent instantiation
  }

  /**
   * Initialized in {@link SlabifyWPPlugin}.
   */
  public static void init() {
    // Populate shapes list
    // Vanilla shapes
    shapesList.add(new SlabShape());
    shapesList.add(new StairShape());
    shapesList.add(new LayerShape());
    shapesList.add(new HeadShape());
    // Conquest shapes
    shapesList.add(new AltLayerShape());
    shapesList.add(new VerticalCornerShape());
    shapesList.add(new QuarterSlabShape());
    shapesList.add(new VerticalQuarterShape());
    shapesList.add(new CornerSlabShape());
    shapesList.add(new VerticalCornerSlabShape());
    shapesList.add(new EighthSlabShape());
    shapesList.add(new VerticalSlabShape());

    for (int i = 0; i < shapesList.size(); i++) {
      shapesMap.put(i, shapesList.get(i).getName());

      if (shapesList.get(i).supportsStacking()) {
        stackableShapesList.add(shapesList.get(i));
      }
    }

    // Get the block mapping list
    File mappingFile = Utils.addFileToAppData(Constants.MAPPING_NAME);

    // Load the file
    try {
      List<String[]> rows = Utils.readCsv(mappingFile);

      // Get and remove header row
      String[] shapes = rows.get(0);
      rows.remove(0);
      // In = csv index; out = index in shapes list
      int[] indices = new int[shapes.length];

      for (int i = 1; i < shapes.length; i++) {
        // Skip first element
        indices[i] = shapesMap.inverse().get(shapes[i]);
      }

      for (String[] row : rows) {
        String[] arr = new String[shapesList.size()];
        String baseMaterial = row[0].contains(":") ? row[0] : Constants.MC_NAMESPACE + ":" + row[0];
        for (int i = 1; i < row.length; i++) {
          // Skip first element
          if (row[i].isEmpty()) {
            arr[indices[i]] = null;
          } else {
            arr[indices[i]] = row[i].contains(":") ? row[i] : Constants.MC_NAMESPACE + ":" + row[i];
          }
        }

        mappings.put(baseMaterial, arr);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Find the most similar shapes.
   *
   * @param differenceMap        the difference map.
   * @param resolution           the upscaled resolution.
   * @param shapeMatrices        a list of all the available shape matrices.
   * @param shapeMatricesStacked a list of all the available shape matrices for stacked shapes.
   * @param stacking             {@code true} if stacked shapes are available.
   * @return the most similar shapes as a shapemap. The final dimension is an array of indices in the order of
   * closeness.
   */
  public static Shapemap findMostSimilarShapes(float[][] differenceMap, int resolution, List<Matrix> shapeMatrices,
                                               List<Matrix> shapeMatricesStacked, boolean stacking) {
    int height = differenceMap.length / resolution;
    int width = differenceMap[0].length / resolution;
    int resolutionSquared = resolution * resolution;
    float[] scratch = new float[resolutionSquared];
    float[] scratch2 = new float[resolutionSquared];
    long[] scratch3 = new long[shapeMatrices.size()];

    if (stacking) {
      // Determine global bounds
      Pair<Float, Float> maxMin = Utils.findMinAndMax(differenceMap);
      int maxHeight = (int) Math.round(Math.ceil(maxMin.getValue0()));
      int minHeight = (int) Math.round(Math.floor(maxMin.getValue1()));
      int vertDiff = maxHeight - minHeight;
      int fullIdx = shapeMatrices.size() - 2;
      int emptyIdx = shapeMatrices.size() - 1;
      int fullStackedIdx = shapeMatricesStacked.size() - 2;

      int[][][][] shapeMap = new int[height][width][vertDiff][shapeMatrices.size()];

      // Make minHeight equal to 0
      for (int i = 0; i < differenceMap.length; i++) {
        for (int j = 0; j < differenceMap[0].length; j++) {
          differenceMap[i][j] -= minHeight;
        }
      }

      for (int x = 0; x < height; x++) {
        int rowBase = x * resolution;
        for (int y = 0; y < width; y++) {
          int colBase = y * resolution;

          int k = 0;

          for (int i1 = 0; i1 < resolution; i1++) {
            for (int i2 = 0; i2 < resolution; i2++) {
              scratch[k] = differenceMap[rowBase + i1][colBase + i2];
              k += 1;
            }
          }

          boolean top = true;

          for (int i = vertDiff - 1; i >= 0; i--) {
            for (int j = 0; j < resolutionSquared; j++) {
              // Scratch should always be between 1 and 0
              scratch2[j] = Math.max(i, Math.min(i + 1, scratch[j])) - i;
            }

            if (Utils.allZeros(scratch2)) {
              shapeMap[x][y][i][0] = emptyIdx;
            } else if (Utils.allOnes(scratch2)) {
              shapeMap[x][y][i][0] = top ? fullIdx : fullStackedIdx;
            } else {
              Shapes.findMostSimilarShape(shapeMap[x][y][i], scratch2, top ? shapeMatrices : shapeMatricesStacked, scratch3);
            }

            if (shapeMap[x][y][i][0] != emptyIdx) {
              top = false;
            }
          }

        }
      }

      return new StackedShapemap(shapeMap, minHeight, maxHeight);
    } else {
      int[][][] shapeMap = new int[height][width][shapeMatrices.size()];

      for (int x = 0; x < height; x++) {
        int rowBase = x * resolution;
        for (int y = 0; y < width; y++) {
          int colBase = y * resolution;

          for (int i1 = 0; i1 < resolution; i1++) {
            System.arraycopy(
                differenceMap[rowBase + i1],
                colBase,
                scratch,
                i1 * resolution,
                resolution
            );
          }

          findMostSimilarShape(shapeMap[x][y], scratch, shapeMatrices, scratch3);
        }
      }

      return new FlatShapemap(shapeMap);
    }
  }

  /**
   * Find the most similar shapes.
   *
   * @param target     the array to write the output to, as indices ordered by similarity.
   * @param difference the difference between the terrain height and the heightmap height.
   * @param matrices   a list of the matrices of available shapes.
   * @param scratch    a buffer with a length greater or equal to {@code matrices}.
   */
  public static void findMostSimilarShape(int[] target, float[] difference, List<Matrix> matrices, long[] scratch) {
    int size = matrices.size();

    for (int i = 0; i < size; i++) {
      float loss = matrices.get(i).getLoss(difference, Constants.LOSS_EXPONENT);
      scratch[i] = ((long) Float.floatToRawIntBits(loss) << 32) | (i & 0xFFFFFFFFL);
    }
    Arrays.sort(scratch, 0, size);

    for (int i = 0; i < size; i++) {
      target[i] = (int) (scratch[i] & 0xFFFFFFFFL);
    }
  }

  @Nullable
  public static String getMaterial(Shape shape, String baseMaterial) {

    // Special treatment for heads
    if (shape instanceof HeadShape && HeadShape.getHeads().containsKey(baseMaterial)) {
      return HeadShape.BLOCK;
    }

    String[] row = mappings.get(baseMaterial);

    if (row == null) {
      return null;
    }

    Integer index = shapesMap.inverse().get(shape.getName());
    return index != null ? row[index] : null;
  }

  /**
   * Get a list of the shapes available for a material.
   *
   * @param baseMaterial the material.
   * @return the shapes available for the material.
   */
  public static List<String> getAvailableShapes(String baseMaterial) {
    List<String> availableShapes = new ArrayList<>();

    // Empty and full are ALWAYS available
    availableShapes.add(EmptyShape.NAME);
    availableShapes.add(FullShape.NAME);

    if (mappings.containsKey(baseMaterial)) {
      String[] s = mappings.get(baseMaterial);

      for (int i = 0; i < s.length; i++) {
        if (s[i] != null) {
          availableShapes.add(shapesMap.get(i));
        }
      }
    }

    // Special treatment for heads
    if (!availableShapes.contains(HeadShape.NAME) && HeadShape.getHeads().containsKey(baseMaterial)) {
      availableShapes.add(HeadShape.NAME);
    }

    return availableShapes;
  }
}
