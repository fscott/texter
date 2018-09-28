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

public class RegexTexterImpl implements Texter<String,Pattern> {

    private List<NonIndexedDocument> documents = new ArrayList<>();
    
    @Override
    public void setFilesToProcess(final Path documentDir, boolean doPreProcess) throws FileNotFoundException, IOException {
        
        try (Stream<Path> stream = Files.walk(documentDir)) {
            stream.filter(path -> path.toFile().exists()).forEach(path -> { 
                NonIndexedDocument doc = new NonIndexedDocument(path.toFile());
                documents.add(doc);
            });
        };
    
        if (doPreProcess == true)
            preProcess();
    }
    
    private void preProcess() throws FileNotFoundException, IOException {
        for (NonIndexedDocument doc : documents) {
            doc.loadContent();
        }
    }

    @Override
    public void process(final List<String> searchTerms) {
        AtomicInteger trial = new AtomicInteger(1);
        searchTerms.stream().parallel().forEach(
            target -> {
            System.out.println("(Trial " + trial.get() + ")The target is: " + target);
            
            Pattern searchTermPattern = Pattern.compile(target, Pattern.CASE_INSENSITIVE);
            
            List<Result> results = new ArrayList<>();
            
            trial.incrementAndGet();
            for (NonIndexedDocument doc : documents) {
                AtomicInteger counter = new AtomicInteger(0);
                try (BufferedReader br = new BufferedReader(new FileReader(doc.getFile()))) {
                    br.lines().forEach(
                        line -> counter.addAndGet(getHits(line.toLowerCase(), searchTermPattern))
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
    public int getHits(final String line, final Pattern searchTermPattern) {
        Matcher matcher = searchTermPattern.matcher(line);
        int hits = 0;
        while (matcher.find()) {
            hits++;
        }
        return hits;
    }
}