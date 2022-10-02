package com.chiknas.swancloudserver.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author nkukn
 * @since 3/28/2021
 */
@Getter
@Setter
@Table(name = "file_thumbnail")
@Entity
public class ThumbnailEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "file_name", unique = true)
    private String fileName;

    @Lob
    @Column(name = "thumbnail")
    private byte[] thumbnail;
}
