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

package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.linalg.Matrix;
import com.gmail.frogocomics.slabify.utils.Skull;
import com.gmail.frogocomics.slabify.utils.Utils;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class HeadShape extends Shape {

  public static final String NAME = "head";
  public static final String BLOCK = "minecraft:player_head";

  private static final Map<String, String> heads = new HashMap<>();
  private final Material headMaterial = Material.get(BLOCK);
  private final Matrix shape = Matrix.of(new float[][]{
      {0, 0, 0, 0},
      {0, 0.5f, 0.5f, 0},
      {0, 0.5f, 0.5f, 0},
      {0, 0, 0, 0}
  });

  public HeadShape() {
    super("Head ", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 4, false, false, Options.DISABLE);

    // Populate heads array
    File mappingFile = Utils.addFileToAppData(Constants.HEAD_MAPPING_NAME);

    // Load the file
    List<String[]> rows;

    try {
      rows = Utils.readCsv(mappingFile);

      for (String[] row : rows) {
        String name = row[0];
        heads.put(name.startsWith(Constants.MC_NAMESPACE + ":") ? name : Constants.MC_NAMESPACE + ":" +  name, row[1]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    assert resolution >= getMinResolution(null);

    return selectedOption == Options.ENABLE ? Optional.of(Collections.singletonList(shape.upscale(resolution / 4))) : Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    return headMaterial;
  }

  @Override
  public void place(int worldX, int worldY, int worldZ, int localX, int localZ, Chunk chunk, Material material, Material baseMaterial) {
    String s = heads.get(baseMaterial.name);

    if (s != null) {
      Skull skull = new Skull(s);
      chunk.setMaterial(localX, worldY, localZ, headMaterial);
      skull.setX(worldX);
      skull.setY(worldY);
      skull.setZ(worldZ);
      chunk.getTileEntities().add(skull);
    }
  }

  public static Map<String, String> getHeads() {
    return heads;
  }
}
