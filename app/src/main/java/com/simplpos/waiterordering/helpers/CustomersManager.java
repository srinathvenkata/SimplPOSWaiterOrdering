package com.simplpos.waiterordering.helpers;

import com.simplpos.waiterordering.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

public class CustomersManager {


    MySQLJDBC mySqlObj = null;
    MySQLJDBC mySqlCrmObj = null;

    private static final Random rnd = new Random();
    private static DecimalFormat df = new DecimalFormat("#.##");

    public DatabaseVariables dbVar = new DatabaseVariables();
    public ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
    public ServerSync serverSync = new ServerSync();

    private static DecimalFormat decf = new DecimalFormat("#.##");

    public CustomersManager()
    {

        mySqlObj = MainActivity.mySqlObj;
        mySqlCrmObj = MainActivity.mySqlCrmObj;
    }
    public void autoSaveCustomerWisePricing(String invoiceNumber) {
        ArrayList<JSONObject> autoSaveCustomerPricing = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.PREFERENCES + " WHERE " + dbVar.PRE_ATTRIBUTE + "=\"autosave_customer_pricing\" ");

        if(autoSaveCustomerPricing.size()==0){
            return ;
        }else if(autoSaveCustomerPricing.size()==1 && autoSaveCustomerPricing.get(0).optString(dbVar.PRE_VALUE).equals("false"))
        {
            return ;
        }


