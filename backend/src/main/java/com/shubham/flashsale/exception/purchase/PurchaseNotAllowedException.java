package com.shubham.flashsale.exception.purchase;

import lombok.AllArgsConstructor;


public class PurchaseNotAllowedException
        extends RuntimeException {

    public PurchaseNotAllowedException() {
        super("Purchase is not allowed.");
    }

}