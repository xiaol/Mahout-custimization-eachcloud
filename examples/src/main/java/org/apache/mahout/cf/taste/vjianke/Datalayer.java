package org.apache.mahout.cf.taste.vjianke;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.*;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-22
 * Time: 下午1:54
 * To change this template use File | Settings | File Templates.
 */
public class Datalayer {
    public static final String _connectionString =
            "jdbc:sqlserver://qm05uctv57.database.chinacloudapi.cn" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@qm05uctv57" + ";" +
                    "password=IONisgreat!";

    public static final String _connectionStringHongkong =
            "jdbc:sqlserver://llwko2tjlq.database.windows.net" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@llwko2tjlq" + ";" +
                    "password=IONisgreat!";

    public final String baseTimestamp = "2013-06-15";
    public  final String upTimestamp = "2013-07-01";       //morning 10:00

    public Datalayer(){
    }

    public Timestamp get_ts() {
        return _ts;
    }

    public void set_ts(Timestamp _ts) {
        this._ts = _ts;
    }

    private Timestamp _ts;

    public Timestamp get_tsEnd() {
        return _tsEnd;
    }

    public void set_tsEnd(Timestamp _tsEnd) {
        this._tsEnd = _tsEnd;
    }

    private Timestamp _tsEnd;

    public UserEntity Query(String uuid){
        // The types for the following variables are
        // defined in the java.sql library.
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        UserEntity userEntity = new UserEntity();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return userEntity;
        }

        try
        {
            String sqlString = "SELECT * FROM PanamaUserEntity WHERE id = '" + uuid +"'";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                userEntity.setUuid(uuid);
                userEntity.setUser_screen_name(resultSet.getString(3));
                userEntity.setProfile_image_url(resultSet.getString(17));

                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return userEntity;
    }


    public String queryBoard(String boardId){
        // The types for the following variables are
        // defined in the java.sql library.
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        UserEntity userEntity = new UserEntity();

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        String uuid = boardId.substring(0,8)+"-"+boardId.substring(8,12)+"-"+
                boardId.substring(12,16)+"-"+boardId.substring(16,20) +"-"+boardId.substring(20,boardId.length());
        try
        {
            String sqlString = "SELECT board_name FROM BoardEntity WHERE id = '" + uuid +"'";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                return resultSet.getString(1);

            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return "";
    }



    public Hashtable<String, UserEntity> QueryUsers(){
        // The types for the following variables are
        // defined in the java.sql library.
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        Hashtable<String, UserEntity> userEntities =
                new Hashtable<String, UserEntity>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return userEntities;
        }
        try
        {
            String sqlString = "SELECT Id,user_screen_name,profile_image_url FROM PanamaUserEntity";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);

            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                UserEntity userEntity = new UserEntity();
                userEntity.setUuid(resultSet.getString(1));
                userEntity.setUser_screen_name(resultSet.getString(2));
                userEntity.setProfile_image_url(resultSet.getString(3));
                userEntities.put(userEntity.uuid,userEntity);
                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return userEntities;
    }

    public List<String> QueryContributor(){
        // The types for the following variables are
        // defined in the java.sql library.
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        List<String> userEntities =
                new ArrayList<String>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
        try
        {
            String sqlString = "SELECT DISTINCT  user_id FROM UnLikeClipEntity";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);

            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                userEntities.add(resultSet.getString(1));
                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return userEntities;
    }

    public class UserEntity{
        public String getProfile_image_url() {
            return profile_image_url;
        }

        public void setProfile_image_url(String profile_image_url) {
            this.profile_image_url = profile_image_url;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUser_screen_name() {
            return user_screen_name;
        }

        public void setUser_screen_name(String user_screen_name) {
            this.user_screen_name = user_screen_name;
        }

        String uuid;
        String user_screen_name;
        String profile_image_url;
    }

