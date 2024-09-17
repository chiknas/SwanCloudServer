package com.chiknas.swancloudserver.geolocation;

import com.chiknas.swancloudserver.entities.FileMetadataEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Table(name = "geolocation")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeolocationEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "geolocation")
    private FileMetadataEntity fileMetadataEntity;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;
}
