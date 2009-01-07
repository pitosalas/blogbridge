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
// $Id: FeedsInMultipleGuidesReport.java,v 1.6 2008/04/01 12:51:27 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.reports.actions.DeleteFeed;
import com.salas.bb.search.ResultItemType;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.LinkLabel;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Reports feeds that are in multiple guides.
 */
class FeedsInMultipleGuidesReport extends AbstractTableReport
{
    private List<IFeed> data;

    /**
     * Returns the name of the report.
     *
     * @return name.
     */
    public String getReportName()
    {
        return Strings.message("report.feeds.in.multiple.guides");
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected void doInitializeData(IReportDataProvider provider)
    {
        // Find all feeds of a sort
        data = new LinkedList<IFeed>();
        GuidesSet set = provider.getGuidesSet();
        List<IFeed> feeds = set.getFeeds();
        for (IFeed feed : feeds)
        {
            if (feed.getParentGuides().length > 1) data.add(feed);
        }

        // Sort them
        Collections.sort(data, new Comparator<IFeed>()
        {
            /**
             * Compares two feeds by the number of parent guides they have.
             *
             * @param f1    feed 1.
             * @param f2    feed 2.
             *
             * @return comparison result.
             */
            public int compare(IFeed f1, IFeed f2)
            {
                return ((Integer)f2.getParentGuides().length).compareTo(f1.getParentGuides().length);
            }
        });
    }

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
        BBFormBuilder builder = new BBFormBuilder("16px, 4dlu, 16px, 4dlu, 50dlu:grow, 2dlu, center:p", table);
        builder.setDefaultDialogBorder();

        // Output header
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.feed"))), 5);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.guide"))));

        // Output data
        int i = 0;
        for (IFeed feed : data)
        {
            if (i++ == max) break;

            IGuide[] guides = feed.getParentGuides();
            builder.append(new JLabel(ResultItemType.FEED.getIcon()));
            builder.nextColumn(2);
            builder.append(createFeedLabel(feed));
            builder.append(new JLabel(Integer.toString(guides.length)));

            builder.setLeadingColumnOffset(2);
            for (IGuide guide : guides)
            {
                builder.append(new JLabel(ResultItemType.GUIDE.getIcon()));
                builder.append(createGuideLabel(guide, feed), 1);
                builder.nextColumn(2);
            }
            builder.setLeadingColumnOffset(0);

            builder.appendUnrelatedComponentsGapRow(2);
        }

        return builder.getPanel();
    }

    /**
     * Creates a guide label.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @return label.
     */
    private Component createGuideLabel(final IGuide guide, final IFeed feed)
    {
        LinkLabel label = LinkLabel.create(guide.getTitle(), new GuideClickAction(guide, guide.getID()));
        label.addMouseListener(new GuidePopupAdapter(guide, -1)
        {
            @Override
            protected Map<String, Action> getActions()
            {
                Map<String, Action> actions = super.getActions();

                actions.remove(ACTION_DELETE);
                actions.put(ACTION_DELETE, new DeleteFeed(feed, guide));

                return actions;
            }
        });

        return label;
    }
}
