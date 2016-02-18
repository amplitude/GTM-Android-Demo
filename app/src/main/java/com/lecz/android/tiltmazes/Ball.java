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

import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

public class Ball {
    private GameEngine mEngine;
    private Map mMap;
    private MazeView mMazeView;

    // Current position
    private float mX = 0;
    private float mY = 0;

    // Target position
    private int mXTarget;
    private int mYTarget;

    // Speed
    private int mVX = 0; // -1, 0 or 1
    private int mVY = 0; // -1, 0 or 1

    // On-screen speed
    private static float SPEED_MULTIPLIER = 0.005f;

    // Time
    private long mT1;
    private long mT2;
    private static final int DT_TARGET = 1000 / 25; // Target time step (ms)

    // Timer to schedule simulation steps
    private Timer mTimer;

    private boolean mIsRolling = false;
    private Direction mRollDirection = Direction.NONE;

    public Ball(GameEngine engine, Map map, int init_x, int init_y) {
        mEngine = engine;
        mMap = map;
        mX = init_x;
        mY = init_y;
        mXTarget = init_x;
        mYTarget = init_y;
    }

    public void setMazeView(MazeView mazeView) {
        mMazeView = mazeView;
    }

    public void setMap(Map map) {
        mMap = map;
    }

    public boolean isRolling() {
        return mIsRolling;
    }

    private boolean isValidMove(int x, int y, Direction dir) {
        switch (dir) {
            case LEFT:
                // Left wall
                if (x <= 0) return false;
                if ((mMap.getWalls(x, y) & Wall.LEFT) > 0 ||
                        (mMap.getWalls(x - 1, y) & Wall.RIGHT) > 0
                        ) return false;
                break;
            case RIGHT:
                // Right wall
                if (x >= mMap.getSizeX() - 1) return false;
                if ((mMap.getWalls(x, y) & Wall.RIGHT) > 0 ||
                        (mMap.getWalls(x + 1, y) & Wall.LEFT) > 0
                        ) return false;
                break;
            case UP:
                // Top wall
                if (y <= 0) return false;
                if ((mMap.getWalls(x, y) & Wall.TOP) > 0 ||
                        (mMap.getWalls(x, y - 1) & Wall.BOTTOM) > 0
                        ) return false;
                break;
            case DOWN:
                // Bottom wall
                if (y >= mMap.getSizeY() - 1) return false;
                if ((mMap.getWalls(x, y) & Wall.BOTTOM) > 0 ||
                        (mMap.getWalls(x, y + 1) & Wall.TOP) > 0
                        ) return false;
                break;
        }

        return true;
    }

    public synchronized boolean roll(Direction dir) {
        // Don't accept another roll command if the ball is already rolling
        if (mIsRolling) return false;

        // Set speed according to commanded direction
        switch (dir) {
            case LEFT: {
                mVX = -1;
                mVY = 0;
                break;
            }
            case RIGHT: {
                mVX = 1;
                mVY = 0;
                break;
            }
            case UP: {
                mVX = 0;
                mVY = -1;
                break;
            }
            case DOWN: {
                mVX = 0;
                mVY = 1;
                break;
            }
        }

        // Calculate target position
        mXTarget = Math.round(mX);
        mYTarget = Math.round(mY);
        while (isValidMove(mXTarget, mYTarget, dir)) {
            mXTarget = mXTarget + mVX;
            mYTarget = mYTarget + mVY;
        }

        // We can't move
        if (mXTarget == mX && mYTarget == mY) return false;

        // Let's roll...
        mIsRolling = true;
        mRollDirection = dir;

        // Schedule animation
        mT1 = SystemClock.elapsedRealtime();
        TimerTask simTask = new TimerTask() {
            public void run() {
                doStep();
            }
        };
        mTimer = new Timer(true);
        mTimer.schedule(simTask, 0, DT_TARGET);

        return true;
    }

    public void stop() {
        if (!mIsRolling) return;

        mTimer.cancel();
        mIsRolling = false;
        mX = mXTarget;
        mY = mYTarget;
        mMazeView.postInvalidate();
    }

    private void doStep() {
        // Calculate elapsed time since last step
        mT2 = SystemClock.elapsedRealtime();
        float dt = (float) (mT2 - mT1);
        mT1 = mT2;

        // Calculate next position
        float xNext = mX + mVX * SPEED_MULTIPLIER * dt;
        float yNext = mY + mVY * SPEED_MULTIPLIER * dt;

        // Check if we have reached the target position
        boolean reachedTarget = false;
        switch (mRollDirection) {
            case LEFT:
                if (xNext <= 1f * mXTarget) {
                    xNext = mXTarget;
                    reachedTarget = true;
                }
                break;
            case RIGHT:
                if (xNext >= 1f * mXTarget) {
                    xNext = mXTarget;
                    reachedTarget = true;
                }
                break;
            case UP:
                if (yNext <= 1f * mYTarget) {
                    yNext = mYTarget;
                    reachedTarget = true;
                }
                break;
            case DOWN:
                if (yNext > 1f * mYTarget) {
                    yNext = mYTarget;
                    reachedTarget = true;
                }
                break;
        }

        mX = xNext;
        mY = yNext;

        // Check if we have reached a goal
        if (mMap.getGoal(Math.round(mX), Math.round(mY)) == 1) {
            // FIXME(leczbalazs): maybe it's not the Ball's repsonibility to actually remove
            // the goal from the map
            mMap.removeGoal(Math.round(mX), Math.round(mY));
            mEngine.sendEmptyMessage(Messages.MSG_REACHED_GOAL);
        }

        // Stop rolling if we have reached the target position
        if (reachedTarget) {
            mRollDirection = Direction.NONE;
            mVX = 0;
            mVY = 0;
            mIsRolling = false;
            mTimer.cancel();

            // Send MSG_REACHED_WALL message to the parent View
            mEngine.sendEmptyMessage(Messages.MSG_REACHED_WALL);
        }

        // Send invalidate message to the parent View,
        // so that it gets redrawn during the next cycle.
        mMazeView.postInvalidate();
    }

    public Direction getRollDirection() {
        return mRollDirection;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void setX(float x) {
        mX = x;
    }

    public void setY(float y) {
        mY = y;
    }

    public void setXTarget(int x) {
        mXTarget = x;
    }

    public void setYTarget(int y) {
        mYTarget = y;
    }

    public float getXTarget() {
        return mXTarget;
    }

    public float getYTarget() {
        return mYTarget;
    }
}
