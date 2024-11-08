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

/*
public class FileSetting<T> extends Setting<T> {

    private final File[] values;
    private final List<String> suggestions;
    private final File File;
    private final String format;

    public FileSetting(String name, String description, T defaultValue, File File, String format,
                       Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated,
                       IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.File = File;
        this.format = format;
        this.values = valuesFromFile(File, format);
        this.suggestions = new ArrayList<>(values.length);
        for (File value : values) suggestions.add(value.getName());
    }

    protected static File[] valuesFromFile(File File, String format) {
        File[] listFiles = new File[0];
        if (Files.exists(File) && Files.isDirectory(File)) {
            FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(format);
            listFiles = File.toFile().listFiles(filter);
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

        private File File;
        private String format;

        public Builder() {
            super(null);
        }

        public Builder<T> File(File File) {
            this.File = File;
            return this;
        }

        public Builder<T> format(String format) {
            this.format = format;
            return this;
        }

        @Override
        public FileSetting<T> build() {
            return new FileSetting<>(name, description, defaultValue, File, format, onChanged, onModuleActivated, visible);
        }
    }
}

 */
