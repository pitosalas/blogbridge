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
// $Id: DiscoverAction.java,v 1.1 2008/04/01 08:20:05 spyromus Exp $
//

package com.salas.bb.discovery.actions;

import com.salas.bb.core.GlobalController;
import com.salas.bb.discovery.MDManager;
import com.salas.bb.domain.IArticle;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;

/**
 * Basic discovery action.
 */
public abstract class DiscoverAction extends AbstractAction
{
    /**
     * Starts the discovery for links from the given articles.
     *
     * @param articles articles to scan for links.
     */
    protected static void discover(IArticle[] articles)
    {
        LinkedHashSet<String> articleLinks = new LinkedHashSet<String>();
        for (IArticle article : articles) articleLinks.addAll(article.getLinks());

        MDManager mdmanager = GlobalController.SINGLETON.getMetaDataManager();
        for (String link : articleLinks)
        {
            try
            {
                // Lookup or discover the link.
                // If it's already known, the result will be returned without
                // any overhead; if not, the discovery will be scheduled.
                mdmanager.lookupOrDiscover(new URL(link));
            } catch (MalformedURLException e1)
            {
                // Not a problem, just skip
            }
        }
    }
}
