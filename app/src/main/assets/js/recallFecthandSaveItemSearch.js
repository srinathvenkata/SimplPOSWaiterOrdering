var invoiceAmtOfFetchInvoice = 0;
jQuery(document).ready(function(){
		repeatLocalServerSyncCall();
		fetchLatestInvoicesCall();			
});

function repeatLocalServerSyncCall()
{
	setTimeout(function(){
							try{
							AndroidInterface.localServerRefreshCall();	
							repeatLocalServerSyncCall();
							}
							catch(Err)
							{
								console.log(Err.stack);
							}
					},180000);

}
jQuery(document).ready(function(){
fetchLatestNotificationsCall();
});

function finalPayFromInterface(){
    finalPay();
}
function fetchLatestNotificationsCall(){

	setTimeout(function(){
							try{
//							AndroidInterface.fetchLatestNotifications();
//							fetchLatestNotificationsCall();
							}
							catch(Err)
							{
								console.log(Err.stack);
							}
					},3000);
}

function fetchLatestInvoicesCall(){
}
function setAsyncInvoiceItems(invoiceItemsListTemp)
{
                    try{
                        invoiceItemsList = JSON.parse(invoiceItemsListTemp)
    								closeRecallPopup();
    								var invoiceAmt = invoiceAmtOfFetchInvoice;
    								var invoiceItemsValue = 0; var taxesValue = 0; var totalQtyOfItems = 0;
    					if(!	(invoiceItemsList === undefined || invoiceItemsList === null))
    										{
    											if(invoiceItemsList!=false && invoiceItemsList.length!=0)
    											{
    												var tableStr = "";
    												for(p=0;p< invoiceItemsList.length; p++){
    													var newProductJSON = invoiceItemsList[p];
    													totalQtyOfItems = totalQtyOfItems + parseFloat(newProductJSON.item_quantity);
    													invoiceItemsValue = invoiceItemsValue  + parseFloat(newProductJSON.price_you_charge);
    													var productDetails = AndroidInterface.productDetailsForFetch(newProductJSON.item_id,newOrderType,(jQuery("#invoiceStoreId").val()));
    													var productInfo = JSON.parse(productDetails);
    													var invCategory = ""; var invCategoryUniqueId = "";
    													if(productInfo.is_item == true && productInfo.product_exists == true)
    														{
    															var storewiseInfo = JSON.parse(productInfo.storewise_pricing_info);
    															var inventoryInfo = JSON.parse(productInfo.inventory_info);
    															var optionalInfo = JSON.parse(productInfo.optional_info);
    															var orderingInfo = JSON.parse(productInfo.ordering_info);
    															var departmentInfo = JSON.parse(productInfo.dept_info);
    															var invitemUnits =  "";

    															// alert(storewiseInfo.toString());
    																var invitemName = inventoryInfo[0].inventary_item_name;
    																var invitemId = inventoryInfo[0].inventory_item_no;
    																var invitemPrice = inventoryInfo[0].inventary_price_tax;
    																var invitemPromptQuantity = "no";
    																var invitemPromptPrice = "no";
    																if(optionalInfo != false && optionalInfo.length !=0)
    																{
    																var invitemPromptQuantity = optionalInfo[0].inventary_prompt_quantity;
    																var invitemPromptPrice = optionalInfo[0].inventary_prompt_price;
    																var invitemUnits =  optionalInfo[0].unit_type;
    																}
    																if(departmentInfo != false && departmentInfo.length !=0)
    																{
    																	invCategory = departmentInfo[0].category_id;
    																	invCategoryUniqueId = departmentInfo[0].category_unique_id;
    																}

    														}
    													var hiddenParameters = "<input type=\"hidden\" name=\"categoryUniqueId[]\" value=\""+invCategoryUniqueId+"\" /><input type=\"hidden\" name=\"categoryid[]\" value=\""+invCategory+"\" /><input type=\"hidden\" name=\"savedRowQuantity[]\" value=\""+ newProductJSON.item_quantity +"\"><input type=\"hidden\" name=\"savedRowUniqueId[]\" value=\""+ newProductJSON.unique_id +"\"><input type=\"hidden\" name=\"oldNotes[]\" /><input type=\"hidden\" name=\"savedRowNotes[]\" value=\"\"><input type=\"hidden\" name=\"discountProvidedInCurrency[]\" value=\"0\" />";
    													tableStr += "<tr><td>"+(p+1)+"</td><td>"+hiddenParameters+"<input type=\"hidden\" name=\"itemId[]\" value=\""+newProductJSON.item_id+"\" />"+newProductJSON.item_id+"</td><td><input type=\"hidden\" name=\"itemName[]\" value=\""+newProductJSON.item_name+"\" /><span class=\"itemNametd\">"+newProductJSON.item_name+"</span></td><td><input type=\"hidden\" name=\"itemUnitPrice[]\" value=\"" + newProductJSON.avg_cost + "\" /><input type=\"hidden\" name=\"itemQuantity[]\" value=\""+newProductJSON.item_quantity+"\" /><span class=\"updateQtytd\">"+((newProductJSON.item_quantity).toString())+"</span></td><td><input type=\"hidden\" name=\"units[]\" value=\""+invitemUnits+"\" />"+invitemUnits+"</td><td style=\"text-align:right;\"><input type=\"hidden\" name=\"totalItemPrice[]\" value=\""+ newProductJSON.price_you_charge +"\" /><span class=\"totalItemPricetd\">"+newProductJSON.price_you_charge+"</span></td><td class=\"deleteItemFromCart\" style=\"padding-right:10px;\"><i class=\"fa fa-trash\"></i></td></tr>";

    												}
    												taxesValue = parseFloat(invoiceAmt) - parseFloat(invoiceItemsValue);
    												jQuery("#itemsFormBody").html(tableStr);
    												jQuery('#itemsFormBody tr:last-child').addClass("highlightrow");
    											}
    										}

    								jQuery("#totalQtyOfItemsDisplay").html((totalQtyOfItems.toString()));
    							    jQuery("#subTotalOfInvoice").html((invoiceItemsValue.toString())+ "&nbsp;"+currencyHtmlSymbol);
    							    jQuery("#taxesOfInvoice").html((taxesValue.toString())+ "&nbsp;"+currencyHtmlSymbol);
    							    jQuery("#grandTotalOfInvoice").html((invoiceAmt.toString())+ "&nbsp;"+currencyHtmlSymbol);
    							    calculateGrandTotal();
    							    try{
    							    var customerDetailsForInvoice = JSON.parse(AndroidInterface.customerDetailsForInvoice(invoiceId));

    										jQuery("#customerIdForInvoice").val(customerDetailsForInvoice.customerid);
    										jQuery("#customerNameDisplay").html(customerDetailsForInvoice.customername);
    								}
    								catch(Err){ console.log(Err.stack);}
    							    var serializedData = jQuery("#invoiceAndItemsForm").serialize();
    							    // console.log(serializedData);
    							    setTimeout(function(){
    							    		jQuery("#printPreviewButton").show();
    								},1000);
    							    changeFetchOnHoldButton();
    								return;
    								}catch(Err){
                                                        						console.log(Err.stack);
                                                        						AndroidInterface.printLog("Fetch",Err.stack);
                                    }
}

