package com.fitmap.function.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.cloud.firestore.annotation.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(value = Include.NON_ABSENT)
public class Event {

    public static final String EVENTS_COLLECTION = "events";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PICTURE_URL = "pictureUrl";
    public static final String DESCRIPTION = "description";
    public static final String EVENT_TYPE = "eventType";
    public static final String BEGIN_AT = "beginAt";
    public static final String END_AT = "endAt";
    public static final String EVENT_COACH = "eventCoach";
    public static final String ADDRESS_ID = "addressId";
    public static final String CURRENT_EVENT_VALUE = "currentEventValue";
    public static final String ORIGINAL_EVENT_VALUE = "originalEventValue";
    public static final String CONTACT_ID = "contactId";
    public static final String SHOW_PHONE_CONTACT = "showPhoneContact";
    public static final String SHOW_EMAIL_CONTACT = "showEmailContact";

    @NotBlank
    @DocumentId
    private String id;

    @Size(max = 180)
    private String name;

    private String pictureUrl;

    @Size(max = 2000)
    private String description;

    @Size(max = 1000)
    private String eventType;

    private Date beginAt;

    private Date endAt;

    @Size(max = 1000)
    private String eventCoach;

    private String addressId;

    @PositiveOrZero
    private BigDecimal currentEventValue;

    @PositiveOrZero
    private BigDecimal originalEventValue;

    private String contactId;

    private Boolean showPhoneContact;

    private Boolean showEmailContact;

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
        final Event other = (Event) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Map<String, Object> createPropertiesMap() {

        var fields = new HashMap<String, Object>();

        fields.put(NAME, name);
        fields.put(PICTURE_URL, pictureUrl);
        fields.put(DESCRIPTION, description);
        fields.put(EVENT_TYPE, eventType);
        fields.put(BEGIN_AT, beginAt);
        fields.put(END_AT, endAt);
        fields.put(EVENT_COACH, eventCoach);
        fields.put(ADDRESS_ID, addressId);
        fields.put(CURRENT_EVENT_VALUE, currentEventValue);
        fields.put(ORIGINAL_EVENT_VALUE, originalEventValue);
        fields.put(CONTACT_ID, contactId);
        fields.put(SHOW_PHONE_CONTACT, showPhoneContact);
        fields.put(SHOW_EMAIL_CONTACT, showEmailContact);

        return fields;
    }

}