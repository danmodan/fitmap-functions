package com.fitmap.function.v1.payload.request;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;

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
public class AddressCreateRequest {

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

    @NotBlank
    private String latitude;

    @NotBlank
    private String longitude;

    @JsonIgnore
    @AssertTrue(message = "latitude and longitude properties must be Double parsables.")
    public boolean isValidAddress() {

        try {

            CheckConstraintsRequestBodyService.checkIsDoubleParsables(latitude, longitude);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

}