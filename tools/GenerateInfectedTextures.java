import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Original pixel art, drawn directly on the standard 64x64 humanoid UV layout. No external assets. */
public class GenerateInfectedTextures {
    private static final int SKIN = 0x899079;
    private static final int SHIRT = 0x566765;
    private static final int PANTS = 0x343A42;

    public static void main(String[] args) throws Exception {
        Path textures = Path.of("src/main/resources/assets/zombie-infection/textures");
        BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        // Head: top, bottom and four faces. The outer hat layer stays transparent.
        cloth(skin, 8, 0, 16, 8, SKIN);
        cloth(skin, 0, 8, 32, 8, SKIN);
        rect(skin, 0, 8, 32, 2, 0x343D35);
        cloth(skin, 8, 0, 8, 8, 0x343D35);
        rect(skin, 9, 11, 2, 1, 0x323833);
        rect(skin, 13, 11, 2, 1, 0x323833);
        rect(skin, 10, 11, 1, 1, 0xC6C59B);
        rect(skin, 13, 11, 1, 1, 0xC6C59B);
        rect(skin, 11, 12, 2, 2, 0x6B745F);
        rect(skin, 10, 14, 4, 1, 0x484942);
        rect(skin, 14, 13, 1, 2, 0x784D43);
        // Faded work shirt, chest pocket, broken buttons and torn hem.
        cloth(skin, 16, 16, 24, 16, SHIRT);
        rect(skin, 22, 20, 4, 2, SKIN);
        rect(skin, 23, 22, 2, 1, 0x394A48);
        rect(skin, 24, 23, 1, 7, 0x404F4D);
        rect(skin, 20, 24, 3, 3, 0x435453);
        rect(skin, 20, 24, 3, 1, 0x77807A);
        rect(skin, 25, 28, 2, 4, 0x735148);
        rect(skin, 26, 30, 1, 2, SKIN);
        // Matching arms, with short sleeves and bare damaged hands.
        arm(skin, 40, 16);
        arm(skin, 32, 48);
        // Both legs, worn trousers and dark shoes.
        leg(skin, 0, 16);
        leg(skin, 16, 48);
        rect(skin, 5, 24, 2, 2, 0x66705F);
        rect(skin, 22, 57, 2, 2, 0x5B6259);
        Files.createDirectories(textures.resolve("entity"));
        ImageIO.write(skin, "PNG", textures.resolve("entity/infected.png").toFile());

        BufferedImage egg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[] widths = {4, 6, 8, 10, 10, 12, 12, 12, 12, 10, 8, 6};
        for (int row = 0; row < widths.length; row++) {
            int left = (16 - widths[row]) / 2;
            for (int x = left; x < left + widths[row]; x++) {
                boolean edge = x == left || x == left + widths[row] - 1 || row == 0 || row == 11;
                int color = edge ? 0x35443B : (x < 8 ? 0x9CA58B : 0x7C8B71);
                rect(egg, x, row + 2, 1, 1, color);
            }
        }
        rect(egg, 6, 4, 2, 2, SHIRT);
        rect(egg, 9, 7, 2, 3, SHIRT);
        rect(egg, 4, 9, 2, 2, SHIRT);
        rect(egg, 7, 11, 2, 2, 0x584D43);
        rect(egg, 5, 6, 1, 2, 0xC0C7AC);
        Files.createDirectories(textures.resolve("item"));
        ImageIO.write(egg, "PNG", textures.resolve("item/infected_spawn_egg.png").toFile());
    }

    private static void arm(BufferedImage image, int x, int y) {
        cloth(image, x, y, 16, 16, SHIRT);
        cloth(image, x, y + 9, 16, 7, SKIN);
        rect(image, x + 5, y + 12, 2, 3, 0x725349);
    }

    private static void leg(BufferedImage image, int x, int y) {
        cloth(image, x, y, 16, 16, PANTS);
        cloth(image, x, y + 13, 16, 3, 0x252B2C);
    }

    private static void cloth(BufferedImage image, int x, int y, int w, int h, int base) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                int noise = Math.floorMod(px * 17 + py * 29 + px * py * 3, 13) - 6;
                int r = Math.clamp((base >> 16 & 255) + noise, 0, 255);
                int g = Math.clamp((base >> 8 & 255) + noise, 0, 255);
                int b = Math.clamp((base & 255) + noise, 0, 255);
                image.setRGB(px, py, 0xFF000000 | r << 16 | g << 8 | b);
            }
        }
    }

    private static void rect(BufferedImage image, int x, int y, int w, int h, int color) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                image.setRGB(px, py, 0xFF000000 | color);
            }
        }
    }
}
