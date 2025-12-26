package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.pepsoft.minecraft.Constants;
import org.pepsoft.minecraft.Material;

import java.util.*;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class SlabShape extends Shape {

    public static final String NAME = "slab";

    private final Map<String, Material> materials = new HashMap<>();
    private final Matrix shape;

    public SlabShape() {
        super("Slab", NAME, new Options[]{Options.ENABLE, Options.DISABLE}, true, 1);
        shape = new Matrix(new float[][] {{0.5f}});
    }

    @Override
    public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {

        assert resolution >= getMinimumResolution();

        return selectedOption == Options.ENABLE ? Optional.of(Collections.singletonList(shape.upscale(resolution / getMinimumResolution()))) : Optional.empty();
    }

    @Override
    public Material getMaterial(Material baseMaterial, int i) {
        if (materials.containsKey(baseMaterial.name)) {
            return materials.get(baseMaterial.name);
        }

        String materialName = Shapes.getMaterial(this, baseMaterial.name);

        if (materialName == null) {
            // Default
            materialName = baseMaterial.name + "_slab";
        }

        Material newMaterial = Material.get(materialName, Constants.MC_HALF, "bottom");
        materials.put(baseMaterial.name, newMaterial);

        return newMaterial;
    }
}
