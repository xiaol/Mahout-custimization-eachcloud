package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
        ItemSimilarity similarity =
            new LogLikelihoodSimilarity(model);
        recommend = new BoardBasedRecommend(model,similarity);
        if(recommend == null){
            System.out.println("Recommender init failed.");
            return;
        }
        Hashtable<String, Datalayer.BoardEntity> boardMap = datalayer.getBoards("", false, false);
        for (Map.Entry<String, Datalayer.BoardEntity> because : boardMap.entrySet()) {
            Datalayer.BoardEntity boardEntity = because.getValue();
            List<RecommendBoardEntity> recommendBoardEntities
                    = new ArrayList<RecommendBoardEntity>();
            List<RecommendedItem> items = null;
            try {
                if(!boardIds.contains(because.getKey()))
                    continue;
                items = recommend.mostSimilarItems(boardIds.indexOf(because.getKey()), 7);
            } catch (TasteException e) {
                e.printStackTrace();
                continue;
            }
            for (RecommendedItem item : items) {
                if(item == null)
                    continue;
                String recommendBoardId = boardIds.get((int) item.getItemID());
                Datalayer.BoardEntity recommendEntity = boardMap.get(recommendBoardId);
                if(recommendEntity == null)
                    continue;
                boolean isPrivate = recommendEntity.isPrivate;
                if (isPrivate)
                    continue;
                System.out.println("Because: " + because.getKey() +
                        " recommend " + recommendBoardId + " :" + item.getValue());
                RecommendBoardEntity recommendBoardEntity = new RecommendBoardEntity(because.getKey(), recommendBoardId);
                recommendBoardEntity.setName(boardEntity.name);
                recommendBoardEntity.setRank(String.valueOf(item.getValue()));
                recommendBoardEntity.setIsPrivate(recommendEntity.isPrivate);
                recommendBoardEntity.setRecomendBoardName(recommendEntity.name);
                recommendBoardEntities.add(recommendBoardEntity);
            }
            if (items.isEmpty() || recommendBoardEntities.isEmpty()) {
                System.out.println("Because: " + because.getKey() + " but not at all");
                continue;
            }

            azureStorageHelper.deleteByPartitionKey("RecommendBoardEntity",because.getKey());
            azureStorageHelper.uploadToAzureTable("RecommendBoardEntity",recommendBoardEntities);
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

            List<String> itsboards = datalayer.queryCreatedBoards(uuid,0);
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
