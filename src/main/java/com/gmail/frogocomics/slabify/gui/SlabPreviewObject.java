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

package com.gmail.frogocomics.slabify.gui;

import com.gmail.frogocomics.slabify.shape.Shape;
import com.gmail.frogocomics.slabify.shape.Shapes;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.util.AttributeKey;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.objects.WPObject;

import javax.vecmath.Point3i;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SlabPreviewObject implements WPObject {

  private final int length;
  private final int width = 7;
  private final int height = 4;
  private final Material[][][] blocks;

  public SlabPreviewObject(Map<String, Material> mapping) {
    length = 2 * mapping.size() - 1;
    blocks = new Material[length][width][height];

    // Populate blocks
    // Fill array with air
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < width; j++) {
        for (int k = 0; k < height; k++) {
          blocks[i][j][k] = Material.AIR;
        }
      }
    }

    int i = 0;

    Shape slabShape = Shapes.shapesList.get(0); // Slab shape

    for (Entry<String, Material> entry : mapping.entrySet()) {
      Material baseMaterial = Material.getPrototype(entry.getKey());
      Material slabBlock = slabShape.getMaterial(baseMaterial, 0, null);

      // Set underlying block
      for (int j = 0; j < width; j++) {
        blocks[i][j][0] = baseMaterial;
      }

      blocks[i][4][1] = baseMaterial;
      blocks[i][5][1] = baseMaterial;
      blocks[i][6][1] = baseMaterial;
      blocks[i][6][2] = baseMaterial;

      // Set slab block
      blocks[i][2][1] = slabBlock;
      blocks[i][3][1] = slabBlock;
      blocks[i][5][2] = slabBlock;

      i += 2;
    }
  }

  @Override
  public Point3i getDimensions() {
    return new Point3i(length, width, height);
  }

  @Override
  public Material getMaterial(int x, int y, int z) {
    // x = length, y = width, z = height
    return blocks[x][y][z];
  }

  @Override
  public boolean getMask(int x, int y, int z) {
    // x = length, y = width, z = height
    Material material = getMaterial(x, y, z);
    return material != Material.AIR;
  }

  /**
   * Unused
   */
  @Override
  public Point3i getOffset() {
    return new Point3i(0, 0, 0);
  }

  /**
   * Unused
   */
  @Override
  public String getName() {
    return "";
  }

  /**
   * Unused
   */
  @Override
  public void setName(String s) {
  }

  /**
   * Unused
   */
  @Override
  public List<Entity> getEntities() {
    return Collections.emptyList();
  }

  /**
   * Unused
   */
  @Override
  public List<TileEntity> getTileEntities() {
    return Collections.emptyList();
  }

  /**
   * Unused
   */
  @Override
  public void prepareForExport(Dimension dimension) {
  }

  /**
   * Unused
   */
  @Override
  public Map<String, Serializable> getAttributes() {
    return Collections.emptyMap();
  }

  /**
   * Unused
   */
  @Override
  public void setAttributes(Map<String, Serializable> map) {
  }

  /**
   * Unused
   */
  @Override
  public <T extends Serializable> void setAttribute(AttributeKey<T> attributeKey, T t) {
  }

  /**
   * Unused
   */
  @Override
  public WPObject clone() {
    return this;
  }
}
