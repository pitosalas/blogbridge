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
// $Id: TestQueryScenarios.java,v 1.7 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain.query;

import junit.framework.TestCase;

import java.util.Collection;

import com.salas.bb.domain.query.articles.*;
import com.salas.bb.domain.query.general.*;

/**
 * This suite contains scenarios of <code>IQuery</code> usage. 
 */
public class TestQueryScenarios extends TestCase
{
    private IQuery query;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        query = new DummyStringQuery();
    }

    /**
     * Tests the gerneral scenario of reading meta-data for a property.
     */
    public void testReadingProperties()
    {
        Collection availProperties = query.getAvailableProperties();
        assertTrue("Should expose some properties.", availProperties.size() > 0);

        IProperty property = (IProperty)availProperties.iterator().next();

        Collection availOperations = property.getComparsonOperations();
        assertFalse("Should expose some comparison operations.", availOperations.isEmpty());

        PropertyType type = property.getType();
        assertNotNull("Should not be NULL.", type);
    }

    /**
     * Tests the general scenario when building query from scratch.
     *
     * First, we will need the list of available properties to display them to
     * the user in property selection list. Each property has the associated type
     * (boolean, starz, integer, string and etc). The property has the default
     * value, which should be used to initialize the criteria by default. And of
     * course each property has the set of allowed coparison operations.
     *
     * Each query can join criteria by "and" or "or".
     */
    public void testBuildingQuery()
    {
        // Get some property from available properties list
        Collection availProperties = query.getAvailableProperties();
        IProperty someProperty = (IProperty)availProperties.iterator().next();

        // Get the attributes of the property: some operation, type, default value
        IComparisonOperation someOperation =
            (IComparisonOperation)someProperty.getComparsonOperations().iterator().next();
        PropertyType type = someProperty.getType();
        String defaultValue = someProperty.getDefaultValue();

        assertNotNull("Type is necessary to display proper controls.", type);

        // Create new criteria and initialize it with some valid values
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(someProperty);
        criteria.setComparisonOperation(someOperation);
        criteria.setValue(defaultValue);

        assertEquals("Criteria should be added.", 1, query.getCriteriaCount());
        assertTrue("Wrong criteria object.", criteria == query.getCriteriaAt(0));

        // Set the flag to join criteria by "AND"
        query.setAndQuery(true);

        // Remove the criteria by its index
        query.removeCriteria(0);
        assertEquals("Criteria should be removed.", 0, query.getCriteriaCount());
    }

    /**
     * This is the general scenario of storing and restoring query.
     */
    public void testStoringRestoringQuery()
    {
        // Create some sample criteria
        ICriteria someCriteria = query.addCriteria();
        IProperty someProperty = (IProperty)query.getAvailableProperties().iterator().next();

        // Init the criteria with some parameters
        someCriteria.setProperty(someProperty);
        someCriteria.setComparisonOperation((IComparisonOperation)
            someProperty.getComparsonOperations().iterator().next());
        someCriteria.setValue(someProperty.getDefaultValue());

        // Store the query into database. Query is a set of criteria joined by
        // "And" or "Or". Here we do reading of everything what's necessary.
        boolean isAndQuery = query.isAndQuery();

        ICriteria criteria = query.getCriteriaAt(0);
        IProperty property = criteria.getProperty();
        IComparisonOperation operation = criteria.getComparisonOperation();
        String value = criteria.getValue();

        // We prefer to save criteria components as strings to see better what we
        // deal with with unarmed eye when examining the database or OPML or anything
        // else. The value is saved as-is.
        String propertyDescriptor = property.getDescriptor();
        String operationDescriptor = operation.getDescriptor();

        // Now restore the query. Query knows the properties by their descriptors.
        // Properties know their comparison operations by descriptors.
        IQuery restoredQuery = new DummyStringQuery();
        restoredQuery.setAndQuery(isAndQuery);
        ICriteria restoredCriteria = restoredQuery.addCriteria();

        IProperty restoredProperty = query.getPropertyByDescriptor(propertyDescriptor);
        IComparisonOperation restoredOperation =
            restoredProperty.getComparsonOperationByDescriptor(operationDescriptor);

        restoredCriteria.setProperty(restoredProperty);
        restoredCriteria.setComparisonOperation(restoredOperation);
        restoredCriteria.setValue(value);

        assertEquals("The criteria has been restored incorrectly.", criteria, restoredCriteria);
        assertEquals("The query has been restored incorrectly.", query, restoredQuery);
    }

    /**
     * For OPML import/export and synchronizations we will need serializing queries to string.
     */
    public void testSerializingDeserializingQuery()
    {
        Query sourceQuery = new Query();

        // Article Date
        addCriteria(sourceQuery, ArticleDateProperty.INSTANCE, DateMatchCO.INSTANCE, IDates.VALUE_TODAY);
        addCriteria(sourceQuery, ArticleDateProperty.INSTANCE, DateBeforeCO.INSTANCE, IDates.VALUE_TWO_WEEKS_AGO);
        addCriteria(sourceQuery, ArticleDateProperty.INSTANCE, DateAfterCO.INSTANCE, IDates.VALUE_YESTERDAY);

        // Article Status
        addCriteria(sourceQuery, ArticleStatusProperty.INSTANCE, StringEqualsCO.INSTANCE, ArticleStatusProperty.VALUE_READ);
        addCriteria(sourceQuery, ArticleStatusProperty.INSTANCE, StringEqualsCO.INSTANCE, ArticleStatusProperty.VALUE_UNREAD);

        // Article Text
        addCriteria(sourceQuery, ArticleTextProperty.INSTANCE, StringContainsCO.INSTANCE, "a");
        addCriteria(sourceQuery, ArticleTextProperty.INSTANCE, StringNotContainsCO.INSTANCE, "b");

        // Feed Starz
        addCriteria(sourceQuery, FeedStarzProperty.INSTANCE, StringEqualsCO.INSTANCE, "3");
        addCriteria(sourceQuery, FeedStarzProperty.INSTANCE, LongLessCO.INSTANCE, "3");
        addCriteria(sourceQuery, FeedStarzProperty.INSTANCE, LongGreaterCO.INSTANCE, "3");

        // Feed Title
        addCriteria(sourceQuery, FeedTitleProperty.INSTANCE, StringEqualsCO.INSTANCE, "a;b:c/d\\e");
        addCriteria(sourceQuery, FeedTitleProperty.INSTANCE, StringNotEqualsCO.INSTANCE, "b f");
        addCriteria(sourceQuery, FeedTitleProperty.INSTANCE, StringContainsCO.INSTANCE, "c");
        addCriteria(sourceQuery, FeedTitleProperty.INSTANCE, StringNotContainsCO.INSTANCE, "d");

        sourceQuery.setAndQuery(!sourceQuery.isAndQuery());

        String serializedQuery = sourceQuery.serializeToString();

        Query destQuery = Query.deserializeFromString(serializedQuery);

        assertEquals(sourceQuery, destQuery);
    }

    private ICriteria addCriteria(Query aQuery, IProperty aProperty,
        IComparisonOperation aComparisonOperation, String aValue)
    {
        ICriteria criteria = aQuery.addCriteria();
        criteria.setProperty(aProperty);
        criteria.setComparisonOperation(aComparisonOperation);
        criteria.setValue(aValue);

        return criteria;
    }

    /**
     * This is scenario of using query for matching.
     */
    public void testUsingQuery()
    {
        // Create a query from some property, operation and value
        IProperty textProperty = query.getPropertyByDescriptor("text");
        IComparisonOperation eqOperation = textProperty.getComparsonOperationByDescriptor("is");

        assertNotNull(textProperty);
        assertNotNull(eqOperation);

        ICriteria criteria = query.addCriteria();
        criteria.setProperty(textProperty);
        criteria.setComparisonOperation(eqOperation);
        criteria.setValue("a");

        // Check the simple query matching
        String matchingObject = "a";
        String nonmatchingObject = "b";

        assertTrue("Matching object has been told.", query.match(matchingObject));
        assertFalse("Non-matching object has been told.", query.match(nonmatchingObject));
    }

    /**
     * This is a simple query clonning scenario.
     */
    public void testClonning()
    {
        Query query = new Query();
        query.setAndQuery(!query.isAndQuery());
        ICriteria criteria1 = query.addCriteria();
        criteria1.setProperty(FeedTitleProperty.INSTANCE);
        criteria1.setComparisonOperation(StringEqualsCO.INSTANCE);
        criteria1.setValue("a");
        ICriteria criteria2 = query.addCriteria();
        criteria2.setProperty(ArticleDateProperty.INSTANCE);
        criteria2.setComparisonOperation(DateBeforeCO.INSTANCE);
        criteria2.setValue("today");

        Query cloneQuery = query.getClone();

        assertEquals(query, cloneQuery);
    }

    public void testEscapeValue()
    {
        String value = "a;b:c\\";
        String escaped = "a\\;b:c\\\\";

        assertEquals(escaped, Query.escapeValue(value));
        assertEquals(value, Query.unescapeValue(escaped));
    }
}
