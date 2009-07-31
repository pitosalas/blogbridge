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
// $Id: QueryBuilder.java,v 1.11 2006/06/01 12:49:59 spyromus Exp $
//

package com.salas.bb.views.querybuilder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.IProperty;
import com.salas.bb.domain.query.IQuery;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

/**
 * Query builder component. It takes query as input and edits its criterias.
 */
public class QueryBuilder extends JPanel
{
    private static final String QUERY_TYPE_ALL = Strings.message("querybuilder.type.all");
    private static final String QUERY_TYPE_ANY = Strings.message("querybuilder.type.any");

    public static final String PROP_CRITERIA_COUNT = "criteriaCount";

    /** Width (in dlu) of Add/Delete control buttons. */
    private static final int CONTROL_BUTTON_WIDTH = 20;

    private IQuery      query;

    private JPanel      criteriaPanel;
    private JComboBox   cbConstraint;
    private JButton     btnAdd;

    /**
     * Creates builder which
     */
    public QueryBuilder()
    {
        initGUI();
        setQuery(null);
    }

    /**
     * Loads data from query to the criteria builders. If query has no criteria yet,
     * one will be added.
     *
     * @param aQuery query to put.
     *
     * @throws NullPointerException if query is not specified.
     */
    public void setQuery(IQuery aQuery)
    {
        query = aQuery;

        setEnabled(query != null);
        if (query != null)
        {
            loadCriteriasFromQuery();

            cbConstraint.setSelectedItem(query.isAndQuery() ? QUERY_TYPE_ALL : QUERY_TYPE_ANY);
        }
    }

    private void loadCriteriasFromQuery()
    {
        int count = query.getCriteriaCount();
        if (count > 0)
        {
            for (int i = 0; i < count; i++)
            {
                ICriteria criteria = query.getCriteriaAt(i);
                appendCriteriaBuilderToPanel(criteria);
            }
        } else addEmptyCriteria();
    }


    /**
     * Adds new criteria and corresponding row to the table.
     */
    void addEmptyCriteria()
    {
        // We want Article Text to be the first because it is the most obvious and helps the user understand the dialog box.
        IProperty firstProperty = ArticleTextProperty.INSTANCE;
        IComparisonOperation firstOperation =
            (IComparisonOperation)firstProperty.getComparsonOperations().iterator().next();
        String defaultValue = firstProperty.getDefaultValue();

        ICriteria criteria = query.addCriteria();
        criteria.setProperty(firstProperty);
        criteria.setComparisonOperation(firstOperation);
        criteria.setValue(defaultValue);
        
        appendCriteriaBuilderToPanel(criteria);
    }

