/*
 * 
 * Copyright 2018 Franklin Scott
 * All Rights Reserved.
 * 
 */

package com.fscott.texter.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.fscott.texter.api.Texter;
import com.fscott.texter.model.NonIndexedDocument;
import com.fscott.texter.model.Result;
import com.fscott.texter.util.TexterUtils;
import com.google.common.base.Preconditions;

/**
 * Performs search by string match. 
 *
 * @author Franklin Scott
 */

public class StringMatchTexterImpl implements Texter<String,String> {

    private List<NonIndexedDocument> documents = new ArrayList<>();
    private boolean preProcessed = false;
    
    /**
     * Pre-processing for StringMatchTexterImpl loads the strings of a file in a list in memory.
     *
     * @param path to a directory that contains txt to search (non txt files will be skipped).
     * @param whether to pre-process the documents.
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public void prepareDocs(final Path documentDir, boolean doPreProcess) throws FileNotFoundException, IOException {
        Preconditions.checkArgument(documentDir.toFile().exists(), "documentDir must exist");
        
        try (Stream<Path> stream = Files.walk(documentDir)) {
            stream.filter(path -> path.toFile().exists() 
                                  && !path.toFile().isDirectory() 
                                  && path.toFile().getName().endsWith("txt")).forEach(path -> { 
                NonIndexedDocument doc = new NonIndexedDocument(path.toFile());
                documents.add(doc);
            });
        };
    
        if (doPreProcess == true) {
            preProcess();
            this.preProcessed = true;
        }
    }
    
    private void preProcess() throws FileNotFoundException, IOException {
        for (NonIndexedDocument doc : documents) {
            doc.loadContent();
        }
    }

    @Override
    public void searchDocs(final List<String> searchTerms) {
        Preconditions.checkNotNull(searchTerms, "searchTerms cannot be null");
        // AtomicInteger used to possibly support .parallel() in certain cases
        AtomicInteger trial = new AtomicInteger(1);
        searchTerms.stream().forEach(
            target -> {
            System.out.println("(Trial " + trial.get() + ")The target is: " + target);
            
            List<Result> results = new ArrayList<>();
            
            trial.incrementAndGet();
            for (NonIndexedDocument doc : documents) {
                AtomicInteger counter = new AtomicInteger(0);
                if (this.preProcessed) {
                    doc.getContent().forEach(
                            line -> {
                                counter.addAndGet(getHits(line.toLowerCase(),target.toLowerCase()));
                            });
                } else {                
                    try (BufferedReader br = new BufferedReader(new FileReader(doc.getFile()))) {
                        br.lines().forEach(
                            line -> {
                                counter.addAndGet(getHits(line.toLowerCase(),target.toLowerCase()));
                            });
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                results.add(Result.create(doc.getFile().getName(), counter));
            }
            Collections.sort(results, Collections.reverseOrder());
            TexterUtils.printResults(results);
        });
    }

    @Override
    public int getHits(final String line, final String target) {
        Preconditions.checkNotNull(line, "line cannot be null");
        Preconditions.checkNotNull(target, "target cannot be null");
        
        char[] targetChars = target.toCharArray();
        int hits = 0;
        
        final int lenTarget = target.length();
        final int len = line.length() - lenTarget;

        int pos = 0;     
        while (pos <= len) {
//            System.out.println("pos: " + pos);
//            System.out.println("len: " + len);
//            System.out.println("line.charAt(pos): " + line.charAt(pos));

            if (line.charAt(pos) == targetChars[0]) {
                int bump = 1;
                for (int i = 1; i < lenTarget; i++) {
//                    System.out.println("inner line.charAt(pos + i): " + line.charAt(pos + i));
                    if (line.charAt(pos + i) == targetChars[i]) {
                        bump++;
                        if (i + 1 == lenTarget) {
//                            System.out.println("hit!");
                            hits++;
                        }
                    } else {
                        break;
                    }
                }
                pos = pos + bump;
//                System.out.println("inner pos: " + pos);
            } else {
                pos++;
            }
        }
        return hits;
    }
}