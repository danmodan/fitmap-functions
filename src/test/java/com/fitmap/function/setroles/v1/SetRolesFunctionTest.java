package com.fitmap.function.setroles.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmap.function.common.config.ObjectMapperConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.payload.response.ErrorResponse;
import com.fitmap.function.setroles.service.SetRolesService;
import com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto;
import com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto.UserType;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class SetRolesFunctionTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperConfig.OBJECT_MAPPER;

    public static ErrorResponse getErrorResponse(String body) throws Exception {

        return OBJECT_MAPPER.readValue(body, ErrorResponse.class);
    }

    public static String createJson(Object obj) throws Exception {

        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    @Mock
    private SetRolesService setRolesService;
    @Mock
    private HttpRequest request;
    @Mock
    private HttpResponse response;

    private SetRolesFunction setRolesFunction;

    private StringWriter responseOut;
    private BufferedWriter writerOut;

    @BeforeEach
    void setUp() throws IOException {

        responseOut = new StringWriter();
        writerOut = new BufferedWriter(responseOut);

        setRolesFunction = new SetRolesFunction(setRolesService);
    }

    @Test
    @DisplayName(value = "should return emtpy response when success")
    void should_return_emtpy_response_when_success() throws Exception {

        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_JSON_VALUE));
        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(createJson(new SetRolesRequestDto("idToken", UserType.GYM)).getBytes()));

        setRolesFunction.service(request, response);

        writerOut.flush();

        assertThat(responseOut.toString()).isBlank();
    }

    @Nested
    class WhenErrors {

        @BeforeEach
        void setUp() throws IOException {

            when(request.getPath()).thenReturn("/test");

            when(response.getWriter()).thenReturn(writerOut);

        }

        @Test
        @DisplayName(value = "should answer Method Not Allowed if request method is different from POST")
        void should_answer_Method_Not_Allowed_if_request_method_is_different_from_POST() throws Exception {

            when(request.getMethod()).thenReturn("GET");

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(405, "Method Not Allowed", "405 METHOD_NOT_ALLOWED \"Request method 'GET' not supported\". Allowed methods are [POST]", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

        @Test
        @DisplayName(value = "should answer Unsupported Media Type if request content type is different from application/json")
        void should_answer_Unsupported_Media_Type_if_request_content_type_is_different_from_application_json() throws Exception {

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_PDF_VALUE));

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(415, "Unsupported Media Type", "415 UNSUPPORTED_MEDIA_TYPE \"Content type 'application/pdf' not supported\". Allowed Content-Type are [application/json, application/json;charset=UTF-8]", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

        @Test
        @DisplayName(value = "should answer Bad Request if request body is not readable")
        void should_answer_Bad_Request_if_request_body_is_not_readable() throws Exception {

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_JSON_VALUE));
            when(request.getInputStream()).thenReturn(new ByteArrayInputStream("{\"user_type\": \"fake_user_type\"}".getBytes()));

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(400, "Bad Request", "Cannot convert request body in SetRolesRequestDto; nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto$UserType` from String \"fake_user_type\": not one of the values accepted for Enum class: [STUDENT, PERSONAL_TRAINER, GYM]\n at [Source: (ByteArrayInputStream); line: 1, column: 15] (through reference chain: com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto[\"user_type\"])", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

        @Test
        @DisplayName(value = "should answer Bad Request if there are request constraints violations")
        void should_answer_Bad_Request_if_there_are_request_constraints_violations() throws Exception {

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_JSON_VALUE));
            when(request.getInputStream()).thenReturn(new ByteArrayInputStream(createJson(new SetRolesRequestDto()).getBytes()));

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(400, "Bad Request", "There are fields errors.", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

        @Test
        @DisplayName(value = "should answer internal server error if some unexpected exception occur")
        void should_answer_InternalServerError_if_some_unexpected_exception_occur() throws Exception {

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_JSON_VALUE));
            when(request.getInputStream()).thenReturn(new ByteArrayInputStream(createJson(new SetRolesRequestDto("idToken", UserType.GYM)).getBytes()));
            doThrow(new RuntimeException("message")).when(setRolesService).setRoles(any(), any());

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(500, "Internal Server Error", "message", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

        @Test
        @DisplayName(value = "should answer an terminal known error if some expected exception occur")
        void should_answer_an_terminal_known_error_if_some_expected_exception_occur() throws Exception {

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn(Optional.of(MediaType.APPLICATION_JSON_VALUE));
            when(request.getInputStream()).thenReturn(new ByteArrayInputStream(createJson(new SetRolesRequestDto("idToken", UserType.GYM)).getBytes()));
            doThrow(new TerminalException("message", HttpStatus.BAD_REQUEST)).when(setRolesService).setRoles(any(), any());

            setRolesFunction.service(request, response);

            writerOut.flush();

            var actual = getErrorResponse(responseOut.toString());

            assertThat(actual)
                .extracting("status", "statusError", "message", "path")
                .containsExactly(400, "Bad Request", "message", "/test");

            assertThat(actual).extracting("timestamp").isNotNull();
        }

    }

}
