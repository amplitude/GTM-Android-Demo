/*
 * Copyright (c) 2015, Amplitude
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
 *     * Neither the name of Amplitude nor the names of
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

import java.util.LinkedList;
import java.util.List;

import static com.lecz.android.tiltmazes.Wall.BOTTOM;
import static com.lecz.android.tiltmazes.Wall.LEFT;
import static com.lecz.android.tiltmazes.Wall.RIGHT;
import static com.lecz.android.tiltmazes.Wall.TOP;

public final class MapDesigns {

    public static final List<MapDesign> designList = new LinkedList<MapDesign>();

    static {
        designList.add(new MapDesign(
                "Small",
                5, 5,
                new int[][]{
                        {LEFT | TOP, TOP, TOP | RIGHT, TOP, TOP | RIGHT},
                        {LEFT, 0, 0, RIGHT, RIGHT},
                        {LEFT | BOTTOM, LEFT, BOTTOM | RIGHT, 0, RIGHT},
                        {LEFT, BOTTOM, LEFT, 0, RIGHT},
                        {LEFT | BOTTOM, BOTTOM, BOTTOM, BOTTOM, BOTTOM | RIGHT | TOP}
                },
                new int[][]{
                        {0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1}
                },
                0, 0
        ));
        designList.add(new MapDesign(
                "Medium",
                7, 7,
                new int[][]{
                        {LEFT | TOP, TOP, TOP, TOP, TOP | RIGHT, TOP, TOP | RIGHT},
                        {LEFT, 0, 0, 0, 0, RIGHT, RIGHT},
                        {LEFT, 0, TOP | LEFT | RIGHT, 0, 0, RIGHT, RIGHT},
                        {LEFT, LEFT, 0, 0, 0, RIGHT, RIGHT},
                        {LEFT | BOTTOM, 0, RIGHT, 0, BOTTOM, 0, RIGHT},
                        {LEFT, TOP, 0, 0, 0, 0, RIGHT},
                        {LEFT | BOTTOM, RIGHT | BOTTOM, BOTTOM, BOTTOM, BOTTOM, LEFT | BOTTOM, BOTTOM | RIGHT | TOP}
                },
                new int[][]{
                        {0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 1, 0, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0}
                },
                0, 0
        ));
    }

    private MapDesigns() {
        throw new AssertionError();
    }
}
