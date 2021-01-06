package com.chiknas.swancloudserver.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.imageio.ImageIO;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataConverter implements Converter<File, FileMetadataEntity> {

	@Override
	public FileMetadataEntity convert(File file) {

		FileMetadataEntity fileMetadataEntity = new FileMetadataEntity();
		fileMetadataEntity.setFileName(file.getName());
		fileMetadataEntity.setPath(file.getAbsolutePath());
		// TODO: update with proper image/vid time taken
		fileMetadataEntity.setCreatedDate(LocalDate.now());

		BufferedImage thumbnail = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		try {
			String mimeType = Files.probeContentType(Path.of(file.getAbsolutePath()));
			if(mimeType.contains("video")){
				FFmpegFrameGrabber g = new FFmpegFrameGrabber(file.getAbsolutePath());
				g.start();
				thumbnail = new Java2DFrameConverter().convert(g.grabImage());
				g.stop();
			}else if(mimeType.contains("image")){
				thumbnail.createGraphics().drawImage(ImageIO.read(file).getScaledInstance(320, 240, Image.SCALE_DEFAULT), 0,
						0, null);
			}

			fileMetadataEntity.setThumbnail(toByteArray(thumbnail));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileMetadataEntity;
	}

	public static byte[] toByteArray(BufferedImage bi) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bi, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
	}

	//TODO: https://stackoverflow.com/questions/59280534/image-is-90-degree-left-rotated-after-encoding-to-base64
    //TODO: use image metadata to rotate to the correct orientation for portraits
	public static BufferedImage rotateClockwise90(BufferedImage src) {
		int width = src.getWidth();
		int height = src.getHeight();
	
		BufferedImage dest = new BufferedImage(height, width, src.getType());
	
		Graphics2D graphics2D = dest.createGraphics();
		graphics2D.translate((height - width) / 2, (height - width) / 2);
		graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
		graphics2D.drawRenderedImage(src, null);
	
		return dest;
	}
}
