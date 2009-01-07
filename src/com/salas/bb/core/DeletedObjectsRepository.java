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
// $Id $
//

package com.salas.bb.core;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.persistence.IPersistenceManager;
import com.salas.bb.persistence.PersistenceException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records all deleted feeds and reading lists and manipulates these records.
 */
public class DeletedObjectsRepository extends DomainAdapter
{
    private static final Logger LOG = Logger.getLogger(DeletedObjectsRepository.class.getName());

    private static final String MSG_FAILED_QUERY = "Failed to query for deleted object record presence.";
    private static final String FAILED_PURGE = "Failed to purge deleted object records.";
    private static final String FAILED_ADD = "Failed to delete deleted object record.";
    private static final String FAILED_REMOVE = "Failed to add deleted object record.";

    private final IPersistenceManager pm;

    /**
     * Creates repository.
     *
     * @param pm persistence manager.
     */
    public DeletedObjectsRepository(IPersistenceManager pm)
    {
        this.pm = pm;
    }

    /**
     * Returns <code>TRUE</code> if a object was deleted.
     *
     * @param guideTitle    guide title.
     * @param objectKey       object match key.
     *
     * @return <code>TRUE</code> if a object was deleted.
     */
    public boolean wasDeleted(String guideTitle, String objectKey)
    {
        boolean deleted = false;

        try
        {
            deleted = pm.isDeletedObjectRecordPresent(guideTitle, objectKey);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_FAILED_QUERY, e);
        }

        return deleted;
    }

    /**
     * Removes all known records about deleted items.
     */
    public void purge()
    {
        try
        {
            pm.purgeDeletedObjectRecords();
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, FAILED_PURGE, e);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Domain Events
    // --------------------------------------------------------------------------------------------


    /**
     * Invoked when the feed has been removed directly from the feed. It has nothing to do with the
     * visual representation of the guide because this feed can still be visible in the guide
     * because of its presence in one or more associated reading lists. This even simply means that
     * there's no direct connection between the guide and the feed.
     *
     * @param guide guide.
     * @param feed  removed feed.
     */
    public void feedLinkRemoved(IGuide guide, IFeed feed)
    {
        String guideTitle = guide.getTitle();
        addDeletedObjectRecord(guideTitle, feed.getMatchKey());
    }

    /**
     * Invoked after reading list is removed from the guide.
     *
     * @param guide source guide.
     * @param list  reading list removed.
     */
    public void readingListRemoved(IGuide guide, ReadingList list)
    {
        String guideTitle = guide.getTitle();
        addDeletedObjectRecord(guideTitle, list.getURL().toString());
    }

    /**
     * Invoked when new feed has been added directly to this guide (not through the Reading List).
     * In fact, it doesn't mean that the feed should appear in the guide if it's already there. This
     * event will be followed by <code>feedAdded</code> event if this is the first addition of this
     * feed (not visible yet) and will not, if the feed is already in the list.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedLinkAdded(IGuide guide, IFeed feed)
    {
        String guideTitle = guide.getTitle();
        removeDeletedObjectRecord(guideTitle, feed.getMatchKey());
    }


    /**
     * Invoked after new reading list is added to the guide.
     *
     * @param guide source guide.
     * @param list  reading list added.
     */
    public void readingListAdded(IGuide guide, ReadingList list)
    {
        String guideTitle = guide.getTitle();
        removeDeletedObjectRecord(guideTitle, list.getURL().toString());
    }

    /**
     * Invoked when the property of the guide has been changed.
     *
     * @param guide    guide owning the property.
     * @param property property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IGuide guide, String property, Object oldValue, Object newValue)
    {
        if (IGuide.PROP_TITLE.equals(property))
        {
            // When the guide title changes all of the feeds and rl's it had can be marked as deleted
            // and all the feeds and rl's in the guide with a new name -- undeleted
            String oldGuideTitle = (String)oldValue;
            String newGuideTitle = (String)newValue;

            IFeed[] feeds = guide.getFeeds();
            for (int i = 0; i < feeds.length; i++)
            {
                IFeed feed = feeds[i];

                String feedKey = feed.getMatchKey();
                addDeletedObjectRecord(oldGuideTitle, feedKey);
                removeDeletedObjectRecord(newGuideTitle, feedKey);
            }

            if (guide instanceof StandardGuide)
            {
                ReadingList[] rls = ((StandardGuide)guide).getReadingLists();
                for (int i = 0; i < rls.length; i++)
                {
                    ReadingList rl = rls[i];

                    String rlKey = rl.getURL().toString();
                    addDeletedObjectRecord(oldGuideTitle, rlKey);
                    removeDeletedObjectRecord(newGuideTitle, rlKey);
                }
            }
        }
    }

    /**
     * Add an deleted object record.
     *
     * @param guideTitle    guide title.
     * @param key           key.
     */
    private void addDeletedObjectRecord(String guideTitle, String key)
    {
        try
        {
            pm.addDeletedObjectRecord(guideTitle, key);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, FAILED_ADD, e);
        }
    }

    /**
     * Remove deleted object record.
     *
     * @param guideTitle    guide title.
     * @param key           key.
     */
    private void removeDeletedObjectRecord(String guideTitle, String key)
    {
        try
        {
            pm.removeDeletedObjectRecord(guideTitle, key);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, FAILED_REMOVE, e);
        }
    }
}
