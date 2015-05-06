package com.nag.android.bs_match_maker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.RadioGroup;

import com.nag.android.util.LabeledItem;

public class ResultSelector extends RadioGroup {

    public interface OnResultListener{
        void onSelected();
    }
    private static class LabeledResult extends LabeledItem<Result>{
        public LabeledResult(String label, Result value) {
            super(label, value);
        }
    }

    private final Game game;
	private final Match match;

	public ResultSelector(Context context, Game game, Match match) {
		super(context);
        this.game = game;
		this.match = match;
	}

	private LabeledResult[] getLabels(){
		if(game.isThreePointMatch){
            return new LabeledResult[]{
			new LabeledResult("2-0:"+match.getPlayers()[0].getName()+" win", new Result(2,0)),
            new LabeledResult("2-1:"+match.getPlayers()[0].getName()+" win", new Result(2,1)),
            new LabeledResult("1-0:"+match.getPlayers()[0].getName()+" win", new Result(1,0)),
            new LabeledResult("1-1:Draw", new Result(1,1)),
            new LabeledResult("0-0:Draw", new Result(0,0)),
            new LabeledResult("0-1:"+match.getPlayers()[1].getName()+" win", new Result(0, 1)),
            new LabeledResult("1-2:"+match.getPlayers()[1].getName()+" win", new Result(1, 2)),
            new LabeledResult("0-2:"+match.getPlayers()[1].getName()+" win", new Result(0, 2))};
		}else{
            return new LabeledResult[]{
                    new LabeledResult("1-0:"+match.getPlayers()[0].getName()+" win", new Result(1,0)),
                    new LabeledResult("0-0:Draw", new Result(0,0)),
                    new LabeledResult("0-1:"+match.getPlayers()[1].getName()+" win", new Result(0,1))};
		}
	}

    private int getCurrentPosition(LabeledResult[] items){
        if(match.getStatus()== Match.STATUS.DONE) {
            for (int i = 0; i < items.length; ++i) {
                if(items[i].getValue().equals(match.getResult())){
                    return i;
                }
            }
            throw new UnsupportedOperationException();
        }else {
            return -1;
        }
    }

    public void show(final OnResultListener listener){
        final LabeledResult[] items = getLabels();
        new AlertDialog.Builder(getContext())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(match.toString())
            .setSingleChoiceItems(items, getCurrentPosition(items), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (whichButton >= 0) {
                        match.update(items[whichButton].getValue());
                        listener.onSelected();
                        dialog.dismiss();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
