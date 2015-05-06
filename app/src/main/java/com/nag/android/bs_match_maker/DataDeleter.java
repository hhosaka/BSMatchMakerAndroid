package com.nag.android.bs_match_maker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

class DataDeleter {
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
 		.setNegativeButton(R.string.label_cancel, null)
		.setNeutralButton(R.string.label_delete_all, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int index) {
				for (String file : files) {
					context.deleteFile(file);
				}
			}
		})
        .setPositiveButton(R.string.label_delete, new OnClickListener() {
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
