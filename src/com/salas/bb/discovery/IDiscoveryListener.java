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
// $Id: IDiscoveryListener.java,v 1.11 2006/03/23 11:59:18 spyromus Exp $
//

package com.salas.bb.discovery;

import java.net.URL;

/**
 * Listener of events from discovery module.
 */
public interface IDiscoveryListener
{
    /**
     * Invoked when discovery of some meta-data object started.
     *
     * @param url   URL being discovered.
     */
    void discoveryStarted(URL url);

    /**
     * Invoked when discovery of some meta-data object successfully finished.
     *
     * @param url       URL has been discovered.
     * @param complete  <code>TRUE</code> when discovery is complete and there will be no
     *                  rediscovery scheduled.
     */
    void discoveryFinished(URL url, boolean complete);

    /**
     * Invoked when discovery of some meta-data object failed.
     *
     * @param url   URL has been failed to discover.
     */
    void discoveryFailed(URL url);
}
