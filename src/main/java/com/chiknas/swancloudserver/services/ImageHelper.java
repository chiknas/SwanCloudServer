package com.chiknas.swancloudserver.services;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
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
}
