package com.fitmap.function.studentcontext.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.studentcontext.domain.Contact;
import com.google.cloud.firestore.Firestore;

public class ContactService extends SubCollectionsCrudService<Contact> {

    public ContactService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "students";
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
        return contact -> {

            var props = new HashMap<String, Object>();

            props.put("name", contact.getName());
            props.put("email", contact.getEmail());
            props.put("phone", contact.getPhone());
            props.put("whatsapp", contact.getWhatsapp());
            props.put("instagram", contact.getInstagram());
            props.put("mainContact", contact.isMainContact());

            return props;
        };
    }

}
