package org.apache.mahout.cf.taste.vjianke.engine;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.mahout.cf.taste.vjianke.AzureStorageHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.apache.mahout.cf.taste.vjianke.engine.TermVectorBasedSimilarity.getTermRank;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-12
 * Time: 上午10:35
 * To change this template use File | Settings | File Templates.
 */
public class ContentBasedRecommender {
    static boolean bDebug = true;

    public static void main(String[] args) throws Exception {
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
            parallelProducer(i, reader, suggestedClipEntities, helper);
            Date end = new Date();
            if(i%1000 == 0)
                System.out.println((end.getTime() - start.getTime())/60000.0d + " mins to build rank map");
        }

        reader.close();
    }

    static void process(IndexSearcher searcher, Query query){

    }

    static void parallelProducer(int docId, IndexReader reader,
                                 List<SuggestedClipEntity> suggestedClipEntities,
                                 AzureStorageHelper helper){

        Map<String,Double> terms = null;
        double sim = 0.0d;
        int docsCount = reader.maxDoc();
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_40);

        int numberOfHits = 200;
        TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfHits, true);

        QueryParser qp = new QueryParser(
                Version.LUCENE_41, TikaIndexer.CONTENT_FIELD, analyzer);
        qp.setDefaultOperator(QueryParser.Operator.AND);


        //phraseQuery(terms,searcher);
        //moreLikeThisQuery(terms,searcher,analyzer);
        //termQuery(terms,searcher);
        booleanQuery(docId,reader,searcher,helper,suggestedClipEntities);
    }

    static void phraseQuery(Map<String,Double> terms, IndexSearcher searcher){
        PhraseQuery query = new PhraseQuery();

        for(Map.Entry<String, Double> word:terms.entrySet()){
            query.add(new Term(TikaIndexer.CONTENT_FIELD,word.getKey()));
        }
        TopDocs matches;
        try {
            matches = searcher.search(query,1);
            System.out.println(matches.scoreDocs.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void booleanQuery(int docId, IndexReader reader,
                             IndexSearcher searcher,
                             AzureStorageHelper helper,
                             List<SuggestedClipEntity> suggestedClipEntities){
        Map<String,Double> terms = null;
        float factor = 1.0f;
        BooleanQuery query = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(65536);
        String srcId = "";
        try {
            srcId = reader.document(docId).get(TikaIndexer.CLIP_ID);
            System.out.println("Get clip "+ srcId + " rank");
            terms = getTermRank(reader, docId, TikaIndexer.CONTENT_FIELD);
            if(terms == null){

            }else{
                for(Map.Entry<String, Double> word:terms.entrySet()){
                    TermQuery tq = new TermQuery(new Term(
                            TikaIndexer.CONTENT_FIELD,word.getKey()));
                    tq.setBoost(Float.parseFloat(word.getValue().toString()));
                    query.add(tq, BooleanClause.Occur.SHOULD);
                }
            }

            terms = getTermRank(reader, docId, TikaIndexer.CLIP_TITLE);
            if(terms == null){

            }else{
                for(Map.Entry<String, Double> word:terms.entrySet()){
                    TermQuery tq = new TermQuery(new Term(
                            TikaIndexer.CLIP_TITLE,word.getKey()));
                    tq.setBoost(Float.parseFloat(word.getValue().toString())*factor);
                    query.add(tq, BooleanClause.Occur.SHOULD);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TopDocs matches;
        try {
            matches = searcher.search(query,100);
            System.out.println(matches.totalHits);
            int count = 0;
            for(ScoreDoc scoreDoc:matches.scoreDocs){
                if(count > 5 )
                    break;
                if(scoreDoc.doc == 2147483647)
                    continue;

                String destId =  reader.document(
                        scoreDoc.doc).get(TikaIndexer.CLIP_ID);
                if(srcId.equals(destId))
                    continue;

                System.out.println(destId + ": " + scoreDoc.score);
                TermVectorBasedSimilarity.process(srcId,String.format("%03d",count),
                        destId,(double)scoreDoc.score,helper,
                        suggestedClipEntities);
                count++;
            }
            if(!bDebug)
                helper.uploadToAzureTable("SuggestedClipByContent",suggestedClipEntities);
            suggestedClipEntities.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void termQuery(Map<String,Double> terms, IndexSearcher searcher){
        TopDocs matches;
        TermQuery query = new TermQuery(
                new Term(TikaIndexer.CONTENT_FIELD,"中国"));
        try {
            matches = searcher.search(query,100);
            System.out.println(matches.totalHits);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void moreLikeThisQuery(Map<String,Double> terms,
                                  IndexSearcher searcher, Analyzer analyzer){

        String content = "";
        for(Map.Entry<String, Double> word:terms.entrySet()){
            content = content + " " + word.getKey();
        }
        String[] fields = { TikaIndexer.CONTENT_FIELD,TikaIndexer.CLIP_TITLE};
        MoreLikeThisQuery query = new MoreLikeThisQuery(content,fields,analyzer,TikaIndexer.CONTENT_FIELD);
        //query.setMinTermFrequency(1);
        //query.setMaxQueryTerms(5);
        //query.setMinDocFreq(1);

        TopDocs matches;
        //searcher.setSimilarity(new DefaultSimilarity());
        try {
            matches = searcher.search(query,100);
            System.out.println(matches.totalHits);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}