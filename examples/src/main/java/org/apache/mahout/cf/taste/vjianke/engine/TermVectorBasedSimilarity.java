package org.apache.mahout.cf.taste.vjianke.engine;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.google.common.collect.TreeMultimap;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.apache.commons.math3.linear.SparseRealVector;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.mahout.cf.taste.vjianke.AzureStorageHelper;


/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-4
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */
public class TermVectorBasedSimilarity {
    public static Hashtable<String,Integer> termsTable =
            new Hashtable<String, Integer>();
    public static Hashtable<String,Integer> termsDocFreq =
            new Hashtable<String, Integer>();

    public static boolean bDebug = false;

    public static void main(String[] args){
        try{
            IndexReader reader = DirectoryReader.open(
                    FSDirectory.open(new File(TikaIndexer.INDEX_PATH)));

            int docsCount = reader.maxDoc();
            DocVector[] docs = new DocVector[docsCount];
            for (int i =0; i < docsCount; i++) {
                Map<String,Integer> terms = getTermFrequencies(reader,i);
                if(terms == null){
                    docs[i] = new DocVector(new HashMap<String, Integer>());
                    continue;
                }
                docs[i] = new DocVector(terms);
            }
            for(DocVector docVector:docs){
                docVector.initVec(docsCount);
                for(Map.Entry<String, Integer> term: termsTable.entrySet()){
                    docVector.setEntry(term.getKey(), docsCount);
                }
                //docVector.normalize();
            }

            Date start = new Date();
            for(int i =0; i < docsCount-1; i++){
                for(int j = i+1; j < docsCount; j++) {
                    double cosim = getCosineSimilarity(docs[i], docs[j]);
                    if(docs[i].simap.containsKey(cosim)){
                        docs[i].simap.get(cosim).add(j);
                    }else{
                        List<Integer> docsIndexs = new ArrayList<Integer>();
                        docsIndexs.add(j);
                        docs[i].simap.put(cosim,docsIndexs);
                    }

                }
            }
            Date end = new Date();
            System.out.println((end.getTime() - start.getTime())/60000.0d + " mins to build rank map");

            AzureStorageHelper helper = new AzureStorageHelper();
            helper.init();
            List<SuggestedClipEntity> suggestedClipEntities =
                    new ArrayList<SuggestedClipEntity>();
            for(int i=0; i< docsCount; i++){
                int count = 0;
                for(Map.Entry<Double, List<Integer>> simEntity :
                        docs[i].simap.descendingMap().entrySet()) {
                    if(isNaN(simEntity.getKey()) || simEntity.getKey() > 0.9)
                        continue;
                    for(Integer docIndex:simEntity.getValue()) {
                        //System.out.println(reader.document(i).get("clipId")+
                                //": --> "+ reader.document(docIndex).get("clipId") +
                                //": "+ simEntity.getKey());
                        proceed(reader.document(i).get("clipId"),String.format("%03d",count),
                                reader.document(docIndex).get("clipId"),simEntity.getKey(),helper,
                                suggestedClipEntities);
                        count++;
                    }

                    if(count >= 3)
                        break;
                }
                if(!bDebug)
                    helper.uploadToAzureTable("SuggestedClipByContent",suggestedClipEntities);
                suggestedClipEntities.clear();
                if(i%10000 == 0)
                    System.out.println(i);
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static void parallelProducer(int docId ){

    }

    static void parallelComparer(int docId1, int docId2){

    }

    static double getCosineSimilarity(DocVector d1, DocVector d2) {
        return (d1.vector.dotProduct(d2.vector)) /
                (d1.vector.getNorm() * d2.vector.getNorm()+1);
    }

    static class DocVector {
        public Map<String,Integer> terms;
        public SparseRealVector vector;
        public TreeMap<Double, List<Integer>> simap;

        public DocVector(Map<String,Integer> terms) {
            this.terms = terms;
            this.simap = new TreeMap<Double, List<Integer>>();
        }

        public void initVec(int docsCount){
            this.vector = new OpenMapRealVector(termsTable.size());
        }

        public void setEntry(String term, int docCounts) {
            int pos = (Integer) termsTable.get(term);
            if (terms.containsKey(term)) {
                int docFreq = termsDocFreq.get(term);
                double score = terms.get(term)*Math.log(docCounts/(double)docFreq+1);
                vector.setEntry(pos, score );
            }else{
                vector.setEntry(pos, 0);
            }
        }

        public void normalize() {
            double sum = vector.getL1Norm();
            vector = (SparseRealVector) vector.mapDivide(sum);
        }

        public String toString() {
            RealVectorFormat formatter = new RealVectorFormat();
            return formatter.format(vector);
        }
    }

    static Map<String, Integer> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, TikaIndexer.CONTENT_FIELD);
        if(vector == null)
            return null;
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Integer> frequencies = new HashMap<String,Integer>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            if(term.length() < 2)
                continue;
            int freq = (int) termsEnum.totalTermFreq();

            frequencies.put(term, freq);
            if(!termsTable.containsKey(term)){
                termsTable.put(term, termsTable.size());
                termsDocFreq.put(term, 1);
            }else{
                termsDocFreq.put(term, termsDocFreq.get(term)+1);
            }
            //if(docFreq != 1)
                //System.out.print(term+": "+score+" " + docFreq +" | ");
        }

        vector = reader.getTermVector(docId, TikaIndexer.CLIP_TITLE);
        if(vector == null)
            return frequencies;
        termsEnum = vector.iterator(termsEnum);
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            if(term.length() < 2)
                continue;
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
            if(!termsTable.containsKey(term)){
                termsTable.put(term, termsTable.size());
                termsDocFreq.put(term, 1);
            }else{
                termsDocFreq.put(term, termsDocFreq.get(term)+1);
            }
            //if(docFreq != 1)
                //System.out.print(term+": "+score+" " + docFreq +" | ");
        }
        return frequencies;
    }

    public static void proceed(String clipId, String rowKey, String desClipId, Double rank,
                            AzureStorageHelper azureStorageHelper,
                            List<SuggestedClipEntity> suggestedClipEntities){


        AzureStorageHelper.FeedClipEntity feedClipEntity =
                azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

        if(feedClipEntity == null){
            //System.out.println("Can't retrieve Clip: " +clipId);
            return ;
        }
        SuggestedClipEntity clipEntity = new SuggestedClipEntity(clipId,rowKey);
        clipEntity.setBase36(desClipId);
        clipEntity.setAction("Suggest");
        clipEntity.setRank(Double.toString(rank));

        clipEntity.setSenderComment("");
        clipEntity.setcontentBrief(feedClipEntity.getcontentBrief());
        clipEntity.sethasUT(feedClipEntity.gethasUT());
        clipEntity.setcontentTitle(feedClipEntity.getcontentTitle());
        clipEntity.setheight(feedClipEntity.getheight());
        clipEntity.setwidth(feedClipEntity.getwidth());

        clipEntity.setorigheight(feedClipEntity.getorigheight());
        clipEntity.setorigsite(feedClipEntity.getorigheight());
        clipEntity.setorigtitle(feedClipEntity.getorigtitle());
        clipEntity.setorigurl(feedClipEntity.getorigurl());
        clipEntity.setorigwidth(feedClipEntity.getorigwidth());
        clipEntity.setsmallTitlePic(feedClipEntity.getsmallTitlePic());
        clipEntity.setsmallTPHeight(feedClipEntity.getsmallTPHeight());
        clipEntity.setsmallTPWidth(feedClipEntity.getsmallTPWidth());
        clipEntity.settitlePic(feedClipEntity.gettitlePic());
        clipEntity.settitlePicHeight(feedClipEntity.gettitlePicHeight());
        clipEntity.settitlePicWidth(feedClipEntity.gettitlePicWidth());
        clipEntity.settype(feedClipEntity.gettype());

        clipEntity.setuguid(feedClipEntity.getuguid());
        clipEntity.setuimage(feedClipEntity.getuimage());
        clipEntity.setuname(feedClipEntity.getuname());

        suggestedClipEntities.add(clipEntity);
    }

    static public boolean isNaN(double v) {
        return (v != v);
    }
}
