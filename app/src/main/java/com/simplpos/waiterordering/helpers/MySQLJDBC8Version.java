package com.simplpos.waiterordering.helpers;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MySQLJDBC8Version {
    private String databaseConnectionType = "mysql";
    private String mysqlConnectionUserId = "root";
    private String mysqlConnectionPassword = "";
    private String mysqlDatabaseSelection = "simplposdata";
    private String portNumber = "3306";
    private String connectionhost = "localhost";
    private Boolean isTransactionRunningOld = false;

    public MySQLJDBC8Version(String databaseName,String mysqlUserId,String mysqlPassword,String mysqlPortNumber,String hostName)
    {
//        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        try {
            this.mysqlDatabaseSelection = databaseName;
            this.mysqlConnectionUserId = mysqlUserId;
            this.mysqlConnectionPassword = mysqlPassword;
            this.portNumber = mysqlPortNumber;
            this.connectionhost = hostName;
            this.databaseConnectionType = "mysql";
        }catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }
    public ArrayList<JSONObject> executeRawqueryJSON(String query)
    {
        Log.v("Testing","Called executeRawqueryJSON for "+query);
        ArrayList<JSONObject> mArrayList = new ArrayList<JSONObject>();
//        if(true){ return mArrayList;}

        Connection c = null;
        Statement stmt = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // DriverManager.setLoginTimeout(5);
            /* c = DriverManager.getConnection
                    ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword); */
            c =
                    DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                            "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData rsmd = rs.getMetaData();

            int numColumns = rsmd.getColumnCount();
            List<String> columnNames = new ArrayList<String>();
            for(int i=1;i<=numColumns;i++) {
                columnNames.add(rsmd.getColumnName(i));
            }
            while(rs.next()) { // convert each object to an human readable JSON object
                JSONObject obj = new JSONObject();
                for(int i=1;i<=numColumns;i++) {
                    String key = columnNames.get(i - 1);
                    String value = rs.getString(i);
                    obj.put(key, value);
                }
                mArrayList.add(obj);
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("For the query "+query);
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//            System.exit(0);
        }
        finally {

            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            stmt = null;
            c = null;
        }

        return mArrayList;
    }


    public void executeInsertBatch(String tableName, ArrayList<JSONObject> insertingValues) {

        JSONObject contentValues = new JSONObject();
        StringBuilder sbv = new StringBuilder();
        StringBuilder sbk = new StringBuilder();

        if(insertingValues.size()>0) {
            contentValues = insertingValues.get(0);
            Iterator<String> keys = contentValues.keys();
            String prefix = "";
            while (keys.hasNext()) {
                String key = keys.next();
                String  value = String.valueOf(contentValues.optString(key));
//                value = this.escapeWildcardsForMySQL(value);
                sbk.append(prefix);
                sbv.append(prefix);
                prefix = ",";
//                sbv.append("'").append(value).append("'");
//            sbv.append("?");
                sbk.append(key);
            }
            String totalQry = "INSERT INTO "+tableName+" (" + sbk.toString() + ") VALUES "; //(" + sbv.toString() + "),";

            for(int i=0; i < insertingValues.size(); i++)
            {
                sbv = new StringBuilder();
                contentValues = insertingValues.get(i);
                Iterator<String> values = contentValues.keys();
                totalQry +=" (";
                prefix = "";
                while (values.hasNext()) {
                    String val = "";

                    String nextValue = values.next();
                    val = addSlashes(contentValues.optString(nextValue));



                    sbv.append(prefix);
                    prefix = ",";
                    sbv.append("'").append(val).append("'");

                }
                totalQry += sbv.toString();
                totalQry +=" ),";
            }
            totalQry = (StringUtils.substring(totalQry, 0, totalQry.length() - 1)) + ";";

            Connection c = null;
            Statement stmt = null;
            try {
                {
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                    // DriverManager.setLoginTimeout(5);

                   /* c = DriverManager.getConnection
                           ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword); */
                    c =
                            DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                                    "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));

                }

                c.setAutoCommit(false);

                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                stmt = c.createStatement();
                stmt.executeUpdate(totalQry);

                stmt.close();
                c.commit();
                c.setAutoCommit(false);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }finally {

                try {
                    if(!c.isClosed())
                    { c.close(); }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void executeInsert(String tableName, JSONObject contentValues) {

        StringBuilder sbv = new StringBuilder();
        StringBuilder sbk = new StringBuilder();
        Iterator<String> keys = contentValues.keys();
        String prefix = "";
        while (keys.hasNext()) {
            String key = keys.next();

            String value = "";
            {
                value = addSlashes(String.valueOf(contentValues.optString(key)));//addSlashes(values.next());
            }
//            value = this.escapeWildcardsForMySQL(value);
            sbk.append(prefix);
            sbv.append(prefix);
            prefix = ",";
            sbv.append("'").append(value).append("'");
//            sbv.append("?");
            sbk.append(key);
        }
        String totalQry = "INSERT INTO "+tableName+" (" + sbk.toString() + ") VALUES (" + sbv.toString() + ");";

        Connection c = null;
        Statement stmt = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // DriverManager.setLoginTimeout(5);

            /* c = DriverManager.getConnection
                        ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword);
            */
            c =
                    DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                            "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));


            c.setAutoCommit(false);

            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt = c.createStatement();
            stmt.executeUpdate(totalQry);

            stmt.close();

            c.commit();
            c.setAutoCommit(false);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }finally {

            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void executeRawquery(String RawQuery) {

        Connection c = null;
        Statement stmt = null;
        try {
            {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                // DriverManager.setLoginTimeout(5);

                 /* c = DriverManager.getConnection
                         ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword);
                   */
                c =
                        DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                                "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));
            }

            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt = c.createStatement();
            stmt.executeUpdate(RawQuery);
            c.commit();
            c.setAutoCommit(false);

            stmt.close();
        }catch (SQLIntegrityConstraintViolationException e) {
            // Duplicate entry
            System.out.println("Exception for query "+RawQuery);
            e.printStackTrace();
        } catch (SQLException e){
            System.out.println("Exception for query "+RawQuery);
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("Exception Query is "+RawQuery);
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//            System.exit(0);
        }
        finally {

            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeDelete(String tableName, String conditionColumn, String conditionValue) {

        String prefix = "";
        String updatingColumnsString = "";
        conditionValue = addSlashes(conditionValue);
        String totalQry = "DELETE FROM "+tableName+" WHERE "+conditionColumn+"='"+conditionValue+"';";
        Connection c = null;
        Statement stmt = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // DriverManager.setLoginTimeout(5);

            /* c = DriverManager.getConnection
                        ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword);
            */


            c.setAutoCommit(false);

            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt = c.createStatement();
            stmt.executeUpdate(totalQry);

            stmt.close();
            c.commit();
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }finally {

            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeUpdate(String tableName, JSONObject contentValues, String conditionColumn, String conditionValue) {

        StringBuilder sbv = new StringBuilder();
        StringBuilder sbk = new StringBuilder();
        Iterator<String> keys = contentValues.keys();
        String prefix = "";
        String updatingColumnsString = "";
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = contentValues.optString(key);
            sbk.append(prefix);
            prefix = ",";

            sbk.append(key).append("='").append(addSlashes(String.valueOf(value))).append("'");
//            sbv.append("'").append(value).append("'");
//            sbv.append("?");
//            sbk.append(key);
        }
        String totalQry = "UPDATE "+tableName+" SET " + (sbk.toString())+ " WHERE "+conditionColumn+"='"+conditionValue+"';";

        Connection c = null;
        Statement stmt = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            // DriverManager.setLoginTimeout(5);
                 /* c = DriverManager.getConnection
                         ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword);
                */
            c =
                    DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                            "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));



//            c.setAutoCommit(false);

            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt = c.createStatement();
            stmt.executeUpdate(totalQry);

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        finally {

            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static String addSlashes(String s) {
        if(s==null){ return s; }
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\\n", "\\\\n");
        s = s.replaceAll("\\r", "\\\\r");
        s = s.replaceAll("\\00", "\\\\0");
        s = s.replaceAll("'", "\\\\'");
        return s;
    }

    public String urlEncode(String url) {

        try {
            String encodeURL= URLEncoder.encode( url, "UTF-8" );
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" +e.getMessage();
        }

    }

    public static String random9digits() {
        Random r = new Random(System.currentTimeMillis());
        int trr = ((1 + r.nextInt(9)) * 100000000 + r.nextInt(100000000));
        return "" + trr;
    }

    public Boolean checkConnectivity() {
        Boolean connectionStatus = false;
        Connection c = null;
        Statement stmt = null;
        ResultSet resultset = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Log.v("Connection","jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection + " - "+ this.mysqlConnectionUserId+" - "+this.mysqlConnectionPassword);
            // DriverManager.setLoginTimeout(5);
           /* c = DriverManager.getConnection
                    ("jdbc:mysql://"+ this.connectionhost +":"+ this.portNumber +"/"+this.mysqlDatabaseSelection,this.mysqlConnectionUserId,this.mysqlConnectionPassword); */
            c =
                    DriverManager.getConnection("jdbc:mysql://"+connectionhost+":"+portNumber+"/"+this.mysqlDatabaseSelection+"?characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Kolkata&" +
                            "user="+(this.mysqlConnectionUserId)+"&password="+(this.mysqlConnectionPassword));
            stmt = c.createStatement();
            resultset = stmt.executeQuery("SHOW DATABASES;");

            if (stmt.execute("SHOW DATABASES;")) {
                resultset = stmt.getResultSet();
            }

            while (resultset.next()) {
                System.out.println(resultset.getString("Database"));
            }

            c.setAutoCommit(false);
            connectionStatus = true;
        }catch (SQLException exp){
            Log.v("DBConnectivity","We couldn't connect to database because of SQL Exception");

            exp.printStackTrace();
            if(c!=null)
            {
                try {
                    c.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.v("DBConnectivity","We couldn't connect to database");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
//            System.exit(0);
        }finally {

            try {
                if(c != null )
                { c.close();}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connectionStatus;
    }


    private String escapeStringForMySQL(String s) {
        s = s.replace("\\", "\\\\");

        return s.replaceAll("\b","\\b")
                .replaceAll("\n","\\n")
                .replaceAll("\r", "\\r")
                .replaceAll("\t", "\\t")
                .replaceAll("\\x1A", "\\Z")
                .replaceAll("\\x00", "\\0")
                .replaceAll("'", "\\'")
                .replaceAll("\"", "\\\"");
    }
    public String getConnectionhost()
    {
        return this.connectionhost;
    }
}
