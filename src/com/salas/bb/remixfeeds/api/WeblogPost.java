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
// $Id: WeblogPost.java,v 1.3 2007/01/31 09:13:29 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import com.salas.bb.remixfeeds.prefs.TargetBlog;

import java.net.URL;
import java.util.List;
import java.util.Date;

/**
 * Weblog post info holder.
 */
public class WeblogPost
{
    public final String title;
    public final String description;
    public String excerpt = null;

    public final URL sourceURL;
    public final String sourceTitle;

    public boolean allowComments = true;
    public boolean allowTrackbacks = true;

    public List<TargetBlog.Category> categories;
    public final boolean publish;
    public Date dateCreated;

    /**
     * Creates a holder.
     *
     * @param title         title of the post.
     * @param description   content of the post.
     * @param sourceURL     source feed URL (can be <code>NULL</code>)
     * @param sourceTitle   source feed Title (can be <code>NULL</code>)
     * @param publish       <code>TRUE</code> to publish, otherwise -- save as draft.
     */
    public WeblogPost(String title, String description, URL sourceURL, String sourceTitle,
                      boolean publish)
    {
        this.title = title;
        this.description = description;
        this.sourceURL = sourceURL;
        this.sourceTitle = sourceTitle;
        this.publish = publish;
    }
}
