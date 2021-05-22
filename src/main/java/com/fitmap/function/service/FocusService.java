package com.fitmap.function.service;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Focus;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class FocusService {


    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static List<Focus> create(List<Focus> focus) {

        var batch = db.batch();

        var collRef = db.collection(Focus.FOCUS_COLLECTION);

        var focusPerDocRef = focus.stream().map(f -> {
            var ref = collRef.document();
            f.setId(ref.getId());
            return Pair.of(f, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(focus);

        focusPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return focus;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @SneakyThrows
    public static List<Focus> find(List<String> ids) {

        return db.collection(Focus.FOCUS_COLLECTION).whereIn(FieldPath.documentId(), ids).get().get().toObjects(Focus.class);

    }

    @SneakyThrows
    public static List<Focus> findAll() {

        return db.collection(Focus.FOCUS_COLLECTION).get().get().toObjects(Focus.class);

    }

    public static List<Focus> update(List<Focus> focus) {

        var batch = db.batch();

        var collRef = db.collection(Focus.FOCUS_COLLECTION);

        focus.forEach(sport ->  batch.update(collRef.document(sport.getId()), Focus.NAME, sport.getName()));

        CheckConstraintsRequestBodyService.checkConstraints(focus);

        try {

            batch.commit().get();

            return focus;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    public static void remove(List<String> ids) {

        var batch = db.batch();

        var collRef = db.collection(Focus.FOCUS_COLLECTION);

        ids.forEach(id -> batch.delete(collRef.document(id)));

        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }
}
