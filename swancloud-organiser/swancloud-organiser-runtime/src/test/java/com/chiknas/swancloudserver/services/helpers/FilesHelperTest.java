package com.chiknas.swancloudserver.services.helpers;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FilesHelperTest {

    @Test
    public void localDateFromPath() {
        Optional<LocalDateTime> localDateFromPath = FilesHelper.getLocalDateFromPath(new File("sdfg/sdfgsdfg/2019/December"));

        // returns always the first day of the month
        assertTrue(localDateFromPath.isPresent());
        // set time at the beginning of the day since we can not know time from folder structure
        assertEquals(LocalDate.of(2019, Month.DECEMBER, 1).atStartOfDay(), localDateFromPath.get());

        Optional<LocalDateTime> localDateFromPathEmpty = FilesHelper.getLocalDateFromPath(new File("sdfg/sdfgsdfg/dsfg/Decembsfdger"));

        assertFalse(localDateFromPathEmpty.isPresent());
    }

}