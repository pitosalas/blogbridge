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
// $Id: Criteria.java,v 1.7 2006/05/31 08:55:21 spyromus Exp $
//

package com.salas.bb.domain.query;

import com.salas.bb.utils.i18n.Strings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Simple criteria holder.
 */
public class Criteria implements ICriteria
{
    /** Property isn't specified error text. */
    public static final String ERR_PROPERTY_NOT_SPECIFIED = Strings.message("query.criteria.unspecified.property");
    public static final String ERR_OPERATION_NOT_SPECIFIED = Strings.message("query.criteria.unspecified.operation");
    public static final String ERR_OPERATION_NOT_SUPPORTED = Strings.message("query.criteria.unsupported.operation");

    public IProperty            property;
    public IComparisonOperation operation;
    public String               value;
    private PropertyChangeSupport pcs;

    /**
     * Creates criteria object.
     */
    public Criteria()
    {
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Returns comparison operation.
     *
     * @return comparison operation.
     */
    public IComparisonOperation getComparisonOperation()
    {
        return operation;
    }

    /**
     * Sets the comparison operation.
     *
     * @param aOperation operation.
     */
    public void setComparisonOperation(IComparisonOperation aOperation)
    {
        operation = aOperation;
    }

    /**
     * Returns current property.
     *
     * @return property.
     */
    public IProperty getProperty()
    {
        return property;
    }

    /**
     * Sets the property to be used in this criteria.
     *
     * @param aProperty property to set.
     */
    public void setProperty(IProperty aProperty)
    {
        property = aProperty;
    }

    /**
     * Value for comparison.
     *
     * @return value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Adds property change listener.
     *
     * @param l listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     *
     * @param l listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Sets the value to use in conjuntion with comparison operation.
     *
     * @param aValue value to use in conjuntion with comparison operation.
     */
    public void setValue(String aValue)
    {
        String oldValue = value;
        value = aValue;
        pcs.firePropertyChange(PROP_VALUE, oldValue, value);
    }

    /**
     * Validates the data of this criteria object and returns error message.
     *
     * @return error message or <code>NULL</code> if everything is OK.
     */
    public String validate()
    {
        String errorMessage;

        errorMessage = validateProperty();
        if (errorMessage == null) errorMessage = validateOperation();
        if (errorMessage == null) errorMessage = validateValue();

        return errorMessage;
    }

    private String validateProperty()
    {
        return property == null ? ERR_PROPERTY_NOT_SPECIFIED : null;
    }

    private String validateOperation()
    {
        String errorMessage = null;

        if (operation == null)
        {
            errorMessage = ERR_OPERATION_NOT_SPECIFIED;
        } else if (!property.getComparsonOperations().contains(operation))
        {
            errorMessage = ERR_OPERATION_NOT_SUPPORTED;
        }

        return errorMessage;
    }

    private String validateValue()
    {
        return property.validateValue(operation, value);
    }

    /**
     * Compares this object to another one.
     *
     * @param o another object.
     *
     * @return <code>TRUE</code> if two objects are the same.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Criteria criteria = (Criteria)o;

        if (operation != null ? !operation.equals(criteria.operation) : criteria.operation != null)
            return false;
        if (property != null ? !property.equals(criteria.property) : criteria.property != null)
            return false;
        if (value != null ? !value.equals(criteria.value) : criteria.value != null) return false;

        return true;
    }

    /**
     * Returns the hash code of this object.
     *
     * @return code.
     */
    public int hashCode()
    {
        int result;
        result = (property != null ? property.hashCode() : 0);
        result = 29 * result + (operation != null ? operation.hashCode() : 0);
        return result;
    }
}
