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
// $Id: IPersistenceManager.java,v 1.31 2008/02/27 08:35:59 spyromus Exp $
//

package com.salas.bb.persistence;

import com.salas.bb.domain.*;
import com.salas.bb.utils.net.auth.IPasswordsRepository;

import java.io.File;

/**
 * Persistence manager. Interface defines the contract between the application and
 * persistence layer.
 */
public interface IPersistenceManager
{
    /**
     * Loads the list of guides and feeds into the set from database.
     *
     * @param set set to load data into.
     *
     * @throws NullPointerException  if the set isn't specified.
     * @throws IllegalStateException if the set isn't empty.
     * @throws PersistenceException if database operation fails.
     */
    void loadGuidesSet(GuidesSet set) throws PersistenceException;

    /**
     * Inserts guide into database.
     *
     * @param guide guide to insert.
     * @param position position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is already in database.
     * @throws IllegalArgumentException if guide is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    void insertGuide(IGuide guide, int position) throws PersistenceException;

    /**
     * Removes guide from database.
     *
     * @param guide guide to remove.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is not in database.
     * @throws PersistenceException if database operation fails.
     */
    void removeGuide(IGuide guide) throws PersistenceException;

    /**
     * Updates guide information in database.
     *
     * @param guide guide to update.
     * @param position position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is not in database.
     * @throws PersistenceException if database operation fails.
     */
    void updateGuide(IGuide guide, int position) throws PersistenceException;

    /**
     * Updates guide positions in database.
     *
     * @param set guides set.
     *
     * @throws PersistenceException if database operation fails.
     */
    void updateGuidePositions(GuidesSet set) throws PersistenceException;

    /**
     * Inserts reading list which is connected to some guide.
     *
     * @param aList reading list.
     * 
     * @throws PersistenceException if database operation fails.
     */
    void insertReadingList(ReadingList aList) throws PersistenceException;

    /**
     * Updates information about reading list.
     *
     * @param aList reading list.
     *
     * @throws PersistenceException if database operation fails.
     */
    void updateReadingList(ReadingList aList)
        throws PersistenceException;

    /**
     * Removes reading list from the database.
     * All connected feeds become disconnected.
     *
     * @param aList reading list.
     *
     * @throws PersistenceException if database operation fails.
     */
    void removeReadingList(ReadingList aList)
        throws PersistenceException;

    /**
     * Inserts the feed into database.
     *
     * @param feed feed to insert.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is already in database,
     *                                  or has no guide assigned,
     *                                  or guide isn't persisted.
     * @throws IllegalArgumentException if feed is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    void insertFeed(IFeed feed) throws PersistenceException;

    /**
     * Removes the feed from database.
     *
     * @param feed feed to remove.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is not in database.
     * @throws PersistenceException if database operation fails.
     */
    void removeFeed(IFeed feed) throws PersistenceException;

    /**
     * Moves feed from source guide to destination guide.
     *
     * @param feed      feed to move.
     * @param source    source guide to move from.
     * @param dest      destination guide to move to.
     *
     * @throws NullPointerException if feed or source or destination guides aren't specified.
     * @throws IllegalStateException if feed or one of the guides are transient.
     * @throws PersistenceException if database operation fails.
     */
    void moveFeed(IFeed feed, IGuide source, IGuide dest) throws PersistenceException;

    /**
     * Updates the feed in database.
     *
     * @param feed      feed to update.
     * @param property  name of property being updated or NULL if full update required.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is not in database.
     * @throws PersistenceException if database operation fails.
     */
    void updateFeed(IFeed feed, String property) throws PersistenceException;

    /**
     * Inserts the article in database.
     *
     * @param article article to insert.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is already in database, or
     *                               article isn't assigned to feed, or
     *                               feed this article is assigned to is transient.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    void insertArticle(IArticle article) throws PersistenceException;

    /**
     * Removes article from database.
     *
     * @param article article to remove.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws PersistenceException if database operation fails.
     */
    void removeArticle(IArticle article) throws PersistenceException;

    /**
     * Updates article in database.
     *
     * @param article article to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    void updateArticle(IArticle article) throws PersistenceException;

    /**
     * Updates article properties in database.
     *
     * @param article article properties to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws com.salas.bb.persistence.PersistenceException if database operation fails.
     */
    void updateArticleProperties(IArticle article) throws PersistenceException;

