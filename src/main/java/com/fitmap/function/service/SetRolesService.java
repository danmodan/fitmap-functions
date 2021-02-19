package com.fitmap.function.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import com.fitmap.function.config.FirebaseAuthConfig;
import com.fitmap.function.exception.TerminalException;
import com.google.firebase.auth.FirebaseToken;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SetRolesService {

    private static final String ROLES_KEY = "roles";

    @SneakyThrows
    public static void setRoles(String idToken, String userType) {

        var firebaseToken = verifyIdToken(idToken);

        checkRolesAlreadySetted(firebaseToken);

        var uid = firebaseToken.getUid();

        var rolesValues = List.of("ROLE_" + userType, "ROLE_USER");

        var claims = new HashMap<String, Object>();
        claims.put(ROLES_KEY, rolesValues);

        FirebaseAuthConfig.FIREBASE_AUTH.setCustomUserClaims(uid, claims);

    }

    private static void checkRolesAlreadySetted(FirebaseToken firebaseToken) {

        var claims = firebaseToken.getClaims();

        if (MapUtils.isEmpty(claims) || 
            Objects.isNull(claims.get(ROLES_KEY)) ||
            CollectionUtils.isEmpty((Collection) claims.get(ROLES_KEY))) {

            return;
        }

        throw new TerminalException("User already has roles.", HttpStatus.CONFLICT);
    }


    private static FirebaseToken verifyIdToken(String idToken) {

        try {

            return FirebaseAuthConfig.FIREBASE_AUTH.verifyIdToken(idToken);

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException("The IDToken has expired.", HttpStatus.UNAUTHORIZED);
        }
    }

}
