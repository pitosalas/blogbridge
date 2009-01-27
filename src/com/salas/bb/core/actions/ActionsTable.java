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
// $Id: ActionsTable.java,v 1.107 2008/04/08 08:06:19 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.action.ToggleAction;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.article.*;
import com.salas.bb.core.actions.feed.*;
import com.salas.bb.core.actions.guide.*;
import com.salas.bb.discovery.actions.DiscoverInArticlesAction;
import com.salas.bb.discovery.actions.DiscoverInFeedsAction;
import com.salas.bb.imageblocker.BlockImageAction;
import com.salas.bb.imageblocker.ImageBlockerConfigAction;
import com.salas.bb.plugins.ManagerAction;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.reports.ReportsAction;
import com.salas.bb.search.SearchAction;
import com.salas.bb.sentiments.SettingsAction;
import com.salas.bb.service.ShowServiceDialogAction;
import com.salas.bb.service.sync.SyncFullAction;
import com.salas.bb.tags.SelectiveShowTagsAction;
import com.salas.bb.tags.ShowArticleTagsAction;
import com.salas.bb.tags.ShowFeedTagsAction;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.osx.OSXSupport;
import com.salas.bb.utils.uif.TipOfTheDay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.feeds.image.SaveImageAction;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.whatshot.WhatsHotAction;
import com.salas.bb.twitter.TweetThisAction;

/**
 * The following are command codes - constant strings. Theses strings are used to look in
 * Action.Properties. The naming convention is CMD_[noun]_[verb] and the value is a string, camel
 * case, starting with lower case.
 * @noinspection ALL
 */
public final class ActionsTable
{
    private static final ActionsTable INSTANCE = new ActionsTable();

    public static final String CMD_GUIDE_WHATIS                     = "guide.whatis";
    public static final String CMD_GUIDE_ADD                        = "guide.add";
    public static final String CMD_GUIDE_ADD_TB                     = "guide.add.toolbar";
    public static final String CMD_GUIDE_DELETE                     = "guide.delete";
    public static final String CMD_GUIDE_MERGE                      = "guide.merge";
    public static final String CMD_GUIDE_PROPERTIES                 = "guide.properties";
    public static final String CMD_GUIDE_RELOAD                     = "guide.reload";
    public static final String CMD_GUIDE_RELOAD_SM                  = "guide.reload.submenu";
    public static final String CMD_GUIDE_RELOAD_ALL_TB              = "guide.reload.all.toolbar";
    public static final String CMD_GUIDE_RELOAD_ALL_SM              = "guide.reload.all.submenu";
    public static final String CMD_GUIDE_IMPORT                     = "guide.import";
    public static final String CMD_GUIDE_EXPORT                     = "guide.export";
    public static final String CMD_GUIDE_MARK_READ                  = "guide.mark.read";
    public static final String CMD_GUIDE_MARK_UNREAD                = "guide.mark.unread";
    public static final String CMD_GUIDE_MARK_READ_SM               = "guide.mark.read.submenu";
    public static final String CMD_GUIDE_MARK_UNREAD_SM             = "guide.mark.unread.submenu";
    public static final String CMD_GUIDE_MARK_ALL_READ              = "guide.mark.all.read";
    public static final String CMD_GUIDE_MARK_ALL_UNREAD            = "guide.mark.all.unread";
    public static final String CMD_GUIDE_GOTO_NEXT_UNREAD           = "guide.goto.next.unread";
    public static final String CMD_GUIDE_SORT_BY_TITLE              = "guide.sort.by.title";
    public static final String CMD_GUIDE_SUBSCRIBE_READINGLIST      = "guide.subscribe.readinglist";
    public static final String CMD_GUIDE_POST_TO_BLOG               = "guide.posttoblog";

