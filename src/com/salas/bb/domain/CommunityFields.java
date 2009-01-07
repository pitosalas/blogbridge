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
// $Id: CommunityFields.java,v 1.3 2006/01/08 04:48:16 kyank Exp $
//

package com.salas.bb.domain;

import java.util.Hashtable;
import java.util.Map;

/**
 * Holder for community fields.
 */
public class CommunityFields extends Hashtable
{
    /**
     * Country of origin field.
     */
    public static final String KEY_COUNTRY  = "country";

    /**
     * Tags field.
     */
    public static final String KEY_TAGS     = "tags";

    /**
     * Create empty map.
     */
    public CommunityFields()
    {
    }

    /**
     * Load properties from the other map.
     *
     * @param t map.
     */
    public CommunityFields(Map t)
    {
        super(t);
    }

    /**
     * Returns country of origin.
     *
     * @return country or null if not set.
     */
    public String getCountryOfOrigin()
    {
        String[] country = (String[])get(KEY_COUNTRY);

        return country == null || country.length == 0 ? null : country[0];
    }

    /**
     * Sets country of origin.
     *
     * @param country country of origin.
     */
    public void setCountryOfOrigin(String country)
    {
        put(KEY_COUNTRY, new String[] { country });
    }

    /**
     * Returns the tags list or null if not set.
     *
     * @return tags list or null.
     */
    public String[] getTags()
    {
        return (String[])get(KEY_TAGS);
    }

    /**
     * Sets the tags list.
     *
     * @param tags tags list.
     */
    public void setTags(String[] tags)
    {
        put(KEY_TAGS, tags);
    }
}
