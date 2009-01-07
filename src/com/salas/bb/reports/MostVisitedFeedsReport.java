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
// $Id: MostVisitedFeedsReport.java,v 1.5 2007/10/03 12:32:29 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.search.ResultItemType;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;

/**
 * Most visited feeds report.
 */
class MostVisitedFeedsReport extends AbstractMostVisitedReport
{
    /**
     * Creates a report.
     */
    public MostVisitedFeedsReport()
    {
        super(Strings.message("report.feed"), ResultItemType.FEED.getIcon());
    }

    /**
     * Returns the name of the report.
     *
     * @return name.
     */
    public String getReportName()
    {
        return Strings.message("report.most.visited.feeds");
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected void doInitializeData(IReportDataProvider provider)
    {
        visitStats = provider.statGetMostVisitedFeeds(Integer.MAX_VALUE);
    }

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
        return createFeedLabel(id, title);
    }
}