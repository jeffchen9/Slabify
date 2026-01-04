package com.gmail.frogocomics.slabify.linalg;

/**
 * Wrapper for a 2D float array.
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
   * Subtract a matrix.
   *
   * @param b the matrix to subtract.
   */
  void sub(Matrix b);

  /**
   * Elementwise power.
   *
   * @param a the power to raise all elements to.
   */
  void pow(double a);

  /**
   * Get the sum of all elements in the matrix.
   *
   * @return the sum.
   */
  float sum();

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

  Matrix clone();
}

