package com.chiknas.swancloudserver.services.helpers;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FilesHelperTest {

    @Test
    public void localDateFromPath() {
        Optional<LocalDate> localDateFromPath = FilesHelper.getLocalDateFromPath(new File("sdfg/sdfgsdfg/2019/December"));

        // returns always the first day of the month
        assertTrue(localDateFromPath.isPresent());
        assertEquals(LocalDate.of(2019, Month.DECEMBER, 1), localDateFromPath.get());

        Optional<LocalDate> localDateFromPathEmpty = FilesHelper.getLocalDateFromPath(new File("sdfg/sdfgsdfg/dsfg/Decembsfdger"));

        assertFalse(localDateFromPathEmpty.isPresent());
    }

}