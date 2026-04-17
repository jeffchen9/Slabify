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

import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Implementation of a 8x8 matrix.
 */
public final class Matrix8 implements Matrix {

  float m00, m01, m02, m03, m04, m05, m06, m07, m10, m11, m12, m13, m14, m15, m16, m17, m20, m21, m22, m23, m24, m25, m26, m27, m30, m31, m32, m33, m34, m35, m36, m37, m40, m41, m42, m43, m44, m45, m46, m47, m50, m51, m52, m53, m54, m55, m56, m57, m60, m61, m62, m63, m64, m65, m66, m67, m70, m71, m72, m73, m74, m75, m76, m77;

  Matrix8() {
  }

  public Matrix8(float[] data) {
    if (data.length != 8 * 8) {
      throw new IllegalArgumentException("Array must be of length 8*8");
    }

    m00 = data[0];
    m01 = data[1];
    m02 = data[2];
    m03 = data[3];
    m04 = data[4];
    m05 = data[5];
    m06 = data[6];
    m07 = data[7];
    m10 = data[8];
    m11 = data[9];
    m12 = data[10];
    m13 = data[11];
    m14 = data[12];
    m15 = data[13];
    m16 = data[14];
    m17 = data[15];
    m20 = data[16];
    m21 = data[17];
    m22 = data[18];
    m23 = data[19];
    m24 = data[20];
    m25 = data[21];
    m26 = data[22];
    m27 = data[23];
    m30 = data[24];
    m31 = data[25];
    m32 = data[26];
    m33 = data[27];
    m34 = data[28];
    m35 = data[29];
    m36 = data[30];
    m37 = data[31];
    m40 = data[32];
    m41 = data[33];
    m42 = data[34];
    m43 = data[35];
    m44 = data[36];
    m45 = data[37];
    m46 = data[38];
    m47 = data[39];
    m50 = data[40];
    m51 = data[41];
    m52 = data[42];
    m53 = data[43];
    m54 = data[44];
    m55 = data[45];
    m56 = data[46];
    m57 = data[47];
    m60 = data[48];
    m61 = data[49];
    m62 = data[50];
    m63 = data[51];
    m64 = data[52];
    m65 = data[53];
    m66 = data[54];
    m67 = data[55];
    m70 = data[56];
    m71 = data[57];
    m72 = data[58];
    m73 = data[59];
    m74 = data[60];
    m75 = data[61];
    m76 = data[62];
    m77 = data[63];
  }

  public Matrix8(float[][] data) {
    if (data.length != 8 || data[0].length != 8) {
      throw new IllegalArgumentException("Array must be 8x8");
    }
    m00 = data[0][0];
    m01 = data[0][1];
    m02 = data[0][2];
    m03 = data[0][3];
    m04 = data[0][4];
    m05 = data[0][5];
    m06 = data[0][6];
    m07 = data[0][7];
    m10 = data[1][0];
    m11 = data[1][1];
    m12 = data[1][2];
    m13 = data[1][3];
    m14 = data[1][4];
    m15 = data[1][5];
    m16 = data[1][6];
    m17 = data[1][7];
    m20 = data[2][0];
    m21 = data[2][1];
    m22 = data[2][2];
    m23 = data[2][3];
    m24 = data[2][4];
    m25 = data[2][5];
    m26 = data[2][6];
    m27 = data[2][7];
    m30 = data[3][0];
    m31 = data[3][1];
    m32 = data[3][2];
    m33 = data[3][3];
    m34 = data[3][4];
    m35 = data[3][5];
    m36 = data[3][6];
    m37 = data[3][7];
    m40 = data[4][0];
    m41 = data[4][1];
    m42 = data[4][2];
    m43 = data[4][3];
    m44 = data[4][4];
    m45 = data[4][5];
    m46 = data[4][6];
    m47 = data[4][7];
    m50 = data[5][0];
    m51 = data[5][1];
    m52 = data[5][2];
    m53 = data[5][3];
    m54 = data[5][4];
    m55 = data[5][5];
    m56 = data[5][6];
    m57 = data[5][7];
    m60 = data[6][0];
    m61 = data[6][1];
    m62 = data[6][2];
    m63 = data[6][3];
    m64 = data[6][4];
    m65 = data[6][5];
    m66 = data[6][6];
    m67 = data[6][7];
    m70 = data[7][0];
    m71 = data[7][1];
    m72 = data[7][2];
    m73 = data[7][3];
    m74 = data[7][4];
    m75 = data[7][5];
    m76 = data[7][6];
    m77 = data[7][7];
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
        return m04;
      case 5:
        return m05;
      case 6:
        return m06;
      case 7:
        return m07;
      case 8:
        return m10;
      case 9:
        return m11;
      case 10:
        return m12;
      case 11:
        return m13;
      case 12:
        return m14;
      case 13:
        return m15;
      case 14:
        return m16;
      case 15:
        return m17;
      case 16:
        return m20;
      case 17:
        return m21;
      case 18:
        return m22;
      case 19:
        return m23;
      case 20:
        return m24;
      case 21:
        return m25;
      case 22:
        return m26;
      case 23:
        return m27;
      case 24:
        return m30;
      case 25:
        return m31;
      case 26:
        return m32;
      case 27:
        return m33;
      case 28:
        return m34;
      case 29:
        return m35;
      case 30:
        return m36;
      case 31:
        return m37;
      case 32:
        return m40;
      case 33:
        return m41;
      case 34:
        return m42;
      case 35:
        return m43;
      case 36:
        return m44;
      case 37:
        return m45;
      case 38:
        return m46;
      case 39:
        return m47;
      case 40:
        return m50;
      case 41:
        return m51;
      case 42:
        return m52;
      case 43:
        return m53;
      case 44:
        return m54;
      case 45:
        return m55;
      case 46:
        return m56;
      case 47:
        return m57;
      case 48:
        return m60;
      case 49:
        return m61;
      case 50:
        return m62;
      case 51:
        return m63;
      case 52:
        return m64;
      case 53:
        return m65;
      case 54:
        return m66;
      case 55:
        return m67;
      case 56:
        return m70;
      case 57:
        return m71;
      case 58:
        return m72;
      case 59:
        return m73;
      case 60:
        return m74;
      case 61:
        return m75;
      case 62:
        return m76;
      case 63:
        return m77;
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
    m04 += value;
    m05 += value;
    m06 += value;
    m07 += value;
    m10 += value;
    m11 += value;
    m12 += value;
    m13 += value;
    m14 += value;
    m15 += value;
    m16 += value;
    m17 += value;
    m20 += value;
    m21 += value;
    m22 += value;
    m23 += value;
    m24 += value;
    m25 += value;
    m26 += value;
    m27 += value;
    m30 += value;
    m31 += value;
    m32 += value;
    m33 += value;
    m34 += value;
    m35 += value;
    m36 += value;
    m37 += value;
    m40 += value;
    m41 += value;
    m42 += value;
    m43 += value;
    m44 += value;
    m45 += value;
    m46 += value;
    m47 += value;
    m50 += value;
    m51 += value;
    m52 += value;
    m53 += value;
    m54 += value;
    m55 += value;
    m56 += value;
    m57 += value;
    m60 += value;
    m61 += value;
    m62 += value;
    m63 += value;
    m64 += value;
    m65 += value;
    m66 += value;
    m67 += value;
    m70 += value;
    m71 += value;
    m72 += value;
    m73 += value;
    m74 += value;
    m75 += value;
    m76 += value;
    m77 += value;
  }

  @Override
  public void sub(float value) {
    m00 -= value;
    m01 -= value;
    m02 -= value;
    m03 -= value;
    m04 -= value;
    m05 -= value;
    m06 -= value;
    m07 -= value;
    m10 -= value;
    m11 -= value;
    m12 -= value;
    m13 -= value;
    m14 -= value;
    m15 -= value;
    m16 -= value;
    m17 -= value;
    m20 -= value;
    m21 -= value;
    m22 -= value;
    m23 -= value;
    m24 -= value;
    m25 -= value;
    m26 -= value;
    m27 -= value;
    m30 -= value;
    m31 -= value;
    m32 -= value;
    m33 -= value;
    m34 -= value;
    m35 -= value;
    m36 -= value;
    m37 -= value;
    m40 -= value;
    m41 -= value;
    m42 -= value;
    m43 -= value;
    m44 -= value;
    m45 -= value;
    m46 -= value;
    m47 -= value;
    m50 -= value;
    m51 -= value;
    m52 -= value;
    m53 -= value;
    m54 -= value;
    m55 -= value;
    m56 -= value;
    m57 -= value;
    m60 -= value;
    m61 -= value;
    m62 -= value;
    m63 -= value;
    m64 -= value;
    m65 -= value;
    m66 -= value;
    m67 -= value;
    m70 -= value;
    m71 -= value;
    m72 -= value;
    m73 -= value;
    m74 -= value;
    m75 -= value;
    m76 -= value;
    m77 -= value;
  }

