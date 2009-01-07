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
// $Id: TargetBlogProxy.java,v 1.3 2008/03/31 14:48:14 spyromus Exp $
//

package com.salas.bb.remixfeeds.prefs;

import com.salas.bb.remixfeeds.api.IWeblogAPI;
import com.salas.bb.remixfeeds.templates.Templates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for blog preferences object. Lets others watch it while the underlying
 * object changes.
 */
public class TargetBlogProxy extends TargetBlog implements PropertyChangeListener
{
    public static final String PROP_BLOG_PREFERENCES = "blogPreferences";

    private TargetBlog bp;

    /**
     * Returns the blog preferences.
     *
     * @return preferences.
     */
    public TargetBlog getBlogPreferences()
    {
        return bp;
    }

    /**
     * Creates proxy around given object.
     *
     * @param bp proxy.
     */
    public TargetBlogProxy(TargetBlog bp)
    {
        setBlogPreferences(bp);
    }

    /**
     * Registers new blog preferences object.
     *
     * @param bp new preferences.
     */
    public void setBlogPreferences(TargetBlog bp)
    {
        Field[] fields = getFields();
        Object[] oldPropertyVal = getFieldValues(fields);

        TargetBlog old = this.bp;
        if (this.bp != null) this.bp.removePropertyChangeListener(this);
        this.bp = bp;
        if (bp != null) bp.addPropertyChangeListener(this);

        Object[] newPropertyVal = getFieldValues(fields);

        firePropertyChange(PROP_BLOG_PREFERENCES, old, bp);

        // Fire changes
        for (int i = 0; i < fields.length; i++)
        {
            String property = fields[i].getName();
            firePropertyChange(property, oldPropertyVal[i], newPropertyVal[i]);
        }
    }

    /**
     * Returns <code>TRUE</code> when the proxy is loaded.
     *
     * @return <code>TRUE</code> when the proxy is loaded.
     */
    public boolean isLoaded()
    {
        return bp != null;
    }

    /**
     * Invoked when some property of the underlying blog preferences object changes.
     *
     * @param evt event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        // Refire the change event
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    // ----------------------------------------------------------------------------------
    // Delegated properties
    // ----------------------------------------------------------------------------------

    /**
     * Returns blog title.
     *
     * @return title.
     */
    public String getTitle()
    {
        return bp == null ? null : bp.getTitle();
    }

    /**
     * Sets blog title.
     *
     * @param title title.
     */
    public void setTitle(String title)
    {
        if (bp != null) bp.setTitle(title);
    }

    /**
     * Returns XMLRPC API url.
     *
     * @return API URL.
     */
    public String getApiURL()
    {
        return bp == null ? null : bp.getApiURL();
    }

    /**
     * Sets XMLRPC API URL.
     *
     * @param apiURL API URL.
     */
    public void setApiURL(String apiURL)
    {
        if (bp != null) bp.setApiURL(apiURL);
    }

    /**
     * Returns XMLRPC API type.
     *
     * @return API type.
     */
    public IWeblogAPI getApiType()
    {
        return bp == null ? null : bp.getApiType();
    }

    /**
     * Sets XMLRPC API type.
     *
     * @param apiType API type.
     */
    public void setApiType(IWeblogAPI apiType)
    {
        if (bp != null) bp.setApiType(apiType);
    }

    /**
     * Returns user name of the blog.
     *
     * @return user name.
     */
    public String getUser()
    {
        return bp == null ? null : bp.getUser();
    }

    /**
     * Sets user name of the blog.
     *
     * @param user user name.
     */
    public void setUser(String user)
    {
        if (bp != null) bp.setUser(user);
    }

    /**
     * Returns user password to access XML API.
     *
     * @return user password.
     */
    public String getPassword()
    {
        return bp == null ? null : bp.getPassword();
    }

    /**
     * Sets user password to access XML API.
     *
     * @param password password.
     */
    public void setPassword(String password)
    {
        if (bp != null) bp.setPassword(password);
    }

