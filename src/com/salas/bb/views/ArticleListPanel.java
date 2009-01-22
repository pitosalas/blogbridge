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
// $Id: ArticleListPanel.java,v 1.52 2008/04/02 14:31:28 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.action.ToggleAction;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IFeedListener;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.sentiments.SentimentsFeature;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.CoolInternalFrame;
import com.salas.bb.utils.uif.JComboBoxActionItem;
import com.salas.bb.utils.uif.JumplessScrollPane;
import com.salas.bb.views.feeds.CompositeFeedDisplay;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.settings.HTMLFeedDisplayConfig;
import com.salas.bb.views.settings.ImageFeedDisplayConfig;
import com.salas.bb.views.settings.RenderingManager;
import com.salas.bb.views.settings.RenderingSettingsNames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

/**
 * Display and work on the Articles of the selected Channel.
 */
public final class ArticleListPanel extends CoolInternalFrame implements PropertyChangeListener
{
    private final IFeedDisplay feedDisplay;
    private final JToolBar     subToolBar;

    /** Page number model. */
    private ValueModel pageModel;

    /**
     * Constructs panel with list of articles for selected channel.
     */
    public ArticleListPanel()
    {
        super(Strings.message("panel.articles"));

        setPreferredSize(new Dimension(300, 100));

        RenderingManager.addPropertyChangeListener(RenderingSettingsNames.THEME, this);

        // Register own controller listener
        GlobalController.SINGLETON.addControllerListener(new ControllerListener());
        PropertyChangeListener articleViewChangeHandler = new ArticleViewChangeHandler();
        GlobalModel.SINGLETON.getGlobalRenderingSettings().addPropertyChangeListener(
            "articleViewMode", articleViewChangeHandler);

        // Set the sub-toolbar (right justified in the CoolInternalFrame)
        pageModel = new ValueHolder(0);
        pageModel.addValueChangeListener(new PageModelListener());
        ValueModel pageCountModel = new ValueHolder(0);
        subToolBar = createSubtoolbar();
        setHeaderControl(subToolBar);

        // Create the list that will contain the channels. Uses a custom
        // renderer.
        HTMLFeedDisplayConfig htmlConfig = new HTMLFeedDisplayConfig();
        ImageFeedDisplayConfig imageConfig = new ImageFeedDisplayConfig();
        RenderingManager.addPropertyChangeListener(htmlConfig.getRenderingManagerListener());
        RenderingManager.addPropertyChangeListener(imageConfig.getRenderingManagerListener());

        // Get page size user preferences and subscribe to updates
        UserPreferences preferences = GlobalModel.SINGLETON.getUserPreferences();
        PropertyAdapter paPageSize = new PropertyAdapter(preferences, UserPreferences.PROP_PAGE_SIZE, true);
        paPageSize.addPropertyChangeListener(new PageSizeListener());

        feedDisplay = new CompositeFeedDisplay(htmlConfig, imageConfig, pageModel, pageCountModel);
        feedDisplay.setPageSize(preferences.getPageSize());

        // Setup data-adapter
        FeedDisplayAdapter adapter = new FeedDisplayAdapter(feedDisplay);
        GlobalController.SINGLETON.addControllerListener(adapter);

        // Create scroll pane and put list in it
        JScrollPane listSP = new JumplessScrollPane(feedDisplay.getComponent());
        listSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        listSP.setFocusable(false);
        listSP.setBorder(null);

        // Register viewport to enable correct scrolling
        feedDisplay.setViewport(listSP.getViewport());

        JPanel content = new JPanel(new BorderLayout());
        content.add(new PagingPanel(pageModel, pageCountModel), BorderLayout.NORTH);
        content.add(listSP, BorderLayout.CENTER);
        
        // Register scroll pane
        setContent(content);

        setFeedTitle(null);
    }

