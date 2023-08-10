package com.simplpos.waiterordering.helpers;

import static com.simplpos.waiterordering.helpers.ConstantsAndUtilities.getDoubleValueof;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.POSWebActivity;
import com.simplpos.waiterordering.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;
public class SaveFetchVoidInvoice {
    public static JSONObject saveItems = new JSONObject();
    private static boolean tableMigration = false;

    public MySQLJDBC sqlObj = MainActivity.mySqlObj;
    public MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
    public MySQLJDBC LanServersqlCrmObj = sqlCrmObj; //mysqlObj.LanServersqlCrmObj;
    public static DatabaseVariables dbVar = new DatabaseVariables();
    public ConstantsAndUtilities Parameters = new ConstantsAndUtilities();

    private static DecimalFormat df = new DecimalFormat("#.##");
    
    public static Boolean voidTheInvoice(String invoiceIdForVoid){
        DatabaseVariables dbVar = new DatabaseVariables();
        ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ DatabaseVariables.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");
        String invoiceDeliveryStatus = "After Completion";
        if(InvoiceDetails.size()==1){
            String timeC = ConstantsAndUtilities.currentTime();

            JSONObject invoiceRow = InvoiceDetails.get(0);
            String bookedTableUniqueId = invoiceRow.optString(DatabaseVariables.INVOICE_BOOKED_TABLE);

            JSONObject contentValues = new JSONObject();
            try {
                contentValues.put(DatabaseVariables.INVOICE_ID, invoiceIdForVoid);

            contentValues.put(dbVar.INVOICE_STORE_ID, invoiceRow.optString(dbVar.INVOICE_STORE_ID));
            contentValues.put(dbVar.INVOICE_TOTAL_AMT, invoiceRow.optString(dbVar.INVOICE_TOTAL_AMT));
            contentValues.put(dbVar.INVOICE_STATUS, "void");
            contentValues.put(dbVar.ORDER_TYPE,invoiceRow.optString(dbVar.ORDER_TYPE));
            if(invoiceRow.optString(dbVar.INVOICE_HOLD_ID).equals("hold")){
                invoiceDeliveryStatus = "Before Completion";
            }
            contentValues.put(dbVar.INVOICE_HOLD_ID,invoiceRow.optString(dbVar.INVOICE_HOLD_ID));
            contentValues.put(dbVar.INVOICE_PAYMENT_TYPE,invoiceRow.optString(dbVar.INVOICE_PAYMENT_TYPE));
            contentValues.put(dbVar.INVOICE_TOTAL_AVG,invoiceRow.optString(dbVar.INVOICE_TOTAL_AVG));
            contentValues.put(dbVar.UNIQUE_ID, invoiceRow.optString(dbVar.UNIQUE_ID));
            contentValues.put(dbVar.INVOICE_PROFIT,"0");
            contentValues.put(dbVar.INVOICE_BILL_TS,invoiceRow.optString(dbVar.INVOICE_BILL_TS));
            contentValues.put(dbVar.CREATED_DATE,invoiceRow.optString(dbVar.CREATED_DATE));
            contentValues.put(dbVar.MODIFIED_DATE,timeC);
            contentValues.put(dbVar.INVOICE_DELIVERY_DATE,invoiceRow.optString(dbVar.INVOICE_DELIVERY_DATE));
            contentValues.put(dbVar.INVOICE_EMPLOYEE,dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));

            contentValues.put(dbVar.INVOICE_CHEQUE_NO,invoiceRow.optString(dbVar.INVOICE_CHEQUE_NO));

                ItemCancellationReasons icr = new ItemCancellationReasons();
                String CancellationReason = icr.cancellationReasonForInvoice();
                contentValues.put(dbVar.INVOICE_DELIVERY_STATUS,invoiceDeliveryStatus);
                if(!CancellationReason.equals(""))
                {
                    contentValues.put(dbVar.INVOICE_DELIVERY_STATUS,invoiceDeliveryStatus+" - Reason for cancellation - "+CancellationReason);
                }
            contentValues.put(dbVar.INVOICE_CUSTOMER,invoiceRow.optString(dbVar.INVOICE_CUSTOMER));
            String customerId = invoiceRow.optString(dbVar.INVOICE_CUSTOMER);
            String here = dbVar.UNIQUE_ID + "=?";
            String uniq = invoiceRow.optString(dbVar.UNIQUE_ID);
            dbVar.executeUpdateToDB(
                    dbVar.INVOICE_TOTAL_TABLE, contentValues,
                    dbVar.UNIQUE_ID, uniq);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String updateInvoiceItemsQuery = "UPDATE "+ dbVar.INVOICE_ITEMS_TABLE+" SET status='void',"+dbVar.MODIFIED_DATE+"='"+timeC+"' WHERE "+dbVar.INVOICE_ID+"='"+invoiceIdForVoid+"'";
            dbVar.executeExecSQL(updateInvoiceItemsQuery);

            Log.v("Void","Update items query is "+updateInvoiceItemsQuery);

            InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");
            ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");
            ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");
            ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");

            ArrayList<JSONObject> bookedTableDetails = new ArrayList<>();
            if(!bookedTableUniqueId.equals("")){
                updateInvoiceItemsQuery = "UPDATE " + dbVar.TABLE_SETTINGS_TABLE + " SET occupancy_status='Not Occupied'," + dbVar.MODIFIED_DATE + "='" + timeC + "',associated_invoice_id='' WHERE " + dbVar.UNIQUE_ID + "='" + bookedTableUniqueId + "'";
                dbVar.executeExecSQL(updateInvoiceItemsQuery);
                bookedTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TABLE_SETTINGS_TABLE + " WHERE unique_id='" + bookedTableUniqueId + "'");

            }

            JSONObject subarr = new JSONObject();
            try {
                deleteOldPendingQueriesRelatedtoInvoice(invoiceIdForVoid);
                subarr.put("completeinvoiceDetails",InvoiceDetails);

                subarr.put("invoiceitemstotal",InvoiceItemDetails);
                subarr.put("invoicetaxdetails",InvoiceTaxDetails);
                subarr.put("categorytaxes",InvoiceCategoryTaxDetails);
                subarr.put("bookedtable",bookedTableDetails);
                dbVar.sendToServerServiceData(subarr);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(!invoiceRow.optString(dbVar.INVOICE_CUSTOMER).equals("")){

                System.out.println("Customer is not blank");
                PromotionsValidation PVV = new PromotionsValidation();
//                PVV.revertBackRewardPoints(invoiceIdForVoid);
                System.out.println("Revert back account payments is called");
                PVV.revertBackAccountPayment(invoiceIdForVoid);
            }
        }
        return true;
    }
    public static void deleteOldPendingQueriesRelatedtoInvoice(String invoiceId){
        String oldRowsQuery = "DELETE FROM "+dbVar.PENDING_QUERIES_TABLE+" WHERE pending_data LIKE '%completeinvoiceDetails%' AND pending_data LIKE '%"+invoiceId+"%'";
        Log.v("DeleteQueries",oldRowsQuery);
        MainActivity.mySqlCrmObj.executeRawquery(oldRowsQuery);
    }
    public static void finalizeOldInvoice(String InvoiceNumber,JSONObject obj) {


        ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");

        if(InvoiceDetails.size()==1) {
            JSONObject invoiceRow = InvoiceDetails.get(0);
            String timeC = ConstantsAndUtilities.currentTime();
            String customerId = obj.optString("customerIdForInvoice");

            String bookedTableUniqueId = invoiceRow.optString(dbVar.INVOICE_BOOKED_TABLE);

            JSONObject contentValues = new JSONObject();
            String paymentType = "Cash";
            try {
                contentValues.put(dbVar.INVOICE_ID, InvoiceNumber);

            contentValues.put(dbVar.INVOICE_STORE_ID, obj.optString("storeIdForInvoice"));
            contentValues.put(dbVar.INVOICE_TOTAL_AMT, obj.optString("grandTotal"));
            contentValues.put(dbVar.INVOICE_STATUS, "complete");
            contentValues.put(dbVar.ORDER_TYPE,POSWebActivity.selectedOrderType);
            contentValues.put(dbVar.INVOICE_HOLD_ID,invoiceRow.optString(dbVar.INVOICE_HOLD_ID));
            String modeOfPayment2 = obj.optString("modeOfPayment2");
            if(!modeOfPayment2.equals("")){ paymentType="multiple"; }else{ paymentType = obj.optString("modeOfPayment1");}
            contentValues.put(dbVar.INVOICE_PAYMENT_TYPE,paymentType);
            contentValues.put(dbVar.INVOICE_TOTAL_AVG,obj.optString("grandTotal"));
            contentValues.put(dbVar.UNIQUE_ID, invoiceRow.optString(dbVar.UNIQUE_ID));
            contentValues.put(dbVar.INVOICE_PROFIT,"0");
            contentValues.put(dbVar.INVOICE_BILL_TS,timeC);
            contentValues.put(dbVar.CREATED_DATE,invoiceRow.optString(dbVar.CREATED_DATE));
            contentValues.put(dbVar.MODIFIED_DATE,timeC);
            String orderDeliveryDate = obj.optString("orderDeliveryDate");
            if(orderDeliveryDate.equals("")){ orderDeliveryDate = timeC;}
            contentValues.put(dbVar.INVOICE_DELIVERY_DATE,orderDeliveryDate);
            contentValues.put(dbVar.INVOICE_EMPLOYEE,dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
            String paymentRefNo = "";
            if(!paymentType.equals("multiple")){
                paymentRefNo = obj.optString("paymentRefNo1");
            }
            contentValues.put(dbVar.INVOICE_CHEQUE_NO,paymentRefNo);
            contentValues.put(dbVar.INVOICE_DELIVERY_STATUS,"Delivered");
            contentValues.put(dbVar.INVOICE_CUSTOMER,customerId);



            String here = dbVar.UNIQUE_ID + "";
            String uniq = invoiceRow.optString(dbVar.UNIQUE_ID);
            dbVar.executeUpdateToDB(dbVar.INVOICE_TOTAL_TABLE, contentValues,here, uniq);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Update table booking to Not Occupied

            if(paymentType.equals("Account"))
            {

                Double gt =  Double.parseDouble(obj.optString("grandTotal"));
                CustomersManager cm = new CustomersManager();
                cm.customerAccountPaymentDebit(gt,InvoiceNumber,customerId);
            }
            // insert multiple mode payments
            if(paymentType.equals("multiple")){
                ArrayList<JSONObject> splitPaymentsBulk = new ArrayList<>();
                for(int v=1;v<=4;v++){
                    String paymentTypeStr = "paymentType"+v;
                    String modeOfPaymentStr = "modeOfPayment"+v;
                    String paymentRefNoStr = "paymentRefNo"+v;
                    String paymentTypeMultiple = obj.optString(paymentTypeStr);
                    if(!paymentTypeMultiple.equals("")){
                        JSONObject contentTaxValues = new JSONObject();
                        try {
                            contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);

                        contentTaxValues.put(dbVar.UNIQUE_ID,ConstantsAndUtilities.randomValue());
                        contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                        contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                        contentTaxValues.put("server_local","local");
                        contentTaxValues.put("cheque_no",obj.optString(paymentRefNoStr));
                        contentTaxValues.put("payment_type",obj.optString(modeOfPaymentStr));
                        contentTaxValues.put("amount",obj.optString(paymentTypeStr));
                        contentTaxValues.put(dbVar.SPLIT_ACCOUNT_NO,"");
//                        dbVar.executeInsertToDB(dbVar.SPLIT_INVOICE_TABLE, contentTaxValues);
                            splitPaymentsBulk.add(contentTaxValues);

                            if(obj.optString(modeOfPaymentStr).equals("Account"))
                            {
                                CustomersManager cm2 = new CustomersManager();
                                cm2.customerAccountPaymentDebit(Double.parseDouble(String.valueOf(obj.optString(paymentTypeStr))),InvoiceNumber,customerId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(splitPaymentsBulk.size()>0)
                {
                    dbVar.executeInsertBatch(dbVar.SPLIT_INVOICE_TABLE,splitPaymentsBulk);
                }
            }

            JSONObject olderUniqueIdsMapJSON = new JSONObject();
            String allUniqueIdsString = "";

            ArrayList<JSONObject> invoiceItemsOld = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE invoice_id='"+InvoiceNumber + "'");
            // merge all olderunique ids
            if(invoiceItemsOld.size()>0)
            {
                for(int j=0; j < invoiceItemsOld.size(); j++)
                {
                    JSONObject invoiceItemRowOld = invoiceItemsOld.get(j);
                    String olderUniqueId  = invoiceItemRowOld.optString(dbVar.UNIQUE_ID);
                    allUniqueIdsString += "'"+olderUniqueId+"',";
                    try {
                        olderUniqueIdsMapJSON.put(olderUniqueId,invoiceItemRowOld);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            Log.v("Testing",olderUniqueIdsMapJSON.toString());

            JSONArray itemIds = obj.optJSONArray("itemIds");
            JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
            JSONArray itemNames = obj.optJSONArray("itemNames");
            JSONArray itemNotes = obj.optJSONArray("itemNotes");
            JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
            JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
            JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
            String holdInvoiceId = obj.optString("holdInvoiceId");

            String deletedItemsList = obj.optString("deletedItemsUniqueIdsFromSavedInvoice");
            Log.v("HoldInvoice",deletedItemsList);
            if(deletedItemsList.contains(",")){
                String[] individualDeletedItems = deletedItemsList.split(",");
                for(int pp = 0 ; pp < individualDeletedItems.length ; pp++){
                    String where = dbVar.UNIQUE_ID + "";
                    dbVar.executeDeleteInDB(dbVar.INVOICE_ITEMS_TABLE,
                            where, individualDeletedItems[pp]);
                }
            }else if(!deletedItemsList.equals("")){
                String where = dbVar.UNIQUE_ID + "=?";
                dbVar.executeDeleteInDB(dbVar.INVOICE_ITEMS_TABLE,
                        dbVar.UNIQUE_ID, deletedItemsList );
            }
            // Update the items
            try {

                ArrayList<JSONObject> bulkSaveNewItems = new ArrayList<>();
                for (int k = 0; k < itemIds.length(); k++) {
                    String insertItemid = null;
                    insertItemid = itemIds.getString(k);
                    String olderUniqueId = savedOlderUniqueIds.getString(k);
                    if(olderUniqueId.equals("")){

                        String discount = discountInCurrency.getString(k);
                        JSONObject contentItemValues = new JSONObject();
                        contentItemValues.put(dbVar.INVOICE_ITEM_ID, insertItemid);
                        contentItemValues.put(dbVar.INVOICE_DISCOUNT, discount);
                        contentItemValues.put(dbVar.INVOICE_TAX, "0");
                        contentItemValues.put(dbVar.INVOICE_ITEM_NAME, itemNames.getString(k));
                        contentItemValues.put(dbVar.INVOICE_YOUR_COST, totalItemPrices.getString(k));
                        double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                        contentItemValues.put(dbVar.INVOICE_AVG_COST, df.format(itemUnitPrice));
                        contentItemValues.put(dbVar.INVOICE_QUANTITY, itemQuantitys.getString(k));
                        contentItemValues.put(DatabaseVariables.INVOICE_ID, InvoiceNumber);
                        contentItemValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                        timeC = ConstantsAndUtilities.currentTime();
                        contentItemValues.put(dbVar.CREATED_DATE, timeC);
                        contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                        contentItemValues.put("payment_type", paymentType);
                        String deptIdForItem = "";

                        ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + insertItemid + "' LIMIT 1");
                        if (itemGetDetails.size() == 0) {
                            // check if it is a plu code
                            ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + insertItemid + "' LIMIT 1");
                            if (plucodes.size() == 1) {
                                JSONObject itemRow = plucodes.get(0);
                                String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                                itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                            }
                        }

                        if (itemGetDetails.size() != 0) {
                            JSONObject itemRow = itemGetDetails.get(0);
                            deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                        }
                        contentItemValues.put("in_department", deptIdForItem);
                        contentItemValues.put("store_id", obj.optString("storeIdForInvoice"));
                        contentItemValues.put("status", "complete");
                        contentItemValues.put("hold_status", "Completed");
                        contentItemValues.put("notes", itemNotes.getString(k));
//                        dbVar.executeInsertToDB(dbVar.INVOICE_ITEMS_TABLE, contentItemValues);
                        bulkSaveNewItems.add(contentItemValues);
                    }else{
                        JSONObject olderRecord = olderUniqueIdsMapJSON.getJSONObject(olderUniqueId);
                        String oldRecordPrice = olderRecord.optString(dbVar.INVOICE_YOUR_COST);
                        String oldRecordQty =  olderRecord.optString(dbVar.INVOICE_QUANTITY);
                        String oldRecordName =  olderRecord.optString(dbVar.INVOICE_ITEM_NAME);
                        // map json object by unique id
                        // update the new record only if quantity or price are different
                        if(!oldRecordPrice.equals(totalItemPrices.getString(k)) || !oldRecordQty.equals(itemQuantitys.getString(k))  || !oldRecordName.equals(itemNames.getString(k)) )
                        {
// add push to delete queries

                            Log.v("Hold Invoice", "Update this record for " + (itemNames.getString(k)) + " and item id " + insertItemid + " and unique id " + olderUniqueId);
                            ArrayList<JSONObject> InvoiceItemDetails = new ArrayList<>();//dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE unique_id='" + olderUniqueId + "'");
                            InvoiceItemDetails.add(olderRecord);

                            if (InvoiceItemDetails.size() == 1) {
                                JSONObject invoiceItemRow = InvoiceItemDetails.get(0);
                                String discount = discountInCurrency.getString(k);
                                JSONObject contentItemValues = new JSONObject();

                                contentItemValues.put(dbVar.INVOICE_ITEM_ID, invoiceItemRow.optString(dbVar.INVOICE_ITEM_ID));
                                contentItemValues.put(dbVar.INVOICE_DISCOUNT, discount);
                                contentItemValues.put(dbVar.INVOICE_TAX, "0");
                                contentItemValues.put(dbVar.INVOICE_ITEM_NAME, itemNames.getString(k));
                                contentItemValues.put(dbVar.INVOICE_YOUR_COST, totalItemPrices.getString(k));
                                double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                                contentItemValues.put(dbVar.INVOICE_AVG_COST, df.format(itemUnitPrice));
                                contentItemValues.put(dbVar.INVOICE_QUANTITY, itemQuantitys.getString(k));
                                contentItemValues.put(dbVar.INVOICE_ID, InvoiceNumber);
                                contentItemValues.put(dbVar.UNIQUE_ID, invoiceItemRow.optString(dbVar.UNIQUE_ID));
                                timeC = ConstantsAndUtilities.currentTime();
                                contentItemValues.put(dbVar.CREATED_DATE, invoiceItemRow.optString(dbVar.CREATED_DATE));
                                contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                                contentItemValues.put("payment_type", paymentType);
                                String deptIdForItem = "";

                                ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + insertItemid + "' LIMIT 1");
                                if (itemGetDetails.size() == 0) {
                                    // check if it is a plu code
                                    ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + insertItemid + "' LIMIT 1");
                                    if (plucodes.size() == 1) {
                                        JSONObject itemRow = plucodes.get(0);
                                        String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                                        itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                                    }
                                }

                                if (itemGetDetails.size() != 0) {
                                    JSONObject itemRow = itemGetDetails.get(0);
                                    deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                                }
                                contentItemValues.put("in_department", deptIdForItem);
                                contentItemValues.put("store_id", obj.optString("storeIdForInvoice"));
                                contentItemValues.put("status", "complete");
                                contentItemValues.put("hold_status", "Completed");
                                contentItemValues.put("notes", itemNotes.getString(k));
//                            here = dbVar.UNIQUE_ID + "=?";
//                            uniq = invoiceItemRow.optString(dbVar.UNIQUE_ID);
                                dbVar.executeUpdateToDB(
                                        dbVar.INVOICE_ITEMS_TABLE, contentItemValues,
                                        dbVar.UNIQUE_ID, invoiceItemRow.optString(dbVar.UNIQUE_ID));

                                if (Double.parseDouble((String) itemQuantitys.get(k)) < Double.parseDouble((String) invoiceItemRow.get(dbVar.INVOICE_QUANTITY))) {
                                    Double diffQty = (Double.parseDouble((String) invoiceItemRow.get(dbVar.INVOICE_QUANTITY)) - Double.parseDouble((String) itemQuantitys.get(k)));
                                    JSONObject duplicateItemRow = invoiceItemRow;
                                    duplicateItemRow.put("item_desscription",invoiceItemRow.optString(dbVar.UNIQUE_ID));
                                    reducedQtypushToDeleteQueriesAndSendToServer(duplicateItemRow, (diffQty));
                                    String newRandomString = ConstantsAndUtilities.randomValue();
                                }
                            }
                        }
                    }



                }
                if(bulkSaveNewItems.size()>0)
                {
                    dbVar.executeInsertBatch(dbVar.INVOICE_ITEMS_TABLE,bulkSaveNewItems);
                }
                JSONObject allTaxes = (obj.getJSONObject("allTaxes"));
                JSONArray categoryTaxes = allTaxes.getJSONArray("categorywise");
                JSONArray overallTaxes =  allTaxes.getJSONArray("overall");

                String where = dbVar.INVOICE_ID + "";
                dbVar.executeDeleteInDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE,
                        where, InvoiceNumber);
                dbVar.executeDeleteInDB(dbVar.TAX_AMOUNT_TABLE,
                        where, InvoiceNumber);
                timeC = ConstantsAndUtilities.currentTime();
                for(int p=0; p < categoryTaxes.length();p++){
                    JSONObject taxRow = categoryTaxes.optJSONObject(p);
                    if(Double.valueOf(taxRow.optString("tax_calculated_value"))==0){continue;}
                    JSONObject contentTaxValues = new JSONObject();
                    contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                    contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+"_"+taxRow.optString("category")+"_"+taxRow.optString("tax_name"));
                    contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                    contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                    contentTaxValues.put("server_local","local");
                    contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                    contentTaxValues.put("category_id",taxRow.optString("category"));
                    contentTaxValues.put("tax_value",taxRow.optString("tax_calculated_value"));
                    dbVar.executeInsertToDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE, contentTaxValues);

                }
                // insert overall taxes
                for(int p=0; p < overallTaxes.length();p++){
                    JSONObject taxRow = overallTaxes.optJSONObject(p);
                    JSONObject contentTaxValues = new JSONObject();
                    contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                    contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+taxRow.optString("tax_name"));
                    contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                    contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                    contentTaxValues.put("server_local","local");
                    contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                    contentTaxValues.put("tax_value",taxRow.optString("tax_value"));
                    dbVar.executeInsertToDB(dbVar.TAX_AMOUNT_TABLE,  contentTaxValues);


                }
            }catch (JSONException exp){

            }finally {
                InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> splitInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + InvoiceNumber  + "'");

                ArrayList<JSONObject> bookedTableDetails = new ArrayList<>();
                if(!bookedTableUniqueId.equals("")){
                    String updateInvoiceItemsQuery = "UPDATE "+ dbVar.TABLE_SETTINGS_TABLE+" SET occupancy_status='Not Occupied',"+dbVar.MODIFIED_DATE+"='"+timeC+"',associated_invoice_id='' WHERE "+dbVar.UNIQUE_ID+"='"+bookedTableUniqueId+"'";
                    dbVar.executeExecSQL(updateInvoiceItemsQuery);
                    bookedTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TABLE_SETTINGS_TABLE + " WHERE unique_id='" + bookedTableUniqueId + "'");

                }

                JSONObject subarr = new JSONObject();
                try {
                    deleteOldPendingQueriesRelatedtoInvoice(InvoiceNumber);
                    subarr.put("completeinvoiceDetails",InvoiceDetails);
                    subarr.put("splitpayments",splitInvoiceDetails);
                    subarr.put("invoiceitemstotal",InvoiceItemDetails);
                    subarr.put("invoicetaxdetails",InvoiceTaxDetails);
                    subarr.put("categorytaxes",InvoiceCategoryTaxDetails);
                    subarr.put("deletedItemsFromSave",deletedItemsList);
                    subarr.put("bookedtable",bookedTableDetails);
                    dbVar.sendToServerServiceData(subarr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(!customerId.equals(""))
            {

                CustomersManager cm = new CustomersManager();
                cm.autoSaveCustomerWisePricing(InvoiceNumber);
                ArrayList<JSONObject> promotionsValidationItems = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
//            PromotionsValidation.createMembershipsPackagesPrepaidCards(promotionsValidationItems,newInvoiceNumber);
//            PromotionsValidation.deductPackagesAndPrepaidCards(newInvoiceNumber);

            }

        }

    }
    public static void shiftTable(String fromInvoice,String toTable,String newOrderRefNum,String storeId)
    {
        ArrayList<JSONObject> queryExecResults = new ArrayList<>();
        // Check if new order reference number and the new table
        String query = "SELECT * from "
                + dbVar.INVOICE_TOTAL_TABLE
                + " where "
                + dbVar.INVOICE_HOLD_ID
                + "=\"" + newOrderRefNum + "\" AND "+ dbVar.INVOICE_STORE_ID +"=\""+ storeId +"\" AND status='hold' order by "
                + dbVar.MODIFIED_DATE;
        ArrayList<JSONObject> tableNumberSelection = dbVar.executeRawqueryJSON(query);
        Log.v("TableShifting",tableNumberSelection.toString());
        String timeC = ConstantsAndUtilities.currentTime();
        // Condition 1 -> If new order reference number doesnt have existing order
        if(tableNumberSelection.size()==0) {
            // Update to new order reference number to invoice
            ArrayList<JSONObject> currentInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM invoice_total_table WHERE invoice_id='"+fromInvoice+"'");
            ArrayList<JSONObject> newTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.TABLE_SETTINGS_TABLE+" WHERE table_name='"+toTable+"' AND "+dbVar.SETTINGS_TABLE_STOREID+"='"+storeId+"'");
            Log.v("TableShifting",currentInvoiceDetails.toString());
            Log.v("TableShifting",newTableDetails.toString());
            String newTableUniqueId = "";
            if(newTableDetails.size()==1){
                // associate new invoice id to this table and change status to occupied
                newTableUniqueId = (String) newTableDetails.get(0).optString("unique_id");
                String updateInvoiceItemsQuery = "UPDATE "+ dbVar.TABLE_SETTINGS_TABLE+" SET occupancy_status='Occupied',"+dbVar.MODIFIED_DATE+"='"+timeC+"',associated_invoice_id='"+ fromInvoice +"' WHERE "+dbVar.UNIQUE_ID+"='"+newTableUniqueId+"'";
                dbVar.executeExecSQL(updateInvoiceItemsQuery);

            }
            // vacate old table
            // change status of table existing table to vacant,
            String oldTableUniqueId = "";
            if(!currentInvoiceDetails.get(0).optString(dbVar.INVOICE_BOOKED_TABLE).equals(""))
            {
                oldTableUniqueId = (String) currentInvoiceDetails.get(0).optString(dbVar.INVOICE_BOOKED_TABLE);
                String updateInvoiceItemsQuery = "UPDATE " + dbVar.TABLE_SETTINGS_TABLE + " SET occupancy_status='Not Occupied'," + dbVar.MODIFIED_DATE + "='" + timeC + "',associated_invoice_id='' WHERE " + dbVar.UNIQUE_ID + "='" + currentInvoiceDetails.get(0).optString(dbVar.INVOICE_BOOKED_TABLE) + "'";
                dbVar.executeExecSQL(updateInvoiceItemsQuery);
//                ArrayList<JSONObject> oldTableDetails = sqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.TABLE_SETTINGS_TABLE + " WHERE "+ dbVar.UNIQUE_ID + "='" + currentInvoiceDetails.get(0).get(dbVar.INVOICE_BOOKED_TABLE) + "'");
            }
            // update new table number to invoice
            // use new table
            String updatetotalInvoice = "UPDATE " + dbVar.INVOICE_TOTAL_TABLE + " SET "+dbVar.INVOICE_BOOKED_TABLE+"='"+newTableUniqueId+"'," + dbVar.MODIFIED_DATE + "='" + timeC + "',"+dbVar.INVOICE_HOLD_ID+"='"+newOrderRefNum+"' WHERE " + dbVar.UNIQUE_ID + "='" + currentInvoiceDetails.get(0).optString(dbVar.UNIQUE_ID) + "'";
            dbVar.executeExecSQL(updatetotalInvoice);
            String updatenewTableQuery = "UPDATE " + dbVar.TABLE_SETTINGS_TABLE + " SET occupancy_status='Occupied'," + dbVar.MODIFIED_DATE + "='" + timeC + "',associated_invoice_id='"+currentInvoiceDetails.get(0).optString(dbVar.INVOICE_ID)+"' WHERE " + dbVar.UNIQUE_ID + "='" + newTableUniqueId + "'";
            dbVar.executeExecSQL(updatenewTableQuery);
            // send whole data to server with full details of old table and new table
            try {
                String InvoiceNumber = fromInvoice;

                deleteOldPendingQueriesRelatedtoInvoice(InvoiceNumber);

                JSONObject subarr = new JSONObject();
                ArrayList<JSONObject> bookedTableDetails = new ArrayList<>();
                bookedTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TABLE_SETTINGS_TABLE + " WHERE unique_id='" + newTableUniqueId + "' OR unique_id='"+oldTableUniqueId+"'");

                ArrayList<JSONObject> splitInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                subarr.put("splitpayments", splitInvoiceDetails);
                ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");

                subarr.put("completeinvoiceDetails", InvoiceDetails);
                subarr.put("invoiceitemstotal", InvoiceItemDetails);
                subarr.put("invoicetaxdetails", InvoiceTaxDetails);
                subarr.put("categorytaxes", InvoiceCategoryTaxDetails);
                subarr.put("splitpayments", splitInvoiceDetails);
                subarr.put("bookedtable", bookedTableDetails);

                dbVar.sendToServerServiceData(subarr);


            }catch (Exception exp){}
        }
        // Condition 2 -> If new order reference number has existing order
        if(tableNumberSelection.size()!=0) {
            // Void the existing invoice
            tableMigration = true;
            Boolean voidCheck = voidTheInvoice(fromInvoice);
            tableMigration = false;
            String newTableExistingInvoiceId = (String) tableNumberSelection.get(0).optString(dbVar.INVOICE_ID);
            String oldGrandTotal = "0";
            ArrayList<JSONObject> oldInvoiceInfo = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + fromInvoice + "'");
            ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + fromInvoice + "'");


