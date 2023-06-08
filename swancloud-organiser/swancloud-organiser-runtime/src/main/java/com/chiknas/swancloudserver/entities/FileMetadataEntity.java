package com.chiknas.swancloudserver.entities;

import com.chiknas.swancloudserver.geolocation.GeolocationEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Table(name = "file_metadata", indexes = @Index(columnList = "created_date DESC, id DESC"))
@Entity
public class FileMetadataEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "path")
    private String path;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "id")
    private ThumbnailEntity thumbnail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "geolocation_id", referencedColumnName = "id")
    private GeolocationEntity geolocation;


    public File getFile() {
        return Path.of(this.getPath()).toFile();
    }

}
