package com.fscott.texter.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Result implements Comparable<Result> {
    
    private int hits;
    private String documentName;
    
    private Result(final String name, AtomicInteger hits) {
        this.documentName = name;
        this.hits = hits.get();
    }
    
    static public Result create(final String name, AtomicInteger hits) {
        Result res = new Result(name, hits);
        return res;
    }
    
    public int getHits() {
        return hits;
    }

    public String getDocumentName() {
        return documentName;
    }
    
    @Override
    public int compareTo(Result that) {
        
        if (this.getHits() < that.getHits()) {
            return -1;
        } else if (this.getHits() > that.getHits()) {
            return 1;
        } else {
            return this.getDocumentName().compareTo(that.getDocumentName());
        }
    }
    
    @Override
    public String toString() {
        return documentName + " has " + hits + " hits.";
    }
}