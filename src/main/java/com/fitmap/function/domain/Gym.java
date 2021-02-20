package com.fitmap.function.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.cloud.firestore.annotation.Exclude;

import org.apache.commons.collections4.CollectionUtils;

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
public class Gym {

    public static final String GYMS_COLLECTION = "gyms_v2";
    public static final String ID = "id";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String BIOGRAPHY = "biography";
    public static final String GALLERY_PICTURES_URLS = "galleryPicturesUrls";
    public static final String SPORTS = "sports";
    public static final String CONTACTS = "contacts";
    public static final String ADDRESSES = "addresses";
    public static final String EVENTS = "events";
    public static final String SUBSCRIPTION_PLANS = "subscriptionPlans";
    public static final String PROFILE_NAME = "profileName";

    @NotBlank
    private String id;

    @NotNull
    @PastOrPresent
    private Date createdAt;

    @NotNull
    @PastOrPresent
    private Date updatedAt;

    @Size(max = 2000)
    private String biography;

    @Builder.Default
    private List<@NotBlank String> galleryPicturesUrls = new ArrayList<>();

    @Builder.Default
    private List<@NotBlank String> sports = new ArrayList<>();

    @Getter(onMethod = @__({ @Exclude }))
    @Builder.Default
    private List<@NotNull Contact> contacts = new ArrayList<>();

    @Getter(onMethod = @__({ @Exclude }))
    @Builder.Default
    private List<@NotNull Address> addresses = new ArrayList<>();

    @Getter(onMethod = @__({ @Exclude }))
    @Builder.Default
    private List<@NotNull Event> events = new ArrayList<>();

    @Getter(onMethod = @__({ @Exclude }))
    @Builder.Default
    private List<@NotNull SubscriptionPlan> subscriptionPlans = new ArrayList<>();

    @NotEmpty
    @Size(max = 200)
    private String profileName;

    public void addSports(List<String> sports) {

        var newSports = Objects.requireNonNullElse(sports, new ArrayList<String>());

        this.sports = Objects.requireNonNullElse(this.sports, new ArrayList<String>());

        this.sports.addAll(newSports);
    }

    public void addGalleryPicturesUrls(List<String> galleryPicturesUrls) {

        var newGalleryPicturesUrls = Objects.requireNonNullElse(galleryPicturesUrls, new ArrayList<String>());

        this.galleryPicturesUrls = Objects.requireNonNullElse(this.galleryPicturesUrls, new ArrayList<String>());

        this.galleryPicturesUrls.addAll(newGalleryPicturesUrls);
    }

    public void addContacts(List<Contact> contacts) {

        var newContacts = Objects.requireNonNullElse(contacts, new ArrayList<Contact>());

        this.contacts = Objects.requireNonNullElse(this.contacts, new ArrayList<Contact>());

        this.contacts.addAll(newContacts);
    }

    public void addAddresses(List<Address> addresses) {

        var newAddresses = Objects.requireNonNullElse(addresses, new ArrayList<Address>());

        this.addresses = Objects.requireNonNullElse(this.addresses, new ArrayList<Address>());

        this.addresses.addAll(newAddresses);
    }

    public void addEvents(List<Event> events) {

        var newEvents = Objects.requireNonNullElse(events, new ArrayList<Event>());

        this.events = Objects.requireNonNullElse(this.events, new ArrayList<Event>());

        this.events.addAll(newEvents);
    }

    public void addSubscriptionPlan(List<SubscriptionPlan> subscriptionPlans) {

        var newSubscriptionPlans = Objects.requireNonNullElse(subscriptionPlans, new ArrayList<SubscriptionPlan>());

        this.subscriptionPlans = Objects.requireNonNullElse(this.subscriptionPlans, new ArrayList<SubscriptionPlan>());

        this.subscriptionPlans.addAll(newSubscriptionPlans);
    }

    public Optional<Contact> findContactById(String contactId) {

        if(CollectionUtils.isEmpty(contacts)) {

            return Optional.empty();
        }

        for(var contact : contacts) {

            if(contact.getId().equals(contactId)) {

                return Optional.of(contact);
            }
        }

        return Optional.empty();
    }

    public Optional<Address> findAddressById(String addressId) {

        if(CollectionUtils.isEmpty(addresses)) {

            return Optional.empty();
        }

        for(var address : addresses) {

            if(address.getId().equals(addressId)) {

                return Optional.of(address);
            }
        }

        return Optional.empty();
    }

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
        final Gym other = (Gym) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}