    public static final String CMD_FEED_WHATIS                      = "feed.whatis";
    public static final String CMD_FEED_BROWSE                      = "feed.browse";
    public static final String CMD_FEED_SUBSCRIBE                   = "feed.subscribe";
    public static final String CMD_FEED_SUBSCRIBE_TB                = "feed.subscribe.toolbar";
    public static final String CMD_FEED_ADD_SMART_FEED              = "feed.subscribe.smartfeed";
    public static final String CMD_FEED_ADD_SMART_FEED_TB           = "feed.subscribe.smartfeed.toolbar";
    public static final String CMD_FEED_DELETE                      = "feed.delete";
    public static final String CMD_FEED_PROPERTIES                  = "feed.properties";
    public static final String CMD_FEED_RATING_UP                   = "feed.rating.up";
    public static final String CMD_FEED_RATING_DOWN                 = "feed.rating.down";
    public static final String CMD_FEED_MARK_READ                   = "feed.mark.read";
    public static final String CMD_FEED_MARK_UNREAD                 = "feed.mark.unread";
    public static final String CMD_FEED_RELOAD                      = "feed.reload";
    public static final String CMD_FEED_TAGS                        = "feed.tags";
    public static final String CMD_FEED_SUBSCRIBE_BY_LINK           = "feed.subscribe.by.link";
    public static final String CMD_FEED_GOTO_BY_LINK                = "feed.goto.by.link";
    public static final String CMD_FEED_POST_TO_BLOG                = "feed.posttoblog";

    public static final String CMD_FEED_LINK_MARK_READ              = "feed.link.mark.read";
    public static final String CMD_FEED_LINK_MARK_UNREAD            = "feed.link.mark.unread";
    public static final String CMD_FEED_LINK_PROPERTIES             = "feed.link.properties";
    public static final String CMD_FEED_LINK_TAGS                   = "feed.link.tags";
    public static final String CMD_FEED_LINK_POST_TO_BLOG           = "feed.link.posttoblog";
    public static final String CMD_FEED_DISCOVER                    = "feed.discover";

    public static final String CMD_ARTICLE_WHATIS                   = "article.whatis";
    public static final String CMD_ARTICLE_GOTO_NEXT_UNREAD         = "article.goto.next.unread";
    public static final String CMD_ARTICLE_GOTO_NEXT_UNREAD_TB      = "article.goto.next.unread.toolbar";
    public static final String CMD_ARTICLE_GOTO_NEXT_UNREAD_FEED    = "article.goto.next.unread.feed";
    public static final String CMD_ARTICLE_GOTO_PREV_UNREAD         = "article.goto.prev.unread";
    public static final String CMD_ARTICLE_PROPERTIES               = "article.properties";
    public static final String CMD_ARTICLE_MARK_UNREAD              = "article.mark.unread";
    public static final String CMD_ARTICLE_MARK_READ                = "article.mark.read";
    public static final String CMD_ARTICLE_SHOW_ALL                 = "article.show.all";
    public static final String CMD_ARTICLE_SHOW_ALL_TB              = "article.show.all.toolbar";
    public static final String CMD_ARTICLE_SHOW_UNREAD              = "article.show.unread";
    public static final String CMD_ARTICLE_SHOW_UNREAD_TB           = "article.show.unread.toolbar";
    public static final String CMD_ARTICLE_SHOW_PINNED              = "article.show.pinned";
    public static final String CMD_ARTICLE_SHOW_PINNED_TB           = "article.show.pinned.toolbar";
    public static final String CMD_ARTICLE_SHOW_POSITIVE            = "article.show.positive";
    public static final String CMD_ARTICLE_SHOW_POSITIVE_TB         = "article.show.positive.toolbar";
    public static final String CMD_ARTICLE_SHOW_NEGATIVE            = "article.show.negative";
    public static final String CMD_ARTICLE_SHOW_NEGATIVE_TB         = "article.show.negative.toolbar";
    public static final String CMD_ARTICLE_SHOW_NON_NEGATIVE        = "article.show.nonnegative";
    public static final String CMD_ARTICLE_SHOW_NON_NEGATIVE_TB     = "article.show.nonnegative.toolbar";
    public static final String CMD_ARTICLE_VIEWMODE_FULL            = "article.viewmode.full";
    public static final String CMD_ARTICLE_VIEWMODE_FULL_TB         = "article.viewmode.full.toolbar";
    public static final String CMD_ARTICLE_VIEWMODE_BRIEF           = "article.viewmode.brief";
    public static final String CMD_ARTICLE_VIEWMODE_BRIEF_TB        = "article.viewmode.brief.toolbar";
    public static final String CMD_ARTICLE_VIDEMODE_MINI            = "article.viewmode.mini";
    public static final String CMD_ARTICLE_VIEWMODE_MINI_TB         = "article.viewmode.mini.toolbar";
    public static final String CMD_ARTICLE_BROWSE                   = "article.browse";
    public static final String CMD_ARTICLE_BROWSE_TB                = "article.browse.toolbar";
    public static final String CMD_ARTICLE_COPY_TEXT                = "article.copy.text";
    public static final String CMD_ARTICLE_COPY_TEXT_STYLED         = "article.copy.text.styled";
    public static final String CMD_ARTICLE_COPY_LINK                = "article.copy.link";
    public static final String CMD_ARTICLE_SEND_LINK                = "article.send.link";
    public static final String CMD_ARTICLE_SAVE_IMAGE               = "article.save.image";
    public static final String CMD_ARTICLE_FONT_BIGGER              = "article.font.bigger";
    public static final String CMD_ARTICLE_FONT_SMALLER             = "article.font.smaller";
    public static final String CMD_ARTICLE_TAGS                     = "article.tags";
    public static final String CMD_ARTICLE_PIN_UNPIN                = "article.pinunpin";
    public static final String CMD_ARTICLE_POST_TO_BLOG             = "article.posttoblog";
    public static final String CMD_ARTICLE_HYPERLINK_OPEN           = "article.hyperlink.open";
    public static final String CMD_ARTICLE_HYPERLINK_SEND           = "article.hyperlink.send";
    public static final String CMD_ARTICLE_HYPERLINK_COPY           = "article.hyperlink.copy";
    public static final String CMD_ARTICLE_HYPERLINK_SAVE_AS        = "article.hyperlink.saveas";
    public static final String CMD_ARTICLE_BLOCK_IMAGE              = "article.blockimage";
    public static final String CMD_ARTICLE_DISCOVER                 = "article.discover";
    public static final String CMD_ARTICLE_TWEET_THIS               = "article.tweet.this";

