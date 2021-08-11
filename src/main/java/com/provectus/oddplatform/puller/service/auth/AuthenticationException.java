package com.provectus.oddplatform.puller.service.auth;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticationException extends RuntimeException {
    private final String message;
}
