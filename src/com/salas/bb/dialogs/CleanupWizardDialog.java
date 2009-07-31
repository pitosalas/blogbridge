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
// $Id: CleanupWizardDialog.java,v 1.20 2008/04/01 09:24:37 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.component.UIFButton;
import com.salas.bb.core.FeedFormatter;
import com.salas.bb.core.ScoresCalculator;
import com.salas.bb.domain.*;
import com.salas.bb.domain.utils.GuidesUtils;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.FilterableList;
import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.mainframe.StarsSelectionComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CleanupWizard dialog.
 */
public final class CleanupWizardDialog extends AbstractDialog
{
    // constants
    private static final int PURGE_LIMIT        = 10;
    private static final int PURGE_LIMIT_MAX    = 40;
    private static final int PURGE_LIMIT_MIN    = 0;
    // 'Article Publish Period' constants
    private static final int AP_PERIOD          = 5;
    private static final int AP_PERIOD_MAX      = 40;
    private static final int AP_PERIOD_MIN      = 1;
    // 'Feed Non-Attendance Period' constants
    private static final int FNA_PERIOD         = 5;
    private static final int FNA_PERIOD_MAX     = 40;
    private static final int FNA_PERIOD_MIN     = 1;

    // widgets
    private JComboBox               cbGuides;

    private JCheckBox               chPurgeLimit;
    private JSpinner                spPurgeLimit;

    private JCheckBox               chRating;
    private StarsSelectionComponent cRating;
    private ValueModel              modelRating;

    private JCheckBox               chArticlePublishPeriod;
    private JSpinner                spArticlePublishPeriod;

    private JCheckBox               chFeedNonAttendancePeriod;
    private JSpinner                spFeedNonAttendancePeriod;

    private TableModelFeeds         modelFeeds;

    private JLabel                  lblInfo;

    // model fields
    private IGuide                  allGuides = new GuideAdapter(Strings.message("cleanup.wizard.all.guides"));
    private List<IFeed>             feedsAll;
    private FilterableList          feeds;
    private List<IFeed>             unselectedFeeds;
    private IGuide                  guide;
    private int                     purgeLimit = -1;
    private int                     articleAge = -1;
    private int                     rating = -1;
    private int                     articlePublishPeriod = -1;
    private int                     feedNonAttendancePeriod = -1;
    // caching of articles to remove number
    private List<IArticle>          articlesToDelete;
    private int                     feedsToDelete;
    // delete options number
    private int                     deleteOptionsNumber;

    // from system
    private GuidesSet               guidesSet;
    private ScoresCalculator        scoresCalculator;

    private JCheckBox               chArticleAge;
    private JSpinner                spArticleAge;

    private JCheckBox               chNotPinned;
    private JCheckBox               chNotUnread;

    private boolean                 notPinned;
    private boolean                 notUnread;


    /**
     * Creates CleanupWizard dialog.
     * 
     * @param owner                     owning frame.
     * @param aGuide                    selected guide.
     * @param aPurgeLimit               purge limit.
     * @param aRating                   rating.
     * @param anArticlePublishPeriod    article publish period.
     * @param aFeedNonAttendancePeriod  feed non attendance period.
     */
    protected CleanupWizardDialog(
            Frame owner, IGuide aGuide, final int aPurgeLimit, final int aRating,
            final int anArticlePublishPeriod, final int aFeedNonAttendancePeriod)
    {
        super(owner, Strings.message("cleanup.wizard.dialog.title"));

        unselectedFeeds = new ArrayList<IFeed>();
        guide = (aGuide == null) ? allGuides : aGuide;
        purgeLimit = aPurgeLimit;
        rating = aRating;
        articlePublishPeriod = anArticlePublishPeriod;
        feedNonAttendancePeriod = aFeedNonAttendancePeriod;
    }

    /**
     * Creates CleanupWizard dialog.
     * 
     * @param owner         owning frame.
     * @param aGuidesSet    guides set.
     * @param aScoresCalculator     scores calculator. 
     */
    public CleanupWizardDialog(Frame owner,
                               GuidesSet aGuidesSet, ScoresCalculator aScoresCalculator)
    {
        this(owner, null, -1, -1, -1, -1);

        guidesSet = aGuidesSet;
        scoresCalculator = aScoresCalculator;
    }

    /**
     * Gets the purge limit set in dialog.
     * 
     * @return purge limit (<code>-1</code> if not set).
     */
    public int getPurgeLimit()
    {
        return (chPurgeLimit.isSelected() ? (Integer)spPurgeLimit.getValue() : -1);
    }

    /**
     * Returns the list of articles to delete.
     *
     * @return articles.
     */
    public List<IArticle> getArticlesToDelete()
    {
        return articlesToDelete;
    }

    /**
     * Gets article's publish period in days (<code>-1</code> if not selected).
     * 
     * @return period in days.
     */
    public int getArticlePublishPeriod()
    {
        return articlePublishPeriod;
    }

    /**
     * Gets feed's non-attendance period in days (<code>-1</code> if not selected).
     * 
     * @return period in days.
     */
    public int getFeedNonAttendancePeriod()
    {
        return feedNonAttendancePeriod;
    }

