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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fscott.texter.api.Texter;

/**
 * Runs 
 *
 * @author Franklin Scott
 */

public class StringMatchTexterImpl implements Texter {

	private List<File> filesToProcess;
	private boolean doPreProcess = false;
	private HashMap<String,List<String>> contents = new HashMap<>();
	
	@Override
	public void setFilesToProcess(List<File> filesToProcess, boolean doPreProcess) throws FileNotFoundException, IOException {
		
		this.filesToProcess = filesToProcess;
		this.doPreProcess = doPreProcess;
		
		if (this.doPreProcess) {
			this.preProcess();
		} else {
			for (File file : this.filesToProcess) {
				if (!file.exists()) {
					throw new FileNotFoundException();
				}
			}
		}
	}
	
	private void preProcess() throws FileNotFoundException, IOException {
		for (File file : filesToProcess) {
			List<String> content = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {	        	    
				br.lines().parallel().forEach(
        	    	line -> content.add(line)
        	    );
        	}
			contents.put(file.getName(),content);
		}
		
	}

	@Override
	public void process(final List<String> searchTerms) {
		if (!this.doPreProcess) {
			AtomicInteger trial = new AtomicInteger(1);
			searchTerms.stream().parallel().forEach(
				target -> {
		    	System.out.println("(Trial " + trial.get() + ")The target is: " + target);
		    	
		    	trial.incrementAndGet();
	    		for (File file : filesToProcess) {
		    		AtomicInteger counter = new AtomicInteger(0);
		    		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		        	    br.lines().forEach(
		        	    	line -> counter.addAndGet(getHits(line.toLowerCase(),target.toLowerCase()))
		        	    );
		        	} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					}
		    		System.out.println("Hits for " + file.getName() + ": " + counter.toString());
		    	}
	    	});
		} else {
    		AtomicInteger trial = new AtomicInteger(1);
	    	searchTerms.stream().parallel().forEach(
	    		target -> {
		    	System.out.println("(Trial " + trial.get() + ") The target is: " + target);
		    	
		    	trial.incrementAndGet();
		    	for (String contentName : contents.keySet()) {
		    		AtomicInteger counter = new AtomicInteger(0);
		    		contents.get(contentName).stream().forEach(
		    				line -> counter.addAndGet(getHits(line.toLowerCase(),target.toLowerCase()))
		        	);
		    		System.out.println("Hits for " + contentName + ": " + counter.toString());
		    	}
	    	});
		}
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