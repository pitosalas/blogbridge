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
// $Id: SmartFeedDialog.java,v 1.47 2007/06/29 10:45:30 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.DataFeed;
import com.salas.bb.domain.FeedHandlingType;
import com.salas.bb.domain.FeedType;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryEditorPanel;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;
import com.salas.bb.views.querybuilder.QueryBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Abstract smart feed dialog suited for showing all info.
 */
public abstract class SmartFeedDialog extends AbstractDialog
{
    private static final int LABEL_COL_WIDTH = 40;

    private final IFeed feed;

    protected JTextField        tfTitle;
    protected JComboBox         cbService;
    protected JTextField        tfArticlesLimit;
    private Component           panelMyOwnFeed;
    private JTextArea           lbDescription;
    protected JCheckBox         chDedupEnabled;
    protected JSpinner          spnDedupFrom;
    protected JSpinner          spnDedupTo;
    protected JLabel            lbTo;
    protected QueryBuilder      queryBuilder;
    protected Query             query;

    private boolean                     queryFeed;
    private JPanel                      pnlOptions;
    private QueryEditorPanel            queryEditor;
    private DisplayPropertiesTabPanel   displayTab;
    private FeedUpdatePeriodPanel       pnlFeedUpdatePeriod;
    private FeedAutoSavePanel           pnlFeedAutoSave;
    private JComboBox                   cbHandlingType;

    /**
     * Creates dialog.
     *
     * @param feed  feed to show properties of or <code>NULL</code> when adding.
     * @param frame parent frame.
     * @param title title of the dialog.
     */
    public SmartFeedDialog(IFeed feed, Frame frame, String title)
    {
        super(frame, title);
        this.feed = feed;

        query = new Query();
        pnlOptions = new JPanel(new BorderLayout());
    }

    /**
     * The content is everything other than the header, including the body and the
     * buttons at the bottom.
     *
     * @return Content part of this dialog box.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Build the actual body of the modal dialog.
     *
     * @return body.
     */
    private Component buildBody()
    {
        initComponents();

        JTabbedPane tp = new JTabbedPane();
        tp.addTab("Basic", createBasicTab());
        tp.addTab("Display", displayTab);

        if (GlobalController.SINGLETON.getFeatureManager().isSfDeduplication() ||
            GlobalController.SINGLETON.getFeatureManager().isAutoSaving() ||
            feed instanceof DataFeed)
        {
            tp.addTab("Advanced", createAdvancedTab());
        }

        return tp;
    }

    /**
     * Creates basic configuration tab available both to free/basic and advanced users.
     *
     * @return tab.
     */
    public Component createBasicTab()
    {
        BBFormBuilder b = new BBFormBuilder(LABEL_COL_WIDTH + "dlu, 4dlu, 120dlu, 75dlu:grow");
        b.setDefaultDialogBorder();

        b.append(Strings.message("create.smartfeed.source"), cbService);
        b.setLeadingColumnOffset(2);
        b.appendRelatedComponentsGapRow(2);
        b.appendRow("40dlu");
        b.append(lbDescription, 2, CellConstraints.FILL, CellConstraints.TOP);

        b.setLeadingColumnOffset(0);
        b.appendUnrelatedComponentsGapRow(2);
        b.append(createTitlePanel(), 4);

        b.appendUnrelatedComponentsGapRow(2);
        b.append(pnlOptions, 4, CellConstraints.FILL, CellConstraints.FILL);

        return b.getPanel();
    }

    private Component createTitlePanel()
    {
        BBFormBuilder b = new BBFormBuilder(LABEL_COL_WIDTH + "dlu, 4dlu, p:grow, 7dlu, p, 4dlu, 25dlu");

        b.append(Strings.message("create.smartfeed.title"), tfTitle);
        b.append(Strings.message("create.smartfeed.max"), tfArticlesLimit);

        return b.getPanel();
    }

    /**
     * Creates advanced configuration tab available only to advanced users.
     *
     * @return panel.
     */
    private Component createAdvancedTab()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, 30dlu, 2dlu, p, 2dlu, 30dlu, 0:grow");
        builder.setDefaultDialogBorder();

        if (GlobalController.SINGLETON.getFeatureManager().isSfDeduplication())
        {
            builder.append(chDedupEnabled);
            builder.append(spnDedupFrom);
            builder.append(lbTo);
            builder.append(spnDedupTo);
            builder.nextLine();
        }

