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
// $Id: IFeed.java,v 1.24 2007/07/24 14:04:29 spyromus Exp $
//
package com.salas.bb.domain;

import com.salas.bb.views.feeds.IFeedDisplayConstants;

/**
 * General feed interface. Each feed type in application implements this interface.
 */
public interface IFeed
{
    /** Title property name. */
    String PROP_TITLE                   = "title";

    /** Unread articles counter property name. */
    String PROP_UNREAD_ARTICLES_COUNT   = "unreadArticlesCount";

    /** Name of the processing property. */
    String PROP_PROCESSING              = "processing";

    /** Name of the invalidness reason property. */
    String PROP_INVALIDNESS_REASON      = "invalidnessReason";

    /** Name of rating property. */
    String PROP_RATING                  = "rating";

    /** Name of the last visit time property */
    String PROP_LAST_VISIT_TIME         = "lastVisitTime";

    /** Name of the feed type property. */
    String PROP_TYPE                    = "type";

    /** Name of the custom view mode flag. */
    String PROP_CUSTOM_VIEW_MODE_ENABLED = "customViewModeEnabled";

    /** Name of the custom view mode property. */
    String PROP_CUSTOM_VIEW_MODE        = "customViewMode";

    /** The time of last update of the property involved in synchronization. */
    String PROP_LAST_UPDATE_TIME        = "lastUpdateTime";

    /** Number of times this feed has been viewed. */
    String PROP_VIEWS                   = "views";

    /** Number of times the articles from this feed were opened in the browser. */
    String PROP_CLICKTHROUGHS           = "clickthroughs";

    /** The ascending sorting flag override. */
    String PROP_ASCENDING_SORTING       = "ascendingSorting";

    /** Auto-saving of articles. */
    String PROP_AUTO_SAVE_ARTICLES      = "autoSaveArticles";
    /** The folder for auto-saved articles. */
    String PROP_AUTO_SAVE_ARTICLES_FOLDER = "autoSaveArticlesFolder";
    /** The format of the name for auto-saved articles. */
    String PROP_AUTO_SAVE_ARTICLES_NAME_FORMAT = "autoSaveArticlesNameFormat";

    /** Auto-saving of enclosures. */
    String PROP_AUTO_SAVE_ENCLOSURES      = "autoSaveEnclosures";
    /** The folder for auto-saved enclosures. */
    String PROP_AUTO_SAVE_ENCLOSURES_FOLDER = "autoSaveEnclosuresFolder";
    /** The format of the name for auto-saved enclosures. */
    String PROP_AUTO_SAVE_ENCLOSURES_NAME_FORMAT = "autoSaveEnclosuresNameFormat";

    /** The value for unset rating. */
    int RATING_NOT_SET = -1;
    /** Minimum rating. */
    int RATING_MIN = 0;
    /** Maximum rating. */
    int RATING_MAX = 4;

    /**
     * Adds a guide currently holding this feed.
     *
     * @param aGuide parent guide.
     */
    void addParentGuide(IGuide aGuide);

    /**
     * Removes a guide that no longer holds this feed.
     *
     * @param aGuide guide.
     */
    void removeParentGuide(IGuide aGuide);

    /**
     * Returns guides currently holding this feed.
     *
     * @return parent guides.
     */
    IGuide[] getParentGuides();

    /**
     * Notifies the feed that processing of it has been started.
     */
    void processingStarted();

    /**
     * Notifies the feed that processing of it has been finished.
     */
    void processingFinished();

    /**
     * Returns TRUE if the feed is under some lengthy processing.
     *
     * @return TRUE if the feed is under some lengthy processing.
     */
    boolean isProcessing();

    /**
     * Returns TRUE when all articles are read in this channel.
     *
     * @return TRUE when everything is read.
     */
    boolean isRead();

    /**
     * Sets the whole feed read / unread.
     *
     * @param read TRUE to mark as read.
     */
    void setRead(boolean read);

    /**
     * Returns the Article at the specified index.
     *
     * @param index index of article in the feed.
     *
     * @return article object.
     */
    IArticle getArticleAt(int index);

