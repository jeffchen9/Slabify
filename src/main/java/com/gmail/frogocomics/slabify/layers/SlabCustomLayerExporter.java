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

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.linalg.Matrix;
import com.gmail.frogocomics.slabify.shape.EmptyShape;
import com.gmail.frogocomics.slabify.shape.FullShape;
import com.gmail.frogocomics.slabify.shape.Shape;
import com.gmail.frogocomics.slabify.shape.Shape.Options;
import com.gmail.frogocomics.slabify.shape.Shapes;
import com.gmail.frogocomics.slabify.utils.Utils;
import org.javatuples.Quartet;
import org.pepsoft.minecraft.Chunk;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MixedMaterial;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.Tile;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.FirstPassLayerExporter;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

import static org.pepsoft.minecraft.Constants.MC_WATERLOGGED;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

public final class SlabCustomLayerExporter extends AbstractLayerExporter<Slab> implements
    FirstPassLayerExporter {

  private static final Logger logger = LoggerFactory.getLogger(SlabCustomLayerExporter.class);

  private final Map<String, Options> shapes;
  private int resolution = 1;
  private final List<Matrix> shapeMatrices = new ArrayList<>();
  // Shape, id, true if fill, false if cut, option associated with shape
  private final List<Quartet<Shape, Integer, Boolean, Options>> shapeIndices = new ArrayList<>();
  private final Map<String, Set<Integer>> availableIndices = new HashMap<>();
  private final Map<Tile, Shapemap> shapemaps = new HashMap<>();

  private Map<String, Material> mapping;

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

    // Disable if all shapes are disabled
    this.shapes = layer.getShapes();
    if (shapes.values().stream().allMatch(options -> options == Options.DISABLE)) {
      disable = true;
    } else {
      // If there are shapes that are enabled, get a list of the shapes

      // First, get the resolution
      for (Entry<String, Options> entry : layer.getShapes().entrySet()) {
        if (entry.getValue() != Options.DISABLE) {
          resolution = Math.max(resolution, Shapes.SHAPES.get(entry.getKey()).getMinimumResolution(entry.getValue()));
        }
      }

      logger.info("Resolution: {}", resolution);

      for (Entry<String, Options> entry : layer.getShapes().entrySet()) {
        Shape shape = Shapes.SHAPES.get(entry.getKey());
        Optional<List<Matrix>> optBakedShapes = shape.getBakedShapes(entry.getValue(), resolution);

        if (optBakedShapes.isPresent()) {
          List<Matrix> bakedShapes = optBakedShapes.get();

          // Fill
          shapeMatrices.addAll(bakedShapes);

          if (!layer.isAddHalf()) {
            // Cut
            for (Matrix m : bakedShapes) {
              Matrix copyM = m.clone();
              copyM.sub(resolution);
              shapeMatrices.add(copyM);
            }
          }

          // Fill
          for (int i = 0; i < bakedShapes.size(); i++) {
            shapeIndices.add(Quartet.with(shape, i, true, entry.getValue()));
          }

          if (!layer.isAddHalf()) {
            // Cut
            for (int i = 0; i < bakedShapes.size(); i++) {
              shapeIndices.add(Quartet.with(shape, i, false, entry.getValue()));
            }
          }
        }
      }

      // Add additional shapes: full and empty
      shapeMatrices.addAll(FullShape.getInstance().getBakedShapes(null, resolution).get());
      shapeIndices.add(Quartet.with(FullShape.getInstance(), 0, true, null));
      shapeMatrices.addAll(EmptyShape.getInstance().getBakedShapes(null, resolution).get());
      shapeIndices.add(Quartet.with(EmptyShape.getInstance(), 0, true, null));
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

      int[][][] shapemap;

      // Create shape map and difference map
      if (shapemaps.containsKey(tile)) {
        // The tile was already created
        shapemap = shapemaps.get(tile).map;
      } else {

        float[][] doubleHeightMap;
        logger.debug("Upscaling tile: {}, {}", tile.getX(), tile.getY());

        if (resolution == 1) {
          // No upscaling, just copy over everything.
          doubleHeightMap = new float[TILE_SIZE][TILE_SIZE];
          for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
              doubleHeightMap[x][y] = tile.getHeight(x, y);
            }
          }
        } else {
          doubleHeightMap = Utils.upscaleTile(tile, dimension, layer.getInterpolation(), resolution);
        }

        // We need to create a difference map
        int[][] originalMap = new int[TILE_SIZE][TILE_SIZE];
        for (int x = 0; x < TILE_SIZE; x++) {
          for (int y = 0; y < TILE_SIZE; y++) {
            originalMap[x][y] = tile.getIntHeight(x, y);
          }
        }

        float[][] differenceMap = Utils.getDifference(doubleHeightMap, originalMap, layer.isAddHalf());

        shapemap = Shapes.findMostSimilarShapes(differenceMap, resolution, shapeMatrices, Constants.LOSS_EXPONENT);
        shapemaps.put(tile, new Shapemap(shapemap));
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

          // TODO For future feature
          Material blockTwoAbove =
              (terrainHeight < maxHeight - 2) ? chunk.getMaterial(x, terrainHeight + 2, z)
                  : Material.AIR;

          // Do not place anything if the block below is not solid
          if (!blockBelow.solid) {
            continue;
          }

          String materialStr = blockBelow.namespace + ":" + blockBelow.simpleName;

          // Do not place anything if there is no slab material specified for the underlying block
          if (layer.mimicsTerrain() && !mapping.containsKey(materialStr)) {
            continue;
          }

          Material baseMaterial = layer.mimicsTerrain() ? mapping.get(materialStr)
              : mixedMaterial.getMaterial(seed, worldX, worldY, terrainHeight + 1);

          int[] shapemapI = shapemap[localX][localY];

          if (!availableIndices.containsKey(baseMaterial.name)) {
            availableIndices.put(baseMaterial.name, getAvailableIndices(baseMaterial.name));
          }

          Quartet<Shape, Integer, Boolean, Options> q = shapeIndices.get(Utils.filter(shapemapI, availableIndices.get(baseMaterial.name)));
          Material slabMaterial = q.getValue0().getMaterial(baseMaterial, q.getValue1(), q.getValue3());

          // If material is empty, skip
          if (slabMaterial == Material.AIR) {
            continue;
          }

          // If material is Conquest and the layer does not allow Conquest, skip
          if (slabMaterial.namespace.equals(Constants.CQ_NAMESPACE) && !layer.allowConquest()) {
            continue;
          }

          boolean fill = q.getValue2();

          if (fill) {

            // Full blocks will replace everything no matter what
            if (!(q.getValue0() instanceof FullShape)) {
              if (layer.replacesNonSolidBlocks() && blockAbove.solid) {
                continue;
              }

              if (!layer.replacesNonSolidBlocks() && (blockAbove != Material.AIR) && (blockAbove
                      != Material.STATIONARY_WATER) && (blockAbove != Material.WATER) && (blockAbove
                      != Material.FALLING_WATER)
                      && (blockAbove != Material.FLOWING_WATER) && !blockAbove.containsWater()) {
                continue;
              }
            }

            // Check for waterlogging
            if (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                || blockAbove.containsWater()) { // (material.hasProperty(MC_WATERLOGGED) && material.getProperty(MC_WATERLOGGED).equals("true"))
              if (!(q.getValue0() instanceof FullShape)) {
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
              slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
            }

            chunk.setMaterial(x, terrainHeight, z, slabMaterial);
          }
        }
      }
    }
  }

  private Set<Integer> getAvailableIndices(String baseMaterial) {
    List<String> availableShapes = Shapes.getAvailableShapes(baseMaterial);
    Set<Integer> availableIndices = new HashSet<>();

    for (int i = 0; i < shapeIndices.size(); i++) {

      Shape shape = shapeIndices.get(i).getValue0();
      if (availableShapes.contains(shape.getName())) {
        if (layer.allowConquest() || (Shapes.getMaterial(shape, baseMaterial) != null && Shapes.getMaterial(shape, baseMaterial).startsWith(Constants.MC_NAMESPACE))) {
          availableIndices.add(i);
        } else if (shape instanceof FullShape || shape instanceof EmptyShape) {
          availableIndices.add(i);
        }
      }
    }

    return availableIndices;
  }

  private static class Shapemap {

    private final int[][][] map;

    private Shapemap(int[][][] map) {
      this.map = map;
    }
  }
}