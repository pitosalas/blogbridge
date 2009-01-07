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
// $Id: SearchDialog.java,v 1.26 2007/07/20 16:51:27 spyromus Exp $
//

package com.salas.bb.search;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ProgressSpinner;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;

/**
 * Search dialog box.
 */
public class SearchDialog extends AbstractDialog
{
    /** Minimum dialog size. */
    private static final Dimension  MIN_SIZE = new Dimension(500, 350);

    private final SearchEngine      searchEngine;

    private ResultsList             itemsList;
    private JLabel                  lbResults;
    private ProgressSpinner         pgSpinner;
    private final SearchDialog.ResultsListener resultsListener;
    private SearchResultsListModel model;

    /**
     * Creates search dialog.
     *
     * @param owner     the dialog's parent frame.
     * @param engine    search engine to use.
     * @param listener  selection listener.
     */
    public SearchDialog(Frame owner, SearchEngine engine, ActionListener listener)
    {
        super(owner, Strings.message("search.dialog.title"));
        searchEngine = engine;

        model = new SearchResultsListModel();
        itemsList = new ResultsList(model);
        itemsList.addActionListener(listener);

        lbResults = new JLabel();
        UifUtilities.smallerFont(lbResults);

        pgSpinner = new ProgressSpinner();

        resultsListener = new ResultsListener();
        searchEngine.getResult().addChangesListener(resultsListener);

        setModal(false);
    }

    /** Release resources before closing. */
    public void close()
    {
        searchEngine.getResult().removeChangesListener(resultsListener);

        itemsList = null;
        super.close();
        getContentPane().removeAll();
    }

    /**
     * Sets the dialog's resizable state. By default dialogs are non-resizable; subclasses may
     * override.
     */
    protected void setResizable()
    {
        setResizable(true);
    }

    /**
     * Creates main content pane.
     *
     * @return the dialog's main content without header and border.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildTopBar(), BorderLayout.NORTH);
        panel.add(buildResultsPanel(), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates top bar with progress indicator, results count and search field.
     *
     * @return top bar component.
     */
    private Component buildTopBar()
    {
        final JLabel helpIcon = new JLabel(ResourceUtils.getIcon("search.ext.icon"));
        helpIcon.setToolTipText(Strings.message("search.ext.text"));
        helpIcon.setEnabled(false);

        final JCheckBox chPinnedArticlesOnly = new JCheckBox(Strings.message("search.pinned.articles.only"));
        UifUtilities.smallerFont(chPinnedArticlesOnly);
        if (SystemUtils.IS_OS_MAC) chPinnedArticlesOnly.setMargin(new Insets(0, 0, 2, 0));

        final SearchField tfSearch = new SearchField();
        tfSearch.addKeyListener(new NavigationListener());

        // Register changes monitor
        ActionListener monitor = new SearchCriteriaChangeMonitor(chPinnedArticlesOnly, tfSearch, helpIcon);
        tfSearch.addActionListener(monitor);
        chPinnedArticlesOnly.addActionListener(monitor);

        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, 50dlu, 2dlu, p, 4dlu, p, 7dlu, p, 14dlu:grow, p");

        JLabel lbSearch = builder.append(Strings.message("search.prompt"), 1);
        lbSearch.setLabelFor(tfSearch);
        UifUtilities.smallerFont(lbSearch);
        UifUtilities.smallerFont(tfSearch);
        builder.append(tfSearch);
        builder.append(helpIcon);
        builder.append(chPinnedArticlesOnly, 1, CellConstraints.DEFAULT, CellConstraints.CENTER);
        builder.append(lbResults);
        builder.append(pgSpinner);
        builder.appendUnrelatedComponentsGapRow();

        return builder.getPanel();
    }

    /**
     * Creates results panel with results list and controls.
     *
     * @return results panel component.
     */
    private Component buildResultsPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(itemsList, BorderLayout.NORTH);
        panel2.setBackground(Color.WHITE);

        panel.add(panel2, BorderLayout.CENTER);
        panel.add(buildControlPanel(), BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(panel);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return sp;
    }

    /**
     * Returns currently selected item.
     *
     * @return item.
     */
    public ResultItem getSelectedItem()
    {
        return itemsList == null ? null : itemsList.getSelectedItem();
    }

