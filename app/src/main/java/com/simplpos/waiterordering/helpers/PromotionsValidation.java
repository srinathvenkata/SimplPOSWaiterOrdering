package com.simplpos.waiterordering.helpers;

import com.simplpos.waiterordering.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

public class PromotionsValidation {

    private static final Random rnd = new Random();
    private static DecimalFormat df = new DecimalFormat("#.##");

    MySQLJDBC mySqlObj = null;
    MySQLJDBC mySqlCrmObj = null;
    public DatabaseVariables dbVar = new DatabaseVariables();
    public ConstantsAndUtilities Parameters = new ConstantsAndUtilities();
    public ServerSync serverSync = new ServerSync();
    
    public PromotionsValidation()
    {

        mySqlObj = MainActivity.mySqlObj;
        mySqlCrmObj = MainActivity.mySqlCrmObj;
    }
    public void revertBackAccountPayment(String invoiceIdForVoid) {
        try{

            Double amountToBeRevertedBack = 0.0d;
            ArrayList<JSONObject> completeInvoiceDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "'");
            if(completeInvoiceDetails.size()==0){ return; }
            JSONObject invoiceInfo = completeInvoiceDetails.get(0);
            if(invoiceInfo.get(dbVar.INVOICE_PAYMENT_TYPE).equals("Account"))
            {
                amountToBeRevertedBack = Double.parseDouble(String.valueOf( invoiceInfo.get(dbVar.INVOICE_TOTAL_AMT) ));

            }else if(invoiceInfo.get(dbVar.INVOICE_PAYMENT_TYPE).equals("multiple")){
                ArrayList<JSONObject> splitInvoiceDetails = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.SPLIT_INVOICE_TABLE + " WHERE invoice_id='" + invoiceIdForVoid + "' AND "+dbVar.SPLIT_PAYMENT_TYPE+"='Account'");
                if(splitInvoiceDetails.size()>0)
                {
                    for(int j=0; j< splitInvoiceDetails.size();j++)
                    {
                        JSONObject splitInvoiceRow = splitInvoiceDetails.get(j);
                        amountToBeRevertedBack += Double.parseDouble(String.valueOf(  splitInvoiceRow.get(dbVar.SPLIT_AMOUNT) ));
                    }
                }
            }else{
                return;
            }
            if(invoiceInfo.get(dbVar.INVOICE_CUSTOMER).equals("")){ return; }
            String invoiceCustomer = (String) invoiceInfo.get(dbVar.INVOICE_CUSTOMER);


            try {
                Double currentUserBalance = 0.0d;
                Double oldBalance = 0.0d;
                Double newUserBalance = 0.0d;
                ArrayList<JSONObject> customerAccountPayment = mySqlObj.executeRawqueryJSON("SELECT * FROM " + dbVar.CUSTOMER_META_TABLE + " WHERE customer_id='" + invoiceCustomer + "' AND attribute='account_balance'");
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
                newUserBalance = oldBalance + amountToBeRevertedBack;

                contentValues.put(dbVar.UNIQUE_ID, invoiceCustomer + "_account_balance");
                contentValues.put(dbVar.MODIFIED_DATE, timeC);
                contentValues.put(dbVar.CUSTOMER_ID, invoiceCustomer);
                contentValues.put(dbVar.CUSTOMER_ATTRIBUTE, "account_balance");
                contentValues.put(dbVar.CUSTOMER_ATTR_VALUE, df.format(newUserBalance));
                System.out.println("Updating balance records are ");
                System.out.println(contentValues.toString());
                if (customerAccountPayment.size() == 0) {
                    mySqlObj.executeInsert(dbVar.CUSTOMER_META_TABLE, contentValues);
                } else {
                    mySqlObj.executeUpdate(dbVar.CUSTOMER_META_TABLE, contentValues, dbVar.UNIQUE_ID, invoiceCustomer + "_account_balance");
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
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_AMOUNT, df.format(amountToBeRevertedBack));
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_CLOSING_BALANCE, df.format(newUserBalance));
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_INVOICE_ID, invoiceIdForVoid);
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_STATUS, "invoice payment");
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_REF_NUM, "");
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_PAYMENT_MODE, "voided invoice");
                contentValues.put(dbVar.CUSTOMER_ACCOUNT_CUSTOMER_ID, invoiceCustomer);
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
            } catch (JSONException exp){
                exp.printStackTrace();
            } catch (Exception e){ e.printStackTrace();}

        }catch (Exception exp){
            exp.printStackTrace();
        }
    }
}
