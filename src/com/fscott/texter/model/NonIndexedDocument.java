package com.fscott.texter.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NonIndexedDocument {
    
    private File file;
    private List<String> content = new ArrayList<>();
    
    /**
     *
     * @param a File to be searched.
     */
    public NonIndexedDocument(File file) {
        this.file = file;
    }
    
    /**
    *
    * @return the file associated with this document.
    */
    public File getFile() {
        return this.file;
    }
    
    /**
     * Processes the lines of text of the file into a list of Strings. Holds the 
     * text in memory rather than reading it for each trial.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadContent() throws FileNotFoundException, IOException {
        System.out.println("Loading content from " + this.file.getName());
        try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {                    
            br.lines().forEach(
                line -> this.content.add(line)
            );
        }
    }
    
    /**
    *
    * @return the document's list of strings, or if the content hasn't been
    * loaded yet, process the document and then return them. 
    */
    public List<String> getContent() {
        if (this.content != null) {
            return this.content;
        } else {
            System.out.println("no content loaded for " + this.getFile().getName());
            try {
                this.loadContent();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return this.content;
        }
    }
}