var customerSearchFlag = false;
var typingTextField = false;
			jQuery(document).on("click","#cameraBarcodeScanning", function(event){
				// bootbox.alert("Advanced Settings button clicked");
				try{
					AndroidInterface.callCameraBarcodeScan();
				}catch(Err){
					console.log(Err.stack);
				}
			});
			jQuery(document).on("click","#advancedSettings", function(event){
				// bootbox.alert("Advanced Settings button clicked");
				if(portraitView==true){
                    closeItemsSelection();
                }
				jQuery("#advancedSettingsPopup").show();
				jQuery("#newRefNumForOrder").val("");
				try{
				var existingTables = JSON.parse(AndroidInterface.existingTablesForShifting(jQuery("#invoiceStoreId").val()));
				// bootbox.alert(JSON.stringify(existingTables.holdinvoices));
				var onHoldInvoices = JSON.parse(existingTables.holdinvoices);
				
					var fromTableString = "";
					if(onHoldInvoices.length > 0)
					{
						fromTableString += "<option value=\"\">---</option>";
						for(k=0;k<onHoldInvoices.length;k++)
						{
							var currentRow = onHoldInvoices[k];
								fromTableString += "<option value=\""+ currentRow.invoice_id +"\">"+ currentRow.holdid +"</option>";
						}
					}
					else{
						fromTableString = "<option value=\"\">No Invoices On Hold</option>";
					}
					jQuery("#fromTable").html(fromTableString);
					
					var tablesForStore = JSON.parse(existingTables.allTables);
				
					var fromTableString = "";
					if(tablesForStore.length > 0)
					{
						fromTableString += "<option value=\"\">---</option>";
						for(k=0;k<tablesForStore.length;k++)
						{
							var currentRow = tablesForStore[k];
								fromTableString += "<option value=\""+ currentRow.table_name +"\">"+ currentRow.table_name +"</option>";
						}
					}
					else{
						fromTableString = "<option value=\"\">No Tables For This Store</option>";
					}
					jQuery("#toTable").html(fromTableString);

					
				}catch(Err){
					console.log(Err.stack);
				}
				jQuery(".overlay2").show();
			});
			jQuery(document).on("click","#closeAdvancedSetting", function(event){
				// bootbox.alert("Advanced Settings button clicked");
				jQuery("#advancedSettingsPopup").hide();

				jQuery(".overlay2").hide();
			});

jQuery(document).on("change","#toTable", function(event){
			// jQuery( "#change" ).change(function() {
  				var toTableValue = jQuery(this).val();
  				jQuery("#newRefNumForOrder").val(toTableValue);
			});
			jQuery(document).on("click","#shiftTheTable", function(event){
				// bootbox.alert("Advanced Settings button clicked");
				jQuery("#advancedSettingsPopup").hide();

				jQuery(".overlay2").hide();
				var fromInvoiceVal = jQuery("#fromTable").val();
				var toTableVal = jQuery("#toTable").val();
				var totableVal = jQuery("#newRefNumForOrder").val();

				AndroidInterface.transferTheTable(fromInvoiceVal,toTableVal,totableVal,jQuery("#invoiceStoreId").val());
			});
			jQuery(document).on("click","#customerLabel", function(event){
				customerSearchPopup();
			});
			jQuery(document).on("click",".customerSelectionButton", function(event){
				customerSearchPopup();
			});

			jQuery(document).on("click","#closeCustomerSearch", function(event){
				closeCustomerSearchPopup();
			});

function customerSearchPopup()
{

		jQuery("#customerSearchPopup").show();
		jQuery(".overlay2").show();
		customerSearchFlag = true;
		typingTextField = false;
		jQuery("#customerSearchBody").html("");
		jQuery("#addCustomerBody").hide();
		jQuery("#addCustomerForm")[0].reset();		
}
function closeCustomerSearchPopup()
{

		jQuery("#customerSearchPopup").hide();
		jQuery(".overlay2").hide();
		jQuery("#customerSearchBody").html("");				    
		customerSearchFlag = false;
		typingTextField = false;
		jQuery("#customerSearchField").val("");
		jQuery("#addCustomerBody").hide();
		jQuery("#addCustomerForm")[0].reset();
}


function customersearchList(customerSearchKey)
{
	console.log(customerSearchKey);
 	searchCustomerForKey(customerSearchKey);
}

