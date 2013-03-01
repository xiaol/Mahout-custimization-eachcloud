package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-26
 * Time: 下午12:57
 * To change this template use File | Settings | File Templates.
 */
public class IntrestBasedRecommendEntryPoint {
    public static List<String> version = Arrays.asList("1.0v");
    public static boolean bSwitch = false;
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
                    "b30bb7e0-0722-4bca-85c2-a146006abf0a",     // aoliw
                    "27e6b768-de93-446a-8637-a12a00dfcb89",     // monica
                    "a90c77a2-767c-47fc-b7d9-a101005d58ba",     // vivi
                    "9d293f0f-2bb1-43b7-80cd-b3caed577e1e",     // chris
                    "a6490629-e92f-4edd-9aa0-9f7f007b4f46",     // jacky
                    "b5d3b26b-876b-4827-a904-d68130639d82",     // abely
                    "96c04d86-c4a4-487a-ba6d-a0e400299937"      // paul
            );

    public static List<String> mates2 =
            Arrays.asList(

            );

    static class BoardCachedEntity{
        FastByIDMap<FastIDSet> prefsIDSet;
        ArrayList<UUID> users;
    }

    public static void main(String[] args) throws Exception{
        Timestamp _ts = Timestamp.valueOf("2011-12-03 23:23:23");
        Timestamp _tsEnd = Timestamp.valueOf("2013-01-23 23:23:23");
        int count = 0;

        Map<String,BoardCachedEntity> cachedEntityMap = new HashMap<String, BoardCachedEntity>();

        FastByIDMap<PreferenceArray> prefsMap = null;
        ArrayList<UUID> users = null;
        FastByIDMap<FastIDSet> prefsIDSet;
        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();
        Datalayer datalayer = new Datalayer();

        FastByIDMap<PreferenceArray> boardPrefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<String> boardIds = new ArrayList<String>();
        ArrayList<String> boardUsers = new ArrayList<String>();
        BoardBasedRecommend boardBasedRecommend = null;
        if(bSwitch){
            datalayer.fetchBoardRelationsData(boardPrefsMap,boardIds,boardUsers);
            DataModel genericDataModel = new GenericDataModel(prefsMap);
            ItemSimilarity itemSimilarity =
                    new LogLikelihoodSimilarity(genericDataModel);
            boardBasedRecommend =
                    new BoardBasedRecommend(genericDataModel, itemSimilarity);
        }

        for(String uuid: mates){
            List<String> boards = datalayer.querySubscription(uuid);
            count++;
            //List<Datalayer.BoardRelated> relatedBoards = datalayer.queryRelatedBoards(uuid);
            System.out.println("users:" + uuid + "  "+ count);
            RecommendBalancer balancer = new RecommendBalancer(boards.size());
            List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();
            for(final String board:boards){
                //System.out.println("board: http://vjianke.com/board/"+board.replace("-","") +".clip");
                if(!cachedEntityMap.containsKey(board)){
                    prefsMap =  new FastByIDMap<PreferenceArray>();
                    users = new ArrayList<UUID>();
                    List<String> clipIds = datalayer.queryClipByBoard(new ArrayList<String>(){{ add(board); }});
                    if(clipIds.isEmpty())
                        continue;
                    datalayer.fetchData(prefsMap, users, clipIds);
                    BoardCachedEntity cachedEntity = new BoardCachedEntity();
                    prefsIDSet = GenericBooleanPrefDataModel.toDataMap(prefsMap);
                    cachedEntity.prefsIDSet = prefsIDSet;
                    cachedEntity.users = users;
                    cachedEntityMap.put(board,cachedEntity);
                }else{
                    System.out.println("hit board: "+board.replace("-","") +".clip");
                    prefsIDSet = cachedEntityMap.get(board).prefsIDSet;
                    users = cachedEntityMap.get(board).users;
                }
                DataModel model = new GenericBooleanPrefDataModel(prefsIDSet);

                UserSimilarity similarity =
                        new LogLikelihoodSimilarity(model);
                UserNeighborhood neighborhood =
                        new NearestNUserNeighborhood(users.size(), similarity, model);
                IntrestBasedRecommend recommend = new IntrestBasedRecommend(model, neighborhood, similarity);

                RecommendBalancer.BalanceResult balanceResult = balancer.balance(0, null);
                List<RecommendClipEntity> results = proceed(uuid, recommend,
                        prefsIDSet, users, azureStorageHelper, _ts, _tsEnd, balanceResult.howMany,"");
                for(RecommendClipEntity entity:results){
                    recommendClipEntityList.add(entity);
                }


            }

            if(recommendClipEntityList.isEmpty()){
                //System.out.println("no recommend clip found totally.");
            }else{

                //azureStorageHelper.uploadToAzureTable("RecommendClipEntity",recommendClipEntityList);
            }
            recommendClipEntityList.clear();

            if(bSwitch){
                List<String> createdBoards = datalayer.queryCreatedBoards(uuid);
                for(String board:createdBoards){
                    List<RecommendedItem> items = boardBasedRecommend.mostSimilarItems(boardIds.indexOf(board),1);
                    for(RecommendedItem item:items){
                        final String recommendBoard =  boardIds.get((int)item.getItemID());
                        //System.out.println("Because: "+board+
                                //" recommend "+recommendBoard +" :"+ item.getValue());
                        String prefix =  "Because: "+board+
                                " recommend "+recommendBoard +" :"+ item.getValue()+" ";
                        List<String> clipIds = datalayer.queryClipByBoard(new ArrayList<String>(){{ add(recommendBoard); }});
                        if(clipIds.isEmpty())
                            continue;

                        datalayer.fetchData(prefsMap, users, clipIds);
                        prefsIDSet = GenericBooleanPrefDataModel.toDataMap(prefsMap);
                        DataModel model = new GenericBooleanPrefDataModel(prefsIDSet);

                        UserSimilarity similarity =
                                new LogLikelihoodSimilarity(model);
                        UserNeighborhood neighborhood =
                                new NearestNUserNeighborhood(users.size(), similarity, model);
                        IntrestBasedRecommend recommend = new IntrestBasedRecommend(model, neighborhood, similarity);

                        List<RecommendClipEntity> results = proceed(uuid, recommend,
                                prefsIDSet, users, azureStorageHelper, _ts, _tsEnd, 1, prefix);
                        for(RecommendClipEntity entity:results){
                            recommendClipEntityList.add(entity);
                        }
                        prefsMap.clear();
                        users.clear();
                    }

                }
                if(recommendClipEntityList.isEmpty()){
                    //System.out.println("no recommend clip found totally.");
                }else{
                    //azureStorageHelper.uploadToAzureTable("RecommendClipEntity",recommendClipEntityList);
                }
                recommendClipEntityList.clear();
            }
        }

    }

    public static List<RecommendClipEntity> proceed(String uuid,
                               IntrestBasedRecommend recommend,
                               FastByIDMap<FastIDSet> prefsIDSet,
                               ArrayList<UUID> users,
                               AzureStorageHelper azureStorageHelper,
                               Timestamp _ts,
                               Timestamp _tsEnd, int howMany, String prefix) throws Exception {

        List<Long> neighborhoodUsers= new ArrayList<Long>();
        List<RecommendedItem> recommendedItemList =
                recommend.recommend(uuid, users, howMany, neighborhoodUsers);

        List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();

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
            String rowKey = version.get(version.size()-1) +"|"+ time + "|i|"+ recommendedItemList.indexOf(item);

            RecommendClipEntity clipEntity = new RecommendClipEntity(
                    uuidWithoutDash,
                    rowKey);

            AzureStorageHelper.FeedClipEntity feedClipEntity =
                    azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

            if(feedClipEntity == null){
                //System.out.println("Can't retrieve Clip: " +clipId);
                continue;
            }

            Datalayer.UserEntity userEntity = datalayer.Query(users.get((int) mate).toString());
            clipEntity.setRecommendStrategy("user-based:log-likelyhood");
            clipEntity.setRecommendContext("feedhome");
            clipEntity.setBase36(clipId);
            clipEntity.setAction("");
            clipEntity.setSender(uuidWithoutDash);

            String strSource= prefix;
            for(int i =0; i< arraySourceUser.size();i++){
                Datalayer.UserEntity entity = datalayer.Query(users.get(arraySourceUser.get(i).intValue()).toString());
                strSource += arraySourceUserInfluence.get(i)+":"+entity.getUser_screen_name()+" | ";
            }
            //System.out.println(strSource);
            clipEntity.setSenderName(strSource);
            clipEntity.setSenderImage(userEntity.getProfile_image_url());
            clipEntity.setSenderLink("/home/" + uuidWithoutDash + ".clip");
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

            recommendClipEntityList.add(clipEntity);
            arraySourceUser.clear();
            mapSourceUserInfluence.clear();
        }

        if(recommendClipEntityList.isEmpty()){
            //System.out.println("no recommend clip found.");
            return new ArrayList<RecommendClipEntity>();
        }else{
            return recommendClipEntityList;
        }
    }
}
