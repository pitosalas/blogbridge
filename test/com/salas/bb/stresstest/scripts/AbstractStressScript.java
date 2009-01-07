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
// $Id: AbstractStressScript.java,v 1.19 2006/01/27 15:23:24 spyromus Exp $
//

package com.salas.bb.stresstest.scripts;

import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.core.GuideModel;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.*;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.INavigationModes;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.mainframe.FeedsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

/**
 * Abstract implementation of <code>IStressScript</code> with some cool tools.
 */
public abstract class AbstractStressScript implements IStressScript
{
    private static final Pattern PAT_RANDOM         = Pattern.compile("\\$\\{rnd\\}");
    private static final Pattern PAT_ITEMS          = Pattern.compile("\\$\\{items\\}");

    /**
     * Pre-run initialization.
     */
    public final void init()
    {
        childInit();

        try
        {
            Thread.sleep(3000);
        } catch (InterruptedException e)
        {
        }
    }

    /**
     * Pre-run initialization.
     */
    protected abstract void childInit();

    // ---------------------------------------------------------------------------------------------
    // System Properties Tools
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns integer system property property.
     *
     * @param key   key.
     * @param def   default value.
     *
     * @return value.
     */
    protected static int getIntSystemProperty(String key, int def)
    {
        int value = def;

        String valueStr = System.getProperty(key);
        try
        {
            if (valueStr != null) value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e)
        {
        }

        return value;
    }

    /**
     * Returns the value of system property.
     *
     * @param key   key of the property.
     * @param def   default value.
     *
     * @return value.
     */
    protected String getSystemProperty(String key, String def)
    {
        String value = def;

        String val = System.getProperty(key);
        if (val != null) value = val;

        return value;
    }

    // ---------------------------------------------------------------------------------------------
    // Fast Access To Objects
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns user preferences.
     *
     * @return user preferences.
     */
    protected UserPreferences getUserPreferences()
    {
        return getModel().getUserPreferences();
    }

    /**
     * Returns active guide set.
     *
     * @return active guide set.
     */
    protected GuidesSet getGuidesSet()
    {
        return getModel().getGuidesSet();
    }

    /**
     * Returns active channel guide model.
     *
     * @return active channel guide model.
     */
    protected GuideModel getGuideModel()
    {
        return getModel().getGuideModel();
    }

    /**
     * Returns current model.
     *
     * @return current model.
     */
    protected GlobalModel getModel()
    {
        return getController().getModel();
    }

    /**
     * Returns current article list.
     *
     * @return current article list.
     */
    protected IFeedDisplay getArticleList()
    {
        return null; //getController().getMainFrame().getArticlesListPanel().getArticleList();
    }

    /**
     * Returns current channel list.
     *
     * @return current channel list.
     */
    protected JList getChannelList()
    {
        return getChannelListPanel().getFeedsList();
    }

    /**
     * Returns channel list panel.
     *
     * @return channel list panel.
     */
    protected FeedsPanel getChannelListPanel()
    {
        return getController().getMainFrame().getFeedsPanel();
    }

    /**
     * Returns current controller.
     *
     * @return current controller.
     */
    protected GlobalController getController()
    {
        return GlobalController.SINGLETON;
    }

    // ---------------------------------------------------------------------------------------------
    // Common Actions
    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the parameters of cleanup.
     *
     * @param period        period in minutes.
     * @param maxArticles   maximum articles in feed to leave.
     */
    protected void setCleanupParameters(int period, int maxArticles)
    {
        UserPreferences prefs = getUserPreferences();
        prefs.setAutoPurgeIntervalMinutes(period);
        prefs.setPurgeCount(maxArticles);
    }

    /**
     * Removes all guides.
     */
    protected void removeGuides()
    {
        GuidesSet set = getGuidesSet();
        set.clear();
    }

