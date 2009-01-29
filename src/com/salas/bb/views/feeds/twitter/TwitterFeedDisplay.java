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
// $Id$
//

package com.salas.bb.views.feeds.twitter;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uifextras.util.PopupAdapter;
import com.jgoodies.uif.action.ActionManager;
import com.salas.bb.domain.IArticle;
import com.salas.bb.views.feeds.AbstractFeedDisplay;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.html.ArticlesGroup;
import com.salas.bb.views.feeds.html.IArticleDisplayConfig;
import com.salas.bb.views.feeds.html.IHTMLFeedDisplayConfig;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.twitter.FollowAction;
import com.salas.bb.twitter.ReplyAction;
import com.salas.bb.twitter.TwitterGateway;
import com.salas.bb.twitter.SubscribeAction;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import java.net.URL;
import java.io.IOException;

/**
 * Twitter feed display.
 */
public class TwitterFeedDisplay extends AbstractFeedDisplay
{
    private static final Map<String, String> USER_INFO_CACHE =
        Collections.synchronizedMap(new WeakHashMap<String, String>());

    private static final Map<String, String> SEARCH_CACHE =
        Collections.synchronizedMap(new WeakHashMap<String, String>());

    private static final Logger LOG = Logger.getLogger(TwitterFeedDisplay.class.getName());
    private IHTMLFeedDisplayConfig htmlConfig;

    private PopupAdapter userLinkPopupAdapter;
    private PopupAdapter hashtagLinkPopupAdapter;