    public static final String CMD_ARTICLEGROUP_MARK_UNREAD         = "articlegroup.mark.unread";
    public static final String CMD_ARTICLEGROUP_MARK_READ           = "articlegroup.mark.read";

    public static final String CMD_BB_ABOUT                         = "bb.about";
    public static final String CMD_BB_HOME                          = "bb.home";
    public static final String CMD_BB_FAQ                           = "bb.faq";
    public static final String CMD_BB_TIP_OF_THE_DAY                = "bb.tip.of.the.day";
    public static final String CMD_BB_KEYBOARD_SHORTCUTS            = "bb.keyboard.shortcuts";
    public static final String CMD_BB_SEND_FEEDBACK                 = "bb.send.feedback";
    public static final String CMD_BB_TAGS_TB                       = "bb.tags.toolbar";
    public static final String CMD_BB_CHECK_FOR_UPDATES             = "bb.check.for.updates";
    public static final String CMD_BB_ONLINEOFFLINE                 = "bb.onlineoffline";
    public static final String CMD_BB_FORGET_PASSWORDS              = "bb.forget.passwords";
    public static final String CMD_BB_PLUGIN_MANAGER                = "bb.plugin.manager";
    public static final String CMD_BB_DATABASE_COMPACT              = "bb.database.compact";
    public static final String CMD_BB_DATABASE_BACKUP               = "bb.database.backup";
    public static final String CMD_BB_ACTIVITY                      = "bb.activity";
    public static final String CMD_BB_SERVICE                       = "bb.service";
    public static final String CMD_BB_PREFERENCES                   = "bb.preferences";
    public static final String CMD_BB_CLEANUP_WIZARD                = "bb.cleanup.wizard";
    public static final String CMD_BB_SEARCH                        = "bb.search";
    public static final String CMD_BB_SEARCH_TB                     = "bb.search.toolbar";
    public static final String CMD_BB_WHATS_HOT                     = "bb.whatshot";
    public static final String CMD_BB_WHATS_HOT_TB                  = "bb.whatshot.toolbar";
    public static final String CMD_BB_BLOGSTARZ                     = "bb.blogstarz";
    public static final String CMD_BB_EXIT                          = "bb.exit";
    public static final String CMD_BB_STATISTICS                    = "bb.reports";
    public static final String CMD_BB_STATISTICS_TB                 = "bb.reports.toolbar";
    public static final String CMD_BB_IMAGE_BLOCKER                 = "bb.imageblocker";
    public static final String CMD_BB_SENTIMENT_ANALYSIS            = "bb.sentiment.analysis";

