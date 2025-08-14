package fr.heneriacore.claim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageVerifier {
    public boolean verify(byte[] imageBytes, byte[] expected) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            for (int i = 0; i < expected.length && i < 64; i++) {
                int rgba = img.getRGB(i, 0);
                int v = (rgba >>> 24) & 0xFF;
                if (v != (expected[i] & 0xFF)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
