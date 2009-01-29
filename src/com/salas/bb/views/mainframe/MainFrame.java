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
// $Id: MainFrame.java,v 1.212 2008/04/15 10:30:32 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.jgoodies.uif.AbstractFrame;
import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.application.ApplicationDescription;
import com.jgoodies.uif.component.UIFSplitPane;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uif.util.WindowUtils;
import com.jgoodies.uifextras.util.PopupAdapter;
import com.jgoodies.uifextras.util.UIFactory;
import com.salas.bb.core.FeedRelocationController;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.*;
import com.salas.bb.core.actions.article.*;
import com.salas.bb.core.actions.feed.*;
import com.salas.bb.core.actions.guide.*;
import com.salas.bb.core.actions.logging.SwitchLogLevelAction;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.imageblocker.BlockImageAction;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.search.*;
import com.salas.bb.service.ShowServiceDialogAction;
import com.salas.bb.service.sync.SyncInAction;
import com.salas.bb.service.sync.SyncOutAction;
import com.salas.bb.tags.ShowArticleTagsAction;
import com.salas.bb.tags.ShowFeedTagsAction;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.OSSettings;
import com.salas.bb.utils.dnd.DNDList;
import com.salas.bb.utils.dnd.DNDListContext;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.utils.osx.OSXSupport;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.ActivityIndicatorView;
import com.salas.bb.views.ArticleListPanel;
import com.salas.bb.views.GuidesList;
import com.salas.bb.views.GuidesPanel;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.feeds.AbstractFeedDisplay;
import com.salas.bb.views.feeds.html.ArticlesGroup;
import com.salas.bb.views.feeds.html.HTMLArticleDisplay;
import com.salas.bb.views.feeds.html.HTMLFeedDisplay;
import com.salas.bb.twitter.TweetThisDialog;
import com.salas.bbnative.Taskbar;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Containing the main content of the application.
 */
