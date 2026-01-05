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
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
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

  private final List<Matrix> shapeMatrices = new ArrayList<>();
  // Shape, id, layer (0 if cut, 1+ if fill), option associated with shape
  private Shape[] listShapes;
  private int[] listLocalIds;
  private int[] listHeights; // 0 if cut, 1+ if fill
  private Options[] listOptions; // Option corresponding to shape

  private final Map<String, Set<Integer>> availableIndices = new HashMap<>();
  private final Map<Tile, int[][][]> shapemaps = new HashMap<>();
  private final Multiset<Tile> tileCounter = HashMultiset.create();
  private int resolution = 1;
  private Map<String, Material> mapping;

  // Buffers
  private float[][] heightmapBuffer;
  private float[][] differenceBuffer;

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
    if (layer.getShapes().values().stream().allMatch(options -> options == Options.DISABLE)) {
      disable = true;
    } else {
      // If there are shapes that are enabled, get a list of the shapes

      // First, get the resolution
      for (Entry<String, Options> entry : layer.getShapes().entrySet()) {
        if (entry.getValue() != Options.DISABLE) {
          resolution = Math.max(resolution,
              Shapes.shapesList.get(Shapes.shapesMap.inverse().get(entry.getKey())).getMinResolution(entry.getValue()));
        }
      }

      List<Quartet<Shape, Integer, Integer, Options>> shapeIndices = new ArrayList<>();

      for (Entry<String, Options> entry : layer.getShapes().entrySet()) {
        Shape shape = Shapes.shapesList.get(Shapes.shapesMap.inverse().get(entry.getKey()));
        Optional<List<Matrix>> optBakedShapes = shape.getShapeMatrices(entry.getValue(), resolution);

        if (optBakedShapes.isPresent()) {
          List<Matrix> bakedShapes = optBakedShapes.get();

          // Fill + 1
          shapeMatrices.addAll(bakedShapes);

          // Fill + 2
          for (Matrix m : bakedShapes) {
            Matrix copyM = m.clone();
            copyM.add(resolution);
            shapeMatrices.add(copyM);
          }

          // Cut + 0
          for (Matrix m : bakedShapes) {
            Matrix copyM = m.clone();
            copyM.sub(resolution);
            shapeMatrices.add(copyM);
          }

          // Fill + 1
          for (int i = 0; i < bakedShapes.size(); i++) {
            shapeIndices.add(Quartet.with(shape, i, 1, entry.getValue()));
          }

          // Fill + 2
          for (int i = 0; i < bakedShapes.size(); i++) {
            shapeIndices.add(Quartet.with(shape, i, 2, entry.getValue()));
          }

          // Cut + 0
          for (int i = 0; i < bakedShapes.size(); i++) {
            shapeIndices.add(Quartet.with(shape, i, 0, entry.getValue()));
          }
        }
      }

      // Add additional shapes: full and empty
      shapeMatrices.addAll(FullShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndices.add(Quartet.with(FullShape.getInstance(), 0, 1, null));
      shapeMatrices.addAll(EmptyShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndices.add(Quartet.with(EmptyShape.getInstance(), 0, 1, null));

      listShapes = new Shape[shapeMatrices.size()];
      listLocalIds = new int[shapeMatrices.size()];
      listHeights = new int[shapeMatrices.size()];
      listOptions = new Options[shapeMatrices.size()];

      for (int i = 0; i < shapeIndices.size(); i++) {
        Quartet<Shape, Integer, Integer, Options> q = shapeIndices.get(i);
        listShapes[i] = q.getValue0();
        listLocalIds[i] = q.getValue1();
        listHeights[i] = q.getValue2();
        listOptions[i] = q.getValue3();
      }

      // Create buffers
      heightmapBuffer = new float[TILE_SIZE * resolution][TILE_SIZE * resolution];
      differenceBuffer = new float[TILE_SIZE * resolution][TILE_SIZE * resolution];
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
        shapemap = shapemaps.get(tile);
      } else {
        logger.debug("Upscaling tile: {}, {}", tile.getX(), tile.getY());

        if (resolution == 1) {
          // No upscaling, just copy over everything.
          for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
              heightmapBuffer[x][y] = tile.getHeight(x, y);
            }
          }
        } else {
          Utils.upscaleTile(tile, dimension, layer.getInterpolation(), resolution, heightmapBuffer);
        }

        Utils.getDifference(heightmapBuffer, tile, layer.getHeight(), differenceBuffer);
        shapemap = Shapes.findMostSimilarShapes(differenceBuffer, resolution, shapeMatrices);
        shapemaps.put(tile, shapemap);
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
                  : null;

          Material blockTwoAbove =
              (terrainHeight < maxHeight - 2) ? chunk.getMaterial(x, terrainHeight + 2, z)
                  : null;

          // Do not place anything if the block below is not solid
          if (!blockBelow.solid) {
            continue;
          }

          String materialStr = blockBelow.name;

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

          int idx = Utils.filter(shapemapI, availableIndices.get(baseMaterial.name));

          Material slabMaterial = listShapes[idx].getMaterial(baseMaterial, listLocalIds[idx], listOptions[idx]);

          // If material is empty, skip
          if (slabMaterial == Material.AIR) {
            continue;
          }

          // If material is Conquest and the layer does not allow Conquest, skip
          if (slabMaterial.namespace.equals(Constants.CQ_NAMESPACE) && !layer.allowConquest()) {
            continue;
          }

          int height = listHeights[idx];

          if (height >= 1 && blockAbove == null) {
            continue;
          }

          if (height == 1) {

            // Full blocks will replace everything no matter what
            if (!(listShapes[idx] instanceof FullShape)) {
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
              if (!(listShapes[idx] instanceof FullShape)) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              }
            }

            chunk.setMaterial(x, terrainHeight + 1, z, slabMaterial);
          } else if (height == 2) {
            // Set the base material
            chunk.setMaterial(x, terrainHeight + 1, z, baseMaterial);

            if (blockTwoAbove == null) {
              continue;
            }

            // Full blocks will replace everything no matter what
            if (!(listShapes[idx] instanceof FullShape)) {
              if (layer.replacesNonSolidBlocks() && blockTwoAbove.solid) {
                continue;
              }

              if (!layer.replacesNonSolidBlocks() && (blockTwoAbove != Material.AIR) && (blockTwoAbove
                  != Material.STATIONARY_WATER) && (blockTwoAbove != Material.WATER) && (blockTwoAbove
                  != Material.FALLING_WATER)
                  && (blockTwoAbove != Material.FLOWING_WATER) && !blockTwoAbove.containsWater()) {
                continue;
              }
            }

            // Check for waterlogging
            if (blockTwoAbove == Material.STATIONARY_WATER || blockTwoAbove == Material.WATER ||
                blockTwoAbove == Material.FALLING_WATER || blockTwoAbove == Material.FLOWING_WATER
                || blockTwoAbove.containsWater()) { // (material.hasProperty(MC_WATERLOGGED) && material.getProperty(MC_WATERLOGGED).equals("true"))
              if (!(listShapes[idx] instanceof FullShape)) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              }
            }

            chunk.setMaterial(x, terrainHeight + 2, z, slabMaterial);
          } else { // Cut, height == 0
            // Check for waterlogging
            if (blockBelow == Material.STATIONARY_WATER ||
                blockBelow == Material.WATER || blockBelow == Material.FALLING_WATER ||
                blockBelow == Material.FLOWING_WATER || blockBelow.containsWater() ||
                tile.getWaterLevel(localX, localY) == terrainHeight) {
              slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
            } else if (blockAbove != null && (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                || blockAbove.containsWater())) {
              slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
            }

            chunk.setMaterial(x, terrainHeight, z, slabMaterial);
          }
        }
      }

      tileCounter.add(tile);

      // 64 chunks per tile (8*8)
      // Remove shapemap to save memory when all chunks in particular tile have been fully processed
      if (tileCounter.count(tile) == 64) {
        shapemaps.remove(tile);
        tileCounter.remove(tile, 64);
      }
    }
  }

  private Set<Integer> getAvailableIndices(String baseMaterial) {
    List<String> availableShapes = Shapes.getAvailableShapes(baseMaterial);
    Set<Integer> availableIndices = new HashSet<>();

    for (int i = 0; i < listShapes.length; i++) {

      Shape shape = listShapes[i];
      if (availableShapes.contains(shape.getName())) {
        if (layer.allowConquest() || (Shapes.getMaterial(shape, baseMaterial) != null &&
            !Shapes.getMaterial(shape, baseMaterial).startsWith(Constants.CQ_NAMESPACE))) {
          availableIndices.add(i);
        } else if (shape instanceof FullShape || shape instanceof EmptyShape) {
          availableIndices.add(i);
        }
      }
    }

    return availableIndices;
  }
}