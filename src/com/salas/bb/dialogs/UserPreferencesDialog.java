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
// $Id: UserPreferencesDialog.java,v 1.110 2008/10/23 08:14:25 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.AbstractFrame;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.*;
import com.salas.bb.domain.FeedClass;
import com.salas.bb.domain.FeedsSortOrder;
import com.salas.bb.domain.GuideClass;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.prefs.ViewModePreferences;
import com.salas.bb.remixfeeds.prefs.BloggingPreferencesPanel;
import com.salas.bb.sentiments.SentimentsFeature;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.SpinnerModelAdapter;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.utils.uif.*;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingSettingsNames;
import com.salas.bb.twitter.TwitterPreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builds the User Preferences dialog.
 */
public final class UserPreferencesDialog extends AbstractDialog
{
    private final GlobalModel   model;
    private final Pages         pages;
    private AdvancedPreferencesPanel pnlAdvanced;

    private final AtomicBoolean feedsFilterChanged = new AtomicBoolean(false);
    private final AtomicBoolean guidesFilterChanged = new AtomicBoolean(false);

    private static final ColorListCellRenderer colorListCellRenderer =
        new ColorListCellRenderer(Strings.message("userprefs.tab.feeds.filter.hidden"));

    /**
     * Creates user preferences dialog.
     *
     * @param owner owning frame.
     * @param aModel global Model of BlogBridge.
     */
    public UserPreferencesDialog(AbstractFrame owner, GlobalModel aModel)
    {
        super(owner);
        this.model = aModel;
        pages = new Pages();
    }

    // Building *************************************************************

    /**
     * Builds and answers the preference's header.
     *
     * @return JComponent header of dialog box
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("userprefs.dialog.title"),
            Strings.message("userprefs.dialog.header"),
            IconSource.getIcon(ResourceID.ICON_PREFERENCES));
    }

    /**
     * Builds and answers the preference's content pane.
     *
     * @return JComponent of content part of the dialog box
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildNavPanel(), BorderLayout.NORTH);
        content.add(buildMainPane(), BorderLayout.CENTER);
        content.add(buildButtonsBar(), BorderLayout.SOUTH);
        return content;
    }

    private Component buildNavPanel()
    {
        JComboBox selector = pages.getSelector();
        JButton btnPrev = new JButton(Strings.message("userprefs.dialog.nav.prev"));
        btnPrev.addActionListener(new MoveSelection(selector, false));
        JButton btnNext = new JButton(Strings.message("userprefs.dialog.nav.next"));
        btnNext.addActionListener(new MoveSelection(selector, true));

        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, p, 2dlu, p");

        builder.append(selector);
        builder.append(btnPrev);
        builder.append(btnNext);

        JPanel panel = builder.getPanel();
        Insets ins = Borders.DIALOG_BORDER.getBorderInsets(null);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, ins.bottom, 0));
        return panel;
    }

    /**
     * Moves selection in the combo-box forward/backwards.
     */
    private static class MoveSelection implements ActionListener
    {
        private JComboBox box;
        private boolean forward;

        /**
         * Creates action.
         *
         * @param box       box.
         * @param forward   direction.
         */
        public MoveSelection(JComboBox box, boolean forward)
        {
            this.box = box;
            this.forward = forward;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            int sel = box.getSelectedIndex();
            int siz = box.getItemCount();

            if (forward)
            {
                sel++;
                if (sel == siz) sel = 0;
            } else
            {
                sel--;
                if (sel < 0) sel = siz - 1;
            }

            box.setSelectedIndex(sel);
        }
    }
    private JComponent buildButtonsBar()
    {
        return buildButtonBarWithOKCancelApply();
    }

