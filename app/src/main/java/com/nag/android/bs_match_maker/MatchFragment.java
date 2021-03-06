package com.nag.android.bs_match_maker;

import com.nag.android.bs_match_maker.Match.STATUS;

import android.app.Activity;
//import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MatchFragment extends Fragment implements OnItemClickListener, AppCore.OnUpdateMatchListener {

	private static final String ARG_ROUND = "round";
	private int round;
	private ListView listview = null;
	private Button buttonFix = null;
	private Button buttonStart = null;
	private Button buttonShuffle = null;
	private Button buttonMakeMatch = null;
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

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		getAppCore().setOnUpdateMatchListener(this);
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
		buttonMakeMatch = (Button)getActivity().findViewById(R.id.buttonMakeMatch);
		listview = (ListView)rootView.findViewById(R.id.listViewPlayer);
		listview.setEmptyView(rootView.findViewById(R.id.emptyTextView));
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
				timerview.start(getGame().start(getActivity()));
				setUIByStatus();
			}
		});
		buttonShuffle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				getGame().make();
				listview.setAdapter(new InternalAdapter(getActivity(), getMatches()));
			}
		});
		buttonMakeMatch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				getGame().make();
				getAppCore().makeMatch();
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
			getGame().make();
		}
		listview.setAdapter(new InternalAdapter(getActivity(), getMatches()));
	}

	private void setUIByStatus(){
		switch(getGame().getLatestRound().getStatus()){
        case UNDEFINED:
		case MATCHING:
			timerview.stop();
			timerview.setVisibility(View.GONE);
			buttonFix.setEnabled(true);
            buttonFix.setVisibility(View.VISIBLE);
			buttonStart.setEnabled(false);
            buttonStart.setVisibility(View.VISIBLE);
            buttonShuffle.setEnabled(true);
			buttonShuffle.setVisibility(View.VISIBLE);
			buttonMakeMatch.setVisibility(View.GONE);
			break;
		case READY:
			timerview.stop();
			timerview.setVisibility(View.GONE);
            buttonFix.setVisibility(View.VISIBLE);
			buttonFix.setEnabled(false);
			buttonStart.setVisibility(View.VISIBLE);
            buttonStart.setEnabled(true);
            buttonShuffle.setVisibility(View.VISIBLE);
			buttonShuffle.setEnabled(false);
			buttonMakeMatch.setVisibility(View.GONE);
			break;
		case PLAYING:
			timerview.setVisibility(View.VISIBLE);
            buttonFix.setVisibility(View.GONE);
            buttonStart.setVisibility(View.GONE);
            buttonShuffle.setVisibility(View.GONE);
			buttonMakeMatch.setVisibility(View.GONE);
			break;
		case DONE:
			timerview.stop();
			timerview.setVisibility(View.GONE);
			buttonFix.setVisibility(View.GONE);
			buttonStart.setVisibility(View.GONE);
			buttonShuffle.setVisibility(View.GONE);
			buttonMakeMatch.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view,final int position, long id) {
		if (getAppCore().getGame().isCurrentRound(round)) {
			switch (getStatus()) {
				case PLAYING:
				case DONE: {
					final Match match = (Match) adapter.getItemAtPosition(position);
					if (!match.isBYEGame()) {
						new ResultSelector(view.getContext(), match).show(getGame().isThreePointMatch, new ResultSelector.OnResultListener() {
							@Override
							public void onSelected() {
								adapter.getAdapter().getView(position, view, listview);
								setUIByStatus();
								//							if (game.getStatus() == STATUS.DONE) {
								//								timerview.stop();
								//								buttonMakeMatch.setVisibility(View.VISIBLE);
								//							}
								Context context = getActivity();
								getAppCore().updatePlayer();
								try {
									getGame().save(context, null);
								} catch (IOException e) {
									Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
								}
							}
						});
					}
				}
			}
		}
	}

	private class InternalAdapter extends ArrayAdapter<Match>{
		private final LayoutInflater inflater;
		public InternalAdapter(Context context, Match[] matches) {
			super(context, 0, matches);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Match match = getItem(position);
			if(convertView==null){
				convertView = inflater.inflate(R.layout.fragment_match_item, null);
			}
			((TextView)convertView.findViewById(R.id.textViewPlayer1)).setText(match.getPlayers()[0].getName());
			((TextView)convertView.findViewById(R.id.textViewPlayer2)).setText(match.getPlayers()[1].getName());
			((TextView)convertView.findViewById(R.id.textViewID1)).setText(String.valueOf(position+1));
			((TextView)convertView.findViewById(R.id.textViewID2)).setText(String.valueOf(position+1));
			TextView status = (TextView)convertView.findViewById(R.id.textViewStatus);
			switch(match.getStatus()){
			case DONE:
				if(match.getResult().getDiff()>0){
					status.setText(getString(R.string.label_player1_win));
				}else if(match.getResult().getDiff()<0){
					status.setText(getString(R.string.label_player2_win));
				}else{
					status.setText(getString(R.string.label_draw));
				}
				status.setBackgroundColor(Color.GREEN);
				break;
			case MATCHING:
			case READY:
			case PLAYING:
				if(match.isGapMatch()){
					status.setText(getString(R.string.label_gap) + match.getStatus().name());
					status.setBackgroundColor(Color.GRAY);
				}else {
					status.setText(match.getStatus().name());
					status.setBackgroundColor(Color.LTGRAY);
				}
				break;
			}
			return convertView;
		}
	}
}
