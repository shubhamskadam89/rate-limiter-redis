package com.shubham.flashsale.exception.purchase;

import lombok.AllArgsConstructor;


public class PurchaseLimitExceededException extends RuntimeException{
    public PurchaseLimitExceededException(){
        super("Purchase limit exceeded");
    }
}