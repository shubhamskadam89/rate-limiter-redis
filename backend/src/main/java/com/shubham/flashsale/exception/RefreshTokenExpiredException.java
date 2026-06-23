package com.shubham.flashsale.exception;

public class RefreshTokenExpiredException
        extends RuntimeException {

    public RefreshTokenExpiredException() {
        super("Refresh token expired");
    }
}


