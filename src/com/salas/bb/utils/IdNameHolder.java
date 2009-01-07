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
// $Id: IdNameHolder.java,v 1.2 2007/01/31 14:33:11 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.StringUtils;

import java.util.prefs.Preferences;

/**
 * Simple ID - name holder.
 */
public class IdNameHolder implements Comparable
{
    public final String id;
    public final String name;

    /**
     * Creates holder.
     *
     * @param id    id.
     * @param name  name.
     */
    public IdNameHolder(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param o the object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    public int compareTo(Object o)
    {
        IdNameHolder h = (IdNameHolder)o;

        return name.compareTo(h.name);
    }

    /**
     * Returns holder taken from the preferences.
     *
     * @param prefs preferences.
     * @param prop  property name.
     *
     * @return holder or <code>NULL</code>.
     */
    protected static IdNameHolder restore0(Preferences prefs, String prop)
    {
        IdNameHolder hld = null;

        String val = prefs.get(prop, null);
        if (StringUtils.isNotEmpty(val))
        {
            int i = val.indexOf('-');
            if (i > 0 && val.length() > i + 1)
            {
                try
                {
                    String id = val.substring(0, i);
                    String name = val.substring(i + 1);

                    hld = new IdNameHolder(id, name);
                } catch (NumberFormatException e)
                {
                    // Invalid ID format. Skipping...
                }
            }
        }

        return hld;
    }

    /**
     * Stores this holder in preferences.
     *
     * @param prefs preferences.
     * @param prop  property name.
     * @param obj   holder.
     */
    public static void store(Preferences prefs, String prop, IdNameHolder obj)
    {
        if (obj != null) obj.store(prefs, prop);
    }

    /**
     * Stores this holder in preferences.
     *
     * @param prefs preferences.
     * @param prop  property name.
     */
    public void store(Preferences prefs, String prop)
    {
        String val = id + "-" + name;
        prefs.put(prop, val);
    }

    /**
     * Returns <code>TRUE</code> if objects are equal.
     *
     * @param o second object.
     *
     * @return <code>TRUE</code> if objects are equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdNameHolder that = (IdNameHolder)o;

        if (!id.equals(that.id)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    /**
     * Returns the hash code of this object.
     *
     * @return code.
     */
    public int hashCode()
    {
        return id.hashCode();
    }
}
