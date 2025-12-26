package com.gmail.frogocomics.slabify.shape;

import com.gmail.frogocomics.slabify.linalg.Matrix;
import org.pepsoft.minecraft.Material;

import java.util.List;
import java.util.Optional;

/**
 * Abstract class representing a block "shape" like stairs and slabs; a seperate class is needed to account for the
 * complex shapes provided by Conquest.
 */
public abstract class Shape {

    private final String displayName;
    private final String name;
    private final List<Options> availableOptions;
    private final boolean vanilla;
    private final int minResolution;

    public Shape(String displayName, String name, Options[] availableOptions, boolean vanilla, int minResolution) {
        this.displayName = displayName;
        this.name = name;
        this.availableOptions = List.of(availableOptions);
        this.vanilla = vanilla;
        this.minResolution = minResolution;
    }

    public String getName() {
        return name;
    }

    public List<Options> getAvailableOptions() {
        return availableOptions;
    }

    /**
     * Check if the shape is vanilla.
     *
     * @return <code>true</code> if the shape is vanilla.
     */
    public boolean isVanilla() {
        return vanilla;
    }

    /**
     * Get the minimum multiplier needed for upscaling. Vanilla blocks require 2x upscaling. However, some Conquest
     * shapes require more upscaling.
     *
     * @return the minimum upscaling needed.
     */
    public int getMinimumResolution() {
        return minResolution;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public abstract Optional<List<Matrix>> getBakedShapes(Options selectedOption, int resolution);

    public abstract Material getMaterial(Material baseMaterial, int i);

    public enum Options {
        DISABLE("Disable"),
        ENABLE("Enable"),
        EIGHTHS("Eighths"),
        QUARTERS("Quarters"),
        HALVES("Halves");

        private final String displayText;

        Options(String displayText) {
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }
}
