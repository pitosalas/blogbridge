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
// $Id: AbstractProperty.java,v 1.5 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query;

import java.util.*;

/**
 * Base implementation of <code>IProperty</code>. It takes care of supported comparison
 * operations queries, property type, descriptor and default value returning. Allows to
 * concentrate exactly on property matching which is specific to each property object. 
 */
public abstract class AbstractProperty implements IProperty
{
    private final String        name;
    private final String        descriptor;
    private final PropertyType  type;
    private final String        defaultValue;
    private final List<IComparisonOperation> operations;

    /**
     * Creates propery with given descriptor, type, default value and list of supported
     * comparison operations.
     *
     * @param aName                 the name of the property.
     * @param aDescriptor           descriptor.
     * @param aType                 type.
     * @param aDefaultValue         default value.
     * @param aComparisonOperations list of supported comparison operations.
     */
    protected AbstractProperty(String aName, String aDescriptor, PropertyType aType, String aDefaultValue,
                               IComparisonOperation[] aComparisonOperations)
    {
        name = aName;
        descriptor = aDescriptor;
        type = aType;
        defaultValue = aDefaultValue;

        operations = Arrays.asList(aComparisonOperations);
    }

    /**
     * Returns type of the property.
     *
     * @return type of the property.
     */
    public PropertyType getType()
    {
        return type;
    }

    /**
     * The name of this property.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Descriptor for this property. Useful when serializing the criteria.
     *
     * @return descriptor.
     */
    public String getDescriptor()
    {
        return descriptor;
    }

    /**
     * Returns default value which can be used when creating the criteria for the property.
     *
     * @return default property value of criteria objects.
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Returns all acceptable comparison operations.
     *
     * @return operations.
     */
    public Collection getComparsonOperations()
    {
        return Collections.unmodifiableList(operations);
    }

    /**
     * Returns comparison operation by its descriptor.
     *
     * @param descriptor descriptor of operation.
     *
     * @return operation or NULL if no such operation supported.
     */
    public IComparisonOperation getComparsonOperationByDescriptor(String descriptor)
    {
        if (descriptor == null) return null;
        
        for (IComparisonOperation operation : operations)
        {
            if (descriptor.equals(operation.getDescriptor()) ||
                descriptor.equals(operation.getName())) return operation;
        }

        return null;
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
        return value == null ? ERROR_VALUE_NOT_SPECIFIED : null;
    }

    /**
     * Compares this object to the other.
     *
     * @param o other object.
     *
     * @return <code>TRUE</code> if the other property is identical to this one.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractProperty abstractProperty = (AbstractProperty)o;

        if (defaultValue != null ? !defaultValue.equals(abstractProperty.defaultValue) : abstractProperty.defaultValue != null)
            return false;
        if (!descriptor.equals(abstractProperty.descriptor)) return false;
        if (!name.equals(abstractProperty.name)) return false;
        if (!type.equals(abstractProperty.type)) return false;

        return true;
    }

    /**
     * Returns hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return descriptor.hashCode();
    }

    /**
     * Returns string representation of property.
     *
     * @return string representation.
     */
    public String toString()
    {
        return name;
    }
}
