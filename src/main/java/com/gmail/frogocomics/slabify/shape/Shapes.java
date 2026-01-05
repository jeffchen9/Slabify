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
import com.gmail.frogocomics.slabify.linalg.Matrix;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.Nullable;
import org.pepsoft.worldpainter.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

  public static void init() {
    // Populate shapes list
    shapesList.add(new SlabShape());
    shapesList.add(new StairShape());
    shapesList.add(new LayerShape());
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
    File configDir = Configuration.getConfigDir();
    File mappingFile = new File(configDir, "mappings.csv");
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
      indices[i] = shapesMap.inverse().get(shapes[i]);
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

      mappings.put(baseMaterial, arr);
    }
  }

  public static int[][][] findMostSimilarShapes(float[][] differenceMap, int resolution, List<Matrix> shapeMatrices) {
    int height = differenceMap.length / resolution;
    int width = differenceMap[0].length / resolution;
    int resolutionSquared = resolution * resolution;

    int[][][] shapeMap = new int[height][width][shapeMatrices.size()];
    float[] scratch = new float[resolutionSquared];
    long[] scratch2 = new long[shapeMatrices.size()];

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

        findMostSimilarShape(shapeMap[x][y], scratch, shapeMatrices, scratch2);
      }
    }

    return shapeMap;
  }

  public static void findMostSimilarShape(int[] target, float[] difference, List<Matrix> shapeMatrices, long[] scratch) {
    int size = shapeMatrices.size();

    for (int i = 0; i < size; i++) {
      float loss = shapeMatrices.get(i).getLoss(difference);
      scratch[i] = ((long) Float.floatToRawIntBits(loss) << 32) | (i & 0xFFFFFFFFL);
    }
    Arrays.sort(scratch);

    for (int i = 0; i < size; i++) {
      target[i] = (int) (scratch[i] & 0xFFFFFFFFL);
    }
  }

  @Nullable
  public static String getMaterial(Shape shape, String baseMaterial) {

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

    return availableShapes;
  }
}
