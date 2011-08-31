package org.apache.lucene.analysis.kr;

import java.util.Iterator;

import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.MorphAnalyzer;
import org.apache.lucene.analysis.kr.tagging.Tagger;

import junit.framework.TestCase;

public class TaggerTest extends TestCase {

	public void testTagging() throws Exception {
		Iterator<String[]> iter = Tagger.getGR("할");
		while (iter.hasNext()) {
			String[] strs = iter.next();
			System.out.println(strs[0] + "?" + strs[1] + "?" + strs[2] + "?" + strs[3] + "?" + strs[4] + "?" + strs[5]);
		}

	}

    public void testTag() throws Exception {

		String str1 = "메시지를";
		String str2 = "못했다";

		MorphAnalyzer morphAnal = new MorphAnalyzer();

		Tagger tagger = new Tagger();
		AnalysisOutput o = tagger.tagging(str1, str2, morphAnal.analyze(str1), morphAnal.analyze(str2));

		System.out.println(o);
	}
}