    /**
     * Builds content pane.
     *
     * @return pane.
     */
    private JComponent buildMainPane()
    {
        final UserPreferences prefs = model.getUserPreferences();
        final StarzPreferences starzPreferences = model.getStarzPreferences();
        final FeedRenderingSettings feedRS = model.getGlobalRenderingSettings();

        final ValueModel trigger = getTriggerChannel();

        GeneralPreferencesPanel generalPanel = new GeneralPreferencesPanel(prefs, feedRS, trigger);
        pnlAdvanced = new AdvancedPreferencesPanel(prefs, starzPreferences, feedRS, trigger);
        TagsPreferencesPanel tagsPanel = new TagsPreferencesPanel(prefs, model.getServicePreferences(), trigger);

        pages.addPage(Strings.message("userprefs.tab.general"), generalPanel);
        pages.addPage(Strings.message("userprefs.tab.guides"), new GuidesPanel(feedRS, trigger, guidesFilterChanged));
        pages.addPage(Strings.message("userprefs.tab.feeds"), new FeedsPanel(feedRS, prefs, trigger, feedsFilterChanged));
        pages.addPage(Strings.message("userprefs.tab.articles"), new ArticlesPanel(prefs, trigger));
        pages.addPage(Strings.message("userprefs.tab.tags"), tagsPanel);
        pages.addPage(Strings.message("userprefs.tab.readinglists"), new ReadingListsPanel(prefs, trigger));
        pages.addPage(Strings.message("userprefs.tab.blogs"),
            new BloggingPreferencesPanel(this, trigger, prefs.getBloggingPreferences()));
        pages.addPage(Strings.message("userprefs.tab.twitter"),
            new TwitterPreferencesPanel(this, trigger, prefs.getTwitterPreferences()));
        pages.addPage(Strings.message("userprefs.tab.notifications"), new NotificationsPanel(prefs, trigger));
        pages.addPage(Strings.message("userprefs.tab.advanced"), pnlAdvanced);

        pages.setBorder(BorderFactory.createEtchedBorder());

        // Restore last selected page
        int i = prefs.getSelectedPrefsPage();
        if (i > -1 && i < pages.getSelector().getItemCount()) pages.getSelector().setSelectedIndex(i);

        return pages;
    }

    /**
     * Opens dialog. We intercept it to save the last visible page index.
     */
    public void open()
    {
        super.open();

        // Store last selected page
        model.getUserPreferences().setSelectedPrefsPage(pages.getSelector().getSelectedIndex());
    }

    protected void resizeHook(JComponent comp)
    {
        int width = SystemUtils.IS_OS_MAC ? 565 : (int)(460 * Constants.SIZE_FACTOR);
        int height = SystemUtils.IS_OS_MAC ? 585 : (int)(500 * Constants.SIZE_FACTOR);
        comp.setPreferredSize(new Dimension(width, height));
    }

    // Misc *****************************************************************

