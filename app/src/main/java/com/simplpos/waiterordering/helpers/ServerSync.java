package com.simplpos.waiterordering.helpers;

import static com.simplpos.waiterordering.MenuScreen.isRunningCopy;

import android.content.ContentValues;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.SplashScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerSync {
    private DatabaseHelper dbHelper;
    private DatabaseVariables dbVar;
    private ArrayList<String> tablesForSync = new ArrayList<>();
    public ServerSync()
    {
        tablesForSync = new ArrayList<>();
        this.dbHelper = SplashScreen.dbHelper;
        this.dbVar = new DatabaseVariables();
        tablesForSync.add(DatabaseVariables.INVENTORY_TABLE);
        tablesForSync.add(DatabaseVariables.CATEGORY_TABLE);
        tablesForSync.add(DatabaseVariables.DEPARTMENT_TABLE);
        tablesForSync.add(DatabaseVariables.OPTIONAL_INFO_TABLE);
        tablesForSync.add(DatabaseVariables.STORE_PRODUCTS_TABLE);
        tablesForSync.add(DatabaseVariables.CAT_TAX_TABLE);
        tablesForSync.add(DatabaseVariables.TAX_TABLE);
        tablesForSync.add(DatabaseVariables.TABLE_INVENTORY_PRODUCT_IMAGES);
        tablesForSync.add(DatabaseVariables.EMPLOYEE_TABLE);
        tablesForSync.add(DatabaseVariables.EMP_PERMISSIONS_TABLE);
        tablesForSync.add(DatabaseVariables.ADVANCED_TABLE);
        tablesForSync.add(DatabaseVariables.EMP_STORE_TABLE);
        tablesForSync.add(DatabaseVariables.STORE_CATEGORY_TABLE);
        tablesForSync.add(DatabaseVariables.STORE_TABLE);
    }
    public void callBackgroundRefresh()
    {

        ArrayList<JSONObject> localItemsList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.INVENTORY_TABLE+" WHERE 1");
        Log.v("ServerCheck",localItemsList.toString());

        if(localItemsList.size()==0)
        {
            Log.v("ServerCheck","Returning because of null size");
        }else if(localItemsList.size()==1) {
            JSONObject localItemsObj = localItemsList.get(0);
            if (localItemsObj.has("totalcount")) {
                int localcount = localItemsObj.optInt("totalcount");
                if(localcount==0){

                    Log.v("ServerCheck","Returning because of zero size");
                }
            }
        }
        executeDeleteQueriesOnLocal();
        updateAndReplaceRecords(dbVar.INVENTORY_TABLE);
        updateAndReplaceRecords(dbVar.CATEGORY_TABLE);
        updateAndReplaceRecords(dbVar.DEPARTMENT_TABLE);
        updateAndReplaceRecords(dbVar.OPTIONAL_INFO_TABLE);
        updateAndReplaceRecords(dbVar.STORE_PRODUCTS_TABLE);
        updateAndReplaceRecords(dbVar.CAT_TAX_TABLE);
        updateAndReplaceRecords(dbVar.TAX_TABLE);
        updateAndReplaceRecords(dbVar.TABLE_INVENTORY_PRODUCT_IMAGES);
        updateAndReplaceRecords(dbVar.EMPLOYEE_TABLE);
        updateAndReplaceRecords(dbVar.EMP_PERMISSIONS_TABLE);
        updateAndReplaceRecords(dbVar.ADVANCED_TABLE);
        updateAndReplaceRecords(dbVar.EMP_STORE_TABLE);
        updateAndReplaceRecords(dbVar.STORE_CATEGORY_TABLE);
        updateAndReplaceRecords(dbVar.STORE_TABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            validateLicenseKey();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void validateLicenseKey() {
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        String MacAddress = SplashScreen.MacAddress;
        ArrayList<JSONObject> existingLicenseCheck = sqlCrmObj.executeRawqueryJSON("SELECT * FROM addon_counters_licenses WHERE mac_addr='"+MacAddress+"'");
        if(existingLicenseCheck.size()==0)
        {
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.LICENSE_KEY_VALID_UPTO});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.LICENSE_KEY});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.INV_SERIAL_NUMBER});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.INV_SERIAL_PREFIX});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.PREFERRED_PRINTER});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.PREFERRED_KOT_PRINTER});
            dbHelper.executeExecSQL("DELETE FROM "+dbHelper.CATEGORYWISE_PRINTING_TABLE+" WHERE 1");
        }else{
            JSONObject licenseCheckRow = existingLicenseCheck.get(0);
            String validUpto = licenseCheckRow.optString("valid_upto");
            String encryptedLicenseKey = licenseCheckRow.optString("license_key");
            String decryptedLicenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.DECRYPTED_LICENSE_KEY);//Java_AES_Cipher.DarKnight.getDecrypted(encryptedLicenseKey);
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.LICENSE_KEY_VALID_UPTO,validUpto);
            Log.v("Srinath","License Key is "+decryptedLicenseKey);

            try {
                JSONObject insertObj = new JSONObject();
                insertObj.put("devicetoken",dbVar.getValueForAttribute("device_token"));
                insertObj.put("employee_id",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                insertObj.put("updated_on",ConstantsAndUtilities.currentTime());

                sqlCrmObj.executeUpdate("addon_counters_licenses",insertObj,"mac_addr",MacAddress);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void executeDeleteQueriesOnLocal() {
        ArrayList<JSONObject> ServerRecordList =  dbVar.executeRawqueryJSON("SELECT * FROM delete_queries WHERE 1");

        if(ServerRecordList.size()>0)
        {
            for(int j=0; j < ServerRecordList.size(); j++)
            {
                JSONObject serverRecordObj = ServerRecordList.get(j);
                String tableNameForDelete = serverRecordObj.optString("tablename");
                String deleteQuery = serverRecordObj.optString("delete_query");
                if(tablesForSync.contains(tableNameForDelete)) {
                    try {
                        dbHelper.executeExecSQL(deleteQuery);
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }else{
                    Log.v("ServerSync","tablesForSync doesnot contain the table "+tableNameForDelete);
                }

            }
        }
    }

    private void updateAndReplaceRecords(String tableName) {
        if(isRunningCopy){ return;}
        ConstantsAndUtilities constantsAndUtilities = new ConstantsAndUtilities();
        try{
            if(MainActivity.mySqlObj.checkConnectivity()==false)
            {
                isRunningCopy = false;
                Log.v("ServerCheck","Unable to connect to database");return;
            }

                ArrayList<JSONObject> ServerRecordList =  dbVar.executeRawqueryJSON("SELECT * FROM "+tableName+" WHERE 1 ORDER BY _id ASC");
                if(ServerRecordList.size()>0)
                {
                    for(int b=0; b < ServerRecordList.size();b++)
                    {
                        JSONObject serverRecordObj = new JSONObject();
                        serverRecordObj  = ServerRecordList.get(b);
                        if(serverRecordObj.has("unique_id"))
                        {
                            try{
                                String uniqueId = serverRecordObj.optString("unique_id");
                                ArrayList<JSONObject> LocalRecordForUniqueId =  dbHelper.executeRawqueryJSON("SELECT * FROM "+tableName+" WHERE unique_id='"+uniqueId+"'");
                                if(LocalRecordForUniqueId.size()==0)
                                {
                                    ContentValues cv = constantsAndUtilities.convertJSONObjectToContentValues(serverRecordObj);
                                    dbHelper.insertData(cv,tableName);
                                }else{
                                    String ServermodifiedTimestamp = serverRecordObj.optString("modified_timestamp");
                                    String LocalmodifiedTimestamp = LocalRecordForUniqueId.get(0).optString("modified_timestamp");
                                    if(!LocalmodifiedTimestamp.equals(ServermodifiedTimestamp)) {
                                        String here = DatabaseVariables.UNIQUE_ID + "=?";
                                        ContentValues cv = constantsAndUtilities.convertJSONObjectToContentValues(serverRecordObj);

                                        dbHelper.executeUpdate(tableName, cv, here, new String[]{uniqueId});
                                    }
                                }
                            }catch (Exception exp){
                                exp.printStackTrace();
                            }
                        }
                    }
                }
        }catch (Exception exp){
            exp.printStackTrace();
            isRunningCopy = false; return;
        }
    }
}
