package com.fitmap.function.gymcontext.service;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.gymcontext.domain.Contact;
import com.google.cloud.firestore.Firestore;

public class ContactService extends SubCollectionsCrudService<Contact> {

    public ContactService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "gyms";
    }

    @Override
    protected String getSubCollection() {
        return "contacts";
    }

    @Override
    protected Class<Contact> getSubEntityType() {
        return Contact.class;
    }

    @Override
    protected BiConsumer<Contact, String> updateId() {
        return Contact::setId;
    }

    @Override
    protected Function<Contact, String> getId() {
        return Contact::getId;
    }

    @Override
    protected Function<Contact, Map<String, Object>> getFieldsToUpdate() {
        return contact -> Map.of(
            "name", contact.getName(),
            "email", contact.getEmail(),
            "phone", contact.getPhone(),
            "whatsapp", contact.getWhatsapp()
        );
    }

}