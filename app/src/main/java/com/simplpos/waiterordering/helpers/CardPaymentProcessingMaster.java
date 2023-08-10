package com.simplpos.waiterordering.helpers;

import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.helpers.paymentprocessors.PAXPaymentProcessing;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

public class CardPaymentProcessingMaster {
    MySQLJDBC mySqlObj = null;
    MySQLJDBC mySqlCrmObj = null;
    public CardPaymentProcessingMaster(){
            mySqlObj = MainActivity.mySqlObj;
            mySqlCrmObj = MainActivity.mySqlCrmObj;

    }
    public JSONObject processRefundInDollars(String transactionAmountInDollar)
    {
        JSONObject paymentProcessingResult = new JSONObject();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String amountInCents = convertDollarsToCents(transactionAmountInDollar);
        String randomTransactionId = generateRandomTransactionId(9);
        System.out.println("CardPaymentProcessingMaster processing an amount of "+transactionAmountInDollar+" and total amount in cents is "+amountInCents+" And random transaction ID is "+randomTransactionId);

        String terminalCompany = "PAX";

        terminalCompany = preferredCardProcessingTerminal();
        JSONObject insertIntoTransactionsObj = new JSONObject();
        try {
            DatabaseVariables dbVar = new DatabaseVariables();
            String timeC = ConstantsAndUtilities.currentTime();
            insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.CREATED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.UNIQUE_ID,ConstantsAndUtilities.randomValue());
            insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local,"local");
            insertIntoTransactionsObj.put("pos_transaction_reference_id",randomTransactionId);
            insertIntoTransactionsObj.put("card_terminal_type",terminalCompany);
            insertIntoTransactionsObj.put("given_amount",transactionAmountInDollar);
            insertIntoTransactionsObj.put("tip_amount","0");
            insertIntoTransactionsObj.put("transaction_type","RETURN");
            insertIntoTransactionsObj.put("card_holder_name","");
            insertIntoTransactionsObj.put("edc_type","");
            insertIntoTransactionsObj.put("employee",ConstantsAndUtilities.userid);
            insertIntoTransactionsObj.put("initiated_time",timeC);

            mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

