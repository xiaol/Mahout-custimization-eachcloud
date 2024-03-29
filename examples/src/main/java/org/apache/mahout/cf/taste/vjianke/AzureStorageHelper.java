package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;
import com.microsoft.windowsazure.services.table.client.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-19
 * Time: 下午4:21
 * To change this template use File | Settings | File Templates.
 */
public class AzureStorageHelper {
    public static final String storageConnectionString =
            "BlobEndpoint=http://eachcloudcn.blob.core.chinacloudapi.cn/;QueueEndpoint=http://eachcloudcn.queue.core.chinacloudapi.cn/;TableEndpoint=http://eachcloudcn.table.core.chinacloudapi.cn/;" +
                    "DefaultEndpointsProtocol=http;" +
                    "AccountName=eachcloudcn;" +
                    "AccountKey=l5IO6gj+r+pKri09QGzQ33bN31/XyaLcRaurhQsOT2EkC71bJyk+rjetUhANbk9WXq6z6OgOfYdimRLGE5W6Ew==";
    private CloudTableClient _tableClient;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _container;
    String _clipsContainerName = "clips";

    public void init()
    {
        try
        {
            CloudStorageAccount account;


            account = CloudStorageAccount.parse(storageConnectionString);
            _blobClient = account.createCloudBlobClient();
            // Container name must be lower case.
            _container = _blobClient.getContainerReference(_clipsContainerName);
            //container.createIfNotExist();

            // Set anonymous access on the container.
            BlobContainerPermissions containerPermissions;
            containerPermissions = new BlobContainerPermissions();

            _tableClient = account.createCloudTableClient();


            // Upload an image file.
            //blob = container.getBlockBlobReference("image1.jpg");
            //File fileReference = new File ("c:\\myimages\\image1.jpg");
            //blob.upload(new FileInputStream(fileReference), fileReference.length());
        }
        catch (Exception e)
        {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }

    }


    public void uploadToAzureTable(String tableName, List<? extends TableServiceEntity> listEntity){
        // Define a batch operation.
        TableBatchOperation batchOperation = new TableBatchOperation();

        for(int i =0; i < listEntity.size(); i++ ){
             batchOperation.insert(listEntity.get(i));
        }

        try {
            _tableClient.execute(tableName, batchOperation);
        } catch (StorageException e) {
            System.out.print("StorageException encountered: ");
            System.out.println(e.getMessage());
            //System.exit(-1);
        }
    }

    public List<RecommendClipEntity> deleteByPartitionKey(String tableName, String partitionKey){
        // Create a filter condition where the partition key is "Smith".
        String partitionFilter = TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                partitionKey);


        String rowFilter = TableQuery.generateFilterCondition(
                TableConstants.ROW_KEY,
                TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
                0);

