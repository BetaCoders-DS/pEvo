package com.github.betacoders.Demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.enemies.ColossalEnemy;
import com.github.betacoders.world.enemies.FlyingEnemy;
import com.github.betacoders.world.enemies.GroundEnemy;

import processing.core.PConstants;
import processing.core.PImage;

public final class BloonsSprites {
    public enum Sprite {
        CORE("nucleo"), TURRET("torretanew"), RELAY("celulaneutra"),
        EXTRACTOR("extrator"), GROUND("ground"), AEREO("aereo"),
        MAHORAGA("mahoraga");

        private final String file;

        Sprite(String file) {
            this.file = file;
        }
    }

    private static final class Assets {
        private static final Map<Sprite, PImage> IMAGES = load();

        private static Map<Sprite, PImage> load() {
            Map<Sprite, PImage> images = new EnumMap<>(Sprite.class);
            for (Sprite sprite : Sprite.values()) {
                String path = "/bloons/" + sprite.file + ".png";
                try (InputStream stream = BloonsSprites.class.getResourceAsStream(path)) {
                    if (stream == null) {
                        throw new IllegalStateException("Sprite ausente no classpath: " + path);
                    }
                    ImageInputStream pixels = new MemoryCacheImageInputStream(stream);
                    java.awt.image.BufferedImage image = ImageIO.read(pixels);
                    if (image == null) {
                        pixels.close();
                        throw new IllegalStateException("Sprite inválido: " + path);
                    }
                    PImage texture = new PImage(image.getWidth(), image.getHeight(), PConstants.ARGB);
                    image.getRGB(0, 0, image.getWidth(), image.getHeight(), texture.pixels, 0, image.getWidth());
                    texture.updatePixels();
                    images.put(sprite, texture);
                } catch (IOException error) {
                    throw new UncheckedIOException("Não foi possível ler " + path, error);
                }
            }
            return images;
        }
    }

    private BloonsSprites() {
    }

    public static PImage image(Sprite sprite) {
        return Assets.IMAGES.get(sprite);
    }

    public static Sprite cell(Cells cell) {
        if (cell instanceof Cells.Core) return Sprite.CORE;
        if (cell instanceof Cells.Turret) return Sprite.TURRET;
        if (cell instanceof Cells.Extractor) return Sprite.EXTRACTOR;
        return Sprite.RELAY;
    }

    public static Sprite enemy(Enemy enemy) {
        if (enemy instanceof ColossalEnemy) return Sprite.MAHORAGA;
        if (enemy instanceof FlyingEnemy) return Sprite.AEREO;
        if (enemy instanceof GroundEnemy) return Sprite.GROUND;
        return Sprite.GROUND;
    }
}
