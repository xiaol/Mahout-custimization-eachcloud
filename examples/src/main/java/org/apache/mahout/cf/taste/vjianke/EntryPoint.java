package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
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

        AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
        azureStorageHelper.init();

        recommend.init(prefsMap, users);
        String uuid = "07221718-b190-4536-8191-a0410029de34";
        List<Long> neighborhoodUsers= new ArrayList<Long>();
        List<RecommendedItem> recommendedItemList =
                recommend.recommend(uuid, prefsMap, users, 12, neighborhoodUsers);

        List<RecommendClipEntity> recommendClipEntityList = new ArrayList<RecommendClipEntity>();
        Date date = new Date();
        JDBCHelper jdbcHelper = new JDBCHelper();

        for(RecommendedItem item : recommendedItemList ){
            String clipId = Long.toString(item.getItemID(),36).toUpperCase();
            long sourceUser = -1;
            for(long neighborhoodUser : neighborhoodUsers){
                boolean hasPref = FastIDSet.class.cast(prefsMap.get(neighborhoodUser)).contains(item.getItemID());
                if(hasPref) {
                    sourceUser = neighborhoodUser;
                    break;
                }
            }
            String uuidWithoutDash = uuid.replace("-","");
            if(sourceUser == -1)
                throw new Exception("No Way");

            RecommendClipEntity clipEntity = new RecommendClipEntity(
                    uuid,
                    Long.toString(date.getTime())+recommendedItemList.indexOf(item));

            AzureStorageHelper.FeedClipEntity feedClipEntity =
                    azureStorageHelper.retrieveFeedClipEntity(clipId, "-","ClipEntity");

            JDBCHelper.UserEntity userEntity = jdbcHelper.Query(users.get((int)sourceUser).toString());
            clipEntity.setRecommendStrategy("user-based:log-likelyhood");
            clipEntity.setRecommendContext("feedhome");
            clipEntity.setBase36(clipId);
            clipEntity.setAction("");
            clipEntity.setSender(uuidWithoutDash);
            clipEntity.setSenderName(userEntity.getUser_screen_name());
            clipEntity.setSenderImage(userEntity.getProfile_image_url());
            clipEntity.setSenderLink("/home/"+uuidWithoutDash+".clip");
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

        azureStorageHelper.uploadToAzureTable("RecommendClipEntity",recommendClipEntityList);
    }
}
