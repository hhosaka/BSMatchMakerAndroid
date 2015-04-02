package com.nag.android.bs_match_maker;

import java.io.IOException;

import com.nag.android.bs_match_maker.Match.STATUS;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Toast;

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
		}else{
			game = new Game();
		}
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
                if(position ==0 || position!=actionBar.getTabCount()-1){
                    findViewById(R.id.layoutButtons).setVisibility(View.GONE);
                }else{
                    findViewById(R.id.layoutButtons).setVisibility(View.VISIBLE);
                }
			}
		});

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	private void addTab(int round) {
		final ActionBar actionBar = getActionBar();
		actionBar.addTab(
				actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(round))
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
		}
		return super.onOptionsItemSelected(item);
	}

	private void openGame(){
		DataSelector.show(this, "Open Game", new DataSelector.ResultListener(){
			@Override
			public void onSelected(String filename) {
				if(filename!=null){
					try {
						game = Game.load(MainActivity.this, filename);
					} catch (ClassNotFoundException e) {
						Toast.makeText(MainActivity.this, "Corrupted data", Toast.LENGTH_LONG);
					} catch (IOException e) {
						Toast.makeText(MainActivity.this, "File IO error", Toast.LENGTH_LONG);
					}
				}
			}
		});
	}
	private void createGame() {
		View view = LayoutInflater.from(this).inflate(R.layout.view_inital, null);
		final NumberPicker np = (NumberPicker)view.findViewById(R.id.numberPickerPlayer);
		np.setMaxValue(MAX_PLAYER);
		np.setMinValue(MIN_PLAYER);
		np.setValue(8);// TODO it will be in preference
		final CheckBox cb = (CheckBox)view.findViewById(R.id.checkBoxIsThreePointMatch);
		new AlertDialog.Builder(this)
		.setTitle("Initial")
		.setView(view)
		.setNegativeButton("Cancel", null)
		.setPositiveButton("OK", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				game.initial(Player.create(np.getValue()),cb.isChecked());
				if(onupdateplayerslistener!=null){
					onupdateplayerslistener.onUpdatePlayer(game.getPlayers());
					addTab(game.make());
				}
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
				return "Player";
			}else{
				return "Round"+position;
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
	public boolean update() {
		this.onupdateplayerslistener.onUpdatePlayer(game.getPlayers());
		if(game.getLatestRound().getStatus()==STATUS.DONE){
			addTab(game.make());
			return true;
		}
		return false;
	}
}
