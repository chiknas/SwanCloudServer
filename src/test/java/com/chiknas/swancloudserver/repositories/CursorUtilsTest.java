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
        cursor.setCreatedDate(LocalDate.now());
    }

    @Test
    void toBase64() {
        assertEquals("rO0ABXNyAFRjb20uY2hpa25hcy5zd2FuY2xvdWRzZXJ2ZXIucmVwb3NpdG9yaWVzLmN1cnNvcnBhZ2luYXRpb24u" +
                        "Y3Vyc29ycy5GaWxlTWV0YWRhdGFDdXJzb3JanhoASWlMegIAAkwAC2NyZWF0ZWREYXRldAAVTGphdmEvdGltZS9Mb2Nhb" +
                        "ERhdGU7TAACaWR0ABNMamF2YS9sYW5nL0ludGVnZXI7eHBzcgANamF2YS50aW1lLlNlcpVdhLobIkiyDAAAeHB3BwMAAA" +
                        "flAgt4c3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAU=",
                CursorUtils.toBase64(cursor));
    }

    @Test
    void base64ToCursor() {
        FileMetadataCursor deserializedCursor =
                (FileMetadataCursor) CursorUtils.base64ToCursor("rO0ABXNyAFRjb20uY2hpa25hcy5zd2FuY2xvdWRzZXJ" +
                        "2ZXIucmVwb3NpdG9yaWVzLmN1cnNvcnBhZ2luYXRpb24uY3Vyc29ycy5GaWxlTWV0YWRhdGFDdXJzb3JanhoASWlMegIAA" +
                        "kwAC2NyZWF0ZWREYXRldAAVTGphdmEvdGltZS9Mb2NhbERhdGU7TAACaWR0ABNMamF2YS9sYW5nL0ludGVnZXI7eHBzcgA" +
                        "NamF2YS50aW1lLlNlcpVdhLobIkiyDAAAeHB3BwMAAAflAgt4c3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAA" +
                        "V2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAU=");
        assertEquals(cursor.getId(), deserializedCursor.getId());
        assertEquals(cursor.getCreatedDate(), deserializedCursor.getCreatedDate());
    }

    @Test
    void base64NullChecks() {
        assertNull(CursorUtils.base64ToCursor(null));
        assertNull(CursorUtils.toBase64(null));
    }

}