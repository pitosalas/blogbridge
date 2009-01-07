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
// $Id: ResultItem.java,v 1.1 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

import com.salas.bb.domain.FeedType;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;

import java.util.Date;

/**
 * Single search result item.
 */
public class ResultItem
{
    private final Object object;
    private final ResultItemType type;
    private final Date date;
    private final boolean priority;

    /**
     * Creates result item with an object.
     *
     * @param aObject object.
     */
    public ResultItem(Object aObject)
    {
        object = aObject;

        if (object instanceof IGuide)
        {
            type = ResultItemType.GUIDE;
            date = null;
            priority = false;
        } else if (object instanceof IFeed)
        {
            type = ResultItemType.FEED;
            long lastUpdateTime = ((IFeed)object).getLastUpdateTime();
            date = lastUpdateTime == -1L ? null : new Date(lastUpdateTime);
            priority = false;
        } else
        {
            IArticle article = (IArticle)object;
            date = article.getPublicationDate();
            priority = article.isPinned();

            IFeed feed = article.getFeed();
            if (feed.getType() == FeedType.TEXT)
            {
                type = ResultItemType.ARTICLE;
            } else
            {
                type = ResultItemType.PICTURE;
            }
        }
    }

    /**
     * Returns result item object.
     *
     * @return object.
     */
    public Object getObject()
    {
        return object;
    }

    /**
     * Returns <code>TRUE</code> if this item has priority.
     *
     * @return <code>TRUE</code> if this item has priority.
     */
    public boolean isPriority()
    {
        return priority;
    }

    /**
     * Returns string representation of the object.
     *
     * @return string representation.
     */
    public String toString()
    {
        return getTitle();
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        return object.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     *
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object obj)
    {
        ResultItem sri = (ResultItem)obj;
        return object == sri.object;
    }

    /**
     * Returns the type.
     *
     * @return type.
     */
    public ResultItemType getType()
    {
        return type;
    }

    /**
     * Get title.
     *
     * @return title.
     */
    private String getTitle()
    {
        String str;

        if (object instanceof IGuide)
        {
            str = ((IGuide)object).getTitle();
        } else if (object instanceof IFeed)
        {
            str = ((IFeed)object).getTitle();
        } else
        {
            str = ((IArticle)object).getTitle();
        }

        return str;
    }

    /**
     * Returns associated date.
     *
     * @return date.
     */
    public Date getDate()
    {
        return date;
    }
}
