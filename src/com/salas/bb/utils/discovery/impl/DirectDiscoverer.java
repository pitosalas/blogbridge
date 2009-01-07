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
// $Id: DirectDiscoverer.java,v 1.7 2006/11/27 17:00:02 spyromus Exp $
//
package com.salas.bb.utils.discovery.impl;

import com.salas.bb.utils.discovery.DiscoveryResult;
import com.salas.bb.utils.discovery.UrlDiscovererException;
import com.salas.bb.utils.discovery.UrlDiscovererIF;
import com.salas.bb.utils.discovery.detector.XMLFormat;
import com.salas.bb.utils.discovery.detector.XMLFormatDetector;
import com.salas.bb.utils.net.IPermanentRedirectionListener;
import com.salas.bb.utils.net.URLInputStream;

import java.io.IOException;
import java.net.URL;

/**
 * Checks if the URL points directly to the XML resources.
 */
public class DirectDiscoverer implements UrlDiscovererIF
{
    /**
     * Resolves source URL into corresponding XML URL ready for parsing.
     *
     * @param source source URL.
     *
     * @return result of discovery or <code>null</code> in case if URL cannot be resolved.
     *
     * @throws UrlDiscovererException in case of any errors.
     */
    public DiscoveryResult discover(URL source)
        throws UrlDiscovererException
    {
        boolean detected = false;

        RedirectionListener listener = new RedirectionListener(source);
        URLInputStream uis = null;
        try
        {
            uis = new URLInputStream(source, System.getProperty("http.agent.discoverer"));
            uis.setRedirectionListener(listener);

            XMLFormatDetector detector = new XMLFormatDetector();
            XMLFormat fmt = detector.detect(uis);

            detected = fmt != null && fmt != XMLFormat.OPML;
        } catch (IOException e)
        {
            throw new UrlDiscovererException(e);
        } finally
        {
            try
            {
                if (uis != null) uis.close();
            } catch (IOException e)
            {
                // No problems. Just trying to be nice.
            }
        }

        return detected ? new DiscoveryResult(DiscoveryResult.TYPE_FEED, listener.getURL()) : null;
    }

    /**
     * Listener to redirection events.
     */
    private static class RedirectionListener implements IPermanentRedirectionListener
    {
        private URL url;

        /**
         * Creates listener with given URL.
         *
         * @param aUrl url.
         */
        public RedirectionListener(URL aUrl)
        {
            url = aUrl;
        }

        /**
         * Invoked when redirection detected.
         *
         * @param newLocation new location.
         */
        public void redirectedTo(URL newLocation)
        {
            url = newLocation;
        }

        /**
         * Returns the URL which was queried. If there was redirection event, this URL will
         * hold new location.
         *
         * @return old or new URL.
         */
        public URL getURL()
        {
            return url;
        }
    }
}
