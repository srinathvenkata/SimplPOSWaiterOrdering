package com.simplpos.waiterordering;

import static com.simplpos.waiterordering.SplashScreen.dbHelper;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.simplpos.waiterordering.helpers.ConstantsAndUtilities;
import com.simplpos.waiterordering.helpers.DatabaseHelper;
import com.simplpos.waiterordering.helpers.DatabaseVariables;
import com.simplpos.waiterordering.helpers.DialogBox;
import com.simplpos.waiterordering.helpers.MySQLJDBC;
import com.simplpos.waiterordering.helpers.ServerSync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MenuScreen extends FragmentActivity implements Runnable{

    public DatabaseHelper dbHelper = null;
    WebView webView;
    SharedPreferences preferences = null;//  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
    DatabaseVariables dbVar = null;
    private ProgressDialog dialog;
    private boolean unequalQty = false;

    public static Boolean isRunningCopy = false;
    public static BackgroundRefreshTimerTask bgTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);





        Thread thread = new Thread(this);
        thread.start();

        dbHelper = SplashScreen.dbHelper;
        preferences =  PreferenceManager.getDefaultSharedPreferences(MenuScreen.this);
        dbVar = new DatabaseVariables();

        initiateConnections();
        initControls();
    }

    private void copyPreferencesFromServer() {
        Boolean autoPrintInvoice = false;
        Boolean autoPrintKOT = false;
        Boolean qtyInSecondColumn = false;
        Boolean roundOffInvoice = false;
        Boolean promptReasonForCancellation = false;
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;


        ArrayList<JSONObject> attributeResults = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+ ConstantsAndUtilities.INVOICE_AUTO_PRINTING_WITHOUT_PROMPT+"'");
        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            String returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);
            if(returnValue.equals("Yes")){ autoPrintInvoice = true; }
        }

        attributeResults = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+ ConstantsAndUtilities.KOT_AUTO_PRINTING_WITHOUT_PROMPT+"'");
        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            String returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);
            if(returnValue.equals("Yes")){ autoPrintKOT = true; }
        }

        attributeResults = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+ ConstantsAndUtilities.QTY_PRINT_AFTER_NAME+"'");
        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            String returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);
            if(returnValue.equals("Yes")){ qtyInSecondColumn = true; }
        }

        attributeResults = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='roundOff'");
        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            String returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);
            if(returnValue.equals("Yes")){ roundOffInvoice = true; }
        }

        attributeResults = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+ ConstantsAndUtilities.PROMPT_REASON_FOR_CANCELLATION+"'");

        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            String returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);
            if(returnValue.equals("true")){ promptReasonForCancellation = true; }
        }

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MenuScreen.this);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(ConstantsAndUtilities.PROMPT_INVOICE_PRINT, autoPrintInvoice);
        editor.putBoolean(ConstantsAndUtilities.PROMPT_KOT_PRINT, autoPrintKOT);
        editor.putBoolean(ConstantsAndUtilities.QTY_PRINT_AFTER_NAME, qtyInSecondColumn);
        editor.putBoolean(ConstantsAndUtilities.PROMPT_REASON_FOR_CANCELLATION,promptReasonForCancellation);
        editor.putBoolean("roundOff",roundOffInvoice);
        editor.commit();

    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void initControls() {
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        progressBar = findViewById(R.id.pb);
        webView = findViewById(R.id.webview);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MenuScreen.this);


        //setting webviewclient
        webView.setWebViewClient(new WebViewClient() {



            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
                webView.loadUrl("file:///android_asset/nointernet.html");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                progressBar.setVisibility(View.VISIBLE);
//                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.d("MainActivity", "SSL Error");
                super.onReceivedSslError(view, handler, error);
            }
        });
        //setting webchromeclient
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
//                progressBar.setProgress(newProgress);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Always grant permission since the app itself requires location
                // permission and the user has therefore already granted it
                callback.invoke(origin, true, false);
            }
        });
        //setting other settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);// For geolocation
        //checking internet connection
//        if (!isNetworkConnected()) {
//            webView.loadData("<html><body style='text-align:center;'><h1>Connection Error ...</h1><h2>Check Your Connection ... </h2></body></html>", "text/html", null);
//        } else


        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidInterface"); // To call methods in Android from using js in the html, AndroidInterface.showToast, AndroidInterface.getAndroidVersion etc

        //setting swiperefreshlistener
        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });*/

        webView.loadData("<html><body style='text-align:center;'><h1>Loading ...</h1></body></html>", "text/html", null);

        String connectivityAddress = sharedpreferences.getString("connectivityAddress","");
        String usagePurpose = sharedpreferences.getString("usagePurpose","");
