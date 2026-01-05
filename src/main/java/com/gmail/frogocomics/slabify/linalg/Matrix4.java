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
 * Implementation of a 4x4 matrix.
 */
public final class Matrix4 implements Matrix {

  float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

  Matrix4() {
  }

  public Matrix4(float[] data) {
    if (data.length != 4 * 4) {
      throw new IllegalArgumentException("Array must be of length 4*4");
    }

    m00 = data[0];
    m01 = data[1];
    m02 = data[2];
    m03 = data[3];
    m10 = data[4];
    m11 = data[5];
    m12 = data[6];
    m13 = data[7];
    m20 = data[8];
    m21 = data[9];
    m22 = data[10];
    m23 = data[11];
    m30 = data[12];
    m31 = data[13];
    m32 = data[14];
    m33 = data[15];
  }

  public Matrix4(float[][] data) {
    if (data.length != 4 || data[0].length != 4) {
      throw new IllegalArgumentException("Array must be 4x4");
    }
    m00 = data[0][0];
    m01 = data[0][1];
    m02 = data[0][2];
    m03 = data[0][3];
    m10 = data[1][0];
    m11 = data[1][1];
    m12 = data[1][2];
    m13 = data[1][3];
    m20 = data[2][0];
    m21 = data[2][1];
    m22 = data[2][2];
    m23 = data[2][3];
    m30 = data[3][0];
    m31 = data[3][1];
    m32 = data[3][2];
    m33 = data[3][3];
  }

  @Override
  public float get(int i) {
    switch (i) {
      case 0:
        return m00;
      case 1:
        return m01;
      case 2:
        return m02;
      case 3:
        return m03;
      case 4:
        return m10;
      case 5:
        return m11;
      case 6:
        return m12;
      case 7:
        return m13;
      case 8:
        return m20;
      case 9:
        return m21;
      case 10:
        return m22;
      case 11:
        return m23;
      case 12:
        return m30;
      case 13:
        return m31;
      case 14:
        return m32;
      case 15:
        return m33;
      default:
        throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public void add(float value) {
    m00 += value;
    m01 += value;
    m02 += value;
    m03 += value;
    m10 += value;
    m11 += value;
    m12 += value;
    m13 += value;
    m20 += value;
    m21 += value;
    m22 += value;
    m23 += value;
    m30 += value;
    m31 += value;
    m32 += value;
    m33 += value;
  }

  @Override
  public void sub(float value) {
    m00 -= value;
    m01 -= value;
    m02 -= value;
    m03 -= value;
    m10 -= value;
    m11 -= value;
    m12 -= value;
    m13 -= value;
    m20 -= value;
    m21 -= value;
    m22 -= value;
    m23 -= value;
    m30 -= value;
    m31 -= value;
    m32 -= value;
    m33 -= value;
  }

  @Override
  public int getSize() {
    return 4;
  }

  @Override
  public Matrix upscale(int scale) {
    if (scale < 1) {
      throw new IllegalArgumentException("Invalid scale");
    }

    if (scale == 1) {
      return clone();
    }

    // 4x4 -> 8x8
    if (scale == 2) {
      Matrix8 result = new Matrix8();
      float v;

      // Row 0 of Matrix4 -> Rows 0 & 1 of Matrix8
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
      v = m02 * scale;
      result.m04 = v;
      result.m05 = v;
      result.m14 = v;
      result.m15 = v;
      v = m03 * scale;
      result.m06 = v;
      result.m07 = v;
      result.m16 = v;
      result.m17 = v;

      // Row 1 of Matrix4 -> Rows 2 & 3 of Matrix8
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
      v = m12 * scale;
      result.m24 = v;
      result.m25 = v;
      result.m34 = v;
      result.m35 = v;
      v = m13 * scale;
      result.m26 = v;
      result.m27 = v;
      result.m36 = v;
      result.m37 = v;

      // Row 2 of Matrix4 -> Rows 4 & 5 of Matrix8
      v = m20 * scale;
      result.m40 = v;
      result.m41 = v;
      result.m50 = v;
      result.m51 = v;
      v = m21 * scale;
      result.m42 = v;
      result.m43 = v;
      result.m52 = v;
      result.m53 = v;
      v = m22 * scale;
      result.m44 = v;
      result.m45 = v;
      result.m54 = v;
      result.m55 = v;
      v = m23 * scale;
      result.m46 = v;
      result.m47 = v;
      result.m56 = v;
      result.m57 = v;

      // Row 3 of Matrix4 -> Rows 6 & 7 of Matrix8
      v = m30 * scale;
      result.m60 = v;
      result.m61 = v;
      result.m70 = v;
      result.m71 = v;
      v = m31 * scale;
      result.m62 = v;
      result.m63 = v;
      result.m72 = v;
      result.m73 = v;
      v = m32 * scale;
      result.m64 = v;
      result.m65 = v;
      result.m74 = v;
      result.m75 = v;
      v = m33 * scale;
      result.m66 = v;
      result.m67 = v;
      result.m76 = v;
      result.m77 = v;

      return result;
    }

    int newSize = scale * 4;
    MatrixN result = new MatrixN(newSize);
    float[] outData = result.data;

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        float value = get(i * 4 + j) * scale;

        for (int k = 0; k < scale; k++) {
          int rowOffset = (i * scale + k) * newSize + (j * scale);
          for (int l = 0; l < scale; l++) {
            outData[rowOffset + l] = value;
          }
        }
      }
    }

    return result;
  }

