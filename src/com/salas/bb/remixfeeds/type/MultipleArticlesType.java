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
// $Id: MultipleArticlesType.java,v 1.6 2008/04/02 14:58:44 spyromus Exp $
//

package com.salas.bb.remixfeeds.type;

import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.FeatureManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * Post to blog type with multiple articles.
 */
abstract class MultipleArticlesType implements IType
{
    enum Post { ALL, UNREAD, PINNED }

    /**
     * Returns the list of articles taken from all feeds.
     *
     * @param feeds feeds.
     *
     * @return articles.
     */
    protected Set<IArticle> flatten(IFeed[] feeds)
    {
        Set<IArticle> articles = new LinkedHashSet<IArticle>();

        for (IFeed feed : feeds)
        {
            articles.addAll(Arrays.asList(feed.getArticles()));
        }

        return articles;
    }

    /**
     * Returns <code>TRUE</code> if action of this type is available.
     *
     * @return <code>TRUE</code> if action of this type is available.
     */
    public boolean isAvailable()
    {
        GlobalController controller = GlobalController.SINGLETON;
        GlobalModel model = controller.getModel();
        FeatureManager fm = controller.getFeatureManager();

        boolean ptbEnabled = fm.isPtbEnabled();
        boolean ptbAdvanced = fm.isPtbAdvanced();
        boolean blogRecordsPresent = model.getUserPreferences().getBloggingPreferences().getBlogsCount() > 0;

        return ptbEnabled && ptbAdvanced && blogRecordsPresent;
    }

    /**
     * Says if dynamic template change is supported by this type (with SHIFT-click over the PTB command).
     *
     * @return TRUE if it is.
     */
    public boolean isTemplateChangeSupported()
    {
        return true;
    }

    /**
     * Takes the list of articles and the mode and prepares the text for
     * the post. If there are unread and/or pinned articles, the dialog appears
     * asking what kind of articles to choose.
     *
     * @param articles  all articles to choose from.
     *
     * @return text.
     */
    protected Collection<IArticle> chooseArticles(Collection<IArticle> articles)
    {
        int all = articles.size();
        int unread = 0;
        int pinned = 0;
        for (IArticle article : articles)
        {
            if (!article.isRead()) unread++;
            if (article.isPinned()) pinned++;
        }

        // Ask for what to publish
        Post toPost = whatToPost(all, unread, pinned);
        if (toPost == null) return new ArrayList<IArticle>(0);

        // Select articles we need
        Collection<IArticle> arts = new LinkedHashSet<IArticle>();
        switch (toPost)
        {
            case UNREAD:
                for (IArticle article : articles) if (!article.isRead()) arts.add(article);
                break;
            case PINNED:
                for (IArticle article : articles) if (article.isPinned()) arts.add(article);
                break;
            default:
                arts = articles;
                break;
        }

        return arts;
    }

    /**
     * Asks user what to post and returns the answer.
     *
     * @param all       all articles.
     * @param unread    unread articles.
     * @param pinned    pinned articles.
     *
     * @return what to publish.
     */
    private Post whatToPost(int all, int unread, int pinned)
    {
        if (unread == 0 && pinned == 0) return Post.ALL;
        if ((unread == 0 && (all == pinned)) ||
            (pinned == 0 && (unread == all)) ||
            (pinned == all && unread == all)) return Post.ALL;

        ModeSelectionDialog dialog = new ModeSelectionDialog(
            GlobalController.SINGLETON.getMainFrame(),
            all, unread, pinned);

        return dialog.ask();
    }

    /**
     * The dialog to ask what articles to use in the post.
     */
    private static class ModeSelectionDialog extends AbstractDialog
    {
        private final int all;
        private final int unread;
        private final int pinned;

        private ValueModel postMode;

        /**
         * Creates the dialog.
         *
         * @param frame parent frame.
         */
        private ModeSelectionDialog(Frame frame, int all, int unread, int pinned)
        {
            super(frame);

            this.pinned = pinned;
            this.unread = unread;
            this.all = all;

            postMode = new ValueHolder(Post.ALL);
        }

        @Override
        protected JComponent buildHeader()
        {
            return new HeaderPanelExt(
                Strings.message("ptb.multiple.dialog.title"),
                Strings.message("ptb.multiple.dialog.header"));
        }

        /**
         * Returns the panel.
         *
         * @return panel.
         */
        protected JComponent buildContent()
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(buildMainPanel(), BorderLayout.CENTER);
            panel.add(buildButtonBar(), BorderLayout.SOUTH);
            return panel;
        }

        /**
         * Returns the main panel.
         *
         * @return panel.
         */
        private Component buildMainPanel()
        {
            JRadioButton rbAll = ComponentsFactory.createRadioButton(
                MessageFormat.format(Strings.message("ptb.multiple.dialog.all"), all),
                new ToggleButtonAdapter(postMode, Post.ALL, null));
            JRadioButton rbUnread = ComponentsFactory.createRadioButton(
                MessageFormat.format(Strings.message("ptb.multiple.dialog.unread"), unread),
                new ToggleButtonAdapter(postMode, Post.UNREAD, Post.ALL));
            rbUnread.setEnabled(unread > 0);
            JRadioButton rbPinned = ComponentsFactory.createRadioButton(
                MessageFormat.format(Strings.message("ptb.multiple.dialog.pinned"), pinned),
                new ToggleButtonAdapter(postMode, Post.PINNED, Post.ALL));
            rbPinned.setEnabled(pinned > 0);

            BBFormBuilder builder = new BBFormBuilder("p");
            builder.setDefaultDialogBorder();

            builder.append(rbAll);
            builder.append(rbUnread);
            builder.append(rbPinned);

            return builder.getPanel();
        }

        /**
         * Returns the buttons bar.
         *
         * @return bar.
         */
        private Component buildButtonBar()
        {
            return buildButtonBarWithOKCancel();
        }

        public Post ask()
        {
            super.open();
            return hasBeenCanceled() ? null : (Post)postMode.getValue();
        }
    }
}
