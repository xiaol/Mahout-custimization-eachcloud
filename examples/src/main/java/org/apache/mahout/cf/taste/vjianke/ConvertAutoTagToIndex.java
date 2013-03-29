package org.apache.mahout.cf.taste.vjianke;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.mahout.cf.taste.vjianke.engine.IntrestGenerator;

import java.lang.reflect.Type;
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

    }

    public void convert(Datalayer layer, AzureStorageHelper helper){
            List<Datalayer.ClipEntity> clipEntities =
                    layer.getClips(Integer.toString(50), false, false);
            List<ClipTagEntity> clipTagEntities = new ArrayList<ClipTagEntity>();
            for(Datalayer.ClipEntity entity:clipEntities){
                AzureStorageHelper.TagEntity tagEntity =
                        helper.retrieveTagEntity(entity.id, "-", "AutoTag");
                Gson gs = new Gson();
                Type listType = new TypeToken<List<IntrestGenerator.Tag>>() {}.getType();
                if(tagEntity == null)
                    continue;
                List<IntrestGenerator.Tag> tags = gs.fromJson(tagEntity.getTags(), listType);
                for(IntrestGenerator.Tag tag:tags){
                    ClipTagEntity clipTagEntity = new ClipTagEntity();
                    clipTagEntity.ClipId = entity.id;
                    clipTagEntity.Tag = tag.getKey();
                    clipTagEntity.OwnerGuid = entity.user_id;
                }
            }

    }

    public class ClipTagEntity{
        String ClipId;
        String Tag;
        String OwnerGuid;
        String BoardId;
        Float Weight;
        String Timestamp;
    }
}
