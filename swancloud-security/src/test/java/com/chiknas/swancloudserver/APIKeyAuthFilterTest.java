package com.chiknas.swancloudserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nkukn
 * @since 2/17/2021
 */
class APIKeyAuthFilterTest {

    @Test
    public void hashSHA256() {
        final String hashedKey = APIKeyAuthFilter.hashSHA256("nikos");
        assertEquals("335c54f3b6061b0b8bb1e3fdff4c771c3680caa68d1299c36fd7e08f7dcf647f", hashedKey);
    }
}