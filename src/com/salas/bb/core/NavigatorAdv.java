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
// $Id: NavigatorAdv.java,v 1.14 2008/03/17 12:23:06 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended to provide navigation services. It drives the process of
 * switching between channels and guides on user's demand. It finds next or previous
 * targets for switching and makes this switching using <code>GlobalController</code>
 * services.
 */
public class NavigatorAdv extends ControllerAdapter
{
    private static final Logger LOG = Logger.getLogger(NavigatorAdv.class.getName());

    private GuideModel       navigationModel;
    private GuideModel       viewModel;

    private final GuidesListModel guidesListModel;
    private GuidesSet               guidesSet;

    private IGuide                  selectedGuide;
    private IFeed                   selectedFeed;

    /**
     * Creates navigator for the model.
     *
     * @param aNavigationModel  navigation model used for view.
     * @param guidesListModel   guides list model.
     */
    public NavigatorAdv(GuideModel aNavigationModel, GuidesListModel guidesListModel)
    {
        navigationModel = aNavigationModel;
        this.guidesListModel = guidesListModel;

        viewModel = null;
    }

    /**
     * Sets the view model that will be used for navavigation through visible guide. If this
     * model isn't set then the regular model will be used.
     *
     * @param aViewModel view model.
     */
    public void setViewModel(GuideModel aViewModel)
    {
        viewModel = aViewModel;
    }

    /**
     * Sets the set of gudies to refer to when looking for next/previous guide.
     *
     * @param set guide set.
     */
    public void setGuidesSet(GuidesSet set)
    {
        guidesSet = set;
    }

    /**
     * Invoked after application changes the guide.
     *
     * @param guide guide to with we have switched.
     */
    public synchronized void guideSelected(IGuide guide)
    {
        selectedGuide = guide;
        selectedFeed = null;
    }

    /**
     * Invoked after application changes the channel.
     *
     * @param feed channel to which we are switching.
     */
    public synchronized void feedSelected(IFeed feed)
    {
        selectedFeed = feed;
    }

    /**
     * Returns destination feed for the given action key.
     *
     * @param key   key.
     *
     * @return feed or NULL if there's no destination.
     */
    public synchronized Destination getDestination(NavigationInfoKey key)
    {
        Destination dest;

        if (key instanceof NavigationInfoKey.Next)
        {
            boolean unreadOnly = key == NavigationInfoKey.NEXT_UNREAD;

            dest = recalcNext(unreadOnly);
        } else
        {
            boolean unreadOnly = key == NavigationInfoKey.PREV_UNREAD;

            dest = recalcPrev(unreadOnly);
        }

        return dest;
    }

    /**
     * Called by <code>Calculator</code> in separate thread.
     *
     * @param unreadOnly    if TRUE feeds with unread articles only will be returned.
     *                                                              
     * @return destination for "Next" command.
     */
    private synchronized Destination recalcNext(boolean unreadOnly)
    {
        IFeed destFeed = null;
        IFeed feed;
        IGuide guide;
        GuideModel model;

        feed = selectedFeed;
        guide = selectedGuide;
        if (guide == null) return null;

        // We are ready to start

        int pass = 0;
        while (destFeed == null && pass < 2)
        {
            // Get model for the guide
            if (guide != null && isVisible(guide))
            {
                model = getModel(guide);

                feed = getNextInGuide(model, feed, unreadOnly);
            }

            // If feed isn't empty then we probably found what we needed.
            if (feed != null)
            {
                destFeed = feed;
            } else
            {
                // We increment pass counter each time the guide is what we started with.
                // On the leaving the guide for the second time we will break the loop.
                if (guide == selectedGuide) pass++;

                guide = getNextGuide(guide);
            }
        }

        return destFeed == null ? null : new Destination(guide, destFeed);
    }

