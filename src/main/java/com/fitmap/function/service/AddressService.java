package com.fitmap.function.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.Student;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    @SneakyThrows
    public static List<Address> findInSuperAddressCollection(List<String> addressesIds) {

        return db
                 .collection(Address.ADDRESSES_COLLECTION)
                 .whereIn(FieldPath.documentId(), addressesIds)
                 .get()
                 .get()
                 .toObjects(Address.class);

    }

    // @SneakyThrows
    // public static List<Address> findById(List<String> addressesIds) {

    //     return db
    //              .collection(Address.ADDRESSES_COLLECTION)
    //              .whereIn(FieldPath.documentId(), addressesIds)
    //              .get()
    //              .get()
    //              .toObjects(Address.class);

    // }

    @SneakyThrows
    public static List<Address> find(String superEntityId, String superCollection) {

        var docRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);

        return docRef.get().get().toObjects(Address.class);
    }

	public static List<Address> create(String superEntityId, String superCollection, List<Address> addresses) {

        var batch = db.batch();

        var collRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);

        var subEntitiesPerDocRef = addresses.stream().map(address -> {
            var ref = collRef.document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(addresses);

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);
        var masterAddressPerDocRef = new ArrayList<Pair<Address, DocumentReference>>();

        subEntitiesPerDocRef.forEach(pair -> {
            batch.create(pair.getRight(), pair.getLeft());

            Address newAddress = null;
            switch (superCollection) {
                case Student.STUDENTS_COLLECTION:
                    newAddress = pair.getLeft().withStudentId(superEntityId);
                    break;
                case Gym.GYMS_COLLECTION:
                    newAddress = pair.getLeft().withGymId(superEntityId);
                    break;
                case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:
                    newAddress = pair.getLeft().withPersonalTrainerId(superEntityId);
                    break;
                default:
                    return;
            }

            var newAddressDocRef = addressesCollRef.document(newAddress.getId());

            masterAddressPerDocRef.add(Pair.of(newAddress, newAddressDocRef));
        });
        masterAddressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        return addresses;

	}

    public static List<Address> edit(String superEntityId, String superCollection, List<Address> addresses) {

        var batch = db.batch();

        var subAddressesCollRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        addresses.forEach(address -> {

            CheckConstraintsRequestBodyService.checkConstraints(address);

            var subDocRef = subAddressesCollRef.document(address.getId());
            var superDocRef = superAddressesCollRef.document(address.getId());

            var fields = address.createPropertiesMap();
            fields.remove(Address.GYM_ID);
            fields.remove(Address.PERSONAL_TRAINER_ID);
            fields.remove(Address.STUDENT_ID);
            fields.remove(Address.EVENTS_IDS);

            batch.update(subDocRef, fields);
            batch.update(superDocRef, fields);

        });

        commit(batch);

        return addresses;

    }

    public static void delete(String superEntityId, String superCollection, List<String> addressesIds) {

        var batch = db.batch();

        var subAddressesCollRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        addressesIds.forEach(id -> {

            var subDocRef = subAddressesCollRef.document(id);
            var superDocRef = superAddressesCollRef.document(id);

            batch.delete(subDocRef);
            batch.delete(superDocRef);
        });

        commit(batch);
    }

    private static void commit(WriteBatch batch) {
        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
