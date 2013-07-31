package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.json.simple.JSONArray;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-21
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class EntryPoint {

    public static List<String> mates =
            Arrays.asList(
                    "07221718-b190-4536-8191-a0410029de34",     // ivanl
                    "8b6257c9-5055-48fe-babc-9eec005a94d2",     // kevin
                    "a544a317-2359-4ae5-a716-9eea002c0db1",     // nick
                    "da6b1949-d998-4344-a0f6-a08a008ab49b",     // vera
                    "178676e2-c21d-4a95-b234-38c0b01c00d1",     // bertony
                    "08295a93-ac97-44db-a107-9fc000630d7c",     // farstar1
                    "6af0f808-dacf-4fac-adba-9e8500faf11c",     // farstar2
                    "80d47b58-7b97-4b36-9d3b-9fd20059d711",     // joy
                    "3bdd4051-9a3c-49f5-bdc4-a144001d420c",     // wilson
                    "80c0161a-34a4-4363-b9b7-9c513020e083",     // bruce
                    "fad2cc24-c259-4d81-ac60-e4bf18606378",     // zilong
                    "7de3bb1e-13aa-44ae-ab1e-a0d100995f63",     // michael
                    "b30bb7e0-0722-4bca-85c2-a146006abf0a",     // oaliw
                    "27e6b768-de93-446a-8637-a12a00dfcb89",     // monica
                    "a90c77a2-767c-47fc-b7d9-a101005d58ba",     // vivi
                    "9d293f0f-2bb1-43b7-80cd-b3caed577e1e"      // chris
            );

    public static List<String> mates2 =
            Arrays.asList(
                    "a6490629-e92f-4edd-9aa0-9f7f007b4f46",     // jacky
                    "b5d3b26b-876b-4827-a904-d68130639d82"      // abely
            );

    public static void main(String[] args) throws Exception{
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date endDate = new Date();
        Date startDate = calendar.getTime();
        Timestamp _ts = new Timestamp(startDate.getTime());
        Timestamp _tsEnd = new Timestamp(endDate.getTime());

        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();
        Datalayer datalayer = new Datalayer();

        FastByIDMap<PreferenceArray> localPrefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<UUID> localUsers = new ArrayList<UUID>();

        System.out.println("Start to get preference---");
        UserBasedAnalyzer userBasedAnalyzer = new UserBasedAnalyzer();
        userBasedAnalyzer.init(localPrefsMap, localUsers, _ts, _tsEnd);
        System.out.println("load preferences , parse to data map");
        FastByIDMap<FastIDSet> localprefsIDSet = GenericBooleanPrefDataModel.toDataMap(localPrefsMap);

        System.out.println("Start to query users---");
        Hashtable<String, Datalayer.UserEntity> userEntities = datalayer.QueryUsers();
        System.out.println("Get active users.");
        JSONArray activeUsers = datalayer.getActiveUsers(1);

        DataModel localModel = new GenericBooleanPrefDataModel(localprefsIDSet);

        UserSimilarity localSimilarity = new LogLikelihoodSimilarity(localModel);
        UserNeighborhood localNeighborhood =
                new NearestNUserNeighborhood(localUsers.size(), localSimilarity, localModel);
        IntrestBasedRecommend localRecommend = new IntrestBasedRecommend(
                localModel, localNeighborhood, localSimilarity);


        for(String uuid: mates2){
            List<RecommendUserEntity> userBasedResults = proceed(uuid, userEntities, localRecommend,
                    localprefsIDSet, localUsers, azureStorageHelper, _ts, _tsEnd, 2,
                    "Fullscope ",Collections.EMPTY_LIST, "推荐用户");

            if(userBasedResults.isEmpty()){
            }else{

                 azureStorageHelper.uploadToAzureTable(
                            "RecommendUserEntity",userBasedResults);

            }
        }
    }

    public static List<RecommendUserEntity> proceed(String uuid, Hashtable<String, Datalayer.UserEntity> userEntityHashtable,
                                                    IntrestBasedRecommend recommend,
                                                    FastByIDMap<FastIDSet> prefsIDSet,
                                                    ArrayList<UUID> users,
                                                    AzureStorageHelper azureStorageHelper,
                                                    Timestamp _ts,
                                                    Timestamp _tsEnd, int howMany, String prefix,List<String> exceptionItemIds,
                                                    String recommendReason) throws Exception {

        List<Long> neighborhoodUsers= new ArrayList<Long>();

        List<RecommendedItem> recommendedItemList =
                recommend.recommend(uuid, users, howMany, neighborhoodUsers,exceptionItemIds);

        List<RecommendUserEntity> recommendUserEntityList = new ArrayList<RecommendUserEntity>();
        Datalayer datalayer = new Datalayer();

        List<Long> arraySourceUser = new ArrayList<Long>();
        Map<Long,Double> mapSourceUserInfluence = new HashMap<Long, Double>();
        List<Double> arraySourceUserInfluence = new ArrayList<Double>();

        int userIndex = users.indexOf(UUID.fromString(uuid));

        for(RecommendedItem item : recommendedItemList ){
            String clipId = Long.toString(item.getItemID(),36).toUpperCase();
            long mate = -1;
            for(long neighborhoodUser : neighborhoodUsers){
                boolean hasPref = prefsIDSet.get(neighborhoodUser).contains(item.getItemID());
                if(hasPref) {
                    arraySourceUser.add(neighborhoodUser);
                    double value = recommend.getSimilarity().userSimilarity(neighborhoodUser,userIndex);
                    BigDecimal influence = new BigDecimal(value);
                    //System.out.println(users.get((int)neighborhoodUser).toString()+" to user's influence: " +influence.setScale(2,2));
                    mapSourceUserInfluence.put(neighborhoodUser, influence.setScale(2,2).doubleValue());
                    arraySourceUserInfluence.add(influence.setScale(2,2).doubleValue());
                }
                hasPref = false;
            }
            String uuidWithoutDash = uuid.replace("-","");
            if(mapSourceUserInfluence.isEmpty())
                throw new Exception("No Way");

            mate = arraySourceUser.get(0);

            Date date = new Date();
            long time =  date.getTime();
            String rowKey =  users.get((int) mate).toString().toUpperCase();
            Datalayer.UserEntity userEntity = userEntityHashtable.get(users.get((int) mate).toString().toUpperCase());
            if(userEntity == null){
                System.out.println("can't find user in hashtable");
                userEntity = datalayer.Query(users.get((int) mate).toString());

            }
            String strSource= prefix;
            for(int i =0; i< arraySourceUser.size();i++){
                Datalayer.UserEntity entity = userEntityHashtable.get(users.get(arraySourceUser.get(i).intValue()).toString().toUpperCase());
                if(entity == null)
                    entity = datalayer.Query(users.get(arraySourceUser.get(i).intValue()).toString());
                strSource += arraySourceUserInfluence.get(i)+":"+entity.getUser_screen_name()+" | ";
            }

            RecommendUserEntity recommendUserEntity = generateUserEntity(uuidWithoutDash,
                    rowKey, azureStorageHelper, clipId, userEntity, strSource,
                    "user-based:log-likelyhood", "feedhome", recommendReason);
            if(recommendUserEntity == null) {
                arraySourceUser.clear();
                mapSourceUserInfluence.clear();
                continue;
            }
            recommendUserEntityList.add(recommendUserEntity);
        }

        if(recommendUserEntityList.isEmpty()){
            //System.out.println("no recommend clip found.");
            return Collections.EMPTY_LIST;
        }else{
            return recommendUserEntityList;
        }
    }

    public static RecommendUserEntity generateUserEntity(String uuidWithoutDash,
                                                         String rowKey,
                                                         AzureStorageHelper azureStorageHelper,
                                                         String clipId,
                                                         Datalayer.UserEntity userEntity,
                                                         String strSource,
                                                         String recommendStrategy,
                                                         String recommendContext,
                                                         String recommendReason
    ){
        RecommendUserEntity recommendUserEntity = new RecommendUserEntity(
                uuidWithoutDash,
                rowKey);

        AzureStorageHelper.FeedClipEntity feedClipEntity =
                azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

        if(feedClipEntity == null){
            //System.out.println("Can't retrieve Clip: " +clipId);
            return null;
        }

        recommendUserEntity.setUserScreenName(userEntity.getUser_screen_name());
        recommendUserEntity.setProfileImageUrl(userEntity.getProfile_image_url());

        return recommendUserEntity;
    }
}
