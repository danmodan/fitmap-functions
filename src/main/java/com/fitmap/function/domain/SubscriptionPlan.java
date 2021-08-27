package com.fitmap.function.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
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
public class SubscriptionPlan {

    public static final String SUBSCRIPTION_PLANS_COLLECTION = "subscriptionPlans_v2";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String NUMBER_MONTH = "numberMonth";
    public static final String DESCRIPTION = "description";

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
        final SubscriptionPlan other = (SubscriptionPlan) obj;
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
        fields.put(PRICE, price);
        fields.put(NUMBER_MONTH, numberMonth);
        fields.put(DESCRIPTION, description);

        return fields;
    }

}