    /**
     * Performs repacking of window on accept.
     */
    public void doApply()
    {
        String msg = checkValidity();

        if (msg == null)
        {
            super.doApply();

            if (feedsFilterChanged.get())
            {
                model.getGuidesSet().invalidateFeedVisibilityCaches();
                model.getGuideModel().filterChanged();
            }

            feedsFilterChanged.set(false);
            guidesFilterChanged.set(false);

            pack();

            GlobalModel.touchPreferences();
        } else
        {
            JOptionPane.showMessageDialog(getParent(), msg, getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks the validity of the properties.
     *
     * @return the message or <code>NULL</code> if fine.
     */
    private String checkValidity()
    {
        return pnlAdvanced.checkValidity();
    }

    /**
     * Closes the window.
     */
    protected void doCloseWindow()
    {
        doCancel();
    }

    /**
     * Basic preferences panel.
     */
    private static class PreferencesPanel extends JPanel
    {
        final ValueModel trigger;

        /**
         * Creates the panel.
         *
         * @param aTrigger trigger.
         */
        public PreferencesPanel(ValueModel aTrigger)
        {
            trigger = aTrigger;
        }

    }

    /**
     * Pages component knows how to register and show titled pages. It has selector sub-component
     * based on combo-box (drop-down list).
     */
    private static class Pages extends JPanel
    {
        private JComboBox selector;

        /**
         * Creates pages control.
         */
        public Pages()
        {
            setLayout(new BorderLayout());

            selector = new JComboBox();
            selector.addItemListener(new PageSelectedListener());
        }

        /**
         * Returns page selector.
         *
         * @return page selector.
         */
        public JComboBox getSelector()
        {
            return selector;
        }

        /**
         * Adds page to the selector.
         *
         * @param title title.
         * @param page  page.
         */
        public void addPage(String title, JComponent page)
        {
            selector.addItem(new Page(title, page));
        }

        /**
         * Invoked when the page gets selected.
         *
         * @param page page.
         */
        public void onPageSelected(JComponent page)
        {
            removeAll();
            add(page, BorderLayout.CENTER);

            validate();
            repaint();
        }

        /**
         * Listens to page selection events and sends them to the main class.
         */
        private class PageSelectedListener implements ItemListener
        {
            /**
             * Invoked when another page gets selected.
             *
             * @param e event.
             */
            public void itemStateChanged(ItemEvent e)
            {
                JComponent page = null;
                Page pageItem = (Page)selector.getSelectedItem();
                if (pageItem != null) page = pageItem.getPage();
                onPageSelected(page);
            }
        }

        /** Single titled page. */
        private static class Page
        {
            private String title;
            private JComponent page;

            /**
             * Creates titled page.
             *
             * @param title title.
             * @param page  page.
             */
            public Page(String title, JComponent page)
            {
                this.title = title;
                this.page = page;
            }

            /**
             * Returns page title.
             *
             * @return title.
             */
            public String toString()
            {
                return title;
            }

            /**
             * Returns page component.
             *
             * @return component.
             */
            public JComponent getPage()
            {
                return page;
            }
        }
    }
    /**
     * Guides preferences panel.
     */
    private static class GuidesPanel extends PreferencesPanel
    {
        /**
         * Creates panel.
         *
         * @param aFeedRS       feed rendering settings.
         * @param aTrigger      trigger.
         * @param filterChanged atomic boolean flag.
         */
        public GuidesPanel(FeedRenderingSettings aFeedRS, ValueModel aTrigger, AtomicBoolean filterChanged)
        {
            super(aTrigger);

            JCheckBox chBigIconInGuides = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.guides.use.large.icons"),
                aFeedRS, RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES, trigger);
            JCheckBox chShowUnreadInGuides = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.guides.show.unread.counter"),
                aFeedRS, "showUnreadInGuides", trigger);
            JCheckBox chShowIconInGuides = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.guides.show.icon"),
                aFeedRS, RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING, trigger);
            JCheckBox chShowTextInGuides = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.guides.show.name"),
                aFeedRS, RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING, trigger);

            new MandatoryCheckBoxController(chShowIconInGuides, chShowTextInGuides);

            GuideDisplayModeManager gdmm = GuideDisplayModeManager.getInstance();
            JComboBox cbFilterRead = createFilterComboBox(GuideClass.READ, gdmm, filterChanged, trigger);

            // Layout
            BBFormBuilder builder =
                new BBFormBuilder("7dlu, 7dlu, 35dlu, 40dlu, 2dlu, 90dlu, 2dlu, p:grow", this);
            builder.setDefaultDialogBorder();

            builder.setLeadingColumnOffset(1);
            builder.appendSeparator(Strings.message("userprefs.tab.feeds.filtering"));
            builder.append(Strings.message("userprefs.fully.read"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT).setLabelFor(cbFilterRead);
            builder.append(cbFilterRead);
            builder.nextLine();

            builder.appendSeparator(Strings.message("userprefs.tab.guides.show"));
            builder.setLeadingColumnOffset(1);
            builder.append(chShowIconInGuides, 7);
            builder.setLeadingColumnOffset(2);
            builder.append(chBigIconInGuides, 6);
            builder.setLeadingColumnOffset(1);
            builder.append(chShowTextInGuides, 7);
            builder.append(chShowUnreadInGuides, 7);
        }
    }

