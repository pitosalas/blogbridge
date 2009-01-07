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
// $Id: DeliciousTags.java,v 1.3 2008/04/01 10:55:54 spyromus Exp $
//

package com.salas.bb.utils.net.delicious;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holder for user-tags pair.
 */
public class DeliciousTags
{
    private String      user;
    private String[]    tags;

    /**
     * Creates holder.
     *
     * @param aUser user name.
     * @param aTags tags list.
     */
    public DeliciousTags(String aUser, String[] aTags)
    {
        user = aUser;
        tags = aTags;
    }

    /**
     * Returns name of user.
     *
     * @return user.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Returns tags.
     *
     * @return tags.
     */
    public String[] getTags()
    {
        return tags;
    }

    /**
     * Scans given tags and return tags owned by user with given name.
     *
     * @param tags  tags.
     * @param user  user.
     *
     * @return tags or <code>NULL</code> if tags aren't found.
     */
    public static String[] findTagsByUser(DeliciousTags[] tags, String user)
    {
        String[] userTags = null;

        for (int i = 0; userTags == null && i < tags.length; i++)
        {
            DeliciousTags t = tags[i];
            if (t.getUser().equals(user)) userTags = t.getTags();
        }

        return userTags;
    }

    /**
     * Scans given tags and return tags which aren't owned by user with given name.
     *
     * @param tags  tags.
     * @param user  user.
     *
     * @return tags or <code>NULL</code> if tags aren't found.
     */
    public static SortedTags filterTagsByUser(DeliciousTags[] tags, String user)
    {
        ArrayList<String> userTags      = new ArrayList<String>(tags.length);
        ArrayList<String> othersTags    = new ArrayList<String>(tags.length);
        
        for (DeliciousTags t : tags)
        {
            List<String> ttags = Arrays.asList(t.getTags());

            if (user == null || !t.getUser().equals(user))
            {
                othersTags.addAll(ttags);
            } else
            {
                userTags.addAll(ttags);
            }
        }

        return new SortedTags(userTags.toArray(new String[userTags.size()]),
            othersTags.toArray(new String[othersTags.size()]));
    }

    /**
     * Simple holder for user / other's tags.
     */
    public static class SortedTags
    {
        public final String[] userTags;
        public final String[] othersTags;

        public SortedTags(String[] userTags, String[] othersTags)
        {
            this.userTags = userTags;
            this.othersTags = othersTags;
        }
    }
}
