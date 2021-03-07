package com.chiknas.swancloudserver.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Return this exception in the API if we are not ready to handle a request for some reason.
 *
 * @author nkukn
 * @since 3/1/2021
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class UnavailableException extends RuntimeException {
    public UnavailableException(String message) {
        super(message);
    }
}
