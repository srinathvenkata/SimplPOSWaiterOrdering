function goback()
{

    AndroidInterface.goToMenu();
}
function resetKOTPrinterSettingsToServer()
{
    AndroidInterface.resetKOTPrinterSettingsToServer();
}
function savePrinterSelection()
{

    AndroidInterface.savePrinterSelection(jQuery("#selectedPrinterForKOT").val());
}
jQuery(document).ready(function(){
    var printersList = JSON.parse(AndroidInterface.printersList());

    for(var b=0; b < printersList.length; b++)
    {
        var printerRowId = printersList[b]['_id'];
        var printerNameForRow = printersList[b]['printer_name'];
        jQuery("#selectedPrinterForKOT").append("<option value=\""+printerRowId+"\">"+printerNameForRow+"</option>");
    }
    var selectedPrinterRowId = AndroidInterface.selectedPrinter();
    jQuery("#selectedPrinterForKOT").val(selectedPrinterRowId);


                try{
                                var params = AndroidInterface.initializeParameters();
                                // bootbox.alert(params);
                                var parameters = JSON.parse(params);
var categoriesTable = JSON.parse(parameters.categoriesList);

var printersList = JSON.parse(parameters.printersList);
                                var printerTableHTML = "";
                                var itemListPrintHtml = "<option value=\"\">--</option>";
                                for(i=0; i < printersList.length ; i++)
                                {
                                    var printerName = printersList[i].printer_name; // (printersList[i].printer_name).replace(/'/g, "\\'");;
                                    var printerWidth = printersList[i].printer_width;
                                    var printerPrimary = printersList[i].is_primary_printer;
                                    var printerRowId = printersList[i]._id;
                                    printerTableHTML += "<tr><td>"+printerName+"</td><td>"+printerWidth+"</td><td>"+printerPrimary+"</td><td><button  onclick=\"editPrinter('"+printerName+"','"+printerRowId+"');\" class=\"btn btn-info\" ><i class=\"fa fa-edit\"></i></button></td><td><button class=\"btn btn-danger\" onclick=\"deletePrinter('"+printerName+"','"+printerRowId+"');\"><i class=\"fa fa-trash\"></i></button></td></tr>";

                                    itemListPrintHtml += "<option value=\""+printerRowId+"\">"+printerName+"</option>";
                                }

                                var categoryHtml = "<table class=\"table table-striped\">";
                                var categoryWisePrintersList = { };
                                    for(p=0; p< categoriesTable.length; p++)
                                    {
                                        AndroidInterface.printLog(categoriesTable[p]);
                                        var categoryId = categoriesTable[p].category_id;
                                        var categoryUniqueId = categoriesTable[p].unique_id;
                                        categoryWisePrintersList[categoryUniqueId] =     categoriesTable[p].printer_row_id;
                                        categoryHtml += "<tr><td>"+ (categoryId) +"</td><td><select class=\"form-control\" id=\"printerForCategory_"+categoryUniqueId+"\">" + itemListPrintHtml + "</select></td><td><button id=\"saveCategoryPrintBtn_"+ categoryUniqueId +"\" onclick=\"javascript:saveCategoryPrinter('" + categoryUniqueId + "');\" class=\"btn btn-success btn-block\"><i class=\"fa fa-check\"></i> Save</button></td></tr>";
                                    }
                                categoryHtml += "</table>";
                                AndroidInterface.printLog(categoryHtml);
                                jQuery("#categoryprinting").append(categoryHtml);


                                for(var k in categoryWisePrintersList) {
                                    if(categoryWisePrintersList[k]!="")
                                    {
                                   jQuery("#printerForCategory_"+k).val(categoryWisePrintersList[k]);
                                   }
                                }
    }catch(Err){
    AndroidInterface.printLog(Err.stack);
    AndroidInterface.printLog(Err.message);
    }
});


function saveCategoryPrinter(categoryUniqueId){
    var printerForCategoryRowId = jQuery("#printerForCategory_"+categoryUniqueId).val();
    try{
    AndroidInterface.savePrinterForCategoryWisePrinting(categoryUniqueId,printerForCategoryRowId);
    }catch(Err){
        bootbox.alert(Err.stack);
    }
    jQuery("#saveCategoryPrintBtn_"+categoryUniqueId).notify("Saved Successfully","success")
}
function resetCategorywiseKOTPrinterSettingsToServer()
{
    AndroidInterface.resetCategorywiseKOTPrinterSettingsToServer();
}