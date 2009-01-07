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
// $Id: TagsUtils.java,v 1.2 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.tags;

import com.salas.bb.utils.StringUtils;

import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Set of simple utils for working with tags.
 */
public final class TagsUtils
{
    /**
     * Hidden utility class constructor.
     */
    private TagsUtils()
    {
    }

    /**
     * Creates a quick summary of tags. For example, "test (5), java (2), fun".
     *
     * @param aTags tags.
     *
     * @return summary.
     */
    public static String createTagsSummary(String[] aTags)
    {
        String summary = null;

        if (aTags != null)
        {
            String[] tagGroups = groupTags(aTags);
            summary = StringUtils.join(tagGroups, ", ");
        }

        return summary;
    }

    /**
     * Groups tags and adds counters to the tails. For example, "a, c, a, b" will be
     * grouped into "a (2), b, c". Note that sorting happens also and tags are converted
     * to lower case.
     *
     * @param aTags tags list.
     *
     * @return groupped tags list.
     */
    private static String[] groupTags(String[] aTags)
    {
        Map tagsMap = buildSortedTagsMap(aTags);
        return convertTagsMapToGroups(tagsMap);
    }

    /**
     * Converts the tags map (tag:count) into the list of groupped tags.
     *
     * @param aTagsMap map.
     *
     * @return sorted groupped list of tags.
     */
    private static String[] convertTagsMapToGroups(Map aTagsMap)
    {
        String[] groups = new String[aTagsMap.size()];
        Iterator iterator = aTagsMap.entrySet().iterator();

        int i = 0;
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry)iterator.next();
            String tag = (String)entry.getKey();
            int count = ((Integer)entry.getValue()).intValue();

            if (count > 1) tag += " (" + count + ")";
            groups[i++] = tag;
        }

        return groups;
    }

    /**
     * Converts the list of tags into the map (tag:count).
     *
     * @param aTags tags.
     *
     * @return map.
     */
    private static Map buildSortedTagsMap(String[] aTags)
    {
        Map tagsMap = new TreeMap();

        for (int i = 0; i < aTags.length; i++)
        {
            String tag = aTags[i].toLowerCase();
            Integer countI = (Integer)tagsMap.get(tag);
            int count = (countI == null) ? 1 : countI.intValue() + 1;
            tagsMap.put(tag, new Integer(count));
        }

        return tagsMap;
    }
}
