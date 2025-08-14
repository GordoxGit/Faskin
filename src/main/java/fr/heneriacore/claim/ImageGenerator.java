package fr.heneriacore.claim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class ImageGenerator {
    private final SecureRandom random = new SecureRandom();

    public byte[] generate(byte[] token) throws IOException {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < token.length && i < 64; i++) {
            int v = token[i] & 0xFF;
            int rgba = (v << 24) | 0x00FFFFFF; // store byte in alpha channel
            img.setRGB(i, 0, rgba);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }

    public byte[] randomToken(int len) {
        byte[] b = new byte[len];
        random.nextBytes(b);
        return b;
    }

    public String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            sb.append(String.format("%02x", value));
        }
        return sb.toString();
    }
}
