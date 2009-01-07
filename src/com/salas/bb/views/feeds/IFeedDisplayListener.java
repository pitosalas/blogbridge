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
// $Id: IFeedDisplayListener.java,v 1.5 2007/05/30 09:43:15 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;

import java.net.URL;

/**
 * Listener for events from feed view object.
 */
public interface IFeedDisplayListener
{
    /**
     * Invoked when user selects article or article is selected as
     * result of direct invocation of {@link IFeedDisplay#selectArticle(com.salas.bb.domain.IArticle)}
     * method.
     *
     * @param lead              lead article.
     * @param selectedArticles  all selected articles.
     */
    void articleSelected(IArticle lead, IArticle[] selectedArticles);

    /**
     * Invoked when user clicks on some link at the article text or header.
     * The expected behaviour is openning the link in browser.
     *
     * @param link link clicked.
     */
    void linkClicked(URL link);

    /**
     * Invoked when user hovers some link with mouse pointer.
     *
     * @param link link hovered or <code>NULL</code> if previously hovered link
     *             is no longer hovered.
     */
    void linkHovered(URL link);

    /**
     * Invoked when user clicks on some quick-link to the other feed.
     *
     * @param feed feed to select.
     */
    void feedJumpLinkClicked(IFeed feed);

    /**
     * Invoked when the user made something to zoom content in.
     */
    void onZoomIn();

    /**
     * Invoked when the user made something to zoom the content out.
     */
    void onZoomOut();
}
