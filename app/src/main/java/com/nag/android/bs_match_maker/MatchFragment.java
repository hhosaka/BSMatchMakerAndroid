package com.nag.android.bs_match_maker;

import com.nag.android.bs_match_maker.Match.STATUS;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;

public class MatchFragment extends Fragment implements OnItemClickListener{
	private static final String ARG_ROUND = "round";
	private int round;// TODO
	private ListView listview = null;
	private Button buttonFix = null;
	private Button buttonStart = null;
	private Button buttonShuffle = null;
	private TimerView timerview = null;

	public static Fragment newInstance(int round) {
		Fragment fragment =  new MatchFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_ROUND, round);
		fragment.setArguments(args);
		return fragment;
	}

	public MatchFragment() {
	}

	private Game getGame(){
		return ((Game.GameHolder)getActivity()).getGame();
	}

	private boolean update(){
		return ((Game.GameHolder)getActivity()).update();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		round = getArguments().getInt(ARG_ROUND);
		View rootView = inflater.inflate(R.layout.fragment_match, container, false);
		timerview = (TimerView)getActivity().findViewById(R.id.TimerTextTimer);

		buttonFix = (Button)getActivity().findViewById(R.id.buttonFix);
		buttonFix.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                try {
                    getGame().bind(getActivity());//TODO
                }catch(IOException e){
                    Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_LONG);
                }
				setUIByStatus();
			}
		});
		buttonStart = (Button)getActivity().findViewById(R.id.buttonStart);
		buttonStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				getGame().getLatestRound().start();//TODO
				setUIByStatus();
				timerview.start();
			}
		});
		buttonShuffle = (Button)getActivity().findViewById(R.id.buttonShuffle);
		buttonShuffle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				getGame().make();
				listview.setAdapter(new InternalAdapter(getActivity(), getRound().getMatches()));
			}
		});
		listview = (ListView)rootView.findViewById(R.id.listViewPlayer);

		listview.setAdapter(new InternalAdapter(getActivity(), getRound().getMatches()));
		listview.setOnItemClickListener(this);
		setUIByStatus();
		return rootView;
	}

	private void setUIByStatus(){
		switch(getGame().getLatestRound().getStatus()){
		case MATCHING:
			timerview.setVisibility(View.GONE);
			buttonFix.setEnabled(true);
			buttonStart.setEnabled(false);
			buttonShuffle.setEnabled(true);
			listview.setEnabled(false);
			break;
		case READY:
			timerview.setVisibility(View.GONE);
			buttonFix.setEnabled(false);
			buttonStart.setEnabled(true);
			buttonShuffle.setEnabled(false);
			listview.setEnabled(false);
			break;
		case PLAYING:
		case DONE:
			timerview.setVisibility(View.VISIBLE);
			buttonFix.setEnabled(false);
			buttonStart.setEnabled(false);
			buttonShuffle.setEnabled(false);
			listview.setEnabled(true);
			break;
		}
	}
	public Round getRound(){
		return getGame().getRound(round);
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view,final int position, long id) {
		final Match match = (Match)adapter.getItemAtPosition(position);
		final ResultSelector resultselector = new ResultSelector(getActivity(), match, false);
		new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(match.toString())
			.setView(resultselector)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resultselector.onClick(dialog, which);
					adapter.getAdapter().getView(position, view, listview);
					if(update()){
						timerview.stop();
					}
				}
			})
			.setNegativeButton("Cancel", null)
			.show();
	}

	private class InternalAdapter extends ArrayAdapter<Match>{

		private LayoutInflater inflater;
		public InternalAdapter(Context context, Match[] matches) {
			super(context, android.R.layout.simple_list_item_1, matches);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
			}
			if(getItem(position).getStatus()==STATUS.DONE){
				convertView.setBackgroundColor(Color.GREEN);
			}
			return super.getView(position, convertView, parent);
		}

	}
}
