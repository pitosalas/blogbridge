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
// $Id: TestSmartFeedDialog.java,v 1.5 2006/12/13 12:00:03 spyromus Exp $
//

package com.salas.bb.dialogs;

import junit.framework.TestCase;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.IProperty;
import com.salas.bb.domain.query.IComparisonOperation;

/**
 * This suite contains tests for <code>SmartFeedAction</code> unit.
 */
public class TestSmartFeedDialog extends TestCase
{
    /**
     * Tests data validation.
     */
    public void testValidateCommonFields()
    {
        assertNotNull("Invalid title.", SmartFeedDialog.validateCommonFields("", "1"));
        assertNotNull("Invalid title.", SmartFeedDialog.validateCommonFields(" ", "1"));
        assertNotNull("Invalid limit.", SmartFeedDialog.validateCommonFields("a", ""));
        assertNotNull("Invalid limit.", SmartFeedDialog.validateCommonFields("a", "-1"));
        assertNotNull("Invalid limit.", SmartFeedDialog.validateCommonFields("a", "0"));
        assertNotNull("Invalid limit.", SmartFeedDialog.validateCommonFields("a", "a"));

        assertNull(SmartFeedDialog.validateCommonFields("a", "1"));
    }

    /**
     * Tests validation of query feeds.
     */
    public void testValidateQueryFeedData()
    {
        QueryType type = QueryType.getQueryType(QueryType.TYPE_DELICIOUS);

        assertNotNull("Invalid parameter.", SmartFeedDialog.validateQueryFeedData(type, "", "1", true, 1));
        assertNotNull("Invalid parameter.", SmartFeedDialog.validateQueryFeedData(type, " ", "1", true, 1));

        assertNull(SmartFeedDialog.validateQueryFeedData(type, "a", "1", true, 1));
    }

    /**
     * Tests validation of query feeds.
     */
    public void testValidateSearchFeedData()
    {
        Query query = new Query();

        assertNotNull("Query is empty.", SmartFeedDialog.validateSearchFeedData(query, true, 1));

        ICriteria criteria = query.addCriteria();

        assertNotNull("Criteria is bad.", SmartFeedDialog.validateSearchFeedData(query, true, 1));

        IProperty someProperty = (IProperty)query.getAvailableProperties().toArray()[0];
        IComparisonOperation someOperation = (IComparisonOperation)someProperty.getComparsonOperations().toArray()[0];
        criteria.setProperty(someProperty);
        criteria.setComparisonOperation(someOperation);
        criteria.setValue("1");

        assertEquals("Query is ok.", null, SmartFeedDialog.validateSearchFeedData(query, true, 1));
    }
}
