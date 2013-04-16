package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

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
        Timestamp _ts = Timestamp.valueOf("2011-12-01 23:23:23");
        Timestamp _tsEnd = Timestamp.valueOf("2013-01-23 23:23:23");
        UserBasedAnalyzer analyzer = new UserBasedAnalyzer();
        FastByIDMap<PreferenceArray> prefsMap = new FastByIDMap<PreferenceArray>();
        ArrayList<UUID> users = new ArrayList<UUID>();
        analyzer.init(prefsMap, users, _ts, _tsEnd);
        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        FastByIDMap<FastIDSet> prefsIDSet = GenericBooleanPrefDataModel.toDataMap(prefsMap);

        azureStorageHelper.init();

          for(String uuid: mates2){
              proceed(uuid, analyzer, prefsIDSet, users, azureStorageHelper, _ts, _tsEnd);
          }

    }

    public static void proceed(String uuid,
                               UserBasedAnalyzer analyzer,
                               FastByIDMap<FastIDSet> prefsIDSet,
                               ArrayList<UUID> users,
                               AzureStorageHelper azureStorageHelper,
                               Timestamp _ts,
                               Timestamp _tsEnd) throws Exception {

        List<Long> neighborhoodUsers= new ArrayList<Long>();
        List<RecommendedItem> recommendedItemList =
                analyzer.recommend(uuid, prefsIDSet, users, 12, neighborhoodUsers);

        List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();

        Datalayer datalayer = new Datalayer();

        for(RecommendedItem item : recommendedItemList ){
            String clipId = Long.toString(item.getItemID(),36).toUpperCase();
            long sourceUser = -1;
            for(long neighborhoodUser : neighborhoodUsers){
                boolean hasPref = prefsIDSet.get(neighborhoodUser).contains(item.getItemID());
                if(hasPref) {
                    sourceUser = neighborhoodUser;
                    break;
                }
            }
            String uuidWithoutDash = uuid.replace("-","");
            if(sourceUser == -1)
                throw new Exception("No Way");

            RecommendClipEntity clipEntity = new RecommendClipEntity(
                    uuidWithoutDash,
                    Long.toString(_ts.getTime())+recommendedItemList.indexOf(item));

            AzureStorageHelper.FeedClipEntity feedClipEntity =
                    azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

            if(feedClipEntity == null){
                System.out.println("Can't retrieve Clip: " +clipId);
                continue;
            }

            Datalayer.UserEntity userEntity = datalayer.Query(users.get((int)sourceUser).toString());
            clipEntity.setRecommendStrategy("user-based:log-likelyhood");
            clipEntity.setRecommendContext("feedhome");
            clipEntity.setBase36(clipId);
            clipEntity.setAction("");
            clipEntity.setSender(uuidWithoutDash);
            clipEntity.setSenderName(userEntity.getUser_screen_name());
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
        }

        if(recommendClipEntityList.isEmpty()){
           System.out.println("no recommend clip found.");
        }else{
            azureStorageHelper.uploadToAzureTable("RecommendClipEntity",recommendClipEntityList);
        }
    }
}
