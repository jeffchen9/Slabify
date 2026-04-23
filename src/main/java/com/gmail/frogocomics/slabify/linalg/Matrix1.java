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

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Implementation of a 1x1 matrix.
 */
public final class Matrix1 implements Matrix {

  private float m00;

  Matrix1() {
  }

  public Matrix1(float data) {
    m00 = data;
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
      throw new IllegalArgumentException("Invalid scale: " + scale);
    }

    if (scale == 1) {
      return clone();
    }

    float[] arr = new float[scale * scale];
    Arrays.fill(arr, m00);

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
    // Rotation does not result in any difference for a 1 x 1 matrix
    return clone();
  }

  @Override
  public Matrix clone() {
    Matrix1 copy = new Matrix1();
    copy.m00 = m00;
    return copy;
  }

  @Override
  public float getLoss(float[] arr, double exponent) {
    if (exponent == 2) {
      float l = m00 - arr[0];
      return l * l;
    } else if (exponent == 1) {
      return Math.abs(m00 - arr[0]);
    } else {
      return (float) Math.pow(Math.abs(m00 - arr[0]), exponent);
    }
  }

  @Override
  public float getLossClip(float[] arrUnclip, float[] arrMin0, float[] arrMax1, double exponent) {
    if (exponent == 2) {
      float l00 = m00 == 0 ? m00 - arrMin0[0] : (m00 == 1 ? m00 - arrMax1[0] : m00 - arrUnclip[0]);
      return l00 * l00;
    } else if (exponent == 1) {
      return m00 == 0 ? abs(m00 - arrMin0[0]) : (m00 == 1 ? abs(m00 - arrMax1[0]) : abs(m00 - arrUnclip[0]));
    } else {
      return (float) (m00 == 0 ? pow(abs(m00 - arrMin0[0]), exponent) : (m00 == 1 ? pow(abs(m00 - arrMax1[0]), exponent) : pow(abs(m00 - arrUnclip[0]), exponent)));
    }
  }

  @Override
  public String toString() {
    return "[1x1]:  [" + m00 + "]";
  }
}
