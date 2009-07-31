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
// $Id: IArticle.java,v 1.18 2008/02/27 15:28:09 spyromus Exp $
//

package com.salas.bb.domain;

import java.net.URL;
import java.util.Collection;
import java.util.Date;

/**
 * Article which is part of the feed.
 */
public interface IArticle
{
    /**
     * Name of property holding read flag.
     */
    String PROP_READ = "read";

    /**
     * The name of pin property.
     */
    String PROP_PINNED = "pinned";

    /** Non-existent property to indicate sentiment counts change. */
    String PROP_SENTIMENT_COUNTS = "sentimentCounts";
    /** Positive connotation flag. */
    String PROP_POSITIVE = "positive";
    /** Negative connotation flag. */
    String PROP_NEGATIVE = "negative";

    /**
     * Returns parent feed.
     *
     * @return parent feed.
     */
    IFeed getFeed();

    /**
     * Sets the parent feed.
     *
     * @param aFeed parent feed.
     */
    void setFeed(IFeed aFeed);

    /**
     * Returns simple match key which is close to unique.
     *
     * @return simple match key.
     */
    String getSimpleMatchKey();

    /**
     * Returns TRUE if the article is read.
     *
     * @return TRUE if the article is read.
     */
    boolean isRead();

    /**
     * Sets the value of read flag for the article.
     *
     * @param read TRUE if the article is read.
     */
    void setRead(boolean read);

    /**
     * Returns the pin flag state.
     *
     * @return pin flag.
     */
    boolean isPinned();

    /**
     * Sets the pin flag state.
     *
     * @param pinned TRUE to pin.
     */
    void setPinned(boolean pinned);

    /**
     * Returns title of the article.
     *
     * @return title.
     */
    String getTitle();

    /**
     * Returns the words of the title in the order of appearance.
     *
     * @return words of the title.
     */
    String[] getTitleWords();

    /**
     * Returns HTML version of article text.
     *
     * @return HTML version of text.
     */
    String getHtmlText();

    /**
     * Returns plain version of article text.
     *
     * @return plain version of text.
     */
    String getPlainText();

    /**
     * Returns brief version of plain text.
     *
     * @return brief text.
     */
    String getBriefText();

    /**
     * Returns the name of article author.
     *
     * @return author.
     */
    String getAuthor();

    /**
     * Returns the subject of article.
     *
     * @return subject.
     */
    String getSubject();

    /**
     * Returns the date of publication.
     *
     * @return publication date.
     */
    Date getPublicationDate();

    /**
     * Returns URL of associated article page.
     *
     * @return URL of article page.
     */
    URL getLink();

    /**
     * Adds listener to receive article's events.
     *
     * @param listener listener.
     */
    void addListener(IArticleListener listener);

    /**
     * Removes listener.
     *
     * @param listener listener.
     */
    void removeListener(IArticleListener listener);

    /**
     * Returns all links (absolute and relative) found in the article text.
     *
     * @return links.
     */
    Collection<String> getLinks();

    /**
     * Returns ID of the article in database. This ID is used by persistence layer.
     *
     * @return ID of the article in database.
     */
    long getID();

    /**
     * Sets ID of the article in database.
     *
     * @param id ID of the article.
     */
    void setID(long id);

    /**
     * Returns <code>TRUE</code> if article has just been added.
     * This flag remains <code>TRUE</code> during the initial article
     * added even processing. Right after that it's no longer new.
     *
     * @return <code>TRUE</code> if it's the first time the article is added to a feed.
     */
    boolean isNew();

    /**
     * Sets the new state.
     *
     * @param n new state.
     */
    void setNew(boolean n);

    /**
     * Recalculates sentiment counts.
     */
    void recalculateSentimentCounts();

    /**
     * Returns positive sentiments count.
     *
     * @return count.
     */
    int getPositiveSentimentsCount();

    /**
     * Returns negative sentiments count.
     *
     * @return count.
     */
    int getNegativeSentimentsCount();

    /**
     * Returns TRUE if article is positive based on the sentiments count.
     *
     * @return TRUE if positive.
     */
    boolean isPositive();

    /**
     * Returns TRUE if article is negative based on the sentiments count.
     *
     * @return TRUE if negative. 
     */
    boolean isNegative();

    /**
     * Recalculates connotation.
     */
    void recalculateConnotation();

    /**
     * Sets the candidate feed.
     *
     * @param feed parent feed.
     */
    void setCandidateFeed(IFeed feed);
}
