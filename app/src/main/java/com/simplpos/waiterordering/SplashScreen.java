package com.simplpos.waiterordering;

import static com.simplpos.waiterordering.helpers.ConstantsAndUtilities.currentTime;
import static com.simplpos.waiterordering.helpers.ConstantsAndUtilities.randomValue;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
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

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.messaging.FirebaseMessaging;
import com.simplpos.waiterordering.helpers.ConstantsAndUtilities;
import com.simplpos.waiterordering.helpers.DatabaseHelper;
import com.simplpos.waiterordering.helpers.DatabaseVariables;
import com.simplpos.waiterordering.helpers.DialogBox;
import com.simplpos.waiterordering.helpers.Java_AES_Cipher;
import com.simplpos.waiterordering.helpers.MySQLJDBC;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.ArrayList;

import javax.crypto.Mac;

public class SplashScreen extends Activity implements Runnable{

    public static DatabaseHelper dbHelper = null;
    WebView webView;
    SharedPreferences preferences = null;//  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
    public static String MacAddress = "";;//prefs.get(ConstantsAndUtilities.MACADDR,"");
    public static String printersByNetworkWifiAssignment = "{\"sravanibar_floor1\":{\"cashcounter_printer\":\"Floor1Counter\",\"kot_printer\":\"\",\"categorywise_printers\":{\"Food\":\"KOT\",\"Liquor\":\"Floor1Counter\"}},\"sravanibar_floor2\":{\"cashcounter_printer\":\"Floor3Counter\",\"kot_printer\":\"\",\"categorywise_printers\":{\"Food\":\"KOT\",\"Liquor\":\"Floor3Counter\"}},\"sravanibar_floor3\":{\"cashcounter_printer\":\"Floor3Counter\",\"kot_printer\":\"\",\"categorywise_printers\":{\"Food\":\"KOT\",\"Liquor\":\"Floor3Counter\"}},\"sravanibar_floor4\":{\"cashcounter_printer\":\"Floor3Counter\",\"kot_printer\":\"\",\"categorywise_printers\":{\"Food\":\"KOT\",\"Liquor\":\"Floor3Counter\"}}}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION

        }, PackageManager.PERMISSION_GRANTED);

        dbHelper = new DatabaseHelper(SplashScreen.this);
        preferences =  PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
        initControls();


        Thread thread = new Thread(this);
        thread.start();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FMS", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(ConstantsAndUtilities.firebaseToken, token);
                        editor.commit();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("FMS", msg);
                        Log.d("FMS", "TOken is "+ token);
//                        Toast.makeText(SplashScreen.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void initControls() {
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        progressBar = findViewById(R.id.pb);
        webView = findViewById(R.id.webview);

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);


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
        webView.loadUrl("file:///android_asset/config.html");

        MacAddress = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

    }

    @Override
    public void run() {

        dbHelper = new DatabaseHelper(SplashScreen.this);
        MainActivity.mySqlObj = new MySQLJDBC("simplposdata",preferences.getString(ConstantsAndUtilities.MYSQLUSERID,"root"),preferences.getString(ConstantsAndUtilities.MYSQLPASSWORD,""),preferences.getString(ConstantsAndUtilities.MYSQLPortNumber,"3306"),preferences.getString(ConstantsAndUtilities.HOSTAddress,""));
        MainActivity.mySqlCrmObj = new MySQLJDBC("simplposcrm",preferences.getString(ConstantsAndUtilities.MYSQLUSERID,"root"),preferences.getString(ConstantsAndUtilities.MYSQLPASSWORD,""),preferences.getString(ConstantsAndUtilities.MYSQLPortNumber,"3306"),preferences.getString(ConstantsAndUtilities.HOSTAddress,""));
        ArrayList<JSONObject> preferencesData = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbHelper.PREFERENCES_TABLE+" WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='companyid' OR "+dbHelper.PREFERENCES_ATTRIBUTE+"='server_url'");
        Log.v("Preferences",preferencesData.toString());
        ArrayList<JSONObject> adminList = new ArrayList<>();


    }


    public class WebAppInterface {
        Context mContext;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);

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
        public void tryAgainForCaptainOrdering()
        {
            finish();
            startActivity(getIntent());
            return;
        }
        @JavascriptInterface
        public void goToMainScreen()
        {
            try{

            }catch (Exception exp){
                exp.printStackTrace();
            }
            Intent intent;
            intent = new Intent(SplashScreen.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;

        }
        @JavascriptInterface
        public void goBackToMain(){

        }
        @JavascriptInterface
        public void rememberUser(String companyId,String userId,String password){

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("companyId", companyId);
            editor.putString("username", userId);
            editor.putString("password", password);
            editor.commit();

        }
        @JavascriptInterface
        public void forgetUser()
        {

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("companyId", "");
            editor.putString("username", "");
            editor.putString("password", "");
            editor.commit();

        }
        @JavascriptInterface
        public String getFirebaseToken()
        {
            return "12345";
        }
        @JavascriptInterface
        public String getCompanyId() {
            String compId = "";
            Log.v("Printing","Get company ID Called");
            compId = sharedpreferences.getString("companyId","demo");
            Log.v("Printing","Get company ID Called"+compId);
            return compId;
        }

        @JavascriptInterface
        public void gotoConfigPage(){
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
            Log.v("Config","Config page has to be retrieved");
        }

        @JavascriptInterface
        public void showAlertDialogJS(String titleMessage,String contentMessage)
        {
            DialogBox dbx = new DialogBox();
            boolean loadDialog = dbx.dialogBox(titleMessage,contentMessage, "OK","Cancel",mContext);
        }

        @JavascriptInterface
        public void savePrinterConfigDetails(String configobject){

        }

        @JavascriptInterface
        public String getUsername() {
            String compId = "";
            compId = sharedpreferences.getString("username","srinath");
            return compId;
        }
        @JavascriptInterface
        public String getPassword() {
            String compId = "";
            compId = sharedpreferences.getString("password","srinath");
            return compId;
        }


        @JavascriptInterface
        public boolean validateThePin(String pinNumber)
        {
            if(pinNumber.equals("1234")){
                return true;
            }
            else {
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
                String connectivityType = sharedpreferences.getString("usagePurpose","");
                Log.v("Srinath","Connectivity type is "+connectivityType);

                configJsonObj.put("usagePurpose",connectivityType);

                String connectivityAddress = sharedpreferences.getString("connectivityAddress","");
                configJsonObj.put("connectivityAddress",connectivityAddress);
                String startURL = sharedpreferences.getString("startURL","");
                configJsonObj.put("startURL",startURL);
                String printerModel = sharedpreferences.getString("printerModel","");
                configJsonObj.put("printerModel",printerModel);
                Log.v("Srinath","Config obj to string is "+configJsonObj.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (configJsonObj.toString());
        }
    }


    public String ssidName() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        String ssid = info.getSSID();
        ssid = ssid.replaceAll("^\"|\"$", "");
        return ssid;
    }
}