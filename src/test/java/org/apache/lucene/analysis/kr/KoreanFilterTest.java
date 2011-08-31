package org.apache.lucene.analysis.kr;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.PatternConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KoreanFilterTest {
	
	private static final Logger log = LoggerFactory.getLogger(KoreanFilterTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAlphaNumeric() {
		
		System.out.println("Numeric...");
		for(int i = 48; i < 58; i++) {
			System.out.println(i + " : " + String.valueOf(Character.toChars(i)));
		}
		
		System.out.println("Alpha...");
		
		// 소문자: 97 ~ 122
		// 기호들: 91 ~ 96 ==> ???
		// 대문자: 65 ~ 90
		for(int i = 65; i < 123; i++) {
			System.out.println(i + " : " + String.valueOf(Character.toChars(i)));
		}
		
		Assert.assertTrue(isAlphaNumChar("^".charAt(0)));
		
		Assert.assertFalse(isAlphaNumCharNew("^".charAt(0)));
		
	}
	
	private boolean isAlphaNumChar(int c) {
		if ((c >= 48 && c <= 57) || (c >= 65 && c <= 122))
			return true;
		return false;
	}
	
	private boolean isAlphaNumCharNew(int c) {
		if ((c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122))
			return true;
		return false;
	}

	@Test
	public void testExtractKeyword() {
		boolean bigrammable = false;
		
		List<AnalysisOutput> outputs = new ArrayList<AnalysisOutput>();
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for (AnalysisOutput output : outputs) {
			// 동사가 아닐 경우에는 추가
			if (output.getPos() != PatternConstants.POS_VERB) {
				map.put(output.getStem(), new Integer(1));
			}

			
			// 복합어, 단어인 경우: 더 분석 할 것이 없는 상황인듯
			if (output.getScore() >= AnalysisOutput.SCORE_COMPOUNDS) {
				List<CompoundEntry> cnouns = output.getCNounList();
				for (int jj = 0; jj < cnouns.size(); jj++) {
					CompoundEntry cnoun = cnouns.get(jj);
					
					// 한글자 이상의 단어면 추가
					if (cnoun.getWord().length() > 1)
						map.put(cnoun.getWord(), new Integer(0));
					
					// 시작인 한글자 단어면 다음단어까지 추가
					if (jj == 0 && cnoun.getWord().length() == 1) {
						map.put(cnoun.getWord() + cnouns.get(jj + 1).getWord(), new Integer(0));
					// 3번째 이상부터 한글자 일 경우.. 같은 글자를 두번
					} else if (jj > 1 && cnoun.getWord().length() == 1) {
						map.put(cnouns.get(jj).getWord() + cnoun.getWord(), new Integer(0));
					}
				}
			} else if (bigrammable) {
//				addBiagramToMap(output.getStem(), map);
			}
		}

		log.debug("extractKeyword(), outputs:{}\r\n, map:{}", outputs, map);
	}
}
