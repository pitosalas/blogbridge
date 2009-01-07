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
// $Id: Calculator.java,v 1.2 2008/02/27 10:52:07 spyromus Exp $
//

package com.salas.bb.sentiments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculates sentiment counts.
 */
public abstract class Calculator
{
    private static SentimentsConfig config = new SentimentsConfig();

    /**
     * Returns configuration of the calculator.
     *
     * @return configuration.
     */
    public static SentimentsConfig getConfig()
    {
        return config;
    }

    /**
     * Counts the number of positive sentiment occurances in
     * the text.
     *
     * @param text text.
     *
     * @return count.
     */
    public static synchronized int countPositiveOccurances(String text)
    {
        return countOccurances(text, config.getPositivePattern());
    }

    /**
     * Counts the number of negative sentiment occurances in
     * the text.
     *
     * @param text text.
     *
     * @return count.
     */
    public static synchronized int countNegativeOccurances(String text)
    {
        return countOccurances(text, config.getNegativePattern());
    }

    /**
     * Counts occurances of a pattern in the text.
     *
     * @param text  text.
     * @param pat   pattern.
     *
     * @return number of occurances.
     */
    private static int countOccurances(String text, Pattern pat)
    {
        if (text == null || pat == null) return 0;

        int cnt = 0;
        int st = 0;
        Matcher mat = pat.matcher(text);
        while (mat.find(st))
        {
            cnt++;
            st = mat.end(2);
        }

        return cnt;
    }
}