  @Override
  public int getSize() {
    return 8;
  }

  @Override
  public Matrix upscale(int scale) {
    if (scale < 1) {
      throw new IllegalArgumentException("Invalid scale: " + scale);
    }

    // Given the current shapes, scale must be equal to 1. This may change in the future, however.
    return clone();
  }

  @Override
  public Matrix rotate(int degrees) {
    degrees = ((degrees % 360) + 360) % 360;
    Matrix8 result = new Matrix8();

    switch (degrees) {
      case 0:
        result.m00 = m00;
        result.m01 = m01;
        result.m02 = m02;
        result.m03 = m03;
        result.m04 = m04;
        result.m05 = m05;
        result.m06 = m06;
        result.m07 = m07;
        result.m10 = m10;
        result.m11 = m11;
        result.m12 = m12;
        result.m13 = m13;
        result.m14 = m14;
        result.m15 = m15;
        result.m16 = m16;
        result.m17 = m17;
        result.m20 = m20;
        result.m21 = m21;
        result.m22 = m22;
        result.m23 = m23;
        result.m24 = m24;
        result.m25 = m25;
        result.m26 = m26;
        result.m27 = m27;
        result.m30 = m30;
        result.m31 = m31;
        result.m32 = m32;
        result.m33 = m33;
        result.m34 = m34;
        result.m35 = m35;
        result.m36 = m36;
        result.m37 = m37;
        result.m40 = m40;
        result.m41 = m41;
        result.m42 = m42;
        result.m43 = m43;
        result.m44 = m44;
        result.m45 = m45;
        result.m46 = m46;
        result.m47 = m47;
        result.m50 = m50;
        result.m51 = m51;
        result.m52 = m52;
        result.m53 = m53;
        result.m54 = m54;
        result.m55 = m55;
        result.m56 = m56;
        result.m57 = m57;
        result.m60 = m60;
        result.m61 = m61;
        result.m62 = m62;
        result.m63 = m63;
        result.m64 = m64;
        result.m65 = m65;
        result.m66 = m66;
        result.m67 = m67;
        result.m70 = m70;
        result.m71 = m71;
        result.m72 = m72;
        result.m73 = m73;
        result.m74 = m74;
        result.m75 = m75;
        result.m76 = m76;
        result.m77 = m77;
        break;
      case 90:
        result.m00 = m70;
        result.m01 = m60;
        result.m02 = m50;
        result.m03 = m40;
        result.m04 = m30;
        result.m05 = m20;
        result.m06 = m10;
        result.m07 = m00;
        result.m10 = m71;
        result.m11 = m61;
        result.m12 = m51;
        result.m13 = m41;
        result.m14 = m31;
        result.m15 = m21;
        result.m16 = m11;
        result.m17 = m01;
        result.m20 = m72;
        result.m21 = m62;
        result.m22 = m52;
        result.m23 = m42;
        result.m24 = m32;
        result.m25 = m22;
        result.m26 = m12;
        result.m27 = m02;
        result.m30 = m73;
        result.m31 = m63;
        result.m32 = m53;
        result.m33 = m43;
        result.m34 = m33;
        result.m35 = m23;
        result.m36 = m13;
        result.m37 = m03;
        result.m40 = m74;
        result.m41 = m64;
        result.m42 = m54;
        result.m43 = m44;
        result.m44 = m34;
        result.m45 = m24;
        result.m46 = m14;
        result.m47 = m04;
        result.m50 = m75;
        result.m51 = m65;
        result.m52 = m55;
        result.m53 = m45;
        result.m54 = m35;
        result.m55 = m25;
        result.m56 = m15;
        result.m57 = m05;
        result.m60 = m76;
        result.m61 = m66;
        result.m62 = m56;
        result.m63 = m46;
        result.m64 = m36;
        result.m65 = m26;
        result.m66 = m16;
        result.m67 = m06;
        result.m70 = m77;
        result.m71 = m67;
        result.m72 = m57;
        result.m73 = m47;
        result.m74 = m37;
        result.m75 = m27;
        result.m76 = m17;
        result.m77 = m07;
        break;
      case 180:
        result.m00 = m77;
        result.m01 = m76;
        result.m02 = m75;
        result.m03 = m74;
        result.m04 = m73;
        result.m05 = m72;
        result.m06 = m71;
        result.m07 = m70;
        result.m10 = m67;
        result.m11 = m66;
        result.m12 = m65;
        result.m13 = m64;
        result.m14 = m63;
        result.m15 = m62;
        result.m16 = m61;
        result.m17 = m60;
        result.m20 = m57;
        result.m21 = m56;
        result.m22 = m55;
        result.m23 = m54;
        result.m24 = m53;
        result.m25 = m52;
        result.m26 = m51;
        result.m27 = m50;
        result.m30 = m47;
        result.m31 = m46;
        result.m32 = m45;
        result.m33 = m44;
        result.m34 = m43;
        result.m35 = m42;
        result.m36 = m41;
        result.m37 = m40;
        result.m40 = m37;
        result.m41 = m36;
        result.m42 = m35;
        result.m43 = m34;
        result.m44 = m33;
        result.m45 = m32;
        result.m46 = m31;
        result.m47 = m30;
        result.m50 = m27;
        result.m51 = m26;
        result.m52 = m25;
        result.m53 = m24;
        result.m54 = m23;
        result.m55 = m22;
        result.m56 = m21;
        result.m57 = m20;
        result.m60 = m17;
        result.m61 = m16;
        result.m62 = m15;
        result.m63 = m14;
        result.m64 = m13;
        result.m65 = m12;
        result.m66 = m11;
        result.m67 = m10;
        result.m70 = m07;
        result.m71 = m06;
        result.m72 = m05;
        result.m73 = m04;
        result.m74 = m03;
        result.m75 = m02;
        result.m76 = m01;
        result.m77 = m00;
        break;
      case 270:
        result.m00 = m07;
        result.m01 = m17;
        result.m02 = m27;
        result.m03 = m37;
        result.m04 = m47;
        result.m05 = m57;
        result.m06 = m67;
        result.m07 = m77;
        result.m10 = m06;
        result.m11 = m16;
        result.m12 = m26;
        result.m13 = m36;
        result.m14 = m46;
        result.m15 = m56;
        result.m16 = m66;
        result.m17 = m76;
        result.m20 = m05;
        result.m21 = m15;
        result.m22 = m25;
        result.m23 = m35;
        result.m24 = m45;
        result.m25 = m55;
        result.m26 = m65;
        result.m27 = m75;
        result.m30 = m04;
        result.m31 = m14;
        result.m32 = m24;
        result.m33 = m34;
        result.m34 = m44;
        result.m35 = m54;
        result.m36 = m64;
        result.m37 = m74;
        result.m40 = m03;
        result.m41 = m13;
        result.m42 = m23;
        result.m43 = m33;
        result.m44 = m43;
        result.m45 = m53;
        result.m46 = m63;
        result.m47 = m73;
        result.m50 = m02;
        result.m51 = m12;
        result.m52 = m22;
        result.m53 = m32;
        result.m54 = m42;
        result.m55 = m52;
        result.m56 = m62;
        result.m57 = m72;
        result.m60 = m01;
        result.m61 = m11;
        result.m62 = m21;
        result.m63 = m31;
        result.m64 = m41;
        result.m65 = m51;
        result.m66 = m61;
        result.m67 = m71;
        result.m70 = m00;
        result.m71 = m10;
        result.m72 = m20;
        result.m73 = m30;
        result.m74 = m40;
        result.m75 = m50;
        result.m76 = m60;
        result.m77 = m70;
        break;
      default:
        throw new IllegalArgumentException("Must be multiple of 90");
    }

    return result;
  }

