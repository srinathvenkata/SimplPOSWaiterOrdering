package com.simplpos.waiterordering;

import static android.os.Build.VERSION.SDK_INT;


import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.simplpos.waiterordering.helpers.ConstantsAndUtilities;
import com.simplpos.waiterordering.helpers.CustomersManager;
import com.simplpos.waiterordering.helpers.DatabaseHelper;
import com.simplpos.waiterordering.helpers.DatabaseVariables;
import com.simplpos.waiterordering.helpers.DialogBox;

import com.simplpos.waiterordering.helpers.ItemCancellationReasons;
import com.simplpos.waiterordering.helpers.MyApplication;
import com.simplpos.waiterordering.helpers.POSWebPrinting;
import com.simplpos.waiterordering.helpers.SaveFetchVoidInvoice;
import com.simplpos.waiterordering.helpers.ServerSync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.prefs.Preferences;

import cz.msebera.android.httpclient.Header;

public class POSWebActivity extends FragmentActivity {

    public DatabaseHelper dbHelper = null;
    WebView webView;
    SharedPreferences preferences = null;//  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
    DatabaseVariables dbVar = null;
    private ProgressDialog dialog;
    String screenOrientation = "landscape";
    private String userid="";
    private String userpass="";
    private String usertype="";
    private boolean itemsListPrint = true;
    private boolean isServerPrint = true;
    private boolean isServerPrintOnlyForInvoice = false;
    private boolean isXamppPrint = false;
    private boolean kotXamppPrint = false;

    private String invoiceFetchTime="";

    public static String ApachePortNumber="";
    public static String printInvoiceId = "";
    public static JSONObject printInfoObj = new JSONObject();

    private static Boolean isSavingInvoice = false;
    private ArrayList<String> storearray = new ArrayList<String>();
    private ArrayList<String> storearrayid = new ArrayList<String>();

    public static HashMap<String,String> allImagesHolder = new HashMap<>();
    private boolean fetchingInvoice = false;

    public static String selectedOrderType="store sale";
    private static Context posContext = null;
    public static String ssid="";
    public static JSONObject assignmentOfPrinters = new JSONObject();

