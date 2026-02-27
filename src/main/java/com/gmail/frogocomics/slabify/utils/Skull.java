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

package com.gmail.frogocomics.slabify.utils;

import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.TileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the player head tile entity.
 */
public class Skull extends TileEntity {

  private static final long serialVersionUID = 1L;

  private final String texture;

  public Skull(@Nullable String texture) {
    super("minecraft:skull");
    this.texture = texture;

    if (texture != null) {
      Map<String, Tag> profileMap = new HashMap<>();
      List<CompoundTag> l = new ArrayList<>();
      Map<String, Tag> m = new HashMap<>();
      m.put("name", new StringTag("name", "textures"));
      m.put("value", new StringTag("value", texture));
      l.add(new CompoundTag(null, m));
      profileMap.put("properties", new ListTag<>("properties", CompoundTag.class, l));
      setMap("profile", profileMap);
    }
  }

  /**
   * Get the texture of the skull.
   *
   * @return the texture.
   */
  @Nullable
  public String getTexture() {
    return texture;
  }
}
