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
// $Id: XMLFormat.java,v 1.1 2006/11/27 16:52:09 spyromus Exp $
//

package com.salas.bb.utils.discovery.detector;

/**
 * XML Format identifiers.
 */
public final class XMLFormat
{
    /** RSS */
    public static final XMLFormat RSS = new XMLFormat("RSS");

    /** RDF */
    public static final XMLFormat RDF = new XMLFormat("RDF");

    /** Atom */
    public static final XMLFormat ATOM = new XMLFormat("Atom");

    /** OPML */
    public static final XMLFormat OPML = new XMLFormat("OPML");

    private final String name;

    /**
     * Creates format.
     *
     * @param name format name.
     */
    public XMLFormat(String name)
    {
        this.name = name;
    }

    /**
     * Returns string representation.
     *
     * @return string representation.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Compares two objects.
     *
     * @param o another object.
     *
     * @return <code>TRUE</code> if equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XMLFormat xmlFormat = (XMLFormat)o;

        return name.equals(xmlFormat.name);
    }

    /**
     * Calculates the hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return name.hashCode();
    }
}
