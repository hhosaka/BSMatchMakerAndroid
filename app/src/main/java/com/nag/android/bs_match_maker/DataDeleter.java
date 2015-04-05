package com.nag.android.bs_match_maker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.Button;

public class DataDeleter {
	public static void show(final Context context, String title){
		final String[]files=context.fileList();
        final boolean[]flags=new boolean[files.length];
		new AlertDialog.Builder(context)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(title)
		.setMultiChoiceItems(files, flags, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int index, boolean flag) {
                flags[index]=flag;
            }
        })
 		.setNegativeButton("Cancel", null)
        .setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                for (int i = 0; i < files.length; ++i) {
                    if (flags[i]) {
                        context.deleteFile(files[i]);
                    }
                }
            }
        })
		.show();
	}
}
