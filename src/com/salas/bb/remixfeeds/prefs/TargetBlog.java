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
// $Id: TargetBlog.java,v 1.6 2008/03/31 14:48:14 spyromus Exp $
//

package com.salas.bb.remixfeeds.prefs;

import com.jgoodies.binding.beans.Model;
import com.salas.bb.remixfeeds.api.IWeblogAPI;
import com.salas.bb.remixfeeds.api.WeblogAPIs;
import com.salas.bb.remixfeeds.templates.Templates;
import com.salas.bb.utils.IdNameHolder;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

/**
 * Single blog preferences.
 */
public class TargetBlog extends Model
{
    private static final MessageFormat FMT = new MessageFormat("blog.{0}.{1}");

    public static final String PROP_TITLE               = "title";
    public static final String PROP_API_URL             = "apiURL";
    public static final String PROP_API_TYPE            = "apiType";
    public static final String PROP_USER                = "user";
    public static final String PROP_PASSWORD            = "password";
    public static final String PROP_DEFAULT_CATEGORY    = "defaultCategory";
    public static final String PROP_DRAFT               = "draft";
    public static final String PROP_MODE                = "mode";
    public static final String PROP_BLOG                = "blog";
    public static final String PROP_TEMPLATE_NAME       = "templateName";

    public static final int MODE_TITLE_AND_LINK = 0;
    public static final int MODE_EXCERPT_AND_LINK = 1;
    public static final int MODE_FULL_TEXT = 2;

    protected String      title;
    protected String      apiURL;
    protected IWeblogAPI  apiType;

    protected String      user;
    protected String      password;

    protected Category    defaultCategory;
    protected boolean     draft;
    protected int         mode;

    protected Blog        blog;

    // Simple storage (no need to persist)
    private Category[] categories = new Category[0];
    private Blog[] blogs = new Blog[0];

    // The name of a template to use as default
    protected String      templateName;

    /**
     * Returns blog title.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets blog title.
     *
     * @param title title.
     */
    public void setTitle(String title)
    {
        String oldTitle = this.title;
        this.title = title;
        firePropertyChange(PROP_TITLE, oldTitle, title);
    }

    /**
     * Returns XMLRPC API url.
     *
     * @return API URL.
     */
    public String getApiURL()
    {
        return apiURL;
    }

    /**
     * Sets XMLRPC API URL.
     *
     * @param apiURL API URL.
     */
    public void setApiURL(String apiURL)
    {
        String oldApiURL = this.apiURL;
        this.apiURL = apiURL;
        firePropertyChange(PROP_API_URL, oldApiURL, apiURL);
    }

    /**
     * Returns XMLRPC API type.
     *
     * @return API type.
     */
    public IWeblogAPI getApiType()
    {
        return apiType;
    }

    /**
     * Sets XMLRPC API type.
     *
     * @param apiType API type.
     */
    public void setApiType(IWeblogAPI apiType)
    {
        IWeblogAPI oldApiType = this.apiType;
        this.apiType = apiType;
        firePropertyChange(PROP_API_TYPE, oldApiType, apiType);
    }

    /**
     * Returns user name of the blog.
     *
     * @return user name.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Sets user name of the blog.
     *
     * @param user user name.
     */
    public void setUser(String user)
    {
        String oldUser = this.user;
        this.user = user;
        firePropertyChange(PROP_USER, oldUser, user);
    }

    /**
     * Returns user password to access XML API.
     *
     * @return user password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets user password to access XML API.
     *
     * @param password password.
     */
    public void setPassword(String password)
    {
        String oldPassword = this.password;
        this.password = password;
        firePropertyChange(PROP_PASSWORD, oldPassword, password);
    }

    /**
     * Returns default category to post to.
     *
     * @return default category.
     */
    public Category getDefaultCategory()
    {
        return defaultCategory;
    }

    /**
     * Sets default category to post to.
     *
     * @param defaultCategory default category.
     */
    public void setDefaultCategory(Category defaultCategory)
    {
        Category oldDefaultCategory = this.defaultCategory;
        this.defaultCategory = defaultCategory;
        firePropertyChange(PROP_DEFAULT_CATEGORY, oldDefaultCategory, defaultCategory);
    }

    /**
     * Returns <code>TRUE</code> if posts should be sent as drafts by default.
     *
     * @return <code>TRUE</code> if posts should be sent as drafts by default.
     */
    public boolean isDraft()
    {
        return draft;
    }

    /**
     * Sets the draft by default flag.
     *
     * @param draft <code>TRUE</code> if posts should be sent as drafts by default.
     */
    public void setDraft(boolean draft)
    {
        boolean oldDraft = this.draft;
        this.draft = draft;
        firePropertyChange(PROP_DRAFT, oldDraft, draft);
    }

