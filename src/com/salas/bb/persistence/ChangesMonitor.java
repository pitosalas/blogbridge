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
// $Id: ChangesMonitor.java,v 1.34 2008/02/27 15:45:40 spyromus Exp $
//

package com.salas.bb.persistence;

import com.salas.bb.domain.*;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.i18n.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monitor of changes in model. It looks for changes in model and calls correspondent
 * methods of a given persistence manager to make persistent changes.
 */
public final class ChangesMonitor extends DomainAdapter
{
    /** Default logger. */
    private static final Logger LOG = Logger.getLogger(ChangesMonitor.class.getName());

    /** Persistence operation has failed. */
    private static final String MSG_PERS_OP_FAILED = Strings.error("db.persistent.operation.has.failed");

    /** Persistence manager used to perform persistent changes. */
    private final IPersistenceManager manager;

    /** The set being monitored. */
    private final GuidesSet set;

    /** List of properties to skip updating in database. */
    private final List<String> articlePropertiesToSkip;
    /** List of properties to skip updating in database. */
    private final List<String> feedPropertiesToSkip;

    /**
     * Creates the monitor for a given set.
     *
     * @param aSet      set to monitor.
     * @param aManager  manager to call for persistent changes.
     */
    public ChangesMonitor(GuidesSet aSet, IPersistenceManager aManager)
    {
        manager = aManager;
        set = aSet;

        articlePropertiesToSkip = new ArrayList<String>();
        articlePropertiesToSkip.add(ITaggable.PROP_SHARED_TAGS);
        articlePropertiesToSkip.add(ITaggable.PROP_TAGS_DESCRIPTION);
        articlePropertiesToSkip.add(ITaggable.PROP_UNSAVED_USER_TAGS);
        articlePropertiesToSkip.add(ITaggable.PROP_USER_TAGS);
        articlePropertiesToSkip.add(AbstractArticle.PROP_ID);
        articlePropertiesToSkip.add(IArticle.PROP_POSITIVE);
        articlePropertiesToSkip.add(IArticle.PROP_NEGATIVE);

        feedPropertiesToSkip = new ArrayList<String>();
        feedPropertiesToSkip.add(IFeed.PROP_PROCESSING);
        feedPropertiesToSkip.add(IFeed.PROP_UNREAD_ARTICLES_COUNT);
    }

    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet aSet, IGuide guide, boolean lastInBatch)
    {
        try
        {
            manager.insertGuide(guide, set.indexOf(guide));
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when the guide has been removed from the set.
     *
     * @param set   guides set.
     * @param guide removed guide.
     * @param index old guide index.
     */
    public void guideRemoved(GuidesSet set, IGuide guide, int index)
    {
        try
        {
            manager.removeGuide(guide);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when the guide has been moved to a new location in list.
     *
     * @param set      guides set.
     * @param guide    guide which has been removed.
     * @param oldIndex old guide index.
     * @param newIndex new guide index.
     */
    public void guideMoved(GuidesSet set, IGuide guide, int oldIndex, int newIndex)
    {
        try
        {
            manager.updateGuidePositions(set);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when new feed has been added to the guide.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedAdded(IGuide guide, IFeed feed)
    {
        // If feed is still transient, it will be added later with the link-creation
        // even and the position will be updated there too
        if (feed == null || feed.getID() == -1) return;

        // Update feed or feeds position
        try
        {
            boolean last = guide.indexOf(feed) == guide.getFeedsCount() - 1;
            if (last)
            {
                manager.updateFeedPosition(guide, feed);
            } else
            {
                manager.updateFeedsPositions(guide);
            }
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when a feed is moved from one position to another.
     *
     * @param guide       source guide.
     * @param feed        feed moved.
     * @param oldPosition old position.
     * @param newPosition new position.
     */
    public void feedRepositioned(IGuide guide, IFeed feed, int oldPosition, int newPosition)
    {
        try
        {
            if (oldPosition != newPosition) manager.updateFeedsPositions(guide);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
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
        try
        {
            manager.addFeedToGuide(guide, feed);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when the feed has been removed directly from the feed. It has nothing to do with the
     * visual representation of the guide because this feed can still be visible in the guide
     * because of its presence in one or more associated reading lists. This even simply means that
     * there's no direct connection between the guide and the feed.
     *
     * @param guide         source guide.
     * @param feed removed feed.
     */
    public void feedLinkRemoved(IGuide guide, IFeed feed)
    {
        try
        {
            manager.removeFeedFromGuide(guide, feed);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when feed link property changes its value.
     *
     * @param guide    source guide.
     * @param feed     feed, who's link property has changed.
     * @param property property name.
     * @param oldValue old value.
     * @param newValue new value.
     */
    public void feedLinkPropertyChanged(StandardGuide guide, IFeed feed, String property,
        long oldValue, long newValue)
    {
        try
        {
            manager.updateFeedLink(guide, feed);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked after new reading list is added to the guide.
     *
     * @param guide source guide.
     * @param list  reading list added.
     */
    public void readingListAdded(IGuide guide, ReadingList list)
    {
        try
        {
            manager.insertReadingList(list);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked after reading list is removed from the guide.
     *
     * @param guide source guide.
     * @param list  reading list removed.
     */
    public void readingListRemoved(IGuide guide, ReadingList list)
    {
        try
        {
            manager.removeReadingList(list);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when new feed has been added to the reading list.
     *
     * @param list reading list the feed was added to.
     * @param feed added feed.
     */
    public void feedAdded(ReadingList list, IFeed feed)
    {
        try
        {
            manager.addFeedToReadingList(list, feed);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when the feed has been removed from the reading list.
     *
     * @param list reading list the feed was removed from.
     * @param feed removed feed.
     */
    public void feedRemoved(ReadingList list, IFeed feed)
    {
        try
        {
            manager.removeFeedFromReadingList(list, feed);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Invoked when the property of the list has been changed.
     *
     * @param list     list owning the property.
     * @param property property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(ReadingList list, String property, Object oldValue, Object newValue)
    {
        try
        {
            manager.updateReadingList(list);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
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
        try
        {
            manager.updateGuide(guide, set.indexOf(guide));
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Called when some article is added to the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAdded(IFeed feed, IArticle article)
    {
        if (feed instanceof SearchFeed) return;

        try
        {
            manager.insertArticle(article);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Called when some article is removed from the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleRemoved(IFeed feed, IArticle article)
    {
        if (feed instanceof SearchFeed) return;

        try
        {
            manager.removeArticle(article);
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }

    /**
     * Called when information in feed changed.
     *
     * @param feed     feed.
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
    {
        boolean persistent = (feed instanceof DataFeed || feed instanceof SearchFeed) &&
            !feedPropertiesToSkip.contains(property);

        if (persistent)
        {
            try
            {
                manager.updateFeed(feed, property);
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
            }
        }

        // STATS: Report that a feed being visited
        if (IFeed.PROP_VIEWS.equals(property)) manager.getStatisticsManager().feedVisited(feed);
    }

    /**
     * Invoked when the property of the article has been changed.
     *
     * @param article  article.
     * @param property property of the article.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IArticle article, String property, Object oldValue,
                                Object newValue)
    {
        if (article.getID() == -1 || articlePropertiesToSkip.contains(property)) return;

        try
        {
            if (property.equals(IArticle.PROP_SENTIMENT_COUNTS))
            {
                manager.updateArticleProperties(article);
            } else
            {
                manager.updateArticle(article);
            }
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, MSG_PERS_OP_FAILED, e);
        }
    }
}
