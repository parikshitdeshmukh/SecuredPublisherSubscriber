package Server;


import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DBConnectionFactory {


    private static BasicDataSource ds = new BasicDataSource();

    // TODO: fill this in
    // The instance connection name can be obtained from the instance overview page in Cloud Console
    // or by running "gcloud sql instances describe <instance> | grep connectionName".
    static String instanceConnectionName = "pubsubmastersproject:us-central1:myinstance";

    // TODO: fill this in
    // The database from which to list tables.
    static String databaseName = "BrokerStorage";

    static String username = "root";

    // TODO: fill this in
    // This is the password that was set via the Cloud Console or empty if never set
    // (not recommended).
    static String password = "root";

    static String jdbcUrl = String.format(
            "jdbc:mysql://google/%s?cloudSqlInstance=%s"
                    + "&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
            databaseName,
            instanceConnectionName);

    static {
        ds.setUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    private DBConnectionFactory() throws SQLException {
    }

    public static HashMap<String, String> getBacklog() throws SQLException {

        HashMap<String, String> map = new HashMap<>();

        Connection connection= DBConnectionFactory.getConnection();
        PreparedStatement stmt = connection.prepareStatement("select * from Backlog");


        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
                System.out.println("At the DB Factory: "+rs.getString(1) + "#" + rs.getString(2));
            }
        } finally {
            connection.close();
        }

        return map;
    }

    public static void setBacklog(HashMap<String, String> map) throws SQLException {


        Connection connection= null;
        PreparedStatement stmt=null;

        try {
            connection = DBConnectionFactory.getConnection();


            stmt = connection.prepareStatement("insert into Backlog(IPAdd, Data) values(?,?) on duplicate key update Data=?");

            for (Map.Entry<String, String> e: map.entrySet()){

                stmt.setString(1, e.getKey().toString());
                stmt.setString(2, e.getValue().toString());
                stmt.setString(3, e.getValue());
                stmt.addBatch();
            }

            stmt.executeBatch();

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                } // nothing we can do
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                } // nothing we can do
            }
        }


    }


    public static void removeBacklog(String hostName) {
        Connection connection= null;
        PreparedStatement stmt=null;

        try {
            connection = DBConnectionFactory.getConnection();

            stmt = connection.prepareStatement("delete from Backlog where IPAdd = ?");
            stmt.setString(1,hostName);

            stmt.execute();


        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                } // nothing we can do
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                } // nothing we can do
            }
        }

    }
}
//    private static Connection getConn() throws IOException, SQLException {
//        // TODO: fill this in
//        // The instance connection name can be obtained from the instance overview page in Cloud Console
//        // or by running "gcloud sql instances describe <instance> | grep connectionName".
//        String instanceConnectionName = "pubsubmastersproject:us-central1:myinstance";
//
//        // TODO: fill this in
//        // The database from which to list tables.
//        String databaseName = "BrokerStorage";
//
//        String username = "root";
//
//        // TODO: fill this in
//        // This is the password that was set via the Cloud Console or empty if never set
//        // (not recommended).
//        String password = "root";
//
////        if (instanceConnectionName.equals("<insert_connection_name>")) {
////            System.err.println("Please update the sample to specify the instance connection name.");
////            System.exit(1);
////        }
////
////        if (password.equals("<insert_password>")) {
////            System.err.println("Please update the sample to specify the mysql password.");
////            System.exit(1);
////        }
//
//        try {
//            Class.forName("com.mysql.jdbc.GoogleDriver");
//        } catch (ClassNotFoundException e) {
//            e.getStackTrace().toString();
//        }
//
//        //[START doc-example]
//
//        String jdbcUrl = String.format(
//                "jdbc:mysql://google/%s?cloudSqlInstance=%s"
//                        + "&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
//                databaseName,
//                instanceConnectionName);
//
////        authExplicit("F:\\PractiseProbems\\Google_Cloud\\PubSubMastersProject-e332f797dfdb.json");
//
//        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
//        //[END doc-example]
//
//        return connection;
//    }





//
//
//CREATE TABLE Backlog (IPAdress VARCHAR(20), ClientName VARCHAR(20), data VARCHAR(20));
//
//Insert into Backlog values("8.8.8.8", "DummyClient", "NULL");