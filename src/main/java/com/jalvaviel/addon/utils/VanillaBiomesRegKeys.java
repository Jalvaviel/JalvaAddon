package com.jalvaviel.addon.utils;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class VanillaBiomesRegKeys {
    // The singleton instance
    private static VanillaBiomesRegKeys instance;

    // List to hold the registry keys
    private final List<RegistryKey<Biome>> biomes;

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
    public void add(RegistryKey<Biome> biomeKey) {
        biomes.add(biomeKey);
    }

    // Method to get all registered biome keys
    public List<RegistryKey<Biome>> getBiomes() {
        return new ArrayList<>(biomes); // Return a copy to prevent modification
    }
}
