package com.nag.android.bs_match_maker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.nag.android.util.PreferenceHelper;

public class CreateGameDialog extends DialogFragment {
	private static final String PREF_DEFAULT_NUMBER_OF_PLAYER = "default_number_of_player";
	private static final String PREF_DEFAULT_IS_THREE_POINT_MATCH = "default_is_three_point_match";
	private final static String PREF_PLAYER_PREFIX = "player_prefix";

	interface CreateGameHandler{
		void onCreateGame(String prefix, int num, boolean isThirdPointMatch);
	}
	private NumberPicker np;
	private CheckBox cb;
	private TextView prefix;

	public CreateGameDialog(){
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int MAX_PLAYER = 128;
		final int MIN_PLAYER = 2;

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_inital, null);// TODO how should I handle 2nd parameter
		np = (NumberPicker)view.findViewById(R.id.numberPickerPlayer);
		np.setMaxValue(MAX_PLAYER);
		np.setMinValue(MIN_PLAYER);
		np.setValue(PreferenceHelper.getInstance(getActivity()).getInt(PREF_DEFAULT_NUMBER_OF_PLAYER, 8));
		cb = (CheckBox)view.findViewById(R.id.checkBoxIsThreePointMatch);
		cb.setChecked(PreferenceHelper.getInstance(getActivity()).getBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, false));
		prefix = (EditText)view.findViewById(R.id.editTextPlayerPrefix);
		prefix.setText(PreferenceHelper.getInstance(getActivity()).getString(PREF_PLAYER_PREFIX,getString(R.string.label_player_prefix_default)));
		prefix.setInputType(InputType.TYPE_CLASS_TEXT);
		return new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.action_initial))
				.setView(view)
				.setNegativeButton(getString(R.string.label_cancel), null)
				.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PreferenceHelper.getInstance(getActivity()).putInt(PREF_DEFAULT_NUMBER_OF_PLAYER, np.getValue());
						PreferenceHelper.getInstance(getActivity()).putBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, cb.isChecked());
						PreferenceHelper.getInstance(getActivity()).putString(PREF_PLAYER_PREFIX, prefix.getText().toString());
						((CreateGameHandler)getActivity()).onCreateGame(prefix.getText().toString(), np.getValue(),cb.isChecked());
					}
				}).create();
	}
}
