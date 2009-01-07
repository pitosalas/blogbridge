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
// $Id: AbstractTableReport.java,v 1.5 2008/04/01 12:51:27 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.LinkLabel;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract report with the table and the header.
 */
abstract class AbstractTableReport extends AbstractReport
{
    /** Number of entities to show at first. */
    private static final int TOP_ENTITIES_COUNT = 10;
    /** Table. */
    private JPanel table;

    /** Prepares the view for the display. Should be called after the initializeData() method. */
    protected void doLayoutView()
    {
        // Initialize component
        if (table == null) table = new JPanel();

        createDataTable(table, TOP_ENTITIES_COUNT);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);

        setBackground(Color.WHITE);
        table.setBackground(Color.WHITE);

        // Build the layout
        BBFormBuilder builder = new BBFormBuilder("p:grow", this);
        builder.setDefaultDialogBorder();
        builder.append(UifUtilities.boldFont(new JLabel(getReportName())), 1,
            CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu:grow");
        builder.append(sp, 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(new SeeAllLinkLabel(Strings.message("report.see.all")));
    }

    /**
     * Creates a table for stats display.
     *
     * @param table table component to initialize.
     * @param max maximum number of rows.
     *
     * @return table.
     */
    protected abstract JPanel createDataTable(JPanel table, int max);

    /**
     * Called to create a label component for an entity.
     *
     * @param id    entity ID.
     * @param title entity title.
     *
     * @return component.
     */
    protected JComponent createLabel(long id, String title)
    {
        return new JLabel(title);
    }

    /**
     * A link label to open the complete report.
     */
    private class SeeAllLinkLabel extends LinkLabel
    {
        /**
         * Creates pure link label without link.
         *
         * @param text text to put.
         */
        private SeeAllLinkLabel(String text)
        {
            super(text);
        }

        @Override
        protected void doAction()
        {
            setEnabled(false);

            table.removeAll();
            createDataTable(table, Integer.MAX_VALUE);
            table.revalidate();
        }
    }
}