        if (feed instanceof DataFeed)
        {
            DataFeed dfeed = (DataFeed)feed;
            long initialUpdatePeriod = dfeed.getUpdatePeriod();

            pnlFeedUpdatePeriod = new FeedUpdatePeriodPanel(initialUpdatePeriod);

            builder.append(Strings.message("show.feed.properties.tab.advanced.handling.type"));
            builder.append(cbHandlingType, 5);
            builder.nextLine();
            builder.append(Strings.message("show.feed.properties.tab.advanced.update.period"), 1,
                    CellConstraints.LEFT, CellConstraints.TOP);
            builder.append(pnlFeedUpdatePeriod, 6);
        }

        pnlFeedAutoSave = new FeedAutoSavePanel(feed, GlobalController.SINGLETON.getFeatureManager().isAutoSaving());
        builder.append(pnlFeedAutoSave, 8);

        return builder.getPanel();
    }

    private Component buildMyOwnFeedsPanel()
    {
        queryBuilder = new QueryBuilder();
        queryBuilder.addPropertyChangeListener(QueryBuilder.PROP_CRITERIA_COUNT,
            new QueryBuilderResizer());

        queryBuilder.setQuery(query);

        return queryBuilder;
    }

    /**
     * Initializes components.
     */
    protected void initComponents()
    {
        // Return if the components are already initialized
        if (tfTitle != null) return;

        tfTitle = new JTextField();
        tfArticlesLimit = new JTextField();
        lbDescription = ComponentsFactory.createWrappedMultilineLabel("");
        UifUtilities.smallerFont(lbDescription);
        cbService = new JComboBox();

        cbHandlingType = new JComboBox(FeedHandlingType.ALL_TYPES);
        cbHandlingType.setSelectedItem(feed == null ? FeedHandlingType.DEFAULT : feed.getHandlingType());

        panelMyOwnFeed = buildMyOwnFeedsPanel();
        queryFeed = false;

        // Init the service combo-box data
        cbService.setRenderer(new ServicesListRenderer());
        cbService.addItemListener(new ServiceSelectionListener());

        cbService.addItem(Strings.message("create.smartfeed.own.feeds.service"));
        QueryType[] types = QueryType.getAvailableTypes();
        Arrays.sort(types, new QueryTypeNameComparator());
        for (QueryType type : types) cbService.addItem(type);

        // Deduplication panel
        String str = Strings.message("create.smartfeed.remove.duplicates");
        String[] chunks = StringUtils.split(str, "~");
        lbTo = new JLabel(chunks[1]);
        spnDedupFrom = createSpinner();
        spnDedupTo = createSpinner();
        chDedupEnabled = new JCheckBox(chunks[0]);
        StateUpdatingToggleListener.install(chDedupEnabled, spnDedupFrom, spnDedupTo);

        displayTab = new DisplayPropertiesTabPanel(feed);
    }

    @Override
    public void setVisible(boolean b)
    {
        // Request focus for the title component
        tfTitle.requestFocusInWindow();
        
        super.setVisible(b);
    }

    /**
     * Creates a spinner.
     * @return spinner component.
     */
    private static JSpinner createSpinner()
    {
        return new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    }

    /**
     * Validates title and max count.
     *
     * @param title     articles.
     * @param maxCount  max count.
     *
     * @return error message or NULL.
     */
    static String validateCommonFields(String title, String maxCount)
    {
        String msg = null;

        if (title.trim().length() == 0)
        {
            msg = Strings.message("create.smartfeed.validation.empty.title");
        } else
        {
            try
            {
                int limit = Integer.parseInt(maxCount);
                if (limit <= 0) msg = Strings.message("create.smartfeed.validation.negative.limit");
            } catch (NumberFormatException e)
            {
                msg = Strings.message("create.smartfeed.validation.invalid.limit");
            }
        }

        return msg;
    }

    /**
     * Validates data and returns error message if anything is wrong.
     *
     * @param type          query type.
     * @param parameter     query parameter.
     * @param articlesLimit articles limit.
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @param maxDupWords   number of first duplicate words to use as a filter.
     *
     * @return error message or <code>NULL</code>.
     */
    static String validateQueryFeedData(QueryType type, String parameter, String articlesLimit,
                                        boolean removeDuplicates, int maxDupWords)
    {
        return type.validateEntry(parameter, Integer.parseInt(articlesLimit),
            removeDuplicates, maxDupWords);
    }

    /**
     * Validates data of query and returns error message if anything is wrong.
     *
     * @param aQuery        query to validate.
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @param maxDupWords   maximum first duplicate words to treat as a filter.
     *
     * @return error message or <code>NULL</code>.
     */
    static String validateSearchFeedData(Query aQuery, boolean removeDuplicates, int maxDupWords)
    {
        return aQuery.validate(removeDuplicates, maxDupWords);
    }

    /**
     * Called when user hits "OK" button. Validates the entry and accepts or
     * displays error messages and discards.
     */
    public void doAccept()
    {
        Object selectedSource = cbService.getSelectedItem();
        String msg;

        String title = tfTitle.getText();
        String articlesLimit = tfArticlesLimit.getText();
        int maxDupWords = getDedupFrom();
        boolean removeDuplicates = isDedupEnabled();

        // Validate the entry
        msg = validateCommonFields(title, articlesLimit);
        if (msg == null && pnlFeedAutoSave != null) msg = pnlFeedAutoSave.validateData();

        if (msg == null)
        {
            if (selectedSource instanceof QueryType)
            {
                QueryType queryType = (QueryType)selectedSource;
                String queryParam = getFeedParameter();
                msg = validateQueryFeedData(queryType, queryParam, articlesLimit, removeDuplicates, maxDupWords);
            } else
            {
                msg = validateSearchFeedData(query, removeDuplicates, maxDupWords);
            }
        }

        if (msg == null)
        {
            displayTab.commitChanges();
            if (feed != null)
            {
                feed.setHandlingType((FeedHandlingType)cbHandlingType.getSelectedItem());
                if (pnlFeedAutoSave != null) pnlFeedAutoSave.commitChanges(feed);
            }

            // Propagate the update period
            if (feed instanceof DataFeed)
            {
                DataFeed dfeed = (DataFeed)feed;
                dfeed.setUpdatePeriod(pnlFeedUpdatePeriod.getUpdatePeriod());
            }

            
            super.doAccept();
        } else
        {
            JOptionPane.showMessageDialog(this, msg,
                Strings.message("create.smartfeed.validation.dialog.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Commits the properties from the autosave to the feed.
     *
     * @param feed feed.
     */
    public void commitAutoSaveProperties(IFeed feed)
    {
        if (pnlFeedAutoSave != null) pnlFeedAutoSave.commitChanges(feed);
    }

    /**
     * Returns title of the feed.
     *
     * @return feed title.
     */
    public String getFeedTitle()
    {
        return tfTitle.getText();
    }

    /**
     * Returns type of the service.
     *
     * @return type of the service (-1 for "my own feeds").
     */
    public int getFeedQueryType()
    {
        Object selectedItem = cbService.getSelectedItem();

        return selectedItem instanceof QueryType ? ((QueryType)selectedItem).getType() : -1;
    }

    /**
     * Returns query feed parameter.
     *
     * @return parameter.
     */
    public String getFeedParameter()
    {
        return queryEditor == null ? null : queryEditor.getParameter();
    }

    /**
     * Sets the text of the parameter.
     *
     * @param text text.
     */
    protected void setFeedParameter(String text)
    {
        if (queryEditor != null) queryEditor.setParameter(text);
    }

    /**
     * Returns selected articles limit.
     *
     * @return limit: positive number or (-1) for global.
     */
    public int getFeedArticlesLimit()
    {
        return strToInt(tfArticlesLimit.getText());
    }

    /**
     * Returns selected words limit.
     *
     * @return limit: positive number or (-1) for global.
     */
    public int getDedupFrom()
    {
        return (Integer)spnDedupFrom.getValue();
    }

    /**
     * Returns the last dedup word.
     *
     * @return limit: positive number or (-1) for global.
     */
    public int getDedupTo()
    {
        return (Integer)spnDedupTo.getValue();
    }

    /**
     * Converts string to integer with <code>-1</code> as default.
     *
     * @param str string.
     *
     * @return integer.
     */
    private int strToInt(String str)
    {
        int limit = -1;
        if (str.trim().length() != 0)
        {
            limit = Integer.parseInt(str);
        }
        return limit;
    }

    /**
     * Returns <code>TRUE</code> if removing of duplicates is enabled.
     *
     * @return <code>TRUE</code> if removing of duplicates is enabled. 
     */
    public boolean isDedupEnabled()
    {
        return chDedupEnabled.isSelected();
    }

    /**
     * Returns TRUE if we were working with query feed.
     *
     * @return TRUE if we were working with query feed.
     */
    public boolean isQueryFeed()
    {
        return queryFeed;
    }

    /**
     * Returns query object.
     *
     * @return query object.
     */
    public Query getFeedSearchQuery()
    {
        return query;
    }

    /**
     * Returns selected feed type.
     *
     * @return feed type.
     */
    public FeedType getFeedType()
    {
        return displayTab.getFeedType();
    }

    /**
     * Returns selected view mode.
     *
     * @return view mode.
     */
    public int getViewMode()
    {
        return displayTab.getViewMode();
    }

    /**
     * Returns <code>TRUE</code> if custom view mode is selected.
     *
     * @return <code>TRUE</code> if custom view mode is selected.
     */
    public boolean isCustomViewModeEnabled()
    {
        return displayTab.isCustomViewModeEnabled();
    }
    /**
     * Simple renderer of services list. It has icon to the left and service name to the right.
     */
    private static class ServicesListRenderer extends JPanel implements ListCellRenderer
    {
        private JLabel label;

        /**
         * Creates renderer.
         */
        public ServicesListRenderer()
        {
            super(new BorderLayout());

            label = new JLabel();
            label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            add(label, BorderLayout.CENTER);
        }

        /**
         * Returns the component to render the item. This implementation respects only
         * the <code>value</code> and <code>isSelected</code> flag.
         *
         * @return this component.
         *
         * @see ListCellRenderer#getListCellRendererComponent
         */
        public Component getListCellRendererComponent(JList list, Object value, int index,
              boolean isSelected, boolean cellHasFocus)
        {
            ImageIcon icon;
            String text;

            if (value instanceof QueryType)
            {
                QueryType type = (QueryType)value;

                String key = type.getIconKey();
                icon = key != null ? IconSource.getIcon(key) : type.getIcon();
                text = type.getName();
            } else
            {
                icon = IconSource.getIcon(ResourceID.ICON_SEARCHFEED_MYFEEDS);
                text = value.toString();
            }

            label.setIcon(icon);
            label.setText(text);

            final Color back = isSelected ? list.getSelectionBackground() : list.getBackground();
            setBackground(back);

            return this;
        }
    }

    /**
     * Listens for service selection and changes the panels.
     */
    private class ServiceSelectionListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;

            if (selected)
            {
                Object item = e.getItem();

                boolean queryTypeSelected = (item instanceof QueryType);
                queryFeed = queryTypeSelected;

                Component pnlEditor;
                String description;
                if (queryTypeSelected)
                {
                    QueryType queryType = (QueryType)item;
                    description = queryType.getQueryDescription();
                    pnlEditor = queryEditor = queryType.getEditorPanel(LABEL_COL_WIDTH);

                    if (displayTab != null)
                    {
                        displayTab.setViewMode(queryType.getPreferredViewMode());
                        displayTab.setFeedType(queryType.getFeedType());
                    }
                } else
                {
                    description = Strings.message("create.smartfeed.own.feeds.description");
                    pnlEditor = panelMyOwnFeed;
                }

                pnlOptions.removeAll();
                pnlOptions.add(pnlEditor, BorderLayout.CENTER);
                pnlOptions.validate();

                lbDescription.setText(description);

                updateWindowSize();
            }
        }

        private void updateWindowSize()
        {
            pack();
        }
    }

    /**
     * This comparator compares the names of two input <code>QueryType</code> objects.
     * It is used for sorting of available query types for the service combo-box.
     */
    private static class QueryTypeNameComparator implements Comparator<QueryType>
    {
        public int compare(QueryType t1, QueryType t2)
        {
            return t1.getName().compareToIgnoreCase(t2.getName());
        }
    }

    /**
     * Listens to changes in query builder and resizes the dialog when necessary.
     */
    private class QueryBuilderResizer implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            pack();
        }
    }
}
