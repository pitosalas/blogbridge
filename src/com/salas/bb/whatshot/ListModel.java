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
// $Id: ListModel.java,v 1.15 2007/08/27 15:07:49 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.search.ResultsListModel;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.swingworker.SwingWorker;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The results list model.
 */
public class ListModel extends ResultsListModel implements ILinkResolverListener
{
    /** Minimum number of references in a group for the link to be hot. */
    private static final int MIN_REFS_IN_GROUP = 2;
    /** Maximum number of hot links to put in the model from the results. */
    private static final int MAX_HOT_LINKS = 20;

    /** Hotlink sequence number which is used to generate unique keys within the model. */
    private int hotlinkSeqNum;

    /** What's hot engine used to get the results. */
    private final Engine engine;

    /** Filter: minimum starz. '1' means no filtering. */
    private final ValueModel mdlStarz;
    /** Filter: unread only article allowed. */
    private final ValueModel mdlOnlyUnread;
    /** Filter: time options. */
    private final ValueModel mdlTimeOption;

    /** Resolves links into titles. */
    private final LinkResolver resolver;

    /** The list of all hotlinks in the model. */
    private List<Engine.HotLink> hotlinks = new ArrayList<Engine.HotLink>();

    /** The guide to look for hot links in. */
    private IGuide targetGuide;
    /** The ignore-pattern compiled from ignorePatterns or <code>NULL</code> if not set. */
    private Pattern ignorePattern;
    /** Don't count self references flag. */
    private boolean dontCountSelfReferences;
    /** Suppress single source links. */
    private boolean suppressSameSourceLinks;

    /**
     * Creates a list model for a given engine.
     *
     * @param engine            engine.
     * @param mdlStarz          starz filter model.
     * @param mdlOnlyUnread     unread only flag model.
     * @param mdlTimeOption     time options.
     */
    public ListModel(Engine engine, ValueModel mdlStarz, ValueModel mdlOnlyUnread, ValueModel mdlTimeOption)
    {
        this.engine = engine;
        this.mdlStarz = mdlStarz;
        this.mdlOnlyUnread = mdlOnlyUnread;
        this.mdlTimeOption = mdlTimeOption;

        resolver = new LinkResolver(this);
        FilterListener fl = new FilterListener();
        mdlStarz.addValueChangeListener(fl);
        mdlOnlyUnread.addValueChangeListener(fl);
        mdlTimeOption.addValueChangeListener(fl);
    }

