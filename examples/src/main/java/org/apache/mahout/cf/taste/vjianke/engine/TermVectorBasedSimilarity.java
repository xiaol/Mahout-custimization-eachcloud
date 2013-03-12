package org.apache.mahout.cf.taste.vjianke.engine;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    public static boolean bDebug = true;

    public static void main(String[] args){

        System.out.println("1.0v --->");
        try{
            IndexReader reader = DirectoryReader.open(
                    FSDirectory.open(new File(TikaIndexer.INDEX_PATH)));
            AzureStorageHelper helper = new AzureStorageHelper();
            helper.init();
            List<SuggestedClipEntity> suggestedClipEntities =
                    new ArrayList<SuggestedClipEntity>();

            int docsCount = reader.maxDoc();
            for (int i =0; i < docsCount; i++) {
                System.out.println("Document id: " +i);
                Date start = new Date();
                parallelProducer(i,reader,suggestedClipEntities,helper);
                Date end = new Date();
                System.out.println((end.getTime() - start.getTime())/60000.0d + " mins to build rank map");
            }

            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static void prepare(){

    }


    static void parallelProducer(int docId, IndexReader reader,
                                 List<SuggestedClipEntity> suggestedClipEntities,
                                 AzureStorageHelper helper){

        Map<String,Double> terms = null;
        double sim = 0.0d;
        int docsCount = reader.maxDoc();
        TreeMap<Double, List<Integer>> simap =
                new TreeMap<Double, List<Integer>>();
        try {
            System.out.println("Get term "+ docId+" rank");
            terms = getTermRank(reader, docId,TikaIndexer.CONTENT_FIELD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DocVector doc;
        if(terms == null){
            return;
        }else{
            doc = new DocVector(terms);
        }

        for (int i =0; i < docsCount; i++) {
            termsTable.clear();
            if(docId == i)
                continue;
            Map<String,Double> termsDest = null;
            try {
                System.out.println("Get term "+ i+" rank");
                termsDest = getTermRank(reader, i,TikaIndexer.CONTENT_FIELD);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Start ranking ");
            DocVector docDest;
            sim = 0.0d;
            if(termsDest == null){
                if(simap.containsKey(sim)){
                    simap.get(sim).add(i);
                }else{
                    List<Integer> docsIndexs = new ArrayList<Integer>();
                    docsIndexs.add(i);
                    simap.put(sim,docsIndexs);
                }
                continue;
            }else{
                docDest = new DocVector(termsDest);
            }

            for(Map.Entry<String, Double> term:terms.entrySet()){
                if(!termsTable.containsKey(term.getKey()))
                    termsTable.put(term.getKey(), termsTable.size());
            }

            for(Map.Entry<String, Double> term:termsDest.entrySet()){
                if(!termsTable.containsKey(term.getKey()))
                    termsTable.put(term.getKey(), termsTable.size());
            }

            doc.initVec();
            for(Map.Entry<String, Integer> term: termsTable.entrySet()){
                doc.setEntry(term.getKey());
            }

            docDest.initVec();
            for(Map.Entry<String, Integer> term: termsTable.entrySet()){
                docDest.setEntry(term.getKey());
            }

            sim = getCosineSimilarity(doc,docDest);
            if(simap.containsKey(sim)){
                simap.get(sim).add(i);
            }else{
                List<Integer> docsIndexs = new ArrayList<Integer>();
                docsIndexs.add(i);
                simap.put(sim,docsIndexs);
            }
            System.out.println("ranking: " + sim);
        }

        int count = 0;
        for(Map.Entry<Double, List<Integer>> simEntity :
                simap.descendingMap().entrySet()) {
            if(isNaN(simEntity.getKey()) || simEntity.getKey() > 0.9 || simEntity.getKey() == 0.0)
                continue;
            for(Integer docIndex:simEntity.getValue()) {
                try {
                    System.out.println(reader.document(docId).get("clipId")+
                            ": --> "+ reader.document(docIndex).get("clipId") +
                            ": "+ simEntity.getKey());

                    //process(reader.document(docId).get("clipId"),String.format("%03d",count),
                            //reader.document(docIndex).get("clipId"),simEntity.getKey(),helper,
                            //suggestedClipEntities);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count++;
            }

            if(count >= 3)
                break;
        }
        if(!bDebug)
            helper.uploadToAzureTable("SuggestedClipByContent",suggestedClipEntities);
        suggestedClipEntities.clear();
        if(docId%10000 == 0)
            System.out.println(docId);

    }

    static double getCosineSimilarity(DocVector d1, DocVector d2) {
        return (d1.vector.dotProduct(d2.vector)) /
                (d1.vector.getNorm() * d2.vector.getNorm()+1);
    }

    static class DocVector {
        public Map<String,Double> terms;
        public SparseRealVector vector;
        public TreeMap<Double, List<Integer>> simap;

        public DocVector(Map<String,Double> terms) {
            this.terms = terms;
            this.simap = new TreeMap<Double, List<Integer>>();
        }

        public void initVec(){
            this.vector = new OpenMapRealVector(termsTable.size());
        }

        public void setEntry(String term) {
            int pos = (Integer) termsTable.get(term);
            if (terms.containsKey(term)) {
                vector.setEntry(pos, terms.get(term));
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

    static Map<String, Double> getTermRank(IndexReader reader, int docId, String fieldName)
            throws IOException {
        Terms vector = reader.getTermVector(docId, fieldName);
        if(vector == null)
            return null;
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Double> frequencies = new HashMap<String,Double>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            if(term.length() < 2)
                continue;
            int freq = (int) termsEnum.totalTermFreq();
            //System.out.println("Get doc freq from reader");
            int docFreq = reader.docFreq(new Term(TikaIndexer.CONTENT_FIELD,termsEnum.term()));
            double score = freq*Math.log(reader.numDocs()/(double)docFreq+1);
            frequencies.put(term, score);

            //if(docFreq != 1)
                //System.out.print(term+": "+score+" " + docFreq +" | ");
        }
        return frequencies;
    }

    public static void process(String clipId, String rowKey, String desClipId, Double rank,
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
