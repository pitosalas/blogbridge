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
// $Id: DeliciousStorage.java,v 1.6 2008/04/01 10:55:55 spyromus Exp $
//

package com.salas.bb.tags.net;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.delicious.DeliciousService;
import com.salas.bb.utils.net.delicious.DeliciousTags;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delicious tags storage is capable of talking to del.icio.us service.
 * It knows how to read tags for URL's and how to record them there.
 */
public class DeliciousStorage implements ITagsStorage
{
    private static final Logger LOG = Logger.getLogger(DeliciousStorage.class.getName());

    private static final String SYNTHETIC_TAG = "feed";

    private ICredentialsCallback credentialsCallback;

    /**
     * Creates delicious-based tags storage.
     *
     * @param aCredentialsCallback credentials callback.
     */
    public DeliciousStorage(ICredentialsCallback aCredentialsCallback)
    {
        credentialsCallback = aCredentialsCallback;
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
        DeliciousTags[] tags;

        try
        {
            tags = DeliciousService.getLinkTags(aTaggable.getTaggableLink());
            DeliciousTags.SortedTags sortedTags = DeliciousTags.filterTagsByUser(tags, getUserName());
            aTaggable.setSharedTags(sortedTags.othersTags);

            String[] userTags = aTaggable.getUserTags();
            if ((userTags == null || userTags.length == 0) && sortedTags.userTags.length > 0)
            {
                aTaggable.setUserTags(sortedTags.userTags);
            }
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, Strings.error("tags.failed.to.load.tags"), e);
        }
    }

    /**
     * Returns the name of the user.
     *
     * @return user name.
     */
    private String getUserName()
    {
        return credentialsCallback.getUserName();
    }

    /**
     * Returns the password of the user.
     *
     * @return user password.
     */
    private String getPassword()
    {
        return credentialsCallback.getUserPassword();
    }

    /**
     * Stores user tags in the way, specific to this storage.
     *
     * @param aTaggable taggable object.
     *
     * @throws NullPointerException if object isn't specified.
     */
    public void storeUserTags(ITaggable aTaggable)
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Store user tags of " + aTaggable);

        String user = getUserName();
        String password = getPassword();

        if (user != null && password != null)
        {
            String[] userTags = aTaggable.getUserTags();

            if (userTags != null)
            {
                boolean success;

                String description = aTaggable.getTagsDescription();
                String extended = aTaggable.getTagsExtended();
                if (StringUtils.isEmpty(extended)) extended = null;
                URL link = aTaggable.getTaggableLink();

                try
                {
                    if (userTags.length == 0)
                    {
                        success = DeliciousService.untagLink(link, user, password);
                    } else
                    {
                        // If we deal with a feed then we need to add a markup
                        // synthetic tag which may or may not be mentioned by user.
                        if (aTaggable instanceof IFeed)
                        {
                            userTags = appendSynthicTagIfNecessary(userTags);
                        }

                        success = DeliciousService.tagLink(link, user, password,
                            userTags, description, extended);
                    }

                    if (!success)
                    {
                        LOG.log(Level.SEVERE, MessageFormat.format(
                            Strings.error("tags.failed.to.save.tags"),
                            new Object[] { link, StringUtils.join(userTags, ",") }));
                    }
                } catch (IOException e)
                {
                    LOG.log(Level.WARNING, Strings.error("tags.failed.to.store.tags.updates"), e);
                }
            }
        }
    }

    /**
     * If tags contain synthetic tag then just continue, otherwise add the
     * synthetic tag to the list.
     *
     * @param tags tags.
     *
     * @return resulting tags.
     */
    private String[] appendSynthicTagIfNecessary(String[] tags)
    {
        String[] newTags = tags;

        if (Arrays.binarySearch(tags, SYNTHETIC_TAG) < 0)
        {
            newTags = new String[tags.length + 1];
            for (int i = 0; i < tags.length; i++)
            {
                newTags[i + 1] = tags[i];
            }
            newTags[0] = SYNTHETIC_TAG;
        }

        return newTags;
    }
}
