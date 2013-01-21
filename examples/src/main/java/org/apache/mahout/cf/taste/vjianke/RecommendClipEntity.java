package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-21
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */
public class RecommendClipEntity extends TableServiceEntity {
    public RecommendClipEntity(String uuid, String timeStamp, String base36){
        this.partitionKey = uuid;
        this.rowKey = timeStamp;
        this.base36 = base36;
    }

    String base36;

    public String getRecommendStrategy() {
        return RecommendStrategy;
    }

    public void setRecommendStrategy(String recommendStrategy) {
        RecommendStrategy = recommendStrategy;
    }

    String RecommendStrategy;

    public String getRecommendContext() {
        return RecommendContext;
    }

    public void setRecommendContext(String recommendContext) {
        RecommendContext = recommendContext;
    }

    String RecommendContext;


}
