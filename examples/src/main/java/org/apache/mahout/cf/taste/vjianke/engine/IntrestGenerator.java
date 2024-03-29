package org.apache.mahout.cf.taste.vjianke.engine;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.mahout.cf.taste.vjianke.AzureStorageHelper;
import org.apache.mahout.cf.taste.vjianke.Datalayer;
import org.apache.mahout.cf.taste.vjianke.IntrestBasedRecommendEntryPoint;
import org.apache.mahout.cf.taste.vjianke.Utils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-25
 * Time: 下午3:07
 * To change this template use File | Settings | File Templates.
 */
public class IntrestGenerator {

    public static void main(String[] args) throws Exception{
        Datalayer layer = new Datalayer();
        AzureStorageHelper helper = new AzureStorageHelper();
        helper.init();
        IntrestGenerator generator = new IntrestGenerator();
        //generator.getTagFromWeibo(IntrestBasedRecommendEntryPoint.mates.get(2),layer);
        org.json.simple.JSONArray activeUsers = layer.getActiveUsers(1);
        List<ChannelEntity> channelEntities = new ArrayList<ChannelEntity>();
        int count = 0;
        for(Object actvieUser:activeUsers){
            StringBuilder sb = new StringBuilder((String)actvieUser);

            sb.insert(8,"-").insert(13,"-").insert(18,"-").insert(23,"-");
            String userId = UUID.fromString(sb.toString()).toString().toUpperCase();
            System.out.println(userId+": "+count);
            Map<Integer,Integer> channelMaps = generator.getClipChannelFreq(layer,userId);
            ArrayList<String> channels = new ArrayList<String>();
            ArrayList<String> readCounts = new ArrayList<String>();

            for(Map.Entry<Integer, Integer> channelEntry:channelMaps.entrySet()){
                channels.add(Integer.toString(channelEntry.getKey()));
                readCounts.add(Integer.toString(channelEntry.getValue()));
            }

            String strChannels =  Utils.arrayToString(channels.toArray(new String[]{}),",");
            String strReadCounts = Utils.arrayToString(readCounts.toArray(new String[]{}),",");
            Date date = new Date();
            ChannelEntity channelEntity = new ChannelEntity(userId,date.toString());
            channelEntity.setChannels(strChannels.toString());
            channelEntity.setChannelReadCounts(strReadCounts);
            channelEntities.add(channelEntity);
            helper.deleteByPartitionKey("ChannelEntity",userId);
            helper.uploadToAzureTable("ChannelEntity",channelEntities);
            channelEntities.clear();
            count++;
        }

    }

    public void getTagFromReadHistory(Datalayer layer, AzureStorageHelper helper){
        for(String uuid:IntrestBasedRecommendEntryPoint.mates){
            List<String> clipIds = layer.getReadHistory(uuid,false,"","");
            Hashtable<String,Float> tagsMap = new Hashtable<String, Float>();
            for(String clipId:clipIds){
                AzureStorageHelper.TagEntity tagEntity =
                        helper.retrieveTagEntity(clipId, "-", "AutoTag");
                Gson gs = new Gson();
                Type listType = new TypeToken<List<Tag>>() {}.getType();
                if(tagEntity == null)
                    continue;
                List<Tag> tags = gs.fromJson(tagEntity.getTags(), listType);
                for(Tag tag:tags){
                    if(tagsMap.containsKey(tag.getKey())){
                        tagsMap.put(tag.getKey(),tag.getValue()+tag.getValue());
                    }else{
                        tagsMap.put(tag.getKey(),tag.getValue());
                    }
                }
            }
            //sortValue(tagsMap);
            tagsMap.clear();
        }
    }

    public Map<Integer, Integer> getClipChannelFreq(Datalayer layer, String userId){
        List<String> clipIds = layer.getReadHistory(userId,false,"","");
        Map<Integer,Integer> channelMaps = layer.getChannelByClips(clipIds);
        Map<Integer,Integer> sortedMap = Utils.sortMapByValueInt(channelMaps);
        return sortedMap;
    }

    public ArrayList<String> getTagFromWeibo(String userId,Datalayer layer){
        List<Datalayer.WeiboTag> weiboTags = layer.getWeiboTag(userId);
        //System.out.println(weiboTags);
        Hashtable<String,Integer> tagsMap = new Hashtable<String, Integer>();
        ArrayList<String> results = new ArrayList<String>();
        Gson gs = new Gson();

        Type listType = new TypeToken<List<WeiboTag>>() {}.getType();
        for(Datalayer.WeiboTag weiboTag:weiboTags){
            try {
                if(weiboTag.getUserTag() == null || weiboTag.getUserTag().equals(""))
                    continue;
                JSONArray ja = new JSONArray(weiboTag.getUserTag());
                for(int i = 0; i< ja.length();i++){
                    JSONObject jojo = ja.getJSONObject(i);
                    Iterator it = jojo.keys();
                    int count = 0;
                    String tag= "";int weight;
                    while(it.hasNext()){
                        String key = (String)it.next();
                        if(count ==0) {
                            tag = (String)jojo.get(key);
                        }
                        if(count ==1)  {
                            weight = (Integer)jojo.get(key);
                            tagsMap.put(tag,weight);
                            results.add(tag);
                        }
                        count ++;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            WeiboFavTag favTags = gs.fromJson(weiboTag.getUserFavTag(),WeiboFavTag.class);
            for(WeiboSubTag tag:favTags.getTags()){
                tagsMap.put(tag.tag,Integer.parseInt(tag.count));
                results.add(tag.tag);
            }
        }
        return results;

    }


    class WeiboTag{
        String name;
        String weight;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }
    }

    class WeiboFavTag{
        List<WeiboSubTag> tags;
        int total_number;

        public List<WeiboSubTag> getTags() {
            return tags;
        }

        public void setTags(List<WeiboSubTag> tags) {
            this.tags = tags;
        }

        public int getTotal_number() {
            return total_number;
        }

        public void setTotal_number(int total_number) {
            this.total_number = total_number;
        }
    }

    class WeiboSubTag{
        int id;
        String tag;
        String count;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }
    }

    public class Tag{
        String Key;
        Float Value;

        public String getKey() {
            return Key;
        }

        public void setKey(String key) {
            this.Key = key;
        }

        public Float getValue() {
            return Value;
        }

        public void setValue(Float value) {
            this.Value = value;
        }
    }

    public static void sortValue(Hashtable<?, Integer> t){

        //Transfer as List and sort it
        ArrayList<Map.Entry<?, Integer>> l = new ArrayList(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>(){

            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }});

        //System.out.println(l);
    }
}
