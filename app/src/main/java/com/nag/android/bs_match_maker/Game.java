package com.nag.android.bs_match_maker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import com.nag.android.util.PreferenceHelper;

import android.content.Context;

public class Game implements Serializable{
	static final long serialVersionUID = 1L;
    private static final String TEMP_FILENAME = "auto_save";
	private static final String OPTION = ".bsmm";

    private String filename = null;
    public boolean isThreePointMatch = false;
    private Player[] players = null;
    private final Stack<Round> rounds = new Stack<Round>();

    enum UPDATE_MODE{DATA,ADD,CREATE}
	interface OnUpdateMatchListener{
		void updateMatch();
	}
	interface GameHolder{
		Game getGame();
		void update(UPDATE_MODE mode);
		void updateMatch();
		void setOnUpdateMatchListener(OnUpdateMatchListener listener);
	}

	public int getCount(){
		return rounds.size();
	}

	public Round getRound(int round){
		return rounds.get(round);
	}
	public Match.STATUS getStatus(){return getLatestRound().getStatus();}

	public void save(Context context,boolean isTemporary) throws IOException{
        if(rounds.size()>0) {
            String filename;
            if (isTemporary) {
                filename = TEMP_FILENAME;
            } else {
                if (this.filename == null) {
                    this.filename = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US).format(new Date());
                }
                filename = this.filename;
            }
            filename += OPTION;
            ObjectOutputStream o = new ObjectOutputStream(context.openFileOutput(filename, 0));
            o.writeObject(this);
            o.close();
        }
	}


    public static Game load(Context context) throws IOException, ClassNotFoundException{
        String filename = PreferenceHelper.getInstance(context).getString(TEMP_FILENAME,null);
        if(filename != null) {
            return load(context, filename);
        }
        return new Game();
    }

	public static Game load(Context context, String filename) throws IOException, ClassNotFoundException{
		ObjectInputStream o = new ObjectInputStream(context.openFileInput(filename));
		Game game = (Game)o.readObject();
		game.restore();
		o.close();
		return game;
	}

	Player[] getPlayers(){
		return players;
	}

    Game(){
        this(new Player[0],false);
    }

    Game(Player[]players, boolean isThreePointMatch){
        this.players = players;
        this.isThreePointMatch = isThreePointMatch;
    }

	private static Player Confirm(Stack<Player> stack,Stack<Player> temp, Player player) {
		while (stack.size() > 0) {
			Player ret = stack.pop();
			if (!ret.hasMatched(player)) {
				return ret;
			} else {
				temp.push(ret);
			}
		}
		return null;
	}

	private static Player Confirm(Stack<Player> stack, Player player) {
		Stack<Player> temp = new Stack<Player>();
		Player ret= Confirm(stack, temp, player);
		for(Player p : temp){
			stack.push(p);
		}
		return ret;
	}


	public boolean make() {
		final int MAX_TRY_COUNT = 10;

        if (rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.MATCHING
                || rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.UNDEF) {
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
					Player p = Confirm(stack, player);
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

	Round getLatestRound() {
		return rounds.peek();
	}

//	public void Reset() {
//		if (rounds.size() > 0) {
//			if (rounds.peek().getStatus() == Match.STATUS.MATCHING) {
//				rounds.pop();
//			}
//		}
//	}
	public void bind(Context context) throws IOException{
		getLatestRound().bind();
		save(context,true);
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
