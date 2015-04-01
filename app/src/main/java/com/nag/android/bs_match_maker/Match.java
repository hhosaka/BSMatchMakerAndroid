package com.nag.android.bs_match_maker;

import java.io.Serializable;

class Match implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private final int PLAYER1 = 0;
	private final int PLAYER2 = 1;
	private static final Player BYE = new Player(0, "BYE");
	private int[] points = new int[2];
	private int id;
	private String label = null;
	private STATUS status;
	private Player[] players = new Player[2];

	public enum STATUS { UNDEF, MATCHING, READY, PLAYING, DONE };

	public Player[] getPlayers(){
		return players;
	}

	public STATUS getStatus(){
		return status;
	}

	public String getLabel(){
		if(label == null){
			label = points[0]+"-"+points[1];
		}
		return label;
	}

	public void Update(int player1_point, int player2_point) {
		points[PLAYER1] = player1_point;
		points[PLAYER2] = player2_point;
		label = null;
		status = STATUS.DONE;
	}

	public boolean isBYEGame() {
		return players[PLAYER2]==BYE;
	}

	Match(int id, Player player1, Player player2) {
		this.id = id;
		this.players[PLAYER1] = player1;
		this.players[PLAYER2] = player2;
		status = STATUS.MATCHING;
	}

	Match(int id, Player player, int fullpoint){
		this(id,player,BYE);
		this.points[0] = fullpoint;
		status = STATUS.DONE;
	}

	private static int GetMatchPoint(int player, int opponent) {
		if (player > opponent) {
			return 3;
		} else if (player < opponent) {
			return 0;
		} else {
			return 1;
		}
	}

	public boolean IsTarget(int player1_point, int player2_point) {
		return points[PLAYER1] == player1_point && points[PLAYER2] == player2_point;
	}

	private int MyIndex(Player player) {
		return this.players[PLAYER1] == player ? PLAYER1 : PLAYER2;
	}

	private int OpponentIndex(Player player) {
		return this.players[PLAYER1] == player ? PLAYER2 : PLAYER1;
	}

	private int GetMyPoint(Player player) {
		return this.points[MyIndex(player)];
	}

	public int GetOpponentMatchPoint(Player player) {
		return this.players[OpponentIndex(player)].getMatchPoint();
	}

	private int GetOpponentPoint(Player player) {
		return this.points[OpponentIndex(player)];
	}

	void bind() {
		for (Player player : players) {
			player.Bind(this);
		}
		status = isBYEGame()?STATUS.DONE:STATUS.READY;
	}

	public void start(){
		if(!isBYEGame()){
			status = STATUS.PLAYING;
		}
	}
	private String getSeparator(){
		switch(getStatus()){
		case DONE:
			return "["+ getLabel() + "]";
		case MATCHING:
		case READY:
		case PLAYING:
			return " - ";
		default:
			throw new UnsupportedOperationException();
		}
	}

	public String toString(){
		return id+")"+players[0].getName() + getSeparator() + players[1].getName();
	}

	public int getMatchPoint(Player player){
		return GetMatchPoint(GetMyPoint(player), GetOpponentPoint(player));
	}

	public int getWinPoint(Player player) {
		return GetMyPoint(player) - GetOpponentPoint(player);
	}

	public float getOpponentMatchPoint(Player player){
		return players[OpponentIndex(player)].getMatchPoint();
	}
}
