var pathToImages = "";
var userAssignedStores = false;
var userType = "employee";
var userIdLoggedIn = "";
var userPermissions = false;
var userAdvancedPermissions = false;
var categorywiseTaxes = false;
var overallTaxes = false;
var newOrderType = "store sale";
var hideKeyboardForView = false;
var hasInbuiltKeyboard = true;
var quantityPopupFlag = false;
var payPopupFlag = false
var locallanguageinprinting = "false";
var locallanguageinpos = "false";
var itemsListPrint  = "false";
var isServerPrint = false;
var weighingScaleEnabled = false;
var weighingScalePrefix = "";
var weighingScaleItemLength = 0;
var weighingScaleWeightLength = 0;
var weighingScalePriceLength = 0;
var weighingScaleUnitSuffix = "";
var weighingScaleWeightSuffix = "";

var confirmationAlertFlag = false;
var recallInvoiceFlag = false;
var saveNewInvoiceFlag = false;
var voidedInvoiceId = "";
var itemSearchFlag = false;
var feedbackWebPage = "";

var autoprintKot = false;
var autoprintInvoice = false;
var externalKeyboard = false;
var portraitView = false;
var customerSelectionMandatory = false;


var promptReasonForCancellation = false;
var reasonProvided = false;
var saveOrFetchInPromptReason = "";


var allDepartments = null;

jQuery(document).ready(function(){

setScreenDimensions();
setTimeout(function(){ setScreenDimensions();

					loadAllImages();
					beginTimer();
}, 1000);
setTimeout(function(){
		selectStorePopup();
}, 250);
setTimeout(function(){
keyboardFocus();
},1500);
		try{


			$(document).on("click",".popup-btn-close", function(event){
				closeSelectStorePopup();
			});
		

			$(document).on("click","#selectStoreTableBody tr", function(event){
					var storeId = jQuery(this).attr("data-storeid");
					var StoreName = jQuery(this).html();
					jQuery("#selectedStoreId").html(storeId);
					jQuery("#invoiceStoreId").val(storeId);
					filterCategoriesAndProducts(storeId);
					closeSelectStorePopup();
					try{
						categorywiseTaxes = JSON.parse(AndroidInterface.categorywiseTaxes(storeId,newOrderType));
						overallTaxes = JSON.parse(AndroidInterface.overallTaxes(storeId,newOrderType));
						generateTaxObjects();
						AndroidInterface.saveStoreId(storeId);
					}catch(Err){
						AndroidInterface.showToast(Err.message); AndroidInterface.printLog("NewError",Err.stack);
					}
			});

				// bootbox.alert("Welcome To SrPOS");
				// jQuery("a[href='#top']").click();
				// AndroidInterface.showToast("Welcome");
					// AndroidInterface.scrollToTop();
					// AndroidInterface.showAlertDialogJS("This is a heading after loading",messageForAlertDialog);
					//AndroidInterface.showToast("My Custom Message");
				var parameters = AndroidInterface.initializeParameters();
				var params = JSON.parse(parameters);
				userAssignedStores = params.userAssignedStores;
				if(params.displayImages == false)
				{
					// alert("displayimages is false");
					jQuery("#itemsWithSelection").removeClass("withImages");
					jQuery("#itemsWithSelection").addClass("withoutImages");
				}
				else{

					// alert("displayimages is true");
					jQuery("#itemsWithSelection").removeClass("withoutImages");
					jQuery("#itemsWithSelection").addClass("withImages");
				}
				changeOrderType(params.OrderType);
				currencyHtmlSymbol = (params.currencyType == "") ? "&#8377;" : (params.currencyType);
				currency = params.currency;
				locallanguageinpos = params.locallanguageinpos;
				locallanguageinprinting = params.locallanguageinprinting;

				itemsListPrint = params.itemsListPrint;
				AndroidInterface.printLog("Preferences","Items list print in javascript is "+itemsListPrint)
				isServerPrint = params.isServerPrint;


                if(params.mandatory_customer_selection == true)
                {
                    customerSelectionMandatory = true;
                }

                if(params.prompt_reason_for_cancellation == true)
                {
                    promptReasonForCancellation = true;
                }

				jQuery("#subTotalOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
			    jQuery("#taxesOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
			    jQuery("#grandTotalOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
                autoprintInvoice = params.autoprint_invoice;
                autoprintKot = params.autoprint_kot;
                AndroidInterface.printLog("Kot Check","Auto print kot is "+autoprintKot);
                externalKeyboard = params.external_keyboard_usage;
				userType = params.loggedInUserType;
				userPermissions = params.employee_permissions;
				roundInvoiceAmount = params.roundOff;
				userAdvancedPermissions = params.employee_advanced_permissions;
				if(params.paymentTypes!=false && params.paymentTypes!=null){
						try{

						var paymentType = (params.paymentTypes);
						if(paymentType!=false && paymentType!=null && paymentType.length > 0){
							for(j=0; j< paymentType.length;j++){

								var currentPaymentType = paymentType[j].payment_type;
								
            					jQuery('#modeofpayment').append("<option value=\""+ currentPaymentType +"\"> "+currentPaymentType+"</option>");
							}
						}
						else{
						}
						}catch(Err){
							console.log(Err.stack);
						}

						try{
                        var paymentType = (params.paymentTypes);
                                                        if(currency=="INR"){
                                                            jQuery('#customerPaymentMode').append("<option value=\"UPI\">UPI</option>");
                                                        }
                        if(paymentType!=false && paymentType!=null && paymentType.length > 0){
                                for(j=0; j< paymentType.length;j++){

                                        var currentPaymentType = paymentType[j].payment_type;

                        jQuery('#customerPaymentMode').append("<option value=\""+ currentPaymentType +"\"> "+currentPaymentType+"</option>");
                                }
                        }
                        else{
                        }
                        }catch(Err){
                                AndroidInterface.printLog("CustomerPaymentMode",Err.message);
                                AndroidInterface.showToast(Err.stack);
                        }
                            //customerPaymentMode
				}


				
				if(params.dinein_enabled == false)
				{
					jQuery("#storeSaleTD").hide();	
				}

				if(params.takeaway_enabled == false)
				{
					jQuery("#takeAwayTD").hide();	
				}

				if(params.homedelivery_enabled == false)
				{
					jQuery("#homeDeliveryTD").hide();	
				}
				jQuery("#loggedInUserId").html(params.loggedInUser);
				userIdLoggedIn =  params.loggedInUser;
				if(params.CategoryFilter == false){
					jQuery(".categoriesSelection").hide();
					jQuery("#paymentTypesAndOrderTypes").css({'padding-top':'10px'});
				}else{
					jQuery(".categoriesSelection").show();
				}

                if(params.tableSelectionButton == true ){
                topSelectTableButton(true);
                }
				if(params.dinein_text !=""){jQuery("#storeSaleSpan").html(params.dinein_text);}
				if(params.takeaway_text !=""){jQuery("#takeAwaySpan").html(params.takeaway_text);}
				if(params.homedelivery_text !=""){jQuery("#homeDeliverySpan").html(params.homedelivery_text);}
				pathToImages = params.pathToImages;
				var departments = params.departments;
                allDepartments = params.departments;
				var totalDeptsString = "";
				var departmentActive = "";
				for(var j=0;j< departments.length ; j++)
				{
					var currentDept = departments[j];
					if(j==0)
					{
						departmentActive = "dept_"+currentDept.unique_id;
					}
					try{
						totalDeptsString += "<div id=\"dept_"+ currentDept.unique_id +"\" class=\"eachDepartment\" dataval-categoryid=\""+ currentDept.category_id + "\">" + currentDept.department_id + "</div>";
					}catch(Err)
					{

					}

				}
				jQuery("#departmentsSelection").html(totalDeptsString);

				departmentSelected(departmentActive);
				feedbackWebPage =params.feedbackurl;
				
				var totalCategoriesString = "<td><div class=\"eachCategory active\" data-category=\"\" id=\"All\">All</div></td>";;
				
				var categories = JSON.parse(params.categories);
				totalCategoriesString += "" ;
				for(var k=0; k < categories.length; k++)
				{

					var currentCategory = categories[k];
					
					totalCategoriesString += "<td><div class=\"eachCategory\" data-category=\""+ categories[k].category_id +"\" id=\""+ categories[k].unique_id +"\">" + categories[k].category_id +"</div></td>";
				}
				jQuery("#paymentCustomerButtonsSection .categoriesSelection .table tr").html(totalCategoriesString);
				productsForDepartment(departmentActive);
				
				
				if(params.weighingscale_enabled == true){
					weighingScaleEnabled = true;
					weighingScalePrefix = params.weighingscale_prefix;
					weighingScaleWeightSuffix = params.weighingscale_weight_suffix;
					weighingScaleUnitSuffix = params.weighingscale_unit_suffix;
					weighingScalePriceLength =  parseInt (params.weighingscale_price_length);
					weighingScaleWeightLength = parseInt(params.weighingscale_weight_length);
					weighingScaleItemLength =  parseInt(params.weighingscale_item_length);
					// 
				}else{ weighingScaleEnabled = false; }

			}catch(Err)
			{
				console.log(Err.stack);
          			
			}
			$(document).on("click","#goForScan", function(event){
				jQuery(this).blur();
				if(jQuery("#barcodeNumber").val() == ""){

					try{
					AndroidInterface.showToast("Item Code cannot be blank");
					}catch(Err)
					{
						AndroidInterface.showToast(Err.stack);
					}
					return false;
				}

				try{
                    if(weighingScaleEnabled == true){
                        var toastStr = "";
                        var barcodeString = jQuery("#barcodeNumber").val();
                        var firstCharacter = barcodeString.charAt(0);
                        var lastCharacter = barcodeString.charAt((barcodeString.length)-1);
                        toastStr = "First character is "+firstCharacter+ " and last character is "+lastCharacter;
                        // calculate total length of string
                        var totalLengthOfBarcode = 1 + weighingScaleWeightLength + weighingScaleItemLength + weighingScalePriceLength + 1;
                         toastStr += " Total length of barcode should be " + totalLengthOfBarcode +  " and current length is " + barcodeString.length;
//                         AndroidInterface.showToast(toastStr);
                        if(firstCharacter == weighingScalePrefix && (lastCharacter == weighingScaleUnitSuffix || lastCharacter == weighingScaleWeightSuffix) && totalLengthOfBarcode == (barcodeString.length))
                        {
                            var wsItemId = barcodeString.substring(1, (1+weighingScaleItemLength));
                            var wsQty = barcodeString.substring( (1+weighingScaleItemLength) , (1+weighingScaleItemLength+weighingScaleWeightLength) );
                            // AndroidInterface.showToast("Item Id Is "+wsItemId+" Weigh suffix is "+weighingScaleWeightSuffix+" Unit suffix is "+ weighingScaleUnitSuffix + " quantity is "+wsQty);
                            // jQuery("#barcodeNumber").val()
                            if(lastCharacter == weighingScaleWeightSuffix){
                                wsQty = parseFloat(wsQty) / 1000;
                            }else { wsQty = parseFloat(wsQty) ;}
                            wsQty = wsQty.toFixed(3);
                            retrieveProductDetails( wsItemId , wsQty);
                            return false;
                        }
                    }
                }catch(Err){	console.log(Err.stack);}


				var itemQty = 1;
				if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())>0)
				{
					itemQty = parseFloat(jQuery("#itemQty").val());
				}
				else if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())==0)
				{
					try{
					AndroidInterface.showToast("Invalid Quantity Pressed");
					}catch(Err)
					{
						AndroidInterface.showToast(Err.stack);
					}
					return false;
				}
				retrieveProductDetails(jQuery("#barcodeNumber").val(),itemQty);
				jQuery("#itemQty").val("");
				keyboardFocus();
			});

			jQuery(document).on("keydown", "#barcodeNumber", function(e) {

				if(quantityPopupFlag==false && payPopupFlag==false && confirmationAlertFlag == false && saveNewInvoiceFlag==false){
					if(event.keyCode == 13 || event.keyCode==0) { 
						if(jQuery("#barcodeNumber").val()==""){
							event.preventDefault();
							return;
						}
						event.preventDefault();
					AndroidInterface.showToast("we are here");
					var itemQty = 1;
					if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())>0)
					{
						itemQty = parseFloat(jQuery("#itemQty").val());
					}
					else if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())==0)
					{
						try{
						AndroidInterface.showToast("Invalid Quantity Pressed");
						}catch(Err)
						{
							AndroidInterface.showToast(Err.stack);
						}
						return false;
					}
					try{
					if(weighingScaleEnabled == true){
						var toastStr = "";
						var barcodeString = jQuery("#barcodeNumber").val();
						var firstCharacter = barcodeString.charAt(0);
						var lastCharacter = barcodeString.charAt((barcodeString.length)-1);
						toastStr = "First character is "+firstCharacter+ " and last character is "+lastCharacter;
						// calculate total length of string
						var totalLengthOfBarcode = 1 + weighingScaleWeightLength + weighingScaleItemLength + weighingScalePriceLength + 1;
						 toastStr += " Total length of barcode should be " + totalLengthOfBarcode +  " and current length is " + barcodeString.length;
						 AndroidInterface.showToast(toastStr);
						if(firstCharacter == weighingScalePrefix && (lastCharacter == weighingScaleUnitSuffix || lastCharacter == weighingScaleWeightSuffix) && totalLengthOfBarcode == (barcodeString.length))
						{
							var wsItemId = barcodeString.substring(1, (1+weighingScaleItemLength));
							var wsQty = barcodeString.substring( (1+weighingScaleItemLength) , (1+weighingScaleItemLength+weighingScaleWeightLength) );
							// AndroidInterface.showToast("Item Id Is "+wsItemId+" Weigh suffix is "+weighingScaleWeightSuffix+" Unit suffix is "+ weighingScaleUnitSuffix + " quantity is "+wsQty);
							// jQuery("#barcodeNumber").val()
							if(lastCharacter == weighingScaleWeightSuffix){
								wsQty = parseFloat(wsQty) / 1000;
							}else { wsQty = parseFloat(wsQty) ;}
							wsQty = wsQty.toFixed(3);
							retrieveProductDetails( wsItemId , wsQty);
							return false;							
						}
					}
					}catch(Err){	console.log(Err.stack);}

					retrieveProductDetails(jQuery("#barcodeNumber").val(),itemQty)
					 return false; 
					}
					else if(	event.keyCode == 46 ) {
							var elem = jQuery('#itemsFormBody tr.highlightrow').find(".deleteItemFromCart");
							removeElementFromCart(elem);
					}
				}
				else if(quantityPopupFlag == true){
						if(e.keyCode==13 || e.keyCode==0)
						{
							numpadDoneClicked();
							hideNumPad();
                            keyboardFocus();
							e.preventDefault();
							return;
						}
				}
				else if(confirmationAlertFlag == true){
						if(e.keyCode==13 || e.keyCode==0)
						{

							confirmAlert();
                            keyboardFocus();
							e.preventDefault();
							return;
						}
				}
				else if(saveNewInvoiceFlag==true){
					saveNewInvoice();
                    keyboardFocus();
				}
			});


				jQuery(window).on("keydown", document, function(e) {
					if(e.keyCode == 113){
						e.preventDefault();
						$("#barcodeNumber").focus();
					}
				});

			jQuery(document).on("keydown", "#itemQty", function(e) {
				
				
				if(event.keyCode == 13 || event.keyCode==0) { 
					event.preventDefault();
				
				var itemQty = 1;
				if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())>0)
				{
					itemQty = parseFloat(jQuery("#itemQty").val());
				}
				else if(jQuery("#itemQty").val()!='' && parseFloat(jQuery("#itemQty").val())==0)
				{
					try{
					AndroidInterface.showToast("Invalid Quantity Pressed");
					}catch(Err)
					{
						AndroidInterface.showToast(Err.stack);
					}
					return false;
				}
				jQuery("#itemQty").val("");

				retrieveProductDetails(jQuery("#barcodeNumber").val(),itemQty)
				 return false; 
				}
			});

			$(document).on("click","#selectStoreDiv", function(event){
					
					try{				AndroidInterface.storeSelectionClick(); }catch(Err)	{ bootbox.alert(Err.message); }
			});

			$(document).on("click","#backMenuButton", function(event){
					try{				AndroidInterface.backButtonClick(); }catch(Err)	{ bootbox.alert(Err.message); }
			});

			jQuery(document).on("click",".productImageAndName",function(event){
					var productId = jQuery(this).attr("data-productid");
					retrieveProductDetails(productId,"1");
					keyboardFocus();
			});
			
			$(document).on("click",".eachCategory", function(event){
				var currentCategoryId = jQuery(this).attr("data-category");
				var categoryUniqueId = jQuery(this).attr("id");

				jQuery(".eachCategory").removeClass("active");	
				jQuery("div.eachDepartment").removeClass("active");
				// bootbox.alert(currentCategoryId);
				if(categoryUniqueId == 'All'){
					jQuery(".eachDepartment").show();
					jQuery("div.eachDepartment:first-child").addClass("active");
					var departmentId = jQuery("div.eachDepartment:first-child").attr('id');
					productsForDepartment(departmentId)	
				}
				else{
					jQuery(".eachDepartment").hide();
					jQuery("[dataval-categoryid='"+currentCategoryId+"']").show();
					jQuery("[dataval-categoryid='"+currentCategoryId+"']:first").addClass("active");	

					var departmentId = jQuery("[dataval-categoryid='"+currentCategoryId+"']:first").attr('id');
					productsForDepartment(departmentId)	
				}
				jQuery(this).addClass("active");
			    keyboardFocus();
				
			});


			$(document).on("click","#searchForScan", function(event){
				jQuery(this).blur();
				searchItemPopup();
				//
				// hideKeyboard(jQuery("#barcodeNumber"));
			});


			$(window).on("keyup", document, function(e) {
				
					if(quantityPopupFlag==false && payPopupFlag==false && confirmationAlertFlag ==false && itemSearchFlag == false && customerSearchFlag == false){

						if(e.keyCode == 113){
						e.preventDefault();
						keyboardFocus();
						return;
						}

						if(e.keyCode == 114){
							cardPayment();
							e.preventDefault();
						}
						if(e.keyCode == 115){
								cashPayment();
								e.preventDefault();
						}
						if(e.keyCode == 116){
							try{
								AndroidInterface.storeSelectionClick(); 
								e.preventDefault();
						
							}catch(Err){
								
							}
						}
						if(e.keyCode == 117){
//							saveAndFetchInvoice();
                            InitSaveAndFetchInvoice();
							e.preventDefault();
							jQuery("#orderRefNum").focus();
						}

						if(e.keyCode == 121){
							try{
								changeProductQuantity();
								e.preventDefault();
						        keyboardFocus();
							}catch(Err){
								
							}
						}
						if(e.keyCode == 112){
							searchItemPopup();
							e.preventDefault();
							jQuery("#itemSearchField").focus();
							
						}
					}

				if(quantityPopupFlag==true){
					try{
						var keyString = String.fromCharCode(e.keyCode);

						if(e.keyCode==13 || e.keyCode==0)
						{
							numpadDoneClicked();
							hideNumPad();
							e.preventDefault();
							keyboardFocus();
							return;
						}
						else if(e.keyCode==27)
						{
							jQuery("#easy-numpad-output").html("");
							hideNumPad();
							e.preventDefault();
							keyboardFocus();
							return;
						}

						else if( ((e.keyCode == 8) || (e.keyCode == 46)) )
						{
							var presentNumpadText = jQuery("#easy-numpad-output").text();
							if(presentNumpadText.length !=0){
							// var str= "Hello TecAdmin!";
							var newStr = presentNumpadText.substring(0, presentNumpadText.length - 1);
							jQuery("#easy-numpad-output").html(newStr);
							}
								e.preventDefault();
								return;	
						}
						else if(keyString=="0" || keyString=="1" || keyString=="2" ||  keyString=="3" ||  keyString=="4" ||  keyString=="5" ||  keyString=="6" ||  keyString=="7" ||  keyString=="8" ||  keyString=="9" ||  keyString=="." ||  ((e.keyCode == 110) || (e.keyCode == 190)) )
						{
					var presentNumpadText = jQuery("#easy-numpad-output").text();
					var newCharacter = (	(e.keyCode == 110) || (e.keyCode == 190)) ? (".") : String.fromCharCode(e.keyCode);
					if( (newCharacter=="." && !presentNumpadText.includes('.')) || (newCharacter!=".")){
					presentNumpadText =  presentNumpadText + newCharacter;
					}

					jQuery("#easy-numpad-output").html(presentNumpadText);
					e.preventDefault();
						}

						else if(e.keyCode == 121){
							if(jQuery("#adjustQuantityToRateCheck").is(":checked")){

							    jQuery("#adjustQuantityToRateCheck").prop("checked", false);
							  } else {
							    jQuery("#adjustQuantityToRateCheck").prop("checked", true);
							  }
							quantityRateAdjustment();
							e.preventDefault();
						}
					}catch(Err)
					{
						console.log(Err.stack);
					}
				}else if(payPopupFlag==true){
						try{
							
						var keyString = String.fromCharCode(e.keyCode);
						if(e.keyCode==13 || e.keyCode==0)
						{

							InitFinalPay();
							e.preventDefault();
							return;
						}
						else if(e.keyCode==27)
						{
							jQuery("#easy-numpad-output-2").html("");
							cancelPaymentOption();
							e.preventDefault();
							return;
						}

						else if( ((e.keyCode == 8) || (e.keyCode == 46)) )
						{
							var presentNumpadText = jQuery("#easy-numpad-output-2").text();
							if(presentNumpadText.length !=0){
							// var str= "Hello TecAdmin!";
							var newStr = presentNumpadText.substring(0, presentNumpadText.length - 1);
							jQuery("#easy-numpad-output-2").html(newStr);
							}
								e.preventDefault();
								return;	
						}
						else if(keyString=="0" || keyString=="1" || keyString=="2" ||  keyString=="3" ||  keyString=="4" ||  keyString=="5" ||  keyString=="6" ||  keyString=="7" ||  keyString=="8" ||  keyString=="9" ||  keyString=="." ||  ((e.keyCode == 110) || (e.keyCode == 190)) )
						{
							if (jQuery("#paymentRefNo").is(":focus") || jQuery("#orderRefNo").is(":focus")){
								return;
							}
					var presentNumpadText = jQuery("#easy-numpad-output-2").text();
					var newCharacter = (	(e.keyCode == 110) || (e.keyCode == 190)) ? (".") : String.fromCharCode(e.keyCode);
					if( (newCharacter=="." && !presentNumpadText.includes('.')) || (newCharacter!=".")){
					presentNumpadText =  presentNumpadText + newCharacter;
					}

					jQuery("#easy-numpad-output-2").html(presentNumpadText);
					e.preventDefault();
						}
					}catch(Err){
							AndroidInterface.showToast("Error is "+Err.stack);
						}
					}
				else if(itemSearchFlag == true && e.keyCode != 38 && e.keyCode != 40)
				{
					var itemSearchKey = jQuery("#itemSearchField").val();
					itemsearchList(itemSearchKey);
				}
				else if(customerSearchFlag == true  && e.keyCode != 38 && e.keyCode != 40 )
				{
					var customerSearchKey = jQuery("#customerSearchField").val();
					customersearchList(customerSearchKey);
				}
				else if(recallInvoiceFlag == true)
				{
					if(e.keyCode==27)
					{
						closeRecallPopup();
						recallInvoiceFlag = false;
						e.preventDefault();
					}
				}
				else if(confirmationAlertFlag == true){
					
					if(e.keyCode==13 || e.keyCode==0)
						{
							confirmAlert();
							e.preventDefault();
						}
					if(e.keyCode==27)
					{
						cancelAlert();
						e.preventDefault();
					}	
				}

			});

			jQuery("table.orderTypes").on("click","td", function(event){
				var tableCellId = jQuery(this).attr('id');
				if(tableCellId=="storeSaleTD")
				{
					changeOrderType("store sale");
				}
				else if(tableCellId=="takeAwayTD")
				{
					changeOrderType("take away");	
				}
				else if(tableCellId=="homeDeliveryTD")
				{
					changeOrderType("home delivery");	
				}

			});
			$("#departmentsSelection").on("click",".eachDepartment", function(event){
				departmentSelected(jQuery(this).attr('id'));
				productsForDepartment(jQuery(this).attr('id'));
				keyboardFocus();
			});

			
	});

