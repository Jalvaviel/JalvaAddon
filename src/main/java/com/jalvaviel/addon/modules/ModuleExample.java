package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;

import java.util.List;

public class ModuleExample extends Module {
    public ModuleExample() {
        super(Addon.CATEGORY, "example", "An example module in a custom category.");
    }
}
