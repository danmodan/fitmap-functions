package com.fitmap.function.studentcontext.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmap.function.studentcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.studentcontext.v1.payload.request.EditRequestDtos;
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
public class Address {

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

    public static Address from(CreateRequestDtos.Address dto) {

        return Address
            .builder()
            .zipCode(dto.getZipCode())
            .publicPlace(dto.getPublicPlace())
            .complement(dto.getComplement())
            .district(dto.getDistrict())
            .city(dto.getCity())
            .federalUnit(dto.getFederalUnit())
            .mainAddress(dto.isMainAddress())
            .build();
    }

    public static Address from(EditRequestDtos.Address dto) {

        return Address
            .builder()
            .id(dto.getId())
            .zipCode(dto.getZipCode())
            .publicPlace(dto.getPublicPlace())
            .complement(dto.getComplement())
            .district(dto.getDistrict())
            .city(dto.getCity())
            .federalUnit(dto.getFederalUnit())
            .mainAddress(dto.isMainAddress())
            .build();
    }

    public static <T> List<Address> from(Collection<T> dtos, Function<T, Address> mapper) {

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toCollection(ArrayList::new));
    }

}