            String bookedTableUniqueId = (String) tableNumberSelection.get(0).optString(dbVar.INVOICE_BOOKED_TABLE);
            JSONObject obj = tableNumberSelection.get(0);
            String newGrandTotal = df.format( Double.parseDouble((String) obj.optString(dbVar.INVOICE_TOTAL_AMT)) + Double.parseDouble((String) oldInvoiceInfo.get(0).optString(dbVar.INVOICE_TOTAL_AMT)));
            String paymentType = "Cash";
            try {
                JSONObject contentValues = new JSONObject();
                contentValues.put(dbVar.INVOICE_ID, (String) obj.get(dbVar.INVOICE_ID));
                contentValues.put(dbVar.INVOICE_STORE_ID, (String) obj.get(dbVar.INVOICE_STORE_ID));
                contentValues.put(dbVar.INVOICE_TOTAL_AMT, newGrandTotal);
                contentValues.put(dbVar.INVOICE_STATUS, "hold");
                contentValues.put(dbVar.ORDER_TYPE, (String) obj.get(dbVar.ORDER_TYPE));
                contentValues.put(dbVar.INVOICE_HOLD_ID, (String) obj.get(dbVar.INVOICE_HOLD_ID));

                contentValues.put(dbVar.INVOICE_PAYMENT_TYPE, "");
                contentValues.put(dbVar.INVOICE_TOTAL_AVG, newGrandTotal);
                contentValues.put(dbVar.UNIQUE_ID, (String) obj.get(dbVar.UNIQUE_ID));
                contentValues.put(dbVar.INVOICE_PROFIT, "0");
                timeC = ConstantsAndUtilities.currentTime();
                contentValues.put(dbVar.INVOICE_BILL_TS, String.valueOf(obj.get(dbVar.INVOICE_BILL_TS)));
                contentValues.put(dbVar.CREATED_DATE, String.valueOf(obj.get(dbVar.CREATED_DATE)));
                contentValues.put(dbVar.MODIFIED_DATE, timeC);

                contentValues.put(dbVar.INVOICE_DELIVERY_DATE, String.valueOf(obj.get(dbVar.INVOICE_DELIVERY_DATE)));
                contentValues.put(dbVar.INVOICE_EMPLOYEE, String.valueOf(obj.get(dbVar.INVOICE_EMPLOYEE)));

                contentValues.put(dbVar.INVOICE_CHEQUE_NO, String.valueOf(obj.get(dbVar.INVOICE_CHEQUE_NO)));
                contentValues.put(dbVar.INVOICE_DELIVERY_STATUS, "Delivered");
                String customerId = (String) obj.get(dbVar.INVOICE_CUSTOMER);
                contentValues.put(dbVar.INVOICE_CUSTOMER, customerId);

                String here = dbVar.UNIQUE_ID;
                String uniq = (String) obj.get(dbVar.UNIQUE_ID);
                dbVar.executeUpdateToDB(dbVar.INVOICE_TOTAL_TABLE, contentValues, here, uniq);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            // add the existing items to the older invoice id with matching reference number
            if(InvoiceItemDetails.size()>0){
                for(int k=0;k < InvoiceItemDetails.size(); k++)
                {
                    JSONObject invItem = InvoiceItemDetails.get(k);
                    Boolean insertToExistingItem = false;
                    ArrayList<JSONObject> itemCheckInToTable = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + obj.optString(dbVar.INVOICE_ID) + "' AND "+dbVar.INVOICE_ITEM_ID+"='"+invItem.optString(dbVar.INVOICE_ITEM_ID)+"'");
                    if(itemCheckInToTable.size()>0)
                    {
                        Double priceCheck  = (Double.parseDouble((String) itemCheckInToTable.get(0).optString(dbVar.INVOICE_YOUR_COST))) / (Double.parseDouble((String) itemCheckInToTable.get(0).optString(dbVar.INVOICE_QUANTITY)));
                        Double oldItemPrice = Double.parseDouble((String) InvoiceItemDetails.get(k).optString(dbVar.INVOICE_YOUR_COST)) / (Double.parseDouble((String) InvoiceItemDetails.get(k).optString(dbVar.INVOICE_QUANTITY)));
                        if(oldItemPrice.equals(priceCheck))
                        {
                            insertToExistingItem = true;
                        }else{ System.out.println("Both are different");}

                    }
                    String insertItemid = null;
                    if(insertToExistingItem==false) {
                        insertItemid = (String) InvoiceItemDetails.get(k).optString(dbVar.INVOICE_ITEM_ID);
                        String discount = (String) InvoiceItemDetails.get(k).optString(dbVar.INVOICE_DISCOUNT);
                        try {
                            JSONObject contentItemValues = new JSONObject();
                            contentItemValues.put(dbVar.INVOICE_ITEM_ID, insertItemid);

                            contentItemValues.put(dbVar.INVOICE_DISCOUNT,df.format(Double.parseDouble(discount)));
                            contentItemValues.put(dbVar.INVOICE_TAX, "0");
                            contentItemValues.put(dbVar.INVOICE_ITEM_NAME, String.valueOf(InvoiceItemDetails.get(k).get(dbVar.INVOICE_ITEM_NAME)));
                            contentItemValues.put(dbVar.INVOICE_YOUR_COST, String.valueOf(InvoiceItemDetails.get(k).get(dbVar.INVOICE_YOUR_COST)));
//                        double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                            contentItemValues.put(dbVar.INVOICE_AVG_COST, String.valueOf(InvoiceItemDetails.get(k).get(dbVar.INVOICE_AVG_COST)));
                            contentItemValues.put(dbVar.INVOICE_QUANTITY, String.valueOf(InvoiceItemDetails.get(k).get(dbVar.INVOICE_QUANTITY)));
                            contentItemValues.put(dbVar.INVOICE_ID, newTableExistingInvoiceId);
                            contentItemValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                            timeC = ConstantsAndUtilities.currentTime();
                            contentItemValues.put(dbVar.CREATED_DATE, timeC);
                            contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                            contentItemValues.put("payment_type", paymentType);
                            String deptIdForItem = "";

                            ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + insertItemid + "' LIMIT 1");
                            if (itemGetDetails.size() == 0) {
                                // check if it is a plu code
                                ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + insertItemid + "' LIMIT 1");
                                if (plucodes.size() == 1) {
                                    JSONObject itemRow = plucodes.get(0);
                                    String itemIdForPLU = (String) itemRow.get(dbVar.ALTERNATE_PLU_item_no);
                                    itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                                }
                            }

                            if (itemGetDetails.size() != 0) {
                                JSONObject itemRow = itemGetDetails.get(0);
                                deptIdForItem = (String) itemRow.get(dbVar.INVENTORY_DEPARTMENT);
                            }
                            contentItemValues.put("in_department", deptIdForItem);
                            contentItemValues.put("store_id", String.valueOf(obj.get(dbVar.INVOICE_STORE_ID)));
                            contentItemValues.put("status", "hold");
                            contentItemValues.put("hold_status", "hold");
                            contentItemValues.put("notes", String.valueOf(InvoiceItemDetails.get(k).get("notes")));
                            dbVar.executeInsertToDB(dbVar.INVOICE_ITEMS_TABLE, contentItemValues);

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }else{
                        Double newQtyForItem = (((Double.parseDouble((String) itemCheckInToTable.get(0).optString(dbVar.INVOICE_QUANTITY))) + (Double.parseDouble((String) InvoiceItemDetails.get(k).optString(dbVar.INVOICE_QUANTITY)))    ));
                        Double priceCheck  = (Double.parseDouble((String) itemCheckInToTable.get(0).optString(dbVar.INVOICE_YOUR_COST))) / (Double.parseDouble((String) itemCheckInToTable.get(0).optString(dbVar.INVOICE_QUANTITY)));

                        Double newPrice = priceCheck * newQtyForItem;
                        try {
                            JSONObject contentItemValues = itemCheckInToTable.get(0);
                            contentItemValues.put(dbVar.INVOICE_YOUR_COST, String.valueOf(newPrice));

//                        double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                            contentItemValues.put(dbVar.INVOICE_AVG_COST, itemCheckInToTable.get(0).get(dbVar.INVOICE_AVG_COST));
                            contentItemValues.put(dbVar.INVOICE_QUANTITY, String.valueOf(newQtyForItem));
                            timeC = ConstantsAndUtilities.currentTime();
                            contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                            String updateUniqueId = (String) contentItemValues.get(dbVar.UNIQUE_ID);
                            ConstantsAndUtilities cv = new ConstantsAndUtilities();
                            ContentValues tableRecordContentValues = cv.convertJSONObjectToContentValues(contentItemValues);//new ContentValues();
                            if(tableRecordContentValues.size()!=0)
                            {   dbVar.executeUpdateToDB(dbVar.INVOICE_ITEMS_TABLE,contentItemValues,dbVar.UNIQUE_ID, updateUniqueId); }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            // vacate the table -> Done
            // calculate new subtotal
            // calculate new taxes
            // calculate new grandtotal -> Done
            // send the new information to server
            deleteOldPendingQueriesRelatedtoInvoice(newTableExistingInvoiceId);
            try {
                JSONObject subarr = new JSONObject();
                ArrayList<JSONObject> bookedTableDetails = new ArrayList<>();
                bookedTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.TABLE_SETTINGS_TABLE + " WHERE unique_id='" + tableNumberSelection.get(0).get(dbVar.INVOICE_BOOKED_TABLE) + "'");

                ArrayList<JSONObject> splitInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + newTableExistingInvoiceId + "'");
                subarr.put("splitpayments", splitInvoiceDetails);
                ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + newTableExistingInvoiceId + "'");
                InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + newTableExistingInvoiceId + "'");
                ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + newTableExistingInvoiceId + "'");
                ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + newTableExistingInvoiceId + "'");

                subarr.put("completeinvoiceDetails", InvoiceDetails);
                subarr.put("invoiceitemstotal", InvoiceItemDetails);
                subarr.put("invoicetaxdetails", InvoiceTaxDetails);
                subarr.put("categorytaxes", InvoiceCategoryTaxDetails);
                subarr.put("splitpayments", splitInvoiceDetails);
                subarr.put("bookedtable", bookedTableDetails);

                dbVar.sendToServerServiceData(subarr);
            }catch (Exception exp){
                exp.printStackTrace();
            }

            // print the voided invoice

        }
    }
    public static void finalizeNewInvoice(String newInvoiceNumber,JSONObject obj){

        JSONObject contentValues = new JSONObject();
        String timeC = "";
        String paymentType = "Cash";
        String customerId = obj.optString("customerIdForInvoice");
        try {
            contentValues.put(dbVar.INVOICE_ID, newInvoiceNumber);

        contentValues.put(dbVar.INVOICE_STORE_ID, obj.optString("storeIdForInvoice"));
        contentValues.put(dbVar.INVOICE_TOTAL_AMT, obj.optString("grandTotal"));
        contentValues.put(dbVar.INVOICE_STATUS, "complete");
        contentValues.put(dbVar.ORDER_TYPE,POSWebActivity.selectedOrderType);
        contentValues.put(dbVar.INVOICE_HOLD_ID,obj.optString("orderRefNo"));
        String modeOfPayment2 = obj.optString("modeOfPayment2");
        if(!modeOfPayment2.equals("")){ paymentType="multiple"; }else{ paymentType = obj.optString("modeOfPayment1");}
        contentValues.put(dbVar.INVOICE_PAYMENT_TYPE,paymentType);
        contentValues.put(dbVar.INVOICE_TOTAL_AVG,obj.optString("grandTotal"));
        contentValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
        contentValues.put(dbVar.INVOICE_PROFIT,"0");
        timeC = ConstantsAndUtilities.currentTime();
        contentValues.put(dbVar.INVOICE_BILL_TS,timeC);
        contentValues.put(dbVar.CREATED_DATE,timeC);
        contentValues.put(dbVar.MODIFIED_DATE,timeC);
        String orderDeliveryDate = obj.optString("orderDeliveryDate");
        if(orderDeliveryDate.equals("")){ orderDeliveryDate = timeC;}
        contentValues.put(dbVar.INVOICE_DELIVERY_DATE,orderDeliveryDate);
        contentValues.put(dbVar.INVOICE_EMPLOYEE,dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
        String paymentRefNo = "";
        if(!paymentType.equals("multiple")){
            paymentRefNo = obj.optString("paymentRefNo1");
        }
        contentValues.put(dbVar.INVOICE_CHEQUE_NO,paymentRefNo);
        contentValues.put(dbVar.INVOICE_DELIVERY_STATUS,"Delivered");
        contentValues.put(dbVar.INVOICE_CUSTOMER,customerId);

        dbVar.executeInsertToDB(dbVar.INVOICE_TOTAL_TABLE, contentValues);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//                        Log.v("Srinath","New invoice for saving is "+newInvoiceNumber+ " and content values are "+contentValues.toString());
        Log.v("Srinath","Store ID is "+ obj.toString());
        JSONArray itemIds = obj.optJSONArray("itemIds");
        JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
        JSONArray itemNames = obj.optJSONArray("itemNames");
        JSONArray itemNotes = obj.optJSONArray("itemNotes");
        JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
        JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
        JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
        String holdInvoiceId = obj.optString("holdInvoiceId");
        try{
            // insert items
            ArrayList<JSONObject> itemsBulkInsert = new ArrayList<>();
            for(int k=0; k< itemIds.length();k++){
                String insertItemid = null;
                insertItemid = itemIds.getString(k);
                String discount = discountInCurrency.getString(k);
                JSONObject contentItemValues = new JSONObject();
                contentItemValues.put(dbVar.INVOICE_ITEM_ID,insertItemid);
                contentItemValues.put(dbVar.INVOICE_DISCOUNT,discount);
                contentItemValues.put(dbVar.INVOICE_TAX,"0");
                contentItemValues.put(dbVar.INVOICE_ITEM_NAME,itemNames.getString(k));
                contentItemValues.put(dbVar.INVOICE_YOUR_COST,totalItemPrices.getString(k));
                double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                contentItemValues.put(dbVar.INVOICE_AVG_COST,df.format(itemUnitPrice));
                contentItemValues.put(dbVar.INVOICE_QUANTITY,itemQuantitys.getString(k));
                contentItemValues.put(dbVar.INVOICE_ID,newInvoiceNumber);
                contentItemValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                timeC = ConstantsAndUtilities.currentTime();
                contentItemValues.put(dbVar.CREATED_DATE,timeC);
                contentItemValues.put(dbVar.MODIFIED_DATE,timeC);
                contentItemValues.put("payment_type",paymentType);
                String deptIdForItem="";

                ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+insertItemid+"' LIMIT 1");
                if(itemGetDetails.size()==0){
                    // check if it is a plu code
                    ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.ALTERNATE_PLU_TABLE+" WHERE "+dbVar.ALTERNATE_PLU_plu_number+"='"+insertItemid+"' LIMIT 1");
                    if(plucodes.size()==1) {
                        JSONObject itemRow = plucodes.get(0);
                        String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                        itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+itemIdForPLU+"' LIMIT 1");
                    }
                }

                if(itemGetDetails.size() != 0){
                    JSONObject itemRow = itemGetDetails.get(0);
                    deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                }
                contentItemValues.put("in_department",deptIdForItem);
                contentItemValues.put("store_id",obj.optString("storeIdForInvoice"));
                contentItemValues.put("status","complete");
                contentItemValues.put("hold_status","Completed");
                contentItemValues.put("notes",itemNotes.getString(k));
//                dbVar.executeInsertToDB(dbVar.INVOICE_ITEMS_TABLE,  contentItemValues);
                itemsBulkInsert.add(contentItemValues);
            }
            if(itemsBulkInsert.size()>0) {
                dbVar.executeInsertBatch(dbVar.INVOICE_ITEMS_TABLE, itemsBulkInsert);
            }

            try {
                JSONObject allTaxes = (obj.getJSONObject("allTaxes"));
                JSONArray categoryTaxes = allTaxes.getJSONArray("categorywise");
                JSONArray overallTaxes =  allTaxes.getJSONArray("overall");
//                            Log.v("Srinath","Category taxes are "+categoryTaxes.toString()+" and overall taxes are "+overallTaxes.toString());
                // insert category wise taxes
                timeC = ConstantsAndUtilities.currentTime();
                ArrayList<JSONObject> bulkInsertCategoryTaxes = new ArrayList<>();
                for(int p=0; p < categoryTaxes.length();p++){
                    JSONObject taxRow = categoryTaxes.optJSONObject(p);
                    if(Double.valueOf(taxRow.optString("tax_calculated_value"))==0){continue;}
                    JSONObject contentTaxValues = new JSONObject();
                    contentTaxValues.put(dbVar.INVOICE_ID,newInvoiceNumber);
                    contentTaxValues.put(dbVar.UNIQUE_ID,newInvoiceNumber+"_"+(taxRow.optString("category"))+"_"+taxRow.optString("tax_name"));
                    contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                    contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                    contentTaxValues.put("server_local","local");
                    contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                    contentTaxValues.put("category_id",taxRow.optString("category"));
                    contentTaxValues.put("tax_value",taxRow.optString("tax_calculated_value"));
//                    dbVar.executeInsertToDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE, contentTaxValues);
                    bulkInsertCategoryTaxes.add(contentTaxValues);
                }
                    if(bulkInsertCategoryTaxes.size()>0)
                    {
                        dbVar.executeInsertBatch(dbVar.CATEGORY_TAX_AMOUNT_TABLE,bulkInsertCategoryTaxes);
                    }
                // insert overall taxes
                ArrayList<JSONObject> bulkInsertTaxes = new ArrayList<>();

                for(int p=0; p < overallTaxes.length();p++){
                    JSONObject taxRow = overallTaxes.optJSONObject(p);
                    JSONObject contentTaxValues = new JSONObject();
                    contentTaxValues.put(dbVar.INVOICE_ID,newInvoiceNumber);
                    contentTaxValues.put(dbVar.UNIQUE_ID,newInvoiceNumber+taxRow.optString("tax_name"));
                    contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                    contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                    contentTaxValues.put("server_local","local");
                    contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                    contentTaxValues.put("tax_value",taxRow.optString("tax_value"));
//                    dbVar.executeInsertToDB(dbVar.TAX_AMOUNT_TABLE,  contentTaxValues);
                    bulkInsertTaxes.add(contentTaxValues);
                }
                if(bulkInsertTaxes.size()>0)
                {
                    dbVar.executeInsertBatch(dbVar.TAX_AMOUNT_TABLE,bulkInsertTaxes);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }



            // insert multiple mode payments
            if(paymentType.equals("multiple")){
                ArrayList<JSONObject> splitPaymentsBulk = new ArrayList<>();
                for(int v=1;v<=4;v++){
                    String paymentTypeStr = "paymentType"+v;
                    String modeOfPaymentStr = "modeOfPayment"+v;
                    String paymentRefNoStr = "paymentRefNo"+v;
                    String paymentTypeMultiple = obj.optString(paymentTypeStr);
                    if(!paymentTypeMultiple.equals("")){
                        JSONObject contentTaxValues = new JSONObject();
                        contentTaxValues.put(dbVar.INVOICE_ID,newInvoiceNumber);
                        contentTaxValues.put(dbVar.UNIQUE_ID,ConstantsAndUtilities.randomValue());
                        contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                        contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                        contentTaxValues.put("server_local","local");
                        contentTaxValues.put("cheque_no",obj.optString(paymentRefNoStr));
                        contentTaxValues.put("payment_type",obj.optString(modeOfPaymentStr));
                        contentTaxValues.put("amount",obj.optString(paymentTypeStr));
                        contentTaxValues.put(dbVar.SPLIT_ACCOUNT_NO,"");
//                        dbVar.executeInsertToDB(dbVar.SPLIT_INVOICE_TABLE,  contentTaxValues);
                        splitPaymentsBulk.add(contentTaxValues);

                        if(obj.optString(modeOfPaymentStr).equals("Account"))
                        {
                            CustomersManager cm2 = new CustomersManager();
                            cm2.customerAccountPaymentDebit(Double.parseDouble(String.valueOf(obj.optString(paymentTypeStr))),newInvoiceNumber,customerId);
                        }
                    }
                }
                if(splitPaymentsBulk.size()>0)
                {
                    dbVar.executeInsertBatch(dbVar.SPLIT_INVOICE_TABLE,splitPaymentsBulk);
                }
            }
            // check for promotions and membership programs

            // send complete invoice through invoice info
            markCreditCardTransactionsAsComplete(newInvoiceNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(paymentType.equals("Account"))
        {

            Double gt =  Double.parseDouble(obj.optString("grandTotal"));
            CustomersManager cm = new CustomersManager();
            cm.customerAccountPaymentDebit(gt,newInvoiceNumber,customerId);
        }

        try{
            JSONObject subarr = new JSONObject();
            ArrayList<JSONObject> splitInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
            subarr.put("splitpayments",splitInvoiceDetails);
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
            ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
            ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
            ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
            ArrayList<JSONObject> cardTransactionDetails = dbVar.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE invoice_id='" + newInvoiceNumber + "' ");

            deleteOldPendingQueriesRelatedtoInvoice(newInvoiceNumber);
            subarr.put("completeinvoiceDetails",InvoiceDetails);
            subarr.put("invoiceitemstotal",InvoiceItemDetails);
            subarr.put("invoicetaxdetails",InvoiceTaxDetails);
            subarr.put("categorytaxes",InvoiceCategoryTaxDetails);
            subarr.put("splitpayments",splitInvoiceDetails);
            subarr.put("card_transactions_details",cardTransactionDetails);
            dbVar.sendToServerServiceData(subarr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!customerId.equals(""))
        {

            CustomersManager cm = new CustomersManager();
            cm.autoSaveCustomerWisePricing(newInvoiceNumber);
            ArrayList<JSONObject> promotionsValidationItems = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + newInvoiceNumber + "'");
//            PromotionsValidation.createMembershipsPackagesPrepaidCards(promotionsValidationItems,newInvoiceNumber);
//            PromotionsValidation.deductPackagesAndPrepaidCards(newInvoiceNumber);

        }

    }
    public static void saveOnHoldInvoice(String InvoiceNumber,JSONObject obj,String newOrRepeat){
        Log.v("Test","saveOnHoldInvoice called ");
        Log.v("Test",obj.toString());

        if(newOrRepeat.equals("New")) {

            JSONObject contentValues = new JSONObject();
            String timeC = ConstantsAndUtilities.currentTime();
            String paymentType = "Cash";

            try {
                contentValues.put(dbVar.INVOICE_ID, InvoiceNumber);

            contentValues.put(dbVar.INVOICE_STORE_ID, obj.optString("storeIdForInvoice"));
            contentValues.put(dbVar.INVOICE_TOTAL_AMT, obj.optString("grandTotal"));
            contentValues.put(dbVar.INVOICE_STATUS, "hold");
            contentValues.put(dbVar.ORDER_TYPE,POSWebActivity.selectedOrderType);
            contentValues.put(dbVar.INVOICE_HOLD_ID,obj.optString("holdOrderRefNumForNewInvoice"));
            contentValues.put(dbVar.INVOICE_BOOKED_TABLE,obj.optString("holdOrderTableNum"));
            contentValues.put(dbVar.INVOICE_PAYMENT_TYPE,paymentType);
            contentValues.put(dbVar.INVOICE_TOTAL_AVG,obj.optString("grandTotal"));
            contentValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
            contentValues.put(dbVar.INVOICE_PROFIT,"0");
            contentValues.put(dbVar.INVOICE_BILL_TS,timeC);
            contentValues.put(dbVar.CREATED_DATE,timeC);
            contentValues.put(dbVar.MODIFIED_DATE,timeC);
            String orderDeliveryDate = obj.optString("orderDeliveryDate");
            if(orderDeliveryDate.equals("")){ orderDeliveryDate = timeC;}
            contentValues.put(dbVar.INVOICE_DELIVERY_DATE,orderDeliveryDate);
            contentValues.put(dbVar.INVOICE_EMPLOYEE,dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));
            String paymentRefNo = "";
            contentValues.put(dbVar.INVOICE_CHEQUE_NO,paymentRefNo);
            contentValues.put(dbVar.INVOICE_DELIVERY_STATUS,"Undelivered");
            String customerId = obj.optString("customerIdForInvoice");
            contentValues.put(dbVar.INVOICE_CUSTOMER,customerId);

            dbVar.executeInsertToDB(dbVar.INVOICE_TOTAL_TABLE, contentValues);


            } catch (JSONException e) {
                e.printStackTrace();
            }
//                        Log.v("Srinath","New invoice for saving is "+newInvoiceNumber+ " and content values are "+contentValues.toString());
            try{
                JSONArray itemIds = obj.optJSONArray("itemIds");
                JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
                JSONArray itemNames = obj.optJSONArray("itemNames");
                JSONArray itemNotes = obj.optJSONArray("itemNotes");
                JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
                JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
                JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
                ArrayList<JSONObject> itemsBulkInsert = new ArrayList<>();
                // insert items
                for(int k=0; k< itemIds.length();k++){
                    String insertItemid = null;
                    insertItemid = itemIds.getString(k);
                    String discount = discountInCurrency.getString(k);
                    JSONObject contentItemValues = new JSONObject();
                    contentItemValues.put(dbVar.INVOICE_ITEM_ID,insertItemid);
                    contentItemValues.put(dbVar.INVOICE_DISCOUNT,discount);
                    contentItemValues.put(dbVar.INVOICE_TAX,"0");
                    contentItemValues.put(dbVar.INVOICE_ITEM_NAME,itemNames.getString(k));
                    contentItemValues.put(dbVar.INVOICE_YOUR_COST,totalItemPrices.getString(k));
                    double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                    contentItemValues.put(dbVar.INVOICE_AVG_COST,df.format(itemUnitPrice));
                    contentItemValues.put(dbVar.INVOICE_QUANTITY,itemQuantitys.getString(k));
                    contentItemValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                    contentItemValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                    timeC = ConstantsAndUtilities.currentTime();
                    contentItemValues.put(dbVar.CREATED_DATE,timeC);
                    contentItemValues.put(dbVar.MODIFIED_DATE,timeC);
                    contentItemValues.put("payment_type",paymentType);
                    String deptIdForItem="";

                    ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+insertItemid+"' LIMIT 1");
                    if(itemGetDetails.size()==0){
                        // check if it is a plu code
                        ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.ALTERNATE_PLU_TABLE+" WHERE "+dbVar.ALTERNATE_PLU_plu_number+"='"+insertItemid+"' LIMIT 1");
                        if(plucodes.size()==1) {
                            JSONObject itemRow = plucodes.get(0);
                            String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                            itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+itemIdForPLU+"' LIMIT 1");
                        }
                    }

                    if(itemGetDetails.size() != 0){
                        JSONObject itemRow = itemGetDetails.get(0);
                        deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                    }
                    contentItemValues.put("in_department",deptIdForItem);
                    contentItemValues.put("store_id",obj.optString("storeIdForInvoice"));
                    contentItemValues.put("status","hold");
                    contentItemValues.put("hold_status","");
                    contentItemValues.put("notes",itemNotes.getString(k));
//                    dbVar.executeInsertToDB(dbVar.INVOICE_ITEMS_TABLE,  contentItemValues);
                    itemsBulkInsert.add(contentItemValues);
                }

                dbVar.executeInsertBatch(dbVar.INVOICE_ITEMS_TABLE,itemsBulkInsert);

                try {
                    JSONObject allTaxes = (obj.getJSONObject("allTaxes"));
                    JSONArray categoryTaxes = allTaxes.getJSONArray("categorywise");
                    JSONArray overallTaxes =  allTaxes.getJSONArray("overall");
//                            Log.v("Srinath","Category taxes are "+categoryTaxes.toString()+" and overall taxes are "+overallTaxes.toString());
                    // insert category wise taxes
                    timeC = ConstantsAndUtilities.currentTime();

                    String where = dbVar.INVOICE_ID + "";
                    dbVar.executeDeleteInDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE,
                            where, InvoiceNumber);
                    for(int p=0; p < categoryTaxes.length();p++){
                        JSONObject taxRow = categoryTaxes.optJSONObject(p);
                        if(Double.valueOf(taxRow.optString("tax_calculated_value"))==0){continue;}
                        JSONObject contentTaxValues = new JSONObject();
                        contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                        contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+"_"+taxRow.optString("category")+"_"+taxRow.optString("tax_name"));
                        contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                        contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                        contentTaxValues.put("server_local","local");
                        contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                        contentTaxValues.put("category_id",taxRow.optString("category"));
                        contentTaxValues.put("tax_value",taxRow.optString("tax_calculated_value"));
                        dbVar.executeInsertToDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE, contentTaxValues);

                    }
                    // insert overall taxes
                    for(int p=0; p < overallTaxes.length();p++){
                        JSONObject taxRow = overallTaxes.optJSONObject(p);
                        JSONObject contentTaxValues = new JSONObject();
                        contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                        contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+taxRow.optString("tax_name"));
                        contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                        contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                        contentTaxValues.put("server_local","local");
                        contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                        contentTaxValues.put("tax_value",taxRow.optString("tax_value"));
                        dbVar.executeInsertToDB(dbVar.TAX_AMOUNT_TABLE, contentTaxValues);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // check for promotions and membership programs

                // send complete invoice through invoice info

                markCreditCardTransactionsAsComplete(InvoiceNumber);

            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                try {
                    JSONObject subarr = new JSONObject();
                    ArrayList<JSONObject> bookedTableDetails = new ArrayList<>();
                    if(!obj.optString("holdOrderTableNum").equals("")){
                        String updateInvoiceItemsQuery = "UPDATE "+ dbVar.TABLE_SETTINGS_TABLE+" SET occupancy_status='Occupied',"+dbVar.MODIFIED_DATE+"='"+timeC+"',associated_invoice_id='"+ InvoiceNumber +"' WHERE "+dbVar.UNIQUE_ID+"='"+obj.optString("holdOrderTableNum")+"'";
                        dbVar.executeExecSQL(updateInvoiceItemsQuery);
                        bookedTableDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TABLE_SETTINGS_TABLE + " WHERE unique_id='" + obj.optString("holdOrderTableNum") + "'");

                    }
                    ArrayList<JSONObject> splitInvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    subarr.put("splitpayments", splitInvoiceDetails);
                    ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> cardTransactionDetails = dbVar.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE invoice_id='" + InvoiceNumber + "' ");

                    subarr.put("completeinvoiceDetails", InvoiceDetails);
                    subarr.put("invoiceitemstotal", InvoiceItemDetails);
                    subarr.put("invoicetaxdetails", InvoiceTaxDetails);
                    subarr.put("categorytaxes", InvoiceCategoryTaxDetails);
                    subarr.put("splitpayments", splitInvoiceDetails);
                    subarr.put("card_transactions_details",cardTransactionDetails);
                    subarr.put("bookedtable", bookedTableDetails);
                    dbVar.sendToServerServiceData(subarr);
                }catch (JSONException exp){}
            }
        }else if(newOrRepeat.equals("Repeat")){
            ArrayList<JSONObject> InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
            String paymentType = "Cash";
            String timeC = ConstantsAndUtilities.currentTime();

            if(InvoiceDetails.size()==1) {
                JSONObject invoiceRow = InvoiceDetails.get(0);
                JSONObject contentValues = new JSONObject();
                try {
                    contentValues.put(dbVar.INVOICE_ID, InvoiceNumber);


                contentValues.put(dbVar.INVOICE_STORE_ID, obj.optString("storeIdForInvoice"));
                contentValues.put(dbVar.INVOICE_TOTAL_AMT, obj.optString("grandTotal"));
                contentValues.put(dbVar.INVOICE_STATUS, "hold");
                contentValues.put(dbVar.ORDER_TYPE,POSWebActivity.selectedOrderType);
                contentValues.put(dbVar.INVOICE_HOLD_ID,invoiceRow.optString(dbVar.INVOICE_HOLD_ID));
                contentValues.put(dbVar.INVOICE_BOOKED_TABLE,invoiceRow.optString(dbVar.INVOICE_BOOKED_TABLE));
                contentValues.put(dbVar.INVOICE_PAYMENT_TYPE,paymentType);
                contentValues.put(dbVar.INVOICE_TOTAL_AVG,obj.optString("grandTotal"));
                contentValues.put(dbVar.UNIQUE_ID, invoiceRow.optString(dbVar.UNIQUE_ID));
                contentValues.put(dbVar.INVOICE_PROFIT,"0");
                contentValues.put(dbVar.INVOICE_BILL_TS,invoiceRow.optString(dbVar.INVOICE_BILL_TS));
                contentValues.put(dbVar.CREATED_DATE,invoiceRow.optString(dbVar.CREATED_DATE));
                contentValues.put(dbVar.MODIFIED_DATE,timeC);
                String orderDeliveryDate = obj.optString("orderDeliveryDate");
                if(orderDeliveryDate.equals("")){ orderDeliveryDate = timeC;}
                contentValues.put(dbVar.INVOICE_DELIVERY_DATE,invoiceRow.optString(dbVar.INVOICE_DELIVERY_DATE));
//                contentValues.put(dbVar.INVOICE_DELIVERY_DATE,timeC);
                contentValues.put(dbVar.INVOICE_EMPLOYEE,invoiceRow.optString(dbVar.INVOICE_EMPLOYEE));
                String paymentRefNo = "";
                contentValues.put(dbVar.INVOICE_CHEQUE_NO,"");
                if(obj.has("printpreview") && obj.optString("printpreview").equals("true"))
                {
                    contentValues.put(dbVar.INVOICE_DELIVERY_STATUS, "Receipt Printed");
                }else {
                    contentValues.put(dbVar.INVOICE_DELIVERY_STATUS, "Undelivered");
                }
                String customerId = obj.optString("customerIdForInvoice");
                contentValues.put(dbVar.INVOICE_CUSTOMER,customerId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String here = dbVar.UNIQUE_ID + "";
                String uniq = invoiceRow.optString(dbVar.UNIQUE_ID);
                dbVar.executeUpdateToDB(dbVar.INVOICE_TOTAL_TABLE, contentValues,here,uniq);
                JSONArray itemIds = obj.optJSONArray("itemIds");
                JSONArray discountInCurrency = obj.optJSONArray("discountInCurrency");
                JSONArray itemNames = obj.optJSONArray("itemNames");
                JSONArray itemNotes = obj.optJSONArray("itemNotes");
                JSONArray savedOlderUniqueIds = obj.optJSONArray("savedOlderUniqueIds");
                JSONArray totalItemPrices = obj.optJSONArray("totalItemPrices");
                JSONArray itemQuantitys = obj.optJSONArray("itemQuantitys");
                String holdInvoiceId = obj.optString("holdInvoiceId");

                String deletedItemsList = obj.optString("deletedItemsUniqueIdsFromSavedInvoice");
                Log.v("HoldInvoice",deletedItemsList);
                if(deletedItemsList.contains(",")){
                    String[] individualDeletedItems = deletedItemsList.split(",");
                    for(int pp = 0 ; pp < individualDeletedItems.length ; pp++){
                        String where = dbVar.UNIQUE_ID + "";
                        dbVar.executeDeleteInDB(dbVar.INVOICE_ITEMS_TABLE,
                                where, individualDeletedItems[pp]);

                    }
                }else if(!deletedItemsList.equals("")){
                    String where = dbVar.UNIQUE_ID + "";
                    dbVar.executeDeleteInDB(dbVar.INVOICE_ITEMS_TABLE,
                            where, deletedItemsList );
                }
                // Update the items
                try {

                    ArrayList<JSONObject> itemsBulkInsert = new ArrayList<>();
                    String allUniqueIdsString = "";
                    JSONObject olderUniqueIdsMapJSON = new JSONObject();
                    ArrayList<JSONObject> invoiceItemsOld = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE invoice_id='"+InvoiceNumber + "'");
                    // merge all olderunique ids
                    if(invoiceItemsOld.size()>0)
                    {
                        for(int j=0; j < invoiceItemsOld.size(); j++)
                        {
                            JSONObject invoiceItemRowOld = invoiceItemsOld.get(j);
                            String olderUniqueId  = invoiceItemRowOld.optString(dbVar.UNIQUE_ID);
                            allUniqueIdsString += "'"+olderUniqueId+"',";
                            olderUniqueIdsMapJSON.put(olderUniqueId,invoiceItemRowOld);
                        }

                    }

                    Log.v("Testing",olderUniqueIdsMapJSON.toString());

                    for (int k = 0; k < itemIds.length(); k++) {
                        String insertItemid = null;
                        insertItemid = itemIds.getString(k);
                        String olderUniqueId = savedOlderUniqueIds.getString(k);


                        if(olderUniqueId.equals("")){
                            String discount = discountInCurrency.getString(k);
                            JSONObject contentItemValues = new JSONObject();
                            contentItemValues.put(dbVar.INVOICE_ITEM_ID, insertItemid);
                            contentItemValues.put(dbVar.INVOICE_DISCOUNT, discount);
                            contentItemValues.put(dbVar.INVOICE_TAX, "0");
                            contentItemValues.put(dbVar.INVOICE_ITEM_NAME, itemNames.getString(k));
                            contentItemValues.put(dbVar.INVOICE_YOUR_COST, totalItemPrices.getString(k));
                            double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                            contentItemValues.put(dbVar.INVOICE_AVG_COST, df.format(itemUnitPrice));
                            contentItemValues.put(dbVar.INVOICE_QUANTITY, itemQuantitys.getString(k));
                            contentItemValues.put(dbVar.INVOICE_ID, InvoiceNumber);
                            contentItemValues.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                            timeC = ConstantsAndUtilities.currentTime();
                            contentItemValues.put(dbVar.CREATED_DATE, timeC);
                            contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                            contentItemValues.put("payment_type", paymentType);
                            String deptIdForItem = "";

                            ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + insertItemid + "' LIMIT 1");
                            if (itemGetDetails.size() == 0) {
                                // check if it is a plu code
                                ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + insertItemid + "' LIMIT 1");
                                if (plucodes.size() == 1) {
                                    JSONObject itemRow = plucodes.get(0);
                                    String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                                    itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                                }
                            }

                            if (itemGetDetails.size() != 0) {
                                JSONObject itemRow = itemGetDetails.get(0);
                                deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                            }
                            contentItemValues.put("in_department", deptIdForItem);
                            contentItemValues.put("store_id", obj.optString("storeIdForInvoice"));
                            contentItemValues.put("status", "hold");
                            contentItemValues.put("hold_status", "hold");
                            contentItemValues.put("notes", itemNotes.getString(k));
//                            dbVar.executeInsertToDB(dbVar.INVOICE_ITEMS_TABLE, contentItemValues);
                            itemsBulkInsert.add(contentItemValues);
                        }else{


                            // retrieve records olderUniqueIdsMapJSON
                            JSONObject olderRecord = olderUniqueIdsMapJSON.getJSONObject(olderUniqueId);
                            String oldRecordPrice = olderRecord.optString(dbVar.INVOICE_YOUR_COST);
                            String oldRecordQty =  olderRecord.optString(dbVar.INVOICE_QUANTITY);
                            String oldRecordName =  olderRecord.optString(dbVar.INVOICE_ITEM_NAME);
                            // map json object by unique id
                            // update the new record only if quantity or price are different
                            if(!oldRecordPrice.equals(totalItemPrices.getString(k)) || !oldRecordQty.equals(itemQuantitys.getString(k))  || !oldRecordName.equals(itemNames.getString(k)) )
                            {
                                Log.v("Testing","We have to save the record for "+itemNames.getString(k));
                                Log.v("Testing","We are saving "+oldRecordName+ " - old name "+ itemNames.getString(k)+ " - new name ,  "+ oldRecordQty+ " - old quantity "+ itemQuantitys.getString(k)+ " - new quantity ,  "+ oldRecordPrice + " for old price "+ totalItemPrices.getString(k) + " - new price");
                                ArrayList<JSONObject> InvoiceItemDetails = new ArrayList<>();//dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE unique_id='" + olderUniqueId + "'");
                                InvoiceItemDetails.add(olderRecord);

                                if (InvoiceItemDetails.size() == 1) {
                                    JSONObject invoiceItemRow = InvoiceItemDetails.get(0);
                                    String discount = discountInCurrency.getString(k);
                                    JSONObject contentItemValues = new JSONObject();

                                    contentItemValues.put(dbVar.INVOICE_ITEM_ID, invoiceItemRow.optString(dbVar.INVOICE_ITEM_ID));
                                    contentItemValues.put(dbVar.INVOICE_DISCOUNT, discount);
                                    contentItemValues.put(dbVar.INVOICE_TAX, "0");
                                    contentItemValues.put(dbVar.INVOICE_ITEM_NAME, itemNames.getString(k));
                                    contentItemValues.put(dbVar.INVOICE_YOUR_COST, totalItemPrices.getString(k));
                                    double itemUnitPrice = (getDoubleValueof(totalItemPrices.getString(k))) / (getDoubleValueof(itemQuantitys.getString(k)));
                                    contentItemValues.put(dbVar.INVOICE_AVG_COST, df.format(itemUnitPrice));
                                    contentItemValues.put(dbVar.INVOICE_QUANTITY, itemQuantitys.getString(k));
                                    contentItemValues.put(dbVar.INVOICE_ID, InvoiceNumber);
                                    contentItemValues.put(dbVar.UNIQUE_ID, invoiceItemRow.optString(dbVar.UNIQUE_ID));
                                    timeC = ConstantsAndUtilities.currentTime();
                                    contentItemValues.put(dbVar.CREATED_DATE, invoiceItemRow.optString(dbVar.CREATED_DATE));
                                    contentItemValues.put(dbVar.MODIFIED_DATE, timeC);
                                    contentItemValues.put("payment_type", paymentType);
                                    String deptIdForItem = "";

                                    ArrayList<JSONObject> itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + insertItemid + "' LIMIT 1");
                                    if (itemGetDetails.size() == 0) {
                                        // check if it is a plu code
                                        ArrayList<JSONObject> plucodes = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.ALTERNATE_PLU_TABLE + " WHERE " + dbVar.ALTERNATE_PLU_plu_number + "='" + insertItemid + "' LIMIT 1");
                                        if (plucodes.size() == 1) {
                                            JSONObject itemRow = plucodes.get(0);
                                            String itemIdForPLU = itemRow.optString(dbVar.ALTERNATE_PLU_item_no);
                                            itemGetDetails = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM " + dbVar.INVENTORY_TABLE + " WHERE " + dbVar.INVENTORY_ITEM_NO + "='" + itemIdForPLU + "' LIMIT 1");
                                        }
                                    }

                                    if (itemGetDetails.size() != 0) {
                                        JSONObject itemRow = itemGetDetails.get(0);
                                        deptIdForItem = itemRow.optString(dbVar.INVENTORY_DEPARTMENT);
                                    }
                                    contentItemValues.put("in_department", deptIdForItem);
                                    contentItemValues.put("store_id", obj.optString("storeIdForInvoice"));
                                    contentItemValues.put("status", "hold");
                                    contentItemValues.put("hold_status", "hold");
                                    contentItemValues.put("notes", itemNotes.getString(k));
                                    here = dbVar.UNIQUE_ID + "";
                                    uniq = invoiceItemRow.optString(dbVar.UNIQUE_ID);
                                    dbVar.executeUpdateToDB(
                                            dbVar.INVOICE_ITEMS_TABLE, contentItemValues,
                                            here, uniq);

                                    if (Double.parseDouble((String) itemQuantitys.get(k)) < Double.parseDouble((String) invoiceItemRow.get(dbVar.INVOICE_QUANTITY))) {
                                        Double diffQty = (Double.parseDouble((String) invoiceItemRow.get(dbVar.INVOICE_QUANTITY)) - Double.parseDouble((String) itemQuantitys.get(k)));
                                        JSONObject duplicateItemRow = invoiceItemRow;
                                        duplicateItemRow.put("item_desscription",uniq);
                                        reducedQtypushToDeleteQueriesAndSendToServer(duplicateItemRow, (diffQty));
                                        String newRandomString = ConstantsAndUtilities.randomValue();
                                    }
                                }
                            }else{
                                Log.v("Testing","We are skipping "+oldRecordName+ " - name "+ oldRecordQty+ " - quantity "+ oldRecordPrice + " for price");
                            }
                        }




                    }

                    if(itemsBulkInsert.size()>0)
                    {
                        dbVar.executeInsertBatch(dbVar.INVOICE_ITEMS_TABLE,itemsBulkInsert);
                    }

                    try {
                        JSONObject allTaxes = (obj.getJSONObject("allTaxes"));
                        JSONArray categoryTaxes = allTaxes.getJSONArray("categorywise");
                        JSONArray overallTaxes =  allTaxes.getJSONArray("overall");

                        String where = dbVar.INVOICE_ID + "";
                        dbVar.executeDeleteInDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE,
                                where, InvoiceNumber);
                        dbVar.executeDeleteInDB(dbVar.TAX_AMOUNT_TABLE,
                                where, InvoiceNumber);
//                            Log.v("Srinath","Category taxes are "+categoryTaxes.toString()+" and overall taxes are "+overallTaxes.toString());
                        // insert category wise taxes
                        timeC = ConstantsAndUtilities.currentTime();
                        for(int p=0; p < categoryTaxes.length();p++){
                            JSONObject taxRow = categoryTaxes.optJSONObject(p);
                            if(Double.valueOf(taxRow.optString("tax_calculated_value"))==0){continue;}
                            JSONObject contentTaxValues = new JSONObject();
                            contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                            contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+"_"+taxRow.optString("category")+"_"+taxRow.optString("tax_name"));
                            contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                            contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                            contentTaxValues.put("server_local","local");
                            contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                            contentTaxValues.put("category_id",taxRow.optString("category"));
                            contentTaxValues.put("tax_value",taxRow.optString("tax_calculated_value"));
                            dbVar.executeInsertToDB(dbVar.CATEGORY_TAX_AMOUNT_TABLE,  contentTaxValues);

                        }
                        // insert overall taxes
                        for(int p=0; p < overallTaxes.length();p++){
                            JSONObject taxRow = overallTaxes.optJSONObject(p);
                            JSONObject contentTaxValues = new JSONObject();
                            contentTaxValues.put(dbVar.INVOICE_ID,InvoiceNumber);
                            contentTaxValues.put(dbVar.UNIQUE_ID,InvoiceNumber+taxRow.optString("tax_name"));
                            contentTaxValues.put(dbVar.CREATED_DATE,timeC);
                            contentTaxValues.put(dbVar.MODIFIED_DATE,timeC);
                            contentTaxValues.put("server_local","local");
                            contentTaxValues.put("tax_name",taxRow.optString("tax_name"));
                            contentTaxValues.put("tax_value",taxRow.optString("tax_value"));
                            dbVar.executeInsertToDB(dbVar.TAX_AMOUNT_TABLE, contentTaxValues);

                        }

                        markCreditCardTransactionsAsComplete(InvoiceNumber);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }catch (JSONException exp){

                }finally {
                    InvoiceDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceItemDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> InvoiceCategoryTaxDetails = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CATEGORY_TAX_AMOUNT_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                    ArrayList<JSONObject> cardTransactionDetails = dbVar.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE invoice_id='" + InvoiceNumber + "' ");

                    JSONObject subarr = new JSONObject();
                    try {
                        deleteOldPendingQueriesRelatedtoInvoice(InvoiceNumber);
                        subarr.put("completeinvoiceDetails",InvoiceDetails);

                        subarr.put("invoiceitemstotal",InvoiceItemDetails);
                        subarr.put("invoicetaxdetails",InvoiceTaxDetails);
                        subarr.put("categorytaxes",InvoiceCategoryTaxDetails);
                        subarr.put("deletedItemsFromSave",deletedItemsList);
                        subarr.put("card_transactions_details",cardTransactionDetails);
                        dbVar.sendToServerServiceData(subarr);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public static String saveTheCustomer(String formDataOfCustomer, String storeId) {
        String response = "false";
        JSONArray formData = null;
        String customerName = "",customerLastName = "",customerId = "",customerPhone = "",customerEmail = "", customerStreet1 = "",customerStreet2 = "",customerState = "",customerCity = "",customerZipcode = "",customerTaxId="",customerCompanyName="";
        try {
            formData = new JSONArray(formDataOfCustomer);
            for(int k=0;k< formData.length();k++)
            {
                JSONObject formRow = (JSONObject) formData.get(k);
                Log.v("Customer","Form row is" + formRow.toString());
                switch (formRow.optString("name"))
                {

                    case "addcustomerid" : customerId = (String) formRow.get("value"); break;
                    case "addcustomerfirstname" : customerName = (String) formRow.get("value"); break;
                    case "addcustomerlastname" : customerLastName = (String) formRow.get("value"); break;
                    case "addcustomerphone" : customerPhone = (String) formRow.get("value"); break;
                    case "addcustomeremail" : customerEmail = (String) formRow.get("value"); break;
                    case "addcustomeraddressline1" : customerStreet1 = (String) formRow.get("value"); break;
                    case "addcustomeraddressline2" : customerStreet2 = (String) formRow.get("value"); break;
                    case "addcustomeraddresscity" : customerCity = (String) formRow.get("value"); break;
                    case "addcustomeraddressstate" : customerState = (String) formRow.get("value"); break;
                    case "addcustomeraddresszipcode" : customerZipcode = (String) formRow.get("value"); break;
                    case "addcustomertaxid" : customerTaxId = (String) formRow.get("value"); break;
                    case "addcustomercompanyname"   :   customerCompanyName = (String) formRow.get("value"); break;

                    default:break;
                }
            }
            if(customerEmail.equals("") && customerPhone.equals("")){
                showToast("Either Customer Phone Number or Email address are mandatory");
                return response;
            }
            if(!customerId.equals("")){
                ArrayList<JSONObject> customerDetailsForID = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CUSTOMER_TABLE + " WHERE customer_no='" + customerId + "' ");
                if(customerDetailsForID.size()!=0) {
                    showToast("Customer ID already in use");
                    return response;
                }
            }
            if(!customerPhone.equals("")){
                ArrayList<JSONObject> customerDetailsForID = dbVar.executeRawqueryJSON("SELECT * FROM "+ dbVar.CUSTOMER_GENERAL_INFO_TABLE + " WHERE customer_primary_phone='" + customerPhone + "' ");
                if(customerDetailsForID.size()!=0) {
                    showToast("Customer Phone Number already in use");
                    return response;
                }
            }
            if(!customerEmail.equals("")) {
                ArrayList<JSONObject> customerDetailsForID = dbVar.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_TABLE + " WHERE customer_email='" + customerEmail + "' ");
                if (customerDetailsForID.size() != 0) {
                    showToast("Customer Email already in use");
                    return response;
                }
            }

            // all validation done. Save it to server now
            if(customerId.equals(""))
            {
                customerId = ConstantsAndUtilities.random9digits();
            }
            HashMap<String, String> hp = new HashMap<String, String>();
            String uniqueid = "",datavald="",dataval1="",datavals="";

            String currentTimeforEdit = ConstantsAndUtilities.currentTime();
            String modifiedTimeforEdit = ConstantsAndUtilities.currentTime();
            try {
                uniqueid = ConstantsAndUtilities.randomValue();
                JSONObject data = new JSONObject();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put(dbVar.UNIQUE_ID, uniqueid);
                jsonobj.put(dbVar.CUSTOMER_NO,
                        customerId);
                jsonobj.put(dbVar.CREATED_DATE,
                        currentTimeforEdit);
                jsonobj.put(dbVar.MODIFIED_DATE,
                        modifiedTimeforEdit);
                jsonobj.put(dbVar.CUSTOMER_LAST_NAME,
                        customerLastName);
                jsonobj.put(dbVar.CUSTOMER_EMAIL,
                        customerEmail);
                jsonobj.put(dbVar.CUSTOMER_NOTES, "");
                jsonobj.put(
                        dbVar.CUSTOMER_FIRST_NAME,
                        customerName);
                JSONArray fields = new JSONArray();
                fields.put(0, jsonobj);
                data.put("fields", fields);

                MainActivity.mySqlObj.executeInsert(dbVar.CUSTOMER_TABLE,jsonobj);
                SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_TABLE, data);
                datavald = data.toString();
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                JSONObject data = new JSONObject();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put(dbVar.UNIQUE_ID, uniqueid);
                jsonobj.put(
                        dbVar.CUSTOMER_COMPANY_NAME, customerCompanyName);
                jsonobj.put(
                        dbVar.CUSTOMER_GENERAL_TIN, customerTaxId);
                jsonobj.put(
                        dbVar.CUSTOMER_PRIMARY_PHONE,
                        customerPhone);
                jsonobj.put(dbVar.CREATED_DATE,
                        currentTimeforEdit);
                jsonobj.put(dbVar.MODIFIED_DATE,
                        modifiedTimeforEdit);
                jsonobj.put(
                        dbVar.CUSTOMER_ALTERNATE_PHONE,
                        "");
                jsonobj.put(dbVar.CUSTOMER_STREET1,
                        customerStreet1);
                jsonobj.put(dbVar.CUSTOMER_STREET2,
                        customerStreet2);
                jsonobj.put(dbVar.CUSTOMER_STATE,
                        customerState);
                jsonobj.put(dbVar.CUSTOMER_CITY,
                        customerCity);
                jsonobj.put(dbVar.CUSTOMER_COUNTRY,
                        "");
                jsonobj.put(dbVar.CUSTOMER_ZIPCODE,
                        customerZipcode);
                jsonobj.put(dbVar.CUSTOMER_BIRTHDAY,
                        "");
                jsonobj.put(dbVar.CUSTOMER_NO,
                        customerId);
                jsonobj.put(dbVar.CUSTOMER_GENDER,
                        "");
                JSONArray fields = new JSONArray();
                fields.put(0, jsonobj);
                data.put("fields", fields);
                dataval1 = data.toString();
                // System.out.println("data val is:" +
                // dataval1);

                MainActivity.mySqlObj.executeInsert(dbVar.CUSTOMER_GENERAL_INFO_TABLE,jsonobj);
                SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_GENERAL_INFO_TABLE, data);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            /*Parameters.sendToServer(
                    dbVar.CUSTOMER_TABLE,
                    datavald, "", hp);
            Parameters.sendToServer(
                    dbVar.CUSTOMER_GENERAL_INFO_TABLE,
                    dataval1, "", hp);*/

            String uId = ConstantsAndUtilities.randomValue();
            String time = ConstantsAndUtilities.currentTime();

            JSONObject contentValues = new JSONObject();
            contentValues.put(dbVar.STORE_ID,
                    storeId);
            contentValues.put(dbVar.CUSTOMER_NO,
                    customerId);
            contentValues.put(dbVar.UNIQUE_ID,
                    uId);
            contentValues.put(dbVar.CREATED_DATE,
                    time);
            contentValues.put(dbVar.MODIFIED_DATE,
                    time);
            datavald = "";
            try {
                JSONObject data = new JSONObject();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put(dbVar.STORE_ID, storeId);
                jsonobj.put(dbVar.CUSTOMER_NO, customerId);
                jsonobj.put(dbVar.UNIQUE_ID, uId);
                jsonobj.put(dbVar.CREATED_DATE, time);
                jsonobj.put(dbVar.MODIFIED_DATE, time);
                JSONArray fields = new JSONArray();
                fields.put(0, jsonobj);
                data.put("fields", fields);

                MainActivity.mySqlObj.executeInsert(dbVar.CUSTOMER_STORES_TABLE,contentValues);
                SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_STORES_TABLE, data);
                datavald = data.toString();
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (datavald.length() > 0)
//                Parameters.sendToServer(dbVar.CUSTOMER_STORES_TABLE, datavald, "true", hp);


            // add customer ID to customer_stores info

            // return the customer ID
            return customerId;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Customer","Form data is" + formData.toString());
        return response;

    }

    private static void showToast(String s) {
        Toast.makeText(MyApplication.getAppContext(), s, Toast.LENGTH_LONG).show();
    }


    private static void reducedQtypushToDeleteQueriesAndSendToServer(JSONObject invoiceItemRowDuplicate, Double reducedQty) {

        JSONObject invoiceItemRow = invoiceItemRowDuplicate;
        Double OldQty = Double.parseDouble((String) invoiceItemRow.optString("item_quantity"));
        Double totalPriceCharged = Double.parseDouble((String) invoiceItemRow.optString("price_you_charge"));
        Double newPrice = (totalPriceCharged / OldQty) * reducedQty;

        try {
            String oldUniqueId =  (String) invoiceItemRow.get("unique_id");

            invoiceItemRow.put("unique_id",ConstantsAndUtilities.randomValue());

            invoiceItemRow.put("item_quantity",String.valueOf(reducedQty));
            String oldInvoiceId = (String) invoiceItemRow.get("invoice_id");
            String newInvoiceId = "Deleted_"+oldInvoiceId;
            invoiceItemRow.put("unique_id",ConstantsAndUtilities.randomValue());
            invoiceItemRow.put("appliedoffer_id",oldInvoiceId);
            invoiceItemRow.remove("_id");
            invoiceItemRow.put("status","deleted");
            invoiceItemRow.put("invoice_id",newInvoiceId);
            invoiceItemRow.put("employee",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));

            ArrayList<JSONObject> invoiceOfDeletedItem = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_TOTAL_TABLE+" WHERE invoice_id='"+oldInvoiceId+"'");
            String invoiceDeliveryStatus = "";
            String holdValue = "";
            if(invoiceOfDeletedItem.size()==1)
            {
                invoiceDeliveryStatus = (String) invoiceOfDeletedItem.get(0).get("delivery_status");
                holdValue = (String) invoiceOfDeletedItem.get(0).get(dbVar.INVOICE_HOLD_ID);
            }

            ItemCancellationReasons icr = new ItemCancellationReasons();
            String CancellationReason = icr.cancellationReasonForUniqueId(oldUniqueId);
            if(invoiceDeliveryStatus.equals("Receipt Printed"))
            {
                invoiceItemRow.put("notes","After Receipt print - Reduced qty"+CancellationReason);
            }else{
                invoiceItemRow.put("notes","Before Receipt print - Reduced qty"+CancellationReason);
            }
            invoiceItemRow.put("hold_status",holdValue);
            String newPriceCharged = df.format(newPrice);
            invoiceItemRow.put("price_you_charge",newPriceCharged);
            invoiceItemRow.put("discount","0");


            dbVar.executeInsertToDB("invoice_items_table",invoiceItemRow);
