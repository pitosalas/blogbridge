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
// $Id: LineChart.java,v 1.5 2007/10/15 08:57:43 spyromus Exp $
//

package com.salas.bb.utils.uif.charts;

import javax.swing.*;
import java.awt.*;

/**
 * Shows data on a line chart. Each chart has two axes and the values (dots)
 * connected by lines.
 *
 * The chart is static meaning that the data is given upon initialization
 * and can't be changed later. The chart has several properties for customization
 * that also can't be changed in real time. This is the simplification
 * assumption.
 */
public class LineChart extends JComponent
{
    /** 360 degrees. */
    private static final int ANGLE_360 = 360;

    /** Configuration. */
    private final LineChartConfig config;

    /** Data to plot. */
    private final LineChartData data;

    /** Preferred size. */
    private final Dimension prefSize;

    /** Maximum value among all given data values. */
    private final int maxValue;
    /** Minimum value among all given data values. */
    private final int minValue;
    /** Median between the max and min. */
    private final int medianValue;

    /**
     * Creates a line chart with default configuration.
     *
     * @param data data to plot.
     */
    public LineChart(LineChartData data)
    {
        this(data, new LineChartConfig());
    }

    /**
     * Creates a line chart with the given data and configuration.
     *
     * @param data   data to plot.
     * @param config configuration to use.
     */
    public LineChart(LineChartData data, LineChartConfig config)
    {
        if (data == null) throw new IllegalArgumentException("Data can't be NULL");
        if (config == null) throw new IllegalArgumentException("Config can't be NULL");
        
        this.config = config;
        this.data = data;

        // Find maximum and median within data values
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < data.getValuesCount(); i++)
        {
            int val = data.getValue(i);
            max = Math.max(max, val);
        }
        maxValue = max;
        minValue = 0;
        medianValue = (maxValue - minValue) / 2; 

        // Calculate a preferred size
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
        prefSize = new Dimension(w, h);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return prefSize;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // Configuration properties
        int dotRadiusI              = config.getDotRadius();
        int dotRadiusO              = dotRadiusI + 2;
        int valueXStep              = config.getValueXStep();
        int valueScaleWidth         = config.getValueScaleWidth();
        Font valueScaleFont         = config.getValueScaleFont();
        int indexLabelHeight        = config.getIndexLabelHeight();
        int indexLabelBorderWidth   = config.getIndexLabelBorderWidth();
        Color indexLabelBorderColor = config.getIndexLabelBorderColor();
        Font indexLabelFont         = config.getIndexLabelFont();
        int indexLabelStep          = config.getIndexLabelStep();
        Color gridLineColor         = config.getGridLineColor();
        Color labelColor            = config.getGetLabelColor();
        Color medianLineColor       = config.getMedianLineColor();
        int mainLineWidth           = config.getMainLineWidth();
        Color mainLineColor         = config.getMainLineColor();
        Color backGroundColor       = config.getBackgroundColor();
        Color fillColor             = config.getFillColor();
        boolean stepStretching      = config.getStepStretching();
        
        // Data properties
        int valuesCount             = data.getValuesCount();

        // Calculate sizes
        Dimension total = getSize();
        int width = total.width;
        int height = total.height;

        // Legend:
        //  il -- index label area
        //  ma -- main area
        //  vl -- value label area
        //  bo -- border
        //  me -- median
        //
        //  X, Y, W, H -- x, y, width, height

        // Minimal main area width and height
        int maMinW = 2 * dotRadiusI + valueXStep * (valuesCount - 1);
        int maMinH = 2 * dotRadiusI;

        // X coordinates of vl 1/2 and the main area
        int vl1X = 0;
        int maX = vl1X + valueScaleWidth;
        int vl2X = Math.max(width - valueScaleWidth, maX + maMinW);

        // Calculate steps
        if (stepStretching)
        {
            valueXStep = Math.max((vl2X - maX) / (valuesCount - 1), valueXStep);
        }

        // Heights
        int maH = Math.max(height - Math.max(1, indexLabelHeight - indexLabelBorderWidth), maMinH);
        int tH = maH + indexLabelBorderWidth + indexLabelHeight;

        // Widths
        int tW = vl2X + valueScaleWidth;

