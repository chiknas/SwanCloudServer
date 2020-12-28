package com.chiknas.swancloudserver.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.awt.image.BufferedImage;
import java.awt.Image;

import javax.imageio.ImageIO;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;

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

		// TODO: create thumbnail from video files
		try {
			BufferedImage thumbnail = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			thumbnail.createGraphics().drawImage(ImageIO.read(file).getScaledInstance(100, 100, Image.SCALE_SMOOTH), 0,
					0, null);
			fileMetadataEntity.setThumbnail(toByteArray(thumbnail));
		} catch (IOException e) {
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
}
