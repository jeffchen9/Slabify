package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.pepsoft.minecraft.Material;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 *
 * This shape is available in Vanilla.
 */
public class EmptyShape extends Shape {

    private static final EmptyShape instance = new EmptyShape();

    public static final String NAME = "empty";
    private final Matrix shape = new Matrix(new float[][]{{0}});

    public EmptyShape() {
        super(null, NAME, new Options[]{}, true, 1);
    }

    public static EmptyShape getInstance() {
        return instance;
    }

    @Override
    public Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution) {
        return Optional.of(Collections.singletonList(shape.upscale(resolution / getMinimumResolution())));
    }

    @Override
    public Material getMaterial(Material baseMaterial, int i) {
        return Material.AIR;
    }
}
