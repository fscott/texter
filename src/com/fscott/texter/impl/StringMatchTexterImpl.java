/*
 * 
 * Copyright 2018 Franklin Scott
 * All Rights Reserved.
 * 
 */

package com.fscott.texter.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fscott.texter.api.Texter;
import com.fscott.texter.model.Document;
import com.fscott.texter.model.Result;

/**
 * Runs 
 *
 * @author Franklin Scott
 */

public class StringMatchTexterImpl implements Texter {

	private List<Document> documents = new ArrayList<>();
	
	@Override
	public void setFilesToProcess(List<File> filesToProcess, boolean doPreProcess) throws FileNotFoundException, IOException {
		
        for (File file : filesToProcess) {
        	if (!file.exists()) {
				throw new FileNotFoundException();
			} else {
				Document doc = new Document(file);
				documents.add(doc);
			}
		}
	
		if (doPreProcess == true)
		    preProcess();
	}
	
	private void preProcess() throws FileNotFoundException, IOException {
		for (Document doc : documents) {
			doc.loadContent();
		}
	}

	@Override
	public void process(final List<String> searchTerms) {
		AtomicInteger trial = new AtomicInteger(1);
		searchTerms.stream().parallel().forEach(
			target -> {
	    	System.out.println("(Trial " + trial.get() + ")The target is: " + target);
	    	
	    	List<Result> results = new ArrayList<>();
	    	
	    	trial.incrementAndGet();
    		for (Document doc : documents) {
	    		AtomicInteger counter = new AtomicInteger(0);
	    		try (BufferedReader br = new BufferedReader(new FileReader(doc.getFile()))) {
	        	    br.lines().forEach(
	        	    	line -> counter.addAndGet(getHits(line.toLowerCase(),target.toLowerCase()))
	        	    );
	        	} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
	    		results.add(Result.create(doc.getFile().getName(), counter));	    		
	    	}
    		Collections.sort(results, Collections.reverseOrder());
    		System.out.println(results.toString());
    	});
	}

	@Override
	public int getHits(final String line, final String target) {
    	char[] targetChars = target.toCharArray();
    	int hits = 0;
    	
    	final int lenTarget = target.length();
    	final int len = line.length() - lenTarget;

    	int pos = 0; 	
    	while (pos < len) {
    		//System.out.println("pos: " + pos);
    		//System.out.println("line.charAt(pos): " + line.charAt(pos));

    		if (line.charAt(pos) == targetChars[0]) {
    			int bump = 1;
    			for (int i = 1; i < lenTarget; i++) {
    				//System.out.println("line.charAt(pos + i): " + line.charAt(pos + i));
    				if (line.charAt(pos + i) == targetChars[i]) {
    					bump++;
    					if (i + 1 == lenTarget) {
    						hits++;
    					}
    				} else {
    					break;
    				}
    			}
    			pos += bump;
    		} else {
    			pos++;
    		}
    	}
    	return hits;
	}
}