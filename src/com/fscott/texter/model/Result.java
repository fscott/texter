package com.fscott.texter.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Result implements Comparable<Result> {
    
    private int hits;
    private String documentName;
    
    private Result(final String name, AtomicInteger hits) {
        this.documentName = name;
        this.hits = hits.get();
    }
    
    /**
     * Creates a result, which consists of a name and a number of hits.
     *
     * @param a name (e.g. a document name).
     * @param the number of hits.
     * @return a {@link Result.class Result}
     */
    static public Result create(final String name, AtomicInteger hits) {
        Result res = new Result(name, hits);
        return res;
    }
    
    /**
     * Gets the hits.
     *
     * @return the hits for the document.
     */
    public int getHits() {
        return hits;
    }

    /**
     * Gets the document name.
     *
     * @return gets the document name.
     */
    public String getDocumentName() {
        return documentName;
    }
    
    /**
     * Compares a Result to another Result by the number of hits
     * (falling back to the string comparison to break ties).
     * 
     * Enables lists of Results to be sorted.
     *
     * @return which result has more hits.
     */
    @Override
    public int compareTo(final Result that) {
        
        if (this.getHits() < that.getHits()) {
            return -1;
        } else if (this.getHits() > that.getHits()) {
            return 1;
        } else {
            return this.getDocumentName().compareTo(that.getDocumentName());
        }
    }
    
    /**
     * @return a simple representation of the results.
     */
    @Override
    public String toString() {
        return documentName + " has " + hits + " hits.";
    }
}