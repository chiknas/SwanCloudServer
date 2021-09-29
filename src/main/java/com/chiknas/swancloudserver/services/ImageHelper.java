package com.chiknas.swancloudserver.services;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author nkukn
 * @since 5/2/2021
 */
@Slf4j
public class ImageHelper {

    private ImageHelper() {
        // static methods to help process images. no need to init.
    }

    public static byte[] toByteArray(BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static BufferedImage rotate90(BufferedImage src, boolean clockWise) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dest = new BufferedImage(height, width, src.getType());

        int x = clockWise ? height / 2 : - height / 2;
        int y = clockWise ? width / 2 : - width / 2;

        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, x, y);
        graphics2D.drawRenderedImage(src, null);

        return dest;
    }
}
