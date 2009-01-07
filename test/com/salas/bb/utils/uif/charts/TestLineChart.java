// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: TestLineChart.java,v 1.1 2007/09/11 18:59:49 spyromus Exp $
//

package com.salas.bb.utils.uif.charts;

import junit.framework.TestCase;

import java.awt.*;

/**
 * Tests line chart component.
 */
public class TestLineChart extends TestCase
{
    /** Tests NULL-data. */
    public void testNullData()
    {
        try
        {
            new LineChart(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /** Tests NULL-config. */
    public void testNullConfig()
    {
        try
        {
            new LineChart(new LineChartData(new int[0], new String[0]), null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /** Tests the calculation of a preferred size. */
    public void testPreferredSize()
    {
        // Configure the chart
        LineChartConfig config = new LineChartConfig();
        LineChartData data = new LineChartData(new int[] { 1, 2, 3 }, new String[] { "a", "b", "c" });
        LineChart chart = new LineChart(data, config);

        // Read configuration
        Dimension min       = config.getMinGraphSize();
        int step            = config.getValueXStep();
        int radius          = config.getDotRadius();
        int hIL             = config.getIndexLabelHeight();
        int wILB            = config.getIndexLabelBorderWidth();
        int wVS             = config.getValueScaleWidth();

        // Calculate
        // Width:  label padding + dot radius + graph itself + dot radius + label padding
        // Height: minimum one dot radius + 2px padding + border width + padding
        int w = Math.max(min.width, wVS + (radius + (step * (data.getValuesCount() - 1)) + radius) + wVS);
        int h = Math.max(min.height, radius * 2 + wILB + hIL);

        // Check
        Dimension dim = chart.getPreferredSize();
        assertEquals(w, dim.width);
        assertEquals(h, dim.height);
    }
}