    /**
     * Creates control panel with grouping, sorting and filtering options.
     *
     * @return control panel.
     */
    private JComponent buildControlPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("5dlu, p, 5dlu");

        builder.appendRelatedComponentsGapRow();
        builder.setLeadingColumnOffset(1);
        builder.nextLine();

        // Grouping
        Action actGroup = new AbstractAction()
        {
            private ActionLabel selection;
            public void actionPerformed(ActionEvent e)
            {
                if (selection == e.getSource()) return;
                if (selection != null) selection.setSelected(false);
                onGroupingChange(e.getID());
                selection = (ActionLabel)e.getSource();
            }
        };
        ActionLabel albFlat = new ActionLabel(actGroup,
            Strings.message("search.groupping.flat"), SearchResultsListModel.GROUP_FLAT);
        ActionLabel albKind = new ActionLabel(actGroup,
            Strings.message("search.groupping.kind"), SearchResultsListModel.GROUP_KIND);
        ActionLabel albDate = new ActionLabel(actGroup,
            Strings.message("search.groupping.date"), SearchResultsListModel.GROUP_DATE);
        albKind.setSelected(true);

        // Filtering
        Action actFiltering = new AbstractAction()
        {
            private ActionLabel selection;
            public void actionPerformed(ActionEvent e)
            {
                if (selection == e.getSource()) return;
                if (selection != null) selection.setSelected(false);
                onFilteringChange(e.getID());
                selection = (ActionLabel)e.getSource();
            }
        };
        ActionLabel albAnyDate = new ActionLabel(actFiltering,
            Strings.message("search.when.any.date"),
            ResultsList.DATE_ANY);
        ActionLabel albToday = new ActionLabel(actFiltering,
            Strings.message("search.when.today"),
            ResultsList.DATE_TODAY);
        ActionLabel albYesterday =new ActionLabel(actFiltering,
            Strings.message("search.when.since.yesterday"),
            ResultsList.DATE_YESTERDAY);
        ActionLabel albThisWeek = new ActionLabel(actFiltering,
            Strings.message("search.when.this.week"),
            ResultsList.DATE_WEEK);
        ActionLabel albThisMonth = new ActionLabel(actFiltering,
            Strings.message("search.when.this.month"),
            ResultsList.DATE_MONTH);
        ActionLabel albThisYear = new ActionLabel(actFiltering,
            Strings.message("search.when.this.year"),
            ResultsList.DATE_YEAR);
        albAnyDate.setSelected(true);

        builder.append(smallLabel(Strings.message("search.groupping")));
        builder.nextLine();
        builder.append(albFlat);
        builder.nextLine();
        builder.append(albKind);
        builder.nextLine();
        builder.append(albDate);
//        builder.nextLine();

        builder.appendUnrelatedComponentsGapRow(2);
        builder.setLeadingColumnOffset(0);
        builder.append(new JLabel(), 3);
//        builder.append(new JPopupMenu.Separator(), 3);

        builder.setLeadingColumnOffset(1);
        builder.append(smallLabel(Strings.message("search.when")));
        builder.nextLine();
        builder.append(albAnyDate);
        builder.nextLine();
        builder.append(albToday);
        builder.nextLine();
        builder.append(albYesterday);
        builder.nextLine();
        builder.append(albThisWeek);
        builder.nextLine();
        builder.append(albThisMonth);
        builder.nextLine();
        builder.append(albThisYear);
        builder.nextLine();

