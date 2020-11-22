package com.fitmap.function.gymcontext.service;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.gymcontext.domain.Address;
import com.google.cloud.firestore.Firestore;

public class AddressService extends SubCollectionsCrudService<Address> {

    public AddressService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "gyms";
    }

    @Override
    protected String getSubCollection() {
        return "addresses";
    }

    @Override
    protected Class<Address> getSubEntityType() {
        return Address.class;
    }

    @Override
    protected BiConsumer<Address, String> updateId() {
        return Address::setId;
    }

    @Override
    protected Function<Address, String> getId() {
        return Address::getId;
    }

    @Override
    protected Function<Address, Map<String, Object>> getFieldsToUpdate() {
        return address -> Map.of(
            "zipCode", address.getZipCode(),
            "publicPlace", address.getPublicPlace(),
            "complement", address.getComplement(),
            "district", address.getDistrict(),
            "city", address.getCity(),
            "federalUnit", address.getFederalUnit()
        );
    }

}
