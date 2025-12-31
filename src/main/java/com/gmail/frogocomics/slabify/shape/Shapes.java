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

package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.worldpainter.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.IntStream;

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

  public static final Map<String, Shape> SHAPES = new LinkedHashMap<>();
  private static final List<String> shapesList = new ArrayList<>();
  private static final Map<String, Integer> shapesListInv = new HashMap<>();

  // Key: block, Values: block for each shape
  private static final Map<String, String[]> mapping = new HashMap<>();

  public static void init() {
    // Populate shapes list
    SHAPES.put(SlabShape.NAME, new SlabShape());
    SHAPES.put(StairShape.NAME, new StairShape());
    SHAPES.put(LayerShape.NAME, new LayerShape());
    SHAPES.put(VerticalCornerShape.NAME, new VerticalCornerShape());
    SHAPES.put(QuarterSlabShape.NAME, new QuarterSlabShape());
    SHAPES.put(VerticalQuarterShape.NAME, new VerticalQuarterShape());
    SHAPES.put(CornerSlabShape.NAME, new CornerSlabShape());
    SHAPES.put(VerticalCornerSlabShape.NAME, new VerticalCornerSlabShape());
    SHAPES.put(EighthSlabShape.NAME, new EighthSlabShape());
    SHAPES.put(VerticalSlabShape.NAME, new VerticalSlabShape());
    shapesList.addAll(SHAPES.keySet());

    // Create inverse
    for (int i = 0; i < shapesList.size(); i++) {
      shapesListInv.put(shapesList.get(i), i);
    }

    // Get the block mapping list
    File configDir = Configuration.getConfigDir();
    File mappingFile =  new File(configDir, "mappings.csv");
    if (!mappingFile.exists()) {
      // Create the default file

      try (InputStream stream = Shapes.class.getClassLoader().getResourceAsStream("mappings.csv")) {
        if (stream != null) {
          Files.copy(stream, mappingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
          throw new IOException("mappings.csv not found");
        }
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }

    // Load the file
    List<String[]> rows = new ArrayList<>();
    String headerRow = null;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(mappingFile.toPath())));
      String line;
      boolean isHeader = true;
      while ((line = reader.readLine()) != null) {
        if (isHeader) {
          // Skip header row
          headerRow = line;
          isHeader = false;
          continue;
        }
        rows.add(line.split(","));
      }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    String[] shapes = headerRow.split(",");
    // In = csv index; out = index in shapes list
    int[] indices = new int[shapes.length];

    for (int i = 1; i < shapes.length; i++) {
      // Skip first element
      indices[i] = shapesList.indexOf(shapes[i]);
    }

    for (String[] row : rows) {

      String[] arr = new String[shapes.length];
      String baseMaterial = row[0].contains(":") ? row[0] : Constants.MC_NAMESPACE + ":" + row[0];
      for (int i = 1; i < row.length; i++) {
        // Skip first element
        if (row[i].isEmpty()) {
          arr[indices[i]] = null;
        } else {
          arr[indices[i]] = row[i].contains(":") ? row[i] : Constants.MC_NAMESPACE + ":" + row[i];
        }
      }

      mapping.put(baseMaterial, arr);
    }
  }

  private Shapes() {
    // Prevent instantiation
  }

  public static int[][][] findMostSimilarShapes(float[][] differenceMap, int resolution, List<Matrix> shapeMatrices, double exponent) {
    int height = differenceMap.length / resolution;
    int width = differenceMap[0].length / resolution;

    int[][][] shapeMap = new int[height][width][shapeMatrices.size()];

    for (int x = 0; x < height; x++) {
      for (int y = 0; y < width; y++) {

        float[][] difference = new float[resolution][resolution];

        for (int i1 = 0; i1 < resolution; i1++) {
          for (int i2 = 0; i2 < resolution; i2++) {
            difference[i1][i2] = differenceMap[x * resolution + i1][y * resolution + i2];
          }
        }

        shapeMap[x][y] = findMostSimilarShape(difference, shapeMatrices, exponent);
      }
    }

    return shapeMap;
  }

  public static int[] findMostSimilarShape(float[][] difference, List<Matrix> shapeMatrices, double exponent) {
    Matrix differenceMatrix = new Matrix(difference);

    float[] lossArr = new float[shapeMatrices.size()];

    for (int i = 0; i < shapeMatrices.size(); i++) {
      Matrix lossMatrix = differenceMatrix.clone();
      lossMatrix.sub(shapeMatrices.get(i));
      lossMatrix.pow(exponent);
      lossArr[i] = lossMatrix.sum();
    }

    return IntStream.range(0, lossArr.length)
            .boxed()
            .sorted(Comparator.comparingDouble(i -> lossArr[i]))
            .mapToInt(i -> i)
            .toArray();
  }

  @Nullable
  public static String getMaterial(Shape shape, String baseMaterial) {
    return mapping.containsKey(baseMaterial) ? mapping.get(baseMaterial)[shapesListInv.get(shape.getName())] : null;
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

    if (mapping.containsKey(baseMaterial)) {
      String[] s = mapping.get(baseMaterial);

      for (int i = 0; i < s.length; i++) {
        if (s[i] != null) {
          availableShapes.add(shapesList.get(i));
        }
      }
    }

    return availableShapes;
  }
}
