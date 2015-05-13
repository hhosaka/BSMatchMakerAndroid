package com.nag.android.bs_match_maker;

/**
 * Created by ddiamond on 2015/05/13.
 */
public interface AppCore {
	enum UPDATE_MODE{DATA,ADD,CREATE}
	interface OnUpdateMatchListener {
		void updateMatch();
	}
	interface OnUpdatePlayersListener {
		void onUpdatePlayer(Player[]player);
	}

	Game getGame();
	void updateMatch();
	void setOnUpdateMatchListener(OnUpdateMatchListener listener);
	void setOnUpdatePlayersListener(OnUpdatePlayersListener listener);
	void updatePlayer(UPDATE_MODE mode);


}
