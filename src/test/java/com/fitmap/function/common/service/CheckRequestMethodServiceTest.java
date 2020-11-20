package com.fitmap.function.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.MethodNotAllowedException;

@ExtendWith(MockitoExtension.class)
class CheckRequestMethodServiceTest {

    @Mock
    private HttpRequest request;

    @Test
    @DisplayName(value = "when request method is an expected method, nothing must happen")
    void when_request_method_is_an_expected_method_nothing_must_happen() {

        when(request.getMethod()).thenReturn("GET");

        CheckRequestMethodService.checkMethod(request, HttpMethod.GET);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request method is GET, nothing must happen")
    void when_request_method_is_GET_nothing_must_happen() {

        when(request.getMethod()).thenReturn("GET");

        CheckRequestMethodService.checkGetMethod(request);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request method is POST, nothing must happen")
    void when_request_method_is_POST_nothing_must_happen() {

        when(request.getMethod()).thenReturn("POST");

        CheckRequestMethodService.checkPostMethod(request);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request method is PUT, nothing must happen")
    void when_request_method_is_PUT_nothing_must_happen() {

        when(request.getMethod()).thenReturn("PUT");

        CheckRequestMethodService.checkPutMethod(request);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request method is DELETE, nothing must happen")
    void when_request_method_is_DELETE_nothing_must_happen() {

        when(request.getMethod()).thenReturn("DELETE");

        CheckRequestMethodService.checkDeleteMethod(request);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request method is not an expected method, an MethodNotAllowedException should be thown")
    void when_request_method_is_not_an_expected_method_an_MethodNotAllowedException_should_be_thown() {

        when(request.getMethod()).thenReturn("POST");

        assertThatThrownBy(() -> CheckRequestMethodService.checkMethod(request, HttpMethod.GET))
            .isInstanceOf(MethodNotAllowedException.class)
            .hasMessage("405 METHOD_NOT_ALLOWED \"Request method 'POST' not supported\"");
    }

}
