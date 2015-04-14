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

public class Map {
    private MapDesign mDesign;

    private int[][] mGoals;
    private int mGoalCount;

    public Map(MapDesign design) {
        mDesign = design;
        init();
    }

    public void init() {
        if (mGoals == null) mGoals = new int[mDesign.getSizeY()][mDesign.getSizeX()];

        int[][] goals = mDesign.getGoals();
        for (int y = 0; y < mDesign.getSizeY(); y++)
            for (int x = 0; x < mDesign.getSizeX(); x++)
                mGoals[y][x] = goals[y][x];

        mGoalCount = mDesign.getGoalCount();
    }

    public String getName() {
        return mDesign.getName();
    }

    public int[][] getWalls() {
        return mDesign.getWalls();
    }

    public int getWalls(int x, int y) {
        return mDesign.getWalls(x, y);
    }

    public int[][] getGoals() {
        return mGoals;
    }

    public int getGoal(int x, int y) {
        return mGoals[y][x];
    }

    public void removeGoal(int x, int y) {
        mGoalCount = mGoalCount - mGoals[y][x];
        mGoals[y][x] = 0;
    }

    public void setGoal(int x, int y, int value) {
        mGoalCount = mGoalCount - (mGoals[y][x] - value);
        mGoals[y][x] = value;
    }

    public int getSizeX() {
        return mDesign.getSizeY();
    }

    public int getSizeY() {
        return mDesign.getSizeY();
    }

    public int getInitialPositionX() {
        return mDesign.getInitialPositionX();
    }

    public int getInitialPositionY() {
        return mDesign.getInitialPositionY();
    }

    public int getGoalCount() {
        return mGoalCount;
    }
}
