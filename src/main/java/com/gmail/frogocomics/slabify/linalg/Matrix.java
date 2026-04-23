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

package com.gmail.frogocomics.slabify.linalg;

/**
 * Represents a 2 x 2 float matrix with fixed-size implementations to improve performance.
 */
public interface Matrix extends Cloneable {

  /**
   * Create a new matrix. Uses specific implementations for certain sizes for speed.
   *
   * @param arr the values of the matrix. Note that this 2D array must be square, that is, the size of the first
   *            dimension must be equal to the size of the second dimension.
   * @return a new matrix.
   */
  static Matrix of(float[][] arr) {
    switch (arr.length) {
      case 1:
        return new Matrix1(arr);
      case 2:
        return new Matrix2(arr);
      case 4:
        return new Matrix4(arr);
      case 8:
        return new Matrix8(arr);
      default:
        return new MatrixN(arr);
    }
  }

  /**
   * Get a value from the matrix.
   *
   * @param i the index of the value to get.
   * @return the value.
   */
  float get(int i);

  /**
   * Add a value to all elements of the array.
   *
   * @param value the value to subtract.
   */
  void add(float value);

  /**
   * Subtract a value from all elements of the array.
   *
   * @param value the value to subtract.
   */
  void sub(float value);

  /**
   * Get the size of either dimension.
   *
   * @return the size of either dimension.
   */
  int getSize();

  /**
   * Return an upscaled matrix.
   *
   * @param scale the amount to upscale. The scale must be greater or equal to 1.
   * @return the upscaled matrix.
   */
  Matrix upscale(int scale);

  /**
   * Rotate the matrix.
   *
   * @param degrees must be a multiple of 90.
   * @return a rotated copy of the matrix.
   */
  Matrix rotate(int degrees);

  /**
   * Clone the matrix.
   *
   * @return the cloned matrix.
   */
  Matrix clone();

  /**
   * Get the loss metric.
   *
   * @param arr      the array to compare against; it is assumed the sizes are compatible.
   * @param exponent 2 for MSE, 1 for MAE (but other values greater than 0 are acceptable).
   * @return the loss.
   */
  float getLoss(float[] arr, double exponent);

  /**
   * Get a modified loss metric. The value of the array to use is dependent on the value of the matrix, at a given
   * point. The value of the array is as follows:
   * <ul>
   *   <li>If the value of the matrix is equal to 0, the array value is clipped to be greater or equal to 0.</li>
   *   <li>If the value of the matrix is equal to 1, the array value is clipped to be less or equal to 1.</li>
   *   <li>Otherwise, the unclipped array value is taken.</li>
   * </ul>
   *
   * @param arrUnclip the unclipped array to compare against; it is assumed the sizes are compatible.
   * @param arrMin0   the clipped array (>=0) to compare against; it is assumed the sizes are compatible.
   * @param arrMax1   the clipped array (<=1) to compare against; it is assumed the sizes are compatible.
   * @param exponent  2 for MSE, 1 for MAE (but other values greater than 0 are acceptable).
   * @return the modified loss.
   */
  float getLossClip(float[] arrUnclip, float[] arrMin0, float[] arrMax1, double exponent);
}

