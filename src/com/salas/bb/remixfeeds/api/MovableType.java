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
// $Id: MovableType.java,v 1.9 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import com.salas.bb.remixfeeds.prefs.TargetBlog;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Movable type API.
 */
public class MovableType extends MetaWeblogAPI
{
    private static final String TYPE = "MovableType";

    /**
     * Hidden contructor.
     */
    MovableType()
    {
        super("Movable Type");
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
        return "mt-xmlrpc.cgi";
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
     * Fetches the list of categories.
     *
     * @param blog the blog to query.
     * 
     * @return categories.
     */
    public TargetBlog.Category[] getCategories(TargetBlog blog)
    {
        return getCategories(blog, "mt.getCategoryList");
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
        Object res = super.newPost0(prefs, post);

        if (res != null)
        {
            String postId = (String)res;

            // Setting category
            XmlRpcClient cl = new XmlRpcClient(getApiURL(prefs));
            Vector<Object> params = new Vector<Object>(4);
            params.add(postId);
            params.add(prefs.getUser());
            params.add(prefs.getPassword());

            try
            {
                params.add(packCategories(post.categories));
                cl.execute("mt.setPostCategories", params);
            } catch (Throwable e)
            {
                throw new WeblogAPIException("Failed to set category to a post.", e);
            }
        }
    }

    /**
     * Pack category for sending to setPostCategories call.
     *
     * @param categories  category.
     *
     * @return object ready for
     */
    private Object[] packCategories(List<TargetBlog.Category> categories)
    {
        Object[] res = new Object[categories.size()];

        int i = 0;
        for (TargetBlog.Category c : categories)
        {
            Map<String, String> m = new Hashtable<String, String>();
            m.put("categoryId", c.id);
            res[i++] = m;
        }

        return res;
    }

    /**
     * Returns the description for this web API and its fields.
     *
     * @return the description.
     */
    public String getDescription()
    {
        return "MovableType powered blogs require (XMLRPC) API URL, user names and passwords to log in.\n\n" +
            "Typically the API URL is http://your.blog/mt-xmlrpc.cgi";
    }
}
