package org.apache.mahout.cf.taste.vjianke;

import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableBatchOperation;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

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
            "DefaultEndpointsProtocol=https;" +
                    "AccountName=eachcloudasia;" +
                    "AccountKey= mo1bmV+/AAbDTNpLFVV1F64zNEpB8v96fxX4x+HemU9tEUrF49oEp4FabUmlOVQWWramhyohjeWj70wVEQooug==";
    private CloudTableClient _tableClient;

    public void init()
    {
        try
        {
            CloudStorageAccount account;
            CloudBlobClient blobClient;
            CloudBlobContainer container;
            CloudBlockBlob blob;

            account = CloudStorageAccount.parse(storageConnectionString);
            blobClient = account.createCloudBlobClient();
            // Container name must be lower case.
            //container = blobClient.getContainerReference("blobsample");
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
            System.exit(-1);
        }
    }


}