//        retrieveUsersList();
        webView.loadUrl("file:///android_asset/menu.html");


        if(bgTimerTask == null){
            bgTimerTask = new BackgroundRefreshTimerTask();
        }
        if (bgTimerTask.hasStarted) {
//                bgTimerTask.cancel();
//                bgTimerTask.resetTask();
            System.out.println("Task already running");
        } else {
            bgTimerTask = new BackgroundRefreshTimerTask();
            Timer timer = new Timer();
            long delay = 0;
//                long intevalPeriod = 1800 * 1000;
//            long intevalPeriod = 30 * 60 * 1000;
            long intevalPeriod = 20 * 60 * 1000;
            // schedules the task to be run in an interval
            timer.scheduleAtFixedRate(bgTimerTask, delay,
                    intevalPeriod);
        }

    }

    private void initiateConnections() {
        Log.v("Menu","Logged in User is "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
        Log.v("Menu","Logged in User password "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERPASS));
        Log.v("Menu","Logged in User type "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE));
        dbVar.createTablesIfNotExists();

    }

    @Override
    public void run() {
        Log.v("Menu","Thread has started");
        copyPreferencesFromServer();
        checkServervsLocalDb();
    }

    private void checkServervsLocalDb() {
        ArrayList<JSONObject> localItemsList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.INVENTORY_TABLE+" WHERE 1");
        Log.v("ServerCheck",localItemsList.toString());
        ArrayList<JSONObject> serverItemsList =  dbVar.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.INVENTORY_TABLE+" WHERE 1");
        Log.v("ServerCheck",serverItemsList.toString());


        ArrayList<JSONObject> localCategoriesList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.CATEGORY_TABLE+" WHERE 1");
        Log.v("ServerCheck",localCategoriesList.toString());
        ArrayList<JSONObject> serverCategoriesList =  dbVar.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.CATEGORY_TABLE+" WHERE 1");
        Log.v("ServerCheck",serverCategoriesList.toString());


        ArrayList<JSONObject> localDepartmentsList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.DEPARTMENT_TABLE+" WHERE 1");
        Log.v("ServerCheck",localDepartmentsList.toString());
        ArrayList<JSONObject> serverDepartmentsList =  dbVar.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.DEPARTMENT_TABLE+" WHERE 1");
        Log.v("ServerCheck",serverDepartmentsList.toString());


        ArrayList<JSONObject> localOptionalInfoList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.OPTIONAL_INFO_TABLE+" WHERE 1");
        Log.v("ServerCheck",localOptionalInfoList.toString());
        ArrayList<JSONObject> serverOptionalInfoList =  dbVar.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.OPTIONAL_INFO_TABLE+" WHERE 1");
        Log.v("ServerCheck",serverOptionalInfoList.toString());


        ArrayList<JSONObject> localStoreProductPricingList =  dbHelper.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.STORE_PRODUCTS_TABLE+" WHERE 1");
        Log.v("ServerCheck",localStoreProductPricingList.toString());
        ArrayList<JSONObject> serverStoreProductPricingList =  dbVar.executeRawqueryJSON("SELECT COUNT(_id) as totalcount FROM "+dbVar.STORE_PRODUCTS_TABLE+" WHERE 1");
        Log.v("ServerCheck",serverStoreProductPricingList.toString());

        if(localItemsList.size()==0)
        {
            unequalQty = true;
        }else if(localItemsList.size()==1){
            JSONObject serverItemsObj = serverItemsList.get(0);
            JSONObject localItemsObj = localItemsList.get(0);
            if(localItemsObj.has("totalcount") && serverItemsObj.has("totalcount"))
            {
                Log.v("ServerCheck","We are within if having total counts");
                int localcount = localItemsObj.optInt("totalcount");
                int serverRecordsCount = serverItemsObj.optInt("totalcount");
                Log.v("ServerCheck",localcount+" is the local count "+serverRecordsCount+" is the server records count");
                if(localcount != serverRecordsCount){
                    unequalQty = true;
                }else{
                    Log.v("ServerCheck","Local quantity and server quantity are equal");

                    unequalQty = false;
                }

            }else{                unequalQty = true;
                Log.v("ServerCheck","We are within else having total counts");

            }
        }
        Log.v("ServerCheck","Unequal quantities is Run method is "+unequalQty);

    }


    public class WebAppInterface {
            Context mContext;
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MenuScreen.this);

            // Instantiate the interface and set the context
            WebAppInterface(Context c) {
                mContext = c;
                sharedpreferences = PreferenceManager
                        .getDefaultSharedPreferences(mContext);


            }

            @JavascriptInterface
            public void printLog(String msg) {
            Log.v("Printing", msg);
        }

        @JavascriptInterface
        public String userPermissionCheck(String permissionFor)
        {
            String permissionEnabled = "false";


            String loggedinusertype = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE);
            String userid = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID);

            if(loggedinusertype.equals("admin")){
                return "true";
            }
            else {
                ArrayList<JSONObject> employeePermissions = dbHelper.executeRawqueryJSON(
                        "select * from " + dbVar.EMP_PERMISSIONS_TABLE
                                + " where " + dbVar.EMPLOYEE_EMPLOYEE_ID
                                + "=\"" + userid + "\"");
                JSONObject currentRow = employeePermissions.get(0);
                if(currentRow.optString("emp_"+permissionFor).equals("Enable"))
                {
                    permissionEnabled = "true";
                }

                if(!permissionEnabled.equals("true"))
                {
                    ArrayList<JSONObject> employeeAdvancedPermissionCheck = dbVar.executeRawqueryJSON(
                            "select * from " + dbVar.ADVANCED_TABLE
                                    + " where " + dbVar.ADVANCED_EMPLOYEE_ID
                                    + "=\"" + userid + "\" AND " + dbVar.ADVANCED_PERMISSIONS
                                    + " LIKE \"%"+permissionFor+"%\"");
                    if (employeeAdvancedPermissionCheck!=null && employeeAdvancedPermissionCheck.size() > 0) {
                        permissionEnabled= "true";
                    }
                }
            }
            return permissionEnabled;
        }
            @JavascriptInterface
            public void goToHardware()
            {

                String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
                if(licenseKey.equals(""))
                {
                    showAlertDialogJS("Permission Denied","You donot have a valid license key");
                    return;
                }
                // check if user has permissions
                if(userPermissionCheck("settings").equals("false"))
                {
                    showAlertDialogJS("Permission Denied","You donot have the permissions to change hardware settings");
                    return;
                }

                Intent intent;
                intent = new Intent(MenuScreen.this, Hardware.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            @JavascriptInterface
            public void goToSettings()
            {

                Intent intent;
                intent = new Intent(MenuScreen.this, Settings.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            @JavascriptInterface
            public void goToPOS()
            {
                Log.v("ServerCheck","Unequal quantities is "+unequalQty);
                if(unequalQty==true)
                {
                    showAlertDialogJS("Error ! Products Mismatch","Please click on copy data from server");
                    return;
                }
                /*
                if(isLocationEnabled(getApplicationContext()))
                {
                    Log.v("Printing","Location is enabled");
                }else{
                    Log.v("Printing","Location is disabled");
                    Toast.makeText(getApplicationContext(), "Please enable your location to access wifi state", Toast.LENGTH_SHORT).show();
                    showAlertDialogJS("Location Access Error","Please enable your location");
                    return;
                }
                String mySSID = setSSIDName();
                if(!mySSID.equals("<unknown ssid>"))
                {
                    Log.v("Printing","Ssid is "+mySSID);
                }else{
                    Log.v("Printing","Location is disabled");
                    Toast.makeText(getApplicationContext(), "Please enable your location to access wifi state", Toast.LENGTH_SHORT).show();
                    showAlertDialogJS("Wifi Access Error","Please enable your location");
                    return;
                } */

                Intent intent;
                intent = new Intent(MenuScreen.this, POSWebActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }


            @JavascriptInterface
            public void showAlertDialogJS(String titleMessage,String contentMessage)
            {
                DialogBox dbx = new DialogBox();
                dbx.dialogBox(titleMessage,contentMessage, "OK","Cancel",mContext);
            }


        @JavascriptInterface
        public void showLoadingDialog()
        {

            // called before request is started
            dialog = new ProgressDialog(MenuScreen.this);
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.setCanceledOnTouchOutside(false);
        }
        @JavascriptInterface
        public void stopLoadingDialog(){
            try {
                dialog.hide();
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
            @JavascriptInterface
            public void copyProductsData()
            {

                Log.v("ServerCheck","We have to copy the product data");
                isRunningCopy = true;
                deleteAndReplaceRecords(DatabaseVariables.INVENTORY_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.CATEGORY_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.DEPARTMENT_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.OPTIONAL_INFO_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.STORE_PRODUCTS_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.CAT_TAX_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.TAX_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.TABLE_INVENTORY_PRODUCT_IMAGES);
                deleteAndReplaceRecords(DatabaseVariables.EMPLOYEE_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.EMP_PERMISSIONS_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.ADVANCED_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.EMP_STORE_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.STORE_CATEGORY_TABLE);
                deleteAndReplaceRecords(DatabaseVariables.STORE_TABLE);
                isRunningCopy = false;

                unequalQty = false;

                Toast.makeText(getApplicationContext(), "Copy Completed", Toast.LENGTH_SHORT).show();

            }
            @JavascriptInterface
            public void logoutUser()
            {

                String here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
                String uniq = ConstantsAndUtilities.SP_LOGGEDINUSERID;
                dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                        here, new String[]{uniq});


                here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
                uniq = ConstantsAndUtilities.SP_LOGGEDINUSERPASS;
                dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                        here, new String[]{uniq});


                here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
                uniq = ConstantsAndUtilities.SP_LOGGEDINUSERTYPE;
                dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                        here, new String[]{uniq});
                ConstantsAndUtilities.userid = "";
                ConstantsAndUtilities.usertype = "";
                ConstantsAndUtilities.useridPass = "";
                ConstantsAndUtilities.loggedinusertype = "";
                finish();
                Intent intent;
                intent = new Intent(MenuScreen.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        private void deleteAndReplaceRecords(String tableName)
        {
            ArrayList<JSONObject> serverItemsList =  dbVar.executeRawqueryJSON("SELECT * FROM "+tableName+" WHERE 1 ORDER BY _ID ASC");
            if(serverItemsList.size()>0)
            {
                dbHelper.executeExecSQL("DELETE FROM "+tableName+" WHERE 1");
                for(int j=0; j < serverItemsList.size(); j++)
                {
                    JSONObject currentServerItem = serverItemsList.get(j);

                    ConstantsAndUtilities cv = new ConstantsAndUtilities();
                    ContentValues contentValues = new ContentValues();
                    contentValues = cv.convertJSONObjectToContentValues(currentServerItem);
                    String primaryKey = currentServerItem.optString("_id");
                    String here = "_id" + "=?";
                    String uniq = primaryKey;
                        /*dbHelper.executeDelete(dbVar.INVENTORY_TABLE,
                                here, new String[]{uniq});*/
                    dbHelper.insertData(contentValues,tableName);

                }
            }
        }

        public  static class BackgroundRefreshTimerTask extends TimerTask {

            private boolean hasStarted = false;

            @Override
            public void run() {

                this.hasStarted = true;
                //rest of run logic here...
                System.out.println("Background refresh timer started");
                OneTimeThreads R1 = new OneTimeThreads("Background Refresh");
                R1.start();
            }

            public boolean hasRunStarted() {
                return this.hasStarted;
            }

            public void resetTask() {
                this.hasStarted = false;
            }
        }


        public static  class  OneTimeThreads implements Runnable{

            private Thread t;
            private String threadName;

            OneTimeThreads( String name) {
                threadName = name;
            }


            @Override
            public void run() {
                try {

                    if(threadName.equals("Background Refresh")){
                        ServerSync serverSyncObj = new ServerSync();
                        System.out.println("Background Refresh timer called at "+ConstantsAndUtilities.currentTime());

                        serverSyncObj.callBackgroundRefresh();
                    }
                    Thread.sleep(50);

                } catch (InterruptedException e) {
                    System.out.println("Thread " +  threadName + " interrupted.");
                    e.printStackTrace();
                }
            }

            public void start() {
                if (t == null) {
                    t = new Thread (this, threadName);
                    t.start ();
                }
            }
        }


    public String setSSIDName()
    {
        return "";
        /*
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        System.out.println(wifiManager.toString());
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid  = info.getSSID();
        ssid = ssid. replaceAll("^\"|\"$", "");
        Log.v("Printing","Ssid name is "+ssid);
        return ssid;

         */
    }
    @SuppressWarnings("deprecation")
    public static Boolean isLocationEnabled(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            return false;
        }
    }
    }