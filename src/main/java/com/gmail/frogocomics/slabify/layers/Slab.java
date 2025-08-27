/*
 *     A plugin for WorldPainter that adds slab and stair detail to terrain.
 *     Copyright (C) 2025  Jeff Chen
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify.layers;

import com.gmail.frogocomics.slabify.HeightmapLayer;
import com.gmail.frogocomics.slabify.utils.Utils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.MixedMaterialManager;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.renderers.LayerRenderer;

/**
 * Automatically place slabs according to the heightmap. The user can specify a specific slab to
 * use or use the "mimic" function, which looks at the underlying block to determine which slab
 * to apply.
 */
public final class Slab extends CustomLayer implements HeightmapLayer {

  /**
   * This class is serialised in the .world file when it is saved, so it must be stable. It is
   * recommended to give it a fixed {@code serialVersionUID} and ensure that any changes are
   * backwards compatible.
   */
  private static final long serialVersionUID = 1L;

  private MixedMaterial material;
  private boolean replace = false;
  private boolean mimic = false;
  private Map<String, Material> mapping = new LinkedHashMap<>();
  private boolean stairs = false;
  private Interpolation interpolation = Interpolation.BILINEAR;
  private File heightmapLocation;

  public Slab(String name, MixedMaterial material) {
    // Load slab icon from resources
    super(name, "a layer of " + material.getName() + " slab on top of the terrain", DataSize.BIT,
        65, Color.RED);

    // Make sure the material is registered, in case it's new
    MixedMaterialManager.getInstance().register(material);
    this.material = material;
  }

  @Override
  public LayerRenderer getRenderer() {

    // If the paint happens to be an image, we just use average color instead.
    if (getPaint() instanceof BufferedImage) {
      return new SlabCustomLayerRenderer((Utils.averageColor((BufferedImage) getPaint())).getRGB(), getOpacity());
    }
    return new SlabCustomLayerRenderer(((Color) getPaint()).getRGB(), getOpacity());
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    setDescription("a layer of slab on top of the terrain");
  }

  @Override
  public String getType() {
    return "Slab";
  }

  /**
   * Get the material.
   *
   * @return the slab material.
   */
  public MixedMaterial getMaterial() {
    return material;
  }

  /**
   * Set the material.
   *
   * @param material the slab material.
   */
  public void setMaterial(MixedMaterial material) {
    this.material = material;
  }

  @Override
  public Class<? extends LayerExporter> getExporterType() {
    return SlabCustomLayerExporter.class;
  }

  /**
   * A custom layer must override this method. The default implementation only works for singular
   * non-configurable {@link Layer}s.
   */
  @Override
  public LayerExporter getExporter(Dimension dimension, Platform platform,
      ExporterSettings settings) {
    return new SlabCustomLayerExporter(dimension, platform, this);
  }

  @Override
  public Slab clone() {
    Slab clone = (Slab) super.clone();
    clone.setMaterial(material.clone());
    clone.setMimic(mimic);
    clone.setReplaceNonSolidBlocks(replace);
    clone.setMapping(mapping);
    clone.setUseStairs(stairs);
    clone.setInterpolation(interpolation);
    clone.setHeightmap(heightmapLocation);
    MixedMaterialManager.getInstance().register(clone.material);
    return clone;
  }

  /**
   * Get whether the slab will replace all non-solid blocks (other than air).
   *
   * @return <code>true</code> if the slab will replace all non-solid blocks.
   */
  public boolean replacesNonSolidBlocks() {
    return replace;
  }

  /**
   * Set whether the slab will replace all non-solid blocks.
   *
   * @param replace if the slab will replace all non-solid blocks.
   */
  public void setReplaceNonSolidBlocks(boolean replace) {
    this.replace = replace;
  }

  /**
   * Get whether the layer will use the underlying block to determine the type of slab that will
   * be placed, if any.
   *
   * @return <code>true</code> if the layer will look at the underlying block.
   */
  public boolean mimicsTerrain() {
    return mimic;
  }

  /**
   * Set whether the layer will use the underlying block to determine the type of slab that will be
   * placed, if any.
   *
   * @param mimic if the layer will look at the underlying block.
   */
  public void setMimic(boolean mimic) {
    this.mimic = mimic;
  }

  /**
   * Get the mapping as an unmodifiable map.
   *
   * @return the mapping, where the key represents the underlying block as a {@link String} and the
   * slab block as a {@link Material}.
   */
  public Map<String, Material> getMapping() {
    return Collections.unmodifiableMap(mapping);
  }

  /**
   * Set the mapping.
   *
   * @param mapping the mapping. A {@link LinkedHashMap} is recommended.
   */
  public void setMapping(Map<String, Material> mapping) {
    this.mapping = mapping;
  }

  public boolean useStairs() {
    return stairs;
  }

  public void setUseStairs(boolean stairs) {
    this.stairs = stairs;
  }

  public Interpolation getInterpolation() {
    return interpolation;
  }

  public void setInterpolation(Interpolation interpolation) {
    this.interpolation = interpolation;
  }

  @Override
  public Optional<File> getHeightmap() {
    return Optional.ofNullable(heightmapLocation);
  }

  @Override
  public void setHeightmap(File map) {
    heightmapLocation = map;
  }

  /**
   * Represents the interpolation method to use for stairs.
   */
  public enum Interpolation {
    BILINEAR("Bilinear"),
    BICUBIC("Bicubic"),
    HEIGHTMAP("From height map");

    private final String name;

    Interpolation(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}