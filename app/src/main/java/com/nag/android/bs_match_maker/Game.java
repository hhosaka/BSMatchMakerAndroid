package com.nag.android.bs_match_maker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

import com.nag.android.util.PreferenceHelper;

import android.content.Context;

public class Game implements Serializable{
	static final long serialVersionUID = 1L;
	private static final String PROP_DEFAULT_FILENAME = "defaul_filename";
	private static final String OPTION = ".bsmm";

	interface GameHolder{
		Game getGame();
		boolean update();
	}

	private long id = 0;
	private Player[] players = new Player[0];
	private Stack<Round> Rounds = new Stack<Round>();
	
	public int getCount(){
		return Rounds.size();
	}
//	public Match[] getMatches(int round){
//		return Rounds.get(round).getMatches();
//	}

	public Round getRound(int round){
		return Rounds.get(round);
	}
//	private List<Match[]> matches=new ArrayList<Match[]>();

	public interface OnUpdatePlayerListener{
		void onUpdatePlayer(Player[] players);
		void onUpdatePlayer();
	}

	public boolean isThreePointMatch;
	private static Random rand = new Random();


//	public Player[] sortByRank() {
//		return Player.OrderByRank(players);
//	}

	public Game() {
		id = System.currentTimeMillis();
	}

	public static void save(Context context, Game game) throws IOException{
		ObjectOutputStream o = new ObjectOutputStream(context.openFileOutput(String.valueOf(game.id)+OPTION, 0));// TODO
		o.writeObject(game);
		o.close();
		PreferenceHelper.getInstance(context).putLong(PROP_DEFAULT_FILENAME, game.id);
	}

	public static Game load(Context context) throws IOException, ClassNotFoundException{
		return load(context, PreferenceHelper.getInstance(context).getLong(PROP_DEFAULT_FILENAME, 0)+OPTION);
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

//	public int make(){
//		matches.add(new Match[]{new Match(0, new Player(0, "player1"), new Player(1, "player2")),
//			new Match(1, new Player(2, "player3"), new Player(3, "player4")),
//			new Match(2, new Player(4, "player5"), new Player(5, "player6"))});
//		return matches.size();
//	}

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
		Game.save(context, this);
	}
}