            if(terminalCompany.equals("PAX"))
            {
                PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                paymentProcessingResult = paxProcessing.returnThePaymentWithAmount(amountInCents,randomTransactionId);
            }
        }catch (Exception exp)
        {
            exp.printStackTrace();
        }
        return paymentProcessingResult;
    }
    public JSONObject batchSettlement()
    {
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        JSONObject updateTransactionDetailsObj = new JSONObject();
        try {
            String terminalCompany = "PAX";

            terminalCompany = preferredCardProcessingTerminal();


            JSONObject paymentProcessingResult = new JSONObject();

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String amountInCents = convertDollarsToCents("0");
            String randomTransactionId = generateRandomTransactionId(9);

            JSONObject insertIntoTransactionsObj = new JSONObject();
            String timeC = ConstantsAndUtilities.currentTime();
            insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE, timeC);
            insertIntoTransactionsObj.put(dbVar.CREATED_DATE, timeC);
            insertIntoTransactionsObj.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
            insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local, "local");
            insertIntoTransactionsObj.put("pos_transaction_reference_id", randomTransactionId);
            String posTerminalTransactionReferenceID = randomTransactionId;
            insertIntoTransactionsObj.put("card_terminal_type", terminalCompany);
            insertIntoTransactionsObj.put("given_amount", "0");
            insertIntoTransactionsObj.put("employee", ConstantsAndUtilities.userid);
            insertIntoTransactionsObj.put("initiated_time", timeC);
            insertIntoTransactionsObj.put("transaction_type", "BATCH CLOSE");
            insertIntoTransactionsObj.put("invoice_id", "");
            MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

            if (terminalCompany.equals("PAX")) {
                PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                paymentProcessingResult = paxProcessing.closeTheBatch();
                System.out.println("paymentProcessingResult - " + paymentProcessingResult.toString());
                if (paymentProcessingResult.has("result_status")) {

                    timeC = ConstantsAndUtilities.currentTime();
                    updateTransactionDetailsObj.put("terminal_transaction_id", "");
                    updateTransactionDetailsObj.put("terminal_transaction_auth_code", "");
                    updateTransactionDetailsObj.put("response_message", paymentProcessingResult.get("result_status"));
                    updateTransactionDetailsObj.put("completed_time", timeC);
                    updateTransactionDetailsObj.put(dbVar.MODIFIED_DATE, timeC);
                    updateTransactionDetailsObj.put("response_json", paymentProcessingResult.toString());
                    if (!paymentProcessingResult.get("result_status").equals("OK")) {
                        updateTransactionDetailsObj.put("status", "Cancelled");
                        sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", posTerminalTransactionReferenceID);
                    } else {
                        updateTransactionDetailsObj.put("status", "Completed");
                        JSONObject finishedTransactionAsVoided = new JSONObject();
                        finishedTransactionAsVoided.put("void_status", "Yes");
                        finishedTransactionAsVoided.put(dbVar.MODIFIED_DATE, timeC);
                        sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", posTerminalTransactionReferenceID);
                    }
                    return paymentProcessingResult;
                }
            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return updateTransactionDetailsObj;
    }
    public JSONObject processEBTPaymentInDollars(String transactionAmountInDollar)
    {
        JSONObject paymentProcessingResult = new JSONObject();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String amountInCents = convertDollarsToCents(transactionAmountInDollar);
        String randomTransactionId = generateRandomTransactionId(9);
        System.out.println("CardPaymentProcessingMaster processing an amount of "+transactionAmountInDollar+" and total amount in cents is "+amountInCents+" And random transaction ID is "+randomTransactionId);

        String terminalCompany = "PAX";

        terminalCompany = preferredCardProcessingTerminal();
        JSONObject insertIntoTransactionsObj = new JSONObject();
        try {
            DatabaseVariables dbVar = new DatabaseVariables();
            String timeC = ConstantsAndUtilities.currentTime();
            insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.CREATED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.UNIQUE_ID,ConstantsAndUtilities.randomValue());
            insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local,"local");
            insertIntoTransactionsObj.put("pos_transaction_reference_id",randomTransactionId);
            insertIntoTransactionsObj.put("card_terminal_type",terminalCompany);
            insertIntoTransactionsObj.put("given_amount",transactionAmountInDollar);
            insertIntoTransactionsObj.put("tip_amount","0");
            insertIntoTransactionsObj.put("card_holder_name","");
            insertIntoTransactionsObj.put("edc_type","");
            insertIntoTransactionsObj.put("employee",ConstantsAndUtilities.userid);
            insertIntoTransactionsObj.put("initiated_time",timeC);

            MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

            if(terminalCompany.equals("PAX"))
            {
                PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                paymentProcessingResult = paxProcessing.initializeTheEBTTransactionWithAmount(amountInCents,randomTransactionId);
            }
        }catch (Exception exp)
        {
            exp.printStackTrace();
        }
        return paymentProcessingResult;
    }
    public JSONObject processPaymentInDollars(String transactionAmountInDollar)
    {
        JSONObject paymentProcessingResult = new JSONObject();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String amountInCents = convertDollarsToCents(transactionAmountInDollar);
        String randomTransactionId = generateRandomTransactionId(9);
        System.out.println("CardPaymentProcessingMaster processing an amount of "+transactionAmountInDollar+" and total amount in cents is "+amountInCents+" And random transaction ID is "+randomTransactionId);

        String terminalCompany = "PAX";

        terminalCompany = preferredCardProcessingTerminal();
        JSONObject insertIntoTransactionsObj = new JSONObject();
        try {
            DatabaseVariables dbVar = new DatabaseVariables();
            String timeC = ConstantsAndUtilities.currentTime();
            insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.CREATED_DATE,timeC);
            insertIntoTransactionsObj.put(dbVar.UNIQUE_ID,ConstantsAndUtilities.randomValue());
            insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local,"local");
            insertIntoTransactionsObj.put("pos_transaction_reference_id",randomTransactionId);
            insertIntoTransactionsObj.put("card_terminal_type",terminalCompany);
            insertIntoTransactionsObj.put("given_amount",transactionAmountInDollar);
            insertIntoTransactionsObj.put("tip_amount","0");
            insertIntoTransactionsObj.put("card_holder_name","");
            insertIntoTransactionsObj.put("edc_type","");
            insertIntoTransactionsObj.put("employee",ConstantsAndUtilities.userid);
            insertIntoTransactionsObj.put("initiated_time",timeC);

            MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

            if(terminalCompany.equals("PAX"))
            {
                PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                paymentProcessingResult = paxProcessing.initializeTheTransactionWithAmount(amountInCents,randomTransactionId);
            }
        }catch (Exception exp)
        {
            exp.printStackTrace();
        }
        return paymentProcessingResult;
    }
    public JSONObject returnTransactionVoided(String posTerminalTransactionReferenceID,String TerminalTransactionReferenceID)
    {
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        JSONObject updateTransactionDetailsObj = new JSONObject();
        try {
            ArrayList<JSONObject> cardTransactionDetails = sqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE pos_transaction_reference_id='" + posTerminalTransactionReferenceID + "'");
            // if transaction is already voided, donot void again
            if (cardTransactionDetails.size() == 0) {
                return new JSONObject();
            }

            JSONObject paymentProcessingResult = new JSONObject();
            JSONObject cardTransactionObj = cardTransactionDetails.get(0);
            String rowIDOfTransaction = String.valueOf(cardTransactionObj.get("_id"));
            if (cardTransactionObj.get("void_status").equals("Yes")) {
                return new JSONObject();
            } else {
                String voidedInvoiceId = String.valueOf(cardTransactionObj.get("invoice_id"));
                String transactionRefNumForVoid = String.valueOf(cardTransactionObj.get("terminal_transaction_id"));
                String transactionAmountForVoid = String.valueOf(cardTransactionObj.get("transaction_amount"));
                String posTransactionRefNumForVoid = String.valueOf(cardTransactionObj.get("pos_transaction_reference_id"));
                String terminalCompany = "PAX";

                terminalCompany = preferredCardProcessingTerminal();


                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                String amountInCents = convertDollarsToCents(transactionAmountForVoid);
                String randomTransactionId = generateRandomTransactionId(9);

                JSONObject insertIntoTransactionsObj = new JSONObject();
                String timeC = ConstantsAndUtilities.currentTime();
                insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE, timeC);
                insertIntoTransactionsObj.put(dbVar.CREATED_DATE, timeC);
                insertIntoTransactionsObj.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local, "local");
                insertIntoTransactionsObj.put("pos_transaction_reference_id", randomTransactionId);
                insertIntoTransactionsObj.put("card_terminal_type", terminalCompany);
                insertIntoTransactionsObj.put("given_amount", transactionAmountForVoid);
                insertIntoTransactionsObj.put("employee", ConstantsAndUtilities.userid);
                insertIntoTransactionsObj.put("initiated_time", timeC);
                insertIntoTransactionsObj.put("transaction_type", "VOID");
                insertIntoTransactionsObj.put("invoice_id", voidedInvoiceId);
                MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

                if (terminalCompany.equals("PAX")) {
                    PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                    String tenderType = "DEBIT";

                    String edcTypeOfTransaction = String.valueOf(cardTransactionObj.get("edc_type"));
                    if (edcTypeOfTransaction.equals("Credit")) {
                        tenderType = "CREDIT";
                    }

                    paymentProcessingResult = paxProcessing.voidTheRefundWithReferenceID(transactionRefNumForVoid, randomTransactionId, amountInCents, posTransactionRefNumForVoid, tenderType);
                    if (paymentProcessingResult.has("result_status")) {
                        String resultStatus = String.valueOf(paymentProcessingResult.get("result_status"));
                        String ECRtransactionID = "";
                        JSONObject tempTransactionDetails = paymentProcessingResult;
                        if (tempTransactionDetails.has("result_status")) {
                            resultStatus = String.valueOf(tempTransactionDetails.get("result_status"));
                            ECRtransactionID = String.valueOf(tempTransactionDetails.get("generated_transaction_reference_id"));
                            timeC = ConstantsAndUtilities.currentTime();
                            updateTransactionDetailsObj.put("terminal_transaction_id", String.valueOf(tempTransactionDetails.get("reference_number")));
                            updateTransactionDetailsObj.put("terminal_transaction_auth_code", String.valueOf(tempTransactionDetails.get("auth_code")));
                            updateTransactionDetailsObj.put("response_message", resultStatus);
                            updateTransactionDetailsObj.put("completed_time", timeC);
                            updateTransactionDetailsObj.put(dbVar.MODIFIED_DATE, timeC);
                            updateTransactionDetailsObj.put("response_json", tempTransactionDetails.toString());
                            if (!resultStatus.equals("OK")) {
                                updateTransactionDetailsObj.put("status", "Cancelled");
                            } else {
                                updateTransactionDetailsObj.put("status", "Completed");
                                JSONObject finishedTransactionAsVoided = new JSONObject();
                                finishedTransactionAsVoided.put("void_status", "Yes");
                                finishedTransactionAsVoided.put(dbVar.MODIFIED_DATE, timeC);
                                sqlObj.executeUpdate("card_transactions_processing", finishedTransactionAsVoided, "pos_transaction_reference_id", posTerminalTransactionReferenceID);
                            }
                        }

                        if (!ECRtransactionID.equals("")) {
                            sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", ECRtransactionID);
                        }
                        return paymentProcessingResult;
                    }
                }

            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return updateTransactionDetailsObj;
    }
    public JSONObject reportRequest()
    {
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        JSONObject updateTransactionDetailsObj = new JSONObject();
        try {
            String terminalCompany = "PAX";

            terminalCompany = preferredCardProcessingTerminal();


            JSONObject paymentProcessingResult = new JSONObject();

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String amountInCents = convertDollarsToCents("0");
            String randomTransactionId = generateRandomTransactionId(9);

            JSONObject insertIntoTransactionsObj = new JSONObject();
            String timeC = ConstantsAndUtilities.currentTime();
            insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE, timeC);
            insertIntoTransactionsObj.put(dbVar.CREATED_DATE, timeC);
            insertIntoTransactionsObj.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
            insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local, "local");
            insertIntoTransactionsObj.put("pos_transaction_reference_id", randomTransactionId);
            String posTerminalTransactionReferenceID = randomTransactionId;
            insertIntoTransactionsObj.put("card_terminal_type", terminalCompany);
            insertIntoTransactionsObj.put("given_amount", "0");
            insertIntoTransactionsObj.put("employee", ConstantsAndUtilities.userid);
            insertIntoTransactionsObj.put("initiated_time", timeC);
            insertIntoTransactionsObj.put("transaction_type", "REPORT RESPONSE");
            insertIntoTransactionsObj.put("invoice_id", "");
            MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

            if (terminalCompany.equals("PAX")) {
                PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                paymentProcessingResult = paxProcessing.localTotalReportGenerate();
                System.out.println("paymentProcessingResult report request - " + paymentProcessingResult.toString());
                if (paymentProcessingResult.has("result_status")) {
                    timeC = ConstantsAndUtilities.currentTime();
                    updateTransactionDetailsObj.put("terminal_transaction_id", "");
                    updateTransactionDetailsObj.put("terminal_transaction_auth_code", "");
                    updateTransactionDetailsObj.put("response_message", paymentProcessingResult.get("result_status"));
                    updateTransactionDetailsObj.put("completed_time", timeC);
                    updateTransactionDetailsObj.put(dbVar.MODIFIED_DATE, timeC);
                    updateTransactionDetailsObj.put("response_json", paymentProcessingResult.toString());
                    if (!paymentProcessingResult.get("result_status").equals("OK")) {
                        updateTransactionDetailsObj.put("status", "Cancelled");
                        sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", posTerminalTransactionReferenceID);

                    } else {
                        updateTransactionDetailsObj.put("status", "Completed");
                        JSONObject finishedTransactionAsVoided = new JSONObject();
                        finishedTransactionAsVoided.put("void_status", "Yes");
                        finishedTransactionAsVoided.put(dbVar.MODIFIED_DATE, timeC);
                        sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", posTerminalTransactionReferenceID);
                    }
                    return paymentProcessingResult;
                }
            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return updateTransactionDetailsObj;
    }
    public JSONObject saleTransactionVoided(String posTerminalTransactionReferenceID,String TerminalTransactionReferenceID)
    {
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        JSONObject updateTransactionDetailsObj = new JSONObject();
        JSONObject paymentProcessingResult = new JSONObject();
        try {
            ArrayList<JSONObject> cardTransactionDetails = sqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE pos_transaction_reference_id='" + posTerminalTransactionReferenceID + "'");
            // if transaction is already voided, donot void again
            if (cardTransactionDetails.size() == 0) {
                return new JSONObject();
            }
            JSONObject cardTransactionObj = cardTransactionDetails.get(0);
            String rowIDOfTransaction = String.valueOf(cardTransactionObj.optString("_id"));
            if (cardTransactionObj.optString("void_status").equals("Yes")) {
                return new JSONObject();
            } else {
                String voidedInvoiceId = String.valueOf(cardTransactionObj.optString("invoice_id"));
                String transactionRefNumForVoid = String.valueOf(cardTransactionObj.optString("terminal_transaction_id"));
                String transactionAmountForVoid = String.valueOf(cardTransactionObj.optString("transaction_amount"));
                String posTransactionRefNumForVoid = String.valueOf(cardTransactionObj.optString("pos_transaction_reference_id"));
                String terminalCompany = "PAX";

                terminalCompany = preferredCardProcessingTerminal();


                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                String amountInCents = convertDollarsToCents(transactionAmountForVoid);
                String randomTransactionId = generateRandomTransactionId(9);

                JSONObject insertIntoTransactionsObj = new JSONObject();
                String timeC = ConstantsAndUtilities.currentTime();
                insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE, timeC);
                insertIntoTransactionsObj.put(dbVar.CREATED_DATE, timeC);
                insertIntoTransactionsObj.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local, "local");
                insertIntoTransactionsObj.put("pos_transaction_reference_id", randomTransactionId);
                insertIntoTransactionsObj.put("card_terminal_type", terminalCompany);
                insertIntoTransactionsObj.put("given_amount", transactionAmountForVoid);
                insertIntoTransactionsObj.put("employee", ConstantsAndUtilities.userid);
                insertIntoTransactionsObj.put("initiated_time", timeC);
                insertIntoTransactionsObj.put("transaction_type", "VOID");
                insertIntoTransactionsObj.put("invoice_id", voidedInvoiceId);
                MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);

                if (terminalCompany.equals("PAX")) {
                    PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                    String tenderType = "DEBIT";

                    String edcTypeOfTransaction = String.valueOf(cardTransactionObj.get("edc_type"));
                    if (edcTypeOfTransaction.equals("Credit")) {
                        tenderType = "CREDIT";
                    }
                    if (edcTypeOfTransaction.equals("EBT")) {
                        tenderType = "EBT";
                    }

                    paymentProcessingResult = paxProcessing.voidTheTransactionWithReferenceID(transactionRefNumForVoid, randomTransactionId, amountInCents, posTransactionRefNumForVoid, tenderType);
                    if (paymentProcessingResult.has("result_status")) {
                        String resultStatus = String.valueOf(paymentProcessingResult.get("result_status"));
                        String ECRtransactionID = "";
                        JSONObject tempTransactionDetails = paymentProcessingResult;
                        if (tempTransactionDetails.has("result_status")) {
                            resultStatus = String.valueOf(tempTransactionDetails.get("result_status"));
                            ECRtransactionID = String.valueOf(tempTransactionDetails.get("generated_transaction_reference_id"));
                            timeC = ConstantsAndUtilities.currentTime();
                            updateTransactionDetailsObj.put("terminal_transaction_id", String.valueOf(tempTransactionDetails.get("reference_number")));
                            updateTransactionDetailsObj.put("terminal_transaction_auth_code", String.valueOf(tempTransactionDetails.get("auth_code")));
                            updateTransactionDetailsObj.put("response_message", resultStatus);
                            updateTransactionDetailsObj.put("completed_time", timeC);
                            updateTransactionDetailsObj.put(dbVar.MODIFIED_DATE, timeC);
                            updateTransactionDetailsObj.put("response_json", tempTransactionDetails.toString());
                            if (!resultStatus.equals("OK")) {
                                updateTransactionDetailsObj.put("status", "Cancelled");
                            } else {
                                updateTransactionDetailsObj.put("status", "Completed");
                                JSONObject finishedTransactionAsVoided = new JSONObject();
                                finishedTransactionAsVoided.put("void_status", "Yes");
                                finishedTransactionAsVoided.put(dbVar.MODIFIED_DATE, timeC);
                                sqlObj.executeUpdate("card_transactions_processing", finishedTransactionAsVoided, "pos_transaction_reference_id", posTerminalTransactionReferenceID);
                            }
                        }

                        if (!ECRtransactionID.equals("")) {
                            sqlObj.executeUpdate("card_transactions_processing", updateTransactionDetailsObj, "pos_transaction_reference_id", ECRtransactionID);
                        }

                    }
                }

            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return paymentProcessingResult;
    }
    public void paymentOfInvoiceCancelled(String voidedInvoiceId)
    {
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        System.out.println("Void the invoice and return the payment "+voidedInvoiceId);
        ArrayList<JSONObject> InvoiceDetailsForTransaction = sqlObj.executeRawqueryJSON("SELECT * FROM "+ dbVar.INVOICE_TOTAL_TABLE + " WHERE invoice_id='" + voidedInvoiceId + "'");
        if(InvoiceDetailsForTransaction.size()==0){ return; }
        JSONObject invoiceROw = InvoiceDetailsForTransaction.get(0);
        String ModeOfPayment = String.valueOf(invoiceROw.optString(dbVar.INVOICE_PAYMENT_TYPE));
        if(ModeOfPayment.equals("Card")) {
            String transactionId = String.valueOf(invoiceROw.optString("cheque_no"));
            if(!transactionId.equals(""))
            {
                ArrayList<JSONObject> cardTransactionDetails = sqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE pos_transaction_reference_id='" + transactionId + "'");
                // if transaction is already voided, donot void again
                if(cardTransactionDetails.size()==0){
                    return;
                }

                JSONObject cardTransactionObj = cardTransactionDetails.get(0);
                String rowIDOfTransaction = String.valueOf(cardTransactionObj.optString("_id"));
                if(cardTransactionObj.optString("void_status").equals("Yes"))
                {
                    return;
                }else{
                    String transactionRefNumForVoid = String.valueOf(cardTransactionObj.optString("terminal_transaction_id"));
                    String transactionAmountForVoid = String.valueOf(cardTransactionObj.optString("transaction_amount"));
                    String posTransactionRefNumForVoid = String.valueOf(cardTransactionObj.optString("pos_transaction_reference_id"));
                    String terminalCompany = "PAX";

                    terminalCompany = preferredCardProcessingTerminal();


                    JSONObject paymentProcessingResult = new JSONObject();

                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(2);
                    nf.setMaximumFractionDigits(2);
                    String amountInCents = convertDollarsToCents(transactionAmountForVoid);
                    String randomTransactionId = generateRandomTransactionId(9);

                    JSONObject insertIntoTransactionsObj = new JSONObject();
                    try {
                        String timeC = ConstantsAndUtilities.currentTime();
                        insertIntoTransactionsObj.put(dbVar.MODIFIED_DATE, timeC);
                        insertIntoTransactionsObj.put(dbVar.CREATED_DATE, timeC);
                        insertIntoTransactionsObj.put(dbVar.UNIQUE_ID, ConstantsAndUtilities.randomValue());
                        insertIntoTransactionsObj.put(dbVar.COMBO_ITEMS_server_local, "local");
                        insertIntoTransactionsObj.put("pos_transaction_reference_id", randomTransactionId);
                        insertIntoTransactionsObj.put("card_terminal_type", terminalCompany);
                        insertIntoTransactionsObj.put("given_amount", transactionAmountForVoid);
                        insertIntoTransactionsObj.put("employee", ConstantsAndUtilities.userid);
                        insertIntoTransactionsObj.put("initiated_time", timeC);
                        insertIntoTransactionsObj.put("transaction_type", "VOID");
                        insertIntoTransactionsObj.put("invoice_id", voidedInvoiceId);
                        MainActivity.mySqlObj.executeInsert("card_transactions_processing", insertIntoTransactionsObj);
                    }catch (JSONException exp){
                        exp.printStackTrace();
                    }
                    if(terminalCompany.equals("PAX"))
                    {
                        PAXPaymentProcessing paxProcessing = new PAXPaymentProcessing();
                        String tenderType = "DEBIT";
                        String edcTypeOfTransaction = String.valueOf(cardTransactionObj.optString("edc_type"));
                        if(edcTypeOfTransaction.equals("Credit")){ tenderType = "CREDIT"; }
                        paymentProcessingResult = paxProcessing.voidTheTransactionWithReferenceID(transactionRefNumForVoid,randomTransactionId,amountInCents,posTransactionRefNumForVoid,tenderType);
                        if(paymentProcessingResult.has("result_status")) {
                            String resultStatus = String.valueOf(paymentProcessingResult.optString("result_status"));
                            String ECRtransactionID = "";
                            JSONObject updateTransactionDetailsObj = new JSONObject();
                            JSONObject tempTransactionDetails = paymentProcessingResult;
                            if(tempTransactionDetails.has("result_status"))
                            {
                                try {
                                    resultStatus = String.valueOf(tempTransactionDetails.optString("result_status"));
                                    ECRtransactionID = String.valueOf(tempTransactionDetails.optString("generated_transaction_reference_id"));
                                    String timeC = ConstantsAndUtilities.currentTime();
                                    updateTransactionDetailsObj.put("terminal_transaction_id", String.valueOf(tempTransactionDetails.optString("reference_number")));
                                    updateTransactionDetailsObj.put("terminal_transaction_auth_code", String.valueOf(tempTransactionDetails.optString("auth_code")));
                                    updateTransactionDetailsObj.put("response_message", resultStatus);
                                    updateTransactionDetailsObj.put("completed_time", timeC);
                                    updateTransactionDetailsObj.put(dbVar.MODIFIED_DATE, timeC);
                                    updateTransactionDetailsObj.put("response_json", tempTransactionDetails.toString());
                                    if (!resultStatus.equals("OK")) {
                                        updateTransactionDetailsObj.put("status", "Cancelled");
                                    } else {
                                        updateTransactionDetailsObj.put("status", "Completed");
                                        JSONObject finishedTransactionAsVoided = new JSONObject();
                                        finishedTransactionAsVoided.put("void_status", "Yes");
                                        finishedTransactionAsVoided.put(dbVar.MODIFIED_DATE, timeC);
                                        sqlObj.executeUpdate("card_transactions_processing", finishedTransactionAsVoided, "pos_transaction_reference_id", transactionId);
                                    }
                                }catch (JSONException exp){
                                    exp.printStackTrace();
                                }
                            }

                            if(!ECRtransactionID.equals(""))
                            {
                                sqlObj.executeUpdate("card_transactions_processing",updateTransactionDetailsObj,"pos_transaction_reference_id",ECRtransactionID);
                            }
                        }
                    }

                }
            }
        }
    }
    private String preferredCardProcessingTerminal() {
        DatabaseVariables dbVar = new DatabaseVariables();

        return dbVar.valueForAttribute(DatabaseVariables.CARD_PROCESSING_TERMINAL_TABLE,DatabaseVariables.CARD_PROCESSING_MANUFACTURER);

    }
    private static Random rnd = new Random();
    private String generateRandomTransactionId(int digCount) {
        StringBuilder sb = new StringBuilder(digCount);
        for(int i=0; i < digCount; i++)
            sb.append((char)('0' + rnd.nextInt(10)));
        return sb.toString();
    }

    public String convertDollarsToCents(String transactionAmountInDollar)
    {
        String totalAmountInCents = "0";
        try {

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String numberFormat = nf.format(Double.parseDouble(transactionAmountInDollar));
            totalAmountInCents = numberFormat = numberFormat.replace(".", "");

        }catch (Exception exp){
            exp.printStackTrace();
        }
        return totalAmountInCents;
    }
    public String allCardTransactionDetailsForInvoice(String printInvoiceId)
    {
        String printContent = "";

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        MySQLJDBC sqlObj = MainActivity.mySqlObj;
        DatabaseVariables dbVar = new DatabaseVariables();
        ArrayList<JSONObject> cardTransactionDetails = sqlObj.executeRawqueryJSON("SELECT * FROM card_transactions_processing WHERE invoice_id='" + printInvoiceId + "' AND transaction_type='SALE' AND response_message='OK'");
        System.out.println(cardTransactionDetails.toString());
        if(cardTransactionDetails.size()==0)
        {
            return printContent;
        }
        else {
//            JSONParser parser = new JSONParser();

            for(int b=0; b < cardTransactionDetails.size(); b++)
            {
                JSONObject transactionDetailsObj = cardTransactionDetails.get(b);
                printContent += "<tr><td colspan=\"4\" style=\"border-top:2px solid #000;\">&nbsp;</td></tr>";
                String cardType = "";
                String expiryDate = "";
                String cardHolderData = "";
                String cardInfoData = "";
                String ecrRefData = "";
                String appLabData = "";
                cardType = String.valueOf(transactionDetailsObj.optString("edc_type"));
                String transactionAmount = String.valueOf(transactionDetailsObj.optString("transaction_amount"));
                String cardHolderName = String.valueOf(transactionDetailsObj.optString("card_holder_name"));
                String cardNumberDetails = "";
                String transactionAuthorizationNumber = "";//String.valueOf(transactionDetailsObj.get("auth_code"));
                try {
                    JSONObject responseJSON  = new JSONObject(transactionDetailsObj.optString("response_json"));//
                    String extraData = String.valueOf(responseJSON.optString("extra_data"));
                    transactionAuthorizationNumber = String.valueOf(responseJSON.optString("auth_code"));
                    expiryDate = PAXPaymentProcessing.getTagValue(extraData,"ExpDate");
                    String ecrRefNum = PAXPaymentProcessing.getTagValue(extraData,"ECRRefNum");
                    String appLab = PAXPaymentProcessing.getTagValue(extraData,"APPLAB");
                    String cardNum = PAXPaymentProcessing.getTagValue(extraData,"CARDBIN");
                    if(expiryDate.equals("")){ expiryDate = "XXXX";}
                    System.out.println("Extra data is "+extraData);
                    expiryDate = ConstantsAndUtilities.addChar(expiryDate, '/',2);
                    if (!cardHolderName.equals(""))
                    {
                        cardHolderData = "<tr><td colspan=\"4\">Card Holder Name : "+cardHolderName+"</td></tr>";
                    }
                    if(!ecrRefNum.equals(""))
                    {
                        ecrRefData =  "<tr><td colspan=\"2\">ECR Transaction ID : </td><td colspan=\"2\">"+ecrRefNum+"</td></tr>";
                    }
                    if(!cardNum.equals(""))
                    {
                        cardNum = String.format("%-16s", cardNum).replace(' ', '*');
                        cardNumberDetails =  "<tr><td colspan=\"2\">Card Number : </td><td colspan=\"2\">"+cardNum+"</td></tr>";
                    }
                    if(!appLab.equals(""))
                    {
                        appLabData =  "<tr><td colspan=\"4\">Card : "+appLab+"</td></tr>";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // ExpDate
                printContent += "<tr><td colspan=\"2\">Card Type: "+cardType+"</td><td colspan=\"2\">Expiry Date: "+expiryDate+"</td></tr>";
                printContent += appLabData;
                printContent += cardNumberDetails;
                printContent += cardHolderData;
                printContent += ecrRefData;
                printContent += "<tr><td colspan=\"2\" >Transaction Amount</td><td colspan=\"2\" align=\"left\">$&nbsp;"+nf.format(Double.parseDouble(transactionAmount))+"</td></tr>";
                printContent += "<tr><td colspan=\"2\" >Authorization Number</td><td colspan=\"2\">"+transactionAuthorizationNumber+"</td></tr>";
                printContent += "<tr><td colspan=\"4\" style=\"border-bottom:2px solid #000;\">&nbsp;</td></tr>";
            }

        }
        return printContent;
    }
}
