package com.jalvaviel.addon.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

public class FileSetting extends Setting<File> {
    public final List<File> values;
    private final List<String> suggestions;

    public FileSetting(String name, String description, File defaultValue, List<File> values,
                       Consumer<File> onChanged, Consumer<Setting<File>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.values = values;
        this.suggestions = new ArrayList<>(values.size());
        for (File value : values) suggestions.add(value.getName());
    }

    @Override
    protected File parseImpl(String str) {
        File file = new File(str);
        return values.contains(file) ? file : null;
    }

    @Override
    protected boolean isValueValid(File value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", get().getAbsolutePath());
        return tag;
    }

    @Override
    public File load(NbtCompound tag) {
        String FileString = tag.getString("value");
        File file = new File(FileString);
        if (isValueValid(file)) {
            set(file);
        }
        return get();
    }

    public static class Builder extends SettingBuilder<Builder, File, FileSetting> {
        private List<File> values = new ArrayList<>();

        public Builder() {
            super(null);
        }

        public Builder values(List<File> values) {
            this.values = values;
            return this;
        }

        @Override
        public FileSetting build() {
            return new FileSetting(name, description, defaultValue, values, onChanged, onModuleActivated, visible);
        }
    }
}

