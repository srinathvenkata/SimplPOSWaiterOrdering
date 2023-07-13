jQuery(document).ready(function(){

});
function logoutuser()
{
    AndroidInterface.logoutUser();
}
function goToHardware()
{
    AndroidInterface.goToHardware();
}
function goToPos()
{
    AndroidInterface.goToPOS();
}
function goToSettingsPage()
{
    AndroidInterface.goToSettings();
}
var isCopying = false;
function copyProductsData()
{
    if(isCopying==true){ return; }
    jQuery("#showLoadingModal").modal("show");
    jQuery("#copyDataFromServerBtn").attr("disabled","disabled");
    isCopying = true;
    AndroidInterface.copyProductsData();
    isCopying = false;
    jQuery("#copyDataFromServerBtn").removeAttr("disabled");
    jQuery("#showLoadingModal").modal("hide");
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