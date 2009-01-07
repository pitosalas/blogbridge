// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
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
// $Id: IControllerListener.java,v 1.10 2006/01/08 04:43:45 kyank Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;

import java.util.EventListener;

/**
 * Interface for listener of all event emitted by <code>GlobalController</code>.
 */
public interface IControllerListener extends EventListener
{
    /**
     * Invoked when new article was added to the feed.
     *
     * @param article   article added.
     * @param feed      feed.
     */
    void articleAdded(IArticle article, IFeed feed);

    /**
     * Invoked when article removed from feed.
     *
     * @param article   article.
     * @param feed      feed.
     */
    void articleRemoved(IArticle article, IFeed feed);

    /**
     * Invoked when article selected / deselected.
     *
     * @param article article selected or NULL if deselected.
     */
    void articleSelected(IArticle article);

    /**
     * Invoked after application changes the feed.
     *
     * @param feed feed to which we have switched.
     */
    void feedSelected(IFeed feed);

    /**
     * Invoked after application changes the guide.
     *
     * @param guide guide to with we have switched.
     */
    void guideSelected(IGuide guide);

    /**
     * Invoked after application finishes initialization of data.
     */
    void initializationFinished();
}
