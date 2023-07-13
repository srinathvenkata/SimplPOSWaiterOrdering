package com.simplpos.waiterordering.helpers;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.simplpos.waiterordering.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

public class ItemCancellationReasons {


    MySQLJDBC mySqlObj = null;
    MySQLJDBC mySqlCrmObj = null;

    private static final Random rnd = new Random();
    private static DecimalFormat df = new DecimalFormat("#.##");

    public DatabaseVariables dbVar = new DatabaseVariables();
    public ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
    public ServerSync serverSync = new ServerSync();

    private static DecimalFormat decf = new DecimalFormat("#.##");

    public ItemCancellationReasons()
    {

        mySqlObj = MainActivity.mySqlObj;
        mySqlCrmObj = MainActivity.mySqlCrmObj;
    }


    public static ArrayList<JSONObject> deletedItemsInfo = new ArrayList<>();
    public static JSONObject cancelledItemsObj = new JSONObject();
    public static String voidInvoiceReason = "";
    public void clearCancellationReasons()
    {
        deletedItemsInfo = new ArrayList<>();   cancelledItemsObj = new JSONObject(); voidInvoiceReason = "";
    }
    public boolean voidedInvoiceReducedQuantities(String formData)
    {

        boolean deletionResult = false;
        try {
//            JSONParser parser = new JSONParser();
            JSONObject obj = new JSONObject(formData);
            String deletedItemUniueIds = (String) obj.get("deletedItemsUniqueIdsFromSavedInvoice");

            JSONArray itemIds = (JSONArray) obj.get("itemIds");
            JSONArray discountInCurrency = (JSONArray) obj.get("discountInCurrency");

            JSONArray itemNames = (JSONArray) obj.get("itemNames");
            JSONArray itemNotes = (JSONArray) obj.get("itemNotes");
            JSONArray savedOlderUniqueIds = (JSONArray) obj.get("savedOlderUniqueIds");
            JSONArray totalItemPrices = (JSONArray) obj.get("totalItemPrices");
            JSONArray itemQuantitys = (JSONArray) obj.get("itemQuantitys");
            JSONArray savedQuantities = (JSONArray) obj.get("savedQuantities");
            System.out.println("Deleted Items are "+deletedItemUniueIds);
            ArrayList<JSONObject> deletedItemInfo = new ArrayList<>();
            if(deletedItemUniueIds.contains(",")){

                String[] individualDeletedItems = deletedItemUniueIds.split(",");
                ArrayList<String> deleteUniqueIds = new ArrayList<String >();
                for(int pp = 0 ; pp < individualDeletedItems.length ; pp++){
                    ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+individualDeletedItems[pp]+"'");
                    if(itemDel.size()==1){
                        JSONObject deletedItemObj = itemDel.get(0);
                        cancelledItemsObj.put(individualDeletedItems[pp],deletedItemObj);
                        deletedItemsInfo.add(deletedItemObj);
//                        int oldRowCount = itemsDeleted.size();
//                        itemsDeleted.add(oldRowCount,itemDel.get(0));
                    }
                }
            }else if(!deletedItemUniueIds.equals("")){
                ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+deletedItemUniueIds+"'");
                if(itemDel.size()!=0){  deletedItemsInfo.add(itemDel.get(0)); cancelledItemsObj.put(deletedItemUniueIds,itemDel.get(0)); }
            }

