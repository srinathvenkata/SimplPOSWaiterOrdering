package com.simplpos.waiterordering.helpers;

import static com.simplpos.waiterordering.helpers.SaveFetchVoidInvoice.dbVar;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.POSWebActivity;
import com.simplpos.waiterordering.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;

public class POSWebPrinting {


    private static DecimalFormat df2 = new DecimalFormat("#.##");
    public JSONObject printInfoObj = new JSONObject();
    private static String printInvoiceOrKOT = "KOT";


    public static void clearAllTaxContentRawText(){
        cumilativeTaxComponent.clear();
        mSubTotal = 0;
        ItemsPriceExcludingTax = 0;
        ItemswiseTotalTax = 0;
        //Also set total values like totalPriceWithoutTax to 0d
    }
    public static Map<String, Map<String, Double>>  getAllInclusiveTaxesCumilativeComponent()
    {
        return cumilativeTaxComponent;
    }
    private static void clearAllTaxContent() {
        cumilativeTaxComponent.clear();
        mSubTotal = 0;
        ItemsPriceExcludingTax = 0;
        ItemswiseTotalTax = 0;
        //Also set total values like totalPriceWithoutTax to 0d
    }

    static private Map<String, Map<String, Double>> cumilativeTaxComponent = new LinkedHashMap<String, Map<String, Double>>();
    static double ItemsPriceExcludingTax = 0;
    static double ItemswiseTotalTax = 0;
    static double mSubTotal = 0;
    public static String itemwiseTaxCalc(String taxStr, Double itemTotalPrice) {
        Double overallDiscGiven = 0.0d;
        mSubTotal += itemTotalPrice;

        String returnStr = "";
        Double itemTPrice = new Double(itemTotalPrice);
        itemTPrice = itemTotalPrice - overallDiscGiven;
        Double totalTaxPercentOnItem = 0d;
        Double totalPriceWithoutTax = new Double(itemTotalPrice);
        Double totalTaxValueOnItem = 0d;
        String[] taxsArray = taxStr.split(",");
        Map<String, String> itemTaxMap = new LinkedHashMap<String, String>();
        ;
        // Shipping Tax@10.75,VAT@3.2,VAT 3@6.92
        for (String taxItem : taxsArray) {
            if (taxItem != null && taxItem.indexOf("@") > 0) {
                String[] singleTaxArray = taxItem.split("@");
                if (singleTaxArray.length == 2) {
                    String taxType = singleTaxArray[0].trim(), strTaxValue = singleTaxArray[1].trim();
                    itemTaxMap.put(taxType, strTaxValue);
                    Double taxValue = (Double.parseDouble(strTaxValue));
                    totalTaxPercentOnItem = totalTaxPercentOnItem + taxValue;
                }
            }
        }

        if (totalTaxPercentOnItem != 0) {
            returnStr = "Incl Total GST " + (totalTaxPercentOnItem) + "%";

            totalPriceWithoutTax = itemTPrice / (1 + (totalTaxPercentOnItem / 100));
            ItemsPriceExcludingTax = ItemsPriceExcludingTax + totalPriceWithoutTax;
            totalTaxValueOnItem = itemTPrice - totalPriceWithoutTax;
            ItemswiseTotalTax = ItemswiseTotalTax + totalTaxValueOnItem;

            if (itemTaxMap.size() > 0) {
                Iterator<Map.Entry<String, String>> entryIterator = itemTaxMap.entrySet().iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, String> entry = entryIterator.next();
                    String strTaxType = entry.getKey();
                    Map<String, Double> taxType = cumilativeTaxComponent.get(strTaxType);
                    String stCurrentTaxValue = entry.getValue();
                    Double currentAmountTaxValue = (totalPriceWithoutTax / 100) * (Double.parseDouble(stCurrentTaxValue));
                    if (taxType == null) {
                        taxType = new LinkedHashMap<String, Double>();
                        taxType.put(stCurrentTaxValue, currentAmountTaxValue);
                        cumilativeTaxComponent.put(strTaxType, taxType);
                    } else {
                        Double existingTaxAmount = taxType.get(stCurrentTaxValue);
                        if (existingTaxAmount == null) {
                            taxType.put(stCurrentTaxValue, currentAmountTaxValue);
                        } else {
                            taxType.put(stCurrentTaxValue, existingTaxAmount + currentAmountTaxValue);
                        }
                        cumilativeTaxComponent.put(strTaxType, taxType);
                    }
                }
            }


        }
        else{
            try {
                returnStr = "Incl Total GST " + (totalTaxPercentOnItem) + "%";
                totalPriceWithoutTax = itemTPrice;
                ItemsPriceExcludingTax = ItemsPriceExcludingTax + totalPriceWithoutTax;
                totalTaxValueOnItem = itemTPrice - totalPriceWithoutTax;
                ItemswiseTotalTax = ItemswiseTotalTax + totalTaxValueOnItem;

                if (itemTaxMap.size() > 0) {
                    Iterator<Map.Entry<String, String>> entryIterator = itemTaxMap.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry<String, String> entry = entryIterator.next();
                        String strTaxType = entry.getKey();
                        Map<String, Double> taxType = cumilativeTaxComponent.get(strTaxType);
                        String stCurrentTaxValue = entry.getValue();
                        Double currentAmountTaxValue = (totalPriceWithoutTax / 100) * (Double.parseDouble(stCurrentTaxValue));
                        if (taxType == null) {
                            taxType = new LinkedHashMap<String, Double>();
                            taxType.put(stCurrentTaxValue, currentAmountTaxValue);
                            cumilativeTaxComponent.put(strTaxType, taxType);
                        } else {
                            Double existingTaxAmount = taxType.get(stCurrentTaxValue);
                            if (existingTaxAmount == null) {
                                taxType.put(stCurrentTaxValue, currentAmountTaxValue);
                            } else {
                                taxType.put(stCurrentTaxValue, existingTaxAmount + currentAmountTaxValue);
                            }
                            cumilativeTaxComponent.put(strTaxType, taxType);
                        }
                    }
                }
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
        return returnStr;

    }

    public void voidInvoiceCancelledBillPrinting(String voidInvoiceId,JSONObject contentObj) {
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);
            if(cancellationStatus.contains(" - Reason for"))
            {
                cancellationStatus = (cancellationStatus.split(" - Reason for"))[0];
            }
            String htmlString = "";
            String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                    "</head>";
            String bodyContent = "<body>";
            bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
            bodyContent += "<tr><td colspan=\"2\"><h2>Cancelled Invoice #"+voidInvoiceId+" "+orderRefNum+"</h2></td></tr>";
            bodyContent += "<tr><td>Employee</td><td>"+ dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID) +"</td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan='1' style=\"font-size:22px\">Cancellation Status :  </td><td>"+cancellationStatus+"</td></tr>";
            ItemCancellationReasons icr = new ItemCancellationReasons();
            String reasonForCancellation = icr.cancellationReasonForVoidInvoice(voidInvoiceId);
            bodyContent += "<tr valign=\"top\"><td align=\"left\"  colspan='1' style=\"font-size:22px\">Reason For Cancellation :  </td><td>"+reasonForCancellation+"<br /><br /><br /><br /></td></tr>";
            bodyContent += "<tr><td align=\"center\"  colspan=\"2\" style=\"font-size:22px\">***** </td></tr>";
            bodyContent += "</table>";
            htmlString += "<html>"+headTagContent+bodyContent+"</html>";
            String primaryPrinter = Parameters.primaryPrinterName();
