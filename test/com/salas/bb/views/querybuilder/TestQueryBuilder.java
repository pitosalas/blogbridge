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
// $Id: TestQueryBuilder.java,v 1.4 2006/01/08 05:28:35 kyank Exp $
//

package com.salas.bb.views.querybuilder;

import junit.framework.TestCase;
import com.salas.bb.domain.query.IQuery;
import com.salas.bb.domain.query.DummyStringQuery;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;

/**
 * This suite contains tests for <code>QueryBuilder</code> unit.
 */
public class TestQueryBuilder extends TestCase
{
    private QueryBuilder builder;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        builder = new QueryBuilder();
    }

    /**
     * Tests scenario with new and existing queries.
     */
    public void testWorkingWithQuery()
    {
        Query query = new Query();
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(ArticleStatusProperty.INSTANCE);
        criteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        criteria.setValue(ArticleStatusProperty.VALUE_UNREAD);

        Query clone = query.getClone();

        builder.setQuery(query);

        assertEquals("Default criteria should be added to empty query.",
            1, query.getCriteriaCount());

        // ... User makes changes to query and closes dialog ...

        assertEquals("Query should change just because of assigning into builder.",
            clone, query);
    }

    /**
     * Tests adding criteria to the tail of the query when "Add" button is pressed.
     */
    public void testAddCriteria()
    {
        IQuery query = new DummyStringQuery();
        builder.setQuery(query);

        ICriteria defaultCriteria = query.getCriteriaAt(0);

        builder.addEmptyCriteria();
        assertEquals("Criteria should be added to query.",
            2, query.getCriteriaCount());
        assertFalse("Wrong criteria object in the tail.",
            defaultCriteria == query.getCriteriaAt(1));
    }

    /**
     * Tests removing the criteria by its index and protecting the last criteria from
     * being removed.
     */
    public void testRemoveCriteria()
    {
        IQuery query = new DummyStringQuery();
        builder.setQuery(query);
        builder.addEmptyCriteria();
        ICriteria defaultCriteria = query.getCriteriaAt(0);

        builder.removeCriteria(0);
        assertEquals("Criteria should be removed.", 1, query.getCriteriaCount());
        assertFalse("Wrong criteria removed.", defaultCriteria == query.getCriteriaAt(0));

        builder.removeCriteria(0);
        assertEquals("The last criteria should not be removed.", 1, query.getCriteriaCount());
    }
}
