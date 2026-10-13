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
        String baseMaterial = Utils.withNamespace(row[0]);
        for (int i = 1; i < row.length; i++) {
          // Skip first element
          if (row[i].isEmpty()) {
            arr[indices[i]] = null;
          } else {
            arr[indices[i]] = Utils.withNamespace(row[i]);
          }
        }

        mappings.put(baseMaterial, arr);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Find the most similar shapes, with stacking. Unlike
   * {@link #findMostSimilarShapes(float[][], int, List, List, boolean, boolean[][])}, each cell only covers the range of
   * z values it needs, rather than the range of the whole difference map.
   *
   * @param differenceMap        the difference map.
   * @param resolution           the upscaled resolution.
   * @param shapeMatrices        a list of all the available shape matrices.
   * @param shapeMatricesStacked a list of all the available shape matrices for stacked shapes.
   * @param mask                 {@code true} for each cell that needs shapes; other cells are skipped.
   * @return the most similar shapes as a shapemap. The final dimension is an array of indices in the order of
   * closeness.
   */
  public static Shapemap findMostSimilarShapesRagged(float[][] differenceMap, int resolution, List<Matrix> shapeMatrices,
                                               List<Matrix> shapeMatricesStacked, boolean[][] mask) {
    int height = differenceMap.length / resolution;
    int width = differenceMap[0].length / resolution;
    int resolutionSquared = resolution * resolution;

    int maxShapeSize = Math.max(shapeMatrices.size(), shapeMatricesStacked.size());

    float[] scratch = new float[resolutionSquared];
    float[] scratch2 = new float[resolutionSquared];
    float[] scratch3 = new float[resolutionSquared];
    float[] scratch4 = new float[resolutionSquared];
    long[] scratch5 = new long[maxShapeSize];

    int totalMapSize = 0;

    int[] minZ = new int[height * width];
    int[] maxZ = new int[height * width];
    int[] offsets = new int[height * width];

    for (int x = 0; x < height; x++) {
      int rowBase = x * resolution;
      for (int y = 0; y < width; y++) {
        if (!mask[x][y]) {
          continue;
        }

        int colBase = y * resolution;
        float localMin = Float.POSITIVE_INFINITY;
        float localMax = Float.NEGATIVE_INFINITY;

        for (int i1 = 0; i1 < resolution; i1++) {
          for (int j1 = 0; j1 < resolution; j1++) {
            float val = differenceMap[rowBase + i1][colBase + j1];
            if (val < localMin) {
              localMin = val;
            }
            if (val > localMax) {
              localMax = val;
            }
          }
        }

        int localMaxHeight = (int) Math.ceil(localMax);
        int localMinHeight = (int) Math.floor(localMin) - 1;

        int flatXY = (x << 7) | y;
        minZ[flatXY] = localMinHeight;
        maxZ[flatXY] = localMaxHeight;
        offsets[flatXY] = totalMapSize;
        totalMapSize += (localMaxHeight - localMinHeight) * maxShapeSize;
      }
    }

    short[] shapeMap = new short[totalMapSize];
    Arrays.fill(shapeMap, (short) -1);

    for (int x = 0; x < height; x++) {
      int rowBase = x * resolution;
      for (int y = 0; y < width; y++) {
        if (!mask[x][y]) {
          continue;
        }

        int colBase = y * resolution;
        int flatXY = (x << 7) | y;
        int localMinHeight = minZ[flatXY];
        int localVertDiff = maxZ[flatXY] - localMinHeight;
        int baseOffset = offsets[flatXY];
        for (int i1 = 0; i1 < resolution; i1++) {
          System.arraycopy(
              differenceMap[rowBase + i1], colBase, scratch, i1 * resolution, resolution
          );
        }

        for (int i = 0; i < resolutionSquared; i++) {
          scratch[i] -= localMinHeight;
        }

        rankLevels(shapeMap, baseOffset, localVertDiff, maxShapeSize, scratch, scratch2, scratch3, scratch4, scratch5,
            shapeMatrices, shapeMatricesStacked);
      }
    }

    return new RaggedStackedShapemap(shapeMap, minZ, maxZ, offsets, maxShapeSize);
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
                                               List<Matrix> shapeMatricesStacked, boolean stacking, boolean[][] mask) {
    int height = differenceMap.length / resolution;
    int width = differenceMap[0].length / resolution;
    int resolutionSquared = resolution * resolution;
    int shapeSize = shapeMatrices.size();
    int maxShapeSize = Math.max(shapeMatrices.size(), shapeMatricesStacked.size());
    float[] scratch = new float[resolutionSquared];
    float[] scratch2 = new float[resolutionSquared];
    float[] scratch3 = new float[resolutionSquared];
    float[] scratch4 = new float[resolutionSquared];
    long[] scratch5 = new long[shapeMatrices.size()];

    if (stacking) {
      // Determine global bounds
      Pair<Float, Float> maxMin = Utils.findMinAndMax(differenceMap, mask, resolution);
      int maxHeight = (int) Math.ceil(maxMin.getValue0());
      int minHeight = (int) Math.floor(maxMin.getValue1());
      int vertDiff = maxHeight - minHeight;

      short[] shapeMap = new short[height * width * vertDiff * maxShapeSize];
      Arrays.fill(shapeMap, (short) -1);

      // Make minHeight equal to 0
      for (int i = 0; i < differenceMap.length; i++) {
        for (int j = 0; j < differenceMap[0].length; j++) {
          differenceMap[i][j] -= minHeight;
        }
      }

      for (int x = 0; x < height; x++) {
        int rowBase = x * resolution;
        for (int y = 0; y < width; y++) {
          if (!mask[x][y]) {
            continue;
          }

          int colBase = y * resolution;
          int flatXY = (x << 7) | y;
          int baseOffset = flatXY * vertDiff * maxShapeSize;

          for (int i1 = 0; i1 < resolution; i1++) {
            System.arraycopy(
                differenceMap[rowBase + i1],
                colBase,
                scratch,
                i1 * resolution,
                resolution
            );
          }

          rankLevels(shapeMap, baseOffset, vertDiff, maxShapeSize, scratch, scratch2, scratch3, scratch4, scratch5,
              shapeMatrices, shapeMatricesStacked);
        }
      }

      return new StackedShapemap(shapeMap, minHeight, maxHeight, maxShapeSize);
    } else {
      short[] shapeMap = new short[height * width * shapeSize];

      for (int x = 0; x < height; x++) {
        int rowBase = x * resolution;
        for (int y = 0; y < width; y++) {
          int colBase = y * resolution;
          int targetOffset = ((x << 7) | y) * shapeSize;

          for (int i1 = 0; i1 < resolution; i1++) {
            System.arraycopy(
                differenceMap[rowBase + i1],
                colBase,
                scratch,
                i1 * resolution,
                resolution
            );
          }

          findMostSimilarShape(shapeMap, targetOffset, scratch, shapeMatrices, scratch5);
        }
      }

      return new FlatShapemap(shapeMap, shapeSize);
    }
  }

  /**
   * Rank the shapes for each level of one cell, from the top down. Levels use the full shape list until the first one
   * whose closest shape is not empty, and the stackable shape list below it.
   *
   * @param scratch the cell's difference values, relative to the bottom of the lowest level.
   */
  private static void rankLevels(short[] shapeMap, int baseOffset, int levels, int maxShapeSize, float[] scratch,
                                 float[] scratch2, float[] scratch3, float[] scratch4, long[] scratch5,
                                 List<Matrix> shapeMatrices, List<Matrix> shapeMatricesStacked) {
    int fullIdx = shapeMatrices.size() - 2;
    int emptyIdx = shapeMatrices.size() - 1;
    int fullStackedIdx = shapeMatricesStacked.size() - 2;
    boolean top = true;

    for (int i = levels - 1; i >= 0; i--) {
      int targetOffset = baseOffset + (i * maxShapeSize);

      for (int j = 0; j < scratch.length; j++) {
        float val = scratch[j] - i;
        scratch2[j] = val;
        scratch3[j] = Math.max(0f, val);
        scratch4[j] = Math.min(1f, val);
      }

      if (Utils.allAtOrBelowZero(scratch2)) {
        shapeMap[targetOffset] = (short) emptyIdx;
      } else if (Utils.allAtOrAboveOne(scratch2)) {
        shapeMap[targetOffset] = (short) (top ? fullIdx : fullStackedIdx);
      } else {
        findMostSimilarShape(shapeMap, targetOffset, scratch2, scratch3, scratch4,
            top ? shapeMatrices : shapeMatricesStacked, scratch5);
      }

      if (shapeMap[targetOffset] != emptyIdx) {
        top = false;
      }
    }
  }

  /**
   * Rank all shapes by similarity. The last two matrices must be the full and empty shapes, which are allowed for every
   * block, so the ranking is never read past the better of the two. Other shapes therefore stop being scored once their
   * loss exceeds its loss; such shapes still rank after it, so every index that can be read is unchanged.
   */
  public static void findMostSimilarShape(short[] flatMap, int targetOffset, float[] difference, List<Matrix> matrices, long[] scratch) {
    int size = matrices.size();
    float fullLoss = matrices.get(size - 2).getLoss(difference, Constants.LOSS_EXPONENT, Float.POSITIVE_INFINITY);
    float emptyLoss = matrices.get(size - 1).getLoss(difference, Constants.LOSS_EXPONENT, Float.POSITIVE_INFINITY);
    float bound = Math.min(fullLoss, emptyLoss);

    for (int i = 0; i < size - 2; i++) {
      scratch[i] = sortKey(matrices.get(i).getLoss(difference, Constants.LOSS_EXPONENT, bound), i);
    }
    scratch[size - 2] = sortKey(fullLoss, size - 2);
    scratch[size - 1] = sortKey(emptyLoss, size - 1);
    Arrays.sort(scratch, 0, size);

    for (int i = 0; i < size; i++) {
      flatMap[targetOffset + i] = (short) (scratch[i] & 0xFFFFFFFFL);
    }
  }

  /**
   * Rank all shapes by similarity using the clipped loss. Stops scoring early in the same way as
   * {@link #findMostSimilarShape(short[], int, float[], List, long[])}.
   */
  public static void findMostSimilarShape(short[] flatMap, int targetOffset, float[] differenceUnclip, float[] differenceMin0, float[] differenceMax1, List<Matrix> matrices, long[] scratch) {
    int size = matrices.size();
    float fullLoss = matrices.get(size - 2).getLossClip(differenceUnclip, differenceMin0, differenceMax1,
        Constants.LOSS_EXPONENT, Float.POSITIVE_INFINITY);
    float emptyLoss = matrices.get(size - 1).getLossClip(differenceUnclip, differenceMin0, differenceMax1,
        Constants.LOSS_EXPONENT, Float.POSITIVE_INFINITY);
    float bound = Math.min(fullLoss, emptyLoss);

    for (int i = 0; i < size - 2; i++) {
      scratch[i] = sortKey(matrices.get(i).getLossClip(differenceUnclip, differenceMin0, differenceMax1,
          Constants.LOSS_EXPONENT, bound), i);
    }
    scratch[size - 2] = sortKey(fullLoss, size - 2);
    scratch[size - 1] = sortKey(emptyLoss, size - 1);
    Arrays.sort(scratch, 0, size);

    for (int i = 0; i < size; i++) {
      flatMap[targetOffset + i] = (short) (scratch[i] & 0xFFFFFFFFL);
    }
  }

  /**
   * Pack a loss and shape index into a key that sorts by loss, then by index. Losses are never negative, so their bits
   * sort in the same order as their values.
   */
  private static long sortKey(float loss, int i) {
    return ((long) Float.floatToRawIntBits(loss) << 32) | (i & 0xFFFFFFFFL);
  }

  /**
   * Get the block to use for a shape of a material.
   *
   * @param shape        the shape.
   * @param baseMaterial the name of the material.
   * @return the name of the block, or {@code null} if the shape is not available for the material.
   */
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
   * Check if a material is in the block mapping.
   *
   * @param baseMaterial the name of the material.
   * @return {@code true} if the material is in the block mapping.
   */
  public static boolean isAvailable(String baseMaterial) {
    return mappings.containsKey(baseMaterial);
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
