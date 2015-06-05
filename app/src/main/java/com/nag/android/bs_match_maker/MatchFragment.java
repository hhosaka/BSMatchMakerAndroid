package com.nag.android.bs_match_maker;

import com.nag.android.bs_match_maker.Match.STATUS;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

public class MatchFragment extends Fragment implements OnItemClickListener, AppCore.OnUpdateMatchListener {

	private static final String ARG_ROUND = "round";
	private int round;
	private ListView listview = null;
	private Button buttonFix = null;
	private Button buttonStart = null;
	private Button buttonShuffle = null;
	private TimerView timerview = null;

	static Fragment newInstance(int round) {
		Fragment fragment =  new MatchFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_ROUND, round);
		fragment.setArguments(args);
		return fragment;
	}

	public MatchFragment() {
	}

	private AppCore getAppCore(){
		return (AppCore)getActivity();
	}

	private Game getGame(){
		return getAppCore().getGame();
	}
	private Round getRound(){return getGame().getRounds().get(round);}
	private STATUS getStatus(){return getRound().getStatus();}
	private Match[] getMatches(){return getRound().getMatches();}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		round = getArguments().getInt(ARG_ROUND);
		View rootView = inflater.inflate(R.layout.fragment_match, container, false);
		timerview = (TimerView)getActivity().findViewById(R.id.TimerTextTimer);
		buttonFix = (Button)getActivity().findViewById(R.id.buttonFix);
		buttonStart = (Button)getActivity().findViewById(R.id.buttonStart);
		buttonShuffle = (Button)getActivity().findViewById(R.id.buttonShuffle);
		listview = (ListView)rootView.findViewById(R.id.listViewPlayer);

		buttonFix.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                try {
                    getGame().bind(getActivity());//TODO
                }catch(IOException e){
                    Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_LONG).show();
                }
				setUIByStatus();
			}
		});

		buttonStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				timerview.start(getGame().start());
				setUIByStatus();
			}
		});
		buttonShuffle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!getGame().make()){
                    Toast.makeText(getActivity(),getString(R.string.message_round_create_error),Toast.LENGTH_LONG).show();
                }
				listview.setAdapter(new InternalAdapter(getActivity(), getMatches()));
			}
		});

		listview.setAdapter(new InternalAdapter(getActivity(), getMatches()));
		listview.setOnItemClickListener(this);
		setUIByStatus();
		if(getStatus()==STATUS.MATCHING) {
			getAppCore().setOnUpdateMatchListener(this);// TODO , it should be ADD?
		}
		return rootView;
	}

	@Override
	public void updateMatch() {
		if(getStatus()==STATUS.MATCHING) {
			if (!getGame().make()) {
				Toast.makeText(getActivity(), getString(R.string.message_round_create_error), Toast.LENGTH_LONG).show();
			}
			listview.setAdapter(new InternalAdapter(getActivity(), getMatches()));
		}
	}

	private void setUIByStatus(){
		switch(getGame().getLatestRound().getStatus()){
        case UNDEFINED:
		case MATCHING:
			timerview.setVisibility(View.GONE);
			buttonFix.setEnabled(true);
            buttonFix.setVisibility(View.VISIBLE);
			buttonStart.setEnabled(false);
            buttonStart.setVisibility(View.VISIBLE);
            buttonShuffle.setEnabled(true);
			buttonShuffle.setVisibility(View.VISIBLE);
			break;
		case READY:
			timerview.setVisibility(View.GONE);
            buttonFix.setVisibility(View.VISIBLE);
			buttonFix.setEnabled(false);
			buttonStart.setVisibility(View.VISIBLE);
            buttonStart.setEnabled(true);
            buttonShuffle.setVisibility(View.VISIBLE);
			buttonShuffle.setEnabled(false);
			break;
		case PLAYING:
		case DONE:
			timerview.setVisibility(View.VISIBLE);
            buttonFix.setVisibility(View.GONE);
            buttonStart.setVisibility(View.GONE);
            buttonShuffle.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view,final int position, long id) {
		if(getStatus()==STATUS.PLAYING) {
			final Match match = (Match) adapter.getItemAtPosition(position);
			if(!match.isBYEGame()) {
				new ResultSelector(view.getContext(), match).show(getGame().isThreePointMatch, new ResultSelector.OnResultListener() {
					@Override
					public void onSelected() {
						adapter.getAdapter().getView(position, view, listview);
						Game game = getGame();
						Context context = getActivity();
						if (game.getStatus() == STATUS.DONE) {
							timerview.stop();
							if (!game.make()) {
								Toast.makeText(context, getString(R.string.message_round_create_error), Toast.LENGTH_LONG).show();
							} else {
								try {
									game.save(context, null);
								} catch (IOException e) {
									Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
								}
								((AppCore) getActivity()).updatePlayer(AppCore.UPDATE_MODE.ADD);
							}
						} else {
							((AppCore) getActivity()).updatePlayer(AppCore.UPDATE_MODE.DATA);
						}
					}
				});
			}
		}
	}

	private class InternalAdapter extends ArrayAdapter<Match>{
		private final LayoutInflater inflater;
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
			}else{
				convertView.setBackgroundColor(Color.LTGRAY);
			}
			return super.getView(position, convertView, parent);
		}
	}
}