    /**
     * Returns the mode.
     *
     * @return mode.
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * Sets the new mode.
     *
     * @param mode mode.
     */
    public void setMode(int mode)
    {
        int oldMode = this.mode;
        this.mode = mode;
        firePropertyChange(PROP_MODE, oldMode, mode);
    }

    /**
     * Returns currently selected blog.
     *
     * @return blog.
     */
    public Blog getBlog()
    {
        return blog;
    }

    /**
     * Sets new selected blog.
     *
     * @param blog blog.
     */
    public void setBlog(Blog blog)
    {
        Blog old = this.blog;
        this.blog = blog;
        firePropertyChange(PROP_BLOG, old, blog);
    }

    /**
     * Returns the template name.
     *
     * @return template name.
     */
    public String getTemplateName()
    {
        String name = templateName;
        if (name == null) name = Templates.fromOldMode(mode);

        return name;
    }

    /**
     * Sets the name of the template to use for rendering.
     *
     * @param name template name.
     */
    public void setTemplateName(String name)
    {
        String oldTemplateName = templateName;
        templateName = name;
        firePropertyChange(PROP_TEMPLATE_NAME, oldTemplateName, templateName);
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    protected TargetBlog createClone()
    {
        TargetBlog prefs = new TargetBlog();

        prefs.setTitle(getTitle());
        prefs.setApiURL(getApiURL());
        prefs.setApiType(getApiType());
        prefs.setUser(getUser());
        prefs.setPassword(getPassword());
        prefs.setDraft(isDraft());
        prefs.setMode(getMode());

        prefs.setDefaultCategory(getDefaultCategory());
        prefs.setCategories(getCategories());
        prefs.setBlog(getBlog());
        prefs.setBlogs(getBlogs());

        prefs.setTemplateName(getTemplateName());

        return prefs;
    }

    // ----------------------------------------------------------------------------------
    // Storage attributes
    // ----------------------------------------------------------------------------------

    /**
     * Returns the list of categories.
     *
     * @return categories.
     */
    public Category[] getCategories()
    {
        return categories;
    }

    /**
     * Updates categories with new list.
     *
     * @param newList new list to be copied to the categories lookup.
     */
    public void setCategories(Category[] newList)
    {
        categories = newList == null ? new Category[0] : newList;
    }

    /**
     * Returns the list of blogs.
     *
     * @return blogs.
     */
    public Blog[] getBlogs()
    {
        return blogs;
    }

    /**
     * Sets the map of blogs.
     *
     * @param newList new map.
     */
    public void setBlogs(Blog[] newList)
    {
        blogs = newList == null ? new Blog[0] : newList;
    }

    // ----------------------------------------------------------------------------------
    // Work methods
    // ----------------------------------------------------------------------------------

    /**
     * Tests the connection to the blog using this configuration.
     *
     * @return <code>NULL</code> if fine or error message.
     */
    public String testConnection()
    {
        return apiType == null
            ? Strings.message("ptb.prefs.details.setup.status.noapi")
            : apiType.validateBlog(this);
    }

    // ----------------------------------------------------------------------------------
    // Persistence
    // ----------------------------------------------------------------------------------

    /**
     * Stores its preferences as a blog number <code>cnt</code>.
     *
     * @param cnt   sequence number of this blog.
     * @param prefs preferences map.
     */
    void store(int cnt, Preferences prefs)
    {
        String n = Integer.toString(cnt);

        put(prefs, n, PROP_TITLE, getTitle());
        put(prefs, n, PROP_API_URL, getApiURL());
        put(prefs, n, PROP_API_TYPE, getApiType().getTypeID());

        put(prefs, n, PROP_USER, getUser());
        put(prefs, n, PROP_PASSWORD, getPassword());

        Category.store(prefs, prop(n, PROP_DEFAULT_CATEGORY), getDefaultCategory());
        Blog.store(prefs, prop(n, PROP_BLOG), getBlog());

        put(prefs, n, PROP_DRAFT, isDraft());
        put(prefs, n, PROP_MODE, Integer.toString(getMode()));

        put(prefs, n, PROP_TEMPLATE_NAME, getTemplateName());
    }

    /**
     * Fetches preferences from the record for a blog number <code>cnt</code>.
     *
     * @param cnt   sequence number of a blog.
     * @param prefs preferences map.
     */
    void restore(int cnt, Preferences prefs)
    {
        String n = Integer.toString(cnt);

        setTitle(get(prefs, n, PROP_TITLE));
        setApiURL(get(prefs, n, PROP_API_URL));
        setApiType(getApiType(prefs, n, PROP_API_TYPE));

        setUser(get(prefs, n, PROP_USER));
        setPassword(get(prefs, n, PROP_PASSWORD));

        setDefaultCategory(Category.restore(prefs, prop(n, PROP_DEFAULT_CATEGORY)));
        setBlog(Blog.restore(prefs, prop(n, PROP_BLOG)));

        setDraft(getBoolean(prefs, n, PROP_DRAFT));
        setMode(getInteger(prefs, n, PROP_MODE, MODE_FULL_TEXT));

        setTemplateName(get(prefs, n, PROP_TEMPLATE_NAME));
    }

    /**
     * Returns integer value from the preferences map.
     *
     * @param prefs preferences map.
     * @param cnt   sequence number of a blog.
     * @param name  property name.
     * @param def   default value.
     *
     * @return value.
     */
    private int getInteger(Preferences prefs, String cnt, String name, int def)
    {
        return prefs.getInt(prop(cnt, name), def);
    }

    /**
     * Returns boolean value from the preferences map.
     *
     * @param prefs preferences map.
     * @param cnt   sequence number of a blog.
     * @param name  property name.
     *
     * @return property value.
     */
    private boolean getBoolean(Preferences prefs, String cnt, String name)
    {
        return prefs.getBoolean(prop(cnt, name), false);
    }

    /**
     * Returns API type from the preferences map.
     *
     * @param prefs preferences map.
     * @param cnt   sequence number of a blog.
     * @param name  property name.
     *
     * @return property value.
     */
    private IWeblogAPI getApiType(Preferences prefs, String cnt, String name)
    {
        String apiTypeID = get(prefs, cnt, name);
        return WeblogAPIs.getWeblogAPIByID(apiTypeID);
    }

    /**
     * Gets a string value of a property.
     *
     * @param prefs preferences map.
     * @param cnt   sequence number of a blog.
     * @param name  property name.
     *
     * @return property value.
     */
    private String get(Preferences prefs, String cnt, String name)
    {
        return prefs.get(prop(cnt, name), null);
    }

    /**
     * Puts the string representation of an object to the preferences map if
     * the object is not <code>NULL</code>.
     *
     * @param prefs preferences map.
     * @param cnt   sequence number of a blog.
     * @param name  property name.
     * @param value property value.
     */
    private static void put(Preferences prefs, String cnt, String name, Object value)
    {
        if (value == null) return;
        prefs.put(prop(cnt, name), value.toString());
    }

    /**
     * Returns the property name for the property.
     *
     * @param cnt   blog sequence number.
     * @param prop  property name.
     *
     * @return name.
     */
    private static String prop(String cnt, String prop)
    {
        return FMT.format(new Object[] { cnt, prop });
    }


    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return title;
    }

