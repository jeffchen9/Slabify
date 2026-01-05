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
 * Implementation of a 2x2 matrix.
 */
public final class Matrix2 implements Matrix {

  private float m00, m01, m10, m11;

  Matrix2() {
  }

  public Matrix2(float[] data) {
    if (data.length != 2 * 2) {
      throw new IllegalArgumentException("Array must be of length 2*2");
    }

    m00 = data[0];
    m01 = data[1];
    m10 = data[2];
    m11 = data[3];
  }

  public Matrix2(float[][] data) {
    if (data.length != 2 || data[0].length != 2) {
      throw new IllegalArgumentException("Array must be 2x2");
    }
    m00 = data[0][0];
    m01 = data[0][1];
    m10 = data[1][0];
    m11 = data[1][1];
  }

  @Override
  public float get(int i) {
    switch (i) {
      case 0:
        return m00;
      case 1:
        return m01;
      case 2:
        return m10;
      case 3:
        return m11;
      default:
        throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public void add(float value) {
    m00 += value;
    m01 += value;
    m10 += value;
    m11 += value;
  }

  @Override
  public void sub(float value) {
    m00 -= value;
    m01 -= value;
    m10 -= value;
    m11 -= value;
  }

  @Override
  public int getSize() {
    return 2;
  }

  @Override
  public Matrix upscale(int scale) {
    if (scale < 1) {
      throw new IllegalArgumentException("Invalid scale");
    }

    if (scale == 1) {
      return clone();
    }

    if (scale == 2) {
      Matrix4 result = new Matrix4();
      float v;

      v = m00 * scale;
      result.m00 = v;
      result.m01 = v;
      result.m10 = v;
      result.m11 = v;
      v = m01 * scale;
      result.m02 = v;
      result.m03 = v;
      result.m12 = v;
      result.m13 = v;
      v = m10 * scale;
      result.m20 = v;
      result.m21 = v;
      result.m30 = v;
      result.m31 = v;
      v = m11 * scale;
      result.m22 = v;
      result.m23 = v;
      result.m32 = v;
      result.m33 = v;

      return result;
    }

    if (scale == 4) {
      Matrix8 result = new Matrix8();
      float v;

      // Top-Left 4x4 block (m00)
      v = m00 * scale;
      result.m00 = v;
      result.m01 = v;
      result.m02 = v;
      result.m03 = v;
      result.m10 = v;
      result.m11 = v;
      result.m12 = v;
      result.m13 = v;
      result.m20 = v;
      result.m21 = v;
      result.m22 = v;
      result.m23 = v;
      result.m30 = v;
      result.m31 = v;
      result.m32 = v;
      result.m33 = v;

      // Top-Right 4x4 block (m01)
      v = m01 * scale;
      result.m04 = v;
      result.m05 = v;
      result.m06 = v;
      result.m07 = v;
      result.m14 = v;
      result.m15 = v;
      result.m16 = v;
      result.m17 = v;
      result.m24 = v;
      result.m25 = v;
      result.m26 = v;
      result.m27 = v;
      result.m34 = v;
      result.m35 = v;
      result.m36 = v;
      result.m37 = v;

      // Bottom-Left 4x4 block (m10)
      v = m10 * scale;
      result.m40 = v;
      result.m41 = v;
      result.m42 = v;
      result.m43 = v;
      result.m50 = v;
      result.m51 = v;
      result.m52 = v;
      result.m53 = v;
      result.m60 = v;
      result.m61 = v;
      result.m62 = v;
      result.m63 = v;
      result.m70 = v;
      result.m71 = v;
      result.m72 = v;
      result.m73 = v;

      // Bottom-Right 4x4 block (m11)
      v = m11 * scale;
      result.m44 = v;
      result.m45 = v;
      result.m46 = v;
      result.m47 = v;
      result.m54 = v;
      result.m55 = v;
      result.m56 = v;
      result.m57 = v;
      result.m64 = v;
      result.m65 = v;
      result.m66 = v;
      result.m67 = v;
      result.m74 = v;
      result.m75 = v;
      result.m76 = v;
      result.m77 = v;

      return result;
    }

    int newSize = scale * 2;
    MatrixN upscaled = new MatrixN(newSize);
    float[] outData = upscaled.data;

    float v;
    // Source m00
    v = m00 * scale;
    for (int k = 0; k < scale; k++) {
      int offset = k * newSize;
      for (int l = 0; l < scale; l++) {
        outData[offset + l] = v;
      }
    }
    // Source m01
    v = m01 * scale;
    for (int k = 0; k < scale; k++) {
      int offset = k * newSize + scale;
      for (int l = 0; l < scale; l++) {
        outData[offset + l] = v;
      }
    }
    // Source m10
    v = m10 * scale;
    for (int k = 0; k < scale; k++) {
      int offset = (scale + k) * newSize;
      for (int l = 0; l < scale; l++) {
        outData[offset + l] = v;
      }
    }
    // Source m11
    v = m11 * scale;
    for (int k = 0; k < scale; k++) {
      int offset = (scale + k) * newSize + scale;
      for (int l = 0; l < scale; l++) {
        outData[offset + l] = v;
      }
    }

    return upscaled;
  }

  @Override
  public Matrix rotate(int degrees) {
    degrees = ((degrees % 360) + 360) % 360;
    Matrix2 result = new Matrix2();

    switch (degrees) {
      case 0:
        result.m00 = m00;
        result.m01 = m01;
        result.m10 = m10;
        result.m11 = m11;
        break;
      case 90:
        result.m00 = m10;
        result.m01 = m00;
        result.m10 = m11;
        result.m11 = m01;
        break;
      case 180:
        result.m00 = m11;
        result.m01 = m10;
        result.m10 = m01;
        result.m11 = m00;
        break;
      case 270:
        result.m00 = m01;
        result.m01 = m11;
        result.m10 = m00;
        result.m11 = m10;
        break;
      default:
        throw new IllegalArgumentException("Must be multiple of 90");
    }

    return result;
  }

  @Override
  public Matrix clone() {
    Matrix2 copy = new Matrix2();
    copy.m00 = m00;
    copy.m01 = m01;
    copy.m10 = m10;
    copy.m11 = m11;
    return copy;
  }

  @Override
  public float getLoss(float[] arr) {
    return (m00 - arr[0]) * (m00 - arr[0]) + (m01 - arr[1]) * (m01 - arr[1]) + (m10 - arr[2]) * (m10 - arr[2]) +
        (m11 - arr[3]) * (m11 - arr[3]);
  }
}
