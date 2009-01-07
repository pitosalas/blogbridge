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
// $Id: FeedClassifier.java,v 1.5 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.core.GlobalModel;

/**
 * A feed can have several classes assigned.
 */
public final class FeedClassifier
{
    /**
     * Hidden utility class constructor.
     */
    private FeedClassifier()
    {
    }

    /**
     * Returns the OR'ed classes corresponding to feed.
     *
     * @param feed feed to test.
     *
     * @return corresponding classes.
     */
    public static int classify(IFeed feed)
    {
        int classes = 0;

        if (feed != null)
        {
            if (isLowRated(feed)) classes |= FeedClass.LOW_RATED;
            if (isRead(feed)) classes |= FeedClass.READ;
            if (isInvalid(feed)) classes |= FeedClass.INVALID;
            if (isUndiscovered(feed)) classes |= FeedClass.UNDISCOVERED;
            if (isDisabled(feed)) classes |= FeedClass.DISABLED;
        }

        return classes;
    }

    /**
     * Returns <code>TRUE</code> if it's direct feed and it's disabled.
     *
     * @param aFeed feed to test.
     *
     * @return <code>TRUE</code> if it's direct feed and it's disabled.
     */
    static boolean isDisabled(IFeed aFeed)
    {
        return (aFeed instanceof DirectFeed) && ((DirectFeed)aFeed).isDisabled();
    }

    /**
     * Returns <code>TRUE</code> if feed has rating below the threshold.
     *
     * @param feed feed to test.
     *
     * @return <code>TRUE</code> if low-rated.
     */
    static boolean isLowRated(IFeed feed)
    {
        GlobalModel model = GlobalModel.SINGLETON;
        if (model == null) return false;

        // NOTE: We intentionally don't count gray starz
        int threshold = model.getUserPreferences().getGoodChannelStarz() - 1;

        return feed.getRating() > -1 && feed.getRating() < threshold;
    }

    /**
     * Returns <code>TRUE</code> if feed is completely read.
     *
     * @param feed feed to test.
     *
     * @return <code>TRUE</code> if read.
     */
    static boolean isRead(IFeed feed)
    {
        return feed.isRead();
    }

    /**
     * Returns <code>TRUE</code> if feed is invalid.
     *
     * @param feed feed to test.
     *
     * @return <code>TRUE</code> if is invalid.
     */
    static boolean isInvalid(IFeed feed)
    {
        return feed.isInvalid();
    }

    /**
     * Returns <code>TRUE</code> if feed is undiscovered.
     *
     * @param feed feed to test.
     *
     * @return <code>TRUE</code> if is undiscovered.
     */
    static boolean isUndiscovered(IFeed feed)
    {
        boolean undiscovered = false;
        if (feed instanceof DirectFeed)
        {
            FeedMetaDataHolder metaData = ((DirectFeed)feed).getMetaDataHolder();
            undiscovered = metaData == null || !metaData.isComplete();
        }

        return undiscovered;
    }
}
