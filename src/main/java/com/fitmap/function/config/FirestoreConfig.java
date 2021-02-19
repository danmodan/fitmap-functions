package com.fitmap.function.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FirestoreConfig {

    public static final Firestore FIRESTORE;

    static {

        FIRESTORE = FirestoreOptions.getDefaultInstance().getService();
    }

}
