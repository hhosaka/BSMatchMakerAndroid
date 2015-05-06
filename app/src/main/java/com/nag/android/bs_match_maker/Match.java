package com.nag.android.bs_match_maker;

import java.io.Serializable;

class Match implements Serializable{
	private static final long serialVersionUID = Game.serialVersionUID;
	private static final int PLAYER1 = 0;
	private static final int PLAYER2 = 1;
    private static final int PLAYER_ID_BYE = -1;
	private static final Player BYE = new Player(PLAYER_ID_BYE, "BYE");
	private final int id;
	private STATUS status;
    private transient Player[] players;
    private final int playerId[];
    private Result result = new Result();

	public enum STATUS {UNDEFINED, MATCHING, READY, PLAYING, DONE }

	public Player[] getPlayers(){
		return players;
	}

	public STATUS getStatus(){
		return status;
	}

    public Result getResult(){
        return result;
    }
	public void update(Result result) {
        this.result = result;
		status = STATUS.DONE;
	}

	public boolean isBYEGame() {
		return players[PLAYER2]==BYE;
	}

	Match(int id, Player player1, Player player2) {
		this.id = id;
        players = new Player[2];
        playerId = new int[2];
        this.players[PLAYER1] = player1;
        this.playerId[PLAYER1]=player1.id;
		this.players[PLAYER2] = player2;
        this.playerId[PLAYER2]=player2.id;
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

//	public int GetOpponentMatchPoint(Player player) {
//		return this.players[OpponentIndex(player)].getMatchPoint();
//	}

    private Player getOpponent(Player player){return players[OpponentIndex(player)];}

    private String getResultMark(Player player){
        int point = result.getDiff();
        if(point==0){
            return "△";
        }else if(point>0 && player==players[PLAYER1]
                ||point<0 && player==players[PLAYER2]){
            return "○";
        }else{
            return "×";
        }
    }

    public String getLogString(Player player){
        return getResultMark(player) + "-"+getOpponent(player).getName();
    }

	private int getOpponentPoint(Player player) {
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
		return GetMatchPoint(GetMyPoint(player), getOpponentPoint(player));
	}

	public int getWinPoint(Player player) {
		return GetMyPoint(player) - getOpponentPoint(player);
	}

	public float getOpponentMatchPoint(Player player){
		return players[OpponentIndex(player)].getMatchPoint();
	}

    private static Player getStoredPlayer(int id, Player[]players){
        if(id == PLAYER_ID_BYE){
            return BYE;
        }else {
            for (Player player : players) {
                if (id == player.id) {
                    return player;
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    public void restore(Player[]players){
        this.players=new Player[2];
        for(int i=0; i<2; ++i){
            this.players[i] = getStoredPlayer(playerId[i], players);
        }
    }
}
