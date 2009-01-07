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
// $Id: CompositePropertyVE.java,v 1.13 2008/02/28 09:58:33 spyromus Exp $
//

package com.salas.bb.views.querybuilder.editors;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.articles.ArticleFlagProperty;
import com.salas.bb.domain.query.articles.ArticleSentimentsProperty;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;
import com.salas.bb.domain.query.general.IDates;
import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Composite property value editor which examines the type of the property
 * and knows what editor to enable.
 */
public class CompositePropertyVE extends JPanel
{
    private IValueEditor        currentEditor;
    private final ValueModel    model;

    /**
     * Creates new editor.
     *
     * @param aModel model for value property.
     *
     * @throws NullPointerException if model is not specified.
     */
    public CompositePropertyVE(ValueModel aModel)
    {
        if (aModel == null) throw new NullPointerException(Strings.error("unspecified.model"));

        model = aModel;
        setLayout(new BorderLayout());
    }

    /**
     * Sets the value to editor.
     *
     * @param aType     type of the value.
     */
    public void setType(PropertyType aType)
    {
        setEditor(getEditorForType(aType));
    }

    private IValueEditor getEditorForType(PropertyType aType)
    {
        IValueEditor editor;

        if (PropertyType.LONG == aType)
        {
            editor = new LongVE(1, 5, model);

        } else if (PropertyType.STARZ == aType)
        {
            editor = new StarzVE(model);

        } else if (PropertyType.STATUS == aType)
        {
            editor = new ChoiceVE(new String[] {
                    Strings.message("querybuilder.articlestatus.read"),
                    Strings.message("querybuilder.articlestatus.unread")
                }, new String[] {
                    ArticleStatusProperty.VALUE_READ,
                    ArticleStatusProperty.VALUE_UNREAD
                }, model);

        } else if (PropertyType.SENTIMENTS == aType)
        {
            editor = new ChoiceVE(new String[] {
                    Strings.message("querybuilder.articlesentiments.positive"),
                    Strings.message("querybuilder.articlesentiments.negative")
                }, new String[] {
                    ArticleSentimentsProperty.VALUE_POSITIVE,
                    ArticleSentimentsProperty.VALUE_NEGATIVE
                }, model);

        } else if (PropertyType.SET_UNSET == aType)
        {
            editor = new ChoiceVE(new String[] {
                    Strings.message("querybuilder.articlepin.set"),
                    Strings.message("querybuilder.articlepin.unset")
                }, new String[] {
                    ArticleFlagProperty.VALUE_SET,
                    ArticleFlagProperty.VALUE_UNSET
                }, model);

        } else if (PropertyType.DATE == aType)
        {
            editor = new ChoiceVE(new String[] {
                    TimeRange.TITLE_TODAY, TimeRange.TITLE_YESTERDAY, TimeRange.TITLE_LAST_WEEK,
                    TimeRange.TITLE_TWO_WEEKS_AGO },
                new String[] {
                    IDates.VALUE_TODAY, IDates.VALUE_YESTERDAY, IDates.VALUE_LAST_WEEK,
                    IDates.VALUE_TWO_WEEKS_AGO}, model);

        } else editor = new StringVE(model);

        return editor;
    }

    private synchronized void setEditor(IValueEditor newEditor)
    {
        if (currentEditor != null)
        {
            uninstallEditor(currentEditor);
            currentEditor = null;
        }

        if (newEditor != null)
        {
            installEditor(newEditor);
            currentEditor = newEditor;
        }
    }

    private void uninstallEditor(IValueEditor aCurrentEditor)
    {
        Component visualComponent = aCurrentEditor.getVisualComponent();
        remove(visualComponent);
    }

    private void installEditor(IValueEditor aNewEditor)
    {
        Component visualComponent = aNewEditor.getVisualComponent();
        add(visualComponent, BorderLayout.CENTER);
    }
}
