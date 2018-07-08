package com.nag.android.bs_match_maker;

/**
 * Created by ddiamond on 2015/05/13.
 */
public interface AppCore {
	interface OnUpdateMatchListener {
		void updateMatch();
	}
	interface OnUpdatePlayersListener {
		void onUpdatePlayer(Player[]player);
	}

	Game getGame();
	void makeMatch();
	void updateMatch();
	void setOnUpdateMatchListener(OnUpdateMatchListener listener);
	void setOnUpdatePlayersListener(OnUpdatePlayersListener listener);
	void updatePlayer();
}
