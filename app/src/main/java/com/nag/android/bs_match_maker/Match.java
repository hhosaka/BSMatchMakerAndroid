package com.nag.android.bs_match_maker;

import java.io.Serializable;

class Match implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private final int PLAYER1 = 0;
	private final int PLAYER2 = 1;
	private static final Player BYE = new Player(0, "BYE");
	private int id;
	private String label = null;
	private STATUS status;
	private Player[] players = new Player[2];
    private Result result = new Result();

	public enum STATUS { UNDEF, MATCHING, READY, PLAYING, DONE };

	public Player[] getPlayers(){
		return players;
	}

	public STATUS getStatus(){
		return status;
	}

    public Result getResult(){
        return result;
    }
	public void Update(Result result) {
        this.result = result;
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
        this.result = new Result(fullpoint,0);
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

	private int MyIndex(Player player) {
		return this.players[PLAYER1] == player ? PLAYER1 : PLAYER2;
	}

	private int OpponentIndex(Player player) {
		return this.players[PLAYER1] == player ? PLAYER2 : PLAYER1;
	}

	private int GetMyPoint(Player player) {
		return this.result.getPoint(MyIndex(player));
	}

	public int GetOpponentMatchPoint(Player player) {
		return this.players[OpponentIndex(player)].getMatchPoint();
	}

	private int GetOpponentPoint(Player player) {
		return this.result.getPoint(OpponentIndex(player));
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
			return "["+ result.toString() + "]";
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
