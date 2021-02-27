package com.fitmap.function.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
@JsonIgnoreProperties(ignoreUnknown = true, value = { "is_main_address" })
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(value = Include.NON_ABSENT)
public class Address {

    public static final String ADDRESSES_COLLECTION = "addresses_v2";
    public static final String ID = "id";
    public static final String ADDRESS_TEXT = "addressText";
    public static final String MAIN_ADDRESS = "mainAddress";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String GEO_HASH = "geoHash";
    public static final String GYM = "gym";
    public static final String PERSONAL_TRAINER = "personalTrainer";
    public static final String STUDENT = "student";
    public static final String EVENTS = "events";

    @NotBlank
    private String id;

    private String addressText;

    @JsonProperty(value = "is_main_address")
    private boolean mainAddress;

    private String latitude;

    private String longitude;

    private String geoHash;

    @With
    private Gym gym;

    @With
    private PersonalTrainer personalTrainer;

    @With
    private Student student;

    @With
    private List<Event> events;

    public void addEvents(List<Event> events) {

        var newEvents = Objects.requireNonNullElse(events, new ArrayList<Event>());

        this.events = Objects.requireNonNullElse(this.events, new ArrayList<Event>());

        this.events.addAll(newEvents);
    }

    public void addEvent(Event event) {

        addEvents(List.of(event));
    }

    public void removeEvent(Event event) {

        if(this.events == null) {
            return;
        }

        this.events.remove(event);
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

        fields.put(ID, id);
        fields.put(ADDRESS_TEXT, addressText);
        fields.put(MAIN_ADDRESS, mainAddress);
        fields.put(LATITUDE, latitude);
        fields.put(LONGITUDE, longitude);
        fields.put(GEO_HASH, geoHash);
        fields.put(GYM, gym);
        fields.put(PERSONAL_TRAINER, personalTrainer);
        fields.put(STUDENT, student);
        fields.put(EVENTS, events);

        return fields;
    }

}
