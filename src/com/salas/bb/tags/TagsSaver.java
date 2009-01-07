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
// $Id: TagsSaver.java,v 1.7 2007/05/14 15:50:33 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.domain.*;
import com.salas.bb.tags.net.ITagsStorage;
import com.salas.bb.utils.i18n.Strings;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tags saver is a task, which is scanning for unsaved user tags across all savable
 * objects in given guides set and sends them to the appropriate services using
 * networker.
 */
public class TagsSaver implements Runnable
{
    private static final Logger LOG = Logger.getLogger(TagsSaver.class.getName());

    private final ITagsStorage netHandler;
    private GuidesSet       guidesSet;

    /**
     * Creates tags saver.
     *
     * @param aNetHandler networker object to talk to tagging services.
     */
    public TagsSaver(ITagsStorage aNetHandler)
    {
        netHandler = aNetHandler;
    }

    /**
     * Sets the guides set to operate.
     *
     * @param aGuidesSet guides set.
     */
    public void setGuidesSet(GuidesSet aGuidesSet)
    {
        guidesSet = aGuidesSet;
    }

    /**
     * Main cycle.
     */
    public void run()
    {
        if (guidesSet != null) saveTaggables(collectUnsavedTaggables(guidesSet));
    }

    /**
     * Collect unsaved taggable objects from the guides set. We take only feeds and
     * articles from data feeds. Any of above should implement {@link com.salas.bb.domain.ITaggable}
     * interface.
     *
     * @param guidesSet the set to scan.
     *
     * @return collection of unsaved untaggables.
     */
    static Collection<ITaggable> collectUnsavedTaggables(GuidesSet guidesSet)
    {
        Set<ITaggable> unsavedTaggables = new HashSet<ITaggable>();

        Collection<IFeed> feeds = guidesSet.getFeeds();
        for (IFeed feed : feeds)
        {
            checkObject(feed, unsavedTaggables);

            if (feed instanceof DataFeed)
            {
                IArticle[] articles = feed.getArticles();
                for (IArticle article : articles) checkObject(article, unsavedTaggables);
            }
        }

        return unsavedTaggables;
    }

    /**
     * Checks if object is {@link com.salas.bb.domain.ITaggable} implemenation
     * and having unsaved user tags. If it is, then it will be added to
     * <code>aUnsavedTaggables</code> set.
     *
     * @param aObject               object to verify.
     * @param aUnsavedTaggables     set to save unsaved taggables to.
     */
    private static void checkObject(Object aObject, Set<ITaggable> aUnsavedTaggables)
    {
        if (aObject instanceof ITaggable && ((ITaggable)aObject).hasUnsavedUserTags())
        {
            aUnsavedTaggables.add((ITaggable)aObject);
        }
    }

    /**
     * Save all unsaved user tags.
     *
     * @param aUnsavedTaggables collection of taggable objects with unsaved user tags.
     */
    private void saveTaggables(Collection<ITaggable> aUnsavedTaggables)
    {
        for (ITaggable taggable : aUnsavedTaggables) saveTaggable(taggable);
    }

    /**
     * Saves user tags to the service and marks object as having no unsaved tags.
     *
     * @param aTaggable taggble object to save user tag for.
     */
    private void saveTaggable(ITaggable aTaggable)
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Saving user tags: " + aTaggable);

        try
        {
            String[] beforeSaving = aTaggable.getUserTags();
            netHandler.storeUserTags(aTaggable);
            String[] afterSaving = aTaggable.getUserTags();

            // Mark user tags as saved only if the tags we were saving didn't change
            // during saving operation
            if (Arrays.equals(beforeSaving, afterSaving))
            {
                aTaggable.setUnsavedUserTags(false);
            }
        } catch (Exception e)
        {
            if (LOG.isLoggable(Level.WARNING))
            {
                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("tags.could.not.store.user.tags"), aTaggable), e);
            }
        }
    }
}
