/*
 * This file is part of Animatica - https://github.com/FoundationGames/Animatica
 * Copyright (C) FoundationGames: https://github.com/FoundationGames/Animatica
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.foundationgames.animatica.config;

import io.github.foundationgames.animatica.Animatica;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CyclingOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AnimaticaConfig {
    public boolean animatedTextures;
    public Integer maxAnimFrames;
    public boolean safeMode;

    public static String ANIMATED_TEXTURES_KEY = "animated_textures";
    public static String MAX_ANIM_FRAMES_KEY = "max_animation_frames";
    public static String SAFE_MODE_KEY = "safe_mode";

    public static final String FILE_NAME = "animatica.properties";
    public static final String[] COMMENTS = {
            "Configuration file for Animatica",
            "animated_textures=<true|false> - Determines whether custom texture animation support should be enabled or not",
            "max_animation_frames=<integer value, or 'none'> - Maximum unique animation frames a texture can have, to prevent high RAM/VRAM usage (disabled when set to 'none')",
            "safe_mode=<true|false> - When enabled, doesn't generate animations, however provides important debug information"
    };

    public static final CyclingOption<Boolean> ANIMATED_TEXTURES_OPTION = CyclingOption.create("option.animatica.animated_textures", opts -> {
        try {
            Animatica.CONFIG.load();
        } catch (IOException e) { Animatica.LOG.error("Error loading config for options screen!", e); }
        return Animatica.CONFIG.animatedTextures;
    }, (opts, option, value) -> {
        Animatica.CONFIG.animatedTextures = value;
        try {
            Animatica.CONFIG.save();
        } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
        MinecraftClient.getInstance().reloadResources();
    });

    public AnimaticaConfig() {
        try {
            load();
        } catch (IOException e) {
            Animatica.LOG.error("Error loading config during initialization!", e);
        }
    }

    public void writeTo(Properties properties) {
        properties.put(ANIMATED_TEXTURES_KEY, Boolean.toString(animatedTextures));
        properties.put(MAX_ANIM_FRAMES_KEY, maxAnimFrames == null ? "none" : maxAnimFrames.toString());
        properties.put(SAFE_MODE_KEY, Boolean.toString(safeMode));
    }

    public void readFrom(Properties properties) {
        this.animatedTextures = boolFrom(properties.getProperty(ANIMATED_TEXTURES_KEY), true);
        this.maxAnimFrames = nullableIntFrom(properties.getProperty(MAX_ANIM_FRAMES_KEY), 7000); // 7K frames should often go past a gigabyte worth of animation, memory failsafe
        this.safeMode = boolFrom(properties.getProperty(SAFE_MODE_KEY), false);
    }

    public Path getFile() throws IOException {
        var file = MinecraftClient.getInstance().runDirectory.toPath().getFileSystem().getPath("config").resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        return file;
    }

    public void save() throws IOException {
        var file = getFile();

        var ppt = new Properties();
        writeTo(ppt);

        try (var os = Files.newOutputStream(file)) {
            ppt.store(os, String.join("\n", COMMENTS));
        }
    }

    public void load() throws IOException {
        var file = getFile();

        var ppt = new Properties();

        try (var is = Files.newInputStream(file)) {
            ppt.load(is);
        }

        readFrom(ppt);
    }

    private static boolean boolFrom(String s, boolean defaultVal) {
        return s == null ? defaultVal : "true".equals(s);
    }

    private static Integer nullableIntFrom(String s, Integer defaultVal) {
        try {
            return s == null ? defaultVal : (s.equals("none") ? null : Integer.parseInt(s));
        } catch (NumberFormatException ex) {
            Animatica.LOG.error("Value {} must be an integer, or 'none'", s);
        }
        return defaultVal;
    }
}
