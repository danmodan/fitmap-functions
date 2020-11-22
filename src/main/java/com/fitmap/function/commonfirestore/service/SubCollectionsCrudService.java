package com.fitmap.function.commonfirestore.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public abstract class SubCollectionsCrudService<T> {

    private final Firestore db;

    protected abstract String getSuperCollection();
    protected abstract String getSubCollection();
    protected abstract Class<T> getSubEntityType();
    protected abstract BiConsumer<T, String> updateId();
    protected abstract Function<T, String> getId();
    protected abstract Function<T, Map<String, Object>> getFieldsToUpdate();

    public List<T> find(String superEntityId) throws InterruptedException, ExecutionException {

        var docRef = db.collection(getSuperCollection()).document(superEntityId).collection(getSubCollection());

        var coll = docRef.get().get();

        return coll.getDocuments().stream().map(doc -> doc.toObject(getSubEntityType())).collect(Collectors.toList());
    }

	public List<T> create(String superEntityId, List<T> subEntities) {

        var batch = db.batch();

        var collRef = db.collection(getSuperCollection()).document(superEntityId).collection(getSubCollection());

        var subEntitiesPerDocRef = subEntities.stream().map(subEntity -> {
            var ref = collRef.document();
            updateId().accept(subEntity, ref.getId());
            return Pair.of(subEntity, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(subEntities);

        subEntitiesPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        return subEntities;

	}

    public List<T> edit(String superEntityId, List<T> subEntities) {

        var batch = db.batch();

        subEntities.forEach(subEntity -> {

            CheckConstraintsRequestBodyService.checkConstraints(subEntity);

            var docRef = db.collection(getSuperCollection()).document(superEntityId).collection(getSubCollection()).document(getId().apply(subEntity));

            Map<String, Object> fields = getFieldsToUpdate().apply(subEntity);

            batch.update(docRef, fields);
        });

        commit(batch);

        return subEntities;

    }

    public void delete(String superEntityId, List<String> subEntitiesIds) {

        var batch = db.batch();

        subEntitiesIds.forEach(id -> {

            var docRef = db.collection(getSuperCollection()).document(superEntityId).collection(getSubCollection()).document(id);

            batch.delete(docRef);
        });

        commit(batch);
    }

    private void commit(WriteBatch batch) {
        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}