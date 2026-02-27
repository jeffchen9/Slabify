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

package com.gmail.frogocomics.slabify.layers;

import static com.gmail.frogocomics.slabify.Constants.CHUNK_SIZE;
import static com.gmail.frogocomics.slabify.Constants.CQ_NAMESPACE;
import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.minecraft.Constants.MC_WATERLOGGED;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import com.gmail.frogocomics.slabify.shape.*;
import com.gmail.frogocomics.slabify.shape.Shape.Options;
import com.gmail.frogocomics.slabify.utils.Utils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.*;
import java.util.Map.Entry;
import org.javatuples.Quartet;
import org.jspecify.annotations.Nullable;
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

public final class SlabCustomLayerExporter extends AbstractLayerExporter<Slab> implements
    FirstPassLayerExporter {

  private static final Logger logger = LoggerFactory.getLogger(SlabCustomLayerExporter.class);

  private final List<Matrix> shapeMatrices = new ArrayList<>();
  private int fullIdx;
  private int fullIdxStacked;
  private int emptyIdx;
  // Shape, id, layer (0 if cut, 1+ if fill), option associated with shape
  private Shape[] listShapes;
  private int[] listLocalIds;
  private int[] listHeights; // 0 if cut, 1+ if fill
  private Options[] listOptions; // Option corresponding to shape

  private final List<Matrix> shapeMatricesStacked = new ArrayList<>();
  private Shape[] listShapesStacked;
  private int[] listLocalIdsStacked;
  private Options[] listOptionsStacked;

  private final Map<String, Set<Integer>> availableIndices = new HashMap<>();
  private final Set<Integer> layerIndices = new HashSet<>();
  private final Map<String, Set<Integer>> availableIndicesNoLayer = new HashMap<>();
  private final Map<String, Set<Integer>> availableIndicesStacked = new HashMap<>();
  private final Map<Tile, Shapemap> shapemaps = new HashMap<>();
  private final Multiset<Tile> tileCounter = HashMultiset.create();
  private int resolution = 1;
  private Map<String, Material> mapping;
  private boolean stacking;

  // Buffers
  private float[][] heightmapBuffer;
  private float[][] differenceBuffer;

  private boolean disable = false;

  public SlabCustomLayerExporter(Dimension dimension, Platform platform, Slab layer) {
    super(dimension, platform, null, layer);

    logger.debug("Creating {}", getClass().getName());

    stacking = layer.supportsStacking() && layer.allowConquest();

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
      List<Quartet<Shape, Integer, Integer, Options>> shapeIndicesStacked = new ArrayList<>();

      for (Entry<String, Options> entry : layer.getShapes().entrySet()) {
        Shape shape = Shapes.shapesList.get(Shapes.shapesMap.inverse().get(entry.getKey()));
        Optional<List<Matrix>> optBakedShapes = shape.getShapeMatrices(entry.getValue(), resolution);

        if (optBakedShapes.isPresent()) {
          List<Matrix> bakedShapes = optBakedShapes.get();

          // Fill + 1
          shapeMatrices.addAll(bakedShapes);

          for (int i = 0; i < bakedShapes.size(); i++) {
            shapeIndices.add(Quartet.with(shape, i, 1, entry.getValue()));
          }

          if (shape.supportsStacking()) {
            shapeMatricesStacked.addAll(bakedShapes);

            for (int i = 0; i < bakedShapes.size(); i++) {
              shapeIndicesStacked.add(Quartet.with(shape, i, 1, entry.getValue()));
            }
          }

          if (!stacking) {
            // Fill + 2
            for (Matrix m : bakedShapes) {
              Matrix copyM = m.clone();
              copyM.add(1);
              shapeMatrices.add(copyM);
            }

            for (int i = 0; i < bakedShapes.size(); i++) {
              shapeIndices.add(Quartet.with(shape, i, 2, entry.getValue()));
            }

            // Cut + 0
            for (Matrix m : bakedShapes) {
              Matrix copyM = m.clone();
              copyM.sub(1);
              shapeMatrices.add(copyM);
            }

            for (int i = 0; i < bakedShapes.size(); i++) {
              shapeIndices.add(Quartet.with(shape, i, 0, entry.getValue()));
            }
          }
        }
      }

      // Add additional shapes: full and empty
      shapeMatrices.addAll(FullShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndices.add(Quartet.with(FullShape.getInstance(), 0, 1, null));
      shapeMatrices.addAll(EmptyShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndices.add(Quartet.with(EmptyShape.getInstance(), 0, 1, null));

      shapeMatricesStacked.addAll(FullShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndicesStacked.add(Quartet.with(FullShape.getInstance(), 0, 1, null));
      shapeMatricesStacked.addAll(EmptyShape.getInstance().getShapeMatrices(null, resolution).get());
      shapeIndicesStacked.add(Quartet.with(EmptyShape.getInstance(), 0, 1, null));

      fullIdx = shapeMatrices.size() - 2;
      fullIdxStacked = shapeMatricesStacked.size() - 2;
      emptyIdx = shapeMatrices.size() - 1;

      listShapes = new Shape[shapeMatrices.size()];
      listLocalIds = new int[shapeMatrices.size()];
      listHeights = new int[shapeMatrices.size()];
      listOptions = new Options[shapeMatrices.size()];
      listShapesStacked = new Shape[shapeMatricesStacked.size()];
      listLocalIdsStacked = new int[shapeMatricesStacked.size()];
      listOptionsStacked = new Options[shapeMatricesStacked.size()];

      for (int i = 0; i < shapeIndices.size(); i++) {
        Quartet<Shape, Integer, Integer, Options> q = shapeIndices.get(i);
        listShapes[i] = q.getValue0();
        listLocalIds[i] = q.getValue1();
        listHeights[i] = q.getValue2();
        listOptions[i] = q.getValue3();
      }

      for (int i = 0; i < shapeIndicesStacked.size(); i++) {
        Quartet<Shape, Integer, Integer, Options> q = shapeIndicesStacked.get(i);
        listShapesStacked[i] = q.getValue0();
        listLocalIdsStacked[i] = q.getValue1();
        listOptionsStacked[i] = q.getValue3();
      }

      for (int i = 0; i < listShapes.length; i++) {
        if (listShapes[i] instanceof LayerShape || listShapes[i] instanceof AltLayerShape) {
          layerIndices.add(i);
        }
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

      Shapemap shapemap;

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
        shapemap = Shapes.findMostSimilarShapes(differenceBuffer, resolution, shapeMatrices, shapeMatricesStacked, stacking);
        shapemaps.put(tile, shapemap);
      }

      int minZ = shapemap.getMinZ();
      int range = shapemap.getRange();

      for (int x = 0; x < CHUNK_SIZE; x++) {
        int localX = xOffset + x;
        int worldX = (chunk.getxPos() << 4) + x;

        for (int z = 0; z < CHUNK_SIZE; z++) {
          int localZ = zOffset + z;
          int worldZ = (chunk.getzPos() << 4) + z;

          // Do not place anything if the layer is not present
          if (!tile.getBitLayerValue(layer, localX, localZ)) {
            continue;
          }

          // Note: z is vertical direction in this case.
          int terrainHeight = tile.getIntHeight(localX, localZ);

          // Block stacking
          if (stacking) {
            Material blockBelow = chunk.getMaterial(x, terrainHeight, z);

            // Do not place anything if there is no slab material specified for the underlying block
            if (layer.mimicsTerrain() && !mapping.containsKey(blockBelow.name)) {
              continue;
            }

            Material baseMaterial = layer.mimicsTerrain() ? mapping.get(blockBelow.name)
                : mixedMaterial.getMaterial(seed, worldX, worldZ, terrainHeight + 1);

            if (!availableIndices.containsKey(baseMaterial.name)) {
              availableIndices.put(baseMaterial.name, getAvailableIndices(baseMaterial.name, false));
            }

            Set<Integer> availableIndex = availableIndices.get(baseMaterial.name);

            if (!availableIndicesNoLayer.containsKey(baseMaterial.name)) {
              Set<Integer> s = new HashSet<>(availableIndex);
              s.removeAll(layerIndices);
              availableIndicesNoLayer.put(baseMaterial.name, s);
            }

            if (!availableIndicesStacked.containsKey(baseMaterial.name)) {
              availableIndicesStacked.put(baseMaterial.name, getAvailableIndices(baseMaterial.name, true));
            }

            Set<Integer> availableIndexNoLayer = availableIndicesNoLayer.get(baseMaterial.name);
            Set<Integer> availableIndexStacked = availableIndicesStacked.get(baseMaterial.name);

            boolean top = true;

            for (int relZ = range - 1; relZ >= 0; relZ--) {
              boolean updateTop = true;
              int idx = shapemap.getIndexAt(localX, localZ, relZ, top ? availableIndex : availableIndexStacked);

              if (top && layerIndices.contains(idx) && relZ >= 1) {
                // Index indicates a layer shape; ensure that layer shapes only end up on full blocks
                int belowIdx = shapemap.getIndexAt(localX, localZ, relZ - 1, availableIndexStacked);

                // Block below layer is NOT full block
                if (belowIdx != fullIdxStacked) {
                  idx = shapemap.getIndexAt(localX, localZ, relZ, availableIndexNoLayer);

                  if (idx == emptyIdx) {
                    updateTop = false;
                  }
                }
              }

              Material slabMaterial = top ? listShapes[idx].getMaterial(baseMaterial, listLocalIds[idx], listOptions[idx]) :
                  listShapesStacked[idx].getMaterial(baseMaterial, listLocalIdsStacked[idx], listOptionsStacked[idx]);

              // If material is empty, skip
              if (slabMaterial == Material.AIR) {
                continue;
              }

              if (idx == (top ? fullIdx : fullIdxStacked) && relZ + minZ + 1 <= 0) {
                continue;
              }

//              // If material is Conquest and the layer does not allow Conquest, skip
//              if (slabMaterial.namespace.equals(Constants.CQ_NAMESPACE) && !layer.allowConquest()) {
//                continue;
//              }

              Material blockAbove = chunk.getMaterial(x, terrainHeight + relZ + minZ + 2, z);

              // Full blocks will replace everything no matter what
//              if (relZ + minZ + 1 > 0 && !(listShapes[idx] instanceof FullShape)) {
//                if (layer.replacesNonSolidBlocks() && blockAbove.solid) {
//                  continue;
//                }
//
//                if (!layer.replacesNonSolidBlocks() && (blockAbove != Material.AIR) && (blockAbove
//                    != Material.STATIONARY_WATER) && (blockAbove != Material.WATER) && (blockAbove
//                    != Material.FALLING_WATER)
//                    && (blockAbove != Material.FLOWING_WATER) && !blockAbove.containsWater()) {
//                  continue;
//                }
//              }

              // Check for waterlogging
              if (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                  blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                  || blockAbove.containsWater()) { // (material.hasProperty(MC_WATERLOGGED) && material.getProperty(MC_WATERLOGGED).equals("true"))
                if (top) {
                  if (!(listShapes[idx] instanceof FullShape)) {
                    slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
                  }
                } else {
                  if (!(listShapesStacked[idx] instanceof FullShape)) {
                    slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
                  }
                }

              }

              listShapesStacked[idx].place(worldX, terrainHeight + relZ + minZ + 1, worldZ, x, z, chunk, slabMaterial, baseMaterial);

              if (updateTop) {
                top = false;
              }
            }
          } else { // No block stacking
            // Get blocks
            Material blockBelow =
                ((terrainHeight >= minHeight) && (terrainHeight < maxHeight)) ? chunk.getMaterial(x,
                    terrainHeight, z) : Material.AIR;
            String materialStr = blockBelow.name;

            // Do not place anything if the block below is not solid
            if (!blockBelow.solid) {
              continue;
            }

            // Do not place anything if there is no slab material specified for the underlying block
            if (layer.mimicsTerrain() && !mapping.containsKey(materialStr)) {
              continue;
            }

            Material blockAbove =
                (terrainHeight < maxHeight - 1) ? chunk.getMaterial(x, terrainHeight + 1, z)
                    : null;

            Material blockTwoAbove =
                (terrainHeight < maxHeight - 2) ? chunk.getMaterial(x, terrainHeight + 2, z)
                    : null;

            Material blockThreeAbove =
                (terrainHeight < maxHeight - 3) ? chunk.getMaterial(x, terrainHeight + 3, z)
                    : null;

            Material baseMaterial = layer.mimicsTerrain() ? mapping.get(materialStr)
                : mixedMaterial.getMaterial(seed, worldX, worldZ, terrainHeight + 1);

            if (!availableIndices.containsKey(baseMaterial.name)) {
              availableIndices.put(baseMaterial.name, getAvailableIndices(baseMaterial.name, false));
            }

            int idx = shapemap.getIndexAt(localX, localZ, -1, availableIndices.get(baseMaterial.name));
            Material slabMaterial = listShapes[idx].getMaterial(baseMaterial, listLocalIds[idx], listOptions[idx]);

            // If material is empty, skip
            if (slabMaterial == Material.AIR) {
              continue;
            }

            // If material is Conquest and the layer does not allow Conquest, skip
            if (slabMaterial.namespace.equals(CQ_NAMESPACE) && !layer.allowConquest()) {
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
                  || blockAbove.containsWater()) {
                if (!(listShapes[idx] instanceof FullShape)) {
                  slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
                }
              }

              // Place the block
              listShapes[idx].place(worldX, terrainHeight + 1, worldZ, x, z, chunk, slabMaterial, baseMaterial);

              if (isDoubleBlock(blockAbove, blockTwoAbove)) {
                chunk.setMaterial(x, terrainHeight + 2, z, Material.AIR);
              }
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
                  || blockTwoAbove.containsWater()) {
                if (!(listShapes[idx] instanceof FullShape)) {
                  slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
                }
              }

              // Place the block
              listShapes[idx].place(worldX, terrainHeight + 2, worldZ, x, z, chunk, slabMaterial, baseMaterial);

              if (isDoubleBlock(blockTwoAbove, blockThreeAbove)) {
                chunk.setMaterial(x, terrainHeight + 3, z, Material.AIR);
              }
            } else { // Cut, height == 0
              // Check for waterlogging
              if (blockBelow == Material.STATIONARY_WATER ||
                  blockBelow == Material.WATER || blockBelow == Material.FALLING_WATER ||
                  blockBelow == Material.FLOWING_WATER || blockBelow.containsWater() ||
                  tile.getWaterLevel(localX, localZ) == terrainHeight) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              } else if (blockAbove != null && (blockAbove == Material.STATIONARY_WATER || blockAbove == Material.WATER ||
                  blockAbove == Material.FALLING_WATER || blockAbove == Material.FLOWING_WATER
                  || blockAbove.containsWater())) {
                slabMaterial = slabMaterial.withProperty(MC_WATERLOGGED, "true");
              }

              // Place the block
              listShapes[idx].place(worldX, terrainHeight, worldZ, x, z, chunk, slabMaterial, baseMaterial);

              if (isDoubleBlock(blockBelow, blockAbove)) {
                chunk.setMaterial(x, terrainHeight + 1, z, Material.AIR);
              }
            }
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

  private Set<Integer> getAvailableIndices(String baseMaterial, boolean stacked) {
    List<String> availableShapes = Shapes.getAvailableShapes(baseMaterial);
    Set<Integer> availableIndices = new HashSet<>();

    Shape[] arr = stacked ? listShapesStacked : listShapes;

    for (int i = 0; i < arr.length; i++) {

      Shape shape = arr[i];
      if (availableShapes.contains(shape.getName())) {
        if (layer.allowConquest() || (Shapes.getMaterial(shape, baseMaterial) != null &&
            !Shapes.getMaterial(shape, baseMaterial).startsWith(CQ_NAMESPACE))) {
          availableIndices.add(i);
        } else if (shape instanceof FullShape || shape instanceof EmptyShape) {
          availableIndices.add(i);
        }
      }
    }

    return availableIndices;
  }

  /**
   * Check whether two blocks form a double-height block such as tall grass.
   *
   * @param lower the lower block.
   * @param upper the upper block.
   * @return {@code true} if the two blocks for a double-height block.
   */
  private boolean isDoubleBlock(@Nullable Material lower, @Nullable Material upper) {
      return lower != null && upper != null && lower.hasProperty(MC_HALF) && lower.getProperty(MC_HALF).equals("lower") &&
          upper.hasProperty(MC_HALF) && upper.getProperty(MC_HALF).equals("upper") && lower.name.equals(upper.name);
  }
}