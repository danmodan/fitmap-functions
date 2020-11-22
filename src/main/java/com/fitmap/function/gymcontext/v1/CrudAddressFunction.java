package com.fitmap.function.gymcontext.v1;

import java.util.List;
import java.util.function.Function;

import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.commonfirestore.config.FirestoreConfig;
import com.fitmap.function.commonfirestore.function.SubCollectionsCrudFunction;
import com.fitmap.function.gymcontext.domain.Address;
import com.fitmap.function.gymcontext.service.AddressService;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.gymcontext.v1.payload.request.EditRequestDtos;

import lombok.extern.java.Log;

@Log
public class CrudAddressFunction extends SubCollectionsCrudFunction<Address, EditRequestDtos.Address, CreateRequestDtos.Address> {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    public CrudAddressFunction() {
        super(new AddressService(FirestoreConfig.FIRESTORE));

        log.info("awake function");
    }

    @Override
    protected Class<EditRequestDtos.Address[]> getEditRequestDtoClass() {
        return EditRequestDtos.Address[].class;
    }

    @Override
    protected Class<CreateRequestDtos.Address[]> getCreateRequestDtoClass() {
        return CreateRequestDtos.Address[].class;
    }

    @Override
    protected Function<List<CreateRequestDtos.Address>, List<Address>> mapCreationDtosToSubEntities() {
        return dtos -> Address.from(dtos, Address::from);
    }

    @Override
    protected Function<List<EditRequestDtos.Address>, List<Address>> mapEditDtosToSubEntities() {
        return dtos -> Address.from(dtos, Address::from);
    }

}
