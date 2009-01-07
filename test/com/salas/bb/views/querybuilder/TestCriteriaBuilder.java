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
// $Id: TestCriteriaBuilder.java,v 1.3 2006/01/08 05:28:35 kyank Exp $
//

package com.salas.bb.views.querybuilder;

import junit.framework.TestCase;
import com.salas.bb.domain.query.*;

import java.util.Collection;

/**
 * This suite contains tests for <code>CriteriaBuilder</code> unit.
 * It covers:
 * <ul>
 *  <li>TODO: put here</li>
 * </ul>
 */
public class TestCriteriaBuilder extends TestCase
{
    private IQuery dummyQuery;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        dummyQuery = new DummyStringQuery();
    }

    /**
     * Tests assigning of fresh (just created) criteria, which isn't initialized yet.
     * The component should select the first property from its list, first available
     * comparison operation and initialize the value with property default.
     */
    public void testAssignNewCriteria()
    {
        ICriteria newCriteria = createNewCriteria();
        Collection properties = dummyQuery.getAvailableProperties();
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(properties, newCriteria);

        IProperty reqProperty = (IProperty)properties.toArray()[0];
        IComparisonOperation reqOperation =
            (IComparisonOperation)reqProperty.getComparsonOperations().toArray()[0];
        String reqValue = reqProperty.getDefaultValue();

        checkInitOfBuilder(criteriaBuilder, reqProperty, reqOperation, reqValue);
    }

    /**
     * Tests assigning of some existing criteria. The component should load the values
     * from criteria into its controls.
     */
    public void testAssignExistingCriteria()
    {
        ICriteria existingCriteria = createExistingCriteria();
        IProperty reqProperty = existingCriteria.getProperty();
        IComparisonOperation reqOperation = existingCriteria.getComparisonOperation();
        String reqValue = existingCriteria.getValue();

        Collection properties = dummyQuery.getAvailableProperties();
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(properties, existingCriteria);

        checkInitOfBuilder(criteriaBuilder, reqProperty, reqOperation, reqValue);
    }

    private static void checkInitOfBuilder(CriteriaBuilder aCriteriaBuilder, IProperty aReqProperty,
        IComparisonOperation aReqOperation, String aReqValue)
    {
        IProperty selProperty = aCriteriaBuilder.getSelectedProperty();
        assertEquals("Wrong property selected: ", aReqProperty, selProperty);

        IComparisonOperation selOperation =
            aCriteriaBuilder.getSelectedComparisonOperation();
        assertEquals("Wrong operation selected.", aReqOperation, selOperation);

        String entValue = aCriteriaBuilder.getEnteredValue();
        assertEquals("Wrong value entered.", aReqValue, entValue);
    }

    // ---------------------------------------------------------------------------------------------

    private ICriteria createExistingCriteria()
    {
        ICriteria criteria = createNewCriteria();
        configureCriteria(criteria);

        return criteria;
    }

    private void configureCriteria(ICriteria aCriteria)
    {
        IProperty someProperty = new DummyObjectHashProperty();
        IComparisonOperation someOperation = (IComparisonOperation)
            someProperty.getComparsonOperations().toArray()[1];
        String someValue = "2";

        aCriteria.setProperty(someProperty);
        aCriteria.setComparisonOperation(someOperation);
        aCriteria.setValue(someValue);
    }

    private ICriteria createNewCriteria()
    {
        return dummyQuery.addCriteria();
    }

    /**
     * Dummy query, encorporating text and hash properties.
     */
    private static class DummyStringQuery extends BasicQuery
    {
        /**
         * Creates basic query.
         */
        public DummyStringQuery()
        {
            // The order of properties is intentional to test second property selection
            super(new IProperty[] { new DummyObjectTextProperty(), new DummyObjectHashProperty() });
        }
    }
}
