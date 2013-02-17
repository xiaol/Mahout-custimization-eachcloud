package org.apache.mahout.cf.taste.vjianke;


import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import java.sql.*;
import java.util.Collection;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: liuivan
 * Date: 13-2-15
 * Time: 下午5:12
 * To change this template use File | Settings | File Templates.
 */
public class FastCustomizeDataModel extends AbstractDataModel{
    private final String _connectionString =
            "jdbc:sqlserver://llwko2tjlq.database.windows.net" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@llwko2tjlq" + ";" +
                    "password=IONisgreat!";


    public void load(){
        Connection connection = null;  // For making the connection
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable

        try{
            // Establish the connection.
            connection = DriverManager.getConnection(_connectionString);

            // Define the SQL string.
            String sqlString = "SELECT * FROM ClickEntity " +
                    "WHERE add_time BETWEEN ? AND ? ORDER BY user_id";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            //preparedStatement.setTimestamp(1, _ts);
            //preparedStatement.setTimestamp(2, _tsEnd);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {

            }
        }catch (Exception e)
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
            }
        }

    }

    @Override
    public LongPrimitiveIterator getUserIDs() throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LongPrimitiveIterator getItemIDs() throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Float getPreferenceValue(long userID, long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getPreferenceTime(long userID, long itemID) throws TasteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumItems() throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsers() throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPreference(long userID, long itemID, float value) throws TasteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removePreference(long userID, long itemID) throws TasteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPreferenceValues() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
