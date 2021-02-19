package com.fitmap.function.v1.payload.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
public class GymCreateRequest {

    @Size(max = 2000)
    private String biography;

    @Valid
    private List<@NotNull ContactCreateRequest> contacts;

    @Valid
    private List<@NotNull AddressCreateRequest> addresses;

    @Valid
    private List<@NotNull EventCreateRequest> events;

    @Valid
    private List<@NotNull SubscriptionPlanCreateRequest> subscriptionPlans;

    private List<@NotBlank String> sports;

    private List<@NotBlank String> galleryPicturesUrls;

    @NotEmpty
    @Size(max = 200)
    private String profileName;

}