    /**
     * Removes guide from current guides set.
     *
     * @param aGuide    guide.
     */
    protected void removeGuide(final IGuide aGuide)
    {
        if (UifUtilities.isEDT())
        {
            removeGuide0(aGuide);
        } else
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        removeGuide0(aGuide);
                    }
                });
            } catch (Exception e)
            {
                throw new StressScriptException("Couldn't remove guide.", e);
            }
        }
    }

    /**
     * Removes guide from current guides set.
     *
     * @param aGuide    guide.
     *
     * @edt
     */
    private void removeGuide0(IGuide aGuide)
    {
        GuidesSet set = getGuidesSet();
        set.remove(aGuide);
    }

    /**
     * Creates given number of guides.
     *
     * @param guides number of guides to create.
     */
    protected void createGuides(final int guides)
    {
        for (int i = 0; i < guides; i++)
        {
            createGuide(Integer.toString(i));
        }
    }

    /**
     * Creates guide with a given title.
     *
     * @param title title of the guide.
     */
    protected void createGuide(final String title)
    {
        if (UifUtilities.isEDT())
        {
            createGuide0(title);
        } else
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        createGuide0(title);
                    }
                });
            } catch (Exception e)
            {
                throw new StressScriptException("Couldn't create guide.", e);
            }
        }
    }

    /**
     * Creates single guide.
     *
     * @param title title of the guide.
     *
     * @edt
     */
    private void createGuide0(String title)
    {
        getController().createStandardGuide(title, null, false);
    }

    /**
     * Populates each guide from the guides set with <code>feedPerGuide</code> number of feeds.
     * Feeds are initialized from <code>urlTemplate</code> URL. In the URL <code>{rnd}</code>
     * is replaced with current feed sequential number, which is unique to current population
     * session. The <code>{items}</code> is replaced with <code>articlesPerFeed</code> value.
     * If <code>waitForInit</code> is set then the engine will wait for feeds initialization
     * (when articles are loaded) once per guide, meaning that it will populate the guide with
     * feeds and then wait for their complete initialization.
     *
     * @param feedsPerGuide     number of feeds to create per guide.
     * @param articlesPerFeed   number of articles per feed to generate.
     * @param urlTemplate       template of feed URL.
     * @param waitForInit       TRUE to wait for initialization of all feeds in the guide.
     */
    protected void populateGuides(int feedsPerGuide, int articlesPerFeed, String urlTemplate,
        boolean waitForInit)
    {
        int feedNumber = 0;

        GuidesSet set = getModel().getGuidesSet();
//        synchronized (set)
//        {
            int count = set.getGuidesCount();
            for (int i = 0; i < count; i++)
            {
                populateGuide(set.getGuideAt(i), feedNumber, feedsPerGuide, articlesPerFeed,
                    urlTemplate, waitForInit);
                feedNumber += feedsPerGuide;
            }
//        }
    }

    /**
     * Populates the given guide with <code>feedsPerGuide</code> number of feeds. For detailed
     * description of options see {@link AbstractStressScript#populateGuides}.
     *
     * @param guide             guide to populate with feeds.
     * @param feedNumber        first feed sequential number.
     * @param feedsPerGuide     number of feeds to create in the guide.
     * @param articlesPerFeed   number of articles per feed to generate.
     * @param urlTemplate       template of feed URL.
     * @param waitForInit       TRUE to wait for initialization of all feeds in the guide.
     */
    protected void populateGuide(IGuide guide, int feedNumber, int feedsPerGuide,
        int articlesPerFeed, String urlTemplate, boolean waitForInit)
    {
        getController().selectGuideAndFeed(guide);
        waitForPendingEvents();

        DirectFeed[] feeds = new DirectFeed[feedsPerGuide];
        for (int i = 0; i < feedsPerGuide; i++)
        {
            String feedUrl = expandTemplate(urlTemplate, feedNumber + i, articlesPerFeed);
            feeds[i] = getController().createDirectFeed(feedUrl, false);
        }

        if (waitForInit)
        {
            for (int i = 0; i < feeds.length; i++)
            {
                DirectFeed feed = feeds[i];
                if (!waitForInitialization(feed))
                {
                    throw new StressScriptException("Failed to initialize feed " +
                        "with URL: " + feed.getXmlURL());
                }
            }
        }
    }

    /**
     * Waits for feed initialization; checks every second.
     *
     * @param feed  feed to wait for.
     *
     * @return TRUE if not invalid.
     */
    protected boolean waitForInitialization(DataFeed feed)
    {
        while (!feed.isInitialized() && !feed.isInvalid())
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
            }
        }

        return !feed.isInvalid();
    }

    /**
     * Expands URL template by replacing <code>{rnd}</code> and <code>{items}</code> markers with
     * <code>aRnd</code> and <code>aItems</code>.
     *
     * @param urlTemplate       template.
     * @param aRnd              <code>{rnd}</code> replacement.
     * @param aItems            <code>{items}</code> replacement.
     *
     * @return prepared URL.
     */
    private String expandTemplate(String urlTemplate, int aRnd, int aItems)
    {
        String text = urlTemplate;

        text = PAT_RANDOM.matcher(text).replaceAll(Integer.toString(aRnd));
        text = PAT_ITEMS.matcher(text).replaceAll(Integer.toString(aItems));

        return text;
    }

    /**
     * Marks all guides as (un)read.
     *
     * @param read TRUE if read.
     */
    protected void markAllGuidesRead(boolean read)
    {
        getGuidesSet().setRead(read);

        waitForPendingEvents();
    }

    /**
     * Waits until all events in EDT queue scheduled by this moment are dispatched.
     */
    protected void waitForPendingEvents()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    // Simply do noting
                }
            });
        } catch (Exception e)
        {
            throw new StressScriptException("Couldn't finish waiting for pending events " +
                "dispatching.", e);
        }
    }

    /**
     * Merges two guides.
     *
     * @param aGuide1   first guide (will be removed).
     * @param aGuide2   second guide.
     */
    protected void mergeGuides(final StandardGuide aGuide1, final StandardGuide aGuide2)
    {
        if (UifUtilities.isEDT())
        {
            getController().mergeGuides(new IGuide[] { aGuide1 }, aGuide2);
        } else
        {
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        getController().mergeGuides(new IGuide[] { aGuide1 }, aGuide2);
                    }
                });
            } catch (Exception e)
            {
                throw new StressScriptException("Couldn't merge two guides: " + aGuide1 +
                    " and " + aGuide2, e);
            }
        }
    }

    protected void postKeyPressed(Component source, int key, int modifiers)
    {
        if (source == null) return;
        
        long when = System.currentTimeMillis();

        KeyEvent keyEvent = new KeyEvent(source, KeyEvent.KEY_PRESSED, when, modifiers, key,
            KeyEvent.CHAR_UNDEFINED);

        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(keyEvent);
    }

    /** Selects first guide, feed, unread article. */
    protected void selectFirstGuideFeedArticle()
    {
        selectFirstGuideFeedArticle(INavigationModes.MODE_UNREAD);
    }

    protected void selectFirstGuideFeedArticle(final int mode)
    {
        GuidesSet set = getGuidesSet();
        final IGuide guide = set.getGuideAt(0);

        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    getController().selectGuideAndFeed(guide);

                    IFeed firstFeed = (IFeed)getGuideModel().getElementAt(0);
                    getController().selectFeed(firstFeed);

                    getArticleList().selectFirstArticle(mode);
                }
            });
        } catch (Exception e)
        {
            throw new StressScriptException("Couldn't select first feed.", e);
        }
    }

    /** Selects last guide, feed, unread article. */
    protected void selectLastGuideFeedArticle()
    {
        selectLastGuideFeedArticle(INavigationModes.MODE_UNREAD);
    }

    protected void selectLastGuideFeedArticle(final int mode)
    {
        GuidesSet set = getGuidesSet();
        final IGuide guide = set.getGuideAt(set.getGuidesCount() - 1);

        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    getController().selectGuideAndFeed(guide);

                    IFeed lastFeed =
                        (IFeed)getGuideModel().getElementAt(getGuideModel().getSize() - 1);

                    getController().selectFeed(lastFeed);

                    getArticleList().selectLastArticle(mode);
                }
            });
        } catch (Exception e)
        {
            throw new StressScriptException("Could not select last feed.", e);
        }
    }

    protected void checkIfAllGuidesRead()
    {
        GuidesSet set = getGuidesSet();
        int aGuideCount = set.getGuidesCount();
        for (int g = 0; g < aGuideCount; g++)
        {
            IGuide guide = set.getGuideAt(g);
            if (!guide.isRead()) throw new StressScriptException(
                "Guide " + guide + " still has unread articles.");
        }
    }

    protected void simulateForwardReadingOfAllArticles()
    {
        selectFirstGuideFeedArticle();

        JComponent panel = null; //getArticleList().getSelectedPanel();
        if (panel != null)
        {
            panel.requestFocusInWindow();
            waitForPendingEvents();
        }

        GuidesSet cgs = getGuidesSet();
        int guideCount = cgs.getGuidesCount();
        for (int g = 0; g < guideCount; g++)
        {
            IGuide guide = getModel().getSelectedGuide();
            if (guide == null) return;
            int feedCount = guide.getFeedsCount();
            for (int f = 0; f < feedCount; f++)
            {
                IFeed feed = getModel().getSelectedFeed();
                if (feed == null) return;
                while (!feed.isRead())
                {
                    postKeyPressed(getArticleList().getComponent(), KeyEvent.VK_SPACE, 0);
                    waitForPendingEvents();
                    sleep(100);
                }
            }
        }
    }

    protected void simulateBackwardReadingOfAllArticles()
    {
        selectLastGuideFeedArticle();

        JComponent panel = null; //getArticleList().getSelectedPanel();
        if (panel != null)
        {
            panel.requestFocusInWindow();
            waitForPendingEvents();
        }

        GuidesSet cgs = getGuidesSet();
        int guideCount = cgs.getGuidesCount();
        for (int g = 0; g < guideCount; g++)
        {
            IGuide guide = getModel().getSelectedGuide();
            if (guide == null) return;
            int feedCount = guide.getFeedsCount();
            for (int f = 0; f < feedCount; f++)
            {
                IFeed feed = getModel().getSelectedFeed();
                if (feed == null) return;
                while (feed.getUnreadArticlesCount() > 0)
                {
                    postKeyPressed(getArticleList().getComponent(), KeyEvent.VK_SPACE, KeyEvent.SHIFT_DOWN_MASK);
                    waitForPendingEvents();
                    sleep(100);
                }
            }
        }
    }

    private void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException e)
        {
        }
    }
}
