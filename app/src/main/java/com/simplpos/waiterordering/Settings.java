package com.simplpos.waiterordering;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
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
import com.simplpos.waiterordering.helpers.Java_AES_Cipher;
import com.simplpos.waiterordering.helpers.MySQLJDBC;
import com.simplpos.waiterordering.helpers.ServerSync;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import cz.msebera.android.httpclient.Header;

public class Settings extends FragmentActivity implements Runnable {

    public DatabaseHelper dbHelper = null;
    WebView webView;
    SharedPreferences preferences = null;//  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
    DatabaseVariables dbVar = null;
    private ProgressDialog dialog;
    private boolean unequalQty = false;

    private static String encryptedValue = "";
    public static Boolean isRunningCopy = false;
    MySQLJDBC sqlCrmObj = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);





        Thread thread = new Thread(this);
        thread.start();

        dbHelper = SplashScreen.dbHelper;
        preferences =  PreferenceManager.getDefaultSharedPreferences(Settings.this);
        dbVar = new DatabaseVariables();

        initiateConnections();
        initControls();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void initControls() {
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        progressBar = findViewById(R.id.pb);
        webView = findViewById(R.id.webview);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);


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
        webView.loadUrl("file:///android_asset/settings.html");

    }

    private void initiateConnections() {
        Log.v("Menu","Logged in User is "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
        Log.v("Menu","Logged in User password "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERPASS));
        Log.v("Menu","Logged in User type "+dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERTYPE));
        sqlCrmObj = MainActivity.mySqlCrmObj;
    }

    @Override
    public void run() {
        Log.v("Menu","Thread has started");
        checkServervsLocalDb();
    }

    private void checkServervsLocalDb() {

    }


    public class WebAppInterface {
        Context mContext;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);

        // Instantiate the interface and set the context
        WebAppInterface(Context c) {
            mContext = c;
            sharedpreferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);


        }
        @JavascriptInterface
        public String allConfigDetails()
        {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);

            JSONObject allConfigDetails = new JSONObject();
            String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
            String invoiceSerialPrefix = dbVar.getValueForAttribute(ConstantsAndUtilities.INV_SERIAL_PREFIX);
            String invoiceSerialNumber = dbVar.getValueForAttribute(ConstantsAndUtilities.INV_SERIAL_NUMBER);
            String preferredPrinter = dbVar.getValueForAttribute(ConstantsAndUtilities.PREFERRED_PRINTER);
            try{
                String upToNCharacters = "";
                String lastFour = "";
                if(licenseKey.length()>8) {
                    String actualLicenseKey = licenseKey;
                    licenseKey = licenseKey.replaceAll("\\S", "*");
                    upToNCharacters = licenseKey.substring(0, Math.min(licenseKey.length(), (licenseKey.length() - 4)));
                    lastFour = actualLicenseKey.substring(actualLicenseKey.length() - 4);
                }

                allConfigDetails.put("license_key",upToNCharacters + lastFour);
                allConfigDetails.put(ConstantsAndUtilities.INV_SERIAL_PREFIX,invoiceSerialPrefix);
                allConfigDetails.put(ConstantsAndUtilities.INV_SERIAL_NUMBER,invoiceSerialNumber);
                allConfigDetails.put(ConstantsAndUtilities.PREFERRED_PRINTER,preferredPrinter);
                allConfigDetails.put("storesale_text",sharedPreferences.getString("dinetext", ""));
                allConfigDetails.put("takeaway_text",sharedPreferences.getString("tacktext", ""));
            }catch (Exception exp){
                exp.printStackTrace();
            }
            return allConfigDetails.toString();
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @JavascriptInterface
        public void validateLicenseKey(String enteredLicenseKey)
        {

            // check if user has permissions
            if(userPermissionCheck("settings").equals("false"))
            {
                showAlertDialogJS("Permission Denied","You donot have the permissions to set the license key");
                return;
            }
            String MacAddress = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);;//prefs.get(ConstantsAndUtilities.MACADDR,"");
            sqlCrmObj = MainActivity.mySqlCrmObj;
            if(sqlCrmObj.checkConnectivity()==false){
                return;
            }
            EncryptionFromServer(enteredLicenseKey);//Java_AES_Cipher.DarKnight.getEncrypted(enteredLicenseKey);

        }
        @JavascriptInterface
        public String printersList()
        {
            Log.v("PrintersList",(MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM printers WHERE 1 ORDER BY printer_name ASC")).toString());
            return (MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM printers WHERE 1 ORDER BY printer_name ASC")).toString();
        }
        @JavascriptInterface
        public String selectedPrinter()
        {
            String printerRowId= "";
            String selectedPrinter = dbVar.getValueForAttribute(ConstantsAndUtilities.PREFERRED_PRINTER);
            ArrayList<JSONObject> printerDetails = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM printers WHERE printer_name='"+selectedPrinter+"'");
            if(printerDetails.size()==0)
            {
                return "";
            }
            else{
                JSONObject printerRow = printerDetails.get(0);
                printerRowId = printerRow.optString("_id");
            }
            return printerRowId;
        }
        @JavascriptInterface
        public void savePrinterSelection(String printerRowId)
        {

            // check if license key has been entered
            String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
            if(licenseKey.equals(""))
            {
                showAlertDialogJS("Permission Denied","You donot have a valid license key");
                return;
            }
            // check if user has permissions
            if(userPermissionCheck("settings").equals("false"))
            {
                showAlertDialogJS("Permission Denied","You donot have the permissions to change settings");
                return;
            }
            String printerName= "";
            ArrayList<JSONObject> printerDetails = MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM printers WHERE _id='"+printerRowId+"'");
            if(printerDetails.size()==0)
            {
                showAlertDialogJS("Error","Printer name could not be found");
                return;
            }
            else{
                JSONObject printerRow = printerDetails.get(0);
                printerName = printerRow.optString("printer_name");
            }
            //Now save the serial number
            showAlertDialogJS("Success","You have successfully saved the printer selection");
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.PREFERRED_PRINTER,printerName);
        }
        @JavascriptInterface
        public void saveOrderTypeNamingConvention(String storeSaleText,String takeAwayText)
        {
            if(userPermissionCheck("settings").equals("false"))
            {
                showAlertDialogJS("Permission Denied","You donot have the permissions to change settings");
                return;
            }
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);

            SharedPreferences.Editor edit = sharedPreferences.edit();

            edit.putString("tacktext", takeAwayText);
            edit.putString("dinetext", storeSaleText);
            edit.commit();
            showAlertDialogJS("Success","You have successfully saved the Order Type Names");

        }
        @JavascriptInterface
        public void saveSerialNumber(String prefix, String serialNum)
        {
            // check if license key has been entered
            String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
            if(licenseKey.equals(""))
            {
                showAlertDialogJS("Permission Denied","You donot have the permissions to change settings");
                return;
            }
            // check if user has permissions
            if(userPermissionCheck("settings").equals("false"))
            {
                showAlertDialogJS("Permission Denied","You donot have the permissions to change settings");
                return;
            }

            //Now save the serial number
            showAlertDialogJS("Success","You have successfully saved the serial number of the invoice");
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.INV_SERIAL_PREFIX,prefix);
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.INV_SERIAL_NUMBER,serialNum);
        }
        @JavascriptInterface
        public String userPermissionCheck(String permissionFor)
        {
            String permissionEnabled = "false";
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(Settings.this);


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
        public void printLog(String msg) {
            Log.v("Printing", msg);
        }
        @JavascriptInterface
        public void goToMenu()
        {

            Intent intent;
            intent = new Intent(Settings.this, MenuScreen.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
            dialog = new ProgressDialog(Settings.this);
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


    }

    private void EncryptionFromServer(String enteredLicenseKey) {
        String encryptedLicenseKey = "";


        final String server_url = ConstantsAndUtilities.serverUrl+"crm/cipherencryptdecrypt.php";
        Log.v("Fetch",server_url);
        final RequestParams params = new RequestParams();

        try {
            params.put("text", enteredLicenseKey);
            params.put("function_type", "encryption");

        }catch (Exception e){
            e.printStackTrace();
        }
                try{
                    Log.v("GET",params.toString());
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
                            dialog.hide();
                            Toast.makeText(getApplicationContext(), "Error : "+responseString, Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onStart() {
                            // called before request is started
                            dialog = new ProgressDialog(Settings.this);
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
                                if(obj.has("response"))
                                {
                                     encryptedValue = String.valueOf(obj.optString("response"));
                                     Log.v("Encryption","Encrypted value in response "+encryptedValue);
                                    validateLicenseKeyWithEncryptedString(encryptedValue,enteredLicenseKey);

                                }
                            } catch (JSONException exp) {
                                exp.printStackTrace();
                                Toast.makeText(Settings.this, "Invalid Response. " + exp.getMessage(), Toast.LENGTH_LONG).show();

                            }
                            dialog.hide();

                        }

                        @Override
                        public void onRetry(int retryNo) {
                            // called when request is retried
                        }
                    });

                }catch (Exception exception){
                    exception.printStackTrace();
                }
    }

    private void validateLicenseKeyWithEncryptedString(String encryptedValue,String enteredLicenseKey) {

        String aboutPhone = "Android Phone";
        try {
            aboutPhone = "version:" + android.os.Build.VERSION.SDK
                    + "device:" + android.os.Build.DEVICE + "Model:"
                    + android.os.Build.MODEL + "product:"
                    + android.os.Build.PRODUCT;;
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<JSONObject> existingLicenseCheck = sqlCrmObj.executeRawqueryJSON("SELECT * FROM addon_counters_licenses WHERE license_key='"+encryptedValue+"'");
        if(existingLicenseCheck.size()==1)
        {
            JSONObject existingLicenseRow = existingLicenseCheck.get(0);
            //
            String makeothersBlank = "UPDATE addon_counters_licenses SET mac_addr='',devicetoken='',aboutphone='' WHERE mac_addr='"+ConstantsAndUtilities.addSlashes(SplashScreen.MacAddress)+"' AND license_key !='"+encryptedValue+"'";
            sqlCrmObj.executeRawquery(makeothersBlank);
            JSONObject insertObj = new JSONObject();
            try {
                insertObj.put("mac_addr", SplashScreen.MacAddress);

                insertObj.put("device_os","Android");
                insertObj.put("aboutphone",aboutPhone);
                insertObj.put("devicetoken",dbVar.getValueForAttribute("device_token"));
                insertObj.put("employee_id",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                insertObj.put("updated_on",ConstantsAndUtilities.currentTime());
                sqlCrmObj.executeUpdate("addon_counters_licenses",insertObj,"license_key",encryptedValue);

                String validUpto = String.valueOf(existingLicenseRow.get("valid_upto"));
//                prefs.put(ConstantsAndUtilities.LICENSE_KEY_VALID_UPTO, validUpto);
                dbVar.replaceAttributesWithValues(ConstantsAndUtilities.LICENSE_KEY_VALID_UPTO,validUpto);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            WebAppInterface wp = new WebAppInterface(Settings.this);
            wp.showAlertDialogJS("Success","License Key has been successfully validated");

            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.LICENSE_KEY,enteredLicenseKey);
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.ENCRYPTED_LICENSE_KEY,encryptedValue);
            dbVar.replaceAttributesWithValues(ConstantsAndUtilities.DECRYPTED_LICENSE_KEY,enteredLicenseKey);

        }else{
            String makeothersBlank = "UPDATE addon_counters_licenses SET mac_addr='',devicetoken='',aboutphone='' WHERE mac_addr='"+ConstantsAndUtilities.addSlashes(SplashScreen.MacAddress)+"'";
            sqlCrmObj.executeRawquery(makeothersBlank);

            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.LICENSE_KEY_VALID_UPTO});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.LICENSE_KEY});

            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.INV_SERIAL_NUMBER});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.INV_SERIAL_PREFIX});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.PREFERRED_PRINTER});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.ENCRYPTED_LICENSE_KEY});
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,dbHelper.PREFERENCES_ATTRIBUTE+"=?",new String[]{ConstantsAndUtilities.DECRYPTED_LICENSE_KEY});

            WebAppInterface wp = new WebAppInterface(Settings.this);
            wp.showAlertDialogJS("Error","Invalid License Key");

        }
    }
}