    public static void showAlertDialogForDatabaseDisconnectivity()
    {
        DialogBox dbx = new DialogBox();

        dbx.dialogBox("Connection Error","Couldnot connect to database", "OK","Cancel", posContext);

    }
    public static void showAlertDialogforPrint(Context printerContext, String title, JSONObject printInfoObj, String printingType) {
        Log.v("Reprint","Connection Failed");


        AlertDialog alertDialog = new AlertDialog.Builder(printerContext).create();

        alertDialog.setTitle(title);

        alertDialog.setMessage("Please check the IP address and Status of Apache Server in Xampp. Please check the connectivity with the server.");

        alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                POSWebPrinting.printFromServer(printInfoObj,printingType);
                dialog.dismiss();
            }
        });
        alertDialog.setButton2("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posweb);
        RelativeLayout layout = findViewById(R.id.posWebActivity);


        dbHelper = SplashScreen.dbHelper;
        DatabaseVariables dbVar = new DatabaseVariables();
        screenOrientation = dbVar.getValueForAttribute(ConstantsAndUtilities.SCREENORIENTATION);
        if(screenOrientation.equals("portrait")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }


        layout.setOnTouchListener(new OnSwipeTouchListener(POSWebActivity.this) {

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Toast.makeText(POSWebActivity.this, "Swipe Left gesture detected", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Toast.makeText(POSWebActivity.this, "Swipe Right gesture detected", Toast.LENGTH_SHORT).show();
            }
        });

        initiateConnections();
        initControls();
        preferences =  PreferenceManager.getDefaultSharedPreferences(POSWebActivity.this);
        dbVar = new DatabaseVariables();
        posContext = POSWebActivity.this;
        isSavingInvoice = false;

    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onResume() {

        super.onResume();
        Log.v("Testing", "Screen has resumed");
    }


    private void initControls()
    {
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        progressBar = findViewById(R.id.pb);
        webView = findViewById(R.id.pos_webview);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(POSWebActivity.this);

        ApachePortNumber = sharedpreferences.getString(ConstantsAndUtilities.APACHE_PORT_NUMBER,"");

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

        webView.addJavascriptInterface(new POSWebActivity.WebAppInterface(this), "AndroidInterface"); // To call methods in Android from using js in the html, AndroidInterface.showToast, AndroidInterface.getAndroidVersion etc

        webView.loadData("<html><body style='text-align:center;'><h1>Loading ...</h1></body></html>", "text/html", null);

        String connectivityAddress = sharedpreferences.getString("connectivityAddress","");
        String usagePurpose = sharedpreferences.getString("usagePurpose","");
//        retrieveUsersList();

        if(screenOrientation.equals("portrait")) {
            webView.loadUrl("file:///android_asset/pos_portrait_view.html");
        }else{
            webView.loadUrl("file:///android_asset/pos_landscape_view.html");
        }
        ConstantsAndUtilities cv = new ConstantsAndUtilities();
        assignmentOfPrinters = cv.StringToJSONObj(SplashScreen.printersByNetworkWifiAssignment);

    }

    private void initiateConnections() {
        dbVar = new DatabaseVariables();
        userid = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE);
        userpass = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERPASS);
        usertype = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE);

        ConstantsAndUtilities.printerContext = this;
    }

    public class WebAppInterface {
        Context mContext;
        SharedPreferences sharedpreferences;

        // Instantiate the interface and set the context
        WebAppInterface(Context c) {
            mContext = c;
            sharedpreferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);


        }
        @JavascriptInterface
        public boolean isUserAdmin(){
            return getAdminStatus();
        }
        // Show a toast from the web page
        @JavascriptInterface
        public void submitHeight(String ht){
            Log.v("Printing","Webview Returned height is "+ht);
        }
        @JavascriptInterface
        public String imageNameForProductId(String productId)
        {
            String imageName = "";
            try{
                imageName = (String) allImagesHolder.get(productId);
//                Log.v("Img Check",imageName+" is the image");
            }catch(Exception exp)
            {
//                Log.v("Img Check","With exception " + exp.getMessage());
            }
            return imageName;
        }
        @JavascriptInterface
        public void windowFocusCallFoodPartners()
        {
            Log.v("Notifications","Received the notification");
            /*FoodPartners fp = new FoodPartners();
            fp.callFoodPartnersFromPOS();*/
        }
        @JavascriptInterface
        public void sendHeightAndPrintCall()
        {

        }
        @JavascriptInterface
        public void callCameraBarcodeScan()
        {
            Boolean hasCamera = false;
            int numCameras = Camera.getNumberOfCameras();
            if (numCameras > 0) {
                hasCamera = true;
                goForBarcodeScan();
            }
            else{
                showToast("No Camera Found");
            }

        }
        @JavascriptInterface
        public void hideTheKeyboard()
        {

        }

        @JavascriptInterface
        public String existingTablesForShifting(String storeId){
            String tablesList = "";
            JSONObject sendTablesList = new JSONObject();
            ArrayList<JSONObject> recallInvoicesList = dbVar.executeRawqueryJSON("SELECT * FROM invoice_total_table WHERE status='hold' AND store_id='"+storeId+"' ORDER BY holdid ASC");
            ArrayList<JSONObject> alltablesList = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.TABLE_SETTINGS_TABLE+" WHERE store_id='"+storeId+"' ORDER BY table_name ASC");

            try {
                sendTablesList.put("holdinvoices", recallInvoicesList);
                sendTablesList.put("allTables", alltablesList);
            }catch (JSONException exp){

            }
            return sendTablesList.toString();
        }

        @JavascriptInterface
        public void printerConnectivityCheck(){

            establishPrinterConnectionsWeb();
        }
        @JavascriptInterface
        public void selectDateTime(){
            Log.v("Srinath","Select date time called");
            showDateTimePicker();
        }
        @JavascriptInterface
        public void printLog(String tag,String msg)
        {
            Log.v(tag,msg);
        }

        @JavascriptInterface
        public void establishPrinterConnectionsJS(){
            establishPrinterConnectionsWeb();
        }
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }
        @JavascriptInterface
        public String getFileContents(String filePath){
            return readAssetsContent(getApplicationContext(),"jquery.js");
        }
        public String readAssetsContent(Context context, String name) {
            BufferedReader in = null;
            try {
                StringBuilder buf = new StringBuilder();
                InputStream is = context.getAssets().open(name);
                in = new BufferedReader(new InputStreamReader(is));

                String str;
                boolean isFirst = true;
                while ( (str = in.readLine()) != null ) {
                    if (isFirst)
                        isFirst = false;
                    else
                        buf.append('\n');
                    buf.append(str);
                }
                return buf.toString();
            } catch (IOException e) {
                Log.e("error", "Error opening asset " + name);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("error", "Error closing asset " + name);
                    }
                }
            }

            return null;
        }
        @JavascriptInterface
        public void showAlertDialogJS(String titleMessage,String contentMessage)
        {
            DialogBox dbx = new DialogBox();
            dbx.dialogBox(titleMessage,contentMessage, "OK","Cancel",mContext);
        }
        @JavascriptInterface
        public String initializeParameters()
        {
            JSONObject parametersOfUser = new JSONObject();
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);
            userid = ConstantsAndUtilities.userid;
            usertype = ConstantsAndUtilities.usertype;
            userpass = ConstantsAndUtilities.useridPass;

            // items_list_print_type
            itemsListPrint = sharedPreferences.getBoolean(dbVar.ITEMLIST_PRINT, true);

            try {

                parametersOfUser.put("dinein_enabled",sharedPreferences.getBoolean("dineCheck", true));
                parametersOfUser.put("takeaway_enabled",sharedPreferences.getBoolean("tackCheck", true));
                parametersOfUser.put("homedelivery_enabled",sharedPreferences.getBoolean("homeCheck", false));
                if (!TextUtils.isEmpty(sharedPreferences.getString("tacktext", ""))){
                    parametersOfUser.put("takeaway_text",sharedPreferences.getString("tacktext", "Take Away"));
                }else{ parametersOfUser.put("takeaway_text","Take Away");}
                if (!TextUtils.isEmpty(sharedPreferences.getString("dinetext", ""))){
                    parametersOfUser.put("dinein_text",sharedPreferences.getString("dinetext", "Dine In"));
                }else{ parametersOfUser.put("dinetext","Dine In");}
                if (!TextUtils.isEmpty(sharedPreferences.getString("hometext", ""))){
                    parametersOfUser.put("homedelivery_text",sharedPreferences.getString("hometext", "Home Delivery"));
                }else{ parametersOfUser.put("homedelivery_text","Home Delivery");}

                String currency = "INR";
                try {
                    ArrayList<JSONObject> currencyPreferences = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.PREFERENCES +
                            " WHERE " + dbVar.PRE_ATTRIBUTE + "='currency'");
                    if (currencyPreferences.size() > 0) {
                        currency = (String) currencyPreferences.get(0).get("value");
                    }
                }catch (Exception exp){
                    exp.printStackTrace();
                }
                switch (currency){
                    case "USD" :
                        dbVar.currencyTypehtml = "$";
                        break;
                    case "INR" :
                        dbVar.currencyTypehtml = "&#8377;";
                        break;
                    default:
                        dbVar.currencyTypehtml = "&#8377;";
                        break;
                }
                parametersOfUser.put("currency",currency);
                parametersOfUser.put("currencyType",dbVar.currencyTypehtml);

                parametersOfUser.put("locallanguageinpos",dbVar.PS_VALUE);
                Boolean enableLocal = sharedPreferences.getBoolean(dbVar.PL_TEXT, false);
                parametersOfUser.put("locallanguageinprinting",enableLocal.toString());
                parametersOfUser.put("loggedInUser" ,ConstantsAndUtilities.userid);
                parametersOfUser.put("loggedInUserType" ,ConstantsAndUtilities.usertype);
                ArrayList<JSONObject> employeeAdvancedPermissions = dbHelper.executeRawqueryJSON(
                        "select * from " + dbVar.ADVANCED_TABLE
                                + " where " + dbVar.ADVANCED_EMPLOYEE_ID
                                + "=\"" + userid + "\"");
                parametersOfUser.put("employee_advanced_permissions",employeeAdvancedPermissions);
                ArrayList<JSONObject> employeePermissions = dbHelper.executeRawqueryJSON(
                        "select * from " + dbVar.EMP_PERMISSIONS_TABLE
                                + " where " + dbVar.EMPLOYEE_EMPLOYEE_ID
                                + "=\"" + userid + "\"");
                parametersOfUser.put("employee_permissions",employeePermissions);
                if (sharedPreferences.getBoolean(ConstantsAndUtilities.DISPLAY_IMAGES_IN_POS, false)) { parametersOfUser.put("displayImages" ,true); }
                else{ parametersOfUser.put("displayImages" ,false); }

                if (sharedPreferences.getBoolean("roundOff", true)) { parametersOfUser.put("roundOff" ,true); }
                else{ parametersOfUser.put("roundOff" ,false); }

                if(sharedPreferences.getBoolean("categoryfilter", true))
                {
                    parametersOfUser.put("CategoryFilter",false);
                }else{ parametersOfUser.put("CategoryFilter",true);}

                if(sharedPreferences.getBoolean("tableSelectionButton", true))
                {
                    parametersOfUser.put("tableSelectionButton",true);
                }else{ parametersOfUser.put("tableSelectionButton",false);}


                parametersOfUser.put("CategoryFilter",sharedPreferences.getBoolean("categoryfilter", true));
                parametersOfUser.put("OrderType",sharedPreferences.getString("ordertype", "store sale"));

                itemsListPrint = false;

                ArrayList<JSONObject> itemsListPrintingCheck = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM  store_preferences WHERE attribute='items_list_print_type'");
                Log.v("Preferences",itemsListPrintingCheck.toString());
                if(itemsListPrintingCheck.size()==1)
                {
                    JSONObject itemsListPrintingSelectionRow = itemsListPrintingCheck.get(0);
                    if(itemsListPrintingSelectionRow.has("value"))
                    {
                        itemsListPrint = true;
                    }
                }
                parametersOfUser.put("itemsListPrint",itemsListPrint);

                ArrayList<JSONObject> serverPrintingCheck = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM  store_preferences WHERE attribute='"+ ConstantsAndUtilities.SERVER_PRINT +"'");
                Log.v("Preferences",serverPrintingCheck.toString());
                isServerPrint = false;
                if(serverPrintingCheck.size()==1)
                {
                    JSONObject serverPrintSelectionRow = serverPrintingCheck.get(0);
                    if(serverPrintSelectionRow.has("value"))
                    {
                        if(serverPrintSelectionRow.get("value").equals("true")){ isServerPrint = true; }
                    }
                }
                parametersOfUser.put("isServerPrint",isServerPrint);
                parametersOfUser.put("prompt_reason_for_cancellation",sharedPreferences.getBoolean(ConstantsAndUtilities.PROMPT_REASON_FOR_CANCELLATION,false));

                ArrayList<JSONObject> serverPrintingOnlyForInvoiceCheck = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM  store_preferences WHERE attribute='"+ ConstantsAndUtilities.SERVER_PRINT_ONLY_FOR_INVOICE +"'");
                Log.v("Preferences",serverPrintingOnlyForInvoiceCheck.toString());
                isServerPrintOnlyForInvoice = false;
                if(serverPrintingOnlyForInvoiceCheck.size()==1)
                {
                    JSONObject serverPrintSelectionRow = serverPrintingOnlyForInvoiceCheck.get(0);
                    if(serverPrintSelectionRow.has("value"))
                    {
                        if(serverPrintSelectionRow.get("value").equals("true")){ isServerPrintOnlyForInvoice = true; }
                    }
                }
                parametersOfUser.put("isServerPrintOnlyForInvoice",isServerPrintOnlyForInvoice);

                parametersOfUser.put("autoprint_invoice",sharedPreferences.getBoolean(ConstantsAndUtilities.PROMPT_INVOICE_PRINT, false));
                parametersOfUser.put("autoprint_kot",sharedPreferences.getBoolean(ConstantsAndUtilities.PROMPT_KOT_PRINT, false));
                parametersOfUser.put("external_keyboard_usage",sharedPreferences.getBoolean(ConstantsAndUtilities.EXTERNAL_KEYBOARD_USAGE, false));

                JSONArray userStores = new JSONArray();

                storearray = new ArrayList<String>();
                storearrayid = new ArrayList<String>();
                if(usertype=="admin")
                {
                    ArrayList<JSONObject> adminStores = dbHelper.executeRawqueryJSON(
                            "select * from " + dbVar.STORE_TABLE);
                    if(adminStores!=null && adminStores.size()>0)
                    {
                        for(int cnt=0; cnt<adminStores.size();cnt++)
                        {
                            JSONObject currentRow = adminStores.get(cnt);
                            JSONObject insertStoreInfo = new JSONObject();
                            String storename = currentRow.optString(dbVar.STORE_NAME);
                            String storeid = currentRow.optString(dbVar.STORE_ID);
                            insertStoreInfo.put("StoreName",storename);
                            insertStoreInfo.put("StoreId",storeid);
                            userStores.put(cnt,insertStoreInfo);
                        }
                    }

                }
                else{
                    ArrayList<JSONObject> employeeStores = dbHelper.executeRawqueryJSON(
                            "select * from " + dbVar.EMP_STORE_TABLE
                                    + " where " + dbVar.EMPLOYEE_EMPLOYEE_ID
                                    + "=\"" + userid + "\"");
                    if(employeeStores!=null && employeeStores.size()>0 )
                    {
                        for(int ji = 0 ; ji < employeeStores.size(); ji++)
                        {
                            JSONObject result = employeeStores.get(ji);
                            String storeid = result.optString(dbVar.EMP_STORE_ID);
                            String storename = result.optString(dbVar.EMP_STORE_NAME);

                            JSONObject insertStoreInfo = new JSONObject();
                            insertStoreInfo.put("StoreName",storename);
                            insertStoreInfo.put("StoreId",storeid);
                            userStores.put(ji,insertStoreInfo);

                        }
                    }
                }
                parametersOfUser.put("userAssignedStores",userStores);
                String feedbackUrl = sharedPreferences.getString(ConstantsAndUtilities.FEEDBACK, "");

                parametersOfUser.put("feedbackurl",feedbackUrl);

                ArrayList<JSONObject> customerSelectionCheck = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT attribute,value FROM  store_preferences WHERE attribute='"+ConstantsAndUtilities.CUSTOMER_SELECTION_MANDATORY+"'");// attribute='"+ ConstantsAndUtilities.CUSTOMER_SELECTION_MANDATORY +"'");
                Boolean customerSelectionVal = false;
                if(customerSelectionCheck.size()==1)
                {
                    JSONObject customerSelectionRow = customerSelectionCheck.get(0);
                    if(customerSelectionRow.has("value"))
                    {
                        if(customerSelectionRow.get("value").equals("true")){ customerSelectionVal = true; }
                    }
                }
                parametersOfUser.put("mandatory_customer_selection",customerSelectionVal);

                String selectQuery = "SELECT  * FROM payment_types WHERE 1";

                ArrayList<JSONObject> paymentTable = MainActivity.mySqlCrmObj.executeRawqueryJSON(selectQuery);

                JSONArray paymentRows = new JSONArray();
                if(paymentTable!=null && paymentTable.size()>0 ) {

                    for (int cnt = 0; cnt < paymentTable.size(); cnt++) {

                        JSONObject currentRow = paymentTable.get(cnt);
                        paymentRows.put((paymentRows.length()),currentRow);
                    }
                }
                parametersOfUser.put("paymentTypes",paymentRows);
                ArrayList<JSONObject> visibleDepts = dbHelper.executeRawqueryJSON(
                        "select * from "
                                + DatabaseVariables.DEPARTMENT_TABLE + " where "
                                + DatabaseVariables.FoodstampableForDept + "!=\"yes\" and " + DatabaseVariables.CHECKED_VALUE + "=\"true\" ORDER BY department_id ASC");
                JSONArray deptrows = new JSONArray();
                if(visibleDepts!=null && visibleDepts.size()>0 ) {
                    for (int cnt = 0; cnt < visibleDepts.size(); cnt++) {
                        JSONObject currentRow = visibleDepts.get(cnt);
                        String catid = currentRow.optString(DatabaseVariables.DepartmentID);
                        String check = currentRow.optString(DatabaseVariables.CHECKED_VALUE);
                        if (check.equals("true")) {
                            deptrows.put((deptrows.length()),currentRow);
                        }
                    }
                }

                parametersOfUser.put("departments",deptrows);

                /*ArrayList<JSONObject> updateStoreCategoriesList = dbVar.executeRawqueryJSON(
                        "select " + dbVar.STORE_CATEGORY_ID + " from " + dbVar.STORE_CATEGORY_TABLE + " where "
                                + dbVar.STORE_CATEGORY_STOREID + "='" + id + "'");*/

                ArrayList<JSONObject> updateStoreCategoriesList = dbHelper.executeRawqueryJSON(
                        "select " + DatabaseVariables.CategoryId + ",unique_id from " + DatabaseVariables.CATEGORY_TABLE+" ORDER BY " + DatabaseVariables.CategoryId + " ASC");
                parametersOfUser.put("categories",updateStoreCategoriesList);
