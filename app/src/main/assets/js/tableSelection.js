jQuery(document).ready(function(){
    try{
//    bootbox.alert("Hello from table selection");
    }catch(Err){

						AndroidInterface.showToast(Err.message); AndroidInterface.printLog("NewError",Err.stack);
    }
});

function topSelectTableButton(valueForDisplay)
{
    if(valueForDisplay==true)
    {
        jQuery("#pageHead").show();
        jQuery("#defaultPageHeadButtons").hide();

    }else{

        jQuery("#pageHead").show();
        jQuery("#defaultPageHeadButtons").hide();

    }
}

jQuery(document).on("click","#tableSelectionButton", function(event){

                                if(AndroidInterface.checkConnectivityDetails()=="false")
                                {
                                    return;
                                }
    showTableLayoutWithTables();

});
function showTableLayoutWithTables()
{
    try{
    AndroidInterface.showLoadingDialog();
    AndroidInterface.fetchTableSelectionAsync();
    }catch(Err)
    {
        bootbox.alert(Err.message);
        AndroidInterface.printLog("Table Selection",Err.message);
    }
}
function displayPendingTablesFromAsync()
{
    tableLayoutDisplay();
    AndroidInterface.stopLoadingDialog();
}

var storeTablesP = null;

function closeSaveNewInvoicePopup()
{
	saveNewInvoiceFlag = false;
	jQuery("#saveInvoiceTableSelectionPopup").hide();
	jQuery("#tableSelectionPopup").hide();
	jQuery(".overlay2").hide();
}

function tableLayoutDisplay(){

	if(jQuery("#recallInvoiceId").val()!=""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please clear the recalled invoice");
					}catch(Err){
						JSInterface.showToast(Err.stack);
					}
					AndroidInterface.stopLoadingDialog();
					return;
	}

    jQuery("#tableSelectionPopup").show();

		jQuery(".overlay2").show();
	try{
                		var tablesList = JSON.parse(AndroidInterface.tableNumbersSave(jQuery("#invoiceStoreId").val()));
//		                var invoicesForTables = JSON.parse(AndroidInterface.invoicesForTables(jQuery("#invoiceStoreId").val()));
		                var userAdmin = AndroidInterface.isUserAdmin();
		                AndroidInterface.printLog("User admin","User admin is "+userAdmin);

		                storeTablesP = tablesList;
                		var showTablesStr = "<div class=\"row\">";
                        var tablesListForAutocomplete = [];

                                                                if(!	(tablesList === undefined || tablesList === null))
                        										{

                        											if(tablesList!=false && tablesList.length!=0)
                        											{
                        												for(k=0;k<tablesList.length;k++){

                                                                            var disabledStr = " ";
                        													var tableRow = tablesList[k];
                        													if(tablesList[k].occupancy_status=='Occupied'){
                        														tablebuttonClass = "btn-warning";
                                                                                var associatedInvoiceId = tablesList[k].associated_invoice_id;
                                                                                if(userAdmin==false)
                                                                                {
                                                                                    if(tablesList[k]['employee'] != userIdLoggedIn)
                                                                                    { disabledStr = " disabled=\"disabled\" "; }
                                                                                }

                                                                                if(tablesList[k]['delivery_status'] =='Receipt Printed'){
                                                                                    tablebuttonClass = "btn-info";
                                                                                }
                        													}
                        													else{
                        													tablebuttonClass = "btn-success";
                        													}
                        													var w = (window.innerWidth);
                        													AndroidInterface.printLog("table width",w);
                        													try{
                        													                if(portraitView==true)
                        													                {
                        													                    showTablesStr += "<div class=\"col-4\" style=\"margin:10px 0px; text-align:center;\"><button data-preselectiontableName=\""+tablesList[k].table_name+"\" data-preselectionassociatedInvoiceId=\""+ tablesList[k].associated_invoice_id + "\"   data-preselectiontableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+" preselectedTableBtn\" "+ disabledStr +" >"+tableRow.table_name+"</button></div>";

                        													                }else{
                        													                        if(w>=1280){
                                                                                                    showTablesStr += "<div class=\"col-2\" style=\"margin:10px 0px; text-align:center;\"><button data-preselectiontableName=\""+tablesList[k].table_name+"\" data-preselectionassociatedInvoiceId=\""+ tablesList[k].associated_invoice_id + "\"   data-preselectiontableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+" preselectedTableBtn\" "+ disabledStr +" >"+tableRow.table_name+"</button></div>";
                                                                                                    }else{
                                                                                                    showTablesStr += "<div class=\"col-3\" style=\"margin:10px 0px; text-align:center;\"><button data-preselectiontableName=\""+tablesList[k].table_name+"\"  data-preselectionassociatedInvoiceId=\""+ tablesList[k].associated_invoice_id + "\"   data-preselectiontableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+" preselectedTableBtn\" "+ disabledStr +" >"+tableRow.table_name+"</button></div>";
                                                                                                    }
                        													                }

                        													}catch(Err){
                        													    bootbox.alert(Err.stack);
                        													}

//                        										            tablesListForAutocomplete[k] = 	tableRow.table_name;

                        												}

                        											}
                        											tableNames = tablesListForAutocomplete ;
                        										}
                        		showTablesStr += "</div>";
                        		jQuery("#tableNumberSelectionForPopup").html(showTablesStr);
//                        		saveNewInvoiceFlag = true;
//                                setTableNamesForAutocomplete();
                                AndroidInterface.stopLoadingDialog();

	}catch(Err){
	    AndroidInterface.printLog("Error",Err.message);
	}
}

