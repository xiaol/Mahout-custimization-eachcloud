package org.apache.mahout.cf.taste.vjianke;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.mahout.cf.taste.vjianke.engine.IntrestGenerator;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-28
 * Time: 下午2:36
 * To change this template use File | Settings | File Templates.
 */
public class ConvertAutoTagToIndex {
    public static void main(String[] args) throws Exception{
        Datalayer layer = new Datalayer();
        AzureStorageHelper helper = new AzureStorageHelper();
        helper.init();
        ConvertAutoTagToIndex converter = new ConvertAutoTagToIndex();
        converter.convert(layer,helper);
    }

    public void convert(Datalayer layer, AzureStorageHelper helper){
        //TODO order by clip id , 越小越日期越近
            List<Datalayer.ClipEntity> clipEntities =
                    layer.getClips(Integer.toString(1), false, false,true,false);
            System.out.println("Total delta clips count:"+clipEntities.size());
            List<ClipTagEntity> clipTagEntities = new ArrayList<ClipTagEntity>();
        int count = 0;
            for(Datalayer.ClipEntity entity:clipEntities){
                //if(layer.isInTable(entity.id,"ClipTagEntity","ClipId")){
                    //continue;
                //}
                AzureStorageHelper.TagEntity tagEntity =
                        helper.retrieveTagEntity(entity.id, "-", "AutoTag");
                Gson gs = new Gson();
                Type listType = new TypeToken<List<IntrestGenerator.Tag>>() {}.getType();
                if(tagEntity == null)
                    continue;
                List<IntrestGenerator.Tag> tags = gs.fromJson(tagEntity.getTags(), listType);
                Date date = new Date(tagEntity.getTimestamp().getTime());
                String boardId = layer.getBoardByClip(entity.id);
                for(IntrestGenerator.Tag tag:tags){
                    ClipTagEntity clipTagEntity = new ClipTagEntity();
                    clipTagEntity.ClipId = entity.id;
                    clipTagEntity.Tag = tag.getKey();
                    clipTagEntity.OwnerGuid = entity.user_id;
                    clipTagEntity.BoardId = boardId;
                    if(clipTagEntity.BoardId.equals(""))
                        continue;
                    clipTagEntity.Weight = tag.getValue();
                    clipTagEntity.Timestamp = date;
                    clipTagEntities.add(clipTagEntity);
                }
                if(count % 400 == 0 && count != 0){
                    layer.addClipTagIndex(clipTagEntities);
                    System.out.println("Count: "+ count);
                    clipTagEntities.clear();
                }
                count++;
            }
        layer.addClipTagIndex(clipTagEntities);
    }
    public class ClipTagEntity{
        String ClipId;
        String Tag;
        String OwnerGuid;
        String BoardId;
        Float Weight;
        Date Timestamp;
    }
}