//                parametersOfUser.put("pathToImages", Environment.getExternalStorageDirectory()+ "/SIMPLPOS/ProductImages");

                // New File access for Android 'R' and above
                if (SDK_INT >= Build.VERSION_CODES.R) { 
                    parametersOfUser.put("pathToImages", getApplicationContext().getFilesDir()+ "/SIMPLPOS/ProductImages");
                }else{
                    parametersOfUser.put("pathToImages", Environment.getExternalStorageDirectory()+ "/SIMPLPOS/ProductImages");
                }

            } catch (JSONException e) {
                Log.v("Srinath",e.toString());
                e.printStackTrace();
            }


            if (sharedPreferences.getBoolean(ConstantsAndUtilities.SERVER_PRINT, false)) {  isServerPrint = true;  }
            if (sharedPreferences.getBoolean(ConstantsAndUtilities.XAMPP_PRINT, false)) {  isXamppPrint = true; }
            if (sharedPreferences.getBoolean(ConstantsAndUtilities.KOT_XAMPP_PRINT, false)) {  kotXamppPrint = true;}

            // Weighing scale
            String prefix = sharedPreferences.getString("w_prefix", "");
            String item_length = sharedPreferences.getString("item_length", "");
            String w_length = sharedPreferences.getString("w_length", "");
            String price_length = sharedPreferences.getString("price_length", "");
            String unit_suffix = sharedPreferences.getString("unit_suffix", "");
            String w_suffix = sharedPreferences.getString("w_suffix", "");
            try {
                parametersOfUser.put("weighingscale_enabled",sharedPreferences.getBoolean("weight_scale", false));
                parametersOfUser.put("weighingscale_prefix",prefix);
                parametersOfUser.put("weighingscale_item_length",item_length);
                parametersOfUser.put("weighingscale_weight_length",w_length);
                parametersOfUser.put("weighingscale_price_length",price_length);
                parametersOfUser.put("weighingscale_unit_suffix",unit_suffix);
                parametersOfUser.put("weighingscale_weight_suffix",w_suffix);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            clearCancellationReasons();
            Log.v("Console",parametersOfUser.toString());
            return parametersOfUser.toString();
        }
        @JavascriptInterface
        public void saveStoreId(String storeId)
        {
            Log.v("Fetch","Store selection clicked "+storeId);
            /*String qry = "update "
                    + dbVar.MISCELLANEOUS_TABLE + " set "
                    + dbVar.MISCEL_STORE + "=\""
                    + storeId + "\"";
            ConstantsAndUtilities.store_id = storeId;
            dbVar.executeExecSQL(qry);*/
        }
        @JavascriptInterface
        public void feedbackForInvoice(String invoiceId){

            String feedbackUrl = sharedpreferences.getString(ConstantsAndUtilities.FEEDBACK, "");
            String url=feedbackUrl+"?invoiceid="+invoiceId+"&userid="+userid;
            /*Intent intent=new Intent(POSWebActivity.this, FeedbackActivity.class);
            intent.putExtra(ConstantsAndUtilities.FEEDBACK, url);

            startActivity(intent);

            POSWebActivity.this.finish();*/
        }
        @JavascriptInterface
        public String saveTheCustomer(String formDataOfCustomer,String storeId)
        {
            String savedCustomer =  SaveFetchVoidInvoice.saveTheCustomer(formDataOfCustomer,storeId);
            return savedCustomer;
        }
        @JavascriptInterface
        public void fetchLatestNotifications()
        {
//            Log.v("MyFirebaseMsgService", String.valueOf(notificationDetails));

        }
        @JavascriptInterface
        public boolean serverInvoiceEditCheck(String holdInvoiceId, final String typeOfEdit)
        {
            String ipAddress = sharedpreferences.getString("IPAddress", "");
            Log.v("Testing","Invoice fetch time is "+invoiceFetchTime);
            if(ipAddress.equals("")) {

                Boolean invoiceEditedAfterFetch = false;


                ArrayList<JSONObject> invoiceDetails = dbVar.executeRawqueryJSON("SELECT "+dbVar.MODIFIED_DATE+" FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + holdInvoiceId + "' ");
                if(invoiceDetails.size()==0)
                {
                    return false;
                }else{
                    showLoadingDialog();
                    JSONObject invObj = invoiceDetails.get(0);
                    String modifiedTimeOfInvoice = invObj.optString(dbVar.MODIFIED_DATE);
                    Log.v("Testing","Modified time of invoice is "+modifiedTimeOfInvoice);
                    if(!modifiedTimeOfInvoice.equals(invoiceFetchTime)  )
                    {
                        invoiceEditedAfterFetch = true;
                    }
                }


                if(invoiceEditedAfterFetch==true)
                {
                    showAlertDialogJS("Error","This Invoice has been edited at the server.Please press select store, refresh and try again.");
                    stopLoadingDialog();
                    if(!typeOfEdit.equals("invoicecompletion"))
                    {

                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("javascript:hideLoadingPopupModal();");
                            }
                        });
                    }
                    return true;
                }
                return false;
            }else{
                return false;
            }
        }
        @JavascriptInterface
        public boolean employeeCanHandleInvoice(String associatedInvoiceId)
        {
            if(isUserAdmin())
            {
                return true;
            }

            ArrayList<JSONObject> recallInvoicesList = dbVar.executeRawqueryJSON("SELECT invoice_id FROM invoice_total_table WHERE invoice_id='"+associatedInvoiceId+"' AND "+dbVar.INVOICE_EMPLOYEE+"='"+userid+"'");
            if(recallInvoicesList.size()>0)
            { return  true;}
            return false;
        }
        @JavascriptInterface
        public void fetchTableSelectionAsync()
        {
            Log.v("Table Selection","Fetch invoices called");
            SharedPreferences sharedPreferences = sharedpreferences;
            String ipAddress = sharedPreferences.getString("IPAddress", "");
            if(ipAddress.equals("")) {
//                 POSWebPrinting.callLocalServerBackgroundRefresh(ipAddress);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
//                        webView.loadUrl("javascript:hideLoadingPopupModal();");
                        webView.loadUrl("javascript:displayPendingTablesFromAsync();");
                    }
                });
                return;
            }


        }

        @JavascriptInterface
        public void showLoadingDialog()
        {
            try {
                // called before request is started
                dialog = new ProgressDialog(POSWebActivity.this);
                dialog.show();
                dialog.setContentView(R.layout.progress_dialog);
                dialog.setCanceledOnTouchOutside(false);
            }catch (Exception exp){
                exp.printStackTrace();
            }
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
        public void fetchLatestInvoicesAsync()
        {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);
            String ipAddress = sharedPreferences.getString("IPAddress", "");
            if(ipAddress.equals("")) {
//                 POSWebPrinting.callLocalServerBackgroundRefresh(ipAddress);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:displayPendingFromAsync();");
                    }
                });
                return;
            }
            else{
                String loggedinusertype = sharedPreferences.getString("SP_LOGGEDINUSERTYPE","");
                String userid = sharedPreferences.getString("SP_LOGGEDINUSERID","");
                String useridPass = sharedPreferences.getString("SP_LOGGEDINUSERPASS","");

                ArrayList<JSONObject> miscDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.MISCELLANEOUS_TABLE + " WHERE 1 ");

                String serverUrl = "",selectedStoreId="";
                if(miscDetails.size()>0)
                {
                    JSONObject miscRow = miscDetails.get(0);
                    serverUrl = miscRow.optString("server_url");
                    selectedStoreId = miscRow.optString("selected_store");
                }
                JSONObject postObj= new JSONObject();
                useridPass = ConstantsAndUtilities.MD5(useridPass);
                try {
                    postObj.put("selectedStore", selectedStoreId);
                    postObj.put("userid", userid);
                    postObj.put("onlyinvoices", "true");
                    postObj.put("userpassword", useridPass);
                }catch (JSONException exp){

                }
                
                final String server_url = serverUrl+"pendingAndCompletedInvoices.php";
                Log.v("Fetch",server_url);
                final RequestParams params = new RequestParams();

                try {
                    params.put("selectedStore", selectedStoreId);
                    params.put("userid", userid);
                    params.put("userpassword", useridPass);
                    params.put("onlyinvoices", "true");
                }catch (Exception e){
                    e.printStackTrace();
                }
                webView.post(new Runnable() {
                    @Override
                    public void run() {

                        AsyncHttpClient client = new AsyncHttpClient();
                        client.post(server_url, params, new TextHttpResponseHandler() {

                            @Override
                            public void onStart() {
                                // called before request is started
                    /*dialog = new ProgressDialog(POSWebActivity.this);
                    dialog.show();
                    dialog.setContentView(R.layout.poswebactivty_progress_dialog);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.getWindow().setBackgroundDrawableResource(
                            android.R.color.transparent
                    );*/
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(POSWebActivity.this, "We found an error. Couldnot connect", Toast.LENGTH_LONG).show();
//                    dialog.hide();
                                webView.loadUrl("javascript:hideLoadingPopupModal();");
                                // hideLoadingPopupModal()
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Log.v("Fetch",responseString);
                                try {
                                    JSONObject obj = new JSONObject(responseString);
                                    JSONArray queriesarray=null;
                                    queriesarray = obj.getJSONArray("queries-array");
                                    for(int j=0;j<queriesarray.length();j++)
                                    {
                                        String querytemp = queriesarray.getString(j);
                                        Log.v("Fetch","Query Temp is "+querytemp);
                                        dbVar.executeExecSQL(querytemp);
//                            JSONObject currentRow = queriesarray.get(j);
//                            String backgroundQuery =
                                    }
                                }catch (JSONException exp)
                                {
                                    exp.printStackTrace();
                                    Toast.makeText(POSWebActivity.this, "Invalid Response. "+exp.getMessage(), Toast.LENGTH_LONG).show();

                                }
//                    dialog.hide();

//                    showThePendingInvoicesInWebview();
                                webView.loadUrl("javascript:displayPendingFromAsync();");

                            }

                            @Override
                            public void onRetry(int retryNo) {
                                // called when request is retried
                            }
                        });
                    }
                });
            }
