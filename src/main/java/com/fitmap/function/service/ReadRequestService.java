package com.fitmap.function.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.fitmap.function.config.ObjectMapperConfig;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.functions.HttpRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadRequestService {

    public static <T> T getBody(HttpRequest request, Class<T> clazz) {

        try {

            return ObjectMapperConfig.OBJECT_MAPPER.readValue(request.getInputStream(), clazz);

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
                try {
                    return request.getInputStream();
                } catch (Exception e) {
                    return null;
                }
            }

        };
    }

    public static String getUserId(HttpRequest request) {

        return request.getFirstHeader("User_id").orElseThrow(() -> new TerminalException("User_id header is mandatory.", HttpStatus.BAD_REQUEST));
    }

    public static String getUserIdToken(HttpRequest request) {

        return request.getFirstHeader("User_id_token").orElseThrow(() -> new TerminalException("User_id_token header is mandatory.", HttpStatus.BAD_REQUEST));
    }

    public static Locale getAcceptLanguage(HttpRequest request) {

        try {

            var acceptLang = request.getFirstHeader("Accept-Language").orElse("en");

            if(acceptLang.matches("[a-z]{2}-[A-Z]{2}")) {

                var split = acceptLang.split("-");

                return new Locale(split[0], split[1]);
            }

            return new Locale(acceptLang);

        } catch (Exception e) {

            return new Locale("en");
        }
    }
}
