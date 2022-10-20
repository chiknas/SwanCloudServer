package com.chiknas.swancloudserver.services;

import com.chiknas.swancloudserver.cursorpagination.CursorPage;
import com.chiknas.swancloudserver.cursorpagination.cursors.FileMetadataCursor;
import com.chiknas.swancloudserver.dto.FileMetadataDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    void storeFile(MultipartFile file);

    CursorPage<FileMetadataDTO> findAllFilesMetadata(FileMetadataCursor fileMetadataCursor, int limit, boolean uncategorized);


}
