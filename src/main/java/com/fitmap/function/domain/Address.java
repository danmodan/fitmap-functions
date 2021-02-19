package com.fitmap.function.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.cloud.firestore.annotation.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(value = Include.NON_ABSENT)
public class Address {

    public static final String ADDRESSES_COLLECTION = "addresses";
    public static final String ID = "id";
    public static final String ZIP_CODE = "zipCode";
    public static final String PUBLIC_PLACE = "publicPlace";
    public static final String COMPLEMENT = "complement";
    public static final String DISTRICT = "district";
    public static final String CITY = "city";
    public static final String FEDERAL_UNIT = "federalUnit";
    public static final String MAIN_ADDRESS = "mainAddress";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String GEO_HASH = "geoHash";
    public static final String GYM_ID = "gymId";
    public static final String PERSONAL_TRAINER_ID = "personalTrainerId";
    public static final String STUDENT_ID = "studentId";
    public static final String EVENTS_IDS = "eventsIds";

    @NotBlank
    @DocumentId
    private String id;

    @Size(max = 50)
    private String zipCode;

    @Size(max = 300)
    private String publicPlace;

    @Size(max = 300)
    private String complement;

    @Size(max = 300)
    private String district;

    @Size(max = 300)
    private String city;

    @Size(max = 300)
    private String federalUnit;

    @JsonProperty(value = "is_main_address")
    private boolean mainAddress;

    private String latitude;

    private String longitude;

    private String geoHash;

    @With
    private String gymId;

    @With
    private String personalTrainerId;

    @With
    private String studentId;

    @With
    private List<String> eventsIds;

    public void addEventsIds(List<String> eventsIds) {

        var newEventsIds = Objects.requireNonNullElse(eventsIds, new ArrayList<String>());

        this.eventsIds = Objects.requireNonNullElse(this.eventsIds, new ArrayList<String>());

        this.eventsIds.addAll(newEventsIds);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Address other = (Address) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Map<String, Object> createPropertiesMap() {

        var fields = new HashMap<String, Object>();

        fields.put(ZIP_CODE, zipCode);
        fields.put(PUBLIC_PLACE, publicPlace);
        fields.put(COMPLEMENT, complement);
        fields.put(DISTRICT, district);
        fields.put(CITY, city);
        fields.put(FEDERAL_UNIT, federalUnit);
        fields.put(MAIN_ADDRESS, mainAddress);
        fields.put(LATITUDE, latitude);
        fields.put(LONGITUDE, longitude);
        fields.put(GEO_HASH, geoHash);
        fields.put(GYM_ID, gymId);
        fields.put(PERSONAL_TRAINER_ID, personalTrainerId);
        fields.put(STUDENT_ID, studentId);
        fields.put(EVENTS_IDS, eventsIds);

        return fields;
    }

}
