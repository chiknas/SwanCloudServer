package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.repositories.cursorpagination.CursorUtils;
import com.chiknas.swancloudserver.repositories.cursorpagination.cursors.FileMetadataCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author nkukn
 * @since 2/11/2021
 */
class CursorUtilsTest {

    private FileMetadataCursor cursor;

    @BeforeEach
    void setUp() {
        cursor = new FileMetadataCursor();
        cursor.setId(5);
        cursor.setCreatedDate(LocalDate.of(2021, 2, 15));
    }

    @Test
    void toBase64() {
        assertEquals("rO0ABXNyAFRjb20uY2hpa25hcy5zd2FuY2xvdWRzZXJ2ZXIucmVwb3NpdG9yaWVzLmN1cnNvcnBhZ2luYXRpb24uY" +
                        "3Vyc29ycy5GaWxlTWV0YWRhdGFDdXJzb3JanhoASWlMegIAAkwAC2NyZWF0ZWREYXRldAAVTGphdmEvdGltZS9Mb2NhbER" +
                        "hdGU7TAACaWR0ABNMamF2YS9sYW5nL0ludGVnZXI7eHBzcgANamF2YS50aW1lLlNlcpVdhLobIkiyDAAAeHB3BwMAAAflAg" +
                        "94c3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAU=",
                CursorUtils.toBase64(cursor));
    }

    @Test
    void base64ToCursor() {
        FileMetadataCursor deserializedCursor =
                (FileMetadataCursor) CursorUtils.base64ToCursor("rO0ABXNyAFRjb20uY2hpa25hcy5zd2FuY2xvdWRzZXJ2" +
                        "ZXIucmVwb3NpdG9yaWVzLmN1cnNvcnBhZ2luYXRpb24uY3Vyc29ycy5GaWxlTWV0YWRhdGFDdXJzb3JanhoASWlMegIAAkw" +
                        "AC2NyZWF0ZWREYXRldAAVTGphdmEvdGltZS9Mb2NhbERhdGU7TAACaWR0ABNMamF2YS9sYW5nL0ludGVnZXI7eHBzcgANam" +
                        "F2YS50aW1lLlNlcpVdhLobIkiyDAAAeHB3BwMAAAflAg94c3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2Y" +
                        "Wx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAU=");
        assertEquals(cursor.getId(), deserializedCursor.getId());
        assertEquals(cursor.getCreatedDate(), deserializedCursor.getCreatedDate());
    }

    @Test
    void base64NullChecks() {
        assertNull(CursorUtils.base64ToCursor(null));
        assertNull(CursorUtils.toBase64(null));
    }

}