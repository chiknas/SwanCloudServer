package com.chiknas.swancloudserver.cursorpagination.cursors;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author nkukn
 * @since 2/11/2021
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataCursor implements Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    private Integer id;

    private LocalDate createdDate;
}
