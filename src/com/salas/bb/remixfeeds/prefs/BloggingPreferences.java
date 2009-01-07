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
// $Id: BloggingPreferences.java,v 1.11 2008/03/31 16:13:00 spyromus Exp $
//

package com.salas.bb.remixfeeds.prefs;

import com.salas.bb.remixfeeds.templates.Templates;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

/**
 * Blogging preferences.
 */
public class BloggingPreferences
{
    private static final String KEY_RICH_EDITOR = "blog.richEditor";
    public static final String PROP_RICH_EDITOR = "richEditor";

    private List<TargetBlog> blogs;
    private TargetBlog defaultTargetBlog;
    private boolean richEditor;

    /**
     * Creates blogging preferences.
     */
    public BloggingPreferences()
    {
        blogs = new CopyOnWriteArrayList<TargetBlog>();
        defaultTargetBlog = null;
        richEditor = true;
    }

    /**
     * Returns the list of blogs.
     *
     * @return blogs.
     */
    public List<TargetBlog> getBlogs()
    {
        return blogs;
    }

    /**
     * Returns the number of blogs registered.
     *
     * @return count.
     */
    public int getBlogsCount()
    {
        return blogs.size();
    }

    /**
     * Returns the blog at a given index.
     *
     * @param i index.
     *
     * @return blog.
     */
    public TargetBlog getBlog(int i)
    {
        return blogs.get(i);
    }

    /**
     * Adds new targetBlog to the list.
     *
     * @param targetBlog targetBlog.
     */
    public void addBlog(TargetBlog targetBlog)
    {
        blogs.add(targetBlog);
    }

    /**
     * Removes a targetBlog from a list.
     *
     * @param targetBlog targetBlog.
     */
    public void removeBlog(TargetBlog targetBlog)
    {
        boolean removedDefault = targetBlog == defaultTargetBlog;
        blogs.remove(targetBlog);

        if (removedDefault && getBlogsCount() > 0) setDefaultBlog(getBlog(0));
    }

    /**
     * Sets the default targetBlog.
     *
     * @param targetBlog targetBlog.
     */
    public void setDefaultBlog(TargetBlog targetBlog)
    {
        if (blogs.contains(targetBlog)) defaultTargetBlog = targetBlog;
    }

    /**
     * Gets the default blog.
     *
     * @return blog.
     */
    public TargetBlog getDefaultBlog()
    {
        return defaultTargetBlog == null && getBlogsCount() > 0 ? getBlog(0) : defaultTargetBlog;
    }

    /**
     * Returns <code>TRUE</code> if rich editor is selected.
     *
     * @return rich editor.
     */
    public boolean isRichEditor()
    {
        return richEditor;
    }

    /**
     * Sets the rich editor selection flag.
     *
     * @param richEditor <code>TRUE</code> to select the editor.
     */
    public void setRichEditor(boolean richEditor)
    {
        this.richEditor = richEditor;
    }

    /**
     * Persists the information about blogs in the preferences map.
     *
     * @param prefs map.
     */
    public void store(Preferences prefs)
    {
        // Store blogs count
        int count = getBlogsCount();
        prefs.put("blog.count", Integer.toString(count));

        // Store blogs
        int i = 0;
        for (TargetBlog blog : blogs) blog.store(i++, prefs);

        // Store the default
        if (defaultTargetBlog != null)
        {
            prefs.put("blog.default", Integer.toString(blogs.indexOf(defaultTargetBlog)));
        }

        prefs.putBoolean(KEY_RICH_EDITOR, richEditor);

        Templates.store(prefs);
    }

    /**
     * Restores the information about blogs from the preferences map.
     *
     * @param prefs map.
     */
    public void restore(Preferences prefs)
    {
        Templates.restore(prefs);

        blogs.clear();
        defaultTargetBlog = null;

        String countS = prefs.get("blog.count", null);
        if (countS != null)
        {
            int count = Integer.parseInt(countS);

            // Restores blogs
            for (int i = 0; i < count; i++)
            {
                TargetBlog targetBlog = new TargetBlog();
                targetBlog.restore(i, prefs);
                blogs.add(targetBlog);
            }

            // Restores the default
            String defaultS = prefs.get("blog.default", null);
            if (defaultS != null)
            {
                int defaultB = Integer.parseInt(defaultS);
                if (defaultB >= 0 && defaultB < getBlogsCount()) setDefaultBlog(getBlog(defaultB));
            }
        }

        setRichEditor(prefs.getBoolean(KEY_RICH_EDITOR, true));
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     */
    protected BloggingPreferences createClone()
    {
        BloggingPreferences prefs = new BloggingPreferences();
        copy(this, prefs, true);
        return prefs;
    }

    /**
     * Copies data from the other preferences object.
     *
     * @param prefs preferences.
     */
    public void copyFrom(BloggingPreferences prefs)
    {
        copy(prefs, this, false);
    }

    /**
     * Copies data from <code>src</code> to <code>dst</code>.
     *
     * @param src   source preferences.
     * @param dst   destination preferences.
     * @param clone <code>TRUE</code> top clone blog preferences, not just copy refs.
     */
    private void copy(BloggingPreferences src, BloggingPreferences dst, boolean clone)
    {
        TargetBlog oldDefaultBlog = src.getDefaultBlog();
        TargetBlog newDefaultBlog = null;

        List blogs = src.getBlogs();
        dst.getBlogs().clear();
        for (Object blog : blogs)
        {
            TargetBlog srcBlog = (TargetBlog)blog;
            TargetBlog dstBlog = clone ? srcBlog.createClone() : srcBlog;

            dst.addBlog(dstBlog);

            if (oldDefaultBlog == srcBlog) newDefaultBlog = dstBlog;
        }

        dst.setDefaultBlog(newDefaultBlog);
        dst.setRichEditor(src.isRichEditor());
    }
}
