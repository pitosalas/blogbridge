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
// $Id: ITaggable.java,v 1.11 2006/01/08 04:48:17 kyank Exp $
//

package com.salas.bb.domain;

import java.net.URL;

/**
 * Definition of taggable object interface.
 */
public interface ITaggable
{
    /** Name of shared tags property. */
    String PROP_SHARED_TAGS = "sharedTags";
    /** Name of user tags property. */
    String PROP_USER_TAGS = "userTags";
    /** Name of tags desciription property. */
    String PROP_TAGS_DESCRIPTION = "tagsDescription";
    /** Name of tags extended description property. */
    String PROP_TAGS_EXTENDED = "tagsExtended";
    /** Name of unsaved user tags property. */
    String PROP_UNSAVED_USER_TAGS = "unsavedUserTags";

    /**
     * Sets user tags.
     *
     * @param tags user tags.
     */
    void setUserTags(String[] tags);

    /**
     * Returns user tags.
     *
     * @return user tags.
     */
    String[] getUserTags();

    /**
     * Returns shared tags.
     *
     * @return shared tags.
     */
    String[] getSharedTags();

    /**
     * Sets shared tags.
     *
     * @param tags shared tags.
     */
    void setSharedTags(String[] tags);

    /**
     * Returns tags assigned by author.
     *
     * @return author tags.
     */
    String[] getAuthorTags();

    /**
     * Returns the name of taggable object type.
     *
     * @return type name.
     */
    String getTaggableTypeName();

    /**
     * Returns link which can be tagged at the service (BB or third-party).
     *
     * @return link or <code>NULL</code> if tagging isn't supported.
     */
    URL getTaggableLink();

    /**
     * Sets unsaved user tags flag.
     *
     * @param unsaved <code>TRUE</code> if this object has unsaved user tags.
     */
    void setUnsavedUserTags(boolean unsaved);

    /**
     * Returns <code>TRUE</code> if this object has unsaved user tags.
     *
     * @return <code>TRUE</code> if this object has unsaved user tags.
     */
    boolean hasUnsavedUserTags();

    /**
     * Returns the description of tags.
     *
     * @return description.
     */
    String getTagsDescription();

    /**
     * Returns title of this taggable object.
     *
     * @return title.
     */
    String getTitle();

    /**
     * Sets new description text.
     *
     * @param description new description text.
     */
    void setTagsDescription(String description);

    /**
     * Returns tags extended description text.
     *
     * @return tags extended description text.
     */
    String getTagsExtended();

    /**
     * Tests new tags extended description text.
     *
     * @param extended new extended description.
     */
    void setTagsExtended(String extended);
}
