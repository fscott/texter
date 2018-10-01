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
    public void prepareDocs(final Path documentDir, final boolean doPreProcess) throws FileNotFoundException, IOException;
        
    public void searchDocs(final List<String> searchTerms);
    
    public int getHits(final T text, final S target);
}