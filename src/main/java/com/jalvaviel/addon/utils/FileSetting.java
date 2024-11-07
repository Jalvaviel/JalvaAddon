package com.jalvaviel.addon.utils;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

public class FileSetting<T> extends Setting<T> {

    private final File[] values;
    private final List<String> suggestions;
    private final Path path;
    private final String format;

    // Constructor for FileSetting
    public FileSetting(String name, String description, T defaultValue, Path path, String format,
                       Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated,
                       IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.path = path;
        this.format = format;
        this.values = valuesFromPath(path, format);
        this.suggestions = new ArrayList<>(values.length);
        for (File value : values) suggestions.add(value.getName());
    }

    protected static File[] valuesFromPath(Path path, String format) {
        File[] listFiles = new File[0];
        if (Files.exists(path) && Files.isDirectory(path)) {
            FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(format);
            listFiles = path.toFile().listFiles(filter);
        }
        return listFiles;
    }

    @Override
    protected T parseImpl(String str) {
        for (File possibleValue : values) {
            if (str.equalsIgnoreCase(possibleValue.getName())) {
                return (T) possibleValue; // Safely casting to T
            }
        }
        return null;
    }

    @Override
    protected boolean isValueValid(T value) {
        return value instanceof File;
    }

    @Override
    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", get().toString());
        return tag;
    }

    @Override
    public T load(NbtCompound tag) {
        parse(tag.getString("value"));
        return get();
    }

    // Builder class to facilitate creating FileSetting
    public static class Builder<T> extends SettingBuilder<Builder<T>, T, FileSetting<T>> {

        private Path path;
        private String format;

        public Builder() {
            super(null);
        }

        public Builder<T> path(Path path) {
            this.path = path;
            return this;
        }

        public Builder<T> format(String format) {
            this.format = format;
            return this;
        }

        @Override
        public FileSetting<T> build() {
            return new FileSetting<>(name, description, defaultValue, path, format, onChanged, onModuleActivated, visible);
        }
    }
}