    /**
     * Returns number of articles in the feed.
     *
     * @return number of articles.
     */
    int getArticlesCount();

    /**
     * Returns number of articles this feed owns.
     *
     * @return number of articles.
     */
    int getOwnArticlesCount();

    /**
     * Returns the list of all articles which are currently in the feed.
     *
     * @return all articles at this moment.
     */
    IArticle[] getArticles();

    /**
     * Returns the number of unread articles.
     *
     * @return count.
     */
    int getUnreadArticlesCount();

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    String getTitle();

    /**
     * Returns <code>TRUE</code> if this channel is invalid.
     *
     * @return <code>TRUE</code> if invalid.
     */
    boolean isInvalid();

    /**
     * Sets the reason of invalidness of this feed. If reason is set to <code>NULL</code> the feed
     * is considered valid.
     *
     * @param reason reason of invalidness or <code>NULL</code>.
     */
    void setInvalidnessReason(String reason);

    /**
     * Returns reason for being invalid.
     *
     * @return reason.
     */
    String getInvalidnessReason();

    /**
     * Adds listener to the list.
     *
     * @param l listener.
     */
    void addListener(IFeedListener l);

    /**
     * Removes listener from the list.
     *
     * @param l listener.
     */
    void removeListener(IFeedListener l);

    /**
     * Returns ID of the feed. This ID is used by persistence layer to identify
     * record in database.
     *
     * @return ID of the feed.
     */
    long getID();

    /**
     * Sets the ID of the feed.
     *
     * @param id ID of the feed.
     */
    void setID(long id);

    /**
     * Returns the rating of this feed set by user.
     *
     * @return rating of the feed or (-1) if not set.
     */
    int getRating();

    /**
     * Sets new rating for the feed.
     *
     * @param aRating new rating in range [0;4] or (-1) to reset.
     *
     * @throws IllegalArgumentException if rating is not within range or (-1).
     * @see #RATING_NOT_SET
     */
    void setRating(int aRating);

    /**
     * Returns time when feed was visited for the last time.
     *
     * @return time     last visit time.
     */
    long getLastVisitTime();

    /**
     * Sets time when feed was visited for the last time.
     *
     * @param time      last visit time
     */
    void setLastVisitTime(long time);

    /**
     * Returns the type of the feed.
     *
     * @return feed type.
     */
    FeedType getType();

    /**
     * Sets the type of the feed.
     *
     * @param type type.
     */
    void setType(FeedType type);

    /**
     * When <code>TRUE</code> the feed has preferred view mode set.
     *
     * @return <code>TRUE</code> when feed has its own view mode.
     */
    boolean isCustomViewModeEnabled();

    /**
     * Sets the custom view mode enabled / disabled.
     *
     * @param enabled <code>TRUE</code> to enable.
     */
    void setCustomViewModeEnabled(boolean enabled);

    /**
     * Returns custom view mode.
     *
     * @return view mode.
     *
     * @see IFeedDisplayConstants#MODE_MINIMAL
     * @see IFeedDisplayConstants#MODE_BRIEF
     * @see IFeedDisplayConstants#MODE_FULL
     */
    int getCustomViewMode();

    /**
     * Sets the custom view mode.
     *
     * @param mode mode.
     *
     * @see IFeedDisplayConstants#MODE_MINIMAL
     * @see IFeedDisplayConstants#MODE_BRIEF
     * @see IFeedDisplayConstants#MODE_FULL
     */
    void setCustomViewMode(int mode);

    /**
     * Returns <code>TRUE</code> if the feed belongs to the guide.
     *
     * @param guide guide to check.
     *
     * @return <code>TRUE</code> if the feed belongs to the guide.
     */
    boolean belongsTo(IGuide guide);

    /**
     * Returns <code>TRUE</code> if this feed is assigned to some reading list.
     *
     * @return <code>TRUE</code> if this feed is assigned to some reading list.
     */
    boolean isDynamic();

