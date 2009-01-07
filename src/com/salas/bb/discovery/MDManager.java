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
// $Id: MDManager.java,v 1.4 2006/01/08 04:45:31 kyank Exp $
//

package com.salas.bb.discovery;

import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.utils.ConnectionState;

import java.net.URL;

/**
 * Manager of meta-data operations.
 */
public final class MDManager
{
    // discoverer of meta-data
    private final MDDiscoverer    discoverer;

    // repository of meta-data
    private final MDRepository    repository;

    /** Connection state interface. */
    private final ConnectionState connectionState;

    /**
     * Creates manager with default compoenents.
     *
     * @param aConnectionState connection state interface.
     */
    public MDManager(ConnectionState aConnectionState)
    {
        this(new MDDiscoverer(aConnectionState), new MDRepository(), aConnectionState);
    }

    /**
     * Creates manager with particular components.
     *
     * @param aDiscoverer       discoverer component.
     * @param aRepository       repository component.
     * @param aConnectionState  connection state interface.
     */
    MDManager(MDDiscoverer aDiscoverer, MDRepository aRepository, ConnectionState aConnectionState)
    {
        discoverer = aDiscoverer;
        repository = aRepository;
        connectionState = aConnectionState;
    }

    /**
     * Looks up the meta-data for a given URL or starts new discovery if it is not
     * present in repository yet.
     *
     * @param url   URL to lookup.
     *
     * @return meta-data object.
     */
    public FeedMetaDataHolder lookupOrDiscover(URL url)
    {
        FeedMetaDataHolder holder = lookup(url);

        if (holder == null)
        {
            holder = new FeedMetaDataHolder();
            repository.record(holder, url);
            discoverer.scheduleDiscovery(url, holder);
        }

        return holder;
    }

    /**
     * Looks up the meta-data for a given URL. If meta-data is not present <code>NULL</code>
     * will be returned.
     *
     * @param url   URL to lookup.
     *
     * @return meta-data object or <code>NULL</code>.
     */
    public FeedMetaDataHolder lookup(URL url)
    {
        return repository.lookup(url);
    }

    /**
     * Returns all valid meta-data objects which are currently in repository.
     *
     * @return all valid meta-data objects.
     */
    public FeedMetaDataHolder[] lookupValid()
    {
        return repository.lookupValid();
    }

    /**
     * Updates the meta-data holder using the given URL.
     *
     * @param holder    holder to update.
     * @param url       base URL to use in updating discovery.
     */
    public void update(FeedMetaDataHolder holder, URL url)
    {
        // Record this holder to repository if it isn't there yet
        repository.record(holder, url);

        discoverer.scheduleDiscovery(url, holder);
    }

    /**
     * Orders the repository to forget anything about given holders.
     *
     * @param aHolders hoders to forget.
     */
    public void forget(FeedMetaDataHolder[] aHolders)
    {
        repository.forget(aHolders);
    }

    /**
     * Adds discovery listener.
     *
     * @param aListener listener.
     */
    public void addDiscoveryListener(IDiscoveryListener aListener)
    {
        discoverer.addListener(aListener);
    }
}
