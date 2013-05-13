package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-29
 * Time: 下午2:51
 * To change this template use File | Settings | File Templates.
 */
public class BoardBasedRecommendEntryPoint {
    public static void main(String[] args) throws Exception{
        BoardBasedRecommendEntryPoint entryPoint = new BoardBasedRecommendEntryPoint();
        entryPoint.process();
    }

    public void process(){
        FastByIDMap<PreferenceArray> prefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<String> boardIds = new ArrayList<String>();
        ArrayList<String> clipIds = new ArrayList<String>();
        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();

        Datalayer datalayer = new Datalayer();
        datalayer.getBoardRelationByClips(prefsMap, boardIds,clipIds);
        DataModel model = new GenericDataModel(prefsMap);
        BoardBasedRecommend recommend = null;
        BoardBasedRecommend recommend1 = null;
        try {
            ItemSimilarity similarity =
                new LogLikelihoodSimilarity(model);
            recommend = new BoardBasedRecommend(model,similarity);
            ItemSimilarity similarity1 = new PearsonCorrelationSimilarity(model);
            recommend1 = new BoardBasedRecommend(model,similarity1);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        if(recommend == null){
            System.out.println("Recommender init failed.");
            return;
        }
        for(String uuid: IntrestBasedRecommendEntryPoint.mates){

            List<String> itsboards = datalayer.queryCreatedBoards(uuid);
            System.out.println("user: "+ uuid);
            for(String because: itsboards){
                List<RecommendBoardEntity> recommendBoardEntities
                        = new ArrayList<RecommendBoardEntity>();
                List<RecommendedItem> items = null;
                try {
                    items = recommend.mostSimilarItems(boardIds.indexOf(because), 2);
                } catch (TasteException e) {
                    e.printStackTrace();
                }
                for(RecommendedItem item:items){
                    String recommendBoardId = boardIds.get((int) item.getItemID());
                    System.out.println("Because: "+because+
                            " recommend "+ recommendBoardId+" :"+ item.getValue());
                    RecommendBoardEntity recommendBoardEntity = new RecommendBoardEntity(because,recommendBoardId);
                    recommendBoardEntities.add(recommendBoardEntity);
                }
                if(items.isEmpty()){
                    System.out.println("Because: "+because+ " but not at all");
                }

                List<RecommendedItem> items1 = null;
                try {
                    items1 = recommend.mostSimilarItems(boardIds.indexOf(because), 2);
                } catch (TasteException e) {
                    e.printStackTrace();
                }
                for(RecommendedItem item:items1){
                    String recommendBoardId = boardIds.get((int) item.getItemID());
                    System.out.println("Because: "+because+
                            " recommend1 "+ recommendBoardId+" :"+ item.getValue());
                    RecommendBoardEntity recommendBoardEntity = new RecommendBoardEntity(because,recommendBoardId);
                    recommendBoardEntities.add(recommendBoardEntity);
                }
                if(items1.isEmpty()){
                    System.out.println("Because: "+because+ " but not at all");
                }
                //azureStorageHelper.uploadToAzureTable("RecommendBoardEntity",recommendBoardEntities);
            }
        }
        prefsMap.clear();
        boardIds.clear();
    }


    public void processByUser(){
        FastByIDMap<PreferenceArray> prefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<String> boardIds = new ArrayList<String>();
        ArrayList<String> users = new ArrayList<String>();
        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();
        Datalayer datalayer = new Datalayer();
        datalayer.getBoardRelationsByUser(prefsMap, boardIds, users);
        DataModel model = new GenericDataModel(prefsMap);
        ItemSimilarity similarity =
                new LogLikelihoodSimilarity(model);

        BoardBasedRecommend recommend = new BoardBasedRecommend(model, similarity);

        for(String uuid: IntrestBasedRecommendEntryPoint.mates){

            List<String> itsboards = datalayer.queryCreatedBoards(uuid);
            System.out.println("user: "+ uuid);
            for(String because: itsboards){
                //List<RecommendedItem> items = recommend.recommendedBecause(users.indexOf(uuid.toUpperCase()), boardIds.indexOf(because), 2);
                List<RecommendedItem> items = null;
                try {
                    items = recommend.mostSimilarItems(boardIds.indexOf(because),2);
                } catch (TasteException e) {
                    e.printStackTrace();
                }
                for(RecommendedItem item:items){
                    System.out.println("Because: "+because+
                            " recommend "+boardIds.get((int)item.getItemID())+" :"+ item.getValue());
                }

            }
        }

        prefsMap.clear();
        boardIds.clear();
    }
}
