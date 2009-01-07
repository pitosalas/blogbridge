// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software Foundation;
// either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program;
// if not, write to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA
//
// Contact: R. Pito Salas
// mailto:pitosalas@users.sourceforge.net
// More information: about BlogBridge
// http://www.blogbridge.com
// http://sourceforge.net/projects/blogbridge
//
// $Id: TextRange.java,v 1.6 2007/02/06 15:33:01 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.utils.Constants;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple holder of text range.
 */
public class TextRange
{
    private int start;
    private int end;

    /**
     * Creates range.
     *
     * @param aStart    start of range.
     * @param aEnd      end of range.
     */
    public TextRange(int aStart, int aEnd)
    {
        start = aStart;
        end = aEnd;
    }

    /**
     * Returns range start.
     *
     * @return start.
     */
    public int getStart()
    {
        return start;
    }

    /**
     * Returns range end.
     *
     * @return end.
     */
    public int getEnd()
    {
        return end;
    }

    /**
     * Compares this range to the other.
     *
     * @param o the other range to compare with.
     *
     * @return result.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TextRange textRange = (TextRange)o;

        if (end != textRange.end) return false;
        if (start != textRange.start) return false;

        return true;
    }

    /**
     * Range hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        int result;
        result = start;
        result = 29 * result + end;
        return result;
    }

    /**
     * Finds ranges in text according to the pattern.
     *
     * @param text      text.
     * @param pattern   pattern.
     *
     * @return ranges list (not <code>NULL</code>).
     */
    public static TextRange[] findRanges(String text, String pattern)
    {
        return findRanges(text, Pattern.compile(pattern));
    }

    /**
     * Finds ranges in text according to the pattern.
     *
     * @param text      text.
     * @param pattern   pattern.
     *
     * @return ranges list (not <code>NULL</code>).
     */
    public static TextRange[] findRanges(String text, Pattern pattern)
    {
        TextRange[] tranges;

        if (pattern != null)
        {
            Matcher mat = pattern.matcher(text);

            List<TextRange> ranges = new ArrayList<TextRange>();
            int st = 0;
            while (mat.find(st)) ranges.add(new TextRange(mat.start(2), st = mat.end(2)));
            tranges = ranges.toArray(new TextRange[ranges.size()]);
        } else tranges = Constants.EMPTY_TEXT_RANGE_LIST;

        return tranges;
    }
}
