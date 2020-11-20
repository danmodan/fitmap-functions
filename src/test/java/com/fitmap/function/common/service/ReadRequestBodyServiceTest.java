package com.fitmap.function.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Month;

import com.google.cloud.functions.HttpRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageNotReadableException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ExtendWith(MockitoExtension.class)
class ReadRequestBodyServiceTest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class POJOClassTest {

        private Month prop;
    }

    @Mock
    private HttpRequest request;

    @Test
    @DisplayName(value = "when cannot convert request into the given class, a HttpMessageNotReadableException should be thown")
    void when_cannot_convert_request_into_the_given_class_a_HttpMessageNotReadableException_should_be_thown() throws IOException {

        when(request.getInputStream()).thenReturn(new ByteArrayInputStream("{\"prop\": \"fakemonth\"}".getBytes()));

        assertThatThrownBy(() -> ReadRequestService.getBody(request, POJOClassTest.class))
            .isInstanceOf(HttpMessageNotReadableException.class)
            .hasMessageContaining("Cannot convert request body in POJOClassTest");
    }

    @Test
    @DisplayName(value = "when can convert request into the given class, nothing should happen")
    void when_can_convert_request_into_the_given_class_nothing_should_happen() throws IOException {

        when(request.getInputStream()).thenReturn(new ByteArrayInputStream("{\"prop\": \"JANUARY\"}".getBytes()));

        ReadRequestService.getBody(request, POJOClassTest.class);

        assertThat(Boolean.TRUE).isTrue();
    }

}
