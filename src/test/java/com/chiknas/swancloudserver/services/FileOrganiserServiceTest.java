package com.chiknas.swancloudserver.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author nkukn
 * @since 1/24/2021
 */
class FileOrganiserServiceTest {

    @Test
    public void localDateFromPath() {
        Optional<LocalDate> localDateFromPath = FileOrganiserService.getLocalDateFromPath("sdfg/sdfgsdfg/2019/December");

        // returns always the first day of the month
        assertTrue(localDateFromPath.isPresent());
        assertEquals(LocalDate.of(2019, Month.DECEMBER, 1), localDateFromPath.get());

        Optional<LocalDate> localDateFromPathEmpty = FileOrganiserService.getLocalDateFromPath("sdfg/sdfgsdfg/dsfg/Decembsfdger");

        assertFalse(localDateFromPathEmpty.isPresent());
    }

}