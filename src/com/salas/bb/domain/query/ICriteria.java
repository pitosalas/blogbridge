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
// $Id: ICriteria.java,v 1.3 2006/01/08 04:48:16 kyank Exp $
//

package com.salas.bb.domain.query;

import java.beans.PropertyChangeListener;

/**
 * Criteria defines the comparison rule to be used to match against some target object.
 * It has property, comparison operation and value. The implementations of this type
 * play only the role of data holders, which are capable to verify themselves and return
 * error message if the information is incomplete.
 */
public interface ICriteria
{
    /** Value property name. */
    String PROP_VALUE = "value";

    /**
     * Validates the data of this criteria object and returns error message.
     *
     * @return error message or <code>NULL</code> if everything is OK.
     */
    String validate();

    /**
     * Sets the property to be used in this criteria.
     *
     * @param property property to set.
     */
    void setProperty(IProperty property);

    /**
     * Sets the comparison operation.
     *
     * @param operation operation.
     */
    void setComparisonOperation(IComparisonOperation operation);

    /**
     * Sets the value to use in conjuntion with comparison operation.
     *
     * @param value value to use in conjuntion with comparison operation.
     */
    void setValue(String value);

    /**
     * Returns current property.
     *
     * @return property.
     */
    IProperty getProperty();

    /**
     * Returns comparison operation.
     *
     * @return comparison operation.
     */
    IComparisonOperation getComparisonOperation();

    /**
     * Value for comparison.
     *
     * @return value.
     */
    String getValue();

    /**
     * Adds property change listener.
     *
     * @param l listener.
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l listener.
     */
    void removePropertyChangeListener(PropertyChangeListener l);
}