function hideKeyboard(element) {
    return;

	if(hideKeyboardForView == true)
	{
		try{
		AndroidInterface.hideTheKeyboard();
		}catch(Err)
		{
			AndroidInterface.showToast(Err.stack);
		}
		element.attr('readonly', 'readonly'); // Force keyboard to hide on input field.
    element.attr('disabled', 'true'); // Force keyboard to hide on textarea field.
    setTimeout(function() {
        element.blur();  //actually close the keyboard
        // Remove readonly attribute after keyboard is hidden.
        element.removeAttr('readonly');
        element.removeAttr('disabled');
    }, 100);	
	
	}
/*

	
	
    */
}
function departmentSelected(departmentActive){
	
	jQuery("div.eachDepartment").removeClass("active");
	jQuery("#"+departmentActive).addClass("active");
}
function productsForDepartment(departmentId)
{
	try{

		var w = (window.innerWidth);
		var numberInRow = 4;
		if(w <= 980){
			numberInRow = 3;
		}
		jQuery("#defaultProducts").html("");
	var storeIdOfUser = jQuery("#invoiceStoreId").val();
	var departmentContent = jQuery("#"+departmentId).text();
	var allProducts = AndroidInterface.productsForDepartment(departmentContent,storeIdOfUser);
	var productsList = JSON.parse(allProducts);

	// alert(productsList);
    var productsContent = "";
	var imageCheck = "";

	for(k=0;k<productsList.length;k++){
		var updatingProduct = productsList[k];
		var productImgDetails = "";
		try{
		var itemNum = updatingProduct.inventory_item_no;
        productImgDetails = AndroidInterface.imageNameForProductId(itemNum);
        }
        catch(Err)
        {
            AndroidInterface.printLog("Img check",Err.stack);
        }
		productImageUrl = pathToImages + "/"+ productImgDetails;
		prodctNameToUpdate = updatingProduct.inventary_item_name;
		prodctNameToUpdate = prodctNameToUpdate.substring(0,35);
		if((k==0) || (k) % numberInRow == 0)
		{
			productsContent += "<div class=\"row\" style=\"margin-left:0px; margin-right:0px;\">";	
		}

							if(locallanguageinpos == true && updatingProduct.local_name != ""){
								prodctNameToUpdate = updatingProduct.local_name;
							}

		productsContent += "<div data-productid=\""+ updatingProduct.inventory_item_no + "\" class=\"productImageAndName\">";
		productsContent += "<div class=\"productImage\"><img src=\"file://"+ productImageUrl + "\" onerror=\"this.onerror=null;this.src='images/imagenotavailable.png';\"  /></div>";
		productsContent += "<div class=\"productName\">"+ prodctNameToUpdate	+ "</div>";
		productsContent += "</div>";
		if(	((k+1) % numberInRow == 0) || (k+1)== productsList.length)
		{
			productsContent += "</div>";
		}
//		AndroidInterface.printLog("Img Check",productsContent);
	}
	// bootbox.alert(productsContent);
	// bootbox.alert(imageCheck);
	jQuery("#defaultProducts").html(productsContent);
									
									
							
	}catch(Err){
		// AndroidInterface.showToast(Err.message); 
		// AndroidInterface.printLog("NewError",Err.stack);
	}
}
function changeOrderType(orderType)
{
		jQuery(".orderTypes").find('td').find("i.fa").removeClass("fa-check-square");
		jQuery(".orderTypes").find('td').find("i.fa").addClass("fa-square");


	if(orderType=="store sale")
	{
		jQuery("#storeSaleTD").find("i.fa").removeClass("fa-square");
		jQuery("#storeSaleTD").find("i.fa").addClass("fa-check-square");
		newOrderType = "store sale";
	}
	if(orderType=="take away")
	{
		jQuery("#takeAwayTD").find("i.fa").removeClass("fa-square");
		jQuery("#takeAwayTD").find("i.fa").addClass("fa-check-square");
		newOrderType = "take away";
	}
	if(orderType=="home delivery")
	{
		jQuery("#homeDeliveryTD").find("i.fa").removeClass("fa-square");
		jQuery("#homeDeliveryTD").find("i.fa").addClass("fa-check-square");
		newOrderType = "home delivery";
	}

	try{
		var storeId = jQuery("#invoiceStoreId").val();
					try{

						categorywiseTaxes = JSON.parse(AndroidInterface.categorywiseTaxes(storeId,newOrderType));
						overallTaxes = JSON.parse(AndroidInterface.overallTaxes(storeId,newOrderType));
						generateTaxObjects();
					}catch(Err){
						AndroidInterface.showToast(Err.message); AndroidInterface.printLog("NewError",Err.stack);
					}

	AndroidInterface.saveOrderType(newOrderType);
	jQuery("#holdInvoiceId").val("");
	jQuery("#itemsFormBody").html("");
	resetCartToBlank();
	changeFetchOnHoldButton();
    jQuery("#invoiceAndItemsForm")[0].reset();

	}catch(Err){

	}
}
function loadAllImages()
{
	var images = new Array();
	try{
		allProductImages = AndroidInterface.allImages();
		allImages = JSON.parse(allProductImages);
//		AndroidInterface.printLog("Img Check",allProductImages);
		// bootbox.alert(allImages);
		for(j=0;j< allImages.length;j++)
		{
				var imageRow = allImages[j];
				var itemNo = imageRow.inventory_item_no;
				var productImageUrl = "file://"+pathToImages + "/"+ imageRow.compressed_image_path;
					images[j] = new Image();
					images[j].src = productImageUrl;
		}
	} catch(Err){
		console.log(Err.message);
	}
}
function setScreenDimensions()
{
    if(jQuery("body").hasClass("portrait"))
    {
        setPortraitDimensions();
        portraitView = true;
        return;
    }
	try{
	var w = (window.innerWidth).toString();
	var h = parseInt(jQuery("#completeHolder").height());
//	 AndroidInterface.showToast("width is "+w+"px and height is "+h+"px"+" and ration is "+window.devicePixelRatio);
	// jQuery("body").css({ 'width' : (w+"px"), 'height' : (h+"px"),'overflow':'hidden'});
	
	
	var remainingHeight = parseInt(h) - parseInt(jQuery(".headingSet").height());
	
	jQuery("#rightHalf").css({'height' : (remainingHeight+"px"),'overflow':'hidden'});
	jQuery("#leftHalf").css({'height' : (remainingHeight+"px"),'overflow':'hidden'});
	
	var bottomPercentage = 40;
	if(	((parseInt(w)) > 700) && ((parseInt(w)) < 1000)	)
	{
		bottomPercentage = 38;
	}
	else if(	((parseInt(w)) >= 1000) && ((parseInt(w)) < 1280)	)
	{
		bottomPercentage = 32;
	}
	else if(	((parseInt(w)) >= 1280) )
	{
		bottomPercentage = 30;	
	}


	var bottomDivHeight = parseFloat(h) * (parseFloat(bottomPercentage) ) /100 ;
	var topDivHeight = parseFloat(remainingHeight)  - parseFloat(jQuery("#barcodeScanningSection").height()) -  parseFloat(bottomDivHeight);
	// console.log(bottomDivHeight+" "+topDivHeight);
	var topSpacingForItems = parseInt(jQuery(".headingSet").height());
	
	var topSpacingForLeftButtons = topSpacingForItems + topDivHeight;
	jQuery("#itemsCartSection").css({'height' : ((parseInt(topDivHeight)).toString())+"px" , 'top' : (topSpacingForItems.toString())+"px" });
	jQuery("#leftButtonsSection").css({'height' : ((parseInt(bottomDivHeight)).toString())+"px" ,'top' : (topSpacingForLeftButtons.toString())+"px" });

	// right Section
	var rightPaymentsSectionHeight = parseFloat(h) * (parseFloat(bottomPercentage) ) /100 ;
	var rightItemsSelectionHeight = parseFloat(remainingHeight)  - parseFloat(bottomDivHeight);

	jQuery("#itemsSelector").css({'height' : ((parseInt(rightItemsSelectionHeight)).toString())+"px" , 'top' : "0px" });
	jQuery("#paymentCustomerButtonsSection").css({'height' : ((parseInt(bottomDivHeight)).toString())+"px" ,'top' : (topSpacingForLeftButtons.toString())+"px" });

	jQuery("#itemsWithSelection").css({'height' : ((parseInt(rightItemsSelectionHeight )).toString())+"px" });
	jQuery("#departmentsSelection").css({'height' : ((parseInt(rightItemsSelectionHeight)).toString())+"px" });
	jQuery("#departmentsSelection").css({'min-height' : ((parseInt(rightItemsSelectionHeight)).toString())+"px" });
	
	// AndroidInterface.showToast("Bottom Div Height is "+bottomDivHeight.toString());
		jQuery("#myContainer").css({'display':'block'});
	}catch(Err)
	{
				// AndroidInterface.showToast(Err.message); AndroidInterface.printLog("NewError",Err.stack);
          			
	}
}
function numpadDoneClicked()
{
	try{
							quantityPopupFlag = false;
							var numberEntered = jQuery("#easy-numpad-output").text();
							var numpadType = jQuery("#numbermodificationtype").val();
								if(numpadType=="changequantity")
								{
									updateNewQuantity(numberEntered);
								}
								else if(numpadType=="changeprice"){
									updateNewPrice(numberEntered);
								}
								else if(numpadType=="discountapply"){
									var applicablediscount = parseFloat(numberEntered);
									var discType = jQuery('input[name^="discountType"]:checked').val();
									var activeItemTotalPrice = jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val();

									try{
										if(discType=="percentage" && applicablediscount > 100){
												AndroidInterface.showAlertDialogJS("Error","Discount percentage cannot be greater than 100");
												return false;
											}
										else if(discType=="currency" && (parseFloat(applicablediscount))> (parseFloat(activeItemTotalPrice)))
										{
												AndroidInterface.showAlertDialogJS("Error","Discount value cannot be greater than the item value");
												return false;	
										}
										var overallDisc = false;
										if(jQuery('#' + "overallDiscount").is(":checked")){
												overallDisc = true;
										}

										applyDiscountToCart(discType,applicablediscount,overallDisc);
									}catch(Err){
										console.log(Err.stack);
									}
								}
							hideNumPad();
		}catch(Err)
		{
			console.log(Err.stack);
		}					
}

