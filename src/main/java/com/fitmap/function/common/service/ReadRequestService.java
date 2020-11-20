package com.fitmap.function.common.service;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmap.function.common.config.ObjectMapperConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.google.cloud.functions.HttpRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadRequestService {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperConfig.OBJECT_MAPPER;

    public static <T> T getBody(HttpRequest request, Class<T> clazz) {

        try {

            return OBJECT_MAPPER.readValue(request.getInputStream(), clazz);

        } catch (Exception e) {

            throw new HttpMessageNotReadableException("Cannot convert request body in " + clazz.getSimpleName(), e, getHttpInputMessage(request));
        }
    }

    private static HttpInputMessage getHttpInputMessage(HttpRequest request) {

        return new HttpInputMessage() {

            @Override
            public HttpHeaders getHeaders() {
                var headers = new HttpHeaders();
                headers.putAll(request.getHeaders());
                return headers;
            }

            @Override
            public InputStream getBody() throws IOException {
                return request.getInputStream();
            }

        };
    }

    public static String getUserId(HttpRequest request) {

        return request.getFirstHeader("USER_ID").orElseThrow(() -> new TerminalException("USER_ID header is mandatory.", HttpStatus.BAD_REQUEST));
    }

}
