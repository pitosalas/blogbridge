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
// $Id: PropertyType.java,v 1.7 2008/02/28 09:58:32 spyromus Exp $
//

package com.salas.bb.domain.query;

/**
 * This class represents the enumeration of all possible property types.
 */
public final class PropertyType
{
    /** String property type. */
    public static final PropertyType STRING     = new PropertyType();

    /** Long/Integer property type. */
    public static final PropertyType LONG       = new PropertyType();

    /** Starz property type. */
    public static final PropertyType STARZ      = new PropertyType();

    /** Boolean property type. */
    public static final PropertyType BOOLEAN    = new PropertyType();

    /** Date property type. */
    public static final PropertyType DATE       = new PropertyType();

    /** Object status type (i.e., article - read/unread). */
    public static final PropertyType STATUS     = new PropertyType();

    /** Object sentiments type (i.e., article - positive/negative). */
    public static final PropertyType SENTIMENTS = new PropertyType();

    /** Set / unset property type. */
    public static final PropertyType SET_UNSET  = new PropertyType();

    /**
     * Hidden enumeration constructor.
     */
    private PropertyType()
    {
    }
}
