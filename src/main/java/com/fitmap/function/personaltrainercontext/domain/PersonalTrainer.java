package com.fitmap.function.personaltrainercontext.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmap.function.personaltrainercontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.personaltrainercontext.v1.payload.request.EditRequestDtos;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;

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
public class PersonalTrainer {

    @NotBlank
    @DocumentId
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
        final PersonalTrainer other = (PersonalTrainer) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public static PersonalTrainer from(CreateRequestDtos.PersonalTrainer dto, String id) {

        return PersonalTrainer
            .builder()
            .id(id)
            .contacts(Contact.from(dto.getContacts(), Contact::from))
            .addresses(Address.from(dto.getAddresses(), Address::from))
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .build();
    }

    public static PersonalTrainer from(EditRequestDtos.PersonalTrainer dto, String id) {

        return PersonalTrainer
            .builder()
            .id(id)
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .build();
    }

}