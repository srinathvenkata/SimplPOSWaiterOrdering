package com.simplpos.waiterordering.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.POSWebActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class ConstantsAndUtilities {
    public static final String REMEMBEREDUSERNAME = "posw_rememberedusername";
    public static final String REMEMBEREDPASSWORD = "posw_rememberedpassword";
    public static final String SCREENORIENTATION = "posw_screen_orientation";
    public static final String APACHE_PORT_NUMBER = "posw_apacheportnumber";
    public static String serverUrl="https://account.simplpos.com/";
    public static String HOSTAddress = "posw_mysqlhostaddress";
    public static boolean device_Token = true;
    public static Context printerContext;
    public static String firebaseToken = "posw_firebasetoken";
    private static char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static final String MYSQLUSERID = "posw_mysqluserid";
    public static final String MYSQLPASSWORD = "posw_mysqlpassword";
    public static final String MYSQLPortNumber="posw_mysqlportnumber";

    public static String loggedinusertype="";
    public static String SP_LOGGEDINUSERTYPE= "posw_loggedinusertype",SP_LOGGEDINUSERID="posw_loggedinuserid",SP_LOGGEDINUSERPASS="posw_useridPass";
    public static String usertype = "employee",userid="",useridPass="";
    public static  String currencyTypehtml = "$";


    public static String BARCODE_LENGTH = "barcode_length";
    public static String FEEDBACK = "feedback_url";
    public static String RP_TEXT = "reciept_print";
    public static String PS_TEXT = "pos_screen_local_language";
    public static String PP_TEXT = "print_preferences";
    public static String PL_TEXT = "print_localname";
    public static String SERVER_PRINT = "server_printing_fromsimplpos";
    public static String SERVER_PRINT_ONLY_FOR_INVOICE = "server_printing_only_for_invoice_fromsimplpos";
    public static String RAW_TEXT_PRINTING = "rawtext_printing_simplpos";
    public static String CATEGORY_PRINTING = "category_printing";
    public static String INVOICE_AUTO_PRINTING_WITHOUT_PROMPT = "invoice_autoprinting_without_prompt";
    public static String KOT_AUTO_PRINTING_WITHOUT_PROMPT = "kot_autoprinting_without_prompt";
    public static String QTY_PRINT_AFTER_NAME = "qty_print_after_itemname_fromsimplpos";
    public static String XAMPP_PRINT = "xampp_print";
    public static String KOT_XAMPP_PRINT = "kot_xampp_print";
    public static String PC_TEXT = "print_category";
    public static String PP_VALUE_TEXT = "print_pref_val";
    public static String CUSTOMER_SELECTION_MANDATORY = "customer_mandatory_simplpos";
    public static  String PROMOTIONS_AND_DEALS = "promotions_and_deals_simplpos";
    public static String AUTOLOCK_SCREEN_AFTER_ORDERS = "autolock_screen_after_orders";
    public static String DISPLAY_IMAGES_IN_POS = "display_images_in_POS";
    public static String PROMPT_REASON_FOR_CANCELLATION = "prompt_reason_for_cancellation_simplpos";
    public static String PARAMETERS_ATTRIBUTES_FOR_PRODUCT = "parameters_attributes_for_product_simplpos";
    public static String FOOD_PARTNERS_ENABLE = "enable_food_partners";
    public static  String invoiceSerialNumber="invoiceserialnumber";
    public static  String INVOICEIDPREFIX="invoiceIdPrefix";
    public static String FOODPARTNERS_PRIMARY_PRINT = "food_partners_primary_print";
    public static String SWIGGY_SELECTED_STORE = "swiggyselectedstore";
    public static String ZOMATO_SELECTED_STORE = "zomatoselectedstore";


    public static String ALLOWED_USER_IDS = "simplpos_allowed_user_ids_text";
    public static String LICENSE_KEY_VALID_UPTO = "licensekeyValidUpto";
    public static String LICENSE_KEY = "posw_licensekey";
    public static String ENCRYPTED_LICENSE_KEY = "posw_encrypted_licensekey";
    public static String DECRYPTED_LICENSE_KEY = "posw_decrypted_licensekey";
    public static String INV_SERIAL_NUMBER = "invoice_serial_number";
    public static String INV_SERIAL_PREFIX = "invoice_prefix";
    public static String PREFERRED_PRINTER = "preferred_printer";
    public static String PREFERRED_KOT_PRINTER = "preferred_kot_printer";

    public static String TOKEN_NUMBER_ENABLE = "token_enable";

    public static Boolean RP_VALUE = false;
    public static String FOOD_PARTNERS_KOT_INTERNAL = "false";

    public static Boolean PS_VALUE = false;
    public static Boolean TOKEN_GENERATION = false;
    public static long TOKEN_GENERATION_INTERVAL =  32400;

    public static String DUPLICATE_CUSTOMER_COPY_DINEIN="duplicate_customer_copy_dinein";
    public static String DUPLICATE_INVOICE_COPY_TAKEAWAY="duplicate_invoice_copy_takeaway";
    public static String store_id="";

    public static String generatedInvoiceId = "";
    public static String serialPortWeighingScale = "weighingscaleSerialPort";
    public static String tableSelectionButton  = "tableSelectionButton";
    public String TOKEN_NUMBER_RESET_MINUTES ="token_number_reset_time_in_minutes_simplpos";


    public static String PROMPT_INVOICE_PRINT = "prompt_invoice_print";
    public static String PROMPT_KOT_PRINT = "prompt_kot_print";
    public static String EXTERNAL_KEYBOARD_USAGE = "posw_EXTERNAL_KEYBOARD_USAGE";

    public static String randomValue() {

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;

    }

    public static String currentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static JSONObject convertContentValuesToJSON(ContentValues contentValueObject) {
            JSONObject finalJSONObject = new JSONObject();

            Set<Map.Entry<String, Object>> s=contentValueObject.valueSet();
            Iterator itr = s.iterator();

            Log.d("DatabaseSync", "ContentValue Length :: " +contentValueObject.size());

            while(itr.hasNext())
            {
                Map.Entry me = (Map.Entry)itr.next();
                String key = me.getKey().toString();
                Object value =  me.getValue();

//                Log.d("DatabaseSync", "Key:"+key+", values:"+(String)(value == null?null:value.toString()));
                try {
                    finalJSONObject.put(key,value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return  finalJSONObject;
    }

    public static String generateRandomNumber() {

        String inv_prefix = ("");
        String inv_serialnumber = ("");
        String localNextInvoiceId = nextInvoiceNumberIfLicensed();
        if(!localNextInvoiceId.equals("")){ return localNextInvoiceId; }

        ArrayList<JSONObject> prefixResults =  MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='invoiceprefix'");
        if(prefixResults.size()==1)
        {
            inv_prefix = prefixResults.get(0).optString("value");
        }

        ArrayList<JSONObject> serialNumResults =  MainActivity.mySqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='invoice_serial_number'");
        if(serialNumResults.size()==1)
        {
            inv_serialnumber = serialNumResults.get(0).optString("value");
        }

        try {
            if (inv_prefix.length() == 0 && inv_serialnumber.length() == 0) {
                Random r = new Random(System.currentTimeMillis());
                int trr = ((1 + r.nextInt(2)) * 100000000 + r.nextInt(100000000));
                return "" + trr;
            } else {
                int serial = Integer.parseInt(inv_serialnumber);
                serial = serial + 1;

                String number = String.format("%07d", serial);
                String randomNumber = inv_prefix + number;

                Log.i("inv_serialnumber", "inv_serialnumberFinal  : " + randomNumber);

                JSONObject updateObj = new JSONObject();
                updateObj.put("value",""+number);
                updateObj.put(DatabaseVariables.MODIFIED_DATE,currentTime());
                MainActivity.mySqlCrmObj.executeUpdate("store_preferences",updateObj,"attribute","invoice_serial_number");

                generatedInvoiceId = randomNumber;
                return randomNumber;
            }
        } catch (Exception e) {
            Log.i("inv_serialnumber", "inv_serialnumberError : " + e.getLocalizedMessage());
            Random r = new Random(System.currentTimeMillis());
            int trr = ((1 + r.nextInt(2)) * 100000000 + r.nextInt(100000000));
            generatedInvoiceId = "" + trr;
            return "" + trr;
        }
    }

    private static String kotPrinterNameIfLicensed() {
        String kotPrinterName = "";


        DatabaseVariables dbVar = new DatabaseVariables();
        String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
        if(!licenseKey.equals("")) {
            kotPrinterName = dbVar.getValueForAttribute(PREFERRED_KOT_PRINTER);
        }
        return kotPrinterName;
    }
    private static String nextInvoiceNumberIfLicensed() {
        DatabaseVariables dbVar = new DatabaseVariables();
        String prefix = dbVar.getValueForAttribute(INV_SERIAL_PREFIX);
        String inv_serialnumber = dbVar.getValueForAttribute(INV_SERIAL_NUMBER);
        if(prefix.equals(""))
        {
            return "";
        }
        if(inv_serialnumber.equals("")){
            inv_serialnumber = "0";
        }

        int serial = Integer.parseInt(inv_serialnumber);
        serial = serial + 1;

        String number = String.format("%07d", serial);
        String randomNumber = prefix + number;

        Log.i("inv_serialnumber", "inv_serialnumberFinal  : " + randomNumber);

        dbVar.replaceAttributesWithValues(INV_SERIAL_NUMBER,String.valueOf(serial));

        return randomNumber;
    }

    public static String generateTokenNumber()
    {
        if(TOKEN_GENERATION==false)
        {return "";}
        else{

            DatabaseVariables dbVar = new DatabaseVariables();
            String lastTokenNumber =  dbVar.getValueForAttribute("last_token");
            String lastInvoiceTime = dbVar.getValueForAttribute("last_invoice_time");
            String currentTimeVal = "";
            if (lastTokenNumber.length() == 0 && lastInvoiceTime.length() == 0) {
                lastTokenNumber = "0";
                currentTimeVal = lastInvoiceTime = currentTime();
            }
            else{
                long timeDiff = 0;
                currentTimeVal = currentTime();
                try {
                    timeDiff = calculateTimeDifference(lastInvoiceTime, currentTimeVal);
                    if(timeDiff > TOKEN_GENERATION_INTERVAL)
                    {
                        lastInvoiceTime = currentTime();
                        lastTokenNumber = "0";
                    }

                }catch(Exception exp)
                {

                }

            }
            int serial = Integer.parseInt(lastTokenNumber);
            serial = serial + 1;

            String number = String.format("%03d", serial);
            String randomNumber = number;


            dbVar.replaceAttributesWithValues("last_token", "" + number);
            dbVar.replaceAttributesWithValues("last_invoice_time", "" + currentTimeVal);

            return randomNumber;
        }
    }


    public static long calculateTimeDifference(String startDateDt,String endDateDt) throws ParseException {
        long diffInSeconds=0,diffInMinutes=0,diffInHours=0;
        // Set end date
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = (Date)formatter.parse(startDateDt);
        Date endDate = (Date)formatter.parse(endDateDt);
        long duration  = endDate.getTime() - startDate.getTime();

        diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        return diffInSeconds;
    }


    public static String currentTimeDayMonthFormat() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String primaryKOTName() {
        String printerName =   "";
        String printerRowId ="";

        printerName = kotPrinterNameIfLicensed();
        if(!printerName.equals("")){ return printerName; }

        String kotPrinterQuery = "SELECT value FROM store_preferences WHERE attribute='kot_printer_rowid'";
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
//        Preferences prefs = Preferences.userRoot();
        ArrayList<JSONObject> kotPrinterDetails = sqlCrmObj.executeRawqueryJSON(kotPrinterQuery);
        if(kotPrinterDetails.size()==1)
        {
            JSONObject storePreferencesDetails = kotPrinterDetails.get(0);
            printerRowId = (String) storePreferencesDetails.optString("value");

        }else{
            return printerName;
        }
        String query = "";

            query = "SELECT * FROM printers RIGHT JOIN store_preferences ON store_preferences.value = printers._id WHERE store_preferences.attribute='kot_printer_rowid'";

            ArrayList<JSONObject> primaryPrinter = sqlCrmObj.executeRawqueryJSON(query);

        if(primaryPrinter.size()==1)
        {
            printerName = (String) primaryPrinter.get(0).optString("printer_name");
        }
        //"POS-80C";
        return  printerName;
    }

    public String incrementKotSerialNo() {
        String print_text = "&nbsp;";
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        try {
//            "server_kot_prefix"";
//                "server_kot_number";
//            "incrementKOTNum";
            String prefix = "";
            ArrayList<JSONObject> prefixDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='server_kot_prefix'");
            if(prefixDetails.size()==0){
                return  print_text;
            }
            else {
                prefix = (String) prefixDetails.get(0).get("value");
                Integer kotNumber = 1;
                ArrayList<JSONObject> kotNum = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='server_kot_number'");
                String newKotValue = "";
                if(kotNum.size()==0)
                {
                    newKotValue = "1";
                    JSONObject contentValues  = new JSONObject();
                    contentValues.put("attribute","server_kot_number");
                    contentValues.put("value","2");
                    contentValues.put("unique_id",randomValue());
                    contentValues.put("server_local","local");
                    contentValues.put("created_timestamp",currentTime());
                    contentValues.put("modified_timestamp",currentTime());
                    sqlCrmObj.executeInsert("store_preferences",contentValues);
                }else{
                    kotNumber = Integer.parseInt((String) kotNum.get(0).get("value"));
                    String uniqId = (String) kotNum.get(0).get("unique_id");
                    newKotValue = String.valueOf(kotNumber);
                    String updateKOTValue = String.valueOf(kotNumber+1);
                    JSONObject contentValues = kotNum.get(0);

                    contentValues.put("modified_timestamp",currentTime());
                    contentValues.put("value",updateKOTValue);
                    sqlCrmObj.executeUpdate("store_preferences",contentValues,"unique_id",uniqId);
                }
                print_text = prefix + newKotValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return print_text;
    }

    public ContentValues convertJSONObjectToContentValues(JSONObject contentItemValues) {

        ContentValues tableRecordContentValues = new ContentValues();

        try {
            Iterator<String> keys = contentItemValues.keys();


            while(keys.hasNext()) {
                String key = keys.next();
                if (contentItemValues.get(key) instanceof String) {
                    // do something with jsonObject here

                    tableRecordContentValues.put(key, String.valueOf(contentItemValues.get(key)));
                }
            }
        }catch (Exception exp){
            Log.v("Error",exp.getMessage());
            exp.printStackTrace();
        }
        finally {
            return tableRecordContentValues;
        }
    }

    public String md5Encryption(String input)
    {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String random9digits() {
        Random r = new Random(System.currentTimeMillis());
        int trr = ((1 + r.nextInt(9)) * 100000000 + r.nextInt(100000000));
        return "" + trr;
    }


    public static Double getDoubleValueof(String str) {
        Double value = 0.0;
        try{
            value = Double.valueOf(str);
        }catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }

    public String urlEncode(String url) {

        try {
            String encodeURL= URLEncoder.encode( url, "UTF-8" );
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" +e.getMessage();
        }

    }

    public static String tokenNumber()
    {
        if(TOKEN_GENERATION==false)
        {return "";}
        else{
            MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
            Preferences prefs = Preferences.userRoot();
            String print_text = prefs.get(
                    "KOT_Serial", "");
            String KOTPrefix = "";
            ArrayList<JSONObject> prefixDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM store_preferences WHERE attribute='server_kot_prefix'");
            if(prefixDetails.size()==0){
                KOTPrefix = "";
            }
            else {
                KOTPrefix = (String) prefixDetails.get(0).optString("value");

            }
            if (!print_text.isEmpty() && print_text.contains(" ")) {
                String[] no = print_text.split(" ");
                int number = Integer.parseInt(no[1]);
                number++;
                print_text = no[0] + " " + number;
                KOTPrefix = no[0];
            }
            String lastTokenNumber = prefs.get("last_token", "");

            return (KOTPrefix+lastTokenNumber);
        }
    }


        public String primaryPrinterName() {
            DatabaseVariables dbVar = new DatabaseVariables();

            String printerName = "";
            String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
            printerName = counterPrinterForSSID(POSWebActivity.assignmentOfPrinters, POSWebActivity.ssid);

            if(!licenseKey.equals(""))
            {
                if (!printerName.equals(""))
                {
                    return printerName;
                }

                printerName = dbVar.getValueForAttribute(ConstantsAndUtilities.PREFERRED_PRINTER);
                if(!printerName.equals(""))
                {
                    return printerName;
                }
            }
            MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
            ArrayList<JSONObject> primaryPrinter = sqlCrmObj.executeRawqueryJSON("SELECT * FROM printers WHERE is_primary_printer='Yes' ORDER BY _id DESC LIMIT 1");
            if(primaryPrinter.size()==1)
            {
                printerName = (String) primaryPrinter.get(0).optString("printer_name");
            }
            return  printerName;//""POS-80C";
        }


    public static String leftPadding(String str, int num) {
        return String.format("%1$" + num + "s", str);
    }
    public static String rightPadding(String str, int num) {
        return String.format("%1$-" + num + "s", str);
    }


    public static String addSingleQuotes(String s) {
        s = s.replace("'","''");
        return s;

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


    public String counterPrinterForSSID(JSONObject printersByNetworkWifiAssignment,String ssid)
    {
        String printerName = "";
        try {
            if (printersByNetworkWifiAssignment.has(ssid)) {
                JSONObject printersForWifi = printersByNetworkWifiAssignment.optJSONObject(ssid);
                if (printersForWifi.has("cashcounter_printer")) {
                    return printersForWifi.optString("cashcounter_printer");
                }
            }
        }catch (Exception exp){
            exp.printStackTrace();
        }
        return printerName;
    }


    public String categoryAssignedPrinterForSSID(JSONObject printersByNetworkWifiAssignment,String ssid,String categoryId)
    {
        Log.v("Printing","categoryAssignedPrinterForSSID called for "+categoryId);
        String printerName = "";
        try {
            if (printersByNetworkWifiAssignment.has(ssid)) {
                JSONObject printersForWifi = printersByNetworkWifiAssignment.optJSONObject(ssid);
                if (printersForWifi.has("categorywise_printers")) {
                    JSONObject categoryWisePrinters = printersForWifi.optJSONObject("categorywise_printers");
                    if (categoryWisePrinters.has(categoryId)) {
                        return categoryWisePrinters.optString(categoryId);
                    }
                }
            }
        }catch (Exception exp){
            exp.printStackTrace();
        }
        return printerName;
    }

    public JSONObject StringToJSONObj(String originalString) {
        JSONObject returnObj = new JSONObject();
        try {
            returnObj = new JSONObject(originalString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnObj;
    }
    public static String addChar(String str, char ch, int position) {
        return str.substring(0, position) + ch + str.substring(position);
    }
}
