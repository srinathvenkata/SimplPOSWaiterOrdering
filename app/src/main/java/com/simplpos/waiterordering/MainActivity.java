package com.simplpos.waiterordering;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.http.SslError;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.simplpos.waiterordering.helpers.ConstantsAndUtilities;
import com.simplpos.waiterordering.helpers.DatabaseHelper;
import com.simplpos.waiterordering.helpers.DatabaseVariables;
import com.simplpos.waiterordering.helpers.DialogBox;
import com.simplpos.waiterordering.helpers.MySQLJDBC;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends FragmentActivity {
    public static MySQLJDBC mySqlObj = null;
    public static MySQLJDBC mySqlCrmObj = null;
    public static String currencyTypehtml = "$";
    public DatabaseHelper dbHelper = null;
    WebView webView;
    SharedPreferences preferences = null;//  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
    DatabaseVariables dbVar = null;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



/*
        Thread thread = new Thread(this);
        thread.start();
*/

        initiateConnections();
        initControls();
        dbHelper = SplashScreen.dbHelper;
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        dbVar = new DatabaseVariables();
        saveFirebaseToken();
    }

    private void saveFirebaseToken() {
        String firebaseToken = preferences.getString(ConstantsAndUtilities.firebaseToken, "");
        dbVar.replaceAttributesWithValues("device_token", firebaseToken);
        Log.v("FirebaseToken", "Firebase token is " + firebaseToken);
    }

    private void initControls() {
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        progressBar = findViewById(R.id.pb);
        webView = findViewById(R.id.webview);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);


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
        webView.setWebChromeClient(new WebChromeClient() {
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

        String connectivityAddress = sharedpreferences.getString("connectivityAddress", "");
        String usagePurpose = sharedpreferences.getString("usagePurpose", "");
//        retrieveUsersList();
        webView.loadUrl("file:///android_asset/login.html");

        String ssidName = ssidName();
        Log.v("Printing", ssidName + " is the SSID");
        ConstantsAndUtilities cv = new ConstantsAndUtilities();
        JSONObject assignmentOfPrinters = cv.StringToJSONObj(SplashScreen.printersByNetworkWifiAssignment);
        Log.v("Printing", "" + assignmentOfPrinters.toString());
        String counterPrinter = cv.counterPrinterForSSID(assignmentOfPrinters, ssidName);
        Log.v("Printing", "Counter Printer is " + counterPrinter);
        String foodPrinter = cv.categoryAssignedPrinterForSSID(assignmentOfPrinters, ssidName, "Food");
        String liquorPrinter = cv.categoryAssignedPrinterForSSID(assignmentOfPrinters, ssidName, "Liquor");

        Log.v("Printing", "Food Printer is " + foodPrinter);
        Log.v("Printing", "Liquor Printer is " + liquorPrinter);

    }

    private void initiateConnections() {

        Log.v("Connection", "Check connection being called");
        ConstantsAndUtilities.userid = "";
        ConstantsAndUtilities.usertype = "";
        ConstantsAndUtilities.useridPass = "";

    }

    public class WebAppInterface {
        Context mContext;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

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
        public void checkLocationServices(){
          /*  if(isLocationEnabled(getApplicationContext()))
            {
                Log.v("Printing","Location is enabled");
            }else{
                Log.v("Printing","Location is disabled");
            Toast.makeText(getApplicationContext(), "Please enable your location to access wifi state", Toast.LENGTH_SHORT).show();
            showAlertDialogJS("Location Access Error","Please enable your location");
            } */
        }
        @JavascriptInterface
        public void saveMysqlDetails(String hostaddress, String mysqluserid, String mysqlpassword, String mysqluserportnumber, String apachePortNumber) {
            MySQLJDBC tempSqlObj = new MySQLJDBC("simplposdata", mysqluserid, mysqlpassword, mysqluserportnumber, hostaddress);
            if (tempSqlObj.checkConnectivity() == true) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ConstantsAndUtilities.MYSQLUSERID, mysqluserid);
                editor.putString(ConstantsAndUtilities.MYSQLPASSWORD, mysqlpassword);
                editor.putString(ConstantsAndUtilities.MYSQLPortNumber, mysqluserportnumber);
                editor.putString(ConstantsAndUtilities.HOSTAddress, hostaddress);
                editor.putString(ConstantsAndUtilities.APACHE_PORT_NUMBER, apachePortNumber);
                editor.commit();

                MainActivity.mySqlObj = new MySQLJDBC("simplposdata", mysqluserid, mysqlpassword, mysqluserportnumber, hostaddress);
                MainActivity.mySqlCrmObj = new MySQLJDBC("simplposcrm", mysqluserid, mysqlpassword, mysqluserportnumber, hostaddress);

                showAlertDialogJS("Success", "Connection has been successfully established");
            } else {
                showAlertDialogJS("Error", "Connection failed with the given credentials");
            }
        }

        @JavascriptInterface
        public String allConfigDetails() {
            JSONObject configDetails = new JSONObject();
            String companyId = "";
            ArrayList<JSONObject> preferencesData = dbHelper.executeRawqueryJSON("SELECT * FROM " + dbHelper.PREFERENCES_TABLE + " WHERE " + dbHelper.PREFERENCES_ATTRIBUTE + "='companyid'");
            if (preferencesData.size() == 1) {
                JSONObject prefObj = preferencesData.get(0);
                companyId = prefObj.optString(dbHelper.PREFERENCES_VALUE);
            }

            try {
                configDetails.put("company_id", companyId);
                configDetails.put("mysqluserid", preferences.getString(ConstantsAndUtilities.MYSQLUSERID, ""));
                configDetails.put("mysqlpassword", preferences.getString(ConstantsAndUtilities.MYSQLPASSWORD, ""));
                configDetails.put("mysqluserportnumber", preferences.getString(ConstantsAndUtilities.MYSQLPortNumber, ""));
                configDetails.put("hostaddress", preferences.getString(ConstantsAndUtilities.HOSTAddress, ""));
                configDetails.put("username", preferences.getString(ConstantsAndUtilities.REMEMBEREDUSERNAME, ""));
                configDetails.put("password", preferences.getString(ConstantsAndUtilities.REMEMBEREDPASSWORD, ""));
                configDetails.put("apacheportnumber", preferences.getString(ConstantsAndUtilities.APACHE_PORT_NUMBER, ""));
                configDetails.put("orienation", ((dbVar.getValueForAttribute(ConstantsAndUtilities.SCREENORIENTATION) == "") ? ("landscape") : (dbVar.getValueForAttribute(ConstantsAndUtilities.SCREENORIENTATION))));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return configDetails.toString();
        }

        @JavascriptInterface
        public void savescreenOrientationForPOS(String orientation) {
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.SCREENORIENTATION, orientation);
            showAlertDialogJS("Success", "Orientation saved to " + orientation + " Successfully");
        }

        @JavascriptInterface
        public void userLogin(String username, String password, String rememberMe) {
            Log.v("userLogin", "Remember me is " + rememberMe);
            Boolean loginCheck = loginCheck(username, password);
            if (loginCheck == true) {
                saveLoggedInUserToDB();
                if (rememberMe.equals("true")) {
                    rememberUser(username, password);
                } else {
                    forgetUser();
                }
                stopLoadingDialog();
                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent;
                intent = new Intent(MainActivity.this, MenuScreen.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
//                 finish();
                return;
            } else {
                showAlertDialogJS("Error", "Invalid login credentials");
            }
        }

        @JavascriptInterface
        public void showLoadingDialog() {

            // called before request is started
            dialog = new ProgressDialog(MainActivity.this);
//            dialog.show();
//            dialog.setContentView(R.layout.progress_dialog);
//            dialog.setCanceledOnTouchOutside(true);
        }

        @JavascriptInterface
        public void stopLoadingDialog() {
            dialog.hide();
        }

        @JavascriptInterface
        public void saveCompanyID(String companyID) {
            final String server_url = ConstantsAndUtilities.serverUrl + "crm/serverurl.php";
            Log.v("Fetch", server_url);
            final RequestParams params = new RequestParams();

            try {
                params.put("companyid", companyID);

            } catch (Exception e) {
                e.printStackTrace();
            }
            webView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.v("GET", params.toString());
                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestHandle post = client.post(server_url, params, new TextHttpResponseHandler() {

                            /**
                             * Called when request fails
                             *
                             * @param statusCode     http response status line
                             * @param headers        response headers if any
                             * @param responseString string response of given charset
                             * @param throwable      throwable returned when processing request
                             */
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(getApplicationContext(), "Error : " + responseString, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onStart() {
                                // called before request is started
                                dialog = new ProgressDialog(MainActivity.this);
                                dialog.show();
                                dialog.setContentView(R.layout.progress_dialog);
                                dialog.setCanceledOnTouchOutside(true);
//                                dialog.getWindow().setBackgroundDrawableResource(
//                                        android.R.color.transparent
//                                );
                            }


                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Log.v("Fetch", responseString);
                                Boolean invoiceEditedAfterFetch = false;
                                try {
                                    JSONObject obj = new JSONObject(responseString);
                                    if (obj.has("response")) {
                                        JSONObject response = (JSONObject) obj.get("response");
                                        if (response.has("result")) {
                                            String here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
                                            String uniq = "companyid";
                                            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                                                    here, new String[]{uniq});


                                            here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
                                            uniq = "server_url";
                                            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                                                    here, new String[]{uniq});

                                            Boolean result = response.getBoolean("result");
                                            if (result == true) {
                                                showAlertDialogJS("Success", "Company ID is valid");


                                                ContentValues cv = new ContentValues();
                                                cv.put(dbHelper.PREFERENCES_ATTRIBUTE, "companyid");
                                                cv.put(dbHelper.PREFERENCES_VALUE, companyID);
                                                dbHelper.insertData(cv, dbHelper.PREFERENCES_TABLE);

                                                cv = new ContentValues();

                                                cv.put(dbHelper.PREFERENCES_ATTRIBUTE, "server_url");
                                                cv.put(dbHelper.PREFERENCES_VALUE, response.getString("serverurl"));
                                                dbHelper.insertData(cv, dbHelper.PREFERENCES_TABLE);
                                                // serverurl
                                            } else {
                                                showAlertDialogJS("Error", "Company ID is invalid");
                                            }
                                        }
                                    }
                                } catch (JSONException exp) {
                                    exp.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Invalid Response. " + exp.getMessage(), Toast.LENGTH_LONG).show();

                                }
                                dialog.hide();

                            }

                            @Override
                            public void onRetry(int retryNo) {
                                // called when request is retried
                            }
                        });

                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }

        @JavascriptInterface
        public void tryAgainForCaptainOrdering() {
            finish();
            startActivity(getIntent());
            return;
        }

        @JavascriptInterface
        public void goToMainScreen() {

        }

        @JavascriptInterface
        public void goBackToMain() {

        }

        @JavascriptInterface
        public void rememberUser(String userId, String password) {

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(ConstantsAndUtilities.REMEMBEREDUSERNAME, userId);
            editor.putString(ConstantsAndUtilities.REMEMBEREDPASSWORD, password);
            editor.commit();

        }

        @JavascriptInterface
        public void forgetUser() {

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(ConstantsAndUtilities.REMEMBEREDUSERNAME, "");
            editor.putString(ConstantsAndUtilities.REMEMBEREDPASSWORD, "");
            editor.commit();

        }

        @JavascriptInterface
        public String getFirebaseToken() {
            return "12345";
        }

        @JavascriptInterface
        public String getCompanyId() {
            String compId = "";
            Log.v("Printing", "Get company ID Called");
            compId = sharedpreferences.getString("companyId", "demo");
            Log.v("Printing", "Get company ID Called" + compId);
            return compId;
        }

        @JavascriptInterface
        public void gotoConfigPage() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("file:///android_asset/config.html");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v("Config", "Config page has to be retrieved");
        }

        @JavascriptInterface
        public void showAlertDialogJS(String titleMessage, String contentMessage) {
            DialogBox dbx = new DialogBox();
            boolean loadDialog = dbx.dialogBox(titleMessage, contentMessage, "OK", "Cancel", mContext);
        }

        @JavascriptInterface
        public void savePrinterConfigDetails(String configobject) {

        }

        @JavascriptInterface
        public String getUsername() {

            String compId = "";
            compId = sharedpreferences.getString("username", "srinath");
            return compId;
        }

        @JavascriptInterface
        public String getPassword() {
            String compId = "";
            compId = sharedpreferences.getString("password", "srinath");
            return compId;
        }


        @JavascriptInterface
        public boolean validateThePin(String pinNumber) {
            if (pinNumber.equals("1234")) {
                return true;
            } else {
                return false;
            }
        }


        @JavascriptInterface
        public String getAllConfigDetails() {

        /*
        connectivityType        : (jQuery("input[name='printerConnectivityType']:checked").val()),
                connectivityAddress     : (jQuery("#connectivityAddress").val()),
                startURL                : (jQuery("#startURL").val()),
                printerModel            : (jQuery("#printerModel").val())
         */
            JSONObject configJsonObj = new JSONObject();
            try {
                String connectivityType = sharedpreferences.getString("usagePurpose", "");
                Log.v("Srinath", "Connectivity type is " + connectivityType);

                configJsonObj.put("usagePurpose", connectivityType);

                String connectivityAddress = sharedpreferences.getString("connectivityAddress", "");
                configJsonObj.put("connectivityAddress", connectivityAddress);
                String startURL = sharedpreferences.getString("startURL", "");
                configJsonObj.put("startURL", startURL);
                String printerModel = sharedpreferences.getString("printerModel", "");
                configJsonObj.put("printerModel", printerModel);
                Log.v("Srinath", "Config obj to string is " + configJsonObj.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (configJsonObj.toString());
        }
    }

    private void saveLoggedInUserToDB() {
        dbVar = new DatabaseVariables();
        dbVar.replaceAttributesWithValues(ConstantsAndUtilities.SP_LOGGEDINUSERID, ConstantsAndUtilities.userid);
        dbVar.replaceAttributesWithValues(ConstantsAndUtilities.SP_LOGGEDINUSERPASS, ConstantsAndUtilities.useridPass);
        dbVar.replaceAttributesWithValues(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE, ConstantsAndUtilities.usertype);
    }
    private Boolean loginCheck(String userId, String checkPw) {
        Boolean loginresult = false;

        try {


            userId = userId.trim().toLowerCase();
            ConstantsAndUtilities util = new ConstantsAndUtilities();
            // change here
            String md5Pass = util.md5Encryption(checkPw);
            JSONObject loginServerJSONResponse = new JSONObject();
            SharedPreferences prefs = preferences;
            DatabaseVariables dbVar = new DatabaseVariables();

//            javascriptConnector.call("showResult", "showLoadingBootbox");

            Boolean serverLoginCheck = null; //validateUserLoginFromServer(userId,md5Pass);
//            javascriptConnector.call("showResult", "hideLoadingBootbox");

            JSONObject responseObject = new JSONObject();
            ArrayList<JSONObject> adminResults = mySqlObj.executeRawqueryJSON("SELECT * FROM admin_details WHERE userid='" + userId + "' AND password='" + md5Pass + "'");
            ArrayList<JSONObject> userResults = mySqlObj.executeRawqueryJSON("SELECT * FROM " + DatabaseVariables.EMPLOYEE_TABLE + " WHERE " + DatabaseVariables.EMPLOYEE_EMPLOYEE_ID + "='" + userId + "' AND " + DatabaseVariables.EMPLOYEE_PASSWORD + "='" + md5Pass + "'");
            Log.v("LoginCheck", adminResults.toString());
            Log.v("LoginCheck", userResults.toString());
            try {
                if (serverLoginCheck != null) {
                    System.out.println(loginServerJSONResponse);
                    if (serverLoginCheck == true) {
                        responseObject.put("validation", true);
                        responseObject.put("message", "Login Successful");
                        String userFirstAndLastName = userId;

                        if (userResults.size() > 0) {
                            userFirstAndLastName = (String) userResults.get(0).get(dbVar.EMPLOYEE_DISPLAY_NAME);
                        } else if (adminResults.size() > 0) {
                            JSONObject userRow = adminResults.get(0);
                            userFirstAndLastName = (String) userRow.get(dbVar.FIRSTNAME) + " " + userRow.get(dbVar.LASTNAME);
                        }


                        ConstantsAndUtilities.userid = userId;
                        ConstantsAndUtilities.usertype = (String) loginServerJSONResponse.get("user-type");
                        ConstantsAndUtilities.useridPass = checkPw;
                    } else {
                        responseObject.put("validation", false);
                        responseObject.put("message", "Invalid Login Credentials");
                    }
                    return false;


                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
            if (userResults.size() > 0) {
                responseObject.put("validation", true);
                responseObject.put("message", "Login Successful");
                JSONObject userRow = (JSONObject) userResults.get(0);
                String userFirstAndLastName = (String) userRow.get(dbVar.EMPLOYEE_DISPLAY_NAME);
                ConstantsAndUtilities.userid = userId;
                ConstantsAndUtilities.usertype = "employee";
                ConstantsAndUtilities.useridPass = checkPw;
                return true;
            } else if (adminResults.size() > 0) {
                responseObject.put("validation", true);
                responseObject.put("message", "Login Successful");
                try {
                    JSONObject userRow = adminResults.get(0);
                    String userFirstAndLastName = (String) userRow.get(dbVar.FIRSTNAME) + " " + userRow.get(dbVar.LASTNAME);
//                        prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERID, userId);
//                        prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE, "admin");
//                        prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERPASS, checkPw);
//                        prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERNAME, userFirstAndLastName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ConstantsAndUtilities.userid = userId;
                ConstantsAndUtilities.usertype = "admin";
                ConstantsAndUtilities.useridPass = checkPw;
                return true;
            } else {
                responseObject.put("validation", false);

                ArrayList<JSONObject> userIDResults = mySqlObj.executeRawqueryJSON("SELECT * FROM admin_details WHERE userid='" + userId + "' ");
                if (userIDResults.size() == 0) {
                    responseObject.put("message", "Invalid User ID");
                } else {
                    responseObject.put("message", "Invalid Password");
                }
//                    prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERID,"");
//                    prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE,"");
//                    prefs.put(ConstantsAndUtilities.SP_LOGGEDINUSERPASS,"");
            }


            return false;

            /*ArrayList<JSONObject> preferencesData = dbHelper.executeRawqueryJSON("SELECT * FROM " + dbHelper.PREFERENCES_TABLE + " WHERE " + dbHelper.PREFERENCES_ATTRIBUTE + "='server_url'");
            if (preferencesData.size() != 0) {
                loginresult = true;
            }*/
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return loginresult;
    }

    public String ssidName() {
        return "";
        /* WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        String ssid = info.getSSID();
        ssid = ssid.replaceAll("^\"|\"$", "");
        return ssid; */
    }

    public String findSSIDForWifiInfo(WifiManager manager, WifiInfo wifiInfo) {
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            List<WifiConfiguration> listOfConfigurations = manager.getConfiguredNetworks();

            for (int index = 0; index < listOfConfigurations.size(); index++) {
                WifiConfiguration configuration = listOfConfigurations.get(index);
                if (configuration.networkId == wifiInfo.getNetworkId()) {
                    return configuration.SSID;
                }
            }
        } */


        return null;
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