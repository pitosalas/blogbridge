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
// $Id: TagsRepository.java,v 1.4 2007/05/14 15:50:33 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.utils.net.delicious.DeliciousService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * In this repository we store the tags used by user. It is capable of collecting
 * tags from different sources: local taggable objects (through scanning),
 * remotely used tags (through questioning the del.icio.us service).
 */
public final class TagsRepository
{
    private static final TagsRepository INSTANCE = new TagsRepository();

    private Set<String> usedTags;

    /**
     * Hidden singleton constructor.
     */
    TagsRepository()
    {
        usedTags = new TreeSet<String>();
    }

    /**
     * Returns instance of repository.
     *
     * @return instance.
     */
    public static TagsRepository getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns unmodifiable set of used tags.
     *
     * @return used tags.
     */
    public Set<String> getUsedTags()
    {
        return Collections.unmodifiableSet(usedTags);
    }

    /**
     * Loads the tags into the set of used tags from delicious service using
     * given user/password pair.
     *
     * @param username  name of delicious user.
     * @param password  password of the user.
     *
     * @throws NullPointerException if user or password aren't specified.
     * @throws IllegalArgumentException if user or password are empty.
     * @throws IOException if communication to service fails.
     */
    public void loadTagsFromDelicious(String username, String password)
        throws IOException
    {
        String[] userTags = DeliciousService.getUserTags(username, password);

        addMissingTags(userTags);
    }

    /**
     * Loads missing tags from the guides set.
     *
     * @param set set to examine.
     */
    public void loadFromGuidesSet(GuidesSet set)
    {
        Collection<IFeed> feeds = set.getFeeds();
        for (IFeed feed : feeds)
        {
            addMissingTagsFromTaggable(feed);

            IArticle[] articles = feed.getArticles();
            for (IArticle article : articles)
            {
                addMissingTagsFromTaggable(article);
            }
        }
    }

    /**
     * If the object is taggable and has some user-defined tags then they will
     * be used for addition of missing tags to repository.
     *
     * @param object object to examine.
     */
    void addMissingTagsFromTaggable(Object object)
    {
        if (object instanceof ITaggable)
        {
            ITaggable taggable = (ITaggable)object;
            String[] userTags = taggable.getUserTags();
            if (userTags != null) addMissingTags(userTags);
        }
    }

    /**
     * Adds missing tags to the set from the given list.
     *
     * @param tags tags to add from.
     */
    public void addMissingTags(String[] tags)
    {
        if (tags != null)
        {
            for (String tag : tags)
            {
                if (tag != null) usedTags.add(tag.toLowerCase());
            }
        }
    }
}
