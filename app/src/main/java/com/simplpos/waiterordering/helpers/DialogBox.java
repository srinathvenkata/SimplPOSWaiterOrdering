package com.simplpos.waiterordering.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogBox {
    public boolean dialogBox(String title,String msg, String okButton, String cancelButton, final Context activity) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(msg);
        if (okButton != "") {
            alertDialogBuilder.setPositiveButton(okButton,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) { /**/ }
                    });
        }
        if (cancelButton != "") {
            alertDialogBuilder.setNegativeButton(cancelButton,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) { /**/ }
                    });
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return true;
    }
}

