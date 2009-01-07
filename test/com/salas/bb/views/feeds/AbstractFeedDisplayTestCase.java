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
// $Id: AbstractFeedDisplayTestCase.java,v 1.3 2006/08/01 18:51:28 spyromus Exp $
//

package com.salas.bb.views.feeds;

import junit.framework.TestCase;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;

import java.util.Date;

/**
 * Abstract test case for feed view. 
 */
public abstract class AbstractFeedDisplayTestCase extends TestCase
{
    protected static final long DAY = 24 * 60 * 60 * 1000;
    protected static final long DELTA_TOMORROW        = DAY;
    protected static final long DELTA_NOW             = 0;
    protected static final long DELTA_YESTERDAY       = -DAY;
    protected static final long DELTA_6_DAYS_AGO      = -6 * DAY;
    protected static final long DELTA_13_DAYS_AGO     = -13 * DAY;
    protected static final long DELTA_30_DAYS_AGO     = -30 * DAY;

    /**
     * Creates article with publication date equal to current time + some delta and
     * adds this article to the given feed.
     *
     * @param feed      feed to append the new article to.
     * @param deltaTime delta to add to current moment to evaluate the publication date.
     *
     * @return new article.
     */
    protected IArticle appendArticle(DirectFeed feed, long deltaTime)
    {
        StandardArticle article = createArticle(deltaTime);

        feed.appendArticle(article);

        return article;
    }

    /**
     * Creates article with publication date equal to current time + some delta.
     *
     * @param deltaTime delta to add to current moment to evaluate the publication date.
     *
     * @return new article.
     */
    protected static StandardArticle createArticle(long deltaTime)
    {
        String txt = Long.toString(deltaTime);
        StandardArticle article = new StandardArticle(txt);
        article.setTitle(txt);
        article.setPublicationDate(new Date(System.currentTimeMillis() + deltaTime));
        return article;
    }
}