//            printHtmlToPrinter(htmlString,primaryPrinter,false);
            printInvoiceOrKOT = "Invoice";
            saveToPrintingQueue("",htmlString,voidInvoiceId,contentObj,"general",primaryPrinter);
            printInvoiceOrKOT = "KOT";
        }
    }


    public static void shiftTablePrint(String oldOrderRefNum,String fromInvoice, String toTable, String newOrderRefNum, String storeId,final String mergingOrShifting) {
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        DatabaseVariables dbVar = new DatabaseVariables();
        String htmlString = "";
        ArrayList<JSONObject> invoiceDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + fromInvoice + "' ");
        if(invoiceDetails.size()==1){
            String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                    "</head>";
            String bodyContent = "<body>";
            bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
            bodyContent += "<tr><td colspan=\"2\"><h2>Table "+mergingOrShifting+"</h2></td></tr>";
            bodyContent += "<tr><td colspan=\"2\">Invoice ID "+fromInvoice+" from "+oldOrderRefNum+" to  "+newOrderRefNum+" </td></tr>";
            bodyContent += "<tr><td>Employee</td><td>"+ Parameters.userid +"</td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr>";
            bodyContent += "</table>";
            htmlString += "<html>"+headTagContent+bodyContent+"</html>";
            ConstantsAndUtilities cv = new ConstantsAndUtilities();
            saveToPrintingQueue("",htmlString,fromInvoice,new JSONObject(),"general",cv.primaryPrinterName());

        }

    }

    public static boolean freshItemCheck(JSONObject printInfoObj) {
        Boolean checkResult = false;

        JSONObject obj =  printInfoObj.optJSONObject("orderdetails");
        Log.v("Printing","Item IDs are "+obj.toString());
        JSONArray itemIds = obj.optJSONArray("itemIds");
        JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
        JSONArray itemNames = obj.optJSONArray("itemNames");
        JSONArray itemNotes = obj.optJSONArray("itemNotes");
        JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
        JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
        JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
        JSONArray savedQuantities = obj.optJSONArray("savedQuantities");
        String holdInvoiceId = obj.optString("holdInvoiceId");
        try {
            for(int p=0; p < itemIds.length(); p++)
            {
                String itemId = itemIds.getString(p);
                if(Double.parseDouble(savedQuantities.getString(p)) != Double.parseDouble(itemQuantitys.getString(p))){  return true;}
                Log.v("Printing","Item id is "+itemId+" - item name is "+itemNames.getString(p)+" old quantity is "+savedQuantities.getString(p)+" new quantity is "+itemQuantitys.getString(p));
            }
        }catch (Exception exp){
            Log.v("Printing","Error is "+exp.toString());
        }
        return  checkResult;
    }

    public static Boolean printHistoryCheck(String printableInvoiceId, String finalbill) {
        return false;
    }

    public String htmlContentForItemsList(String printInvoiceId, JSONObject contentObj) {
        JSONObject sendContentObjtoTracking = contentObj;
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat df3 = new DecimalFormat("#.###");
        ItemCancellationReasons icr = new ItemCancellationReasons();
        String printString="";
        String itemsKot = "";
        String heightFunction = "function getHeight(){ var x = document.getElementsByTagName(\"body\")[0].offsetHeight;  AndroidInterface.showToast(\"Toast Check\" + x); } " +
                " ";
        String tokenNoStr = (obtainTokenNumber().equals("")) ? "" : "<tr><td colspan=\"4\"><h4 style=\"text-align:center; font-size:20px;\">"+ (obtainTokenNumber())+"</h4></td></tr>";
        String headTagContent = " <head><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;} tr.kotItemRow td:first-child{ width:70px; }</style><meta charset=\"UTF-8\" /> <meta http-equiv=\"Content-type\" content=\"text/html; charset=UTF-8\" /></head>";
        heightFunction += "function returnHeight(){var x = document.getElementsByTagName(\"body\")[0].offsetHeight;  AndroidInterface.submitHeight(x);}";
        String numbersDisplay = "", itemPrinter ="";


        Boolean itemsListPrint  = true;
        String headlines = "KOT", footerEmpty=""; int iPaperWidth=3;
        if(itemsListPrint==true) {
            itemPrinter = ConstantsAndUtilities.primaryKOTName();
        }
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        if(!itemPrinter.equals("")){
            String selectQuery2 = "SELECT  * FROM printers where printer_name='"+itemPrinter+"'";
            ArrayList<JSONObject> printer1Details = sqlCrmObj.executeRawqueryJSON(selectQuery2);

            if (printer1Details!=null && printer1Details.size() > 0) {


                for(int cntPrnt1=0; cntPrnt1<printer1Details.size();cntPrnt1++)
                {
                    JSONObject currentRowPrnt1 = printer1Details.get(cntPrnt1);
                    headlines = (String) currentRowPrnt1.optString(DatabaseVariables.PRINTER_TEXT);
                    footerEmpty = (String) currentRowPrnt1.optString(DatabaseVariables.FOOTER_TEXT_RECEIPT);
                    // iPaperWidth = Integer.parseInt((String) currentRowPrnt1.get(dbVar.PAPER_WIDTH));
                }

            }
        }
        ConstantsAndUtilities cv = new ConstantsAndUtilities();
        String kotNum = cv.incrementKotSerialNo();
        String generatedkotNum = kotNum;
        if(!kotNum.equals("")){ kotNum = "KOT "+kotNum; }
        String neworrepeat = (String) contentObj.optString("neworrepeat");
        kotNum = neworrepeat + " "+kotNum;

        String orderRefNum = "";
        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM invoice_total_table WHERE invoice_id='"+printInvoiceId+"'");
        String storeName="",storeAddress="",storePhoneNum="",orderTypeString="";
        if(invoiceDetails.size()==1){
            orderRefNum = (String) invoiceDetails.get(0).optString(DatabaseVariables.INVOICE_HOLD_ID);
            if(!orderRefNum.equals("")){ orderRefNum = "<tr><td align=\"center\" colspan=\"4\" id=\"kotOrderRefNum\" style=\"font-size:22px\">ORDER REF. NO# "+ orderRefNum+" </td></tr>";}
            String storeIdForInvoice = (String) invoiceDetails.get(0).optString(DatabaseVariables.STORE_ID);
            if(!storeIdForInvoice.equals("")){
                ArrayList<JSONObject> storeDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ DatabaseVariables.STORE_TABLE + " WHERE store_id='" + storeIdForInvoice + "'");
                if(storeDetails.size()==1){
                    storeName = (String) storeDetails.get(0).optString(DatabaseVariables.STORE_NAME);
                    storeAddress = storeDetails.get(0).optString(DatabaseVariables.STORE_STREET)+",\n"+storeDetails.get(0).optString(DatabaseVariables.STORE_CITY)+"-"+storeDetails.get(0).optString(DatabaseVariables.STORE_POSTAL);
                    storePhoneNum = (!storeDetails.get(0).optString(DatabaseVariables.STORE_NUMBER).equals("")) ? ("<tr><td align=\"center\" colspan=\"4\" style=\"font-size:22px\">Phone No# "+ storeDetails.get(0).optString(DatabaseVariables.STORE_NUMBER) +" </td></tr>") : "";
                }
            }

            orderTypeString = orderTypeName((String) invoiceDetails.get(0).optString(DatabaseVariables.ORDER_TYPE));
        }
        ArrayList<JSONObject> invoiceItemDetails = sqlObj.executeRawqueryJSON("SELECT * FROM invoice_items_table WHERE invoice_id='"+printInvoiceId+"'");
        ArrayList<JSONObject> invoiceItemCountDetails = sqlObj.executeRawqueryJSON("SELECT SUM(item_quantity) as totalQuantity FROM invoice_items_table WHERE invoice_id='"+printInvoiceId+"'");

        String totalQuantityOfItems = (String) invoiceItemCountDetails.get(0).optString("totalQuantity");
        String kotString = "";
        if(!kotNum.equals("")){
            kotString = "<br /><span class=\"tablehead\">"+kotNum+"</span>";

        }
        String billContent = "<tr> <th align=\"center\" colspan=\"4\"><span class=\"tablehead\">"+headlines+"</span>"+kotString+"</th></tr>"+tokenNoStr+"<tr><td align=\"left\" colspan=\"4\" style=\"text-align:center; font-size:22px; font-weight:bold;\">INVOICE# "+printInvoiceId+"</td></tr>"+orderRefNum+"";
        billContent += "<tr><tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr><tr ><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Employee: "+(invoiceDetails.get(0).optString(DatabaseVariables.INVOICE_EMPLOYEE))+" </td><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Order Type: "+orderTypeString+" </td></tr><tr><td colspan=\"4\" style=\"border-top:dotted 2px solid;\">&nbsp;</td> <!--  for blank row --></tr>";
        String currencyHtmlStr = MainActivity.currencyTypehtml;
        String itemsListStr ="" , cancelledItemsKOTString = "";
        Double subTotal = 0.0d;
        try {

            JSONObject obj = (JSONObject) contentObj.get("orderdetails");
            JSONArray itemIds = (JSONArray) obj.get("itemIds");
            JSONArray discountInCurrency = (JSONArray) obj.get("discountInCurrency");
            JSONArray itemNames = (JSONArray) obj.get("itemNames");
            JSONArray itemNotes = (JSONArray) obj.get("itemNotes");
            JSONArray savedOlderUniqueIds = (JSONArray) obj.get("savedOlderUniqueIds");
            JSONArray totalItemPrices = (JSONArray) obj.get("totalItemPrices");
            JSONArray itemQuantitys = (JSONArray) obj.get("itemQuantitys");
            JSONArray savedQuantities = (JSONArray) obj.get("savedQuantities");
            String holdInvoiceId = (String) obj.get("holdInvoiceId");
            itemsListStr = "<tr><td colspan=\"4\"><table>";
            for (int p = 0; p < itemIds.length(); p++) {
                String itemId = (String) itemIds.get(p);
                String savedQty = "0";
                if(     (savedQuantities.get(p) != null) && (!savedQuantities.get(p).equals(""))   )
                {
                    savedQty = (String) savedQuantities.get(p);
                }

                if (Double.parseDouble(savedQty) != Double.parseDouble((String) itemQuantitys.get(p))) {
                    Double newQty = Double.parseDouble((String) itemQuantitys.get(p)) - Double.parseDouble(savedQty);

                    itemsKot = "";
                    String itemName = (String) itemNames.get(p);
                    String itemNote = (!itemNotes.get(p).equals("")) ? (" - " + itemNotes.get(p)) : "";
                    if (Double.parseDouble(savedQty) != 0) {
                        itemNote = " - Old Qty " + df.format(Double.parseDouble(savedQty)) + itemNote;
                    }

                    String cancelledString = "";
                    if(newQty < 0)
                    {
                        itemsKot += itemName;
                        itemsKot += itemNote;

                        Double newQtyCheck = Math.abs(newQty);

                        String itemRowUniqueId = String.valueOf(savedOlderUniqueIds.get(p));
                        String reasonForCancellationOfKOT = icr.cancellationReasonForItemWithUniqueId(itemRowUniqueId);
                        itemsKot += reasonForCancellationOfKOT;


                        cancelledItemsKOTString += "<tr class=\"kotItemRow\"><td>"+ df3.format(newQtyCheck) +"</td><td colspan=\"3\">"+itemsKot+"</td></tr>";
//                        itemNote += ( " - <span class=\"cancelledItemSpan\"><b> Cancelled "+ df.format(Double.parseDouble(savedQty)+"</b></span>");
                    }
                    itemName = itemName.substring(0, Math.min(itemName.length(), 33));
                    itemsKot += itemName;
                    itemsKot += itemNote;

                    if(newQty > 0 ) { itemsListStr += "<tr class=\"kotItemRow\"><td>"+ df3.format(newQty) + cancelledString +"</td><td colspan=\"3\">"+itemsKot+"</td></tr>"; }
                }

            }

            if(!cancelledItemsKOTString.equals(""))
            {
                itemsListStr += "<tr><td colspan=\"4\">&nbsp;<br /><td></tr><tr><td style=\"text-decoration : underline; text-align : center;\" colspan=\"4\"><h3>Cancelled Items</h3></td></tr>" + cancelledItemsKOTString +"<tr><td colspan=\"4\">&nbsp;<br /><td></tr>";
                cancelledItemsKOTString = "";
            }
            itemsListStr += "</table></td></tr>";
        } catch (Exception exp) {
            exp.printStackTrace();
        }



        billContent = billContent + itemsListStr;
        printString = "<html>" + headTagContent + "<body id=\"kotBody\"><table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">"+billContent+"<tr><td colspan=\"4\">"+footerEmpty+"</td></tr></table><script language=\"javascript\">" + heightFunction + " window.onload = function() { returnHeight(); };</script></body></html>";

        kotTrackingSave(generatedkotNum,printString,printInvoiceId, contentObj,"general","","Order Placed");
        saveToPrintingQueue(generatedkotNum,printString,printInvoiceId,contentObj,"general",itemPrinter);

        return printString;
    }


    private void kotTrackingSave(String generatedkotNum, String printString, String printInvoiceId, JSONObject contentObj, String generalOrCategory,String categoryName, String kotType) {
        //                 Date previousSyncDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(previousSyncTime);
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date oneweekbackDate  = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String mysqlDateString = formatter.format(oneweekbackDate);
//        sqlCrmObj.executeDelete("kot_tracking","")
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseHelper dbHelper = SplashScreen.dbHelper;
        sqlCrmObj.executeRawquery("DELETE FROM kot_tracking WHERE created_on <='"+mysqlDateString+"'");

        if(!freshItemCheck(contentObj) && !kotType.equals("Cancelled"))
        {
//            System.out.println("We have no new items");
            return;
        }
        JSONObject obj = null;
        try {
            obj = (JSONObject) contentObj.get("orderdetails");


        DecimalFormat df = new DecimalFormat("#.###");
        JSONArray itemIds = (JSONArray) obj.get("itemIds");

        JSONArray discountInCurrency = (JSONArray) obj.get("discountInCurrency");
        JSONArray itemNames = (JSONArray) obj.get("itemNames");
        JSONArray itemNotes = (JSONArray) obj.get("itemNotes");
        JSONArray savedOlderUniqueIds = (JSONArray) obj.get("savedOlderUniqueIds");
        JSONArray totalItemPrices = (JSONArray) obj.get("totalItemPrices");
        JSONArray itemQuantitys = (JSONArray) obj.get("itemQuantitys");
        JSONArray savedQuantities = (JSONArray) obj.get("savedQuantities");
        String holdInvoiceId = (String) obj.get("holdInvoiceId");
        JSONArray itemCategoryIds = (JSONArray) obj.get("categoryIds");

        String randomKotId = ConstantsAndUtilities.randomValue();

        // save the kot_tracking
        ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
        try{
            JSONObject contentValues = new JSONObject();
            contentValues.put("invoice_id", printInvoiceId);
            contentValues.put("kot_id",randomKotId);
            contentValues.put("kot_no",generatedkotNum);
            contentValues.put("device_os","Android");
            contentValues.put("devicetoken",dbVar.getValueForAttribute("device_token"));
            contentValues.put("kot_type",kotType);
            contentValues.put("kot_content",printString);
            if(generalOrCategory.equals("general")) {
                contentValues.put("category_or_general", "General");
            }else{
                contentValues.put("category_or_general", "Category");
            }
            contentValues.put("created_by",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
            contentValues.put("updated_by",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
            contentValues.put("update_user_type","Employee");
            contentValues.put("category_id",categoryName);
            contentValues.put("created_on",ConstantsAndUtilities.currentTime());
            contentValues.put("updated_on",ConstantsAndUtilities.currentTime());

            insertingValues.add(0,contentValues);
            sqlCrmObj.executeInsertBatch("kot_tracking",insertingValues);
            insertingValues = new ArrayList<JSONObject>();
            for(int p=0; p < itemIds.length(); p++)
            {
                if(!itemCategoryIds.get(p).equals(categoryName) && !generalOrCategory.equals("general")){
                    continue;
                }
                contentValues = new JSONObject();
                String itemId = (String) itemIds.get(p);
                String savedQty = "0";
                if(     (savedQuantities.get(p) != null) && (!savedQuantities.get(p).equals(""))   )
                {
                    savedQty = (String) savedQuantities.get(p);
                }

                if (Double.parseDouble(savedQty) != Double.parseDouble((String) itemQuantitys.get(p))) {
                    Double newQty = Double.parseDouble((String) itemQuantitys.get(p)) - Double.parseDouble(savedQty);

                    String itemName = (String) itemNames.get(p);
                    String itemNote = (!itemNotes.get(p).equals("")) ? (" - " + itemNotes.get(p)) : "";
                    if (Double.parseDouble(savedQty) != 0) {
                        itemNote = "Old Qty " + df.format(Double.parseDouble(savedQty)) + itemNote;
                    }
                    itemName = itemName.substring(0, Math.min(itemName.length(), 33));
                    int sizeOfNewItems = insertingValues.size();
                    contentValues.put("kot_id",randomKotId);
                    contentValues.put("item_name",itemName);
                    contentValues.put("item_id",itemId);
                    contentValues.put("item_quantity", df2.format(newQty));
                    contentValues.put("notes",itemNote);
                    contentValues.put("updated_by","");
                    contentValues.put("created_on",ConstantsAndUtilities.currentTime());
                    contentValues.put("updated_on",ConstantsAndUtilities.currentTime());

                    insertingValues.add(sizeOfNewItems,contentValues);
                }

            }
            if(insertingValues.size()>0)
            {
                sqlCrmObj.executeInsertBatch("kot_tracking_items",insertingValues);
            }
//            serviceCallForPushNotification(randomKotId);
            }catch (Exception exp){
                exp.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void saveToPrintingQueue(String generatedkotNum, String printString, String printInvoiceId, JSONObject contentObj, String generalOrCategorywise,String printerId) {
        JSONObject contentValues = new JSONObject();
        try {
            contentValues.put("priority","Immediate");
            String timeC = ConstantsAndUtilities.currentTime();
            contentValues.put(DatabaseVariables.CREATED_DATE, timeC);
            contentValues.put(DatabaseVariables.MODIFIED_DATE, timeC);
            contentValues.put(DatabaseVariables.UNIQUE_ID, ConstantsAndUtilities.randomValue());
            contentValues.put("status","Pending");
            contentValues.put("invoice_or_kot",printInvoiceOrKOT);
            contentValues.put("print_after_time",timeC);
            contentValues.put("print_html_content",printString);
            contentValues.put("printer_name_for_printing",printerId);
            contentValues.put("kot_row_id",generatedkotNum);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MainActivity.mySqlCrmObj.executeInsert("printing_queue",contentValues);
    }

    public static String orderTypeName(String orderTypeInput) {
        String storeSaleText = "Store Sale";

        if(orderTypeInput.equals("store sale")) {
            storeSaleText = "Dine In";
        }

        else if(orderTypeInput.equals("take away")) {
            storeSaleText = "Parcel";
        }
        else if(orderTypeInput.equals("home delivery")) {
            storeSaleText = "Home Delivery";
        }
        return  storeSaleText;
    }


    public static String obtainTokenNumber(){
        String tokenNo = "";
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        if (!Parameters.tokenNumber().equals("") && Parameters.TOKEN_GENERATION==true) {
            tokenNo = "TOKEN # " + " - " + Parameters.tokenNumber();
        }
        return tokenNo;
    }
    public static void postDataToServerForPrinting(String printInvoiceId, JSONObject printInfoObj, String printingType) {
        try{
            DatabaseVariables dbVar = new DatabaseVariables();
            ArrayList<JSONObject> InvoiceDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
            ArrayList<JSONObject> InvoiceItemDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
            ArrayList<JSONObject> InvoiceTaxDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
            ArrayList<JSONObject> InvoiceCategoryTaxDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
            ArrayList<JSONObject> splitInvoiceDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");

            printInfoObj.put("printingType",printingType);
            printInfoObj.put("loggedInEmployee",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
            printInfoObj.put("invoiceDetails",InvoiceDetails);
            printInfoObj.put("invoiceItemDetails",InvoiceItemDetails);
            printInfoObj.put("invoiceTaxDetails",InvoiceTaxDetails);
            printInfoObj.put("invoiceCategoryDetails",InvoiceCategoryTaxDetails);
            printInfoObj.put("splitInvoiceDetails",splitInvoiceDetails);
            Log.v("PrintFromServer",InvoiceDetails.toString());
        }catch (JSONException exp){}
        printFromServer(printInfoObj,printingType);
//        showReprintDialog(printInfoObj);
    }

    public static String postDataToServer(JSONObject obj) throws JSONException{

        URL url;
        String response = "";
        Log.v("PrintFromServer","postDataToServer obj is "+obj.toString());
        try {
            String portNumberStr = "";
            if(!POSWebActivity.ApachePortNumber.equals(""))
            {
                portNumberStr += ":"+POSWebActivity.ApachePortNumber;
            }

            url = new URL("http://"+MainActivity.mySqlCrmObj.getConnectionhost()+ portNumberStr +"/account/escpos-php-development/example/srpospostprintdata.php");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json");


            String str = obj.toString();
            byte[] outputBytes = str.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(outputBytes);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            Log.v("Printing","Error in printing is "+e.toString());
            e.printStackTrace();
        }
        Log.v("PrintFromServer","response is "+response);
        return response;
    }

    public static void printFromServer(JSONObject printInfoObj, String printingType) {
        Log.v("PrintFromServer",printInfoObj.toString());
        Log.v("PrintFromServer",printingType);
                String portNumberStr = "";
                if(!POSWebActivity.ApachePortNumber.equals(""))
                {
                    portNumberStr += ":"+POSWebActivity.ApachePortNumber;
                }
                Log.v("PrintFromServer",MainActivity.mySqlCrmObj.getConnectionhost());
                final String serverurl = "http://"+MainActivity.mySqlCrmObj.getConnectionhost()+ portNumberStr +"/account/escpos-php-development/example/srpospostprintdata.php";
                Log.v("PrintFromServer",serverurl);
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(15 * 1000);
                final RequestParams params = new RequestParams();
                try {

                    printInfoObj.put("printingtype",printingType);
                    printInfoObj.put("source","Android");
                    params.put("printdetails", (printInfoObj));
                    params.put("printingType",printingType);
                    postDataToServer(printInfoObj);
                }catch (Exception e){
                    e.printStackTrace();
                }
/*
                RequestHandle post = client.post(serverurl, params, new TextHttpResponseHandler() {


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.v("ErrorPrinting","We have an error in printing "+responseString);

                        POSWebActivity.showAlertDialogforPrint(ConstantsAndUtilities.printerContext, "Unable to Print. Do You Want to Print again?",printInfoObj,printingType);

                    }


                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Log.v("PrintFromServer","Response is "+responseString);
                    }
                }); */

    }

    public void deletedItemsKOTSPrinting(ArrayList<JSONObject> deletedItems, String voidInvoiceId) {
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        ItemCancellationReasons icr = new ItemCancellationReasons();
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        DatabaseHelper dbHelper = SplashScreen.dbHelper;
        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);
            String htmlString = "";
            String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                    "</head>";
            String bodyContent = "<body>";
            bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
            bodyContent += "<tr><td colspan=\"4\"><h2>Deleted Item Cancelled KOT For Invoice #"+voidInvoiceId+" "+orderRefNum+"</h2></td></tr>";
            bodyContent += "<tr><td>Employee</td><td>"+ Parameters.userid +"</td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Cancellation Status :  </td><td colspan=\"2\">"+cancellationStatus+"</td></tr>";
            ArrayList<JSONObject> invoiceItemDetails = deletedItems;

            String itemsListStr ="<tr><td align=\"left\" style=\"font-size:22px\">Qty</td><td colspan=\"3\">Item</td></tr> ";
            //= "<tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice</td><td align=\"right\"><span style=\"white-space:nowrap;\">110.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 110.00 &#8377</span></td></tr><tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice Full</td><td align=\"right\"><span style=\"white-space:nowrap;\">150.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 150.00 &#8377</span></td></tr>";
            Double subTotal = 0.0d;
            if(invoiceItemDetails.size()>0) {
                for (int p = 0; p < invoiceItemDetails.size(); p++) {
                    JSONObject itemRow = invoiceItemDetails.get(p);
                    String itemRowUniqueId = String.valueOf(itemRow.opt(dbVar.UNIQUE_ID));
                    String reasonForCancellationOfKOT = icr.cancellationReasonForItemWithUniqueId(itemRowUniqueId);

                    itemsListStr += "<tr><td>"+(itemRow.optString(dbVar.INVOICE_QUANTITY))+"</td><td colspan='3'>"+(itemRow.optString(dbVar.INVOICE_ITEM_NAME))+reasonForCancellationOfKOT+"</td></tr>";
                }
            }
            bodyContent += itemsListStr;
            bodyContent += "<tr><td align=\"center\"  colspan=\"4\" style=\"font-size:22px\">***** </td></tr>";
            bodyContent += "</table>";
            htmlString += "<html>"+headTagContent+bodyContent+"</html>";
            String primaryPrinter = Parameters.primaryKOTName();

            saveToPrintingQueue("",htmlString,voidInvoiceId,printInfoObj,"category",primaryPrinter);

            // change here
            {

                String randomKotId = ConstantsAndUtilities.randomValue();
                ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
                try {
                    JSONObject contentValues = new JSONObject();
                    contentValues.put("invoice_id", voidInvoiceId);
                    contentValues.put("kot_id", randomKotId);
                    contentValues.put("kot_no", "");
                    contentValues.put("device_os", "Windows");
                    contentValues.put("devicetoken", dbVar.getValueForAttribute("device_token"));
                    contentValues.put("kot_type", "Cancelled");
                    contentValues.put("kot_content", htmlString);

                    contentValues.put("category_or_general", "General");
                    contentValues.put("created_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                    contentValues.put("updated_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                    contentValues.put("update_user_type", "Employee");
                    contentValues.put("category_id", "-");
                    contentValues.put("created_on", Parameters.currentTime());
                    contentValues.put("updated_on", Parameters.currentTime());

                    insertingValues.add(0, contentValues);
                    sqlCrmObj.executeInsertBatch("kot_tracking", insertingValues);
                    insertingValues = new ArrayList<JSONObject>();
                    for (int p = 0; p < invoiceItemDetails.size(); p++) {
                        String itemName = (String) invoiceItemDetails.get(p).get("item_name");
                        String itemId = (String) invoiceItemDetails.get(p).get("item_id");
                        Double newQty = Double.valueOf((String) invoiceItemDetails.get(p).get("item_quantity"));
                        String itemNote = "Cancelled - "+ (String) invoiceItemDetails.get(p).get("notes");
                        contentValues = new JSONObject();
                        int sizeOfNewItems = insertingValues.size();
                        contentValues.put("kot_id", randomKotId);
                        contentValues.put("item_name", itemName);
                        contentValues.put("item_id", itemId);
                        contentValues.put("item_quantity", df2.format(newQty));
                        contentValues.put("notes", itemNote);
                        contentValues.put("updated_by", "");
                        contentValues.put("created_on", Parameters.currentTime());
                        contentValues.put("updated_on", Parameters.currentTime());

                        insertingValues.add(sizeOfNewItems, contentValues);


                    }
                    if(insertingValues.size()>0)
                    {
                        sqlCrmObj.executeInsertBatch("kot_tracking_items",insertingValues);
                    }
//                    serviceCallForPushNotification(randomKotId);
                }catch (Exception exp){
                    exp.printStackTrace();
                }
            }

        }
    }


    public void deletedItemsCategorywiseKOTSPrinting(ArrayList<JSONObject> deletedItems, String voidInvoiceId, JSONObject printInfoObj) {
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        DatabaseHelper dbHelper = SplashScreen.dbHelper;
        ItemCancellationReasons icr = new ItemCancellationReasons();
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat df3 = new DecimalFormat("#.###");

        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            String orderTypeForCancelledInvoice = orderTypeName(    (String) invoiceDetails.get(0).optString(dbVar.ORDER_TYPE)    );
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);

            ArrayList<JSONObject> invoiceItemDetails = deletedItems;
            JSONObject categoryItems = new JSONObject();
            if(invoiceItemDetails.size()>0){
                for(int i = 0; i < invoiceItemDetails.size(); i++)
                {
                    JSONObject itemRow = invoiceItemDetails.get(i);
                    String itemCategoryId = "";
                    ArrayList<JSONObject> itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemRow.optString("item_id") + "' LIMIT 1");
                    String itemName = (String) itemRow.optString("item_name");
                    if (itemGetDetails.size() == 0) {
                        // check if it is a plu code
                        ArrayList<JSONObject> plucodes = dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + itemRow.optString("item_id") + "' LIMIT 1");
                        if (plucodes.size() == 1) {
                            JSONObject itemRow2 = plucodes.get(0);
                            String itemIdForPLU = (String) itemRow2.optString(dbVar.ALTERNATE_PLU_item_no);
                            itemGetDetails = dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                        }
                    }

                    if (itemGetDetails.size() != 0) {
                        JSONObject itemRow2 = itemGetDetails.get(0);
                        String deptIdForItem = (String) itemRow2.optString(dbVar.INVENTORY_DEPARTMENT);
                        ArrayList<JSONObject> deptDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.DEPARTMENT_TABLE+" WHERE "+dbVar.DepartmentID+"='"+ deptIdForItem +"'");
                        if(deptDetails.size()==1){
                            itemCategoryId = (String) deptDetails.get(0).optString(dbVar.CategoryId);
                        }
                    }
                    if(!categoryItems.has(itemCategoryId)){
                        ArrayList<JSONObject> itemsRowsForCategory = new ArrayList<>();
                        itemsRowsForCategory.add(0,itemRow);
                        try {
                            categoryItems.put(itemCategoryId,itemsRowsForCategory);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
//                            int indexForItem = categoryItems.get()
                        ArrayList<JSONObject> itemsRowsForCategory = new ArrayList<>();
                        try {
                            itemsRowsForCategory = (ArrayList<JSONObject>) categoryItems.get(itemCategoryId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int indexForItem = itemsRowsForCategory.size();
                        itemsRowsForCategory.add(indexForItem,itemRow);
                        try {
                            categoryItems.put(itemCategoryId,itemsRowsForCategory);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }

            if(categoryItems.length()>0){
                ArrayList<JSONObject> allCategoryDetais = sqlObj.executeRawqueryJSON("SELECT * FROM category_details ");
                for(int j=0; j< allCategoryDetais.size();j++) {
                    String categoryUniqueId = (String) allCategoryDetais.get(j).optString("unique_id");
                    ArrayList<JSONObject> categoryprintersDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM categorywise_printing WHERE category_unique_id='"+categoryUniqueId+"'");

                    ArrayList<JSONObject> categoryDetails = sqlObj.executeRawqueryJSON("SELECT * FROM category_details WHERE unique_id='"+categoryUniqueId+"'");
                    String printerCategoryName  = "";
                    if(categoryDetails.size()==0){
                        continue;
                    }
                    else{
                        printerCategoryName = (String) categoryDetails.get(0).optString("category_id");
                    }

                    if(!categoryItems.has(printerCategoryName)){
                        continue;
                    }
                    ArrayList<JSONObject> cancelCategoryinvoiceItemDetails = new ArrayList<>();
                    try {
                        cancelCategoryinvoiceItemDetails = (ArrayList<JSONObject>) categoryItems.get(printerCategoryName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String printerRowId = "";
                    if(categoryprintersDetails.size()==1)
                    {   printerRowId = (String) categoryprintersDetails.get(0).optString("printer_row_id");   }
                    String selectQuery2 = "SELECT  * FROM printers where _id='" + printerRowId + "'";
                    ArrayList<JSONObject> printer1Details = sqlCrmObj.executeRawqueryJSON(selectQuery2);
                    String headlines = "", footerEmpty = "";
                    int iPaperWidth = 3;
                    String printerName = "";
                    if (printer1Details != null && printer1Details.size() > 0) {


                        for (int cntPrnt1 = 0; cntPrnt1 < printer1Details.size(); cntPrnt1++) {
                            JSONObject currentRowPrnt1 = printer1Details.get(cntPrnt1);
                            headlines = (String) currentRowPrnt1.optString("header_characters");
                            footerEmpty = (String) currentRowPrnt1.optString("footer_characters");
//                            String paperWidth = (String) currentRowPrnt1.optString("paper_width");
//                iPaperWidth = (paperWidth.equals("3 inch")) ? (570) : (380);
                            printerName = (String) currentRowPrnt1.optString("printer_name");


                        }
                    }
                    String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                            "</head>";
                    String bodyContent = "<body>";
                    bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
                    bodyContent += "<tr><td colspan=\"4\"><h2>Deleted Item - Cancelled KOT For Invoice #"+voidInvoiceId+" "+orderRefNum+"</h2></td></tr>";
                    bodyContent += "<tr><td colspan=\"4\"><h2>"+printerCategoryName+" - "+orderTypeForCancelledInvoice+"</h2></td></tr>";
                    bodyContent += "<tr><td>Employee</td><td>"+ Parameters.userid +"</td></tr>";
                    bodyContent += "<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (Parameters.currentTimeDayMonthFormat())+" </td></tr>";
                    bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Cancellation Status :  </td><td colspan=\"2\">"+cancellationStatus+"</td></tr>";

                    String itemsListStr ="<tr><td align=\"left\" style=\"font-size:22px\">Qty</td><td>Item</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;</td></tr> ";
                    //= "<tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice</td><td align=\"right\"><span style=\"white-space:nowrap;\">110.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 110.00 &#8377</span></td></tr><tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice Full</td><td align=\"right\"><span style=\"white-space:nowrap;\">150.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 150.00 &#8377</span></td></tr>";
                    Double subTotal = 0.0d;
                    if(cancelCategoryinvoiceItemDetails.size()>0) {
                        for (int p = 0; p < cancelCategoryinvoiceItemDetails.size(); p++) {
                            JSONObject itemRow = cancelCategoryinvoiceItemDetails.get(p);
                            String itemRowUniqueId = String.valueOf(itemRow.optString(dbVar.UNIQUE_ID));
                            String reasonForCancellationOfKOT = icr.cancellationReasonForItemWithUniqueId(itemRowUniqueId);

                            itemsListStr += "<tr><td>"+(itemRow.optString(dbVar.INVOICE_QUANTITY))+"</td><td colspan='3'>"+(itemRow.optString(dbVar.INVOICE_ITEM_NAME))+reasonForCancellationOfKOT+"</td></tr>";
                        }
                    }
                    bodyContent += itemsListStr;
                    bodyContent += "<tr><td align=\"center\"  colspan=\"4\" style=\"font-size:22px\">***** </td></tr>";
                    bodyContent += "</table>";
                    String htmlString = "<html>"+headTagContent+bodyContent+"</html>";

                    String primaryPrinter = Parameters.primaryPrinterName();
                    kotTrackingSaveDeletedItems("-",htmlString,voidInvoiceId, printInfoObj,"category",printerCategoryName,"Item Deleted",cancelCategoryinvoiceItemDetails);
                    String localCategorywisePrinter = localCategoryWisePrinterNameForCategoryId(printerCategoryName); if(!localCategorywisePrinter.equals("")) { printerName = localCategorywisePrinter; }
                    if(!printerName.equals(""))
                    {
//                        printHtmlToPrinter(htmlString,printerName,false);
                        saveToPrintingQueue("",htmlString,voidInvoiceId,printInfoObj,"general",printerName);

                    }
                    saveToPrintingQueue("",htmlString,voidInvoiceId,printInfoObj,"general",primaryPrinter);



                }

            }
        }
    }

    private String localCategoryWisePrinterNameForCategoryId(String categoryId) {
        String printerName = "";
        DatabaseHelper dbHelper = SplashScreen.dbHelper;
        DatabaseVariables dbVar = new DatabaseVariables();
        Log.v("Printing","SSID is "+POSWebActivity.ssid);
        String licenseKey = dbVar.getValueForAttribute(ConstantsAndUtilities.LICENSE_KEY);
        if(licenseKey.equals("")) {
           return printerName;
        }
        ConstantsAndUtilities cv = new ConstantsAndUtilities();
        printerName = cv.categoryAssignedPrinterForSSID(POSWebActivity.assignmentOfPrinters,POSWebActivity.ssid,categoryId);
        if(!printerName.equals(""))
        {
            return printerName;
        }
        ArrayList<JSONObject> categoryPrintingDetails = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbHelper.CATEGORYWISE_PRINTING_TABLE+" WHERE "+dbHelper.CATEGORYWISE_PRINTING_CATEGORY_ID+"='"+categoryId+"'");
        if(categoryPrintingDetails.size()==0)
        {
            return printerName;
        }else{
            JSONObject categoryPrinterObj = categoryPrintingDetails.get(0);
            printerName = categoryPrinterObj.optString(dbHelper.CATEGORYWISE_PRINTING_PRINTER_NAME);
        }
        return printerName;
    }

    public void printCategoryWiseKots(String printInvoiceId, JSONObject printInfoObj) {

        JSONObject obj = null;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        ItemCancellationReasons icr = new ItemCancellationReasons();
        String cancelledItemsKOTString = "";
        try {
            obj = printInfoObj.getJSONObject("orderdetails");


        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat df3 = new DecimalFormat("#.###");

        JSONArray itemIds = (JSONArray) obj.get("itemIds");
        JSONArray discountInCurrency = (JSONArray) obj.get("discountInCurrency");
        JSONArray itemNames = (JSONArray) obj.get("itemNames");
        JSONArray itemNotes = (JSONArray) obj.get("itemNotes");
        JSONArray savedOlderUniqueIds = (JSONArray) obj.get("savedOlderUniqueIds");
        JSONArray totalItemPrices = (JSONArray) obj.get("totalItemPrices");
        JSONArray itemQuantitys = (JSONArray) obj.get("itemQuantitys");

        JSONArray itemCategoryIds = (JSONArray) obj.get("categoryIds");

            ConstantsAndUtilities cv = new ConstantsAndUtilities();
        String kotNum = cv.incrementKotSerialNo();
        String generatedKot = kotNum;
        if(!kotNum.equals("")){ kotNum = "KOT "+kotNum; }
        String neworrepeat = (String) printInfoObj.optString("neworrepeat");
        kotNum = neworrepeat + " "+kotNum;

        String orderRefNum = "";
        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM invoice_total_table WHERE invoice_id='"+printInvoiceId+"'");
        String storeName="",storeAddress="",storePhoneNum="",orderTypeString="";
        if(invoiceDetails.size()==1){
            orderRefNum = (String) invoiceDetails.get(0).get(DatabaseVariables.INVOICE_HOLD_ID);
            if(!orderRefNum.equals("")){ orderRefNum = "<tr><td align=\"center\" colspan=\"4\"  id=\"kotOrderRefNum\" style=\"font-size:22px\">ORDER REF. NO# "+ orderRefNum+" </td></tr>";}
            String storeIdForInvoice = (String) invoiceDetails.get(0).get(DatabaseVariables.STORE_ID);
            if(!storeIdForInvoice.equals("")){
                ArrayList<JSONObject> storeDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ DatabaseVariables.STORE_TABLE + " WHERE store_id='" + storeIdForInvoice + "'");
                if(storeDetails.size()==1){
                    storeName = (String) storeDetails.get(0).get(DatabaseVariables.STORE_NAME);
                    storeAddress = storeDetails.get(0).get(DatabaseVariables.STORE_STREET)+",\n"+storeDetails.get(0).get(DatabaseVariables.STORE_CITY)+"-"+storeDetails.get(0).get(DatabaseVariables.STORE_POSTAL);
                    storePhoneNum = (!storeDetails.get(0).get(DatabaseVariables.STORE_NUMBER).equals("")) ? ("<tr><td align=\"center\" colspan=\"4\" style=\"font-size:22px\">Phone No# "+ storeDetails.get(0).get(DatabaseVariables.STORE_NUMBER) +" </td></tr>") : "";
                }
            }

            orderTypeString = orderTypeName((String) invoiceDetails.get(0).get(DatabaseVariables.ORDER_TYPE));
        }

        String kotString = "";
        if(!kotNum.equals("")){
            kotString = "<br /><span class=\"tablehead\">"+kotNum+"</span>";

        }
        String tokenNoStr = (obtainTokenNumber().equals("")) ? "" : "<tr><td colspan=\"4\"><h4 style=\"text-align:center; font-size:20px;\">"+ (obtainTokenNumber())+"</h4></td></tr>";
        String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                "</head>";

        // ArrayList<JSONObject> categoryprintersDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM categorywise_printing ");

        ArrayList<JSONObject> categoryDetails = sqlObj.executeRawqueryJSON("SELECT * FROM category_details ");
        for(int j=0; j< categoryDetails.size();j++)
        {
            String categoryUniqueId = (String) categoryDetails.get(j).get("unique_id");
            ArrayList<JSONObject> categoryprintersDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM categorywise_printing WHERE category_unique_id='"+categoryUniqueId+"'");
            String printerRowId = "";
            if(categoryprintersDetails.size()==1) {
                printerRowId = (String) categoryprintersDetails.get(0).get("printer_row_id");
            }
            String selectQuery2 = "SELECT  * FROM printers where _id='"+printerRowId+"'";
            ArrayList<JSONObject> printer1Details = sqlCrmObj.executeRawqueryJSON(selectQuery2);
            String headlines = "", footerEmpty=""; int iPaperWidth=3; String printerName="";
            if (printer1Details!=null && printer1Details.size() > 0) {


                for(int cntPrnt1=0; cntPrnt1<printer1Details.size();cntPrnt1++)
                {
                    JSONObject currentRowPrnt1 = printer1Details.get(cntPrnt1);
                    headlines = (String) currentRowPrnt1.get("header_characters");
                    footerEmpty = (String) currentRowPrnt1.get("footer_characters");
//                    String paperWidth = (String) currentRowPrnt1.get("paper_width");
//                iPaperWidth = (paperWidth.equals("3 inch")) ? (570) : (380);
                    printerName  = (String) currentRowPrnt1.get("printer_name");
                }

            }
            String billContent = "<tr> <th align=\"center\" colspan=\"4\"><span class=\"tablehead\">"+"place@For@Category "+orderTypeString + headlines+"</span>"+kotString+"</th></tr>"+tokenNoStr+"<tr><td align=\"left\" colspan=\"4\" style=\"text-align:center; font-size:22px; font-weight:bold;\">INVOICE# "+printInvoiceId+"</td></tr>"+orderRefNum+"";
            billContent += "<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr><tr ><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">CASHIER: "+(invoiceDetails.get(0).get(DatabaseVariables.INVOICE_EMPLOYEE))+" </td></tr><tr><td colspan=\"4\" style=\"border-top:dotted 2px solid;\">&nbsp;</td> <!--  for blank row --></tr>";
            ArrayList<JSONObject> categoryInfo = sqlObj.executeRawqueryJSON("SELECT * FROM  "+DatabaseVariables.CATEGORY_TABLE+" WHERE unique_id='"+categoryUniqueId+"'");
            String categoryName = "";
            if(categoryInfo.size()==1){
                categoryName = (String) categoryInfo.get(0).get(DatabaseVariables.CategoryId);
            }

            JSONArray savedQuantities = (JSONArray) obj.get("savedQuantities");
            String holdInvoiceId = (String) obj.get("holdInvoiceId");
            String itemsKot = "";
            String itemsListStr = "";
            for (int p = 0; p < itemIds.length(); p++) {
                if(!itemCategoryIds.get(p).equals(categoryName)){
                    continue;
                }
                String itemId = (String) itemIds.get(p);
                String savedQty = "0";
                if(     (savedQuantities.get(p) != null) && (!savedQuantities.get(p).equals(""))   )
                {
                    savedQty = (String) savedQuantities.get(p);
                }

                if (Double.parseDouble(savedQty) != Double.parseDouble((String) itemQuantitys.get(p))) {
                    Double newQty = Double.parseDouble((String) itemQuantitys.get(p)) - Double.parseDouble(savedQty);

                    itemsKot = "";
                    String itemName = (String) itemNames.get(p);
                    String itemNote = (!itemNotes.get(p).equals("")) ? (" - " + itemNotes.get(p)) : "";
                    if(newQty < 0)
                    {

                        Double newQtyCheck = Math.abs(newQty);

                        if (Double.parseDouble(savedQty) != 0) {
                            itemNote = " - Older Qty " + df.format(Double.parseDouble(savedQty)) + itemNote;
                        }

                        itemsKot += itemName;
                        itemsKot += itemNote;
                        String itemRowUniqueId = String.valueOf(savedOlderUniqueIds.get(p));
                        String reasonForCancellationOfKOT = icr.cancellationReasonForItemWithUniqueId(itemRowUniqueId);
                        itemsKot += reasonForCancellationOfKOT;
                        cancelledItemsKOTString += "<tr class=\"kotItemRow\"><td>"+ df3.format(newQtyCheck) +"</td><td colspan=\"3\">"+itemsKot+"</td></tr>";
//                        itemNote += ( " - <span class=\"cancelledItemSpan\"><b> Cancelled "+ df.format(Double.parseDouble(savedQty)+"</b></span>");
                    }

                    if (Double.parseDouble(savedQty) != 0) {
                        itemNote = " - Old Qty " + df.format(Double.parseDouble(savedQty)) + itemNote;
                    }
                    itemName = itemName.substring(0, Math.min(itemName.length(), 33));
                    itemsKot += itemName;
                    itemsKot += itemNote;
                    if(newQty > 0) { itemsListStr += "<tr class=\"kotItemRow\"><td>"+ df3.format(newQty) +"</td><td colspan=\"3\">"+itemsKot+"</td></tr>"; }
                }

            }

            if(!cancelledItemsKOTString.equals(""))
            {
                itemsListStr += "<tr><td colspan=\"4\">&nbsp;<br /><td></tr><tr><td style=\"text-decoration : underline; text-align : center;\" colspan=\"4\"><h3>Cancelled Items</h3></td></tr>" + cancelledItemsKOTString +"<tr><td colspan=\"4\">&nbsp;<br /><td></tr>";
                cancelledItemsKOTString = "";
            }
            if(!itemsListStr.equals("")) {
                billContent += "" + itemsListStr;
                billContent = "<html>" + headTagContent + "<body class=\"categoryWiseKOTBody\"><table  id='page'  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">" + billContent + "<tr><td colspan=\"4\">"+footerEmpty+"</td></tr></table></body></html>";
                billContent = billContent.replace("place@For@Category",categoryName);
                kotTrackingSave(generatedKot,billContent,printInvoiceId, printInfoObj,"category",categoryName,"Order Placed");
                String localCategorywisePrinter = localCategoryWisePrinterNameForCategoryId(categoryName); if(!localCategorywisePrinter.equals("")) { printerName = localCategorywisePrinter; }
                if(categoryprintersDetails.size()==1 || !printerName.equals(""))
                {
//                    System.out.println("Printing Category wise KOT for "+categoryName);
//                    printHtmlToPrinter(billContent,printerName,false);
                    saveToPrintingQueue(generatedKot,billContent,printInvoiceId,printInfoObj,"category",printerName);

                }

            }
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String htmlContentForInvoice(String printInvoiceId, JSONObject contentObj)
    {

        String printString="";
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        Preferences prefs = Preferences.userRoot();
        DecimalFormat df = new DecimalFormat("#.##");
        Double totalDiscountGivenOnSubtotal = 0.0d;
        Double totalSavingsOnMrp = 0.0d;
        Boolean hasMRPforAllItems = true;
        String heightFunction = "function getHeight(){ var x = document.getElementsByTagName(\"body\")[0].offsetHeight;  AndroidInterface.showToast(\"Toast Check\" + x); } " +
                " ";
        String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;} .bottomborderStyle { border-bottom:2px solid #000000;} .topborderStyle { border-top:2px solid #000000;}  .topandbottomborderStyle { border-top:2px solid #000000; border-bottom:2px solid #000000;} table td.totalAmtWithRoundoff,table td.grandTotalTag{ font-size:32px; } </style>" +
                "</head>";
        int randomNumber= 	new Random().nextInt((89 - 10) + 1) + 10;
        String randomNumberString = String.valueOf(randomNumber);
        String tokenNoStr = (obtainTokenNumber().equals("")) ? "" : "<tr><td colspan=\"4\"><h4 style=\"text-align:center; font-size:20px;\">"+ (obtainTokenNumber())+"</h4></td></tr>";
        heightFunction += "function returnHeight(){var x = document.getElementsByTagName(\"body\")[0].offsetHeight;  AndroidInterface.submitHeight(x);}";
        String numbersDisplay = "";
        for(int k=1;k<=randomNumber;k++)
        {
            numbersDisplay += (String.valueOf(k)) + "<br />";
        }

        String selectQuery2 = "SELECT  * FROM printers where is_primary_printer='Yes' ORDER BY _id DESC LIMIT 1";
        ArrayList<JSONObject> printer1Details = sqlCrmObj.executeRawqueryJSON(selectQuery2);
        String headlines = "", footerEmpty=""; int iPaperWidth=3;
        if (printer1Details!=null && printer1Details.size() > 0) {


            for(int cntPrnt1=0; cntPrnt1<printer1Details.size();cntPrnt1++)
            {
                JSONObject currentRowPrnt1 = printer1Details.get(cntPrnt1);
                headlines = (String) currentRowPrnt1.optString("header_characters");
                footerEmpty = (String) currentRowPrnt1.optString("footer_characters");
                String paperWidth = (String) currentRowPrnt1.optString("paper_width");
//                iPaperWidth = (paperWidth.equals("3 inch")) ? (570) : (380);

            }

        }
        try {
            if (contentObj.get("content_type").equals("reprintinvoice")) {
                headlines = "Reprinted Invoice <br />" + headlines;
            }
        }catch (Exception exp){
            exp.printStackTrace();
        }

        String orderRefNum = "";
        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM invoice_total_table WHERE invoice_id='"+printInvoiceId+"'");
        String storeName="",storeAddress="",storePhoneNum="",orderTypeString="";
        String customerId = "";
        if(invoiceDetails.size()==1){
            customerId = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_CUSTOMER);
            orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            if(!orderRefNum.equals("")){ orderRefNum = "<tr><td align=\"center\"  id=\"kotOrderRefNum\" colspan=\"4\" style=\"font-size:22px\">ORDER REF. NO# "+ orderRefNum+" </td></tr>";}
            String storeIdForInvoice = (String) invoiceDetails.get(0).optString(dbVar.STORE_ID);
            if(!storeIdForInvoice.equals("")){
                ArrayList<JSONObject> storeDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.STORE_TABLE + " WHERE store_id='" + storeIdForInvoice + "'");
                if(storeDetails.size()==1){
                    storeName = (String) storeDetails.get(0).optString(dbVar.STORE_NAME);
                    storeAddress = storeDetails.get(0).optString(dbVar.STORE_STREET)+",\n"+storeDetails.get(0).optString(dbVar.STORE_CITY)+"-"+storeDetails.get(0).optString(dbVar.STORE_POSTAL);
                    storePhoneNum = (!storeDetails.get(0).optString(dbVar.STORE_NUMBER).equals("")) ? ("<tr><td align=\"center\" colspan=\"4\" style=\"font-size:22px\">Phone No# "+ storeDetails.get(0).optString(dbVar.STORE_NUMBER) +" </td></tr>") : "";
                }
            }

            orderTypeString = orderTypeName((String) invoiceDetails.get(0).optString(dbVar.ORDER_TYPE));
        }
        ArrayList<JSONObject> invoiceItemDetails = sqlObj.executeRawqueryJSON("SELECT invoice_items_table.*,optional_info_table.unit_type,optional_info_table.mrp,optional_info_table.bottle_deposit_value, optional_info_table.has_bottle_deposit , inventorytable.inventary_taxone FROM invoice_items_table LEFT JOIN optional_info_table ON invoice_items_table.item_id = optional_info_table.inventory_item_no LEFT JOIN inventorytable ON optional_info_table.inventory_item_no = inventorytable.inventory_item_no WHERE invoice_id='"+printInvoiceId+"'");
        ArrayList<JSONObject> invoiceItemCountDetails = sqlObj.executeRawqueryJSON("SELECT SUM(item_quantity) as totalQuantity FROM invoice_items_table WHERE invoice_id='"+printInvoiceId+"'");
        String customerCopyString = "";
        printInfoObj = contentObj;
        if(printInfoObj.optString("content_type").equals("printpreview")){
            customerCopyString = "<tr><td colspan=\"4\" style=\"text-align:center;\"><span class=\"tablehead\">Customer Copy</span></td></tr>";
        }
        if(printInfoObj.optString("content_type").equals("duplicatecustomercopy")){
            customerCopyString = "<tr><td colspan=\"4\" style=\"text-align:center;\"><span class=\"tablehead\">Duplicate Bill</span></td></tr>";
        }
        String totalQuantityOfItems = (String) invoiceItemCountDetails.get(0).optString("totalQuantity");
        String currencyHtmlStr = MainActivity.currencyTypehtml;
        String customerDisplayString = "";
        if(!customerId.equals(""))
        {
            CustomersManager cm = new CustomersManager();
            JSONObject customerInfoForId = cm.customerInfoForId(customerId);
            customerDisplayString = "<tr class=\"customerInfoRow\"><td class=\"topborderStyle\" colspan=\"2\">Customer Name : "+(customerInfoForId.optString("customer_name"))+"</td><td  class=\"topborderStyle\"  colspan=\"2\">Phone : "+(customerInfoForId.optString("phone_number"))+"</td></tr>";
            if(!customerInfoForId.optString("company_name").equals("") && !customerInfoForId.optString("company_tin").equals("")) {
                customerDisplayString += "<tr class=\"customerInfoRow\"><td colspan=\"2\">Customer Company : " + (customerInfoForId.optString("company_name")) + "</td><td colspan=\"2\">Company TIN : " + (customerInfoForId.optString("company_tin")) + "</td></tr>";
            }
            customerDisplayString += "<tr class=\"customerInfoRow\" ><td class=\"bottomborderStyle\" colspan=\"2\">Account Balance : "+(customerInfoForId.optString("account_balance"))+"&nbsp;"+ (currencyHtmlStr) +"</td><td  class=\"bottomborderStyle\" colspan=\"2\">Customer ID : "+(customerInfoForId.optString("customer_id"))+"</td></tr>";
            customerDisplayString += "<tr class=\"customerInfoRow\" ><td class=\"bottomborderStyle\" colspan=\"4\">Available Reward Points : "+(cm.rewardPointsOfCustomer(customerId))+"</td></tr>";

        }

        String billContent = customerCopyString+"<tr> <th align=\"center\" colspan=\"4\"><span class=\"tablehead\">"+headlines+"</span></th></tr><tr class=\"storeNameRow\"><td align=\"center\" colspan=\"4\" style=\"font-size:22px;font-weight:bold;\">"+storeName+"</td></tr><tr><td align=\"left\" colspan=\"4\" style=\"text-align:center; font-size:22px; font-weight:bold;\">INVOICE# "+printInvoiceId+"</td></tr>"+tokenNoStr+orderRefNum+"";
        billContent += "<tr><td align=\"center\" colspan=\"4\" style=\"font-size:22px\">"+storeAddress+"</td></tr>"+storePhoneNum+"<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (ConstantsAndUtilities.currentTimeDayMonthFormat())+" </td></tr><tr class=\"cashierTagRow\"><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">CASHIER: "+(invoiceDetails.get(0).optString(dbVar.INVOICE_EMPLOYEE))+" </td><td align=\"right\"  colspan=\"2\" style=\"font-size:22px\">Order Type: "+orderTypeString+" </td></tr>"+customerDisplayString+"<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px;padding-bottom:12px;\">Item Count: "+ (invoiceItemDetails.size())+" </td><td align=\"right\"  colspan=\"2\" style=\"font-size:22px;padding-bottom:12px;\">Total Qty: "+df.format(Double.parseDouble(totalQuantityOfItems))+" </td></tr>";

        String itemsListStr ="<tr style=\"border-bottom:2px solid #000000;\"><td align=\"left\" style=\"font-size:22px\" class=\"topandbottomborderStyle\">Qty</td><td class=\"topandbottomborderStyle\">Item</td><td class=\"topandbottomborderStyle\" align=\"right\">Unit Price</td><td align=\"right\" class=\"topandbottomborderStyle\">Amount</td></tr> ";
        //= "<tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice</td><td align=\"right\"><span style=\"white-space:nowrap;\">110.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 110.00 &#8377</span></td></tr><tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice Full</td><td align=\"right\"><span style=\"white-space:nowrap;\">150.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 150.00 &#8377</span></td></tr>";
        Double subTotal = 0.0d;
        Double totalBottleRefundValue = 0.0d;
        if(invoiceItemDetails.size()>0){
            for(int p=0;p<invoiceItemDetails.size();p++){
                String quantityString= (String) invoiceItemDetails.get(p).optString(dbVar.INVOICE_QUANTITY);
                String itemName= (String) invoiceItemDetails.get(p).optString(dbVar.INVOICE_ITEM_NAME);
                String totalPrice= (String) invoiceItemDetails.get(p).optString(dbVar.INVOICE_YOUR_COST);
                Double discountGivenOnItem = 0.0d;
                if( !((String) invoiceItemDetails.get(p).optString(dbVar.INVOICE_DISCOUNT)).equals(""))
                {
                    discountGivenOnItem = Double.parseDouble((String) invoiceItemDetails.get(p).optString(dbVar.INVOICE_DISCOUNT));
                    totalDiscountGivenOnSubtotal += discountGivenOnItem;
                }
                Double unitPrice = (Double.parseDouble(totalPrice)) / (Double.parseDouble(quantityString));
                Double qtyPurchased = (Double.parseDouble(quantityString));
                String units="";
                String hasBottleDeposit = "no";
                Double bottleRefundValuePerUnit = 0.0d;
//                ArrayList<JSONObject> optionalInfoDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.OPTIONAL_INFO_TABLE + " WHERE "+dbVar.INVENTORY_ITEM_NO+"='" + invoiceItemDetails.get(p).optString(dbVar.INVOICE_ITEM_ID)+ "'");
//                ArrayList<JSONObject> inventoryInfoDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVENTORY_TABLE + " WHERE "+dbVar.INVENTORY_ITEM_NO+"='" + invoiceItemDetails.get(p).optString(dbVar.INVOICE_ITEM_ID)+ "'");
//                if(optionalInfoDetails.size()==1)
                {
                    units = (String) invoiceItemDetails.get(p).optString(dbVar.UNIT_TYPE);
                    try{ hasBottleDeposit = (String) invoiceItemDetails.get(p).optString("has_bottle_deposit"); bottleRefundValuePerUnit = Double.parseDouble( String.valueOf(invoiceItemDetails.get(p).optString("bottle_deposit_value"))); }catch (Exception exp){ exp.printStackTrace(); }
                }
                if(!units.equals("")){                    quantityString += " "+units; }
                Boolean itemShouldDisplayMRP = false;
                Boolean itemShouldDisplayBottleRefund = true;
                Double mrp = 0.0d;
                try{
                    String mrpString = (String)invoiceItemDetails.get(p).optString(dbVar.INVENTORY_MRP);

                    mrp = Double.parseDouble(!mrpString.equals("") ? mrpString : "0.00");

                }catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                if((mrp == 0) || mrp < unitPrice)
                {
                    hasMRPforAllItems = false;
                }else{
                    itemShouldDisplayMRP = true;
                    totalSavingsOnMrp += (mrp * (qtyPurchased)) - (Double.parseDouble(totalPrice));
                }

                String serviceEmployeeId = "";
                serviceEmployeeId = serviceEmployeeForUniqueId((String) invoiceItemDetails.get(p).optString(dbVar.UNIQUE_ID));
                itemName += serviceEmployeeId;
                itemsListStr += "<tr class=\"billItemsRow\" valign='top'><td align=\"left\" style=\"font-size:22px\">"+quantityString+"</td>";
                itemsListStr +="<td>"+itemName+"</td><td align=\"right\"><span style=\"white-space:nowrap;\">"+ currencyHtmlStr +"&nbsp;"+ (df.format(unitPrice).toString()) +"</span></td><td align=\"right\"><span style=\"white-space:nowrap;\">"+ currencyHtmlStr  +"&nbsp;"+ (nf.format(Double.parseDouble(totalPrice.toString())))+"</span></td></tr>";

                if(hasBottleDeposit.equals("yes") && bottleRefundValuePerUnit!=0)
                {
                    totalBottleRefundValue += bottleRefundValuePerUnit * qtyPurchased;
                    itemsListStr +="<tr class=\"bottleRefundableAmountOfItemRow\"><td>&nbsp;</td><td>Refundable Deposit</td><td align=\"right\">"+currencyHtmlStr+"&nbsp;"+(nf.format(bottleRefundValuePerUnit))+"</td><td align=\"right\">"+ currencyHtmlStr+"&nbsp;"+ (nf.format(bottleRefundValuePerUnit * qtyPurchased)) +"</td></tr>";
                }
                if(discountGivenOnItem!=0)
                {
                    itemsListStr +="<tr class=\"totalDiscountGivenOnItemRow\"><td>&nbsp;</td><td colspan=\"2\">Total Discount Given <span style=\"white-space:nowrap;\">"+(nf.format(discountGivenOnItem))+"&nbsp;"+currencyHtmlStr+"</span></td><td>&nbsp;</td></tr>";
                    itemsListStr +="<tr class=\"actualPriceOfItemRow\"><td>&nbsp;</td><td colspan=\"2\">Total Selling Price <span style=\"white-space:nowrap;\">"+(nf.format(discountGivenOnItem + (Double.parseDouble((String) totalPrice))))+"&nbsp;"+currencyHtmlStr+"</span></td><td>&nbsp;</td></tr>";

                }
                if(itemShouldDisplayMRP==true)
                {
                    // itemsListStr +="<tr class=\"mrpPriceOfItemRow\"><td>&nbsp;</td><td colspan=\"2\">MRP <span style=\"white-space:nowrap;\">"+(df.format((mrp)))+"&nbsp;"+currencyHtmlStr+"</span></td><td>&nbsp;</td></tr>";
                }
                String taxStr = "";
//                if(inventoryInfoDetails.size()==1)
                {
//                    JSONObject inventoryInfo = inventoryInfoDetails.get(0);
                    taxStr = (String) invoiceItemDetails.get(p).optString(dbVar.INVENTORY_TAXONE);
                }
                if(taxStr!=null && !taxStr.equals(""))
                {
                    String itemInclusiveTax = itemwiseTaxCalc(taxStr,Double.parseDouble(totalPrice));
                    String gstStr = "<td>&nbsp;</td><td colspan=\"3\">"+itemInclusiveTax+"</td></tr>";
                    itemsListStr += gstStr;
                }
                subTotal += (Double.parseDouble(totalPrice));
            }
        }

        ArrayList<JSONObject> InvoiceTaxDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
        ArrayList<JSONObject> InvoiceCategoryTaxDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");

        ArrayList<JSONObject> NumberOfCategoriesInvoiceCategoryTaxDetails = sqlObj.executeRawqueryJSON("SELECT DISTINCT("+dbVar.CAT_TAX_CAT_ID+") FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
        String taxCalculationStr = "",modeOfPaymentString="",calculatedGrandTotal="";
        Boolean showOnlyDistinctTaxes = true;
        Double totalTaxValue = 0.0d;
        if(InvoiceCategoryTaxDetails.size()>0 || InvoiceTaxDetails.size()>0){
            taxCalculationStr += "<tr class=\"taxesSubHeadingRow\"><td colspan=\"4\" align=\"left\">Taxes:</td></tr>";
            if(NumberOfCategoriesInvoiceCategoryTaxDetails.size()>1){ showOnlyDistinctTaxes = true;}

            if(InvoiceCategoryTaxDetails.size()>0){
                if(showOnlyDistinctTaxes==false)
                {
                    for(int k=0;k<InvoiceCategoryTaxDetails.size();k++){
                        JSONObject taxRow = InvoiceCategoryTaxDetails.get(k);
                        String printTax = ConstantsAndUtilities.rightPadding((taxRow.optString(dbVar.TAX_AMOUNT_NAME))+" ON "+(taxRow.optString(dbVar.CategoryId)),35);

                        String taxValueStr = nf.format(Double.parseDouble(String.valueOf( taxRow.opt(dbVar.TAX_AMOUNT_VALUE))));
                        totalTaxValue = totalTaxValue + Double.parseDouble((String) taxRow.opt(dbVar.TAX_AMOUNT_VALUE));
                        taxCalculationStr += "<tr><td colspan=\"3\" align=\"left\">"+printTax + "</td><td align=\"right\">"+currencyHtmlStr+"&nbsp;"+ taxValueStr +"</td></tr>";
                    }
                }else{InvoiceCategoryTaxDetails = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
                    ArrayList<JSONObject> InvoiceDistinctCategoryTaxDetails = sqlObj.executeRawqueryJSON("SELECT DISTINCT("+dbVar.TAX_AMOUNT_NAME+") FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "'");
                    if(InvoiceDistinctCategoryTaxDetails.size()>0)
                    {
                        for(int ct=0;ct<InvoiceDistinctCategoryTaxDetails.size();ct++)
                        {
                            JSONObject currentCategoryTaxRow = InvoiceDistinctCategoryTaxDetails.get(ct);
                            String categoryTaxName = (String) currentCategoryTaxRow.opt("tax_name");
                            InvoiceCategoryTaxDetails = sqlObj.executeRawqueryJSON("SELECT SUM(tax_value) AS totalTaxOnCategory FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + printInvoiceId + "' AND "+dbVar.TAX_AMOUNT_NAME+"='"+ dbVar.addSlashes(categoryTaxName)+"'");

                            String printTax = ConstantsAndUtilities.rightPadding((categoryTaxName),35);

                            String taxValueStr = ConstantsAndUtilities.leftPadding((String) InvoiceCategoryTaxDetails.get(0).optString("totalTaxOnCategory"),10);
                            totalTaxValue = totalTaxValue + Double.parseDouble((String) InvoiceCategoryTaxDetails.get(0).opt("totalTaxOnCategory"));
                            taxCalculationStr += "<tr><td colspan=\"3\" align=\"left\">"+printTax + "</td><td align=\"right\">"+ currencyHtmlStr +"&nbsp;"+ nf.format(Double.valueOf((String) InvoiceCategoryTaxDetails.get(0).opt("totalTaxOnCategory")))+"</td></tr>";

                        }
                    }
                }


            }


            if(InvoiceTaxDetails.size()>0){

                for(int k=0;k<InvoiceTaxDetails.size();k++){
                    JSONObject taxRow = InvoiceTaxDetails.get(k);
                    String printTax = ConstantsAndUtilities.rightPadding((String) taxRow.opt(dbVar.TAX_AMOUNT_NAME),35);

                    String taxValueStr = nf.format(Double.parseDouble(String.valueOf( taxRow.opt(dbVar.TAX_AMOUNT_VALUE))));
                    totalTaxValue = totalTaxValue + Double.parseDouble((String) taxRow.opt(dbVar.TAX_AMOUNT_VALUE));

                    taxCalculationStr += "<tr><td colspan=\"3\" align=\"left\">"+printTax + "</td><td align=\"right\">"+currencyHtmlStr+"&nbsp;"+taxValueStr+"</td></tr>";
                }
            }

            String totalTaxStr = ConstantsAndUtilities.leftPadding((nf.format(totalTaxValue)).toString(),34);
            taxCalculationStr += "<tr class=\"totalExclusiveTaxesRow\"><td colspan=\"3\" align=\"left\"><b>Total Tax</b></td><td align=\"right\">"+currencyHtmlStr+"&nbsp;"+totalTaxStr+"</td></tr>";

        }

        Double grandTotalWithoutRoundOff = subTotal + totalTaxValue + totalBottleRefundValue;
        calculatedGrandTotal = (nf.format(grandTotalWithoutRoundOff).toString());

        modeOfPaymentString ="";
        if(invoiceDetails.get(0).opt(dbVar.INVOICE_STATUS).equals("complete")){
            if(!(invoiceDetails.get(0).opt(dbVar.INVOICE_PAYMENT_TYPE)).equals("multiple")) {
                modeOfPaymentString = "<tr><td align=\"left\" colspan=\"2\" >" + invoiceDetails.get(0).opt(dbVar.INVOICE_PAYMENT_TYPE) + "</td><td align=\"right\" colspan=\"2\" >" + currencyHtmlStr + "&nbsp;" + calculatedGrandTotal + "</td></tr>";
            }else{
                ArrayList<JSONObject> splitInvoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + (invoiceDetails.get(0).opt(dbVar.INVOICE_ID)) + "'");
                if(splitInvoiceDetails.size()>0)
                {
                    for(int jsi=0; jsi< splitInvoiceDetails.size();jsi++)
                    {
                        JSONObject splitInvoiceRow = splitInvoiceDetails.get(jsi);
//                        amountToBeRevertedBack += Double.parseDouble(String.valueOf(  splitInvoiceRow.get(dbVar.SPLIT_AMOUNT) ));
                        modeOfPaymentString += "<tr><td align=\"left\" colspan=\"2\" >" + splitInvoiceRow.opt(dbVar.SPLIT_PAYMENT_TYPE) + "</td><td align=\"right\" colspan=\"2\" >" + currencyHtmlStr + "&nbsp;" + nf.format(Double.parseDouble(String.valueOf((splitInvoiceRow.opt(dbVar.SPLIT_AMOUNT)))))  + "</td></tr>";
                    }
                }
            }
        }
        modeOfPaymentString += tenderedChangeForInvoiceID(printInvoiceId);
        String roundOffStr = "";
        String grandTotalStyle = "topandbottomborderStyle";
        if (grandTotalWithoutRoundOff != (Double.parseDouble((String) invoiceDetails.get(0).opt("total_amt"))) && (prefs.getBoolean("roundOff", false)))
        {
            grandTotalStyle = "";
            String roundOffAmtDisplay = df.format( (Double.parseDouble((String) invoiceDetails.get(0).opt("total_amt"))) - grandTotalWithoutRoundOff );
            roundOffStr = "<tr><td align=\"left\" colspan=\"2\" ><b>RoundOff</b></td><td align=\"right\" class=\"\" colspan=\"2\" ><b>"+currencyHtmlStr+"&nbsp;"+roundOffAmtDisplay +"</b></td></tr><tr ><td align=\"left\" colspan=\"2\" class=\"topandbottomborderStyle totalAmtWithRoundoff\" ><b>Total Amount</b></td><td align=\"right\" colspan=\"2\" class=\"topandbottomborderStyle totalAmtWithRoundoff\" ><b>"+currencyHtmlStr+"&nbsp;"+(nf.format(Double.parseDouble(String.valueOf(invoiceDetails.get(0).opt("total_amt")))))+"</b></td></tr>";
        }

        String totalCalculationStr = "<tr><td colspan=\"4\" style=\"border-top:dotted 2px solid;\">&nbsp;</td> <!--  for blank row --></tr><tr><td align=\"left\" colspan=\"2\" ><b>Sub Total</b></td><td align=\"right\" colspan=\"2\" style=\"white-space:nowrap;\"><b>"+ currencyHtmlStr+"&nbsp;"+ ((nf.format(subTotal + totalBottleRefundValue)).toString()) +"</b></td></tr>"+taxCalculationStr+modeOfPaymentString+"<tr><td align=\"left\" colspan=\"2\" style=\"padding-top:5px; padding-bottom:5px;\" class=\""+grandTotalStyle+" grandTotalTag\" ><b>Grand Total</b></td><td align=\"right\" colspan=\"2\"  style=\"padding-top:5px; padding-bottom:5px;\" class=\""+grandTotalStyle+" grandTotalTag\" ><b>"+currencyHtmlStr+"&nbsp;"+calculatedGrandTotal+"</b></td></tr>"+roundOffStr+"<tr><td colspan=\"4\">&nbsp;</td> <!--  for blank row --></tr>";
        String inclusiveTaxesContent = "";
        inclusiveTaxesContent = prepareInclusiveTaxsHTML();
        String cardTransactionDetails = "";
        CardPaymentProcessingMaster cppm = new CardPaymentProcessingMaster();
        cardTransactionDetails = cppm.allCardTransactionDetailsForInvoice(printInvoiceId); // printInvoiceId
        String vouchersAppliedOnInvoice = returnVouchersAppliedOnInvoicePrintString(printInvoiceId);
        billContent = billContent + itemsListStr + totalCalculationStr + cardTransactionDetails + inclusiveTaxesContent + vouchersAppliedOnInvoice;

        if(totalDiscountGivenOnSubtotal!=0)
        {
            billContent += "<tr class=\"totalDiscountAvailedRow\"><td colspan=\"3\" align=\"center\"><b>Total Discount Availed On Sub Total</td><td align=\"right\"><b>"+ (df.format(totalDiscountGivenOnSubtotal)) +"&nbsp;"+currencyHtmlStr+"</b></td></tr>";

        }
        if(hasMRPforAllItems==true && totalSavingsOnMrp!=0)
        {
            billContent += "<tr class=\"totalSavingsOnMRPRow\"><td colspan=\"3\" align=\"center\"><b>Total Savings On MRP </td><td align=\"right\"><b>"+ (df.format(totalSavingsOnMrp)) +"&nbsp;"+currencyHtmlStr+"</b></td></tr>";

        }
        if(totalBottleRefundValue!=0)
        {
            billContent += "<tr class=\"totalBottleRefundValueRow\"><td colspan=\"3\" align=\"center\"><b>Total Refundable Deposit </td><td align=\"right\"><b>"+ currencyHtmlStr+ "&nbsp;"+ (nf.format(totalBottleRefundValue))+"</b></td></tr>";

        }

        footerEmpty += "<p><br /><br /></p>";
        printString = "<html>" + headTagContent + "<body class=\"invoiceBody androidDeviceInvoiceBody\"><table id=\"page\"  style=\"float:left;\" cellspacing=\"0\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">"+billContent+"<tr><td colspan=\"4\">"+footerEmpty+"</td></tr></table><script language=\"javascript\">" + heightFunction + " window.onload = function() { returnHeight(); };</script></body></html>";
        clearAllTaxContent();

        try{
            JSONObject orderDetails = (JSONObject) contentObj.get("orderdetails");

            String gTotal  = (orderDetails!=null && orderDetails.has("grandTotal")) ? ( (String) orderDetails.get("grandTotal")   ) : "";
            JSONObject allTaxes = (orderDetails!=null &&  orderDetails.has("allTaxes")) ?  (JSONObject) orderDetails.get("allTaxes") : (new JSONObject());
        }catch (JSONException exp){
            exp.printStackTrace();
        }

        System.out.println(printString);
        return printString;

    }


    private String returnVouchersAppliedOnInvoicePrintString(String printInvoiceId) {
        String returnVouchersAppliedStr = "";
        DecimalFormat df = new DecimalFormat("#.##");
        ArrayList<JSONObject> returnVouchersApplied = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.RETURN_VOUCHERS_TABLE+" WHERE redeemed_invoice_id='"+printInvoiceId+"'");
        if(returnVouchersApplied.size()>0)
        {
            returnVouchersAppliedStr += "<tr><td colspan=\"4\">&nbsp;</td><tr>";
            returnVouchersAppliedStr += "<tr><td style=\"border-bottom:thin solid #000;\" colspan=\"4\">Voucher Discounts Availed</td><tr>";
            for(int j=0; j < returnVouchersApplied.size(); j++)
            {
                JSONObject returnVouchersAppliedRow = returnVouchersApplied.get(j);
                String voucherId = (String) returnVouchersAppliedRow.opt(dbVar.RETURN_VOUCHER_ID);
                String availedAmount = (String) returnVouchersAppliedRow.opt("total_amt");
                returnVouchersAppliedStr += "<tr class=\"returnVoucherAvailedDetailsRow\"><td colspan=\"3\" align=\"left\">Return Voucher "+voucherId+"</td><td align=\"right\">"+(df.format(Double.parseDouble(availedAmount)))+"&nbsp;"+ConstantsAndUtilities.currencyTypehtml+"</td></tr>";
            }

            returnVouchersAppliedStr += "<tr><td colspan=\"4\">&nbsp;<br /></td><tr>";
        }
        return returnVouchersAppliedStr;
    }




    private static String prepareInclusiveTaxsHTML() {
        StringBuilder html = new StringBuilder("");
        StringBuilder totalTaxesItemshtml = new StringBuilder("");
        Double currentTaxValue = 0d;
        Double totalAmtWithoutTax = 0d;


        if (cumilativeTaxComponent.size() > 0) {

            Iterator<Map.Entry<String, Map<String, Double>>> entryIterator = cumilativeTaxComponent.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Map<String, Double>> entry = entryIterator.next();
                String taxComponent = entry.getKey();
                Map<String, Double> variations = entry.getValue();
                boolean firstRow = true;
                for (Map.Entry<String, Double> ent : variations.entrySet()) {
                    String taxRate = ent.getKey();
                    Double amount = ent.getValue();
                    amount = round(amount, 2);
                    currentTaxValue = currentTaxValue + (amount);
                    if (firstRow) {
                        firstRow = false;
                        html.append("<tr><td colspan='3'> " + taxComponent + "");
                        html.append(" @ " + taxRate + "&nbsp;% : </td><td style='text-align:right;'>" + MainActivity.currencyTypehtml + " " + round(amount, 2) + "</td></tr>");
                    } else {
                        html.append("<tr><td colspan='3'> " + taxComponent + " @ " + taxRate + " %  </td><td style='text-align:right;'>" + MainActivity.currencyTypehtml + " " + round(amount, 2) + "</td></tr>");
                    }
                }

            }
            totalAmtWithoutTax = mSubTotal - currentTaxValue;

            totalTaxesItemshtml.append("<tr><td colspan='3'>Total Price of Items Excluding Taxes</td><td style='text-align:right;'>" + round(totalAmtWithoutTax, 2) + "&nbsp;" + MainActivity.currencyTypehtml + "</td></tr>");
            totalTaxesItemshtml.append("<tr><td colspan='3'>Total Taxes Included In Items</td><td style='text-align:right;'>" + round(currentTaxValue, 2) + "&nbsp;" + MainActivity.currencyTypehtml + "</td></tr>");
            totalTaxesItemshtml.append(html);
        }
        Log.i("totalTaxesItemshtml", "totalTaxesItemshtml " + totalTaxesItemshtml);
        return totalTaxesItemshtml.toString();
    }


    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z 0-9]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll("");
        return number;
    }

    public static double round(double value, int places) {
        try {
            Double val = 0.00;
            if (!TextUtils.isEmpty(val.toString())) {
                // return Double.valueOf(df.format(val.toString()));
                return Double.parseDouble(String.format("%.2f", value));

            } else {
                return val;
            }
        } catch (Exception e) {
            return value;
        }


    }


    private void kotTrackingSaveDeletedItems(String generatedkotNum, String kotString, String voidInvoiceId, JSONObject contentObj, String generalOrCategory, String categoryName, String kotType, ArrayList<JSONObject> cancelCategoryinvoiceItemDetails) {
        //                 Date previousSyncDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(previousSyncTime);
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date oneweekbackDate  = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String mysqlDateString = formatter.format(oneweekbackDate);

        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        DatabaseVariables dbVar = new DatabaseVariables();

//        sqlCrmObj.executeDelete("kot_tracking","")
        sqlCrmObj.executeRawquery("DELETE FROM kot_tracking WHERE created_on <='"+mysqlDateString+"'");

        if(cancelCategoryinvoiceItemDetails!=null) {

            String randomKotId = Parameters.randomValue();

            ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
            try {
                JSONObject contentValues = new JSONObject();
                contentValues.put("invoice_id", voidInvoiceId);
                contentValues.put("kot_id", randomKotId);
                contentValues.put("kot_no", generatedkotNum);
                contentValues.put("device_os", "Windows");
                contentValues.put("devicetoken", dbVar.getValueForAttribute("device_token"));
                contentValues.put("kot_type", kotType);
                contentValues.put("kot_content", kotString);
                if (generalOrCategory.equals("general")) {
                    contentValues.put("category_or_general", "General");
                } else {
                    contentValues.put("category_or_general", "Category");
                }
                contentValues.put("created_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                contentValues.put("updated_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                contentValues.put("update_user_type", "Employee");
                contentValues.put("category_id", categoryName);
                contentValues.put("created_on", Parameters.currentTime());
                contentValues.put("updated_on", Parameters.currentTime());

                insertingValues.add(0, contentValues);
                sqlCrmObj.executeInsertBatch("kot_tracking", insertingValues);
                insertingValues = new ArrayList<JSONObject>();
                for (int p = 0; p < cancelCategoryinvoiceItemDetails.size(); p++) {
                    String itemName = (String) cancelCategoryinvoiceItemDetails.get(p).get("item_name");
                    String itemId = (String) cancelCategoryinvoiceItemDetails.get(p).get("item_id");
                    Double newQty = Double.valueOf((String) cancelCategoryinvoiceItemDetails.get(p).get("item_quantity"));
                    String itemNote = "Deleted - "+ (String) cancelCategoryinvoiceItemDetails.get(p).get("notes");
                    contentValues = new JSONObject();
                    int sizeOfNewItems = insertingValues.size();
                    contentValues.put("kot_id", randomKotId);
                    contentValues.put("item_name", itemName);
                    contentValues.put("item_id", itemId);
                    contentValues.put("item_quantity", df2.format(newQty));
                    contentValues.put("notes", itemNote);
                    contentValues.put("updated_by", "");
                    contentValues.put("created_on", Parameters.currentTime());
                    contentValues.put("updated_on", Parameters.currentTime());

                    insertingValues.add(sizeOfNewItems, contentValues);


                }
                if(insertingValues.size()>0)
                {
                    sqlCrmObj.executeInsertBatch("kot_tracking_items",insertingValues);
                }
//                serviceCallForPushNotification(randomKotId);
            }catch (Exception exp){
                exp.printStackTrace();
            }
        }
        // save the kot_tracking


    }


    public void voidInvoiceCancelledCategorywiseKOTSPrinting(String voidInvoiceId,JSONObject printInfoObj) {
//        kotTrackingSave();
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat df3 = new DecimalFormat("#.###");
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;


        ArrayList<JSONObject> invoiceDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            String orderTypeForCancelledInvoice = orderTypeName(    (String) invoiceDetails.get(0).optString(dbVar.ORDER_TYPE)    );
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);

            ArrayList<JSONObject> invoiceItemDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
            JSONObject categoryItems = new JSONObject();
            if(invoiceItemDetails.size()>0){
                for(int i = 0; i < invoiceItemDetails.size(); i++)
                {
                    JSONObject itemRow = invoiceItemDetails.get(i);
                    String itemCategoryId = "";
                    ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemRow.optString("item_id") + "' LIMIT 1");
                    String itemName = (String) itemRow.optString("item_name");
                    if (itemGetDetails.size() == 0) {
                        // check if it is a plu code
                        ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + itemRow.optString("item_id") + "' LIMIT 1");
                        if (plucodes.size() == 1) {
                            JSONObject itemRow2 = plucodes.get(0);
                            String itemIdForPLU = (String) itemRow2.optString(dbVar.ALTERNATE_PLU_item_no);
                            itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                        }
                    }

                    if (itemGetDetails.size() != 0) {
                        JSONObject itemRow2 = itemGetDetails.get(0);
                        String deptIdForItem = (String) itemRow2.optString(dbVar.INVENTORY_DEPARTMENT);
                        ArrayList<JSONObject> deptDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+ dbVar.DEPARTMENT_TABLE+" WHERE "+dbVar.DepartmentID+"='"+ deptIdForItem +"'");
                        if(deptDetails.size()==1){
                            itemCategoryId = (String) deptDetails.get(0).optString(dbVar.CategoryId);
                        }
                    }
                    if(!categoryItems.has(itemCategoryId)){
                        ArrayList<JSONObject> itemsRowsForCategory = new ArrayList<>();
                        itemsRowsForCategory.add(0,itemRow);
                        try {
                            categoryItems.put(itemCategoryId,itemsRowsForCategory);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
//                            int indexForItem = categoryItems.get()
                        ArrayList<JSONObject> itemsRowsForCategory = new ArrayList<>();
                        try {
                            itemsRowsForCategory = (ArrayList<JSONObject>) categoryItems.get(itemCategoryId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int indexForItem = itemsRowsForCategory.size();
                        itemsRowsForCategory.add(indexForItem,itemRow);
                        try {
                            categoryItems.put(itemCategoryId,itemsRowsForCategory);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }

            if(categoryItems.length()>0){


                ArrayList<JSONObject> allcategoryDetails = sqlObj.executeRawqueryJSON("SELECT * FROM category_details ");

                for(int j=0; j< allcategoryDetails.size();j++) {
                    String categoryUniqueId = (String) allcategoryDetails.get(j).optString("unique_id");
                    ArrayList<JSONObject> categoryprintersDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM categorywise_printing WHERE category_unique_id='"+categoryUniqueId+"'");

                    ArrayList<JSONObject> categoryDetails = sqlObj.executeRawqueryJSON("SELECT * FROM category_details WHERE unique_id='"+categoryUniqueId+"'");
                    String printerCategoryName  = "";
                    if(categoryDetails.size()==0){
                        continue;
                    }
                    else{
                        printerCategoryName = (String) categoryDetails.get(0).optString("category_id");
                    }
                    if(!categoryItems.has(printerCategoryName)){
                        continue;
                    }
                    ArrayList<JSONObject> cancelCategoryinvoiceItemDetails = null;
                    try {
                        cancelCategoryinvoiceItemDetails = (ArrayList<JSONObject>) categoryItems.get(printerCategoryName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String printerRowId = "";
                    if(categoryprintersDetails.size()!=0)
                    {
                        printerRowId = (String) categoryprintersDetails.get(0).optString("printer_row_id");
                    }
                    String selectQuery2 = "SELECT  * FROM printers where _id='" + printerRowId + "'";
                    ArrayList<JSONObject> printer1Details = sqlCrmObj.executeRawqueryJSON(selectQuery2);
                    String headlines = "", footerEmpty = "";
                    int iPaperWidth = 3;
                    String printerName = "";
                    if (printer1Details != null && printer1Details.size() > 0) {


                        for (int cntPrnt1 = 0; cntPrnt1 < printer1Details.size(); cntPrnt1++) {
                            JSONObject currentRowPrnt1 = printer1Details.get(cntPrnt1);
                            headlines = (String) currentRowPrnt1.optString("header_characters");
                            footerEmpty = (String) currentRowPrnt1.optString("footer_characters");
//                            String paperWidth = (String) currentRowPrnt1.optString("paper_width");
//                iPaperWidth = (paperWidth.equals("3 inch")) ? (570) : (380);
                            printerName = (String) currentRowPrnt1.optString("printer_name");


                        }
                    }
                    String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                            "</head>";
                    String bodyContent = "<body id=\"kotBody\">";
                    bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
                    bodyContent += "<tr><td colspan=\"4\"><h2>Cancelled KOT For Invoice #"+voidInvoiceId+" "+orderRefNum+"</h2></td></tr>";
                    bodyContent += "<tr><td colspan=\"4\"><h2>"+printerCategoryName+" - "+orderTypeForCancelledInvoice+"</h2></td></tr>";
                    bodyContent += "<tr><td>Employee</td><td>"+ Parameters.userid +"</td></tr>";
                    bodyContent += "<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (Parameters.currentTimeDayMonthFormat())+" </td></tr>";
                    bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Cancellation Status :  </td><td colspan=\"2\">"+cancellationStatus+"</td></tr>";

                    String itemsListStr ="<tr><td align=\"left\" style=\"font-size:22px\">Qty</td><td>Item</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;</td></tr> ";
                    //= "<tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice</td><td align=\"right\"><span style=\"white-space:nowrap;\">110.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 110.00 &#8377</span></td></tr><tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice Full</td><td align=\"right\"><span style=\"white-space:nowrap;\">150.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 150.00 &#8377</span></td></tr>";
                    Double subTotal = 0.0d;
                    if(cancelCategoryinvoiceItemDetails.size()>0) {
                        for (int p = 0; p < cancelCategoryinvoiceItemDetails.size(); p++) {
                            JSONObject itemRow = cancelCategoryinvoiceItemDetails.get(p);
                            itemsListStr += "<tr class=\"kotItemRow\"><td>"+(itemRow.optString(dbVar.INVOICE_QUANTITY))+"</td><td colspan='3'>"+(itemRow.optString(dbVar.INVOICE_ITEM_NAME))+"</td></tr>";
                        }
                    }
                    bodyContent += itemsListStr;
                    bodyContent += "<tr><td align=\"center\"  colspan=\"4\" style=\"font-size:22px\">***** </td></tr>";
                    bodyContent += "</table>";
                    String htmlString = "<html>"+headTagContent+bodyContent+"</html>";
                    String primaryPrinter = Parameters.primaryKOTName();
                    String localCategorywisePrinter = localCategoryWisePrinterNameForCategoryId(printerCategoryName); if(!localCategorywisePrinter.equals("")) { printerName = localCategorywisePrinter; }
                    if ( (printer1Details != null && printer1Details.size() > 0) || !printerName.equals("") ) {
//                        printHtmlToPrinter(htmlString, printerName, false);

                        saveToPrintingQueue("",htmlString,voidInvoiceId,printInfoObj,"category",printerName);

                    }
//                            kotTrackingSave("-",htmlString,voidInvoiceId, printInfoObj,"category",printerCategoryName,"Cancelled");
                    {

                        String randomKotId = ConstantsAndUtilities.randomValue();

                        ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
                        try {
                            JSONObject contentValues = new JSONObject();
                            contentValues.put("invoice_id", voidInvoiceId);
                            contentValues.put("kot_id", randomKotId);
                            contentValues.put("kot_no", "");
                            contentValues.put("device_os", "Windows");
                            contentValues.put("devicetoken", dbVar.getValueForAttribute("device_token"));
                            contentValues.put("kot_type", "Cancelled");
                            contentValues.put("kot_content", htmlString);

                            contentValues.put("category_or_general", "Category");
                            contentValues.put("created_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                            contentValues.put("updated_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                            contentValues.put("update_user_type", "Employee");
                            contentValues.put("category_id", printerCategoryName);
                            contentValues.put("created_on", Parameters.currentTime());
                            contentValues.put("updated_on", Parameters.currentTime());

                            insertingValues.add(0, contentValues);
                            sqlCrmObj.executeInsertBatch("kot_tracking", insertingValues);
                            insertingValues = new ArrayList<JSONObject>();
                            for (int p = 0; p < cancelCategoryinvoiceItemDetails.size(); p++) {
                                String itemName = (String) cancelCategoryinvoiceItemDetails.get(p).get("item_name");
                                String itemId = (String) cancelCategoryinvoiceItemDetails.get(p).get("item_id");
                                Double newQty = Double.valueOf((String) cancelCategoryinvoiceItemDetails.get(p).get("item_quantity"));
                                String itemNote = "Cancelled - "+ (String) cancelCategoryinvoiceItemDetails.get(p).get("notes");
                                contentValues = new JSONObject();
                                int sizeOfNewItems = insertingValues.size();
                                contentValues.put("kot_id", randomKotId);
                                contentValues.put("item_name", itemName);
                                contentValues.put("item_id", itemId);
                                contentValues.put("item_quantity", df2.format(newQty));
                                contentValues.put("notes", itemNote);
                                contentValues.put("updated_by", "");
                                contentValues.put("created_on", Parameters.currentTime());
                                contentValues.put("updated_on", Parameters.currentTime());

                                insertingValues.add(sizeOfNewItems, contentValues);


                            }
                            if(insertingValues.size()>0)
                            {
                                sqlCrmObj.executeInsertBatch("kot_tracking_items",insertingValues);
                            }
//                            serviceCallForPushNotification(randomKotId);
                        }catch (Exception exp){
                            exp.printStackTrace();
                        }
                    }

                }
            }
        }
    }


    public void voidInvoiceCancelledKOTPrinting(String voidInvoiceId) {
//        kotTrackingSave();
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        DatabaseHelper dbHelper = SplashScreen.dbHelper;
        ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
        ArrayList<JSONObject> invoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String orderRefNum = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_HOLD_ID);
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);
            String htmlString = "";
            String headTagContent = " <head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, width=device-width, minimum-scale=0.1, user-scalable=no\" /><style type=\"text/css\"> *{ margin:0px; padding:0px;}body { background-color:none; margin-left:5px;font-family : Arial;color:#000000;font-size:14px;} .tablehead{font-family : Arial;color:#000000;font-size:32px;} table td{font-family : Arial;color:#000000;font-size:22px; line-height:40px; padding:0px 4px;}</style>" +
                    "</head>";
            String bodyContent = "<body id=\"kotBody\">";
            bodyContent += "<table id=\"page\"  style=\"float:left;\" cellspacing=\"2\" cellpadding=\"2\"  width=\"100%\" align=\"center\" border=\"0\">";
            bodyContent += "<tr><td colspan=\"4\"><h2>Cancelled KOT For Invoice #"+voidInvoiceId+" "+orderRefNum+"</h2></td></tr>";
            bodyContent += "<tr><td>Employee</td><td>"+ dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID) +"</td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"4\" style=\"font-size:22px\">DATE/TIME:  "+ (Parameters.currentTimeDayMonthFormat())+" </td></tr>";
            bodyContent += "<tr><td align=\"left\"  colspan=\"2\" style=\"font-size:22px\">Cancellation Status :  </td><td colspan=\"2\">"+cancellationStatus+"</td></tr>";
            ArrayList<JSONObject> invoiceItemDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");

            String itemsListStr ="<tr><td align=\"left\" style=\"font-size:22px\">Qty</td><td>Item</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td align=\"right\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr> ";
            //= "<tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice</td><td align=\"right\"><span style=\"white-space:nowrap;\">110.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 110.00 &#8377</span></td></tr><tr valign='top'><td align=\"left\">1 </td> <td> Biryani Rice Full</td><td align=\"right\"><span style=\"white-space:nowrap;\">150.00 &#8377</span> </td><td align=\"right\"><span style=\"white-space:nowrap;\"> 150.00 &#8377</span></td></tr>";
            Double subTotal = 0.0d;
            if(invoiceItemDetails.size()>0) {
                for (int p = 0; p < invoiceItemDetails.size(); p++) {
                    JSONObject itemRow = invoiceItemDetails.get(p);
                    itemsListStr += "<tr class=\"kotItemRow\"><td>"+(itemRow.optString(dbVar.INVOICE_QUANTITY))+"</td><td colspan='3'>"+(itemRow.optString(dbVar.INVOICE_ITEM_NAME))+"</td></tr>";
                }
            }
            bodyContent += itemsListStr;
            bodyContent += "<tr><td align=\"center\"  colspan=\"4\" style=\"font-size:22px\">***** </td></tr>";
            bodyContent += "</table>";
            htmlString += "<html>"+headTagContent+bodyContent+"</html>";
            String primaryPrinter = Parameters.primaryKOTName();
//            printHtmlToPrinter(htmlString,primaryPrinter,false);
            saveToPrintingQueue("",htmlString,voidInvoiceId,printInfoObj,"general",primaryPrinter);

            if(invoiceItemDetails.size()>0) {
                {

                    String randomKotId = Parameters.randomValue();

                    ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
                    try {
                        JSONObject contentValues = new JSONObject();
                        contentValues.put("invoice_id", voidInvoiceId);
                        contentValues.put("kot_id", randomKotId);
                        contentValues.put("kot_no", "-");
                        contentValues.put("device_os", "Windows");
                        contentValues.put("devicetoken", dbVar.getValueForAttribute("device_token"));
                        contentValues.put("kot_type", "Cancelled");
                        contentValues.put("kot_content", htmlString);
                        contentValues.put("category_or_general", "General");
                        contentValues.put("created_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                        contentValues.put("updated_by", dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
                        contentValues.put("update_user_type", "Employee");
                        contentValues.put("category_id", "-");
                        contentValues.put("created_on", Parameters.currentTime());
                        contentValues.put("updated_on", Parameters.currentTime());

                        insertingValues.add(0, contentValues);
                        sqlCrmObj.executeInsertBatch("kot_tracking", insertingValues);
                        insertingValues = new ArrayList<JSONObject>();
                        for (int p = 0; p < invoiceItemDetails.size(); p++) {
                            String itemName = (String) invoiceItemDetails.get(p).get("item_name");
                            String itemId = (String) invoiceItemDetails.get(p).get("item_id");
                            Double newQty = Double.valueOf((String) invoiceItemDetails.get(p).get("item_quantity"));
                            String itemNote = "Deleted - " + (String) invoiceItemDetails.get(p).get("notes");
                            contentValues = new JSONObject();
                            int sizeOfNewItems = insertingValues.size();
                            contentValues.put("kot_id", randomKotId);
                            contentValues.put("item_name", itemName);
                            contentValues.put("item_id", itemId);
                            contentValues.put("item_quantity", df2.format(newQty));
                            contentValues.put("notes", itemNote);
                            contentValues.put("updated_by", "");
                            contentValues.put("created_on", Parameters.currentTime());
                            contentValues.put("updated_on", Parameters.currentTime());

                            insertingValues.add(sizeOfNewItems, contentValues);


                        }
                        if (insertingValues.size() > 0) {
                            sqlCrmObj.executeInsertBatch("kot_tracking_items", insertingValues);
                        }
//                        serviceCallForPushNotification(randomKotId);
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }
            }
        }
    }

    public void lastcustomerAccountPaymentPrint(String customerId) {

    }

    private String serviceEmployeeForUniqueId(String uniqueId) {
        String serviceEmployeeId = "";

        ArrayList<JSONObject> empServiceDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM emp_service_history WHERE unique_id='"+uniqueId+"'");
        if(empServiceDetails.size()>0)
        {
            serviceEmployeeId = "<br />Service Employee : <b>"+ (empServiceDetails.get(0).optString("service_employee_id"))+"</b>";;;
        }
        return serviceEmployeeId;
    }


    private String tenderedChangeForInvoiceID(String printInvoiceId) {
        String tenderedChange = "";
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        ArrayList<JSONObject> printInfoDetails = sqlCrmObj.executeRawqueryJSON("SELECT * FROM invoice_print_history WHERE invoice_id='"+printInvoiceId+"' AND kot_or_invoice='invoice' AND printing_type=''");
        if(printInfoDetails.size()==1)
        {
            JSONObject printRow = printInfoDetails.get(0);
            String printObj = String.valueOf(printRow.opt("print_content"));
            System.out.println("Print Obj is "+printObj);
            String currencyHtmlStr = MainActivity.currencyTypehtml;

            try {
                JSONObject printInfoObj = printRow.optJSONObject("print_content");
                if(printInfoObj.has("orderdetails"))
                {
                    JSONObject orderDetails = (JSONObject) printInfoObj.opt("orderdetails");
                    if(orderDetails.has("changeTendered"))
                    {
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMinimumFractionDigits(2);
                        nf.setMaximumFractionDigits(2);
                        String givenChange = String.valueOf(orderDetails.opt("changeTendered"));
                        String givenAmount = String.valueOf(    Double.parseDouble(String.valueOf(orderDetails.opt("grandTotal"))) + Double.parseDouble(String.valueOf(orderDetails.opt("changeTendered")))   );
                        tenderedChange = "<tr class=\"totalGivenAmount\"><td align=\"left\" colspan=\"2\" >Given Amount</td><td align=\"right\" colspan=\"2\" >" + currencyHtmlStr + "&nbsp;" + nf.format(Double.parseDouble(String.valueOf((givenAmount))))  + "</td></tr>";
                        tenderedChange += "<tr class=\"tenderedChange\"><td align=\"left\" colspan=\"2\" >Tendered Change</td><td align=\"right\" colspan=\"2\" >" + currencyHtmlStr + "&nbsp;" + nf.format(Double.parseDouble(String.valueOf((givenChange))))  + "</td></tr>";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tenderedChange;
    }
}