   public List<String> queryClipByBoard(
           List<String> boards, boolean limited, int count, String userId){
       Statement statement = null;
       ResultSet resultSet = null;
       int rowCount = 0;
       if(boards.isEmpty())
           return null;
       List<String> clipIds = new ArrayList<String>();
       String sqlString = "SELECT";
       if(limited)
           sqlString += " TOP "+count;
       sqlString += " * FROM BoardClipEntity WHERE ";

       if(boards.size() >1 )
           sqlString += "(";
       sqlString += "board_id = '" + boards.get(0) +"'";
       for(int i=1;i < boards.size(); i++){
           sqlString = sqlString +"' OR " + boards.get(i) + "')";
       }
       if(limited)
           sqlString = sqlString +" AND clip_id NOT IN " +
                   "(SELECT clip_id FROM ClickEntity WHERE user_id = '" +userId +"') ORDER BY NEWID()";

       Connection connection;
       try {
           connection = DriverManager.getConnection(_connectionString);
       } catch (SQLException e) {
           e.printStackTrace();
           return clipIds;
       }

       try
       {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
           statement = connection.createStatement();
           statement.setQueryTimeout(0);
           resultSet = statement.executeQuery(sqlString);

           while (resultSet.next())
           {
               String clipId = resultSet.getString(3);
               clipIds.add(clipId);
               rowCount++;
           }
           //System.out.println("There were " + rowCount +" clips.");
       }
       catch (ClassNotFoundException cnfe)
       {
           System.out.println("ClassNotFoundException " + cnfe.getMessage());
       }
       catch (Exception e)
       {
           System.out.println("Exception " + e.getMessage());
           e.printStackTrace();
       }
       finally
       {
           try
           {
               if (null != statement) statement.close();
               if (null != resultSet) resultSet.close();
               connection.close();
           }
           catch (SQLException sqlException){}
       }
       return clipIds;
   }

   public List<String> querySubscription(String userId,int count){
       Statement statement = null;
       ResultSet resultSet = null;
       int rowCount = 0;
       List<String> boardIds = new ArrayList<String>();
       Connection connection;
       try {
           connection = DriverManager.getConnection(_connectionString);
       } catch (SQLException e) {
           e.printStackTrace();
           return boardIds;
       }
       try
       {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

           String sqlString = "SELECT TOP "+count+" * FROM BoardFollowerEntity WHERE follower_id = '" + userId +"' AND board_id NOT IN " +
                   "(SELECT id FROM BoardEntity WHERE owner_id = '" + userId + "')";
           //sqlString = sqlString + " TABLESAMPLE("+ count +" ROWS)";
           sqlString = sqlString + " ORDER BY NEWID()";
           statement = connection.createStatement();
           statement.setQueryTimeout(0);
           resultSet = statement.executeQuery(sqlString);

           while (resultSet.next())
           {
               String boardId = resultSet.getString(2);
               boardIds.add(boardId);
               rowCount++;
           }

           //System.out.println("There were " + rowCount +" subscription.");
       }
       catch (ClassNotFoundException cnfe)
       {
           System.out.println("ClassNotFoundException " + cnfe.getMessage());
       }
       catch (Exception e)
       {
           System.out.println("Exception " + e.getMessage());
           e.printStackTrace();
       }
       finally
       {
           try
           {
               if (null != statement) statement.close();
               if (null != resultSet) resultSet.close();
               connection.close();
           }
           catch (SQLException sqlException){}
       }

       return boardIds;
   }

    public List<BoardRelated> queryRelatedBoards(String userId){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;
        List<BoardRelated> listBoardRelated = new ArrayList<BoardRelated>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return listBoardRelated;
        }
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String sqlString = "SELECT DISTINCT(from_board),board FROM ReclipEntity WHERE board IN " +
                    "(SELECT id FROM BoardEntity WHERE owner_id = '" + userId + "')";
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                String source = resultSet.getString(1);
                String board = resultSet.getString(2);
                listBoardRelated.add(new BoardRelated(board,source));
                rowCount++;
            }

