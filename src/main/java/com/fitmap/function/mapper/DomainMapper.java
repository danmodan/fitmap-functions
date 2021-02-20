package com.fitmap.function.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.firebase.geofire.core.GeoHash;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.Student;
import com.fitmap.function.domain.SubscriptionPlan;
import com.fitmap.function.v1.payload.request.AddressCreateRequest;
import com.fitmap.function.v1.payload.request.AddressEditRequest;
import com.fitmap.function.v1.payload.request.ContactCreateRequest;
import com.fitmap.function.v1.payload.request.ContactEditRequest;
import com.fitmap.function.v1.payload.request.EventCreateRequest;
import com.fitmap.function.v1.payload.request.EventEditRequest;
import com.fitmap.function.v1.payload.request.GymCreateRequest;
import com.fitmap.function.v1.payload.request.GymEditRequest;
import com.fitmap.function.v1.payload.request.PersonalTrainerCreateRequest;
import com.fitmap.function.v1.payload.request.PersonalTrainerEditRequest;
import com.fitmap.function.v1.payload.request.StudentCreateRequest;
import com.fitmap.function.v1.payload.request.StudentEditRequest;
import com.fitmap.function.v1.payload.request.SubscriptionPlanCreateRequest;
import com.fitmap.function.v1.payload.request.SubscriptionPlanEditRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainMapper {

    public static <REQUEST_DTO, DOMAIN_MODEL> List<DOMAIN_MODEL> from(Collection<REQUEST_DTO> dtos, Function<REQUEST_DTO, DOMAIN_MODEL> mapper) {

        if(CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Address from(AddressCreateRequest dto) {

        if(dto == null) {
            return null;
        }

        return Address
            .builder()
            .zipCode(dto.getZipCode())
            .publicPlace(dto.getPublicPlace())
            .complement(dto.getComplement())
            .district(dto.getDistrict())
            .city(dto.getCity())
            .federalUnit(dto.getFederalUnit())
            .mainAddress(dto.isMainAddress())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .geoHash(getGeoHash(dto.getLatitude(), dto.getLongitude()))
            .build();
    }

    public static Address from(AddressEditRequest dto) {

        if(dto == null) {
            return null;
        }

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
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .geoHash(getGeoHash(dto.getLatitude(), dto.getLongitude()))
            .build();
    }

    private static String getGeoHash(String latitude, String longitude) {

        if(StringUtils.isAnyBlank(latitude, longitude)) {

            return null;
        }

        return new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude)).getGeoHashString();
    }

    public static Contact from(ContactCreateRequest dto) {

        if(dto == null) {
            return null;
        }

        return Contact
            .builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .whatsapp(dto.getWhatsapp())
            .instagram(dto.getInstagram())
            .mainContact(dto.isMainContact())
            .build();
    }

    public static Contact from(ContactEditRequest dto) {

        if(dto == null) {
            return null;
        }

        return Contact
            .builder()
            .id(dto.getId())
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .whatsapp(dto.getWhatsapp())
            .instagram(dto.getInstagram())
            .mainContact(dto.isMainContact())
            .build();
    }

    public static Event from(EventCreateRequest dto) {

        if(dto == null) {
            return null;
        }

        return Event
            .builder()
            .name(dto.getName())
            .pictureUrl(dto.getPictureUrl())
            .description(dto.getDescription())
            .eventType(dto.getEventType())
            .beginAt(dto.getBeginAt())
            .endAt(dto.getEndAt())
            .eventCoach(dto.getEventCoach())
            .address(StringUtils.isBlank(dto.getAddressId()) ? null : Address.builder().id(dto.getAddressId()).build())
            .currentEventValue(dto.getCurrentEventValue())
            .originalEventValue(dto.getOriginalEventValue())
            .contact(StringUtils.isBlank(dto.getContactId()) ? null : Contact.builder().id(dto.getContactId()).build())
            .showPhoneContact(dto.getShowPhoneContact())
            .showEmailContact(dto.getShowEmailContact())
            .build();
    }

    public static Event from(EventEditRequest dto) {

        if(dto == null) {
            return null;
        }

        return Event
            .builder()
            .id(dto.getId())
            .name(dto.getName())
            .pictureUrl(dto.getPictureUrl())
            .description(dto.getDescription())
            .eventType(dto.getEventType())
            .beginAt(dto.getBeginAt())
            .endAt(dto.getEndAt())
            .eventCoach(dto.getEventCoach())
            .address(StringUtils.isBlank(dto.getAddressId()) ? null : Address.builder().id(dto.getAddressId()).build())
            .currentEventValue(dto.getCurrentEventValue())
            .originalEventValue(dto.getOriginalEventValue())
            .contact(StringUtils.isBlank(dto.getContactId()) ? null : Contact.builder().id(dto.getContactId()).build())
            .showPhoneContact(dto.getShowPhoneContact())
            .showEmailContact(dto.getShowEmailContact())
            .build();
    }

    public static Student from(StudentCreateRequest dto, String studentId) {

        if(dto == null) {
            return null;
        }

        return Student
            .builder()
            .id(studentId)
            .contacts(DomainMapper.from(dto.getContacts(), DomainMapper::from))
            .addresses(DomainMapper.from(dto.getAddresses(), DomainMapper::from))
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .profileName(dto.getProfileName())
            .build();
    }

    public static Student from(StudentEditRequest dto, String studentId) {

        if(dto == null) {
            return null;
        }

        return Student
            .builder()
            .id(studentId)
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .profileName(dto.getProfileName())
            .build();
    }

    public static SubscriptionPlan from(SubscriptionPlanCreateRequest dto) {

        if(dto == null) {
            return null;
        }

        return SubscriptionPlan
            .builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .numberMonth(dto.getNumberMonth())
            .description(dto.getDescription())
            .build();
    }

    public static SubscriptionPlan from(SubscriptionPlanEditRequest dto) {

        if(dto == null) {
            return null;
        }

        return SubscriptionPlan
            .builder()
            .id(dto.getId())
            .name(dto.getName())
            .price(dto.getPrice())
            .numberMonth(dto.getNumberMonth())
            .description(dto.getDescription())
            .build();
    }

    public static Gym from(GymCreateRequest dto, String gymId) {

        if(dto == null) {
            return null;
        }

        return Gym
            .builder()
            .id(gymId)
            .contacts(DomainMapper.from(dto.getContacts(), DomainMapper::from))
            .addresses(DomainMapper.from(dto.getAddresses(), DomainMapper::from))
            .events(DomainMapper.from(dto.getEvents(), DomainMapper::from))
            .subscriptionPlans(DomainMapper.from(dto.getSubscriptionPlans(), DomainMapper::from))
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .profileName(dto.getProfileName())
            .build();
    }

    public static Gym from(GymEditRequest dto, String gymId) {

        if(dto == null) {
            return null;
        }

        return Gym
            .builder()
            .id(gymId)
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .profileName(dto.getProfileName())
            .build();
    }

    public static PersonalTrainer from(PersonalTrainerCreateRequest dto, String id) {

        if(dto == null) {
            return null;
        }

        return PersonalTrainer
            .builder()
            .id(id)
            .contacts(DomainMapper.from(dto.getContacts(), DomainMapper::from))
            .addresses(DomainMapper.from(dto.getAddresses(), DomainMapper::from))
            .events(DomainMapper.from(dto.getEvents(), DomainMapper::from))
            .subscriptionPlans(DomainMapper.from(dto.getSubscriptionPlans(), DomainMapper::from))
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .busySchedule(dto.getBusySchedule())
            .onlineService(dto.getOnlineService())
            .homeService(dto.getHomeService())
            .profileName(dto.getProfileName())
            .build();
    }

    public static PersonalTrainer from(PersonalTrainerEditRequest dto, String id) {

        if(dto == null) {
            return null;
        }

        return PersonalTrainer
            .builder()
            .id(id)
            .biography(dto.getBiography())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .busySchedule(dto.getBusySchedule())
            .onlineService(dto.getOnlineService())
            .homeService(dto.getHomeService())
            .profileName(dto.getProfileName())
            .build();
    }

}
