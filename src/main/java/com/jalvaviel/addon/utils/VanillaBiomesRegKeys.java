package com.jalvaviel.addon.utils;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VanillaBiomesRegKeys {
    // The singleton instance
    private static VanillaBiomesRegKeys instance;

    // List to hold the registry keys
    private final List<String> biomes;

    // Private constructor to prevent instantiation
    private VanillaBiomesRegKeys() {
        biomes = new ArrayList<>();
    }

    // Method to get the singleton instance
    public static VanillaBiomesRegKeys getInstance() {
        if (instance == null) {
            instance = new VanillaBiomesRegKeys();
        }
        return instance;
    }

    // Method to add a biome registry key
    public void add(String biome) {
        biomes.add(biome);
    }

    // Method to get all registered biome keys
    public List<String> getBiomes() {
        return new ArrayList<>(biomes); // Return a copy to prevent modification
    }

    public Iterable<Identifier> getBiomeIds() {
        return biomes.stream()
            .map(Identifier::of) // Map each string to an Identifier
            .collect(Collectors.toList());
    }
}
