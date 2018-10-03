package com.fscott.texter.util;

import java.util.List;

import com.fscott.texter.model.Result;

public class TexterUtils {
    
    /**
     * Prints the list of results for any document with non-zero hits.
     *
     * @param a list of results of type {@link Result.class Result}.
     */
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