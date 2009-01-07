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
// $Id: DummyDataFeed.java,v 1.5 2007/05/28 11:29:30 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.parser.Channel;

/**
 * Dummy data feed implementation for testing purposes.
 */
class DummyDataFeed extends DataFeed
{
    private Channel channel = null;

    private boolean updateArticlesCalled = false;
    private boolean updateFeedCalled = false;
    private boolean cleanCalled = false;

    /**
     * Sets the channel to return as a result of <code>fetchFeed</code> call.
     *
     * @param aChannel channel to return.
     */
    public void setChannel(Channel aChannel)
    {
        channel = aChannel;
    }

    /**
     * Returns TRUE if <code>updateArticles()</code> method has been called
     * since <code>update()</code>.
     *
     * @return TRUE if called.
     */
    public boolean isUpdateArticlesCalled()
    {
        return updateArticlesCalled;
    }

    /**
     * Returns TRUE if <code>upateFeed()</code> method has been called
     * since <code>update()</code>.
     *
     * @return TRUE if called.
     */
    public boolean isUpdateFeedCalled()
    {
        return updateFeedCalled;
    }

    /**
     * Returns TRUE if <code>clean()</code> method has been called
     * since <code>update()</code>.
     *
     * @return TRUE if called.
     */
    public boolean isCleanCalled()
    {
        return cleanCalled;
    }

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getTitle()
    {
        return "Dummy";
    }

    /**
     * Fetches the feed by some specific means.
     *
     * @return the feed or NULL if there was an error or no updates required.
     */
    protected Channel fetchFeed()
    {
        return channel;
    }

    /**
     * Updates the feed contents using internal algorithms specific to each feed.
     */
    public synchronized void update()
    {
        resetFlags();
        super.update();
    }

    /**
     * Resets all method call flags.
     */
    void resetFlags()
    {
        updateArticlesCalled = false;
        updateFeedCalled = false;
        cleanCalled = false;
    }

    @Override
    protected void updateArticles(StandardArticle[] incomingArticles)
    {
        updateArticlesCalled = true;
        super.updateArticles(incomingArticles);
    }

    /**
     * Updates the feed properties from the channel object.
     *
     * @param channel channel object.
     */
    protected void updateFeed(Channel channel)
    {
        updateFeedCalled = true;
        super.updateFeed(channel);
    }

    /**
     * Removes the tail articles to fit into the current purge limit setting.
     */
    public synchronized void clean()
    {
        cleanCalled = true;
        super.clean();
    }

    /**
     * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
     * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
     * criteria for the search feeds.
     *
     * @return match key.
     */
    public String getMatchKey()
    {
        return null;
    }
}
