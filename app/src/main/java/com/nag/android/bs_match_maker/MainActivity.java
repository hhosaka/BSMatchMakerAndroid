package com.nag.android.bs_match_maker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.nag.android.util.PreferenceHelper;

public class MainActivity extends Activity implements ActionBar.TabListener,
														AppCore{
	private static final String ARG_GAME = "game";
	private static final String PREF_DEFAULT_NUMBER_OF_PLAYER = "default_number_of_player";
	private static final String PREF_DEFAULT_IS_THREE_POINT_MATCH = "default_is_three_point_match";
	private final static String PREF_PLAYER_PREFIX = "player_prefix";

	private OnUpdatePlayersListener onupdateplayerslistener = null;
	private OnUpdateMatchListener onUpdateMatchListener = null;
	private SectionsPagerAdapter adapter = null;
	private ViewPager pager = null;
	private Game game = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(savedInstanceState!=null){
			game = (Game)savedInstanceState.getSerializable(ARG_GAME);
			game.restore();
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
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		pager = (ViewPager) findViewById(R.id.pager);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
				handleNavigationButtons(position, getActionBar());
			}
		});
        updatePlayer(UPDATE_MODE.CREATE);
	}

	private void handleNavigationButtons(int position, ActionBar actionBar) {
		if (position == 0 || position != actionBar.getTabCount() - 1) {
			findViewById(R.id.layoutButtons).setVisibility(View.GONE);
		} else {
			findViewById(R.id.layoutButtons).setVisibility(View.VISIBLE);
		}
	}

	private void addTab() {
		final ActionBar actionBar = getActionBar();
		actionBar.addTab(
				actionBar.newTab()
						.setText(adapter.getPageTitle(actionBar.getTabCount()))
						.setTabListener(this));
        adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.action_help:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_help))));
			break;
		case R.id.action_initial:
			createGame();
			return false;
		case R.id.action_open:
            openGame();
            return false;
        case R.id.action_save:
            try {
                game.save(this, new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US).format(new Date()));
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
                        updatePlayer(UPDATE_MODE.CREATE);
					} catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	}

	private void createGame() {
		final int MAX_PLAYER = 128;
		final int MIN_PLAYER = 2;

		View view = LayoutInflater.from(this).inflate(R.layout.view_inital, null);// TODO how should I handle 2nd parameter
		final NumberPicker np = (NumberPicker)view.findViewById(R.id.numberPickerPlayer);
		np.setMaxValue(MAX_PLAYER);
		np.setMinValue(MIN_PLAYER);
		np.setValue(PreferenceHelper.getInstance(this).getInt(PREF_DEFAULT_NUMBER_OF_PLAYER, 8));
		final CheckBox cb = (CheckBox)view.findViewById(R.id.checkBoxIsThreePointMatch);
        cb.setChecked(PreferenceHelper.getInstance(this).getBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, false));
		final TextView prefix = (EditText)view.findViewById(R.id.editTextPlayerPrefix);
		prefix.setText(PreferenceHelper.getInstance(this).getString(PREF_PLAYER_PREFIX,getString(R.string.label_player_prefix_default)));
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.action_initial))
		.setView(view)
		.setNegativeButton(getString(R.string.label_cancel), null)
		.setPositiveButton(getString(R.string.label_ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PreferenceHelper.getInstance(MainActivity.this).putInt(PREF_DEFAULT_NUMBER_OF_PLAYER, np.getValue());
				PreferenceHelper.getInstance(MainActivity.this).putBoolean(PREF_DEFAULT_IS_THREE_POINT_MATCH, cb.isChecked());
				PreferenceHelper.getInstance(MainActivity.this).putString(PREF_PLAYER_PREFIX, prefix.getText().toString());
				game = new Game(Player.create(prefix.getText().toString(), np.getValue()), cb.isChecked());
				if (!game.make()) {
					Toast.makeText(MainActivity.this, getString(R.string.message_round_create_error), Toast.LENGTH_LONG).show();
				}
				updatePlayer(UPDATE_MODE.CREATE);
			}
		})
		.show();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		pager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// DO Nothing
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// DO Nothing
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if(position==0){
				return PlayerFragment.newInstance();
			}else{
				return MatchFragment.newInstance(position-1);
			}
		}

		@Override
		public int getCount() {
			return game.getRounds().size()+1;
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
	public Game getGame() {
		return game;
	}

	@Override
	public void setOnUpdatePlayersListener(OnUpdatePlayersListener listener) {
		this.onupdateplayerslistener = listener;
	}

	@Override
	public void updatePlayer(AppCore.UPDATE_MODE mode) {
		Player.updateRank(game.getPlayers());
		if(onupdateplayerslistener!=null){
            onupdateplayerslistener.onUpdatePlayer(game.getPlayers());
        }
        switch(mode){
            case ADD:
                addTab();
				pager.setCurrentItem(adapter.getCount() - 1);
                break;
            case CREATE:
                final ActionBar actionBar = getActionBar();
				assert(actionBar!=null);
                actionBar.removeAllTabs();
                adapter = new SectionsPagerAdapter(getFragmentManager());
                pager.setAdapter(adapter);
                for(int i=0;i<game.getRounds().size()+1;++i){
                    addTab();
                }
                break;
			default:
				// Do Nothing
        }
	}

	@Override
	public void setOnUpdateMatchListener(OnUpdateMatchListener listener) {
		this.onUpdateMatchListener = listener;
	}

	@Override
	public void updateMatch() {
		if(onUpdateMatchListener != null) {
			onUpdateMatchListener.updateMatch();
		}
	}
}