            //System.out.println("There were " + rowCount +" subscription.");
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }

        return listBoardRelated;
    }

    public List<String> queryCreatedBoards(String userId,int count){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;
        List<String> boardIds = new ArrayList<String>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return boardIds;
        }
        try
        {
            String sqlString;
            if(count != 0){
                //sqlString = sqlString + " TABLESAMPLE("+ count +" ROWS)";
                sqlString = "SELECT TOP "+count+
                        " id,follower_num FROM BoardEntity WHERE owner_id = '"+ userId + "'";
                sqlString = sqlString + " ORDER BY NEWID()";
            }else{
                sqlString = "SELECT id,follower_num FROM BoardEntity WHERE owner_id = '"+ userId + "' ORDER BY follower_num DESC";
            }
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                String boardId = resultSet.getString(1);
                boardIds.add(boardId);
                rowCount++;
            }

            //System.out.println("There were " + rowCount +" subscription.");
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }

        return boardIds;
    }

    protected class BoardRelated{
        String board;
        String source;
        public BoardRelated(String board, String source){
            this.board = board;
            this.source = source;
        }
    }

    public FastByIDMap<PreferenceArray> getPreferenceByClick(
            FastByIDMap<PreferenceArray> prefsMap, ArrayList<UUID> users, List<String> clipIds){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;
        if(clipIds.isEmpty())
            return null;

        String sqlString = "SELECT * FROM ClickEntity " +
                "WHERE ";
        sqlString = sqlString + "clip_id = '" +clipIds.get(0) +"'";
        for(int i=1; i< clipIds.size(); i++){
            sqlString = sqlString + " OR clip_id = '" + clipIds.get(i) + "'";

        }
        sqlString = sqlString + " ORDER BY user_id";
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return prefsMap;
        }

        try
        {
             preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            ArrayList<BooleanPreference> prefs = new ArrayList<BooleanPreference>();

            while (resultSet.next())
            {
                /*System.out.println("There were " +
                        resultSet.getLong(1) +"2: " +resultSet.getString(2) +  " "     +
                        Long.parseLong(resultSet.getString(2),36) +  " "
                        +Long.toString(Long.parseLong(resultSet.getString(2),36),36)+
                        "3: " + UUID.fromString(resultSet.getString(3)) + "4: " + resultSet.getDate(4)
                        +" rows returned.");*/

                UUID uuid = UUID.fromString(resultSet.getString(3));
                if(!users.contains(uuid))  {
                    users.add(uuid);
                    BooleanPreference booleanPreference = new BooleanPreference(
                            users.indexOf(uuid), Long.parseLong(resultSet.getString(2),36));
                    if(rowCount != 0){
                        prefsMap.put(users.indexOf(uuid)-1,new BooleanUserPreferenceArray(prefs));
                        prefs = new ArrayList<BooleanPreference>();
                    }
                    prefs.add(booleanPreference);
                }else{
                    BooleanPreference booleanPreference = new BooleanPreference(
                            users.indexOf(uuid), Long.parseLong(resultSet.getString(2),36));
                    prefs.add(booleanPreference);
                }
                rowCount++;
            }
            //Add last one
            if(prefs.size() != 0){
                prefsMap.put(prefs.get(0).getUserID(), new BooleanUserPreferenceArray(prefs));
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }

    public FastByIDMap<PreferenceArray> getPreferenceByUserClip(
            FastByIDMap<PreferenceArray> prefsMap, String strUserId,
            ArrayList<UUID> users, List<String> clipIds){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;
        if(clipIds.isEmpty())
            return null;

        String sqlString = "SELECT * FROM ClipEntity " +
                "WHERE ";
        sqlString = sqlString + "(id = '" +clipIds.get(0) +"'";
        for(int i=1; i< clipIds.size(); i++){
            sqlString = sqlString + " OR id = '" + clipIds.get(i) + "'";

        }
        sqlString = sqlString + ") AND user_guid = '" + strUserId +"'";

        UUID uuid = UUID.fromString(strUserId);
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return prefsMap;
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            BooleanUserPreferenceArray prefs;
            if(!users.contains(uuid)){
               users.add(uuid);
                prefs = new BooleanUserPreferenceArray(0);
            }else{
                 prefs =
                        (BooleanUserPreferenceArray) prefsMap.get(users.indexOf(uuid));
            }

            ArrayList<BooleanPreference> userPrefs = new ArrayList<BooleanPreference>();
            while (resultSet.next())
            {
               BooleanPreference booleanPreference = new BooleanPreference(
                       users.indexOf(uuid), Long.parseLong(resultSet.getString(1),36));
               userPrefs.add(booleanPreference);
               rowCount++;
            }

            Iterator it = prefs.iterator();
            while (it.hasNext()){
                Preference pref = (Preference) it.next();
                BooleanPreference booleanPreference = new BooleanPreference(
                        users.indexOf(uuid), pref.getItemID());
                userPrefs.add(booleanPreference);
            }
            prefsMap.put(users.indexOf(uuid), new BooleanUserPreferenceArray(userPrefs));
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }

    public FastByIDMap<PreferenceArray> getBoardRelationsByUser(
            FastByIDMap<PreferenceArray> prefsMap, ArrayList<String> boards, ArrayList<String> users){

        PreparedStatement preparedStatement= null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;

        String sqlString = "SELECT * FROM BoardFollowerEntity LEFT JOIN BoardEntity ON BoardFollowerEntity.board_id = BoardEntity.id";
        sqlString = sqlString + " ORDER BY follower_id ";

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return prefsMap;
        }
        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            ArrayList<GenericPreference> prefs = new ArrayList<GenericPreference>();

            while (resultSet.next())
            {
                String boardId = resultSet.getString(2);
                String followerId = resultSet.getString(3);
                String ownerId = resultSet.getString(8);
                if(!boards.contains(boardId)){
                    boards.add(boardId);
                }
                float value = 1.0f;
                if(followerId.equals(ownerId)){
                    value = 1.0f;
                }else{
                    value = 1.0f;
                }
                if(!users.contains(followerId))  {
                    users.add(followerId);
                    GenericPreference pref = new GenericPreference(
                            users.indexOf(followerId),boards.indexOf(boardId),value);
                    if(rowCount != 0){
                        prefsMap.put(users.indexOf(followerId)-1,new GenericUserPreferenceArray(prefs));
                        prefs = new ArrayList<GenericPreference>();
                    }
                    prefs.add(pref);
                }else{
                    GenericPreference pref = new GenericPreference(
                            users.indexOf(followerId),boards.indexOf(boardId),value);
                    prefs.add(pref);
                }
                rowCount++;
            }
            //Add last one
            if(prefs.size() != 0){
                prefsMap.put(prefs.get(0).getUserID(), new GenericUserPreferenceArray(prefs));
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }

    public FastByIDMap<PreferenceArray> getBoardRelationByClips(
            FastByIDMap<PreferenceArray> prefsMap, ArrayList<String> boards, ArrayList<String> clipIds){

        PreparedStatement preparedStatement= null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT a.board_id,a.clip_id,b.origurl FROM BoardClipEntity a INNER JOIN ClipEntity b on a.clip_id = b.id";
        sqlString = sqlString + " ORDER BY a.clip_id ";

        Hashtable<String,String> originUrlTable = new Hashtable<String, String>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return prefsMap;
        }
        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            ArrayList<GenericPreference> prefs = new ArrayList<GenericPreference>();

            while (resultSet.next())
            {
                String boardId = resultSet.getString(1);
                String clipId = resultSet.getString(2);
                String originUrl = resultSet.getString(3);

                if(!boards.contains(boardId)){
                    boards.add(boardId);
                }
                float value = 1.0f;
                if(originUrlTable.contains(originUrl) && !originUrl.contains("vjianke")){
                    String sameSourceClip = originUrlTable.get(originUrl);
                    GenericPreference pref = new GenericPreference(
                            clipIds.indexOf(sameSourceClip),boards.indexOf(boardId),value);
                    if(rowCount != 0){
                        prefsMap.put(clipIds.indexOf(clipId)-1,new GenericUserPreferenceArray(prefs));
                        prefs = new ArrayList<GenericPreference>();
                    }
                    prefs.add(pref);
                }else if(!clipIds.contains(clipId))  {
                    clipIds.add(clipId);
                    GenericPreference pref = new GenericPreference(
                            clipIds.indexOf(clipId),boards.indexOf(boardId),value);
                    if(rowCount != 0){
                        prefsMap.put(clipIds.indexOf(clipId)-1,new GenericUserPreferenceArray(prefs));
                        prefs = new ArrayList<GenericPreference>();
                    }
                    prefs.add(pref);
                    originUrlTable.put(originUrl,clipId);
                }else{
                    GenericPreference pref = new GenericPreference(
                            clipIds.indexOf(clipId),boards.indexOf(boardId),value);
                    prefs.add(pref);
                }
                rowCount++;
            }
            //Add last one
            if(prefs.size() != 0){
                prefsMap.put(prefs.get(0).getUserID(), new GenericUserPreferenceArray(prefs));
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }

    public class ClipEntity{
        public String id;
        public String title;
        public String user_id;
        public String origurl;
    }

    public class BoardEntity{
        public String id;
        public String name;
        public String ownerId;
        public boolean isPrivate;
    }

    public List<ClipEntity>  getClips(
            String count, boolean limited, boolean increment, boolean exclusive,boolean needTitle){
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        List<ClipEntity> clipEntities = new ArrayList<ClipEntity>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return clipEntities;
        }
        try
        {
            String sqlString = "SELECT ";
            if(limited)
                sqlString += "TOP "+ count +" ";
            sqlString += "Id,";
            if(needTitle)
                sqlString += "title,";
            sqlString += "user_guid, origurl FROM ClipEntity ";
            if(increment)
                sqlString += " where add_time > '"+ baseTimestamp+"' and add_time < '"+ upTimestamp +"'";
            if(exclusive)
                sqlString += "WHERE Id NOT IN " +
                        "(SELECT ClipId FROM ClipTagEntity)";
            sqlString += " ORDER BY Id";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                ClipEntity entity = new ClipEntity();
                entity.id = resultSet.getString(1);
                //System.out.println(entity.id);
                if(needTitle){
                    entity.title = resultSet.getString(2);
                    entity.user_id = resultSet.getString(3);
                    entity.origurl = resultSet.getString(4);
                }else{
                    entity.user_id = resultSet.getString(2);
                    entity.origurl = resultSet.getString(3);
                }
                clipEntities.add(entity);
                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return clipEntities;
    }

    public Hashtable<String,BoardEntity>  getBoards(
            String count, boolean limited, boolean increment){
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        Hashtable<String,BoardEntity> boardMap = new Hashtable<String, BoardEntity>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return boardMap;
        }
        try
        {
            String sqlString = "SELECT ";
            if(limited)
                sqlString += "TOP "+ count +" ";
            sqlString += "Id,board_name,owner_id,isPrivate FROM BoardEntity";
            if(increment)
                sqlString += " where add_time > '"+ baseTimestamp+"' and add_time < '"+ upTimestamp +"'";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                BoardEntity entity = new BoardEntity();
                entity.id = resultSet.getString(1);
                entity.name = resultSet.getString(2);
                entity.ownerId = resultSet.getString(3);
                entity.isPrivate = resultSet.getBoolean(4);
                boardMap.put(entity.id,entity);

                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return boardMap;
    }

    public List<String> getReadHistory(String uuid,
                                       boolean limited, String startTime, String endTime){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT clip_id FROM ClickEntity " +
                "WHERE ";
        sqlString = sqlString + "user_id = '" + uuid +"'";
        if(limited){
            sqlString += " and add_time > '"+ startTime+"' and add_time < '"+ endTime +"'";
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        List<String> clipIds = new ArrayList<String>();
        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                clipIds.add(resultSet.getString(1));
                rowCount++;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return clipIds;
    }


    public List<ClipEntity> getRecentClipByUser(String strUserId, int count, String upTimestamp){
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        List<ClipEntity> clipEntities = new ArrayList<ClipEntity>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return clipEntities;
        }
        try
        {
            String sqlString = "SELECT ";
            sqlString += "TOP "+ count +" ";
            sqlString += "Id,title,user_guid FROM ClipEntity";
            //sqlString += " where add_time < '"+ upTimestamp+"' and
            sqlString += " WHERE user_guid = '"+ strUserId +"'";
            //sqlString += "ORDER BY add_time DESC";
            //sqlString = sqlString + " TABLESAMPLE("+ count +" ROWS)";
            sqlString = sqlString + " ORDER BY NEWID()";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);
            // Print out the returned number of rows.
            while (resultSet.next())
            {
                ClipEntity entity = new ClipEntity();
                entity.id = resultSet.getString(1);
                entity.title = resultSet.getString(2);
                entity.user_id = resultSet.getString(3);
                clipEntities.add(entity);
                rowCount++;
            }

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException) {}
        }
        return clipEntities;
    }

    public boolean isClipRead(String clipId, String uuid){
        boolean result = false;
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT clip_id FROM ClickEntity " +
                "WHERE ";
        sqlString = sqlString + "user_id = '" + uuid +"' and clip_id = '"+clipId+"'";
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                result = true;
                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }

        return result;
    }

    public boolean isOwenClip(String clipId, String uuid){
        boolean result = false;
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT * FROM ClipEntity " +
                "WHERE ";
        sqlString = sqlString + "(id = '" +clipId+"'";

        sqlString = sqlString + ") AND user_guid = '" + uuid +"'";
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                result = true;
                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }

        return result;
    }

    public List<WeiboTag> getWeiboTag(String uuid){
        uuid = uuid.toUpperCase();
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT user_tag,user_fav_tag FROM UserMicroBlogEntity " +
                "WHERE ";
        sqlString = sqlString + "user_id = '" + uuid +"'";
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        List<WeiboTag> weiboTags = new ArrayList<WeiboTag>();
        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                WeiboTag weiboTag = new WeiboTag();
                weiboTag.userTag = resultSet.getString(1);
                weiboTag.userFavTag = resultSet.getString(2);
                if(weiboTag.userFavTag == null && weiboTag.userTag == null)
                    continue;
                weiboTags.add(weiboTag);
                rowCount++;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return weiboTags;
    }

    public void addClipTagIndex(List<ConvertAutoTagToIndex.ClipTagEntity> entityList){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "INSERT INTO ClipTagEntity(ClipId,Tag,OwnerGuid,BoradId,Weight,Timestamp) "
                + "VALUES (?, ?, ?, ?, ?,?)";
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            System.out.println(e);
            return;
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            for(ConvertAutoTagToIndex.ClipTagEntity entity:entityList){
                preparedStatement.setString(1, entity.ClipId);
                preparedStatement.setString(2,entity.Tag);
                preparedStatement.setString(3,entity.OwnerGuid);
                preparedStatement.setString(4,entity.BoardId);
                preparedStatement.setFloat(5, entity.Weight);
                preparedStatement.setDate(6,entity.Timestamp);
                preparedStatement.addBatch();
                preparedStatement.clearParameters();
            }
            int[] results = preparedStatement.executeBatch();

        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
    }

    public String getBoardByClip(String clipId){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;

        List<String> clipIds = new ArrayList<String>();
        String sqlString = "SELECT * FROM BoardClipEntity WHERE ";

        sqlString = sqlString + "clip_id = '" + clipId +"'";

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                String boardId = resultSet.getString(2);
                return boardId;
            }
            //System.out.println("There were " + rowCount +" clips.");
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return "";
    }

    public boolean isInTable(String key,String tableName,String columnId){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;

        List<String> clipIds = new ArrayList<String>();
        String sqlString = "SELECT * FROM "+tableName+" WHERE ";

        sqlString = sqlString + columnId+" = '" + key +"'";

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                return true;
            }
            //System.out.println("There were " + rowCount +" clips.");
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return false;
    }

    public JSONArray getActiveUsers(int countDays){
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();// http://vjiankebuildcn.chinacloudapp.cn/User/ActiveUsers?days=2
            HttpGet getRequest = new HttpGet(
                    "http://vjiankeyoda.cloudapp.net/User/ActiveUsers?days="+countDays);
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            JSONObject jo = null;
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                //System.out.println(output);
                jo = (JSONObject)JSONValue.parse(output);
            }
            httpClient.getConnectionManager().shutdown();
            if(jo !=null){
                JSONArray ja = (JSONArray)jo.get("UserGuid");
                return ja;
            }

        } catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;
    }

    public List<String> getUnlikeClip(String uuid){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;

        List<String> clipIds = new ArrayList<String>();
        String sqlString = "SELECT clip_id FROM UnLikeClipEntity WHERE ";

        sqlString = sqlString + "user_id = '" + uuid +"'";

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);


            while (resultSet.next())
            {
                String clipId = resultSet.getString(1);
                clipIds.add(clipId);
            }
            //System.out.println("There were " + rowCount +" clips.");
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return clipIds;
    }

    public List<String> getClipByBoard(String boardId, boolean isLimited, String userId){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;
        List<String> clipIds = new ArrayList<String>();
        String sqlString = "SELECT TOP 1 * FROM BoardClipEntity WHERE ";

        sqlString = sqlString + "board_id = '" + boardId + "'";
        sqlString = sqlString +" AND clip_id NOT IN " +
                "(SELECT id FROM ClipEntity WHERE user_guid = '" + userId + "')";
        sqlString = sqlString +" AND clip_id NOT IN " +
                "(SELECT clip_id FROM ClickEntity WHERE user_id = '" +userId +"') ORDER BY NEWID()";


        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return clipIds;
        }

        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                String clipId = resultSet.getString(3);
                clipIds.add(clipId);
                rowCount++;
            }
            //System.out.println("There were " + rowCount +" clips.");
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("ClassNotFoundException " + cnfe.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return clipIds;
    }

    public Map<Integer,Integer> getChannelByClips(List<String> clipIds){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;
        if(clipIds.isEmpty())
            return Collections.EMPTY_MAP;

        String sqlString = "SELECT channel_id FROM ClipEntity " +
                "WHERE ";
        sqlString = sqlString + "id = '" +clipIds.get(0) +"'";
        for(int i=1; i< clipIds.size(); i++){
            sqlString = sqlString + " OR id = '" + clipIds.get(i) + "'";

        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.EMPTY_MAP;
        }
        Map<Integer,Integer> channelMaps = new Hashtable<Integer, Integer>();

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                int channelId = resultSet.getInt(1);
                if(channelMaps.containsKey(channelId)){
                    int channelCount = channelMaps.get(channelId);
                    channelMaps.put(channelId,channelCount+1);
                }else{
                    channelMaps.put(channelId,1);
                }
                rowCount++;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }
        return channelMaps;
    }

    public boolean isPrivateBoard(String boardId){
        boolean result = false;
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;

        String sqlString = "SELECT isPrivate FROM BoardEntity " +
                "WHERE ";
        sqlString = sqlString + "id = '" +boardId+"'";

        Connection connection;
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                int isPrivate = resultSet.getByte(1);
                if(isPrivate ==1) {
                    result = true;
                }
                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // Close resources.
                if (null != preparedStatement) preparedStatement.close();
                if (null != resultSet) resultSet.close();
                connection.close();
            }
            catch (SQLException sqlException){}
        }

        return result;
    }


    public class WeiboTag{
        String userTag;
        String userFavTag;

        public String getUserTag() {
            return userTag;
        }

        public void setUserTag(String userTag) {
            this.userTag = userTag;
        }

        public String getUserFavTag() {
            return userFavTag;
        }

        public void setUserFavTag(String userFavTag) {
            this.userFavTag = userFavTag;
        }
    }

}
