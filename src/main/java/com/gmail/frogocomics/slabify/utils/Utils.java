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

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Tile;

import java.util.Set;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

/**
 * Utility class.
 */
public final class Utils {

  private static final int PADDED_SIZE = TILE_SIZE + 2 * Constants.TILE_PADDING;
  private static final ThreadLocal<float[][]> PADDED_BUFFER = ThreadLocal.withInitial(() -> new float[PADDED_SIZE][PADDED_SIZE]);
  private static final ThreadLocal<float[][]> UPSCALE_BUFFER = ThreadLocal.withInitial(() -> new float[PADDED_SIZE * Constants.MAX_UPSCALE_RESOLUTION][PADDED_SIZE * Constants.MAX_UPSCALE_RESOLUTION]);

  private Utils() {
    // Prevent instantiation
  }

  /**
   * Get the difference between the upscaled height and the original height.
   *
   * @param upscaledMap the upscaled map, as a float array.
   * @param originalMap the original tile.
   * @param addHeight   the height to add.
   * @param buffer      the buffer to write to.
   */
  public static void getDifference(float[][] upscaledMap, Tile originalMap, float addHeight, float[][] buffer) {

    int resolution = upscaledMap.length / org.pepsoft.worldpainter.Constants.TILE_SIZE;

    // Resolution must be a power of two: 1, 2, 4, 8, etc.
    if (resolution == 0 || (resolution & (resolution - 1)) != 0) {
      throw new IllegalArgumentException("Resolution must be a power of two");
    }

    // Special case of resolution of 1
    if (resolution == 1) {
      for (int x = 0; x < TILE_SIZE; x++) {
        for (int y = 0; y < TILE_SIZE; y++) {
          buffer[x][y] = upscaledMap[x][y] - originalMap.getIntHeight(x, y) + addHeight;
        }
      }
    } else {
      for (int i1 = 0; i1 < TILE_SIZE; i1++) {
        for (int i2 = 0; i2 < TILE_SIZE; i2++) {

          float baseHeight = originalMap.getIntHeight(i1, i2);
          float offset = addHeight - baseHeight;
          int rowStart = i1 * resolution;
          int colStart = i2 * resolution;

          for (int i3 = 0; i3 < resolution; i3++) {
            for (int i4 = 0; i4 < resolution; i4++) {
              int x = rowStart + i3;
              int y = colStart + i4;
              buffer[x][y] = (upscaledMap[x][y] + offset) * resolution;
            }
          }
        }
      }
    }
  }

  /**
   * Get a heightmap from a {@link Tile} with padding from neighboring {@link Tile}s.
   *
   * @param tile      the tile to get the heightmap from.
   * @param dimension the dimension the tile belongs to.
   * @param pad       the padding on each side.
   * @param buffer    the scratch array.
   */
  public static void padTile(Tile tile, Dimension dimension, int pad, float[][] buffer) {
    int tileX = tile.getX();
    int tileY = tile.getY();

    // Fill center from current tile
    for (int x = 0; x < TILE_SIZE; x++) {
      for (int y = 0; y < TILE_SIZE; y++) {
        buffer[x + pad][y + pad] = tile.getHeight(x, y);
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

        int xStart = (dx < 0) ? 0 : (dx == 0 ? pad : TILE_SIZE + pad);
        int xEnd = (dx < 0) ? pad : (dx == 0 ? TILE_SIZE + pad : PADDED_SIZE);

        int yStart = (dy < 0) ? 0 : (dy == 0 ? pad : TILE_SIZE + pad);
        int yEnd = (dy < 0) ? pad : (dy == 0 ? TILE_SIZE + pad : PADDED_SIZE);

        for (int px = xStart; px < xEnd; px++) {
          for (int py = yStart; py < yEnd; py++) {
            if (neighborTile != null) {
              // Map padded index back to neighbor local index
              int nx = px - (dx * TILE_SIZE + pad);
              int ny = py - (dy * TILE_SIZE + pad);
              buffer[px][py] = neighborTile.getHeight(nx, ny);
            } else {
              // Clamp to the center tile
              int cx = Math.max(pad, Math.min(TILE_SIZE + pad - 1, px));
              int cy = Math.max(pad, Math.min(TILE_SIZE + pad - 1, py));
              buffer[px][py] = buffer[cx][cy];
            }
          }
        }
      }
    }
  }

  /**
   * Upscale a tile using basic interpolation.
   *
   * @param tile       the tile to upscale.
   * @param dimension  the dimension the tile belongs to.
   * @param method     the interpolation method to use. The method must be either
   *                   {@link Interpolation#BICUBIC} or {@link Interpolation#BILINEAR}.
   * @param resolution the amount of upscaling needed, must be a power of 2.
   * @param buffer     the buffer.
   */
  public static void upscaleTile(Tile tile, Dimension dimension, Interpolation method, int resolution, float[][] buffer) {
    // Resolution must be a power of two: 1, 2, 4, 8, etc.
    if (resolution == 0 || (resolution & (resolution - 1)) != 0) {
      throw new IllegalArgumentException("Resolution must be a power of two");
    }

    float[][] paddedBuffer = PADDED_BUFFER.get();
    float[][] upscaleBuffer = UPSCALE_BUFFER.get();

    padTile(tile, dimension, Constants.TILE_PADDING, paddedBuffer);
    upscale(paddedBuffer, resolution, method, upscaleBuffer);
    trimPadding(upscaleBuffer, resolution, Constants.TILE_PADDING, buffer);
  }

