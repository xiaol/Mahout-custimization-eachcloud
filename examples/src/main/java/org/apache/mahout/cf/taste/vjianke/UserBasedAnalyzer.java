package org.apache.mahout.cf.taste.vjianke;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.*;


import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-1-16
 * Time: 下午5:13
 * To change this template use File | Settings | File Templates.
 */
public class UserBasedAnalyzer {


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

    private FastByIDMap<PreferenceArray> getPreferences(
            FastByIDMap<PreferenceArray> prefsMap, ArrayList<UUID> users){
        // The types for the following variables are
        // defined in the java.sql library.
        Connection connection = null;  // For making the connection
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;

        try
        {
            // Ensure the SQL Server driver class is available.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Establish the connection.
            connection = DriverManager.getConnection(Datalayer._connectionString);

            // Define the SQL string.
            String sqlString = "SELECT * FROM ClickEntity " +
                    "WHERE add_time BETWEEN ? AND ? ORDER BY user_id";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setTimestamp(1, _ts);
            preparedStatement.setTimestamp(2, _tsEnd);

            resultSet = preparedStatement.executeQuery();
            ArrayList<BooleanPreference> prefs = new ArrayList<BooleanPreference>();
            // Print out the returned number of rows.
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

            /*System.out.println("There were " +
                    rowCount +
                    " rows returned.");
            // Provide a message when processing is complete.
            System.out.println("Processing complete.");  */

        }
        catch (ClassNotFoundException cnfe)
        {

            System.out.println("ClassNotFoundException " +
                    cnfe.getMessage());
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
                if (null != connection) connection.close();
                if (null != statement) statement.close();
                if (null != resultSet) resultSet.close();
            }
            catch (SQLException sqlException)
            {
                // No additional action if close() statements fail.
            }
        }
        return prefsMap;
    }

    public FastIDSet getPreferenceByReadHistory(
            String strUserId,
            ArrayList<UUID> users){
        PreparedStatement preparedStatement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        PreferenceArray preferenceArray = null;

        String sqlString = "SELECT TOP 20 * FROM ClickEntity " +
                "WHERE user_id = '"+strUserId+"' ORDER BY add_time DESC";

        UUID uuid = UUID.fromString(strUserId);
        FastByIDMap<FastIDSet> prefsIDset = new FastByIDMap<FastIDSet>();
        Connection connection;
        try {
            connection = DriverManager.getConnection(Datalayer._connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return new FastIDSet(0);
        }

        try
        {
            preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, start);
            //preparedStatement.setTimestamp(2, end);
            preparedStatement.setQueryTimeout(0);
            resultSet = preparedStatement.executeQuery();
            if(!users.contains(uuid)){
                users.add(uuid);
            }

            ArrayList<BooleanPreference> userPrefs = new ArrayList<BooleanPreference>();
            while (resultSet.next())
            {
                BooleanPreference booleanPreference = new BooleanPreference(
                        users.indexOf(uuid), Long.parseLong(resultSet.getString(2),36));
                userPrefs.add(booleanPreference);


                rowCount++;
            }
            FastIDSet itemIDs = new FastIDSet(userPrefs.size());
            for(BooleanPreference userPref:userPrefs){
                itemIDs.add(userPref.getItemID());
            }
            return itemIDs;

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
        return new FastIDSet(0);
    }

    public void init(
            FastByIDMap<PreferenceArray> prefsMap,ArrayList<UUID> users, Timestamp ts, Timestamp tsEnd){
        set_ts(ts);
        set_tsEnd(tsEnd);
        getPreferences(prefsMap, users);
    }

    public List<RecommendedItem> recommend(String strUuid, FastByIDMap<FastIDSet> prefsIDSet,
                          ArrayList<UUID> users, int howMany, List<Long> nearestNUsers) throws TasteException {
        DataModel model = new GenericBooleanPrefDataModel(prefsIDSet);

        UserSimilarity similarity =
                new LogLikelihoodSimilarity(model);
        UserNeighborhood neighborhood =
                new NearestNUserNeighborhood(users.size(), similarity, model);

        Recommender recommender =
                new GenericUserBasedRecommender(model, neighborhood, similarity);

        int userIndex = users.indexOf(UUID.fromString(strUuid/*"07221718-b190-4536-8191-a0410029de34")*/));
        List<RecommendedItem> recommendations =
                recommender.recommend(userIndex,howMany);
        for(long entity: neighborhood.getUserNeighborhood(userIndex)){
            nearestNUsers.add(entity);
        }

        System.out.println("user: " +users.get(userIndex));
        for(RecommendedItem item : recommendations ){
            System.out.println(Long.toString(item.getItemID(),36).toUpperCase());
        }

        /*System.out.println("user: " +users.get(userIndex));
        for(long item : nearestNUsers ){
            System.out.println(users.get((int)item));
        }*/
        return recommendations;
    }
}
