package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.*;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-22
 * Time: 下午1:54
 * To change this template use File | Settings | File Templates.
 */
public class Datalayer {
    private final String _connectionString =
            "jdbc:sqlserver://llwko2tjlq.database.windows.net" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@llwko2tjlq" + ";" +
                    "password=IONisgreat!";

    private  Connection connection = null;

    public Datalayer(){
        try {
            connection = DriverManager.getConnection(_connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        try
        {
            // Ensure the SQL Server driver class is available.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establish the connection.


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
        catch (ClassNotFoundException cnfe)
        {

            System.out.println("ClassNotFoundException " +cnfe.getMessage());
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
            }
            catch (SQLException sqlException) {}
        }
        return userEntity;
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

   public List<String> queryClipByBoard(List<String> boards){
       Statement statement = null;
       ResultSet resultSet = null;
       int rowCount = 0;
       if(boards.isEmpty())
           return null;
       List<String> clipIds = new ArrayList<String>();
       String sqlString = "SELECT * FROM BoardClipEntity WHERE ";

       sqlString = sqlString + "board_id = '" + boards.get(0) +"'";
       for(int i=1;i < boards.size(); i++){
           sqlString = sqlString +"' OR " + boards.get(i) + "'";
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

           System.out.println("There were " + rowCount +" clips.");
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
           }
           catch (SQLException sqlException){}
       }
       return clipIds;
   }

   public List<String> querySubscription(String userId){
       Statement statement = null;
       ResultSet resultSet = null;
       int rowCount = 0;
       List<String> boardIds = new ArrayList<String>();
       try
       {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

           String sqlString = "SELECT * FROM BoardFollowerEntity WHERE follower_id = '" + userId +"' AND board_id NOT IN " +
                   "(SELECT id FROM BoardEntity WHERE owner_id = '" + userId + "')";
           statement = connection.createStatement();
           statement.setQueryTimeout(0);
           resultSet = statement.executeQuery(sqlString);

           while (resultSet.next())
           {
               String boardId = resultSet.getString(2);
               boardIds.add(boardId);
               rowCount++;
           }

           System.out.println("There were " + rowCount +" subscription.");
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

            System.out.println("There were " + rowCount +" subscription.");
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
            }
            catch (SQLException sqlException){}
        }

        return listBoardRelated;
    }

    public List<String> queryCreatedBoards(String userId){
        Statement statement = null;
        ResultSet resultSet = null;
        int rowCount = 0;
        List<String> boardIds = new ArrayList<String>();
        try
        {
            String sqlString = "SELECT TOP 3 id,follower_num FROM BoardEntity WHERE owner_id = '"+ userId + "' ORDER BY follower_num DESC";
            statement = connection.createStatement();
            statement.setQueryTimeout(0);
            resultSet = statement.executeQuery(sqlString);

            while (resultSet.next())
            {
                String boardId = resultSet.getString(1);
                boardIds.add(boardId);
                rowCount++;
            }

            System.out.println("There were " + rowCount +" subscription.");
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

    public FastByIDMap<PreferenceArray> fetchData(
            FastByIDMap<PreferenceArray> prefsMap,ArrayList<UUID> users, List<String> clipIds){
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
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }


    public FastByIDMap<PreferenceArray> fetchBoardRelationsData(
            FastByIDMap<PreferenceArray> prefsMap,ArrayList<String> boards, ArrayList<String> users){

        PreparedStatement preparedStatement= null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;

        String sqlString = "SELECT * FROM BoardFollowerEntity LEFT JOIN BoardEntity ON BoardFollowerEntity.board_id = BoardEntity.id";
        sqlString = sqlString + " ORDER BY follower_id ";

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
            }
            catch (SQLException sqlException){}
        }
        return prefsMap;
    }
}