    /** Full synchronization. */
    public static final String CMD_SYNC_FULL                        = "sync.full";
    public static final String CMD_SYNC_FULL_TB                     = "sync.full.toolbar";

    /**
     * Registers actions in the Action Manager so they can be dispatched.
     *
     * @param aConnectionState connection state interface.
     */
    public void registerActions(ConnectionState aConnectionState)
    {
        ActionsMonitor.start(GlobalController.SINGLETON, aConnectionState);

        registerGuideActions();
        registerFeedActions();
        registerArticleActions();
        registerArticleGroupActions();

        ActionManager.register(CMD_BB_SEARCH, SearchAction.getInstance());
        ActionManager.register(CMD_BB_SEARCH_TB, new ActionAlias(SearchAction.getInstance()));
        ActionManager.register(CMD_BB_WHATS_HOT, WhatsHotAction.getInstance());
        ActionManager.register(CMD_BB_WHATS_HOT_TB, new ActionAlias(WhatsHotAction.getInstance()));
        ActionManager.register(CMD_BB_SERVICE, ShowServiceDialogAction.getInstance());
        ActionManager.register(CMD_BB_PREFERENCES, ShowPreferencesAction.getInstance());
        ActionManager.register(CMD_BB_EXIT, ExitAction.getInstance());
        ActionManager.register(CMD_BB_BLOGSTARZ, ShowBlogStarzDialogAction.getInstance());
        ActionManager.register(CMD_BB_ACTIVITY, ShowActivityWindowAction.getInstance());
        ActionManager.register(CMD_BB_ONLINEOFFLINE, ConnectionStateSwitchAction.getInstance());
        ActionManager.register(CMD_BB_PLUGIN_MANAGER, ManagerAction.getInstance());
        ActionManager.register(CMD_BB_DATABASE_COMPACT, DatabaseCompactAction.getInstance());
        ActionManager.register(CMD_BB_DATABASE_BACKUP, DatabaseBackupAction.getInstance());
        ActionManager.register(CMD_BB_STATISTICS, ReportsAction.getInstance());
        ActionManager.register(CMD_BB_STATISTICS_TB, new ActionAlias(ReportsAction.getInstance()));
        ActionManager.register(CMD_BB_IMAGE_BLOCKER, new ImageBlockerConfigAction());
        ActionManager.register(CMD_BB_SENTIMENT_ANALYSIS, new SettingsAction());

        ActionManager.register(CMD_BB_FAQ,
            new OpenUrlAction("http://www.blogbridge.com/category/faq/", "Show FAQ"));
        ActionManager.register(CMD_BB_HOME,
            new OpenUrlAction("http://www.blogbridge.com/", "Show Homepage"));
        ActionManager.register(CMD_BB_SEND_FEEDBACK, SendFeedbackAction.getInstance());
        ActionManager.register(CMD_BB_TIP_OF_THE_DAY, ShowTipOfTheDayAction.getInstance());
        ActionManager.register(CMD_BB_KEYBOARD_SHORTCUTS,
            new ShowTipAction(TipOfTheDay.TIP_KEYBOARD_SHORTCUTS));
        ActionManager.register(CMD_BB_ABOUT, OpenAboutDialogAction.getInstance());

        ActionManager.register(CMD_GUIDE_RELOAD_ALL_TB, UpdateAllGuidesAction.getInstance());
        ActionManager.register(CMD_GUIDE_RELOAD_ALL_SM,
                new ActionAlias(UpdateAllGuidesAction.getInstance()));

        OSXSupport.setApplicationMenu(OpenAboutDialogAction.getInstance(),
                ShowPreferencesAction.getInstance(),
                ExitAction.getInstance());

        ActionManager.register(CMD_FEED_SUBSCRIBE_BY_LINK, AddBlogByLinkAction.getInstance());
        ActionManager.register(CMD_FEED_GOTO_BY_LINK, SelectBlogByLinkAction.getInstance());

        ActionManager.register(CMD_BB_TAGS_TB, SelectiveShowTagsAction.getInstance());
        ActionManager.register(CMD_BB_CHECK_FOR_UPDATES, CheckForUpdatesAction.getInstance());

        ActionManager.register(CMD_SYNC_FULL, SyncFullAction.getInstance());
        ActionManager.register(CMD_SYNC_FULL_TB, new ActionAlias(SyncFullAction.getInstance()));

        ActionManager.register(CMD_BB_FORGET_PASSWORDS, new ForgetPasswordsAction());
    }

