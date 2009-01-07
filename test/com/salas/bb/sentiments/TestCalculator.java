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
// $Id: TestCalculator.java,v 1.3 2008/02/28 15:59:53 spyromus Exp $
//

package com.salas.bb.sentiments;

import junit.framework.TestCase;

/** Tests calculations. */
public class TestCalculator extends TestCase
{
    public void testNoPatterns()
    {
        assertEquals(0, Calculator.countNegativeOccurances("zbc"));
        assertEquals(0, Calculator.countPositiveOccurances("zbc"));
    }

    public void testNoMatch()
    {
        configure();
        assertEquals(0, Calculator.countPositiveOccurances("kb nc"));
        assertEquals(0, Calculator.countNegativeOccurances("abc"));
    }

    public void testMatch()
    {
        configure();
        assertEquals(2, Calculator.countPositiveOccurances("ab ce"));
        assertEquals(2, Calculator.countNegativeOccurances("a bfc d"));
    }

    public void testNullText()
    {
        configure();
        assertEquals(0, Calculator.countPositiveOccurances(null));
        assertEquals(0, Calculator.countNegativeOccurances(null));
    }

    private void configure()
    {
        // Configure
        String[] positive = { "a*", "*e" };
        String[] negative = { "b+c", "d" };
        SentimentsConfig config = Calculator.getConfig();
        config.setPositiveExpressions("a*\n*e");
        config.setNegativeExpressions("b+c\nd");
    }
}
