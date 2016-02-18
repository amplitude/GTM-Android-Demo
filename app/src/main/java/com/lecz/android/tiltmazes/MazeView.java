/*
 * Copyright (c) 2009, Balazs Lecz <leczbalazs@gmail.com>
 * Copyright (c) 2015, Amplitude Mobile Analytics
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *  * Neither the names of Balazs Lecz or Amplitude Mobile Analytics nor the names of
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class MazeView extends View {
    private boolean DEBUG = false;

    private static final float WALL_WIDTH = 5;

    private GameEngine mGameEngine;

    private Ball mBall;
    private float mBallX;
    private float mBallY;

    private int mWidth;
    private float mXMin;
    private float mYMin;
    private float mXMax;
    private float mYMax;
    private float mUnit;

    private int mMapWidth;
    private int mMapHeight;
    private int[][] mWalls;
    private int[][] mGoals;

    private Paint paint;
    private RadialGradient goalGradient = new RadialGradient(
            0, 0, 1,
            getResources().getColor(R.color.goal_highlight),
            getResources().getColor(R.color.goal_shadow),
            TileMode.MIRROR);
    private Matrix matrix = new Matrix();
    private Matrix scaleMatrix = new Matrix();

    private Timer mTimer;
    private long mT1 = 0;
    private long mT2 = 0;
    private int mDrawStep = 0;
    private int mDrawTimeHistorySize = 20;
    private long[] mDrawTimeHistory = new long[mDrawTimeHistorySize];


    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set up default Paint values
        paint = new Paint();
        paint.setAntiAlias(true);

        // Calculate geometry
        int w = getWidth();
        int h = getHeight();
        mWidth = Math.min(w, h);
        mXMin = WALL_WIDTH / 2;
        mYMin = WALL_WIDTH / 2;
        mXMax = Math.min(w, h) - WALL_WIDTH / 2;
        mYMax = mXMax;

        if (DEBUG) {
            // Schedule a redraw at 25 Hz
            TimerTask redrawTask = new TimerTask() {
                public void run() {
                    postInvalidate();
                }
            };
            mTimer = new Timer(true);
            mTimer.schedule(redrawTask, 0, 1000/*ms*/ / 25);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = Math.min(w, h);
        mXMax = Math.min(w, h) - WALL_WIDTH / 2;
        mYMax = mXMax;

        calculateUnit();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // FPS stats
        mT2 = SystemClock.elapsedRealtime();
        long dt = (mT2 - mT1);
        mT1 = mT2;
        mDrawTimeHistory[mDrawStep % mDrawTimeHistorySize] = dt;
        mDrawStep = mDrawStep + 1;

        mBall = mGameEngine.getBall();
        mMapWidth = mGameEngine.getMap().getSizeX();
        mMapHeight = mGameEngine.getMap().getSizeY();

        drawWalls(canvas);
        drawGoals(canvas);
        drawBall(canvas);

        if (DEBUG) {
            // Print FPS
            paint.setColor(Color.WHITE);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(1);
            canvas.drawText("FPS: " + getFPS(), 20, 30, paint);
        }
    }

    public void setGameEngine(GameEngine e) {
        mGameEngine = e;
    }

    public void calculateUnit() {
        if (mGameEngine == null) return;

        // Set up geometry
        float xUnit = ((mXMax - mXMin) / mGameEngine.getMap().getSizeX());
        float yUnit = ((mYMax - mYMin) / mGameEngine.getMap().getSizeY());
        mUnit = Math.min(xUnit, yUnit);
    }

    public double getFPS() {
        double avg = 0;
        int n = 0;
        for (long t : mDrawTimeHistory) {
            if (t > 0) {
                avg = avg + t;
                n = n + 1;
            }
        }
        if (n == 0) return -1;
        return 1000 * n / avg;
    }

    private void drawWalls(Canvas canvas) {
        paint.setColor(getResources().getColor(R.color.wall));
        paint.setStrokeWidth(WALL_WIDTH);
        paint.setStrokeCap(Cap.ROUND);

        mWalls = mGameEngine.getMap().getWalls();

        for (int y = 0; y < mMapHeight; y++) {
            for (int x = 0; x < mMapWidth; x++) {
                if ((mWalls[y][x] & Wall.TOP) > 0) {
                    canvas.drawLine(
                            mXMin + x * mUnit,
                            mYMin + y * mUnit,
                            mXMin + (x + 1) * mUnit,
                            mYMin + y * mUnit,
                            paint);
                }
                if ((mWalls[y][x] & Wall.RIGHT) > 0) {
                    canvas.drawLine(
                            mXMin + (x + 1) * mUnit,
                            mYMin + y * mUnit,
                            mXMin + (x + 1) * mUnit,
                            mYMin + (y + 1) * mUnit,
                            paint);
                }
                if ((mWalls[y][x] & Wall.BOTTOM) > 0) {
                    canvas.drawLine(
                            mXMin + x * mUnit,
                            mYMin + (y + 1) * mUnit,
                            mXMin + (x + 1) * mUnit,
                            mYMin + (y + 1) * mUnit,
                            paint);
                }
                if ((mWalls[y][x] & Wall.LEFT) > 0) {
                    canvas.drawLine(
                            mXMin + x * mUnit,
                            mYMin + y * mUnit,
                            mXMin + x * mUnit,
                            mYMin + (y + 1) * mUnit,
                            paint);
                }
            }
        }

        paint.setShader(null);
    }

    private void drawGoals(Canvas canvas) {
        paint.setShader(goalGradient);
        paint.setStyle(Style.FILL);
        scaleMatrix.setScale(mUnit, mUnit);

        mGoals = mGameEngine.getMap().getGoals();

        for (int y = 0; y < mMapHeight; y++) {
            for (int x = 0; x < mMapWidth; x++) {
                if (mGoals[y][x] > 0) {
                    matrix.setTranslate(
                            mXMin + x * mUnit,
                            mYMin + y * mUnit);
                    matrix.setConcat(matrix, scaleMatrix);
                    goalGradient.setLocalMatrix(matrix);
                    canvas.drawRect(
                            mXMin + x * mUnit + mUnit / 4,
                            mYMin + y * mUnit + mUnit / 4,
                            mXMin + (x + 1) * mUnit - mUnit / 4,
                            mYMin + (y + 1) * mUnit - mUnit / 4,
                            paint);
                }
            }
        }

        paint.setShader(null);
    }

    private void drawBall(Canvas canvas) {
        mBallX = mBall.getX();
        mBallY = mBall.getY();

        paint.setShader(new RadialGradient(
                mXMin + (mBallX + 0.55f) * mUnit,
                mYMin + (mBallY + 0.55f) * mUnit,
                mUnit * 0.35f,
                getResources().getColor(R.color.ball_highlight),
                getResources().getColor(R.color.ball_shadow),
                TileMode.MIRROR
        ));

        paint.setStyle(Style.FILL);
        canvas.drawCircle(
                mXMin + (mBallX + 0.5f) * mUnit,
                mYMin + (mBallY + 0.5f) * mUnit,
                mUnit * 0.4f,
                paint
        );
        paint.setShader(null);
    }
}
