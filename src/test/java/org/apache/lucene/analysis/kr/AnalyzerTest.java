package org.apache.lucene.analysis.kr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.utils.NounUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;

import junit.framework.TestCase;

public class AnalyzerTest extends TestCase {
	
	private String testSentence = "바닥은 짙은 회색의 돌로 되어 있습니다. 金融";
	private Reader sentences;
	
	private StandardAnalyzer standardAnalyzer;
	private WhitespaceAnalyzer whitespaceAnalyzer;
	private StopAnalyzer stopAnalyzer;
	private KoreanAnalyzer koreanAnalyzer;
	
	public void setUp() {
		
		this.sentences = new StringReader(this.testSentence);
		
		this.standardAnalyzer = new StandardAnalyzer(Version.LUCENE_33);
		this.whitespaceAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_33);
		this.stopAnalyzer = new StopAnalyzer(Version.LUCENE_33, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		this.koreanAnalyzer = new KoreanAnalyzer(Version.LUCENE_33);
		
	}
	
	public void testMeaninglessWhitespaceAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();
		
		TokenStream stream = this.whitespaceAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);

		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}
	
	public void testMeaninglessStandardAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();
		TokenStream stream = this.standardAnalyzer.tokenStream("k", this.sentences);

		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
		System.out.println("\n\n");
	}
	
	public void testStandardAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();
		
		TokenStream stream = this.standardAnalyzer.tokenStream("k", this.sentences);

		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}
	
	public void testWhitespaceAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();
		
		TokenStream stream = this.whitespaceAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);

		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}
	
	public void testStopAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();
		
		TokenStream stream = this.stopAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);

		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}

	public void testKoreanAnalyzer() throws IOException, MorphException {
		long start = System.nanoTime();

		this.koreanAnalyzer.setBigrammable(true);
		this.koreanAnalyzer.setHasOrigin(true);
		TokenStream stream = this.koreanAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}

	public void testKoreanAnalyzerWithNoBigrammable() throws IOException, MorphException {
		long start = System.nanoTime();
		
		this.koreanAnalyzer.setBigrammable(false);
		this.koreanAnalyzer.setHasOrigin(true);
		TokenStream stream = this.koreanAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}

	public void testKoreanAnalyzerWithNoHasOrigin() throws IOException, MorphException {
		long start = System.nanoTime();
		
		this.koreanAnalyzer.setBigrammable(true);
		this.koreanAnalyzer.setHasOrigin(false);
		TokenStream stream = this.koreanAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}

	public void testKoreanAnalyzerWithNoBigrammableAndNoHasOrigin() throws IOException, MorphException {
		long start = System.nanoTime();
		
		this.koreanAnalyzer.setBigrammable(false);
		this.koreanAnalyzer.setHasOrigin(false);
		TokenStream stream = this.koreanAnalyzer.tokenStream("k", this.sentences);
		
		displayTokenStreamInfo(stream);
		
		System.out.println("elapsed time: [" + (System.nanoTime() - start) / 1000000 + "]ms");
	}
	
	private void displayTokenStreamInfo(TokenStream stream) throws IOException, MorphException {
		System.out.println(this.getName());
		PositionIncrementAttribute posIncr = (PositionIncrementAttribute) stream.addAttribute(PositionIncrementAttribute.class);
		OffsetAttribute offset = (OffsetAttribute) stream.addAttribute(OffsetAttribute.class);
		TypeAttribute type = (TypeAttribute) stream.addAttribute(TypeAttribute.class);

		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		
		while (stream.incrementToken()) {
			assertFalse(NounUtil.endsWith2Josa(term.toString()));
			System.out.print("[" + term + "] ");
			System.out.print("[" + posIncr.getPositionIncrement() + "] ");
			System.out.print("[" + offset.startOffset() + "] ");
			System.out.print("[" + offset.endOffset() + "] ");
			System.out.println("[" + type.type() + "] ");
		}
	}

}
