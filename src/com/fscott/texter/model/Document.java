package com.fscott.texter.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Document {
	
	private File file;
	private List<String> content = new ArrayList<>();
	
	public Document(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public void loadContent() throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {	        	    
			br.lines().parallel().forEach(
    	    	line -> this.content.add(line)
    	    );
    	}
	}
	
	public List<String> getContent() throws FileNotFoundException, IOException {
		if (this.content != null) {
			return this.content;
		} else {
			System.out.println("no content loaded for " + this.getFile().getName());
			this.loadContent();
			return this.content;
		}
	}
}