    /**
     * Called by <code>Calculator</code> in separate thread.
     *
     * @param unreadOnly    if TRUE feeds with unread articles only will be returned.
     *
     * @return destination for "Previous" command.
     */
    private synchronized Destination recalcPrev(boolean unreadOnly)
    {
        IFeed destFeed = null;
        IFeed feed;
        IGuide guide;
        GuideModel model;

        feed = selectedFeed;
        guide = selectedGuide;
        if (guide == null) return null;

        // We are ready to start

        int pass = 0;
        while (destFeed == null && pass < 2)
        {
            // Get model for the guide
            if (guide != null && isVisible(guide))
            {
                model = getModel(guide);

                feed = getPrevInGuide(model, feed, unreadOnly);
            }

            // If feed isn't empty then we probably found what we needed.
            if (feed != null)
            {
                destFeed = feed;
            } else
            {
                // We increment pass counter each time the guide is what we started with.
                // On the leaving the guide for the second time we will break the loop.
                if (guide == selectedGuide) pass++;

                guide = getPrevGuide(guide);
            }
        }

        return destFeed == null ? null : new Destination(guide, destFeed);
    }

    private boolean isVisible(IGuide guide)
    {
        return guidesListModel == null || guidesListModel.indexOf(guide) != -1;
    }

    /**
     * Returns mode initialized for the guide.
     *
     * @param guide guide.
     *
     * @return model.
     */
    private GuideModel getModel(IGuide guide)
    {
        GuideModel model;

        if (guide == selectedGuide && viewModel != null)
        {
            model = viewModel;
        } else
        {
            model = navigationModel;
            navigationModel.setGuide(guide);
        }

        return model;
    }

