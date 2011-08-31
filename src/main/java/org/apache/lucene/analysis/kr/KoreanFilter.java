package org.apache.lucene.analysis.kr;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.MorphAnalyzer;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.morph.PatternConstants;
import org.apache.lucene.analysis.kr.morph.WordSpaceAnalyzer;
import org.apache.lucene.analysis.kr.utils.DictionaryUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KoreanFilter extends TokenFilter {
	private static final Logger logger = LoggerFactory.getLogger(KoreanFilter.class);

	private LinkedList<Token> koreanQueue;

	private LinkedList<Token> cjQueue;

	private MorphAnalyzer morph;

	WordSpaceAnalyzer wsAnal;

	private boolean bigrammable = true;

	private boolean hasOrigin = true;

	private static final String APOSTROPHE_TYPE = KoreanTokenizerImpl.TOKEN_TYPES[KoreanTokenizerImpl.APOSTROPHE];
	private static final String ACRONYM_TYPE = KoreanTokenizerImpl.TOKEN_TYPES[KoreanTokenizerImpl.ACRONYM];

	// this filters uses attribute type
	private TypeAttribute typeAtt;
	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private PositionIncrementAttribute posIncrAtt;

	public KoreanFilter(TokenStream input) {
		super(input);
		koreanQueue = new LinkedList<Token>();
		cjQueue = new LinkedList<Token>();
		morph = new MorphAnalyzer();
		wsAnal = new WordSpaceAnalyzer();

		termAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	}

	/**
	 *
	 * @param input
	 *            input token stream
	 * @param bigram
	 *            Whether the bigram index term return or not.
	 */
	public KoreanFilter(TokenStream input, boolean bigram) {
		this(input);
		bigrammable = bigram;
	}

	public KoreanFilter(TokenStream input, boolean bigram, boolean has) {
		this(input, bigram);
		hasOrigin = has;
	}

	private void updateFromToken(Token token, CharTermAttribute term, TypeAttribute type, OffsetAttribute offset,
	        PositionIncrementAttribute posInc) {
		if (token == null)
			return;
		if (termAtt != null) {
			termAtt.copyBuffer(token.buffer(), 0, token.length());
		}
		if (typeAtt != null)
			typeAtt.setType(token.type());
		if (offsetAtt != null)
			offsetAtt.setOffset(token.startOffset(), token.endOffset());
		if (posIncrAtt != null)
			posIncrAtt.setPositionIncrement(token.getPositionIncrement());
	}

	/**
	 * Returns the next token in the stream, or null at EOS.
	 * <p>
	 * Removes <tt>'s</tt> from the end of words.
	 * <p>
	 * Removes dots from acronyms.
	 */
	@Override
	public final boolean incrementToken() throws java.io.IOException {
		this.clearAttributes();

		if (koreanQueue.size() > 0) {
			Token token = koreanQueue.removeFirst();
			updateFromToken(token, termAtt, typeAtt, offsetAtt, posIncrAtt);
			logger.debug("KoreanFilter::incrementToken() by queue term:{}", token);
			return (token != null);
		} else if (cjQueue.size() > 0) {
			Token token = cjQueue.removeFirst();
			updateFromToken(token, termAtt, typeAtt, offsetAtt, posIncrAtt);
			logger.debug("KoreanFilter::incrementToken() term:{}", token);
			return (token != null);
		}
		boolean result = false;

		int skippedPositions = 0;

		try {
			while (input.incrementToken()) {
				if (typeAtt.type().equals(KoreanTokenizer.TOKEN_TYPES[KoreanTokenizer.KOROREAN])) {
					result = analysisKorean(skippedPositions);
				} else if (typeAtt.type().equals(KoreanTokenizer.TOKEN_TYPES[KoreanTokenizer.CJ])) {
					result = analysisCJ(skippedPositions);
				} else {
					result = analysisETC();
				}

				if (result == false) {
					skippedPositions++;
					continue;
				}

				logger.debug("KoreanFilter::incrementToken() by queue term(inner):{}", termAtt);

				return result;
			}
		} catch (MorphException e) {
			throw new IOException(e.getMessage());
		}

		return false;
	}

	/**
	 * 한글을 분석한다.
	 *
	 * @param token
	 * @param skipinc
	 * @return
	 * @throws MorphException
	 */
	@SuppressWarnings("unchecked")
    private boolean analysisKorean(int skipinc) throws MorphException {
		String input = termAtt.toString();
		List<AnalysisOutput> outputs = morph.analyze(input);

		if (outputs.size() == 0)
			return false;

		Map<String, Integer> map = new HashMap<String, Integer>();
		if (hasOrigin)
			map.put(input, new Integer(1));

		if (outputs.get(0).getScore() == AnalysisOutput.SCORE_CORRECT) {
			extractKeyword(outputs, map);
		} else {
			// WordSpaceAlanlyzer
			List<AnalysisOutput> list = wsAnal.analyze(input);

			List<AnalysisOutput> results = new ArrayList<AnalysisOutput>();
			if (list.size() > 1) {
				for (AnalysisOutput o : list) {
					if (hasOrigin)
						map.put(o.getSource(), new Integer(1));
					results.addAll(morph.analyze(o.getSource()));
				}
			} else {
				results.addAll(list);
			}

			extractKeyword(results, map);
		}

		Iterator<String> iter = map.keySet().iterator();
		int i = 0;
		Token token = null;
		while (iter.hasNext()) {

			String text = iter.next();

			if (text.length() <= 1)
				continue;

			int index = input.indexOf(text);
			token = new Token(text, offsetAtt.startOffset() + (index != -1 ? index : 0),
			        index != -1 ? offsetAtt.startOffset() + index + text.length() : offsetAtt.endOffset(),
			        KoreanTokenizer.TOKEN_TYPES[KoreanTokenizer.KOROREAN]);
			if (i == 0)
				token.setPositionIncrement(posIncrAtt.getPositionIncrement() + skipinc);
			else
				token.setPositionIncrement(0);
			koreanQueue.add(token);
			i++;

			logger.debug("analysisKorean(), token:{}",token);
		}

		logger.debug("analysisKorean(), koreanQueue:{}",koreanQueue);

		if (koreanQueue.size() == 0)
			return false;

		// TODO: 무슨 이유로 값을 가지고 오는지 확인 할 것
		token = koreanQueue.removeFirst();
		this.updateFromToken(token, termAtt, typeAtt, offsetAtt, posIncrAtt);

		return true;
	}

	private void extractKeyword(List<AnalysisOutput> outputs, Map<String, Integer> map) throws MorphException {
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
				addBiagramToMap(output.getStem(), map);
			}
		}

		logger.debug("extractKeyword(), outputs:{}\r\n, map:{}", outputs, map);
	}

	private void addBiagramToMap(String input, Map<String, Integer> map) {
		int offset = 0;
		int strlen = input.length();
		while (offset < strlen - 1) {
			if (isAlphaNumChar(input.charAt(offset))) {
				String text = findAlphaNumeric(input.substring(offset));
				map.put(text, new Integer(0));
				offset += text.length();
			} else {
				String text = input.substring(offset, offset + 2 > strlen ? strlen : offset + 2);
				map.put(text, new Integer(0));
				offset++;
			}
		}
	}

	private String findAlphaNumeric(String text) {
		int pos = 0;
		for (int i = 0; i < text.length(); i++) {
			if (!isAlphaNumChar(text.charAt(i)))
				break;
			pos++;
		}
		return text.substring(0, pos);
	}

	private boolean analysisCJ(int skipinc) throws MorphException {
		String input = termAtt.toString();
		Token t = new Token(input, 0, offsetAtt.endOffset(), KoreanTokenizer.TOKEN_TYPES[KoreanTokenizer.CJ]);
		t.setPositionIncrement(posIncrAtt.getPositionIncrement() + skipinc);
		cjQueue.add(t);

		String kor = DictionaryUtil.getCJWord(input);
		if (kor != null) {
			Token t1 = new Token(kor, 0, offsetAtt.endOffset(), KoreanTokenizer.TOKEN_TYPES[KoreanTokenizer.CJ]);
			t1.setPositionIncrement(0);
			cjQueue.add(t1);
		}

		return (cjQueue.removeFirst() != null);
	}

	private boolean analysisETC() throws MorphException {
		char[] buffer = termAtt.buffer();
		final int bufferLength = termAtt.length();
		final String type = typeAtt.type();

		if (type == APOSTROPHE_TYPE
		        && // remove 's
		        bufferLength >= 2 && buffer[bufferLength - 2] == '\''
		        && (buffer[bufferLength - 1] == 's' || buffer[bufferLength - 1] == 'S')) {
			// Strip last 2 characters off
			termAtt.setLength(bufferLength - 2);
		} else if (type == ACRONYM_TYPE) { // remove dots
			int upto = 0;
			for (int i = 0; i < bufferLength; i++) {
				char c = buffer[i];
				if (c != '.')
					buffer[upto++] = c;
			}
			termAtt.setLength(upto);
		}

		return true;
	}
	
	private boolean isAlphaNumChar(int c) {
		if ((c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122))
			return true;
		return false;
	}

	public void setHasOrigin(boolean has) {
		hasOrigin = has;
	}
}
