/*
 * 
 * Copyright 2018 Franklin Scott
 * All Rights Reserved.
 * 
 */

package com.fscott.texter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.ParseException;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import com.fscott.texter.impl.LuceneTexterImpl;
import com.fscott.texter.impl.RegexTexterImpl;
import com.fscott.texter.impl.StringMatchTexterImpl;
import com.google.common.base.Stopwatch;

/**
 * Runner for Texter. Use config.properties for options. 
 *
 * @author Franklin Scott
 */

class RunTexter {


    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {

        int num = 1;
        boolean preProcess = false;
        String customTarget = null;
        String texterType = null;
        
        TextIO textIO = TextIoFactory.getTextIO();
        
        customTarget = textIO.newStringInputReader()
                                        .withDefaultValue(null)
                                        .withMinLength(0)
                                        .read("Enter the search term (or leave blank to use a random word(s)): ");
        
        texterType = textIO.newStringInputReader()
                                        .withInlinePossibleValues("string", "regex", "lucene")
                                        .withIgnoreCase()
                                        .withInputTrimming(true)
                                        .read("Enter the search method: ");
        
        if (!texterType.equals("lucene")) {
            preProcess = textIO.newBooleanInputReader()
                               .withDefaultValue(false)
                               .read("Preprocess the text files?: ");
        }

        List<String> targets = new ArrayList<>();
        
        if (customTarget != null && customTarget.length() > 1)
             targets.add(customTarget);
        else {
            num = textIO.newIntInputReader()
            .withDefaultValue(1)
            .withMinVal(1)
            .withMaxVal(Integer.MAX_VALUE)
            .read("How many trials to run (default is 1): ");
            targets = getTargets(num);
        }
        
        final Path docDir = Paths.get("res/docs");
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        
        if (texterType.equals("string")) {
            System.out.println("Using string matcher.");
            StringMatchTexterImpl stringMatcher = new StringMatchTexterImpl();
            stringMatcher.prepareDocs(docDir, preProcess);
            stringMatcher.searchDocs(targets);
        } else if (texterType.equals("regex")) {
            System.out.println("Using regex matcher.");
            RegexTexterImpl regexMatcher = new RegexTexterImpl();
            regexMatcher.prepareDocs(docDir, preProcess);
            regexMatcher.searchDocs(targets);
        } else if (texterType.equals("lucene")) {
            System.out.println("Using lucene index matcher.");
            LuceneTexterImpl luceneMatcher = new LuceneTexterImpl(Paths.get("res/index"), true);
            luceneMatcher.prepareDocs(docDir, preProcess);
            luceneMatcher.searchDocs(targets);
        }
        
        stopwatch.stop();
        System.out.println("Finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " milliseconds.");
        System.out.println("Finished in " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
        double ratio = num / stopwatch.elapsed(TimeUnit.SECONDS);
        System.out.println("Processed " + ratio +  " trials per second.");
    }
    
    public static List<String> getTargets(int num) throws FileNotFoundException, IOException {
        //File wordsFile = new File("res/words_alpha.txt");
        File wordsFile = new File("res/words_ten_hundred.txt");
        List<String> words = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(wordsFile))) {
            br.lines().forEach(word -> {if (word !=null) words.add(word);});
        }
        
        List<String> randomWords = new ArrayList<>(num+1);
        int i = 0;
        while (i < num) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, words.size());
            randomWords.add(words.get(randomNum));
            i++;
        }
        return randomWords;
    }
    
}