package org.example.mangaverse.admin;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.imageio.ImageIO;

/**
 * Detects the real format of an uploaded image from its bytes (never from the file name or the client's
 * content type) and reads its dimensions. JPEG/PNG via ImageIO; WebP from its RIFF header.
 */
public record ImageInfo(String extension, int width, int height) {

    public static Optional<ImageInfo> read(byte[] bytes) {
        try {
            if (isWebp(bytes)) {
                return webp(bytes);
            }
            String extension = isPng(bytes) ? "png" : isJpeg(bytes) ? "jpg" : null;
            if (extension == null) {
                return Optional.empty();
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            return image == null ? Optional.empty() : Optional.of(new ImageInfo(extension, image.getWidth(), image.getHeight()));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private static boolean isPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean isJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean isWebp(byte[] b) {
        return b.length > 30 && ascii(b, 0, 4).equals("RIFF") && ascii(b, 8, 4).equals("WEBP");
    }

    /** Lossy (VP8), lossless (VP8L) and extended (VP8X) WebP headers. */
    private static Optional<ImageInfo> webp(byte[] b) {
        ByteBuffer le = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        switch (ascii(b, 12, 4)) {
            case "VP8X" -> {
                int width = 1 + ((b[24] & 0xFF) | (b[25] & 0xFF) << 8 | (b[26] & 0xFF) << 16);
                int height = 1 + ((b[27] & 0xFF) | (b[28] & 0xFF) << 8 | (b[29] & 0xFF) << 16);
                return Optional.of(new ImageInfo("webp", width, height));
            }
            case "VP8 " -> {
                if ((b[23] & 0xFF) != 0x9D || (b[24] & 0xFF) != 0x01 || (b[25] & 0xFF) != 0x2A) {
                    return Optional.empty();
                }
                return Optional.of(new ImageInfo("webp", le.getShort(26) & 0x3FFF, le.getShort(28) & 0x3FFF));
            }
            case "VP8L" -> {
                if ((b[20] & 0xFF) != 0x2F) {
                    return Optional.empty();
                }
                int bits = le.getInt(21);
                return Optional.of(new ImageInfo("webp", (bits & 0x3FFF) + 1, ((bits >> 14) & 0x3FFF) + 1));
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    private static String ascii(byte[] b, int offset, int length) {
        return new String(b, offset, length, StandardCharsets.US_ASCII);
    }
}
