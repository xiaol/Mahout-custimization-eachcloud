package org.apache.mahout.cf.taste.vjianke;

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
public class JDBCHelper {
    private final String _connectionString =
            "jdbc:sqlserver://llwko2tjlq.database.windows.net" + ";" +
                    "database=demo1" + ";" +
                    "user=eachcloud@llwko2tjlq" + ";" +
                    "password=IONisgreat!";

    public UserEntity Query(String uuid){
        // The types for the following variables are
        // defined in the java.sql library.
        Connection connection = null;  // For making the connection
        Statement statement = null;    // For the SQL statement
        ResultSet resultSet = null;    // For the result set, if applicable
        int rowCount = 0;
        UserEntity userEntity = new UserEntity();

        try
        {
            // Ensure the SQL Server driver class is available.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establish the connection.
            connection = DriverManager.getConnection(_connectionString);

            String sqlString = "SELECT * FROM PanamaUserEntity WHERE id = '" + uuid +"'";
            //PreparedStatement preparedStatement = connection.prepareStatement(sqlString);

            Statement smt = connection.createStatement();
            resultSet = smt.executeQuery(sqlString);


            // Print out the returned number of rows.
            while (resultSet.next())
            {
                userEntity.setUuid(uuid);
                userEntity.setUser_screen_name(resultSet.getString(3));
                userEntity.setProfile_image_url(resultSet.getString(17));

                rowCount++;
            }


            System.out.println("There were " +
                    rowCount +
                    " rows returned.");
            // Provide a message when processing is complete.
            System.out.println("Processing complete.");

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
}
