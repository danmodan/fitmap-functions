package com.fitmap.function.personaltrainercontext.v1.payload.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EditRequestDtos {

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class PersonalTrainer {

        @Size(max = 2000)
        private String biography;

        private List<@NotBlank String> sports;

        private List<@NotBlank String> galleryPicturesUrls;

        @JsonProperty(value = "is_busy_schedule")
        private Boolean busySchedule;

        @JsonProperty(value = "has_online_service")
        private Boolean onlineService;

        @JsonProperty(value = "has_home_service")
        private Boolean homeService;

    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Contact {

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

    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Address {

        @NotBlank
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

    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class Event {

        @NotBlank
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

        private String whatsAppContact;

        private String emailContact;

        private String contactId;

        private Boolean showPhoneContact;

        private Boolean showEmailContact;

    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(value = SnakeCaseStrategy.class)
    @JsonInclude(value = Include.NON_ABSENT)
    public static class SubscriptionPlan {

        @NotBlank
        private String id;
    
        @Size(max = 180)
        private String name;
    
        @PositiveOrZero
        private BigDecimal price;
    
        @PositiveOrZero
        private int numberMonth;
    
        @Size(max = 2000)
        private String description;
    
    }

}
