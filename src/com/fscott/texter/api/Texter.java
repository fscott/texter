/*
 * 
 * Copyright 2018 Franklin Scott
 * All Rights Reserved.
 * 
 */

package com.fscott.texter.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Runs 
 *
 * @author Franklin Scott
 */

public interface Texter {
	public void setFilesToProcess(final List<File> filesToProcess, final boolean doPreProcess) throws FileNotFoundException, IOException;
		
	public void process(final List<String> searchTerms);
	
	public int getHits(final String line, final String target);
}