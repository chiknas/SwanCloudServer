package com.chiknas.swancloudserver.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "file_metadata", indexes = @Index(columnList = "created_date DESC, id DESC"))
@Entity
public class FileMetadataEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "path")
    private String path;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

}
