package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-3-9
 * Time: 上午9:39
 * To change this template use File | Settings | File Templates.
 */
public class RecommendBoardEntity extends TableServiceEntity {
    public RecommendBoardEntity(String boardId, String recommendBoardId){
        this.partitionKey = boardId;
        this.rowKey = recommendBoardId;
    }

    public String getRank() {
        return Rank;
    }

    public void setRank(String rank) {
        Rank = rank;
    }
    public String Rank;



}