        // Combine the two conditions into a filter expression.
        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                TableQuery.Operators.AND, rowFilter);

        TableQuery<RecommendClipEntity> rangeQuery =
                TableQuery.from(tableName, RecommendClipEntity.class)
                        .where(partitionFilter);

        List<RecommendClipEntity> recommendClipEntities =
                new ArrayList<RecommendClipEntity>();
        try{
            for (RecommendClipEntity entity : _tableClient.execute(rangeQuery)) {
                // Create an operation to delete the entity.
                recommendClipEntities.add(entity);
            }
        }catch(Exception e){
            e.printStackTrace();
            return recommendClipEntities;
        }
        TableBatchOperation batchOperation = new TableBatchOperation();

        int count = 0;
        for(RecommendClipEntity entity:recommendClipEntities){
            TableOperation deleteSmithJeff = TableOperation.delete(entity);
            batchOperation.add(deleteSmithJeff);
            if(count%99 ==0 && count !=0){
                try {
                    if(!batchOperation.isEmpty())
                        _tableClient.execute(tableName, batchOperation);
                } catch (StorageException e) {
                    e.printStackTrace();
                }
                batchOperation.clear();
            }
            count++;
        }
        try {
            if(!batchOperation.isEmpty())
                _tableClient.execute(tableName, batchOperation);
        } catch (StorageException e) {
            e.printStackTrace();
        }

        return recommendClipEntities;
    }

    public List<RecommendClipEntity> retrieveByPartitionKey(String tableName, String partitinKey){
        // Create a filter condition where the partition key is "Smith".
        String partitionFilter = TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                partitinKey);


        String rowFilter = TableQuery.generateFilterCondition(
                TableConstants.ROW_KEY,
                TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
                0);

        // Combine the two conditions into a filter expression.
        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                TableQuery.Operators.AND, rowFilter);

        TableQuery<RecommendClipEntity> rangeQuery =
                TableQuery.from(tableName, RecommendClipEntity.class)
                        .where(partitionFilter);

        List<RecommendClipEntity> recommendClipEntities =
                new ArrayList<RecommendClipEntity>();
        for (RecommendClipEntity entity : _tableClient.execute(rangeQuery)) {
            // Create an operation to delete the entity.
            recommendClipEntities.add(entity);
        }
        return recommendClipEntities;
    }

    public List<RecommendBoardEntity> retrieveBoardByPartitionKey(
            String tableName, String partitinKey){
        // Create a filter condition where the partition key is "Smith".
        String partitionFilter = TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                partitinKey);


        String rowFilter = TableQuery.generateFilterCondition(
                TableConstants.ROW_KEY,
                TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
                0);

        // Combine the two conditions into a filter expression.
        String combinedFilter = TableQuery.combineFilters(partitionFilter,
                TableQuery.Operators.AND, rowFilter);

        TableQuery<RecommendBoardEntity> rangeQuery =
                TableQuery.from(tableName, RecommendBoardEntity.class)
                        .where(partitionFilter);

        List<RecommendBoardEntity> recommendBoardEntities =
                new ArrayList<RecommendBoardEntity>();

        try{
        for (RecommendBoardEntity entity : _tableClient.execute(rangeQuery)) {
            // Create an operation to delete the entity.
            recommendBoardEntities.add(entity);
        }
        }catch (Exception e) {
            System.out.println(partitinKey);
            e.printStackTrace();
        }
        return recommendBoardEntities;
    }


    public FeedClipEntity retrieveFeedClipEntity(String clipId, String tableName){

        String filter = TableQuery.generateFilterCondition(
                "Base36", TableQuery.QueryComparisons.EQUAL,clipId);

        TableQuery<FeedClipEntity> tq = TableQuery.from(tableName,FeedClipEntity.class)
                .where(filter);
        ArrayList<FeedClipEntity> feeds = new ArrayList<FeedClipEntity>();
        for(FeedClipEntity entity:_tableClient.execute(tq)){
            feeds.add(entity);
        }
        return feeds.get(0);
    }

    public FeedClipEntity retrieveFeedClipEntity(String partitionKey,String rowKey, String tableName){
        TableOperation to =
                TableOperation.retrieve(partitionKey, rowKey, FeedClipEntity.class);

// Submit the operation to the table service and get the specific entity.
        try {
            FeedClipEntity specificEntity =
                    _tableClient.execute(tableName, to).getResultAsType();
            return  specificEntity;
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TagEntity retrieveTagEntity(String partitionKey, String rowKey, String tableName){

        TableOperation to =
                TableOperation.retrieve(partitionKey, rowKey, TagEntity.class);

// Submit the operation to the table service and get the specific entity.
        try {
            TagEntity specificEntity =
                    _tableClient.execute(tableName, to).getResultAsType();
            return  specificEntity;
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TagEntity> retrieveAllEntities(String tableName){
// Create a filter condition where the partition key is "Smith".
        String partitionFilter = TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                "Smith");

// Specify a partition query, using "Smith" as the partition key filter.
        TableQuery<TagEntity> partitionQuery =
                TableQuery.from("people", TagEntity.class)
                        .where(partitionFilter);

// Loop through the results, displaying information about the entity.
        for (TagEntity entity : _tableClient.execute(partitionQuery)) {

        }
        return null;
    }

    public void deleteTable(String tableName) throws Exception {
        throw new Exception("UnSupport");
    }

    public ByteArrayOutputStream getClipBlob(String clipId) throws StorageException, URISyntaxException, IOException {
        CloudBlockBlob blockBlob = _container.getBlockBlobReference(clipId + ".htm");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blockBlob.download(outputStream);
        return outputStream;
    }

    public static class TagEntity extends TableServiceEntity{
        public TagEntity(){}

        public String getTags() {
            return Tags;
        }

        public void setTags(String tags) {
            Tags = tags;
        }

        public String Tags;

    }

    public static class FeedClipEntity extends TableServiceEntity {

        public FeedClipEntity(){}

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

        public List<String> getTags() {
            return Tags;
        }

        public void setTags(List<String> tags) {
            Tags = tags;
        }

        public List<String> getCats() {
            return Cats;
        }

        public void setCats(List<String> cats) {
            Cats = cats;
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
        public List<String> Tags = new ArrayList<String>();
        public List<String> Cats = new ArrayList<String>();
//        public string FeedTime { get; set; }//分享Feed的时间

        public String getBoards() {
            return boards;
        }

        public void setBoards(String boards) {
            this.boards = boards;
        }

        public String boards;

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
        public String title;

        public String gettitle(){
            return title;
        }

        public void settitle(String title){
            this.title = title;
        }

        public String titlePic;
        public String titlePicWidth;
        public String titlePicHeight;
        public String hasUT;
        public String smallTitlePic;
        public String smallTPWidth;
        public String smallTPHeight;

    }

}
