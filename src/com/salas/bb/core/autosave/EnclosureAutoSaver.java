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
// $Id: EnclosureAutoSaver.java,v 1.1 2007/05/02 10:27:06 spyromus Exp $
//

package com.salas.bb.core.autosave;

import com.jgoodies.uif.application.Application;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.Downloader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatic enclosures saver.
 */
class EnclosureAutoSaver extends AbstractAutoSaver
{
    private static final int EXECUTOR_THREADS_NUMBER = 5;
    private static Pattern patEnclosures;

    /**
     * Invoked when a new article is added.
     *
     * @param article new article.
     * @param feed    feed it was added (not necessarily parent, if it's search feed for ex.)
     */
    protected void onNewArticle(IArticle article, IFeed feed)
    {
        boolean ase = feed.isAutoSaveEnclosures();
        String aseFolder = feed.getAutoSaveEnclosuresFolder();

        // If asa is disabled or article folder is not set, do nothing
        if (!ase || StringUtils.isEmpty(aseFolder)) return;

        downloadEnclosures(article, feed);
    }

    /**
     * Gets the enclosures in the article.
     *
     * @param html  article HTML text.
     *
     * @return enclosure URL's.
     *
     * @see com.salas.bb.utils.parser.RomeFeedParser#formatEnclosure(String, long)
     */
    static String[] getEnclosures(String html)
    {
        String[] encs;

        if (StringUtils.isEmpty(html))
        {
            encs = new String[0];
        } else
        {
            Pattern p = getEnclosurePattern();
            Matcher m = p.matcher(html);

            List<String> encsL = new ArrayList<String>();
            while (m.find()) encsL.add(m.group(2));

            encs = encsL.toArray(new String[encsL.size()]);
        }

        return encs;
    }

    /**
     * Returns the enclosure matching pattern.
     *
     * @return pattern.
     */
    private static synchronized Pattern getEnclosurePattern()
    {
        if (patEnclosures == null)
        {
            patEnclosures = Pattern.compile("<p id=\"bbenclosure\">([^<]|<[^a])+<a href='([^']+)'");
        }

        return patEnclosures;
    }

    /**
     * Schedules downloading of all enclosures from the article.
     *
     * @param article       an article.
     * @param feed          feed.
     */
    private void downloadEnclosures(IArticle article, IFeed feed)
    {
        URL context = article.getLink();
        String[] enclosures = getEnclosures(article.getHtmlText());

        for (String enclosure : enclosures)
        {
            EnclosureParts parts = enclosureToParts(enclosure);

            try
            {
                URL link = new URL(context, enclosure);
                File target = prepareFilename(feed.getAutoSaveEnclosuresFolder(),
                    feed.getAutoSaveEnclosuresNameFormat(),
                    feed, parts.name, parts.extension);

                getExecutor().execute(new DownloadTask(link, target));
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts the enclosure URL into the title and extension.
     *
     * @param enclosure enclosure.
     *
     * @return parts.
     */
    static EnclosureParts enclosureToParts(String enclosure)
    {
        EnclosureParts parts = new EnclosureParts("", "");
        if (!StringUtils.isEmpty(enclosure))
        {
            int i1 = enclosure.lastIndexOf('\\');
            int i2 = enclosure.lastIndexOf('/');
            int i = Math.max(i1, i2);
            parts.name = i == -1 ? enclosure : enclosure.substring(i + 1);

            i = parts.name.lastIndexOf('.');
            if (i != -1)
            {
                parts.extension = parts.name.substring(i).trim();
                parts.name = parts.name.substring(0, i);
            }

            parts.name = parts.name.trim();
        }

        return parts;
    }

    /** Enclosure parts. */
    static class EnclosureParts
    {
        public String name;
        public String extension;

        /**
         * Creates enclosure parts object.
         *
         * @param name      name.
         * @param extension extension.
         */
        public EnclosureParts(String name, String extension)
        {
            this.name = name;
            this.extension = extension;
        }
    }

    /**
     * Downloads task scheduled.
     */
    private class DownloadTask implements Runnable
    {
        private final URL source;
        private final File target;

        /**
         * Creates a download task.
         *
         * @param source    source URL.
         * @param target    target file.
         */
        public DownloadTask(URL source, File target)
        {
            this.source = source;
            this.target = target;
        }

        /**
         * Invoked when downloading starts.
         */
        public void run()
        {
            Downloader d = new Downloader(null);

            ensureAllDirsPresent(target);
            
            File dir = target.getParentFile();
            final String filename = target.getName();

            try
            {
                d.download(source, dir, filename);
            } catch (InterruptedException e)
            {
                // fall through
            } catch (IOException e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        JOptionPane.showMessageDialog(Application.getDefaultParentFrame(),
                                MessageFormat.format(Strings.message("net.download.failed"), filename),
                                "Automatic Enclosure Saving",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }

    /**
     * Returns the name of the executor thread.
     *
     * @return thread.
     */
    protected String getExecutorThreadName()
    {
        return "Enclosure Auto Saver";
    }

    @Override
    protected int getExecutorThreadsNumber()
    {
        return EXECUTOR_THREADS_NUMBER;
    }
}