  @Override
  public Matrix clone() {
    Matrix8 copy = new Matrix8();
    copy.m00 = m00;
    copy.m01 = m01;
    copy.m02 = m02;
    copy.m03 = m03;
    copy.m04 = m04;
    copy.m05 = m05;
    copy.m06 = m06;
    copy.m07 = m07;
    copy.m10 = m10;
    copy.m11 = m11;
    copy.m12 = m12;
    copy.m13 = m13;
    copy.m14 = m14;
    copy.m15 = m15;
    copy.m16 = m16;
    copy.m17 = m17;
    copy.m20 = m20;
    copy.m21 = m21;
    copy.m22 = m22;
    copy.m23 = m23;
    copy.m24 = m24;
    copy.m25 = m25;
    copy.m26 = m26;
    copy.m27 = m27;
    copy.m30 = m30;
    copy.m31 = m31;
    copy.m32 = m32;
    copy.m33 = m33;
    copy.m34 = m34;
    copy.m35 = m35;
    copy.m36 = m36;
    copy.m37 = m37;
    copy.m40 = m40;
    copy.m41 = m41;
    copy.m42 = m42;
    copy.m43 = m43;
    copy.m44 = m44;
    copy.m45 = m45;
    copy.m46 = m46;
    copy.m47 = m47;
    copy.m50 = m50;
    copy.m51 = m51;
    copy.m52 = m52;
    copy.m53 = m53;
    copy.m54 = m54;
    copy.m55 = m55;
    copy.m56 = m56;
    copy.m57 = m57;
    copy.m60 = m60;
    copy.m61 = m61;
    copy.m62 = m62;
    copy.m63 = m63;
    copy.m64 = m64;
    copy.m65 = m65;
    copy.m66 = m66;
    copy.m67 = m67;
    copy.m70 = m70;
    copy.m71 = m71;
    copy.m72 = m72;
    copy.m73 = m73;
    copy.m74 = m74;
    copy.m75 = m75;
    copy.m76 = m76;
    copy.m77 = m77;
    return copy;
  }

  @Override
  public float getLoss(float[] arr, double exponent) {
    if (exponent == 2) {
      return (m00 - arr[0]) * (m00 - arr[0]) + (m01 - arr[1]) * (m01 - arr[1]) + (m02 - arr[2]) * (m02 - arr[2]) + (m03 - arr[3]) * (m03 - arr[3]) + (m04 - arr[4]) * (m04 - arr[4]) + (m05 - arr[5]) * (m05 - arr[5]) + (m06 - arr[6]) * (m06 - arr[6]) + (m07 - arr[7]) * (m07 - arr[7]) + (m10 - arr[8]) * (m10 - arr[8]) + (m11 - arr[9]) * (m11 - arr[9]) + (m12 - arr[10]) * (m12 - arr[10]) + (m13 - arr[11]) * (m13 - arr[11]) + (m14 - arr[12]) * (m14 - arr[12]) + (m15 - arr[13]) * (m15 - arr[13]) + (m16 - arr[14]) * (m16 - arr[14]) + (m17 - arr[15]) * (m17 - arr[15]) + (m20 - arr[16]) * (m20 - arr[16]) + (m21 - arr[17]) * (m21 - arr[17]) + (m22 - arr[18]) * (m22 - arr[18]) + (m23 - arr[19]) * (m23 - arr[19]) + (m24 - arr[20]) * (m24 - arr[20]) + (m25 - arr[21]) * (m25 - arr[21]) + (m26 - arr[22]) * (m26 - arr[22]) + (m27 - arr[23]) * (m27 - arr[23]) + (m30 - arr[24]) * (m30 - arr[24]) + (m31 - arr[25]) * (m31 - arr[25]) + (m32 - arr[26]) * (m32 - arr[26]) + (m33 - arr[27]) * (m33 - arr[27]) + (m34 - arr[28]) * (m34 - arr[28]) + (m35 - arr[29]) * (m35 - arr[29]) + (m36 - arr[30]) * (m36 - arr[30]) + (m37 - arr[31]) * (m37 - arr[31]) + (m40 - arr[32]) * (m40 - arr[32]) + (m41 - arr[33]) * (m41 - arr[33]) + (m42 - arr[34]) * (m42 - arr[34]) + (m43 - arr[35]) * (m43 - arr[35]) + (m44 - arr[36]) * (m44 - arr[36]) + (m45 - arr[37]) * (m45 - arr[37]) + (m46 - arr[38]) * (m46 - arr[38]) + (m47 - arr[39]) * (m47 - arr[39]) + (m50 - arr[40]) * (m50 - arr[40]) + (m51 - arr[41]) * (m51 - arr[41]) + (m52 - arr[42]) * (m52 - arr[42]) + (m53 - arr[43]) * (m53 - arr[43]) + (m54 - arr[44]) * (m54 - arr[44]) + (m55 - arr[45]) * (m55 - arr[45]) + (m56 - arr[46]) * (m56 - arr[46]) + (m57 - arr[47]) * (m57 - arr[47]) + (m60 - arr[48]) * (m60 - arr[48]) + (m61 - arr[49]) * (m61 - arr[49]) + (m62 - arr[50]) * (m62 - arr[50]) + (m63 - arr[51]) * (m63 - arr[51]) + (m64 - arr[52]) * (m64 - arr[52]) + (m65 - arr[53]) * (m65 - arr[53]) + (m66 - arr[54]) * (m66 - arr[54]) + (m67 - arr[55]) * (m67 - arr[55]) + (m70 - arr[56]) * (m70 - arr[56]) + (m71 - arr[57]) * (m71 - arr[57]) + (m72 - arr[58]) * (m72 - arr[58]) + (m73 - arr[59]) * (m73 - arr[59]) + (m74 - arr[60]) * (m74 - arr[60]) + (m75 - arr[61]) * (m75 - arr[61]) + (m76 - arr[62]) * (m76 - arr[62]) + (m77 - arr[63]) * (m77 - arr[63]);
    } else if (exponent == 1) {
      return abs(m00 - arr[0]) + abs(m01 - arr[1]) + abs(m02 - arr[2]) + abs(m03 - arr[3]) + abs(m04 - arr[4]) + abs(m05 - arr[5]) + abs(m06 - arr[6]) + abs(m07 - arr[7]) + abs(m10 - arr[8]) + abs(m11 - arr[9]) + abs(m12 - arr[10]) + abs(m13 - arr[11]) + abs(m14 - arr[12]) + abs(m15 - arr[13]) + abs(m16 - arr[14]) + abs(m17 - arr[15]) + abs(m20 - arr[16]) + abs(m21 - arr[17]) + abs(m22 - arr[18]) + abs(m23 - arr[19]) + abs(m24 - arr[20]) + abs(m25 - arr[21]) + abs(m26 - arr[22]) + abs(m27 - arr[23]) + abs(m30 - arr[24]) + abs(m31 - arr[25]) + abs(m32 - arr[26]) + abs(m33 - arr[27]) + abs(m34 - arr[28]) + abs(m35 - arr[29]) + abs(m36 - arr[30]) + abs(m37 - arr[31]) + abs(m40 - arr[32]) + abs(m41 - arr[33]) + abs(m42 - arr[34]) + abs(m43 - arr[35]) + abs(m44 - arr[36]) + abs(m45 - arr[37]) + abs(m46 - arr[38]) + abs(m47 - arr[39]) + abs(m50 - arr[40]) + abs(m51 - arr[41]) + abs(m52 - arr[42]) + abs(m53 - arr[43]) + abs(m54 - arr[44]) + abs(m55 - arr[45]) + abs(m56 - arr[46]) + abs(m57 - arr[47]) + abs(m60 - arr[48]) + abs(m61 - arr[49]) + abs(m62 - arr[50]) + abs(m63 - arr[51]) + abs(m64 - arr[52]) + abs(m65 - arr[53]) + abs(m66 - arr[54]) + abs(m67 - arr[55]) + abs(m70 - arr[56]) + abs(m71 - arr[57]) + abs(m72 - arr[58]) + abs(m73 - arr[59]) + abs(m74 - arr[60]) + abs(m75 - arr[61]) + abs(m76 - arr[62]) + abs(m77 - arr[63]);
    } else {
      return (float) (pow(abs(m00 - arr[0]), exponent) + pow(abs(m01 - arr[1]), exponent) + pow(abs(m02 - arr[2]), exponent) + pow(abs(m03 - arr[3]), exponent) + pow(abs(m04 - arr[4]), exponent) + pow(abs(m05 - arr[5]), exponent) + pow(abs(m06 - arr[6]), exponent) + pow(abs(m07 - arr[7]), exponent) + pow(abs(m10 - arr[8]), exponent) + pow(abs(m11 - arr[9]), exponent) + pow(abs(m12 - arr[10]), exponent) + pow(abs(m13 - arr[11]), exponent) + pow(abs(m14 - arr[12]), exponent) + pow(abs(m15 - arr[13]), exponent) + pow(abs(m16 - arr[14]), exponent) + pow(abs(m17 - arr[15]), exponent) + pow(abs(m20 - arr[16]), exponent) + pow(abs(m21 - arr[17]), exponent) + pow(abs(m22 - arr[18]), exponent) + pow(abs(m23 - arr[19]), exponent) + pow(abs(m24 - arr[20]), exponent) + pow(abs(m25 - arr[21]), exponent) + pow(abs(m26 - arr[22]), exponent) + pow(abs(m27 - arr[23]), exponent) + pow(abs(m30 - arr[24]), exponent) + pow(abs(m31 - arr[25]), exponent) + pow(abs(m32 - arr[26]), exponent) + pow(abs(m33 - arr[27]), exponent) + pow(abs(m34 - arr[28]), exponent) + pow(abs(m35 - arr[29]), exponent) + pow(abs(m36 - arr[30]), exponent) + pow(abs(m37 - arr[31]), exponent) + pow(abs(m40 - arr[32]), exponent) + pow(abs(m41 - arr[33]), exponent) + pow(abs(m42 - arr[34]), exponent) + pow(abs(m43 - arr[35]), exponent) + pow(abs(m44 - arr[36]), exponent) + pow(abs(m45 - arr[37]), exponent) + pow(abs(m46 - arr[38]), exponent) + pow(abs(m47 - arr[39]), exponent) + pow(abs(m50 - arr[40]), exponent) + pow(abs(m51 - arr[41]), exponent) + pow(abs(m52 - arr[42]), exponent) + pow(abs(m53 - arr[43]), exponent) + pow(abs(m54 - arr[44]), exponent) + pow(abs(m55 - arr[45]), exponent) + pow(abs(m56 - arr[46]), exponent) + pow(abs(m57 - arr[47]), exponent) + pow(abs(m60 - arr[48]), exponent) + pow(abs(m61 - arr[49]), exponent) + pow(abs(m62 - arr[50]), exponent) + pow(abs(m63 - arr[51]), exponent) + pow(abs(m64 - arr[52]), exponent) + pow(abs(m65 - arr[53]), exponent) + pow(abs(m66 - arr[54]), exponent) + pow(abs(m67 - arr[55]), exponent) + pow(abs(m70 - arr[56]), exponent) + pow(abs(m71 - arr[57]), exponent) + pow(abs(m72 - arr[58]), exponent) + pow(abs(m73 - arr[59]), exponent) + pow(abs(m74 - arr[60]), exponent) + pow(abs(m75 - arr[61]), exponent) + pow(abs(m76 - arr[62]), exponent) + pow(abs(m77 - arr[63]), exponent));
    }
  }