function fetchTheInvoiceForId(associatedInvoiceId){
                    if(AndroidInterface.checkConnectivityDetails()=="false")
                    {
                        return;
                    }
                    var retrieveInvoiceDetails  = AndroidInterface.invoiceDetailsForID(associatedInvoiceId);
                    AndroidInterface.printLog("Invoice Details",retrieveInvoiceDetails);
                    retrieveInvoiceDetails = JSON.parse(retrieveInvoiceDetails);
                    var validInvoiceInfo = false;
                    var invoiceInfo  = false;
                    if(!	(retrieveInvoiceDetails === undefined || retrieveInvoiceDetails === null))
                    {
                        if(retrieveInvoiceDetails!=false && retrieveInvoiceDetails.length!=0)
                        {
                                                invoiceInfo = retrieveInvoiceDetails[0];
                                            	validInvoiceInfo = true;
                        }
                    }

                    if(validInvoiceInfo==false){ return;}

					var invoiceId = associatedInvoiceId;
					var invoiceAmt = invoiceInfo["total_amt"];
					var invoiceOrderType = invoiceInfo["order_type"];
					changeOrderType(invoiceOrderType);
					jQuery("#holdInvoiceId").val(invoiceId);
					jQuery("#recallInvoiceId").val("");
					jQuery("#grandTotalOfInvoice").html(invoiceAmt+" "+currencyHtmlSymbol);
					showLoadingPopupModal();
					try{
								var invoiceItemsList = JSON.parse(JSInterface.invoiceItems(invoiceId));
								closeRecallPopup();
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
													var productDetails = JSInterface.productDetails(newProductJSON.item_id,newOrderType,(jQuery("#invoiceStoreId").val()));
													var productInfo = JSON.parse(productDetails);
													var invCategory = ""; var invCategoryUniqueId = "";
													if(productInfo.is_item == true && productInfo.product_exists == true)
														{
															var storewiseInfo = (productInfo.storewise_pricing_info);
															var inventoryInfo = (productInfo.inventory_info);
															var optionalInfo = (productInfo.optional_info);
															var orderingInfo = (productInfo.ordering_info);
															var departmentInfo = (productInfo.dept_info);
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
													var unitPriceOfItem = Number( (parseFloat(newProductJSON.price_you_charge)) / (parseFloat(newProductJSON.item_quantity))    );
                                                    var appenderForUnitPrice = "";
                                                    if(invitemUnits!="" && invitemUnits!=undefined){ appenderForUnitPrice = " / ";}

													tableStr += "<tr><td>"+(p+1)+"</td><td>"+hiddenParameters+"<input type=\"hidden\" name=\"itemId[]\" value=\""+newProductJSON.item_id+"\" />"+newProductJSON.item_id+"</td><td><input type=\"hidden\" name=\"itemName[]\" value=\""+ (addslashesforInput(newProductJSON.item_name)) +"\" /><span class=\"itemNametd\">"+newProductJSON.item_name+"</span></td><td><input type=\"hidden\" name=\"itemUnitPrice[]\" value=\"" + newProductJSON.avg_cost + "\" /><input type=\"hidden\" name=\"itemQuantity[]\" value=\""+newProductJSON.item_quantity+"\" /><span class=\"updateQtytd\">"+((newProductJSON.item_quantity).toString())+"</span></td><td><input type=\"hidden\" name=\"units[]\" value=\""+( (invitemUnits!=undefined) ? invitemUnits : "" ) +"\" />"+"<span class=\"unitPriceAndUnitsDisplay\">"+unitPriceOfItem+appenderForUnitPrice+( (invitemUnits!=undefined) ? invitemUnits : "" ) +"</span></td><td style=\"text-align:right;\"><input type=\"hidden\" name=\"totalItemPrice[]\" value=\""+ newProductJSON.price_you_charge +"\" /><span class=\"totalItemPricetd\">"+newProductJSON.price_you_charge+"</span></td><td class=\"deleteItemFromCart\" style=\"padding-right:10px;\"><i class=\"fa fa-trash\"></i></td></tr>";

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
							    var customerDetailsForInvoice = JSON.parse(JSInterface.customerDetailsForInvoice(invoiceId));

										jQuery("#customerIdForInvoice").val(customerDetailsForInvoice.customerid);
										jQuery("#customerNameDisplay").html(customerDetailsForInvoice.customername);
								}
								catch(Err){ AndroidInterface.showToast(Err.stack);}
							    var serializedData = jQuery("#invoiceAndItemsForm").serialize();
							    // JSInterface.showToast(serializedData);
							    setTimeout(function(){
							    		jQuery("#printPreviewButton").show();
								},1000);
							    changeFetchOnHoldButton();
							    AndroidInterface.invoiceFetchTimeSave(invoiceInfo["modified_timestamp"]);
								return;
					}catch(Err){
						AndroidInterface.showToast(Err.stack);
					}
					hideLoadingPopupModal();


}
jQuery(document).on("click",".preselectedTableBtn", function(event){


        if(AndroidInterface.checkConnectivityDetails()=="false")
        {
            return;
        }

    AndroidInterface.showLoadingDialog();

    clearAllContentOfCart();
	jQuery("#tableSelectionPopup").hide();
    		jQuery(".overlay2").hide();
					var tableName = jQuery(this).attr("data-preselectiontableName");
					var tableUniqueId = jQuery(this).attr("data-preselectiontableUniqueId");
					var associatedInvoiceId = jQuery(this).attr("data-preselectionassociatedInvoiceId");
//					 bootbox.alert("Table name is "+tableName+" and table unique id is "+tableUniqueId + " and associated invoice id is "+associatedInvoiceId);
					try{

                            if(associatedInvoiceId!=""){
                            AndroidInterface.printLog("Fetch",associatedInvoiceId);
                            var invoiceDetails = JSON.parse(AndroidInterface.invoiceDetailsForID(associatedInvoiceId));
                            if(invoiceDetails.length != 1){
                                bootbox.alert("Invalid Invoice ID");
                                AndroidInterface.stopLoadingDialog();
                                return;
                            }
                    var invoiceOrderType = invoiceDetails[0].order_type;
                    var invoiceAmt =  invoiceDetails[0].total_amt;
                    var invoiceId = associatedInvoiceId;

                            var invoicestatus = invoiceDetails[0].status;
                            if(invoicestatus != "hold"){
                                bootbox.alert("The status of this invoice is "+invoicestatus);
                                AndroidInterface.stopLoadingDialog();
                                return;
                            }
					changeOrderType(invoiceOrderType);
					jQuery("#holdInvoiceId").val(invoiceId);
					jQuery("#recallInvoiceId").val("");
					jQuery("#grandTotalOfInvoice").html(invoiceAmt+" "+currencyHtmlSymbol);
    //                        bootbox.alert(associatedInvoiceId);
    //                            fetchTheInvoiceForId(associatedInvoiceId);
                            var invoiceId = associatedInvoiceId;

                            try{
                            								var invoiceItemsListTemp = (AndroidInterface.invoiceItems(invoiceId,"hold"));
                            								if(invoiceItemsListTemp == null){
                            								    closeRecallPopup();
                            								    return ;
                            								}
                            								var invoiceItemsList = JSON.parse(invoiceItemsListTemp)
                            								closeRecallPopup();
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

                            														                                        if(!	(newProductJSON.item_quantity === undefined || newProductJSON.item_quantity === null))
                                                                                    										{
                                                                                    										}else{ newProductJSON.item_quantity = ""; }
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
                            							    AndroidInterface.invoiceFetchTimeSave(invoiceDetails[0].modified_timestamp);
                                                            jQuery("#tableSelectionSpan").html("&nbsp;&nbsp;"+tableName);
                                                        AndroidInterface.stopLoadingDialog();
                            								return;
                            					}catch(Err){
                            						console.log(Err.stack);
                            					}

                            }else{
                                jQuery("#tableSelectionSpan").html("&nbsp;&nbsp;"+tableName);
                                jQuery("#tableNumberSelectionFromPopup").val(tableUniqueId);
                            }
                    }
                    catch(Err){
                            AndroidInterface.printLog("Error",Err.stack)
                    }
                AndroidInterface.stopLoadingDialog();
    });

