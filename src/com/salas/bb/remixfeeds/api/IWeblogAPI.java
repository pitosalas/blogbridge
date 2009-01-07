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
// $Id: IWeblogAPI.java,v 1.9 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import com.salas.bb.remixfeeds.prefs.TargetBlog;

/**
 * General Weblog XML RPC API.
 */
public interface IWeblogAPI
{
    /**
     * Returns the ID of the API.
     *
     * @return ID.
     */
    String getTypeID();

    /**
     * Tests connection to blog with the given preferences.
     *
     * @param blog preferences.
     *
     * @return <code>NULL</code> if fine, or error message.
     */
    String validateBlog(TargetBlog blog);

    /**
     * Fetches the list of categories.
     *
     * @param blog blog preferences.
     *
     * @return categories.
     */
    TargetBlog.Category[] getCategories(TargetBlog blog);

    /**
     * Fetches the list of all available blogs.
     *
     * @param blog blog preferences.
     *
     * @return categories.
     */
    TargetBlog.Blog[] getBlogs(TargetBlog blog);

    /**
     * Creates new post on the blog.
     *
     * @param blog  blog preferences.
     * @param post  post info.
     *
     * @throws WeblogAPIException in case of any problems with posting.
     */
    void newPost(TargetBlog blog, WeblogPost post) throws WeblogAPIException;

    /**
     * Returns the name of the default category to use when no category is set.
     *
     * @return the name (can't be <code>NULL</code>, but can be empty string).
     */
    TargetBlog.Category getDefaultCategory();

    /**
     * Returns TRUE if the API URL field is not applicable to this weblog API.
     *
     * @return TRUE if not applicable.
     */
    boolean isApiUrlApplicable();

    /**
     * Returns the description for this web API and its fields.
     *
     * @return the description.
     */
    String getDescription();
}
