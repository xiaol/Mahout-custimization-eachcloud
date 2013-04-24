package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-21
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */
public  class RecommendClipEntity extends TableServiceEntity {
    public RecommendClipEntity(){}

    public RecommendClipEntity(String uuid, String timeStamp){
        this.partitionKey = uuid;
        this.rowKey = timeStamp;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String Sender;

    public String getSenderName() {
        return SenderName;
    }

    public void setSenderName(String senderName) {
        SenderName = senderName;
    }

    public String getSenderLink() {
        return SenderLink;
    }

    public void setSenderLink(String senderLink) {
        SenderLink = senderLink;
    }

    public String getSenderImage() {
        return SenderImage;
    }

    public void setSenderImage(String senderImage) {
        SenderImage = senderImage;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    public String getBase36() {
        return Base36;
    }

    public void setBase36(String base36) {
        Base36 = base36;
    }

    public String getSenderComment() {
        return SenderComment;
    }

    public void setSenderComment(String senderComment) {
        SenderComment = senderComment;
    }

    public String getuguid() {
        return uguid;
    }

    public void setuguid(String uguid) {
        this.uguid = uguid;
    }

    public String getuname() {
        return uname;
    }

    public void setuname(String uname) {
        this.uname = uname;
    }

    public String getuimage() {
        return uimage;
    }

    public void setuimage(String uimage) {
        this.uimage = uimage;
    }

    public String getorigtitle() {
        return origtitle;
    }

    public void setorigtitle(String origtitle) {
        this.origtitle = origtitle;
    }

    public String getwidth() {
        return width;
    }

    public void setwidth(String width) {
        this.width = width;
    }

    public String getheight() {
        return height;
    }

    public void setheight(String height) {
        this.height = height;
    }

    public String gettype() {
        return type;
    }

    public void settype(String type) {
        this.type = type;
    }

    public String getorigwidth() {
        return origwidth;
    }

    public void setorigwidth(String origwidth) {
        this.origwidth = origwidth;
    }

    public String getorigheight() {
        return origheight;
    }

    public void setorigheight(String origheight) {
        this.origheight = origheight;
    }

    public String getorigurl() {
        return origurl;
    }

    public void setorigurl(String origurl) {
        this.origurl = origurl;
    }

    public String getorigsite() {
        return origsite;
    }

    public void setorigsite(String origsite) {
        this.origsite = origsite;
    }

    public String getcontentTitle() {
        return contentTitle;
    }

    public void setcontentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getcontentBrief() {
        return contentBrief;
    }

    public void setcontentBrief(String contentBrief) {
        this.contentBrief = contentBrief;
    }

    public String gettitlePic() {
        return titlePic;
    }

    public void settitlePic(String titlePic) {
        this.titlePic = titlePic;
    }

    public String gettitlePicWidth() {
        return titlePicWidth;
    }

    public void settitlePicWidth(String titlePicWidth) {
        this.titlePicWidth = titlePicWidth;
    }

    public String gettitlePicHeight() {
        return titlePicHeight;
    }

    public void settitlePicHeight(String titlePicHeight) {
        this.titlePicHeight = titlePicHeight;
    }

    public String gethasUT() {
        return hasUT;
    }

    public void sethasUT(String hasUT) {
        this.hasUT = hasUT;
    }

    public String getsmallTitlePic() {
        return smallTitlePic;
    }

    public void setsmallTitlePic(String smallTitlePic) {
        this.smallTitlePic = smallTitlePic;
    }

    public String getsmallTPWidth() {
        return smallTPWidth;
    }

    public void setsmallTPWidth(String smallTPWidth) {
        this.smallTPWidth = smallTPWidth;
    }

    public String getsmallTPHeight() {
        return smallTPHeight;
    }

    public void setsmallTPHeight(String smallTPHeight) {
        this.smallTPHeight = smallTPHeight;
    }

    public String SenderName;
    public String SenderLink;
    public String SenderImage;
    public String Action;
    public String Base36;
    public String SenderComment;

    //以下属性都是小写开头。是来自原来的ClipEntity 表的内容复制过来的。
    public String uguid;
    public String uname;
    public String uimage;
    public String origtitle;
    public String width;
    public String height;
    public String type;
    public String origwidth;
    public String origheight;
    public String origurl;
    public String origsite;
    public String contentTitle;
    public String contentBrief;
    public String titlePic;
    public String titlePicWidth;
    public String titlePicHeight;
    public String hasUT;
    public String smallTitlePic;
    public String smallTPWidth;
    public String smallTPHeight;

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

    public String getRecommendReason() {
        return RecommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        RecommendReason = recommendReason;
    }

    String RecommendReason;

}