  private static void upscale(float[][] input, int scale, Interpolation type, float[][] buffer) {
    int inH = input.length;
    int inW = input[0].length;

    int outH = inH * scale;
    int outW = inW * scale;

    for (int y = 0; y < outH; y++) {
      // Standard center-alignment: maps the center of the output pixel
      // to the corresponding center in the input image.
      float srcY = ((y + 0.5f) / scale) - 0.5f;

      for (int x = 0; x < outW; x++) {
        float srcX = ((x + 0.5f) / scale) - 0.5f;

        if (type == Interpolation.BILINEAR) {
          buffer[y][x] = bilinearSample(input, srcX, srcY);
        } else { // Interpolation.BICUBIC
          buffer[y][x] = bicubicSample(input, srcX, srcY);
        }
      }
    }
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
    float xInv = 1 - xFrac;

    // Clamp coordinates to valid array bounds
    int xL = (x1 < 0) ? 0 : (x1 > inW - 1 ? inW - 1 : x1);
    int xR = (x1 + 1 < 0) ? 0 : (x1 + 1 > inW - 1 ? inW - 1 : x1 + 1);
    int yT = (y1 < 0) ? 0 : (y1 > inH - 1 ? inH - 1 : y1);
    int yB = (y1 + 1 < 0) ? 0 : (y1 + 1 > inH - 1 ? inH - 1 : y1 + 1);

    float v00 = img[yT][xL];
    float v10 = img[yT][xR];
    float v01 = img[yB][xL];
    float v11 = img[yB][xR];

    return (v00 * xInv + v10 * xFrac) * (1 - yFrac) + (v01 * xInv + v11 * xFrac) * yFrac;
  }

  private static float bicubicSample(float[][] img, float x, float y) {
    int inH = img.length;
    int inW = img[0].length;

    int xInt = (int) Math.floor(x);
    int yInt = (int) Math.floor(y);

    // Pre-calculate weights for the 4x4 grid
    float xFrac = x - xInt;
    float yFrac = y - yInt;

    // Horizontal weights (n = -1, 0, 1, 2)
    float wx0 = cubic(xFrac + 1.0f); // distance from xInt-1
    float wx1 = cubic(xFrac);          // distance from xInt
    float wx2 = cubic(xFrac - 1.0f); // distance from xInt+1
    float wx3 = cubic(xFrac - 2.0f); // distance from xInt+2

    // Vertical weights (m = -1, 0, 1, 2)
    float wy0 = cubic(yFrac + 1.0f);
    float wy1 = cubic(yFrac);
    float wy2 = cubic(yFrac - 1.0f);
    float wy3 = cubic(yFrac - 2.0f);

    // Clamp Y coordinates
    int y0 = (yInt - 1 < 0) ? 0 : (yInt - 1 > inH - 1 ? inH - 1 : yInt - 1);
    int y1 = (yInt < 0) ? 0 : (yInt > inH - 1 ? inH - 1 : yInt);
    int y2 = (yInt + 1 < 0) ? 0 : (yInt + 1 > inH - 1 ? inH - 1 : yInt + 1);
    int y3 = (yInt + 2 < 0) ? 0 : (yInt + 2 > inH - 1 ? inH - 1 : yInt + 2);

    float[] row0 = img[y0];
    float[] row1 = img[y1];
    float[] row2 = img[y2];
    float[] row3 = img[y3];

    // Clamp X coordinates
    int x0 = (xInt - 1 < 0) ? 0 : (xInt - 1 > inW - 1 ? inW - 1 : xInt - 1);
    int x1 = (xInt < 0) ? 0 : (xInt > inW - 1 ? inW - 1 : xInt);
    int x2 = (xInt + 1 < 0) ? 0 : (xInt + 1 > inW - 1 ? inW - 1 : xInt + 1);
    int x3 = (xInt + 2 < 0) ? 0 : (xInt + 2 > inW - 1 ? inW - 1 : xInt + 2);

    float result = wy0 * (row0[x0] * wx0 + row0[x1] * wx1 + row0[x2] * wx2 + row0[x3] * wx3);
    result += wy1 * (row1[x0] * wx0 + row1[x1] * wx1 + row1[x2] * wx2 + row1[x3] * wx3);
    result += wy2 * (row0[x0] * wx0 + row2[x1] * wx1 + row2[x2] * wx2 + row2[x3] * wx3);
    result += wy3 * (row3[x0] * wx0 + row3[x1] * wx1 + row3[x2] * wx2 + row3[x3] * wx3);

    return result;
  }

  private static float cubic(float t) {
    t = Math.abs(t);

    // Using Catmull-Rom spline
    if (t <= 1) {
      return (1.5f * t - 2.5f) * t * t + 1;
    }

    if (t < 2) {
      return ((-0.5f * t + 2.5f) * t - 4) * t + 2;
    }

    return 0;
  }

  private static void trimPadding(float[][] buffer, int scale, int pad, float[][] outBuffer) {
    int finalSize = TILE_SIZE * scale;
    int offset = pad * scale;

    for (int y = 0; y < finalSize; y++) {
      System.arraycopy(buffer[y + offset], offset, outBuffer[y], 0, finalSize);
    }
  }

  public static int filter(int[] arr, Set<Integer> allowed) {
    for (int j : arr) {
      if (allowed.contains(j)) {
        return j;
      }
    }

    // This should not happen
    return -1;
  }
}
