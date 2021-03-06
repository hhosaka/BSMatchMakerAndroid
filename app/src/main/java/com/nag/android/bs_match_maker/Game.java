package com.nag.android.bs_match_maker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.widget.Toast;

public class Game implements Serializable{
	interface GameHolder{
	}
	static final long serialVersionUID = 1L;
	private static final String OPTION = ".bsmm";
    private static final String TEMP_FILENAME = "auto_save"+OPTION;

//    private String filename = null;
    public boolean isThreePointMatch = false;
    private Player[] players = null;
    private final Stack<Round> rounds = new Stack<Round>();

	Game(){
		this(new Player[0],false);
	}

	Game(Player[]players, boolean isThreePointMatch){
		this.players = players;
		this.isThreePointMatch = isThreePointMatch;
	}

	List<Round>getRounds(){return rounds;}
	Player[] getPlayers(){return players;}
	Round getLatestRound(){return rounds.peek();}
	Match.STATUS getStatus(){return rounds.empty()? Match.STATUS.UNDEFINED : getLatestRound().getStatus();}

	public void save(Context context, String filename) throws IOException{
        if(rounds.size()>0) {
			if(filename==null) {
				filename = TEMP_FILENAME;
			}
            ObjectOutputStream o = new ObjectOutputStream(context.openFileOutput(filename, 0));
            o.writeObject(this);
            o.close();
        }
	}

    public static Game load(Context context) throws IOException, ClassNotFoundException{
		try {
			return load(context, TEMP_FILENAME);
		}catch(IOException e){
			return new Game();
		}
    }

	public static Game load(Context context, String filename) throws IOException, ClassNotFoundException{
		ObjectInputStream o = new ObjectInputStream(context.openFileInput(filename));
		Game game = (Game)o.readObject();
		game.restore();
		o.close();
		return game;
	}

	private static boolean hasIrregularMatch(Player p1, Player p2)
	{
		if(Match.isGapMatch(p1, p2))
		{
			return p1.hasGapMatch() || p2.hasGapMatch() || p1.hasBye() || p2.hasBye();
		}
		return false;
	}

	private static Player confirm(Stack<Player> players, Player target, boolean force) {
		for(Player player: players){
			if(!player.hasMatched(target)){
				if(force || !hasIrregularMatch(player, target)){
					if(force || !target.hasBye()) {
						players.remove(player);
						return player;
					}
				}
			}
		}
		return null;
	}

	public boolean make() {
		final int MAX_TRY_COUNT = 50;

        if (rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.MATCHING
                || rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.UNDEFINED) {
            rounds.pop();
        }
		for(int i=0; i<MAX_TRY_COUNT; ++i){
			Round ret = makeOne(new Player.Comparison(), false);
            if(ret!=null){
                rounds.push(ret);
                return true;
            }
		}
//		Toast.makeText(context, "通常ルールで組み合わせが作成できません。組み合わせ条件を緩和します。", Toast.LENGTH_LONG);

        for(int i=0; i<MAX_TRY_COUNT; ++i){
			Round ret = makeOne(new Player.ComparisonWinOnly(), true);
			if(ret!=null){
				rounds.push(ret);
				return true;
			}
		}

		rounds.push(new Round(rounds.size()+1));
        return false;
	}

	public long start(Context context)
	{
		long ret = getLatestRound().start();
		try{
			save(context, null);
		}catch(Exception e){
			Toast.makeText(context, "I/O error", Toast.LENGTH_LONG);
		}
		return ret;
	}

	private static Player[] shufflePlayer(Player[]players){
		List<Player>list = Arrays.asList(players.clone());
		Collections.shuffle(list);
		return list.toArray(new Player[list.size()]);
	}

	private Round makeOne(Comparator<Player> comp, boolean force) {
		int id = 0;
		Round ret = new Round(rounds.size()+1);
		Player[] temp = shufflePlayer(this.players);
		Arrays.sort(temp, comp);
		Stack<Player> stack = new Stack<Player>();
		for (Player player : temp) {
			if (!player.getDropped()) {
				if (stack.size() == 0) {
					stack.push(player);
				} else {
					Player p = confirm(stack, player, force);
					if (p != null) {
						Match m = new Match(++id, p, player);
						ret.add(m);
					} else {
						stack.push(player);
					}
				}
			}
		}
        if(stack.size()>1){
            return null;
        }
		if (stack.size() > 0) {
			Player p = stack.pop();
			if(p.byeAcceptable(force)){
				ret.add(new Match(++id, p,isThreePointMatch?2:1));
			}
			else{
				return null;
			}
		}
        return ret;
	}

	public void bind(Context context) throws IOException{
		getLatestRound().bind();
		save(context, null);
	}

	public long getStartTime(){
		return getLatestRound().getStartTime();
	}

	public void restore(){
		for(Round round : rounds) {
			round.restore(players);
		}
	}
	private static Player[] addPlayer(Player[] players, Player player){
		List<Player>temp = new ArrayList<Player>();
		temp.addAll(Arrays.asList(players));
		temp.add(player);
		return temp.toArray(new Player[0]);
	}

	public boolean addPlayer(String name){
		if(rounds.size()==1) {
			Player p = new Player(players.length, name);
			players = addPlayer(players,p);
			rounds.peek().add(p, isThreePointMatch?2:1);
		}
		return false;
	}

	public boolean isCurrentRound(int round){return rounds.size()-1 == round;}
}