//        System.exit(0);
            JSONObject data = new JSONObject();
            JSONArray fields = new JSONArray();
            fields.put(0,invoiceItemRow);

            data.put("fields", fields);

            String timeC = ConstantsAndUtilities.currentTime();

            String tableName = "invoice_items_table";
            // deleted item updated to server
            updateRecordsToServer(tableName,data);
            /*Parameters.sendToServer(tableName,
                    (data.toString()), "true", (new HashMap<String, String>()));*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /* String macAddress = Parameters.urlEncode(GetNetworkAddress.GetAddress("mac"));
        String currentTime = Parameters.urlEncode(timeC);
        String licenseKey = dbVar.getLicenseKey();

        JSONObject postJsonInfo = new JSONObject();
        String datavalforedit = Parameters.urlEncode(data.toJSONString());
        postJsonInfo.put("unique_id",ConstantsAndUtilities.randomValue());
        postJsonInfo.put("pending_data","saveinfo.php?javaVersion=true&tablename="+tableName+"&update=true&userid="+Parameters.userid+"&macaddr="+macAddress+"&license_key="+licenseKey+"&desktop_web_pos=true&Currentsystemtime="+currentTime+"&data="+datavalforedit);
        postJsonInfo.put("pending_data_json",invoiceItemRow.toJSONString());
        postJsonInfo.put("created_timestamp",timeC);
        postJsonInfo.put("modified_timestamp",timeC);
        LanServersqlCrmObj.executeInsert("pending_data",postJsonInfo);
        */

    }

    public static void updateRecordsToServer(String tableName, JSONObject data) {
        JSONObject subarr = new JSONObject();
        subarr = data;
        try {
            subarr.put("table_name",tableName);

        subarr.put("action_type","records_update");
        subarr.put("fromJavaDesktop","true");
            subarr.put("androidclient","true");
            ConstantsAndUtilities cv = new ConstantsAndUtilities();
        final String datavalforedit = cv.urlEncode(String.valueOf(subarr));
        String timeC = cv.currentTime();
        String macAddress =  cv.urlEncode("hi");
        String currentTime = cv.urlEncode(timeC);
        String licenseKey = dbVar.getLicenseKey();

        JSONObject postJsonInfo = new JSONObject();
        postJsonInfo.put("unique_id",cv.randomValue());
        postJsonInfo.put("pending_data","web_service.php?javaVersion=true&macaddr="+macAddress+"&license_key="+licenseKey+"&desktop_web_pos=true&Currentsystemtime="+currentTime+"&data="+datavalforedit);
        postJsonInfo.put("pending_data_json",String.valueOf(subarr));
        postJsonInfo.put("created_timestamp",timeC);
        postJsonInfo.put("modified_timestamp",timeC);
        MainActivity.mySqlCrmObj.executeInsert("pending_data",postJsonInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<JSONObject> deletedItemsFromInvoice(String deletedItemsList) {

        ArrayList<JSONObject> itemsDeleted = new ArrayList<>();

        if(deletedItemsList.contains(",")){

            String[] individualDeletedItems = deletedItemsList.split(",");
            ArrayList<String> deleteUniqueIds = new ArrayList<String >();
            for(int pp = 0 ; pp < individualDeletedItems.length ; pp++){
                ArrayList<JSONObject> itemDel = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+individualDeletedItems[pp]+"'");
                if(itemDel.size()==1){
                    int oldRowCount = itemsDeleted.size();
                    itemsDeleted.add(oldRowCount,itemDel.get(0));
                }
            }
        }else if(!deletedItemsList.equals("")){
            itemsDeleted = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_ITEMS_TABLE+" WHERE unique_id='"+deletedItemsList+"'");
        }

        if(itemsDeleted.size()>0)
        {
            try {

                JSONObject data = new JSONObject();
                JSONArray fields = new JSONArray();
                ArrayList<JSONObject> insertingValues = new ArrayList<>();
                String timeC = ConstantsAndUtilities.currentTime();
                fields = new JSONArray();
                for(int jj=0;jj < itemsDeleted.size() ; jj++)
                {

                    JSONObject contentValues = new JSONObject();
                    contentValues = itemsDeleted.get(jj);
                    String oldUniqueId = (String) contentValues.optString("unique_id");

                    contentValues.put("unique_id",ConstantsAndUtilities.randomValue());

                    contentValues.remove("_id");
                    String deleteItemInvoiceId = (String) contentValues.get("invoice_id");
                    ArrayList<JSONObject> invoiceOfDeletedItem = dbVar.executeRawqueryJSON("SELECT * FROM "+dbVar.INVOICE_TOTAL_TABLE+" WHERE invoice_id='"+deleteItemInvoiceId+"'");
                    String invoiceDeliveryStatus = "";
                    String holdValue = "";
                    if(invoiceOfDeletedItem.size()==1)
                    {
                        invoiceDeliveryStatus = (String) invoiceOfDeletedItem.get(0).get("delivery_status");
                        holdValue = (String) invoiceOfDeletedItem.get(0).get(dbVar.INVOICE_HOLD_ID);
                    }
                    contentValues.put("discount","0");
                    contentValues.put("invoice_id","Deleted_"+contentValues.get("invoice_id"));
                    contentValues.put("hold_status",holdValue);
                    contentValues.put("appliedoffer_id",deleteItemInvoiceId);
                    contentValues.put("status","deleted");
                    contentValues.put(dbVar.INVOICE_DISCRIPTION,oldUniqueId);
                    ItemCancellationReasons icr = new ItemCancellationReasons();
                    String CancellationReason = icr.cancellationReasonForUniqueId(oldUniqueId);


                    if(invoiceDeliveryStatus.equals("Receipt Printed"))
                    {
                        contentValues.put("notes","After Receipt print"+CancellationReason);
                    }else{
                        contentValues.put("notes","Before Receipt print"+CancellationReason);
                    }
                    contentValues.put(dbVar.CREATED_DATE,timeC);
                    contentValues.put(dbVar.MODIFIED_DATE,timeC);

                    contentValues.put("employee",dbVar.getValueForAttribute(ConstantsAndUtilities.SP_LOGGEDINUSERID));


                    ContentValues invoiceItemCV = new ContentValues();
                    Iterator<String> keys = contentValues.keys();

                    while(keys.hasNext()) {
                        String key = keys.next();
                        if (contentValues.get(key) instanceof JSONObject) {
                            // do something with jsonObject here
                            invoiceItemCV.put(key, (String) contentValues.get(key));
                        }
                    }
                    Log.v("CV",contentValues.toString());
                    Log.v("CV",invoiceItemCV.toString());
//                    System.out.println(contentValues);
//                    System.out.println(invoiceItemCV);
                    dbVar.executeInsertToDB("invoice_items_table",contentValues);
//        System.exit(0);
                    data = new JSONObject();
                    fields.put(jj,contentValues);



                }
                data.put("fields", fields);

                timeC = ConstantsAndUtilities.currentTime();

                String tableName = "invoice_items_table";
                // deleted item updated to server
                updateRecordsToServer(tableName,data);
                /*Parameters.sendToServer(tableName,
                        (data.toString()), "true", (new HashMap<String, String>()));*/
                // save deleteinfo
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return itemsDeleted;
    }


    private static void markCreditCardTransactionsAsComplete(String InvoiceNumber) {
        ArrayList<JSONObject> InvoiceDetailsForTransaction = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
        if(InvoiceDetailsForTransaction.size()==0)
        {
            return;
        }else{
            JSONObject invoiceROw = InvoiceDetailsForTransaction.get(0);
            String ModeOfPayment = String.valueOf(invoiceROw.optString(dbVar.INVOICE_PAYMENT_TYPE));
            if(ModeOfPayment.equals("Card"))
            {
                String transactionId = String.valueOf(invoiceROw.optString("cheque_no"));
                if(!transactionId.equals(""))
                {

                    ArrayList<JSONObject> cardTransactionDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE pos_transaction_reference_id='" + transactionId + "'");
                    if(cardTransactionDetails.size()>0) {
                        JSONObject updateCardTransactionObj = new JSONObject();
                        try {
                            String timeC = ConstantsAndUtilities.currentTime();
                            updateCardTransactionObj.put("modified_timestamp", timeC);
                            updateCardTransactionObj.put("status", "Completed");
                            updateCardTransactionObj.put("invoice_id", InvoiceNumber);
                            updateCardTransactionObj.put("completed_time", timeC);

                            MainActivity.mySqlObj.executeUpdate("card_transactions_processing", updateCardTransactionObj, "pos_transaction_reference_id", transactionId);
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    }
                }
            }
            else if(ModeOfPayment.equals("multiple"))
            {
                ArrayList<JSONObject> splitInvoiceDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + InvoiceNumber + "'");
                if(splitInvoiceDetails.size()>0)
                {
                    for(int b=0; b < splitInvoiceDetails.size(); b++)
                    {
                        JSONObject splitInvoiceJSONObj = splitInvoiceDetails.get(b);

                        String transactionId = String.valueOf(splitInvoiceJSONObj.optString("cheque_no"));
                        String paymentType = String.valueOf(splitInvoiceJSONObj.optString(("payment_type")));
                        if(paymentType.equals("Card") && !transactionId.equals(""))
                        {
                            ArrayList<JSONObject> cardTransactionDetails = MainActivity.mySqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE pos_transaction_reference_id='" + transactionId + "'");
                            if(cardTransactionDetails.size()>0)
                            {
                                JSONObject updateCardTransactionObj = new JSONObject();
                                try {
                                    String timeC = ConstantsAndUtilities.currentTime();
                                    updateCardTransactionObj.put("modified_timestamp", timeC);
                                    updateCardTransactionObj.put("status", "Completed");
                                    updateCardTransactionObj.put("invoice_id", InvoiceNumber);
                                    updateCardTransactionObj.put("completed_time", timeC);

                                    MainActivity.mySqlObj.executeUpdate("card_transactions_processing", updateCardTransactionObj, "pos_transaction_reference_id", transactionId);
                                } catch (Exception exp) {
                                    exp.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
