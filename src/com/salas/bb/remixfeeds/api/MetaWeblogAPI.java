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
// $Id: MetaWeblogAPI.java,v 1.26 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.remixfeeds.api;

import com.salas.bb.remixfeeds.prefs.TargetBlog;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MetaWeblog API implementation.
 */
abstract class MetaWeblogAPI implements IWeblogAPI
{
    private static final Logger LOG = Logger.getLogger(MetaWeblogAPI.class.getName());

    protected static final String ERR_FAILED_TO_CONTACT = "Failed to connect to the blog API.";
    protected static final String ERR_FAILED_CATEGORIES = "Failed to fetch categories.";
    protected static final String ERR_FAILED_BLOGS = "Failed to fetch blogs.";

    private final String system;

    /**
     * Hidden contructor.
     *
     * @param system the name of blogging system to manage (Wordpress, Movabletype).
     */
    MetaWeblogAPI(String system)
    {
        this.system = system;
    }

    /**
     * Tests connection to blog with the given preferences.
     *
     * @param blog preferences.
     *
     * @return <code>NULL</code> if fine, or error message.
     */
    public String validateBlog(TargetBlog blog)
    {
        if (StringUtils.isEmpty(blog.getApiURL()))
        {
            return Strings.message("ptb.prefs.details.setup.status.nourl");
        }

        String result;

        String apiUser = blog.getUser();
        String apiPassword = blog.getPassword();
        String originalURL = blog.getApiURL();
        String apiURL = StringUtils.fixURL(originalURL);
        if (!apiURL.equals(originalURL)) blog.setApiURL(apiURL);

        try
        {
            URL url = new URL(apiURL);

            result = checkForURL(url, apiUser, apiPassword);
            if (result != null)
            {
                String suggestedURL = suggestURL(url);

                if (suggestedURL != null)
                {
                    url = new URL(suggestedURL);
                    result = checkForURL(url, apiUser, apiPassword);

                    // If success, change the URL specified by user
                    if (result == null) blog.setApiURL(url.toString());
                }
            }
        } catch (MalformedURLException e)
        {
            LOG.log(Level.FINE, ERR_FAILED_CATEGORIES, e);
            result = Strings.message("ptb.prefs.details.setup.status.badurl");
        }

        return result;
    }

    /**
     * Try correcting the URL by adding the default name of the XMLRPC entry point.
     *
     * @param url URL.
     *
     * @return suggestion.
     */
    String suggestURL(URL url)
    {
        String suggestion = null;
        String defaultXMLRPCFile = getDefaultXMLRPCFile();

        if (defaultXMLRPCFile != null)
        {
            String urlS = url.toString();
            if (!urlS.endsWith("/")) urlS += "/";
            suggestion = urlS + defaultXMLRPCFile;
        }

        return suggestion;
    }

    /**
     * Returns the default location of the XMLRPC entry point
     * from root.
     *
     * @return file path.
     */
    protected abstract String getDefaultXMLRPCFile();

