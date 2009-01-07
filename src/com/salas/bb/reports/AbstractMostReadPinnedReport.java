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
// $Id: AbstractMostReadPinnedReport.java,v 1.4 2007/10/03 12:32:29 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.charts.LineChart;
import com.salas.bb.utils.uif.charts.LineChartConfig;
import com.salas.bb.utils.uif.charts.LineChartData;
import com.salas.bb.utils.uif.charts.SparklineConfig;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Most read entities report.
 */
abstract class AbstractMostReadPinnedReport extends AbstractTableReport
{
    // --- Initialized by initializeData() ------------------------------------

    /** The text to put in the header of the table in the entity title column. */
    private final String headerEntityTitle;
    /** Icon to use. */
    private final Icon itemIcon;

    /** Stats. */
    private List<ReadStats> stats;

    /**
     * Creates a report.
     *
     * @param headerEntityTitle header entity.
     * @param itemIcon          icon to use.
     */
    protected AbstractMostReadPinnedReport(String headerEntityTitle, Icon itemIcon)
    {
        this.itemIcon = itemIcon;
        this.headerEntityTitle = headerEntityTitle;
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected final void doInitializeData(IReportDataProvider provider)
    {
        stats = getReadStats(provider);

        if (stats != null)
        {
            // Sort stats by total
            Collections.sort(stats, new Comparator<ReadStats>()
            {
                public int compare(ReadStats s1, ReadStats s2)
                {
                    return new Integer(s2.getTotal()).compareTo(s1.getTotal());
                }
            });
        }
    }

    /**
     * Returns the stats from the data provider.
     *
     * @param provider provider.
     *
     * @return stats.
     */
    protected abstract List<ReadStats> getReadStats(IReportDataProvider provider);

    /**
     * Creates a table for stats display.
     *
     * @param table table component to initialize.
     * @param max   maximum number of rows.
     *
     * @return table.
     */
    protected JPanel createDataTable(JPanel table, int max)
    {
        BBFormBuilder builder = new BBFormBuilder("16px, 4dlu, 50dlu:grow, 2dlu, p, 7dlu, p", table);
        builder.setDefaultDialogBorder();

        // Output header
        builder.append(UifUtilities.boldFont(new JLabel(headerEntityTitle)), 3);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.activity"))), 1,
            CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.stats"))), 1,
            CellConstraints.CENTER, CellConstraints.DEFAULT);

        LineChartConfig config = new SparklineConfig();
        config.setValueXStep(2);

        // Output data
        if (stats != null)
        {
            int i = 0;
            for (ReadStats stat : stats)
            {
                if (i++ == max) break;

                LineChartData data = new LineChartData(stat.getCounts());
                LineChart chart = new LineChart(data, config);

                builder.appendRelatedComponentsGapRow(2);
                builder.appendRow("max(p;20px)");
                builder.append(new JLabel(itemIcon));
                builder.append(createLabel(stat.getObjectId(), stat.getObjectTitle()));
                builder.append(chart, 1, CellConstraints.CENTER, CellConstraints.FILL);
                builder.append(new JLabel("<html><b>" + stat.getTotal() + " articles</b><br>" +
                    getAvg(stat) + " a day"));
            }
        }

        return builder.getPanel();
    }

    /**
     * Formats the average.
     *
     * @param stat stat.
     *
     * @return avg.
     */
    private static String getAvg(ReadStats stat)
    {
        float avg = stat.getTotal() / (float)(stat.getCounts().length);
        return MessageFormat.format("{0,number,#.##}", avg);
    }
}
