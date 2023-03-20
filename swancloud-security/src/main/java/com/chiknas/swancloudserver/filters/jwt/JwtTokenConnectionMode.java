package com.chiknas.swancloudserver.filters.jwt;

/**
 * Enum describing how the Access Token is sent to the server.
 * In other works where the token can be found in the HTTP request.
 */
public enum JwtTokenConnectionMode {

    /**
     * Default mode that most of the app should run on
     * as this is the most secure. The token is expected to be
     * found in an Authorization header of an HTTP request.
     */
    HEADER,

    /**
     * The access token will be present in URL as param
     * with key 'token'.
     * This should not be part of the browser but only
     * used with secured js connections as part of the app.
     * Preferably used for streaming purposes.
     */
    URL

}
