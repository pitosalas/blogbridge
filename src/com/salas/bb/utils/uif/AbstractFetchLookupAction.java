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
// $Id: AbstractFetchLookupAction.java,v 1.3 2007/01/31 14:33:11 spyromus Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Abstract action to asynchronously lookup the contents of the drop-down.
 */
public abstract class AbstractFetchLookupAction extends AbstractAction
{
    private final JComboBox cbLookup;
    private final JButton   btnFetch;

    /**
     * Creates fetch action.
     *
     * @param cbLookup  combo-box lookup.
     * @param btnFetch  button used to start the action.
     * @param title     title of this action.
     */
    public AbstractFetchLookupAction(JComboBox cbLookup, JButton btnFetch, String title)
    {
        super(title);
        this.cbLookup = cbLookup;
        this.btnFetch = btnFetch;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e even.
     */
    public void actionPerformed(ActionEvent e)
    {
        btnFetch.setEnabled(false);
        cbLookup.setEnabled(false);

        startAsyncLoading(new Runnable()
        {
            public void run()
            {
                onLoadingComplete();
            }
        });
    }

    /**
     * Invoked when loading has finished.
     */
    private void onLoadingComplete()
    {
        btnFetch.setEnabled(true);
        cbLookup.setEnabled(true);

        if (isSuccessfulLoading())
        {
            Object sel = cbLookup.getSelectedItem();
            loadLookup();

            cbLookup.setSelectedItem(getNewSelection(sel));
        }
    }

    /**
     * Invoked when the list is updated and ready to select new object.
     * Default implementation scans the lookup box for an object equal to
     * this one and selects it. If the object is not found, the first
     * item will be selected. If there's nothing in the box, nothing
     * will be selected.
     *
     * @param previous  previously selected object.
     *
     * @return new selection.
     */
    protected Object getNewSelection(Object previous)
    {
        int index = -1;
        int itemsCount = cbLookup.getItemCount();
        if (previous != null)
        {
            for (int i = 0; index == -1 && i < itemsCount; i++)
            {
                if (previous.equals(cbLookup.getItemAt(i))) index = i;
            }
        }

        if (index == -1 && itemsCount > 0) index = 0;

        return index == -1 ? null : cbLookup.getItemAt(index);
    }

    /**
     * This method is called asynchronously when the action takes place. You need
     * to call loading procedure from it and the callback should be executed from
     * within EDT upon completion.
     *
     * @param edtCallback callback to execute.
     */
    protected abstract void startAsyncLoading(Runnable edtCallback);

    /**
     * This method is called when the lookup combo-box can be reloaded with new data.
     */
    protected abstract void loadLookup();

    /**
     * After the async loading the state is checked to learn whether the lookup
     * update is neccessary.
     *
     * @return <code>TRUE</code> if loading was successful and lookup should be updated.
     */
    protected abstract boolean isSuccessfulLoading();
}