    /**
     * Feeds preferences panel.
     */
    private static class FeedsPanel extends PreferencesPanel
    {
        private static final Integer[] SORTING_ORDER = {
            FeedsSortOrder.ALPHABETICAL,
            FeedsSortOrder.RATING,
            FeedsSortOrder.READ,
            FeedsSortOrder.INVALIDNESS,
            FeedsSortOrder.VISITS
        };

        private static final String[] SORTING_CLASS_NAMES = {
            getFeedSortOrderName(FeedsSortOrder.ALPHABETICAL),
            getFeedSortOrderName(FeedsSortOrder.RATING),
            getFeedSortOrderName(FeedsSortOrder.READ),
            getFeedSortOrderName(FeedsSortOrder.INVALIDNESS),
            getFeedSortOrderName(FeedsSortOrder.VISITS)
        };

        private JLabel          lbSortFirstBy;
        private JLabel          lbSortThenBy;

        private JCheckBox       chSortingEnabled;
        private JComboBox       cbSortClass1;
        private JCheckBox       chReverseSort1;
        private JComboBox       cbSortClass2;
        private JCheckBox       chReverseSort2;

        /**
         * Creates the panel.
         *
         * @param aFeedRS       rendering settings.
         * @param aPrefs        user preferences.
         * @param aTrigger      trigger.
         * @param filterChanged atomic boolean flag.
         */
        public FeedsPanel(FeedRenderingSettings aFeedRS, UserPreferences aPrefs,
                          ValueModel aTrigger, AtomicBoolean filterChanged)
        {
            super(aTrigger);

            JCheckBox chShowStarz = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.show.starz"), aFeedRS, "showStarz", trigger);
            JCheckBox chShowUnreadInFeeds = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.show.number.of.unread.articles"),
                aFeedRS, "showUnreadInFeeds", trigger);
            JCheckBox chShowActivityChart = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.show.7.day.activity.chart"),
                aFeedRS, "showActivityChart", trigger);

            FeedDisplayModeManager fdmm = FeedDisplayModeManager.getInstance();
            JComboBox cbFilterLowRated = createFilterComboBox(FeedClass.LOW_RATED, fdmm, filterChanged, trigger);
            JComboBox cbFilterRead = createFilterComboBox(FeedClass.READ, fdmm, filterChanged, trigger);
            JComboBox cbFilterInvalid = createFilterComboBox(FeedClass.INVALID, fdmm, filterChanged, trigger);
            JComboBox cbFilterDisabled = createFilterComboBox(FeedClass.DISABLED, fdmm, filterChanged, trigger);

            chSortingEnabled = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.sort"), aPrefs, UserPreferences.PROP_SORTING_ENABLED, trigger);
            chSortingEnabled.addActionListener(new SortingEnabledListener());

