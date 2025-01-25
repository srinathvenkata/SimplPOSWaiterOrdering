package com.simplpos.waiterordering.helpers.paymentprocessors;

import android.util.Log;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.ReportResponse;
import com.simplpos.waiterordering.helpers.DatabaseVariables;
import com.simplpos.waiterordering.helpers.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class PAXPaymentProcessing {

    public JSONObject retrieveBatchRequest()
    {
        JSONObject obj = new JSONObject();
        CommSetting commSetting = new CommSetting();
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSeialPort("COM3");
        // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
        commSetting.setType(CommSetting.TCP);
        commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
        commSetting.setDestPort(portNumberOfPAXPinPad());

        commSetting.setTimeOut("120000");
        PosLink posLink = new PosLink();
        // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
        posLink.SetCommSetting(commSetting);

        ReportRequest reportRequest = new ReportRequest();
//        reportRequest.TransType = reportRequest.ParseTransType(requestEntity.getTransType());
//        reportRequest.EDCType = reportRequest.ParseEDCType(requestEntity.getEdcType());
//        reportRequest.CardType = reportRequest.ParseCardType(requestEntity.getCardType());
        posLink.ReportRequest = reportRequest;
        ProcessTransResult processTransResult = posLink.ProcessTrans();

        return obj;
    }
    public JSONObject initializeTheEBTTransactionWithAmount(String transAmount, String transactionGeneratedId) {
        // Setup comm setting.
        JSONObject returnObj = new JSONObject();
        CommSetting commSetting = new CommSetting();
        try{

        //        System.out.println(commSetting.getType()+ " is the communication type");
        //        commSetting.setType(CommSetting.BT);
        //        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
        //        commSetting.setType(CommSetting.UART);
        //        commSetting.setSerialPort("COM3");
                // Create a POSLink object. In Android, it needs to pass context.
        //        commSetting.setDestPort("10009");
        //        commSetting.setSerialPort("COM3");
        //        commSetting.setType(CommSetting.TCP);
                commSetting.setType(CommSetting.TCP);
                commSetting.setDestIP(ipAddressOfPAXPinPad());
        //        commSetting.setBaudRate("9600");
                commSetting.setDestPort(portNumberOfPAXPinPad());

                commSetting.setTimeOut("120000");
                PosLink posLink = new PosLink();
                // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
                posLink.SetCommSetting(commSetting);
                // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
                PaymentRequest pay = new PaymentRequest();
                // Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
                pay.TenderType = pay.ParseTenderType("EBT_FOODSTAMP");
                //  pay.TenderType =  pay.ParseTenderType("EBT_FOODSTAMP");

        //        pay.TenderType = pay.ParseTenderType("CREDIT");
                pay.TransType = pay.ParseTransType("SALE");

        //        pay.TransType = pay.ParseTransType("RETURN");
        //        pay.AuthCode="01939R";
                pay.ECRTransID = transactionGeneratedId;
        //        pay.AuthCode = "77778888";
                // Your unique ID for this transaction
                pay.ECRRefNum = transactionGeneratedId;
                //Optional fields.
                pay.Amount = transAmount;
                if(taxEnabled().equals("Yes"))
                {
                    pay.TaxAmt = "0";
                }
                pay.ExtData = "<TipRequest>"+   (tipEnabled()  )    +"</TipRequest>";
                posLink.PaymentRequest = pay;



                ProcessTransResult transResult = posLink.ProcessTrans();
                if(transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    System.out.println("all is well");
                    PaymentResponse response = posLink.PaymentResponse;
                    System.out.println(response);

                    System.out.println("Transaction status is "+response.ResultTxt);
                    System.out.println("Extra Data is  "+response.ExtData);
                    System.out.println("Tip Amount is "+(getTagValue(response.ExtData,"TipAmount")));
                    System.out.println("Card Holder Name is "+(getTagValue(response.ExtData,"CARDHOLDER")));
                    System.out.println("Expiry date is "+(getTagValue(response.ExtData,"ExpDate")));
                    System.out.println("Edc type is "+(getTagValue(response.ExtData,"EDCTYPE")));

        //            System.out.println("First Name is "+(getTagValue(response.ExtData,"FirstName")));
        //            System.out.println("Last Name is "+(getTagValue(response.ExtData,"LastName")));
                    System.out.println(response.CardInfo+" is the card info");
                    System.out.println(response.RemainingBalance+" is the remaining balance");
                    System.out.println(response.RefNum+" is the transaction reference number");
                    System.out.println(response.ApprovedAmount+" is the approved amount");
                    System.out.println(response.AuthCode+" is the authorization code");
                    System.out.println(response.CardType+" is the card type");
                    System.out.println(response.CardInfo.CardBin+" is the card info");
                    System.out.println(response.CardInfo.ProgramType+" is the card info");
                    System.out.println(response.CardInfo.NewCardBin+" is the card info");
                    System.out.println(response.AuthorizationResponse+" is the authorization response");
                    System.out.println(response.ECRTransID+" is the ECR Transaction ID");
                    System.out.println(response.RefNum+" is the Reference num");
                    System.out.println(response.RetrievalReferenceNumber + " is the retreival reference number");
                    System.out.println((response.PaymentTransInfo).toString()+" is the ECR Transaction ID");

                    try{
                        returnObj.put("reference_number",response.RefNum);
                        returnObj.put("tip_amount", (getTagValue(response.ExtData,"TipAmount")) );
                        returnObj.put("remaining_balance",response.RemainingBalance);
        //                response.TransactionRemainingAmount;
                        returnObj.put("card_holder_name",(getTagValue(response.ExtData,"CARDHOLDER")));
                        returnObj.put("edc_type",(getTagValue(response.ExtData,"EDCTYPE")));

                        returnObj.put("approved_amount",response.ApprovedAmount);
                        if(response.ApprovedAmount.equals(""))
                        {
                            returnObj.put("approved_amount","0.00");

                        }

                        if(response.ResultTxt.equals("DECLINE"))
                        { returnObj.put("approved_amount","0"); }
                        else{
                            returnObj.put("approved_amount",response.ApprovedAmount);
                        }
                        returnObj.put("auth_code",response.AuthCode);
                        returnObj.put("result_status",response.ResultTxt);
                        returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
                        returnObj.put("given_amount",transAmount);
                        returnObj.put("extra_data",response.ExtData);
                    }catch (Exception exp){
                        exp.printStackTrace();
                    }
                } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                    System.out.println("TIMEOUT ERROR: " + transResult.Msg);

                    returnObj.put("reference_number","");
                    returnObj.put("approved_amount","0.00");
                    returnObj.put("tip_amount", "0" );
                    returnObj.put("card_holder_name","");
                    returnObj.put("edc_type","");

                    returnObj.put("auth_code","");
                    returnObj.put("result_status",transResult.Msg);
                    returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
                    returnObj.put("given_amount",transAmount);

                } else {
                    System.out.println("OTHER ERROR: " + transResult.Msg);
                    returnObj.put("reference_number","");
                    returnObj.put("approved_amount","0.00");
                    returnObj.put("tip_amount", "0" );
                    returnObj.put("card_holder_name","");
                    returnObj.put("edc_type","");
                    returnObj.put("auth_code","");
                    returnObj.put("result_status",transResult.Msg);
                    returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
                    returnObj.put("given_amount",transAmount);
                }
        }catch (JSONException exp){
            exp.printStackTrace();;
        }
        return returnObj;
    }
    public JSONObject initializeTheTransactionWithAmount(String transAmount, String transactionGeneratedId) {
        // Setup comm setting.
        JSONObject returnObj = new JSONObject();
        try {
            String settingIniFile = MyApplication.getAppContext().getFilesDir().getAbsolutePath() + "/SIMPLPOS/";
            Log.v("Paxpayment","initializeTheTransactionWithAmount called");
            CommSetting commSetting = new CommSetting();
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
        commSetting.setType(CommSetting.AIDL);
//        commSetting.setSerialPort("COM3");
            // Create a POSLink object. In Android, it needs to pass context.
        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
//            commSetting.setType(CommSetting.TCP);
//            commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
//            commSetting.setDestPort(portNumberOfPAXPinPad());

            commSetting.setTimeOut("120000");
            POSLinkAndroid.init(MyApplication.getAppContext());
            PosLink posLink = new PosLink(MyApplication.getAppContext());
//            PosLink posLink = new PosLink();
            // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
            posLink.SetCommSetting(commSetting);
            // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
            PaymentRequest pay = new PaymentRequest();
            // Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
            pay.TenderType = pay.ParseTenderType("CREDIT");
            //  pay.TenderType =  pay.ParseTenderType("EBT_FOODSTAMP");

//        pay.TenderType = pay.ParseTenderType("CREDIT");
            pay.TransType = pay.ParseTransType("SALE");

//        pay.TransType = pay.ParseTransType("RETURN");
//        pay.AuthCode="01939R";
            pay.ECRTransID = transactionGeneratedId;
//        pay.AuthCode = "77778888";
            // Your unique ID for this transaction
            pay.ECRRefNum = transactionGeneratedId;
            //Optional fields.
            pay.Amount = transAmount;
            if (taxEnabled().equals("Yes")) {
                pay.TaxAmt = "0";
            }
            pay.ExtData = "<TipRequest>" + (tipEnabled()) + "</TipRequest>";
            posLink.PaymentRequest = pay;


            ProcessTransResult transResult = posLink.ProcessTrans();
            if (transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                System.out.println("all is well");
                PaymentResponse response = posLink.PaymentResponse;
                System.out.println(response);

                System.out.println("Transaction status is "+response.ResultTxt);
                System.out.println("Extra Data is  "+response.ExtData);
                System.out.println("Tip Amount is "+(getTagValue(response.ExtData,"TipAmount")));
                System.out.println("Card Holder Name is "+(getTagValue(response.ExtData,"CARDHOLDER")));
                System.out.println("Expiry date is "+(getTagValue(response.ExtData,"ExpDate")));
                System.out.println("Edc type is "+(getTagValue(response.ExtData,"EDCTYPE")));

//            System.out.println("First Name is "+(getTagValue(response.ExtData,"FirstName")));
//            System.out.println("Last Name is "+(getTagValue(response.ExtData,"LastName")));
                System.out.println(response.CardInfo+" is the card info");

                System.out.println(response.RefNum+" is the transaction reference number");
                System.out.println(response.ApprovedAmount+" is the approved amount");
                System.out.println(response.AuthCode+" is the authorization code");
                System.out.println(response.CardType+" is the card type");
                System.out.println(response.CardInfo.CardBin+" is the card info");
                System.out.println(response.BogusAccountNum+" is the last four digits of card");
                System.out.println(response.CardInfo.ProgramType+" is the card info");
                System.out.println(response.CardInfo.NewCardBin+" is the card info");
                System.out.println(response.AuthorizationResponse+" is the authorization response");
                System.out.println(response.ECRTransID+" is the ECR Transaction ID");
                System.out.println(response.RefNum+" is the Reference num");
                System.out.println(response.RetrievalReferenceNumber + " is the retreival reference number");
                System.out.println((response.PaymentTransInfo).toString()+" is the ECR Transaction ID");

                try{
                    returnObj.put("reference_number",response.RefNum);
                    returnObj.put("tip_amount", (getTagValue(response.ExtData,"TipAmount")) );

                    returnObj.put("card_holder_name",(getTagValue(response.ExtData,"CARDHOLDER")));
                    returnObj.put("edc_type",(getTagValue(response.ExtData,"EDCTYPE")));
                    returnObj.put("last_four_digits_of_card",response.BogusAccountNum);
                    returnObj.put("approved_amount",response.ApprovedAmount);
                    if(response.ApprovedAmount.equals(""))
                    {
                        returnObj.put("approved_amount","0.00");

                    }

                    if(response.ResultTxt.equals("DECLINE"))
                    { returnObj.put("approved_amount","0"); }
                    else{
                        returnObj.put("approved_amount",response.ApprovedAmount);
                    }
                    returnObj.put("auth_code",response.AuthCode);
                    returnObj.put("result_status",response.ResultTxt);
                    returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
                    returnObj.put("given_amount",transAmount);
                    returnObj.put("extra_data",response.ExtData);
                }catch (Exception exp){
                    exp.printStackTrace();
                }
            } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                System.out.println("TIMEOUT ERROR: " + transResult.Msg);

                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0.00");
                returnObj.put("tip_amount", "0");
                returnObj.put("card_holder_name", "");
                returnObj.put("edc_type", "");

                returnObj.put("auth_code", "");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", transactionGeneratedId);
                returnObj.put("given_amount", transAmount);

            } else {
                System.out.println("OTHER ERROR: " + transResult.Msg);
                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0.00");
                returnObj.put("tip_amount", "0");
                returnObj.put("card_holder_name", "");
                returnObj.put("edc_type", "");
                returnObj.put("auth_code", "");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", transactionGeneratedId);
                returnObj.put("given_amount", transAmount);
            }
        }
        catch(JSONException exp){
            exp.printStackTrace();
        }
        return returnObj;
    }
    public JSONObject returnThePaymentWithAmount(String transAmount, String transactionGeneratedId)
    {
        JSONObject returnObj = new JSONObject();
        CommSetting commSetting = new CommSetting();
        try{
        // Setup comm setting.
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSerialPort("COM3");
        // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
        commSetting.setType(CommSetting.TCP);
        commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
        commSetting.setDestPort(portNumberOfPAXPinPad());

//        commSetting.setType(CommSetting.AIDL);
        

        commSetting.setTimeOut("120000");
        PosLink posLink = new PosLink();
        // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
        posLink.SetCommSetting(commSetting);
        // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
        PaymentRequest pay = new PaymentRequest();
        // Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
        pay.TenderType = pay.ParseTenderType("CREDIT");
//        pay.TenderType = pay.ParseTenderType("CREDIT");
        pay.TransType = pay.ParseTransType("RETURN");

//        pay.TransType = pay.ParseTransType("RETURN");
//        pay.AuthCode="01939R";
        pay.ECRTransID = transactionGeneratedId;
//        pay.AuthCode = "77778888";
        // Your unique ID for this transaction
        pay.ECRRefNum = transactionGeneratedId;
        //Optional fields.
        pay.Amount = transAmount;

        pay.ExtData = "";
        posLink.PaymentRequest = pay;



        ProcessTransResult transResult = posLink.ProcessTrans();
        if(transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            System.out.println("all is well");
            PaymentResponse response = posLink.PaymentResponse;
            System.out.println(response);

            System.out.println("Transaction status is "+response.ResultTxt);
            System.out.println("Extra Data is  "+response.ExtData);
            System.out.println("Tip Amount is "+(getTagValue(response.ExtData,"TipAmount")));
            System.out.println("Card Holder Name is "+(getTagValue(response.ExtData,"CARDHOLDER")));
            System.out.println("Expiry date is "+(getTagValue(response.ExtData,"ExpDate")));
            System.out.println("Edc type is "+(getTagValue(response.ExtData,"EDCTYPE")));

//            System.out.println("First Name is "+(getTagValue(response.ExtData,"FirstName")));
//            System.out.println("Last Name is "+(getTagValue(response.ExtData,"LastName")));
            System.out.println(response.CardInfo+" is the card info");

            System.out.println(response.RefNum+" is the transaction reference number");
            System.out.println(response.ApprovedAmount+" is the approved amount");
            System.out.println(response.AuthCode+" is the authorization code");
            System.out.println(response.CardType+" is the card type");
            System.out.println(response.CardInfo.CardBin+" is the card info");
            System.out.println(response.CardInfo.ProgramType+" is the card info");
            System.out.println(response.CardInfo.NewCardBin+" is the card info");
            System.out.println(response.AuthorizationResponse+" is the authorization response");
            System.out.println(response.ECRTransID+" is the ECR Transaction ID");
            System.out.println(response.RefNum+" is the Reference num");
            System.out.println(response.RetrievalReferenceNumber + " is the retreival reference number");
            System.out.println((response.PaymentTransInfo).toString()+" is the ECR Transaction ID");

                returnObj.put("reference_number",response.RefNum);
                returnObj.put("tip_amount", (getTagValue(response.ExtData,"TipAmount")) );

                returnObj.put("card_holder_name",(getTagValue(response.ExtData,"CARDHOLDER")));
                returnObj.put("edc_type",(getTagValue(response.ExtData,"EDCTYPE")));

                returnObj.put("approved_amount",response.ApprovedAmount);
                if(response.ApprovedAmount.equals(""))
                {
                    returnObj.put("approved_amount","0.00");

                }
                returnObj.put("auth_code",response.AuthCode);
                returnObj.put("result_status",response.ResultTxt);
                returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
                returnObj.put("given_amount",transAmount);

        } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            System.out.println("TIMEOUT ERROR: " + transResult.Msg);

            returnObj.put("reference_number","");
            returnObj.put("approved_amount","0.00");
            returnObj.put("tip_amount", "0" );
            returnObj.put("card_holder_name","");
            returnObj.put("edc_type","");

            returnObj.put("auth_code","");
            returnObj.put("result_status",transResult.Msg);
            returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
            returnObj.put("given_amount",transAmount);

        } else {
            System.out.println("OTHER ERROR: " + transResult.Msg);
            returnObj.put("reference_number","");
            returnObj.put("approved_amount","0.00");
            returnObj.put("tip_amount", "0" );
            returnObj.put("card_holder_name","");
            returnObj.put("edc_type","");
            returnObj.put("auth_code","");
            returnObj.put("result_status",transResult.Msg);
            returnObj.put("generated_transaction_reference_id",transactionGeneratedId);
            returnObj.put("given_amount",transAmount);
        }
    }   catch (Exception exp){
        exp.printStackTrace();
    }
        return returnObj;
    }

    private String tipEnabled() {
        DatabaseVariables dbVar = new DatabaseVariables();

        String tipEnabledValue =  dbVar.valueForAttribute(DatabaseVariables.CARD_PROCESSING_TERMINAL_TABLE,DatabaseVariables.CARD_PROCESSING_ENABLE_TIPS);
        if(tipEnabledValue.equals("") || tipEnabledValue.equals("No")){ return "0"; }
        else{ return "1"; }
    }


    private String taxEnabled() {

        DatabaseVariables dbVar = new DatabaseVariables();

        String tipEnabledValue =  dbVar.valueForAttribute(DatabaseVariables.CARD_PROCESSING_TERMINAL_TABLE,DatabaseVariables.CARD_PROCESSING_ENABLE_TAX);
        if(tipEnabledValue.equals("") || tipEnabledValue.equals("No")){ return "0"; }
        else{ return "1"; }
    }


    public JSONObject voidTheTransactionWithReferenceID(String transactionReferenceID,String transactionGeneratedId,String transAmount,String posTransactionRefNumForVoid,String tenderType) {
        System.out.println("POS transaction reference ID is "+posTransactionRefNumForVoid);
        System.out.println("Terminal transaction reference ID is "+transactionReferenceID);
        System.out.println("Tender Type is "+tenderType);

        // Setup comm setting.
        JSONObject returnObj = new JSONObject();
        CommSetting commSetting = new CommSetting();
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSerialPort("COM3");
        // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
        commSetting.setType(CommSetting.TCP);
        commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
        commSetting.setDestPort(portNumberOfPAXPinPad());

        commSetting.setTimeOut("120000");
        try {
            PosLink posLink = new PosLink();
            // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
            posLink.SetCommSetting(commSetting);
            // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
            PaymentRequest pay = new PaymentRequest();
            // Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
//            pay.TenderType = pay.ParseTenderType("DEBIT");

            pay.TenderType = pay.ParseTenderType(tenderType);
            pay.TransType = pay.ParseTransType("VOID SALE");

//        pay.TransType = pay.ParseTransType("RETURN");
//        pay.AuthCode="01939R";
            pay.ECRTransID = transactionGeneratedId;
//        pay.AuthCode = "77778888";
            // Your unique ID for this transaction
            pay.ECRRefNum = transactionGeneratedId;
            pay.OrigRefNum = transactionReferenceID;
            pay.OrigECRRefNum = posTransactionRefNumForVoid;

            posLink.PaymentRequest = pay;


            ProcessTransResult transResult = posLink.ProcessTrans();
            if (transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                System.out.println("all is well");
                PaymentResponse response = posLink.PaymentResponse;
                System.out.println(response);
                System.out.println("Transaction status is " + response.ResultTxt);
                System.out.println(response.RefNum + " is the transaction reference number");
                System.out.println(response.ApprovedAmount + " is the approved amount");
                System.out.println(response.AuthCode + " is the authorization code");
                System.out.println(response.ECRTransID + " is the ECR Transaction ID");
                System.out.println(response.RefNum + " is the Reference num");
                System.out.println(response.RetrievalReferenceNumber + " is the retreival reference number");
                System.out.println((response.PaymentTransInfo).toString() + " is the ECR Transaction ID");
                try {
                    returnObj.put("reference_number", response.RefNum);
                    if (response.ResultTxt.equals("DECLINE")) {
                        returnObj.put("approved_amount", "0");
                    } else {
                        returnObj.put("approved_amount", response.ApprovedAmount);
                    }

                    returnObj.put("tip_amount", "0" );
                    returnObj.put("card_holder_name","");
                    returnObj.put("edc_type","");
                    returnObj.put("auth_code", response.AuthCode);
                    returnObj.put("result_status", response.ResultTxt);
                    returnObj.put("generated_transaction_reference_id", transactionGeneratedId);
                    returnObj.put("given_amount", transAmount);
                    returnObj.put("extra_data", response.ExtData);

                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                System.out.println("TIMEOUT ERROR: " + transResult.Msg);

                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0");
                returnObj.put("auth_code", "");
                returnObj.put("tip_amount", "0" );
                returnObj.put("card_holder_name","");
                returnObj.put("edc_type","");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", transactionGeneratedId);
                returnObj.put("given_amount", transAmount);

            } else {
                System.out.println("OTHER ERROR: " + transResult.Msg);
                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0");
                returnObj.put("auth_code", "");
                returnObj.put("tip_amount", "0" );
                returnObj.put("card_holder_name","");
                returnObj.put("edc_type","");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", transactionGeneratedId);
                returnObj.put("given_amount", transAmount);
            }
        }
        catch(Exception exp)
        {
            exp.printStackTrace();
        }
        return returnObj;
    }


    public JSONObject localTotalReportGenerate() {

        JSONObject RepotGenerationObj = new JSONObject();
        // Setup comm setting.
        CommSetting commSetting = new CommSetting();
        try {
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSerialPort("COM3");
            // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
            commSetting.setType(CommSetting.TCP);
            commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
            commSetting.setDestPort(portNumberOfPAXPinPad());

            commSetting.setTimeOut("120000");
            PosLink posLink = new PosLink();
            // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
            posLink.SetCommSetting(commSetting);
            // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest

            ReportRequest reportRequest = new ReportRequest();
            reportRequest.TransType = reportRequest.ParseTransType("LOCALTOTALREPORT");
            reportRequest.EDCType = reportRequest.ParseEDCType("ALL");
            reportRequest.CardType = reportRequest.ParseCardType("ALL");
            posLink.ReportRequest = reportRequest;


            RepotGenerationObj.put("type_of_report", "LOCALTOTALREPORT");
            ProcessTransResult transResult = posLink.ProcessTrans();
            if (transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                ReportResponse response = posLink.ReportResponse;
                try {
                    RepotGenerationObj.put("valid_response", true);
                    RepotGenerationObj.put("result_status", response.ResultTxt);
                    RepotGenerationObj.put("result_code", response.ResultCode);
                    RepotGenerationObj.put("result_text", response.ResultTxt);
                    RepotGenerationObj.put("credit_count", response.CreditCount);
                    RepotGenerationObj.put("credit_amount", response.CreditAmount);
                    RepotGenerationObj.put("debit_count", response.DebitCount);
                    RepotGenerationObj.put("debit_amount", response.DebitAmount);
                    RepotGenerationObj.put("ebt_count", response.EBTCount);
                    RepotGenerationObj.put("ebt_amount", response.EBTAmount);
                    RepotGenerationObj.put("gift_count", response.GiftCount);
                    RepotGenerationObj.put("gift_amount", response.GiftAmount);
                    RepotGenerationObj.put("loyalty_count", response.LoyaltyCount);
                    RepotGenerationObj.put("loyalty_amount", response.LoyaltyAmount);
                    RepotGenerationObj.put("cash_count", response.CashCount);
                    RepotGenerationObj.put("cash_amount", response.CashAmount);
                    RepotGenerationObj.put("check_count", response.CHECKCount);
                    RepotGenerationObj.put("check_amount", response.CHECKAmount);
                    RepotGenerationObj.put("ext_data", response.ExtData);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                System.out.println("Report Response - response code " + response.ResultCode);
                System.out.println("Report Response - result text " + response.ResultTxt);
                System.out.println("Report Response - credit count " + response.CreditCount);
                System.out.println("Report Response - credit amount " + response.CreditAmount);
                System.out.println("Report Response - debit count " + response.DebitCount);
                System.out.println("Report Response - debit amount " + response.DebitAmount);

            } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                try {
                    RepotGenerationObj.put("valid_response", false);
                    RepotGenerationObj.put("result_status", transResult.Msg);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                System.out.println("TIMEOUT ERROR: " + transResult.Msg);
            } else {
                try {
                    RepotGenerationObj.put("valid_response", false);
                    RepotGenerationObj.put("result_status", transResult.Msg);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                System.out.println("OTHER ERROR: " + transResult.Msg);
            }
        }
        catch(JSONException exp){
            exp.printStackTrace();
        }
        return RepotGenerationObj;
    }

    public JSONObject closeTheBatch() {

        JSONObject closeBatchResponse = new JSONObject();
        // Setup comm setting.
        CommSetting commSetting = new CommSetting();
        try {
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSerialPort("COM3");
            // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
            commSetting.setType(CommSetting.TCP);
            commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
            commSetting.setDestPort(portNumberOfPAXPinPad());

            commSetting.setTimeOut("120000");
            PosLink posLink = new PosLink();
            // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
            posLink.SetCommSetting(commSetting);
            // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest

            BatchRequest batchRequest = new BatchRequest();
            batchRequest.TransType = batchRequest.ParseTransType("BATCHCLOSE");
            posLink.BatchRequest = batchRequest;


            closeBatchResponse.put("type_of_report", "BATCHCLOSE");

            ProcessTransResult transResult = posLink.ProcessTrans();
            if (transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                try {
                    BatchResponse response = posLink.BatchResponse;
                    closeBatchResponse.put("valid_response", true);
                    closeBatchResponse.put("result_code", response.ResultCode);
                    closeBatchResponse.put("result_status", response.ResultTxt);
                    closeBatchResponse.put("result_text", response.ResultTxt);
                    closeBatchResponse.put("credit_count", response.CreditCount);
                    closeBatchResponse.put("credit_amount", response.CreditAmount);
                    closeBatchResponse.put("debit_count", response.DebitCount);
                    closeBatchResponse.put("debit_amount", response.DebitAmount);
                    closeBatchResponse.put("ebt_count", response.EBTCount);
                    closeBatchResponse.put("ebt_amount", response.EBTAmount);
                    closeBatchResponse.put("gift_count", response.GiftCount);
                    closeBatchResponse.put("gift_amount", response.GiftAmount);
                    closeBatchResponse.put("loyalty_count", response.LoyaltyCount);
                    closeBatchResponse.put("loyalty_amount", response.LoyaltyAmount);
                    closeBatchResponse.put("cash_count", response.CashCount);
                    closeBatchResponse.put("cash_amount", response.CashAmount);
                    closeBatchResponse.put("check_count", response.CHECKCount);
                    closeBatchResponse.put("check_amount", response.CHECKAmount);
                    closeBatchResponse.put("ext_data", response.ExtData);
                    System.out.println("Batch Response - response code " + response.ResultCode);
                    System.out.println("Batch Response - result text " + response.ResultTxt);
                    System.out.println("Batch Response - credit count " + response.CreditCount);
                    System.out.println("Batch Response - credit amount " + response.CreditAmount);
                    System.out.println("Batch Response - debit count " + response.DebitCount);
                    System.out.println("Batch Response - debit amount " + response.DebitAmount);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                try {
                    closeBatchResponse.put("error_message", transResult.Msg);
                    closeBatchResponse.put("result_status", transResult.Msg);
                    closeBatchResponse.put("valid_response", false);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                System.out.println("TIMEOUT ERROR: " + transResult.Msg);
            } else {
                System.out.println("OTHER ERROR: " + transResult.Msg);
                try {
                    closeBatchResponse.put("error_message", transResult.Msg);
                    closeBatchResponse.put("result_status", transResult.Msg);
                    closeBatchResponse.put("valid_response", false);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return closeBatchResponse;
    }


    public JSONObject voidTheRefundWithReferenceID(String transactionRefNumForVoid,String randomTransactionId,String transAmount,String posTransactionRefNumForVoid,String tenderType) {
        // Setup comm setting.
        JSONObject returnObj = new JSONObject();
        CommSetting commSetting = new CommSetting();
        try {
//        System.out.println(commSetting.getType()+ " is the communication type");
//        commSetting.setType(CommSetting.BT);
//        commSetting.setMacAddr("BT:70:3E:97:A6:D1:E9");
//        commSetting.setType(CommSetting.UART);
//        commSetting.setSerialPort("COM3");
            // Create a POSLink object. In Android, it needs to pass context.
//        commSetting.setDestPort("10009");
//        commSetting.setSerialPort("COM3");
//        commSetting.setType(CommSetting.TCP);
            commSetting.setType(CommSetting.TCP);
            commSetting.setDestIP(ipAddressOfPAXPinPad());
//        commSetting.setBaudRate("9600");
            commSetting.setDestPort(portNumberOfPAXPinPad());

            commSetting.setTimeOut("120000");
            PosLink posLink = new PosLink();
            // Set the communication type. Currently support TCP, SSL, USB... For Android, it supports Bluetooth and more.
            posLink.SetCommSetting(commSetting);
            // Setup the Request. Can be PaymentRequest, ManageRequest, BatchRequest, ReportRequest
            PaymentRequest pay = new PaymentRequest();
            // Set the TenderType and TransType for the request first so that POS knows which part of data of request should be used.
            pay.TenderType = pay.ParseTenderType(tenderType);
//        pay.TenderType = pay.ParseTenderType("CREDIT");
            pay.TransType = pay.ParseTransType("VOID RETURN");

//        pay.TransType = pay.ParseTransType("RETURN");
//        pay.AuthCode="01939R";
            pay.ECRTransID = randomTransactionId;
//        pay.AuthCode = "77778888";
            // Your unique ID for this transaction
            pay.ECRRefNum = randomTransactionId;
            pay.OrigRefNum = transactionRefNumForVoid;
            pay.OrigECRRefNum = posTransactionRefNumForVoid;

            posLink.PaymentRequest = pay;


            ProcessTransResult transResult = posLink.ProcessTrans();
            if (transResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                System.out.println("all is well");
                PaymentResponse response = posLink.PaymentResponse;
                System.out.println(response);
                System.out.println("Transaction status is " + response.ResultTxt);
                System.out.println(response.RefNum + " is the transaction reference number");
                System.out.println(response.ApprovedAmount + " is the approved amount");
                System.out.println(response.AuthCode + " is the authorization code");
                System.out.println(response.ECRTransID + " is the ECR Transaction ID");
                System.out.println(response.RefNum + " is the Reference num");
                System.out.println(response.RetrievalReferenceNumber + " is the retreival reference number");
                System.out.println((response.PaymentTransInfo).toString() + " is the ECR Transaction ID");
                try {
                    returnObj.put("reference_number", response.RefNum);
                    if (response.ResultTxt.equals("DECLINE")) {
                        returnObj.put("approved_amount", "0");
                    } else {
                        returnObj.put("approved_amount", response.ApprovedAmount);
                    }
                    returnObj.put("tip_amount", "0");
                    returnObj.put("auth_code", response.AuthCode);
                    returnObj.put("result_status", response.ResultTxt);
                    returnObj.put("generated_transaction_reference_id", randomTransactionId);
                    returnObj.put("given_amount", transAmount);
                    returnObj.put("extra_data", response.ExtData);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            } else if (transResult.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                System.out.println("TIMEOUT ERROR: " + transResult.Msg);

                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0");
                returnObj.put("tip_amount", "0");
                returnObj.put("auth_code", "");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", randomTransactionId);
                returnObj.put("given_amount", transAmount);

            } else {
                System.out.println("OTHER ERROR: " + transResult.Msg);
                returnObj.put("reference_number", "");
                returnObj.put("approved_amount", "0");
                returnObj.put("auth_code", "");
                returnObj.put("tip_amount", "0");
                returnObj.put("result_status", transResult.Msg);
                returnObj.put("generated_transaction_reference_id", randomTransactionId);
                returnObj.put("given_amount", transAmount);
            }
        }catch (JSONException exp){
            exp.printStackTrace();
        }
        return returnObj;
    }

    private String portNumberOfPAXPinPad() {

        DatabaseVariables dbVar = new DatabaseVariables();

        return dbVar.valueForAttribute(DatabaseVariables.CARD_PROCESSING_TERMINAL_TABLE,DatabaseVariables.CARD_PROCESSING_CONNECTION_PORT_NUMBER);

    }

    private String ipAddressOfPAXPinPad() {
        DatabaseVariables dbVar = new DatabaseVariables();

        return dbVar.valueForAttribute(DatabaseVariables.CARD_PROCESSING_TERMINAL_TABLE,DatabaseVariables.CARD_PROCESSING_CONNECTION_ADDRESS);
    }



    public static String getTagValue(String xml, String tagName){
        if(xml.contains("<"+tagName+">"))
        { return xml.split("<"+tagName+">")[1].split("</"+tagName+">")[0]; }
        else{
            System.out.println("DOesnot contain the tag name "+tagName);
            return "";
        }
    }
}