public class MainFrame extends AbstractFrame
{
    private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());

    /** default window size for small monitor */
    private static final Dimension WINDOW_SIZE_SMALL = new Dimension(620, 510);
    /** default window size for big monitor */
    private static final Dimension WINDOW_SIZE_BIG = new Dimension(760, 570);
    /** "big" monitors are at least this wide (after deducting taskbar insets) */
    private static final int WINDOW_WIDTH_BIG_THRESHOLD = 1200;
    /** minimum amount of window that must be initially on screen */
    private static final Dimension WINDOW_MIN_VISIBLE = new Dimension(80, 50);
    /** minimum size for window */
    private static final Dimension WINDOW_MIN_SIZE = new Dimension(300, 200);

    /** preferences key for storing left pane divider size */
    private static final String KEY_LEFT_SPLIT_DIV = "win.mainLeftSplitDiv";
    /** preferences key for storing rightpane divider size */
    private static final String KEY_RIGHT_SPLIT_DIV = "win.mainRightSplitDiv";

    private static final Border ACTIVITY_BORDER = new EmptyBorder(0, 0, 0, 10);
    private static final Border STATUS_BORDER = new EmptyBorder(0, 6, 0, 6);

    /** A feed that the feed link in the article title of a smart feed article belongs to. */
    public static IFeed         feedLinkFeed;

    private final ConnectionState connectionState;

    private ArticleListPanel    articleListPanel;
    private FeedsPanel          feedsPanel;
    private GuidesPanel         guidesPanel;

    private UIFSplitPane        rightSplitPane;
    private UIFSplitPane        leftSplitPane;

    private PopupAdapter        guidesListPopupAdapter;
    private PopupAdapter        feedListPopupAdapter;
    private PopupAdapter        feedLinkPopupAdapter;
    private PopupAdapter        htmlDisplayPopupAdapter;
    private PopupAdapter        imageDisplayPopupAdapter;
    private PopupAdapter        articleHyperLinkPopupAdapter;
    private PopupAdapter        articleGroupPopupAdapter;

    private JTextField          tfStatus;
    private JToolBar            toolbar;

    private Component           mainPane;
    private SearchField         searchField;
    private SearchPopupMenu     searchPopup;

    private boolean             minimizeToSystemTray;
    private JPanel statusBar;
    private Box iconsPanel;

    /** All popup menu types. */
    private static enum PopupMenuType
        { ARTICLE_HYPERLINK, FEEDS_LIST, GUIDES_LIST, HTML_ARTICLE, IMAGE_ARTICLE, ARTICLE_GROUP }

    private java.util.List<IPopupMenuHook> popupMenuHooks;

    /**
     * Build the actual main window with this call. This leads to the calls to the
     * other methods in this class.
     *
     * @param aConnectionState object for tracking the connection state.
     */
    public MainFrame(ConnectionState aConnectionState)
    {
        super("BlogBridge");
        connectionState = aConnectionState;
        popupMenuHooks = new ArrayList<IPopupMenuHook>();

        // TODO move to a better place
        UIManager.put("SearchPopupMenuItemUI", SearchPopupMenuItemUI.class.getName());
        UIManager.put("SearchPopupMenuItem.selectionForeground", Color.WHITE);
        UIManager.put("SearchPopupMenuItem.typeForeground", Color.GRAY);
        UIManager.put("SearchPopupMenuItem.typeSelectionForeground", Color.WHITE);

        searchField = new SearchField();
        searchPopup = new SearchPopupMenu();

        setFeedTitle(null);

        // Register a notification area listener which opens and focuses
        // the window upon the icon / message click.
        NotificationArea.setAppIconActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                restoreWindow();
            }
        });

        enableEvents(AWTEvent.WINDOW_FOCUS_EVENT_MASK | AWTEvent.WINDOW_STATE_EVENT_MASK);
    }

    private void restoreWindow()
    {
        int state = getExtendedState();

        // If window was previously iconified, restore its state and show taskbar button
        // if it was minimized to systray
        if ((state & ICONIFIED) != 0)
        {
            // If the window was iconified then we restore its button etc
            if (NotificationArea.isSupported() && minimizeToSystemTray)
            {
                showTaskbarButton();
            }

            setExtendedState((state & ~ICONIFIED));
        }
        
        toFront();
    }


    /**
     * Processes window state event occuring on this window by
     * dispatching them to any registered <code>WindowStateListener</code>
     * objects.
     *
     * @param e the window state event
     */
    protected void processWindowStateEvent(WindowEvent e)
    {
        boolean isIconified = (e.getNewState() & Frame.ICONIFIED) != 0;
        boolean wasIconified = (e.getOldState() & Frame.ICONIFIED) != 0;

        // Hide button if notification area is supported
        if (NotificationArea.isSupported() && minimizeToSystemTray &&
            !wasIconified && isIconified)
        {
            hideTaskbarButton();
        }
    }

    /**
     * Shows taskbar button.
     */
    private void showTaskbarButton()
    {
        setVisible(false);
        Taskbar.showButton(MainFrame.this);
        NotificationArea.setAppIconTempVisible(false);
        setVisible(true);
    }

    /**
     * Hides taskbar button.
     */
    private void hideTaskbarButton()
    {
        setVisible(false);
        NotificationArea.setAppIconTempVisible(true);
        Taskbar.hideButton(MainFrame.this);
    }

    /**
     * Changes the flag of this window minimization mode.
     *
     * @param flag <code>TRUE</code> to minimize the window to system tray (if supported).
     */
    public void setMinimizeToSystemTray(boolean flag)
    {
        minimizeToSystemTray = flag && OSSettings.isMinimizeToSystraySupported();
    }

    /**
     * Hides notifications when window gets focus.
     *
     * @param e event.
     */
    protected void processWindowFocusEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_GAINED_FOCUS)
        {
            NotificationArea.setAppIconTempVisible(false);
        }
    }

    /**
     * Sets the title of window.
     *
     * @param feedTitle puts the name of selected feed into the title.
     */
    private void setFeedTitle(String feedTitle)
    {
        ApplicationDescription description = Application.getDescription();
        setTitle("BlogBridge - " +
            (feedTitle != null ? feedTitle + " - " : "") +
            description.getVersion());
    }

    /**
     * Returns search field component.
     *
     * @return search field.
     */
    public SearchField getSearchField()
    {
        return searchField;
    }

    /**
     * Register global application shortcuts.
     */
    public void registerShortcuts()
    {
        setupGlobalShortcuts();
        setupGuidesListShortcuts();
        setupFeedsListShortcuts();
        setupTestShortcuts();

        // Here we register additional global key events dispatcher
        // to track TAB / SHIFT-TAB.
        final KeyboardFocusManager keyFocusManager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();

        keyFocusManager.addKeyEventDispatcher(new FocusDispatcher());
    }

    private void setupTestShortcuts()
    {
        ActionMap ulAMap = getRootPane().getActionMap();
        ulAMap.put("test-f10", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println(AbstractLockupHandler.getThreadsDump(null));
            }
        });
        ulAMap.put("test-f11", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        ulAMap.put("test-f12", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });

        InputMap ulIMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ulIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "test-f10");
        ulIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "test-f11");
        ulIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "test-f12");
    }

    /**
     * Registers global application shortcuts.
     * <ul>
     * <li><b>Ctrl-[1..9]</b> - switch to guide 1 - 9.</li>
     * <li><b>Ctrl-N</b> - subscribe to new feed.</li>
     * <li><b>Ctrl-M</b> - update channel.</li>
     * <li><b>Ctrl-Shift-M</b> - update guide.</li>
     * <li><b>Alt-Ctrl-Shift-M</b> - update all guides.</li>
     * <li><b>Ctrl-Up </b>/ <b>Ctrl-Down</b> - move selected guide up / down.</li>
     * <li><b>Space </b>/ <b>Shift-Space</b> - select next / previous unread article and
     *                                         mark current as read.</li>
     * <li><b>K</b> - select next article with keywords.</li>
     * <li><b>Ctrl-Shift-P </b> - show preferences dialog.</li>
     * <li><b>Ctrl-Shift-F10</b> - show Networking Manager.</li>
     * <li><b>Ctrl-"-"</b> - make article text font smaller.</li>
     * <li><b>Ctrl-"+"</b> - make article text font bigger.</li>
     * <li><b>Q </b>- toggle article read/unread.</li>
     * <li><b>Ctrl-Q </b>- mark article as read.</li>
     * <li><b>Ctrl-Shift-Q </b>- mark channel as read.</li>
     * <li><b>Ctrl-Up </b>/ <b>Ctrl-Down </b>- move selection upper / lower.</li>
     * <li><b>T</b> - show article tags.</li>
     * </ul>
     */
    private void setupGlobalShortcuts()
    {
        // Record the actions in the action Map
        ActionMap glAMap = getRootPane().getActionMap();
        glAMap.put("switchGuide", SwitchGuideAction.getInstance());
        glAMap.put("updateChannel", UpdateSelectedFeedsAction.getInstance());
        glAMap.put("updateGuide", UpdateGuideAction.getInstance());
        glAMap.put("updateAllGuides", UpdateAllGuidesAction.getInstance());
        glAMap.put("serviceDialog", ShowServiceDialogAction.getInstance());
        glAMap.put("syncIn", SyncInAction.getInstance());
        glAMap.put("syncOut", SyncOutAction.getInstance());
        glAMap.put("gotoNextUnreadArticle", GotoNextUnreadAction.getInstance());
        glAMap.put("gotoNextUnreadArticleInNextFeed", GotoNextUnreadInNextFeedAction.getInstance());
        glAMap.put("gotoPreviousUnreadArticle", GotoPreviousUnreadAction.getInstance());
        glAMap.put("showPreferences", ShowPreferencesAction.getInstance());
        glAMap.put("newChannel", AddDirectFeedAction.getInstance());
        glAMap.put("networkingManager", ShowActivityWindowAction.getInstance());
        glAMap.put("fontBiasUp", new FontSizeBiasChangeAction(1));
        glAMap.put("fontBiasDown", new FontSizeBiasChangeAction(-1));
        glAMap.put("networkingManager", ShowActivityWindowAction.getInstance());
        glAMap.put("search", SearchAction.getInstance());

        // Logging
        glAMap.put("logLevelAll", new SwitchLogLevelAction(Constants.EMPTY_STRING, Level.FINE));
        glAMap.put("logLevelBlogbridge", new SwitchLogLevelAction("com.salas.bb", Level.FINE));

        // Articles
        glAMap.put("markArticleAsRead", MarkArticleReadAction.getInstance());
        glAMap.put("toggleArticleRead", ToggleArticleReadAction.getInstance());
        glAMap.put("gotoNextArticle", GotoNextArticleAction.getInstance());
        glAMap.put("gotoPreviousArticle", GotoPreviousArticleAction.getInstance());
        glAMap.put("markChannelAsRead", MarkFeedAsReadAction.getInstance());
        glAMap.put("showArticleTags", ShowArticleTagsAction.getInstance());
        glAMap.put("copyArticleToClipboard", StyledTextCopyAction.getInstance());
        glAMap.put("postToBlogAlt", PostToBlogAction.getActionSelector());

        // Map those actions to specific keystrokes (inputMap)
        InputMap glIMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        for (int i = KeyEvent.VK_1; i <= KeyEvent.VK_9; i++)
        {
            glIMap.put(KeyStroke.getKeyStroke(i, KeyEvent.CTRL_DOWN_MASK), "switchGuide");
        }

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK),
            "updateChannel");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "updateGuide");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
            "updateAllGuides");

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "showPreferences");

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.CTRL_DOWN_MASK),
            "serviceDialog");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_DOWN_MASK),
            "syncIn");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.CTRL_DOWN_MASK),
            "syncOut");

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ctrlOrCmd()), "fontBiasUp");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, ctrlOrCmd()), "fontBiasUp");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ctrlOrCmd()), "fontBiasDown");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, ctrlOrCmd()), "fontBiasDown");

        // Logging
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
            "logLevelAll");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
            "logLevelBlogbridge");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
            "logLevelInforma");

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
            "gotoNextUnreadArticle");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK),
            "gotoNextUnreadArticleInNextFeed");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.SHIFT_DOWN_MASK),
            "gotoPreviousUnreadArticle");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0),
            "gotoNextActicleWithKeywords");

        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrlOrCmd()), "newChannel");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10,
            KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
            "networkingManager");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrlOrCmd()), "search");

        // Articles
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "toggleArticleRead");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK),
            "markArticleAsRead");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK),
            "gotoNextArticle");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK),
            "gotoPreviousArticle");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK |
            KeyEvent.SHIFT_DOWN_MASK), "markChannelAsRead");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "showArticleTags");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK |
            KeyEvent.SHIFT_DOWN_MASK), "copyArticleToClipboard");
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "postToBlogAlt");

        // Text components compatibility shortcuts
        if (SystemUtils.IS_OS_WINDOWS)
        {
            addWindowsShortcuts("TextField");
            addWindowsShortcuts("TextArea");
            addWindowsShortcuts("TextPane");
            addWindowsShortcuts("EditorPane");
        }
    }

    /**
     * Adds Windows editor keys to the component input map.
     *
     * @param component component name.
     */
    private static void addWindowsShortcuts(String component)
    {
        InputMap inputMap = (InputMap)UIManager.getDefaults().get(component + ".focusInputMap");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), DefaultEditorKit.copyAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), DefaultEditorKit.cutAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), DefaultEditorKit.pasteAction);
    }

    private static int ctrlOrCmd()
    {
        return SystemUtils.IS_OS_MAC ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
    }

    /**
     * Registers shortcuts for guides list.
     * <ul>
     * <li><b>Insert </b>- add new guide.</li>
     * <li><b>Delete </b>- delete guide.</li>
     * </ul>
     */
    private void setupGuidesListShortcuts()
    {
        ActionMap ulAMap = guidesPanel.getActionMap();
        ulAMap.put("deleteGuide", DeleteGuideAction.getInstance());
        ulAMap.put("newGuide", AddGuideAction.getInstance());

        InputMap ulIMap = guidesPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        ulIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteGuide");
        ulIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "newGuide");

        // Reseting guide list bindings
        GuidesList guidesList = guidesPanel.getGuidesList();
        InputMap glIMap = guidesList.getInputMap(JComboBox.WHEN_FOCUSED).getParent();
        if (glIMap != null)
        {
            glIMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
            glIMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK));
            glIMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.SHIFT_DOWN_MASK));
            glIMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK));
            glIMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK));
        }

        // Add feed paste operation to the list
        ActionMap glAMap = guidesList.getActionMap();
        glIMap = guidesList.getInputMap(JComboBox.WHEN_FOCUSED);
        glAMap.put("pasteFeeds", FeedRelocationController.PASTE_OPERATION);
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrlOrCmd()), "pasteFeeds");
        glAMap.put("abortCopyMoveFeeds", FeedRelocationController.ABORT_OPERATION);
        glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "abortCopyMoveFeeds");

        if (SystemUtils.IS_OS_WINDOWS)
        {
            glIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK), "pasteFeeds");
        }
    }

    /**
     * Registers shortcuts for channels list.
     * <ul>
     * <li><b>Alt-Up </b>/ <b>Alt-Down </b>- move channel up / down in the list.</li>
     * <li><b>Insert </b>- new channel.</li>
     * <li><b>Delete </b>- delete channel.</li>
     * <li><b>Ctrl-Shift-Q </b>- mark channel as read.</li>
     * <li><b>Ctrl-P </b>- show feed properties dialog.</li>
     * <li><b>T</b> - show feed tags dialog.</li>
     * </ul>
     */
    private void setupFeedsListShortcuts()
    {
        ActionMap clAMap = feedsPanel.getActionMap();
        InputMap clIMap = feedsPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        clAMap.put("moveChannelUp", new MoveSelectedFeedUpAction(feedsPanel.feedsList));
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK), "moveChannelUp");

        clAMap.put("moveChannelDown", new MoveSelectedFeedDownAction(feedsPanel.feedsList));
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK), "moveChannelDown");

        clAMap.put("newChannel", AddDirectFeedAction.getInstance());
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "newChannel");

        clAMap.put("deleteChannel", DeleteFeedAction.getInstance());
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteChannel");
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), "deleteChannel");

        clAMap.put("markChannelAsRead", MarkFeedAsReadAction.getInstance());
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                "markChannelAsRead");

        clAMap.put("showFeedProps", ShowFeedPropertiesAction.getInstance());
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "showFeedProps");

        clAMap.put("showFeedTags", ShowFeedTagsAction.getInstance());
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "showFeedTags");

        // Feed list map
        clAMap = feedsPanel.getFeedsList().getActionMap();
        clIMap = feedsPanel.getFeedsList().getInputMap(JComponent.WHEN_FOCUSED);

        clAMap.put("copyFeeds", FeedRelocationController.COPY_OPERATION);
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ctrlOrCmd()), "copyFeeds");
        clAMap.put("cutFeeds", FeedRelocationController.CUT_OPERATION);
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ctrlOrCmd()), "cutFeeds");
        clAMap.put("pasteFeeds", FeedRelocationController.PASTE_OPERATION);
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ctrlOrCmd()), "pasteFeeds");
        clAMap.put("abortCopyMoveFeeds", FeedRelocationController.ABORT_OPERATION);
        clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "abortCopyMoveFeeds");
        if (SystemUtils.IS_OS_WINDOWS)
        {
            clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), "copyFeeds");
            clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), "cutFeeds");
            clIMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK), "pasteFeeds");
        }
    }

    /**
     * Build and return content pane.
     *
     * @return pane.
     */
    protected JComponent buildContentPane()
    {
        initComponents();

        BBMenuBuilder menuBldr = new BBMenuBuilder();
        BBToolBarBuilder tbBldr = new BBToolBarBuilder();

        // Build and attach the menu bar
        JMenuBar myMenuBar = menuBldr.buildMenuBar();
        setJMenuBar(myMenuBar);

        UIFactory.createPlainLabel(Application.getDescription().getCopyright());

        // Create the Content Pane for the main frame, set it up to be a gridbag and then add
        // each subpanel with the appropriate GridBag Constraint. (Sketch this on paper first :-)

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        toolbar = tbBldr.buildToolBar();
        topPanel.add(toolbar, BorderLayout.NORTH);

        mainPane = buildMainPane();
        topPanel.add(mainPane, BorderLayout.CENTER);
        statusBar = buildStatusBar();
        topPanel.add(statusBar, BorderLayout.SOUTH);

        // Choose default window size
        // Note this will get overridden in restoreState if we have
        // any saved values from a prevous session.
        Rectangle visRect = getVisibleScreenRect(new Rectangle());
        Dimension prefSize = (visRect.width >= WINDOW_WIDTH_BIG_THRESHOLD)
            ? WINDOW_SIZE_BIG : WINDOW_SIZE_SMALL;

        topPanel.setPreferredSize(prefSize);

        Preferences prefs = Application.getUserPreferences();
        setToolbarVisible(prefs.getBoolean(UserPreferences.PROP_SHOW_TOOLBAR,
            UserPreferences.DEFAULT_SHOW_TOOLBAR));
        setToolbarLabelsVisible(prefs.getBoolean(UserPreferences.PROP_SHOW_TOOLBAR_LABELS,
            UserPreferences.DEFAULT_SHOW_TOOLBAR_LABELS));

        return topPanel;
    }

    /**
     * Adds an icon component to the status bar icon section.
     *
     * @param icon icon.
     */
    public void addIconComponent(JComponent icon)
    {
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        iconsPanel.add(icon, 0);
        statusBar.validate();
    }

    /**
     * Changes the state of toolbar button labels.
     *
     * @param visible <code>TRUE</code> to make labels visible.
     */
    public void setToolbarLabelsVisible(boolean visible)
    {
        Component[] components = toolbar.getComponents();
        for (Component component : components)
        {
            if (component instanceof JButton)
            {
                JButton btn = (JButton)component;

                Action action = btn.getAction();
                if (action != null) btn.setText(visible ? (String)action.getValue("Name") : null);
            }
        }

        invalidate();
    }

    /**
     * Shows or hides toolbar.
     *
     * @param visible <code>TRUE</code> to show toolbar.
     */
    public void setToolbarVisible(boolean visible)
    {
        toolbar.setVisible(visible);
    }

    /**
     * Override to return our minimum window size. Unfortunately Swing doesn't
     * enforce this as the window border is being dragged; instead, the window
     * bounces back to this size when the border is released.
     */
    public Dimension getWindowMinimumSize()
    {
        return WINDOW_MIN_SIZE;
    }

    /**
     * Return a visible screen rectangle available for positioning a window.
     * Takes into account top-level insets like Windows taskbar, and
     * multi-monitor configurations
     *
     * @param findRect
     *            If multi-monitor configuration, use the monitor which on which
     *            this rectangle most covers. If findRect is an empty rectangle,
     *            this will use the default monitor
     *
     * @return Visible rectangle
     */
    protected Rectangle getVisibleScreenRect(Rectangle findRect)
    {
        GraphicsEnvironment grEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Iterate over all monitors and find the one with the
        // greatest area of overlap with findRect.
        int maxOverlap = 0;
        GraphicsConfiguration grConfig = grEnv.getDefaultScreenDevice().getDefaultConfiguration();
        GraphicsDevice[] gs = grEnv.getScreenDevices();
        for (GraphicsDevice gd : gs)
        {
            Rectangle r = gd.getDefaultConfiguration().getBounds();
            Dimension overSize = r.intersection(findRect).getSize();
            int overlap = overSize.width * overSize.height;
            if (overlap > maxOverlap)
            {
                maxOverlap = overlap;
                grConfig = gd.getDefaultConfiguration();
            }
        }

        Rectangle screenBounds = grConfig.getBounds();

        // Reduce bounds by the insets which represent always-on-top
        // toolbars on edge of screen.
        Insets screenInsets = getToolkit().getScreenInsets(grConfig);

        // A bug in Java 1.4.1 causes negative insets sometimes
        // on multiple monitors (see Java community bug database #4654713)
        screenInsets.left = Math.abs(screenInsets.left);
        screenInsets.right = Math.abs(screenInsets.right);
        screenInsets.top = Math.abs(screenInsets.top);
        screenInsets.bottom = Math.abs(screenInsets.bottom);

        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= (screenInsets.left + screenInsets.right);
        screenBounds.height -= (screenInsets.top + screenInsets.bottom);

        return screenBounds;
    }

    /**
     * Initializes components of the frame.
     */
    private void initComponents()
    {
        articleListPanel = new ArticleListPanel();
        feedsPanel = new FeedsPanel();
        guidesPanel = new GuidesPanel();
        guidesPanel.setupGuidesList(getGuidesPopupAdapter());

        // On double click actions setup
        feedsPanel.setOnDoubleClickAction(OpenBlogHomeAction.getInstance());
        guidesPanel.setOnDoubleClickAction(GuidePropertiesAction.getInstance());

        // Enable DND from Channel list to Guide list.
        feedsPanel.getFeedsList().addPropertyChangeListener(DNDList.PROP_MOUSE_DRAGGED_EVENT,
                guidesPanel.getGuidesList());

        // Search objects
        Dimension size = searchField.getPreferredSize();
        size.width = 100;
        searchField.setMinimumSize(size);
        searchField.setPreferredSize(size);
        searchField.setMaximumSize(size);
        searchPopup.setInvoker(this);

// TODO !!! review !!!
//        articleListPanel.getFeedView().addArticleSelectionListener(this);
    }

    /**
     * MainPane consists of a ChannelGuidePanel on the left and the SplitPane in the center.
     *
     * @return main pane.
     */
    private Component buildMainPane()
    {
        JPanel mainPane = new JPanel(new BorderLayout());

        buildRightSplitPane();
        buildLeftSplitPane();
        mainPane.add(leftSplitPane, BorderLayout.CENTER);

        return mainPane;
    }

    /**
     * This consist of the ChannelGuidePanel on the left and the RightSplitPanel on the right,
     * with a movable split bar.
     */
    private void buildLeftSplitPane()
    {
        leftSplitPane = (UIFSplitPane)
            UIFactory.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT, guidesPanel,
                rightSplitPane, 0);

        // On OS X the correct width of a split pane divider is a little thinner.
        if (SystemUtils.IS_OS_MAC)
        {
            leftSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7));
            leftSplitPane.setDividerSize(OSXSupport.SPLITPANE_WIDTH);
        } else
        {
            leftSplitPane.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 7));
        }
    }

    /**
     * This consist of the ChannelListPanel on the left and the ItemListPanel on the right,
     * with a movable split bar.
     */
    private void buildRightSplitPane()
    {
        rightSplitPane = (UIFSplitPane)
            UIFactory.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT, feedsPanel,
                articleListPanel, 0);

        // On OS X the correct width of a split pane divider is a little thinner.
        if (SystemUtils.IS_OS_MAC)
        {
            rightSplitPane.setDividerSize(OSXSupport.SPLITPANE_WIDTH);
        } else
        {
            rightSplitPane.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    /**
     * Create and configure the StatusBar.
     * 
     * @return status bar.
     */
    private JPanel buildStatusBar()
    {
        JPanel panel = new JPanel(new BorderLayout());

        // Display activity and connection indicators
        iconsPanel = Box.createHorizontalBox();
        iconsPanel.setBorder(ACTIVITY_BORDER);

        JComponent activityIndicator = new ActivityIndicatorView(connectionState,
                new MouseAdapter()
                {
                    public void mouseClicked(MouseEvent e)
                    {
                        ConnectionStateSwitchAction.getInstance().actionPerformed(null);
                    }
                });
        iconsPanel.add(activityIndicator);

        // On OS X, the rightmost position of the status bar is usurped by the silly little resize
        // control.
        if (SystemUtils.IS_OS_MAC)
        {
            final JPanel spacer = new JPanel();
            spacer.setPreferredSize(new Dimension(10, -1));
            iconsPanel.add(spacer);
        }

        panel.add(iconsPanel, BorderLayout.EAST);

        // Status bar
        tfStatus = new JTextField()
        {
            @Override
            protected void processEvent(AWTEvent e)
            {
                // No events in labels
            }

            @Override
            public void updateUI()
            {
                super.updateUI();
                setOpaque(false);
                setMargin(new Insets(0, 0, 0, 0));
            }
        };
        tfStatus.setFocusable(false);
        tfStatus.setBorder(STATUS_BORDER);
        tfStatus.setEditable(false);
        panel.add(tfStatus, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Sets the status of the application or other information to the status bar.
     *
     * @param status
     *        status.
     */
    public void setStatus(String status)
    {
        tfStatus.setText(status);
        if (status != null) tfStatus.setCaretPosition(0);
    }

    /**
     * Called just before we close, for an opportunity to save state.
     *
     * JGoodies would normally call storeState from the ApplicationClosingHandler,
     * but our shutdown process quits before we get that far, so this is alternate wiring.
     */
    public void prepareToClose()
    {
        // The storeState call will save bound our bounds and window state.
        storeState();

        final Preferences prefs = Application.getUserPreferences();

        if (leftSplitPane != null && rightSplitPane != null)
        {
            prefs.putInt(KEY_LEFT_SPLIT_DIV, leftSplitPane.getDividerLocation());
            prefs.putInt(KEY_RIGHT_SPLIT_DIV, rightSplitPane.getDividerLocation());
        }
    }

    /**
     * Stores window state before closing.
     */
    protected void storeState()
    {
        Preferences userPrefs = Application.getUserPreferences();
        WindowUtils.storeBounds(userPrefs, this);
        WindowUtils.storeState(userPrefs, this);
    }

    /**
     * Restore our window state as we are being launched.
     *
     * Restore state after all is built.
     */
    protected void restoreState()
    {
        // Restore window position
        super.restoreState();

        // Restore divider sizes
        final Preferences prefs = Application.getUserPreferences();
        int leftSplitDiv = prefs.getInt(KEY_LEFT_SPLIT_DIV, -1);
        int rightSplitDiv = prefs.getInt(KEY_RIGHT_SPLIT_DIV, -1);

        // Do some simple sanity checking
        if (leftSplitDiv > 0 && rightSplitDiv > 0 &&
            (leftSplitDiv + rightSplitDiv) < getBounds().width)
        {
            leftSplitPane.setDividerLocation(leftSplitDiv);
            rightSplitPane.setDividerLocation(rightSplitDiv);
        }

        // Make sure window is not sized larger than the current screen, and that
        // at least the top-left corner is visible on the current screen, so that
        // it can be dragged to make it fully visible.
        Rectangle r = getBounds();

        Rectangle visRect = getVisibleScreenRect(r);
        r.x = Math.min(r.x, visRect.width - WINDOW_MIN_VISIBLE.width);
        r.y = Math.min(r.y, visRect.height - WINDOW_MIN_VISIBLE.height);
        r.x = Math.max(r.x, visRect.x);
        r.y = Math.max(r.y, visRect.y);
        r.width = Math.min(r.width, visRect.width);
        r.height = Math.min(r.height, visRect.height);

        setBounds(r);

        GlobalController.SINGLETON.selectFeed(GlobalModel.SINGLETON.getSelectedFeed());

        registerShortcuts();

        // Setup notification area menu
        if (NotificationArea.isSupported())
        {
            final String miShowBB = Strings.message("systray.menu.showbb");
            final String miCheckUpdates = Strings.message("systray.menu.checkupdates");
            final String miExit = Strings.message("systray.menu.exit");

            PopupMenu menu = new PopupMenu();
            menu.add(miShowBB);
            menu.addSeparator();
            menu.add(miCheckUpdates);
            menu.addSeparator();
            menu.add(miExit);

            NotificationArea.setAppIconMenu(menu);

            menu.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (miShowBB.equals(e.getActionCommand()))
                    {
                        restoreWindow();
                    } else if (miCheckUpdates.equals(e.getActionCommand()))
                    {
                        ActionManager.get(ActionsTable.CMD_GUIDE_RELOAD_ALL_SM).actionPerformed(null);
                    } else if (miExit.equals(e.getActionCommand()))
                    {
                        ActionManager.get(ActionsTable.CMD_BB_EXIT).actionPerformed(null);
                    }
                }
            });
        }

        // Don't let the application start in iconified mode. 
        setExtendedState(getExtendedState() & ~ICONIFIED);
    }

    /**
     * Returns channels list panel reference.
     *
     * @return list panel.
     */
    public FeedsPanel getFeedsPanel()
    {
        return feedsPanel;
    }

    /**
     * Returns reference onto guides panel.
     *
     * @return guides panel.
     */
    public GuidesPanel getGudiesPanel()
    {
        return guidesPanel;
    }

    /**
     * Returns items list panel reference.
     *
     * @return list panel.
     */
    public ArticleListPanel getArticlesListPanel()
    {
        return articleListPanel;
    }

    /**
     * Selects the feed in list.
     *
     * @param feed
     *        feed to select.
     */
    public void selectFeed(IFeed feed)
    {
        updateTitle(feed);

        if (feedsPanel != null) feedsPanel.selectListItem(feed);
    }

    /**
     * Updates title of the frame with name of currently selected feed.
     *
     * @param feed feed to select.
     */
    public void updateTitle(IFeed feed)
    {
        setFeedTitle(feed == null ? null : feed.getTitle());
    }

    /**
     * Returns a popup adapter for the guides list.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getGuidesPopupAdapter()
    {
        if (guidesListPopupAdapter == null)
        {
            guidesListPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.feeds"));

                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_RELOAD));
                    menu.addSeparator();

                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_ADD));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_DELETE));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MERGE));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_SUBSCRIBE_READINGLIST));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_POST_TO_BLOG));
                    menu.addSeparator();

                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_UNREAD));
                    menu.addSeparator();

                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_IMPORT));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_EXPORT));

                    menu.addSeparator();
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_SORT_BY_TITLE));
                    menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_PROPERTIES));

                    appendCustomActions(menu, PopupMenuType.GUIDES_LIST);

                    return menu;
                }
            };
        }

        return guidesListPopupAdapter;
    }

    /**
     * Returns a PopupAdapter for the Feeds List right click menu.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getFeedsListPopupAdapter()
    {
        if (feedListPopupAdapter == null)
        {
            feedListPopupAdapter = new PopupAdapter()
            {

                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.feeds"));

                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_RELOAD));
                    menu.addSeparator();

                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_MARK_UNREAD));
                    menu.addSeparator();

                    // Make sure the icon is never displayed.
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_SUBSCRIBE));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_ADD_SMART_FEED));

                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_DELETE));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_PROPERTIES));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_TAGS));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_POST_TO_BLOG));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_DISCOVER));

                    menu.addSeparator();
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_BROWSE));

                    appendCustomActions(menu, PopupMenuType.FEEDS_LIST);

                    return menu;
                }
            };
        }

        return feedListPopupAdapter;
    }

    /**
     * Returns a PopupAdapter for the Feeds Link in the article header (Smart Feed).
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getFeedLinkPopupAdapter()
    {
        if (feedLinkPopupAdapter == null)
        {
            feedLinkPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.feeds"));

                    FeedLinkMarkFeedAsReadAction.setFeed(feedLinkFeed);
                    FeedLinkMarkFeedAsUnreadAction.setFeed(feedLinkFeed);
                    FeedLinkShowFeedPropertiesAction.setFeed(feedLinkFeed);
                    FeedLinkShowFeedTagsAction.setFeed(feedLinkFeed);
                    FeedLinkPostToBlogAction.setFeed(feedLinkFeed);

                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_LINK_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_LINK_MARK_UNREAD));

                    menu.addSeparator();
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_LINK_PROPERTIES));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_LINK_TAGS));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_LINK_POST_TO_BLOG));
                    menu.add(ActionManager.get(ActionsTable.CMD_FEED_DISCOVER));

                    return menu;
                }
            };
        }

        return feedLinkPopupAdapter;
    }

    /**
     * Returns a popup adapter for HTML feed display.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getHTMLDisplayPopupAdapter()
    {
        if (htmlDisplayPopupAdapter == null)
        {
            htmlDisplayPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    boolean canHaveSelection = false;
                    if (anevent.getSource() instanceof AbstractFeedDisplay)
                    {
                        AbstractFeedDisplay fd = (AbstractFeedDisplay)anevent.getSource();
                        SelectedTextCopyAction.setDisplay(fd);
                        canHaveSelection = true;
                    }
                    
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.articles"))
                    {
                        /**
                         * Sets the visibility of the popup menu.
                         *
                         * @param visible true to make the popup visible, or false to
                         *          hide it
                         */
                        public void setVisible(boolean visible)
                        {
                            super.setVisible(visible);
                            if (!visible)
                            {
                                SelectedTextCopyAction.setDisplay(null);
                            }
                        }
                    };

                    addBlockImageCommand(menu);

                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_UNREAD));

                    menu.addSeparator();
                    
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_BROWSE));
                    if (canHaveSelection) menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_COPY_TEXT));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_COPY_LINK));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_SEND_LINK));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_POST_TO_BLOG));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_TWEET_THIS));

                    menu.addSeparator();

                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PROPERTIES));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_TAGS));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PIN_UNPIN));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_DISCOVER));

                    appendCustomActions(menu, PopupMenuType.HTML_ARTICLE);

                    return menu;
                }
            };
        }

        return htmlDisplayPopupAdapter;
    }

    /**
     * Returns a popup adapter for image feed display.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getImageDisplayPopupAdapter()
    {
        if (imageDisplayPopupAdapter == null)
        {
            imageDisplayPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.articles"));

                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_UNREAD));
                    menu.addSeparator();
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_BROWSE));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_COPY_LINK));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_SAVE_IMAGE));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_POST_TO_BLOG));
                    menu.addSeparator();
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PROPERTIES));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_TAGS));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PIN_UNPIN));

                    appendCustomActions(menu, PopupMenuType.IMAGE_ARTICLE);

                    return menu;
                }
            };
        }

        return imageDisplayPopupAdapter;
    }

    public synchronized MouseListener getArticleGroupPopupAdapter()
    {
        if (articleGroupPopupAdapter == null)
        {
            articleGroupPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    ArticlesGroup ag = (ArticlesGroup)anevent.getSource();
                    MarkArticlesGroupAction.setGroup(ag);

                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.article.groups"));

                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLEGROUP_MARK_READ));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLEGROUP_MARK_UNREAD));

                    appendCustomActions(menu, PopupMenuType.ARTICLE_GROUP);

                    return menu;
                }
            };
        }

        return articleGroupPopupAdapter;
    }

    /**
     * Returns popup adapter for article hyper-links.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getArticleHyperLinkPopupAdapter()
    {
        if (articleHyperLinkPopupAdapter == null)
        {
            articleHyperLinkPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    JPopupMenu menu = new NonlockingPopupMenu(Strings.message("ui.popup.hyperlinks"));

                    GlobalController controller = GlobalController.SINGLETON;

                    NetworkFeed hoveredFeed = controller.getFeedByHoveredHyperLink();

                    // Set links to the actions as the hovered link will be reset upon
                    // the menu opening as the mouse pointer will move away off the link.
                    URL link = controller.getHoveredHyperLink();
                    HyperLinkOpenAction.setLink(link);
                    HyperLinkCopyAction.setLink(link);
                    HyperLinkSaveAsAction.setLink(link);
                    HyperLinkEmailAction.setLink(link);

                    addBlockImageCommand(menu);

                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_HYPERLINK_OPEN));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_HYPERLINK_COPY));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_HYPERLINK_SAVE_AS));
                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_HYPERLINK_SEND));

                    menu.addSeparator();

                    if (hoveredFeed != null)
                    {
                        SelectBlogByLinkAction.setFeed(hoveredFeed);
                        menu.add(ActionManager.get(ActionsTable.CMD_FEED_GOTO_BY_LINK));
                    } else
                    {
                        AddBlogByLinkAction.setLink(link);
                        menu.add(ActionManager.get(ActionsTable.CMD_FEED_SUBSCRIBE_BY_LINK));
                    }

                    appendCustomActions(menu, PopupMenuType.ARTICLE_HYPERLINK);

                    return menu;
                }
            };
        }

        return articleHyperLinkPopupAdapter;
    }

    /**
     * Adds a block image command if that was clicked.
     *
     * @param menu menu.
     */
    private void addBlockImageCommand(JPopupMenu menu)
    {
        URL url = HTMLArticleDisplay.clickImageURL;
        BlockImageAction.setBlockURL(url);
        if (url != null)
        {
            menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_BLOCK_IMAGE));

            menu.addSeparator();
        }
    }

    /**
     * Appends custom actions to the menu if they are present.
     *
     * @param menu  menu.
     * @param type  type of the hook.
     */
    private void appendCustomActions(JPopupMenu menu, PopupMenuType type)
    {
        java.util.List<Action> actions = new ArrayList<Action>();
        for (IPopupMenuHook hook : popupMenuHooks)
        {
            Collection<Action> pa;

            switch (type)
            {
                case ARTICLE_HYPERLINK:
                    pa = hook.getArticleHyperlinkActions();
                    break;
                case FEEDS_LIST:
                    pa = hook.getFeedsListActions();
                    break;
                case GUIDES_LIST:
                    pa = hook.getGuidesListActions();
                    break;
                case HTML_ARTICLE:
                    pa = hook.getHTMLArticleActions();
                    break;
                case IMAGE_ARTICLE:
                    pa = hook.getImageArticleActions();
                    break;
                case ARTICLE_GROUP:
                    pa = hook.getArticleGroupActions();
                    break;
                default:
                    pa = null;
                    break;
            }

            if (pa != null) actions.addAll(pa);
        }

        if (actions.size() > 0)
        {
            menu.addSeparator();
            for (Action action : actions) menu.add(action);
        }
    }

    /**
     * Focuses on channel list.
     */
    public void setFocusChannelGuide()
    {
        feedsPanel.returnFocusableComponent().requestFocus();
    }

    /**
     * Repaints highlights in articles list.
     */
    public void repaintArticlesListHighlights()
    {
        if (articleListPanel == null) return;

        final IFeedDisplay articleList = articleListPanel.getFeedView();
        if (UifUtilities.isEDT())
        {
            articleList.repaintHighlights();
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    articleList.repaintHighlights();
                }
            });
        }
    }

    /**
     * Registers search result to monitor.
     *
     * @param aResult result.
     */
    public void setSearchResult(ISearchResult aResult)
    {
        aResult.addChangesListener(new SearchResultListener());
    }

    /**
     * This class is a replacement of FocustTraversalPolicies. It represents the listener of keyboard events which will
     * be plugged globally. What it does is checking each of key-pressed events for TAB and if it finds it then moves
     * focus to the next "correct" component and consumes original event. <p/>Why things are done in this way? Honestly,
     * I was not able to create correct focus traversal policies for crossing focus cycles. I'll continue my
     * investigations later, but for now this class covers all of the requirements pretty good.
     */
    private class FocusDispatcher implements KeyEventDispatcher
    {

        /**
         * Invoked before any of application handlers check this event.
         *
         * @param e
         *        keyboard event.
         * @return TRUE if event consumed.
         * @see KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
         */
        public boolean dispatchKeyEvent(final KeyEvent e)
        {
            boolean consumed = false;
            int code = e.getKeyCode();

            if (e.getID() == KeyEvent.KEY_PRESSED)
            {
                if (code == KeyEvent.VK_TAB)
                {
                    if (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK)
                    {
                        consumed = focusBackward();
                    } else if (e.getModifiersEx() == 0)
                    {
                        consumed = focusForward();
                    }

                    if (consumed) e.consume();
                } else if (isCopyGuesture(code))
                {
                    DNDListContext.setCopying(true);
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED && isCopyGuesture(code))
            {
                DNDListContext.setCopying(false);
            }

            return consumed;
        }

        private boolean isCopyGuesture(int aCode)
        {
            return (SystemUtils.IS_OS_MAC && aCode == KeyEvent.VK_ALT) ||
                aCode == KeyEvent.VK_CONTROL;
        }

        /**
         * Pass focus forward.
         *
         * @return TRUE if took care about event.
         */
        private boolean focusForward()
        {
            if (LOG.isLoggable(Level.FINEST)) LOG.finest("Forward");
            final KeyboardFocusManager keyFocusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();

            final Component current = keyFocusManager.getFocusOwner();
            Component next = null;

            // TODO REVIEW !!!
            if (current instanceof IFeedDisplay)
            {
                next = guidesPanel.getFocusableComponent();
            } else if (current == guidesPanel.getFocusableComponent())
            {
                next = feedsPanel.feedsList;
            } else if (current == feedsPanel.feedsList)
            {
                next = articleListPanel.getFeedView().getComponent();
            }

            if (next != null)
            {
                next.requestFocusInWindow();
            }

            return next != null;
        }

        /**
         * Pass focus backward.
         *
         * @return TRUE if took care about event.
         */
        private boolean focusBackward()
        {
            if (LOG.isLoggable(Level.FINEST)) LOG.finest("Backward");

            final KeyboardFocusManager keyFocusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            final Component current = keyFocusManager.getFocusOwner();
            Component next = null;

            // TODO REVIEW
            if (current == articleListPanel.getFeedView().getComponent())
            {
                next = feedsPanel.feedsList;
            } else if (current == feedsPanel.feedsList)
            {
                next = guidesPanel.getFocusableComponent();
            } else if (current == guidesPanel.getFocusableComponent())
            {
                next = articleListPanel.getFeedView().getComponent();
            }

            if (next != null)
            {
                next.requestFocusInWindow();
            }

            return next != null;
        }
    }

    /**
     * Tell the Application class what to do on Close of the window.
     */
    protected void configureCloseOperation()
    {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                GlobalController.exitApplication();
            }
        });
    }

    /**
     * Processes window events occurring on this component. Hides the window or disposes of it, as
     * specified by the setting of the <code>defaultCloseOperation</code> property.
     *
     * @param e the window event
     *
     * @see #setDefaultCloseOperation
     * @see java.awt.Window#processWindowEvent
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (SystemUtils.IS_OS_WINDOWS && e.getID() == WindowEvent.WINDOW_DEICONIFIED)
        {
            // The code fragment below fixes bug with incorrect repaint of the deiconified
            // main frame on Win32 platform, meaning, when expanding from iconified to open window.
            repaint();
        }
    }

    /**
     * The UI Framework needs some kind of ID to tell windows apart.
     * Since we only have one window, it doesn't matter what it is.
     *
     * @return ID of the window.
     */
    public String getWindowID()
    {
        return "BlogBridgeMainWindow";
    }

    /**
     * Registers a popup menu hook.
     *
     * @param h hook.
     */
    public void addPopupMenuHook(IPopupMenuHook h)
    {
        if (!popupMenuHooks.contains(h)) popupMenuHooks.add(h);
    }

    /**
     * Removes a popup menu hook.
     *
     * @param h hook.
     */
    public void removePopupMenuHook(IPopupMenuHook h)
    {
        popupMenuHooks.remove(h);
    }

    /**
     * Creates a non-locking menu.
     *
     * @param label label.
     *
     * @return menu.
     */
    public JPopupMenu createNonLockingPopupMenu(String label)
    {
        return new NonlockingPopupMenu(label);
    }

    /**
     * The menu which isn't locking invoker component. Upon hiding it releases the reference
     * allowing it to be normally GC'ed even if this menu instance is cached somewhere.
     */
    private class NonlockingPopupMenu extends JPopupMenu
    {
        /**
         * Constructs a <code>JPopupMenu</code> with the specified title.
         *
         * @param label the string that a UI may use to display as a title for the popup menu.
         */
        public NonlockingPopupMenu(String label)
        {
            super(label);
        }

        /**
         * Displays the popup menu at the position x,y in the coordinate space of the component
         * invoker.
         *
         * @param invoker the component in whose space the popup menu is to appear
         * @param x       the x coordinate in invoker's coordinate space at which the popup menu is to
         *                be displayed
         * @param y       the y coordinate in invoker's coordinate space at which the popup menu is to
         *                be displayed
         */
        public void show(Component invoker, int x, int y)
        {
            super.show(invoker, x, y);

            // Release the invoker
            setInvoker(MainFrame.this);
        }
    }

    /**
     * Monitors the updates in search results and shows/updates popup.
     */
    private class SearchResultListener implements ISearchResultListener
    {
        private int items = 0;
        private static final int MAX_ITEMS = 10;

        /**
         * Invoked when new result item is added to the list.
         *
         * @param result results list object.
         * @param item   item added.
         * @param index  item index.
         */
        public void itemAdded(ISearchResult result, final ResultItem item, int index)
        {
            if (items < MAX_ITEMS)
            {
                items++;
                final boolean showPopup = items == 1;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        searchPopup.add(item);
                        if (showPopup) showPopup();
                    }
                });
            }
        }

        /**
         * Invoked when the result items are removed from the list.
         *
         * @param result results list object.
         */
        public void itemsRemoved(ISearchResult result)
        {
            items = 0;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    hidePopup();
                    searchPopup.removeAll();
                }
            });
        }

        /**
         * Invoked when underlying search is finished.
         *
         * @param result results list object.
         */
        public void finished(ISearchResult result)
        {
        }

        /**
         * Shows popup.
         */
        private void showPopup()
        {
            int mainPaneWidth = mainPane.getSize().width;
            Point loc = new Point(mainPaneWidth - searchPopup.getPreferredSize().width, 0);
            SwingUtilities.convertPointToScreen(loc, mainPane);
            searchPopup.setLocation(loc);
            searchPopup.setVisible(true);
            searchField.requestFocusInWindow();
        }

        /**
         * Hides popup.
         */
        private void hidePopup()
        {
            searchPopup.setVisible(false);
        }
    }
}