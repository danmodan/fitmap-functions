package com.fitmap.function.service;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Fight;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FightService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static List<Fight> create(List<Fight> fights) {

        var batch = db.batch();

        var collRef = db.collection(Fight.FIGHTS_COLLECTION);

        var fightsPerDocRef = fights.stream().map(fight -> {
            var ref = collRef.document();
            fight.setId(ref.getId());
            return Pair.of(fight, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(fights);

        fightsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return fights;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @SneakyThrows
    public static List<Fight> find(List<String> ids) {

        return db.collection(Fight.FIGHTS_COLLECTION).whereIn(FieldPath.documentId(), ids).get().get().toObjects(Fight.class);

    }

    @SneakyThrows
    public static List<Fight> findAll() {

        return db.collection(Fight.FIGHTS_COLLECTION).get().get().toObjects(Fight.class);

    }

    public static List<Fight> update(List<Fight> fights) {

        var batch = db.batch();

        var collRef = db.collection(Fight.FIGHTS_COLLECTION);

        fights.forEach(fight ->  batch.update(collRef.document(fight.getId()), Fight.NAME, fight.getName()));

        CheckConstraintsRequestBodyService.checkConstraints(fights);

        try {

            batch.commit().get();

            return fights;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    public static void remove(List<String> ids) {

        var batch = db.batch();

        var collRef = db.collection(Fight.FIGHTS_COLLECTION);

        ids.forEach(id -> batch.delete(collRef.document(id)));

        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

}
