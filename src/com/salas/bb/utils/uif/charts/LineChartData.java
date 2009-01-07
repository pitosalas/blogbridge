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
// $Id: LineChartData.java,v 1.2 2007/09/20 13:21:03 spyromus Exp $
//

package com.salas.bb.utils.uif.charts;

/**
 * Line chart data object that is used to store and provide plot information.
 */
public class LineChartData
{
    /** Labels for all value indexes. */
    private final String[] labels;
    /** Values. */
    private final int[] values;

    /**
     * Creates a chart data model.
     *
     * @param values    values to paint.
     */
    public LineChartData(int[] values)
    {
        this(values, null);
    }

    /**
     * Creates a chart data model.
     *
     * @param values    values to paint.
     * @param labels    labels for values.
     */
    public LineChartData(int[] values, String[] labels)
    {
        if (values == null) throw new IllegalArgumentException("Values can't be NULL");
        if (labels != null)
        {
            if (values.length != labels.length) throw new IllegalArgumentException("Different number of values and labels");
            for (String label : labels) if (label == null) throw new IllegalArgumentException("Labels should not be NULL");
        }

        this.values = values;
        this.labels = labels;
    }

    /**
     * Returns the number of values in the set.
     *
     * @return values count.
     */
    public int getValuesCount()
    {
        return values.length;
    }

    /**
     * Returns the value at the given index.
     *
     * @param index index.
     *
     * @return value.
     */
    public int getValue(int index)
    {
        return values[index];
    }

    /**
     * Returns the label for the given index.
     *
     * @param index index.
     *
     * @return label.
     */
    public String getIndexLabel(int index)
    {
        return labels == null ? "" : labels[index];
    }
}