        return builder.getPanel();
    }

    /**
     * Creates small label component.
     *
     * @param txt text.
     *
     * @return label component.
     */
    private static JComponent smallLabel(String txt)
    {
        JLabel label = new JLabel(txt);
        UifUtilities.smallerFont(label);

        return label;
    }

    /**
     * Inoked when grouping changes.
     *
     * @param grouping new grouping option.
     */
    private void onGroupingChange(int grouping)
    {
        model.setGroupBy(grouping);
    }

    /**
     * Invoked when filtering changes.
     *
     * @param filtering new filtering option.
     */
    private void onFilteringChange(int filtering)
    {
        itemsList.setDateRange(filtering);
    }

    /**
     * Resizes the specified component. This method is called during the build process and enables
     * subclasses to achieve a better aspect ratio, by applying a resizer, e.g. the
     * <code>Resizer</code>.
     *
     * @param component the component to be resized
     */
    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.ONE2ONE.fromWidth(MIN_SIZE.width));
    }

    // ---------------------------------------------------------------------------------------------

    /** Action label with selection indication and click handler. */
    private static class ActionLabel extends JLabel
    {
        private final Action action;
        private final int id;

        /**
         * Creates label.
         *
         * @param anAction action to call when clicked.
         * @param title    label title.
         * @param anID       ID.
         */
        public ActionLabel(Action anAction, String title, int anID)
        {
            super(title);
            UifUtilities.smallerFont(this);

            this.action = anAction;
            id = anID;

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        /**
         * Selects / deselects item.
         *
         * @param aSelected <code>TRUE</code> to select.
         */
        public void setSelected(boolean aSelected)
        {
            Color cl = aSelected ? Color.BLUE : Color.BLACK;
            setForeground(cl);
            if (aSelected) action.actionPerformed(new ActionEvent(this, id, null));
        }

        /**
         * Processes clicks.
         *
         * @param e event.
         */
        protected void processMouseEvent(MouseEvent e)
        {
            if (e.getID() == MouseEvent.MOUSE_PRESSED)
            {
                setSelected(true);
            }
        }
    }

    /**
     * Listener for results updates.
     */
    private class ResultsListener implements ISearchResultListener
    {
        private int results = 0;

        /**
         * Invoked when new result item is added to the list.
         *
         * @param result results list object.
         * @param item   item added.
         * @param index  item index.
         */
        public void itemAdded(ISearchResult result, final ResultItem item, int index)
        {
            results++;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    lbResults.setText(MessageFormat.format(Strings.message("search.0.results"),
                        Integer.toString(results)));
                    model.add(item);
                }
            });
        }

        /**
         * Invoked when the result items are removed from the list.
         *
         * @param result results list object.
         */
        public void itemsRemoved(ISearchResult result)
        {
            results = 0;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    lbResults.setText(Strings.message("search.no.results"));
                    model.clear();
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
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    pgSpinner.stop();
                }
            });
        }
    }

    /**
     * Listens to key taps and passes them to the list component.
     */
    private class NavigationListener extends KeyAdapter
    {
        /**
         * Invoked when a key has been pressed.
         *
         * @param e event.
         */
        public void keyPressed(KeyEvent e)
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    itemsList.onPrevItemSelected();
                    break;
                case KeyEvent.VK_DOWN:
                    itemsList.onNextItemSelected();
                    break;
                case KeyEvent.VK_ESCAPE:
                    doCancel();
                    break;
            }
        }
    }

    private class SearchCriteriaChangeMonitor implements ActionListener
    {
        private String lastText;
        public boolean lastPinnedArticlesOnly;
        private final JCheckBox chPinnedArticlesOnly;
        private final JLabel    helpIcon;
        private final SearchField tfSearch;

        public SearchCriteriaChangeMonitor(JCheckBox chPinnedArticlesOnly, SearchField tfSearch, JLabel helpIcon)
        {
            this.chPinnedArticlesOnly = chPinnedArticlesOnly;
            this.helpIcon = helpIcon;
            this.tfSearch = tfSearch;
            lastPinnedArticlesOnly = chPinnedArticlesOnly.isSelected();
        }

        public void actionPerformed(ActionEvent e)
        {
            String text = tfSearch.getText();
            boolean pinnedArticlesOnly = chPinnedArticlesOnly.isSelected();

            showHelpIfNecessary(text);
            if (!text.equalsIgnoreCase(lastText) ||
                pinnedArticlesOnly != lastPinnedArticlesOnly)
            {
                pgSpinner.start();
                searchEngine.setSearchText(text, pinnedArticlesOnly);
                lastText = text;
                lastPinnedArticlesOnly = pinnedArticlesOnly;
            } else
            {
                itemsList.onItemFired();
            }
        }

        private void showHelpIfNecessary(String text)
        {
            helpIcon.setEnabled(SearchEngine.isComplexSeachPattern(text));
        }
    }
}