var startFoodPartnerFocusEvent = false;
jQuery(document).ready(function(){
    setTimeout(function(){

        startFoodPartnerFocusEvent = true;
    },2000);



        jQuery("#discountTypeSwitch").bootstrapSwitch({
                state  :  true ,
                size   :  'normal',
                onInit :	function(event, state) {

                } ,
                  onSwitchChange : function(event, state) {
                            if(state==false){

                                jQuery('input:radio[name="discountType"]')
                                    .removeAttr('checked');
                                jQuery('input:radio[name="discountType"][value="currency"]')
                                    .attr('checked', 'checked');
                            }else{

                                jQuery('input:radio[name="discountType"]')
                                    .removeAttr('checked');
                                jQuery('input:radio[name="discountType"][value="percentage"]')
                                                                    .attr('checked', 'checked');
                            }
                     }
         });

        $("[name='overallDiscountSwitch']").bootstrapSwitch({
                state  :  false ,
                size   :  'normal',
                onInit :	function(event, state) {
                            $("#overallDiscount").val("false");
                            if(state==true)
                            {
                                $("#overallDiscount").attr("checked","checked");
                            }
                        } ,
                 onSwitchChange : function(event, state) {
                        $("#overallDiscount").val(state);
                        AndroidInterface.printLog("OverallDiscount",state);
                            if(state==true)
                            {

                                AndroidInterface.printLog("OverallDiscount","Overall discount is checked");
                                $("#overallDiscount").attr("checked","checked");
                            }else{
                                $("#overallDiscount").removeAttr("checked");
                                AndroidInterface.printLog("OverallDiscount","Overall discount is unchecked");
                            }
                    }
        });
});


