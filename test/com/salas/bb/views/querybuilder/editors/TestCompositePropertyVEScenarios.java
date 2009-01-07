package com.salas.bb.views.querybuilder.editors;

import junit.framework.TestCase;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.Query;
import com.jgoodies.binding.beans.PropertyAdapter;

/**
 * This suite contains scenarios for composite editor.
 */
public class TestCompositePropertyVEScenarios extends TestCase
{
    public void testIntializing()
    {
        ICriteria criteria = new Query().addCriteria();
        PropertyAdapter model = new PropertyAdapter(criteria, ICriteria.PROP_VALUE, true);
        CompositePropertyVE compositeEditor = new CompositePropertyVE(model);

        // When type of the property becomes obvious we are setting type to editor
        compositeEditor.setType(PropertyType.STATUS);
    }
}
