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

import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

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
   * Get the difference between the upscaled height and the original height.
   *
   * @param upscaledMap the upscaled map, as a float array.
   * @param originalMap the original map, as an integer array.
   * @param addHalf     whether to add 0.5 to the upscaled map.
   * @return the difference, as a float array.
   */
  public static float[][] getDifference(float[][] upscaledMap, int[][] originalMap, boolean addHalf) {

    int resolution = upscaledMap.length / originalMap.length;

    float[][] differenceMap = new float[upscaledMap.length][upscaledMap[0].length];
    float f = addHalf ? 0.5f : 0;

    if (resolution == 1) {
      for (int x = 0; x < TILE_SIZE; x++) {
        for (int y = 0; y < TILE_SIZE; y++) {
          differenceMap[x][y] = upscaledMap[x][y] - originalMap[x][y] + f;
        }
      }
    } else {
      for (int i1 = 0; i1 < TILE_SIZE; i1++) {
        for (int i2 = 0; i2 < TILE_SIZE; i2++) {
          for (int i3 = 0; i3 < resolution; i3++) {
            for (int i4 = 0; i4 < resolution; i4++) {
              int x = resolution * i1 + i3;
              int y = resolution * i2 + i4;
              differenceMap[x][y] = (upscaledMap[x][y] - originalMap[i1][i2] + f) * resolution;
            }
          }
        }
      }
    }

    return differenceMap;
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
   *                  {@link Interpolation#BICUBIC} or {@link Interpolation#BILINEAR}
   * @param resolution the amount of upscaling needed, must be a power of 2.
   * @return the upscaled heightmap as a float array.
   */
  public static float[][] upscaleTile(Tile tile, Dimension dimension, Interpolation method, int resolution) {
    float[][] padded = padTile(tile, dimension, 2);
    float[][] upscaled = method == Interpolation.BICUBIC ? bicubic(padded) : bilinear(padded);

    for (int i = 0; i < Integer.numberOfTrailingZeros(resolution) - 1; i++) {
      upscaled = method == Interpolation.BICUBIC ? bicubic(upscaled) : bilinear(upscaled);
    }

    return upscaled;
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
