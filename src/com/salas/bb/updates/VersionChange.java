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
// $Id: VersionChange.java,v 1.2 2006/01/08 04:57:15 kyank Exp $
//

package com.salas.bb.updates;

/**
 * Single version change item, which describes the type and details of
 * a change in some version.
 */
public class VersionChange
{
    public static final int TYPE_FEATURE    = 0;
    public static final int TYPE_FIX        = 1;

    private int     type;
    private String  details;

    /**
     * Creates empty version change object.
     */
    public VersionChange()
    {
    }

    /**
     * Creates version change object.
     *
     * @param aType         type of change.
     * @param aDetails      details on change.
     *
     * @see #TYPE_FEATURE
     * @see #TYPE_FIX
     */
    public VersionChange(int aType, String aDetails)
    {
        type = aType;
        details = aDetails;
    }

    /**
     * Returns type of change.
     *
     * @return type of change.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Sets type of change.
     *
     * @param aType type of change.
     */
    public void setType(int aType)
    {
        type = aType;
    }

    /**
     * Returns details.
     *
     * @return details.
     */
    public String getDetails()
    {
        return details;
    }

    /**
     * Sets details.
     *
     * @param aDetails details.
     */
    public void setDetails(String aDetails)
    {
        details = aDetails;
    }

    /**
     * Return <code>TRUE</code> if two objects are equivalent.
     *
     * @param o other object.
     *
     * @return <code>TRUE</code> if two objects are equivalent.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VersionChange that = (VersionChange)o;

        if (type != that.type) return false;
        if (!details.equals(that.details)) return false;

        return true;
    }

    /**
     * Returns hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return type;
    }
}