        ArrayList<JSONObject> completeInvoiceDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceNumber + "'");
        ArrayList<JSONObject> invoiceItemsDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_ITEMS_TABLE + " WHERE invoice_id='" + invoiceNumber + "'");

        String customerId = "";
        String storeId = "";
        String uniq = "";
        JSONObject contentValues = new JSONObject();

        if(completeInvoiceDetails.size()==1){
            customerId = (String) completeInvoiceDetails.get(0).optString(dbVar.INVOICE_CUSTOMER);
        }else{
            return;
        }
        ArrayList<JSONObject> insertingValues = new ArrayList<JSONObject>();
        JSONArray fields = new JSONArray();

        storeId =  (String) completeInvoiceDetails.get(0).optString(dbVar.INVOICE_STORE_ID);
        String orderType = (String) completeInvoiceDetails.get(0).optString(dbVar.ORDER_TYPE);
        if(invoiceItemsDetails.size()>0)
        {
            ServerSync serverSync = new ServerSync();
            for(int b=0; b < invoiceItemsDetails.size(); b++)
            {
                JSONObject invoiceItemObj = invoiceItemsDetails.get(b);
                System.out.println(invoiceItemObj.toString());
                String itemId = String.valueOf(invoiceItemObj.optString("item_id"));
                String itemName = String.valueOf(invoiceItemObj.optString("item_name"));
                Double totalPrice = Double.parseDouble(String.valueOf(invoiceItemObj.optString("price_you_charge")));
                Double qty = Double.parseDouble(String.valueOf(invoiceItemObj.optString("item_quantity")));
                Double unitPrice = totalPrice/qty;
                Double generalPrice = 0.0d;
                JSONObject productStorewisePricing = productStorewisePricingAndStock(itemId,orderType,storeId);
                if(productStorewisePricing.has("price"))
                {
                    generalPrice = Double.parseDouble(String.valueOf(productStorewisePricing.optString("price")));
                }

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);


                String unitPriceForSaving = nf.format(unitPrice);

                try {
                    ArrayList<JSONObject> customerStorewisePricing = mySqlObj.executeRawqueryJSON("SELECT * FROM customer_meta WHERE unique_id='" + customerId + "_" + storeId + "_" + itemId + "_pricing' AND customer_id='" + customerId + "' AND attribute='customer_storewise_pricing'");
                    if (customerStorewisePricing.size() == 0) {

                        if((Double.compare(generalPrice,unitPrice) != 0)) {
                            contentValues = new JSONObject();

                            String timeC = Parameters.currentTime();
                            contentValues.put(dbVar.MODIFIED_DATE, timeC);
                            contentValues.put("value", unitPriceForSaving);
                            contentValues.put("unique_id", customerId + "_" + storeId + "_" + itemId + "_pricing");
                            contentValues.put("customer_id", customerId);
                            contentValues.put("attribute", "customer_storewise_pricing");

                            contentValues.put(dbVar.CREATED_DATE, timeC);
                            contentValues.put("server_local", "server");

                            mySqlObj.executeInsert(dbVar.CUSTOMER_META_TABLE, contentValues);

                            JSONObject returnObjRow = contentValues;
                            fields.put((fields.length()), returnObjRow);
                        }
                    } else {
                        System.out.println(customerStorewisePricing);
                        Double oldPriceForCustomer = Double.parseDouble(String.valueOf(customerStorewisePricing.get(0).get("value")));
                        if (Double.compare(oldPriceForCustomer, unitPrice) != 0) {
                            System.out.println("Save the new pricing as old price is " + (String.valueOf(oldPriceForCustomer)) + " and new price is  " + (String.valueOf(unitPrice)));

                            JSONObject returnObjRow = customerStorewisePricing.get(0);
                            contentValues = new JSONObject();

                            String timeC = Parameters.currentTime();
                            contentValues.put(dbVar.MODIFIED_DATE, timeC);
                            contentValues.put("value", unitPriceForSaving);

                            String here = dbVar.UNIQUE_ID;
                            uniq = (String) returnObjRow.get(dbVar.UNIQUE_ID);
                            mySqlObj.executeUpdate(dbVar.CUSTOMER_META_TABLE, contentValues, here, uniq);


                            returnObjRow.put(dbVar.MODIFIED_DATE, timeC);
                            returnObjRow.put("value", unitPriceForSaving);
                            fields.put((fields.length()), returnObjRow);


                        }
                    }
                }catch (Exception exp){
                    exp.printStackTrace();
                }
            }
            if(fields.length()>0) {

                JSONObject data = new JSONObject();
                try {
                    data.put("fields", fields);
                    SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_META_TABLE, data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    public JSONObject customerInfoForId(String customerId) {
        JSONObject customerInfoForId = new JSONObject();
        ArrayList<JSONObject> customerGeneralInfoDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.CUSTOMER_GENERAL_INFO_TABLE+" WHERE "+dbVar.CUSTOMER_NO+"='"+customerId+"' LIMIT 1");
        ArrayList<JSONObject> customerDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.CUSTOMER_TABLE+" WHERE "+dbVar.CUSTOMER_NO+"='"+customerId+"' LIMIT 1");
        ArrayList<JSONObject> customerAccountPayment = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_META_TABLE + " WHERE customer_id='" + customerId + "' AND attribute='account_balance' AND unique_id='"+ customerId + "_account_balance" + "'");

        String customerName = "-";
        String customerCompanyName = "-";
        String customerPhoneNum = "-";
        String companyTin  = "";
        String customerAccountBalance = "0";
        if(customerGeneralInfoDetails.size()==1)
        {
            customerPhoneNum = (String) customerGeneralInfoDetails.get(0).optString(dbVar.CUSTOMER_PRIMARY_PHONE);
            customerCompanyName = (String) customerGeneralInfoDetails.get(0).optString(dbVar.CUSTOMER_COMPANY_NAME);
            companyTin = (String) customerGeneralInfoDetails.get(0).optString(dbVar.CUSTOMER_GENERAL_TIN);
        }

        if(customerAccountPayment.size()==1)
        {
            customerAccountBalance = (String) customerAccountPayment.get(0).optString(dbVar.CUSTOMER_ATTR_VALUE);

        }
        if(customerDetails.size()==1)
        {
            customerName = customerDetails.get(0).optString(dbVar.CUSTOMER_FIRST_NAME)+" "+ customerDetails.get(0).optString(dbVar.CUSTOMER_LAST_NAME);
        }

        try{
            customerInfoForId.put("customer_id",customerId);
            customerInfoForId.put("customer_name",customerName);
            customerInfoForId.put("company_name",customerCompanyName);
            customerInfoForId.put("phone_number",customerPhoneNum);
            customerInfoForId.put("company_tin",companyTin);
            customerInfoForId.put("account_balance",customerAccountBalance);

        }catch (Exception exp){
            exp.printStackTrace();
        }

        return customerInfoForId;
    }

    public void customerAccountPaymentDebit(Double grandTotal, String invoiceNumber, String customerId) {
        try {
            // check existing balance
            Double currentUserBalance = 0.0d;
            Double oldBalance = 0.0d;
            Double newUserBalance = 0.0d;
            ArrayList<JSONObject> customerAccountPayment = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_META_TABLE + " WHERE customer_id='" + customerId + "' AND " + dbVar.CUSTOMER_ATTRIBUTE + "='account_balance' ");
            JSONObject contentValues = new JSONObject();
            Preferences prefs = Preferences.userRoot();
            String timeC = Parameters.currentTime();
            if (customerAccountPayment.size() > 0) {
                currentUserBalance = Double.parseDouble((String) customerAccountPayment.get(0).optString(dbVar.CUSTOMER_ATTR_VALUE));

                contentValues.put(dbVar.CREATED_DATE, timeC);
                contentValues.put(dbVar.ALTERNATE_PLU_server_local, "server");

            } else {
                contentValues.put(dbVar.CREATED_DATE, timeC);
                contentValues.put(dbVar.ALTERNATE_PLU_server_local, "server");
            }
            oldBalance = currentUserBalance;
            newUserBalance = oldBalance - (Double) grandTotal;

            contentValues.put(dbVar.UNIQUE_ID, customerId + "_account_balance");
            contentValues.put(dbVar.MODIFIED_DATE, timeC);
            contentValues.put(dbVar.CUSTOMER_ID, customerId);
            contentValues.put(dbVar.CUSTOMER_ATTRIBUTE, "account_balance");
            contentValues.put(dbVar.CUSTOMER_ATTR_VALUE, df.format(newUserBalance));

            if (customerAccountPayment.size() == 0) {
                mySqlObj.executeInsert(dbVar.CUSTOMER_META_TABLE, contentValues);
            } else {
                mySqlObj.executeUpdate(dbVar.CUSTOMER_META_TABLE, contentValues, dbVar.UNIQUE_ID, customerId + "_account_balance");
            }
            JSONObject data = new JSONObject();
            JSONArray fields = new JSONArray();
            fields.put(0, contentValues);

            data.put("fields", fields);
            SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_META_TABLE, data);

            contentValues = new JSONObject();
            contentValues.put(dbVar.INVOICE_ID, invoiceNumber);
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_TRANSACTION_TYPE, "debit");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_OPENING_BALANCE, df.format(oldBalance));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_AMOUNT, df.format(grandTotal));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_CLOSING_BALANCE, df.format(newUserBalance));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_STATUS, "invoice payment");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_REF_NUM, "");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_MODE, "Account");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_CUSTOMER_ID, customerId);
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_EMPLOYEE, prefs.get(Parameters.SP_LOGGEDINUSERID, ""));
            contentValues.put(dbVar.UNIQUE_ID, Parameters.randomValue());
            contentValues.put(dbVar.CREATED_DATE, timeC);
            contentValues.put(dbVar.MODIFIED_DATE, timeC);
            mySqlObj.executeInsert(dbVar.CUSTOMER_ACCOUNT_HISTORY_TABLE, contentValues);


            data = new JSONObject();
            fields = new JSONArray();
            fields.put(0, contentValues);

            data.put("fields", fields);

            SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_ACCOUNT_HISTORY_TABLE, data);

        }catch(Exception exp){
            exp.printStackTrace();
        }
    }

    public void AddBalanceForCustomer(String amountBeingAdded, String customerId, String modeOfPayment) {
        try {
            Double currentUserBalance = 0.0d;
            Double oldBalance = 0.0d;
            Double newUserBalance = 0.0d;
            ArrayList<JSONObject> customerAccountPayment = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_META_TABLE + " WHERE customer_id='" + customerId + "' ");
            JSONObject contentValues = new JSONObject();
            Preferences prefs = Preferences.userRoot();
            String timeC = Parameters.currentTime();
            if (customerAccountPayment.size() > 0) {
                currentUserBalance = Double.parseDouble((String) customerAccountPayment.get(0).get(dbVar.CUSTOMER_ATTR_VALUE));

                contentValues.put(dbVar.CREATED_DATE, timeC);
                contentValues.put(dbVar.ALTERNATE_PLU_server_local, "server");

            } else {
                contentValues.put(dbVar.CREATED_DATE, timeC);
                contentValues.put(dbVar.ALTERNATE_PLU_server_local, "server");
            }
            oldBalance = currentUserBalance;
            newUserBalance = oldBalance + (Double.parseDouble(amountBeingAdded));

            contentValues.put(dbVar.UNIQUE_ID, customerId + "_account_balance");
            contentValues.put(dbVar.MODIFIED_DATE, timeC);
            contentValues.put(dbVar.CUSTOMER_ID, customerId);
            contentValues.put(dbVar.CUSTOMER_ATTRIBUTE, "account_balance");
            contentValues.put(dbVar.CUSTOMER_ATTR_VALUE, df.format(newUserBalance));

            if (customerAccountPayment.size() == 0) {
                mySqlObj.executeInsert(dbVar.CUSTOMER_META_TABLE, contentValues);
            } else {
                mySqlObj.executeUpdate(dbVar.CUSTOMER_META_TABLE, contentValues, dbVar.UNIQUE_ID, customerId + "_account_balance");
            }
            JSONObject data = new JSONObject();
            JSONArray fields = new JSONArray();
            fields.put(0, contentValues);

            data.put("fields", fields);
            SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_META_TABLE, data);

            contentValues = new JSONObject();
            contentValues.put(dbVar.INVOICE_ID, "");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_TRANSACTION_TYPE, "credit");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_OPENING_BALANCE, df.format(oldBalance));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_AMOUNT, df.format(Double.parseDouble(amountBeingAdded)));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_CLOSING_BALANCE, df.format(newUserBalance));
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_STATUS, "invoice payment");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_REF_NUM, "");
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_MODE, modeOfPayment);
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_CUSTOMER_ID, customerId);
            contentValues.put(dbVar.CUSTOMER_ACCOUNT_EMPLOYEE, prefs.get(Parameters.SP_LOGGEDINUSERID, ""));
            contentValues.put(dbVar.UNIQUE_ID, Parameters.randomValue());
            contentValues.put(dbVar.CREATED_DATE, timeC);
            contentValues.put(dbVar.MODIFIED_DATE, timeC);
            mySqlObj.executeInsert(dbVar.CUSTOMER_ACCOUNT_HISTORY_TABLE, contentValues);


            data = new JSONObject();
            fields = new JSONArray();
            fields.put(0, contentValues);

            data.put("fields", fields);

            SaveFetchVoidInvoice.updateRecordsToServer(dbVar.CUSTOMER_ACCOUNT_HISTORY_TABLE, data);
        }catch (Exception e){ e.printStackTrace();}
    }


    public JSONObject productStorewisePricingAndStock(String productId,String orderType,String StoreId){
        JSONObject productInfo = new JSONObject();
        ArrayList<JSONObject> storeWisePricingDetails =mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.STORE_PRODUCTS_TABLE+" WHERE "+dbVar.PRODUCTS_STOREID+"='"+StoreId+"' AND "+dbVar.PRODUCTS_ITEM_NO+"='"+productId+"' LIMIT 1");
        ArrayList<JSONObject> storeWiseStockDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.STOREWISE_STOCK_TABLE+" WHERE "+dbVar.STORE_CATEGORY_STOREID+"='"+StoreId+"' AND "+dbVar.PRODUCTS_ITEM_NO+"='"+productId+"' AND "+dbVar.STOREID_ITEMNO+"='"+StoreId+productId+"' LIMIT 1");
        ArrayList<JSONObject> itemGetDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM "+dbVar.INVENTORY_TABLE+" WHERE "+dbVar.INVENTORY_ITEM_NO+"='"+productId+"' LIMIT 1");
        String price = "0";
        String stockCount = "0";
        if(storeWisePricingDetails.size() ==1){
            if(orderType.equals( "store sale")){
                price = (String) storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_PRICE);}
            else if(orderType.equals( "take away")){
                price = (String) storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_TAKEAWAY);}
            else if(orderType.equals( "home delivery")){
                price = (String) storeWisePricingDetails.get(0).optString(dbVar.PRODUCTS_DELIVERY_PRICE);}
        }else{
            if(orderType.equals("store sale")){
                price = (String) itemGetDetails.get(0).optString(dbVar.INVENTORY_PRICE_TAX);
            }else{
                price = (String) itemGetDetails.get(0).optString(dbVar.INVENTORY_TAKEAWAY_TAX);
            }
        }
        if(storeWiseStockDetails.size() == 1){
            stockCount = (String) storeWiseStockDetails.get(0).optString(dbVar.STOREWISE_STOCKCOUNT);
        }
        try{
            productInfo.put("price",price);
            productInfo.put("stockCount",stockCount);
        }catch (Exception exp){ exp.printStackTrace(); }
        return productInfo;
    }
}
