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

        Stopwatch stopwatch = Stopwatch.createStarted();
        
        int num = 0;
        boolean preProcess = false;
        String customTarget = null;
        String texterType = null;
        
        final String appConfigPath = "config.properties";
        File f = new File(appConfigPath);
        if (f.exists()) {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(appConfigPath));
            
            num = Integer.parseInt(appProps.getProperty("numberOfTrials"));
            preProcess = Boolean.parseBoolean(appProps.getProperty("preProcess", "false"));
            customTarget = appProps.getProperty("searchTerm");
            
            texterType = appProps.getProperty("texterType");
            
        }
        
        List<String> targets = new ArrayList<>();
        
        if (customTarget != null)
             targets.add(customTarget);
        else 
            targets = getTargets(num);
        
        final Path docDir = Paths.get("res/docs");
        
        if (texterType.equals("string")) {
            System.out.println("Using string matcher.");
            StringMatchTexterImpl stringMatcher = new StringMatchTexterImpl();
            stringMatcher.setFilesToProcess(docDir, preProcess);
            stringMatcher.process(targets);
        } else if (texterType.equals("regex")) {
            System.out.println("Using regex matcher.");
            RegexTexterImpl regexMatcher = new RegexTexterImpl();
            regexMatcher.setFilesToProcess(docDir, preProcess);
            regexMatcher.process(targets);
        }
        
        stopwatch.stop();
        System.out.println("Finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " milliseconds.");
        System.out.println("Finished in " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
    }
    
    public static List<String> getTargets(int num) throws FileNotFoundException, IOException {
        File wordsFile = new File("res/words_alpha.txt");
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