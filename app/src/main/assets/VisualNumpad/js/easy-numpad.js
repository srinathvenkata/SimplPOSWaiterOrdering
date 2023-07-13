$(document).ready(function () {
    $('.easy-get').on('click', function() {
        show_easy_numpad();
    });
});

function show_easy_numpad() {
    var easy_numpad = "";
    $('body').append(easy_numpad);
}

function easy_numpad_close() {
    $('#easy-numpad-frame').remove();
}

function easynum() {
    event.preventDefault();
    var easy_num_button = $(event.target);
    var presentNumpadText = jQuery("#easy-numpad-output").text();
    
    var easy_num_value = easy_num_button.text();
    if(easy_num_value=="."){
       if(presentNumpadText.includes('.')){ return;}
    }
    $('#easy-numpad-output').append(easy_num_value);
}
function easynum2() {
    event.preventDefault();
    var easy_num_button = $(event.target);
    var presentNumpadText = jQuery("#easy-numpad-output-2").text();
    
    var easy_num_value = easy_num_button.text();
    if(easy_num_value=="."){
       if(presentNumpadText.includes('.')){ return;}
    }
    $('#easy-numpad-output-2').append(easy_num_value);
}
function easy_numpad_del() {
    event.preventDefault();
    var easy_numpad_output_val = $('#easy-numpad-output').text();
    var easy_numpad_output_val_deleted = easy_numpad_output_val.slice(0, -1);
    $('#easy-numpad-output').text(easy_numpad_output_val_deleted);
}
function easy_numpad_del2() {
    event.preventDefault();
    var easy_numpad_output_val = $('#easy-numpad-output').text();
    var easy_numpad_output_val_deleted = easy_numpad_output_val.slice(0, -1);
    $('#easy-numpad-output-2').text(easy_numpad_output_val_deleted);
}
function easy_numpad_clear() {
    event.preventDefault();
    $('#easy-numpad-output').text("");
}
function easy_numpad_clear2() {
    event.preventDefault();
    $('#easy-numpad-output-2').text("");
}
function easy_numpad_cancel() {
    event.preventDefault();
    $('#easy-numpad-frame').remove();
}
function easy_numpad_cancel2() {
    event.preventDefault();
}
function easy_numpad_done2() {
    finalPay();
}