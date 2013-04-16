package org.apache.mahout.cf.taste.vjianke.engine;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-4-16
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public class ChannelEntity extends TableServiceEntity {
    public ChannelEntity(){}

    public ChannelEntity(String uuid, String timeStamp){
        this.partitionKey = uuid;
        this.rowKey = timeStamp;
    }

    public String getChannels() {
        return Channels;
    }

    public void setChannels(String channels) {
        Channels = channels;
    }

    String Channels;

    public String getChannelReadCounts() {
        return ChannelReadCounts;
    }

    public void setChannelReadCounts(String channelReadCounts) {
        ChannelReadCounts = channelReadCounts;
    }

    String ChannelReadCounts;

}
