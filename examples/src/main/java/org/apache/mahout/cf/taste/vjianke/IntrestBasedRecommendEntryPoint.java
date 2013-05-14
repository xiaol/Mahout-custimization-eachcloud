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
import org.apache.mahout.cf.taste.vjianke.engine.ContentBasedRecommender;
import org.apache.mahout.cf.taste.vjianke.engine.IntrestGenerator;
import org.apache.mahout.cf.taste.vjianke.engine.TikaIndexer;
import org.json.simple.JSONArray;

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
    public static List<String> version = Arrays.asList("1.1v");
    public static boolean bSwitch = false;
    public static boolean bDebug = false;
    public static List<String> mates =
            Arrays.asList(
                    "07221718-b190-4536-8191-a0410029de34",     // ivanl
                    "8b6257c9-5055-48fe-babc-9eec005a94d2",     // kevin
                    "a544a317-2359-4ae5-a716-9eea002c0db1",     // nick
                    "8d81322a-d2f3-4ffa-b782-9f2b0169672e",
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

    public static String RECOMMEND_BY_USER = "你的好友看过同类剪报";
    public static String RECOMMEND_BY_SUBSCRIPTION = "你错过的订阅剪报";//"你错过的来自";
    public static String RECOMMEND_BY_SUBSCRIPTION_SUFFIX = "的剪报";
    public static String RECOMMEND_BY_SINA = "和你在微博的喜好相关";
    public static String RECOMMEND_BY_BEHAVIOR_PREFIX = "因为";
    public static String RECOMMEND_BY_BEHAVIOR_SUFFIX = "过同类剪报";
    public static String RECOMMEND_BY_BOARD_PREFIX = "和你的订阅专辑";
    public static String RECOMMEND_BY_CREATED_BOARD_PREFIX= "和你的创建专辑";
    public static String RECOMMEND_BY_BOARD_SUFFIX = "相关的";

    public static List<String> mates2 =
            Arrays.asList(

            );

    static class BoardCachedEntity{
        FastByIDMap<FastIDSet> prefsIDSet;
        ArrayList<UUID> users;
    }

    public static void main(String[] args) throws Exception{
        Timestamp _ts = Timestamp.valueOf("2013-04-16 23:23:23");
        Timestamp _tsEnd = Timestamp.valueOf("2013-06-01 23:23:23");
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

        FastByIDMap<PreferenceArray> localPrefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<UUID> localUsers = new ArrayList<UUID>();

        UserBasedAnalyzer userBasedAnalyzer = new UserBasedAnalyzer();
        userBasedAnalyzer.init(localPrefsMap, localUsers, _ts, _tsEnd);
        FastByIDMap<FastIDSet> localprefsIDSet = GenericBooleanPrefDataModel.toDataMap(localPrefsMap);

        ContentBasedRecommender contentBasedRecommender = new ContentBasedRecommender();
        Hashtable<String, Datalayer.UserEntity> userEntities = datalayer.QueryUsers();
        JSONArray activeUsers = datalayer.getActiveUsers(14);
        for(Object actvieUser:activeUsers){
        //for(Map.Entry<String, Datalayer.UserEntity> userEntity:userEntities.entrySet()){
        //for(String mate:mates){
            //String userId = userEntity.getKey();
            //String userId = IntrestBasedRecommendEntryPoint.mates.get(18).toUpperCase();
            StringBuilder sb = new StringBuilder((String)actvieUser);
            sb.insert(8,"-").insert(13,"-").insert(18,"-").insert(23,"-");
            String userId = UUID.fromString(sb.toString()).toString().toUpperCase();
            //String userId = "07221718-B190-4536-8191-A0410029DE34";
            //String userId = mate.toUpperCase();
            List<String> boards = datalayer.querySubscription(userId);
            count++;
            //List<Datalayer.BoardRelated> relatedBoards = datalayer.queryRelatedBoards(uuid);
            System.out.println("users:" + userId + "  "+ count);
            RecommendBalancer balancer = new RecommendBalancer(boards.size());
            List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();

            //FastByIDMap<PreferenceArray> tempLocalPrefsMap = new FastByIDMap<PreferenceArray>();
            //for(Map.Entry<Long, PreferenceArray> entity:localPrefsMap.entrySet()){
                //tempLocalPrefsMap.put(entity.getKey(),entity.getValue());
            //}

            FastIDSet itemsId = userBasedAnalyzer.getPreferenceByReadHistory( userId, localUsers);
            UUID currentUUID = UUID.fromString(userId);
            int userIndex = localUsers.indexOf(currentUUID);
            FastIDSet swapItemId = localprefsIDSet.get(userIndex);
            localprefsIDSet.put(userIndex,itemsId);
            DataModel localModel = new GenericBooleanPrefDataModel(localprefsIDSet);

            UserSimilarity localSimilarity = new LogLikelihoodSimilarity(localModel);
            UserNeighborhood localNeighborhood =
                    new NearestNUserNeighborhood(localUsers.size(), localSimilarity, localModel);
            IntrestBasedRecommend localRecommend = new IntrestBasedRecommend(
                    localModel, localNeighborhood, localSimilarity);
            List<RecommendClipEntity> userBasedResults = proceed(userId, userEntities, localRecommend,
                    localprefsIDSet, localUsers, azureStorageHelper, _ts, _tsEnd, 7, "Fullscope ",RECOMMEND_BY_USER);
            for(RecommendClipEntity entity:userBasedResults){
                recommendClipEntityList.add(entity);
            }
            if(swapItemId == null) {
                localprefsIDSet.remove(userIndex);
            }else{
                localprefsIDSet.put(userIndex,swapItemId);
            }


            List<Datalayer.ClipEntity> recentClipByUser =
                    datalayer.getRecentClipByUser(userId,7,datalayer.baseTimestamp);
            int recommendRecentCount = 2;
            if(boards.size() < 2){
                recommendRecentCount = 3;
            }

            for(Datalayer.ClipEntity recentClipEntity:recentClipByUser){
                List<ContentBasedRecommender.RelativeClipInfo> relativeClipInfoList =
                        contentBasedRecommender.recomendByClip(recentClipEntity.id,recommendRecentCount,datalayer,userId);
                for(ContentBasedRecommender.RelativeClipInfo relativeClipInfo:relativeClipInfoList){
                    Date date = new Date();
                    long time =  date.getTime();
                    String rowKey = version.get(version.size()-1) +"|"+ time + "|c|"+ relativeClipInfo.index;
                    String uuidWithoutDash = userId.replace("-","");
                    String strSource = relativeClipInfo.srcId;
                    RecommendClipEntity clipEntity = generateClipEntity(uuidWithoutDash, rowKey, azureStorageHelper,
                            relativeClipInfo.destId, userEntities.get(userId), strSource,"content-based:vsm","recentClip",
                            RECOMMEND_BY_BEHAVIOR_PREFIX+"发布"+RECOMMEND_BY_BEHAVIOR_SUFFIX);
                    if(clipEntity == null) {
                        continue;
                    }
                    recommendClipEntityList.add(clipEntity);
                }
            }

            int boardRecount = 0;
            for(final String board:boards){
                //System.out.println("board: http://vjianke.com/board/"+board.replace("-","") +".clip");
                if(!cachedEntityMap.containsKey(board)){
                    prefsMap =  new FastByIDMap<PreferenceArray>();
                    users = new ArrayList<UUID>();
                    List<String> clipIds = datalayer.queryClipByBoard(new ArrayList<String>(){{ add(board); }});
                    if(clipIds.isEmpty())
                        continue;
                    datalayer.getPreferenceByClick(prefsMap, users, clipIds);
                    datalayer.getPreferenceByUserClip(prefsMap,userId,users,clipIds);
                    BoardCachedEntity cachedEntity = new BoardCachedEntity();
                    prefsIDSet = GenericBooleanPrefDataModel.toDataMap(prefsMap);
                    cachedEntity.prefsIDSet = prefsIDSet;
                    cachedEntity.users = users;
                    cachedEntityMap.put(board,cachedEntity);
                }else{
                    //System.out.println("hit board: "+board.replace("-","") +".clip");
                    prefsIDSet = cachedEntityMap.get(board).prefsIDSet;
                    users = cachedEntityMap.get(board).users;
                }
                DataModel model = new GenericBooleanPrefDataModel(prefsIDSet);

                if(users.size() < 1)
                    continue;

                UserSimilarity similarity =
                        new LogLikelihoodSimilarity(model);
                UserNeighborhood neighborhood =
                        new NearestNUserNeighborhood(users.size(), similarity, model);
                IntrestBasedRecommend recommend = new IntrestBasedRecommend(model, neighborhood, similarity);

                RecommendBalancer.BalanceResult balanceResult = balancer.balance(0, null);
                List<RecommendClipEntity> results = proceed(userId, userEntities,recommend,
                        prefsIDSet, users, azureStorageHelper, _ts, _tsEnd, balanceResult.howMany,"",RECOMMEND_BY_SUBSCRIPTION);
                for(RecommendClipEntity entity:results){
                    recommendClipEntityList.add(entity);
                    boardRecount++;
                }
                if(boardRecount > 20)
                    break;
            }

            //System.out.println("Start Weibo Recommend");
            IntrestGenerator intrestGenerator = new IntrestGenerator();
            Hashtable<String,Integer> weiboTagsTable = intrestGenerator.getTagFromWeibo(
                    userId,datalayer);
            ContentBasedRecommender recommender = new ContentBasedRecommender();
            for(Map.Entry<String, Integer> weiboTag:weiboTagsTable.entrySet()){
                weiboTag.getKey();
                Hashtable<String,Double> maps = new Hashtable<String, Double>();
                maps.put(weiboTag.getKey(),1.0d);
                List<RecommendClipEntity> entities =
                        proceed( userEntities.get(userId), maps, recommender, azureStorageHelper, datalayer);
                System.out.println(weiboTag.getKey() + " Sina recommended: " + entities.size());
                for(RecommendClipEntity recommendClipEntity:entities){
                    System.out.print(recommendClipEntity.getBase36()+" ");
                    recommendClipEntityList.add(recommendClipEntity);
                }
            }

            List<String> createdBoards = datalayer.queryCreatedBoards(userId);
            String uuidWithoutDash = userEntities.get(userId).getUuid().replace("-", "");
            for(String board:createdBoards){
                List<RecommendBoardEntity> recommendBoardEntities =
                        azureStorageHelper.retrieveBoardByPartitionKey("RecommendBoardEntity",board);

                for(RecommendBoardEntity recommendBoardEntity:recommendBoardEntities){
                    List<String> relatedBoardClipIds =
                            datalayer.getClipByBoard(recommendBoardEntity.getRowKey(),true,userId);
                    for(String relatedBoardClipId:relatedBoardClipIds){
                        Date date = new Date();
                        long time =  date.getTime();
                        String rowKey = version.get(version.size()-1) +"|"+ time + "|u|"+ count;
                        RecommendClipEntity recommendClipEntity = generateClipEntity(uuidWithoutDash,
                                rowKey,azureStorageHelper,relatedBoardClipId,userEntities.get(userId),"",
                                "item-based:log-likelyhood","board",RECOMMEND_BY_CREATED_BOARD_PREFIX+
                                "《"+recommendBoardEntity.getName() +"》"+RECOMMEND_BY_BOARD_SUFFIX);
                        recommendClipEntityList.add(recommendClipEntity);
                    }
                }
            }

            if(recommendClipEntityList.isEmpty()){
                //System.out.println("no recommend clip found totally.");
            }else{
                if(!bDebug){
                    List<RecommendClipEntity> deletedRecommendClipEntity =
                            azureStorageHelper.deleteByPartitionKey("RecommendClipEntity",userId.replace("-",""));

                    //System.out.println("deleted clip: "+ deletedRecommendClipEntity.size());

                    List<RecommendClipEntity> newRecommendClipEntities =
                            new ArrayList<RecommendClipEntity>();
                    List<RecommendClipEntity> oldRecommendClipEntities =
                            new ArrayList<RecommendClipEntity>();
                    for(RecommendClipEntity entity:recommendClipEntityList){
                        String clipId = entity.getBase36();
                        boolean bDeleted = false;
                        for(RecommendClipEntity deletedClipEntity:deletedRecommendClipEntity){
                            if(deletedClipEntity.getBase36().equals(clipId)){
                                //System.out.println("Deleted:" + deletedClipEntity.getBase36() +
                                        //"  Clip:" + clipId);
                                bDeleted = true;
                                break;
                            }
                        }
                        if(bDeleted){
                            oldRecommendClipEntities.add(entity);
                        }
                        else{
                            newRecommendClipEntities.add(entity);
                        }
                    }

                    recommendClipEntityList.clear();
                    Date date = new Date();
                    long time =  date.getTime();


                    for(RecommendClipEntity entity:newRecommendClipEntities){
                        String rowKey = version.get(version.size()-1) +"|"+ time +
                                "|i|"+ newRecommendClipEntities.indexOf(entity);
                        entity.setRowKey(rowKey);
                        recommendClipEntityList.add(entity);
                    }

                    for(RecommendClipEntity entity:oldRecommendClipEntities){
                        String rowKey = version.get(version.size()-1) +"|"+ time +
                                "|i|"+ (newRecommendClipEntities.size()+oldRecommendClipEntities.indexOf(entity));
                        entity.setRowKey(rowKey);
                        recommendClipEntityList.add(entity);
                    }

                    List<String> unLikeClipIds = datalayer.getUnlikeClip(userId);
                    List<RecommendClipEntity> unLikeClipInRecommend = new ArrayList<RecommendClipEntity>();
                    for(String unLikeClipId:unLikeClipIds){
                        for(RecommendClipEntity restRecommendClipEntity:recommendClipEntityList){
                            if(restRecommendClipEntity.getBase36().equals(unLikeClipId)){
                                unLikeClipInRecommend.add(restRecommendClipEntity);
                            }
                        }
                    }
                    for(RecommendClipEntity needRemoveEntity:unLikeClipInRecommend){
                        recommendClipEntityList.remove(needRemoveEntity);
                    }

                    System.out.println("new: "+newRecommendClipEntities.size() +
                            " | old: "+oldRecommendClipEntities.size() +
                            " | unlike total: "+ unLikeClipIds.size()+
                            " | unlike in recommend: "+unLikeClipInRecommend.size());
                    //for(RecommendClipEntity recommendClip:recommendClipEntityList){
                        //System.out.println("PartitionKey: "+recommendClip.getPartitionKey()+" RowKey: "+ recommendClip.getRowKey());
                    //}
                    if(newRecommendClipEntities.isEmpty()){
                        System.out.println("No news.");
                    }


                    azureStorageHelper.uploadToAzureTable(
                            "RecommendClipEntity",recommendClipEntityList);
                }
            }
            recommendClipEntityList.clear();

            if(bSwitch){

            }

        }

    }

    public static List<RecommendClipEntity> proceed(Datalayer.UserEntity userEntity,
                                                    Hashtable<String,Double> tagsTable,
                                                    ContentBasedRecommender recommender,
                                                    AzureStorageHelper helper,
                                                    Datalayer layer){
        List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();
        Hashtable<String,Float> recommendResult = recommender.recommendByTerms(
                tagsTable, userEntity.getUuid(),layer, TikaIndexer.INDEX_PATH);
        String uuidWithoutDash = userEntity.getUuid().replace("-", "");

        int count = 0;
        for(Map.Entry<String, Float> result:recommendResult.entrySet()){
            String clipId = result.getKey();
            Date date = new Date();
            long time =  date.getTime();
            String rowKey = version.get(version.size()-1) +"|"+ time + "|c|"+ count;
            String strSource = tagsTable.toString();

            RecommendClipEntity clipEntity = generateClipEntity(uuidWithoutDash, rowKey, helper,
                    clipId, userEntity, strSource,"content-based:vsm","sina",RECOMMEND_BY_SINA);
            if(clipEntity == null) {
                System.out.println("Sina Generate clip failed");
                continue;
            }
            recommendClipEntityList.add(clipEntity);
            count++;
        }

        return recommendClipEntityList;
    }

    public static List<RecommendClipEntity> proceed(String uuid, Hashtable<String, Datalayer.UserEntity> userEntityHashtable,
                               IntrestBasedRecommend recommend,
                               FastByIDMap<FastIDSet> prefsIDSet,
                               ArrayList<UUID> users,
                               AzureStorageHelper azureStorageHelper,
                               Timestamp _ts,
                               Timestamp _tsEnd, int howMany, String prefix,
                               String recommendReason) throws Exception {

        List<Long> neighborhoodUsers= new ArrayList<Long>();
        if(bDebug && uuid.toUpperCase().equals("818D0F1A-8295-473F-91F9-A0F900546786")) {
            System.out.println("Null PointerException");
        }
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
            Datalayer.UserEntity userEntity = userEntityHashtable.get(users.get((int) mate).toString().toUpperCase());
            if(userEntity == null){
                if(bDebug)
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

            RecommendClipEntity clipEntity = generateClipEntity(uuidWithoutDash,
                    rowKey,azureStorageHelper,clipId,userEntity,strSource,
                    "user-based:log-likelyhood","feedhome",recommendReason);
            if(clipEntity == null) {
                arraySourceUser.clear();
                mapSourceUserInfluence.clear();
                continue;
            }
            recommendClipEntityList.add(clipEntity);
        }

        if(recommendClipEntityList.isEmpty()){
            //System.out.println("no recommend clip found.");
            return new ArrayList<RecommendClipEntity>();
        }else{
            return recommendClipEntityList;
        }
    }

    public static RecommendClipEntity generateClipEntity(String uuidWithoutDash,
                                                          String rowKey,
                                                          AzureStorageHelper azureStorageHelper,
                                                          String clipId,
                                                          Datalayer.UserEntity userEntity,
                                                          String strSource,
                                                          String recommendStrategy,
                                                          String recommendContext,
                                                          String recommendReason
                                                          ){
        RecommendClipEntity clipEntity = new RecommendClipEntity(
                uuidWithoutDash,
                rowKey);

        AzureStorageHelper.FeedClipEntity feedClipEntity =
                azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

        if(feedClipEntity == null){
            //System.out.println("Can't retrieve Clip: " +clipId);
            return null;
        }

        clipEntity.setRecommendStrategy(recommendStrategy);
        clipEntity.setRecommendContext(recommendContext);
        clipEntity.setBase36(clipId);
        clipEntity.setAction("");
        clipEntity.setSender(uuidWithoutDash);


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
        clipEntity.setorigsite(feedClipEntity.getorigsite());
        if(feedClipEntity.getorigtitle() == null || feedClipEntity.getorigtitle().equals("")){
            clipEntity.setorigtitle(feedClipEntity.gettitle());
        }else{
            clipEntity.setorigtitle(feedClipEntity.getorigtitle());
        }
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

        clipEntity.setRecommendReason(recommendReason);

        return clipEntity;
    }
}
