package org.apache.mahout.cf.taste.vjianke;

import org.json.simple.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-22
 * Time: 上午10:56
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator {

    public static void main(String[] args){
        Datalayer layer = new Datalayer();
        JSONArray activeUsers = layer.getActiveUsers(2);
        String userId = "3bdd4051-9a3c-49f5-bdc4-a144001d420c";
        float sum = 0f;
        int count = 0;
        for(Object actvieUser:activeUsers){
            StringBuilder sb = new StringBuilder((String)actvieUser);
            sb.insert(8,"-").insert(13,"-").insert(18,"-").insert(23,"-");
            //String userId = UUID.fromString(sb.toString()).toString().toUpperCase();
            userId = userId.toUpperCase();
            Evaluator evaluator = new Evaluator();
            float readRate = evaluator.evaluate(userId.toUpperCase());
            if(readRate == -1f)
                continue;
            count++;
            sum += readRate;
        }
        float result = sum/count;
        System.out.println("Total : "+ result);
    }

    public List<RecommendClipEntity> getRecentRecommendItems(String userId){
        AzureStorageHelper helper = new AzureStorageHelper();
        helper.init();
        List<RecommendClipEntity> recommendClipEntities =
                helper.retrieveByPartitionKey("RecommendClipEntity",userId.replace("-",""));
        return recommendClipEntities;
    }

    public List<String> getRecentReadHistory(String userId,String startTime,String endTime){
        Datalayer layer = new Datalayer();
        List<String> result = layer.getReadHistory(userId,true,startTime,endTime);
        return result;
    }

    public float evaluate(String userId){
        List<RecommendClipEntity> recommendItems = getRecentRecommendItems(userId);
        if(recommendItems.isEmpty())
            return -1f;
        Date startDate = recommendItems.get(0).getTimestamp();

        SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd");

       /* String dateStringToParse = "6-01-2013";
// Parse the text version of the date.
// We have to perform the parse method in a
// try-catch construct in case dateStringToParse
// does not contain a date in the format we are expecting.
        Date endDate = new Date();
        try {
            endDate = bartDateFormat.parse(dateStringToParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
          */
        List<String> readItems = getRecentReadHistory(userId,bartDateFormat.format(startDate),"2013-06-01");
        System.out.print("Read Items count: "+ readItems.size()+" | ");
        boolean bResult = readItems.retainAll(recommendItems);

        float readRate = readItems.size()/(recommendItems.size()+1);
        System.out.println(userId+":"+readRate);
        return readRate;
    }
}