            cbSortClass1 = new JComboBox(new SortClassesAdapter(new BufferedValueModel(
                new PropertyAdapter(aPrefs, UserPreferences.PROP_SORT_BY_CLASS_1), trigger)));
            cbSortClass2 = new JComboBox(new SortClassesAdapter(new BufferedValueModel(
                new PropertyAdapter(aPrefs, UserPreferences.PROP_SORT_BY_CLASS_2), trigger)));
            chReverseSort1 = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.sort.reverse.1"),
                aPrefs, UserPreferences.PROP_REVERSED_SORT_BY_CLASS_1, trigger);
            chReverseSort2 = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.feeds.sort.reverse.2"),
                aPrefs, UserPreferences.PROP_REVERSED_SORT_BY_CLASS_2, trigger);

            BBFormBuilder builder =
                new BBFormBuilder("7dlu, 40dlu, 2dlu, 40dlu, 2dlu, 90dlu, 2dlu, p:grow", this);
            builder.setDefaultDialogBorder();

            builder.appendSeparator(Strings.message("userprefs.tab.feeds.sorting"));
            builder.setLeadingColumnOffset(1);
            builder.append(chSortingEnabled);
            lbSortFirstBy = builder.append(Strings.message("userprefs.tab.feeds.sorting.first.by"), 1,
                CellConstraints.RIGHT, CellConstraints.DEFAULT);
            builder.append(cbSortClass1);
            builder.append(chReverseSort1);
            builder.nextLine();

            lbSortThenBy = builder.append(Strings.message("userprefs.tab.feeds.sorting.then.by"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT);
            builder.append(cbSortClass2);
            builder.append(chReverseSort2);

            builder.appendSeparator(Strings.message("userprefs.tab.feeds.filtering"));
            builder.append(Strings.message("userprefs.tab.feeds.invalid"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT).setLabelFor(cbFilterInvalid);
            builder.append(cbFilterInvalid);
            builder.nextLine();
            builder.append(Strings.message("userprefs.fully.read"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT).setLabelFor(cbFilterRead);
            builder.append(cbFilterRead);
            builder.nextLine();
            builder.append(Strings.message("userprefs.tab.feeds.below.starz.threshold"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT).setLabelFor(cbFilterLowRated);
            builder.append(cbFilterLowRated);
            builder.nextLine();
            builder.append(Strings.message("userprefs.tab.feeds.disabled"), 3,
                CellConstraints.RIGHT, CellConstraints.DEFAULT).setLabelFor(cbFilterDisabled);
            builder.append(cbFilterDisabled);

            builder.nextLine();
            builder.appendSeparator(Strings.message("userprefs.tab.feeds.show"));
            builder.append(chShowStarz, 5);
            builder.nextLine();

            builder.append(chShowUnreadInFeeds, 5);
            builder.nextLine();
            builder.append(chShowActivityChart, 5);

            setSortingEnabled(aPrefs.isSortingEnabled());
        }

        /**
         * Returns the name of the sorting class.
         *
         * @param clazz class id.
         *
         * @return the name.
         */
        private static String getFeedSortOrderName(int clazz)
        {
            return (String)FeedsSortOrder.SORTING_CLASS_NAMES.get(clazz);
        }

        /**
         * Enables and disables sorting components.
         *
         * @param enabled <code>TRUE</code> to enable.
         */
        private void setSortingEnabled(boolean enabled)
        {
            chReverseSort1.setEnabled(enabled);
            chReverseSort2.setEnabled(enabled);
            cbSortClass1.setEnabled(enabled);
            cbSortClass2.setEnabled(enabled);
            lbSortFirstBy.setEnabled(enabled);
            lbSortThenBy.setEnabled(enabled);
        }

        /** Listens to checkbox and updates enableness state of related controls. */
        private class SortingEnabledListener implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                boolean enabled = chSortingEnabled.isSelected();
                setSortingEnabled(enabled);
            }
        }

        /** Writes class-mask into model, but displays textual names of class-masks. */
        private static class SortClassesAdapter extends ComboBoxAdapter
        {
            public SortClassesAdapter(ValueModel aValueModel)
            {
                super(SORTING_CLASS_NAMES, aValueModel);
            }

            public Object getSelectedItem()
            {
                int clazz = (Integer)super.getSelectedItem();
                int index = classToIndex(clazz);
                return SORTING_CLASS_NAMES[index];
            }

            public void setSelectedItem(Object o)
            {
                int index = classNameToIndex((String)o);
                super.setSelectedItem(SORTING_ORDER[index]);
            }

            private int classToIndex(int clazz)
            {
                int index = -1;
                for (int i = 0; index == -1 && i < SORTING_ORDER.length; i++)
                {
                    index = SORTING_ORDER[i] == clazz ? i : -1;
                }

                return index;
            }

            private int classNameToIndex(String className)
            {
                int index = -1;
                for (int i = 0; index == -1 && i < SORTING_CLASS_NAMES.length; i++)
                {
                    index = SORTING_CLASS_NAMES[i].equals(className) ? i : -1;
                }

                return index;
            }
        }
    }

    /**
     * Articles preferences panel.
     */
    private static class ArticlesPanel extends PreferencesPanel
    {
        private int oldBriefSentences;
        private int oldBriefMaxLength;

        /**
         * Creates articles panel.
         *
         * @param aPrefs    user preferences.
         * @param aTrigger  trigger.
         */
        public ArticlesPanel(final UserPreferences aPrefs, ValueModel aTrigger)
        {
            super(aTrigger);

            BBFormBuilder builder = new BBFormBuilder("7dlu, p, 4dlu, 15dlu, 17dlu, p:grow", this);
            builder.setDefaultDialogBorder();

            builder.appendSeparator(Strings.message("userprefs.tab.articles.what.to.display.in.each.mode"));
            builder.setLeadingColumnOffset(1);
            builder.append(buildViewModesPanel(aPrefs), 5);

            builder.setLeadingColumnOffset(0);
            builder.appendSeparator(Strings.message("userprefs.tab.articles.brief.mode"));
            builder.setLeadingColumnOffset(1);
            builder.append(buildBriefModePanel(aPrefs, aTrigger), 5);

            builder.setLeadingColumnOffset(0);
            builder.appendSeparator(Strings.message("userprefs.tab.articles.pagination"));
            builder.setLeadingColumnOffset(1);
            builder.append(buildPaginationPanel(aPrefs, aTrigger), 5);

            // This block must go after the initialization of the other controls because it depends
            // on the values being propagated upon triggering.
            oldBriefSentences = aPrefs.getBriefSentences();
            oldBriefMaxLength = aPrefs.getBriefMaxLength();
            aTrigger.addValueChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (Boolean.TRUE.equals(evt.getNewValue()) &&
                        (oldBriefMaxLength != aPrefs.getBriefMaxLength() ||
                         oldBriefSentences != aPrefs.getBriefSentences()))
                    {
                        oldBriefSentences = aPrefs.getBriefSentences();
                        oldBriefMaxLength = aPrefs.getBriefMaxLength();

                        MainFrame frame = GlobalController.SINGLETON.getMainFrame();
                        IFeedDisplay feedView = frame.getArticlesListPanel().getFeedView();
                        feedView.repaintIfInMode(true);
                    }
                }
            });
        }

        private Component buildBriefModePanel(UserPreferences prefs, ValueModel trigger)
        {
            SpinnerModelAdapter mdlSentences = new SpinnerModelAdapter(new BoundedRangeAdapter(
                new BufferedValueModel(new PropertyAdapter(prefs, UserPreferences.PROP_BRIEF_SENTENCES),
                    trigger), 0, 1, 10));
            JSpinner spSentences = new JSpinner(mdlSentences);

            SpinnerModelAdapter mdlMaxLength = new SpinnerModelAdapter(new BoundedRangeAdapter(
                new BufferedValueModel(new PropertyAdapter(prefs, UserPreferences.PROP_BRIEF_MAX_LENGTH),
                    trigger), 0, 100, 1000));
            mdlMaxLength.setStepSize(100);
            JSpinner spMaxLength = new JSpinner(mdlMaxLength);

            BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 25dlu, 4dlu, p, 4dlu, 35dlu, 4dlu, p");

            String[] slices = Strings.slices("userprefs.tab.articles.brief.mode.settings");

            builder.append(slices[0], 1);
            builder.append(spSentences);
            builder.append(slices[1], 1);
            builder.append(spMaxLength);
            builder.append(slices[2], 1);

            return builder.getPanel();
        }

        private Component buildPaginationPanel(UserPreferences prefs, ValueModel trigger)
        {
            SpinnerModelAdapter mdlPageSize = new SpinnerModelAdapter(new BoundedRangeAdapter(
                new BufferedValueModel(new PropertyAdapter(prefs, UserPreferences.PROP_PAGE_SIZE),
                    trigger), 0, 10, 200));
            JSpinner spPageSize = new JSpinner(mdlPageSize);
            SpinnerNumberModel model = (SpinnerNumberModel)spPageSize.getModel();
            model.setStepSize(10);

            BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 25dlu, 4dlu, p");

            String[] slices = Strings.slices("userprefs.tab.articles.pagination.settings");
            builder.append(slices[0], 1);
            builder.append(spPageSize);
            builder.append(slices[1], 1);

            return builder.getPanel();
        }

        /**
         * Creates view modes panel.
         *
         * @param aPrefs preferences.
         *
         * @return panel.
         */
        private Component buildViewModesPanel(UserPreferences aPrefs)
        {
            JCheckBox[][] checks = createCheckBoxes(aPrefs.getViewModePreferences());

            BBFormBuilder builder = new BBFormBuilder(
                "p, 4dlu, center:35dlu, 4dlu, center:35dlu, 4dlu, center:35dlu");

            builder.append("", 1);
            builder.append(Strings.message("viewmode.minimal"), 1);
            builder.append(Strings.message("viewmode.brief"), 1);
            builder.append(Strings.message("viewmode.full"), 1);

            String titles[] = {
                Strings.message("article.header.author"),
                Strings.message("article.header.categories"),
                Strings.message("article.header.date"),
                Strings.message("article.header.time"),
                Strings.message("article.header.pin"),
                Strings.message("article.header.colorCode"),
                Strings.message("article.header.url")
            };

            boolean enableness[] = {
                true,
                true,
                true,
                true,
                true,
                SentimentsFeature.isAvailable(),
                true
            };

            for (int i = 0; i < titles.length; i++)
            {
                boolean en = enableness[i];
                String title = titles[i];
                builder.append(title, 1).setEnabled(en);
                builder.append(checks[i][0], 1);
                builder.append(checks[i][1], 1);
                builder.append(checks[i][2], 1);

                for (int j = 0; j < 3; j++) checks[i][j].setEnabled(en);
            }

            // Make time checkboxes dependent on date
            int date = 3;
            int time = 4;
            for (int i = 0; i < 3; i++) UifUtilities.setDependency(checks[date][i], checks[time][i]);
            
            return builder.getPanel();
        }

        /**
         * Creates check boxes to use for the form.
         *
         * @param aPrefs preferences.
         *
         * @return check boxes.
         */
        private JCheckBox[][] createCheckBoxes(ViewModePreferences aPrefs)
        {
            ViewModePreferences.ViewModeBean[] modes = new ViewModePreferences.ViewModeBean[]
            {
                new ViewModePreferences.ViewModeBean(aPrefs, IFeedDisplayConstants.MODE_MINIMAL),
                new ViewModePreferences.ViewModeBean(aPrefs, IFeedDisplayConstants.MODE_BRIEF),
                new ViewModePreferences.ViewModeBean(aPrefs, IFeedDisplayConstants.MODE_FULL)
            };

            String[] properties = {
                ViewModePreferences.AUTHOR_VISIBLE,
                ViewModePreferences.CATEGORIES_VISIBLE,
                ViewModePreferences.DATE_VISIBLE,
                ViewModePreferences.TIME_VISIBLE,
                ViewModePreferences.PIN_VISIBLE,
                ViewModePreferences.COLOR_CODE_VISIBLE,
                ViewModePreferences.URL_VISIBLE
            };

            JCheckBox[][] checks = new JCheckBox[properties.length][modes.length];

            for (int i = 0; i < properties.length; i++)
            {
                String property = properties[i];
                for (int j = 0; j < modes.length; j++)
                {
                    ViewModePreferences.ViewModeBean mode = modes[j];

                    ButtonModel model = new ToggleButtonAdapter(new BufferedValueModel(
                        new PropertyAdapter(mode, property), trigger));
                    checks[i][j] = ComponentsFactory.createCheckBox(null, model);
                }
            }

            return checks;
        }
    }

    /**
     * Global notifications preferences.
     */
    private static class NotificationsPanel extends PreferencesPanel
    {
        /**
         * Creates panel.
         *
         * @param prefs     preferences.
         * @param trigger   trigger.
         */
        public NotificationsPanel(UserPreferences prefs, ValueModel trigger)
        {
            super(trigger);

            JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("userprefs.tab.notifications.wording"));

            JCheckBox chEnabled;
            if (NotificationArea.isSupported())
            {
                chEnabled = ComponentsFactory.createCheckBox(
                    Strings.message("userprefs.tab.notifications.enabled"),
                    prefs, UserPreferences.PROP_NOTIFICATIONS_ENABLED, this.trigger);
            } else
            {
                chEnabled = ComponentsFactory.createCheckBox(
                    Strings.message("userprefs.tab.notifications.enabled"));
                chEnabled.setEnabled(NotificationArea.isSupported());
            }

            JCheckBox chSoundOnNoUnread = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.notifications.sound.no.unread"),
                prefs, UserPreferences.PROP_SOUND_ON_NO_UNREAD, this.trigger);
            JCheckBox chSoundOnNewArticles = ComponentsFactory.createCheckBox(
                Strings.message("userprefs.tab.notifications.sound.new.article"),
                prefs, UserPreferences.PROP_SOUND_ON_NEW_ARTICLES, this.trigger);

            BBFormBuilder builder = new BBFormBuilder("7dlu, p:grow", this);
            builder.setDefaultDialogBorder();

            builder.append(wording, 2);
            builder.setLeadingColumnOffset(1);
            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(chEnabled);

            builder.appendRelatedComponentsGapRow(2);
            builder.appendSeparator(Strings.message("userprefs.tab.notifications.sound"));
            builder.append(chSoundOnNoUnread);
            builder.append(chSoundOnNewArticles);
        }
    }

    /** Converts masked bits into states. */
    private static class FilterClassesAdapter extends ColorComboBoxAdapter
    {
        private static final Color[] COLORS = {
            Color.BLACK,
            Color.decode("#FF344E"), Color.decode("#FFAF2F"), Color.decode("#F6E12A"), Color.decode("#99DE26"),
            Color.decode("#24A4E0"), Color.decode("#F361B4"), Color.decode("#9E9E9E"),
            null };

        /**
         * Creates combo-box adapter for model.
         *
         * @param aModel model to adopt.
         */
        public FilterClassesAdapter(ValueModel aModel)
        {
            super(COLORS, aModel);
        }
    }

    /**
     * Reads and writes data directly to <code>ChannelDisplayModeManager</code>.
     */
    private static class DisplayModeModel extends AbstractValueModel
    {
        private int channelClass;
        private AbstractDisplayModeManager feedDMM;
        private final AtomicBoolean changedFlag;

        /**
         * Creates model adapter for the given class.
         *
         * @param cl            class.
         * @param dmm           display mode manager.
         * @param changedFlag   changed flag.
         */
        public DisplayModeModel(int cl, AbstractDisplayModeManager dmm,
                                AtomicBoolean changedFlag)
        {
            channelClass = cl;
            feedDMM = dmm;
            this.changedFlag = changedFlag;
        }

        /**
         * Takes the value of color from the display mode manager.
         *
         * @return color.
         */
        public Object getValue()
        {
            return feedDMM.getColor(channelClass);
        }

        /**
         * Sets the value of color to the display mode manager.
         *
         * @param object color.
         */
        public void setValue(Object object)
        {
            Color oldColor = feedDMM.getColor(channelClass);
            Color newColor = (Color)object;

            if ((oldColor == null && newColor != null) ||
                (oldColor != null && newColor == null)) changedFlag.set(true);

            feedDMM.setColor(channelClass, (Color)object);
        }
    }

    /**
     * Creates a filter combo-box for a given class.
     *
     * @param cl        class.
     * @param dmm       display mode manager to operate.
     * @param changed   changed atomic flag.
     * @param trigger   trigger to use for data commit.
     *
     * @return combo-box.
     */
    private static JComboBox createFilterComboBox(int cl, AbstractDisplayModeManager dmm, AtomicBoolean changed,
                                           ValueModel trigger)
    {
        JComboBox box = new JComboBox(new FilterClassesAdapter(new BufferedValueModel(
                        new DisplayModeModel(cl, dmm, changed),
                        trigger)));

        box.setRenderer(colorListCellRenderer);

        return box;
    }
}