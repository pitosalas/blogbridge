// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: NameFormat.java,v 1.1 2007/05/01 10:52:03 spyromus Exp $
//

package com.salas.bb.core.autosave;

/**
 * The format of the item being saved.
 */
public class NameFormat
{
    /** The collection of all available formats. */
    public static final NameFormat[] FORMATS = new NameFormat[]
    {
        new NameFormat("Title", "{title}"),
        new NameFormat("Title - Feed", "{title} - {feed}"),
        new NameFormat("Feed - Title", "{feed} - {title}"),
        new NameFormat("YYYY-MM-DD - Title", "{current.date,yyyy-MM-dd} - {title}"),
        new NameFormat("YYYY-MM-DD / Title", "{current.date,yyyy-MM-dd}/{title}"),
        new NameFormat("YYYY-MM-DD / Title - Feed", "{current.date,yyyy-MM-dd}/{title} - {feed}"),
        new NameFormat("YYYY-MM-DD / Feed - Title", "{current.date,yyyy-MM-dd}/{feed} - {title}"),
    };

    private final String name;
    private final String format;

    /**
     * Creates the format.
     *
     * @param name      name of the format.
     * @param format    format.
     */
    private NameFormat(String name, String format)
    {
        this.name = name;
        this.format = format;
    }

    /**
     * Returns the name of the format.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the format string.
     *
     * @return format string.
     */
    public String getFormat()
    {
        return format;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameFormat that = (NameFormat)o;

        if (!format.equals(that.format)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return format.hashCode();
    }
}
