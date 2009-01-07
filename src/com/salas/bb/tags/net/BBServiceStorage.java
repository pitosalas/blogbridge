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
// $Id: BBServiceStorage.java,v 1.9 2007/04/19 11:40:05 spyromus Exp $
//

package com.salas.bb.tags.net;

import com.jgoodies.uif.application.Application;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage which is talking to BB Service.
 */
public class BBServiceStorage  implements ITagsStorage
{
    private static final Logger LOG = Logger.getLogger(BBServiceStorage.class.getName());

    private ICredentialsCallback credentialsCallback;

    /**
     * Creates tags storage.
     *
     * @param aCredentialsCallback credentials callback.
     */
    public BBServiceStorage(ICredentialsCallback aCredentialsCallback)
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
        String email = credentialsCallback.getUserName();
        String password = credentialsCallback.getUserPassword();

        // If user selected this storage type while he had service registration information
        // entered and later removed email/password this can happen and we have to skip
        // calling service with no credentials.
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) return;

        try
        {
            List tagsList = ServerService.tagsFetch(email, password, aTaggable.getTaggableLink());

            String[] sharedTags = convertTagsListToFlatTags(tagsList);

            aTaggable.setSharedTags(sharedTags);
        } catch (ServerServiceException e)
        {
            handleServiceException(e, aTaggable, Strings.message("tags.storage.fetching.tags"));
        }
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
        String email = credentialsCallback.getUserName();
        String password = credentialsCallback.getUserPassword();

        // If user selected this storage type while he had service registration information
        // entered and later removed email/password this can happen and we have to skip
        // calling service with no credentials.
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) return;

        URL link = aTaggable.getTaggableLink();
        boolean feed = aTaggable instanceof IFeed;
        String[] userTags = aTaggable.getUserTags();

        String description = aTaggable.getTagsDescription();
        if (description == null) description = "";

        String extended = aTaggable.getTagsExtended();
        if (extended == null) extended = "";

        try
        {
            ServerService.tagsStore(email, password, link, feed, userTags, description, extended);
        } catch (ServerServiceException e)
        {
            handleServiceException(e, aTaggable, Strings.message("tags.storage.storing.user.tags"));
        }
    }

    /**
     * Takes the list of tags strings (in format "a b c", "d a") and converts them to flat array of
     * tags (like, "a", "b", "c", "d", "a").
     *
     * @param aTagsList original list of tags definitions.
     *
     * @return array of tags.
     */
    private static String[] convertTagsListToFlatTags(List aTagsList)
    {
        List flatTagsList = new ArrayList();
        for (Iterator iterator = aTagsList.iterator(); iterator.hasNext();)
        {
            String tags = StringUtils.fromUTF8((byte[])iterator.next());
            flatTagsList.addAll(Arrays.asList(StringUtils.keywordsToArray(tags)));
        }

        return (String[])flatTagsList.toArray(new String[flatTagsList.size()]);
    }

    /**
     * Reports the error.
     *
     * @param exception exception object.
     * @param taggable  taggable object.
     * @param action    action which has been taken.
     */
    private static void handleServiceException(ServerServiceException exception,
                                               ITaggable taggable, String action)
    {
        if (exception.getCause() instanceof IOException)
        {
            LOG.warning(MessageFormat.format(
                Strings.error("tags.communication.problem.when.0.from.bb.service"),
                new Object[] { action }));
        } else
        {
            JOptionPane.showMessageDialog(Application.getDefaultParentFrame(),
                exception.getMessage(), MessageFormat.format(
                Strings.message("tags.storage.problem.with.0"), new Object[] { action }),
                JOptionPane.ERROR_MESSAGE);

            LOG.log(Level.WARNING, MessageFormat.format(
                Strings.error("tags.failed.when.0.for.1"),
                new Object[] { action, taggable.getTaggableLink() }), exception);
        }
    }
}
