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
// $Id: MDRepository.java,v 1.7 2006/03/16 15:32:46 spyromus Exp $
//

package com.salas.bb.discovery;

import com.salas.bb.domain.FeedMetaDataHolder;

import java.net.URL;
import java.util.*;

/**
 * Repository of discovered meta-data information. Plays role of run-time
 * cache.
 */
final class MDRepository
{
    private final Map repository;

    /**
     * Creates repository.
     */
    public MDRepository()
    {
        repository = new HashMap();
    }

    /**
     * Returns meta-data for a given URL.
     *
     * @param url URL.
     *
     * @return meta-data object.
     */
    public FeedMetaDataHolder lookup(URL url)
    {
        FeedMetaDataHolder holder = null;

        if (url != null)
        {
            String urlString = url.toString();
            synchronized (repository)
            {
                holder = (FeedMetaDataHolder)repository.get(urlString);
            }
        }

        return holder;
    }

    /**
     * Returns all valid meta-data objects which are currently in repository.
     *
     * @return all valid meta-data objects.
     */
    public FeedMetaDataHolder[] lookupValid()
    {
        FeedMetaDataHolder[] holders;

        synchronized (repository)
        {
            Collection holdersCol = repository.values();
            holders = (FeedMetaDataHolder[])holdersCol.toArray(
                new FeedMetaDataHolder[holdersCol.size()]);
        }

        List holdersList = new ArrayList(holders.length);
        for (int i = 0; i < holders.length; i++)
        {
            FeedMetaDataHolder holder = holders[i];
            if (holder.isComplete() && holder.isDiscoveredValid() && !holdersList.contains(holder))
            {
                holdersList.add(holder);
            }
        }

        return (FeedMetaDataHolder[])holdersList.toArray(
            new FeedMetaDataHolder[holdersList.size()]);
    }

    /**
     * Records the holder in repository.
     *
     * @param holder    holder to record.
     * @param url       URL to associate with.
     */
    public void record(FeedMetaDataHolder holder, URL url)
    {
        String urlString = url.toString();

        synchronized (repository)
        {
            if (!repository.containsKey(urlString))
            {
                repository.put(urlString, holder);
            }
        }
    }

    /**
     * Orders the repository to forget anything about given holders.
     *
     * @param aHolders hoders to forget.
     */
    public void forget(FeedMetaDataHolder[] aHolders)
    {
        List holdersList = Arrays.asList(aHolders);

        Map.Entry[] entries;
        synchronized (repository)
        {
            Set entriesSet = repository.entrySet();
            entries = (Map.Entry[])entriesSet.toArray(new Map.Entry[entriesSet.size()]);
        }

        List keysToRemove = new ArrayList();
        for (int i = 0; i < entries.length; i++)
        {
            Map.Entry entry = entries[i];
            FeedMetaDataHolder holder = (FeedMetaDataHolder)entry.getValue();

            if (holdersList.contains(holder)) keysToRemove.add(entry.getKey());
        }

        Iterator iterator = keysToRemove.iterator();
        synchronized (repository)
        {
            while (iterator.hasNext())
            {
                Object key = iterator.next();
                repository.remove(key);
            }
        }
    }
}