    /**
     * <p>Performs single-time initialization before the actual work. This method
     * can be used to prepare the database or perform a migration of data or for
     * other supplementary things.</p>
     *
     * <p>This method is called only once and before any of the data access or
     * modification calls.</p>
     *
     * @throws PersistenceException if initialization has failed.
     */
    void init() throws PersistenceException;

    /**
     * Returns passwords repository.
     *
     * @return passwords repository.
     */
    IPasswordsRepository getPasswordsRepository();

    /**
     * Shutdown the storage.
     */
    void shutdown();

    /**
     * Adds feed to a guide -- adds the link.
     *
     * @param guide    guide.
     * @param feed     feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void addFeedToGuide(IGuide guide, IFeed feed) throws PersistenceException;

    /**
     * Adds feed to a reading list -- adds the link.
     *
     * @param readingList   reading list.
     * @param feed          feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void addFeedToReadingList(ReadingList readingList, IFeed feed) throws PersistenceException;

    /**
     * Removes link between the guide and the feed.
     *
     * @param guide         guide.
     * @param feed          feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void removeFeedFromGuide(IGuide guide, IFeed feed) throws PersistenceException;

    /**
     * Updates the link between the guide and the feed.
     *
     * @param guide         guide.
     * @param feed          feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void updateFeedLink(StandardGuide guide, IFeed feed) throws PersistenceException;

    /**
     * Removes link between the reading list and the feed.
     *
     * @param readingList   reading list.
     * @param feed          feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void removeFeedFromReadingList(ReadingList readingList, IFeed feed) throws PersistenceException;

    /**
     * Returns <code>TRUE</code> if the database was reset as the result
     * of corruption detection or unability to upgrade.
     *
     * @return <code>TRUE</code> if the database was reset.
     */
    boolean isDatabaseReset();

    /**
     * Updates positions of feeds within the guide.
     *
     * @param guide guide to reposition feeds.
     *
     * @throws PersistenceException if database operation fails.
     */
    void updateFeedsPositions(IGuide guide) throws PersistenceException;

    /**
     * Updates position of a feed within the guide.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    void updateFeedPosition(IGuide guide, IFeed feed) throws PersistenceException;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Adds deleted object record to the database.
     *
     * @param guideTitle    guide title.
     * @param objectKey     object match key.
     *
     * @throws PersistenceException if database operation fails.
     */
    void addDeletedObjectRecord(String guideTitle, String objectKey) throws PersistenceException;

    /**
     * Removes the deleted object record from the database.
     *
     * @param guideTitle    guide title.
     * @param objectKey     object match key.
     *
     * @throws PersistenceException if database operation fails.
     */
    void removeDeletedObjectRecord(String guideTitle, String objectKey) throws PersistenceException;

    /**
     * Returns <code>TRUE</code> if a object has been deleted.
     *
     * @param guideTitle    guide title.
     * @param objectKey     object match key.
     *
     * @return <code>TRUE</code> if a object has been deleted.
     *
     * @throws PersistenceException if database operation fails.
     */
    boolean isDeletedObjectRecordPresent(String guideTitle, String objectKey) throws PersistenceException;

    /**
     * Removes all records about deleted keys from the database.
     *
     * @throws PersistenceException if database operation fails.
     */
    void purgeDeletedObjectRecords() throws PersistenceException;

    /**
     * Compacts database.
     *
     * @throws PersistenceException if database operation fails.
     */
    void compact() throws PersistenceException;

    /**
     * Creates complete database backup.
     *
     * @param directory destination directory.
     *
     * @throws PersistenceException if database operation fails.
     */
    void backup(File directory) throws PersistenceException;

    /**
     * Returns the statistics manager.
     *
     * @return manager.
     */
    IStatisticsManager getStatisticsManager();

    /**
     * Returns the application property value.
     *
     * @param key key.
     *
     * @return value or <code>NULL</code> if not present yet.
     *
     * @throws PersistenceException if database fails.
     */
    String getApplicationProperty(String key) throws PersistenceException;

    /**
     * Sets the application property value.
     *
     * @param key   key.
     * @param value value or <code>NULL</code> to delete.
     *
     * @throws PersistenceException if database fails.
     */
    void setApplicationProperty(String key, String value) throws PersistenceException;
}
