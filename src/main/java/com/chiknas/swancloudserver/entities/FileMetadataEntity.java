package com.chiknas.swancloudserver.entities;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "file_metadata")
@Entity
public class FileMetadataEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "path")
    private String path;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Lob
    @Column(name = "thumbnail")
    private byte[] thumbnail;

}
