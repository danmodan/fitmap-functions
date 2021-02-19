package com.fitmap.function.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.google.cloud.functions.HttpRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@ExtendWith(MockitoExtension.class)
class CheckRequestContentTypeServiceTest {

    @Mock
    private HttpRequest request;

    @Test
    @DisplayName(value = "when request has an expected content type, nothing must happen")
    void when_request_has_an_expected_content_type_nothing_must_happen() {

        when(request.getContentType()).thenReturn(Optional.of("application/json"));

        CheckRequestContentTypeService.checkContentType(request, MediaType.APPLICATION_JSON);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request has an APPLICATION_JSON expected content type, nothing must happen")
    void when_request_has_an_APPLICATION_JSON_expected_content_type_nothing_must_happen() {

        when(request.getContentType()).thenReturn(Optional.of("application/json"));

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        assertThat(Boolean.TRUE).isTrue();
    }

    @Test
    @DisplayName(value = "when request has an invalid content type, an UnsupportedMediaTypeStatusException must be thown")
    void when_request_has_an_invalid_content_type_an_UnsupportedMediaTypeStatusException_must_be_thown() {

        when(request.getContentType()).thenReturn(Optional.of("qwerty/qwerty"));

        assertThatThrownBy(() -> CheckRequestContentTypeService.checkContentType(request, MediaType.APPLICATION_JSON))
            .isInstanceOf(UnsupportedMediaTypeStatusException.class)
            .hasMessage("415 UNSUPPORTED_MEDIA_TYPE \"Content type 'qwerty/qwerty' not supported\"");
    }

    @Test
    @DisplayName(value = "when request has not content type, an UnsupportedMediaTypeStatusException must be thown")
    void when_request_has_not_content_type_an_UnsupportedMediaTypeStatusException_must_be_thown() {

        when(request.getContentType()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> CheckRequestContentTypeService.checkContentType(request, MediaType.APPLICATION_JSON))
            .isInstanceOf(UnsupportedMediaTypeStatusException.class)
            .hasMessage("415 UNSUPPORTED_MEDIA_TYPE \"Content type '' not supported\"");
    }

    @Test
    @DisplayName(value = "when request has an unexpected content type, an UnsupportedMediaTypeStatusException must be thown")
    void when_request_has_an_unexpected_content_type_an_UnsupportedMediaTypeStatusException_must_be_thown() {

        when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_PDF_VALUE));

        assertThatThrownBy(() -> CheckRequestContentTypeService.checkContentType(request, MediaType.APPLICATION_JSON))
            .isInstanceOf(UnsupportedMediaTypeStatusException.class)
            .hasMessage("415 UNSUPPORTED_MEDIA_TYPE \"Content type 'application/pdf' not supported\"");
    }

}
