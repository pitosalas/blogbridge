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
// $Id: NetworkFeed.java,v 1.21 2007/07/18 16:21:59 spyromus Exp $
//
package com.salas.bb.domain;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.ClientErrorException;
import com.salas.bb.utils.parser.Channel;
import com.salas.bb.utils.parser.FeedParserConfig;
import com.salas.bb.utils.parser.FeedParserResult;
import com.salas.bb.utils.parser.IFeedParser;

import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Network feed is a basic class for all feeds taking data from network. This class
 * simply organizes the process of getting feed data from the web.</p>
 */
public abstract class NetworkFeed extends DataFeed
{
    private static final Logger LOG = Logger.getLogger(NetworkFeed.class.getName());

    /**
     * Gets XML URL.
     *
     * @return URL.
     */
    public abstract URL getXmlURL();

    /**
     * Fetches the feed by some specific means.
     *
     * @return the feed or NULL if there was an error or no updates required.
     */
    protected Channel fetchFeed()
    {
        IFeedParser parser = FeedParserConfig.create();

        Channel channel = null;
        try
        {
            FeedParserResult result = parser.parse(getXmlURL(), getTitle(),
                getLastUpdateServerTime());

            setInvalidnessReason(null);
            channel = result.getChannel();

            // TODO: Processing of the redirection should go through GlobalController feed replacing logic
            if (result.hasBeenRedirected()) redirected(result.getReridrectionURL());
        } catch (Exception e)
        {
            if (!(e instanceof ClientErrorException))
            {
                String message = e.getMessage();
                setInvalidnessReason(message == null
                    ? Strings.message("feed.invalidness.reason.failed.to.fetch") : message);

                LOG.warning(MessageFormat.format(Strings.error("feed.fetching.errored"), toString()));
                LOG.log(Level.FINE, "Details:", e);
            }
        }

        return channel;
    }

    /**
     * Returns TRUE if this feed is updatable, meaning that it's not invalid for some reason and
     * it's proper time to call <code>update()</code> method. The behaviod may differ if the update
     * operation was called directly to this particular feed and not as a part of a bigger update
     * operation (update guide or update all).
     *
     * @param manual if TRUE then the update was requested directly (not through guide/set or by
     *               periodic check).
     */
    protected boolean isUpdatable(boolean manual)
    {
        return getXmlURL() != null && super.isUpdatable(manual);
    }

    /**
     * Handles permanent redirection to a new URL. This method is required to be
     * overriden by sub-classes if they wish to handle redirects.
     *
     * @param newXmlURL new URL.
     */
    protected void redirected(URL newXmlURL)
    {
    }

    /**
     * Returns string representation of this feed object.
     *
     * @return string representation.
     */
    public String toString()
    {
        return super.toString() + " [" + getXmlURL() + "] [" + getID() + "]";
    }


    /**
     * Returns <code>TRUE</code> if feed is visible.
     *
     * @return <code>TRUE</code> if feed is visible.
     */
    @Override
    public boolean isVisibleNoCache()
    {
        return super.isVisibleNoCache() || (!isInvalid() && !isInitialized() && isVisibleSubClause() &&
            isUpdatable(false, false));
    }

    /**
     * The part of AND-clause of the isVisible() method.
     *
     * @return the subclause.
     * @see com.salas.bb.domain.NetworkFeed#isVisible
     */
    protected boolean isVisibleSubClause()
    {
        return true;
    }
}
