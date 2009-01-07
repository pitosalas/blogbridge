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
// $Id: TestMDDiscoverer.java,v 1.3 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.discovery;

import junit.framework.TestCase;
import EDU.oswego.cs.dl.util.concurrent.Executor;

import java.net.URL;
import java.net.MalformedURLException;

import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.utils.ConnectionState;

/**
 * This suite contains tests for <code>MDDiscoverer</code> unit.
 */
public class TestMDDiscoverer extends TestCase
{
    /**
     * Tests skipping duplicate requests for discovery.
     */
    public void testNoDuplicateDiscoveries()
    {
        CountingNoActionExecutor noActionExecutor = new CountingNoActionExecutor();
        MDDiscoverer discoverer = new MDDiscoverer(noActionExecutor, new ConnectionState());

        // Schedule first discovery.
        discoverer.scheduleDiscovery(getTestLocalURL(), new FeedMetaDataHolder());
        assertEquals(1, noActionExecutor.executions);

        // Schedule the same discovery, while the first hasn't finished (not executed).
        // The execution shouldn't happen.
        discoverer.scheduleDiscovery(getTestLocalURL(), new FeedMetaDataHolder());
        assertEquals(1, noActionExecutor.executions);
    }

    /**
     * Creates local URL.
     *
     * @return local URL.
     */
    private URL getTestLocalURL()
    {
        URL url = null;
        try
        {
            url = new URL("file://some");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Executor which isn't doing any actions, but counting them.
     */
    private static class CountingNoActionExecutor implements Executor
    {
        private int executions = 0;

        /**
         * No action is executed.
         *
         * @param runnable runnable to skip.
         *
         * @throws InterruptedException if waiting has been interrupted.
         */
        public void execute(Runnable runnable)
            throws InterruptedException
        {
            executions++;
        }
    }
}