    /**
     * Gets the selected guide.
     * 
     * @return selected guide (<code>NULL</code> if "All guides" is selected).
     */
    public IGuide getGuide()
    {
        return (guide == allGuides) ? null : guide;
    }

    /**
     * Gets the rating selected in the dialog (<code>-1</code> if not selected).
     * 
     * @return rating.
     */
    public int getRating()
    {
        return rating;
    }

    /**
     * Gets the feeds to clean.
     * 
     * @return array of feeds.
     */
    public List<IFeed> getFeedsToRemove()
    {
        ArrayList<IFeed> feedsToRemove = new ArrayList<IFeed>();

        for (IFeed feed : feeds)
        {
            if (!unselectedFeeds.contains(feed)) feedsToRemove.add(feed);
        }

        return feedsToRemove;
    }

    /**
     * Builds a pretty XP-style white header.
     *
     * @return header.
     */
    protected synchronized JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("cleanup.wizard.dialog.title"),
            Strings.message("cleanup.wizard.dialog.header"));
    }

    /**
     * Builds and answers the preference's content pane.
     *
     * @return JComponent of content part of the dialog box.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Builds "Run Cleanup" & "Cancel" buttons.
     * 
     * @return button's component.
     */
    private JComponent buildButtons()
    {
        UIFButton btnRunCleanup = super.createAcceptButton(Strings.message("cleanup.wizard.run.cleanup"), true);
        UIFButton btnCancel = createCancelButton();

        JPanel panel = ButtonBarFactory.buildOKCancelBar(btnRunCleanup, btnCancel);
        panel.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return panel;
    }

    /**
     * Build the actual body of the modal dialog.
     *
     * @return body.
     */
    private Component buildBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder("40dlu, 2dlu, 95dlu, 2dlu, 72px, 2dlu, max(20dlu;p)");
        appendDeleteArticleSelection(builder);
        appendDeleteFeedSelection(builder);
        JPanel optionsPanel = builder.getPanel();

        builder = new BBFormBuilder("p, 4dlu, p");
        builder.append(buildGuideSelection(), 3);
        builder.append(optionsPanel, 1, CellConstraints.FILL, CellConstraints.TOP);
        builder.append(buildResultsTable(), 1, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    /**
     * Builds 'Guide' selection subcomponent to content panel.
     * 
     * @return component.
     */
    private JComponent buildGuideSelection()
    {
        BBFormBuilder formBuilder =
            new BBFormBuilder("40dlu, 2dlu, 95dlu, 2dlu, 72px, 2dlu, max(20dlu;p)");

        formBuilder.append(Strings.message("cleanup.wizard.select.guide"), 1);
        formBuilder.append(cbGuides, 1, CellConstraints.FILL, CellConstraints.CENTER);

        return formBuilder.getPanel(); 
    }

    /**
     * Appends 'Delete article that:' selection subcomponent to content panel.
     * 
     * @param formBuilder   form builder.
     */
    private void appendDeleteArticleSelection(BBFormBuilder formBuilder)
    {
        formBuilder.appendRow("16dlu");
        formBuilder.appendSeparator(Strings.message("cleanup.wizard.delete.articles"));
        formBuilder.append(chPurgeLimit, 3, CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(spPurgeLimit, 1, CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(Strings.message("cleanup.wizard.articles"), 1);
        formBuilder.append(chNotUnread, 7);
        formBuilder.append(chNotPinned, 7);
        formBuilder.append(chArticleAge, 3);
        formBuilder.append(spArticleAge);
        formBuilder.append(Strings.message("cleanup.wizard.days"), 1);
    }

    /**
     * Appends 'Delete feed that:' selection subcomponent to content panel.
     * 
     * @param formBuilder   form builder.
     */
    private void appendDeleteFeedSelection(BBFormBuilder formBuilder)
    {
        formBuilder.appendRow("16dlu");
        formBuilder.appendSeparator(Strings.message("cleanup.wizard.delete.feeds.that"));

        formBuilder.append(this.chRating, 3, CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(this.cRating, 1, CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(Strings.message("cleanup.wizard.starz"), 1);

        formBuilder.append(this.chArticlePublishPeriod, 3,
                CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(this.spArticlePublishPeriod, 1,
                CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(Strings.message("cleanup.wizard.days"), 1);

        formBuilder.append(this.chFeedNonAttendancePeriod, 3,
                CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(this.spFeedNonAttendancePeriod, 1,
                CellConstraints.FILL, CellConstraints.CENTER);
        formBuilder.append(Strings.message("cleanup.wizard.days"), 1);
    }

    /**
     * Builds 'Delete feed that:' selection subcomponent to content panel.
     * 
     * @return component.
     */
    private JComponent buildResultsTable()
    {
        BBFormBuilder formBuilder = new BBFormBuilder("190dlu");

        formBuilder.appendRow("16dlu");
        formBuilder.appendSeparator("Preview:");
        formBuilder.appendRow("120dlu");
        formBuilder.append(createScrollPane(createFeedsTable(modelFeeds)), 1,
            CellConstraints.FILL, CellConstraints.FILL);

        formBuilder.append(lblInfo, 1);

        return formBuilder.getPanel();
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        // init model
        feedsAll = guidesSet.getFeeds();
        feeds = new FilterableList(feedsAll);
        // prevents show feeds when no delete option selected
        feeds.addFilter(new FilterGuard(), 0);

        // init widgets
        cbGuides = new JComboBox(new ComboBoxModelGuides(guidesSet));
        cbGuides.setSelectedItem(guide);

        // 'Purge Limit' widgets/listeners
        PurgeLimitListener listenerPurgeLimit = new PurgeLimitListener();

        spPurgeLimit = createSpinner(PURGE_LIMIT, PURGE_LIMIT_MIN, PURGE_LIMIT_MAX);
        spPurgeLimit.addChangeListener(listenerPurgeLimit);
        chPurgeLimit = createCheckBox(Strings.message("cleanup.wizard.so.that.all.feeds.have.at.most"),
            false, spPurgeLimit);
        chPurgeLimit.addItemListener(listenerPurgeLimit);

        // Article age
        ArticleAgeListener listenerArticleAge = new ArticleAgeListener();
        spArticleAge = createSpinner(7, 1, 365);
        spArticleAge.addChangeListener(listenerArticleAge);
        chArticleAge = createCheckBox(Strings.message("cleanup.wizard.articles.older"), false, spArticleAge);
        chArticleAge.addItemListener(listenerArticleAge);

        // Not pinned only
        chNotPinned = createCheckBox(Strings.message("cleanup.wizard.articles.not.pinned"), false, null);
        chNotPinned.addItemListener(new NotPinnedListener());

        // Not unread only
        chNotUnread = createCheckBox(Strings.message("cleanup.wizard.articles.not.unread"), false, null);
        chNotUnread.addItemListener(new NotUnreadListener());

        // 'Rating' widgets/listeners
        cRating = createStarsSelectionComponent(2);
        chRating = createCheckBox(Strings.message("cleanup.wizard.are.rated.as.or.fewer.than"), false, cRating);
        modelRating.addValueChangeListener(new RatingModelListener());
        chRating.addItemListener(new RatingListener());

        // 'Article Publish Period' widgets/listeners
        ArticlePublishPeriodListener listenerArticlePublishPeriod =
            new ArticlePublishPeriodListener();

        spArticlePublishPeriod = createSpinner(AP_PERIOD, AP_PERIOD_MIN, AP_PERIOD_MAX);
        chArticlePublishPeriod = createCheckBox(Strings.message("cleanup.wizard.have.not.had.new.articles.posted.in"),
                false, spArticlePublishPeriod);
        spArticlePublishPeriod.addChangeListener(listenerArticlePublishPeriod);
        chArticlePublishPeriod.addItemListener(listenerArticlePublishPeriod);

        // 'Feed non attendance period' widgets/listeners
        FeedNonAttendancePeriodListener listenerFeedNonAttendancePeriod =
            new FeedNonAttendancePeriodListener();

        spFeedNonAttendancePeriod = createSpinner(FNA_PERIOD, FNA_PERIOD_MIN, FNA_PERIOD_MAX);
        chFeedNonAttendancePeriod = createCheckBox(Strings.message("cleanup.wizard.i.havent.looked.at.in.the.last"),
                false, spFeedNonAttendancePeriod);
        spFeedNonAttendancePeriod.addChangeListener(listenerFeedNonAttendancePeriod);
        chFeedNonAttendancePeriod.addItemListener(listenerFeedNonAttendancePeriod);

        modelFeeds = new TableModelFeeds(feeds);

        lblInfo = createInfoLabel();
        updateSelectionInfo();
    }

    /**
     * Shows confirmation window in case there's something going to be deleted.
     */
    public void doAccept()
    {
        int result = JOptionPane.YES_OPTION;

        if (feedsToDelete > 0 || (articlesToDelete != null && articlesToDelete.size() > 0))
        {
            result = JOptionPane.showConfirmDialog(getOwner(),
                MessageFormat.format(Strings.message("cleanup.wizard.confirmation"),
                    feedsToDelete, feedsAll.size(), articlesToDelete == null ? 0 : articlesToDelete.size()),
                Strings.message("cleanup.wizard.dialog.title"), JOptionPane.YES_NO_OPTION);
        }

        if (result == JOptionPane.YES_OPTION) super.doAccept();
    }

    /**
     * Creates {@link JCheckBox} with text, selected and binded
     * component.
     * 
     * @param text      check box's text.
     * @param selected  check box selection.
     * @param component binded component.
     * <p> Component's enable state depends on selection state of the
     * check box (i.e. checked = enabled).
     * @return a {@link JCheckBox} instance.
     */
    private JCheckBox createCheckBox(final String text,
                                     final boolean selected,
                                     final JComponent component)
    {
        final JCheckBox check = new JCheckBox();

        check.setText(text);
        check.setSelected(selected);

        if (component != null)
        {
            component.setEnabled(selected);
            check.addItemListener(new CheckItemListener(check, component));
        }

        return check;
    }

    /**
     * Creates {@link JSpinner} with initial, min & max values.
     * 
     * @param value initial value.
     * @param min   min posiible value.
     * @param max   max possible value.
     * @return a {@link JSpinner} instance.
     */
    private JSpinner createSpinner(final int value, final int min, final int max)
    {
        return new JSpinner(new SpinnerNumberModel(value, min, max, 1));
    }

    /**
     * Creates {@link StarsSelectionComponent} with initial value.
     * 
     * @param value initial value.
     * @return a {@link StarsSelectionComponent} instance.
     */
    private StarsSelectionComponent createStarsSelectionComponent(final int value)
    {
        modelRating = new ValueModelStars(value);
        BoundedRangeModel model = new BoundedRangeAdapter(modelRating, 0, 1, 5);

        return new StarsSelectionComponent(model);
    }

    /**
     * Creates feeds table.
     *
     * @param model feeds table model.
     * @return feeds {@link JTable} instance.
     */
    private JTable createFeedsTable(TableModel model)
    {
        JTable table = new JTable(model);

        UifUtilities.setTableColWidth(table, 0, 18);
        TableColumn colScore = UifUtilities.setTableColWidth(table, 2, 64);
        colScore.setCellRenderer(new ScoreCR());

        return table;
    }

    /**
     * Creates {@link JScrollPane} for feeds table.
     *
     * @param table table.
     * @return scroll pane instance.
     */
    private JScrollPane createScrollPane(JTable table)
    {
        return new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Creates info label.
     *
     * @return info label instance.
     */
    private JLabel createInfoLabel()
    {
        JLabel label = new JLabel(".", JLabel.RIGHT);
        UifUtilities.smallerFont(label);

        return label;
    }

    /**
     * Invoked on new guide selection.
     * 
     * @param aGuide new guide selected .
     */
    private void onGuideSelection(IGuide aGuide)
    {
        if (aGuide == CleanupWizardDialog.this.allGuides)
        {
            feeds.removeFilter(new FilterGuide(null));
        } else
        {
            feeds.addFilter(new FilterGuide(aGuide), 0);
        }

        updateView();
    }

    /**
     * Invoked on any delete option selection/unselection.
     *
     * @param selected is selected.
     */
    private void onDeleteOptionSelection(final boolean selected)
    {
        final int newDeleteOptionNumber = deleteOptionsNumber + ((selected) ? 1 : -1);

        if (newDeleteOptionNumber == 0)
        {
            feeds.addFilter(new FilterGuard(), -1);
        }

        if (deleteOptionsNumber == 0 && newDeleteOptionNumber > 0)
        {
            feeds.removeFilter(new FilterGuard());
        }

        deleteOptionsNumber = newDeleteOptionNumber;
    }

    /**
     * Invoked on new limit selection.
     * 
     * @param selected  is selected.
     * @param aLimit    purge limit.
     */
    private void onPurgeLimitSelection(final boolean selected, final int aLimit)
    {
        purgeLimit = (selected) ? aLimit : -1;

        updateSelectionInfo();
    }

    /**
     * Invoked when article age selection changes.
     *
     * @param selected  <code>TRUE</code> if selected.
     * @param days      max age in days.
     */
    private void onArticleAgeSelection(boolean selected, int days)
    {
        articleAge = selected ? days : -1;

        updateSelectionInfo();
    }

    /**
     * Invoked when 'don't remove pinned articles' flag is set / reset.
     *
     * @param selected <code>TRUE</code> not to remove pinned articles.
     */
    private void onNotPinnedChange(boolean selected)
    {
        notPinned = selected;

        updateSelectionInfo();
    }

    /**
     * Invoked when 'don't remove unread articles' flag is set / reset.
     *
     * @param selected <code>TRUE</code> not to remove unread articles.
     */
    private void onNotUnreadChange(boolean selected)
    {
        notUnread = selected;

        updateSelectionInfo();
    }

    /**
     * Invoked when rating(stars) value is changed or selected/deselected.
     * 
     * @param selected  is selected.
     * @param aRating    rating value.
     */
    private void onRatingSelection(final boolean selected, final int aRating)
    {
        if (selected)
        {
            if (rating != aRating)
            {
                rating = aRating;
                feeds.addFilter(new FilterRating(rating), 12);
            }
        } else
        {
            rating = -1;
            feeds.removeFilter(new FilterRating(-1));
        }

        updateView();
    }

    /**
     * Invoked when period of articles publish
     * is changed or selected/deselected.
     * 
     * @param selected  is selected.
     * @param days      days value.
     */
    private void onArticlePublishPeriodSelection(final boolean selected, final int days)
    {
        if (selected)
        {
            if (articlePublishPeriod != days)
            {
                articlePublishPeriod = days;
                feeds.addFilter(new FilterArticlePublishPeriod(articlePublishPeriod), 100);
            }
        } else
        {
            articlePublishPeriod = -1;
            feeds.removeFilter(new FilterArticlePublishPeriod(-1));
        }

        updateView();
    }

    /**
     * Invoked when period of feed non-attendance 
     * is changed or selected/deselected.
     * 
     * @param selected  is selected.
     * @param days       days value.
     */
    private void onFeedNonAttendancePeriodSelection(final boolean selected, final int days)
    {
        if (selected)
        {
            if (feedNonAttendancePeriod != days)
            {
                feedNonAttendancePeriod = days;
                feeds.addFilter(new FilterFeedNonAttendancePeriod(feedNonAttendancePeriod), 10);
            }
        } else
        {
            feedNonAttendancePeriod = -1;
            feeds.removeFilter(new FilterFeedNonAttendancePeriod(-1));
        }

        updateView();
    }

    /**
     * Updates the dialog view, i.e. refresh the table.
     */
    private void updateView()
    {
        if (modelFeeds != null) modelFeeds.fireTableDataChanged();
        updateSelectionInfo();
    }

    /**
     * Updates selection info.
     */
    private void updateSelectionInfo()
    {
        if (lblInfo != null)
        {
            Counters counters = countFeedsToDelete();
            feedsToDelete = counters.feeds;
            articlesToDelete = findArticlesToDelete();

            // Count articles in feeds to delete

            lblInfo.setText(MessageFormat.format(Strings.message("cleanup.wizard.summary"),
                feedsToDelete, feedsAll.size(),
                counters.articles + (articlesToDelete == null ? 0 : articlesToDelete.size())));
        }
    }

    /**
     * Find articles to delete for specified article purge limit.
     * 
     * @return articles to delete.
     */
    private List<IArticle> findArticlesToDelete()
    {
        if (purgeLimit == -1 && articleAge == -1) return null;

        List<IArticle> articlesToDelete = new LinkedList<IArticle>();
        long now = DateUtils.getTodayTime();

        // Find the selected guide
        IGuide guide = (IGuide)cbGuides.getSelectedItem();
        boolean inAllGuides = guide == allGuides;

        LinkedList<IArticle> removeReserve = new LinkedList<IArticle>();

        for (IFeed feed : feedsAll)
        {
            if (feed instanceof DataFeed && (inAllGuides || feed.belongsTo(guide)) && !isSelectedForRemoval(feed))
            {
                int count = feed.getOwnArticlesCount();
                int minDelete = Math.max(0, count - (purgeLimit == -1 ? count : purgeLimit));

                removeReserve.clear();

                int deleted = 0;

                IArticle[] articles = feed.getArticles();
                for (IArticle article : articles)
                {
                    if ((!notPinned || !article.isPinned()) && (!notUnread || article.isRead()))
                    {
                        // We can remove this article if we need
                        if (articleAge != -1 &&
                            DateUtils.dayDiff(now, article.getPublicationDate().getTime()) > articleAge)
                        {
                            articlesToDelete.add(article);
                            deleted++;
                        } else removeReserve.add(article);
                    }
                }

                // Add more articles from the reserve
                int toDelete = Math.min(minDelete - deleted, removeReserve.size());
                while (toDelete > 0)
                {
                    articlesToDelete.add(removeReserve.removeLast());
                    toDelete--;
                }
            }
        }

        return articlesToDelete;
    }

    /**
     * Counts feeds to delete.
     *
     * @return number of the feeds to delete.
     */
    private Counters countFeedsToDelete()
    {
        int feedsCount = 0;
        int articlesCount = 0;

        for (IFeed feed : feeds)
        {
            if (!unselectedFeeds.contains(feed))
            {
                feedsCount++;
                if (feed instanceof DataFeed) articlesCount += feed.getArticlesCount();
            }
        }

        return new Counters(feedsCount, articlesCount);
    }

    /**
     * Simple counter holder.
     */
    private static class Counters
    {
        int feeds;
        int articles;

        private Counters(int feeds, int articles)
        {
            this.feeds = feeds;
            this.articles = articles;
        }
    }

    /**
     * Is feed selected for removal.
     *
     * @param aFeed feed.
     * @return <code>true</code> if the feed is selected for removal.
     */
    private boolean isSelectedForRemoval(final IFeed aFeed)
    {
        return feeds.contains(aFeed) && !unselectedFeeds.contains(aFeed);
    }

    /**
     * Guard filter.
     * <p> Don't show any feeds on no delete option selection.
     */
    private class FilterGuard extends FilterableList.Filter
    {
        private boolean value;

        /**
         * Creates <code>FilterGuard</code>.
         */
        public FilterGuard()
        {
            super("Guard");
            value = false;
        }

        /**
         * Is more restrictive than specified filter.
         *
         * @param aFilter filter to compare.
         *
         * @return <code>true</code> if this is more restrictive than parameter filter.
         */
        public boolean isMoreRestrictive(final FilterableList.Filter aFilter)
        {
            return false;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            return value;
        }
    }

    /**
     * Filter for guides.
     */
    private class FilterGuide extends FilterableList.Filter
    {
        private IGuide guide;

        /**
         * Creates <code>FilterGuide</code>.
         * 
         * @param aGuide    guide.
         */
        FilterGuide(IGuide aGuide)
        {
            super("guidef");
            this.guide = aGuide;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter
         * #isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(FilterableList.Filter filter)
        {
            return false;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            return ((IFeed)object).belongsTo(guide);
        }
    }

    /**
     * Filter for ratings.
     */
    private class FilterRating extends FilterableList.Filter
    {
        private int rating;

        /**
         * Creates <code>FilterRating</code>.
         * 
         * @param aRating   rating threshold value.
         */
        FilterRating(final int aRating)
        {
            super("raitingf");
            this.rating = aRating;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter
         * #isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(FilterableList.Filter filter)
        {
            return this.rating < ((FilterRating)filter).rating;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            return rating > scoresCalculator.calcFinalScore((IFeed) object);
        }
    }

    /**
     * Filter for article publish period.
     */
    private class FilterArticlePublishPeriod extends FilterableList.Filter
    {
        private int period;
        private TimeRange validTimeRange;

        /**
         * Creates <code>FilterArticlePublishPeriod</code>.
         * 
         * @param aPeriod   days period.
         */
        FilterArticlePublishPeriod(final int aPeriod)
        {
            super("articlePublishPeriodf");
            period = aPeriod;
            validTimeRange = new TimeRange(0, period);
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter
         * #isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(FilterableList.Filter filter)
        {
            return this.period > ((FilterArticlePublishPeriod)filter).period;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            IFeed feed = (IFeed) object;
            IArticle[] articles = feed.getArticles();

            boolean areLatestArticles = false;
            for (int i = 0; i < articles.length && !areLatestArticles; i++)
            {
                long publicationTime = articles[i].getPublicationDate().getTime();
                areLatestArticles = validTimeRange.isInRange(publicationTime);
            }

            return !areLatestArticles;
        }
    }

    /**
     * Filter for feed non attendance period.
     */
    private class FilterFeedNonAttendancePeriod extends FilterableList.Filter
    {
        private int period;
        private TimeRange validTimeRange;

        /**
         * Creates <code>FilterArticlePublish1Period</code>.
         * 
         * @param aPeriod   days period
         */
        FilterFeedNonAttendancePeriod(final int aPeriod)
        {
            super("feedNonAttendancePeriodf");
            period = aPeriod;
            validTimeRange = new TimeRange(0, period);
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter
         * #isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(FilterableList.Filter filter)
        {
            return period > ((FilterFeedNonAttendancePeriod)filter).period;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            IFeed feed = (IFeed) object;

            return !validTimeRange.isInRange(feed.getLastVisitTime());
        }
    }

    /**
     * PurgeLimitListener - 'Purge limit' spinner listener. 
     */
    private class PurgeLimitListener implements ChangeListener, ItemListener
    {
        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e)
        {
            onPurgeLimitSelection(chPurgeLimit.isSelected(), (Integer)spPurgeLimit.getValue());
        }

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            stateChanged(null);
        }
    }

    /**
     * Article age listener.
     */
    private class ArticleAgeListener implements ChangeListener, ItemListener
    {
        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e)
        {
            onArticleAgeSelection(chArticleAge.isSelected(), (Integer)spArticleAge.getValue());
        }

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            stateChanged(null);
        }
    }

    /**
     * Article not pinned listener.
     */
    private class NotPinnedListener implements ItemListener
    {
        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            onNotPinnedChange(chNotPinned.isSelected());
        }
    }

    /**
     * Article not unread listener.
     */
    private class NotUnreadListener implements ItemListener
    {
        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            onNotUnreadChange(chNotUnread.isSelected());
        }
    }

    /**
     * RatingListener - 'Rating' check item listener.
     */
    private class RatingListener implements ItemListener
    {
        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            onDeleteOptionSelection(e.getStateChange() == ItemEvent.SELECTED);
            onRatingSelection(chRating.isSelected(), (Integer)modelRating.getValue());
        }
    }

    /**
     * RatingModelListener - 'Rating model' listener.
     */
    private class RatingModelListener implements PropertyChangeListener
    {
        /**
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            onRatingSelection(chRating.isSelected(), (Integer)modelRating.getValue());
        }
    }

    /**
     * ArticlePublishPeriodListener - 'Article publish period' spinner listener. 
     */
    private class ArticlePublishPeriodListener implements ChangeListener, ItemListener
    {
        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e)
        {
            onArticlePublishPeriodSelection(chArticlePublishPeriod.isSelected(),
                (Integer)spArticlePublishPeriod.getValue());
        }

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            onDeleteOptionSelection(e.getStateChange() == ItemEvent.SELECTED);
            stateChanged(null);
        }
    }

    /**
     * FeedNonAttendancePeriodListener - 'Feed non attendance period' spinner listener. 
     */
    private class FeedNonAttendancePeriodListener implements ChangeListener, ItemListener
    {
        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e)
        {
            onFeedNonAttendancePeriodSelection(chFeedNonAttendancePeriod.isSelected(),
                (Integer)spFeedNonAttendancePeriod.getValue());
        }

        /**
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            onDeleteOptionSelection(e.getStateChange() == ItemEvent.SELECTED);
            stateChanged(null);
        }
    }

    /**
     * Checkbox selection listener.
     */
    private class CheckItemListener implements ItemListener
    {
        private JCheckBox check;
        private JComponent component;

        /**
         * Creates <code>CheckSelectionListener</code>.
         * 
         * @param aCheckBox     the check box to be listened.
         * @param aComponent    related component.
         */
        public CheckItemListener(JCheckBox aCheckBox, JComponent aComponent)
        {
            check = aCheckBox;
            component = aComponent;
        }

        /**
         * Invoked when the target of the listener has changed its item
         * state (i.e. selected/unselected).
         *
         * @param e  a ChangeEvent object.
         */
        public void itemStateChanged(ItemEvent e)
        {
            component.setEnabled(check.isSelected());
        }
    }

    /**
     * 'Guides' combo model.
     */
    private final class ComboBoxModelGuides implements ComboBoxModel
    {
        private GuidesSet guidesSet;

        /**
         * Creates <code>ComboBoxModelGuides</code>.
         * 
         * @param guidesSet guides set.
         */
        ComboBoxModelGuides(GuidesSet guidesSet)
        {
            this.guidesSet = guidesSet;
        }

        /**
         * Returns the length of the list.
         * 
         * @return the length of the list.
         */
        public int getSize()
        {
            return guidesSet.getGuidesCount() + 1;
        }

        /**
         * Returns the value at the specified index.
         *   
         * @param index the requested index.
         * @return the value at <code>index</code>.
         */
        public Object getElementAt(int index)
        {
            return (index == 0) ? allGuides : guidesSet.getGuideAt(index - 1);
        }

        /**
         * Returns the selected item.
         *  
         * @return The selected item or <code>NULL</code> if there is no selection.
         */
        public Object getSelectedItem()
        {
            return guide;
        }

        /**
         * Set the selected item. The implementation of this  method should notify 
         * all registered {@link ListDataListener}s that the contents 
         * have changed. 
         * 
         * @param anItem the list object to select or <code>NULL</code> 
         *        to clear the selection.
         */
        public void setSelectedItem(Object anItem)
        {
            guide = (IGuide)anItem;
            onGuideSelection(guide);
        }

        /**
         * Adds a listener to the list that's notified each time a change
         * to the data model occurs.
         * 
         * @param l the {@link ListDataListener} to be added
         */
        public void addListDataListener(ListDataListener l)
        {
        }

        /**
         * Removes a listener from the list that's notified each time a 
         * change to the data model occurs.
         * 
         * @param l the {@link ListDataListener} to be removed
         */
        public void removeListDataListener(ListDataListener l)
        {
        }
    }

    /**
     * 'Feeds' table model.
     */
    private class TableModelFeeds extends AbstractTableModel
    {
        private final String[] CAPTIONS = {
            " ",
            Strings.message("cleanup.wizard.feedstable.title"),
            Strings.message("cleanup.wizard.feedstable.score")
        };

        private static final int INDEX_SELECTION    = 0;
        private static final int INDEX_TITLE        = 1;
        private static final int INDEX_SCORE        = 2;

        private final List feedsList;

        /**
         * Creates <code>TableModelFeeds</code>.
         * 
         * @param aFeedsList list of feeds.
         */
        public TableModelFeeds(List aFeedsList)
        {
            feedsList = aFeedsList;
        }

        /**
         * Gets the column count.
         * 
         * @return column count.
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return CAPTIONS.length;
        }

        /**
         * Returns the column name.
         *
         * @param column number.
         * @return a name for this column using the string value of the appropriate member in
         *         <code>columnIdentifiers</code>. If <code>columnIdentifiers</code> does not have
         *         an entry for this index, returns the default name provided by the superclass.
         */
        public String getColumnName(int column)
        {
            return CAPTIONS[column];
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         *
         * @return the Object.class
         */
        public Class getColumnClass(int columnIndex)
        {
            return columnIndex == INDEX_SELECTION ? Boolean.class : Object.class;
        }

        /**
         * Gets the row count.
         * 
         * @return row count.
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount()
        {
            return feedsList.size();
        }

        /**
         * Gets the value at cell specified by row,column.
         * 
         * @param rowIndex      row index.
         * @param columnIndex   column index.
         * @return Object       object at cell.
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Object value = null;

            IFeed feed = (IFeed)feedsList.get(rowIndex);
            switch (columnIndex)
            {
                case INDEX_SELECTION:
                    value = unselectedFeeds.contains(feed) ? Boolean.FALSE : Boolean.TRUE;
                    break;
                case INDEX_TITLE:
                    value = GuidesUtils.getGuidesNames(feed.getParentGuides()) + "/" +
                        feed.getTitle();
                    break;
                case INDEX_SCORE:
                    value = scoresCalculator.calcFinalScore(feed);
                    break;
                default:
            }

            return value;
        }

        /**
         * Returns true regardless of parameter values.
         *
         * @param row    the row whose value is to be queried
         * @param column the column whose value is to be queried
         *
         * @return true
         *
         * @see #setValueAt
         */
        public boolean isCellEditable(int row, int column)
        {
            return column == INDEX_SELECTION;
    }

    /**
         * This empty implementation is provided so users don't have to implement this method if
         * their data model is not editable.
         *
         * @param aValue      value to assign to cell
         * @param rowIndex    row of cell
         * @param columnIndex column of cell
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex == INDEX_SELECTION)
            {
                IFeed feed = (IFeed)feedsList.get(rowIndex);

                if ((Boolean)aValue)
                {
                    unselectedFeeds.remove(feed);
                } else
                {
                    unselectedFeeds.add(feed);
                }

                fireTableRowsUpdated(rowIndex, rowIndex);
                updateSelectionInfo();
            }
        }
    }

    /**
     * A dummy implementation of <code>IGuide</code> interface.
     * <p>NOTE: It is necessary to introduce "All Guides" dummy guide.
     */
    private final class GuideAdapter extends AbstractGuide
    {
        /**
         * Creates <code>GuideAdapter</code>.
         * 
         * @param aTitle    guide's title
         */
        public GuideAdapter(String aTitle)
        {
            setTitle(aTitle);
        }

        /**
         * Returns the feed at given position. If the position is out of range [0;size) the IOOB
         * exception will be thrown.
         *
         * @param index index of the feed.
         *
         * @return feed at specified index.
         *
         * @throws IndexOutOfBoundsException if the feed index is out of range [0;size).
         */
        public IFeed getFeedAt(int index)
        {
            return null;
        }

        /**
         * Returns number of feeds in the guide.
         *
         * @return number of feeds.
         */
        public int getFeedsCount()
        {
            return 0;
        }

        /**
         * Adds feed to the guide.
         *
         * @param feed feed to add.
         *
         * @throws NullPointerException  if feed isn't specified.
         * @throws IllegalStateException if feed is already assigned to some feed.
         */
        public void add(IFeed feed)
        {
        }

        /**
         * Removes feed from the guide.
         *
         * @param feed feed to remove.
         *
         * @return TRUE if removed.
         *
         * @throws NullPointerException if feed isn't specified.
         */
        public boolean remove(IFeed feed)
        {
            return false;
        }

        /**
         * Removes the feeds in list from this guide one by one.
         *
         * @param feeds feeds to remove.
         */
        public void remove(IFeed[] feeds)
        {
        }

        /**
         * Returns index of feed within the guide.
         *
         * @param feed feed to get index for.
         *
         * @throws NullPointerException  if feed isn't specified.
         * @throws IllegalStateException if feed is assigned to the other guide.
         */
        public int indexOf(IFeed feed)
        {
            return 0;
        }

        /**
         * Returns alphabetical index of feed within the guide.
         *
         * @param feed feed to get alpha-index for.
         *
         * @return alphabetical index of feed.
         *
         * @throws NullPointerException if feed isn't specified.
         */
        public int alphaIndexOf(IFeed feed)
        {
            return 0;
        }

        /**
         * Returns the array of all feeds.
         *
         * @return array of feeds.
         */
        public IFeed[] getFeeds()
        {
            return new IFeed[0];
        }

        /**
         * Returns <code>TRUE</code> only if the feed was added directly to this guide.
         *
         * @param feed feed.
         *
         * @return <code>TRUE</code> only if the feed was added directly to this guide.
         */
        public boolean hasDirectLinkWith(IFeed feed)
        {
            return false;
        }
    }

    /**
     * Stars model.
     */
    private class ValueModelStars implements ValueModel
    {
        private Object value;
        private PropertyChangeListener listener;

        /**
         * Creates <code>ValueModelStars</code> with initial value.
         * 
         * @param aValue    initial value.
         */
        ValueModelStars(Object aValue)
        {
            setValue(aValue);
        }

        /**
         * @see ValueModel#getValue()
         */
        public Object getValue()
        {
            return value;
        }

        /**
         * @see ValueModel#setValue(java.lang.Object)
         */
        public void setValue(Object aValue)
        {
            value = aValue;

            if (listener != null) listener.propertyChange(null);
        }

        /**
         * @see ValueModel#addValueChangeListener(java.beans.PropertyChangeListener)
         */
        public void addValueChangeListener(PropertyChangeListener l)
        {
            listener = l;
        }

        /**
         * @see ValueModel#removeValueChangeListener(java.beans.PropertyChangeListener)
         */
        public void removeValueChangeListener(PropertyChangeListener l)
        {
            listener = null;
        }
    }

    /**
     * Renderer of score cell.
     */
    private static class ScoreCR extends JLabel implements TableCellRenderer
    {
        /**
         * Returns the component used for drawing the cell.  This method is used to configure the
         * renderer appropriately before drawing.
         * 
         * @param table         table.
         * @param value         value.
         * @param isSelected    is row selected.
         * @param hasFocus      hasFocus.
         * @param row           row.
         * @param column        column.
         *
         * @return renderer component.
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column)
        {
            Integer score = (Integer)value;

            Icon icon = null;

            if (score != null)
            {
                int scoreInt = Math.min(Math.max(score.intValue(), 0), 4);
                icon = FeedFormatter.getStarzIcon(scoreInt, false);
            }

            setIcon(icon);

            return this;
        }
    }
}