//            dialog.setContentView(R.layout.dlgwebview);
        }

        @JavascriptInterface
        public String notesButtonsForItemId(String itemId)
        {
            String notesForItem = "";
            Log.v("notesButtonsForItemId","Notes for item called and item id is "+itemId);
            ArrayList<JSONObject>  itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+itemId+"' LIMIT 1");
            Log.v("notesButtonsForItemId",itemGetDetails.toString());
            if(itemGetDetails.size()==1)
            {
                JSONObject itemRow = itemGetDetails.get(0);

                notesForItem = String.valueOf(itemRow.optString(dbVar.INVENTORY_NOTES));
            }
            return  notesForItem;

        }
        @JavascriptInterface
        public void localServerRefreshCall(){
//
            SharedPreferences sharedPreferences = sharedpreferences;
            String ipAddress = sharedPreferences.getString("IPAddress", "");
            Log.v("IP Check","IP Address is "+ipAddress);
            if(!ipAddress.equals("")) {
//                 POSWebPrinting.callLocalServerBackgroundRefresh(ipAddress);
            }
        }
        @JavascriptInterface
        public String customerDetailsForInvoice(String invoiceId)
        {
            String customerName = "";
            String customerID = "";
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            if(InvoiceDetails.size() > 0){
                JSONObject invoiceRow = InvoiceDetails.get(0);
                customerID = invoiceRow.optString(dbVar.INVOICE_CUSTOMER);
                if(!customerID.equals("")) {
                    ArrayList<JSONObject> CustomerDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_TABLE + " WHERE " + dbVar.CUSTOMER_NO + "='" + customerID + "' ");
                    if (CustomerDetails.size() > 0) {
                        JSONObject customerRow = CustomerDetails.get(0);
                        customerName = customerRow.optString(dbVar.CUSTOMER_FIRST_NAME) + " " + customerRow.optString(dbVar.CUSTOMER_LAST_NAME);
                    }
                }
            }
            JSONObject customerInfo = new JSONObject();
            try {
                customerInfo.put("customername", customerName);
                customerInfo.put("customerid", customerID);
            }catch (JSONException exp){

            }
            return customerInfo.toString();
        }
        @JavascriptInterface
        public String searchCustomerByKey(String searchKey,String orderType,String storeId){
            String itemSearchQuery = "SELECT customer_table.customer_no,customer_first_name,customer_last_name,customer_general_info_table.customer_primary_phone  FROM customer_table LEFT JOIN customer_general_info_table ON customer_table.customer_no=customer_general_info_table.customer_no WHERE LOWER(customer_first_name) LIKE '"+searchKey+"%' OR LOWER(customer_first_name) LIKE '"+searchKey+"%' OR LOWER(customer_first_name) LIKE '%"+searchKey+"%' OR LOWER(customer_last_name) LIKE '"+searchKey+"%' OR LOWER(customer_last_name) LIKE '"+searchKey+"%' OR LOWER(customer_last_name) LIKE '%"+searchKey+"%'  OR LOWER(customer_company_name) LIKE '%"+searchKey+"%' OR customer_general_info_table.customer_primary_phone LIKE '"+searchKey+"%' OR customer_table.customer_no LIKE '"+searchKey+"%' ORDER BY customer_first_name ASC LIMIT 40";
            ArrayList<JSONObject> customerDetails = dbVar.executeRawqueryJSON(itemSearchQuery);
            if(customerDetails.size()>0)
            {
                for(int k=0;k<customerDetails.size();k++)
                {
                    String customerId = (String) customerDetails.get(k).optString("customer_no");
                    String existingBalance = "0";
                    ArrayList<JSONObject> customerAccountPayment = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CUSTOMER_META_TABLE + " WHERE customer_id='" + customerId + "' AND attribute='account_balance' AND unique_id='"+customerId+"_account_balance'");
                    if(customerAccountPayment.size()>0)
                    {
                        existingBalance = (String) customerAccountPayment.get(0).optString(dbVar.CUSTOMER_ATTR_VALUE);
                    }
                    try {
                        customerDetails.get(k).put("customer_available_balance", existingBalance);
                    }catch (Exception exp){
                        exp.printStackTrace();
                    }
                }
            }
            return customerDetails.toString();
        }
        @JavascriptInterface
        public String searchItemByKey(String searchKey,String orderType,String storeId){
            String itemSearchQuery = "SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE LOWER("+dbVar.INVENTORY_ITEM_NO+") LIKE '"+searchKey+"%' OR LOWER("+dbVar.INVENTORY_ITEM_NAME+") LIKE '"+searchKey+"%' " +" OR LOWER("+dbVar.INVENTORY_ITEM_NAME+") LIKE '% "+searchKey+"%' ORDER BY "+dbVar.INVENTORY_ITEM_NAME+" ASC LIMIT 40";
            Log.v("Search",itemSearchQuery);
            ArrayList<JSONObject> ItemDetails = dbHelper.executeRawqueryJSON(itemSearchQuery);
            if(ItemDetails.size()>0){
                for(int k=0; k<ItemDetails.size();k++)
                {
                    String storewisePricingAndStock = productStorewisePricingAndStock(   (ItemDetails.get(k).optString(DatabaseVariables.INVENTORY_ITEM_NO)),orderType,storeId);
                    try {
                        ItemDetails.get(k).put("storewisePricingAndStock",storewisePricingAndStock);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return ItemDetails.toString();
        }
        @JavascriptInterface
        public String priceForCustomer(String itemId,String invitemPrice,String customerIdForInvoice,String invoiceStoreId)
        {
            String pricingToCustomer = invitemPrice;
            // 296203218_pentaiah_hotel_TF035_pricing
            ArrayList<JSONObject> customerStorewisePricing = dbVar.executeRawqueryJSON("SELECT * FROM customer_meta WHERE unique_id='"+customerIdForInvoice+"_"+invoiceStoreId+"_"+itemId+"_pricing' AND customer_id='"+ customerIdForInvoice +"' AND attribute='customer_storewise_pricing'");
            if(customerStorewisePricing.size()==0)
            {
                return invitemPrice;
            }else{
                JSONObject customerPricing = customerStorewisePricing.get(0);
                pricingToCustomer = (customerPricing.optString("value"));
            }
            return pricingToCustomer;
        }
        @JavascriptInterface
        public void invoiceFetchTimeSaveByInvoiceId(String invoiceId){

            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            if(InvoiceDetails.size()==1){
                invoiceFetchTime = (String) InvoiceDetails.get(0).optString(dbVar.MODIFIED_DATE);
            }

            Log.v("Testing","Invoice fetch time is "+invoiceFetchTime);
        }
        @JavascriptInterface
        public void invoiceFetchTimeSave(String fetchTime){
            /*
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            if(InvoiceDetails.size()==1){
                invoiceFetchTime = (String) InvoiceDetails.get(0).optString(dbVar.MODIFIED_DATE);
            }

             */
            invoiceFetchTime = fetchTime;
            Log.v("Testing","Invoice fetch time is "+invoiceFetchTime);
        }
        @JavascriptInterface
        public String invoiceDetailsForID(String invoiceId){
            String invoiceHoldId = "";
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            return InvoiceDetails.toString();
        }
        @JavascriptInterface
        public String invoicesForTables(String storeId){

            String employeeIdLoggedin = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID);
            String withinTablesFilter = "";
            ArrayList<JSONObject> tablesAssignedForEmployees =dbHelper.executeRawqueryJSON("SELECT * FROM employee_advanced_permissions WHERE employee_employee_id='"+employeeIdLoggedin+"_"+storeId+"_assignedtables'");
            if(tablesAssignedForEmployees.size()==1)
            {
                String uniqueTablesId = (String) tablesAssignedForEmployees.get(0).optString("emp_advanced_permissions");
                if(!uniqueTablesId.equals(""))
                {
                    uniqueTablesId = uniqueTablesId.replaceAll(",","','");
                    uniqueTablesId = "'"+uniqueTablesId+"'";
                    withinTablesFilter = " AND unique_id IN (" + uniqueTablesId + ") ";
                }
            }

            String query = "SELECT * from "
                    + dbVar.INVOICE_TOTAL_TABLE
                    + " where "
                    + dbVar.SETTINGS_TABLE_STOREID
                    + "=\"" + storeId + "\" AND status='hold' "+ withinTablesFilter+" order by holdid ";
            JSONObject invoiceIdTableStatusPair = new JSONObject();
            ArrayList<JSONObject> tableNumberSelection = dbVar.executeRawqueryJSON(query);
            for(int i=0 ; i < tableNumberSelection.size(); i++)
            {
                JSONObject currentInvoiceObj = tableNumberSelection.get(i);
                String invoiceId = (String) currentInvoiceObj.optString("invoice_id");
                String invoiceDeliveryStatus = (String) currentInvoiceObj.optString(dbVar.INVOICE_DELIVERY_STATUS);
                try {
                    invoiceIdTableStatusPair.put(invoiceId,invoiceDeliveryStatus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return invoiceIdTableStatusPair.toString();
        }
        @JavascriptInterface
        public String replacedNameForOrderType(String orderType){
            String orderTypeReplacedName = "";
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);
            switch (orderType) {
                case "store sale":
                    if (!TextUtils.isEmpty(sharedPreferences.getString("dinetext", ""))){
                        orderTypeReplacedName = sharedPreferences.getString("dinetext", "Dine In");
                    }else{ orderTypeReplacedName = "Dine In";}
                    break;
                case "take away":
                    if (!TextUtils.isEmpty(sharedPreferences.getString("tacktext", ""))){
                        orderTypeReplacedName = sharedPreferences.getString("tacktext", "Take Away");
                    }else{ orderTypeReplacedName = "Take Away";}
                    break;
                case "home delivery":
                    if (!TextUtils.isEmpty(sharedPreferences.getString("hometext", ""))){
                        orderTypeReplacedName = sharedPreferences.getString("hometext", "Home Delivery");
                    }else{ orderTypeReplacedName = "Home Delivery";}
                    break;
                default:
                    if (!TextUtils.isEmpty(sharedPreferences.getString("dinetext", ""))){
                        orderTypeReplacedName = sharedPreferences.getString("dinetext", "Dine In");
                    }else{ orderTypeReplacedName = "Dine In";}
                    break;
            }
            return orderTypeReplacedName;

        }
        @JavascriptInterface
        public String categoriesAndDepartmentsForStore(String storeID)
        {
            JSONObject returnCategoriesAndDepartments = new JSONObject();
            try {
                ArrayList<JSONObject> updateStoreCategoriesList = dbHelper.executeRawqueryJSON(
                        "select " + dbVar.STORE_CATEGORY_ID + " from " + dbVar.STORE_CATEGORY_TABLE + " where "
                                + dbVar.STORE_CATEGORY_STOREID + "='" + storeID + "'");

                ArrayList<JSONObject> updateStoreDepartmentsList = dbHelper.executeRawqueryJSON(
                        "SELECT department_details.unique_id,department_details.department_id,department_details.category_id FROM department_details,store_category WHERE department_details.category_id = store_category.category_id AND store_category.store_id = '"+storeID+"' ORDER BY department_details.department_id ASC");

                // System.out.println(mCurchisor);
                String catIds = "";
                if (updateStoreCategoriesList != null) {
                    if (updateStoreCategoriesList.size() > 0) {
                        returnCategoriesAndDepartments.put("categories", updateStoreCategoriesList);
                    }

                }
                if (updateStoreDepartmentsList != null) {
                    if (updateStoreDepartmentsList.size() > 0) {
                        returnCategoriesAndDepartments.put("departments", updateStoreDepartmentsList);
                    }

                }
            }catch (JSONException Exp){

            }
            return returnCategoriesAndDepartments.toString();
        }
        @JavascriptInterface
        public void storeSelectionClick(){
            POSWebActivity.this.finish();
            startActivity(new Intent(POSWebActivity.this, POSWebActivity.class));
        }
        @JavascriptInterface
        public void backButtonClick(){
            POSWebActivity.this.finish();
            startActivity(new Intent(POSWebActivity.this, MenuScreen.class));
        }
        @JavascriptInterface
        public String productStorewisePricingAndStock(String productId,String orderType,String StoreId){
            JSONObject productInfo = new JSONObject();
            ArrayList<JSONObject> storeWisePricingDetails =dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.STORE_PRODUCTS_TABLE+" WHERE "+dbVar.PRODUCTS_STOREID+"='"+StoreId+"' AND "+dbVar.PRODUCTS_ITEM_NO+"='"+productId+"' LIMIT 1");
            ArrayList<JSONObject> storeWiseStockDetails = new ArrayList<>();//dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.STOREWISE_STOCK_TABLE+" WHERE "+dbVar.STORE_CATEGORY_STOREID+"='"+StoreId+"' AND "+dbVar.PRODUCTS_ITEM_NO+"='"+productId+"' AND "+dbVar.STOREID_ITEMNO+"='"+StoreId+productId+"' LIMIT 1");
            ArrayList<JSONObject> itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+productId+"' LIMIT 1");
            String price = "0";
            String stockCount = "0";
            if(storeWisePricingDetails.size() ==1){
                if(orderType.equals( "store sale")){
                    price = storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_PRICE);}
                else if(orderType.equals( "take away")){
                    price = storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_TAKEAWAY);}
                else if(orderType.equals( "home delivery")){
                    price = storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_DELIVERY_PRICE);}
            }else{
                if(orderType.equals("store sale")){
                    price = itemGetDetails.get(0).optString(dbVar.INVENTORY_PRICE_TAX);
                }else{
                    price = itemGetDetails.get(0).optString(dbVar.INVENTORY_TAKEAWAY_TAX);
                }
            }
            if(storeWiseStockDetails.size() == 1){
                stockCount = storeWiseStockDetails.get(0).optString(dbVar.STOREWISE_STOCKCOUNT);
            }
            try{
                productInfo.put("price",price);
                productInfo.put("stockCount",stockCount);
            }catch (JSONException exp){ exp.printStackTrace(); }
            return productInfo.toString();
        }
        @JavascriptInterface
        public String productDetailsForFetch(String productId,String orderType,String StoreId){
            fetchingInvoice = true;
            String op = productDetails(productId,orderType,StoreId);
            fetchingInvoice = false;
            return op;
        }
        @JavascriptInterface
        public String productDetails(String productId,String orderType,String StoreId){
            String returnStr = "Hello";
            JSONObject productInfo = new JSONObject();

            if(productId.equals("")){
                returnStr = productInfo.toString();
                return returnStr;
            }

            try {
                productInfo.put("is_plu",false);
                productInfo.put("is_item",true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Boolean itemFinishedRetrieve = false;
            // First Check if it is weighing scale product
//            weighingScaleItemCodeCheck(productId); // Done Locally

            // secondly check if it is a plu code with multiple barcodes
            ArrayList<JSONObject> itemMultiPLUBarcodeGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.ALTERNATE_PLU_TABLE +" WHERE "+dbVar.ALTERNATE_PLU_plu_barcode+"='"+productId+"' ");
            if(itemMultiPLUBarcodeGetDetails.size()>1) {
                try {
                    productInfo.put("is_plu", false);
                    productInfo.put("is_item", false);
                    productInfo.put("multiple_barcodes",false);
                    productInfo.put("multiple_plu_barcodes",true);
                    productInfo.put("plu_barcodes_list",itemMultiPLUBarcodeGetDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return productInfo.toString();
            }
            // thirdly check if it is a plu code
            ArrayList<JSONObject> plucodes = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.ALTERNATE_PLU_TABLE+" WHERE "+dbVar.ALTERNATE_PLU_plu_number+"='"+productId+"' LIMIT 1");
            if(plucodes.size()==1) {
                JSONObject itemRow = plucodes.get(0);
                String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                ArrayList<JSONObject>  itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+itemIdForPLU+"' LIMIT 1");
                try {
                    productInfo.put("is_plu",true);
                    productInfo.put("is_item",true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                productId = itemGetDetails.get(0).optString(dbVar.INVENTORY_ITEM_NO);
            }
            // Done With Single PLU Code

            // Fourth check if it has multiple barcodes
            ArrayList<JSONObject> itemMultiBarcodeGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEMBARCODE+"='"+productId+"' ");
            if(itemMultiBarcodeGetDetails.size()>1) {
                try {
                    productInfo.put("is_plu", false);
                    productInfo.put("is_item", false);
                    productInfo.put("multiple_barcodes",true);
                    productInfo.put("multiple_plu_barcodes",false);
                    productInfo.put("barcodes_list",itemMultiBarcodeGetDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return productInfo.toString();
            }
            // done with multiple barcodes check
            // Fifth check if it is item_barcode
            ArrayList<JSONObject> itemBarcodeGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEMBARCODE+"='"+productId+"' LIMIT 1");
            if(itemBarcodeGetDetails.size()>0)
            {
                JSONObject itemRow = itemBarcodeGetDetails.get(0);
                productId = itemRow.optString(dbVar.INVENTORY_ITEM_NO);
            }
            // Finally Check based on item number
            if(itemFinishedRetrieve==false)
            {
                ArrayList<JSONObject> itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+DatabaseVariables.INVENTORY_TABLE+" WHERE "+DatabaseVariables.INVENTORY_ITEM_NO+"='"+productId+"' LIMIT 1");
                ArrayList<JSONObject> optionalInfoDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+DatabaseVariables.OPTIONAL_INFO_TABLE+" WHERE "+DatabaseVariables.INVENTORY_ITEM_NO+"='"+productId+"' LIMIT 1");
                ArrayList<JSONObject> orderingInfoDetails = new ArrayList<>();//dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.ORDERING_INFO_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+productId+"' LIMIT 1");
                ArrayList<JSONObject> storeWisePricingDetails =dbHelper.executeRawqueryJSON("SELECT * FROM "+DatabaseVariables.STORE_PRODUCTS_TABLE+" WHERE "+DatabaseVariables.PRODUCTS_STOREID+"='"+StoreId+"' AND "+DatabaseVariables.PRODUCTS_ITEM_NO+"='"+productId+"' LIMIT 1");
                ArrayList<JSONObject> storeWiseStockDetails = new ArrayList<>();// dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.STOREWISE_STOCK_TABLE+" WHERE "+dbVar.STORE_CATEGORY_STOREID+"='"+StoreId+"' AND "+dbVar.PRODUCTS_ITEM_NO+"='"+productId+"' AND "+dbVar.STOREID_ITEMNO+"='"+StoreId+productId+"' LIMIT 1");
                if(itemGetDetails.size() ==0)
                {
                    try {
                        productInfo.put("product_exists",false);
                    } catch (JSONException e) {
                        Log.v("Srinath",e.getMessage());
                    }
                }
                else {

                    String departmentId = itemGetDetails.get(0).optString(DatabaseVariables.INVENTORY_DEPARTMENT);
                    ArrayList<JSONObject> deptDetails = dbHelper.executeRawqueryJSON("SELECT department_details."+DatabaseVariables.DepartmentID+",department_details."+dbVar.UNIQUE_ID+",category_details.category_id,category_details.unique_id AS category_unique_id FROM "+dbVar.DEPARTMENT_TABLE+",category_details WHERE "+dbVar.DepartmentID+"='"+departmentId+"' AND category_details.category_id= department_details.category_id LIMIT 1");

                    try {
                        productInfo.put("dept_info",deptDetails);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(storeWisePricingDetails.size()!=0)
                    {
                        JSONObject storewiseRow = storeWisePricingDetails.get(0);

                        if(storewiseRow.optString("disable").equals("1") && fetchingInvoice==false)
                        {
                            DialogBox dbx = new DialogBox();
                            dbx.dialogBox("Disabled Item", "" + itemGetDetails.get(0).optString(dbVar.INVENTORY_ITEM_NAME)+" is disabled temporarily", "OK", "Cancel", POSWebActivity.this);


                            try {
                                productInfo.put("product_exists",false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            returnStr = productInfo.toString();
                            return returnStr;
                        }
                    }

                    try {
                        productInfo.put("product_exists",true);
                        productInfo.put("inventory_info",itemGetDetails);
                        productInfo.put("storewise_pricing_info",storeWisePricingDetails);
                        productInfo.put("storewise_stock_info",storeWiseStockDetails);
                        productInfo.put("optional_info",optionalInfoDetails);
                        productInfo.put("ordering_info",orderingInfoDetails);
                        productInfo.put("plu_info",plucodes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            Boolean productExistsValue = false;
            try {
                productExistsValue =        productInfo.getBoolean("product_exists");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(productExistsValue==false) {
                try {

                    DialogBox dbx = new DialogBox();
                    dbx.dialogBox("Invalid Barcode/Item Code", "" + "Product Not Found", "OK", "Cancel", POSWebActivity.this);
                } catch (Exception exp) {
                    Log.v("Srinath", exp.toString());
                }
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(POSWebActivity.this, notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            returnStr = productInfo.toString();
            return returnStr;
        }

        String datavalforedit = "";
        @JavascriptInterface
        public void deleteTablesforedit(final String tablename, String value) {

            final ArrayList<String> getlist = new ArrayList<String>();
            String selectQueryforinstantpo = "SELECT  * FROM " + tablename
                    + " where " + dbVar.INVOICE_ID + "=\"" + value + "\"";

            ArrayList<JSONObject> invoiceRow = dbVar.executeRawqueryJSON(
                    selectQueryforinstantpo);
            if (invoiceRow!=null && invoiceRow.size() > 0) {
                for(int cnt=0; cnt<invoiceRow.size();cnt++) {
                    JSONObject currentRow = invoiceRow.get(cnt);
                    String uniqueid = currentRow.optString(dbVar.UNIQUE_ID);
                    getlist.add(uniqueid);
                }

            }
            String here = dbVar.INVOICE_ID + "";
            dbVar.executeDeleteInDB(tablename, here, value);
            try {
                JSONArray unique_ids = new JSONArray();
                for (int i = 0; i < getlist.size(); i++) {
                    unique_ids.put(i, getlist.get(i));
                    datavalforedit = unique_ids.toString();
                }
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            Log.e("logdele", "datadelete " + datavalforedit);
            if ((dbVar.getValueForAttribute("server_url")).equals("")) {
                // System.out.println("there is no server url val");
            }
        }

        @JavascriptInterface
        public String overallTaxes(String storeID,String order_type)
        {
            ArrayList<JSONObject> overallTaxes = dbHelper.executeRawqueryJSON(
                    "select * from " + dbVar.TAX_TABLE + " where "
                            + dbVar.TAX_STORE + "='" + storeID + "' AND " + dbVar.TAX_ORDER_TYPE + "='" + order_type + "' AND "+ dbVar.TAX_OVERALL_VALUE + "='true' ");
            return overallTaxes.toString();
        }
        @JavascriptInterface
        public String voidInvoice(String recallInvoiceId,String holdInvoiceId){
            String voidType = "recallInvoice";
            String voidInvoiceId = "";
            printInfoObj = new JSONObject();

            try {
                printInfoObj.put("content_type","voidinvoice");
                printInfoObj.put("devicetoken",ConstantsAndUtilities.device_Token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(!recallInvoiceId.equals("")){
                voidType = "recallInvoice"; voidInvoiceId = recallInvoiceId;
            }else {
                voidType = "holdInvoice"; voidInvoiceId = holdInvoiceId;
            }
            printInvoiceId = voidInvoiceId;
            Boolean isInvoiceVoided = SaveFetchVoidInvoice.voidTheInvoice(voidInvoiceId);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    POSWebPrinting posWebPrinting = new POSWebPrinting();
                    posWebPrinting.voidInvoiceCancelledBillPrinting(printInvoiceId,printInfoObj);
                    Boolean categoryWisePrinting = false;
                    ArrayList<JSONObject> itemListPrintType = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='items_list_print_type'");
                    Log.v("PrintKOTAuto",itemListPrintType.toString());
                    if(itemListPrintType.size()==1)
                    {
                        JSONObject printingTypeObj = itemListPrintType.get(0);
                        String valueOfPrintingType = printingTypeObj.optString("value");
                        if(valueOfPrintingType.equals("categorywise"))
                        {
                            categoryWisePrinting = true;
                        }
                    }
                    if(categoryWisePrinting==true) {
                        posWebPrinting.voidInvoiceCancelledCategorywiseKOTSPrinting(printInvoiceId,printInfoObj);
                    }else{

                            posWebPrinting.voidInvoiceCancelledKOTPrinting(printInvoiceId);

                    }
                    /*ServerSyncClass ss = new ServerSyncClass();
                    ss.pendingclear();*/
                }
            }).start();

            invoiceFetchTime = "";
            return voidInvoiceId;
        }

        @JavascriptInterface
        public void transferTheTable(final String fromInvoice,final String toTable,final String newOrderRefNum,final String storeId) {
            Log.v("console", "From invoice is " + fromInvoice + " to table is " + toTable + " New order Reference num is " + newOrderRefNum + " store id is " + storeId);
            String permissionOfUser = userPermissionCheck("tableshifting");
            String existingHoldId = holdIdForInvoice(fromInvoice);
            if(existingHoldId.equals(newOrderRefNum))
            {
                showToast("You have selected the same order reference number. Please try again.");
                return;
            }
            if (permissionOfUser.equals("true")) {
                String oldOrderRefNum = "";
                ArrayList<JSONObject> invoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_TOTAL_TABLE+" WHERE invoice_id='"+fromInvoice+"'");
                if(invoiceDetails.size()==1)
                {
                    JSONObject invoiceRow = invoiceDetails.get(0);
                    oldOrderRefNum = invoiceRow.optString(dbVar.INVOICE_HOLD_ID);
                }
                String validation = validateExistingRefNum(newOrderRefNum, storeId);
                String alertDialogMessage = "";
                String mergingOrShifting = "Shift";
                if (validation.equals("true")) {
                    alertDialogMessage = "Are you sure that you want to shift the table ?";
                } else {
                    alertDialogMessage = "Are you sure that you want to merge the tables ?";
                    mergingOrShifting = "Merge";
                }


                AlertDialog alertDialog = new AlertDialog.Builder(POSWebActivity.this)
                        .create();
                alertDialog.setMessage(alertDialogMessage);
                String finalMergingOrShifting = mergingOrShifting;

                String finalOldOrderRefNum = oldOrderRefNum;

                alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveFetchVoidInvoice.shiftTable(fromInvoice, toTable, newOrderRefNum, storeId);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                shiftTablePrint(finalOldOrderRefNum,fromInvoice, toTable, newOrderRefNum, storeId, finalMergingOrShifting);

                            }
                        }).start();
                        storeSelectionClick();

                    }
                });
                alertDialog.setButton2("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.v("Srinath", "No Clicked");
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(POSWebActivity.this)
                        .create();
                alertDialog.setMessage("You are not allowed to shift/merge tables");
                alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveFetchVoidInvoice.shiftTable(fromInvoice, toTable, newOrderRefNum, storeId);
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }


        }
        @JavascriptInterface
        public String validateExistingRefNum(String referenceNum,String storeId){
            String query = "SELECT * from "
                    + dbVar.INVOICE_TOTAL_TABLE
                    + " where "
                    + dbVar.INVOICE_HOLD_ID
                    + "=\"" + referenceNum + "\" AND "+ dbVar.INVOICE_STORE_ID +"=\""+ storeId +"\" AND status='hold' order by "
                    + dbVar.MODIFIED_DATE;

            ArrayList<JSONObject> tableNumberSelection = dbVar.executeRawqueryJSON(query);
            if(tableNumberSelection.size()==0) {
                return "true";
            }
            return "false";
        }
        @JavascriptInterface
        public String tableNumbersSave(String storeId) {


            String employeeIdLoggedin = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID);
            String withinTablesFilter = "";
            ArrayList<JSONObject> tablesAssignedForEmployees =dbHelper.executeRawqueryJSON("SELECT * FROM employee_advanced_permissions WHERE employee_employee_id='"+employeeIdLoggedin+"_"+storeId+"_assignedtables'");
            if(tablesAssignedForEmployees.size()==1)
            {
                String uniqueTablesId = (String) tablesAssignedForEmployees.get(0).optString("emp_advanced_permissions");
                if(!uniqueTablesId.equals(""))
                {
                    uniqueTablesId = uniqueTablesId.replaceAll(",","','");
                    uniqueTablesId = "'"+uniqueTablesId+"'";
                    withinTablesFilter = " AND unique_id IN (" + uniqueTablesId + ") ";
                }
            }

            // edit here
            String query = "SELECT tables_setting.table_name,tables_setting.unique_id,tables_setting.occupancy_status, tables_setting.associated_invoice_id,tables_setting.store_id from "
                    + dbVar.TABLE_SETTINGS_TABLE
                    + " where "
                    + dbVar.SETTINGS_TABLE_STOREID
                    + "=\"" + storeId + "\" "+ withinTablesFilter+" order by "
                    + dbVar.SETTINGS_TABLENAME;

            String query2 = "SELECT tables_setting.table_name,tables_setting.unique_id, tables_setting.occupancy_status, tables_setting.associated_invoice_id,tables_setting.store_id, invoice_total_table.delivery_status, invoice_total_table.employee, invoice_total_table.bill_ts,invoice_total_table.holdid FROM `tables_setting`, `invoice_total_table` WHERE tables_setting.store_id='"+ storeId + "' AND invoice_total_table.invoice_id = tables_setting.associated_invoice_id;";
            ArrayList<JSONObject> tableNumberSelectionWithMultiTableInvoices = dbVar.executeRawqueryJSON(query2);

            ArrayList<JSONObject> tableNumberSelection = dbVar.executeRawqueryJSON(query);
            if(tableNumberSelection.size()>0)
            {
                for(int j=0; j < tableNumberSelection.size(); j++)
                {
                    JSONObject tableObj = tableNumberSelection.get(j);
                    String tableUniqueId = tableObj.optString(dbVar.UNIQUE_ID);
                    String employeeId = "";
                    String deliveryStatus = "Undelivered";
                        for(int d=0; d < tableNumberSelectionWithMultiTableInvoices.size(); d++)
                        {
                            JSONObject tableNumberSelectionWithMultiTableInvoiceObj = tableNumberSelectionWithMultiTableInvoices.get(d);
                            if(tableNumberSelectionWithMultiTableInvoiceObj.optString(dbVar.UNIQUE_ID).equals(tableUniqueId))
                            {
                                employeeId = tableNumberSelectionWithMultiTableInvoiceObj.optString(dbVar.INVOICE_EMPLOYEE);
                                deliveryStatus = tableNumberSelectionWithMultiTableInvoiceObj.optString(dbVar.INVOICE_DELIVERY_STATUS);
                            }
                        }
                    try {
                        tableObj.put("employee",employeeId);
                        tableObj.put("delivery_status",deliveryStatus);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    tableNumberSelection.set(j,tableObj);
                }
            }
            Log.v("TestTables",tableNumberSelection.toString());
            return tableNumberSelection.toString();
        }

        @JavascriptInterface
        public String voidedInvoiceReducedQuantities(String formData)
        {
            Log.v("ReasonForCancellation","CartHasDeletedItemsOrReducedQuantities called");
            ItemCancellationReasons icr = new ItemCancellationReasons();
            return (icr.voidedInvoiceReducedQuantities(formData)==true ? "true" : "false");

        }
        @JavascriptInterface
        public String CartHasDeletedItemsOrReducedQuantities(String formData)
        {
            Log.v("ReasonForCancellation","CartHasDeletedItemsOrReducedQuantities called");
            ItemCancellationReasons icr = new ItemCancellationReasons();
            return (icr.CartHasDeletedItemsOrReducedQuantities(formData)==true ? "true" : "false");
        }
        @JavascriptInterface
        public String cancellationTableBodyStr()
        {
            ItemCancellationReasons icr = new ItemCancellationReasons();
            String cancellationTableBodyStr=icr.cancellationTableBodyStr();
            return cancellationTableBodyStr;
        }
        @JavascriptInterface
        public void clearCancellationReasons(){ ItemCancellationReasons icr = new ItemCancellationReasons(); icr.clearCancellationReasons();}
        @JavascriptInterface
        public void saveReasonsForCancellation(String formData)
        {
            ItemCancellationReasons icr = new ItemCancellationReasons();
            icr.saveReasonsForCancellation(formData);
        }

        @JavascriptInterface
        public void saveAndHoldInvoice(String formData){
//            showLoadingDialog();

            Log.v("Test","saveAndHoldInvoice called");
            try {
                printInfoObj = new JSONObject();
                JSONObject obj = new JSONObject(formData);
                printInfoObj.put("orderdetails",obj);
                printInfoObj.put("content_type","saveonhold");
                printInfoObj.put("devicetoken",dbVar.getValueForAttribute("device_token"));
                JSONArray itemIds = obj.optJSONArray("itemIds");
                for(int p=0; p < itemIds.length(); p++)
                {
                    String itemId = itemIds.getString(p);
                }
                String invoiceNumber = "";
                String newOrRepeat = "";
                if(obj.optString("holdInvoiceId").equals("")) {
                    printInfoObj.put("neworrepeat","New Order");
                    newOrRepeat = "New";
                    invoiceNumber = ConstantsAndUtilities.generateRandomNumber();
                }else{
                    printInfoObj.put("neworrepeat","Repeat Order");
                    invoiceNumber = obj.optString("holdInvoiceId");
                    newOrRepeat = "Repeat";
                }
                printInfoObj.put("invoiceid",invoiceNumber);
                printInvoiceId = invoiceNumber;
                // we have to retrieve the fresh items and save it to print info Obj
                String deletedItemUniueIds = (String) obj.get("deletedItemsUniqueIdsFromSavedInvoice");
                if(!deletedItemUniueIds.equals("")) {
                    SaveFetchVoidInvoice sfv = new SaveFetchVoidInvoice();
                    ArrayList<JSONObject> deletedItemsRows = sfv.deletedItemsFromInvoice(deletedItemUniueIds);

                    // post to server here
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            POSWebPrinting posWebPrinting = new POSWebPrinting();
                            Boolean categoryWisePrinting = false;
                            ArrayList<JSONObject> itemListPrintType = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='items_list_print_type'");
                            Log.v("PrintKOTAuto",itemListPrintType.toString());
                            if(itemListPrintType.size()==1)
                            {
                                JSONObject printingTypeObj = itemListPrintType.get(0);
                                String valueOfPrintingType = printingTypeObj.optString("value");
                                if(valueOfPrintingType.equals("categorywise"))
                                {
                                    categoryWisePrinting = true;
                                }
                            }
                            if (categoryWisePrinting == true)
                            {
                                posWebPrinting.deletedItemsCategorywiseKOTSPrinting(deletedItemsRows, printInvoiceId, printInfoObj);
                            }
                            else
                            {

                                POSWebPrinting pWP = new POSWebPrinting();
                                pWP.deletedItemsKOTSPrinting(deletedItemsRows, printInvoiceId);

//                        posWebPrinting.deletedItemsKOTSPrinting(deletedItemsRows, completedInvoiceId);
                            }
//                            clearCancellationReasons();
                        }
                    }).start();


                }
                String finalInvoiceNumber = invoiceNumber;
                String finalNewOrRepeat = newOrRepeat;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        isSavingInvoice = true;
                        Log.v("Testing","Is saving invoice is true at "+ ConstantsAndUtilities.currentTime());
                        SaveFetchVoidInvoice.saveOnHoldInvoice(finalInvoiceNumber,obj, finalNewOrRepeat);

                        isSavingInvoice = false;

                        // save this to print queue
                        if(sharedpreferences.getBoolean(ConstantsAndUtilities.PROMPT_KOT_PRINT, true))
                        {
                            Log.v("PrintKOTAuto","Print the kot without prompt");

                        }
                        Log.v("Testing","Is saving invoice is false at "+ ConstantsAndUtilities.currentTime());
//                        ServerSyncClass ss = new ServerSyncClass();
//                        ss.pendingclear();
                    }
                }).start();

                invoiceFetchTime = "";
                if(!deletedItemUniueIds.equals("")) {


                }

            } catch (Exception e) {
                Log.v("Srinath",e.getMessage());
                e.printStackTrace();
            }