    /**
     * Looks for the feed, which is next in relation to <code>currentFeed</code> in the given
     * <code>model</code>. If <code>unreadOnly</code> or <code>keywordsOnly</code> properties set
     * then only appropriate feeds will be choosen.
     *
     * @param model         model to analyze.
     * @param currentFeed   current feed to start searching from or NULL if to start from the start.
     * @param unreadOnly    if TRUE feeds with unread articles only will be returned.
     *
     * @return next feed, <code>stopFeed</code> or NULL (if next feed not found).
     */
    IFeed getNextInGuide(GuideModel model, IFeed currentFeed, boolean unreadOnly)
    {
        int size = model.getSize();
        int index = currentFeed == null ? -1 : model.indexOf(currentFeed);

        IFeed next = null;

        if (currentFeed != null && index == -1)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("feed.does.not.belong.to.model.feed"),
                currentFeed));
        } else
        {
            for (int i = index + 1; next == null && i < size; i++)
            {
                next = (IFeed)model.getElementAt(i);

                // Make sure feed matches expectations according to the unreadOnly parameter
                if (!isFeedMatching(next, unreadOnly, getArticleFilter())) next = null;
            }
        }

        return next;
    }

    /**
     * Looks for the feed, which is previous in relation to <code>currentFeed</code> in the given
     * <code>model</code>. If <code>unreadOnly</code> or <code>keywordsOnly</code> properties set
     * then only appropriate feeds will be choosen.
     *
     * @param model         model to analyze.
     * @param currentFeed   current feed to start searching from or NULL if to start from the start.
     * @param unreadOnly    if TRUE feeds with unread articles only will be returned.
     *
     * @return previous feed, <code>stopFeed</code> or NULL (if next feed not found).
     */
    IFeed getPrevInGuide(GuideModel model, IFeed currentFeed, boolean unreadOnly)
    {
        int size = model.getSize();
        int index = currentFeed == null ? size : model.indexOf(currentFeed);

        IFeed prev = null;

        if (currentFeed != null && index == -1)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("feed.does.not.belong.to.model.feed"),
                currentFeed));
        } else
        {
            for (int i = index - 1; prev == null && i >= 0; i--)
            {
                prev = (IFeed)model.getElementAt(i);

                // Make sure feed matches expectations according to the unreadOnly parameter
                if (!isFeedMatching(prev, unreadOnly, getArticleFilter())) prev = null;
            }
        }

        return prev;
    }

    /**
     * Returns TRUE if the feed matches the selection criteria.
     *
     * @param feed          feed.
     * @param unreadOnly    unread-only flag.
     * @param filter        article filtering mode (IFeedDisplayConstants.FILTER_XYZ).
     *
     * @return TRUE if the feed matches the selection criteria.
     */
    static boolean isFeedMatching(IFeed feed, boolean unreadOnly, int filter)
    {
        boolean hasVisibleArticles = false;

        int count = feed.getArticlesCount();
        if (count > 0)
        {
            if (filter == IFeedDisplayConstants.FILTER_UNREAD)
            {
                // If showing unread, this will be sufficient
                hasVisibleArticles = feed.getUnreadArticlesCount() > 0;
            } else if (filter == IFeedDisplayConstants.FILTER_ALL)
            {
                // If showing all, this will be sufficient
                hasVisibleArticles = !unreadOnly || feed.getUnreadArticlesCount() > 0;
            } else
            {
                // For all other filters, iterate until all articles are scanned, or
                // the first visible is found. 
                for (int i = 0; !hasVisibleArticles && i < count; i++)
                {
                    IArticle article = feed.getArticleAt(i);
                    if (!unreadOnly || !article.isRead())
                    {
                        // If read state match, see others
                        switch (filter)
                        {
                            case IFeedDisplayConstants.FILTER_PINNED:
                                hasVisibleArticles = article.isPinned();
                                break;

                            case IFeedDisplayConstants.FILTER_NEGATIVE:
                                hasVisibleArticles = article.isNegative();
                                break;

                            case IFeedDisplayConstants.FILTER_POSITIVE:
                                hasVisibleArticles = article.isPositive();
                                break;

                            case IFeedDisplayConstants.FILTER_NON_NEGATIVE:
                                hasVisibleArticles = !article.isNegative();
                                break;
                        }
                    }
                }
            }
        }

        return hasVisibleArticles;
    }

    /**
     * Returns guide which is next to the current in the channel guides set.
     *
     * @param currentGuide  current guide.
     *
     * @return next guide.
     */
    IGuide getNextGuide(IGuide currentGuide)
    {
        IGuide next = currentGuide;

        if (guidesSet == null)
        {
            LOG.warning(Strings.error("guide.set.not.registered"));
        } else
        {
            next = nextGuide(currentGuide);
        }

        return next;
    }

    private IGuide nextGuide(IGuide aCurrentGuide)
    {
        int index = aCurrentGuide == null ? -1 : guidesSet.indexOf(aCurrentGuide);
        int next = index + 1;
        int size = guidesSet.getGuidesCount();

        return guidesSet.getGuideAt(next >= size ? 0 : next);
    }

    /**
     * Returns guide which is previous to the current in the channel guides set.
     *
     * @param currentGuide  current guide.
     *
     * @return previous guide.
     */
    IGuide getPrevGuide(IGuide currentGuide)
    {
        IGuide prev = currentGuide;

        if (guidesSet == null)
        {
            LOG.warning(Strings.error("guide.set.not.registered"));
        } else
        {
            prev = prevChannelGuide(currentGuide);
        }

        return prev;
    }

    private IGuide prevChannelGuide(IGuide aCurrentGuide)
    {
        int size = guidesSet.getGuidesCount();
        int index = aCurrentGuide == null ? size : guidesSet.indexOf(aCurrentGuide);
        int prev = index - 1;

        return guidesSet.getGuideAt(prev < 0 ? size - 1 : prev);
    }

    /**
     * Returns current article filter.
     *
     * @return filter.
     */
    private static int getArticleFilter()
    {
        return GlobalModel.SINGLETON.getGlobalRenderingSettings().getArticleFilter();
    }

    // ------------------------------------------------------------------------
    // Supplementary classes
    // ------------------------------------------------------------------------

    /**
     * Navigation destination.
     */
    public static class Destination
    {
        public IGuide   guide;
        public IFeed    feed;

        /**
         * Creates holder.
         *
         * @param aGuide    guide.
         * @param aFeed     feed.
         */
        public Destination(IGuide aGuide, IFeed aFeed)
        {
            guide = aGuide;
            feed = aFeed;
        }
    }

    /**
     * Keys for navigation info recalculation.
     */
    public static interface NavigationInfoKey
    {
        /**
         * Next.
         */
        NavigationInfoKey NEXT                  = new Next();

        /**
         * Next (unread only).
         */
        NavigationInfoKey NEXT_UNREAD           = new Next();

        /**
         * Previous.
         */
        NavigationInfoKey PREV                  = new Prev();

        /**
         * Previous (unread only).
         */
        NavigationInfoKey PREV_UNREAD           = new Prev();

        /**
         * Marker class for next-operations.
         */
        static final class Next implements NavigationInfoKey
        {
        }

        /**
         * Marker class for previous-operations.
         */
        static final class Prev implements  NavigationInfoKey
        {
        }
    }
}
