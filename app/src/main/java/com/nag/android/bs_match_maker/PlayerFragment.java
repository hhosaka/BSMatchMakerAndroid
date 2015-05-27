package com.nag.android.bs_match_maker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PlayerFragment extends Fragment implements AppCore.OnUpdatePlayersListener, OnItemClickListener{
	private ListView listview = null;

	public static PlayerFragment newInstance() {
		return new PlayerFragment();
	}

	public PlayerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_player, container, false);
		listview = (ListView)rootView.findViewById(R.id.listViewPlayer);
		listview.setOnItemClickListener(this);
		onUpdatePlayer(getPlayers());
		return rootView;
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		((AppCore)getActivity()).setOnUpdatePlayersListener(this);
	}

	private Game getGame(){
		return ((AppCore)getActivity()).getGame();
	}
	Player[]getPlayers(){
		return getGame().getPlayers();
	}

	@Override
	public void onUpdatePlayer(Player[] players) {
		assert(listview!=null);
		if(listview!=null) {// TODO need more consideration
			listview.setAdapter(new InternalAdapter(getActivity(), players));
		}
	}

	private class InternalAdapter extends ArrayAdapter<Player>{

		private LayoutInflater inflater = null;

		public InternalAdapter(Context context,Player[]players) {
			super(context, 0,players);
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            sort(new Player.ComparisonWithId());
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			}
			((TextView)convertView.findViewById(android.R.id.text1)).setText(getTitle(getItem(position), parent.getWidth()));
			return convertView;
		}

		public String getTitle(Player player,int width){
			final int LIMIT_WIDTH = 500;
			StringBuilder sb=new StringBuilder();
			sb.append(String.format("%1$3d", player.getRank()));
			sb.append(")");
			sb.append(String.format("%1$03d", player.getId()));
			sb.append(" : ");
			sb.append(player.getName());
			if(width > LIMIT_WIDTH) {
				sb.append("P(");
				sb.append(player.getMatchPercentage());
				sb.append(") OP(");
				sb.append(player.getOpponentPercentage());
				sb.append(") WP(");
				sb.append(player.getWinPoint());
				sb.append(")");
			}
			return sb.toString();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, final View view, final int position, long id) {
		final Player player = (Player)adapter.getItemAtPosition(position);

        View layout = getActivity().getLayoutInflater().inflate(R.layout.layout_player_information, null);
		final EditText editView = (EditText)layout.findViewById(R.id.editTextName);
		final CheckBox cb = (CheckBox)layout.findViewById(R.id.checkBoxDropped);
		cb.setEnabled(getGame().getStatus() == Match.STATUS.MATCHING);
		((ListView) layout.findViewById(R.id.listViewLog)).setAdapter(new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, player.getLog()));
		editView.setText(player.getName());
		cb.setChecked(player.getDropped());
		new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(getString(R.string.label_player))
			.setView(layout)
			.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					player.setName(editView.getText().toString());
					player.setDropped(cb.isChecked());
					if(cb.isChecked()){
						((AppCore)getActivity()).updateMatch();
					}
					listview.getAdapter().getView(position, view, listview);
				}
			})
			.setNegativeButton(getString(R.string.label_cancel), null)
			.show();
	}
}
