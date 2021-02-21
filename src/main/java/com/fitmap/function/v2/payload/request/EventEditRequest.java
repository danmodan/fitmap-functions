package com.fitmap.function.v2.payload.request;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
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
public class EventEditRequest {

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