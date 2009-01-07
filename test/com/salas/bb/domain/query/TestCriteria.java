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
// $Id: TestCriteria.java,v 1.8 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain.query;

import junit.framework.TestCase;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.general.LongLessCO;

/**
 * This suite contains tests for <code>AbstractCriteria</code> unit.
 */
public class TestCriteria extends TestCase
{
    private Criteria criteria;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        criteria = new Criteria();
    }

    /**
     * Tests validation.
     */
    public void testValidation()
    {
        assertEquals(Criteria.ERR_PROPERTY_NOT_SPECIFIED, criteria.validate());

        IProperty textProperty = new DummyObjectTextProperty();
        criteria.setProperty(textProperty);
        assertEquals(Criteria.ERR_OPERATION_NOT_SPECIFIED, criteria.validate());

        IComparisonOperation unsupportedOperation = new LongLessCO();
        criteria.setComparisonOperation(unsupportedOperation);
        assertEquals(Criteria.ERR_OPERATION_NOT_SUPPORTED, criteria.validate());

        IComparisonOperation supportedOperation = new StringEqualsCO();
        criteria.setComparisonOperation(supportedOperation);
        assertEquals(IProperty.ERROR_VALUE_NOT_SPECIFIED, criteria.validate());

        criteria.setValue("a");
        assertNull(criteria.validate());
    }
}
