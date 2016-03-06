package com.nag.android.bs_match_maker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Round implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private final List<Match> matches=new ArrayList<Match>();
	private final int round;
    private long start_time=0;

	public Round(int round) {
		this.round = round;
	}

	public Match[]getMatches(){
		return matches.toArray(new Match[matches.size()]);
	}

	static String ConvertString(int num) {
		switch (num) {
			case 1:
				return "first";
			case 2:
				return "second";
			case 3:
				return "third";
			default:
				return num + "th";
		}
	}


	public String getTitle() {
		return "The " + ConvertString(round) + " Round "+getStatusTitle(getStatus());
	}


	// TODO it should be moved to UI layer
	private static String getStatusTitle(Match.STATUS status ) {
		switch (status) {
		case UNDEFINED:
			return "Undefined";
		case MATCHING:
			return "Matching";
		case READY:
			return "Ready";
		case PLAYING:
			return "Playing";
		case DONE:
			return "Reviewing";
		default:
			throw new UnsupportedOperationException();
		}
	}

	public Match.STATUS getStatus() {
		if (matches.size() == 0) {
			return Match.STATUS.UNDEFINED;
		} else {
			for(Match match : matches) {
				switch (match.getStatus()) {
					case MATCHING:
					case READY:
					case PLAYING:
						return match.getStatus();
					default:
						break;
				}
			}
			return Match.STATUS.DONE;
		}
	}

	public void add(Match match){
		matches.add(match);
	}
	public void add(Player player, int fullpoint){
		Match m = matches.get(matches.size()-1);
		if(m.isBYEGame()){
			m.add(player, matches.get(0).getStatus());
		}else {
			Match new_m = new Match(matches.size(), player, fullpoint);
			add(new_m);
			if(m.getStatus()== Match.STATUS.PLAYING||m.getStatus()== Match.STATUS.READY){player.Bind(new_m);}

		}
	}

	public void bind() {
		for(Match match : matches) {
			match.bind(Match.STATUS.READY);
		}
	}

	public long start() {
		for(Match match : matches) {
			match.start();
		}
        return start_time = System.currentTimeMillis();
    }

	public long getStartTime(){
		return start_time;
	}

	public void restore(Player[]players){
		for(Match match : matches){
			match.restore(players);
		}
	}
}
