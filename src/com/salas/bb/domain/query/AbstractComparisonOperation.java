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
// $Id: AbstractComparisonOperation.java,v 1.3 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query;

/**
 * Abstract implementation of comparison operation.
 */
public abstract class AbstractComparisonOperation implements IComparisonOperation
{
    private final String name;
    private final String descriptor;

    /**
     * Creates operation.
     *
     * @param aName name.
     * @param aDescriptor descriptor of operation.
     */
    protected AbstractComparisonOperation(String aName, String aDescriptor)
    {
        name = aName;
        descriptor = aDescriptor;
    }

    /**
     * Returns the operation name.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Comparison operation descriptor.
     *
     * @return descriptor.
     */
    public String getDescriptor()
    {
        return descriptor;
    }

    /**
     * Compares the other object with this one.
     *
     * @param o object to compare.
     *
     * @return TRUE if objects are identical.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractComparisonOperation otherOperation = (AbstractComparisonOperation)o;

        if (!descriptor.equals(otherOperation.descriptor)) return false;
        if (!name.equals(otherOperation.name)) return false;

        return true;
    }

    /**
     * Returns the hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return descriptor.hashCode();
    }

    /**
     * Returns string representation of operation.
     *
     * @return string representation.
     */
    public String toString()
    {
        return name;
    }
}
