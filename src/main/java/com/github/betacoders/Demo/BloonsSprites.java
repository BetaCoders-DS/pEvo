package com.github.betacoders.Demo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.github.betacoders.world.Cells;

import processing.core.PConstants;
import processing.core.PImage;

/** Sprites top down locais, carregados uma única vez pelo classpath, inclusive no JAR. */
public final class BloonsSprites {
    public enum Sprite {
        CORE("core-house-atlas"), TURRET("dart-monkey"), RELAY("engineer-monkey"),
        EXTRACTOR("banana-farm"), RED("bloon-red"), BLUE("bloon-blue"),
        GREEN("bloon-green"), YELLOW("bloon-yellow"), PINK("bloon-pink"),
        RAINBOW("bloon-rainbow"), MOAB("moab");

        private final String file;

        Sprite(String file) {
            this.file = file;
        }
    }

    private static final Sprite[] BLOONS = {
        Sprite.RED, Sprite.BLUE, Sprite.GREEN, Sprite.YELLOW, Sprite.PINK, Sprite.RAINBOW
    };

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
                    // Os sprites são pequenos: evita arquivos temporários ao ler PNGs do JAR.
                    ImageInputStream pixels = new MemoryCacheImageInputStream(stream);
                    // ImageIO.read fecha esse ImageInputStream quando encontra um leitor.
                    BufferedImage image = ImageIO.read(pixels);
                    if (image == null) {
                        pixels.close();
                        throw new IllegalStateException("Sprite inválido: " + path);
                    }
                    if (sprite == Sprite.CORE) {
                        image = buildCoreHouse(image);
                    }
                    PImage texture = new PImage(image.getWidth(), image.getHeight(), PConstants.ARGB);
                    image.getRGB(0, 0, image.getWidth(), image.getHeight(), texture.pixels, 0, image.getWidth());
                    // Cor de transparência documentada pelo conjunto de sprites original.
                    for (int i = 0; i < texture.pixels.length; i++) {
                        if (sprite != Sprite.CORE && (texture.pixels[i] & 0x00ffffff) == 0x00585eb5) {
                            texture.pixels[i] = 0;
                        }
                    }
                    texture.updatePixels();
                    images.put(sprite, texture);
                } catch (IOException error) {
                    throw new UncheckedIOException("Não foi possível ler " + path, error);
                }
            }
            return images;
        }

        /** Monta uma sede de telhado vermelho usando os tiles originais do Tiny Town. */
        private static BufferedImage buildCoreHouse(BufferedImage atlas) {
            int[] tiles = {
                52, 53, 54, // Parte superior do telhado.
                64, 65, 66, // Beiral do telhado.
                76, 88, 79, // Paredes e janela.
                76, 89, 79  // Paredes e porta.
            };
            // Margem transparente ao redor da construção para o sprite ter uma borda limpa.
            BufferedImage house = new BufferedImage(50, 66, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < tiles.length; i++) {
                int sourceX = tiles[i] % 12 * 16;
                int sourceY = tiles[i] / 12 * 16;
                int[] pixels = atlas.getRGB(sourceX, sourceY, 16, 16, null, 0, 16);
                house.setRGB(1 + i % 3 * 16, 1 + i / 3 * 16, 16, 16, pixels, 0, 16);
            }
            return house;
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

    /** A aparência acompanha a onda; os atributos continuam definidos pelo WaveConfig. */
    public static Sprite enemy(int wave, int enemyId) {
        if (wave >= 5 && wave % 5 == 0 && enemyId % 3 == 0) {
            return Sprite.MOAB;
        }
        return BLOONS[Math.floorMod(wave - 1 + enemyId - 1, BLOONS.length)];
    }
}
