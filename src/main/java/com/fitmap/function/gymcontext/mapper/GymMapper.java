package com.fitmap.function.gymcontext.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fitmap.function.gymcontext.domain.Address;
import com.fitmap.function.gymcontext.domain.Contact;
import com.fitmap.function.gymcontext.domain.Gym;
import com.fitmap.function.gymcontext.v1.payload.request.CreateGymRequestDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GymMapper {

    public static Gym map(final CreateGymRequestDto dto, final String gymId) {

        return Gym
            .builder()
            .id(gymId)
            .contacts(mapContacts(dto.getContacts()))
            .addresses(mapAddresses(dto.getAddresses()))
            .biography(dto.getBiography())
            .instagram(dto.getInstagram())
            .sports(dto.getSports())
            .galleryPicturesUrls(dto.getGalleryPicturesUrls())
            .build();
    }

    public static Address map(final CreateGymRequestDto.Address dto) {

        return Address
            .builder()
            .zipCode(dto.getZipCode())
            .publicPlace(dto.getPublicPlace())
            .complement(dto.getComplement())
            .district(dto.getDistrict())
            .city(dto.getCity())
            .federalUnit(dto.getFederalUnit())
            .build();
    }

    public static List<Address> mapAddresses(final List<CreateGymRequestDto.Address> dtos) {

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(GymMapper::map)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Contact map(final CreateGymRequestDto.Contact dto) {

        return Contact
            .builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .whatsapp(dto.getWhatsapp())
            .build();
    }

    public static List<Contact> mapContacts(final List<CreateGymRequestDto.Contact> dtos) {

        return dtos
            .stream()
            .filter(Objects::nonNull)
            .map(GymMapper::map)
            .collect(Collectors.toCollection(ArrayList::new));
    }

}