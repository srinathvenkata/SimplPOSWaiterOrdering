
jQuery(document).ready(function(){
       AndroidInterface.checkLocationServices();
      var allConfigDetails = JSON.parse(AndroidInterface.allConfigDetails());
                jQuery("#companyid").val(allConfigDetails.company_id);
                jQuery("#hostaddress").val(allConfigDetails.hostaddress);
                jQuery("#mysqluserid").val(allConfigDetails.mysqluserid);
                jQuery("#mysqlpassword").val(allConfigDetails.mysqlpassword);
                jQuery("#mysqluserportnumber").val(allConfigDetails.mysqluserportnumber);
                jQuery("#username").val(allConfigDetails.username);
                jQuery("#password").val(allConfigDetails.password);
                jQuery("#apacheportnumber").val(allConfigDetails.apacheportnumber);
                if(allConfigDetails.username!="" && allConfigDetails.password!="")
                {
                    jQuery("#rememberme").attr("checked","checked");
                }
                jQuery("#screenorientation").val(allConfigDetails.orienation);
});
function saveMysqlDetails()
{
    AndroidInterface.saveMysqlDetails(jQuery("#hostaddress").val(),jQuery("#mysqluserid").val(),jQuery("#mysqlpassword").val(),jQuery("#mysqluserportnumber").val(),jQuery("#apacheportnumber").val());
}
function showConfigurationSettingsModal()
{
    jQuery("#myModal").modal("show");
}
function saveCompanyID()
{
    AndroidInterface.saveCompanyID(jQuery("#companyid").val());
}
function savescreenOrientationForPOS()
{
    AndroidInterface.savescreenOrientationForPOS(jQuery("#screenorientation").val());
}

jQuery(document).on("click",".loginusertype", function(event){

    jQuery('.loginusertype').each( function() {
        jQuery(this).removeClass("active");
    });
        jQuery(this).addClass("active");

});

$('.carousel').on('touchstart', function(event){
    const xClick = event.originalEvent.touches[0].pageX;
    $(this).one('touchmove', function(event){
        const xMove = event.originalEvent.touches[0].pageX;
        const sensitivityInPx = 5;

        if( Math.floor(xClick - xMove) > sensitivityInPx ){
            $(this).carousel('next');
        }
        else if( Math.floor(xClick - xMove) < -sensitivityInPx ){
            $(this).carousel('prev');
        }
    });
    $(this).on('touchend', function(){
        $(this).off('touchmove');
    });
});

$(document).ready(function() {
$(document).on('swipeleft', '.carousel', function(event){
      $(this).carousel('next');
});

$(document).on('swiperight', '.carousel', function(event){
      $(this).carousel('prev');
});

});
function loginCall()
{

    		        if(allowOnlyLowerChars()==false){ return ;}
//    AndroidInterface.loginCall(jQuery("#username").val(),jQuery("#password").val(),jQuery("#typeofuser").val());
    try{
        AndroidInterface.showLoadingDialog();

        if(jQuery("#rememberme").is(":checked")){
                                                                                AndroidInterface.userLogin(jQuery("#username").val(),jQuery("#password").val(),true);
                                }else{

                                                                                AndroidInterface.userLogin(jQuery("#username").val(),jQuery("#password").val(),false);
                                }

                        AndroidInterface.stopLoadingDialog();
    }catch(Err)
    {
        console.log(Err.stack);
    }

}
function changeTypeOfUser(userType){
    jQuery("#typeofuser").val(userType);
}
function hideLoadingGif()
{
    AndroidInterface.printLog("hideLoadingGif called");
    jQuery("#loadingGif").hide();
}
function showLoadingGif()
{
    AndroidInterface.printLog("showLoadingGif called");
    jQuery("#loadingGif").show();
}

jQuery("#username").change(function(){
    allowOnlyLowerChars();
});

                                                                        function allowOnlyLowerChars()
                                                                        {
                                                                            if(isLowerCase(jQuery("#username").val()) == false  && (jQuery("#Username").val()!="") )
                                                                            {
                                                                                jQuery('#username').notify("Use only lower case alphabets and numbers", "error");
                                                                                var newUserName = jQuery('#username').val();
                                                                                newUserName = newUserName.substring(0,newUserName.length-1);
                                                                                // jQuery('#Username').val(newUserName);
                                                                                return false;
                                                                            }
                                                                            return true;
                                                                        }
                                                                        function isLowerCase(str)
                                                                        {
                                                                            var letters = /^[a-z0-9]+$/;
                                                                            if(str.match(letters))
                                                                             {
                                                                              return true;
                                                                             }
                                                                           else
                                                                             {
                                                                             return false;
                                                                             }
                                                                            return str == str.toLowerCase() && str != str.toUpperCase();
                                                                        }
