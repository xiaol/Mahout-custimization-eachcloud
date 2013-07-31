package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-7-31
 * Time: 下午3:00
 * To change this template use File | Settings | File Templates.
 */
public class RecommendUserEntity extends TableServiceEntity {
    public RecommendUserEntity(){}

    public RecommendUserEntity(String uuid, String recommendUser){
        this.partitionKey = uuid;
        this.rowKey = recommendUser;
    }

    String UserScreenName;


    public String getUserScreenName() {
        return UserScreenName;
    }

    public void setUserScreenName(String userScreenName) {
        UserScreenName = userScreenName;
    }

    public String getProfileImageUrl() {
        return ProfileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        ProfileImageUrl = profileImageUrl;
    }

    String ProfileImageUrl;
}
