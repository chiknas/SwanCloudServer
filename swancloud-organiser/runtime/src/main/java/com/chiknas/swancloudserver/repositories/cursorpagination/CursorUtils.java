package com.chiknas.swancloudserver.repositories.cursorpagination;

import com.chiknas.swancloudserver.cursorpagination.cursors.FileMetadataCursor;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * @author nkukn
 * @since 2/11/2021
 */
public class CursorUtils {

    private CursorUtils() {
        // utils class no point to initialize
    }

    public static String toBase64(FileMetadataCursor cursor) {
        if (cursor == null) {
            return null;
        }
        byte[] serialize = SerializationUtils.serialize(cursor);
        return Base64.getUrlEncoder().encodeToString(serialize);
    }

    public static Object base64ToCursor(String base64Cursor) {
        if (base64Cursor == null) {
            return null;
        }
        byte[] decodedBase64 = Base64.getUrlDecoder().decode(base64Cursor);
        return SerializationUtils.deserialize(decodedBase64);
    }
}
