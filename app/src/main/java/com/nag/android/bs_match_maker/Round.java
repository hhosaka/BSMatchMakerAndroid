package com.nag.android.bs_match_maker;

import java.io.Serializable;
import java.util.ArrayList;

public class Round implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private ArrayList<Match> matches=new ArrayList<Match>();
	private int round;

	public Match[]getMatches(){
		return matches.toArray(new Match[0]);
	}

	public static String ConvertString(int num) {
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
	public Round(int round) {
		this.round = round;
	}

	private static String getStatusTitle(Match.STATUS status ) {
		switch (status) {
		case UNDEF:
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
			return Match.STATUS.UNDEF;
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

	public boolean add(Match match){
		return matches.add(match);
	}

	public void bind() {
		for(Match match : matches) {
			match.bind();
		}
	}
	public void start() {
		for(Match match : matches) {
			match.start();
		}
	}
}