var windowloaded = false;

window.onload = function() {
//    if (hasFocus) alert('example');
    windowloaded = true;
};

window.onfocus = function() {
//    JSInterface.printLog("Window Focussed","Window has just been focussed");
    if(windowloaded)
    {
        if(jQuery("#invoiceStoreId").val()!="" && startFoodPartnerFocusEvent==true)
        {
//            AndroidInterface.windowFocusCallFoodPartners();
        }
    }
};

function keyboardFocus()
{
    AndroidInterface.printLog("KeyboardEnabled","Keyboard enabled value is "+externalKeyboard);
    try{

        if(externalKeyboard==true)
        {
            jQuery("#barcodeNumber").focus();
        }

    }catch(Err){

    AndroidInterface.printLog("KeyboardEnabled",Err.message);
    }
}

function enableNotesButtons(notesButtonsContent)
{
    jQuery("#notesButtonsDiv").html("");
    var totalButtonContent = "";
    if(notesButtonsContent.length > 0)
    {
        var notesButtonsList = notesButtonsContent.split(',');
        for( var j=0 ; j < notesButtonsList.length; j++)
        {
            var buttonContent = "<button class=\"btn btn-secondary col-3 notesSpecificationBtn\" style=\"margin-right: 8px !important; width : calc(90% - 8px); margin-top:15px; \">"+notesButtonsList[j]+"</button>";

            totalButtonContent += buttonContent;
        }
    }
    jQuery("#notesButtonsDiv").html(totalButtonContent);
}

jQuery(document).on("click",".notesSpecificationBtn", function(event){
        var buttonContent = jQuery(this).text();
        jQuery(".notesSpecificationBtn").removeClass("btn-success").addClass("btn-secondary");
        jQuery(this).removeClass("btn-secondary").addClass("btn-success");
        var prependString = ", ";
        if(jQuery("#notesTextarea").val()=="")
        {
            prependString = "";
        }
        var newVal =  (jQuery("#notesTextarea").val()) + prependString + buttonContent;
//        newVal = newVal.substring(0,newVal.length-2);

        jQuery("#notesTextarea").val(newVal);

});


function saveAndFetchInvoice(){
	jQuery("#recalledInvoicesBody").html(""); jQuery("#holdInvoicesBody").html("");
	try{
		// check if it is a recalled invoice
		if(jQuery("#recallInvoiceId").val()!=""){
			try{
				AndroidInterface.showAlertDialogJS("Error","Please Clear the recalled Invoice to save or fetch Invoices");
				return;
			}catch(Err){
				console.log(Err.stack);
			}

		}
		permissionCheck = AndroidInterface.userPermissionCheck("holdprint");

		if( permissionCheck=="true") {
			//

						var itemcount = jQuery("#itemsFormBody tr").length;
						if(itemcount > 0)
						{
							if(jQuery("#holdInvoiceId").val()=="")
							{
							retrieveTableNumbers();
							}else{
								// bootbox.alert(JSON.stringify(formDataOfCart()));
								AndroidInterface.printLog("Preferences","Check before saving . Reason for cancellation is "+promptReasonForCancellation);

                                if(promptReasonForCancellation==true){    saveOrFetchInPromptReason = "save"; promptForTheReasonForCancellation(); if(reasonProvided==true){ reasonProvided = false;}else { return; } }
                                AndroidInterface.saveAndHoldInvoice(JSON.stringify(formDataOfCart()));
//								AndroidInterface.stopLoadingDialog();
								jQuery("#holdInvoiceId").val("");
								jQuery("#itemsFormBody").html("");
								resetCartToBlank();
								changeFetchOnHoldButton();

    							jQuery("#invoiceAndItemsForm")[0].reset();
			                var hasNewItems = AndroidInterface.hasNewItemsCheck();
	                        if(itemsListPrint==true && hasNewItems==true){
								confirmationAlertFlag = true;
    							jQuery("#confirmationHeading").html("Would you like to Print The items List ?");
								jQuery("#confirmationType").val("printitemsList");

								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
//								jQuery("#cancelAlertBtn").hide();

								                                if(autoprintKot==true)
                                								{
                                								    confirmAlert();
                                								}

								}
							}

						//
						}
						else{
							// AndroidInterface.showToast("We have to fetch invoices");
							AndroidInterface.fetchLatestInvoicesAsync();
							showLoadingPopupModal();
//							displayPendingInvoices();
						}

		}
		else{
			AndroidInterface.showAlertDialogJS("Permission Denied","Sorry! You are not authorized to hold invoices");
			// AndroidInterface.showAlertDialogJS("Permission Allowed","You are authorized to hold invoices");

		}
	}catch(Err){ console.log(Err.stack); }
}


