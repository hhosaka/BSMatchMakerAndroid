package com.nag.android.bs_match_maker;

interface GameHolder {
	enum UPDATE_MODE{DATA,ADD,CREATE}
	interface OnUpdateMatchListener{
		void updateMatch();
	}
	Game getGame();
	void update(UPDATE_MODE mode);
	void updateMatch();
	void setOnUpdateMatchListener(OnUpdateMatchListener listener);
}
