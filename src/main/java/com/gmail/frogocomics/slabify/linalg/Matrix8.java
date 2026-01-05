package com.gmail.frogocomics.slabify.linalg;

import static java.lang.Math.abs;

public final class Matrix8 implements Matrix {

  float m00, m01, m02, m03, m04, m05, m06, m07, m10, m11, m12, m13, m14, m15, m16, m17, m20, m21, m22, m23, m24,
      m25, m26, m27, m30, m31, m32, m33, m34, m35, m36, m37, m40, m41, m42, m43, m44, m45, m46, m47, m50, m51, m52, m53,
      m54, m55, m56, m57, m60, m61, m62, m63, m64, m65, m66, m67, m70, m71, m72, m73, m74, m75, m76, m77;

  public Matrix8() {
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
      throw new IllegalArgumentException("Invalid scale");
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
  public float getLoss(float[] arr) {
    return abs(m00 - arr[0]) + abs(m01 - arr[1]) + abs(m02 - arr[2]) + abs(m03 - arr[3]) + abs(m04 - arr[4]) +
        abs(m05 - arr[5]) + abs(m06 - arr[6]) + abs(m07 - arr[7]) + abs(m10 - arr[8]) + abs(m11 - arr[9]) +
        abs(m12 - arr[10]) + abs(m13 - arr[11]) + abs(m14 - arr[12]) + abs(m15 - arr[13]) + abs(m16 - arr[14]) +
        abs(m17 - arr[15]) + abs(m20 - arr[16]) + abs(m21 - arr[17]) + abs(m22 - arr[18]) + abs(m23 - arr[19]) +
        abs(m24 - arr[20]) + abs(m25 - arr[21]) + abs(m26 - arr[22]) + abs(m27 - arr[23]) + abs(m30 - arr[24]) +
        abs(m31 - arr[25]) + abs(m32 - arr[26]) + abs(m33 - arr[27]) + abs(m34 - arr[28]) + abs(m35 - arr[29]) +
        abs(m36 - arr[30]) + abs(m37 - arr[31]) + abs(m40 - arr[32]) + abs(m41 - arr[33]) + abs(m42 - arr[34]) +
        abs(m43 - arr[35]) + abs(m44 - arr[36]) + abs(m45 - arr[37]) + abs(m46 - arr[38]) + abs(m47 - arr[39]) +
        abs(m50 - arr[40]) + abs(m51 - arr[41]) + abs(m52 - arr[42]) + abs(m53 - arr[43]) + abs(m54 - arr[44]) +
        abs(m55 - arr[45]) + abs(m56 - arr[46]) + abs(m57 - arr[47]) + abs(m60 - arr[48]) + abs(m61 - arr[49]) +
        abs(m62 - arr[50]) + abs(m63 - arr[51]) + abs(m64 - arr[52]) + abs(m65 - arr[53]) + abs(m66 - arr[54]) +
        abs(m67 - arr[55]) + abs(m70 - arr[56]) + abs(m71 - arr[57]) + abs(m72 - arr[58]) + abs(m73 - arr[59]) +
        abs(m74 - arr[60]) + abs(m75 - arr[61]) + abs(m76 - arr[62]) + abs(m77 - arr[63]);
  }
}