function promptForTheReasonForCancellation()
{

    AndroidInterface.printLog("Preferences","Reason Provided is "+reasonProvided.toString());

    if(reasonProvided==true) { return; }
    reasonProvided = false;
    if(saveOrFetchInPromptReason=="save" || saveOrFetchInPromptReason=="finalpay")
    {
        var checkForDeletedAndReducedItems =AndroidInterface.CartHasDeletedItemsOrReducedQuantities(JSON.stringify(formDataOfCart()));
        AndroidInterface.printLog("Preferences","Check for deleted items response is "+checkForDeletedAndReducedItems);
        if(checkForDeletedAndReducedItems=="true")
        {
            reasonProvided = false;
        }else{ reasonProvided = true; }
    }else if(saveOrFetchInPromptReason=="void")
    {
                var checkForDeletedAndReducedItems =AndroidInterface.voidedInvoiceReducedQuantities(JSON.stringify(formDataOfCart()));
                if(checkForDeletedAndReducedItems=="true")
                {
                    reasonProvided = false;
                }else{ reasonProvided = true; }
    }
        if(reasonProvided==false){ showReasonForCancellationPopup();}
//    reasonProvided = true;

}


function showReasonForCancellationPopup()
{
    if(saveOrFetchInPromptReason=="void")
    {
        jQuery("#voidinvoice_reasonfor_cancellation").show();
        jQuery("#itemwise_cancellation").hide();
    }else{
            jQuery("#voidinvoice_reasonfor_cancellation").hide();
            jQuery("#itemwise_cancellation").show();
    }
    jQuery("#cancellationReasonModal").modal("show");
    var cancellationBodyStr = AndroidInterface.cancellationTableBodyStr();
    jQuery("#ReasonForCancellationModalBody").html(cancellationBodyStr);
}
function cancellationReasonProceedButton()
{

        var serializedForm = { };
    /*jQuery("#ReasonForCancellationModalBody .cancelleditemidreason").each( function() {
        var rowUniqueId = jQuery(this).attr("data-cancelleditemid");
        var reasonIs = jQuery(this).val();

    });*/
                    var inputValues = jQuery("#ReasonForCancellationModalBody .cancelleditemidreason").map(function() {
                                    return $(this).val();
                            }).toArray();
                    serializedForm.reasons = inputValues;
                    inputValues = jQuery("#ReasonForCancellationModalBody .cancelleditemidreason").map(function() {
                                    return $(this).attr("data-cancelleditemid");
                            }).toArray();
                    serializedForm.saveduniqueids = inputValues;
                    serializedForm.voidinvoicereason = jQuery("#voidInvoiceReasonForCancellation").val();

                    jQuery("#cancellationReasonModal").modal("hide");
                    AndroidInterface.saveReasonsForCancellation(JSON.stringify(serializedForm));
                    reasonProvided = true;
                    if(saveOrFetchInPromptReason=="save")
                    { saveAndFetchInvoice(); }
                    else if(saveOrFetchInPromptReason=="finalpay")
                    {
                        loadPaypopup();
                    }
                    else if(saveOrFetchInPromptReason=="void")
                    {
                        voidInvoiceMethod();
                    }

}
function resetTheReasonsForCancellation()
{
        AndroidInterface.clearCancellationReasons();
        jQuery("#voidInvoiceReasonForCancellation").val("");
        jQuery("#ReasonForCancellationModalBody").html("");
}