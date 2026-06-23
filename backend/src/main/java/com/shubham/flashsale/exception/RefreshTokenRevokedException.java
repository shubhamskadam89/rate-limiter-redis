package com.shubham.flashsale.exception;

public class RefreshTokenRevokedException
        extends RuntimeException {

    public RefreshTokenRevokedException() {
        super("Refresh token revoked");
    }
}
