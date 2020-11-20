package com.fitmap.function.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Month;
import java.util.Arrays;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmap.function.common.config.ObjectMapperConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.payload.response.ErrorResponse;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ExtendWith(MockitoExtension.class)
class ResponseServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperConfig.OBJECT_MAPPER;

    public static ErrorResponse getErrorResponse(String body) throws Exception {

        return OBJECT_MAPPER.readValue(body, ErrorResponse.class);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class POJOClassTest {

        @NotNull
        private Month prop;
    }

    @Mock
    private HttpRequest request;
    @Mock
    private HttpResponse response;

    private StringWriter responseOut;
    private BufferedWriter writerOut;

    @BeforeEach
    void setUp() throws IOException {

        when(request.getPath()).thenReturn("/test");

        responseOut = new StringWriter();
        writerOut = new BufferedWriter(responseOut);
        when(response.getWriter()).thenReturn(writerOut);
    }

    @Test
    @DisplayName(value = "should answer TerminalException for the given Exception")
    void should_answer_TerminalException_for_the_given_Exception() throws Exception {

        ResponseService.answerTerminalException(request, response, new TerminalException("message", HttpStatus.BAD_REQUEST));

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(400, "Bad Request", "message", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

    @Test
    @DisplayName(value = "should answer method not allowed for the given MethodNotAllowedException")
    void should_answer_method_not_allowed_for_the_given_MethodNotAllowedException() throws Exception {

        ResponseService.answerMethodNotAllowed(request, response, new MethodNotAllowedException(HttpMethod.GET, Arrays.asList(HttpMethod.POST)));

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(405, "Method Not Allowed", "405 METHOD_NOT_ALLOWED \"Request method 'GET' not supported\". Allowed methods are [POST]", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

    @Test
    @DisplayName(value = "should answer unsupported media type for the given UnsupportedMediaTypeStatusException")
    void should_answer_unsupported_media_type_for_the_given_UnsupportedMediaTypeStatusException() throws Exception {

        ResponseService.answerUnsupportedMediaType(request, response, new UnsupportedMediaTypeStatusException(MediaType.APPLICATION_PDF, Arrays.asList(MediaType.APPLICATION_JSON)));

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(415, "Unsupported Media Type", "415 UNSUPPORTED_MEDIA_TYPE \"Content type 'application/pdf' not supported\". Allowed Content-Type are [application/json]", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

    @Test
    @DisplayName(value = "should answer bad request for the given HttpMessageNotReadableException")
    void should_answer_bad_request_for_the_given_HttpMessageNotReadableException() throws Exception {

        HttpMessageNotReadableException ex = null;

        try {
            when(request.getInputStream()).thenReturn(new ByteArrayInputStream("{\"prop\": \"fakemonth\"}".getBytes()));
            ReadRequestService.getBody(request, POJOClassTest.class);
        } catch (HttpMessageNotReadableException e) {
            ex = e;
        }

        ResponseService.answerBadRequest(request, response, ex);

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(400, "Bad Request", "Cannot convert request body in POJOClassTest; nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `java.time.Month` from String \"fakemonth\": not one of the values accepted for Enum class: [OCTOBER, SEPTEMBER, JUNE, MARCH, MAY, APRIL, JULY, JANUARY, FEBRUARY, DECEMBER, AUGUST, NOVEMBER]\n at [Source: (ByteArrayInputStream); line: 1, column: 10] (through reference chain: com.fitmap.function.common.service.ResponseServiceTest$POJOClassTest[\"prop\"])", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

    @Test
    @DisplayName(value = "should answer bad request for the given ConstraintViolationException")
    void should_answer_bad_request_for_the_given_ConstraintViolationException() throws Exception {

        ConstraintViolationException ex = null;

        try {
            var request = new POJOClassTest();
            CheckConstraintsRequestBodyService.checkConstraints(request);
        } catch (ConstraintViolationException e) {
            ex = e;
        }

        ResponseService.answerBadRequest(request, response, ex);

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(400, "Bad Request", "There are fields errors.", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

    @Test
    @DisplayName(value = "should answer internal server error for the given Exception")
    void should_answer_internal_server_error_for_the_given_Exception() throws Exception {

        ResponseService.answerInternalServerError(request, response, new Exception("message"));

        writerOut.flush();

        var actual = getErrorResponse(responseOut.toString());

        assertThat(actual)
            .extracting("status", "statusError", "message", "path")
            .containsExactly(500, "Internal Server Error", "message", "/test");

        assertThat(actual).extracting("timestamp").isNotNull();
    }

}
