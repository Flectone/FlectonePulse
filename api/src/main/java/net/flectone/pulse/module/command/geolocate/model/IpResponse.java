package net.flectone.pulse.module.command.geolocate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The answer returned by the geolocation service.
 *
 * @param status whether the lookup succeeded
 * @param country the country
 * @param region the region name
 * @param city the city
 * @param timezone the time zone
 * @param offset the utc offset in seconds
 * @param mobile whether the address belongs to a mobile network
 * @param proxy whether it looks like a proxy or vpn
 * @param hosting whether it belongs to a hosting provider
 * @param query the address that was looked up
 * @author TheFaser
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IpResponse(
        String status,
        String country,

        @JsonProperty("regionName")
        String region,

        String city,
        String timezone,
        Integer offset,
        Boolean mobile,
        Boolean proxy,
        Boolean hosting,
        String query
) {

    /**
     * Whether the lookup returned usable data.
     *
     * @return true if the service reported success
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }

}
