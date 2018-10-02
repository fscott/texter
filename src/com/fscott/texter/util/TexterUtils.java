package com.fscott.texter.util;

import java.util.List;

import com.fscott.texter.model.Result;

public class TexterUtils {
    
    static public void printResults(List<Result> results) {
        if (!results.isEmpty()) {
            for (Result result : results) {
                System.out.println(result.toString());
            }
        } else {
            System.out.println("No hits recorded in the documents analyzed.");
        }
    }
}