    private static String checkForURL(URL apiURL, String apiUser, String apiPassword)
    {
        String msg = null;

        boolean detected;
        try
        {
            XmlRpcClient cl = new XmlRpcClient(apiURL);
            Vector<String> params = new Vector<String>(3);
            params.add("0");
            params.add(apiUser);
            params.add(apiPassword);

            Object res = cl.execute("blogger.getUsersBlogs", params);
            detected = (res instanceof List && ((List)res).size() > 0);
        } catch (IOException e)
        {
            LOG.log(Level.FINE, ERR_FAILED_TO_CONTACT, e);
            detected = false;
        } catch (XmlRpcException e)
        {
            LOG.log(Level.INFO, ERR_FAILED_TO_CONTACT, e);
            msg = e.getMessage();
            if (StringUtils.isEmpty(msg)) msg = null;
            detected = false;
        }

        return detected ? null
            : msg != null ? msg : Strings.message("ptb.prefs.details.setup.status.undetected");
    }


    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return system;
    }

    // ----------------------------------------------------------------------------------
    // API
    // ----------------------------------------------------------------------------------

    /**
     * Fetches the list of categories.
     *
     * @param blog the blog to query.
     *
     * @return categories.
     */
    public TargetBlog.Category[] getCategories(TargetBlog blog)
    {
        return getCategories(blog, "metaWeblog.getCategories");
    }

    /**
     * Fetches the list of categories.
     *
     * @param blog blog to query.
     * @param apiCall the call.
     *
     * @return categories.
     */
    protected TargetBlog.Category[] getCategories(TargetBlog blog, String apiCall)
    {
        TargetBlog.Category[] categories = null;

        URL url = getApiURL(blog);
        if (url != null)
        {
            XmlRpcClient cl = new XmlRpcClient(url);
            Vector<String> params = new Vector<String>(3);
            params.add(getBlogID(blog));
            params.add(blog.getUser());
            params.add(blog.getPassword());

            try
            {
                Object res = cl.execute(apiCall, params);
                categories = res == null ? null : parseFetchedCategories(res);
            } catch (IOException e)
            {
                LOG.log(Level.FINE, ERR_FAILED_CATEGORIES, e);
            } catch (XmlRpcException e)
            {
                LOG.log(Level.WARNING, ERR_FAILED_CATEGORIES, e);
            }
        }
        
        return categories;
    }

    /**
     * Parses the categories we just fetched from the server into
     * the list of category objects.
     *
     * @param res   the result of the fetch.
     *
     * @return the categories list.
     */
    protected TargetBlog.Category[] parseFetchedCategories(Object res)
    {
        List cats = (List)res;
        TargetBlog.Category[] categories = new TargetBlog.Category[cats.size()];

        for (int i = 0; i < cats.size(); i++)
        {
            Map category = (Map)cats.get(i);
            try
            {
                String id = (String)category.get("categoryId");
                String name = (String)category.get("categoryName");

                categories[i] = new TargetBlog.Category(id, name);
            } catch (NumberFormatException e)
            {
                LOG.log(Level.WARNING, "Skipping category. Invalid ID format.", e);
            }
        }

        return categories;
    }


    /**
     * Fetches the list of all available blogs.
     *
     * @param blog blog preferences.
     *
     * @return categories.
     */
    public TargetBlog.Blog[] getBlogs(TargetBlog blog)
    {
        TargetBlog.Blog[] blogs = null;

        URL url = getApiURL(blog);
        if (url != null)
        {
            XmlRpcClient cl = new XmlRpcClient(url);
            Vector<String> params = new Vector<String>(3);
            params.add(getBlogID(blog));
            params.add(blog.getUser());
            params.add(blog.getPassword());

            try
            {
                List bs = (List)cl.execute("blogger.getUsersBlogs", params);
                if (bs != null)
                {
                    blogs = new TargetBlog.Blog[bs.size()];
                    for (int i = 0; i < bs.size(); i++)
                    {
                        Map b = (Map)bs.get(i);
                        try
                        {
                            String ids = (String)b.get("blogid");
                            String name = (String)b.get("blogName");

                            blogs[i] = new TargetBlog.Blog(ids, name);
                        } catch (NumberFormatException e)
                        {
                            LOG.log(Level.WARNING, "Skipping blog. Invalid ID format.", e);
                        }
                    }
                }
            } catch (IOException e)
            {
                LOG.log(Level.FINE, ERR_FAILED_BLOGS, e);
            } catch (XmlRpcException e)
            {
                LOG.log(Level.WARNING, ERR_FAILED_BLOGS, e);
            }
        }

        return blogs;
    }

    /**
     * Returns ID of the blog from the preferences.
     *
     * @param prefs preferences.
     *
     * @return blog ID or 0.
     */
    protected static String getBlogID(TargetBlog prefs)
    {
        TargetBlog.Blog blog = prefs.getBlog();
        return blog != null ? blog.id : "0";
    }

    /**
     * Creates new post on the blog.
     *
     * @param prefs blog preferences.
     * @param post  post info.
     *
     * @throws WeblogAPIException in case of any problems with posting.
     */
    public void newPost(TargetBlog prefs, WeblogPost post) throws WeblogAPIException
    {
        newPost0(prefs, post);
    }

    /**
     * Creates new post on the blog.
     *
     * @param prefs blog preferences.
     * @param post  post info.
     *
     * @return the results of a call.
     * 
     * @throws WeblogAPIException in case of any problems with posting.
     */
    protected Object newPost0(TargetBlog prefs, WeblogPost post)
        throws WeblogAPIException
    {
        Object res;

        URL apiURL = getApiURL(prefs);
        if (apiURL == null) throw new WeblogAPIException("Blog is incorrectly configured.");

        Map<String, Object> content = new Hashtable<String, Object>();
        content.put("title", post.title);
        content.put("description", post.description);
        if (post.sourceURL != null && StringUtils.isNotEmpty(post.sourceTitle))
        {
            content.put("source", source(post.sourceURL, post.sourceTitle));
        }

        // Category names
        String[] catNames = null;
        List<TargetBlog.Category> categories = post.categories;
        if (categories == null)
        {
            TargetBlog.Category catdef = prefs.getDefaultCategory();
            if (catdef != null)
            {
                catNames = new String[] { catdef.name };
            }
        } else
        {
            catNames = new String[categories.size()];
            int i = 0;
            for (TargetBlog.Category c : categories) catNames[i++] = c.name;
        }
        if (catNames != null) content.put("categories", catNames);
        content.put("mt_convert_breaks", "0");

        // Publication date
        if (post.dateCreated != null)
        {
            Calendar now = new GregorianCalendar();

            // Add time to the date
            Calendar local = new GregorianCalendar();
            local.setTime(post.dateCreated);
            local.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
            local.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

            // Adjust time to switch to UTC (GMT)
            // It won't show the timezone in the XMLRPC <dateTime.iso8601> tag and will assume GMT
            // so let's make it GMT.
            TimeZone tz = TimeZone.getDefault();
            local.add(Calendar.MILLISECOND, - (tz.getRawOffset() + tz.getDSTSavings()));

            content.put("date_created_gmt", local.getTime());
            content.put("dateCreated", local.getTime());
        }

        // Excerpt
        if (StringUtils.isNotEmpty(post.excerpt)) content.put("mt_excerpt", post.excerpt.trim());
        setSpecificContentFields(content, post);

        XmlRpcClient cl = new XmlRpcClient(apiURL);
        Vector<Object> params = new Vector<Object>(5);
        params.add(getBlogID(prefs));
        params.add(prefs.getUser());
        params.add(prefs.getPassword());
        params.add(content);
        params.add(post.publish);

        try
        {
            res = cl.execute("metaWeblog.newPost", params);
        } catch (Throwable e)
        {
            throw new WeblogAPIException("Failed to create a post.", e);
        }

        return res;
    }

    /**
     * API-dependent content settings.
     *
     * @param content   content.
     * @param post      post info.
     */
    protected void setSpecificContentFields(Map<String, Object> content, WeblogPost post)
    {
        content.put("mt_allow_comments", post.allowComments ? "1" : "0");
        content.put("mt_allow_pings", post.allowTrackbacks ? "1" : "0");
    }

    /**
     * Creates source array.
     *
     * @param sourceURL     source URL.
     * @param sourceTitle   source title.
     *
     * @return source element.
     */
    private static Map<String, String> source(URL sourceURL, String sourceTitle)
    {
        Map<String, String> source = new Hashtable<String, String>();
        source.put("url", sourceURL.toString());
        source.put("name", sourceTitle);

        return source;
    }

    /**
     * Returns API URL or <code>NULL</code> if URL isn't valid.
     *
     * @param prefs preferences.
     *
     * @return API URL or <code>NULL</code>.
     */
    protected static URL getApiURL(TargetBlog prefs)
    {
        URL url;

        try
        {
            url = new URL(prefs.getApiURL());
        } catch (MalformedURLException e)
        {
            url = null;
        }

        return url;
    }

    /**
     * Returns TRUE if the API URL field is not applicable to this weblog API.
     *
     * @return TRUE if not applicable.
     */
    public boolean isApiUrlApplicable()
    {
        return true;
    }
}
