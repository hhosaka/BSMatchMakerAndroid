package com.nag.android.bs_match_maker;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.nag.android.util.PreferenceHelper;

public class MainActivity extends Activity implements ActionBar.TabListener,
														Game.GameHolder,
														PlayerFragment.PlayersObserver{
	private final int MAX_PLAYER = 128;
	private final int MIN_PLAYER = 2;
	private static final String ARG_GAME = "game";
	private OnUpdatePlayersListener onupdateplayerslistener;
//    Button buttonFix;
//    Button buttonStart;
//    Button buttonShuffle;

	private Game game = null;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(savedInstanceState!=null){
			game = (Game)savedInstanceState.getSerializable(ARG_GAME);
			if(game.getLatestRound().getStatus()== Match.STATUS.PLAYING){
				((TimerView)findViewById(R.id.TimerTextTimer)).start(game.getStartTime());
			}
		}else{
            try {
                game = Game.load(this);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                game = new Game();
            }
        }
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
//		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				if (position == 0 || position != actionBar.getTabCount() - 1) {
					findViewById(R.id.layoutButtons).setVisibility(View.GONE);
				} else {
					findViewById(R.id.layoutButtons).setVisibility(View.VISIBLE);
				}
			}
		});

        update(Game.UPDATE_MODE.CREATE);
//		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//			actionBar.addTab(
//					actionBar.newTab()
//					.setText(mSectionsPagerAdapter.getPageTitle(i))
//					.setTabListener(this));
//		}
	}

	private void addTab() {
		final ActionBar actionBar = getActionBar();
		actionBar.addTab(
				actionBar.newTab()
						.setText(mSectionsPagerAdapter.getPageTitle(actionBar.getTabCount()))
						.setTabListener(this));
        mSectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.action_settings:
			break;
		case R.id.action_initial:
			createGame();
			return false;
		case R.id.action_open:
            openGame();
            return false;
        case R.id.action_save:
            try {
                game.save(this, false);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return false;
        case R.id.action_delete:
            DataDeleter.show(this,getString(R.string.action_delete));
            return false;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openGame(){
		DataSelector.show(this, getString(R.string.action_open), new DataSelector.ResultListener(){
			@Override
			public void onSelected(String filename) {
				if(filename!=null){
					try {
						game = Game.load(MainActivity.this, filename);
                        update(Game.UPDATE_MODE.CREATE);
					} catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

    private static final String PREF_DEFAULT_NUMBER_OF_PLAYER = "default_number_of_player";
    private static final String PREF_DEFAULT_IS_THREE_POINT_MATCH = "default_is_three_point_match";

	private final String PREF_PLAYER_PREFIX = "player_prefix";
	
	private String getPlayerPrefix(){
		return PreferenceHelper.getInstance(this).getString(PREF_PLAYER_PREFIX,getString(R.string.label_player_prefix));
	}
	private void createGame() {
		View view = LayoutInflater.from(this).inflate(R.layout.view_inital, null);
		final NumberPicker np = (NumberPicker)view.findViewById(R.id.numberPickerPlayer);
		np.setMaxValue(MAX_PLAYER);
		np.setMinValue(MIN_PLAYER);
		np.setValue(PreferenceHelper.getInstance(this).getInt(PREF_DEFAULT_NUMBER_OF_PLAYER, 8));// TODO it will be in preference
		final CheckBox cb = (CheckBox)view.findViewById(R.id.checkBoxIsThreePointMatch);
        cb.setChecked(PreferenceHelper.getInstance(this).getBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, false));
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.action_initial))
		.setView(view)
		.setNegativeButton(getString(R.string.label_cancel), null)
		.setPositiveButton(getString(R.string.label_ok), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
                PreferenceHelper.getInstance(MainActivity.this).putInt(PREF_DEFAULT_NUMBER_OF_PLAYER, np.getValue());
                PreferenceHelper.getInstance(MainActivity.this).putBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, cb.isChecked());
				game = new Game(Player.create(getPlayerPrefix(), np.getValue()),cb.isChecked());
                if(!game.make()) {
                    Toast.makeText(MainActivity.this,getString(R.string.message_round_create_error),Toast.LENGTH_LONG).show();
                }
                update(Game.UPDATE_MODE.CREATE);
			}
		})
		.show();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if(position==0){
				return PlayerFragment.newInstance(game.getPlayers());
			}else{
				return MatchFragment.newInstance(position-1);
			}
		}

		@Override
		public int getCount() {
			return game.getCount()+1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if(position==0){
				return getString(R.string.label_player);
			}else{
				return getString(R.string.label_round)+position;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(ARG_GAME, game);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void setOnUpdatePlayersListener(OnUpdatePlayersListener listener) {
		this.onupdateplayerslistener = listener;
	}

	@Override
	public Game getGame() {
		return game;
	}

	@Override
	public void update(Game.UPDATE_MODE mode) {
	if(onupdateplayerslistener!=null){
            onupdateplayerslistener.onUpdatePlayer(game.getPlayers());
        }
        switch(mode){
            case ADD:
                addTab();
				mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount()-1);
                break;
            case CREATE:
                final ActionBar actionBar = getActionBar();
                actionBar.removeAllTabs();
                mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
                mViewPager.setAdapter(mSectionsPagerAdapter);
                for(int i=0;i<game.getCount()+1;++i){
                    addTab();
                }
                break;
        }
	}
}
