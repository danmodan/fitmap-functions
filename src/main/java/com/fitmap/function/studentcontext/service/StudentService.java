package com.fitmap.function.studentcontext.service;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.studentcontext.domain.Address;
import com.fitmap.function.studentcontext.domain.Contact;
import com.fitmap.function.studentcontext.domain.Student;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class StudentService {

    private static final String STUDENTS_COLLECTION = "students";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";

    private final Firestore db;

    public Student create(Student student) {

        var now = new Date();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);

        var batch = db.batch();

        var studentDocRef = db.collection(STUDENTS_COLLECTION).document(student.getId());

        var addressPerDocRef = student.getAddresses().stream().map(address -> {
            var ref = studentDocRef.collection(ADDRESS_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = student.getContacts().stream().map(contact -> {
            var ref = studentDocRef.collection(CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(student);

        batch.create(studentDocRef, student);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return student;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public Student find(String studentId) throws InterruptedException, ExecutionException {

        var docRef = db.collection(STUDENTS_COLLECTION).document(studentId);

        var studentDoc = docRef.get().get();

        if (studentDoc.exists()) {

            var student = studentDoc.toObject(Student.class);

            var contactsColl = docRef.collection(CONTACTS_COLLECTION).get().get();

            var contacts = contactsColl.getDocuments().stream().map(doc -> doc.toObject(Contact.class)).collect(Collectors.toList());

            student.addContacts(contacts);

            var addressColl = docRef.collection(ADDRESS_COLLECTION).get().get();

            var address = addressColl.getDocuments().stream().map(doc -> doc.toObject(Address.class)).collect(Collectors.toList());

            student.addAddresses(address);

            return student;
        }

        throw new TerminalException(String.format("Student, %s, not found.", studentId), HttpStatus.NOT_FOUND);
    }

}
