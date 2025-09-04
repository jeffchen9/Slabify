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

import static org.pepsoft.minecraft.Constants.MC_WATERLOGGED;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

import com.gmail.frogocomics.slabify.Shapes;
import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import com.gmail.frogocomics.slabify.utils.Utils;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SlabCustomLayerExporter extends AbstractLayerExporter<Slab> implements
    FirstPassLayerExporter, SecondPassLayerExporter {

  private static final Logger logger = LoggerFactory.getLogger(SlabCustomLayerExporter.class);

  private final Map<Tile, Shapemap> shapemaps = new HashMap<>();
  private final Map<String, Material[]> materials = new HashMap<>();

  private float[][] heightmap;
  private Map<String, Material> mapping;
  private boolean finished = false;

  private int cornerX;
  private int cornerY;

  private boolean disable = false;

  public SlabCustomLayerExporter(Dimension dimension, Platform platform, Slab layer) {
    super(dimension, platform, null, layer);

    logger.debug("Creating {}", getClass().getName());

    if (layer.mimicsTerrain()) {
      mapping = layer.getMapping();

      if (mapping.isEmpty()) {
        logger.warn("The layer \"{}\" has an empty mapping.", layer.getName());
        disable = true;
      }
    }

    if (layer.useStairs() && layer.getInterpolation() == Interpolation.HEIGHTMAP
        && layer.getHeightmap().isPresent()) {
      // Check if the current world is square
      int minX = Integer.MAX_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int maxY = Integer.MIN_VALUE;

      for (Point p : dimension.getTileCoords()) {
        int tx = p.x;
        int ty = p.y;

        minX = Math.min(minX, tx);
        maxX = Math.max(maxX, tx);
        minY = Math.min(minY, ty);
        maxY = Math.max(maxY, ty);
      }

      int expectedCount = (maxX - minX + 1) * (maxY - minY + 1);

      if (expectedCount != dimension.getTileCount()) {
        disable = true;
      } else {
        cornerX = dimension.getLowestX();
        cornerY = dimension.getLowestY();

        try {
          heightmap = HeightmapStore.INSTANCE.loadHeightmap(layer, layer.getHeightmap().get(),
              dimension);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /**
   * This method will be invoked once for every chunk as it is being created during an Export. It
   * should read the layer values for its own layer from the {@link Tile} and then edit the
   * {@link Chunk} as required. For {@code Tile}s that don't contain this layer this method will not
   * be executed at all.
   *
   * <p>For most layers you will only need to implement this <em>or</em>
   * {@link SecondPassLayerExporter}, not both, although that is possible for complicated
   * situation.
   *
   * <p>Note that the operation must be deterministic. I.e. it must produce the same results for
   * the same world seed and coordinates, and the results must align with those that the exporter
   * would create for the same world seed in neighbouring chunks.
   *
   * @param tile  The {@code Tile} that the current chunk belongs to. Get the layer values from this
   *              object.
   * @param chunk The {@code Chunk} that is currently being created. Apply your changes to this
   *              object.
   */
  @Override
  public void render(Tile tile, Chunk chunk) {

    if (!disable) {
      int xOffset = (chunk.getxPos() & 7) << 4;
      int zOffset = (chunk.getzPos() & 7) << 4;
      MixedMaterial mixedMaterial = layer.getMaterial();
      long seed = dimension.getSeed();

      int[][] shapemap = null;

      if (layer.useStairs()) {
        if (shapemaps.containsKey(tile)) {
          // The tile was already created
          shapemap = shapemaps.get(tile).map;
        } else {
          float[][] doubleHeightMap;

          if (layer.getInterpolation() == Interpolation.BILINEAR
              || layer.getInterpolation() == Interpolation.BICUBIC) {
            logger.debug("Upscaling tile: {}, {}", tile.getX(), tile.getY());
            doubleHeightMap = Utils.upscaleTile(tile, dimension, layer.getInterpolation());
          } else { // if (layer.getInterpolation() == Interpolation.HEIGHTMAP)
            doubleHeightMap = new float[TILE_SIZE * 2][TILE_SIZE * 2];

            int tx = tile.getX() * TILE_SIZE * 2 - cornerX * 2;
            int ty = tile.getY() * TILE_SIZE * 2 - cornerY * 2;

            for (int x = 0; x < TILE_SIZE * 2; x++) {
              for (int y = 0; y < TILE_SIZE * 2; y++) {
                doubleHeightMap[x][y] = heightmap[ty + y][tx + x];
              }
            }

          }

          // We need to create a difference map
          int[][] originalMap = new int[TILE_SIZE][TILE_SIZE];
          for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
              originalMap[x][y] = tile.getIntHeight(x, y);
            }
          }

          float[][] differenceMap = Utils.getDifference(doubleHeightMap, originalMap);

          shapemap = Shapes.findMostSimilarShapes(differenceMap);
          shapemaps.put(tile, new Shapemap(shapemap));
        }
      }

      for (int x = 0; x < 16; x++) {
        int localX = xOffset + x;
        int worldX = (chunk.getxPos() << 4) + x;

        for (int z = 0; z < 16; z++) {

          int localY = zOffset + z;
          int worldY = (chunk.getzPos() << 4) + z;

          // Do not place anything if the layer is not present
          if (!tile.getBitLayerValue(layer, localX, localY)) {
            continue;
          }

          // Note: z is vertical direction in this case.
          int terrainHeight = tile.getIntHeight(localX, localY);

          // Get blocks
          Material blockBelow =
              ((terrainHeight >= minHeight) && (terrainHeight < maxHeight)) ? chunk.getMaterial(x,
                  terrainHeight, z) : Material.AIR;

          Material blockAbove =
              (terrainHeight < maxHeight - 1) ? chunk.getMaterial(x, terrainHeight + 1, z)
                  : Material.AIR;

          Material blockTwoAbove =
              (terrainHeight < maxHeight - 2) ? chunk.getMaterial(x, terrainHeight + 2, z)
                  : Material.AIR;

          // Do not place anything if the block below is solid
          if (!blockBelow.solid) {
            continue;
          }

          String materialStr = blockBelow.namespace + ":" + blockBelow.simpleName;

          // Do not place anything if there is no slab material specified for the underlying block
          if (layer.mimicsTerrain() && !mapping.containsKey(materialStr)) {
            continue;
          }

          Material slabMaterial = layer.mimicsTerrain() ? mapping.get(materialStr)
              : mixedMaterial.getMaterial(seed, worldX, worldY, terrainHeight + 1);
          String baseMaterial = Shapes.getBaseMaterial(slabMaterial);

          if (!materials.containsKey(baseMaterial)) {
            Material[] arr = new Material[Shapes.getShapesLength()];
            for (int i = 0; i < Shapes.getShapesLength(); i++) {
              arr[i] = Shapes.getStairMaterial(baseMaterial, i);
            }
            materials.put(baseMaterial, arr);
          }

          boolean fullBlock = false;
          boolean fill = true;

          if (layer.useStairs()) {
            int idx = shapemap[localX][localY];

            if (idx == 0) {
              // FULL BLOCK
              slabMaterial = blockBelow;
              fullBlock = true;
            } else if (idx <= 26) {
              slabMaterial = materials.get(baseMaterial)[idx];
            } else if (idx == 27) {
              continue;
            }

            fill = Shapes.isFill(idx);
          } else {
            double diff = dimension.getHeightAt(worldX, worldY) - terrainHeight;
            if (!(diff >= 0 && diff < 0.5)) {
              continue;
            }

            slabMaterial = materials.get(baseMaterial)[1];
          }

          if (fill) {
            if (layer.replacesNonSolidBlocks() && blockAbove.solid) {
              continue;
            }

            if (!layer.replacesNonSolidBlocks() && (blockAbove != Material.AIR) && (blockAbove
                != Material.STATIONARY_WATER) && (blockAbove != Material.WATER) && (blockAbove
                != Material.FALLING_WATER)
                && (blockAbove != Material.FLOWING_WATER) && !blockAbove.containsWater()) {
              continue;
            }

            // Check for waterlogging
            if (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                || blockAbove.containsWater()) { // (material.hasProperty(MC_WATERLOGGED) && material.getProperty(MC_WATERLOGGED).equals("true"))
              if (!fullBlock && slabMaterial != Material.AIR) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              }
            }

            chunk.setMaterial(x, terrainHeight + 1, z, slabMaterial);

            // Need to deal with double height blocks. In this case, we need to remove yet another block!
//          if (layer.replacesNonSolidBlocks() && blockAbove.hasProperty("half")
//              && blockAbove.getProperty("half").equals("lower")) {
//            if (blockTwoAbove.name.equals(blockAbove.name) && blockTwoAbove.hasProperty("half")
//                && blockTwoAbove.getProperty("half").equals("upper")) {
//              chunk.setMaterial(x, terrainHeight + 2, z, Material.AIR);
//            }
//          }
          } else { // Cut
            // Check for waterlogging
            if (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                || blockAbove.containsWater() || blockBelow == Material.STATIONARY_WATER ||
                blockBelow == Material.WATER || blockBelow == Material.FALLING_WATER ||
                blockBelow == Material.FLOWING_WATER || blockBelow.containsWater() ||
                tile.getWaterLevel(localX, localY) == terrainHeight) {
              if (slabMaterial != Material.AIR) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              }
            }

            chunk.setMaterial(x, terrainHeight, z, slabMaterial);
          }
        }
      }
    }
  }

  @Override
  public Set<Stage> getStages() {
    return EnumSet.of(Stage.CARVE);
  }

  @Override
  public List<Fixup> carve(Rectangle area, Rectangle exportedArea, MinecraftWorld minecraftWorld) {
    // No operations are actually done here. This is just to signal that the export is finished.
    if (!finished) { // Ensure exporterFinished() is called only once for each thread.
      finished = true;

      HeightmapStore.INSTANCE.exporterFinished(layer);
    }

    return null;
  }

  private static class Shapemap {

    private final int[][] map;

    private Shapemap(int[][] map) {
      this.map = map;
    }
  }

  private static class NeuralNetworkTileStore {

    private static final NeuralNetworkTileStore INSTANCE = new NeuralNetworkTileStore();

    private final ConcurrentMap<Tile, Shapemap> map = new ConcurrentHashMap<>();


  }

  private static class HeightmapStore {

    private static final HeightmapStore INSTANCE = new HeightmapStore();

    private final ConcurrentMap<Slab, Entry> layerMap = new ConcurrentHashMap<>();

    private float[][] loadHeightmap(Slab layer, File heightmapLocation, Dimension dimension)
        throws IOException {
      Entry entry = layerMap.compute(layer, (key, existing) -> {
        if (existing != null) {
          existing.activeExporters.incrementAndGet();
          return existing;
        }

        try {
          BufferedImage image = ImageIO.read(heightmapLocation);

          if (image.getRaster().getDataBuffer().getDataType() != DataBuffer.TYPE_USHORT) {
            throw new IllegalArgumentException("Image is not 16-bit grayscale.");
          }

          int width = image.getWidth();
          int height = image.getHeight();
          Raster raster = image.getRaster();

          float[][] loadedHeightmap = new float[height][width];

          float minValue = Float.MAX_VALUE;
          float maxValue = -Float.MAX_VALUE;

          for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
              int pixelValue = raster.getSample(x, y, 0);  // channel 0
              float value = pixelValue / 65535f;
              loadedHeightmap[y][x] = value;

              if (value > maxValue) {
                maxValue = value;
              }

              if (value < minValue) {
                minValue = value;
              }
            }
          }

          // Get max and min value in heightmap
          float minWorldValue = dimension.getLowestHeight();
          float maxWorldValue = dimension.getHighestHeight();

          Utils.remap(loadedHeightmap, maxValue, minValue, maxWorldValue, minWorldValue);

          logger.debug("Loaded heightmap: {}", heightmapLocation.getName());

          return new Entry(loadedHeightmap, heightmapLocation);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      return entry.heightmap;
    }

    public void exporterFinished(Slab layer) {
      Entry entry = layerMap.get(layer);

      if (entry != null && entry.activeExporters.decrementAndGet() == 0) {
        layerMap.remove(layer);
        logger.debug("Unloaded heightmap: {}", entry.sourceFile.getName());
        System.gc();
      }
    }

    private static class Entry {

      private final AtomicInteger activeExporters = new AtomicInteger(1);
      private final float[][] heightmap;
      private final File sourceFile;

      private Entry(float[][] heightmap, File sourceFile) {
        this.heightmap = heightmap;
        this.sourceFile = sourceFile;
      }
    }
  }
}