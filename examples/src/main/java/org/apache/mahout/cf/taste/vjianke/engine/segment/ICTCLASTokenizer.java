package org.apache.mahout.cf.taste.vjianke.engine.segment;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-4
 * Time: 下午6:05
 * To change this template use File | Settings | File Templates.
 */
public final class ICTCLASTokenizer extends Tokenizer{
    //private TermAttribute termAttr;
    private OffsetAttribute offAttr;
    private Matcher matcher;

    protected ICTCLASTokenizer(Reader input) {
        super(input);
    }

    private void init(String segmented) {
        //termAttr = addAttribute(TermAttribute.class);
        offAttr = addAttribute(OffsetAttribute.class);
        matcher = Pattern.compile("(([^ ]+)| )/(\\w)+").matcher(segmented);
    }

    private boolean filter(String t, String type) {
        if (t.matches("\\s*"))
            return false;
        if (t.matches("[0-9]+"))
            return false;
        if (t.equals("��")) {
            return false;
        }
        if (type.endsWith("n")) {
            return true;
        }
        if (type.equals("x") && t.length() > 1)
            return true;
        return false;
    }

    @Override
    public boolean incrementToken() throws IOException {
        int s = offAttr.endOffset();
        clearAttributes();
        while (matcher.find()) {
            String t = matcher.group(1);
            String type = matcher.group(3);
            if (filter(t, type)) {
                //termAttr.setTermBuffer(t, 0, t.length());
                offAttr.setOffset(s, s + t.length());
                return true;
            } else {
                s += t.length();
            }
        }
        return false;
    }
}
