package com.nag.android.bs_match_maker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import android.content.Context;

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

	private static Player confirm(Stack<Player> players, Player target) {
		for(Player player: players){
			if(!player.hasMatched(target)){
				players.remove(player);
				return player;
			}
		}
		return null;
	}

	public boolean make() {
		final int MAX_TRY_COUNT = 10;

        if (rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.MATCHING
                || rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.UNDEFINED) {
            rounds.pop();
        }
		for(int i=0; i<MAX_TRY_COUNT; ++i){
			Round ret = makeOne();
            if(ret!=null){
                rounds.push(ret);
                return true;
            }
		}
        rounds.push(new Round(rounds.size()+1));
        return false;
	}

	public long start(){
		return getLatestRound().start();
	}

	private static Player[] shufflePlayer(Player[]players){
		List<Player>list = Arrays.asList(players.clone());
		Collections.shuffle(list);
		return list.toArray(new Player[list.size()]);
	}

	private Round makeOne() {
		int id = 0;
		Round ret = new Round(rounds.size()+1);
		Player[] temp = shufflePlayer(this.players);
		Arrays.sort(temp, new Player.Comparison());
		Stack<Player> stack = new Stack<Player>();
		for (Player player : temp) {
			if (!player.getDropped()) {
				if (stack.size() == 0) {
					stack.push(player);
				} else {
					Player p = confirm(stack, player);
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
			ret.add(new Match(++id, stack.pop(),isThreePointMatch?2:1));
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
}
