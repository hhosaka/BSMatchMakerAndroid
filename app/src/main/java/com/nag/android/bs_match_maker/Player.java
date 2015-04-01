package com.nag.android.bs_match_maker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Player implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private String name;
	private List<Match> matches = new ArrayList<Match>();
	public boolean Dropped;
	public int id;
	public int rank;

	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	public float getMatchPercentage() {
		if (matches.size() == 0) {
			return 0;
		} else {
			return (float)getMatchPoint() / (matches.size() * 3);
		}
	}

	public int getMatchPoint() {
		int ret = 0;
		for(Match match : matches) {
			ret += match.getMatchPoint(this);
		}
		return ret;
	}

	public int getWinPoint() {
		int ret = 0;
		for (Match match : matches) {
			ret += match.getWinPoint(this);
		}
		return ret;
	}

	public float getOpponentPercentage() {
		if (matches.size() == 0) {
			return 0;
		} else {
			return getOpponentPoint() / (matches.size() * 3);
		}
	}

	public float getOpponentPoint() {
		float ret = 0;
		int count = 0;
		for (Match match : matches) {
			if (!match.isBYEGame()) {
				++count;
				ret += match.getOpponentMatchPoint(this);
			}
		}
		if (count == 0) {
			return 0;
		} else {
			return ret / count;
		}
	}

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}

	void Bind(Match match){
		matches.add(match); 
	}

	boolean hasMatched(Player opponent) {
		for (Match match : matches) {
			for(Player p : match.getPlayers()){
				if (p == opponent) {
					return true;
				}
			}
		}
		return false;
	}

	public static int comparison(Player a, Player b) {
		int ret;
		float fret;
		if ((ret = b.getMatchPoint() - a.getMatchPoint()) != 0) {
			return ret;
		} else if ((fret = b.getOpponentPoint() - a.getOpponentPoint()) != 0.0) {
			return fret > 0.0 ? 1 : -1;
		} else if ((ret = b.getWinPoint() - a.getWinPoint()) != 0) {
			return ret;
		} else {
			return 0;
		}
	}

	public static class Comparison implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			return comparison(lhs, rhs);
		}
	}

	public static int comparison_with_id(Player a, Player b) {
		int ret;
		if ((ret = comparison(a, b)) != 0) {
			return ret;
		} else {
			return b.id - a.id;
		}
	}

	public static class ComparisonWithId implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			return comparison_with_id(lhs, rhs);
		}
	}

//	public static Player[] OrderByRank(Player[] players) {
//		int rank = 0;
//		int i = 0;
//		Player prev = null;
//
//		Arrays.sort(players, new ComparisonWithId());
//		for (Player player : players) {
//			++i;
//			if (prev == null || comparison(prev, player) != 0) {
//				rank = i;
//			}
//			player.rank = rank;
//			prev = player;
//		}
//		return players;
//	}

	public String toString(){
		return String.format("%1$03d", id) +" : " + name + " ("+ getMatchPercentage()+")";
	}

	public static Player[] create(int count){
		List<Player>ret = new ArrayList<Player>();
		for (int i = 0; i < count; ++i) {
			ret.add(new Player(i, "Player" + String.format("%1$03d", i)));
		}
		return ret.toArray(new Player[0]);
	}
}