jQuery(document).ready(function(){
	// 
			
			jQuery(document).on("click","#notesButton", function(event){
				var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows == 0)
				{
					try{
						AndroidInterface.showAlertDialogJS("Error","Please select atleast 1 item to apply discount.");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
				}

					try{
						var forItem = jQuery("tr.highlightrow").find('input[name^="itemName"]').val();
						var enteredNotes = jQuery("tr.highlightrow").find('input[name^="savedRowNotes"]').val();

						var notesButtonsContent = AndroidInterface.notesButtonsForItemId(jQuery("tr.highlightrow").find('input[name^="itemId"]').val());
                        AndroidInterface.printLog("notesButtonContent",notesButtonsContent);
						enableNotesButtons(notesButtonsContent);
								confirmationAlertFlag = true;
    							jQuery("#confirmationHeading").html("Enter the notes for "+forItem);
								jQuery("#confirmationType").val("notesforitem");
								jQuery("#notesTextarea").val(enteredNotes);
								jQuery("#notesTextareaDiv").show();
								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
								return;
					}
					catch(Err){
						console.log(Err.stack);
					}
				});
			jQuery(document).on("click","#discountButton", function(event){
				var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows == 0)
				{
					try{
						AndroidInterface.showAlertDialogJS("Error","Please select atleast 1 item to apply discount.");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
				}

					try{
						permissionCheck = AndroidInterface.userPermissionCheck("discounts");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to give discounts.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}
				jQuery("#numbermodificationtype").val("discountapply");
				jQuery("#adjustquantitytorate").val("false");
				jQuery("#numpadPopupHeading").html("Enter The Discount");
				jQuery("#discountTypeSelection").show();
				jQuery("#quantityAlterationType").hide();
				setTimeout(function(){
				showNumPad();
				},250);
			});
			jQuery(document).on("click","#adjustQuantityToRateCheck", function(event){
								quantityRateAdjustment();
			});
			jQuery(document).on("click","#changeQtyButton", function(event){
				changeProductQuantity();
			});
			jQuery(document).on("click","#changePriceButton", function(event){

				permissionCheck = AndroidInterface.userPermissionCheck("price_hanges");
				if(permissionCheck=="false")
				{
					AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to change the price");
					return;
				}
				else{
					hideNumPad();
					var activeitemName = (jQuery("tr.highlightrow").find('input[name^="itemName"]').val());
						if(activeitemName.includes("Discount"))
						{ 
							AndroidInterface.showAlertDialogJS("Error","Discount has already been applied on the item.Change of price is not permitted after discount."); discountAlreadyApplied=true;
							return;
						}
					changeTheProductPrice();
				}
				// Check Permission for price change 
			});
			jQuery(document).on("click",".overlay2", function(event){
				hideNumPad();
				cancelPaymentOption();
				closeRecallPopup();
				closeConfirmationPopup();
				closeSaveNewInvoicePopup();
				jQuery(".paypopup").hide();
				jQuery(".popup3").hide();
				jQuery("#confirmationPopup").hide();
				jQuery("#advancedSettingsPopup").hide();
				closeCustomerSearchPopup();
				
			});
			jQuery(document).on("click","#done", function(event){
				hideNumPad();
			});
			jQuery(document).on("click","#cancel", function(event){
				hideNumPad();
			});
			
});
function closeSaveNewInvoicePopup()
{
	saveNewInvoiceFlag = false;
	jQuery("#saveInvoiceTableSelectionPopup").hide();
	jQuery(".overlay2").hide();
}
function hideNumPad()
{
	quantityPopupFlag = false;
	jQuery("#easy-numpad-output").html("");
	jQuery(".popup2").hide();
	jQuery(".overlay2").hide();
	jQuery("#discountTypeSelection").hide();
	jQuery("#quantityAlterationType").hide();
	jQuery("#adjustQuantityToRateCheck"). prop("checked", false);

}
function showNumPad()
{
	if(jQuery("#recallInvoiceId").val()!=""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please clear the recalled invoice");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
	}

	quantityPopupFlag = true;
	jQuery(".popup2").show();
	jQuery(".overlay2").show();
	// hideKeyboard(jQuery("#barcodeNumber"));
	try{
		AndroidInterface.hideTheKeyboard();
	}catch(Err)
	{
		AndroidInterface.showToast(Err.stack);
	}	
}
function closeSelectStorePopup()
{
	jQuery("#selectStorePopup").hide();
	jQuery(".overlay").hide();
	hideKeyboard(jQuery("#barcodeNumber"));
	try{
		AndroidInterface.hideTheKeyboard();
	}catch(Err)
	{
		AndroidInterface.showToast(Err.stack);
	}
}
function selectStorePopup()
{
	try{
	var storesTableContent = "";
	    if(userAssignedStores.length == 0)
    	{
    	    AndroidInterface.showAlertDialogJS("Error","Store ID is blank.");
    	}
	if(userAssignedStores.length > 1)
	{

	jQuery(".overlay").show();
	for(k=0; k< userAssignedStores.length;k++)
	{
		var store = userAssignedStores[k];
		storesTableContent += "<tr data-storeid=\""+ store.StoreId +"\">"+"<td>"+store.StoreName +"</td></tr>";
	}
	jQuery("#selectStoreTableBody").html(storesTableContent);

		var scrollTop = '';
		var newHeight = '100';
			scrollTop = $( window ).scrollTop();
		   newHeight = scrollTop + 100;

	
		   
		   jQuery('#selectStorePopup').removeClass('popup-mobile').css('top', newHeight).toggle();
	}
	else if(userAssignedStores.length == 1){
		var storeId = userAssignedStores[0].StoreId;

		jQuery("#invoiceStoreId").val(storeId);
		jQuery("#selectedStoreId").html(userAssignedStores[0].StoreId);
		filterCategoriesAndProducts(storeId);
		closeSelectStorePopup();

					try{
						categorywiseTaxes = JSON.parse(AndroidInterface.categorywiseTaxes(storeId,newOrderType));
						overallTaxes = JSON.parse(AndroidInterface.overallTaxes(storeId,newOrderType));
						generateTaxObjects();
						AndroidInterface.saveStoreId(storeId);
					}catch(Err){
						AndroidInterface.showToast(Err.message); AndroidInterface.printLog("NewError",Err.stack);
					}
					keyboardFocus();
	}	 
	// bootbox.alert(userAssignedStores.toString());
	}catch(Err)
	{
				console.log(Err.stack);
          			
	}
}
function filterCategoriesAndProducts(storeId)
{
	try{
		var categoriesAndDepartmentsForStore = JSON.parse(AndroidInterface.categoriesAndDepartmentsForStore(storeId));
		if (!	(categoriesAndDepartmentsForStore.categories === undefined || categoriesAndDepartmentsForStore.categories === null))
		{	
		var totalCategoriesString = "<td><div class=\"eachCategory active\" data-category=\"\" id=\"All\">All</div></td>";;
				var categories = JSON.parse(categoriesAndDepartmentsForStore.categories);
				var departments = JSON.parse(categoriesAndDepartmentsForStore.departments);
				totalCategoriesString += "" ;
				for(var k=0; k < categories.length; k++)
				{

					var currentCategory = categories[k];
					
					totalCategoriesString += "<td><div class=\"eachCategory\" data-category=\""+ categories[k].category_id +"\" id=\""+ categories[k].unique_id +"\">" + categories[k].category_id +"</div></td>";
				}
				jQuery("#paymentCustomerButtonsSection .categoriesSelection .table tr").html(totalCategoriesString);
				

				var totalDeptsString = "";
				var departmentActive = "";
				for(var j=0;j< departments.length ; j++)
				{
					var currentDept = departments[j];
					if(j==0)
					{
						departmentActive = "dept_"+currentDept.unique_id;
					}
					try{
						totalDeptsString += "<div id=\"dept_"+ currentDept.unique_id +"\" class=\"eachDepartment\" dataval-categoryid=\""+ currentDept.category_id + "\">" + currentDept.department_id + "</div>";
					}catch(Err)
					{

					}

				}
				jQuery("#departmentsSelection").html(totalDeptsString);
				console.log(totalDeptsString);
				departmentSelected(departmentActive);
		}
				var departmentActive = jQuery(".eachDepartment:first").attr("id");
				productsForDepartment(departmentActive);		
	}catch(Err)
	{
				AndroidInterface.showToast(Err.message+" line number "+Err.stack);
          			
	}	
}

function scrollBottomToDiv()
                {
                    var wtf    = jQuery('#itemsCartSection');
                    var height = jQuery('#itemsCartSection').scrollHeight;
                    wtf.scrollTop(height);
                    jQuery('#itemsCartSection').animate({
                        scrollTop: jQuery('#itemsCartSection')[0].scrollHeight}, "fast");
                }
function changeProductQuantity(){
	var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows == 0)
				{
					try{
						AndroidInterface.showAlertDialogJS("Error","Please select atleast 1 item to apply change of Quantity");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
				}
				jQuery("#numbermodificationtype").val("changequantity");
				jQuery("#adjustquantitytorate").val("false");
				jQuery("#numpadPopupHeading").html("Enter The Quantity");
				jQuery("#quantityAlterationType").show();
				jQuery("#discountTypeSelection").hide();
				setTimeout(function(){
				showNumPad();
				},250);
}
function quantityRateAdjustment(){
								
								if(jQuery("#adjustQuantityToRateCheck").is(":checked")){
										jQuery("#adjustquantitytorate").val("true");
										jQuery("#numpadPopupHeading").html("Enter The Price To adjust quantity");
								}
								else{
									jQuery("#numpadPopupHeading").html("Enter The Quantity");
									jQuery("#adjustquantitytorate").val("false");	
								}
}

function GetFormattedDate(date) {
	const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
		  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
		];
	
	var year = date.getFullYear();

var month = (1 + date.getMonth()).toString();
month = month.length > 1 ? month : '0' + month;

month = monthNames[month-1];
var day = date.getDate().toString();
day = day.length > 1 ? day : '0' + day;
var hours = date.getHours();
hours = parseInt(hours) > 9 ? hours : '0' + hours;
var minutes = date.getMinutes();
minutes = parseInt(minutes) > 9 ? minutes : '0' + minutes;
return month + ' ' + day + ',' + year + " "+hours + ":"+minutes;
}
function formatAMPM(date) {
  var hours = date.getHours();
  var minutes = date.getMinutes();
  var ampm = hours >= 12 ? 'pm' : 'am';
  hours = hours % 12;
  hours = hours ? hours : 12; // the hour '0' should be '12'
  minutes = minutes < 10 ? '0'+minutes : minutes;
  var strTime = hours + ':' + minutes + ' ' + ampm;
  return strTime;
}
function beginTimer()
{
	var date = new Date();
	var t = date.getTime();
var hours = date.getHours();
hours = parseInt(hours) > 9 ? hours : '0' + hours;
var minutes = date.getMinutes();
minutes = parseInt(minutes) > 9 ? minutes : '0' + minutes;

	jQuery("#timeDisplay").html(formatAMPM(date));
setTimeout(function(){ 
beginTimer();
}, 30000);
	
}



// posWebViewInvoicing.js File Conents

var newProductJSON = false;
var currencyHtmlSymbol = "&#8377;";
var currency = "";
var overAllTaxObject;
var categoryWiseTaxObj;
var roundInvoiceAmount = false; 
var allTaxesForSaving = [];
	var totalQty = 0;
	var subTotal = 0;
	var taxesInInvoice = 0;
	var grandTotal = 0;

