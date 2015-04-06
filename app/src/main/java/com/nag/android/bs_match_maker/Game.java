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

public class Game implements Serializable{
	static final long serialVersionUID = 1L;
	private static final String PROP_CURRENT_FILENAME = "current_filename";
	private static final String OPTION = ".bsmm";

    private static Random rand = new Random();

    private String filename;
    public boolean isThreePointMatch;
    private Player[] players = new Player[0];
    private Stack<Round> Rounds = new Stack<Round>();

	interface GameHolder{
		Game getGame();
		boolean update();
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



//	public Player[] sortByRank() {
//		return Player.OrderByRank(players);
//	}

	public void save(Context context) throws IOException{
        filename = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date())+OPTION;
        ObjectOutputStream o = new ObjectOutputStream(context.openFileOutput(filename, 0));
		o.writeObject(this);
		o.close();
		PreferenceHelper.getInstance(context).putString(PROP_CURRENT_FILENAME, filename);
	}

	public static Game load(Context context) throws IOException, ClassNotFoundException{
		return load(context, PreferenceHelper.getInstance(context).getLong(PROP_CURRENT_FILENAME, 0)+OPTION);
	}
	public static Game load(Context context, String filename) throws IOException, ClassNotFoundException{
		ObjectInputStream o = new ObjectInputStream(context.openFileInput(filename));// TODO
		Game game = (Game)o.readObject();
		o.close();
		return game;
	}

	Player[] getPlayers(){
		return players;
	}

	public void initial(Player[] players,boolean isThreePointMatch){
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

	public int make(int count) {
		for(int i=0; i<count; ++i){
			int ret = make();
			if(ret>0){
				return ret;
			}
		}
		return -1;
	}

	int make() {
		if (Rounds.size()>0 && getLatestRound().getStatus() == Match.STATUS.MATCHING) {
			Rounds.pop();
		}
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
		if (stack.size() > 0) {
			ret.add(new Match(++id, stack.pop(),isThreePointMatch?2:1));
		}
		Rounds.push(ret);
		return Rounds.size();
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
		save(context);
	}
}
