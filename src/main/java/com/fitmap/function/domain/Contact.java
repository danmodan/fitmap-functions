package com.fitmap.function.domain;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(value = Include.NON_ABSENT)
public class Contact {

    public static final String CONTACTS_COLLECTION = "contacts";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String WHATSAPP = "whatsapp";
    public static final String INSTAGRAM = "instagram";
    public static final String MAIN_CONTACT = "mainContact";

    @NotBlank
    private String id;

    @Size(max = 500)
    private String name;

    @Email
    @Size(max = 500)
    private String email;

    @Size(max = 50)
    private String phone;

    @JsonProperty(value = "is_whatsapp")
    private Boolean whatsapp;

    @Size(max = 400)
    private String instagram;

    @JsonProperty(value = "is_main_contact")
    private boolean mainContact;

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
        final Contact other = (Contact) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Map<String, Object> createPropertiesMap() {

        var fields = new HashMap<String, Object>();

        fields.put(ID, id);
        fields.put(NAME, name);
        fields.put(EMAIL, email);
        fields.put(PHONE, phone);
        fields.put(WHATSAPP, whatsapp);
        fields.put(INSTAGRAM, instagram);
        fields.put(MAIN_CONTACT, mainContact);

        return fields;
    }

}