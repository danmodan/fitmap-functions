package com.fitmap.function.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final List<HttpMethod> CRUD_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

}