  @Override
  public Matrix rotate(int degrees) {
    degrees = ((degrees % 360) + 360) % 360;
    Matrix4 result = new Matrix4();

    switch (degrees) {
      case 0:
        result.m00 = m00;
        result.m01 = m01;
        result.m02 = m02;
        result.m03 = m03;
        result.m10 = m10;
        result.m11 = m11;
        result.m12 = m12;
        result.m13 = m13;
        result.m20 = m20;
        result.m21 = m21;
        result.m22 = m22;
        result.m23 = m23;
        result.m30 = m30;
        result.m31 = m31;
        result.m32 = m32;
        result.m33 = m33;
        break;
      case 90:
        result.m00 = m30;
        result.m01 = m20;
        result.m02 = m10;
        result.m03 = m00;
        result.m10 = m31;
        result.m11 = m21;
        result.m12 = m11;
        result.m13 = m01;
        result.m20 = m32;
        result.m21 = m22;
        result.m22 = m12;
        result.m23 = m02;
        result.m30 = m33;
        result.m31 = m23;
        result.m32 = m13;
        result.m33 = m03;
        break;
      case 180:
        result.m00 = m33;
        result.m01 = m32;
        result.m02 = m31;
        result.m03 = m30;
        result.m10 = m23;
        result.m11 = m22;
        result.m12 = m21;
        result.m13 = m20;
        result.m20 = m13;
        result.m21 = m12;
        result.m22 = m11;
        result.m23 = m10;
        result.m30 = m03;
        result.m31 = m02;
        result.m32 = m01;
        result.m33 = m00;
        break;
      case 270:
        result.m00 = m03;
        result.m01 = m13;
        result.m02 = m23;
        result.m03 = m33;
        result.m10 = m02;
        result.m11 = m12;
        result.m12 = m22;
        result.m13 = m32;
        result.m20 = m01;
        result.m21 = m11;
        result.m22 = m21;
        result.m23 = m31;
        result.m30 = m00;
        result.m31 = m10;
        result.m32 = m20;
        result.m33 = m30;
        break;
      default:
        throw new IllegalArgumentException("Must be multiple of 90");
    }

    return result;
  }

  @Override
  public Matrix clone() {
    Matrix4 copy = new Matrix4();
    copy.m00 = m00;
    copy.m01 = m01;
    copy.m02 = m02;
    copy.m03 = m03;
    copy.m10 = m10;
    copy.m11 = m11;
    copy.m12 = m12;
    copy.m13 = m13;
    copy.m20 = m20;
    copy.m21 = m21;
    copy.m22 = m22;
    copy.m23 = m23;
    copy.m30 = m30;
    copy.m31 = m31;
    copy.m32 = m32;
    copy.m33 = m33;
    return copy;
  }

  @Override
  public float getLoss(float[] arr) {
    return (m00 - arr[0]) * (m00 - arr[0]) + (m01 - arr[1]) * (m01 - arr[1]) + (m02 - arr[2]) * (m02 - arr[2]) +
        (m03 - arr[3]) * (m03 - arr[3]) + (m10 - arr[4]) * (m10 - arr[4]) + (m11 - arr[5]) * (m11 - arr[5]) +
        (m12 - arr[6]) * (m12 - arr[6]) + (m13 - arr[7]) * (m13 - arr[7]) + (m20 - arr[8]) * (m20 - arr[8]) +
        (m21 - arr[9]) * (m21 - arr[9]) + (m22 - arr[10]) * (m22 - arr[10]) + (m23 - arr[11]) * (m23 - arr[11]) +
        (m30 - arr[12]) * (m30 - arr[12]) + (m31 - arr[13]) * (m31 - arr[13]) + (m32 - arr[14]) * (m32 - arr[14]) +
        (m33 - arr[15]) * (m33 - arr[15]);
  }
}