    /**
     * Returns default category to post to.
     *
     * @return default category.
     */
    public Category getDefaultCategory()
    {
        return bp == null ? null : bp.getDefaultCategory();
    }

    /**
     * Sets default category to post to.
     *
     * @param defaultCategory default category.
     */
    public void setDefaultCategory(Category defaultCategory)
    {
        if (bp != null) bp.setDefaultCategory(defaultCategory);
    }

    /**
     * Returns <code>TRUE</code> if posts should be sent as drafts by default.
     *
     * @return <code>TRUE</code> if posts should be sent as drafts by default.
     */
    public boolean isDraft()
    {
        return bp != null && bp.isDraft();
    }

    /**
     * Sets the draft by default flag.
     *
     * @param draft <code>TRUE</code> if posts should be sent as drafts by default.
     */
    public void setDraft(boolean draft)
    {
        if (bp != null) bp.setDraft(draft);
    }

    /**
     * Returns the mode.
     *
     * @return mode.
     */
    public int getMode()
    {
        return bp == null ? MODE_FULL_TEXT : bp.getMode();
    }

    /**
     * Sets the new mode.
     *
     * @param mode mode.
     */
    public void setMode(int mode)
    {
        if (bp != null) bp.setMode(mode);
    }

    @Override
    public String getTemplateName()
    {
        return bp == null ? Templates.FULL.getName() : bp.getTemplateName();
    }

    @Override
    public void setTemplateName(String name)
    {
        if (bp != null) bp.setTemplateName(name);
    }

    /**
     * Tests the connection to the blog using this configuration.
     *
     * @return <code>NULL</code> if fine or error message.
     */
    public String testConnection()
    {
        return bp != null ? bp.testConnection() : null;
    }

    /**
     * Returns the list of categories.
     *
     * @return categories.
     */
    public Category[] getCategories()
    {
        return bp != null ? bp.getCategories() : super.getCategories();
    }

    /**
     * Updates categories with new list.
     *
     * @param newList new list to be copied to the categories lookup.
     */
    public void setCategories(Category[] newList)
    {
        if (bp != null) bp.setCategories(newList);
    }

    /**
     * Returns currently selected blog.
     *
     * @return blog.
     */
    public Blog getBlog()
    {
        return bp != null ? bp.getBlog() : super.getBlog();
    }

    /**
     * Sets new selected blog.
     *
     * @param blog blog.
     */
    public void setBlog(Blog blog)
    {
        if (bp != null) bp.setBlog(blog);
    }

    /**
     * Returns the list of blogs.
     *
     * @return blogs.
     */
    public Blog[] getBlogs()
    {
        return bp == null ? super.getBlogs() : bp.getBlogs();
    }

    /**
     * Sets the map of blogs.
     *
     * @param newList new map.
     */
    public void setBlogs(Blog[] newList)
    {
        if (bp != null) bp.setBlogs(newList);
    }

    // ----------------------------------------------------------------------------------
    // Reflection
    // ----------------------------------------------------------------------------------

    private static Field[] fields;

    /**
     * Returns the list of all property fields.
     *
     * @return property fields.
     */
    private static Field[] getFields()
    {
        if (fields == null)
        {
            Field[] flds = TargetBlog.class.getDeclaredFields();
            List fieldsL = new ArrayList(flds.length);

            for (int i = 0; i < flds.length; i++)
            {
                Field fld = flds[i];
                if (fld.getModifiers() == Modifier.PROTECTED) fieldsL.add(fld);
            }

            fields = (Field[])fieldsL.toArray(new Field[fieldsL.size()]);
        }

        return fields;
    }

    /**
     * Returns the values of all property fields.
     *
     * @param fld fields.
     *
     * @return values.
     */
    private Object[] getFieldValues(Field[] fld)
    {
        Object[] vals = new Object[fld.length];
        for (int i = 0; i < fld.length; i++)
        {
            try
            {
                Field field = fld[i];
                vals[i] = bp == null ? null : field.get(bp);
            } catch (IllegalAccessException e)
            {
                vals[i] = null;
            }
        }

        return vals;
    }
}
