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
 * Implementation of a NxN matrix.
 */
public final class MatrixN implements Matrix {

  final float[] data;
  private final int size;

  MatrixN(int size) {
    this.size = size;
    data = new float[size * size];
  }

  public MatrixN(float[] data) {
    int sr = (int) Math.sqrt(data.length);

    if (sr * sr != data.length) {
      throw new IllegalArgumentException("Length of input array must be a perfect square");
    }

    size = sr;
    this.data = data;
  }

  public MatrixN(float[][] data) {
    size = data.length;

    if (size == 0 || data[0].length != size) {
      throw new IllegalArgumentException("Incoming array must be square");
    }

    this.data = new float[size * size];

    for (int i = 0; i < size; i++) {
      System.arraycopy(data[i], 0, this.data, i * size, size);
    }
  }

  @Override
  public float get(int i) {
    return data[i];
  }

  @Override
  public void add(float value) {
    for (int i = 0; i < data.length; i++) {
      data[i] += value;
    }
  }

  @Override
  public void sub(float value) {
    for (int i = 0; i < data.length; i++) {
      data[i] -= value;
    }
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public Matrix upscale(int scale) {

    if (scale < 1) {
      throw new IllegalArgumentException("Invalid scale");
    }

    if (scale == 1) {
      return clone();
    }

    int newSize = size * scale;

    MatrixN upscaled = new MatrixN(newSize);

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {

        float value = data[i * size + j];

        for (int k = 0; k < scale; k++) {
          int rowOffset = (i * scale + k) * newSize;
          for (int l = 0; l < scale; l++) {
            upscaled.data[rowOffset + (j * scale + l)] = value;
          }
        }
      }
    }

    return upscaled;
  }

  @Override
  public Matrix rotate(int degrees) {
    degrees = ((degrees % 360) + 360) % 360;

    if (degrees == 0) {
      return clone();
    }

    MatrixN result = new MatrixN(size);
    float[] resData = result.data;

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        float val = data[i * size + j];
        if (degrees == 90) {
          resData[j * size + (size - 1 - i)] = val;
        } else if (degrees == 180) {
          resData[(size - 1 - i) * size + (size - 1 - j)] = val;
        } else if (degrees == 270) {
          resData[(size - 1 - j) * size + i] = val;
        } else {
          throw new IllegalArgumentException("Degrees must be a multiple of 90");
        }
      }
    }
    return result;
  }

  @Override
  public Matrix clone() {
    MatrixN copy = new MatrixN(size);
    System.arraycopy(data, 0, copy.data, 0, data.length);
    return copy;
  }

  @Override
  public float getLoss(float[] arr, double exponent) {
    double loss = 0;

    if (exponent == 2) {
      for (int i = 0; i < size * size; i++) {
        loss += (data[i] - arr[i]) * (data[i] - arr[i]);
      }
    } else if (exponent == 1) {
      for (int i = 0; i < size * size; i++) {
        loss += Math.abs(data[i] - arr[i]);
      }
    } else {
      for (int i = 0; i < size * size; i++) {
        loss += Math.pow(Math.abs(data[i] - arr[i]), exponent);
      }
    }

    return (float) loss;
  }
}

