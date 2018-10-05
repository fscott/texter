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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fscott.texter.api.Texter;
import com.fscott.texter.model.NonIndexedDocument;
import com.fscott.texter.model.Result;
import com.fscott.texter.util.TexterUtils;
import com.google.common.base.Preconditions;

/**
 * Performs search by regex match. 
 *
 * @author Franklin Scott
 */
public class RegexTexterImpl implements Texter {

    private List<NonIndexedDocument> documents = new ArrayList<>();
    private boolean preProcessed = false;
    
    /**
     * Pre-processing for RegexTexterImpl loads the strings of a file in a list in memory.
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
                                  && path.toFile().getName().endsWith(".txt")).forEach(path -> { 
                NonIndexedDocument doc = new NonIndexedDocument(path.toFile());
                documents.add(doc);
            });
        };
    
        if (doPreProcess == true) {
            preProcess();
            preProcessed = true;
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
            
            // Pattern.LITERAL as limited protection against funky input
            Pattern searchTermPattern = Pattern.compile(target, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
            
            List<Result> results = new ArrayList<>();
            
            trial.incrementAndGet();
            for (NonIndexedDocument doc : documents) {
                AtomicInteger counter = new AtomicInteger(0);
                if (this.preProcessed) {
                    doc.getContent().forEach(
                            line -> counter.addAndGet(getHits(line.toLowerCase(), searchTermPattern))
                    );
                } else {
                    try (BufferedReader br = new BufferedReader(new FileReader(doc.getFile()))) {
                        br.lines().forEach(
                            line -> counter.addAndGet(getHits(line.toLowerCase(), searchTermPattern))
                        );
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

    private int getHits(final String line, final Pattern searchTermPattern) {
        Preconditions.checkNotNull(line, "line cannot be null");
        Preconditions.checkNotNull(searchTermPattern, "searchTermPattern cannot be null");
        
        Matcher matcher = searchTermPattern.matcher(line);
        int hits = 0;
        while (matcher.find()) {
            hits++;
        }
        return hits;
    }
}