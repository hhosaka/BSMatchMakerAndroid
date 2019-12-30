package com.nag.android.bs_match_maker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Player implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private String name;
	private final List<Match> matches = new ArrayList<Match>();
	private boolean dropped = false;
	public final int id;
	private int rank;

	public int getId(){return id;}
	public String getName(){
		return name;
	}

	public int getRank(){return rank;}
	public void setDropped(boolean dropped){this.dropped = dropped;}
	public boolean getDropped(){return dropped;}

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
		for (Match match : matches) {
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

	float getOpponentPoint() {
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

	int getDirectResult(Player opponent) {
		for (Match match : matches) {
			if(match.getPlayers()[0].getId()==opponent.getId())
			{
				return -match.getResult().getDiff();
			}
			else if(match.getPlayers()[1].getId()==opponent.getId())
			{
				return match.getResult().getDiff();
			}
		}
		return 0;
	}

	private static int comparison(Player a, Player b) {
		int ret;
		float fret;
		if ((ret = b.getMatchPoint() - a.getMatchPoint()) != 0) {
			return ret;
		} else if ((fret = b.getOpponentPoint() - a.getOpponentPoint()) != 0.0) {
			return fret > 0.0 ? 1 : -1;
		} else if ((ret = b.getWinPoint() - a.getWinPoint()) != 0) {
			return ret;
		} else {
			return a.getDirectResult(b);
		}
	}

	private static int comparison_win_only(Player a, Player b) {
		return b.getMatchPoint() - a.getMatchPoint();
	}

	public static class Comparison implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			return comparison(lhs, rhs);
		}
	}

	public static class ComparisonWinOnly implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			return comparison_win_only(lhs, rhs);
		}
	}

	public static class ComparisonWithId implements Comparator<Player> {
		@Override
		public int compare(Player lhs, Player rhs) {
			int ret;
			if ((ret = comparison(lhs, rhs)) != 0) {
				return ret;
			} else {
				return lhs.id - rhs.id;
			}
		}
	}

	public static void updateRank(Player[] players) {
		int rank = 0;
		int i = 0;
		Player prev = null;

		Arrays.sort(players, new ComparisonWithId());
		for (Player player : players) {
			++i;
			if (prev == null || comparison(prev, player) != 0) {
				rank = i;
			}
			player.rank = rank;
			prev = player;
		}
	}

	public static Player[] create(String prefix, int count){
		List<Player>ret = new ArrayList<Player>();
		for (int i = 0; i < count; ++i) {
			ret.add(new Player(i, prefix + String.format("%1$03d", i+1)));
	}
		return ret.toArray(new Player[ret.size()]);
	}

	public static Player[] create(BufferedReader reader) throws IOException {
		List<Player>ret = new ArrayList<Player>();
		int index = 0;
		for (;;){
			String buf = reader.readLine();
			if(buf==null) {
				return ret.toArray(new Player[ret.size()]);
			}else if(buf.length()>0){
				ret.add(new Player(++index, buf));
			}
		}
	}

    public String[] getLog(){
        List<String>buf=new ArrayList<String>();
        for(Match match : matches){
            buf.add(match.getLogString(this));
        }
        return buf.toArray(new String[buf.size()]);
    }

	public boolean hasGapMatch()
	{
		for(Match match : matches){
			if(match.isGapMatch()){return true;}
		}
		return false;
	}

	public boolean byeAcceptable(boolean force)
	{
		if(matches.size()==0){
			return true;
		}
		else{
			if(force){
                for(Match match : matches){
                    if(match.getWinPoint(this)!=3){return true;}
                }
                return false;
			}else{
                for(Match match : matches){
                    if(match.getWinPoint(this)>0){return false;}
                }
                return true;
			}
		}
	}
	public boolean hasBye()
	{
		for(Match m : matches){
			if(m.isBYEGame()){
				return true;
			}
		}
		return false;
	}
}