    /**
     * Returns articles list component.
     *
     * @return articles list.
     */
    public IFeedDisplay getFeedView()
    {
        return feedDisplay;
    }

    /**
     * Creates a sub-toolbar with two models.
     *
     * @return sub-toolbar.
     */
    private JToolBar createSubtoolbar()
    {
        final JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setAlignmentY(0.0f);
        toolbar.setOpaque(false);
        toolbar.setBorder(null);
        toolbar.setBorderPainted(false);

        ToolBarBuilder subTBbldr = new ToolBarBuilder(toolbar);

        final JComboBox comboBox = new JComboBox();

        // Indexes of items in this list should correspond to the values of
        // filter setting from FeedRenderingSettings class (FILTER_XYZ constants)
        createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_ALL_TB);
        createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_UNREAD_TB);
        createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_PINNED_TB);

        if (SentimentsFeature.isAvailable())
        {
            createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_POSITIVE_TB);
            createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_NEGATIVE_TB);
            createNAddComboBoxAction(comboBox, ActionsTable.CMD_ARTICLE_SHOW_NON_NEGATIVE_TB);
        }

        int filter = Math.min(RenderingManager.getArticleFilter(), comboBox.getItemCount() - 1);
        comboBox.addActionListener(JComboBoxActionItem.getComboBoxListener());
        comboBox.setOpaque(!SystemUtils.IS_OS_MAC);
        comboBox.setFocusable(false);
        comboBox.setSelectedIndex(filter);

        // Listen to changes of FILTER property and update the selection
        RenderingManager.addPropertyChangeListener(RenderingSettingsNames.ARTICLE_FILTER,
            new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                comboBox.setSelectedIndex(RenderingManager.getArticleFilter());
            }
        });

        JPanel comboboxPanel = new JPanel(new FormLayout("10px, p", SystemUtils.IS_OS_MAC ? "1px, p" : "0, 20px"));
        CellConstraints cc = new CellConstraints();
        comboboxPanel.add(comboBox, cc.xy(2, 2));

        // Mac OS Aqua look has a bug with JComboBox sizing depending on the font size
        // The report: http://lists.apple.com/archives/Java-dev/2005/Jan/msg00510.html
        //   and the answer: http://lists.apple.com/archives/Java-dev/2005/Jan/msg00511.html
        //
        // The page with screenshots of how the Quaqua library deals with the JComboBox
        // look through the custom component look painting (not a native component):
        // http://www.randelshofer.ch/quaqua/guide/jcombobox.html

        if (!SystemUtils.IS_OS_MAC)
        {
            comboBox.setFont(new Font("Tahoma", Font.PLAIN, 10));
        }

        comboboxPanel.setOpaque(false);
        
        subTBbldr.add(comboboxPanel);
        subTBbldr.addGap();
        subTBbldr.add(new ViewModeSelector(GlobalModel.SINGLETON.getViewModeValueModel()));

        return toolbar;
    }

    private void createNAddComboBoxAction(JComboBox box, String cmdcode)
    {
        JToggleButton newitem = new JComboBoxActionItem((ToggleAction)ActionManager.get(cmdcode));
        box.addItem(newitem);
    }

    /**
     * Called when the theme changes.
     *
     * @param evt event object.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        updateUI();
    }

    // Changes title text.
    private void setFeedTitle(final IFeed feed)
    {
        String text = (feed == null ? Strings.message("panel.articles.no.feed.selected") : feed.getTitle());
        if (text == null) text = Strings.message("untitled");

        setSubtitle(MessageFormat.format(Strings.message("panel.in"), text));
    }

    /**
     * Listens for <code>GlobalController</code> events in order to get when channel selected. This
     * information is necessary to set correct articles list title.
     */
    private final class ControllerListener extends ControllerAdapter implements IFeedListener
    {
        private IFeed selected;

        /**
         * Invoked after application changes the feed.
         *
         * @param feed feed to which we are switching.
         */
        public void feedSelected(final IFeed feed)
        {
            if (feed != selected)
            {
                if (selected != null) selected.removeListener(this);
                selected = feed;
                if (selected != null) selected.addListener(this);

                // Set the first page
                pageModel.setValue(0);

                setFeedTitle(selected);
            }
        }

        /**
         * Invoked when new article has been added to the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleAdded(IFeed feed, IArticle article)
        {
        }

        /**
         * Invoked when the article has been removed from the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleRemoved(IFeed feed, IArticle article)
        {
        }

        /**
         * Invoked when the property of the feed has been changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(final IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (property.equals(IFeed.PROP_TITLE))
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        setFeedTitle(feed);
                    }
                });
            }
        }
    }

    /**
     * Listener of property changes in article view.
     */
    private class ArticleViewChangeHandler implements PropertyChangeListener
    {
        /**
         * Repaints sub-toolbar on any changes.
         *
         * @param e property change event object.
         */
        public void propertyChange(PropertyChangeEvent e)
        {
            subToolBar.repaint();
        }
    }

    /**
     * Looks at the page model and notifies the feed display on changes in page number.
     */
    private class PageModelListener implements PropertyChangeListener
    {
        /**
         * Invoked when the page number changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            feedDisplay.setPage((Integer)evt.getNewValue());
        }
    }

    /**
     * Looks at the page size model.
     */
    private class PageSizeListener implements PropertyChangeListener
    {
        /**
         * Invoked when the page number changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (PropertyAdapter.PROPERTYNAME_VALUE.equals(evt.getPropertyName()))
            {
                feedDisplay.setPageSize((Integer)evt.getNewValue());
            }
        }
    }

    /**
     * Paging panel that comes out when the paging is available.
     */
    private class PagingPanel extends JPanel implements PropertyChangeListener
    {
        private final ValueModel pageModel;
        private final ValueModel pageCountModel;
        private JLabel lbInfo;

        /**
         * Creates the panel.
         *
         * @param pageModel         page number model.
         * @param pageCountModel    page count model.
         */
        public PagingPanel(ValueModel pageModel, ValueModel pageCountModel)
        {
            this.pageModel = pageModel;
            this.pageCountModel = pageCountModel;

            lbInfo = new JLabel();
            RenderingManager.addPropertyChangeListener(RenderingSettingsNames.THEME, this);
            PagingControl paging = new PagingControl(pageModel, pageCountModel);

            setLayout(new FormLayout("4dlu, p, 7dlu:grow, p, 4dlu", "2dlu, p, 2dlu"));

            CellConstraints cc = new CellConstraints();
            add(lbInfo, cc.xy(2, 2));
            add(paging, cc.xy(4, 2));

            // Bind the panel
            pageModel.addValueChangeListener(this);
            pageCountModel.addValueChangeListener(this);

            onPageConfigChange();
            onThemeChange();

            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        /**
         * Updates the fonts and colors.
         */
        private void onThemeChange()
        {
            lbInfo.setFont(RenderingManager.getArticleTitleFont());
            setBackground(Color.decode("#9cc9ff"));
        }

        /**
         * Updates the text of the info.
         */
        private void onPageConfigChange()
        {
            int pages = (Integer)pageCountModel.getValue();

            setVisible(pages > 1);

            String[] msgs = Strings.slices("pagination.page.of");
            lbInfo.setText(msgs[0] + " " + ((Integer)pageModel.getValue() + 1) + " " + msgs[1] + " " + pages);
        }

        /**
         * Invoked when the page number of the number of pages changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String prop = evt.getPropertyName();
            if (ValueHolder.PROPERTYNAME_VALUE.equals(prop))
            {
                onPageConfigChange();
            } else if (RenderingSettingsNames.THEME.equals(prop))
            {
                onThemeChange();
            }
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e)
        {
            feedDisplay.getComponent().dispatchEvent(e);
        }
    }
}