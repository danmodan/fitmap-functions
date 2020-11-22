package com.fitmap.function.studentcontext.v1;

import java.util.List;
import java.util.function.Function;

import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.commonfirestore.config.FirestoreConfig;
import com.fitmap.function.commonfirestore.function.SubCollectionsCrudFunction;
import com.fitmap.function.studentcontext.domain.Contact;
import com.fitmap.function.studentcontext.service.ContactService;
import com.fitmap.function.studentcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.studentcontext.v1.payload.request.EditRequestDtos;

import lombok.extern.java.Log;

@Log
public class CrudContactFunction extends SubCollectionsCrudFunction<Contact, EditRequestDtos.Contact, CreateRequestDtos.Contact> {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    public CrudContactFunction() {
        super(new ContactService(FirestoreConfig.FIRESTORE));

        log.info("awake function");
    }

    @Override
    protected Class<EditRequestDtos.Contact[]> getEditRequestDtoClass() {
        return EditRequestDtos.Contact[].class;
    }

    @Override
    protected Class<CreateRequestDtos.Contact[]> getCreateRequestDtoClass() {
        return CreateRequestDtos.Contact[].class;
    }

    @Override
    protected Function<List<CreateRequestDtos.Contact>, List<Contact>> mapCreationDtosToSubEntities() {
        return dtos -> Contact.from(dtos, Contact::from);
    }

    @Override
    protected Function<List<EditRequestDtos.Contact>, List<Contact>> mapEditDtosToSubEntities() {
        return dtos -> Contact.from(dtos, Contact::from);
    }

}
