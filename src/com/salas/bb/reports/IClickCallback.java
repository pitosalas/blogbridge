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
// $Id: IClickCallback.java,v 1.1 2007/09/26 15:43:46 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;

/**
 * Listens to clicks and does actions.
 */
interface IClickCallback
{
    /**
     * Invoked when a guide clicked in a report.
     *
     * @param guide guide.
     */
    void guideClicked(IGuide guide);

    /**
     * Invoked when a guide clicked in a report.
     *
     * @param id guide ID.
     */
    void guideClicked(long id);

    /**
     * Invoked when a feed clicked in a report.
     *
     * @param feed feed.
     */
    void feedClicked(IFeed feed);

    /**
     * Invoked when a feed clicked in a report.
     *
     * @param id feed ID.
     */
    void feedClicked(long id);
}
