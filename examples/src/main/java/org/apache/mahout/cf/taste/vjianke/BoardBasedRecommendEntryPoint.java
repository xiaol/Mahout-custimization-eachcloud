package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

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

        FastByIDMap<PreferenceArray> prefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<String> boardIds = new ArrayList<String>();
        ArrayList<String> users = new ArrayList<String>();
        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();
        Datalayer datalayer = new Datalayer();
        datalayer.fetchBoardRelationsData(prefsMap,boardIds,users);
        DataModel model = new GenericDataModel(prefsMap);
        ItemSimilarity similarity =
                new LogLikelihoodSimilarity(model);

        BoardBasedRecommend recommend = new BoardBasedRecommend(model, similarity);

        for(String uuid: IntrestBasedRecommendEntryPoint.mates){

            List<String> itsboards = datalayer.queryCreatedBoards(uuid);
            System.out.println("user: "+ uuid);
            for(String because: itsboards){
                //List<RecommendedItem> items = recommend.recommendedBecause(users.indexOf(uuid.toUpperCase()), boardIds.indexOf(because), 2);
                List<RecommendedItem> items = recommend.mostSimilarItems(boardIds.indexOf(because),2);
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
