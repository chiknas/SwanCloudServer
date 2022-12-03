package com.chiknas.swancloudserver.security;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Details of the current user logged in the system. This is implemented by the security module so
 * DO NOT IMPLEMENT. Only to be injected around the system when needed.
 */
public interface CurrentUser {

    /**
     * Returns the date of the most recent file the user has uploaded.
     * ex. if the user has uploaded all the files with a created date of 2020/12/12 then this will be
     * 2020/12/12 until there is a file uploaded that is created after that.
     * This is used as info for the user to gauge the last file he uploaded.
     */
    Optional<LocalDate> getLastUploadedFileDate();

    /**
     * Updates the last uploaded file date with the specified value.
     * See {@link CurrentUser#getLastUploadedFileDate} for more details.
     * This is a hard set, meaning will not allow dates in the past (before the current set date)
     * to overwrite it.
     */
    void setLastUploadedFileDate(LocalDate localDate);


    /**
     * Returns a Base64 string that represents a QR code image that holds information that allows
     * the user to be synced up to other apps.
     */
    Optional<String> getSyncUserQR();
}
