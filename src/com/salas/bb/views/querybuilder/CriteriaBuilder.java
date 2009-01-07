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
// $Id: CriteriaBuilder.java,v 1.12 2007/11/13 11:45:31 spyromus Exp $
//

package com.salas.bb.views.querybuilder;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.IProperty;
import com.salas.bb.domain.query.articles.ArticleDateProperty;
import com.salas.bb.domain.query.articles.ArticleFlagProperty;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;
import com.salas.bb.domain.query.articles.FeedStarzProperty;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.views.querybuilder.editors.CompositePropertyVE;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Criteria builder visual component. Accepts criteria as input, displays it and allows
 * user to perform modifications.
 *
 * Before the builder is ready to work, it should be initialized with the list of properties
 * which are available in the type of query it is serving to.
 */
public class CriteriaBuilder extends JPanel
{
    // The list of incompatible property classes that require resetting the value
    private static final List<Class> INCOMPATIBLE = Arrays.asList(new Class[] {
        FeedStarzProperty.class, ArticleDateProperty.class,
        ArticleFlagProperty.class, ArticleStatusProperty.class
    });

    private final Collection availableProperties;

    private ICriteria   criteria;

    private JComboBox   cbProperty;
    private JComboBox   cbOperation;
    private CompositePropertyVE valueEditor;

    /**
     * Creates builder and initializes it with available properties.
     *
     * @param aAvailableProperties all properties available for use.
     * @param aCriteria criteria to manage.
     *
     * @throws NullPointerException if available properties aren't specified.
     */
    public CriteriaBuilder(Collection aAvailableProperties, ICriteria aCriteria)
    {
        availableProperties = aAvailableProperties;

        PropertyAdapter model = new PropertyAdapter(aCriteria, ICriteria.PROP_VALUE, true);
        valueEditor = new CompositePropertyVE(model);

        initGUI();

        setCriteria(aCriteria);
    }

    private void setCriteria(ICriteria aCriteria)
    {
        criteria = aCriteria;

        IProperty property = criteria.getProperty();
        IComparisonOperation operation = criteria.getComparisonOperation();

        if (property != null)
        {
            if (!getSelectedProperty().equals(property))
            {
                cbProperty.setSelectedItem(property);
                valueEditor.setType(property.getType());
            } else
            {
                propertySelected(property);
            }

            cbOperation.setSelectedItem(operation);
        } else propertySelected(getSelectedProperty());
    }

    // ---------------------------------------------------------------------------------------------
    // GUI part
    // ---------------------------------------------------------------------------------------------

    private void initGUI()
    {
        initComponents();

        String columnsDefinition = "80dlu, 2dlu, 75dlu, 2dlu, 75dlu:grow";
        BBFormBuilder builder = new BBFormBuilder(columnsDefinition, this);

        builder.appendRow("pref:grow");
        builder.append(cbProperty, 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(cbOperation, 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(valueEditor, 1, CellConstraints.FILL, CellConstraints.FILL);
    }

    private void initComponents()
    {
        cbProperty = new JComboBox(availableProperties.toArray());
        cbProperty.addItemListener(new PropertySelectionListener());

        cbOperation = new JComboBox();
        cbOperation.addItemListener(new OperationSelectionListener());
    }

    private void propertySelected(IProperty property)
    {
        IProperty criteriaProperty = criteria.getProperty();
        boolean differentProperty = criteriaProperty == null || !criteriaProperty.equals(property);

        if (differentProperty) criteria.setProperty(property);
        fillComparisonOperations(property);
        if (differentProperty)
        {
            String currentValue = criteria.getValue();
            if (StringUtils.isEmpty(currentValue) ||
                (criteriaProperty == null || INCOMPATIBLE.contains(criteriaProperty.getClass())) ||
                (INCOMPATIBLE.contains(property.getClass())) ||
                property.validateValue(criteria.getComparisonOperation(), currentValue) != null)
            {
                criteria.setValue(property.getDefaultValue());
            }
        }

        valueEditor.setType(property.getType());
    }

    private void fillComparisonOperations(IProperty aProperty)
    {
        cbOperation.removeAllItems();

        Collection operations = aProperty.getComparsonOperations();
        for (Object operation : operations) cbOperation.addItem(operation);
    }

    private void operationSelected(IComparisonOperation operation)
    {
        criteria.setComparisonOperation(operation);
    }

    // ---------------------------------------------------------------------------------------------
    // Helper classes
    // ---------------------------------------------------------------------------------------------

    /** Listens to property selections. */
    private class PropertySelectionListener implements ItemListener
    {
        /**
         * Invoked when an item has been selected or deselected by the user. The code written
         * for this method performs the operations that need to occur when an item is selected
         * (or deselected).
         */
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                propertySelected((IProperty)e.getItem());
            }
        }
    }

    /** Listens to operation selections. */
    private class OperationSelectionListener implements ItemListener
    {
        /**
         * Invoked when an item has been selected or deselected by the user. The code written
         * for this method performs the operations that need to occur when an item is selected
         * (or deselected).
         */
        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                operationSelected((IComparisonOperation)e.getItem());
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Monitoring
    // ---------------------------------------------------------------------------------------------

    IProperty getSelectedProperty()
    {
        return (IProperty)cbProperty.getSelectedItem();
    }

    IComparisonOperation getSelectedComparisonOperation()
    {
        return (IComparisonOperation)cbOperation.getSelectedItem();
    }

    String getEnteredValue()
    {
        return criteria.getValue();
    }
}
