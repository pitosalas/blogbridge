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
// $Id: LineChartConfig.java,v 1.4 2007/10/15 08:57:43 spyromus Exp $
//

package com.salas.bb.utils.uif.charts;

import java.awt.*;

/**
 * Ac configuration object that is used to provide a look configuration.
 */
public class LineChartConfig
{
    // --- Main Graph ---------------------------------------------------------
    // Main graph is the line and dots.

    /** The radius of the plot dot. */
    private int     dotRadius = 3;

    /** The width of the graph line. */
    private int     mainLineWidth = 2;
    /** The color of the graph line. */
    private Color   mainLineColor = Color.decode("#0077cc");

    /** Grid line color. */
    private Color   gridLineColor = Color.decode("#eeeeee");
    /** Median line color. */
    private Color   medianLineColor = Color.decode("#cccccc");

    /** The step between plot points in pixels along the X-Axis. */
    private int     valueXStep = 15;

    /** The label color. */
    private Color   getLabelColor = Color.decode("#333333");
    /** The background color of the chart. */
    private Color   backgroundColor = Color.WHITE;
    /** The color of the fill below the main line. */
    private Color   fillColor = Color.decode("#e6f2fa");

    /** Flags that when there's available horizontal space, value steps have to be stretched. */
    private boolean stepStretching = true;

    // --- Index Label --------------------------------------------------------
    // Index Label is the area below the main graph area where the index labels
    // are.

    /** Index label font. */
    private Font    indexLabelFont = new Font("Arial", Font.PLAIN, 10);
    /** The height of the index label area. */
    private int     indexLabelHeight = 18;
    /** The width of the border between the main graph area and the index label. */
    private int     indexLabelBorderWidth = 1;
    /** The color of the border. */
    private Color   indexLabelBorderColor = Color.decode("#666666");
    /** The step for painting the index labels. */
    private int     indexLabelStep = 1;

    // --- Value Scale --------------------------------------------------------
    // Value scale is to the left and right from the main graph area where the
    // vertical indexes are.

    /** Value scale font. */
    private Font    valueScaleFont = new Font("Arial", Font.PLAIN, 10);
    /** Value scale area width. */
    private int     valueScaleWidth = 30;

    /**
     * Returns the minimal size of the graph.
     *
     * @return minimal size.
     */
    public Dimension getMinGraphSize()
    {
        return new Dimension(
            valueScaleWidth * 2 + dotRadius + valueXStep,
            dotRadius + indexLabelBorderWidth + indexLabelHeight);
    }

    /**
     * Returns the step in pixels between two values along the X-Axis.
     *
     * @return step in pixels.
     */
    public int getValueXStep()
    {
        return valueXStep;
    }

    /**
     * Sets the step of the value intervals along the X-axis in pixels.
     *
     * @param step step in pixels.
     */
    public void setValueXStep(int step)
    {
        this.valueXStep = step;
    }

    /**
     * Returns a plot point radius.
     *
     * @return radius.
     */
    public int getDotRadius()
    {
        return dotRadius;
    }

    /**
     * Sets the radius of the dot in pixels.
     *
     * @param radius radius.
     */
    public void setDotRadius(int radius)
    {
        this.dotRadius = radius;
    }

    /**
     * Returns the font of the index area label.
     *
     * @return label font.
     */
    public Font getIndexLabelFont()
    {
        return indexLabelFont;
    }

    /**
     * Returns the height of the index area.
     *
     * @return height.
     */
    public int getIndexLabelHeight()
    {
        return indexLabelHeight;
    }

    /**
     * Sets the height of the index area in pixels.
     *
     * @param height height.
     */
    public void setIndexLabelHeight(int height)
    {
        this.indexLabelHeight = height;
    }

    /**
     * Returns the width of the border between the index area and the main graph.
     *
     * @return width of the border.
     */
    public int getIndexLabelBorderWidth()
    {
        return indexLabelBorderWidth;
    }

    /**
     * Sets the width of the border between the index area and the main graph.
     *
     * @param width width in pixels.
     */
    public void setIndexLabelBorderWidth(int width)
    {
        this.indexLabelBorderWidth = width;
    }

    /**
     * Returns the font for the value scale.
     *
     * @return font.
     */
    public Font getValueScaleFont()
    {
        return valueScaleFont;
    }

    /**
     * Returns the width of the value scale area.
     *
     * @return width in pixels.
     */
    public int getValueScaleWidth()
    {
        return valueScaleWidth;
    }

    /**
     * Sets the width of the value scale area.
     *
     * @param width width in pixels.
     */
    public void setValueScaleWidth(int width)
    {
        this.valueScaleWidth = width;
    }

    /**
     * Returns the width of the graph line.
     *
     * @return width in pixels.
     */
    public int getMainLineWidth()
    {
        return mainLineWidth;
    }

    /**
     * Sets the width of the main line.
     *
     * @param width width.
     */
    public void setMainLineWidth(int width)
    {
        this.mainLineWidth = width;
    }

    /**
     * Returns the color of the grid line.
     *
     * @return grid line color.
     */
    public Color getGridLineColor()
    {
        return gridLineColor;
    }

    /**
     * Sets the color of the grid lines or <code>NULL</code> not to paint.
     *
     * @param color color or <code>NULL</code>.
     */
    public void setGridLineColor(Color color)
    {
        this.gridLineColor = color;
    }

    /**
     * Returns the median color.
     *
     * @return median color.
     */
    public Color getMedianLineColor()
    {
        return medianLineColor;
    }

    /**
     * Sets the median line color or <code>NULL</code> not to paint.
     *
     * @param color color or <code>NULL</code>.
     */
    public void setMedianLineColor(Color color)
    {
        this.medianLineColor = color;
    }

    /**
     * Returns the index area border line color.
     *
     * @return border line color.
     */
    public Color getIndexLabelBorderColor()
    {
        return indexLabelBorderColor;
    }

    /**
     * Returns the color of the main graph line.
     *
     * @return main color.
     */
    public Color getMainLineColor()
    {
        return mainLineColor;
    }

    /**
     * Returns the step for painting the index labels.
     *
     * @return step.
     */
    public int getIndexLabelStep()
    {
        return indexLabelStep;
    }

    /**
     * Sets the step for painting the index labels.
     *
     * @param step step.
     */
    public void setIndexLabelStep(int step)
    {
        this.indexLabelStep = step;
    }

    /**
     * Returns the label color.
     *
     * @return color.
     */
    public Color getGetLabelColor()
    {
        return getLabelColor;
    }

    /**
     * Returns the color to use for filling of the under the line area.
     *
     * @return fill color.
     */
    public Color getFillColor()
    {
        return fillColor;
    }

    /**
     * Sets the color for filling of the under the line area.
     *
     * @param color color or <code>NULL</code>.
     */
    public void setFillColor(Color color)
    {
        this.fillColor = color;
    }

    /**
     * Returns the color for the background.
     *
     * @return background color.
     */
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * Returns the flag of that when there's available horizontal space, value steps have to be stretched.
     *
     * @return <code>TRUE</code> to stretch.
     */
    public boolean getStepStretching()
    {
        return stepStretching;
    }
}
