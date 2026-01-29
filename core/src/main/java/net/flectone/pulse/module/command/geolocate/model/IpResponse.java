package net.flectone.pulse.module.command.geolocate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public boolean isSuccess() {
        return "success".equals(status);
    }

}
