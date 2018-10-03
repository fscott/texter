/*
 * 
 * Copyright 2018 Franklin Scott
 * All Rights Reserved.
 * 
 */

package com.fscott.texter.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs 
 *
 * @author Franklin Scott
 */

public interface Texter<T,S> {
    
    /**
     * Checks that the document directory exists and walks the directory looking for txt files.
     * If doPreProcess is true, then performs operations to pre-process the documents. Implementing
     * classes determine what pre-processing means.
     *
     * @param path to a directory that contains txt to search (non txt files will be skipped).
     * @param whether to pre-process the documents.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void prepareDocs(final Path documentDir, final boolean doPreProcess) throws FileNotFoundException, IOException;
    
    
    /**
     * Iterates through the search terms and for each document calls {@link #getHits(T,S) getHits}.
     * 
     * Stores a list of type {@link Result.class Result} which implements Comparable to provide 
     * sorted results.
     *
     * @param a list of search term strings.
     */
    public void searchDocs(final List<String> searchTerms);
    
    
    /**
     * Determine how many instances of a given search term there are. Search is case insensitive.
     *
     * @param some text.
     * @param the target of the search.
     * @return the number of hits.
     */
    public int getHits(final T text, final S target);
}