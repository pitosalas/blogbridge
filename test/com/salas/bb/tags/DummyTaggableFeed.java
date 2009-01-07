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
// $Id: DummyTaggableFeed.java,v 1.9 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.tags;

import com.salas.bb.domain.ITaggable;

import java.net.URL;

/**
 * Sample feed object which is taggable.
 */
public class DummyTaggableFeed implements ITaggable
{
    private String[] userTags = null;
    private String[] sharedTags = null;

    /**
     * Returns user tags.
     *
     * @return user tags.
     */
    public String[] getUserTags()
    {
        return userTags;
    }

    /**
     * Sets user tags.
     *
     * @param aTags user tags.
     */
    public void setUserTags(String[] aTags)
    {
        userTags = aTags;
    }

    /**
     * Returns shared tags.
     *
     * @return shared tags.
     */
    public String[] getSharedTags()
    {
        return sharedTags;
    }

    /**
     * Sets shared tags.
     *
     * @param tags shared tags.
     */
    public void setSharedTags(String[] tags)
    {
        sharedTags = tags;
    }

    /**
     * Returns tags assigned by author.
     *
     * @return author tags.
     */
    public String[] getAuthorTags()
    {
        return new String[0];
    }

    /**
     * Returns the name of taggable object type.
     *
     * @return type name.
     */
    public String getTaggableTypeName()
    {
        return null;
    }

    /**
     * Returns link which can be tagged at the service (BB or third-party).
     *
     * @return link or <code>NULL</code> if tagging isn't supported.
     */
    public URL getTaggableLink()
    {
        return null;
    }

    /**
     * Returns <code>TRUE</code> if this object has unsaved user tags.
     *
     * @return <code>TRUE</code> if this object has unsaved user tags.
     */
    public boolean hasUnsavedUserTags()
    {
        return false;
    }

    /**
     * Sets unsaved user tags flag.
     *
     * @param unsaved <code>TRUE</code> if this object has unsaved user tags.
     */
    public void setUnsavedUserTags(boolean unsaved)
    {
    }

    /**
     * Returns the description of tags.
     *
     * @return description.
     */
    public String getTagsDescription()
    {
        return null;
    }

    /**
     * Returns title of this taggable object.
     *
     * @return title.
     */
    public String getTitle()
    {
        return null;
    }

    /**
     * Sets new description text.
     *
     * @param description new description text.
     */
    public void setTagsDescription(String description)
    {
    }

    /**
     * Returns tags extended description text.
     *
     * @return tags extended description text.
     */
    public String getTagsExtended()
    {
        return null;
    }

    /**
     * Tests new tags extended description text.
     *
     * @param extended new extended description.
     */
    public void setTagsExtended(String extended)
    {
    }
}
