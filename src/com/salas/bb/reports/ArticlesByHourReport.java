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
// $Id: ArticlesByHourReport.java,v 1.4 2007/10/03 12:32:30 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.charts.LineChart;
import com.salas.bb.utils.uif.charts.LineChartConfig;
import com.salas.bb.utils.uif.charts.LineChartData;

import javax.swing.*;
import java.awt.*;

/**
 * Report on articles read by hour of a day.
 */
class ArticlesByHourReport extends AbstractReport
{
    // --- Initialized by initializeData() ------------------------------------

    /** Number of articles read broken by hour (total). */
    private int[] readByHourTotal;
    /** Number of articles read broken by hour (since reset). */
    private int[] readByHourReset;

    /**
     * Returns the report name.
     *
     * @return name.
     */
    public String getReportName()
    {
        return Strings.message("report.articles.read.by.hour");
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected void doInitializeData(IReportDataProvider provider)
    {
        // Do processing
        readByHourReset = new int[Constants.HOURS_IN_DAY];
        readByHourTotal = new int[Constants.HOURS_IN_DAY];
        CountStats[] perHour = provider.statGetItemsReadPerHour();
        for (int i = 0; i < Constants.HOURS_IN_DAY; i++)
        {
            if (perHour == null)
            {
                readByHourReset[i] = readByHourTotal[i] = 0;
            } else
            {
                readByHourReset[i] = (int)perHour[i].getCountReset();
                readByHourTotal[i] = (int)perHour[i].getCountTotal();
            }
        }
    }

    /** Prepares the view for the display. Should be called after the initializeData() method. */
    protected void doLayoutView()
    {
        // Initialize data by hour
        String[] hours = new String[Constants.HOURS_IN_DAY];
        System.arraycopy(Constants.HOUR_NAMES, 0, hours, 0, hours.length);
        hours[0] = Strings.message("report.midnight");
        hours[12] = Strings.message("report.noon");

        LineChartConfig config = new LineChartConfig();
        config.setIndexLabelStep(4);
        LineChartData dataByHour = new LineChartData(readByHourTotal, hours);
        LineChart chartByHour = new LineChart(dataByHour, config);

        LineChartData dataByHourReset = new LineChartData(readByHourReset, hours);
        LineChart chartByHourReset = new LineChart(dataByHourReset, config);

        // Initialize the panel itself
        setBackground(config.getBackgroundColor());

        // Build the layout
        BBFormBuilder builder = new BBFormBuilder("p:grow", this);
        builder.setDefaultDialogBorder();

        JLabel lbByDay = new JLabel(Strings.message("report.articles.read.by.hour.title"));
        Font fntBold = lbByDay.getFont().deriveFont(Font.BOLD);
        lbByDay.setFont(fntBold);
        builder.append(lbByDay, 1, CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("p:grow");
        builder.append(chartByHour, 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.appendUnrelatedComponentsGapRow(2);

        JLabel lbByWeek = new JLabel(Strings.message("report.articles.read.by.hour.title") + " " +
            Strings.message("report.since.reset.box"));
        lbByWeek.setFont(fntBold);
        builder.append(lbByWeek, 1, CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("p:grow");
        builder.append(chartByHourReset, 1, CellConstraints.FILL, CellConstraints.FILL);
    }
}
