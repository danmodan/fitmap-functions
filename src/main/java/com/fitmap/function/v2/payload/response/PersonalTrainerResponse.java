package com.fitmap.function.v2.payload.response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.SubscriptionPlan;

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
public class PersonalTrainerResponse {

    private String id;
    private Date createdAt;
    private Date updatedAt;
    private String biography;
    private List<String> galleryPicturesUrls;
    private List<String> sports;
    private List<String> fights;
    private List<String> focus;
    private Contact contact;
    private Address address;
    private List<Event> events;
    private List<SubscriptionPlan> subscriptionPlans;
    private String profileName;

    @JsonProperty(value = "is_busy_schedule")
    private Boolean busySchedule;

    @JsonProperty(value = "has_online_service")
    private Boolean onlineService;

    @JsonProperty(value = "has_home_service")
    private Boolean homeService;

}