/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.utilities.text.analysis;

import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.utilities.text.cleaners.HTMLCleaner;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextSimilarity {
    protected static String preprocessDocument(String text) {
        //URLs
        text=StringCleaner.tokenizeURLs(text);

        //Remove HTML
        text=HTMLCleaner.extractText(text);

        //Remove an Accents
        text=StringCleaner.removeAccents(text);
        
        //Remove extra spaces
        text=StringCleaner.removeExtraSpaces(text);
        
        return text;
    }

    public static double oliverSimilarity(String text1, String text2) {
        preprocessDocument(text1);
        preprocessDocument(text2);

        String smallerDoc=text1;
        String biggerDoc=text2;
        if(text1.length()>text2.length()) {
            smallerDoc=text2;
            biggerDoc=text1;
        }

        double p=PHPSimilarText.similarityPercentage(smallerDoc, biggerDoc);
        p/=100.0;

        return p;
    }

    
    private static void filterKeywordCombinations(Map<String, Double> keywords, int w) {
        Iterator<Map.Entry<String, Double>> it = keywords.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if(PHPfunctions.substr_count(entry.getKey(), ' ')!=w-1) {
                it.remove();
            }
        }
    }
    public static double shinglerSimilarity(String text1, String text2, int w) {
        //http://phpir.com/shingling-near-duplicate-detection
        //http://www.std.org/~msm/common/clustering.html
        preprocessDocument(text1);
        preprocessDocument(text2);

        NgramsExtractor.Parameters conf = new NgramsExtractor.Parameters();
        conf.setMaxCombinations(w);
        conf.setMaxDistanceBetweenKwds(0);

        NgramsExtractor ngrams = new NgramsExtractor();
        ngrams.setParameters(conf);

        
        Map<String, Double> keywords1 = ngrams.extract(text1);
        Map<String, Double> keywords2 = ngrams.extract(text2);
        
        //remove all the other combinations except of the w-grams
        filterKeywordCombinations(keywords1, w); 
        filterKeywordCombinations(keywords2, w);
        
        ngrams=null;
        conf=null;
        
        double totalKeywords=0.0;
        double commonKeywords=0.0;
        
        Set<String> union = new HashSet<>(keywords1.keySet());
        union.addAll(keywords2.keySet());
        totalKeywords+=union.size();
        union=null;


        Set<String> intersect = new HashSet<>(keywords1.keySet());
        intersect.retainAll(keywords2.keySet());
        commonKeywords+=intersect.size();
        intersect=null;
        
        double resemblance=commonKeywords/totalKeywords;

        keywords1=null;
        keywords2=null;

        return resemblance;
    }
}
    
