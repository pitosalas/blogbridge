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
// $Id: IReport.java,v 1.3 2007/09/26 11:04:02 spyromus Exp $
//

package com.salas.bb.reports;

import javax.swing.*;

/**
 * Report interface.
 */
interface IReport
{
    /**
     * Returns the name of the report.
     *
     * @return name.
     */
    String getReportName();

    /**
     * Returns the report page component.
     *
     * @return component.
     */
    JComponent getReportPage();

    /**
     * Sets a callback a report can use to report clicks on entities.
     *
     * @param callback callback.
     */
    void setClickCallback(IClickCallback callback);

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    void initializeData(IReportDataProvider provider);

    /**
     * Prepares the view for the display. Should be called after the initializeData()
     * method.
     *
     * @throws IllegalStateException if called when data is still not initialized.
     */
    void layoutView();

    /**
     * Invoked when a users presses the reset button. If the report knows what
     * to do, it can be done. Otherwise the report data should be cleared and
     * the initializeData() call expected.
     */
    void reset();

    /**
     * Returns <code>TRUE</code> when data is initialized.
     *
     * @return <code>TRUE</code> when data is initialized. 
     */
    boolean isDataInitialized();
}
