package com.fscott.texter.util;

import java.util.List;

import com.fscott.texter.model.Result;

public class TexterUtils {
    
    static public void printResults(final List<Result> results) {
        if (!results.isEmpty()) {
            boolean doAnyHaveHits = false;
            for (Result result : results) {
                if (result.getHits() != 0) {
                    System.out.println(result.toString());
                    doAnyHaveHits = true;
                }
            }
            if (!doAnyHaveHits) {
                System.out.println("No hits recorded in the documents analyzed.");
            }
        } else {
            System.out.println("No hits recorded in the documents analyzed.");
        }
    }
}