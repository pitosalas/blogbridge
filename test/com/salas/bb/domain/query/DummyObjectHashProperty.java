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
// $Id: DummyObjectHashProperty.java,v 1.11 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query;

import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.general.LongLessCO;

/**
 * Dummy object hash property.
 */
public class DummyObjectHashProperty extends AbstractProperty
{
    /**
     * Creates propery with given descriptor, type, default value and list of supported comparison
     * operations.
     */
    public DummyObjectHashProperty()
    {
        super("hashCode", "hashCode", PropertyType.LONG, "1", new IComparisonOperation[]
            { LongLessCO.INSTANCE, StringEqualsCO.INSTANCE });
    }

    /**
     * Compares the corresponding property of the target object to the value using specific
     * comparison operation.
     *
     * @param target    target object.
     * @param operation comparison operation, supported by this property.
     * @param value     value to compare against.
     *
     * @return TRUE if the object matches the criteria.
     *
     * @throws NullPointerException     if target, operation or value are NULL's.
     * @throws ClassCastException       if target is not supported.
     * @throws IllegalArgumentException if operation is not supported.
     */
    public boolean match(Object target, IComparisonOperation operation, String value)
    {
        return operation.match(Integer.toString(target.hashCode()), value);
    }

    /**
     * Validates the value and tells if the value has incorrect format.
     *
     * @param operation operation which is going to happen.
     * @param value     value.
     *
     * @return error message or <code>NULL</code> in successful case.
     */
    public String validateValue(IComparisonOperation operation, String value)
    {
        String errorMessage = super.validateValue(operation, value);

        if (errorMessage == null)
        {
            try
            {
                Integer.parseInt(value.trim());
            } catch (NumberFormatException e)
            {
                errorMessage = "Wrong integer property format.";
            }
        }

        return errorMessage;
    }
}
