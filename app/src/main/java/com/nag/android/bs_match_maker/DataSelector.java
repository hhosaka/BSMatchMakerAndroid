package com.nag.android.bs_match_maker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DataSelector {
	public interface ResultListener{
		void onSelected(String filename);
	}

	public static void show(Context context, String title,final ResultListener listener){
		final String[]files=context.fileList();
		new AlertDialog.Builder(context)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(title)
		.setSingleChoiceItems(files, 0, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if(whichButton>=0){
					listener.onSelected(files[whichButton]);
				}else{
					listener.onSelected(null);
				}
				dialog.dismiss();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				listener.onSelected(null);
			}
		})
		.show();
	}
}
