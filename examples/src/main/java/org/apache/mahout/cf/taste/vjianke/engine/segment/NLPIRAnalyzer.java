package org.apache.mahout.cf.taste.vjianke.engine.segment;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;


public class NLPIRAnalyzer extends Analyzer {


	private NLPIRDelegate ictc;
	private String result;

	public NLPIRAnalyzer() {
		this(null);
	}

    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {

        Tokenizer tokenizer = new NLPIRTokenizer(reader);
        return new TokenStreamComponents(tokenizer, null);
    }

    public NLPIRAnalyzer(NLPIRDelegate ictc) {
		this.ictc = ictc;
	}

	public String getSegmentedString() {
		return result;
	}

	/*@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		try {
			char[] buff = new char[1024];
			int cpos = 0;
			int len;
			while ((len = reader.read(buff, cpos, 1024)) == 1024) {
				char[] t = buff;
				buff = new char[buff.length + 1024];
				System.arraycopy(t, 0, buff, 0, t.length);
				cpos += len;
			}
			cpos += len;
			if (ictc != null)
				result = ictc.process(new String(buff, 0, cpos));
			else
				result = new String(buff, 0, cpos);
			return new ICTCLASTokenzier(result);
		} catch (IOException e) {
			return null;
		}
	}  */

}
