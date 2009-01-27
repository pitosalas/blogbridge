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
// $Id$
//

package com.salas.bb.twitter;

import com.salas.bb.utils.net.HttpClient;
import com.salas.bb.core.GlobalController;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

/**
 * Twitter gateway.
 */
public class TwitterGateway
{
    /**
     * Posts an update to the twitter account.
     *
     * @param status status message.
     *
     * @throws java.io.IOException if fails to communicate.
     */
    public static void update(String status)
        throws IOException
    {
        reply(status, null);
    }

    /**
     * Posts a reply to the twitter account.
     *
     * @param status    status message.
     * @param replyToId ID of the original message or NULL.
     *
     * @throws java.io.IOException if fails to communicate.
     */
    public static void reply(String status, String replyToId)
        throws IOException
    {
        URL url = new URL("http://twitter.com/statuses/update.json");

        Map<String, String> data = new HashMap<String, String>();
        data.put("status", status);
        if (replyToId != null) data.put("in_reply_to_status_id", replyToId);

        TwitterPreferences prefs = getPreferences();
        HttpClient.post(url, data, prefs.getScreenName(), prefs.getPassword());
    }

    /**
     * Returns preferences.
     *
     * @return preferences.
     */
    private static TwitterPreferences getPreferences()
    {
        return GlobalController.SINGLETON.getModel().getUserPreferences().getTwitterPreferences();
    }
}