    /**
     * Returns simple match key, which can be used to detect similarity of feeds. For
     * example, it's XML URL for the direct feeds, query type + parameter for the
     * query feeds, serialized search criteria for the search feeds.
     *
     * @return match key.
     */
    String getMatchKey();

    /**
     * Returns the time of last properties update. This time is necessary for
     * the synchronization engine to learn what object is newer.
     *
     * @return the time or <code>-1</code> if not updated yet.
     */
    long getLastUpdateTime();

    /**
     * Sets the time of last properties update. When the user changes some property
     * this time is set automatically. This method is necessary for persistence layer
     * to init the object with what is currently in the database.
     *
     * @param time time.
     */
    void setLastUpdateTime(long time);

    /**
     * Returns the number of views of this feed.
     *
     * @return views count.
     */
    int getViews();

    /**
     * Sets the number of views of this feed.
     *
     * @param views views count.
     */
    void setViews(int views);

    /**
     * Returns the number of times articles from this feed were opened in the browser.
     *
     * @return times.
     */
    int getClickthroughs();

    /**
     * Sets the number of times articles from this feed have been opened in the browser.
     *
     * @param times times.
     */
    void setClickthroughs(int times);

    /**
     * Returns the mask of a feed meta-classes.
     *
     * @return mask.
     */
    int getClassesMask();

    /**
     * Returns <code>TRUE</code> if feed is visible.
     * 
     * @return <code>TRUE</code> if feed is visible.
     */
    boolean isVisible();

    /**
     * Invalidates visibility cache immediately.
     */
    void invalidateVisibilityCache();

    /**
     * Returns the state of the articles sort override flag.
     *
     * @return <code>NULL</code> for no override, <code>TRUE / FALSE</code> as a value.
     */
    Boolean getAscendingSorting();

    /**
     * Sets the state of the articles sort override flag.
     *
     * @param asc <code>NULL</code> to clear override, <code>TRUE</code> to sort in ascending order.
     */
    void setAscendingSorting(Boolean asc);

    // ------------------------------------------------------------------------
    // Articles auto-saving
    // ------------------------------------------------------------------------

    /**
     * Enables / disables automatic articles saving.
     *
     * @param en <code>TRUE</code> to enable.
     */
    void setAutoSaveArticles(boolean en);

    /**
     * Returns <code>TRUE</code> if auto-saving of articles is enabled.
     *
     * @return <code>TRUE</code> if auto-saving of articles is enabled.
     */
    boolean isAutoSaveArticles();

    /**
     * Sets the path to the folder to save articles to.
     *
     * @param folder the folder path.
     */
    void setAutoSaveArticlesFolder(String folder);

    /**
     * Returns the path to the folder to save articles to.
     *
     * @return folder path.
     */
    String getAutoSaveArticlesFolder();

    /**
     * Sets the format for the file name.
     *
     * @param nameFormat name format.
     */
    void setAutoSaveArticlesNameFormat(String nameFormat);

    /**
     * Returns the format for the file name.
     *
     * @return name format.
     */
    String getAutoSaveArticlesNameFormat();

    // ------------------------------------------------------------------------
    // Enclosures auto-saving
    // ------------------------------------------------------------------------

    /**
     * Enables / disables automatic enclosures saving.
     *
     * @param en <code>TRUE</code> to enable.
     */
    void setAutoSaveEnclosures(boolean en);

    /**
     * Returns <code>TRUE</code> if auto-saving of enclosures is enabled.
     *
     * @return <code>TRUE</code> if enabled.
     */
    boolean isAutoSaveEnclosures();

    /**
     * Sets the path to the folder to save enclosures to.
     *
     * @param folder the folder path.
     */
    void setAutoSaveEnclosuresFolder(String folder);

    /**
     * Returns the path to the folder to save enclosures to.
     *
     * @return folder path.
     */
    String getAutoSaveEnclosuresFolder();

    /**
     * Sets the format for the file name.
     *
     * @param nameFormat name format.
     */
    void setAutoSaveEnclosuresNameFormat(String nameFormat);

    /**
     * Returns the format for the file name.
     *
     * @return name format.
     */
    String getAutoSaveEnclosuresNameFormat();
}
