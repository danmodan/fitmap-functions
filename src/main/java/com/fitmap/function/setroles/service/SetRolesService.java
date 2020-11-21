package com.fitmap.function.setroles.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import com.fitmap.function.common.exception.TerminalException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class SetRolesService {

    private static final String ROLES_KEY = "roles";

    private final FirebaseAuth firebaseAuth;

    @SneakyThrows
    public void setRoles(String idToken, String userType) {

        var firebaseToken = verifyIdToken(idToken);

        checkRolesAlreadySetted(firebaseToken);

        var uid = firebaseToken.getUid();

        var rolesValues = List.of("ROLE_" + userType, "ROLE_USER");

        var claims = new HashMap<String, Object>();
        claims.put(ROLES_KEY, rolesValues);

        firebaseAuth.setCustomUserClaims(uid, claims);

    }

    private void checkRolesAlreadySetted(FirebaseToken firebaseToken) {

        var claims = firebaseToken.getClaims();

        if (MapUtils.isEmpty(claims) || 
            Objects.isNull(claims.get(ROLES_KEY)) ||
            CollectionUtils.isEmpty((Collection) claims.get(ROLES_KEY))) {

            return;
        }

        throw new TerminalException("User already has roles.", HttpStatus.CONFLICT);
    }


    private FirebaseToken verifyIdToken(String idToken) {

        try {

            return firebaseAuth.verifyIdToken(idToken);

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException("The IDToken has expired.", HttpStatus.UNAUTHORIZED);
        }
    }

}
