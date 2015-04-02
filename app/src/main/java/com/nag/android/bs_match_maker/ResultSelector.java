package com.nag.android.bs_match_maker;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;
import android.view.View.OnClickListener;

public class ResultSelector extends RadioGroup implements DialogInterface.OnClickListener {

	private Match match;
	private int id=0;
	public ResultSelector(Context context, Match match, boolean isThreePointMatch) {
		super(context);
		this.match = match;
		init(context, match, isThreePointMatch);
	}

	private void add(Context context, Match match, String label){
		RadioButton radiobutton = new RadioButton(context);
		radiobutton.setText(label);
		radiobutton.setId(++id);
		addView(radiobutton);
		if(label.equals(match.getLabel())){
			check(radiobutton.getId());
		}
	}

	
	private void init(Context context, Match match, boolean isThreePointMatch){
		if(isThreePointMatch){
			add(context, match, "2-0:"+match.getPlayers()[0].getName()+" win");
			add(context, match, "2-1:"+match.getPlayers()[0].getName()+" win");
			add(context, match, "1-0:"+match.getPlayers()[0].getName()+" win");
			add(context, match, "1-1:Draw");
			add(context, match, "0-0:Draw");
			add(context, match, "0-1:"+match.getPlayers()[1].getName()+" win");
			add(context, match, "1-2:"+match.getPlayers()[1].getName()+" win");
			add(context, match, "0-2:"+match.getPlayers()[1].getName()+" win");
		}else{
			add(context, match, "1-0:"+match.getPlayers()[0].getName()+" win");
			add(context, match, "0-0:Draw");
			add(context, match, "0-1:"+match.getPlayers()[0].getName()+" win");
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		String title = ((RadioButton)findViewById(this.getCheckedRadioButtonId())).getText().toString();
		match.Update(Integer.valueOf(title.substring(0,1)),Integer.valueOf(title.substring(2,3)));
	}
}
