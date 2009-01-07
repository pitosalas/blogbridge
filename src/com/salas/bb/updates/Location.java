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
// $Id: Location.java,v 1.8 2006/02/06 12:42:55 spyromus Exp $
//

package com.salas.bb.updates;

import com.salas.bb.utils.OSSettings;
import com.salas.bb.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Single distribution package location descriptor.
 */
public class Location
{
    public static final String TYPE_GENERIC = "tgz";
    public static final String TYPE_WINDOWS = "windows";
    public static final String TYPE_MAC     = "mac";
    public static final String TYPE_DEBIAN  = "debian";
    public static final String TYPE_GENTOO  = "gentoo";

    /**
     * List of package types applicable to the OS version.
     * First item in every list if MAIN package type.
     *
     * @see OSSettings#OS_TYPE_OTHER
     * @see OSSettings#OS_TYPE_WINDOWS
     * @see OSSettings#OS_TYPE_MAC
     * @see OSSettings#OS_TYPE_LINUX
     */
    private static final List[] TYPES = new List[]
    {
        Arrays.asList(new String[] { TYPE_GENERIC, TYPE_WINDOWS, TYPE_MAC, TYPE_DEBIAN }),
        Arrays.asList(new String[] { TYPE_WINDOWS, TYPE_GENERIC }),
        Arrays.asList(new String[] { TYPE_MAC, TYPE_GENERIC }),
        Arrays.asList(new String[] { TYPE_GENERIC, TYPE_DEBIAN })
    };

    private String  type;
    private String  description;
    private String  link;
    private String  filename;
    private long    size;

    /**
     * Creates location object.
     *
     * @param aType         type of package.
     * @param aDescription  description of package type.
     * @param aLink         URL to the package.
     * @param aSize         size of resource.
     */
    public Location(String aType, String aDescription, String aLink, long aSize)
    {
        type = aType;
        description = aDescription;
        link = aLink;
        size = aSize;

        if (link != null)
        {
            int lastPathSeparator = link.lastIndexOf('/');
            if (lastPathSeparator != -1)
            {
                filename = link.substring(lastPathSeparator + 1);
            }

            if (StringUtils.isEmpty(filename)) filename = null;
        }
    }

    /**
     * Returns type of the package.
     *
     * @return type of the package.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns description of package type.
     *
     * @return description of package type.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns link to the package.
     *
     * @return link to the package.
     */
    public String getLink()
    {
        return link;
    }

    /**
     * Returns size of resource.
     *
     * @return size of resource.
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Returns the name of file.
     *
     * @return file name.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns <code>TRUE</code> if objects are equal.
     *
     * @param o other object to compare to.
     *
     * @return <code>TRUE</code> if objects are equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Location that = (Location)o;

        if (!link.equals(that.link)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    /**
     * Returns hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return type.hashCode();
    }

    /**
     * Returns the list of packages types, which are applicable to
     * current operation system.
     *
     * @return list of types.
     */
    public static List getApplicableTypes()
    {
        return TYPES[OSSettings.getOSType()];
    }

    /**
     * Returns the list of locations applicable to current OS from the given list.
     *
     * @param locations list of locations.
     *
     * @return applicable locations.
     */
    public static Location[] selectApplicable(Location[] locations)
    {
        List applicableTypes = getApplicableTypes();

        List applicable = new ArrayList(locations.length);
        for (int i = 0; i < locations.length; i++)
        {
            Location location = locations[i];
            if (applicableTypes.contains(location.getType())) applicable.add(location);
        }

        return (Location[])applicable.toArray(new Location[applicable.size()]);
    }

    /**
     * Returns <code>TRUE</code> if main installer is between the list of downloaded.
     *
     * @param aLocations    list of downloaded locations.
     *
     * @return main package or <code>NULL</code> if it's not in the list.
     */
    public static Location getMainPackage(List aLocations)
    {
        Location main = null;

        String mainType = (String)getApplicableTypes().get(0);
        for (int i = 0; main == null && i < aLocations.size(); i++)
        {
            Location location = (Location)aLocations.get(i);
            if (location.getType().equals(mainType)) main = location;
        }

        return main;
    }
}
