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
// $Id: IProperty.java,v 1.5 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query;

import com.salas.bb.utils.i18n.Strings;

import java.util.Collection;

/**
 * Property of some object type which is supported by some query. The property supports
 * finite set of comparison operations which are applicable to the property type and
 * can be used to build query criteria.
 *
 * Property object knows how to get the value from the target object and compare the
 * given value with it.
 */
public interface IProperty
{
    /** Indicates validation error when value wasn't specified. */
    String ERROR_VALUE_NOT_SPECIFIED = Strings.message("query.criteria.unspecified.value");

    /**
     * Returns all acceptable comparison operations.
     *
     * @return operations.
     */
    Collection getComparsonOperations();

    /**
     * Returns comparison operation by its descriptor.
     *
     * @param descriptor descriptor of operation.
     *
     * @return operation or NULL if no such operation supported.
     */
    IComparisonOperation getComparsonOperationByDescriptor(String descriptor);

    /**
     * Returns type of the property.
     *
     * @return type of the property.
     */
    PropertyType getType();

    /**
     * Returns default value which can be used when creating the criteria for the property.
     *
     * @return default property value of criteria objects.
     */
    String getDefaultValue();

    /**
     * The name of this property.
     *
     * @return name.
     */
    String getName();

    /**
     * Descriptor for this property. Useful when serializing the criteria.
     *
     * @return descriptor.
     */
    String getDescriptor();

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
     * @throws NullPointerException if target, operation or value are NULL's.
     * @throws ClassCastException if target is not supported.
     * @throws IllegalArgumentException if operation is not supported.
     */
    boolean match(Object target, IComparisonOperation operation, String value);

    /**
     * Validates the value and tells if the value has incorrect format.
     *
     * @param operation operation which is going to happen.
     * @param value     value.
     *
     * @return error message or <code>NULL</code> in successful case.
     *
     */
    String validateValue(IComparisonOperation operation, String value);
}