  @Override
  public float getLossClip(float[] arrUnclip, float[] arrMin0, float[] arrMax1, double exponent) {
    if (exponent == 2) {
      float l00 = m00 == 0 ? m00 - arrMin0[0] : (m00 == 1 ? m00 - arrMax1[0] : m00 - arrUnclip[0]);
      float l01 = m01 == 0 ? m01 - arrMin0[1] : (m01 == 1 ? m01 - arrMax1[1] : m01 - arrUnclip[1]);
      float l02 = m02 == 0 ? m02 - arrMin0[2] : (m02 == 1 ? m02 - arrMax1[2] : m02 - arrUnclip[2]);
      float l03 = m03 == 0 ? m03 - arrMin0[3] : (m03 == 1 ? m03 - arrMax1[3] : m03 - arrUnclip[3]);
      float l04 = m04 == 0 ? m04 - arrMin0[4] : (m04 == 1 ? m04 - arrMax1[4] : m04 - arrUnclip[4]);
      float l05 = m05 == 0 ? m05 - arrMin0[5] : (m05 == 1 ? m05 - arrMax1[5] : m05 - arrUnclip[5]);
      float l06 = m06 == 0 ? m06 - arrMin0[6] : (m06 == 1 ? m06 - arrMax1[6] : m06 - arrUnclip[6]);
      float l07 = m07 == 0 ? m07 - arrMin0[7] : (m07 == 1 ? m07 - arrMax1[7] : m07 - arrUnclip[7]);
      float l10 = m10 == 0 ? m10 - arrMin0[8] : (m10 == 1 ? m10 - arrMax1[8] : m10 - arrUnclip[8]);
      float l11 = m11 == 0 ? m11 - arrMin0[9] : (m11 == 1 ? m11 - arrMax1[9] : m11 - arrUnclip[9]);
      float l12 = m12 == 0 ? m12 - arrMin0[10] : (m12 == 1 ? m12 - arrMax1[10] : m12 - arrUnclip[10]);
      float l13 = m13 == 0 ? m13 - arrMin0[11] : (m13 == 1 ? m13 - arrMax1[11] : m13 - arrUnclip[11]);
      float l14 = m14 == 0 ? m14 - arrMin0[12] : (m14 == 1 ? m14 - arrMax1[12] : m14 - arrUnclip[12]);
      float l15 = m15 == 0 ? m15 - arrMin0[13] : (m15 == 1 ? m15 - arrMax1[13] : m15 - arrUnclip[13]);
      float l16 = m16 == 0 ? m16 - arrMin0[14] : (m16 == 1 ? m16 - arrMax1[14] : m16 - arrUnclip[14]);
      float l17 = m17 == 0 ? m17 - arrMin0[15] : (m17 == 1 ? m17 - arrMax1[15] : m17 - arrUnclip[15]);
      float l20 = m20 == 0 ? m20 - arrMin0[16] : (m20 == 1 ? m20 - arrMax1[16] : m20 - arrUnclip[16]);
      float l21 = m21 == 0 ? m21 - arrMin0[17] : (m21 == 1 ? m21 - arrMax1[17] : m21 - arrUnclip[17]);
      float l22 = m22 == 0 ? m22 - arrMin0[18] : (m22 == 1 ? m22 - arrMax1[18] : m22 - arrUnclip[18]);
      float l23 = m23 == 0 ? m23 - arrMin0[19] : (m23 == 1 ? m23 - arrMax1[19] : m23 - arrUnclip[19]);
      float l24 = m24 == 0 ? m24 - arrMin0[20] : (m24 == 1 ? m24 - arrMax1[20] : m24 - arrUnclip[20]);
      float l25 = m25 == 0 ? m25 - arrMin0[21] : (m25 == 1 ? m25 - arrMax1[21] : m25 - arrUnclip[21]);
      float l26 = m26 == 0 ? m26 - arrMin0[22] : (m26 == 1 ? m26 - arrMax1[22] : m26 - arrUnclip[22]);
      float l27 = m27 == 0 ? m27 - arrMin0[23] : (m27 == 1 ? m27 - arrMax1[23] : m27 - arrUnclip[23]);
      float l30 = m30 == 0 ? m30 - arrMin0[24] : (m30 == 1 ? m30 - arrMax1[24] : m30 - arrUnclip[24]);
      float l31 = m31 == 0 ? m31 - arrMin0[25] : (m31 == 1 ? m31 - arrMax1[25] : m31 - arrUnclip[25]);
      float l32 = m32 == 0 ? m32 - arrMin0[26] : (m32 == 1 ? m32 - arrMax1[26] : m32 - arrUnclip[26]);
      float l33 = m33 == 0 ? m33 - arrMin0[27] : (m33 == 1 ? m33 - arrMax1[27] : m33 - arrUnclip[27]);
      float l34 = m34 == 0 ? m34 - arrMin0[28] : (m34 == 1 ? m34 - arrMax1[28] : m34 - arrUnclip[28]);
      float l35 = m35 == 0 ? m35 - arrMin0[29] : (m35 == 1 ? m35 - arrMax1[29] : m35 - arrUnclip[29]);
      float l36 = m36 == 0 ? m36 - arrMin0[30] : (m36 == 1 ? m36 - arrMax1[30] : m36 - arrUnclip[30]);
      float l37 = m37 == 0 ? m37 - arrMin0[31] : (m37 == 1 ? m37 - arrMax1[31] : m37 - arrUnclip[31]);
      float l40 = m40 == 0 ? m40 - arrMin0[32] : (m40 == 1 ? m40 - arrMax1[32] : m40 - arrUnclip[32]);
      float l41 = m41 == 0 ? m41 - arrMin0[33] : (m41 == 1 ? m41 - arrMax1[33] : m41 - arrUnclip[33]);
      float l42 = m42 == 0 ? m42 - arrMin0[34] : (m42 == 1 ? m42 - arrMax1[34] : m42 - arrUnclip[34]);
      float l43 = m43 == 0 ? m43 - arrMin0[35] : (m43 == 1 ? m43 - arrMax1[35] : m43 - arrUnclip[35]);
      float l44 = m44 == 0 ? m44 - arrMin0[36] : (m44 == 1 ? m44 - arrMax1[36] : m44 - arrUnclip[36]);
      float l45 = m45 == 0 ? m45 - arrMin0[37] : (m45 == 1 ? m45 - arrMax1[37] : m45 - arrUnclip[37]);
      float l46 = m46 == 0 ? m46 - arrMin0[38] : (m46 == 1 ? m46 - arrMax1[38] : m46 - arrUnclip[38]);
      float l47 = m47 == 0 ? m47 - arrMin0[39] : (m47 == 1 ? m47 - arrMax1[39] : m47 - arrUnclip[39]);
      float l50 = m50 == 0 ? m50 - arrMin0[40] : (m50 == 1 ? m50 - arrMax1[40] : m50 - arrUnclip[40]);
      float l51 = m51 == 0 ? m51 - arrMin0[41] : (m51 == 1 ? m51 - arrMax1[41] : m51 - arrUnclip[41]);
      float l52 = m52 == 0 ? m52 - arrMin0[42] : (m52 == 1 ? m52 - arrMax1[42] : m52 - arrUnclip[42]);
      float l53 = m53 == 0 ? m53 - arrMin0[43] : (m53 == 1 ? m53 - arrMax1[43] : m53 - arrUnclip[43]);
      float l54 = m54 == 0 ? m54 - arrMin0[44] : (m54 == 1 ? m54 - arrMax1[44] : m54 - arrUnclip[44]);
      float l55 = m55 == 0 ? m55 - arrMin0[45] : (m55 == 1 ? m55 - arrMax1[45] : m55 - arrUnclip[45]);
      float l56 = m56 == 0 ? m56 - arrMin0[46] : (m56 == 1 ? m56 - arrMax1[46] : m56 - arrUnclip[46]);
      float l57 = m57 == 0 ? m57 - arrMin0[47] : (m57 == 1 ? m57 - arrMax1[47] : m57 - arrUnclip[47]);
      float l60 = m60 == 0 ? m60 - arrMin0[48] : (m60 == 1 ? m60 - arrMax1[48] : m60 - arrUnclip[48]);
      float l61 = m61 == 0 ? m61 - arrMin0[49] : (m61 == 1 ? m61 - arrMax1[49] : m61 - arrUnclip[49]);
      float l62 = m62 == 0 ? m62 - arrMin0[50] : (m62 == 1 ? m62 - arrMax1[50] : m62 - arrUnclip[50]);
      float l63 = m63 == 0 ? m63 - arrMin0[51] : (m63 == 1 ? m63 - arrMax1[51] : m63 - arrUnclip[51]);
      float l64 = m64 == 0 ? m64 - arrMin0[52] : (m64 == 1 ? m64 - arrMax1[52] : m64 - arrUnclip[52]);
      float l65 = m65 == 0 ? m65 - arrMin0[53] : (m65 == 1 ? m65 - arrMax1[53] : m65 - arrUnclip[53]);
      float l66 = m66 == 0 ? m66 - arrMin0[54] : (m66 == 1 ? m66 - arrMax1[54] : m66 - arrUnclip[54]);
      float l67 = m67 == 0 ? m67 - arrMin0[55] : (m67 == 1 ? m67 - arrMax1[55] : m67 - arrUnclip[55]);
      float l70 = m70 == 0 ? m70 - arrMin0[56] : (m70 == 1 ? m70 - arrMax1[56] : m70 - arrUnclip[56]);
      float l71 = m71 == 0 ? m71 - arrMin0[57] : (m71 == 1 ? m71 - arrMax1[57] : m71 - arrUnclip[57]);
      float l72 = m72 == 0 ? m72 - arrMin0[58] : (m72 == 1 ? m72 - arrMax1[58] : m72 - arrUnclip[58]);
      float l73 = m73 == 0 ? m73 - arrMin0[59] : (m73 == 1 ? m73 - arrMax1[59] : m73 - arrUnclip[59]);
      float l74 = m74 == 0 ? m74 - arrMin0[60] : (m74 == 1 ? m74 - arrMax1[60] : m74 - arrUnclip[60]);
      float l75 = m75 == 0 ? m75 - arrMin0[61] : (m75 == 1 ? m75 - arrMax1[61] : m75 - arrUnclip[61]);
      float l76 = m76 == 0 ? m76 - arrMin0[62] : (m76 == 1 ? m76 - arrMax1[62] : m76 - arrUnclip[62]);
      float l77 = m77 == 0 ? m77 - arrMin0[63] : (m77 == 1 ? m77 - arrMax1[63] : m77 - arrUnclip[63]);
      return l00 * l00 + l01 * l01 + l02 * l02 + l03 * l03 + l04 * l04 + l05 * l05 + l06 * l06 + l07 * l07 + l10 * l10 +
          l11 * l11 + l12 * l12 + l13 * l13 + l14 * l14 + l15 * l15 + l16 * l16 + l17 * l17 + l20 * l20 + l21 * l21 +
          l22 * l22 + l23 * l23 + l24 * l24 + l25 * l25 + l26 * l26 + l27 * l27 + l30 * l30 + l31 * l31 + l32 * l32 +
          l33 * l33 + l34 * l34 + l35 * l35 + l36 * l36 + l37 * l37 + l40 * l40 + l41 * l41 + l42 * l42 + l43 * l43 +
          l44 * l44 + l45 * l45 + l46 * l46 + l47 * l47 + l50 * l50 + l51 * l51 + l52 * l52 + l53 * l53 + l54 * l54 +
          l55 * l55 + l56 * l56 + l57 * l57 + l60 * l60 + l61 * l61 + l62 * l62 + l63 * l63 + l64 * l64 + l65 * l65 +
          l66 * l66 + l67 * l67 + l70 * l70 + l71 * l71 + l72 * l72 + l73 * l73 + l74 * l74 + l75 * l75 + l76 * l76 +
          l77 * l77;
    } else if (exponent == 1) {
      float loss = 0;
      loss += m00 == 0 ? abs(m00 - arrMin0[0]) : (m00 == 1 ? abs(m00 - arrMax1[0]) : abs(m00 - arrUnclip[0]));
      loss += m01 == 0 ? abs(m01 - arrMin0[1]) : (m01 == 1 ? abs(m01 - arrMax1[1]) : abs(m01 - arrUnclip[1]));
      loss += m02 == 0 ? abs(m02 - arrMin0[2]) : (m02 == 1 ? abs(m02 - arrMax1[2]) : abs(m02 - arrUnclip[2]));
      loss += m03 == 0 ? abs(m03 - arrMin0[3]) : (m03 == 1 ? abs(m03 - arrMax1[3]) : abs(m03 - arrUnclip[3]));
      loss += m04 == 0 ? abs(m04 - arrMin0[4]) : (m04 == 1 ? abs(m04 - arrMax1[4]) : abs(m04 - arrUnclip[4]));
      loss += m05 == 0 ? abs(m05 - arrMin0[5]) : (m05 == 1 ? abs(m05 - arrMax1[5]) : abs(m05 - arrUnclip[5]));
      loss += m06 == 0 ? abs(m06 - arrMin0[6]) : (m06 == 1 ? abs(m06 - arrMax1[6]) : abs(m06 - arrUnclip[6]));
      loss += m07 == 0 ? abs(m07 - arrMin0[7]) : (m07 == 1 ? abs(m07 - arrMax1[7]) : abs(m07 - arrUnclip[7]));
      loss += m10 == 0 ? abs(m10 - arrMin0[8]) : (m10 == 1 ? abs(m10 - arrMax1[8]) : abs(m10 - arrUnclip[8]));
      loss += m11 == 0 ? abs(m11 - arrMin0[9]) : (m11 == 1 ? abs(m11 - arrMax1[9]) : abs(m11 - arrUnclip[9]));
      loss += m12 == 0 ? abs(m12 - arrMin0[10]) : (m12 == 1 ? abs(m12 - arrMax1[10]) : abs(m12 - arrUnclip[10]));
      loss += m13 == 0 ? abs(m13 - arrMin0[11]) : (m13 == 1 ? abs(m13 - arrMax1[11]) : abs(m13 - arrUnclip[11]));
      loss += m14 == 0 ? abs(m14 - arrMin0[12]) : (m14 == 1 ? abs(m14 - arrMax1[12]) : abs(m14 - arrUnclip[12]));
      loss += m15 == 0 ? abs(m15 - arrMin0[13]) : (m15 == 1 ? abs(m15 - arrMax1[13]) : abs(m15 - arrUnclip[13]));
      loss += m16 == 0 ? abs(m16 - arrMin0[14]) : (m16 == 1 ? abs(m16 - arrMax1[14]) : abs(m16 - arrUnclip[14]));
      loss += m17 == 0 ? abs(m17 - arrMin0[15]) : (m17 == 1 ? abs(m17 - arrMax1[15]) : abs(m17 - arrUnclip[15]));
      loss += m20 == 0 ? abs(m20 - arrMin0[16]) : (m20 == 1 ? abs(m20 - arrMax1[16]) : abs(m20 - arrUnclip[16]));
      loss += m21 == 0 ? abs(m21 - arrMin0[17]) : (m21 == 1 ? abs(m21 - arrMax1[17]) : abs(m21 - arrUnclip[17]));
      loss += m22 == 0 ? abs(m22 - arrMin0[18]) : (m22 == 1 ? abs(m22 - arrMax1[18]) : abs(m22 - arrUnclip[18]));
      loss += m23 == 0 ? abs(m23 - arrMin0[19]) : (m23 == 1 ? abs(m23 - arrMax1[19]) : abs(m23 - arrUnclip[19]));
      loss += m24 == 0 ? abs(m24 - arrMin0[20]) : (m24 == 1 ? abs(m24 - arrMax1[20]) : abs(m24 - arrUnclip[20]));
      loss += m25 == 0 ? abs(m25 - arrMin0[21]) : (m25 == 1 ? abs(m25 - arrMax1[21]) : abs(m25 - arrUnclip[21]));
      loss += m26 == 0 ? abs(m26 - arrMin0[22]) : (m26 == 1 ? abs(m26 - arrMax1[22]) : abs(m26 - arrUnclip[22]));
      loss += m27 == 0 ? abs(m27 - arrMin0[23]) : (m27 == 1 ? abs(m27 - arrMax1[23]) : abs(m27 - arrUnclip[23]));
      loss += m30 == 0 ? abs(m30 - arrMin0[24]) : (m30 == 1 ? abs(m30 - arrMax1[24]) : abs(m30 - arrUnclip[24]));
      loss += m31 == 0 ? abs(m31 - arrMin0[25]) : (m31 == 1 ? abs(m31 - arrMax1[25]) : abs(m31 - arrUnclip[25]));
      loss += m32 == 0 ? abs(m32 - arrMin0[26]) : (m32 == 1 ? abs(m32 - arrMax1[26]) : abs(m32 - arrUnclip[26]));
      loss += m33 == 0 ? abs(m33 - arrMin0[27]) : (m33 == 1 ? abs(m33 - arrMax1[27]) : abs(m33 - arrUnclip[27]));
      loss += m34 == 0 ? abs(m34 - arrMin0[28]) : (m34 == 1 ? abs(m34 - arrMax1[28]) : abs(m34 - arrUnclip[28]));
      loss += m35 == 0 ? abs(m35 - arrMin0[29]) : (m35 == 1 ? abs(m35 - arrMax1[29]) : abs(m35 - arrUnclip[29]));
      loss += m36 == 0 ? abs(m36 - arrMin0[30]) : (m36 == 1 ? abs(m36 - arrMax1[30]) : abs(m36 - arrUnclip[30]));
      loss += m37 == 0 ? abs(m37 - arrMin0[31]) : (m37 == 1 ? abs(m37 - arrMax1[31]) : abs(m37 - arrUnclip[31]));
      loss += m40 == 0 ? abs(m40 - arrMin0[32]) : (m40 == 1 ? abs(m40 - arrMax1[32]) : abs(m40 - arrUnclip[32]));
      loss += m41 == 0 ? abs(m41 - arrMin0[33]) : (m41 == 1 ? abs(m41 - arrMax1[33]) : abs(m41 - arrUnclip[33]));
      loss += m42 == 0 ? abs(m42 - arrMin0[34]) : (m42 == 1 ? abs(m42 - arrMax1[34]) : abs(m42 - arrUnclip[34]));
      loss += m43 == 0 ? abs(m43 - arrMin0[35]) : (m43 == 1 ? abs(m43 - arrMax1[35]) : abs(m43 - arrUnclip[35]));
      loss += m44 == 0 ? abs(m44 - arrMin0[36]) : (m44 == 1 ? abs(m44 - arrMax1[36]) : abs(m44 - arrUnclip[36]));
      loss += m45 == 0 ? abs(m45 - arrMin0[37]) : (m45 == 1 ? abs(m45 - arrMax1[37]) : abs(m45 - arrUnclip[37]));
      loss += m46 == 0 ? abs(m46 - arrMin0[38]) : (m46 == 1 ? abs(m46 - arrMax1[38]) : abs(m46 - arrUnclip[38]));
      loss += m47 == 0 ? abs(m47 - arrMin0[39]) : (m47 == 1 ? abs(m47 - arrMax1[39]) : abs(m47 - arrUnclip[39]));
      loss += m50 == 0 ? abs(m50 - arrMin0[40]) : (m50 == 1 ? abs(m50 - arrMax1[40]) : abs(m50 - arrUnclip[40]));
      loss += m51 == 0 ? abs(m51 - arrMin0[41]) : (m51 == 1 ? abs(m51 - arrMax1[41]) : abs(m51 - arrUnclip[41]));
      loss += m52 == 0 ? abs(m52 - arrMin0[42]) : (m52 == 1 ? abs(m52 - arrMax1[42]) : abs(m52 - arrUnclip[42]));
      loss += m53 == 0 ? abs(m53 - arrMin0[43]) : (m53 == 1 ? abs(m53 - arrMax1[43]) : abs(m53 - arrUnclip[43]));
      loss += m54 == 0 ? abs(m54 - arrMin0[44]) : (m54 == 1 ? abs(m54 - arrMax1[44]) : abs(m54 - arrUnclip[44]));
      loss += m55 == 0 ? abs(m55 - arrMin0[45]) : (m55 == 1 ? abs(m55 - arrMax1[45]) : abs(m55 - arrUnclip[45]));
      loss += m56 == 0 ? abs(m56 - arrMin0[46]) : (m56 == 1 ? abs(m56 - arrMax1[46]) : abs(m56 - arrUnclip[46]));
      loss += m57 == 0 ? abs(m57 - arrMin0[47]) : (m57 == 1 ? abs(m57 - arrMax1[47]) : abs(m57 - arrUnclip[47]));
      loss += m60 == 0 ? abs(m60 - arrMin0[48]) : (m60 == 1 ? abs(m60 - arrMax1[48]) : abs(m60 - arrUnclip[48]));
      loss += m61 == 0 ? abs(m61 - arrMin0[49]) : (m61 == 1 ? abs(m61 - arrMax1[49]) : abs(m61 - arrUnclip[49]));
      loss += m62 == 0 ? abs(m62 - arrMin0[50]) : (m62 == 1 ? abs(m62 - arrMax1[50]) : abs(m62 - arrUnclip[50]));
      loss += m63 == 0 ? abs(m63 - arrMin0[51]) : (m63 == 1 ? abs(m63 - arrMax1[51]) : abs(m63 - arrUnclip[51]));
      loss += m64 == 0 ? abs(m64 - arrMin0[52]) : (m64 == 1 ? abs(m64 - arrMax1[52]) : abs(m64 - arrUnclip[52]));
      loss += m65 == 0 ? abs(m65 - arrMin0[53]) : (m65 == 1 ? abs(m65 - arrMax1[53]) : abs(m65 - arrUnclip[53]));
      loss += m66 == 0 ? abs(m66 - arrMin0[54]) : (m66 == 1 ? abs(m66 - arrMax1[54]) : abs(m66 - arrUnclip[54]));
      loss += m67 == 0 ? abs(m67 - arrMin0[55]) : (m67 == 1 ? abs(m67 - arrMax1[55]) : abs(m67 - arrUnclip[55]));
      loss += m70 == 0 ? abs(m70 - arrMin0[56]) : (m70 == 1 ? abs(m70 - arrMax1[56]) : abs(m70 - arrUnclip[56]));
      loss += m71 == 0 ? abs(m71 - arrMin0[57]) : (m71 == 1 ? abs(m71 - arrMax1[57]) : abs(m71 - arrUnclip[57]));
      loss += m72 == 0 ? abs(m72 - arrMin0[58]) : (m72 == 1 ? abs(m72 - arrMax1[58]) : abs(m72 - arrUnclip[58]));
      loss += m73 == 0 ? abs(m73 - arrMin0[59]) : (m73 == 1 ? abs(m73 - arrMax1[59]) : abs(m73 - arrUnclip[59]));
      loss += m74 == 0 ? abs(m74 - arrMin0[60]) : (m74 == 1 ? abs(m74 - arrMax1[60]) : abs(m74 - arrUnclip[60]));
      loss += m75 == 0 ? abs(m75 - arrMin0[61]) : (m75 == 1 ? abs(m75 - arrMax1[61]) : abs(m75 - arrUnclip[61]));
      loss += m76 == 0 ? abs(m76 - arrMin0[62]) : (m76 == 1 ? abs(m76 - arrMax1[62]) : abs(m76 - arrUnclip[62]));
      loss += m77 == 0 ? abs(m77 - arrMin0[63]) : (m77 == 1 ? abs(m77 - arrMax1[63]) : abs(m77 - arrUnclip[63]));
      return loss;
    } else {
      double loss = 0;
      loss += m00 == 0 ? pow(abs(m00 - arrMin0[0]), exponent) : (m00 == 1 ? pow(abs(m00 - arrMax1[0]), exponent) : pow(abs(m00 - arrUnclip[0]), exponent));
      loss += m01 == 0 ? pow(abs(m01 - arrMin0[1]), exponent) : (m01 == 1 ? pow(abs(m01 - arrMax1[1]), exponent) : pow(abs(m01 - arrUnclip[1]), exponent));
      loss += m02 == 0 ? pow(abs(m02 - arrMin0[2]), exponent) : (m02 == 1 ? pow(abs(m02 - arrMax1[2]), exponent) : pow(abs(m02 - arrUnclip[2]), exponent));
      loss += m03 == 0 ? pow(abs(m03 - arrMin0[3]), exponent) : (m03 == 1 ? pow(abs(m03 - arrMax1[3]), exponent) : pow(abs(m03 - arrUnclip[3]), exponent));
      loss += m04 == 0 ? pow(abs(m04 - arrMin0[4]), exponent) : (m04 == 1 ? pow(abs(m04 - arrMax1[4]), exponent) : pow(abs(m04 - arrUnclip[4]), exponent));
      loss += m05 == 0 ? pow(abs(m05 - arrMin0[5]), exponent) : (m05 == 1 ? pow(abs(m05 - arrMax1[5]), exponent) : pow(abs(m05 - arrUnclip[5]), exponent));
      loss += m06 == 0 ? pow(abs(m06 - arrMin0[6]), exponent) : (m06 == 1 ? pow(abs(m06 - arrMax1[6]), exponent) : pow(abs(m06 - arrUnclip[6]), exponent));
      loss += m07 == 0 ? pow(abs(m07 - arrMin0[7]), exponent) : (m07 == 1 ? pow(abs(m07 - arrMax1[7]), exponent) : pow(abs(m07 - arrUnclip[7]), exponent));
      loss += m10 == 0 ? pow(abs(m10 - arrMin0[8]), exponent) : (m10 == 1 ? pow(abs(m10 - arrMax1[8]), exponent) : pow(abs(m10 - arrUnclip[8]), exponent));
      loss += m11 == 0 ? pow(abs(m11 - arrMin0[9]), exponent) : (m11 == 1 ? pow(abs(m11 - arrMax1[9]), exponent) : pow(abs(m11 - arrUnclip[9]), exponent));
      loss += m12 == 0 ? pow(abs(m12 - arrMin0[10]), exponent) : (m12 == 1 ? pow(abs(m12 - arrMax1[10]), exponent) : pow(abs(m12 - arrUnclip[10]), exponent));
      loss += m13 == 0 ? pow(abs(m13 - arrMin0[11]), exponent) : (m13 == 1 ? pow(abs(m13 - arrMax1[11]), exponent) : pow(abs(m13 - arrUnclip[11]), exponent));
      loss += m14 == 0 ? pow(abs(m14 - arrMin0[12]), exponent) : (m14 == 1 ? pow(abs(m14 - arrMax1[12]), exponent) : pow(abs(m14 - arrUnclip[12]), exponent));
      loss += m15 == 0 ? pow(abs(m15 - arrMin0[13]), exponent) : (m15 == 1 ? pow(abs(m15 - arrMax1[13]), exponent) : pow(abs(m15 - arrUnclip[13]), exponent));
      loss += m16 == 0 ? pow(abs(m16 - arrMin0[14]), exponent) : (m16 == 1 ? pow(abs(m16 - arrMax1[14]), exponent) : pow(abs(m16 - arrUnclip[14]), exponent));
      loss += m17 == 0 ? pow(abs(m17 - arrMin0[15]), exponent) : (m17 == 1 ? pow(abs(m17 - arrMax1[15]), exponent) : pow(abs(m17 - arrUnclip[15]), exponent));
      loss += m20 == 0 ? pow(abs(m20 - arrMin0[16]), exponent) : (m20 == 1 ? pow(abs(m20 - arrMax1[16]), exponent) : pow(abs(m20 - arrUnclip[16]), exponent));
      loss += m21 == 0 ? pow(abs(m21 - arrMin0[17]), exponent) : (m21 == 1 ? pow(abs(m21 - arrMax1[17]), exponent) : pow(abs(m21 - arrUnclip[17]), exponent));
      loss += m22 == 0 ? pow(abs(m22 - arrMin0[18]), exponent) : (m22 == 1 ? pow(abs(m22 - arrMax1[18]), exponent) : pow(abs(m22 - arrUnclip[18]), exponent));
      loss += m23 == 0 ? pow(abs(m23 - arrMin0[19]), exponent) : (m23 == 1 ? pow(abs(m23 - arrMax1[19]), exponent) : pow(abs(m23 - arrUnclip[19]), exponent));
      loss += m24 == 0 ? pow(abs(m24 - arrMin0[20]), exponent) : (m24 == 1 ? pow(abs(m24 - arrMax1[20]), exponent) : pow(abs(m24 - arrUnclip[20]), exponent));
      loss += m25 == 0 ? pow(abs(m25 - arrMin0[21]), exponent) : (m25 == 1 ? pow(abs(m25 - arrMax1[21]), exponent) : pow(abs(m25 - arrUnclip[21]), exponent));
      loss += m26 == 0 ? pow(abs(m26 - arrMin0[22]), exponent) : (m26 == 1 ? pow(abs(m26 - arrMax1[22]), exponent) : pow(abs(m26 - arrUnclip[22]), exponent));
      loss += m27 == 0 ? pow(abs(m27 - arrMin0[23]), exponent) : (m27 == 1 ? pow(abs(m27 - arrMax1[23]), exponent) : pow(abs(m27 - arrUnclip[23]), exponent));
      loss += m30 == 0 ? pow(abs(m30 - arrMin0[24]), exponent) : (m30 == 1 ? pow(abs(m30 - arrMax1[24]), exponent) : pow(abs(m30 - arrUnclip[24]), exponent));
      loss += m31 == 0 ? pow(abs(m31 - arrMin0[25]), exponent) : (m31 == 1 ? pow(abs(m31 - arrMax1[25]), exponent) : pow(abs(m31 - arrUnclip[25]), exponent));
      loss += m32 == 0 ? pow(abs(m32 - arrMin0[26]), exponent) : (m32 == 1 ? pow(abs(m32 - arrMax1[26]), exponent) : pow(abs(m32 - arrUnclip[26]), exponent));
      loss += m33 == 0 ? pow(abs(m33 - arrMin0[27]), exponent) : (m33 == 1 ? pow(abs(m33 - arrMax1[27]), exponent) : pow(abs(m33 - arrUnclip[27]), exponent));
      loss += m34 == 0 ? pow(abs(m34 - arrMin0[28]), exponent) : (m34 == 1 ? pow(abs(m34 - arrMax1[28]), exponent) : pow(abs(m34 - arrUnclip[28]), exponent));
      loss += m35 == 0 ? pow(abs(m35 - arrMin0[29]), exponent) : (m35 == 1 ? pow(abs(m35 - arrMax1[29]), exponent) : pow(abs(m35 - arrUnclip[29]), exponent));
      loss += m36 == 0 ? pow(abs(m36 - arrMin0[30]), exponent) : (m36 == 1 ? pow(abs(m36 - arrMax1[30]), exponent) : pow(abs(m36 - arrUnclip[30]), exponent));
      loss += m37 == 0 ? pow(abs(m37 - arrMin0[31]), exponent) : (m37 == 1 ? pow(abs(m37 - arrMax1[31]), exponent) : pow(abs(m37 - arrUnclip[31]), exponent));
      loss += m40 == 0 ? pow(abs(m40 - arrMin0[32]), exponent) : (m40 == 1 ? pow(abs(m40 - arrMax1[32]), exponent) : pow(abs(m40 - arrUnclip[32]), exponent));
      loss += m41 == 0 ? pow(abs(m41 - arrMin0[33]), exponent) : (m41 == 1 ? pow(abs(m41 - arrMax1[33]), exponent) : pow(abs(m41 - arrUnclip[33]), exponent));
      loss += m42 == 0 ? pow(abs(m42 - arrMin0[34]), exponent) : (m42 == 1 ? pow(abs(m42 - arrMax1[34]), exponent) : pow(abs(m42 - arrUnclip[34]), exponent));
      loss += m43 == 0 ? pow(abs(m43 - arrMin0[35]), exponent) : (m43 == 1 ? pow(abs(m43 - arrMax1[35]), exponent) : pow(abs(m43 - arrUnclip[35]), exponent));
      loss += m44 == 0 ? pow(abs(m44 - arrMin0[36]), exponent) : (m44 == 1 ? pow(abs(m44 - arrMax1[36]), exponent) : pow(abs(m44 - arrUnclip[36]), exponent));
      loss += m45 == 0 ? pow(abs(m45 - arrMin0[37]), exponent) : (m45 == 1 ? pow(abs(m45 - arrMax1[37]), exponent) : pow(abs(m45 - arrUnclip[37]), exponent));
      loss += m46 == 0 ? pow(abs(m46 - arrMin0[38]), exponent) : (m46 == 1 ? pow(abs(m46 - arrMax1[38]), exponent) : pow(abs(m46 - arrUnclip[38]), exponent));
      loss += m47 == 0 ? pow(abs(m47 - arrMin0[39]), exponent) : (m47 == 1 ? pow(abs(m47 - arrMax1[39]), exponent) : pow(abs(m47 - arrUnclip[39]), exponent));
      loss += m50 == 0 ? pow(abs(m50 - arrMin0[40]), exponent) : (m50 == 1 ? pow(abs(m50 - arrMax1[40]), exponent) : pow(abs(m50 - arrUnclip[40]), exponent));
      loss += m51 == 0 ? pow(abs(m51 - arrMin0[41]), exponent) : (m51 == 1 ? pow(abs(m51 - arrMax1[41]), exponent) : pow(abs(m51 - arrUnclip[41]), exponent));
      loss += m52 == 0 ? pow(abs(m52 - arrMin0[42]), exponent) : (m52 == 1 ? pow(abs(m52 - arrMax1[42]), exponent) : pow(abs(m52 - arrUnclip[42]), exponent));
      loss += m53 == 0 ? pow(abs(m53 - arrMin0[43]), exponent) : (m53 == 1 ? pow(abs(m53 - arrMax1[43]), exponent) : pow(abs(m53 - arrUnclip[43]), exponent));
      loss += m54 == 0 ? pow(abs(m54 - arrMin0[44]), exponent) : (m54 == 1 ? pow(abs(m54 - arrMax1[44]), exponent) : pow(abs(m54 - arrUnclip[44]), exponent));
      loss += m55 == 0 ? pow(abs(m55 - arrMin0[45]), exponent) : (m55 == 1 ? pow(abs(m55 - arrMax1[45]), exponent) : pow(abs(m55 - arrUnclip[45]), exponent));
      loss += m56 == 0 ? pow(abs(m56 - arrMin0[46]), exponent) : (m56 == 1 ? pow(abs(m56 - arrMax1[46]), exponent) : pow(abs(m56 - arrUnclip[46]), exponent));
      loss += m57 == 0 ? pow(abs(m57 - arrMin0[47]), exponent) : (m57 == 1 ? pow(abs(m57 - arrMax1[47]), exponent) : pow(abs(m57 - arrUnclip[47]), exponent));
      loss += m60 == 0 ? pow(abs(m60 - arrMin0[48]), exponent) : (m60 == 1 ? pow(abs(m60 - arrMax1[48]), exponent) : pow(abs(m60 - arrUnclip[48]), exponent));
      loss += m61 == 0 ? pow(abs(m61 - arrMin0[49]), exponent) : (m61 == 1 ? pow(abs(m61 - arrMax1[49]), exponent) : pow(abs(m61 - arrUnclip[49]), exponent));
      loss += m62 == 0 ? pow(abs(m62 - arrMin0[50]), exponent) : (m62 == 1 ? pow(abs(m62 - arrMax1[50]), exponent) : pow(abs(m62 - arrUnclip[50]), exponent));
      loss += m63 == 0 ? pow(abs(m63 - arrMin0[51]), exponent) : (m63 == 1 ? pow(abs(m63 - arrMax1[51]), exponent) : pow(abs(m63 - arrUnclip[51]), exponent));
      loss += m64 == 0 ? pow(abs(m64 - arrMin0[52]), exponent) : (m64 == 1 ? pow(abs(m64 - arrMax1[52]), exponent) : pow(abs(m64 - arrUnclip[52]), exponent));
      loss += m65 == 0 ? pow(abs(m65 - arrMin0[53]), exponent) : (m65 == 1 ? pow(abs(m65 - arrMax1[53]), exponent) : pow(abs(m65 - arrUnclip[53]), exponent));
      loss += m66 == 0 ? pow(abs(m66 - arrMin0[54]), exponent) : (m66 == 1 ? pow(abs(m66 - arrMax1[54]), exponent) : pow(abs(m66 - arrUnclip[54]), exponent));
      loss += m67 == 0 ? pow(abs(m67 - arrMin0[55]), exponent) : (m67 == 1 ? pow(abs(m67 - arrMax1[55]), exponent) : pow(abs(m67 - arrUnclip[55]), exponent));
      loss += m70 == 0 ? pow(abs(m70 - arrMin0[56]), exponent) : (m70 == 1 ? pow(abs(m70 - arrMax1[56]), exponent) : pow(abs(m70 - arrUnclip[56]), exponent));
      loss += m71 == 0 ? pow(abs(m71 - arrMin0[57]), exponent) : (m71 == 1 ? pow(abs(m71 - arrMax1[57]), exponent) : pow(abs(m71 - arrUnclip[57]), exponent));
      loss += m72 == 0 ? pow(abs(m72 - arrMin0[58]), exponent) : (m72 == 1 ? pow(abs(m72 - arrMax1[58]), exponent) : pow(abs(m72 - arrUnclip[58]), exponent));
      loss += m73 == 0 ? pow(abs(m73 - arrMin0[59]), exponent) : (m73 == 1 ? pow(abs(m73 - arrMax1[59]), exponent) : pow(abs(m73 - arrUnclip[59]), exponent));
      loss += m74 == 0 ? pow(abs(m74 - arrMin0[60]), exponent) : (m74 == 1 ? pow(abs(m74 - arrMax1[60]), exponent) : pow(abs(m74 - arrUnclip[60]), exponent));
      loss += m75 == 0 ? pow(abs(m75 - arrMin0[61]), exponent) : (m75 == 1 ? pow(abs(m75 - arrMax1[61]), exponent) : pow(abs(m75 - arrUnclip[61]), exponent));
      loss += m76 == 0 ? pow(abs(m76 - arrMin0[62]), exponent) : (m76 == 1 ? pow(abs(m76 - arrMax1[62]), exponent) : pow(abs(m76 - arrUnclip[62]), exponent));
      loss += m77 == 0 ? pow(abs(m77 - arrMin0[63]), exponent) : (m77 == 1 ? pow(abs(m77 - arrMax1[63]), exponent) : pow(abs(m77 - arrUnclip[63]), exponent));
      return (float) loss;
    }
  }

