package com.chiknas.swancloudserver.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

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
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Lob
    @Column(name = "thumbnail")
    private byte[] thumbnail;
}
