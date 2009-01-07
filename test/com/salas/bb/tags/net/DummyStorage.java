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
// $Id: DummyStorage.java,v 1.3 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.tags.net;

import com.salas.bb.domain.ITaggable;

/**
 * Dummy handler which is accessing nothing. It holds user and shared tags lists
 * and loads them into any taggable object. Also, supports replacing own list
 * of user tags through storing them from taggable object.
 */
public class DummyStorage implements ITagsStorage
{
    private String[] sharedTags;

    /**
     * Creates dummy handler.
     *
     * @param aSharedTags   shared tags.
     */
    public DummyStorage(String[] aSharedTags)
    {
        sharedTags = aSharedTags;
    }

    /**
     * Loads shared tags in the way, specific to this storage.
     *
     * @param aTaggable taggable object.
     *
     * @throws NullPointerException if object isn't specified.
     */
    public void loadSharedTags(ITaggable aTaggable)
    {
        aTaggable.setSharedTags(sharedTags);
    }

    /**
     * Stores user tags in the way, specific to this handler.
     *
     * @param aTaggable taggable object.
     *
     * @throws NullPointerException if object isn't specified.
     */
    public void storeUserTags(ITaggable aTaggable)
    {
    }
}