  @Override
  public String toString() {
    return "[8x8]:  [" + m00 + ", " + m01 + ", " + m02 + ", " + m03 + ", " + m04 + ", " + m05 + ", " + m06 + ", " + m07
        + ", " + m10 + ", " + m11 + ", " + m12 + ", " + m13 + ", " + m14 + ", " + m15 + ", " + m16 + ", " + m17 + ", " +
        m20 + ", " + m21 + ", " + m22 + ", " + m23 + ", " + m24 + ", " + m25 + ", " + m26 + ", " + m27 + ", " + m30 +
        ", " + m31 + ", " + m32 + ", " + m33 + ", " + m34 + ", " + m35 + ", " + m36 + ", " + m37 + ", " + m40 + ", " +
        m41 + ", " + m42 + ", " + m43 + ", " + m44 + ", " + m45 + ", " + m46 + ", " + m47 + ", " + m50 + ", " + m51 +
        ", " + m52 + ", " + m53 + ", " + m54 + ", " + m55 + ", " + m56 + ", " + m57 + ", " + m60 + ", " + m61 + ", " +
        m62 + ", " + m63 + ", " + m64 + ", " + m65 + ", " + m66 + ", " + m67 + ", " + m70 + ", " + m71 + ", " + m72 +
        ", " + m73 + ", " + m74 + ", " + m75 + ", " + m76 + ", " + m77 + "]";
  }
}
