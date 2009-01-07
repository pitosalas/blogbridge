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
// $Id: StandardArticle.java,v 1.20 2006/11/08 13:26:56 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;

/**
 * Standard article containing only the text.
 */
public class StandardArticle extends AbstractArticle implements ITaggable
{
    protected String        text;
    protected String        plainText;

    private String[]        sharedTags;
    private String[]        authorTags;
    private String[]        userTags;
    private boolean         unsavedUserTags;
    private String          tagsDescription;
    private String          tagsExtended;

    /**
     * Creates standard article.
     *
     * @param aText original text.
     */
    public StandardArticle(String aText)
    {
        text = aText;
        plainText = null;

        userTags = null;
        sharedTags = null;
        authorTags = null;
        unsavedUserTags = false;
        tagsDescription = null;
    }

    /**
     * Returns HTML version of article text.
     *
     * @return HTML version of text.
     */
    public String getHtmlText()
    {
        return getText();
    }

    /**
     * Returns the text of article.
     *
     * @return text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Returns plain version of article text.
     *
     * @return plain version of text.
     */
    public synchronized String getPlainText()
    {
        if (plainText == null) plainText = super.getPlainText();
        return plainText;
    }

    // ---------------------------------------------------------------------------------------------
    // ITaggable implementation
    // ---------------------------------------------------------------------------------------------

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
        String[] oldSharedTags = sharedTags;
        sharedTags = tags;

        firePropertyChanged(PROP_SHARED_TAGS, oldSharedTags, sharedTags);
    }

    /**
     * Returns tags assigned by author.
     *
     * @return author tags.
     */
    public String[] getAuthorTags()
    {
        if (authorTags == null) authorTags = StringUtils.collectTags(getHtmlText());

        return authorTags;
    }

    /**
     * Returns the name of taggable object type.
     *
     * @return type name.
     */
    public String getTaggableTypeName()
    {
        return Strings.message("taggable.article");
    }

    /**
     * Returns link which can be tagged at the service (BB or third-party).
     *
     * @return link or <code>NULL</code> if tagging isn't supported.
     */
    public URL getTaggableLink()
    {
        return this.getLink();
    }

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
     * @param tags user tags.
     */
    public void setUserTags(String[] tags)
    {
        String[] oldUserTags = userTags;
        userTags = tags;

        firePropertyChanged(PROP_USER_TAGS, oldUserTags, userTags);
    }

    /**
     * Returns <code>TRUE</code> if this object has unsaved user tags.
     *
     * @return <code>TRUE</code> if this object has unsaved user tags.
     */
    public boolean hasUnsavedUserTags()
    {
        return unsavedUserTags;
    }

    /**
     * Sets unsaved user tags flag.
     *
     * @param unsaved <code>TRUE</code> if this object has unsaved user tags.
     */
    public void setUnsavedUserTags(boolean unsaved)
    {
        if (unsavedUserTags != unsaved)
        {
            unsavedUserTags = unsaved;

            firePropertyChanged(PROP_UNSAVED_USER_TAGS, Boolean.valueOf(!unsaved),
                Boolean.valueOf(unsaved));
        }
    }

    /**
     * Returns the description of tags.
     *
     * @return description.
     */
    public String getTagsDescription()
    {
        return tagsDescription;
    }

    /**
     * Sets new description text.
     *
     * @param description new description text.
     */
    public void setTagsDescription(String description)
    {
        String oldValue = tagsDescription;
        tagsDescription = description;

        firePropertyChanged(PROP_TAGS_DESCRIPTION, oldValue, tagsDescription);
    }

    /**
     * Returns tags extended description text.
     *
     * @return tags extended description text.
     */
    public String getTagsExtended()
    {
        return tagsExtended;
    }

    /**
     * Tests new tags extended description text.
     *
     * @param extended new extended description.
     */
    public void setTagsExtended(String extended)
    {
        String oldValue = tagsExtended;
        tagsExtended = extended;

        firePropertyChanged(PROP_TAGS_EXTENDED, oldValue, tagsExtended);
    }
}