function clearAllContentOfCart(){
    jQuery("#tableNumberSelectionFromPopup").val();
	jQuery("#holdInvoiceId").val("");
	jQuery("#recallInvoiceId").val("");
    jQuery("#itemsFormBody").html("");
	resetCartToBlank();
	changeFetchOnHoldButton();
    jQuery("#invoiceAndItemsForm")[0].reset();
    calculateGrandTotal();
    jQuery("#tableSelectionSpan").html("");


}

function insertSelectedTable(){
    if(jQuery("#tableNumberSelectionFromPopup").val()!=""){
    //							        bootbox.alert(jQuery("#tableNumberSelectionFromPopup").val());

                                            var tabluniqidselected = jQuery("#tableNumberSelectionFromPopup").val();
                                            jQuery("#orderTableSelection").find("button[data-tableUniqueId='"+tabluniqidselected+"']").click();
                                            jQuery("#orderRefNum").focus();
    //                                        var tablename = JSInterface.tableNameForUniqueID(tabluniqidselected);
    //                                        jQuery("#orderRefNum").val(tablename);
    //					                    jQuery("#tableSelected").val(tabluniqidselected);

    //						                    saveNewInvoice();
                                                jQuery("#confirmOrderRefId").click();
    						                    keyboardFocus();


    							    }
}
function showLoadingPopupModal()
{
    try{
    jQuery("#showLoadingModal").modal("show");
    }catch(Err)
    {
        bootbox.alert(Err.stack);
    }
}

function hideLoadingPopupModal()
{

    try{
    AndroidInterface.printLog("Testing","Hide modal called");
    jQuery("#showLoadingModal").modal("hide");
    }catch(Err)
    {
    bootbox.alert(Err.stack);
    }

}