    /**
     * Registers articles related actions.
     */
    private static void registerArticleActions()
    {
        FeedRenderingSettings feedRenderingSettings =
            GlobalModel.SINGLETON.getGlobalRenderingSettings();

        // Factory for valueModels corresponding to bean properties of GlobalRenderingSettings
        BeanAdapter globChannelRenderingAdapter = new BeanAdapter(feedRenderingSettings, true);

        ValueModel articleFilterModelHolder = globChannelRenderingAdapter.getValueModel("articleFilter");
        ValueModel articleViewModeHolder = GlobalModel.SINGLETON.getViewModeValueModel();

        ActionManager.register(CMD_ARTICLE_WHATIS,
            new ShowTipAction(TipOfTheDay.TIP_WHAT_IS_ARTICLE));
        ActionManager.register(CMD_ARTICLE_BROWSE, BrowseArticleAction.getInstance());
        ActionManager.register(CMD_ARTICLE_BROWSE_TB,
            new ActionAlias(BrowseArticleAction.getInstance()));
        ActionManager.register(CMD_ARTICLE_COPY_TEXT, SelectedTextCopyAction.getInstance());
        ActionManager.register(CMD_ARTICLE_COPY_TEXT_STYLED, StyledTextCopyAction.getInstance());
        ActionManager.register(CMD_ARTICLE_COPY_LINK, ArticleLinkCopyAction.getInstance());
        ActionManager.register(CMD_ARTICLE_SEND_LINK, ArticleLinkSendAction.getInstance());
        ActionManager.register(CMD_ARTICLE_SAVE_IMAGE, SaveImageAction.getInstance());
        ActionManager.register(CMD_ARTICLE_GOTO_NEXT_UNREAD, GotoNextUnreadAction.getInstance());
        ActionManager.register(CMD_ARTICLE_GOTO_NEXT_UNREAD_TB,
            new ActionAlias(GotoNextUnreadAction.getInstance()));
        ActionManager.register(CMD_ARTICLE_GOTO_NEXT_UNREAD_FEED,
            GotoNextUnreadInNextFeedAction.getInstance());
        ActionManager.register(CMD_ARTICLE_GOTO_PREV_UNREAD, GotoPreviousUnreadAction.getInstance());
        ActionManager.register(CMD_ARTICLE_PROPERTIES, ShowArticlePropertiesAction.getInstance());
        ActionManager.register(CMD_ARTICLE_MARK_UNREAD, MarkArticleUnreadAction.getInstance());
        ActionManager.register(CMD_ARTICLE_MARK_READ, MarkArticleReadAction.getInstance());
        ActionManager.register(CMD_ARTICLE_FONT_BIGGER, new FontSizeBiasChangeAction(1));
        ActionManager.register(CMD_ARTICLE_FONT_SMALLER, new FontSizeBiasChangeAction(-1));

        ActionManager.register(CMD_ARTICLE_SHOW_ALL_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_ALL)));
        ActionManager.register(CMD_ARTICLE_SHOW_UNREAD_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_UNREAD)));
        ActionManager.register(CMD_ARTICLE_SHOW_PINNED_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_PINNED)));
        ActionManager.register(CMD_ARTICLE_SHOW_POSITIVE_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_POSITIVE)));
        ActionManager.register(CMD_ARTICLE_SHOW_NEGATIVE_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_NEGATIVE)));
        ActionManager.register(CMD_ARTICLE_SHOW_NON_NEGATIVE_TB,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_NON_NEGATIVE)));

        ActionManager.register(CMD_ARTICLE_SHOW_ALL,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_ALL)));
        ActionManager.register(CMD_ARTICLE_SHOW_UNREAD,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_UNREAD)));
        ActionManager.register(CMD_ARTICLE_SHOW_PINNED,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_PINNED)));
        ActionManager.register(CMD_ARTICLE_SHOW_POSITIVE,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_POSITIVE)));
        ActionManager.register(CMD_ARTICLE_SHOW_NEGATIVE,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_NEGATIVE)));
        ActionManager.register(CMD_ARTICLE_SHOW_NON_NEGATIVE,
            ToggleAction.createRadio(articleFilterModelHolder,
                new Integer(IFeedDisplayConstants.FILTER_NON_NEGATIVE)));

        ActionManager.register(CMD_ARTICLE_VIDEMODE_MINI,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_MINIMAL));
        ActionManager.register(CMD_ARTICLE_VIEWMODE_BRIEF,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_BRIEF));
        ActionManager.register(CMD_ARTICLE_VIEWMODE_FULL,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_FULL));

        ActionManager.register(CMD_ARTICLE_VIEWMODE_MINI_TB,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_MINIMAL));
        ActionManager.register(CMD_ARTICLE_VIEWMODE_BRIEF_TB,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_BRIEF));
        ActionManager.register(CMD_ARTICLE_VIEWMODE_FULL_TB,
            ToggleAction.createRadio(articleViewModeHolder,
                FeedRenderingSettings.VIEW_MODE_FULL));

        ActionManager.register(CMD_ARTICLE_TAGS, ShowArticleTagsAction.getInstance());
        ActionManager.register(CMD_ARTICLE_PIN_UNPIN, PinUnpinArticleAction.getInstance());

        ActionManager.register(CMD_ARTICLE_HYPERLINK_OPEN, HyperLinkOpenAction.getInstance());
        ActionManager.register(CMD_ARTICLE_HYPERLINK_COPY, HyperLinkCopyAction.getInstance());
        ActionManager.register(CMD_ARTICLE_HYPERLINK_SAVE_AS, HyperLinkSaveAsAction.getInstance());
        ActionManager.register(CMD_ARTICLE_HYPERLINK_SEND, HyperLinkEmailAction.getInstance());

        ActionManager.register(CMD_ARTICLE_POST_TO_BLOG, PostToBlogAction.getInstanceForArticle());
        ActionManager.register(CMD_ARTICLE_TWEET_THIS, TweetThisAction.getInstance());

        ActionManager.register(CMD_ARTICLE_BLOCK_IMAGE, BlockImageAction.getInstance());
        ActionManager.register(CMD_ARTICLE_DISCOVER, DiscoverInArticlesAction.getInstance());
    }

    /**
     * Registers article group actions.
     */
    private static void registerArticleGroupActions()
    {
        ActionManager.register(CMD_ARTICLEGROUP_MARK_READ, new MarkArticlesGroupAction(true));
        ActionManager.register(CMD_ARTICLEGROUP_MARK_UNREAD, new MarkArticlesGroupAction(false));
    }

    /**
     * Registers actions related to feeds.
     */
    private static void registerFeedActions()
    {
        ActionManager.register(CMD_FEED_WHATIS, new ShowTipAction(TipOfTheDay.TIP_WHAT_IS_FEED));
        ActionManager.register(CMD_FEED_BROWSE, OpenBlogHomeAction.getInstance());
        ActionManager.register(CMD_FEED_SUBSCRIBE, AddDirectFeedAction.getInstance());
        ActionManager.register(CMD_FEED_SUBSCRIBE_TB,
            new ActionAlias(AddDirectFeedAction.getInstance()));
        ActionManager.register(CMD_FEED_ADD_SMART_FEED, AddSmartFeedAction.getInstance());
        ActionManager.register(CMD_FEED_ADD_SMART_FEED_TB,
            new ActionAlias(AddSmartFeedAction.getInstance()));
        ActionManager.register(CMD_FEED_DELETE, DeleteFeedAction.getInstance());
        ActionManager.register(CMD_BB_CLEANUP_WIZARD, CleanupFeedAction.getInstance());
        ActionManager.register(CMD_FEED_PROPERTIES, ShowFeedPropertiesAction.getInstance());
        ActionManager.register(CMD_FEED_RATING_UP, RatingHighAction.getInstance());
        ActionManager.register(CMD_FEED_RATING_DOWN, RatingLowAction.getInstance());
        ActionManager.register(CMD_FEED_MARK_READ, MarkFeedAsReadAction.getInstance());
        ActionManager.register(CMD_FEED_MARK_UNREAD, MarkFeedAsUnreadAction.getInstance());
        ActionManager.register(CMD_FEED_RELOAD, UpdateSelectedFeedsAction.getInstance());
        ActionManager.register(CMD_FEED_TAGS, ShowFeedTagsAction.getInstance());
        ActionManager.register(CMD_FEED_POST_TO_BLOG, PostToBlogAction.getInstanceForFeed());

        ActionManager.register(CMD_FEED_LINK_MARK_READ, new FeedLinkMarkFeedAsReadAction());
        ActionManager.register(CMD_FEED_LINK_MARK_UNREAD, new FeedLinkMarkFeedAsUnreadAction());
        ActionManager.register(CMD_FEED_LINK_PROPERTIES, new FeedLinkShowFeedPropertiesAction());
        ActionManager.register(CMD_FEED_LINK_TAGS, new FeedLinkShowFeedTagsAction());
        ActionManager.register(CMD_FEED_LINK_POST_TO_BLOG, FeedLinkPostToBlogAction.getInstance());

        ActionManager.register(CMD_FEED_DISCOVER, DiscoverInFeedsAction.getInstance());
    }

    /**
     * Registers actions related to guides.
     */
    private static void registerGuideActions()
    {
        ActionManager.register(CMD_GUIDE_WHATIS, new ShowTipAction(TipOfTheDay.TIP_WHAT_IS_GUIDE));
        ActionManager.register(CMD_GUIDE_ADD, AddGuideAction.getInstance());
        ActionManager.register(CMD_GUIDE_ADD_TB, new ActionAlias(AddGuideAction.getInstance()));
        ActionManager.register(CMD_GUIDE_DELETE, DeleteGuideAction.getInstance());
        ActionManager.register(CMD_GUIDE_MERGE, MergeGuidesAction.getInstance());
        ActionManager.register(CMD_GUIDE_PROPERTIES, GuidePropertiesAction.getInstance());
        ActionManager.register(CMD_GUIDE_IMPORT, ImportGuidesAction.getInstance());
        ActionManager.register(CMD_GUIDE_EXPORT, ExportGuidesAction.getInstance());
        ActionManager.register(CMD_GUIDE_MARK_READ, MarkGuideReadAction.getInstance());
        ActionManager.register(CMD_GUIDE_MARK_UNREAD, MarkGuideUnreadAction.getInstance());
        ActionManager.register(CMD_GUIDE_MARK_READ_SM, new ActionAlias(MarkGuideReadAction.getInstance()));
        ActionManager.register(CMD_GUIDE_MARK_UNREAD_SM, new ActionAlias(MarkGuideUnreadAction.getInstance()));
        ActionManager.register(CMD_GUIDE_MARK_ALL_READ, MarkAllGuidesReadAction.getInstance());
        ActionManager.register(CMD_GUIDE_MARK_ALL_UNREAD, MarkAllGuidesUnreadAction.getInstance());
        ActionManager.register(CMD_GUIDE_RELOAD, UpdateGuideAction.getInstance());
        ActionManager.register(CMD_GUIDE_RELOAD_SM, new ActionAlias(UpdateGuideAction.getInstance()));
        ActionManager.register(CMD_GUIDE_GOTO_NEXT_UNREAD, GotoNextGuideWithUnreadAction.getInstance());
        ActionManager.register(CMD_GUIDE_SORT_BY_TITLE, SortGuidesByTitleAction.getInstance());
        ActionManager.register(CMD_GUIDE_SUBSCRIBE_READINGLIST, new SubscribeToReadingListAction());
        ActionManager.register(CMD_GUIDE_POST_TO_BLOG, PostToBlogAction.getInstanceForGuide());
    }

    /**
     * Private constructor to ensure singleton.
     */
    private ActionsTable()
    {
    }

    /**
     * Returns instance of the table.
     *
     * @return instance.
     */
    public static ActionsTable getInstance()
    {
        return INSTANCE;
    }
}