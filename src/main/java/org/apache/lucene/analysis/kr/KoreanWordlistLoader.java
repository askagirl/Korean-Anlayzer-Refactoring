package org.apache.lucene.analysis.kr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.lucene.analysis.WordlistLoader;

public final class KoreanWordlistLoader extends WordlistLoader {

    /**
     * Loads a text file and adds every line as an entry to a HashSet (omitting
     * leading and trailing whitespace). Every line of the file should contain only
     * one word. The words need to be in lowercase if you make use of an
     * Analyzer which uses LowerCaseFilter (like StandardAnalyzer).
     *
     * @param wordfile File containing the wordlist
     * @return A HashSet with the file's words
     */
    public static HashSet<String> getWordSet(File wordfile) throws IOException {
        return getWordSet(wordfile, KoreanAnalyzer.DIC_ENCODING);
    }

    /**
     * Loads a text file and adds every line as an entry to a HashSet (omitting
     * leading and trailing whitespace). Every line of the file should contain only
     * one word. The words need to be in lowercase if you make use of an
     * Analyzer which uses LowerCaseFilter (like StandardAnalyzer).
     *
     * @param wordfile File containing the wordlist
     * @return A HashSet with the file's words
     */
    public static HashSet<String> getWordSet(File wordfile, String encoding) throws IOException {
    	InputStream is = new FileInputStream(wordfile);
        InputStreamReader reader = new InputStreamReader(is, encoding);		
        return WordlistLoader.getWordSet(reader);
    }
}
