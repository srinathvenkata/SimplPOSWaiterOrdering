function initializePaymentTransaction()
{
        try{
        AndroidInterface.initTransactionForAmount(jQuery("#initiateCardPayment").val());
        AndroidInterface.holdInvoiceIDForTransaction(jQuery("#holdInvoiceId").val());
        modeOfPaymentSwiping = "Card";
        isCardProcessing = true;
    }catch(Err){
        bootbox.alert(Err.message);
//        bootbox.alert(Err.stack);
    }
}
var isCardProcessing = false;
function cardProcessingCheck(){
            try{
            var resp = AndroidInterface.checkStatusOfCardProcessing();
            if(resp=="true")
            {

                hideLoadingPopupModal();
                isCardProcessing = false;
                processAmountPaidFromTerminal();
            }
            window.setTimeout(function(){
                if(isCardProcessing == false){ return; }
                cardProcessingCheck();
            },300);
            }catch(Err){
                AndroidInterface.showToast(Err.stack);
            }
}

jQuery(document).on("click","#initiateCardPaymentBtn", function(event){
    initializePaymentTransaction();
    cardProcessingCheck();

    jQuery("#initiateCardPaymentBtn").attr("disabled","disabled");
    jQuery("#modeofpayment").val("Card");
    jQuery("#paymentRefNo").val("");
    var infoOptions = {
                              autoHideDelay: 6000,
                              showAnimation: "fadeIn",
                              hideAnimation: "fadeOut",
                              hideDuration: 700,
                              arrowShow: false,
                              className: "info"
                          };

                            $("#initiateCardPaymentBtn").notify("Card Processing initiated",infoOptions);
    window.setTimeout(function(){
            jQuery("#initiateCardPaymentBtn").removeAttr("disabled");
    },5000);

});
function voidTransaction()
{
    try{
    AndroidInterface.voidTransaction(jQuery("#transactionRefId").val());
    }catch(Err){
        bootbox.alert(Err.message);
//        bootbox.alert(Err.stack);
    }
}
var modeOfPaymentSwiping = "Card";
function processAmountPaidFromTerminal()
{
    var cardtransactiondetails = JSON.parse(AndroidInterface.cardTransactionDetails());
//
    try{
    hideLoadingPopupModal();
    isCardProcessing = false;
    AndroidInterface.printLog("Card transaction details in log are ",JSON.stringify(cardtransactiondetails));
    if(cardtransactiondetails['result_status']!='OK'){ return;}
    AndroidInterface.printLog("cardtransaction","Given amount is "+ (cardtransactiondetails['given_amount']) );
//    var recievedAmount = cardtransactiondetails['given_amount'];
    var recievedAmount = cardtransactiondetails['approved_amount'];
    var tipAmount =  cardtransactiondetails['tip_amount'];
    var amountInDollars = (parseFloat(recievedAmount) - parseFloat(tipAmount)) / 100;
    if(amountInDollars!=0){
        jQuery("#modeofpayment").val(modeOfPaymentSwiping);
        jQuery("#paymentRefNo").val(cardtransactiondetails['generated_transaction_reference_id']);
    }else{
        return;
    }
    $('#easy-numpad-output-2').text("");
    jQuery("#remainingChangeForInvoiceTD").hide();
    jQuery('#easy-numpad-output-2').text(amountInDollars);
    jQuery("#confirmPaymentBtn").click();

    }catch(Err){
        bootbox.alert(Err.message);
        AndroidInterface.printLog("erroris ",Err.message);
        AndroidInterface.printLog("errorstackis ",Err.stack);
    }
}
function returnTransaction()
{

    try{
    AndroidInterface.returnTransaction(jQuery("#returnAmount").val());
    }catch(Err){
        bootbox.alert(Err.message);
//        bootbox.alert(Err.stack);
    }
}
var foodStampableDepartmentsObj = new Array();
jQuery(document).ready(function(){

    window.setTimeout(function(){
            for(var b=0; b < allDepartments.length; b++)
            {
                if(allDepartments[b].department_foodstampable=='yes')
                {
                    var currentLength = foodStampableDepartmentsObj.length;
                    foodStampableDepartmentsObj[currentLength] = (allDepartments[b].unique_id);
                }
            }
    },1000);

});

function ebtValueCalculateAndDisplay()
{
    try{

    var totalEBTFoodStampableAmount = 0;
    jQuery('#itemsFormBody tr').each( function() {
        AndroidInterface.printLog("EBT",jQuery(this).html());
        var departmentUniqueId = (jQuery(this).find('input[name^="departmentUniqueId"]').val());
        AndroidInterface.printLog("EBT","Department Unique ID is "+departmentUniqueId);
        if(foodStampableDepartmentsObj.includes(departmentUniqueId))
        {
            var totalItemPrice =  parseFloat(jQuery(this).find('input[name^="totalItemPrice"]').val()).toFixed(2);
            totalEBTFoodStampableAmount = parseFloat(totalEBTFoodStampableAmount) + parseFloat(totalItemPrice);
        }
    });
    if(totalEBTFoodStampableAmount==0)
    {
        jQuery("#ebtAmountCell").hide();
    }else{
        jQuery("#ebtAmountCell").show();

    }
    /*for(var b=0; b < foodStampableDepartmentsObj.length; b++)
    {
        $("#itemsFormBody tr").find('input[name^="departmentUniqueId"][value ="'+foodStampableDepartmentsObj[b]+'"]').each(function(){
            var elem =jQuery(this).parent().parent();
            var totalItemPrice =  parseFloat(elem.find('input[name^="totalItemPrice"]').val()).toFixed(2);
            totalEBTFoodStampableAmount += totalItemPrice;
        });
    }*/
//    totalEBTFoodStampableAmount = totalEBTFoodStampableAmount.toFixed(2);
    jQuery("#ebtFoodStampableValue").val(totalEBTFoodStampableAmount.toFixed(2));
    jQuery("#ebtFoodStampableValueDisplay").html(totalEBTFoodStampableAmount.toFixed(2)+"&nbsp;$");
    jQuery("#initiateEBTFoodstampableCardPayment").val(totalEBTFoodStampableAmount.toFixed(2));
    }catch(Err)
    {
        AndroidInterface.printLog("EBT",Err.stack);
        AndroidInterface.printLog("EBT",Err.message);

    }
}
function initiateEBTPayment()
{
    AndroidInterface.printLog("EBT","initiateEBTPayment() called");
    try{
            AndroidInterface.initTransactionForEBTAmount(jQuery("#initiateEBTFoodstampableCardPayment").val());
            modeOfPaymentSwiping  = "EBT";
            isCardProcessing = true;
        }catch(Err){
            bootbox.alert(Err.message);
    //        bootbox.alert(Err.stack);
        }
    cardProcessingCheck();

        jQuery("#initiateEBTCardPaymentBtn").attr("disabled","disabled");
        jQuery("#paymentRefNo").val("");
        var infoOptions = {
                                  autoHideDelay: 6000,
                                  showAnimation: "fadeIn",
                                  hideAnimation: "fadeOut",
                                  hideDuration: 700,
                                  arrowShow: false,
                                  className: "info",
                                  position  :   "top"
                              };

                                $("#initiateEBTCardPaymentBtn").notify("EBT Card Processing initiated",infoOptions);
        window.setTimeout(function(){
                jQuery("#initiateEBTCardPaymentBtn").removeAttr("disabled");
        },5000);
}