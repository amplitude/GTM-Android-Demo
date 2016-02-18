/*
 * Copyright (c) 2009, Balazs Lecz <leczbalazs@gmail.com>
 * Copyright (c) 2015, Amplitude Mobile Analytics
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the names of Balazs Lecz or Amplitude Mobile Analytics nor the names of
 *       contributors may be used to endorse or promote products derived from this
 *       software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.lecz.android.tiltmazes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.amplitude.api.Amplitude;

public class TiltMazesActivity extends Activity {
    protected PowerManager.WakeLock mWakeLock;

    private MazeView mMazeView;

    private static final int MENU_RESTART = 1;
    private static final int MENU_MAP_PREV = 2;
    private static final int MENU_MAP_NEXT = 3;
    private static final int MENU_SENSOR = 4;
    private static final int MENU_SELECT_MAZE = 5;
    private static final int MENU_ABOUT = 6;

    private static final int REQUEST_SELECT_MAZE = 1;

    private Dialog mAboutDialog;

    private Intent mSelectMazeIntent;

    private TextView mMazeNameLabel;
    private TextView mRemainingGoalsLabel;
    private TextView mStepsLabel;

    private GestureDetector mGestureDetector;
    private GameEngine mGameEngine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Amplitude.getInstance().initialize(this, "2bc81f5feed9ab046f7fbaf6c40fe1b6");
        Amplitude.getInstance().enableForegroundTracking(getApplication()).trackSessionEvents(true);
        Amplitude.getInstance().setLogLevel(Log.VERBOSE);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TiltMazes");

        mSelectMazeIntent = new Intent(TiltMazesActivity.this, SelectMazeActivity.class);

        // Build the About Dialog
        mAboutDialog = new Dialog(TiltMazesActivity.this);
        mAboutDialog.setCancelable(true);
        mAboutDialog.setCanceledOnTouchOutside(true);
        mAboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mAboutDialog.setContentView(R.layout.about_layout);

        Button aboutDialogOkButton = (Button) mAboutDialog.findViewById(R.id.about_ok_button);
        aboutDialogOkButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mAboutDialog.cancel();
            }
        });

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.game_layout);

        // Show the About Dialog on the first start
        if (getPreferences(MODE_PRIVATE).getBoolean("firststart", true)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("firststart", false).commit();
            mAboutDialog.show();
        }

        // Set up game engine and connect it with the relevant views
        mGameEngine = new GameEngine(TiltMazesActivity.this);
        mMazeView = (MazeView) findViewById(R.id.maze_view);
        mGameEngine.setTiltMazesView(mMazeView);
        mMazeView.setGameEngine(mGameEngine);
        mMazeView.calculateUnit();

        mMazeNameLabel = (TextView) findViewById(R.id.maze_name);
        mGameEngine.setMazeNameLabel(mMazeNameLabel);
        mMazeNameLabel.setText(mGameEngine.getMap().getName());
        mMazeNameLabel.invalidate();

        mRemainingGoalsLabel = (TextView) findViewById(R.id.remaining_goals);
        mGameEngine.setRemainingGoalsLabel(mRemainingGoalsLabel);

        mStepsLabel = (TextView) findViewById(R.id.steps);
        mGameEngine.setStepsLabel(mStepsLabel);

        mGameEngine.restoreState(
                savedInstanceState,
                getPreferences(MODE_PRIVATE).getBoolean("sensorenabled", true)
        );


        // Create gesture detector to detect flings
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                // Roll the ball in the direction of the fling
                Direction mCommandedRollDirection = Direction.NONE;

                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX < 0) mCommandedRollDirection = Direction.LEFT;
                    else mCommandedRollDirection = Direction.RIGHT;
                } else {
                    if (velocityY < 0) mCommandedRollDirection = Direction.UP;
                    else mCommandedRollDirection = Direction.DOWN;
                }

                if (mCommandedRollDirection != Direction.NONE) {
                    mGameEngine.rollBall(mCommandedRollDirection);
                }

                return true;
            }
        });
        mGestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mGameEngine.rollBall(Direction.LEFT);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mGameEngine.rollBall(Direction.RIGHT);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                mGameEngine.rollBall(Direction.UP);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mGameEngine.rollBall(Direction.DOWN);
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_MAP_PREV, 0, R.string.menu_map_prev);
        menu.add(0, MENU_RESTART, 0, R.string.menu_restart);
        menu.add(0, MENU_MAP_NEXT, 0, R.string.menu_map_next);
        menu.add(0, MENU_SENSOR, 0, R.string.menu_sensor);
        menu.add(0, MENU_SELECT_MAZE, 0, R.string.menu_select_maze);
        menu.add(0, MENU_ABOUT, 0, R.string.menu_about);

        menu.findItem(MENU_MAP_PREV).setIcon(getResources().getDrawable(android.R.drawable.ic_media_previous));
        menu.findItem(MENU_RESTART).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_rotate));
        menu.findItem(MENU_MAP_NEXT).setIcon(getResources().getDrawable(android.R.drawable.ic_media_next));
        menu.findItem(MENU_SENSOR).setIcon(getResources().getDrawable(
                mGameEngine.isSensorEnabled() ? android.R.drawable.button_onoff_indicator_on : android.R.drawable.button_onoff_indicator_off
        ));
        menu.findItem(MENU_SELECT_MAZE).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_more));
        menu.findItem(MENU_ABOUT).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTART:
                mGameEngine.sendEmptyMessage(Messages.MSG_RESTART);
                return true;

            case MENU_MAP_PREV:
                mGameEngine.sendEmptyMessage(Messages.MSG_MAP_PREVIOUS);
                return true;

            case MENU_MAP_NEXT:
                mGameEngine.sendEmptyMessage(Messages.MSG_MAP_NEXT);
                return true;

            case MENU_SENSOR:
                mGameEngine.toggleSensorEnabled();
                item.setIcon(getResources().getDrawable(
                        mGameEngine.isSensorEnabled() ? android.R.drawable.button_onoff_indicator_on : android.R.drawable.button_onoff_indicator_off
                ));
                getPreferences(MODE_PRIVATE).edit().putBoolean("sensorenabled", mGameEngine.isSensorEnabled()).commit();
                return true;

            case MENU_SELECT_MAZE:
                startActivityForResult(mSelectMazeIntent, REQUEST_SELECT_MAZE);
                return true;

            case MENU_ABOUT:
                mAboutDialog.show();
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (REQUEST_SELECT_MAZE):
                if (resultCode == Activity.RESULT_OK) {
                    int selectedMaze = data.getIntExtra("selected_maze", 0);
                    mGameEngine.loadMap(selectedMaze);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mGameEngine.unregisterListener();
        mWakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Amplitude.getInstance().logEvent("app resumed");

        mGameEngine.registerListener();
        mWakeLock.acquire();
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        mGameEngine.saveState(icicle);
        mGameEngine.unregisterListener();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGameEngine.restoreState(
                savedInstanceState,
                getPreferences(MODE_PRIVATE).getBoolean("sensorenabled", true)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