        // Y coordinates of vl 1/2, the main area, median, border, and the il
        int maY = 0;
        int boY = Math.max(maH, maY + maMinH);
        int meY = dotRadiusI + (boY - maY - dotRadiusI) / 2;

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill the area
        g2d.setColor(backGroundColor);
        g2d.fillRect(0, 0, tW, tH);

        // Prepare a poly coordinates for the main line
        int[] fpolyX = new int[valuesCount + 2];
        int[] fpolyY = new int[valuesCount + 2];
        int[] polyX = new int[valuesCount];
        int[] polyY = new int[valuesCount];
        double k = (double)(maH - dotRadiusI) / maxValue;
        for (int i = 0; i < valuesCount; i++)
        {
            polyX[i] = maX + i * valueXStep;
            polyY[i] = maH - (int)(k * data.getValue(i));
            fpolyX[i + 1] = polyX[i];
            fpolyY[i + 1] = polyY[i];
        }
        fpolyX[0] = maX;
        fpolyX[fpolyX.length - 1] = polyX[polyX.length - 1];
        fpolyY[0] = maH;
        fpolyY[fpolyX.length - 1] = maH;

        // Fill poly of the main area
        if (fillColor != null)
        {
            g2d.setColor(fillColor);
            g2d.fillPolygon(fpolyX, fpolyY, fpolyX.length);
        }

        if (gridLineColor != null)
        {
            // Paint top grid line
            g2d.setColor(gridLineColor);
            g2d.drawLine(vl1X, maY, tW, maY);

            // Paint a vertical grid line
            g2d.setFont(indexLabelFont);
            for (int i = valuesCount - indexLabelStep; i >= 0; i -= indexLabelStep)
            {
                int x = maX + i * valueXStep;
                g2d.setColor(gridLineColor);
                g2d.drawLine(x, maY, x, boY);             
                if (i < valuesCount - 1)
                {
                    g2d.setColor(labelColor);
                    g2d.drawLine(x, boY, x, tH);
                    g2d.drawString(data.getIndexLabel(i), x + 3, tH - 4);
                }
            }
        }

        // Paint median line
        if (medianLineColor != null)
        {
            g2d.setColor(medianLineColor);
            g2d.setStroke(new BasicStroke(1, 0, 0, 1, new float[] { 1.5f, 1.5f }, 0));
            g2d.drawLine(vl1X, meY, tW, meY);
        }

        // Paint border line
        if (indexLabelBorderWidth > 0)
        {
            g2d.setStroke(new BasicStroke(indexLabelBorderWidth));
            g2d.setColor(indexLabelBorderColor);
            g2d.drawLine(vl1X, boY, tW, boY);
        }

        // Draw the main line
        BasicStroke stLine = new BasicStroke(mainLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stLine);
        g2d.setColor(mainLineColor);
        g2d.drawPolyline(polyX, polyY, polyX.length);

        // Draw the dots
        if (dotRadiusI > 0)
        {
            for (int i = 0; i < polyX.length; i++)
            {
                int x = polyX[i];
                int y = polyY[i];

                g2d.setColor(backGroundColor);
                g2d.fillArc(x - dotRadiusO, y - dotRadiusO, 2 * dotRadiusO, 2 * dotRadiusO, 0, ANGLE_360);
                g2d.setColor(mainLineColor);
                g2d.fillArc(x - dotRadiusI, y - dotRadiusI, 2 * dotRadiusI, 2 * dotRadiusI, 0, ANGLE_360);
            }
        }

        // Draw value labels
        if (valueScaleWidth > 0)
        {
            String maxVS = Integer.toString(maxValue);
            String medVS = Integer.toString(medianValue);
            float fhMax = valueScaleFont.getLineMetrics(maxVS, g2d.getFontRenderContext()).getHeight();
            float fhMed = valueScaleFont.getLineMetrics(medVS, g2d.getFontRenderContext()).getHeight();

            g2d.setFont(valueScaleFont);
            g2d.setColor(labelColor);
            g2d.drawString(maxVS, vl1X + 3, dotRadiusI + fhMax + 1);
            if (medianValue > 0) g2d.drawString(medVS, vl1X + 3, meY + fhMed + 1);
            g2d.drawString(maxVS, vl2X + 3, dotRadiusI + fhMax + 1);
            if (medianValue > 0) g2d.drawString(medVS, vl2X + 3, meY + fhMed + 1);
        }
    }
}