    /**
     * Abstract view.
     *
     * @param aConfig        display configuration.
     * @param pageModel      page model to update when page changes.
     * @param pageCountModel page model with the number of pages (updated by the FeedDisplayModel).
     */
    public TwitterFeedDisplay(IHTMLFeedDisplayConfig aConfig, ValueModel pageModel, ValueModel pageCountModel)
    {
        super(aConfig, pageModel, pageCountModel);
        htmlConfig = aConfig;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (ArticlesGroup group : groups) add(group);

        add(noContentPanel);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    /**
     * Returns current logger.
     *
     * @return logger object.
     */
    protected Logger getLogger()
    {
        return LOG;
    }

    /**
     * Creates new article display for addition to the display.
     *
     * @param aArticle article to create display for.
     *
     * @return display.
     */
    protected IArticleDisplay createNewArticleDisplay(IArticle aArticle)
    {
        IArticleDisplayConfig articleConfig = htmlConfig.getArticleViewConfig();
        return new TwitterArticleDisplay(aArticle, articleConfig);
    }

    /**
     * Returns the view popup adapter.
     *
     * @return view popup adapter.
     */
    protected MouseListener getViewPopupAdapter()
    {
        return GlobalController.SINGLETON.getMainFrame().getHTMLDisplayPopupAdapter();
    }

    /**
     * Returns the link popup adapter.
     *
     * @return link popup adapter.
     */
    protected MouseListener getLinkPopupAdapter()
    {
        return isUserLink(hoveredLink) ? getArticleUserLinkPopupAdapter()
            : isHashtagLink(hoveredLink) ? getHashtagLinkPopupAdapter()
            : htmlConfig.getLinkPopupAdapter();
    }

    /**
     * Returns TRUE if it's the user link that is hovered.
     *
     * @param link link.
     *
     * @return TRUE if it is.
     */
    private boolean isUserLink(URL link)
    {
        if (link == null) return false;

        String urls = link.toString();

        return urls.matches("^http://(www\\.)?twitter\\.com/[^/]+($|\\?|#)");
    }

    /**
     * Returns TRUE if it's the hashtag link that is hovered.
     *
     * @param link link.
     *
     * @return TRUE if it is.
     */
    private boolean isHashtagLink(URL link)
    {
        if (link == null) return false;

        String urls = link.toString();

        return urls.matches("^http://search\\.twitter\\.com/search(\\.(json|atom))?\\?q=(#|%23)[^&]+");
    }

    /**
     * Returns popup adapter for article user hyper-links.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getArticleUserLinkPopupAdapter()
    {
        if (userLinkPopupAdapter == null)
        {
            userLinkPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    GlobalController controller = GlobalController.SINGLETON;
                    MainFrame frame = controller.getMainFrame();
                    JPopupMenu menu = frame.createNonLockingPopupMenu("User Link");

                    FollowAction actFollow = FollowAction.getInstance();

                    // Set links to the actions as the hovered link will be reset upon
                    // the menu opening as the mouse pointer will move away off the link.
                    URL link = controller.getHoveredHyperLink();
                    actFollow.setUserURL(link);
                    ReplyAction.getInstance().setUserURL(link);

                    if (actFollow.isAvailable()) menu.add(actFollow);
                    menu.add(ActionManager.get(ActionsTable.CMD_TWITTER_MESSAGE));

                    return menu;
                }
            };
        }

        return userLinkPopupAdapter;
    }

    /**
     * Returns popup adapter for article user hashtag hyper-links.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getHashtagLinkPopupAdapter()
    {
        if (hashtagLinkPopupAdapter == null)
        {
            hashtagLinkPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    GlobalController controller = GlobalController.SINGLETON;
                    MainFrame frame = controller.getMainFrame();
                    JPopupMenu menu = frame.createNonLockingPopupMenu("Hashtag Link");

                    SubscribeAction action = (SubscribeAction)ActionManager.get(ActionsTable.CMD_TWITTER_SUBSCRIBE);

                    // Set links to the actions as the hovered link will be reset upon
                    // the menu opening as the mouse pointer will move away off the link.
                    URL link = controller.getHoveredHyperLink();
                    action.setUserURL(link);
                    
                    menu.add(action);

                    return menu;
                }
            };
        }

        return hashtagLinkPopupAdapter;
    }


    /**
     * Returns tool-tip for a give link.
     *
     * @param link          link.
     * @param textPane      pane requesting the tooltip.
     *
     * @return tool-tip text.
     */
    protected String getHoveredLinkTooltip(URL link, final JComponent textPane)
    {
        if (link == null) return null;

        return isUserLink(link) ? getUserInfoTooltipText(link, textPane)
            : isHashtagLink(link) ? getHashtagTooltipText(link, textPane)
            : null;
    }

    /**
     * Returns the search info tooltip text and schedules the async
     *
     * @param link          link.
     * @param textPane      pane.
     *
     * @return text.
     */
    private String getHashtagTooltipText(URL link, final JComponent textPane)
    {
        final String hashtag = TwitterGateway.urlToHashtag(link);

        String html = SEARCH_CACHE.get(hashtag);
        if (html == null)
        {
            new Thread("Twitter Hashtag Search")
            {
                public void run()
                {
                    String html;
                    try
                    {
                        html = TwitterGateway.search("#" + hashtag);
                    } catch (IOException e)
                    {
                        html = Strings.message("twitter.unavailable");
                    }

                    cacheAndSetHashtag(html, hashtag, textPane);

                }
            }.start();
        }

        return html;
    }

    /**
     * Returns the user info tooltip text and schedules the async loading if necessary.
     *
     * @param link     link.
     * @param textPane text pane.
     *
     * @return text.
     */
    private String getUserInfoTooltipText(URL link, final JComponent textPane)
    {
        final String screenName = TwitterGateway.urlToScreenName(link);

        String info = USER_INFO_CACHE.get(screenName);
        if (info == null)
        {
            new Thread("Twitter User Info")
            {
                public void run()
                {
                    String userInfo;
                    try
                    {
                        userInfo = TwitterGateway.userInfoHTML(screenName);
                    } catch (IOException e)
                    {
                        userInfo = Strings.message("twitter.unavailable");
                    }

                    cacheAndSetUserInfo(userInfo, screenName, textPane);
                }
            }.start();

            return Strings.message("twitter.loading");
        }

        return info;
    }

    /**
     * Places the info in cache and shows the tooltip.
     *
     * @param userInfo      info HTML.
     * @param screenName    screen name.
     * @param textPane      text pane component.
     */
    private void cacheAndSetUserInfo(final String userInfo, final String screenName, final JComponent textPane)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                USER_INFO_CACHE.put(screenName, userInfo);
                showTooltip(textPane, userInfo);
            }
        });
    }

    /**
     * Places the results in cache and shows the tooltip.
     *
     * @param html          search HTML.
     * @param hashtag       hash tag.
     * @param textPane      pane.
     */
    private void cacheAndSetHashtag(final String html, final String hashtag, final JComponent textPane)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                SEARCH_CACHE.put(hashtag, html);
                showTooltip(textPane, html);
            }
        });
    }

    /**
     * Makes the tooltip re-appear with the given text.
     *
     * @param textPane pane.
     * @param text     text.
     */
    private static void showTooltip(JComponent textPane, String text)
    {
        Action hideTip = textPane.getActionMap().get("hideTip");
        if (hideTip != null) hideTip.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, "hideTip"));

        textPane.setToolTipText(text);

        Action postTip = textPane.getActionMap().get("postTip");
        if (postTip != null) postTip.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, "postTip"));
    }
}