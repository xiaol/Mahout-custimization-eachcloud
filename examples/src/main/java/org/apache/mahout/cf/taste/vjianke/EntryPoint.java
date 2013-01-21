package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-21
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class EntryPoint {
    public static void main(String[] args) throws Exception{

        VjiankeRecommend recommend = new VjiankeRecommend();
        FastByIDMap<PreferenceArray> prefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<UUID> users = new ArrayList<UUID>();

        recommend.init(prefsMap, users);
        String uuid = "07221718-b190-4536-8191-a0410029de34";
        List<RecommendedItem> recommendedItemList =
                recommend.recommend(uuid, prefsMap, users, 12);

        List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();
        Date date = new Date();
        for(RecommendedItem item : recommendedItemList ){
            RecommendClipEntity clipEntity = new RecommendClipEntity(
                    uuid,Long.toString(date.getTime()),Long.toString(item.getItemID(),36).toUpperCase());
            recommendClipEntityList.add(clipEntity);
        }

        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();
        azureStorageHelper.uploadToAzureTable("RecommendClipEntity",recommendClipEntityList);
    }
}
