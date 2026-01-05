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

import java.util.Arrays;

/**
 * Implementation of a 1x1 matrix.
 */
public final class Matrix1 implements Matrix {

  private float m00;

  Matrix1() {
  }

  public Matrix1(float[][] data) {
    if (data.length != 1 || data[0].length != 1) {
      throw new IllegalArgumentException("Array must be 1x1");
    }
    m00 = data[0][0];
  }

  @Override
  public float get(int i) {
    if (i == 0) {
      return m00;
    }

    throw new IndexOutOfBoundsException();
  }

  @Override
  public void add(float value) {
    m00 += value;
  }

  @Override
  public void sub(float value) {
    m00 -= value;
  }

  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public Matrix upscale(int scale) {
    if (scale < 1) {
      throw new IllegalArgumentException("Invalid scale");
    }

    if (scale == 1) {
      return clone();
    }

    float[] arr = new float[scale * scale];
    Arrays.fill(arr, m00 * scale);

    switch (scale) {
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

  @Override
  public Matrix rotate(int degrees) {
    return clone();
  }

  @Override
  public Matrix clone() {
    Matrix1 copy = new Matrix1();
    copy.m00 = m00;
    return copy;
  }

  @Override
  public float getLoss(float[] arr) {
    return (m00 - arr[0]) * (m00 - arr[0]);
  }
}
