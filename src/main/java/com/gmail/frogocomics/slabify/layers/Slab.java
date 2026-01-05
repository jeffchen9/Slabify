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

import com.gmail.frogocomics.slabify.shape.Shape;
import com.gmail.frogocomics.slabify.shape.Shape.Options;
import com.gmail.frogocomics.slabify.shape.Shapes;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.MixedMaterialManager;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.renderers.LayerRenderer;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Automatically place slabs according to the heightmap. The user can specify a specific slab to
 * use or use the "mimic" function, which looks at the underlying block to determine which slab
 * to apply.
 */
public final class Slab extends CustomLayer {

  /**
   * This class is serialized in the .world file when it is saved, so it must be stable.
   * <ul>
   *   <li>1 for {@code 1.0.0-SNAPSHOT-6} and before</li>
   *   <li>2 for {@code 1.0.0-SNAPSHOT-7} and after</li>
   * </ul>
   */
  private static final long serialVersionUID = 2L;
  private MixedMaterial material;
  private boolean replace = false;
  private boolean mimic = false;
  private float height = 0;
  private boolean conquest = false;
  private Map<String, Material> mapping = new LinkedHashMap<>();
  private Map<String, Options> shapes;
  private Interpolation interpolation = Interpolation.BILINEAR;

  public Slab(String name, MixedMaterial material) {
    // Load slab icon from resources
    super(name, "A layer of " + material.getName() + " slab on top of the terrain", DataSize.BIT,
        65, Color.RED);

    // Make sure the material is registered, in case it's new
    MixedMaterialManager.getInstance().register(material);
    this.material = material;

    // Create default properties
    Map<String, Options> shapes = new HashMap<>();

    for (Shape shape : Shapes.shapesList) {
      if (shape.isVanilla()) {
        shapes.put(shape.getName(), Options.ENABLE);
      } else {
        shapes.put(shape.getName(), Options.DISABLE);
      }
    }

    this.shapes = shapes;
  }

  @Override
  public LayerRenderer getRenderer() {
    // If the paint happens to be an image, we just use the average color of the image instead.
    if (!(getPaint() instanceof Color)) {
      throw new IllegalStateException("Images are not supported");
    }

    return new SlabCustomLayerRenderer(((Color) getPaint()).getRGB(), getOpacity());
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    setDescription("A layer of slab on top of the terrain");
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
  
  @Override
  public LayerExporter getExporter(Dimension dimension, Platform platform,
                                   ExporterSettings settings) {
    return new SlabCustomLayerExporter(dimension, platform, this);
  }

  @Override
  public Slab clone() {
    Slab clone = (Slab) super.clone();
    clone.setMaterial(material.clone());
    clone.setReplaceNonSolidBlocks(replace);
    clone.setMimic(mimic);
    clone.setHeight(height);
    clone.setAllowConquest(conquest);
    clone.setMapping(mapping);
    clone.setShapes(shapes);
    clone.setInterpolation(interpolation);
    MixedMaterialManager.getInstance().register(clone.material);
    
    return clone;
  }

  /**
   * Get whether the slab will replace all non-solid blocks (other than air).
   *
   * @return {@code true} if the slab will replace all non-solid blocks.
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
   * @return {@code true} if the layer will look at the underlying block.
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
   * Get the height to add to the terrain.
   *
   * @return the height to add.
   */
  public float getHeight() {
    return height;
  }

  /**
   * Set the height to add to the terrain.
   *
   * @param height the height to add.
   */
  public void setHeight(float height) {
    // Check bounds--otherwise, strange behavior occurs
    if (height < 0 || height > 1.5) {
      throw new IllegalArgumentException("Height must be between 0 and 1.5");
    }

    this.height = height;
  }

  /**
   * Get whether the layer allows blocks with the {@code conquest} namespace.
   *
   * @return {@code true} if the layer allows Conquest blocks.
   */
  public boolean allowConquest() {
    return conquest;
  }

  /**
   * Set whether the layer allows blocks with the {@code conquest} namespace.
   *
   * @param conquest whether the layer allows Conquest blocks.
   */
  public void setAllowConquest(boolean conquest) {
    this.conquest = conquest;
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

  /**
   * Get a map of the option for each {@link Shape}.
   *
   * @return a map of the options.
   */
  public Map<String, Options> getShapes() {
    return shapes;
  }

  /**
   * Set a map of the options for each {@link Shape}.
   *
   * @param shapes a map of the options.
   */
  public void setShapes(Map<String, Options> shapes) {
    this.shapes = shapes;
  }

  /**
   * Get the interpolation method used for upscaling.
   *
   * @return the interpolation method.
   */
  public Interpolation getInterpolation() {
    return interpolation;
  }

  /**
   * Set the interpolation method used for upscaling.
   *
   * @param interpolation the interpolation method.
   */
  public void setInterpolation(Interpolation interpolation) {
    this.interpolation = interpolation;
  }

  /**
   * Represents the interpolation method to use for stairs (or more complicated shapes).
   */
  public enum Interpolation {
    /**
     * Bilinear interpolation (faster)
     */
    BILINEAR("Bilinear"),
    /**
     * Bicubic interpolation (slower and sharper)
     */
    BICUBIC("Bicubic");

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