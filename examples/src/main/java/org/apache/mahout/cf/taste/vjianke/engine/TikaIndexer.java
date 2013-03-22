package org.apache.mahout.cf.taste.vjianke.engine;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.mahout.cf.taste.vjianke.AzureStorageHelper;
import org.apache.mahout.cf.taste.vjianke.Datalayer;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-2-28
 * Time: 下午6:15
 * To change this template use File | Settings | File Templates.
 */
public class TikaIndexer {
    public static String INDEX_PATH = "examples/target/index";
    public static String CONTENT_FIELD = "contents";
    public static String CLIP_ID = "clipId";
    public static String CLIP_TITLE = "clipTitle";
    public TikaIndexer(){
    }

    public static void main(String[] args){

        boolean create = true;
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + INDEX_PATH + "'...");

            Directory dir = FSDirectory.open(new File(INDEX_PATH));
            Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_40);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);

            if (create) {
                //Creates a new index or overwrites an existing one.
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                //Creates a new index if one does not exist, otherwise it opens the index and documents will be appended.
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    static Document getDocument(Datalayer.ClipEntity entity,InputStream is) throws Exception {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, entity.id);
        Parser parser = new HtmlParser();
        ContentHandler handler = new BodyContentHandler(10*1024*1024);
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        try {
            parser.parse(is, handler, metadata,
                    new ParseContext());
        } finally {
            is.close();
        }
        Document doc = new Document();
        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize
        // the field into separate words and don't index term frequency
        // or positional information:
        Field pathField = new StringField(CLIP_ID, entity.id, Field.Store.YES);
        doc.add(pathField);

        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
        doc.add(new VecTextField(CONTENT_FIELD, handler.toString(),Field.Store.NO));
        doc.add(new VecTextField(CLIP_TITLE,entity.title, Field.Store.YES));
        return doc;
    }

    static void indexDocs(IndexWriter writer){
        Datalayer layer = new Datalayer();
        AzureStorageHelper helper = new AzureStorageHelper();
        helper.init();
        List<Datalayer.ClipEntity> clipEntityList =
                layer.getClips(
                        Integer.toString(700),ContentBasedRecommender.bDebug,
                        ContentBasedRecommender.bIncrement);

        for(Datalayer.ClipEntity entity:clipEntityList){
            try {
                System.out.print(entity.id + " ");
                ByteArrayOutputStream outputStream = helper.getClipBlob(entity.id);
                Document doc = getDocument(entity, new ByteArrayInputStream(outputStream.toByteArray()));
                writer.addDocument(doc);
            } catch (StorageException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
