package com.gmail.frogocomics.slabify.linalg;

import java.util.Arrays;

/**
 * Wrapper for a 2D float array.
 */
public class Matrix implements Cloneable {

  private final float[][] data;

  public Matrix(int i1, int i2) {
    data = new float[i1][i2];
  }

  public Matrix(float[][] data) {
    this.data = data;
  }

  /**
   * Get the underlying float array.
   *
   * @return the float array.
   */
  public float[][] getData() {
    return data;
  }

  /**
   * Subtract a value from all elements of the array.
   *
   * @param value the value to subtract.
   */
  public void sub(float value) {
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        data[i][j] -= value;
      }
    }
  }

  /**
   * Subtract a matrix.
   *
   * @param b the matrix to subtract.
   */
  public void sub(Matrix b) {
    if (getLength0() != b.getLength0() || getLength1() != b.getLength1()) {
      throw new IllegalArgumentException("Matrix sizes are incompatible");
    }

    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        data[i][j] -= b.getData()[i][j];
      }
    }
  }

  /**
   * Elementwise power.
   *
   * @param a the power to raise all elements to.
   */
  public void pow(double a) {
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        data[i][j] = (float) Math.pow(Math.abs(data[i][j]), a);
      }
    }
  }

  /**
   * Get the sum of all elements in the matrix.
   *
   * @return the sum.
   */
  public float sum() {
    float sum = 0;

    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        sum += data[i][j];
      }
    }

    return sum;
  }

  /**
   * Get the size of the first dimension.
   *
   * @return the size of the first dimension.
   */
  public int getLength0() {
    return data.length;
  }

  /**
   * Get the size of the second dimension.
   *
   * @return the size of the second dimension.
   */
  public int getLength1() {
    return data[0].length;
  }

  /**
   * Return an upscaled matrix.
   *
   * @param scale the amount to upscale. The scale must be greater or equal to 1.
   * @return the upscaled matrix.
   */
  public Matrix upscale(int scale) {

    if (scale == 1) {
      return clone();
    }

    int newLength0 = getLength0() * scale;
    int newLength1 = getLength1() * scale;

    Matrix upscaled = new Matrix(newLength0, newLength1);

    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {

        float value = data[i][j];

        for (int k = 0; k < scale; k++) {
          for (int l = 0; l < scale; l++) {
            upscaled.getData()[scale * i + k][scale * j + l] = value * scale;
          }
        }
      }
    }

    return upscaled;
  }

  /**
   * Rotate the matrix.
   *
   * @param degrees must be a mulitple of 90.
   * @return a rotated copy of the matrix.
   */
  public Matrix rotate(int degrees) {
    degrees = ((degrees % 360) + 360) % 360;

    switch (degrees) {
      case 0:
        return clone();

      case 90: {
        float[][] result = new float[getLength1()][getLength0()];
        for (int i = 0; i < getLength0(); i++) {
          for (int j = 0; j < getLength1(); j++) {
            result[j][getLength0() - 1 - i] = data[i][j];
          }
        }
        return new Matrix(result);
      }

      case 180: {
        float[][] result = new float[getLength0()][getLength1()];
        for (int i = 0; i < getLength0(); i++) {
          for (int j = 0; j < getLength1(); j++) {
            result[getLength0() - 1 - i][getLength1() - 1 - j] = data[i][j];
          }
        }
        return new Matrix(result);
      }

      case 270: {
        float[][] result = new float[getLength1()][getLength0()];
        for (int i = 0; i < getLength0(); i++) {
          for (int j = 0; j < getLength1(); j++) {
            result[getLength1() - 1 - j][i] = data[i][j];
          }
        }
        return new Matrix(result);
      }

      default:
        throw new IllegalArgumentException("Degrees must be a multiple of 90");
    }
  }

  @Override
  public Matrix clone() {

    float[][] copy = new float[getLength0()][getLength1()];

    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        copy[i][j] = data[i][j];
      }
    }

    return new Matrix(copy);
  }

  @Override
  public String toString() {
    return "Matrix [" + getLength0() + "," + getLength1() + "]\n" + Arrays.deepToString(data);
  }
}