function searchCustomerForKey(customerSearchKey){
	if(customerSearchKey.length < 3){ 
		jQuery("#addCustomerBody").hide();
		jQuery("#customerSearchBody").html("");		
		return;
	}
	try{
		setTimeout(function(){
			var storeIdForSearch = jQuery("#invoiceStoreId").val();
			var customerResults =JSON.parse(AndroidInterface.searchCustomerByKey(customerSearchKey,newOrderType,storeIdForSearch));

										if(!	(customerResults === undefined || customerResults === null))
										{
											if(customerResults!=false && customerResults.length!=0)
											{
												var tableStr = "<tr><th>Customer ID #</th><th>Customer Name</th><th>Phone Number</th><th>Available Balance</th></tr>";
												for(p=0;p< customerResults.length; p++){

															var customerRow = customerResults[p]; 
															var customerName =  customerRow.customer_first_name+" "+ customerRow.customer_last_name;
															if(parseFloat(customerRow.customer_available_balance) < 0)
                                                            {
                                                            var updatingBalance = parseFloat(customerRow.customer_available_balance) * (-1);
                                                            }else{
                                                                var updatingBalance = 0;
                                                            }
															jQuery("#oldBalance").html(customerRow.customer_available_balance);


															// var productDetails = JSON.parse(itemRow['storewisePricingAndStock']);
														tableStr += "<tr class=\"tablecustomerrow\" data-customer-account-balance=\"" + (customerRow.customer_available_balance) + "\" data-customername=\"" + customerName + "\" data-customerid=\"" + customerRow.customer_no+"\"><td>"+customerRow.customer_no+"</td><td>"+ customerRow.customer_first_name+" "+ customerRow.customer_last_name+"</td><td>"+customerRow.customer_primary_phone+"</td><td>"+customerRow.customer_available_balance+"</td></tr>";
												}
											}else{
											var tableStr = "<tr><th>Customer ID #</th><th>Customer Name</th><th>Phone Number</th><th>Available Balance</th></tr>";
												
												}
										}else{
											var tableStr = "<tr><th>Customer ID #</th><th>Customer Name</th><th>Phone Number</th><th>Available Balance</th></tr>";
												
										}
										jQuery("#customerSearchBody").html(tableStr);				    
										jQuery("#customerSearchBody tr.tablecustomerrow:first").addClass("active");
							    },500);
	}catch(Err){
		console.log(Err.stack);
	}
}
				jQuery(document).on("click","#saveNewCustomer", function(event){
						
						var serailizedCustomerFormData = jQuery("#addCustomerForm").serializeArray();
						var formInfo = JSON.stringify(serailizedCustomerFormData);
						var storeId = jQuery("#invoiceStoreId").val();

						var newCustomerId = jQuery("#addcustomerid").val();
						var checkValidation = Validate(newCustomerId);
						if(newCustomerId!="" && checkValidation==false)
						{
							AndroidInterface.showToast("Customer ID should have only alphabets and numbers"); return;
						}
						var response = AndroidInterface.saveTheCustomer(formInfo,storeId);

						

						if(response!="false"){
							var customerId = response;
							var customername = (jQuery("#addcustomerfirstname").val()) + " " +  (jQuery("#addcustomerlastname").val());
										jQuery("#customerIdForInvoice").val(customerId);
										jQuery("#customerNameDisplay").html(customername);
										closeCustomerSearchPopup();
						}
						console.log(response);
				});
				jQuery(document).on("click","#addCustomerButton", function(event){
				var oldPhoneNum = jQuery("#customerSearchField").val();
				jQuery("#addcustomerphone").val(oldPhoneNum);
				jQuery("#customerSearchBody").html("");				    
				jQuery("#customerSearchField").val("");
				jQuery("#addCustomerBody").show();
				typingTextField = true;
				customerSearchFlag = false;
				});
				jQuery(document).on("click","#customerSearchBody tr", function(event){
					
					var customerId = jQuery(this).attr("data-customerid");
					var customerName = jQuery(this).attr("data-customername");
					var updatingBalance = jQuery(this).attr("data-customer-account-balance");
                    if(parseFloat(updatingBalance)<0)
                    { jQuery("#balanceBeingPaid").val((-1) * parseFloat(updatingBalance));}
                    else{ jQuery("#balanceBeingPaid").val("0");}

					// alert(productId);

					jQuery("#customerIdForInvoice").val(customerId);
										jQuery("#customerNameDisplay").html(customerName);
										closeCustomerSearchPopup();
										setTimeout(function(){
							    			keyboardFocus();
										},1000);
										
				});

			jQuery(document).on("keydown", "#customerSearchField", function(e) {
						if(e.keyCode==0 || e.keyCode==13)
						{

									var activeRows = jQuery("#customerSearchBody tr.tablecustomerrow.active").length;

									if(activeRows != 0)
									{
										var customerId = jQuery("#customerSearchBody tr.tablecustomerrow.active").attr("data-customerid");
										var customerName = jQuery("#customerSearchBody tr.tablecustomerrow.active").attr("data-customername");
										var updatingBalance = jQuery("#customerSearchBody tr.tablecustomerrow.active").attr("data-customer-account-balance");
                                                if(parseFloat(updatingBalance)<0)
                                                { jQuery("#balanceBeingPaid").val((-1) * parseFloat(updatingBalance));}
                                                else{ jQuery("#balanceBeingPaid").val("0");}

										console.log(customerId+" is the customer id ");
										jQuery("#customerIdForInvoice").val(customerId);
										jQuery("#customerNameDisplay").html(customerName);
										closeCustomerSearchPopup();
										setTimeout(function(){
							    			keyboardFocus();
										},1000);
										
									}
									e.preventDefault();
									return;
						}
						else if(e.keyCode==38)
						{
							try{
								var i=0;
									var foundItem = false;
									jQuery("#customerSearchBody tr.tablecustomerrow").each( function() {

										if(foundItem==false){
											i++;
										}
											
											if(jQuery(this).hasClass("active")){
											 	jQuery(this).removeClass("active");
											 	var nextRowItem = ((i).toString());
											 	console.log(i+" row is active and next row item is "+nextRowItem);
												jQuery("#customerSearchBody tr:nth-child("+ nextRowItem +")" ).addClass("active");
											 	foundItem = true;
											 	return;
											}
									});
									
									var activeRows = jQuery("#customerSearchBody tr.tablecustomerrow.active").length;

									if(activeRows == 0)
									{
										jQuery("#customerSearchBody tr.tablecustomerrow:last-child").addClass("active");
									}
							e.preventDefault();
							}catch(Err){
								console.log(Err.stack);
							}
						}
						else if(e.keyCode==40)
						{
							try{
									var i=0;
									var foundItem = false;


									jQuery("#customerSearchBody tr.tablecustomerrow").each( function() {

										if(foundItem==false){
											i++;
										}
											
											if(jQuery(this).hasClass("active")){
											 	jQuery(this).removeClass("active");
											 	var nextRowItem = ((i+2).toString());
											 	console.log(i+" row is active and next row item is "+nextRowItem);
												jQuery("#customerSearchBody tr:nth-child("+ nextRowItem +")" ).addClass("active");
											 	foundItem = true;
											 	return;
											}
									});

									var activeRows = jQuery("#customerSearchBody tr.tablecustomerrow.active").length;

									if(activeRows == 0)
									{
										jQuery("#customerSearchBody tr.tablecustomerrow:first").addClass("active");
									}
							e.preventDefault();
							}catch(Err){
								console.log(Err.stack);
							}
						}
	});