            for (int p = 0; p < itemIds.length(); p++) {
                String itemId = (String) itemIds.get(p);
                String savedQty = "0";
                if(     (savedQuantities.get(p) != null) && (!savedQuantities.get(p).equals(""))   )
                {
                    savedQty = (String) savedQuantities.get(p);

                    String olderUniqueId = String.valueOf(savedOlderUniqueIds.get(p));
                    ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+olderUniqueId+"'");
                    if(itemDel.size()!=0){
                        JSONObject delItemObj = itemDel.get(0);
                        delItemObj.put(dbVar.INVOICE_QUANTITY,String.valueOf(savedQty));
                        deletedItemsInfo.add(delItemObj);
                        cancelledItemsObj.put(olderUniqueId,delItemObj);
                    }
                }

            }
        }catch (Exception exp){
            exp.printStackTrace();
        }finally {
            System.out.println(deletedItemsInfo.toString());
            if(deletedItemsInfo.size()>0)
            {
                deletionResult = true;
            }
        }
        return deletionResult;
    }
    public Boolean CartHasDeletedItemsOrReducedQuantities(String formData)
    {
        Boolean deletionResult = false;
        try {
            JSONObject obj = new JSONObject(formData);
            String deletedItemUniueIds = (String) obj.get("deletedItemsUniqueIdsFromSavedInvoice");

            JSONArray itemIds = (JSONArray) obj.get("itemIds");
            JSONArray discountInCurrency = (JSONArray) obj.get("discountInCurrency");

            JSONArray itemNames = (JSONArray) obj.get("itemNames");
            JSONArray itemNotes = (JSONArray) obj.get("itemNotes");
            JSONArray savedOlderUniqueIds = (JSONArray) obj.get("savedOlderUniqueIds");
            JSONArray totalItemPrices = (JSONArray) obj.get("totalItemPrices");
            JSONArray itemQuantitys = (JSONArray) obj.get("itemQuantitys");
            JSONArray savedQuantities = (JSONArray) obj.get("savedQuantities");
            System.out.println("Deleted Items are "+deletedItemUniueIds);
            ArrayList<JSONObject> deletedItemInfo = new ArrayList<>();
            if(deletedItemUniueIds.contains(",")){

                String[] individualDeletedItems = deletedItemUniueIds.split(",");
                ArrayList<String> deleteUniqueIds = new ArrayList<String >();
                for(int pp = 0 ; pp < individualDeletedItems.length ; pp++){
                    ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+individualDeletedItems[pp]+"'");
                    if(itemDel.size()==1){
                        JSONObject deletedItemObj = itemDel.get(0);
                        cancelledItemsObj.put(individualDeletedItems[pp],deletedItemObj);
                        deletedItemsInfo.add(deletedItemObj);
//                        int oldRowCount = itemsDeleted.size();
//                        itemsDeleted.add(oldRowCount,itemDel.get(0));
                    }
                }
            }else if(!deletedItemUniueIds.equals("")){
                ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+deletedItemUniueIds+"'");
                if(itemDel.size()!=0){  deletedItemsInfo.add(itemDel.get(0)); cancelledItemsObj.put(deletedItemUniueIds,itemDel.get(0)); }
            }

            for (int p = 0; p < itemIds.length(); p++) {
                String itemId = (String) itemIds.get(p);
                String savedQty = "0";
                if(     (savedQuantities.get(p) != null) && (!savedQuantities.get(p).equals(""))   )
                {
                    savedQty = (String) savedQuantities.get(p);
                }
                if (Double.parseDouble(savedQty) != Double.parseDouble((String) itemQuantitys.get(p))) {
                    Double newQty = Double.parseDouble((String) itemQuantitys.get(p)) - Double.parseDouble(savedQty);

                    String itemName = (String) itemNames.get(p);
                    System.out.println("Item quantity is less for "+itemName+" ,Saved quantity is "+savedQty+" and item quantity is "+ ((String) itemQuantitys.get(p)) );
                    String itemNote = (!itemNotes.get(p).equals("")) ? (" - " + itemNotes.get(p)) : "";

                    if(newQty < 0)
                    {

                        Double newQtyCheck = Math.abs(newQty);
                        String olderUniqueId = String.valueOf(savedOlderUniqueIds.get(p));
                        ArrayList<JSONObject> itemDel = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+olderUniqueId+"'");
                        if(itemDel.size()!=0){
                            JSONObject delItemObj = itemDel.get(0);
                            delItemObj.put(dbVar.INVOICE_QUANTITY,String.valueOf(newQtyCheck));
                            deletedItemsInfo.add(delItemObj);
                            cancelledItemsObj.put(olderUniqueId,delItemObj);
                        }

//                        itemNote += ( " - <span class=\"cancelledItemSpan\"><b> Cancelled "+ df.format(Double.parseDouble(savedQty)+"</b></span>");
                    }

                }

            }
        }catch (Exception exp){
            exp.printStackTrace();
        }finally {
            System.out.println(deletedItemsInfo.toString());
            if(deletedItemsInfo.size()>0)
            {
                deletionResult = true;
            }
        }
        return deletionResult;
    }

    public void saveReasonsForCancellation(String formData)
    {
        try {
            System.out.println("Form data is "+formData);
            JSONObject obj = new JSONObject(formData);
            JSONArray savedOlderUniqueIds = (JSONArray) obj.get("saveduniqueids");
            JSONArray savedReasons = (JSONArray) obj.get("reasons");
            voidInvoiceReason = (String) obj.get("voidinvoicereason");
            System.out.println("Void invoice reason is "+voidInvoiceReason);
            for(int i=0; i < savedOlderUniqueIds.length(); i++)
            {

                String olderUniqueId = String.valueOf(savedOlderUniqueIds.get(i));
                String reason = String.valueOf(savedReasons.get(i));
                JSONObject olderJSONOBJForDeletedItem = (JSONObject) cancelledItemsObj.get(olderUniqueId);
                olderJSONOBJForDeletedItem.put("reason",reason);
                cancelledItemsObj.put(olderUniqueId,olderJSONOBJForDeletedItem);
            }

            dbVar.replaceAttributesWithValues("deleted_items_info",deletedItemsInfo.toString());
            dbVar.replaceAttributesWithValues("cancelled_items_obj",cancelledItemsObj.toString());
            dbVar.replaceAttributesWithValues("void_invoice_reason",voidInvoiceReason);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String cancellationTableBodyStr()
    {
        String cancellationTableBodyStr="";
        DecimalFormat df = new DecimalFormat("#.##");
        for(int i=0; i < deletedItemsInfo.size();i++)
        {
            JSONObject deletedItem = deletedItemsInfo.get(i);
            String itemName = String.valueOf(deletedItem.optString(dbVar.INVOICE_ITEM_NAME));
            String itemQty = String.valueOf(deletedItem.optString(dbVar.INVOICE_QUANTITY));
            String itemUniqueId = String.valueOf(deletedItem.optString(dbVar.UNIQUE_ID));
            cancellationTableBodyStr += "<tr><td>"+ (String.valueOf(i+1))+"</td><td>"+itemName+"</td><td>"+df.format(Double.parseDouble(itemQty))+"</td><td><input type=\"text\" class=\"form-control cancelleditemidreason\" data-cancelleditemid=\""+ itemUniqueId+ "\" name=\"cancelleditemidreason["+ itemUniqueId+"]\" /></td></tr>";
        }
        return cancellationTableBodyStr;
    }
    public String cancellationReasonForInvoice()
    {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        Boolean reasonForCancellationPrompt = prefs.getBoolean(ConstantsAndUtilities.PROMPT_REASON_FOR_CANCELLATION,false);
        if(reasonForCancellationPrompt==false){ return "";}
        else{
            return dbVar.getValueForAttribute("void_invoice_reason");
        }
    }
    public String cancellationReasonForUniqueId(String oldUniqueId) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        Boolean reasonForCancellationPrompt = prefs.getBoolean(ConstantsAndUtilities.PROMPT_REASON_FOR_CANCELLATION,false);
        if(reasonForCancellationPrompt==false){ return "";}
        Log.v("Cancellation","Old cancellation details are "+dbVar.getValueForAttribute("cancelled_items_obj"));
        JSONObject oldItemCancellationDetails = null;// (JSONObject) ItemCancellationReasons.cancelledItemsObj.opt(oldUniqueId);
        try {
            JSONObject totalCancellationJSON = new JSONObject(dbVar.getValueForAttribute("cancelled_items_obj"));
            oldItemCancellationDetails = (JSONObject) totalCancellationJSON.opt(oldUniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        Log.v("Cancellation",oldItemCancellationDetails.toString());
        String CancellationReason = "";
        if(oldItemCancellationDetails.has("reason")){
            CancellationReason = oldItemCancellationDetails.optString("reason");
        };
        CancellationReason = (!CancellationReason.equals("")) ? (" - Reason For Cancellation - "+CancellationReason) : (" - Reason For Cancellation Not Mentioned");
        return CancellationReason;
    }
    public String cancellationReasonForVoidInvoice(String voidInvoiceId) {
        String reasonForVoidingInvoice = "";
        ArrayList<JSONObject> invoiceDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidInvoiceId + "' ");
        if (invoiceDetails.size() == 1) {
            String cancellationStatus = (String) invoiceDetails.get(0).optString(dbVar.INVOICE_DELIVERY_STATUS);
            System.out.println("Complete delivery status is "+cancellationStatus);
            if (cancellationStatus.contains("Reason for cancellation - ")) {
                String[] reasonSplit = cancellationStatus.split("Reason for cancellation - ");
                System.out.println(reasonSplit);
                reasonForVoidingInvoice = reasonSplit[1];
            }
        }
        return reasonForVoidingInvoice;
    }

    public String cancellationReasonForItemWithUniqueId(String itemRowUniqueId) {
        String reasonForCancellingTheItem = "";
        ArrayList<JSONObject> invoiceItemDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE (unique_id='" + itemRowUniqueId + "' OR item_desscription='"+itemRowUniqueId+"') AND status='deleted'  ORDER BY modified_timestamp DESC LIMIT 1");
        Log.v("reasonforcancellation","SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE (unique_id='" + itemRowUniqueId + "' OR item_desscription='"+itemRowUniqueId+"') AND status='deleted'  ORDER BY modified_timestamp DESC LIMIT 1");
        Log.v("reasonforcancellation",invoiceItemDetails.toString());
        if (invoiceItemDetails.size() == 1) {
            String cancellationStatus = (String) invoiceItemDetails.get(0).optString(dbVar.INVOICE_ITEMS_NOTES);
            if (cancellationStatus.contains("Reason For Cancellation -")) {
                String[] reasonSplit = cancellationStatus.split("Reason For Cancellation -");
                System.out.println(reasonSplit);
                reasonForCancellingTheItem = " - Reason For Cancellation - "+reasonSplit[1];
            }
        }
        return reasonForCancellingTheItem;
    }
}
