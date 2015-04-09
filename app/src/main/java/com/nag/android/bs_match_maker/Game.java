package com.nag.android.bs_match_maker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

import com.nag.android.util.PreferenceHelper;

import android.content.Context;
import android.util.Log;

public class Game implements Serializable{
	static final long serialVersionUID = 1L;
	private static final String PROP_CURRENT_FILENAME = "current_filename";
    private static final String TEMP_FILENAME = "auto_save";
	private static final String OPTION = ".bsmm";

    private static Random rand = new Random();

    private String filename = null;
    public boolean isThreePointMatch = false;
    private Player[] players = null;
    private Stack<Round> Rounds = new Stack<Round>();

    enum UPDATE_MODE{DATA,ADD,CREATE};
	interface GameHolder{
		Game getGame();
		void update(UPDATE_MODE mode);
	}

	public int getCount(){
		return Rounds.size();
	}

	public Round getRound(int round){
		return Rounds.get(round);
	}

	public interface OnUpdatePlayerListener{
		void onUpdatePlayer(Player[] players);
		void onUpdatePlayer();
	}

	public void save(Context context,boolean isTemporary) throws IOException{
        if(Rounds.size()>0) {
            String filename;
            if (isTemporary) {
                filename = TEMP_FILENAME;
            } else {
                if (this.filename == null) {
                    this.filename = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date());
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
		ObjectInputStream o = new ObjectInputStream(context.openFileInput(filename));// TODO
		Game game = (Game)o.readObject();
        for(Round round:game.Rounds){
            for(Match match:round.getMatches()){
                match.restore(game.getPlayers());
            }
        }
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

	private static int randomswap(Player a, Player b) {
		return rand.nextInt(3)-1;
	}

	class RandomSwap implements Comparator<Player>{
		@Override
		public int compare(Player lhs, Player rhs) {
			return randomswap(lhs, rhs);
		}
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

    private final int MAX_TRY_COUNT = 10;

	public boolean make() {
        if (Rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.MATCHING
                ||Rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.UNDEF) {
            Rounds.pop();
        }
		for(int i=0; i<MAX_TRY_COUNT; ++i){
			Round ret = makeOne();
            if(ret!=null){
                Rounds.push(ret);
                return true;
            }
		}
        Rounds.push(new Round(Rounds.size()+1));
        return false;
	}

	private Round makeOne() {
		int id = 0;
		Round ret = new Round(Rounds.size()+1);
		Arrays.sort(players,new RandomSwap());
		Arrays.sort(players, new Player.Comparison());
		Stack<Player> stack = new Stack<Player>();
		for (Player player : players) {
			if (!player.Dropped) {
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
		return Rounds.peek();
	}
	public void Reset() {
		if (Rounds.size() > 0) {
			if (Rounds.peek().getStatus() == Match.STATUS.MATCHING) {
				Rounds.pop();
			}
		}
	}
	public void bind(Context context) throws IOException{
		getLatestRound().bind();
		save(context,true);
	}
}
