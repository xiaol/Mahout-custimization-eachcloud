package org.apache.mahout.cf.taste.vjianke.engine;

import org.apache.lucene.document.Document;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-2-28
 * Time: 下午6:15
 * To change this template use File | Settings | File Templates.
 */
public class TikaIndexer {
    public TikaIndexer(){
    }
    protected Document getDocument(String clipId) throws Exception {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, clipId);
        InputStream is = null;
        Parser parser = new HtmlParser();
        ContentHandler handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        try {
            parser.parse(is, handler, metadata,
                    new ParseContext());
        } finally {
            is.close();
        }
        Document doc = new Document();

        return doc;
    }
}
