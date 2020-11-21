package com.fitmap.function.gymcontext.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.gymcontext.v1.payload.request.EditRequestDtos;
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
public class Contact {

    @NotBlank
    @DocumentId
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

    public static Contact from(CreateRequestDtos.Contact dto) {

        return Contact
            .builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .whatsapp(dto.getWhatsapp())
            .build();
    }

    public static Contact from(EditRequestDtos.Contact dto) {

        return Contact
            .builder()
            .id(dto.getId())
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .whatsapp(dto.getWhatsapp())
            .build();
    }

    public static <T> List<Contact> from(Collection<T> dtos, Function<T, Contact> mapper) {

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toCollection(ArrayList::new));
    }

}