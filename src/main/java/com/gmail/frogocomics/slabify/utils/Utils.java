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

package com.gmail.frogocomics.slabify.utils;

import static org.bytedeco.onnxruntime.global.onnxruntime.OrtArenaAllocator;
import static org.bytedeco.onnxruntime.global.onnxruntime.OrtMemTypeDefault;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.onnxruntime.MemoryInfo;
import org.bytedeco.onnxruntime.RunOptions;
import org.bytedeco.onnxruntime.Session;
import org.bytedeco.onnxruntime.Value;
import org.bytedeco.onnxruntime.ValueVector;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;

/**
 * Utility class.
 */
public final class Utils {

  private Utils() {
    // Prevent instantiation
  }

  /**
   * Write a float array to a normalized 16-bit grayscale image.
   *
   * @param data       the float array.
   * @param outputFile the output location.
   * @param format     the image format.
   * @throws IOException when there is an issue with image creation.
   */
  public static void exportFloatArrayToImage(float[][] data, File outputFile, String format)
      throws IOException {
    int width = data.length;
    int height = data[0].length;

    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        float value = data[x][y];
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    }

    float range = max - min;

    if (range == 0) {
      range = 1;
    }

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        float value = data[x][y];
        int gray = (int) ((value - min) / range * 255);
        gray = Math.max(0, Math.min(255, gray));
        image.getRaster().setSample(x, y, 0, gray);
      }
    }

    ImageIO.write(image, format, outputFile);
  }

  /**
   * Get the average color of an image.
   *
   * @param image the image.
   * @return the average color.
   */
  public static Color averageColor(BufferedImage image) {

    int width = image.getWidth();
    int height = image.getHeight();

    long sumRed = 0, sumGreen = 0, sumBlue = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        // Strip alpha information
        Color pixel = new Color(image.getRGB(x, y));
        sumRed += pixel.getRed();
        sumGreen += pixel.getGreen();
        sumBlue += pixel.getBlue();
      }
    }
    int num = width * height;

    return new Color((int) (sumRed / num), (int) (sumGreen / num), (int) (sumBlue / num));
  }

  /**
   * Normalize a float array between 0 and 1. This is in-place.
   *
   * @param arr      the float array.
   * @param maxValue the maximum value in the array.
   * @param minValue the minimum value in the array.
   */
  public static void normalize(float[][] arr, float maxValue, float minValue) {
    float range = maxValue - minValue;
    int height = arr.length;
    int width = arr[0].length;

    if (range == 0) { // Avoid division by 0
      for (float[] d : arr) {
        Arrays.fill(d, 0);
      }
    } else {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          arr[y][x] = (arr[y][x] - minValue) / range;
        }
      }
    }
  }

  /**
   * Unnormalize a float array to its original range. This is in-place.
   *
   * @param arr      the float array.
   * @param maxValue the maximum value in the original range, which corresponds to 1.
   * @param minValue the minimum value in the original range, which corresponds to 0.
   */
  public static void unnormalize(float[][] arr, float maxValue, float minValue) {
    float range = maxValue - minValue;
    int height = arr.length;
    int width = arr[0].length;

    if (range != 0) { // Avoid division by 0
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          arr[y][x] = arr[y][x] * range + minValue;
        }
      }
    }
  }

  /**
   * Remap a float array to a difference range. This is in-place.
   *
   * @param arr       the float array.
   * @param maxValue1 the maximum value in the original range.
   * @param minValue1 the minimum value in the original range.
   * @param maxValue2 the maximum value in the target range.
   * @param minValue2 the minimum value in the target range.
   */
  public static void remap(float[][] arr, float maxValue1, float minValue1, float maxValue2,
      float minValue2) {
    float range1 = maxValue1 - minValue1;
    float range2 = maxValue2 - minValue2;
    int height = arr.length;
    int width = arr[0].length;

    if (range1 != 0 && range2 != 0) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          arr[y][x] = (arr[y][x] - minValue1) / range1 * range2 + minValue2;
        }
      }
    }
  }

  /**
   * Get the difference between the upscaled (2x) height and the original height.
   *
   * @param upscaledMap the upscaled map, as a float array.
   * @param originalMap the original map, as an integer array.
   * @return the difference, as a float array.
   */
  public static float[][] getDifference(float[][] upscaledMap, int[][] originalMap) {
    int height = TILE_SIZE;
    int width = TILE_SIZE;

    float[][] differenceMap = new float[height * 2][width * 2];

    for (int x = 0; x < height; x++) {
      for (int y = 0; y < width; y++) {
        differenceMap[2 * x][2 * y] = (upscaledMap[2 * x][2 * y] - originalMap[x][y]) * 2;
        differenceMap[2 * x + 1][2 * y] = (upscaledMap[2 * x + 1][2 * y] - originalMap[x][y]) * 2;
        differenceMap[2 * x][2 * y + 1] = (upscaledMap[2 * x][2 * y + 1] - originalMap[x][y]) * 2;
        differenceMap[2 * x + 1][2 * y + 1] =
            (upscaledMap[2 * x + 1][2 * y + 1] - originalMap[x][y]) * 2;
      }
    }

    return differenceMap;
  }

  /**
   * Upscale a tile using neural network-powered interpolation.
   *
   * @param tile      the tile to upscale.
   * @param dimension the dimension the tile belongs to.
   * @param session   the current ONNX session containing the loaded super-resolution model.
   * @return the upscaled heightmap as a float array.
   */
  public static float[][] upscaleTile(Tile tile, Dimension dimension, Session session) {
    int pad = 16;
    int twoPad = pad * 2;
    float[][] padded = padTile(tile, dimension, pad);

    // Normalize between 0 and 1
    normalize(padded, dimension.getMaxHeight(), dimension.getMinHeight());

    // Duplicate grayscale along R, G, B channels
    int totalElements = 3 * (TILE_SIZE + twoPad) * (TILE_SIZE + twoPad) * 4;

    PointerPointer<BytePointer> inputNames = new PointerPointer<>(1);
    BytePointer inputName = new BytePointer("input");
    inputNames.put(0, inputName);
    PointerPointer<BytePointer> outputNames = new PointerPointer<>(1);
    BytePointer outputName = new BytePointer("output");
    outputNames.put(0, outputName);

    Value inputTensor = createInputTensor(padded);

    // Run
    ValueVector outputValue = session.Run(new RunOptions(), inputNames, inputTensor, 1, outputNames,
        1);
    Value out = outputValue.get(0);
    FloatPointer floatArr = out.GetTensorMutableDataFloat();

    float[] data = new float[totalElements];
    floatArr.get(data);

    // Clamp values just in case
    for (int i = 0; i < data.length; i++) {
      data[i] = Math.min(1, Math.max(0, data[i]));
    }

    // Extract channels and compute grayscale
    int height = (TILE_SIZE + 2 * pad) * 2;
    int width = (TILE_SIZE + 2 * pad) * 2;
    float[][] sr = new float[TILE_SIZE * 2][TILE_SIZE * 2];

    for (int y = twoPad; y < sr.length + twoPad; y++) {
      for (int x = twoPad; x < sr[0].length + twoPad; x++) {
        int rIndex = y * width + x;
        int gIndex = height * width + y * width + x;
        int bIndex = 2 * height * width + y * width + x;

        // Grayscale luminance
        sr[y - twoPad][x - twoPad] =
            0.299f * data[rIndex] + 0.587f * data[gIndex] + 0.114f * data[bIndex];
      }
    }

    return sr;
  }

  /**
   * Create a tensor from a 2D float array.
   *
   * @param grayscale the float array.
   * @return the tensor.
   */
  public static Value createInputTensor(float[][] grayscale) {
    int height = grayscale.length;
    int width = grayscale[0].length;

    int numElements = height * width * 3;
    FloatPointer pointer = new FloatPointer(numElements);
    FloatIndexer idx = FloatIndexer.create(pointer);

    int i = 0;
    for (int c = 0; c < 3; c++) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          idx.put(i, grayscale[y][x]);
          i++;
        }
      }
    }

    long[] shape = new long[]{1, 3, height, width};

    LongPointer inputNodeDims = new LongPointer(4);
    inputNodeDims.put(shape);

    return Value.CreateTensorFloat(
        MemoryInfo.CreateCpu(OrtArenaAllocator, OrtMemTypeDefault).asOrtMemoryInfo(),
        pointer, numElements, inputNodeDims, shape.length);
  }

  /**
   * Get a heightmap from a {@link Tile} with padding from neighboring {@link Tile}s.
   *
   * @param tile      the tile to get the heightmap from.
   * @param dimension the dimension the tile belongs to.
   * @param pad       the padding on each side.
   * @return the heightmap as a float array.
   */
  public static float[][] padTile(Tile tile, Dimension dimension, int pad) {
    int tileX = tile.getX();
    int tileY = tile.getY();

    // Padding is on each side
    int paddedSize = TILE_SIZE + 2 * pad;

    float[][] padded = new float[paddedSize][paddedSize];

    // Fill center from current tile
    for (int x = 0; x < TILE_SIZE; x++) {
      for (int y = 0; y < TILE_SIZE; y++) {
        padded[x + pad][y + pad] = tile.getHeight(x, y);
      }
    }

    // Fill borders from neighbors or pad edge
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) {
          // Already filled
          continue;
        }

        Tile neighborTile = dimension.getTile(tileX + dx, tileY + dy);

        for (int x = 0; x < TILE_SIZE; x++) {
          for (int y = 0; y < TILE_SIZE; y++) {
            int px = x + dx * TILE_SIZE + pad;
            int py = y + dy * TILE_SIZE + pad;

            if (px >= 0 && px < paddedSize && py >= 0 && py < paddedSize) {
              float value;

              if (neighborTile != null) {
                value = neighborTile.getHeight(x, y);
              } else {
                int cx = Math.max(pad, Math.min(paddedSize - pad - 1, px));
                int cy = Math.max(pad, Math.min(paddedSize - pad - 1, py));
                value = padded[cx][cy];
              }
              padded[px][py] = value;
            }
          }
        }
      }
    }

    return padded;
  }

  /**
   * Upscale a tile using basic interpolation.
   *
   * @param tile      the tile to upscale.
   * @param dimension the dimension the tile belongs to.
   * @param method    the interpolation method to use. The method must be either
   *                  {@link Interpolation#BICUBIC} or {@link Interpolation#BILINEAR}.
   * @return the upscaled heightmap as a float array.
   */
  public static float[][] upscaleTile(Tile tile, Dimension dimension, Interpolation method) {

    if (method == Interpolation.HEIGHTMAP || method == Interpolation.NEUTRAL_NETWORK) {
      throw new IllegalArgumentException("This method should not be called.");
    }

    float[][] padded = padTile(tile, dimension, 2);
    return method == Interpolation.BICUBIC ? bicubic(padded) : bilinear(padded);
  }

  /**
   * Bicubic interpolation. Assumes padding of 2.
   *
   * @param paddedInput the padded input.
   * @return the upscaled heightmap.
   */
  private static float[][] bicubic(float[][] paddedInput) {
    int paddedWidth = paddedInput.length;
    int paddedHeight = paddedInput[0].length;

    int tileWidth = paddedWidth - 4;
    int tileHeight = paddedHeight - 4;

    int upscaledWidth = tileWidth * 2;
    int upscaledHeight = tileHeight * 2;

    float[][] output = new float[upscaledWidth][upscaledHeight];

    for (int x = 0; x < upscaledWidth; x++) {
      float srcX = x / 2.0f;
      int xInt = (int) Math.floor(srcX);
      float dx = srcX - xInt;

      for (int y = 0; y < upscaledHeight; y++) {
        float srcY = y / 2.0f;
        int yInt = (int) Math.floor(srcY);
        float dy = srcY - yInt;

        // Adjust for padding offset
        int px = xInt + 2;
        int py = yInt + 2;

        float[] col = new float[4];
        for (int m = -1; m <= 2; m++) {
          float[] row = new float[4];
          for (int n = -1; n <= 2; n++) {
            row[n + 1] = paddedInput[px + n][py + m];
          }
          col[m + 1] = cubicInterpolate(row[0], row[1], row[2], row[3], dx);
        }

        output[x][y] = cubicInterpolate(col[0], col[1], col[2], col[3], dy);
      }
    }

    return output;
  }

  private static float cubicInterpolate(float p0, float p1, float p2, float p3, float t) {
    float a0 = -0.5f * p0 + 1.5f * p1 - 1.5f * p2 + 0.5f * p3;
    float a1 = p0 - 2.5f * p1 + 2.0f * p2 - 0.5f * p3;
    float a2 = -0.5f * p0 + 0.5f * p2;
    return ((a0 * t + a1) * t + a2) * t + p1;
  }

  /**
   * Bilinear interpolation. Assumes padding of 2.
   *
   * @param paddedInput the padded input.
   * @return the upscaled heightmap.
   */
  private static float[][] bilinear(float[][] paddedInput) {
    int paddedWidth = paddedInput.length;
    int paddedHeight = paddedInput[0].length;

    int tileWidth = paddedWidth - 4;
    int tileHeight = paddedHeight - 4;

    int upscaledWidth = tileWidth * 2;
    int upscaledHeight = tileHeight * 2;

    float[][] output = new float[upscaledWidth][upscaledHeight];

    for (int x = 0; x < upscaledWidth; x++) {
      float srcX = x / 2.0f;
      int x0 = (int) Math.floor(srcX);
      int x1 = x0 + 1;
      float dx = srcX - x0;

      for (int y = 0; y < upscaledHeight; y++) {
        float srcY = y / 2.0f;
        int y0 = (int) Math.floor(srcY);
        int y1 = y0 + 1;
        float dy = srcY - y0;

        // Shift for padding
        int px0 = x0 + 2;
        int px1 = x1 + 2;
        int py0 = y0 + 2;
        int py1 = y1 + 2;

        float v00 = paddedInput[px0][py0];
        float v10 = paddedInput[px1][py0];
        float v01 = paddedInput[px0][py1];
        float v11 = paddedInput[px1][py1];

        float top = (1 - dx) * v00 + dx * v10;
        float bottom = (1 - dx) * v01 + dx * v11;
        output[x][y] = (1 - dy) * top + dy * bottom;
      }
    }

    return output;
  }
}