    /**
     * Invoked when the group resolution is completed.
     *
     * @param group group resolved.
     */
    public void onGroupResolved(final HotResultGroup group)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fireGroupUpdated(group);
            }
        });
    }

    /**
     * Stops link resolution immediately.
     */
    public void stopLinkResolution()
    {
        if (resolver != null) resolver.stop();
    }

    /**
     * Start scanning and filling the model.
     *
     * @return returns the scanner to start.
     */
    public SwingWorker scan()
    {
        return new Scanner();
    }

    /**
     * Processes the links from the engine.
     *
     * @param links links to process.
     */
    void processLinks(List<Engine.HotLink> links)
    {
        for (Engine.HotLink hotLink : links)
        {
            // We don't need links with little references and
            // we allow only some given number of hotlinks
            if (hotLink.size() < MIN_REFS_IN_GROUP) break;

            hotlinks.add(hotLink);
        }

        review();
    }

    private void review()
    {
        List<HotResultItem> groupItems = new LinkedList<HotResultItem>();
        List<HotResultGroup> newGroups = new LinkedList<HotResultGroup>();
        for (Engine.HotLink link : hotlinks)
        {
            List<IArticle> matchingArticles = getMatchingArticles(link);
            if (matchingArticles == null) continue;

            String linkString = link.getLink().toString();
            groupItems.clear();

            for (IArticle article : matchingArticles)
            {
                groupItems.add(new HotResultItem(article, linkString));
            }

            if (groupItems.size() >= MIN_REFS_IN_GROUP)
            {
                HotResultGroup group = new HotResultGroup(hotlinkSeqNum++, link);
                for (HotResultItem item : groupItems) group.add(item);
                newGroups.add(group);
            }
        }

        // Sort groups by the number of items in them
        Collections.sort(newGroups);

        fireClear();
        int i = 0;
        for (HotResultGroup group : newGroups)
        {
            group.setVisible(true);

            // Start resolving the group title
            if (!group.isResolved())
            {
                String title = resolver.resolve(group);
                if (title != null) group.setResolvedTitle(title);
            }

            fireGroupAdded(group, true);

            // Fire all items
            for (HotResultItem item : group) fireItemAdded(item, group);

            i++;
            if (i > MAX_HOT_LINKS) break;
        }
    }

    private List<IArticle> getMatchingArticles(Engine.HotLink link)
    {
        List<IArticle> matches;

        // Check against ignore pattern
        if (ignorePattern != null &&
                ignorePattern.matcher(link.getLink().toString()).find()) return null;

        // Check if has links from one source only
        IFeed feed = null;
        matches = new LinkedList<IArticle>();
        boolean fine = !suppressSameSourceLinks;
        for (IArticle article : link)
        {
            if (!matches(link, article)) continue;
            matches.add(article);

            if (!fine)
            {
                if (feed == null) feed = article.getFeed(); else
                if (feed != article.getFeed()) fine = true;
            }
        }

        return !fine ? null : matches;
    }

    private boolean matches(Engine.HotLink link, IArticle article)
    {
        // Check the rating
        if ((Boolean)mdlOnlyUnread.getValue() && article.isRead()) return false;

        // Check the time
        TimeOption to = (TimeOption)mdlTimeOption.getValue();
        if (article.getPublicationDate().getTime() < System.currentTimeMillis() - to.getOffset()) return false;

        // Check the starz
        Integer starz = (Integer)mdlStarz.getValue();
        IFeed feed = article.getFeed();
        if (starz > 1 && feed.getRating() < starz) return false;

        // Check self-referencing
        if (dontCountSelfReferences)
        {
            String linkHost = link.getLink().getHost();
            URL articleLink = article.getLink();
            if (articleLink != null && articleLink.getHost().equalsIgnoreCase(linkHost)) return false;
        }

        // Check guides
        return targetGuide == null || targetGuide.getID() == -1 || feed.belongsTo(targetGuide);
    }

    /**
     * Sets advanced filtering setup.
     *
     * @param ignorePatterns            ignore patterns in textual form (one per line).
     * @param dontCountSelfReferences   <code>TRUE</code> to never count links to self.
     * @param suppressSameSourceLinks   <code>TRUE</code> to suppress hot links referenced only from one source.
     * @param targetGuide               target guide to look for hot articles in.
     */
    public void setSetup(String ignorePatterns, boolean dontCountSelfReferences,
                         boolean suppressSameSourceLinks, IGuide targetGuide)
    {
        ignorePattern = StringUtils.isEmpty(ignorePatterns)
            ? null
            : Pattern.compile(StringUtils.keywordsToPattern(ignorePatterns));

        this.dontCountSelfReferences = dontCountSelfReferences;
        this.suppressSameSourceLinks = suppressSameSourceLinks;
        this.targetGuide = targetGuide;

        review();
    }

    /**
     * Scanner worker.
     */
    private class Scanner extends SwingWorker<List<Engine.HotLink>, Integer>
        implements IProgressListener
    {
        /**
         * Invoked when background processing should start.
         *
         * @return the result.
         *
         * @throws Exception in case of any error.
         */
        protected List<Engine.HotLink> doInBackground() throws Exception
        {
            return engine.scan(this);
        }

        @Override
        protected void done()
        {
            List<Engine.HotLink> links;
            try
            {
                links = get();
            } catch (Exception e)
            {
                return;
            }

            processLinks(links);
        }

        /**
         * Invoked on progress change.
         *
         * @param percent percents [0 - 100];
         */
        public void onProgress(int percent)
        {
            setProgress(percent);
        }
    }

    private class FilterListener implements PropertyChangeListener
    {
        /**
         * Invoked when a filter property changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            review();
        }
    }
}
