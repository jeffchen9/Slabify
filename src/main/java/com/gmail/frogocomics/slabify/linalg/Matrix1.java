package com.gmail.frogocomics.slabify.linalg;

import java.util.Arrays;

public final class Matrix1 implements Matrix {

  private float m;

  public Matrix1() {
  }

  public Matrix1(float[][] data) {
    if (data.length != 1 || data[0].length != 1) {
      throw new IllegalArgumentException("Array must be 1x1");
    }
    m = data[0][0];
  }

  @Override
  public float get(int i) {
    if (i == 0) {
      return m;
    }

    throw new IndexOutOfBoundsException();
  }

  @Override
  public void add(float value) {
    m += value;
  }

  @Override
  public void sub(float value) {
    m -= value;
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
    Arrays.fill(arr, m * scale);

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
    copy.m = m;
    return copy;
  }

  @Override
  public float getLoss(float[] arr) {
    return Math.abs(m - arr[0]);
  }
}