function Validate(textValid) {
        //Regex for Valid Characters i.e. Alphabets and Numbers.
		var regex = /^[A-Za-z0-9]+$/gi ;
 
        //Validate TextBox value against the Regex.
        var isValid = regex.test(textValid);
        if (!isValid) {
        	try{
        		if(textValid!="")
        		{
            	AndroidInterface.showToast("Only Alphabets and Numbers allowed.");
            	}
        	}catch(Err)
        	{
        		console.log(Err.stack);
        	}
        }
        else{

        }
 
        return isValid;
    }

function InitFinalPay()
{
        if(AndroidInterface.checkConnectivityDetails()=="false")
        {
            return;
        }
    if(jQuery("#holdInvoiceId").val()!="" && isServerPrint==true  )
    {
            if(AndroidInterface.serverInvoiceEditCheck(jQuery("#holdInvoiceId").val(),"invoicecompletion")==false)
            {
                finalPay();
            }
    return;
    }
    else{
        finalPay();
    }
}


function pricingForCustomerRetrieve(itemId,invitemPrice,customerIdForInvoice,invoiceStoreId)
{
    AndroidInterface.printLog("Returning Item Price is  ",invitemPrice);
    var priceForCustomer = AndroidInterface.priceForCustomer(itemId,invitemPrice,customerIdForInvoice,invoiceStoreId);
    return priceForCustomer;
}


function updateBalance()
{
	var currentBalance = jQuery("#oldBalance").html();
	var amountbeingpaid = jQuery("#balanceBeingPaid").val();
	var modePayment = jQuery("#customerPaymentMode").val();
	jQuery("#closeAdvancedSetting").click();


	var newBalance = parseFloat(currentBalance) + parseFloat(amountbeingpaid);
									newBalance = newBalance.toFixed(2);
								    var showString = "Old Balance \""+ (currentBalance.toString())+"\". Amount Being paid \""+(amountbeingpaid)+"\" New Balance \""+newBalance+"\". Are you sure that you want to update the balance ?";
								    bootbox.confirm(showString, function(result){
										if(result)
										{
											bootbox.hideAll();
											AndroidInterface.addBalanceForCustomer(amountbeingpaid,jQuery("#customerIdForInvoice").val(),modePayment);
										}
										bootbox.hideAll();

									});

}