    /**
     * Loads categories asynchronously and calls the callback from within
     * the EDT when finished.
     *
     * @param edtCallback callback.
     */
    public void loadCategories(final Runnable edtCallback)
    {
        new Thread("Loading Categories")
        {
            { setDaemon(true); }

            /** Invoked when loading starts. */
            public void run()
            {
                try
                {
                    Category[] loadedCategories = getApiType().getCategories(TargetBlog.this);
                    if (loadedCategories != null && loadedCategories.length == 0)
                    {
                        loadedCategories = new Category[] { getApiType().getDefaultCategory() };
                    }

                    setCategories(loadedCategories);
                } finally
                {
                    SwingUtilities.invokeLater(edtCallback);
                }
            }
        }.start();
    }

    public void loadBlogs(final Runnable edtCallback)
    {
        new Thread("Loading Blogs")
        {
            { setDaemon(true); }

            /** Invoked when loading starts. */
            public void run()
            {
                try
                {
                    setBlogs(getApiType().getBlogs(TargetBlog.this));
                } finally
                {
                    SwingUtilities.invokeLater(edtCallback);
                }
            }
        }.start();
    }

    /**
     * Simple holder for blog information.
     */
    public static class Blog extends IdNameHolder
    {
        /**
         * Creates holder.
         *
         * @param id   id.
         * @param name name.
         */
        public Blog(String id, String name)
        {
            super(id, name);
        }

        /**
         * Restores blog.
         *
         * @param prefs preferences.
         * @param prop  propery name.
         *
         * @return blog or <code>NULL</code>.
         */
        public static Blog restore(Preferences prefs, String prop)
        {
            IdNameHolder hld = restore0(prefs, prop);
            return hld == null ? null : new Blog(hld.id, hld.name);
        }
    }

    /**
     * Simple holder for category information.
     */
    public static class Category extends IdNameHolder
    {
        /**
         * Creates holder.
         *
         * @param id   id.
         * @param name name.
         */
        public Category(String id, String name)
        {
            super(id, name);
        }

        /**
         * Restores category.
         *
         * @param prefs preferences.
         * @param prop  propery name.
         *
         * @return category or <code>NULL</code>.
         */
        public static Category restore(Preferences prefs, String prop)
        {
            IdNameHolder hld = restore0(prefs, prop);
            return hld == null ? null : new Category(hld.id, hld.name);
        }
    }
}
