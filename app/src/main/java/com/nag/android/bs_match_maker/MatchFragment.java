package com.nag.android.bs_match_maker;

import com.nag.android.bs_match_maker.Match.STATUS;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

public class MatchFragment extends Fragment implements OnItemClickListener, ResultSelector.OnResultListener,Game.OnUpdateMatchListener{
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
				timerview.start(getGame().getLatestRound().start());//TODO
				setUIByStatus();
			}
		});
		buttonShuffle = (Button)getActivity().findViewById(R.id.buttonShuffle);
		buttonShuffle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!getGame().make()){
                    Toast.makeText(getActivity(),getString(R.string.message_round_create_error),Toast.LENGTH_LONG).show();
                }
				listview.setAdapter(new InternalAdapter(getActivity(), getRound().getMatches()));
			}
		});
		listview = (ListView)rootView.findViewById(R.id.listViewPlayer);

		listview.setAdapter(new InternalAdapter(getActivity(), getRound().getMatches()));
		listview.setOnItemClickListener(this);
		setUIByStatus();
		if(getGame().getRound(round).getStatus()==STATUS.MATCHING) {
			((Game.GameHolder)getActivity()).setOnUpdateMatchListener(this);
		}
		return rootView;
	}

	@Override
	public void updateMatch() {
		if(getGame().getRound(round).getStatus()==STATUS.MATCHING) {
			if (!getGame().make()) {
				Toast.makeText(getActivity(), getString(R.string.message_round_create_error), Toast.LENGTH_LONG).show();
			}
			listview.setAdapter(new InternalAdapter(getActivity(), getRound().getMatches()));
		}
	}

	private void setUIByStatus(){
		switch(getGame().getLatestRound().getStatus()){
        case UNDEF:
		case MATCHING:
			timerview.setVisibility(View.GONE);
			buttonFix.setEnabled(true);
            buttonFix.setVisibility(View.VISIBLE);
			buttonStart.setEnabled(false);
            buttonStart.setVisibility(View.VISIBLE);
            buttonShuffle.setEnabled(true);
			buttonShuffle.setVisibility(View.VISIBLE);
			listview.setEnabled(false);
			break;
		case READY:
			timerview.setVisibility(View.GONE);
            buttonFix.setVisibility(View.VISIBLE);
			buttonFix.setEnabled(false);
			buttonStart.setVisibility(View.VISIBLE);
            buttonStart.setEnabled(true);
            buttonShuffle.setVisibility(View.VISIBLE);
			buttonShuffle.setEnabled(false);
			listview.setEnabled(false);
			break;
		case PLAYING:
		case DONE:
			timerview.setVisibility(View.VISIBLE);
            buttonFix.setVisibility(View.GONE);
            buttonStart.setVisibility(View.GONE);
            buttonShuffle.setVisibility(View.GONE);
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
        new ResultSelector(view.getContext(), getGame(),match).show(new ResultSelector.OnResultListener() {
			@Override
			public void onSelected() {
				adapter.getAdapter().getView(position, view, listview);
				if (getGame().getLatestRound().getStatus() == STATUS.DONE) {
					try {
						getGame().save(getActivity(), true);
					} catch (IOException e) {
						Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
					}
					if (!getGame().make()) {
						Toast.makeText(getActivity(), getString(R.string.message_round_create_error), Toast.LENGTH_LONG).show();
					}
					timerview.stop();
					((Game.GameHolder) getActivity()).update(Game.UPDATE_MODE.ADD);
				} else {
					((Game.GameHolder) getActivity()).update(Game.UPDATE_MODE.DATA);
				}
			}
		});
	}

	@Override
    public void onSelected() {

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
			}else{
				convertView.setBackgroundColor(Color.LTGRAY);
			}
			return super.getView(position, convertView, parent);
		}
	}
}
