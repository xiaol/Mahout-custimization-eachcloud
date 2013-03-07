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

    public static void main(String[] args){
        try{
            IndexReader reader = DirectoryReader.open(
                    FSDirectory.open(new File(TikaIndexer.INDEX_PATH)));

            int docsCount = reader.maxDoc();
            DocVector[] docs = new DocVector[docsCount];
            for (int i =0; i < docsCount; i++) {
                Map<String,Double> terms = getTermFrequencies(reader,i);
                if(terms == null){
                    docs[i] = new DocVector(new HashMap<String, Double>());
                    continue;
                }
                docs[i] = new DocVector(terms);
            }
            for(DocVector docVector:docs){
                docVector.initVec(docsCount);
                for(Map.Entry<String, Integer> term: termsTable.entrySet()){
                    docVector.setEntry(term.getKey());
                }
                //docVector.normalize();
            }

            for(int i =0; i < docsCount-1; i++){
                for(int j = i+1; j < docsCount; j++) {
                    double cosim = getCosineSimilarity(docs[i], docs[j]);
                    docs[i].vectorSim.setEntry(j,cosim);
                }
            }

            for(int i=0; i< docsCount; i++){
                int mostDoc = docs[i].vectorSim.getMaxIndex();
                System.out.println(reader.document(i).get("clipId")+
                        ": --> "+ reader.document(mostDoc).get("clipId") +
                        ": "+ docs[i].vectorSim.getMaxValue());
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static double getCosineSimilarity(DocVector d1, DocVector d2) {
        return (d1.vector.dotProduct(d2.vector)) /
                (d1.vector.getNorm() * d2.vector.getNorm()+1);
    }

    static class DocVector {
        public Map<String,Double> terms;
        public SparseRealVector vector;
        public SparseRealVector vectorSim;

        public DocVector(Map<String,Double> terms) {
            this.terms = terms;
        }

        public void initVec(int docsCount){
            this.vector = new OpenMapRealVector(termsTable.size());
            this.vectorSim = new OpenMapRealVector(docsCount);
        }

        public void setEntry(String term) {
            int pos = (Integer) termsTable.get(term);
            if (terms.containsKey(term)) {

                vector.setEntry(pos, (double) terms.get(term));
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

    static Map<String, Double> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, TikaIndexer.CONTENT_FIELD);
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
            int docFreq = reader.docFreq(new Term(TikaIndexer.CONTENT_FIELD,termsEnum.term()));
            double score = freq*Math.log(reader.numDocs()/(double)docFreq+1);
            frequencies.put(term, score);
            if(!termsTable.containsKey(term))
                termsTable.put(term, termsTable.size());
            if(docFreq != 1)
                System.out.print(term+": "+score+" " + docFreq +" | ");
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
            int docFreq = reader.docFreq(new Term(TikaIndexer.CONTENT_FIELD,termsEnum.term()));
            double score = freq*Math.log(reader.numDocs()/(double)docFreq+1);
            frequencies.put(term, score);
            if(!termsTable.containsKey(term))
                termsTable.put(term, termsTable.size());
            if(docFreq != 1)
                System.out.print(term+": "+score+" " + docFreq +" | ");
        }
        return frequencies;
    }
}
