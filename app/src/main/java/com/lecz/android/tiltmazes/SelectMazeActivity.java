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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SelectMazeActivity extends ListActivity {
    private TiltMazesDBAdapter mDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDB = new TiltMazesDBAdapter(getApplicationContext()).open();

        setListAdapter(new CursorAdapter(getApplicationContext(), mDB.allMazes(), true) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                final View rowView = inflater.inflate(R.layout.select_maze_row_layout, parent, false);

                bindView(rowView, context, cursor);
                return rowView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final MapDesign m = MapDesigns.designList.get(cursor.getPosition());

                final ImageView mazeSolvedTickbox = (ImageView) view.findViewById(R.id.maze_solved_tick);
                final TextView mazeName = (TextView) view.findViewById(R.id.maze_name);
                final TextView mazeSolutionSteps = (TextView) view.findViewById(R.id.maze_solution_steps);

                if (cursor.getInt(TiltMazesDBAdapter.SOLUTION_STEPS_COLUMN) == 0) {
                    mazeSolvedTickbox.setImageResource(android.R.drawable.checkbox_off_background);
                    mazeSolutionSteps.setText("");
                } else {
                    mazeSolvedTickbox.setImageResource(android.R.drawable.checkbox_on_background);
                    mazeSolutionSteps.setText(
                            "Solved in "
                                    + cursor.getString(TiltMazesDBAdapter.SOLUTION_STEPS_COLUMN)
                                    + " steps");
                }

                mazeName.setText(
                        cursor.getString(TiltMazesDBAdapter.NAME_COLUMN)
                                + " (" + m.getSizeX() + "x" + m.getSizeY() + "), "
                                + m.getGoalCount() + " goal" + (m.getGoalCount() > 1 ? "s" : "")
                );
            }
        });
        setTitle(R.string.select_maze_title);
        setContentView(R.layout.select_maze_layout);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent result = new Intent();
        result.putExtra("selected_maze", position);
        setResult(RESULT_OK, result);
        finish();
    }
}
