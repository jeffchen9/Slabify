package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Material;

import java.util.*;

/**
 *
 *
 * This shape is only available in Conquest Reforged.
 */
public class AltLayerShape extends Shape {

  public static final String NAME = "alt_layer";
  private final Map<String, Material[]> materials = new HashMap<>();
  private final float[] heights = new float[]{0.125f, 0.25f, 0.5f, 0.75f};

  public AltLayerShape() {
    super("Alt Layer", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, false, 1);
  }

  @Override
  public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

    if (selectedOption == Options.ENABLE) {
      List<Matrix> shapes = new ArrayList<>();


      for (int i = 1; i < heights.length; i++) {
        shapes.add((new Matrix(new float[][]{{heights[i]}})).upscale(resolution / getMinimumResolution(null)));
      }

      return Optional.of(shapes);
    }

    return Optional.empty();
  }

  @Override
  public Material getMaterial(Material baseMaterial, int i, @Nullable Options option) {
    if (!materials.containsKey(baseMaterial.name)) {
      // Create materials if does not exist
      String materialName = Shapes.getMaterial(this, baseMaterial.name);

      Material[] layerMaterials = new Material[heights.length];

      for (int j = 0; j < heights.length; j++) {
        layerMaterials[j] = Material.get(materialName, Constants.CQ_LAYER, j + 1);
      }

      materials.put(baseMaterial.name, layerMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
