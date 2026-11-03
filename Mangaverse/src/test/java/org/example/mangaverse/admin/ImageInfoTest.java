package org.example.mangaverse.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ImageInfoTest {

    @Test
    void readsPngAndJpeg() throws IOException {
        assertEquals(new ImageInfo("png", 30, 40), ImageInfo.read(encode("png", 30, 40)).orElseThrow());
        assertEquals(new ImageInfo("jpg", 50, 20), ImageInfo.read(encode("jpg", 50, 20)).orElseThrow());
    }

    @Test
    void readsLossyWebpHeader() throws IOException {
        byte[] webp = new ClassPathResource("static/media/demo/manga/01.webp").getContentAsByteArray();
        assertEquals(new ImageInfo("webp", 1000, 1500), ImageInfo.read(webp).orElseThrow());
    }

    @Test
    void rejectsNonImagesEvenWithImageNames() {
        assertTrue(ImageInfo.read("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8)).isEmpty());
        assertTrue(ImageInfo.read(new byte[0]).isEmpty());
        byte[] fakeWebp = "RIFF\0\0\0\0WEBPVP8X................".getBytes(StandardCharsets.US_ASCII);
        assertTrue(ImageInfo.read(fakeWebp).isPresent() || ImageInfo.read(fakeWebp).isEmpty()); // never throws
    }

    @Test
    void naturalSortPutsTwoBeforeTen() {
        assertTrue(ChapterUploadService.naturalKey("2.jpg").compareTo(ChapterUploadService.naturalKey("10.jpg")) < 0);
        assertTrue(ChapterUploadService.naturalKey("page 9.png").compareTo(ChapterUploadService.naturalKey("page 11.png")) < 0);
    }

    private static byte[] encode(String format, int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return out.toByteArray();
    }
}
