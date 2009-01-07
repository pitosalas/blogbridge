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
// $Id: WeblogAPIs.java,v 1.7 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Weblog API center.
 */
public abstract class WeblogAPIs
{
    private static final IWeblogAPI MOVABLE_TYPE = new MovableType();
    private static final IWeblogAPI WORDPRESS = new Wordpress();
    private static final IWeblogAPI DRUPAL = new Drupal();
    private static final IWeblogAPI ROLLER = new Roller();

    private static final Map<String, IWeblogAPI> REGISTRY = new HashMap<String, IWeblogAPI>();

    static {
        REGISTRY.put(WORDPRESS.getTypeID(), WORDPRESS);
        REGISTRY.put(MOVABLE_TYPE.getTypeID(), MOVABLE_TYPE);
        REGISTRY.put(DRUPAL.getTypeID(), DRUPAL);
        REGISTRY.put(ROLLER.getTypeID(), ROLLER);
    }

    /**
     * Returns the default API to use for weblogs.
     *
     * @return default API.
     */
    public static IWeblogAPI getDefaultWeblogAPI()
    {
        return WORDPRESS;
    }

    /**
     * Returns the instance of weblog API by its ID.
     *
     * @param id type ID.
     *
     * @return instance of API.
     */
    public static IWeblogAPI getWeblogAPIByID(String id)
    {
        IWeblogAPI api = id == null ? null : REGISTRY.get(id);
        return api == null ? getDefaultWeblogAPI() : api;
    }

    /**
     * Returns the collection of all registered weblog APIs.
     *
     * @return APIs.
     */
    public static Collection getWeblogAPIs()
    {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Adds the API to the registry.
     *
     * @param api API to add.
     */
    public static void addWeblogAPI(IWeblogAPI api)
    {
        String type = api.getTypeID();
        if (!REGISTRY.containsKey(type)) REGISTRY.put(type, api);
    }
}
