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
import com.fitmap.function.v2.payload.request.AddressRequest;
import com.fitmap.function.v2.payload.request.ContactRequest;
import com.fitmap.function.v2.payload.request.EventCreateRequest;
import com.fitmap.function.v2.payload.request.EventEditRequest;
import com.fitmap.function.v2.payload.request.GymCreateRequest;
import com.fitmap.function.v2.payload.request.GymEditRequest;
import com.fitmap.function.v2.payload.request.PersonalTrainerCreateRequest;
import com.fitmap.function.v2.payload.request.PersonalTrainerEditRequest;
import com.fitmap.function.v2.payload.request.StudentCreateRequest;
import com.fitmap.function.v2.payload.request.StudentEditRequest;
import com.fitmap.function.v2.payload.request.SubscriptionPlanCreateRequest;
import com.fitmap.function.v2.payload.request.SubscriptionPlanEditRequest;
import com.fitmap.function.v2.payload.response.GymResponse;
import com.fitmap.function.v2.payload.response.LocationResponse;
import com.fitmap.function.v2.payload.response.PersonalTrainerResponse;
import com.fitmap.function.v2.payload.response.StudentResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DtoMapper {

    public static <T, Q> List<Q> from(Collection<T> dtos, Function<T, Q> mapper) {

        if(CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(mapper)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Address from(AddressRequest dto) {

        if(dto == null) {
            return null;
        }

        return Address
            .builder()
            .addressText(dto.getAddressText())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .geoHash(getGeoHash(dto.getLatitude(), dto.getLongitude()))
            .build();
    }

    public static Contact from(ContactRequest dto) {

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
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
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
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
            .profileName(dto.getProfileName())
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .build();
    }

    public static StudentResponse from(Student model) {

        if(model == null) {
            return null;
        }

        return StudentResponse
            .builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .galleryPicturesUrls(Objects.requireNonNullElse(model.getGalleryPicturesUrls(), Collections.emptyList()))
            .contact(getFirstIfAny(model.getContacts()))
            .address(getFirstIfAny(model.getAddresses()))
            .profileName(model.getProfileName())
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
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .events(from(dto.getEvents(), DtoMapper::from))
            .subscriptionPlans(from(dto.getSubscriptionPlans(), DtoMapper::from))
            .biography(dto.getBiography())
            .sports(Objects.requireNonNullElse(dto.getSports(), Collections.emptyList()))
            .focus(Objects.requireNonNullElse(dto.getFocus(), Collections.emptyList()))
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
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
            .sports(Objects.requireNonNullElse(dto.getSports(), Collections.emptyList()))
            .focus(Objects.requireNonNullElse(dto.getFocus(), Collections.emptyList()))
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
            .profileName(dto.getProfileName())
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .build();
    }

    public static GymResponse from(Gym model) {

        if(model == null) {
            return null;
        }

        return GymResponse
            .builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .biography(model.getBiography())
            .galleryPicturesUrls(model.getGalleryPicturesUrls())
            .sports(model.getSports())
            .focus(model.getFocus())
            .contact(getFirstIfAny(model.getContacts()))
            .address(getFirstIfAny(model.getAddresses()))
            .events(model.getEvents())
            .subscriptionPlans(model.getSubscriptionPlans())
            .profileName(model.getProfileName())
            .build();
    }

    public static PersonalTrainer from(PersonalTrainerCreateRequest dto, String id) {

        if(dto == null) {
            return null;
        }

        return PersonalTrainer
            .builder()
            .id(id)
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .events(from(dto.getEvents(), DtoMapper::from))
            .subscriptionPlans(from(dto.getSubscriptionPlans(), DtoMapper::from))
            .biography(dto.getBiography())
            .sports(Objects.requireNonNullElse(dto.getSports(), Collections.emptyList()))
            .fights(Objects.requireNonNullElse(dto.getFights(), Collections.emptyList()))
            .focus(Objects.requireNonNullElse(dto.getFocus(), Collections.emptyList()))
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
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
            .sports(Objects.requireNonNullElse(dto.getSports(), Collections.emptyList()))
            .focus(Objects.requireNonNullElse(dto.getFocus(), Collections.emptyList()))
            .fights(Objects.requireNonNullElse(dto.getFights(), Collections.emptyList()))
            .galleryPicturesUrls(Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), Collections.emptyList()))
            .busySchedule(dto.getBusySchedule())
            .onlineService(dto.getOnlineService())
            .homeService(dto.getHomeService())
            .profileName(dto.getProfileName())
            .contacts(from(createOneElementList(dto.getContact()), DtoMapper::from))
            .addresses(from(createOneElementList(dto.getAddress()), DtoMapper::from))
            .build();
    }

    public static PersonalTrainerResponse from(PersonalTrainer model) {

        if(model == null) {
            return null;
        }

        return PersonalTrainerResponse
            .builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .biography(model.getBiography())
            .galleryPicturesUrls(model.getGalleryPicturesUrls())
            .sports(model.getSports())
            .focus(model.getFocus())
            .fights(model.getFights())
            .contact(getFirstIfAny(model.getContacts()))
            .address(getFirstIfAny(model.getAddresses()))
            .events(model.getEvents())
            .subscriptionPlans(model.getSubscriptionPlans())
            .profileName(model.getProfileName())
            .busySchedule(model.getBusySchedule())
            .onlineService(model.getOnlineService())
            .homeService(model.getHomeService())
            .build();
    }

    public static LocationResponse from(Address model) {

        if(model == null) {
            return null;
        }

        return LocationResponse
            .builder()
            .id(model.getId())
            .addressText(model.getAddressText())
            .mainAddress(model.isMainAddress())
            .latitude(model.getLatitude())
            .longitude(model.getLongitude())
            .geoHash(model.getGeoHash())
            .gym(from(model.getGym()))
            .personalTrainer(from(model.getPersonalTrainer()))
            .student(from(model.getStudent()))
            .events(model.getEvents())
            .build();
    }

    private static <T> T getFirstIfAny(List<T> col) {

        return CollectionUtils.isEmpty(col) ? null : col.get(0);
    }

    private static <T> List<T> createOneElementList(T t) {

        if(t == null) {
            return Collections.emptyList();
        }

        return List.of(t);
    }

    private static String getGeoHash(String latitude, String longitude) {

        if(StringUtils.isAnyBlank(latitude, longitude)) {

            return null;
        }

        return new GeoHash(Double.parseDouble(latitude), Double.parseDouble(longitude)).getGeoHashString();
    }

}
