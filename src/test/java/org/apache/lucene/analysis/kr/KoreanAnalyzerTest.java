package org.apache.lucene.analysis.kr;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KoreanAnalyzerTest {
	private static final Logger logger = LoggerFactory.getLogger(KoreanAnalyzerTest.class);

	/**
	 * t.getPositionIncrement() 는 같은 단어에서 추출되었는지, 다른 단어에서 추출되었는지를 알려준다. 즉 1이면
	 * 현재의 색인어는 새로운 단어에서 추출된 것이고 0 이면 이전 색인어와 같은 단어에서 추출된 것이다. 이 값은 검색 랭킹에 영향을
	 * 미친다. 즉 값이 작으면 검색랭킹이 올라가서 검색시 상위에 올라오게 된다.
	 *
	 * @throws Exception
	 */
	@Test
	public void koreanAnalyzer() throws Exception {
		String source = "모험가의 우리나라 라면에서부터 일본라면이 파생되었잖니?";

		long start = System.currentTimeMillis();
		logger.debug("testKoreanAnalyzer()[{}] start!!", source);

		KoreanAnalyzer analyzer = new KoreanAnalyzer(Version.LUCENE_32);
		TokenStream ts = analyzer.tokenStream("k", new StringReader(source));

		int countToken = 0;
		while (ts.incrementToken()) {
			CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
			PositionIncrementAttribute posIncrAtt = ts.getAttribute(PositionIncrementAttribute.class);
			OffsetAttribute offsetAtt = ts.getAttribute(OffsetAttribute.class);

			logger.debug("{}:{}:{}:{}", new Object[] { posIncrAtt.getPositionIncrement(), offsetAtt.startOffset(),
			        offsetAtt.endOffset(), termAtt });
			countToken++;
		}
		logger.debug("{} ms", (System.currentTimeMillis() - start));
		assertEquals(15, countToken);
	}

}
