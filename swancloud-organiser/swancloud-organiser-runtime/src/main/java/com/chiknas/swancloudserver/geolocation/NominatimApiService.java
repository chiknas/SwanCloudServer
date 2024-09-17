package com.chiknas.swancloudserver.geolocation;

import com.google.common.util.concurrent.RateLimiter;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.chiknas.swancloudserver.geolocation.GeolocationConfiguration.NOMINATIM_HTTP_CLIENT;

/**
 * Service to consume Nominatic gelocation api. Limited to 1 request per second as per documentation.
 * Page: <a href="https://nominatim.org/">Nominatim</a>
 * Api docs: <a href="https://nominatim.org/release-docs/develop/">Api docs</a>
 */
@Service
public class NominatimApiService {

    private static final RateLimiter rateLimiter = RateLimiter.create(1);

    private final JsonNominatimClient jsonNominatimClient;

    @Autowired
    public NominatimApiService(@Qualifier(NOMINATIM_HTTP_CLIENT) HttpClient httpClient) {
        jsonNominatimClient = new JsonNominatimClient(httpClient, "swancloud@gmail.com");
    }

    public Address getAddress(double longitude, double latitude) {
        try {
            rateLimiter.acquire();
            return jsonNominatimClient.getAddress(longitude, latitude);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
