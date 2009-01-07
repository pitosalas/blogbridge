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
// $Id: TestArticleDateProperty.java,v 1.4 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain.query.articles;

import junit.framework.TestCase;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.domain.query.general.DateMatchCO;
import com.salas.bb.domain.query.general.IDates;
import com.salas.bb.domain.query.general.DateBeforeCO;
import com.salas.bb.utils.Constants;

import java.util.Date;

/**
 * This suite contains tests for <code>ArticleDateProperty</code> unit.
 */
public class TestArticleDateProperty extends TestCase
{
    private ArticleDateProperty articleDateProperty;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        articleDateProperty = new ArticleDateProperty();
    }

    /**
     * Tests matching today's articles
     */
    public void testMatchingTodaysArticles()
    {
        StandardArticle todaysArticle = createTodaysArticle();
        StandardArticle yesterdaysArticle = createYesterdaysArticle();

        assertTrue(articleDateProperty.match(todaysArticle, DateMatchCO.INSTANCE,
            IDates.VALUE_TODAY));
        assertFalse(articleDateProperty.match(yesterdaysArticle, DateMatchCO.INSTANCE,
            IDates.VALUE_TODAY));
    }

    /**
     * Tests matching yesterday's articles
     */
    public void testMatchingYesterdayArticles()
    {
        StandardArticle todaysArticle = createTodaysArticle();
        StandardArticle yesterdaysArticle = createYesterdaysArticle();

        assertFalse(articleDateProperty.match(todaysArticle, DateMatchCO.INSTANCE,
            IDates.VALUE_YESTERDAY));
        assertTrue(articleDateProperty.match(yesterdaysArticle, DateMatchCO.INSTANCE,
            IDates.VALUE_YESTERDAY));
    }

    public void testBeforeToday()
    {
        StandardArticle todaysArticle = createTodaysArticle();
        StandardArticle yesterdaysArticle = createYesterdaysArticle();

        assertFalse(articleDateProperty.match(todaysArticle, DateBeforeCO.INSTANCE,
            IDates.VALUE_TODAY));
        assertTrue(articleDateProperty.match(yesterdaysArticle, DateBeforeCO.INSTANCE,
            IDates.VALUE_TODAY));
    }

    private StandardArticle createYesterdaysArticle()
    {
        return createArticle(System.currentTimeMillis() - Constants.MILLIS_IN_DAY);
    }

    private StandardArticle createTodaysArticle()
    {
        return createArticle(System.currentTimeMillis());
    }

    private StandardArticle createArticle(long time)
    {
        StandardArticle todaysArticle = new StandardArticle("");
        todaysArticle.setPublicationDate(new Date(time));
        return todaysArticle;
    }
}
