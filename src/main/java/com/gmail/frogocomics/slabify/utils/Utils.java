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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 * Utility class.
 */
public final class Utils {

  private Utils() {
    // Prevent instantiation
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
   * Get the difference between the upscaled height and the original height.
   *
   * @param upscaledMap the upscaled map, as a float array.
   * @param originalMap the original map, as an integer array.
   * @param addHeight   the height to add.
   * @return the difference, as a float array.
   */
  public static float[][] getDifference(float[][] upscaledMap, int[][] originalMap, float addHeight) {

    int resolution = upscaledMap.length / originalMap.length;

    float[][] differenceMap = new float[upscaledMap.length][upscaledMap[0].length];

    if (resolution == 1) {
      for (int x = 0; x < TILE_SIZE; x++) {
        for (int y = 0; y < TILE_SIZE; y++) {
          differenceMap[x][y] = upscaledMap[x][y] - originalMap[x][y] + addHeight;
        }
      }
    } else {
      for (int i1 = 0; i1 < TILE_SIZE; i1++) {
        for (int i2 = 0; i2 < TILE_SIZE; i2++) {
          for (int i3 = 0; i3 < resolution; i3++) {
            for (int i4 = 0; i4 < resolution; i4++) {
              int x = resolution * i1 + i3;
              int y = resolution * i2 + i4;
              differenceMap[x][y] = (upscaledMap[x][y] - originalMap[i1][i2] + addHeight) * resolution;
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
    float[][] upscaled = upscale(padded, resolution, method);

    return trimPadding(upscaled, resolution, 2);
  }

  private static float[][] upscale(float[][] input, int scale, Interpolation type) {
    int inH = input.length;
    int inW = input[0].length;

    int outH = inH * scale;
    int outW = inW * scale;

    float[][] output = new float[outH][outW];

    for (int y = 0; y < outH; y++) {
      // Standard center-alignment: maps the center of the output pixel
      // to the corresponding center in the input image.
      float srcY = ((y + 0.5f) / scale) - 0.5f;

      for (int x = 0; x < outW; x++) {
        float srcX = ((x + 0.5f) / scale) - 0.5f;

        if (type == Interpolation.BILINEAR) {
          output[y][x] = bilinearSample(input, srcX, srcY);
        } else {
          output[y][x] = bicubicSample(input, srcX, srcY);
        }
      }
    }

    return output;
  }

  private static float bilinearSample(float[][] img, float x, float y) {
    int inH = img.length;
    int inW = img[0].length;

    // Find the 4 neighboring pixels
    int x1 = (int) Math.floor(x);
    int y1 = (int) Math.floor(y);

    // Fraction for interpolation
    float xFrac = x - x1;
    float yFrac = y - y1;

    // Clamp coordinates to valid array bounds
    int xL = Math.max(0, Math.min(x1, inW - 1));
    int xR = Math.max(0, Math.min(x1 + 1, inW - 1));
    int yT = Math.max(0, Math.min(y1, inH - 1));
    int yB = Math.max(0, Math.min(y1 + 1, inH - 1));

    float v00 = img[yT][xL];
    float v10 = img[yT][xR];
    float v01 = img[yB][xL];
    float v11 = img[yB][xR];

    float i1 = v00 * (1 - xFrac) + v10 * xFrac;
    float i2 = v01 * (1 - xFrac) + v11 * xFrac;

    return i1 * (1 - yFrac) + i2 * yFrac;
  }

  private static float bicubicSample(float[][] img, float x, float y) {
    int inH = img.length;
    int inW = img[0].length;

    int xInt = (int) Math.floor(x);
    int yInt = (int) Math.floor(y);

    float result = 0.0f;

    // Bicubic uses a 4x4 grid of pixels around the target point
    for (int m = -1; m <= 2; m++) {
      for (int n = -1; n <= 2; n++) {
        // Clamp every neighbor access
        int py = Math.max(0, Math.min(yInt + m, inH - 1));
        int px = Math.max(0, Math.min(xInt + n, inW - 1));

        float weight = cubic(x - (xInt + n)) * cubic(y - (yInt + m));
        result += img[py][px] * weight;
      }
    }

    return result;
  }

  private static float cubic(float t) {
    t = Math.abs(t);
    if (t <= 1) {
      return (1.5f * t - 2.5f) * t * t + 1;
    } else if (t < 2) {
      return ((-0.5f * t + 2.5f) * t - 4) * t + 2;
    }
    return 0.0f;
  }

  private static float[][] trimPadding(float[][] input, int scale, int pad) {
    int trim = pad * scale;
    int h = input.length - 2 * trim;
    int w = input[0].length - 2 * trim;

    float[][] out = new float[h][w];
    for (int y = 0; y < h; y++) {
      System.arraycopy(input[y + trim], trim, out[y], 0, w);
    }
    return out;
  }

  public static int filter(int[] arr, Set<Integer> allowed) {
    for (int j : arr) {
      if (allowed.contains(j)) {
        return j;
      }
    }

    // This should not happen
    return 0;
  }
}
