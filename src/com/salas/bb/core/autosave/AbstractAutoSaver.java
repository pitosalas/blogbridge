// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: AbstractAutoSaver.java,v 1.3 2007/05/02 10:27:06 spyromus Exp $
//

package com.salas.bb.core.autosave;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.StringUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Abstract implementation of the saver.
 */
abstract class AbstractAutoSaver
{
    private ExecutorService executor;

    /**
     * Prepares the file using the given information.
     *
     * @param folder        folder to save articles.
     * @param nameFormat    article filename format.
     * @param feed          parent feed.
     * @param title         object title.
     * @param ext           extension.
     *
     * @return file.
     */
    protected File prepareFilename(String folder, String nameFormat, IFeed feed, String title, String ext)
    {
        String filename = replace(nameFormat, "title", "feed");
        filename = replaceDate(filename, "current.date", 2);
        
        // Current Date
        Date currentDate = new Date();

        // Feed Title
        String feedTitle = feed.getTitle();
        feedTitle = escape(feedTitle);
        if (StringUtils.isEmpty(feedTitle)) feedTitle = "Unnamed Feed";

        // Title
        title = escape(title);
        if (StringUtils.isEmpty(title)) title = "Article";

        return new File(folder, MessageFormat.format(filename, title, feedTitle, currentDate) + ext);
    }

    /**
     * Replaces keys with the sequence of numbers.
     *
     * @param str   string.
     * @param keys  keys.
     *
     * @return result.
     */
    protected static String replace(String str, String ... keys)
    {
        if (StringUtils.isNotEmpty(str))
        {
            int i = 0;
            for (String key : keys)
            {
                str = str.replaceAll("\\{" + key + "\\}", "{" + (i++) + "}");
            }
        }

        return str;
    }

    /**
     * Replaces date keys.
     *
     * @param str   string.
     * @param key   keys.
     * @param pos   position to start counting.
     *
     * @return result.
     */
    protected static String replaceDate(String str, String key, int pos)
    {
        return str.replaceAll("\\{" + key + "(,.+)?\\}", "{" + pos + ",date$1}");
    }

    /**
     * Escapes characters for file names.
     *
     * @param str   string.
     *
     * @return result.
     */
    private static String escape(String str)
    {
        return str == null ? null : str.replaceAll("[<>\\?\"'\\*:/\\\\]", "_");
    }

    /**
     * Invoked when a new article is added.
     *
     * @param article new article.
     * @param feed      feed it was added (not necessarily parent, if it's search feed for ex.)
     */
    protected abstract void onNewArticle(IArticle article, IFeed feed);

    /**
     * Returns common executor.
     *
     * @return executor.
     */
    protected synchronized ExecutorService getExecutor()
    {
        if (executor == null)
        {
            executor = Executors.newFixedThreadPool(getExecutorThreadsNumber(), new ThreadFactory()
            {
                /**
                 * Constructs a new <tt>Thread</tt>.  Implementations may also initialize priority, name, daemon status,
                 * <tt>ThreadGroup</tt>, etc.
                 *
                 * @param r a runnable to be executed by new thread instance
                 *
                 * @return constructed thread
                 */
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, getExecutorThreadName());
                }
            });
        }
        return executor;
    }

    /**
     * Returns the number of threads for executor. The default is <code>1</code>.
     *
     * @return the number of threads.
     */
    protected int getExecutorThreadsNumber()
    {
        return 1;
    }

    /**
     * Returns the name of the executor thread.
     *
     * @return thread.
     */
    protected abstract String getExecutorThreadName();

    /**
     * Makes sure all directories are present and creates those that are not.
     *
     * @param file file.
     */
    static void ensureAllDirsPresent(File file)
    {
        // Create all sub-directories if necessary
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) parentFile.mkdirs();
    }
}
