function goback()
{

    AndroidInterface.goToMenu();
}
function validateLicenseKey()
{
    AndroidInterface.validateLicenseKey(jQuery("#licenseKey").val());
}
function saveSerialNumber()
{
        AndroidInterface.saveSerialNumber(jQuery("#invoiceSerialNumberPrefix").val(),jQuery("#invoiceSerialNumber").val());

}
function saveOrderTypeNamingConvention()
{
    AndroidInterface.saveOrderTypeNamingConvention(jQuery("#storeSaleText").val(),jQuery("#takeAwayText").val());

}
function savePrinterSelection()
{

    AndroidInterface.savePrinterSelection(jQuery("#selectedPrinterForCounter").val());
}
jQuery(document).ready(function(){
    var printersList = JSON.parse(AndroidInterface.printersList());

    for(var b=0; b < printersList.length; b++)
    {
        var printerRowId = printersList[b]['_id'];
        var printerNameForRow = printersList[b]['printer_name'];
        jQuery("#selectedPrinterForCounter").append("<option value=\""+printerRowId+"\">"+printerNameForRow+"</option>");
    }
    var selectedPrinterRowId = AndroidInterface.selectedPrinter();
    jQuery("#selectedPrinterForCounter").val(selectedPrinterRowId);

});


jQuery(document).ready(function(){

      var allConfigDetails = JSON.parse(AndroidInterface.allConfigDetails());
                jQuery("#licenseKeyText").html(allConfigDetails.license_key);
                jQuery("#invoiceSerialNumberPrefix").val(allConfigDetails.invoice_prefix);
                jQuery("#invoiceSerialNumber").val(allConfigDetails.invoice_serial_number);
                jQuery("#storeSaleText").val(allConfigDetails.storesale_text);
                jQuery("#takeAwayText").val(allConfigDetails.takeaway_text);
 });