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
// $Id: AbstractMostVisitedReport.java,v 1.4 2007/10/03 12:32:30 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.persistence.domain.VisitStats;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.util.List;

/**
 * Abstract class for the most visited series of reports.
 */
abstract class AbstractMostVisitedReport extends AbstractTableReport
{
    // --- Initialized by initializeData() ------------------------------------

    /** The text to put in the header of the table in the entity title column. */
    private final String headerEntityTitle;
    /** Item icon. */
    private final Icon itemIcon;

    /** Stats of entity visits. */
    protected List<VisitStats> visitStats;

    /**
     * Creates the report.
     *
     * @param headerEntityTitle text to put in the header of the table in the entity title column.
     * @param itemIcon          icon to use for icons.
     */
    protected AbstractMostVisitedReport(String headerEntityTitle, Icon itemIcon)
    {
        this.itemIcon = itemIcon;
        this.headerEntityTitle = headerEntityTitle;
    }

    /**
     * Creates a table for stats display.
     *
     * @param table table component to initialize.
     * @param max maximum number of rows.
     *
     * @return table.
     */
    protected JPanel createDataTable(JPanel table, int max)
    {
        BBFormBuilder builder = new BBFormBuilder("16px, 4dlu, 50dlu:grow, 2dlu, max(p;50dlu), 2dlu, max(p;50dlu)", table);
        builder.setDefaultDialogBorder();

        // Output header
        builder.append(UifUtilities.boldFont(new JLabel(headerEntityTitle)), 3);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.since.reset"))), 1,
            CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.all.time"))), 1,
            CellConstraints.CENTER, CellConstraints.DEFAULT);

        // Output data
        int i = 0;
        for (VisitStats stat : visitStats)
        {
            if (i++ == max) break;

            builder.append(new JLabel(itemIcon));
            builder.append(createLabel(stat.getObjectId(), stat.getObjectTitle()));
            builder.append(new JLabel(Long.toString(stat.getCountReset())), 1,
                CellConstraints.CENTER, CellConstraints.DEFAULT);
            builder.append(new JLabel(Long.toString(stat.getCountTotal())), 1,
                CellConstraints.CENTER, CellConstraints.DEFAULT);
        }

        return builder.getPanel();
    }
}