//            stopLoadingDialog();
            setSSIDName();
            return;
        }
        @JavascriptInterface
        public String userPermissionCheckForDuplicateCustomerCopy(String invoiceId)
        {
            String permissionEnabled = "false";

            String permissionForPreviewReceipt = userPermissionCheck("previewreceiptprint");
            String permissionForDuplicateInvoicePrinting = userPermissionCheck("duplicatepreviewreceiptprint");
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            String existingStatusOfPrint = "Undelivered";
            if(InvoiceDetails.size()==0)
            {
                return "false";
            }else{
                JSONObject invoiceObj = InvoiceDetails.get(0);
                Log.v("TestInvoice",invoiceObj.toString());
                existingStatusOfPrint = invoiceObj.optString(dbVar.INVOICE_DELIVERY_STATUS);
            }
            if(permissionForPreviewReceipt.equals("false"))
            {
                return "false";
            }else{
                if(permissionForDuplicateInvoicePrinting.equals("true"))
                {
                    return "true";
                }
                if(permissionForDuplicateInvoicePrinting.equals("false") && !existingStatusOfPrint.equals("Receipt Printed"))
                {
                    permissionEnabled = "true";
                }
                else{
                    permissionEnabled = "false";
                }
            }
//            String loggedinusertype = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE);
//            String userid = dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID);

            return permissionEnabled;
        }
        @JavascriptInterface
        public String userPermissionCheck(String permissionFor)
        {
            String permissionEnabled = "false";
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);


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
        public boolean hasNewItemsCheck(){
            return POSWebPrinting.freshItemCheck(printInfoObj);
        }

        @JavascriptInterface
        public Boolean saveFromPayButton(String serializeForm)
        {
            printInfoObj = new JSONObject();
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);
            setSSIDName();
            try {

                if(ConstantsAndUtilities.TOKEN_GENERATION){
                    String tokenNum = ConstantsAndUtilities.generateTokenNumber();
                }
                printInfoObj.put("content_type","fullinvoice");

                JSONObject obj = new JSONObject(serializeForm);
                String storeId = obj.optString("storeIdForInvoice");
                // Toast.makeText(POSWebActivity.this, "Store ID is "+storeId, Toast.LENGTH_SHORT).show();
                if(storeId.equals("")){
                    Intent intent=new Intent(POSWebActivity.this, MenuScreen.class);
                    startActivity(intent);
                    Toast.makeText(POSWebActivity.this, "Store ID is empty", Toast.LENGTH_SHORT).show();
                    POSWebActivity.this.finish();
                    return false;
                }
                JSONArray itemIds = obj.optJSONArray("itemIds");
                JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
                JSONArray itemNames = obj.optJSONArray("itemNames");
                JSONArray itemNotes = obj.optJSONArray("itemNotes");
                JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
                JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
                JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
                final String holdInvoiceId = obj.optString("holdInvoiceId");
                printInfoObj.put("order_type",sharedPreferences.getString("ordertype", "store sale"));
                if(holdInvoiceId.equals(""))
                {
                    final String newInvoiceNumber  = ConstantsAndUtilities.generateRandomNumber();
                    printInvoiceId = newInvoiceNumber;
                    printInfoObj.put("neworrepeat","New Order");
                    printInfoObj.put("invoiceid",newInvoiceNumber);
                    printInfoObj.put("orderdetails",obj);

                }
                else{
                    printInfoObj.put("neworrepeat","Repeat Order");
                    printInfoObj.put("invoiceid",holdInvoiceId);
                    printInvoiceId = holdInvoiceId;
                    printInfoObj.put("orderdetails",obj);
                }

                final JSONObject newInvoiceObj = obj;
                final String completedInvoiceId = printInvoiceId;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                                    ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT _id FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + completedInvoiceId + "' ORDER BY "+dbVar.INVOICE_ITEM_NAME+" ASC");

                                    if(InvoiceItemDetails.size() > 15 ) {
                                        Toast.makeText(POSWebActivity.this, "Please wait while invoice is being saved.", Toast.LENGTH_SHORT).show();
                                    }

                        } catch (Exception e) {
                        }
                        if(holdInvoiceId.equals("")){
                            SaveFetchVoidInvoice.finalizeNewInvoice(completedInvoiceId, newInvoiceObj);
                        }
                        else{
                            String deletedItemUniueIds = (String) obj.optString("deletedItemsUniqueIdsFromSavedInvoice");
                            if(!deletedItemUniueIds.equals("")) {
                                SaveFetchVoidInvoice sfv = new SaveFetchVoidInvoice();
                                ArrayList<JSONObject> deletedItemsRows = sfv.deletedItemsFromInvoice(deletedItemUniueIds);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                            POSWebPrinting posWebPrinting = new POSWebPrinting();
                                            Boolean categoryWisePrinting = false;
                                            ArrayList<JSONObject> itemListPrintType = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='items_list_print_type'");
                                            Log.v("PrintKOTAuto",itemListPrintType.toString());
                                            if(itemListPrintType.size()==1)
                                            {
                                                JSONObject printingTypeObj = itemListPrintType.get(0);
                                                String valueOfPrintingType = printingTypeObj.optString("value");
                                                if(valueOfPrintingType.equals("categorywise"))
                                                {
                                                    categoryWisePrinting = true;
                                                }
                                            }
                                            if (categoryWisePrinting == true)
                                            {
                                                posWebPrinting.deletedItemsCategorywiseKOTSPrinting(deletedItemsRows, completedInvoiceId, printInfoObj);
                                            }
                                            else
                                            {
                                                POSWebPrinting pWP = new POSWebPrinting();
                                                pWP.deletedItemsKOTSPrinting(deletedItemsRows, completedInvoiceId);

                                            }

                                    }
                                }).start();
                            }
                            SaveFetchVoidInvoice.finalizeOldInvoice(completedInvoiceId,newInvoiceObj);
                        }
                        invoiceFetchTime = "";
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayPrintPopupInWebview();

                                }
                            });

