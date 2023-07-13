
function setPortraitDimensions()
{
	try{
	var w = (window.innerWidth).toString();
	var h = parseInt(jQuery("#completeHolder").height());
//	 AndroidInterface.showToast("width is "+w+"px and height is "+h+"px"+" and ration is "+window.devicePixelRatio);
	// jQuery("body").css({ 'width' : (w+"px"), 'height' : (h+"px"),'overflow':'hidden'});


	var remainingHeight = parseInt(h) - parseInt(jQuery(".headingSet").height());
    var rightHalfHeight = parseInt(jQuery("#itemSelectionAndPreviewReceipt").height());
	jQuery("#rightHalf").css({'height' : (rightHalfHeight+"px"),'overflow':'hidden'});
	jQuery("#leftHalf").css({'height' : (remainingHeight+"px"),'overflow':'hidden'});

	var bottomPercentage = 40;
	if(	((parseInt(w)) > 250) && ((parseInt(w)) <= 700)	)
    {
        bottomPercentage = 30;
    }
	else if(	((parseInt(w)) > 700) && ((parseInt(w)) < 1000)	)
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
	var bottomPercentageforItemsSection = 25;
	var totalHeightForPopupCOntent = parseFloat(jQuery("#rightHalf").height());
	var rightPaymentsSectionHeight = parseFloat(totalHeightForPopupCOntent) * (parseFloat(bottomPercentageforItemsSection) ) /100 ;
	var rightItemsSelectionHeight = parseFloat(totalHeightForPopupCOntent)  - parseFloat(rightPaymentsSectionHeight);

	jQuery("#itemsSelector").css({'height' : ((parseInt(rightItemsSelectionHeight)).toString())+"px" , 'top' : "0px" });
	jQuery("#paymentCustomerButtonsSection").css({'height' : ((parseInt(rightPaymentsSectionHeight)).toString())+"px" ,'top' : (rightItemsSelectionHeight.toString())+"px" });

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


jQuery(document).on("click","#selectItemsButton", function(event){
    jQuery("#itemSelectionAndPreviewReceipt").show();


	jQuery(".overlay2").show();
});
jQuery(document).on("click","#closeItemsSelection", function(event){
    closeItemsSelection();
});
function closeItemsSelection()
{
    jQuery("#itemSelectionAndPreviewReceipt").hide();


    jQuery(".overlay2").hide();
}

//jQuery(document).on("mousedown","#printPreviewButton", function(event){
//closeItemsSelection();
//});

jQuery(document).on("mousedown","#advancedSettings", function(event){
//closeItemsSelection();
});

jQuery(document).on("mousedown",".productName", function(event){
    jQuery(this).addClass("productNameSelected");
    var elem = jQuery(this);
                                    setTimeout(function(){
    							    		elem.removeClass("productNameSelected");
		                                    var oldQty =  parseFloat(jQuery("tr.highlightrow").find('input[name^="savedRowQuantity"]').val());
                                            var newQty =  parseFloat(jQuery("tr.highlightrow").find('input[name^="itemQuantity"]').val());
                                            var forItem = jQuery("tr.highlightrow").find('input[name^="itemName"]').val();

                                            var diffInQty = newQty - oldQty;
//                                            AndroidInterface.showToast("Added "+diffInQty+" - "+forItem);
                                            elem.notify("Added "+(diffInQty),{ position:"bottom" , className : "success" ,  autoHideDelay: 1000 });
    								},400);

});