jQuery(document).ready(function(){

});
function formDataOfCart()
{
	var serializedForm = { };
		serializedForm.holdInvoiceId = jQuery("#holdInvoiceId").val();
		serializedForm.storeIdForInvoice = jQuery("#invoiceStoreId").val();
		serializedForm.deletedItemsUniqueIdsFromSavedInvoice = jQuery("#deletedItemsUniqueIdsFromSavedInvoice").val();
		var inputValues = jQuery('input[name^="savedRowNotes"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.newNotes = inputValues;
		var inputValues = jQuery('input[name^="oldNotes"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.oldNotes = inputValues;
		var inputValues = jQuery('input[name^="savedRowUniqueId"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.savedOlderUniqueIds = inputValues;
		var inputValues = jQuery('input[name^="itemId"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.itemIds = inputValues;


		var inputValues = jQuery('input[name^="categoryid"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.categoryIds = inputValues;


		var inputValues = jQuery('input[name^="totalItemPrice"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.totalItemPrices = inputValues;
		var inputValues = jQuery('input[name^="itemQuantity"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.itemQuantitys = inputValues;

		//
		var inputValues = jQuery('input[name^="discountProvidedInCurrency"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.discountInCurrency = inputValues;

		var inputValues = jQuery('input[name^="itemName"]').map(function() {
    				return $(this).val();
			}).toArray();
		serializedForm.itemNames = inputValues;

		var inputValues = jQuery('input[name^="savedRowNotes"]').map(function() {
    				return $(this).val();
			}).toArray();

		serializedForm.itemNotes = inputValues; 

		var inputValues = jQuery('input[name^="savedRowQuantity"]').map(function() {
    				return $(this).val();
			}).toArray();

		serializedForm.savedQuantities = inputValues; 
		
		serializedForm.paymentType1 = jQuery("#paymentType1").val();
		serializedForm.paymentType2 = jQuery("#paymentType2").val();
		serializedForm.paymentType3 = jQuery("#paymentType3").val();
		serializedForm.paymentType4 = jQuery("#paymentType4").val();
		
		serializedForm.modeOfPayment1 = jQuery("#modeOfPayment1").val();
		serializedForm.modeOfPayment2 = jQuery("#modeOfPayment2").val();
		serializedForm.modeOfPayment3 = jQuery("#modeOfPayment3").val();
		serializedForm.modeOfPayment4 = jQuery("#modeOfPayment4").val();


		serializedForm.paymentRefNo1 = jQuery("#paymentRefNo1").val();
		serializedForm.paymentRefNo2 = jQuery("#paymentRefNo2").val();
		serializedForm.paymentRefNo3 = jQuery("#paymentRefNo3").val();
		serializedForm.paymentRefNo4 = jQuery("#paymentRefNo4").val();
		

		serializedForm.grandTotal = jQuery("#grandTotal").val();
		serializedForm.customerIdForInvoice = jQuery("#customerIdForInvoice").val();
		serializedForm.orderRefNo = jQuery("#orderRefNo").val(); 
		serializedForm.changeTendered = jQuery("#changeTendered").val();
		serializedForm.orderDeliveryDate = jQuery("#orderDeliveryDate").val();
		
		serializedForm.holdOrderRefNumForNewInvoice = jQuery("#orderRefNum").val();
		serializedForm.holdOrderTableNum = jQuery("#tableSelected").val();
		
		serializedForm.allTaxes = allTaxesForSaving;
		// []
	return serializedForm;	
}
function saveProductToCart()
{
	if(jQuery("#recallInvoiceId").val()!=""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please clear the recalled invoice");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
	}
	var previousAddedQty = 0;var  previousSavedQty = 0; var previousUniqueId = "";
	jQuery('#itemsFormBody tr').removeClass("highlightrow");
            jQuery('#itemsFormBody tr').each( function() {
            	if(jQuery(this).find('input[name^="itemId"]').val()== newProductJSON.itemId){
            		// AndroidInterface.showToast(newProductJSON.itemName+" already exists");
            		var previousTotalPrice = jQuery(this).find('input[name^="totalItemPrice"]').val();
            		var previousTotalQty =  jQuery(this).find('input[name^="itemQuantity"]').val();
            		var previousUnitPrice = parseFloat(previousTotalPrice) / parseFloat(previousTotalQty);
            		previousUniqueId = jQuery(this).find('input[name^="savedRowUniqueId"]').val();
            		previousSavedQty =  jQuery(this).find('input[name^="savedRowQuantity"]').val();

            		if(previousUnitPrice == parseFloat(newProductJSON.itemRate))
            		{
            			jQuery(this).remove();
            			previousAddedQty = previousTotalQty;
            		}

            	}
            });
    
	var totalItemPrice = ((parseFloat(newProductJSON.itemQuantity)) + (parseFloat(previousAddedQty))) * parseFloat(newProductJSON.itemRate);
	totalItemPrice = totalItemPrice.toFixed(2);
	var updateQty = Number((parseFloat(previousAddedQty) + parseFloat(newProductJSON.itemQuantity)).toFixed(3));
	var rowcount = ((jQuery("#itemsFormBody tr").length)+1).toString();
	var hiddenParameters = "<input type=\"hidden\" name=\"categoryUniqueId[]\" value=\""+newProductJSON.categoryUniqueId+"\" /><input type=\"hidden\" name=\"categoryid[]\" value=\""+newProductJSON.categoryId+"\" /><input type=\"hidden\" name=\"savedRowQuantity[]\" value=\""+previousSavedQty+"\"><input type=\"hidden\" name=\"savedRowUniqueId[]\" value=\""+previousUniqueId+"\"><input type=\"hidden\" name=\"oldNotes[]\" /><input type=\"hidden\" name=\"savedRowNotes[]\" value=\"\"><input type=\"hidden\" name=\"discountProvidedInCurrency[]\" value=\"0\" />";
	itemRowHtml = "<tr class=\"highlightrow\"><td>"+rowcount+"</td><td>"+hiddenParameters+"<input type=\"hidden\" name=\"itemId[]\" value=\""+newProductJSON.itemId+"\" />"+newProductJSON.itemId+"</td><td><input type=\"hidden\" name=\"itemName[]\" value=\""+newProductJSON.itemName+"\" /><span class=\"itemNametd\">"+newProductJSON.itemName+"</span></td><td><input type=\"hidden\" name=\"itemUnitPrice[]\" value=\"" + newProductJSON.itemRate + "\" /><input type=\"hidden\" name=\"itemQuantity[]\" value=\""+updateQty+"\" /><span class=\"updateQtytd\">"+(updateQty.toString())+"</span></td><td><input type=\"hidden\" name=\"units[]\" value=\""+newProductJSON.units+"\" />"+newProductJSON.units+"</td><td style=\"text-align:right;\"><input type=\"hidden\" name=\"totalItemPrice[]\" value=\""+totalItemPrice+"\" /><span class=\"totalItemPricetd\">"+totalItemPrice+"</span></td><td class=\"deleteItemFromCart\" style=\"padding-right:10px;\"><i class=\"fa fa-trash\"></i></td></tr>";
	jQuery("#itemsFormBody").append(itemRowHtml);
	scrollBottomToDiv();
	reorganizeNumbersInRows();
	changeFetchOnHoldButton();
	try{
	    if(newProductJSON.promptPrice=="yes")
	    {
	    	hideNumPad();
    	changeTheProductPrice();
	    }
	    else if(newProductJSON.promptQuantity=="yes"){
	    	changeProductQuantity();
	    }
	}catch(Err){
		console.log(Err.stack);
	}
	newProductJSON = false;
	calculateGrandTotal();


}
function changeTheProductPrice()
{

				var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows == 0)
				{
					try{
						AndroidInterface.showAlertDialogJS("Error","Please select atleast 1 item to apply change of Price");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
				}
				jQuery("#numbermodificationtype").val("changeprice");
				jQuery("#adjustquantitytorate").val("false");
				jQuery("#numpadPopupHeading").html("Enter The Price");
				setTimeout(function(){
				showNumPad();
				},250);
}
function reorganizeNumbersInRows(){
	var currentRow = 0;
	jQuery('#itemsFormBody tr').each( function() {
            	currentRow = parseInt(currentRow) + 1;
            	jQuery(this).find("td:first").html( currentRow.toString());
	});
	if(currentRow==0){
		resetCartToBlank();
		changeFetchOnHoldButton();
	}
}

	(function() { 

    // how many milliseconds is a long press?
    var longpress = 2000;
    // holds the start time
    var start;

    jQuery( "#itemsFormBody tr" ).on( 'mousedown', function( e ) {
    	AndroidInterface.showToast('Mouse Down!');   
        start = new Date().getTime();
    } );

    jQuery( "#itemsFormBody tr" ).on( 'mouseleave', function( e ) {
        start = 0;
        AndroidInterface.showToast('Mouse Up!');   
    } );

    jQuery( "#itemsFormBody tr" ).on( 'mouseup', function( e ) {
        if ( new Date().getTime() >= ( start + longpress )  ) {
           AndroidInterface.showToast('long press!');   
        } 
    } );

}());
jQuery(document).ready(function(){


jQuery(document).on("click","#payInvoiceButton", function(event){
		var itemcount = jQuery("#itemsFormBody tr").length;
		if(itemcount > 0){
		if(customerSelectionMandatory==true && (jQuery("#customerIdForInvoice").val()=="") ){  AndroidInterface.showAlertDialogJS("Sorry! Customer Selection is Mandatory","Please select a customer to raise the invoice"); ; return; }
			loadPaypopup();

                    if(autoprintInvoice==true)
                    {
                        jQuery("#confirmPaymentBtn").click();
                    }
		}
		else{
			AndroidInterface.showAlertDialogJS("Sorry","Please select atleast 1 item to raise the invoice");
		}
	});
jQuery(document).on("click","#cashPaymentButton", function(event){
	var itemcount = jQuery("#itemsFormBody tr").length;
		if(itemcount > 0)
		{	cashPayment();}
		else{
			AndroidInterface.showAlertDialogJS("Sorry","Please select atleast 1 item to raise the invoice");
		}
});

jQuery(document).on("click","#upiPaymentButton", function(event){
	var itemcount = jQuery("#itemsFormBody tr").length;
		if(itemcount > 0)
		{	UPIPayment();}
		else{
			AndroidInterface.showAlertDialogJS("Sorry","Please select atleast 1 item to raise the invoice");
		}
});

jQuery(document).on("click","#cardPaymentButton", function(event){
	var itemcount = jQuery("#itemsFormBody tr").length;
		if(itemcount > 0)
		{	cardPayment();}
		else{
			AndroidInterface.showAlertDialogJS("Sorry","Please select atleast 1 item to raise the invoice");
		}
});

jQuery(document).on("click","#cancelPaymentButton", function(event){
	cancelPaymentOption();
});
jQuery(document).on("click","#fetchandSaveInvoiceButton", function(event){
//	saveAndFetchInvoice();
	InitSaveAndFetchInvoice();
});

jQuery(document).on("click","#printLastInvoiceBtn", function(event){
		printLastInvoice();
});
	jQuery("#itemsFormBody").on("click",".deleteItemFromCart", function(event){
		// bootbox.alert("Delete clicked");
		// Check if it is new item
		removeElementFromCart(jQuery(this));
	});
	jQuery('#itemsFormBody').on("click","tr", function(event){

	jQuery('#itemsFormBody tr').removeClass("highlightrow");
	jQuery(this).addClass("highlightrow");

	});
});
function removeElementFromCart(elem)
{

		if(jQuery("#recallInvoiceId").val()!=""){
			try{
				AndroidInterface.showAlertDialogJS("Error","You cannot delete an item from recalled invoice");
				return;			
			}catch(Err){
				console.log(Err.stack);
			}
		}
		// check permission to delete items from cart 

		var savedRowId = elem.parent().find('input[name^="savedRowUniqueId"]').val();
						if(savedRowId!="")
						{
							permissionCheck = AndroidInterface.userPermissionCheck("delete_items");
							if(permissionCheck=="false" && (jQuery("#holdInvoiceId").val()!=""))
							{

								AndroidInterface.showAlertDialogJS("Error","You cannot delete an item from saved invoice");
								return;			
							}
							var currentUniqueIdsVals = jQuery("#deletedItemsUniqueIdsFromSavedInvoice").val();
							if(currentUniqueIdsVals==""){
								jQuery("#deletedItemsUniqueIdsFromSavedInvoice").val(savedRowId);
							}else{ jQuery("#deletedItemsUniqueIdsFromSavedInvoice").val( currentUniqueIdsVals + ","+savedRowId );}
						}
		elem.parent().remove();
		reorganizeNumbersInRows();
		calculateGrandTotal();
			setTimeout(function(){
		jQuery('#itemsFormBody tr:last-child').addClass("highlightrow");
				},125);
		
		var itemcount = jQuery("#itemsFormBody tr").length;
		if(itemcount==0){
			try{
				// AndroidInterface.showToast("We have to reset the cart and flags to blank");
				resetCartToBlank();
			}catch(Err){
				console.log(Err.stack);
			}
		}						
}
function InitSaveAndFetchInvoice()
{

                                if(AndroidInterface.checkConnectivityDetails()=="false")
                                {
                                    return;
                                }
                                if(jQuery("#holdInvoiceId").val()!="" && AndroidInterface.serverInvoiceEditCheck(jQuery("#holdInvoiceId").val(),"invoicesavehold")==true)
								{

							        showLoadingPopupModal();
								    return;
								}else{
								    saveAndFetchInvoice();
								    try{
								        AndroidInterface.stopLoadingDialog();
								    }catch(Err){
								    }
								}
}
// Product Information Display 
function retrieveProductDetails(productId,qty){

	jQuery("#barcodeNumber").val("");
	jQuery("#itemQuantity").val("");
	try{
					var productDetails = AndroidInterface.productDetails(productId,newOrderType,(jQuery("#invoiceStoreId").val()));
					var productInfo = JSON.parse(productDetails);
					
					if(productInfo.is_item == true && productInfo.product_exists == true)
					{
						var storewiseInfo = JSON.parse(productInfo.storewise_pricing_info);
						var inventoryInfo = JSON.parse(productInfo.inventory_info);
						var optionalInfo = JSON.parse(productInfo.optional_info);
						var orderingInfo = JSON.parse(productInfo.ordering_info);
						var departmentInfo = JSON.parse(productInfo.dept_info);
						var invCategory = "";
						// alert(storewiseInfo.toString());
							var invitemName = inventoryInfo[0].inventary_item_name;
							if(locallanguageinpos == true && inventoryInfo[0].local_name != ""){
								invitemName = inventoryInfo[0].local_name;
							}
							var invitemId = inventoryInfo[0].inventory_item_no;
							var invitemPrice = inventoryInfo[0].inventary_price_tax;
							var invitemPromptQuantity = "no";
							var invitemPromptPrice = "no";
							var invitemUnits =  "";
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
						if(storewiseInfo != false && storewiseInfo.length !=0)
						{
							if(newOrderType=="store sale")
							{
								if(storewiseInfo[0].price!=""){
								invitemPrice = (storewiseInfo[0].price);
								}else{ invitemPrice = inventoryInfo[0].inventary_price_tax; }
							}
							else if(newOrderType=="take away"){
									
								if(storewiseInfo[0].storetakeaway_price!=""){
								invitemPrice = (storewiseInfo[0].storetakeaway_price);
								}else{ invitemPrice = inventoryInfo[0].takeaway_pricewithtax; }

							}
							else if(newOrderType=="home delivery"){
								if(storewiseInfo[0].home_delivery_price!=""){
								invitemPrice = (storewiseInfo[0].home_delivery_price);
								}else{ invitemPrice = inventoryInfo[0].takeaway_pricewithtax; }
							}
						}
						else{
							if(newOrderType=="store sale")
							{
								invitemPrice = inventoryInfo[0].inventary_price_tax;
							}
							else if(newOrderType=="take away" || newOrderType=="home delivery"){
								invitemPrice = inventoryInfo[0].takeaway_pricewithtax;
							}
						}
						if(jQuery("#customerIdForInvoice").val()!="")
                        {
                            invitemPrice = pricingForCustomerRetrieve(inventoryInfo[0].inventory_item_no,invitemPrice,jQuery("#customerIdForInvoice").val(),jQuery("#invoiceStoreId").val());
                        }
						if(productInfo.is_plu==true){
						// console.log("PLU is true");
							var pluInfo = JSON.parse(productInfo.plu_info);
							var pluRate = pluInfo[0]['sp_tax'];
							var pluCode = pluInfo[0]['plu_number'];
							var pluName = pluInfo[0]['item_name'];
							newProductJSON = { categoryUniqueId : invCategoryUniqueId,categoryId : invCategory , itemRate : (pluRate.toString()) ,  itemName : pluName , itemId : pluCode , promptQuantity : invitemPromptQuantity , promptPrice : invitemPromptPrice , units : invitemUnits, itemQuantity : qty };
						}
						else{
						newProductJSON = { categoryUniqueId : invCategoryUniqueId,categoryId : invCategory , itemRate : (invitemPrice.toString()) ,  itemName : invitemName , itemId : invitemId , promptQuantity : invitemPromptQuantity , promptPrice : invitemPromptPrice , units : invitemUnits, itemQuantity : qty };
						}
						// alert(newProductJSON.itemPrice);
						saveProductToCart();
					}
					if(productInfo.is_item == false && productInfo.multiple_barcodes==true){
						var itemsListSelectionString = "<tr><th>Item #</th><th>Product Name</th><th>Price</th><th>In Stock</th></tr>";
						var itemsBarcodesList = JSON.parse(productInfo.barcodes_list);
							for(k=0; k < itemsBarcodesList.length ; k++){
								var currentBarcode = itemsBarcodesList[k];
								var storeIdForSearch = jQuery("#invoiceStoreId").val();
								var itemStorewiseDetails = AndroidInterface.productStorewisePricingAndStock(currentBarcode.inventory_item_no,newOrderType,storeIdForSearch);
								var productPrice = currentBarcode.inventary_price_tax;
								var productStock = "0";

								itemStorewiseDetails = JSON.parse(itemStorewiseDetails);

								productPrice = itemStorewiseDetails.price;
								productStock = itemStorewiseDetails.stockCount;

								itemsListSelectionString += "<tr  class=\"tableitemrow \" data-itemid=\"" + currentBarcode.inventory_item_no+"\"><td>"+currentBarcode.inventory_item_no+"</td><td>"+currentBarcode.inventary_item_name+"</td><td>"+productPrice+"</td><td>"+productStock+"</td>";

							}												
						multipleItemsSelectionPopup(itemsListSelectionString);
					}
					if(productInfo.is_item == false && productInfo.multiple_plu_barcodes==true){
						var itemsListSelectionString = "<tr><th>Item #</th><th>Product Name</th><th>Price</th><th>In Stock</th></tr>";
						var itemsBarcodesList = JSON.parse(productInfo.plu_barcodes_list);
							for(k=0; k < itemsBarcodesList.length ; k++){
								var currentBarcode = itemsBarcodesList[k];
								var storeIdForSearch = jQuery("#invoiceStoreId").val();
								var itemStorewiseDetails = AndroidInterface.productStorewisePricingAndStock(currentBarcode.item_no,newOrderType,storeIdForSearch);
								var productPrice = currentBarcode.sp_tax;
								var productStock = "0";

								itemStorewiseDetails = JSON.parse(itemStorewiseDetails);

								productStock = itemStorewiseDetails.stockCount;
									
								itemsListSelectionString += "<tr  class=\"tableitemrow \" data-itemid=\"" + currentBarcode.plu_number+"\"><td>"+currentBarcode.plu_number+"</td><td>"+currentBarcode.item_name+"</td><td>"+productPrice+"</td><td>"+productStock+"</td>";

							}												
						multipleItemsSelectionPopup(itemsListSelectionString);
					}
					// console.log(productInfo);
					}catch(Err){
						AndroidInterface.showToast(Err.stack);
					}
}
function multipleItemsSelectionPopup(itemsListSelectionString)
{
	jQuery("#recalledInvoicesBody").html(""); jQuery("#holdInvoicesBody").html("");jQuery("#itemSearchBody").html("");
	jQuery("#itemSearchSpan").hide();
	jQuery("#itemSearchTextfield").hide();
	jQuery("#recallHeadingText").show();
	jQuery("#recallHeadingText").html("Select The Product");
	jQuery("#itemSearchField").val("");
		displayRecallPopup();	
	itemSearchFlag = true;
	jQuery("#itemSearchBody").html(itemsListSelectionString);
}
function calculateTaxes()
{	 
	var overallTaxesForSaving = [];
	var categoryTaxesForSaving = [];
	var totalCategoryWiseTax = 0;
	var totalOverallTax = 0;
	var hasCategoryTax = false;
	try{
	if(!	(categoryWiseTaxObj === undefined || categoryWiseTaxObj === null))
	{
		if(categoryWiseTaxObj!=false && categoryWiseTaxObj.length > 0)
		{

								for(k=0; k< categoryWiseTaxObj.length; k++)
								{
									categoryTaxesForSaving[k] = categoryWiseTaxObj[k];
									 categoryTaxesForSaving[k]["tax_calculated_value"] = 0;
								}
								
				jQuery('#itemsFormBody tr').each( function() {
					var itemCategoryId = jQuery(this).find('input[name^="categoryid"]').val();
					var itemCategoryUniqueId =  jQuery(this).find('input[name^="categoryUniqueId"]').val();
					var itemTotalPrice = jQuery(this).find('input[name^="totalItemPrice"]').val();

								for(k=0; k< categoryWiseTaxObj.length; k++)
								{
									var categoryWiseTaxRow = categoryWiseTaxObj[k];
									var categoryWiseTaxCalcRow = categoryTaxesForSaving[k];
									if(categoryWiseTaxRow.category == itemCategoryId)
									{
										hasCategoryTax = true;
									var taxPercentage = categoryWiseTaxRow.tax_value;
									var taxInRupees =  parseFloat(itemTotalPrice) * parseFloat(taxPercentage) / 100; 
									//var taxRowForAllTax = { taxname : categoryWiseTaxObj[k]['tax_name'] , tax_percentage : taxPercentage, tax_value : taxInRupees , categoryId :  categoryWiseTaxRow.category };
									// categoryTaxesForSaving.push(taxRowForAllTax);
									var existingCategoryTaxValue = parseFloat(categoryWiseTaxCalcRow.tax_calculated_value);
									var newCategoryTaxValue = existingCategoryTaxValue + taxInRupees;
									categoryWiseTaxCalcRow.tax_calculated_value = newCategoryTaxValue;
									categoryTaxesForSaving[k] = categoryWiseTaxCalcRow;
									categoryId = categoryWiseTaxRow.category;
									
									totalCategoryWiseTax = totalCategoryWiseTax + taxInRupees;
									// AndroidInterface.showToast(taxPercentage+" - "+(categoryWiseTaxRow.tax_name));					
									}
								}
				});

				
		}

	}

	if(!	(overAllTaxObject === undefined || overAllTaxObject === null))
	{
		if(overAllTaxObject!=false && overAllTaxObject.length > 0)
		{
			for(k=0; k< overAllTaxObject.length; k++)
			{
				var overallTaxRow = overAllTaxObject[k];
				var taxPercentage = parseFloat( overallTaxRow.tax_value );
				var taxValue = parseFloat(subTotal) * taxPercentage / 100;
				var taxRowForAllTax = { tax_name : overallTaxRow['tax_name'],tax_percentage : taxPercentage, tax_value : taxValue };
				overallTaxesForSaving.push(taxRowForAllTax);
				totalOverallTax = totalOverallTax + taxValue;
				// AndroidInterface.showToast("Tax Value is "+taxValue.toString()+" and subTotal is "+subTotal);
			}
		}
		
	}
	}catch(Err)
	{
		console.log(Err.stack);
	}
	// totalCategoryWiseTax = totalCategoryWiseTax.toFixed(2);
	// totalOverallTax = totalOverallTax.toFixed(2);
	jQuery("#categoryWiseTax").val(totalCategoryWiseTax);
	jQuery("#overallTax").val(totalOverallTax);
	if(hasCategoryTax==false){
			categoryTaxesForSaving = [];	
	}
	allTaxesForSaving = { overall :  overallTaxesForSaving , categorywise :  categoryTaxesForSaving };
	return totalOverallTax + totalCategoryWiseTax;
}
jQuery(document).on("click","#confirmPaymentBtn", function(event){
		InitFinalPay();

		/*
		var serializedForm = formDataOfCart();
		AndroidInterface.saveFromPayButton(JSON.stringify(serializedForm));*/
	});
function convertToJSON(urlString)
{
	try{
	// bootbox.alert(urlString);
	var parsedFormData = JSON.parse('{"' + decodeURI(urlString).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	alert(JSON.stringify(parsedFormData));
	AndroidInterface.sendFormData(JSON.stringify(parsedFormData));
	}catch(Err){
		AndroidInterface.showToast(Err.message);
	}
}
function calculateGrandTotal()
{
	totalQty = 0;
	subTotal = 0;
	taxesInInvoice = 0;
	grandTotal = 0;


            jQuery('#itemsFormBody tr').each( function() {
            	subTotal += parseFloat(jQuery(this).find('input[name^="totalItemPrice"]').val());
            	totalQty += parseFloat(jQuery(this).find('input[name^="itemQuantity"]').val());
            });
    subTotal = Number(subTotal.toFixed(2));
    jQuery("#subTotal").val(subTotal);
    totalQty = Number(totalQty.toFixed(3));
    taxesInInvoice = calculateTaxes();
    taxesInInvoice = Number(taxesInInvoice.toFixed(2));

    grandTotal = subTotal + taxesInInvoice;
    grandTotal = Number(grandTotal.toFixed(2));
    if(roundInvoiceAmount==true){
    	grandTotal = Math.round(grandTotal); 	
    }
    jQuery("#grandTotal").val(grandTotal);
	jQuery("#totalQtyOfItemsDisplay").html((totalQty.toString()));
    jQuery("#subTotalOfInvoice").html((subTotal.toString())+ "&nbsp;"+currencyHtmlSymbol);
    jQuery("#taxesOfInvoice").html((taxesInInvoice.toString())+ "&nbsp;"+currencyHtmlSymbol);
    jQuery("#grandTotalOfInvoice").html((grandTotal.toString())+ "&nbsp;"+currencyHtmlSymbol);

	jQuery("#printPreviewButton").hide();
}
function generateTaxObjects()
{
	categoryWiseTaxObj = [];
	overAllTaxObject = [];
	try{
		
	if(!	(categorywiseTaxes === undefined || categorywiseTaxes === null))
	{
		if(categorywiseTaxes!=false && categorywiseTaxes.length!=0)
		{
			//AndroidInterface.showToast(categorywiseTaxes.toString());
			for(j=0; j<categorywiseTaxes.length;j++)
			{

				var rowcategoryId = categorywiseTaxes[j]['category_id'];
				var rowCategoryUniqueId = categorywiseTaxes[j]['category_unique_id'];
				// AndroidInterface.printLog("Srinath","Category tax name is "+categorywiseTaxes[j]['taxes_name']);
				var taxNameAndValue = { tax_name : categorywiseTaxes[j]['taxes_name'],tax_value : categorywiseTaxes[j]['taxes_value'], category : rowcategoryId, categoryUniqueId : rowCategoryUniqueId };
				var taxName = categorywiseTaxes[j]['taxes_name'];
				categoryWiseTaxObj[j] = taxNameAndValue;
			}
			
		}
	}
	if(!	(overallTaxes === undefined || overallTaxes === null))
	{
		if(overallTaxes!=false && overallTaxes.length!=0)
		{
			for(j=0; j<overallTaxes.length;j++)
			{
				// overAllTaxObject['tax_name'] = overallTaxes[j]['taxes_name'];
				// overAllTaxObject['tax_value'] = overallTaxes[j]['taxes_value'];
				var taxNameAndValue = { tax_name : overallTaxes[j]['taxes_name'],tax_value : overallTaxes[j]['taxes_value']};
				overAllTaxObject[j] = taxNameAndValue;
			}
		}
	}
	

	}catch(Err){
		alert(Err.stack);
	}
}

function resetCartToBlank(){
	// fetchandSaveInvoiceButtonSpan
	try{

		var count = jQuery("#itemsFormBody tr").length;
		if(count==0){
			jQuery("#holdInvoiceId").val("");
		}
	}catch(Err){ console.log(Err.stack); }
	jQuery("#recallInvoiceId").val("");
	jQuery("#deletedItemsUniqueIdsFromSavedInvoice").val("");
	jQuery("#customerIdForInvoice").val("");
	jQuery("#grandTotal").val("0");
	jQuery("#overallTax").val("0");
	jQuery("#categoryWiseTax").val("0");
	jQuery("#subTotal").val("0");
	jQuery("#orderDeliveryDate").val("");
	jQuery("#paymentType1").val("");
	jQuery("#paymentType2").val("");
	jQuery("#paymentType3").val("");
	jQuery("#paymentType4").val("");

	jQuery("#modeOfPayment1").val("");
	jQuery("#modeOfPayment2").val("");
	jQuery("#modeOfPayment3").val("");
	jQuery("#modeOfPayment4").val("");

	jQuery("#totalQtyOfItemsDisplay").html(("0".toString()));
    jQuery("#subTotalOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
    jQuery("#taxesOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
    jQuery("#grandTotalOfInvoice").html(("0".toString())+ "&nbsp;"+currencyHtmlSymbol);
	jQuery("#customerNameDisplay").html("");
    jQuery("#invoiceAndItemsForm")[0].reset();

    jQuery("#notesButtonsDiv").html("");
    jQuery("#tableNumberSelectionFromPopup").val("");
    jQuery("#tableSelectionSpan").html("");

    AndroidInterface.clearCancellationReasons();

}
function changeFetchOnHoldButton(){
	try{
		var count = jQuery("#itemsFormBody tr").length;
		if(count==0){
			jQuery("#fetchandSaveInvoiceButtonSpan").html("Fetch&nbsp;Invoice");
		}
		else{
			jQuery("#fetchandSaveInvoiceButtonSpan").html("Save&nbsp;Invoice");	
		}
	}catch(Err){ console.log(Err.stack); }
}
function updateNewQuantity(newQty){
	if(newQty==""){ return;}
	try{
		var rateAdjustment = jQuery("#adjustquantitytorate").val();
		if(rateAdjustment=="false")
		{
		var textInHighlightrow = jQuery("tr.highlightrow").html();
		var oldPrice = jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val();
		var oldQty =  jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val();
		var unitPrice = parseFloat(oldPrice) / parseFloat(oldQty);
		newQty = parseFloat(newQty).toFixed(3);
		newQty = Number(newQty);
		var oldDiscProvided = parseFloat(jQuery("tr.highlightrow").find('input[name^="discountProvidedInCurrency"]').val());
		var newPrice = parseFloat((unitPrice * parseFloat(newQty))).toFixed(2);
		var newDiscountProvided = oldDiscProvided * (parseFloat(newQty)) / (parseFloat(oldQty));
		var savedRowId = jQuery("tr.highlightrow").find('input[name^="savedRowUniqueId"]').val();
		oldQty =  jQuery("tr.highlightrow").find('input[name^="savedRowQuantity"]').val();
		var holdId  = jQuery("#holdInvoiceId").val();
//productDetails
        var modifiedQtyResponse = checkModifiedQuantityPermission(oldQty,newQty,savedRowId);
		if(modifiedQtyResponse ==false && holdId!=""){
			AndroidInterface.showAlertDialogJS("No Authorization","Sorry! You are not authorized to reduce the quantity of saved item");
			return;
		}else if(modifiedQtyResponse==null){
		    return;
		}
		jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(newPrice);
		jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val(newQty);
		jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val(unitPrice);
		jQuery("tr.highlightrow").find('input[name^="discountProvidedInCurrency"]').val(newDiscountProvided);
		jQuery("tr.highlightrow").find("span.updateQtytd").html(newQty);
		jQuery("tr.highlightrow").find("span.totalItemPricetd").html(newPrice);
		}else{
				var oldPrice = jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val();
				var oldQty =  jQuery("tr.highlightrow").find('input[name^="savedRowQuantity"]').val();
				var unitPrice = parseFloat(oldPrice) / parseFloat(oldQty);
				var newQtyForUpdate  = parseFloat(newQty) / unitPrice;
				newQtyForUpdate= newQtyForUpdate.toFixed(3);
				newQtyForUpdate = Number(newQtyForUpdate);

				var savedRowId = jQuery("tr.highlightrow").find('input[name^="savedRowUniqueId"]').val();
				if(!checkModifiedQuantityPermission(oldQty,newQtyForUpdate,savedRowId)){
					AndroidInterface.showAlertDialogJS("No Authorization","Sorry! You are not authorized to reduce the quantity of saved item");
					return;
				}

				jQuery("tr.highlightrow").find("span.updateQtytd").html(newQtyForUpdate);
				jQuery("tr.highlightrow").find("span.totalItemPricetd").html(newQty);
				jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(newQty);
				jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val(newQtyForUpdate);

		}
		calculateGrandTotal();
	}catch(Err){ console.log(Err.stack); }
}
function updateNewPrice(newPrice){
	if(newPrice==""){ return;}
	try{
        /*
		var oldPrice = jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val();
		var oldQty =  jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val();
		var unitPrice = parseFloat(oldPrice) / parseFloat(oldQty);
		var totalnewPrice = parseFloat(newPrice).toFixed(2) * parseFloat(oldQty);
		
		jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(totalnewPrice);
		jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val(newPrice);
		jQuery("tr.highlightrow").find("span.totalItemPricetd").html(newPrice);
        */
         var oldPrice = jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val();
                        var oldQty =  jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val();
                        var unitsAre = jQuery("tr.highlightrow").find('input[name^="units"]').val();
//                        JSInterface.showToast("units are "+unitsAre);
                        var appendUnits = "";
                            if(unitsAre!="" && unitsAre!=undefined){
                                appendUnits = " / "+unitsAre;
                            }
                        var unitPrice = parseFloat(oldPrice) / parseFloat(oldQty);
                        var totalnewPrice = parseFloat(newPrice).toFixed(2) * parseFloat(oldQty);

                        jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(totalnewPrice);
                        jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val(newPrice);
//                        JSInterface.showToast("New unit price for display is "+((parseFloat(totalnewPrice)  /  parseFloat(oldQty) ).toFixed(2)));
                jQuery("tr.highlightrow").find("span.unitPriceAndUnitsDisplay").html((    (parseFloat(totalnewPrice)  /  parseFloat(oldQty) ).toFixed(2) )+appendUnits);
                        jQuery("tr.highlightrow").find("span.totalItemPricetd").html(totalnewPrice);

		calculateGrandTotal();
	}catch(Err){ console.log(Err.stack); }
}
function applyDiscountToCart(discountType,applicableDiscount,isOverall)
{
		if(discountType=="percentage"){
				if(isOverall == true)
				{
					var discountAlreadyApplied = false;
					jQuery('#itemsFormBody tr').each( function() {
							var activeitemName = (jQuery(this).find('input[name^="itemName"]').val());
						if(activeitemName.includes("Discount"))
						{ 
							AndroidInterface.showAlertDialogJS("Error","Discount has already been applied on one of the item"); discountAlreadyApplied=true;
							return;
						}
					});
					if(discountAlreadyApplied == false)
					{

					jQuery('#itemsFormBody tr').each( function() {
							var activeItemUnitPrice = parseFloat(jQuery(this).find('input[name^="itemUnitPrice"]').val());
							var activeItemQty =  parseFloat(jQuery(this).find('input[name^="itemQuantity"]').val());
							var activeitemName = (jQuery(this).find('input[name^="itemName"]').val());
							
							var afterDiscountItemPrice = activeItemUnitPrice * (100 - (parseFloat(applicableDiscount)))/100;
							var afterDiscountTotalPrice = afterDiscountItemPrice * activeItemQty;
							var afterDiscountItemName = activeitemName + " Discount "+applicableDiscount+"%";
							var totalDiscountApplied = (activeItemUnitPrice - afterDiscountItemPrice) * activeItemQty;
							jQuery(this).find('input[name^="itemUnitPrice"]').val(afterDiscountItemPrice);
							jQuery(this).find('input[name^="totalItemPrice"]').val(afterDiscountTotalPrice);
							// put discount provided
							afterDiscountTotalPrice = (parseFloat(afterDiscountTotalPrice)).toFixed(2);
							jQuery(this).find("span.totalItemPricetd").html(afterDiscountTotalPrice);
							jQuery(this).find('input[name^="itemName"]').val(afterDiscountItemName);
							jQuery(this).find("span.itemNametd").html(afterDiscountItemName);
								
							jQuery(this).find('input[name^="discountProvidedInCurrency"]').val(totalDiscountApplied);
							});
					}
				}
				else{
					//presentNumpadText.includes('Discount')
					var activeItemUnitPrice = parseFloat(jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val());

					var activeItemQty =  parseFloat(jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val());
					var activeitemName = (jQuery("tr.highlightrow").find('input[name^="itemName"]').val());
					if(activeitemName.includes("Discount"))
					{// put discount provided
						AndroidInterface.showAlertDialogJS("Error","Discount has already been applied on this item");
						return;
					}
					var afterDiscountItemPrice = activeItemUnitPrice * (100 - (parseFloat(applicableDiscount)))/100;
					var afterDiscountTotalPrice = afterDiscountItemPrice * activeItemQty;
					var totalDiscountApplied = (activeItemUnitPrice - afterDiscountItemPrice) * activeItemQty;
					var afterDiscountItemName = activeitemName + " Discount "+applicableDiscount+"%";
					jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val(afterDiscountItemPrice);
					jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(afterDiscountTotalPrice);
					afterDiscountTotalPrice = (parseFloat(afterDiscountTotalPrice)).toFixed(2);
					jQuery("tr.highlightrow").find("span.totalItemPricetd").html(afterDiscountTotalPrice);
					jQuery("tr.highlightrow").find('input[name^="itemName"]').val(afterDiscountItemName);
					jQuery("tr.highlightrow").find("span.itemNametd").html(afterDiscountItemName);
					jQuery("tr.highlightrow").find('input[name^="discountProvidedInCurrency"]').val(totalDiscountApplied);
					// put discount provided
					//bootbox.alert("New item Price is "+afterDiscountItemPrice+" New item total Price is "+afterDiscountTotalPrice+" after discount name is "+afterDiscountItemName);

				}
		}else{
					var activeItemUnitPrice = parseFloat(jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val());
					var activeItemQty =  parseFloat(jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val());
					var activeItemTotalPrice =  parseFloat(jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val());
					
					var afterDiscountTotalPrice = activeItemTotalPrice - applicableDiscount;
					var afterDiscountItemPrice =  afterDiscountTotalPrice / activeItemQty;
					var activeitemName = (jQuery("tr.highlightrow").find('input[name^="itemName"]').val());
					if(activeitemName.includes("Discount"))
					{// put discount provided
						AndroidInterface.showAlertDialogJS("Error","Discount has already been applied on this item");
						return;
					}
					var afterDiscountItemName = activeitemName + " OverallDiscount "+applicableDiscount+" "+currencyHtmlSymbol;

					jQuery("tr.highlightrow").find('input[name^="discountProvidedInCurrency"]').val(applicableDiscount);					
					jQuery("tr.highlightrow").find('input[name^="itemUnitPrice"]').val(afterDiscountItemPrice);
					jQuery("tr.highlightrow").find('input[name^="totalItemPrice"]').val(afterDiscountTotalPrice);
					jQuery("tr.highlightrow").find("span.totalItemPricetd").html(afterDiscountTotalPrice);
					jQuery("tr.highlightrow").find('input[name^="itemName"]').val(afterDiscountItemName);
					jQuery("tr.highlightrow").find("span.itemNametd").html(afterDiscountItemName);
					
		}

	calculateGrandTotal();
}
function checkModifiedQuantityPermission(oldQty,newUpdatingQty,savedRowId){
	try{
			if(parseFloat(oldQty) > parseFloat(newUpdatingQty)){		
						permissionCheck = AndroidInterface.userPermissionCheck("delete_items");
						if(permissionCheck=="false")
						{
							return false;
						}
					}else{
					    var activeRows = jQuery("tr.highlightrow").length;
                    				if(activeRows != 0)
                    				{

                                    var selectedItemId = jQuery("tr.highlightrow").find('input[name^="itemId"]').val();
                                    var productDetails = AndroidInterface.productDetails(selectedItemId,newOrderType,jQuery("#invoiceStoreId").val());
                                    var productInfo = JSON.parse(productDetails);
                                    var invCategory = ""; var invCategoryUniqueId = "";

                                                                                                            if(productInfo.is_item == true && productInfo.product_exists == true)
                                                       														{

                                                       														}
                                                       														else
                                                       														{
                                                       														return null;
                                                       														}
                                    }
					}
	}catch(Err){
		console.log(Err.stack);
		AndroidInterface.printLog("Srinath",Err.stack);
	}
	return true;
}
function UPIPayment()
{
                if(customerSelectionMandatory==true && (jQuery("#customerIdForInvoice").val()=="") ){  AndroidInterface.showAlertDialogJS("Sorry! Customer Selection is Mandatory","Please select a customer to raise the invoice"); ; return; }
                try{
						permissionCheck = AndroidInterface.userPermissionCheck("transactions");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to raise invoices.");
							return;
						}
                    }catch(Err){
						console.log(Err.stack);
					}

	jQuery("#modeofpayment").val("UPI");
	loadPaypopup();
}
function cashPayment(){
                    if(customerSelectionMandatory==true && (jQuery("#customerIdForInvoice").val()=="") ){  AndroidInterface.showAlertDialogJS("Sorry! Customer Selection is Mandatory","Please select a customer to raise the invoice"); ; return; }
				    try{
						permissionCheck = AndroidInterface.userPermissionCheck("endcash");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to perform cash transactions.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}

	jQuery("#modeofpayment").val("Cash");
	loadPaypopup();	
}
function cardPayment(){
                    if(customerSelectionMandatory==true && (jQuery("#customerIdForInvoice").val()=="") ){  AndroidInterface.showAlertDialogJS("Sorry! Customer Selection is Mandatory","Please select a customer to raise the invoice"); ; return; }
					try{
						permissionCheck = AndroidInterface.userPermissionCheck("creditcards");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to accept card payments.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}
	jQuery("#modeofpayment").val("Card");
	loadPaypopup();	
}
function loadPaypopup(){

	if(jQuery("#recallInvoiceId").val()!=""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please clear the recalled invoice");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
	}
	if(jQuery("#invoiceStoreId").val()==""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please make sure that store id is valid");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
	}
				var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows == 0)
				{
					try{
						AndroidInterface.showAlertDialogJS("Error","Please select atleast 1 item to raise invoice.");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
				}

					try{
						permissionCheck = AndroidInterface.userPermissionCheck("transactions");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to raise invoices.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}
					if(promptReasonForCancellation==true){    saveOrFetchInPromptReason = "finalpay"; promptForTheReasonForCancellation(); if(reasonProvided==true){ reasonProvided = false;}else { return; } }

					var holdinvoice = jQuery("#holdInvoiceId").val();
					try{
						holdinvoice = AndroidInterface.holdIdForInvoice(jQuery("#holdInvoiceId").val());
					}catch(Err){
						console.log(Err.stack);
					}
					jQuery("#orderRefNo").val(holdinvoice)
	jQuery(".overlay2").show();
	jQuery(".paypopup").show();
	jQuery("#givenAmount").html(jQuery("#grandTotal").val());
	jQuery("#givenAmountVal").val(jQuery("#grandTotal").val());
    jQuery("#initiateCardPayment").val(jQuery("#grandTotal").val());
	jQuery("#totalAmountDisplay").html(jQuery("#grandTotal").val());
	jQuery("#confirmPaymentBtn").prop('disabled', false);
	payPopupFlag = true;
	jQuery("#changeTendered").val("");
	jQuery("#paymentType1").val("");
	jQuery("#paymentType2").val("");
	jQuery("#paymentType3").val("");
	jQuery("#paymentType4").val("");
	jQuery("#orderRefNo").val("");
	for(j=1;j<=4;j++){
		jQuery("#modeOfPayment"+ j.toString() ).val("");
	}
	jQuery('#easy-numpad-output-2').html(jQuery("#grandTotal").val());
	try{
		AndroidInterface.establishPrinterConnectionsJS();
	}catch(Err){
		console.log(Err.stack);
	}

}
function cancelPaymentOption()
{
	jQuery(".overlay2").hide();
	payPopupFlag = false;
	try{
	easy_numpad_clear2();
	}catch(Err)
	{
	    console.log(Err.message);
	}
	jQuery(".paypopup").hide();
	jQuery("tbody#modesOfPaymentTableBody").html("");
	jQuery("#changeTendered").val("");
	jQuery("#modeofpayment").val("Cash");
	confirmationAlertFlag = false;
}
function finalPay()
{
	var amountBeingPaid = parseFloat(jQuery('#easy-numpad-output-2').text());	
	if(isNaN(amountBeingPaid)){
		try{
		AndroidInterface.showAlertDialogJS("Error","Invalid Amount Entered");
			}catch(Err){
				console.log(Err.stack);
			}
		return;
	}

	if(jQuery("#invoiceStoreId").val()==""){
					try{
						AndroidInterface.showAlertDialogJS("Error","Please make sure that store id is valid");
					}catch(Err){
						console.log(Err.stack);
					}
					return;
	}

	var grandTotalAmount = parseFloat(jQuery("#grandTotal").val()); 
	var remainingAmount = parseFloat(jQuery("#givenAmountVal").val());
	var existingPaymentsCount = jQuery("tbody#modesOfPaymentTableBody tr").length;
	var newPaymentRow = existingPaymentsCount + 1;
	if(parseInt(newPaymentRow)==5){
		try{
			AndroidInterface.showAlertDialogJS("Sorry","You cannot have more than 4 payment modes for an invoice");
			return;
		}catch(Err)
		{
			console.log(Err.stack);
		}
	}
	var presentmodeOfPayment = jQuery("#modeofpayment").val();
				if(presentmodeOfPayment=="Cash"){
					try{
						permissionCheck = AndroidInterface.userPermissionCheck("endcash");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to perform Cash transactions.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}
				}
				else if(presentmodeOfPayment=="Card"){
					try{
						permissionCheck = AndroidInterface.userPermissionCheck("creditcards");
						if(permissionCheck=="false")
						{
							AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to perform Card transactions.");
							return;
						}
					}catch(Err){
						console.log(Err.stack);
					}
				}

	var newTableRowString = "<tr><td>"+ (newPaymentRow.toString()) + "</td><td>"+ presentmodeOfPayment + "</td><td>"+ (amountBeingPaid.toString()) + "</td></tr>";
			jQuery("tbody#modesOfPaymentTableBody").append(newTableRowString);
	var paymentReferenceNumber = jQuery("#paymentRefNo").val();
	if(remainingAmount == 0){ return;}		
	if(amountBeingPaid >= remainingAmount)
	{
		var changeAmount = amountBeingPaid - remainingAmount;
		jQuery("#paymentType"+newPaymentRow).val(remainingAmount);
		jQuery("#modeOfPayment"+newPaymentRow).val(presentmodeOfPayment);
		jQuery("#paymentRefNo"+newPaymentRow).val(paymentReferenceNumber);
		jQuery("#changeTendered").val(changeAmount);
		try{

			AndroidInterface.showToast("Remaining Change is "+ changeAmount.toString());

			
		}catch(Err)
		{
			console.log(Err.stack);
		}
		payPopupFlag = false;
		jQuery("#confirmPaymentBtn").prop('disabled', true);
		completeTheInvoice();
	}else{
		var balanceAmountToBePaid = remainingAmount - amountBeingPaid;
		balanceAmountToBePaid = balanceAmountToBePaid.toFixed(2);
		jQuery("#givenAmountVal").val(balanceAmountToBePaid);
		jQuery("#givenAmount").html(balanceAmountToBePaid);
		jQuery("#easy-numpad-output-2").html(balanceAmountToBePaid);
		jQuery("#paymentType"+newPaymentRow).val(amountBeingPaid);
		jQuery("#modeOfPayment"+newPaymentRow).val(presentmodeOfPayment);
		jQuery("#paymentRefNo"+newPaymentRow).val(paymentReferenceNumber);

        jQuery("#initiateCardPayment").val((jQuery("#givenAmount").html()));
	}
	jQuery("#paymentRefNo").val("");
	var serializedData = jQuery("#invoiceAndItemsForm").serialize();
}
function completeTheInvoice()
{
	try{
		var serializedForm = formDataOfCart();
		AndroidInterface.saveFromPayButton(JSON.stringify(serializedForm));

	jQuery("#orderRefNo").val("");
	jQuery('#itemsFormBody tr').each( function() {
		jQuery(this).remove();
	});
	calculateGrandTotal();
    jQuery("#invoiceAndItemsForm")[0].reset();
	jQuery("tbody#modesOfPaymentTableBody").html("");
	cancelPaymentOption();
	resetCartToBlank();
	changeFetchOnHoldButton();

    AndroidInterface.printLog("Fetch","Complete pay last text");

	}catch(Err){
		console.log(Err.stack);
		AndroidInterface.printLog("Fetch",Err.message);
		AndroidInterface.printLog("Fetch",Err.stack);
	}
}
function showPrintPopup(){

								confirmationAlertFlag = true;
    							jQuery("#confirmationHeading").html("Would you like to Print The Invoice ?");
								jQuery("#confirmationType").val("invoiceprinting");


								                                if(autoprintInvoice==true)
                                								{
                                								    confirmAlert();
                                								}else{

								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
								jQuery("#cancelAlertBtn").show();
                                								}

}
function unabletoPrintError(){
//	alert("Error Called");
								confirmationAlertFlag = true;
								jQuery("#cancelAlertBtn").show();
    							jQuery("#confirmationHeading").html("Would you like to Print The Invoice ?");
								jQuery("#confirmationType").val("invoiceprinting");
								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
}
function printLastInvoice(){
								confirmationAlertFlag = true;
    							jQuery("#confirmationHeading").html("Would you like to Reprint The Invoice ?");
								jQuery("#confirmationType").val("invoicereprint");
								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
								jQuery("#cancelAlertBtn").show();
}


// recallFetchandSaveItemSearch.js file contents

jQuery(document).ready(function(){

				jQuery(document).on("click","#recallInvoiceBtn", function(event){
						
						jQuery("#recalledInvoicesBody").html(""); jQuery("#holdInvoicesBody").html("");jQuery("#itemSearchBody").html("");
						recallInvoicePopup();
						
					});
				jQuery(document).on("click","#printPreviewButton", function(event){
					try{
						jQuery("#printPreviewButton").hide();
							permissionCheck = AndroidInterface.userPermissionCheck("previewreceiptprint");
							permissionCheckForDuplicateCustomerCopy = AndroidInterface.userPermissionCheckForDuplicateCustomerCopy(jQuery("#holdInvoiceId").val());
							if(permissionCheck=="false")
							{
								AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to print proforma invoice.");
								return;
							}else if(permissionCheckForDuplicateCustomerCopy=="false")
							{
								AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to print duplicate copy of customer invoice.");
								return;
							}
							else{

								confirmationAlertFlag = true;
    							jQuery("#confirmationHeading").html("Would you like to Print The Customer Copy Invoice ?");
								jQuery("#confirmationType").val("previewreceipt");
								jQuery("#confirmationPopup").show();
								jQuery(".confirmationOverlay").show();
								jQuery("#cancelAlertBtn").show();
								if(portraitView==true){
								    closeItemsSelection();
								}
								return;
							}
					}catch(Err){
						console.log(Err.stack);
					}	
				});


				jQuery(document).on("keydown", "#itemSearchField", function(e) {
						
						if(e.keyCode==27)
						{
							closeRecallPopup();
							e.preventDefault();
							jQuery("#barcodeNumber").focus();
						}
						else if(e.keyCode==0 || e.keyCode==13)
						{

									var activeRows = jQuery("#itemSearchBody tr.tableitemrow.active").length;

									if(activeRows != 0)
									{
										var productId = jQuery("#itemSearchBody tr.tableitemrow.active").attr("data-itemid");
					
										retrieveProductDetails(productId,"1");
										closeRecallPopup();
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
									jQuery("#itemSearchBody tr.tableitemrow").each( function() {

										if(foundItem==false){
											i++;
										}
											
											if(jQuery(this).hasClass("active")){
											 	jQuery(this).removeClass("active");
											 	var nextRowItem = ((i).toString());
											 	console.log(i+" row is active and next row item is "+nextRowItem);
												jQuery("#itemSearchBody tr:nth-child("+ nextRowItem +")" ).addClass("active");
											 	foundItem = true;
											 	return;
											}
									});
									
									var activeRows = jQuery("#itemSearchBody tr.tableitemrow.active").length;

									if(activeRows == 0)
									{
										jQuery("#itemSearchBody tr.tableitemrow:last-child").addClass("active");
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


									jQuery("#itemSearchBody tr.tableitemrow").each( function() {

										if(foundItem==false){
											i++;
										}
											
											if(jQuery(this).hasClass("active")){
											 	jQuery(this).removeClass("active");
											 	var nextRowItem = ((i+2).toString());
											 	console.log(i+" row is active and next row item is "+nextRowItem);
												jQuery("#itemSearchBody tr:nth-child("+ nextRowItem +")" ).addClass("active");
											 	foundItem = true;
											 	return;
											}
									});

									var activeRows = jQuery("#itemSearchBody tr.tableitemrow.active").length;

									if(activeRows == 0)
									{
										jQuery("#itemSearchBody tr.tableitemrow:first").addClass("active");
									}
							e.preventDefault();
							}catch(Err){
								console.log(Err.stack);
							}
						}
					});


				jQuery(document).on("keydown", "#orderRefNum", function(e) {
				
					if(event.keyCode == 13 || event.keyCode==0) { 
						if(saveNewInvoiceFlag==true){
							if(jQuery("#orderRefNum").val()=="")
							{
								AndroidInterface.showAlertDialogJS("Sorry","You have to enter a order Reference ID");
								return;
							}
						saveNewInvoice();
						keyboardFocus();
						}
						e.preventDefault();
					}
				});


				jQuery(document).on("click","#confirmOrderRefId", function(event){
							if(jQuery("#orderRefNum").val()=="")
							{
								AndroidInterface.showAlertDialogJS("Sorry","You have to enter a order Reference ID");
								return;
							}
						saveNewInvoice();
					});
				jQuery(document).on("click","#voidInvoiceBtn", function(event){
						voidInvoiceMethod();
						
					});
				jQuery(document).on("click","#closeRecall", function(event){
						closeRecallPopup();
					});
				jQuery("#orderTableSelection").on("click","button",function(event){
					var tableName = jQuery(this).attr("data-tableName");
					var tableUniqueId = jQuery(this).attr("data-tableUniqueId");
					jQuery("#orderRefNum").val(tableName);
					jQuery("#tableSelected").val(tableUniqueId);
					// jQuery("#orderRefNum").focus();
					
				});


				jQuery(document).on("click","#itemSearchBody tr", function(event){
					var productId = jQuery(this).attr("data-itemid");
					// alert(productId);
					retrieveProductDetails(productId,"1");
					closeRecallPopup();
				});
				jQuery(document).on("click",".feedbackbtn", function(event){
					var invoiceId = jQuery(this).parent().parent().attr("data-holdinvoiceid");
					try{
							AndroidInterface.feedbackForInvoice(invoiceId);
					}catch(Err){ console.log(Err.stack);}
				});
				jQuery(document).on("click","#holdInvoicesBody tr", function(event){

				    AndroidInterface.showLoadingAsyncDialog();
					var invoiceId = jQuery(this).attr("data-holdinvoiceid");
					var invoiceHoldIdForFetch = jQuery(this).find("td:first-child").text();
					var invoiceAmt = jQuery(this).attr("data-invoiceidamount");
					invoiceAmtOfFetchInvoice = invoiceAmt;
					var invoiceOrderType = jQuery(this).attr("data-invoiceordertype");
					changeOrderType(invoiceOrderType);
					jQuery("#holdInvoiceId").val(invoiceId);
					jQuery("#recallInvoiceId").val("");
					jQuery("#grandTotalOfInvoice").html(invoiceAmt+" "+currencyHtmlSymbol);
                    jQuery("#tableSelectionSpan").html(invoiceHoldIdForFetch);
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

							    AndroidInterface.invoiceFetchTimeSaveByInvoiceId(jQuery("#holdInvoiceId").val());

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
					            AndroidInterface.stopLoadingDialog();
								return;
					}catch(Err){
						console.log(Err.stack);
					}
				});
				jQuery(document).on("click","#recalledInvoicesBody tr", function(event){

				        if(AndroidInterface.checkConnectivityDetails()=="false")
                        {
                            return;
                        }
					var invoiceId = jQuery(this).attr("data-invoiceidrecall");
					var invoiceAmt = jQuery(this).attr("data-invoiceidamount");
					var invoiceOrderType = jQuery(this).attr("data-invoiceordertype");
					changeOrderType(invoiceOrderType);
					jQuery("#recallInvoiceId").val(invoiceId);
					jQuery("#grandTotalOfInvoice").html(invoiceAmt+" "+currencyHtmlSymbol);
					showLoadingPopupModal();
					try{
								var invoiceItemsList = JSON.parse(AndroidInterface.invoiceItems(invoiceId,"recall"));
								jQuery("#recallButtonContent").hide();
								jQuery("#clearrecallButtonContent").show();
								jQuery("#holdInvoiceId").val("");
					
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
													var hiddenParameters = "<input type=\"hidden\" name=\"categoryUniqueId[]\" value=\""+invCategoryUniqueId+"\" /><input type=\"hidden\" name=\"categoryid[]\" value=\""+invCategory+"\" /><input type=\"hidden\" name=\"savedRowQuantity[]\" value=\""+ newProductJSON.item_quantity +"\"><input type=\"hidden\" name=\"savedRowUniqueId[]\" value=\""+ newProductJSON.unique_id +"\"><input type=\"hidden\" name=\"oldNotes[]\" /><input type=\"hidden\" name=\"savedRowNotes[]\" value=\"\"><input type=\"hidden\" name=\"discountProvidedInCurrency[]\" value=\"0\" />";
													tableStr += "<tr><td>"+(p+1)+"</td><td>"+hiddenParameters+"<input type=\"hidden\" name=\"itemId[]\" value=\""+newProductJSON.item_id+"\" />"+newProductJSON.item_id+"</td><td><input type=\"hidden\" name=\"itemName[]\" value=\""+newProductJSON.item_name+"\" /><span class=\"itemNametd\">"+newProductJSON.item_name+"</span></td><td><input type=\"hidden\" name=\"itemUnitPrice[]\" value=\"" + newProductJSON.avg_cost + "\" /><input type=\"hidden\" name=\"itemQuantity[]\" value=\""+newProductJSON.item_quantity+"\" /><span class=\"updateQtytd\">"+((newProductJSON.item_quantity).toString())+"</span></td><td><input type=\"hidden\" name=\"units[]\" value=\""+invitemUnits+"\" />"+invitemUnits+"</td><td style=\"text-align:right;\"><input type=\"hidden\" name=\"totalItemPrice[]\" value=\""+ newProductJSON.price_you_charge +"\" /><span class=\"totalItemPricetd\">"+newProductJSON.price_you_charge+"</span></td><td class=\"deleteItemFromCart\" style=\"padding-right:10px;\"><i class=\"fa fa-trash\"></i></td></tr>";
													console.log("category unique id is "+invCategoryUniqueId+" and category id is "+invCategory);

												}
												taxesValue = parseFloat(invoiceAmt) - parseFloat(invoiceItemsValue);
												jQuery("#itemsFormBody").html(tableStr);
												jQuery('#itemsFormBody tr:last-child').addClass("highlightrow");
											}

											try{
                                            							    var customerDetailsForInvoice = JSON.parse(AndroidInterface.customerDetailsForInvoice(invoiceId));

                                            										jQuery("#customerIdForInvoice").val(customerDetailsForInvoice.customerid);
                                            										jQuery("#customerNameDisplay").html(customerDetailsForInvoice.customername);
                                            								}
                                            								catch(Err){ console.log(Err.stack);}
										}
							    taxesValue = taxesValue.toFixed(2);
							    invoiceItemsValue = invoiceItemsValue.toFixed(2);
							    // invoiceAmt = Number(invoiceAmt.toFixed(2));
								jQuery("#totalQtyOfItemsDisplay").html((totalQtyOfItems.toString()));
							    jQuery("#subTotalOfInvoice").html((invoiceItemsValue.toString())+ "&nbsp;"+currencyHtmlSymbol);
							    jQuery("#taxesOfInvoice").html((taxesValue.toString())+ "&nbsp;"+currencyHtmlSymbol);
							    jQuery("#grandTotalOfInvoice").html((invoiceAmt.toString())+ "&nbsp;"+currencyHtmlSymbol);
                                hideLoadingPopupModal();
							    var serializedData = jQuery("#invoiceAndItemsForm").serialize();
							    // console.log(serializedData);
								return;
						}
						catch(Err){
							console.log(Err.stack);
						}
						hideLoadingPopupModal();
				});
});

function clearRecalledInvoice(){
	jQuery("#holdInvoiceId").val("");
	jQuery("#itemsFormBody").html("");
	resetCartToBlank();
	changeFetchOnHoldButton();
    jQuery("#invoiceAndItemsForm")[0].reset();
    calculateGrandTotal();

}
function recallInvoicePopup()
{

                                if(AndroidInterface.checkConnectivityDetails()=="false")
                                {
                                    return;
                                }

	var recalledInvoice = jQuery("#recallInvoiceId").val();
	if(recalledInvoice!=""){
		jQuery("#recallButtonContent").css({display : 'block'});
		jQuery("#clearrecallButtonContent").css({display : 'none'});
		clearRecalledInvoice();
		return;
	}
						
				var activeRows = jQuery("tr.highlightrow").length;
				if(activeRows != 0)
				{
						try{
								AndroidInterface.showAlertDialogJS("Error","The items list should be empty to recall an invoice");
								return;
						}
						catch(Err){
							console.log(Err.stack);
						}
				}

	var storeId = jQuery("#invoiceStoreId").val();
	                    try{
	                                AndroidInterface.showLoadingAsyncDialog();
                        		var recallInvoices = JSON.parse(AndroidInterface.invoicesList(storeId,"complete"));
								if(!	(recallInvoices === undefined || recallInvoices === null))
								{
											if(recallInvoices!=false && recallInvoices.length!=0)
											{
												var tableStr = "";
												for(p=0;p< recallInvoices.length; p++){
													var rowinvoiceId = recallInvoices[p]['invoice_id'];
													var rowinvoicetotalAmt = recallInvoices[p]['total_amt'];
													var billedTime = recallInvoices[p]['bill_ts'];

													var displayDate = new Date(Date.parse(billedTime.replace('-','/','g')));
	 												displayDate = GetFormattedDate(displayDate);
													
													var orderType = recallInvoices[p]['order_type']
													var paymentType = recallInvoices[p]['payment_type'];
													var displayOrderType = AndroidInterface.replacedNameForOrderType(orderType);
													tableStr += "<tr data-invoiceordertype=\"" + orderType +"\" data-invoiceidamount=\""+rowinvoicetotalAmt+"\" data-invoiceidrecall=\""+rowinvoiceId+"\"><td>"+rowinvoiceId+"</td><td>"+displayDate+"</td><td>"+displayOrderType+"</td><td>"+paymentType+"</td><td>"+recallInvoices[p]['employee']+"</td><td>"+rowinvoicetotalAmt+"</td></tr>";
													// AndroidInterface.printLog("Recall","Row invoice id is "+rowinvoiceId+" amount is "+rowinvoicetotalAmt+" and billed time is "+billedTime+" And employee id is " + recallInvoices[p]['employee']);
															
												}
												jQuery("#recalledInvoicesBody").html(tableStr);
												jQuery("#recallHeadingText").html("Previous Invoices");
												displayRecallPopup();		
											}
								}

						    AndroidInterface.hideLoadingAsyncDialog();
						}
						catch(Err){
							AndroidInterface.showToast(Err.stack);
						}

}
function displayRecallPopup()
{
		jQuery("#popup3").show();
		jQuery(".overlay2").show();
		recallInvoiceFlag = true;
}
function closeRecallPopup(){

		jQuery("#popup3").hide();
		jQuery(".overlay2").hide();
		recallInvoiceFlag = false;
		itemSearchFlag = false;
		jQuery("#recalledInvoicesBody").html(""); jQuery("#holdInvoicesBody").html("");jQuery("#itemSearchBody").html("");

		jQuery("#itemSearchSpan").hide();
			jQuery("#itemSearchTextfield").hide();
			jQuery("#recallHeadingText").show();

}
function voidInvoiceMethod(){
                                if(AndroidInterface.checkConnectivityDetails()=="false")
                                {
                                    return;
                                }

				var activeRows = jQuery("#itemsFormBody tr").length;
				if(activeRows == 0)
				{
					return;
				}
					if(jQuery("#recallInvoiceId").val()!="" || jQuery("#holdInvoiceId").val()!="")
					{
						try{
							permissionCheck = AndroidInterface.userPermissionCheck("void_invoices");
							if(permissionCheck=="false")
							{
								AndroidInterface.showAlertDialogJS("No Authentication","You are not authorized to void invoices.");
								return;
							}
						}catch(Err){
							console.log(Err.stack);
						}
					}
					if(promptReasonForCancellation==true){    saveOrFetchInPromptReason = "void"; promptForTheReasonForCancellation(); if(reasonProvided==true){ reasonProvided = false;}else { return; } }

	confirmationAlertFlag = true;
	jQuery("#confirmationHeading").html("Are you sure that you want to void the invoice ?");
	jQuery("#confirmationType").val("voidTheInvoice");
	jQuery("#confirmationPopup").show();
	jQuery(".confirmationOverlay").show();
	jQuery("#cancelAlertBtn").show();
}
function confirmAlert(){
	if(	(jQuery("#confirmationType").val()) == "voidTheInvoice")
	{

								jQuery("#recallButtonContent").show();
								jQuery("#clearrecallButtonContent").hide();
		try{
			if(jQuery("#recallInvoiceId").val()=="" && jQuery("#holdInvoiceId").val()=="")
			{

				confirmationAlertFlag = false;
		jQuery("#holdInvoiceId").val("");
		jQuery("#itemsFormBody").html("");		
				resetCartToBlank();
				jQuery("#invoiceAndItemsForm")[0].reset();
				cancelAlert();
				changeFetchOnHoldButton();
				
			}
			else{
				voidedInvoiceId = AndroidInterface.voidInvoice(jQuery("#recallInvoiceId").val(),jQuery("#holdInvoiceId").val());
				reloadVoidedInvoiceConfirmation();
				jQuery("#recallInvoiceId").val("");
				jQuery("#holdInvoiceId").val("");
				calculateGrandTotal();
				setItemOldQuantitiesToZero();
			}
		}catch(Err)
		{
			console.log(Err.stack);
		}
	}
	else if((jQuery("#confirmationType").val()) == "reloadTheVoidedInvoice"){
			voidedInvoiceId = "";	

			
			jQuery(".confirmationOverlay").hide();
			jQuery("#confirmationPopup").hide();
			changeFetchOnHoldButton();

	}
	else if(jQuery("#confirmationType").val() == "printitemsList"){
			AndroidInterface.itemsListPrint();

			jQuery(".confirmationOverlay").hide();
			jQuery("#confirmationPopup").hide();
			changeFetchOnHoldButton();
			confirmationAlertFlag = false;
			
			AndroidInterface.printerConnectivityCheck();
	}else if(jQuery("#confirmationType").val() == "invoiceprinting"){
			
			AndroidInterface.invoicePrint();
			keyboardFocus();
			var hasNewItems = AndroidInterface.hasNewItemsCheck();
			if(itemsListPrint==true && hasNewItems==true){

				setTimeout(function(){
						confirmationAlertFlag = true;
						jQuery("#confirmationHeading").html("Would you like to Print The items List ?");
						jQuery("#confirmationType").val("printitemsList");
						jQuery("#confirmationPopup").show();
						jQuery(".confirmationOverlay").show();
						                                                                if(autoprintKot==true)
                                                        								{
                                                        								    confirmAlert();
                                                        								}
						// jQuery("#cancelAlertBtn").hide();
					},1000);
					keyboardFocus();
			}else{
			jQuery(".confirmationOverlay").hide();
			jQuery("#confirmationPopup").hide();
			changeFetchOnHoldButton();
			confirmationAlertFlag = false;
			}
	AndroidInterface.printerConnectivityCheck();
	}
	else if(jQuery("#confirmationType").val() == "previewreceipt"){
			var serializedForm = formDataOfCart();
			AndroidInterface.printPreviewReceipt(jQuery("#holdInvoiceId").val() , JSON.stringify(serializedForm));
			jQuery(".confirmationOverlay").hide();
			jQuery("#confirmationPopup").hide();
			changeFetchOnHoldButton();
			confirmationAlertFlag = false;
			
	AndroidInterface.printerConnectivityCheck();
	        window.setTimeout(function(){
	        try{
	                                        jQuery("#holdInvoiceId").val("");
            								jQuery("#itemsFormBody").html("");
            								resetCartToBlank();
            								changeFetchOnHoldButton();

                							jQuery("#invoiceAndItemsForm")[0].reset();
	        // AndroidInterface.storeSelectionClick();

	        }catch(Err)	{ bootbox.alert(Err.message); }

	        },1500);

	}
	else if(jQuery("#confirmationType").val() == "invoicereprint"){
			AndroidInterface.printLastInvoice(jQuery("#recallInvoiceId").val());
			jQuery(".confirmationOverlay").hide();
			jQuery("#confirmationPopup").hide();
			changeFetchOnHoldButton();
			confirmationAlertFlag = false;
			
	AndroidInterface.printerConnectivityCheck();
	}
	else if(jQuery("#confirmationType").val()== "notesforitem"){

		var itemNotesFromTextArea = jQuery("#notesTextarea").val();
		var forItem = jQuery("tr.highlightrow").find('input[name^="savedRowNotes"]').val(itemNotesFromTextArea);
		jQuery(".confirmationOverlay").hide();
		jQuery("#confirmationPopup").hide();

        jQuery("#notesButtonsDiv").html("");

		confirmationAlertFlag = false;
		changeFetchOnHoldButton();
			
	}
	else{
		AndroidInterface.showToast(jQuery("#confirmationType").val());
	}	
	jQuery("#notesTextareaDiv").hide();
	AndroidInterface.printerConnectivityCheck();
}
function reloadVoidedInvoiceConfirmation(){

	jQuery("#confirmationHeading").html("Would You Like To Reload The voided Invoice ?");
	jQuery("#confirmationType").val("reloadTheVoidedInvoice");
}
function cancelAlert(){
	try{
		
		if((jQuery("#confirmationType").val()) == "reloadTheVoidedInvoice"){
		clearRecalledInvoice();
		}
		else if(jQuery("#confirmationType").val() == "notesforitem"){
                    jQuery("#notesButtonsDiv").html("");
                }

                else if((jQuery("#confirmationType").val()) == "invoiceprinting"){
                        		var hasNewItems = AndroidInterface.hasNewItemsCheck();
                                			if(itemsListPrint==true && hasNewItems==true){

                                				setTimeout(function(){
                                						confirmationAlertFlag = true;
                                						jQuery("#confirmationHeading").html("Would you like to Print The items List ?");
                                						jQuery("#confirmationType").val("printitemsList");
                                						jQuery("#confirmationPopup").show();
                                						jQuery(".confirmationOverlay").show();
                                						                                                                if(autoprintKot==true)
                                                                                        								{
                                                                                        								    confirmAlert();
                                                                                        								}
                                						// jQuery("#cancelAlertBtn").hide();
                                					},1000);
                        }
                    }

	}catch(Err)
	{
		console.log(Err.stack);
	}
	closeConfirmationPopup();
	jQuery(".confirmationOverlay").hide();
	jQuery("#confirmationPopup").hide();
}
function closeConfirmationPopup(){

	if((jQuery("#confirmationType").val()) == "reloadTheVoidedInvoice"){
		// clearRecalledInvoice();
	}
	try{
		// AndroidInterface.showToast("Confirmation Popup called");
	}catch(Err)
	{
		console.log(Err.stack);
	}
	jQuery("#notesTextareaDiv").hide();
	jQuery(".confirmationOverlay").hide();
	jQuery(".overlay2").hide();
	
	confirmationAlertFlag = false;
    if(promptReasonForCancellation==true) { resetTheReasonsForCancellation(); }
}
String.prototype.rtrim = function (s) {
    if (s == undefined)
        s = '\\s';
    return this.replace(new RegExp("[" + s + "]*$"), '');
};
String.prototype.ltrim = function (s) {
    if (s == undefined)
        s = '\\s';
    return this.replace(new RegExp("^[" + s + "]*"), '');
};
function retrieveTableNumbers()
{
	try{
		var tablesList = JSON.parse(AndroidInterface.tableNumbersSave(jQuery("#invoiceStoreId").val()));
		jQuery("#saveInvoiceTableSelectionPopup").show();
		jQuery(".overlay2").show();
		var showTablesStr = "<div class=\"row\">";

										if(!	(tablesList === undefined || tablesList === null))
										{

											if(tablesList!=false && tablesList.length!=0)
											{
												for(k=0;k<tablesList.length;k++){


													var tableRow = tablesList[k];
													if(tablesList[k].occupancy_status=='Occupied'){
														tablebuttonClass = "btn-warning";
													}
													else{
													tablebuttonClass = "btn-success";	
													}

		var w = (window.innerWidth);
		var numberInRow = 4;

		if(portraitView==true)
        {
            showTablesStr += "<div class=\"col-4\" style=\"margin:10px 0px; text-align:center;\"><button data-tableName=\""+tablesList[k].table_name+"\"  data-tableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+"\">"+tableRow.table_name+"</button></div>";

        }
        else{
                    if(w <= 980){
            													showTablesStr += "<div class=\"col-3\" style=\"margin:10px 0px; text-align:center;\"><button data-tableName=\""+tablesList[k].table_name+"\"  data-tableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+"\">"+tableRow.table_name+"</button></div>";
            		}else{
            													showTablesStr += "<div class=\"col-2\" style=\"margin:10px 0px; text-align:center;\"><button data-tableName=\""+tablesList[k].table_name+"\"  data-tableUniqueId=\""+tablesList[k].unique_id+"\" class=\"btn "+tablebuttonClass+"\">"+tableRow.table_name+"</button></div>";
            		}
        }


													
												}
											}
										}
		showTablesStr += "</div>";											
		jQuery("#orderTableSelection").html(showTablesStr);
		saveNewInvoiceFlag = true;

        if(jQuery("#tableNumberSelectionFromPopup").val()!=""){
            insertSelectedTable();
        }

	}catch(Err){
		console.log(Err.stack);
	}
}
function saveNewInvoice()
{
	var orderRefNum = jQuery("#orderRefNum").val();
        AndroidInterface.showLoadingAsyncDialog();
	try{

		var checkExistingRefNum = AndroidInterface.validateExistingRefNum(orderRefNum,jQuery("#invoiceStoreId").val());
		if(checkExistingRefNum=="true"){
			var serializedForm = formDataOfCart();
			AndroidInterface.printLog("Testing","Save new invoice called");
			AndroidInterface.saveAndHoldInvoice(JSON.stringify(serializedForm));
			jQuery("#orderRefNum").val("");
			jQuery("#tableSelected").val("");
			jQuery("#tableNumberSelectionFromPopup").val("");
			jQuery("#tableSelectionSpan").html("");
			closeSaveNewInvoicePopup();


			var hasNewItems = AndroidInterface.hasNewItemsCheck();
	if(itemsListPrint==true && hasNewItems==true){
	confirmationAlertFlag = true;
	jQuery("#confirmationHeading").html("Would you like to Print The items List ?");
	jQuery("#confirmationType").val("printitemsList");
	jQuery("#confirmationPopup").show();
	jQuery(".confirmationOverlay").show();

	                                                                if(autoprintKot==true)
                                    								{
                                    								    confirmAlert();
                                    								}
	 // jQuery("#cancelAlertBtn").hide();
	}

	jQuery("#orderRefNo").val("");
	jQuery('#itemsFormBody tr').each( function() {
		jQuery(this).remove();
	});
	calculateGrandTotal();
    jQuery("#invoiceAndItemsForm")[0].reset();
	jQuery("tbody#modesOfPaymentTableBody").html("");
	cancelPaymentOption();
	resetCartToBlank();
	changeFetchOnHoldButton();
	saveNewInvoiceFlag = false;
		}
		else{
			AndroidInterface.showToast("This Order Reference ID is already in use");
		}
	}
	catch(Err){
		console.log(Err.stack);
	}
	AndroidInterface.stopLoadingDialog();
}
function displayPendingFromAsync(){
    hideLoadingPopupModal();
    displayPendingInvoices();
}
function displayPendingInvoices(){

	var storeId = jQuery("#invoiceStoreId").val();
						try{
								var recallInvoices = JSON.parse(AndroidInterface.invoicesList(storeId,"hold"));
								if(!	(recallInvoices === undefined || recallInvoices === null))
								{
											if(recallInvoices!=false && recallInvoices.length!=0)
											{
												var tableStr = "";
												for(p=0;p< recallInvoices.length; p++){
													var rowinvoiceId = recallInvoices[p]['invoice_id'];
													var rowinvoicetotalAmt = recallInvoices[p]['total_amt'];
													var billedTime = recallInvoices[p]['order_delivery_date'];
													
	 												var displayDate = new Date(Date.parse(billedTime.replace('-','/','g')));
	 												displayDate = GetFormattedDate(displayDate);
													var orderType = recallInvoices[p]['order_type']
													var paymentType = recallInvoices[p]['payment_type'];
													var displayOrderType = AndroidInterface.replacedNameForOrderType(orderType);
													var feedbackButtonStr = "";
													if(feedbackWebPage!=""){ feedbackButtonStr = "<td><button class=\"btn btn-primary feedbackbtn\">Feedback</button></tr>";}
													tableStr += "<tr  data-invoiceordertype=\"" + orderType +"\" data-invoiceidamount=\""+rowinvoicetotalAmt+"\" data-holdinvoiceid=\""+rowinvoiceId+"\"><td>"+recallInvoices[p]['holdid']+"</td><td>"+rowinvoiceId+"</td><td>"+displayDate+"</td><td>"+displayOrderType+"</td><td>"+recallInvoices[p]['employee']+"</td><td>"+rowinvoicetotalAmt+"</td>"+feedbackButtonStr+"</tr>";
													// AndroidInterface.printLog("Recall","Row invoice id is "+rowinvoiceId+" amount is "+rowinvoicetotalAmt+" and billed time is "+billedTime+" And employee id is " + recallInvoices[p]['employee']);
															
												}
												jQuery("#holdInvoicesBody").html(tableStr);
												jQuery("#recallHeadingText").html("Pending Invoices");
												displayRecallPopup();		
											}else{
												AndroidInterface.showToast("No Pending Invoices");
											}
								}else{
												AndroidInterface.showToast("No Pending Invoices");
								}
						}
						catch(Err){
							AndroidInterface.showToast(Err.stack);
						}
}
function searchItemPopup(){
	jQuery("#recalledInvoicesBody").html(""); jQuery("#holdInvoicesBody").html("");jQuery("#itemSearchBody").html("");
	jQuery("#itemSearchSpan").show();
	jQuery("#itemSearchTextfield").show();
	jQuery("#recallHeadingText").hide();
	jQuery("#itemSearchField").val("");
		displayRecallPopup();	
	itemSearchFlag = true;
}
function itemsearchList(itemSearchKey)
{
 searchItemForKey(itemSearchKey);
}
 function searchItemForKey(itemSearchKey){
	if(itemSearchKey.length < 3){ return;}
	try{
		setTimeout(function(){
			var storeIdForSearch = jQuery("#invoiceStoreId").val();
			var itemsResults =JSON.parse(AndroidInterface.searchItemByKey(itemSearchKey,newOrderType,storeIdForSearch));
			
										if(!	(itemsResults === undefined || itemsResults === null))
										{
											if(itemsResults!=false && itemsResults.length!=0)
											{
												var tableStr = "<tr><th>Item #</th><th>Product Name</th><th>Price</th><th>In Stock</th></tr>";
												for(p=0;p< itemsResults.length; p++){

															var itemRow = itemsResults[p]; 
															var productDetails = JSON.parse(itemRow['storewisePricingAndStock']);
							var invCategory = "";
							var invitemPrice = "0";
							var stockCount = 0;							
						 	try{									
															
							var invitemName = itemRow.inventary_item_name;
							if(locallanguageinpos == true && itemRow.local_name != ""){
								invitemName = itemRow.local_name;
							}
							var invitemId = itemRow.inventory_item_no;
							var invitemPrice = itemRow.inventary_price_tax;
							var invitemPromptQuantity = "no";
							var invitemPromptPrice = "no";
							var invitemUnits =  "";
							if(		parseFloat(productDetails.price) != 0){
								invitemPrice = productDetails.price;
							}
								stockCount = productDetails.stockCount;
					
							if(newOrderType=="store sale")
							{
								if( parseFloat(productDetails.price) !=0){
								invitemPrice = (productDetails.price);
								}else{ invitemPrice = itemRow.inventary_price_tax; }
							}
							else if(newOrderType=="take away"){
									
								if( parseFloat(productDetails.price) !=0){
								invitemPrice = (productDetails.price);
								}else{ invitemPrice = itemRow.takeaway_pricewithtax; }

							}
							else if(newOrderType=="home delivery"){
								if( parseFloat(productDetails.price) !=0){
								invitemPrice = (productDetails.price);
								}else{ invitemPrice = itemRow.takeaway_pricewithtax; }
							}
						if(jQuery("#customerIdForInvoice").val()!="")
                        {
                            invitemPrice = pricingForCustomerRetrieve(itemRow.inventory_item_no,invitemPrice,jQuery("#customerIdForInvoice").val(),jQuery("#invoiceStoreId").val());
                        }
					}catch(Err){ console.log(Err.stack);}
														tableStr += "<tr class=\"tableitemrow\" data-itemid=\"" + itemRow.inventory_item_no+"\"><td>"+itemRow.inventory_item_no+"</td><td>"+ itemRow.inventary_item_name+"</td><td>"+invitemPrice+"</td><td>"+stockCount+"</td></tr>";
												}
											}else{
											var tableStr = "<tr><th>Item #</th><th>Product Name</th><th>Price</th><th>In Stock</th></tr>";
												
												}
										}else{
											var tableStr = "<tr><th>Item #</th><th>Product Name</th><th>Price</th><th>In Stock</th></tr>";
												
										}
			jQuery("#itemSearchBody").html(tableStr);				    
			jQuery("#itemSearchBody tr.tableitemrow:first").addClass("active");
							    },500);
			
	}catch(Err){
		console.log(Err.stack);
	}
}

function setItemOldQuantitiesToZero(){
	jQuery('#itemsFormBody tr').each( function() {
            		
            		jQuery(this).find('input[name^="savedRowQuantity"]').val("");

            });
}

// Miscellaneous functions
function webViewInnerContent(){
	var htmlContent = jQuery("body").html();
	alert(htmlContent);
	try{
		AndroidInterface.showAlertDialogJS("HTML Content",webViewInnerContent);
	}catch(Err){

	}
	return htmlContent;
}