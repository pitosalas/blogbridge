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
// $Id: PinTagger.java,v 1.1 2007/04/19 11:40:04 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Tags pinned articles if necessary.
 */
public class PinTagger extends DomainAdapter implements PropertyChangeListener
{
    private UserPreferences prefs;
    private boolean registered = false;

    private final GlobalController controller;

    /**
     * Creates pin tagger.
     *
     * @param controller controller.
     */
    public PinTagger(GlobalController controller)
    {
        this.controller = controller;
    }

    /**
     * Sets user preferences object.
     *
     * @param aPrefs preferences.
     */
    public void setUserPreferences(UserPreferences aPrefs)
    {
        if (prefs != null)
        {
            prefs.removePropertyChangeListener(this);
        }

        prefs = aPrefs;

        if (prefs != null)
        {
            prefs.addPropertyChangeListener(this);
        }

        onPinTaggingPropsChange();
    }

    @Override
    public void propertyChanged(IArticle article, String property, Object oldValue, Object newValue)
    {
        if (property.equals(IArticle.PROP_PINNED) && ((Boolean)newValue) &&
            prefs.isPinTagging() && StringUtils.isNotEmpty(prefs.getPinTags()) &&
            article instanceof ITaggable)
        {
            ITaggable tarticle = (ITaggable)article;
            String pinTags = prefs.getPinTags();

            // Merge tags
            String[] tags = tarticle.getUserTags();
            tags = mergeTags(tags, pinTags);

            // Create a description
            String description = tarticle.getTitle();
            if (StringUtils.isEmpty(description)) description = "No Description";

            // Set tags and the description
            tarticle.setUserTags(tags);
            tarticle.setTagsDescription(description);
            tarticle.setUnsavedUserTags(true);
        }
    }

    /**
     * Merges tags.
     *
     * @param tags      existing tags.
     * @param pinTags   tags list.
     *
     * @return result.
     */
    static String[] mergeTags(String[] tags, String pinTags)
    {
        List<String> newTags = StringUtils.keywordsToList(pinTags);

        if (tags != null && tags.length > 0)
        {
            for (String tag : tags) if (!newTags.contains(tag)) newTags.add(tag);
        }

        return newTags.toArray(new String[newTags.size()]);
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (UserPreferences.PROP_PIN_TAGGING.equals(prop) ||
            UserPreferences.PROP_PIN_TAGS.equals(prop))
        {
            onPinTaggingPropsChange();
        }
    }

    /**
     * Invoked when a pin tagging preference is changed.
     */
    private void onPinTaggingPropsChange()
    {
        if (prefs != null &&
            prefs.isPinTagging() &&
            StringUtils.isNotEmpty(prefs.getPinTags()))
        {
            if (!registered)
            {
                controller.addDomainListener(this);
                registered = true;
            }
        } else
        {
            controller.removeDomainListener(this);
            registered = false;
        }
    }
}
