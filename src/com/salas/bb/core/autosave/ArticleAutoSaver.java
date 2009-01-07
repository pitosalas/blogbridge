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
// $Id: ArticleAutoSaver.java,v 1.3 2007/05/02 10:27:06 spyromus Exp $
//

package com.salas.bb.core.autosave;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.utils.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When a new article comes, this saver checks if the
 * saving is enabled for the parent feed and saves an article
 * if it is.
 */
class ArticleAutoSaver extends AbstractAutoSaver
{
    private static final Logger LOG = Logger.getLogger(AbstractAutoSaver.class.getName());
    private MessageFormat textTemplate;

    /**
     * Invoked when a new article is added.
     *
     * @param article new article.
     * @param feed      feed it was added (not necessarily parent, if it's search feed for ex.)
     */
    protected void onNewArticle(IArticle article, IFeed feed)
    {
        boolean asa = feed.isAutoSaveArticles();
        String asaFolder = feed.getAutoSaveArticlesFolder();

        // If asa is disabled or article folder is not set, do nothing
        if (!asa || StringUtils.isEmpty(asaFolder)) return;

        Executor ex = getExecutor();
        ex.execute(new SaveArticleTask(article, feed, asaFolder));
    }

    /**
     * Returns the name of the executor thread.
     *
     * @return thread.
     */
    protected String getExecutorThreadName()
    {
        return "Article Auto Saver";
    }

    /**
     * Article saving task.
     */
    private class SaveArticleTask implements Runnable
    {
        private final IArticle article;
        private final IFeed feed;
        private final String folder;

        /**
         * Creates a task for a give article / feed to save it to the folder.
         *
         * @param article   article.
         * @param feed      feed.
         * @param folder    folder.
         */
        public SaveArticleTask(IArticle article, IFeed feed, String folder)
        {
            this.article = article;
            this.feed = feed;
            this.folder = folder;
        }

        /**
         * Invoked when saving begins.
         */
        public void run()
        {
            // Prepare file name
            String asaNameFormat = feed.getAutoSaveArticlesNameFormat();
            File file = prepareFilename(folder, asaNameFormat, feed, article.getTitle(), ".html");
            if (file.exists()) return;

            ensureAllDirsPresent(file);

            // Prepare the text
            String text = getText(article, getOffset(asaNameFormat));

            // Write
            try
            {
                writeString(file, text);
            } catch (IOException e)
            {
                LOG.log(Level.WARNING, "Failed to auto-save article", e);
            }
        }

    }

    /**
     * Returns the offset from the root folder to the given.
     *
     * @param format format.
     *
     * @return offset.
     */
    private static String getOffset(String format)
    {
        int i = StringUtils.countMatches(format, "/");
        i += StringUtils.countMatches(format, "\\");
        return StringUtils.repeat("../", i);
    }

    /**
     * Returns the formatted text of an article.
     *
     * @param article article.
     * @param folder    the folder where
     *
     * @return text.
     */
    private String getText(IArticle article, String folder)
    {
        String articleTitle = article.getTitle();
        String articleText = article.getHtmlText();
        String articleAuthor = article.getAuthor();
        String articleLink = toString(article.getLink());

        IFeed feed = article.getFeed();
        String feedTitle, feedAuthor, feedLink;
        feedTitle = feed == null ? "Unknown" : feed.getTitle();
        feedAuthor = (feed instanceof DirectFeed) ? ((DirectFeed)feed).getAuthor() : "";
        feedLink = (feed instanceof NetworkFeed) ? toString(((NetworkFeed)feed).getXmlURL()) : "";

        String stylesheetTag = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + folder + "style.css\" />";

        return getTextTemplate().format(new Object[]
        {
            articleTitle, articleText, articleAuthor, articleLink,
            feedTitle, feedAuthor, feedLink, stylesheetTag,
            new Date()
        });
    }

    private synchronized MessageFormat getTextTemplate()
    {
        if (textTemplate == null)
        {
            String tt = ResourceUtils.readTextFromFile(ResourceUtils.getString("auto.saver.article.template"));
            tt = replace(tt, "article.title", "article.text",
                "article.author", "article.link",
                "feed.title", "feed.author", "feed.link",
                "stylesheet.tag");
            tt = replaceDate(tt, "current.date", 8);
            textTemplate = new MessageFormat(tt);
        }

        return textTemplate;
    }

    /**
     * Writes the text to the file.
     *
     * @param file  file.
     * @param text  text to write.
     *
     * @throws IOException in case of an error.
     */
    private void writeString(File file, String text)
        throws IOException
    {
        FileWriter fw = new FileWriter(file);
        fw.write(text);
        fw.flush();
        fw.close();
    }

    /**
     * Converts the link to string.
     *
     * @param link link.
     *
     * @return string.
     */
    private String toString(URL link)
    {
        return link == null ? null : link.toString();
    }
}