    /**
     * Removes the criteria from the query and corresponding builder from panel.
     *
     * @param index criteria index.
     * @throws IndexOutOfBoundsException if criteria index is less than 0 or greater than
     */
    void removeCriteria(int index)
    {
        if (query.getCriteriaCount() > 1)
        {
            query.removeCriteria(index);
            removeCriteriaBuilderFromPanel(index);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // GUI part
    // ---------------------------------------------------------------------------------------------

    private void initGUI()
    {
        initComponents();

        JPanel contentPane = new JPanel(new FlowLayout());
        contentPane.add(criteriaPanel);
        JScrollPane criteriaPane = new JScrollPane(contentPane,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        String columnsLayout = "max(25dlu;p), 2dlu, max(30dlu;p), 4dlu, 0dlu:grow, 4dlu, " +
            CONTROL_BUTTON_WIDTH + "dlu";

        BBFormBuilder builder = new BBFormBuilder(columnsLayout, this);

        UifUtilities.smallerFont(builder.append(Strings.message("querybuilder.match"), 1));
        builder.append(cbConstraint);
        UifUtilities.smallerFont(builder.append(Strings.message("querybuilder.of.the.following.conditions"), 1));
        builder.append(btnAdd);
        builder.appendRelatedComponentsGapRow();
        builder.appendRow("95dlu");
        builder.nextLine(2);
        builder.append(criteriaPane, 7, CellConstraints.FILL, CellConstraints.FILL);
    }

    private void initComponents()
    {
        criteriaPanel = new JPanel();
        BoxLayout layout = new BoxLayout(criteriaPanel, BoxLayout.Y_AXIS);
        criteriaPanel.setLayout(layout);

        cbConstraint = new JComboBox();
        cbConstraint.addItem(QUERY_TYPE_ALL);
        cbConstraint.addItem(QUERY_TYPE_ANY);
        cbConstraint.addItemListener(new QueryTypeSelectionListener());

        btnAdd = new JButton(new AddEmptyCriteriaAction());
        btnAdd.setText(null);
        btnAdd.setIcon(ResourceUtils.getIcon("add.icon"));
        sizeFunctionButton(btnAdd);
    }

    private void sizeFunctionButton(JButton aButton)
    {
        aButton.setPreferredSize(new Dimension(aButton.getSize().width, 20));
    }

    private void appendCriteriaBuilderToPanel(ICriteria aCriteria)
    {
        Collection properties = query.getAvailableProperties();
        CriteriaBuilder builder = new CriteriaBuilder(properties, aCriteria);

        criteriaPanel.add(createRowComponentForBuilder(builder));

        int count = query.getCriteriaCount();
        firePropertyChange(PROP_CRITERIA_COUNT, count - 1, count);
    }

    private JPanel createRowComponentForBuilder(CriteriaBuilder aBuilder)
    {
        JPanel criteriaRow = new JPanel();

        JButton btnDelete = new JButton(new DeleteCriteriaButtonListener(criteriaRow));
        btnDelete.setText(null);
        btnDelete.setIcon(ResourceUtils.getIcon("delete.icon"));
        sizeFunctionButton(btnDelete);
        
        String columnsDefinition = "pref:grow, 2dlu, " + CONTROL_BUTTON_WIDTH + "dlu";
        BBFormBuilder formBuilder = new BBFormBuilder(columnsDefinition, criteriaRow);
        formBuilder.append(aBuilder);
        formBuilder.append(btnDelete);

        return criteriaRow;
    }

    private void removeCriteriaBuilderFromPanel(int aIndex)
    {
        criteriaPanel.remove(aIndex);

        int count = query.getCriteriaCount();
        firePropertyChange(PROP_CRITERIA_COUNT, count + 1, count);
    }

    /**
     * Listens for button presses and adds new empty criteria record.
     */
    private class AddEmptyCriteriaAction extends AbstractAction
    {
        /** Creates listener. */
        public AddEmptyCriteriaAction()
        {
            super("+");
        }

        /** Invoked when user presses Add button. */
        public void actionPerformed(ActionEvent e)
        {
            addEmptyCriteria();
        }
    }

    /**
     * Listens to changes in query type and puts them into query object.
     */
    private class QueryTypeSelectionListener implements ItemListener
    {
        /** Invoked when user selects different query type. */
        public void itemStateChanged(ItemEvent e)
        {
            query.setAndQuery(cbConstraint.getSelectedItem().equals(QUERY_TYPE_ALL));
        }
    }

    /**
     * Listens for Delete button and issues command to remove corresponding row.
     */
    private class DeleteCriteriaButtonListener extends AbstractAction
    {
        private final Component criteriaRow;

        /**
         * Creates listener for a given row.
         *
         * @param aCriteriaRow row.
         */
        public DeleteCriteriaButtonListener(Component aCriteriaRow)
        {
            super("-");
            criteriaRow = aCriteriaRow;
        }

        /** Invoken when user presses Delete button. */
        public void actionPerformed(ActionEvent e)
        {
            int indexOfCriteria = findIndexOfCriteria(criteriaRow);
            removeCriteria(indexOfCriteria);
        }

        private int findIndexOfCriteria(Component aCriteriaRow)
        {
            Component[] rows = criteriaPanel.getComponents();
            int index = -1;

            for (int i = 0; index == -1 && i < rows.length; i++)
            {
                Component row = rows[i];
                if (row == aCriteriaRow) index = i;
            }

            return index;
        }
    }
}
