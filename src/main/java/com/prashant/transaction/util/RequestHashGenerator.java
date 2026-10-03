package com.prashant.transaction.util;

import org.springframework.stereotype.Component;

@Component
public class RequestHashGenerator {
    public static String generate(String sourceId , String destinationId , double amount) {
        String res = sourceId + ":" + destinationId + ":" + amount;
        return Integer.toString(res.hashCode());
    }
}
