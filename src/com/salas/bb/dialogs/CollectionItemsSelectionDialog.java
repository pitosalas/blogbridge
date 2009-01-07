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
// $Id: CollectionItemsSelectionDialog.java,v 1.7 2006/10/20 19:08:50 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.component.UIFButton;
import com.salas.bb.service.ServerService;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.feedscollections.Picker;
import com.salas.bb.utils.feedscollections.CollectionItem;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * The dialog for selection of feeds from collections.
 */
public class CollectionItemsSelectionDialog extends AbstractDialog
{
    private static final int DIALOG_WIDTH = 400;

    private Picker      picker;
    private Collection  skipUrls;

    /**
     * Creates dialog.
     * 
     * @param aDialog owner dialog.
     */ 
    public CollectionItemsSelectionDialog(Dialog aDialog)
    {
        super(aDialog);
        initialize();
    }

    private void initialize()
    {
        setTitle(Strings.message("feeds.collections.dialog.title"));
        setModal(true);
    }

    protected void setResizable()
    {
        setResizable(true);
    }

    // Builds header
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("feeds.collections.dialog.title"),
            Strings.message("feeds.collections.dialog.header"),
            null, HeaderPanelExt.HEIGHT_DEFAULT
        );
    }

    // Builds content
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        
        content.add(picker, BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);
        
        return content;
    }

    private Component buildButtons()
    {
        UIFButton btnSelect = createAcceptButton(Strings.message("feeds.collections.select"), true);
        UIFButton btnCancel = createCancelButton();

        JPanel panel = ButtonBarFactory.buildOKCancelBar(btnSelect, btnCancel);
        panel.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return panel;
    }

    /**
     * Open dialog and select the specified (comma-delimetered) URL's if they are in list.
     *
     * @param selectedUrls  current user's URL's.
     * @param aSkipUrls     URL's to skip displaying.
     * @param readingLists  <code>TRUE</code> to show reading lists.
     *
     * @return comma-delimetered list of selcted URL's plus user-entered URL's passed as input.
     */
    public String open(String selectedUrls, Collection aSkipUrls, boolean readingLists)
    {
        if (picker == null)
        {
            int type = readingLists ? Picker.ITEM_TYPE_RL : Picker.ITEM_TYPE_FEED;

            picker = new Picker();
            picker.addCollection(ServerService.getStartingPointsURL(),
                Strings.message("collection.collections"), true, type, false);
            picker.addCollection(ServerService.getExpertsURL(),
                Strings.message("collection.experts"), true, type, true);
        }

        skipUrls = aSkipUrls;

        open();

        if (!hasBeenCanceled())
        {
            CollectionItem[] selected = picker.getSelectedCollectionItems();
            String[] urls = new String[selected.length];
            for (int i = 0; i < selected.length; i++)
            {
                CollectionItem item = selected[i];
                urls[i] = item.getXmlURL();
            }

            selectedUrls = StringUtils.join(urls, Constants.URL_SEPARATOR);
        }

        return selectedUrls;
    }

    protected void resizeHook(JComponent content)
    {
        content.setPreferredSize(new Dimension(DIALOG_WIDTH, (int)(DIALOG_WIDTH * 1.1)));
    }
}