//                            ServerSyncClass ss = new ServerSyncClass();
//                            ss.pendingclear();

                        } catch (Exception e) {
                        }
                    }
                }).start();
            } catch (Exception e) {
                Log.v("Srinath",e.getMessage());
            }

            return true;
        }

        int counter =0;

        private Bitmap getImageFromAssetsFile(String fileName) {
            Bitmap image = null;
            AssetManager am = getResources().getAssets();
            try {
                InputStream is = am.open(fileName);
                image = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;

        }



        @JavascriptInterface
        public void printLastInvoice(String recallInvoiceId){
            String printableInvoiceId = (!recallInvoiceId.equals("")) ? recallInvoiceId : printInvoiceId;
            Boolean alreadyPrinted = POSWebPrinting.printHistoryCheck(printableInvoiceId,"finalbill");
            String duplicatePrintingPermission = userPermissionCheck("reprintinvoice");
            if(alreadyPrinted==false && duplicatePrintingPermission.equals("true")){
                try {
                    printInfoObj = new JSONObject();
                    printInfoObj.put("content_type", "reprintinvoice");
                    printInvoiceId = printableInvoiceId;
                    printTheInvoice();
                }catch (JSONException exp){

                }
            }
        }
        @JavascriptInterface
        public String checkConnectivityDetails(){
            Boolean mysqlConnectivityResult = MainActivity.mySqlObj.checkConnectivity();
            Log.v("Testing","Connection check result is "+mysqlConnectivityResult);
            if(mysqlConnectivityResult==false)
            {
                showAlertDialogJS("Error","Unable to connect to the server MySQL");
                return "false";
            }else{
                return "true";
            }
        }
        @JavascriptInterface
        public void printPreviewReceipt(String previewRecieptInvoiceId,String formData){
            try {
                printInfoObj = new JSONObject();
                JSONObject obj = new JSONObject(formData);
                obj.put("printpreview","true");
                SaveFetchVoidInvoice.saveOnHoldInvoice(previewRecieptInvoiceId,obj,"Repeat");
                printInfoObj.put("orderdetails", obj);
                printInfoObj.put("content_type", "printpreview");
                printInfoObj.put("devicetoken",dbVar.getValueForAttribute("device_token"));
                printInvoiceId = previewRecieptInvoiceId;
                printTheInvoice();
            }catch (JSONException exp){

            }
            Log.v("Printing","Preview Receipt Printing "+previewRecieptInvoiceId+" is the invoice id and "+printInfoObj.toString()+" is the object");
        }
        @JavascriptInterface
        public void invoicePrint(){
            Log.v("Printing","Final Printing "+printInvoiceId+" is the invoice id and "+printInfoObj.toString()+" is the object");
            try{
                printInfoObj.put("content_type", "finalbill");
                printInfoObj.put("devicetoken",dbVar.getValueForAttribute("device_token"));
                printTheInvoice();
            }catch (JSONException exp){

            }

        }
        @JavascriptInterface
        public void addBalanceForCustomer(String amountBeingAdded,String customerId,String modeOfPayment)
        {
            if(customerId.equals(""))
            {
                showAlertDialogJS("Invalid customer selection","Customer should be selected to update balance");
                return;
            }
            CustomersManager sfv = new CustomersManager();
            sfv.AddBalanceForCustomer(amountBeingAdded,customerId,modeOfPayment);
            POSWebPrinting poswebprinting = new POSWebPrinting();
            poswebprinting.lastcustomerAccountPaymentPrint(customerId);
            storeSelectionClick();
        }
        @JavascriptInterface
        public void itemsListPrint(){
            Log.v("ServerPrinting","Items list print called");
            Log.v("Testing","Is saving invoice is "+isSavingInvoice+" at "+ ConstantsAndUtilities.currentTime());

            printItemsList();
        }
        @JavascriptInterface
        public String holdIdForInvoice(String invoiceId){
            String invoiceHoldId = "";
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceId + "' ");
            if(InvoiceDetails.size() > 0){
                JSONObject invoiceRow = InvoiceDetails.get(0);
                invoiceHoldId = invoiceRow.optString(dbVar.INVOICE_HOLD_ID);
            }
            return invoiceHoldId;
        }
        @JavascriptInterface
        public String invoiceItems(final String invoiceId,final String typeOfDisplay){

            Log.v("Fetch",typeOfDisplay+" is the type of display");
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(POSWebActivity.this);
            String ipAddress = sharedPreferences.getString("IPAddress", "");
            if(!ipAddress.equals("")) {

            }

            if(ipAddress.equals("")) {
                ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + invoiceId + "' ORDER BY " + dbVar.INVOICE_ITEM_NAME + " ASC");
                return InvoiceItemDetails.toString();
            }else{
                return null;
            }
        }
        @JavascriptInterface
        public String invoicesList(String storeID,String invoiceStatus){
            String recalledInvoices = "";
            String selectQuery = "";
            String orderBy = "";
            String withinTablesFilter = "";

            String employeeIdLoggedin =  dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID);
            ArrayList<JSONObject> tablesAssignedForEmployees =dbHelper.executeRawqueryJSON("SELECT * FROM employee_advanced_permissions WHERE employee_employee_id='"+employeeIdLoggedin+"_"+storeID+"_assignedtables'");
            if(tablesAssignedForEmployees.size()==1)
            {
                String uniqueTablesId = (String) tablesAssignedForEmployees.get(0).optString("emp_advanced_permissions");
                if(!uniqueTablesId.equals(""))
                {
                    uniqueTablesId = uniqueTablesId.replaceAll(",","','");
                    uniqueTablesId = "'"+uniqueTablesId+"'";
                    withinTablesFilter = " AND booked_table IN (''," + uniqueTablesId + ") ";
                }
            }

            if(invoiceStatus.equals("hold")){
                orderBy = dbVar.INVOICE_HOLD_ID+ " ASC";
            }else{
                orderBy = dbVar.INVOICE_BILL_TS + " desc limit 100 offset 0";
            }
            if (getAdminStatus()) {
                selectQuery = "SELECT  * FROM "
                        + dbVar.INVOICE_TOTAL_TABLE + " where "
                        + dbVar.INVOICE_STATUS + "=\""+invoiceStatus+"\" and "
                        + dbVar.INVOICE_STORE_ID + "=\""
                        + storeID + "\"" + withinTablesFilter
                        + " ORDER BY " + orderBy;
            } else {
                selectQuery = "SELECT  * FROM "
                        + dbVar.INVOICE_TOTAL_TABLE + " where "
                        + dbVar.INVOICE_STATUS + "=\""+invoiceStatus+"\" and "
                        + dbVar.INVOICE_EMPLOYEE + "=\""
                        + userid + "\" and "
                        + dbVar.INVOICE_STORE_ID + "=\""
                        + storeID + "\"" + withinTablesFilter
                        + " ORDER BY " + orderBy;
            }
            ArrayList<JSONObject> recallInvoicesList = dbVar.executeRawqueryJSON(selectQuery);
            recalledInvoices = recallInvoicesList.toString();
            return recalledInvoices;
        }
        @JavascriptInterface
        public String categorywiseTaxes(String storeID,String order_type)
        {
            ArrayList<JSONObject> categoryTaxes = dbHelper.executeRawqueryJSON(
                    "select category_taxes.category_id,category_taxes.taxes_name,category_taxes.taxes_value,category_details.unique_id as category_unique_id from " + dbVar.CAT_TAX_TABLE + ",category_details where "
                            + dbVar.CAT_TAX_STORE + "='" + storeID + "' AND " + dbVar.CAT_TAX_ORDER_TYPE + "='" + order_type + "' AND category_details.category_id=category_taxes.category_id");
            return categoryTaxes.toString();
        }
        @JavascriptInterface
        public String productsForDepartment(String departmentId,String storeId) {
            ArrayList<JSONObject> returnProducts = new ArrayList<>();
            try {
                String notInItems = "";
                String itemStoreDisableCheck = "SELECT store_products_prices.item_no,store_products_prices.disable,store_products_prices.storeid FROM store_products_prices,inventorytable WHERE store_products_prices.disable='1' AND storeid='"+storeId+"' AND store_products_prices.item_no = inventorytable.inventory_item_no AND inventorytable.inventary_department = '"+departmentId+"'";
                ArrayList<JSONObject> inventoryStoreDisableCheck = dbHelper.executeRawqueryJSON(itemStoreDisableCheck);
                if(inventoryStoreDisableCheck.size()>0)
                {
                    notInItems += " AND `inventorytable`.`inventory_item_no` NOT IN (";
                    for(int p=0; p< inventoryStoreDisableCheck.size(); p++)
                    {
                        JSONObject currentRow = inventoryStoreDisableCheck.get(p);
                        String productItemNo = currentRow.optString("item_no");
                        notInItems += "'"+productItemNo+"'";
                        if(p!= ((inventoryStoreDisableCheck.size())-1)){ notInItems += ",";}
                    }
                    notInItems += ")";
                }
                returnProducts = dbHelper.executeRawqueryJSON(
                        "SELECT `inventorytable`.`inventary_item_name`,`inventorytable`.`local_name`, `inventorytable`.`inventory_item_no`, `inventory_product_images`.`compressed_image_path_2x`,`inventory_product_images`.`compressed_image_path` FROM inventorytable LEFT JOIN inventory_product_images\n" +
                                "ON inventory_product_images.inventory_item_no = inventorytable.inventory_item_no WHERE check_value='true' AND " + dbVar.INVENTORY_DEPARTMENT + "='" + departmentId + "' "+notInItems+" ORDER BY `inventorytable`.`inventary_item_name` ASC");

            }catch (Exception exp)
            {
                Log.v("Srinath","Exception is "+exp.toString());
            }
            return  returnProducts.toString();
        }
        @JavascriptInterface
        public String allImages(){
            String base = Environment.getExternalStorageDirectory().getAbsolutePath().toString();

            ArrayList<JSONObject> returnProducts = new ArrayList<>();
            try {
                returnProducts = dbHelper.executeRawqueryJSON(
                        "SELECT `inventorytable`.`inventary_item_name`,`inventorytable`.`local_name`, `inventorytable`.`inventory_item_no`, `inventory_product_images`.`compressed_image_path_2x`,`inventory_product_images`.`compressed_image_path` FROM inventorytable LEFT JOIN inventory_product_images\n" +
                                "ON inventory_product_images.inventory_item_no = inventorytable.inventory_item_no WHERE check_value='true' ");
            }catch (Exception exp)
            {
                Log.v("Srinath","Exception is "+exp.toString());
            }
            return  returnProducts.toString();
        }
        @JavascriptInterface
        public void gotourl(String url) {
//            PosWebActivity.loadWebviewwithUrl(url);

        }
        @JavascriptInterface
        public void saveOrderType(String orderType) {

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("ordertype", orderType);
            selectedOrderType = orderType;
            editor.commit();

        }

    }

    private void printItemsList() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean categoryWisePrinting = false;
                ArrayList<JSONObject> itemListPrintType = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='items_list_print_type'");
                Log.v("PrintKOTAuto",itemListPrintType.toString());
                if(itemListPrintType.size()==1)
                {
                    JSONObject printingTypeObj = itemListPrintType.get(0);
                    String valueOfPrintingType = printingTypeObj.optString("value");
                    if(valueOfPrintingType.equals("categorywise"))
                    {
                        categoryWisePrinting = true;
                    }

                    if(isServerPrint==true){

                        Log.v("PrintKOTAuto","Server Print invoice");
                        POSWebPrinting.postDataToServerForPrinting(printInvoiceId,printInfoObj,"itemslist");
                        return;
//            return;
                    }
                }
                POSWebPrinting posWebPrinting = new POSWebPrinting();
                if (categoryWisePrinting)
                {
                    Log.v("PrintKOTAuto","Category wise Printing is true");
                    posWebPrinting.printCategoryWiseKots(printInvoiceId,printInfoObj);

                }else{
                    String printString = posWebPrinting.htmlContentForItemsList(printInvoiceId,printInfoObj);
                    Log.v("PrintKOTAuto",printString);
                }
                try {
                    POSWebActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MyApplication.getAppContext(), "Items List Print Sent", Toast.LENGTH_SHORT).show();
                        }
                    });

                }catch (Exception exp){
                    exp.printStackTrace();
                }
            }

        }).start();

    }

    private void printTheInvoice() {
        Log.v("PrintKOTAuto","We have to print the invoice");

        if(isServerPrint==true || isServerPrintOnlyForInvoice==true){

            Log.v("PrintKOTAuto","Server Print invoice");
            POSWebPrinting.postDataToServerForPrinting(printInvoiceId,printInfoObj,"finalbill");
//            return;
        }
//            posWebPrinting.printHtmlToPrinter(printString,cv.primaryPrinterName(),true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                POSWebPrinting posWebPrinting = new POSWebPrinting();
                String printString = posWebPrinting.htmlContentForInvoice(printInvoiceId,printInfoObj);

            }
        }).start();
            return;


    }

    public void displayPrintPopupInWebview(){
        webView.loadUrl("javascript:showPrintPopup();");
    }

    private void shiftTablePrint(String oldOrderRefNum,String fromInvoice,String  toTable,String  newOrderRefNum,String  storeId,String  mergingOrShifting) {


        POSWebPrinting.shiftTablePrint(oldOrderRefNum, fromInvoice, toTable, newOrderRefNum, storeId, mergingOrShifting);

    }

    private void showDateTimePicker() {
        Calendar date;

        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(getApplicationContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                date.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        Log.v("Srinath", "The choosen one " + date.getTime());
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void establishPrinterConnectionsWeb() {
    }

    private void goForBarcodeScan() {
    }

    private boolean getAdminStatus() {
        if (!usertype.equals("admin")) {
            String query = "SELECT * from " + dbVar.EMPLOYEE_TABLE
                    + " where " + dbVar.EMPLOYEE_EMPLOYEE_ID + "=\""
                    + userid + "\"";
            ArrayList<JSONObject> adminCard = dbVar.executeRawqueryJSON(query);
            String vall = "";
            if (adminCard!=null && adminCard.size() > 0) {


                for(int cnt=0; cnt<adminCard.size();cnt++)
                {
                    JSONObject currentRow = adminCard.get(cnt);

                    if (currentRow.optString(dbVar.EMPLOYEE_ADMIN_CARD).equals("")) {
                    } else {
                        vall = ""
                                + currentRow.optString(dbVar.EMPLOYEE_ADMIN_CARD)
                                .trim();
                    }
                }

            }
            if (vall.equals("true"))
                return true;
            else
                return false;
        } else {
            return true;
        }
    }
    public void setSSIDName()
    {
        /*
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        System.out.println(wifiManager.toString());
        WifiInfo info = wifiManager.getConnectionInfo();
        ssid  = info.getSSID();
        ssid = ssid. replaceAll("^\"|\"$", "");
        Log.v("Printing","Ssid name is "+ssid); */
    }
}