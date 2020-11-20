package com.fitmap.function.setroles.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto;
import com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto.UserType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SetRolesServiceTest {

    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private FirebaseAuthException mockEx;
    @Mock
    private FirebaseToken firebaseToken;

    private SetRolesService setRolesService;

    @BeforeEach
    void setUp() {

        setRolesService = new SetRolesService(firebaseAuth);
    }

    @Test
    @DisplayName(value = "an TokenExpiredException should be thown if the IDToken is invalid")
    void an_TokenExpiredException_should_be_thown_if_the_IDToken_is_invalid() throws Exception {

        when(firebaseAuth.verifyIdToken(any())).thenThrow(mockEx);

        var request = new SetRolesRequestDto("idToken", UserType.GYM);

        assertThatThrownBy(() -> setRolesService.setRoles(request.getIdToken(), request.getUserType().name()))
            .isInstanceOf(TerminalException.class)
            .hasMessageContaining("The IDToken has expired.")
            .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName(value = "if roles has already setted, should throws TerminalException")
    void if_roles_has_already_setted_should_throws_TerminalException() throws Exception {

        when(firebaseAuth.verifyIdToken(any())).thenReturn(firebaseToken);
        when(firebaseToken.getClaims()).thenReturn(Map.of("roles", List.of("ROLE_USER", "ROLE_GYM")));

        var request = new SetRolesRequestDto("idToken", UserType.GYM);

        assertThatThrownBy(() -> setRolesService.setRoles(request.getIdToken(), request.getUserType().name()))
            .isInstanceOf(TerminalException.class)
            .hasMessageContaining("User already has roles.")
            .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName(value = "should set successfuly the roles, when IDToken is valid and there are not roles")
    void should_set_successfuly_the_roles_when_IDToken_is_valid_and_there_are_not_roles() throws Exception {

        when(firebaseAuth.verifyIdToken(any())).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("uid");

        var request = new SetRolesRequestDto("idToken", UserType.GYM);

        setRolesService.setRoles(request.getIdToken(), request.getUserType().name());

        verify(firebaseAuth).setCustomUserClaims(any(), any());
    }


}
