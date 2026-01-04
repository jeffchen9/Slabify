package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.jspecify.annotations.Nullable;
import org.pepsoft.minecraft.Material;

import java.util.*;

import static org.pepsoft.minecraft.Constants.MC_LAYERS;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class LayerShape extends Shape {

  public static final String NAME = "layer";
  private final Map<String, Material[]> materials = new HashMap<>();

  public LayerShape() {
    super("Layer", NAME, new Options[]{Options.DISABLE, Options.ENABLE}, true, 1);
  }

  @Override
  public Optional<List<Matrix>> getShapeMatrices(Options selectedOption, int resolution) {

    if (selectedOption == Options.ENABLE) {
      List<Matrix> shapes = new ArrayList<>();

      for (int i = 1; i < 8; i++) {
        shapes.add((Matrix.of(new float[][]{{i / 8f}})).upscale(resolution / getMinResolution(null)));
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

      Material[] layerMaterials = new Material[7];

      for (int j = 1; j < 8; j++) {
        layerMaterials[j - 1] = Material.get(materialName, MC_LAYERS, j);
      }

      materials.put(baseMaterial.name, layerMaterials);
    }

    return materials.get(baseMaterial.name)[i];
  }
}
