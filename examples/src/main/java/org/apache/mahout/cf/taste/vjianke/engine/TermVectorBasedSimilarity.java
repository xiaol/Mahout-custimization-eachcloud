package org.apache.mahout.cf.taste.vjianke.engine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.RealVectorFormat;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.apache.lucene.document.Document;
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

    public static void main(String[] args){
        try{
            IndexReader reader = DirectoryReader.open(
                    FSDirectory.open(new File(TikaIndexer.INDEX_PATH)));
            int[] docIds = new int[] {0, 1, 2, 3};
            DocVector[] docs = new DocVector[docIds.length];
            int i = 0;
            for (int docId : docIds) {
                Map<String,Integer> terms = getTermFrequencies(reader,docId);
                docs[i] = new DocVector(terms);

                for (Map.Entry<String, Integer> term:terms.entrySet()){
                    docs[i].setEntry(term.getKey(),term.getValue());
                }
                docs[i].normalize();
                i++;
            }
            // now get similarity between doc[0] and doc[1]
            double cosim01 = getCosineSimilarity(docs[0], docs[1]);
            System.out.println("cosim(0,1)=" + cosim01);
            // between doc[0] and doc[2]
            double cosim02 = getCosineSimilarity(docs[0], docs[2]);
            System.out.println("cosim(0,2)=" + cosim02);
            // between doc[0] and doc[3]
            double cosim03 = getCosineSimilarity(docs[0], docs[3]);
            System.out.println("cosim(0,3)=" + cosim03);
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    static double getCosineSimilarity(DocVector d1, DocVector d2) {
        return (d1.vector.dotProduct(d2.vector)) /
                (d1.vector.getNorm() * d2.vector.getNorm());
    }

    static class DocVector {
        public Map<String,Integer> terms;
        public SparseRealVector vector;

        public DocVector(Map<String,Integer> terms) {
            this.terms = terms;
            this.vector = new OpenMapRealVector(terms.size());
        }

        public void setEntry(String term, int freq) {
            if (terms.containsKey(term)) {
                int pos = terms.get(term);
                vector.setEntry(pos, (double) freq);
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
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Integer> frequencies = new HashMap<String,Integer>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
        }
        return frequencies;
    }
}
