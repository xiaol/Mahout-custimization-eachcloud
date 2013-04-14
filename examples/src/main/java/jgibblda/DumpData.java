package jgibblda;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.mahout.cf.taste.vjianke.engine.TermVectorBasedSimilarity;
import org.apache.mahout.cf.taste.vjianke.engine.TikaIndexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-13
 * Time: 上午11:43
 * To change this template use File | Settings | File Templates.
 */
public class DumpData {
    public static void main(String args[]){

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(
                    FSDirectory.open(new File(TikaIndexer.INDEX_PATH)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    TikaIndexer.INDEX_PATH + "/dataset.words")));

            int docsCount = reader.maxDoc();
            System.out.println("MaxDoc: "+ reader.maxDoc()+ " NumDoc:" + reader.numDocs());
            int startDocId =0;
            ArrayList<String> docDatas = new ArrayList<String>();
            for (; startDocId < docsCount; startDocId++) {
                System.out.println("Document id: " +startDocId);
                Date start = new Date();
                Map<String, Double> terms =
                        TermVectorBasedSimilarity.getTermRank(reader,startDocId,TikaIndexer.CONTENT_FIELD);
                if(terms == null)
                    continue;

                String content = "";
                for(Map.Entry<String, Double> term:terms.entrySet()){
                    content = content + term.getKey() +" ";
                }
                docDatas.add(content);

                if(startDocId%1000 == 0){
                    Date end = new Date();
                    System.out.println((end.getTime() - start.getTime())/60000.0d + " mins to dump map");
                }
            }
            reader.close();
            LDADataset.writeDataSet(bw,docDatas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
