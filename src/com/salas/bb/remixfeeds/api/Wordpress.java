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
// $Id: Wordpress.java,v 1.8 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import com.salas.bb.remixfeeds.prefs.TargetBlog;
import org.apache.xmlrpc.XmlRpcException;

import java.util.Map;

/**
 * Movable type API.
 */
public class Wordpress extends MetaWeblogAPI
{
    private static final String TYPE = "Wordpress";

    /**
     * Hidden contructor.
     */
    Wordpress()
    {
        super("Wordpress");
    }

    /**
     * Hidden contructor.
     *
     * @param system the name of blogging system to manage (Wordpress, Movabletype).
     */
    Wordpress(String system)
    {
        super(system);
    }

    /**
     * Returns the ID of the API.
     *
     * @return ID.
     */
    public String getTypeID()
    {
        return TYPE;
    }

    /**
     * Returns the default location of the XMLRPC entry point
     * from root.
     *
     * @return file path.
     */
    protected String getDefaultXMLRPCFile()
    {
        return "xmlrpc.php";
    }


    /**
     * Creates new post on the blog.
     *
     * @param prefs blog preferences.
     * @param post  post info.
     *
     * @throws WeblogAPIException in case of any problems with posting.
     */
    public void newPost(TargetBlog prefs, WeblogPost post)
        throws WeblogAPIException
    {
        try
        {
            super.newPost(prefs, post);
        } catch (WeblogAPIException e)
        {
            Throwable clause = e.getCause();
            if (clause instanceof XmlRpcException)
            {
                throw new WeblogAPIException(clause.getMessage(), clause);
            } else throw e;
        }
    }

    /**
     * Returns the name of the default category to use when no category is set.
     *
     * @return the name (can't be <code>NULL</code>, but can be empty string).
     */
    public TargetBlog.Category getDefaultCategory()
    {
        return new TargetBlog.Category("0", "Uncategorized");
    }


    /**
     * API-dependent content settings.
     *
     * @param content content.
     * @param post    post info.
     */
    protected void setSpecificContentFields(Map content, WeblogPost post)
    {
        content.put("mt_allow_comments", post.allowComments ? "open" : "closed");
        content.put("mt_allow_pings", post.allowTrackbacks ? "open" : "closed");
    }

    /**
     * Returns the description for this web API and its fields.
     *
     * @return the description.
     */
    public String getDescription()
    {
        return "Wordpress powered blogs require (XMLRPC) API URL, user names and passwords to log in.\n\n" +
            "Typically the API URL is http://your.blog/xmlrpc.php";
    }
}
