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
// $Id: IFeedDisplayModelListener.java,v 1.2 2006/01/08 05:12:59 kyank Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.IArticle;

/**
 * Listener for feed view model events.
 */
public interface IFeedDisplayModelListener
{
    /**
     * Invoked when model receives the event about new article addition.
     *
     * @param article       new article.
     * @param group         group index this article was assigned to.
     * @param indexInGroup  index inside the group.
     */
    void articleAdded(IArticle article, int group, int indexInGroup);

    /**
     * Invoked when model receives the event about article removal.
     *
     * @param article       deleted article.
     * @param group         group index this article was assigned to.
     * @param indexInGroup  index inside the group.
     */
    void articleRemoved(IArticle article, int group, int indexInGroup);

    /**
     * Invoked when all articles removed from the model as result of
     * feeds change.
     */
    void articlesRemoved();
}
