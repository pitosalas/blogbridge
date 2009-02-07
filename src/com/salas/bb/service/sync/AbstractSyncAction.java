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
// $Id: AbstractSyncAction.java,v 1.7 2008/02/27 15:28:09 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.sentiments.RecalculateAction;
import com.salas.bb.sentiments.SentimentsConfig;
import com.salas.bb.sentiments.SentimentsFeature;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Common abstract synchronization action.
 */
public abstract class AbstractSyncAction extends ThreadedAction
{
    private Map sentimentsConfig = new HashMap();

    /**
     * Creates action.
     *
     * @param name name of action.
     */
    public AbstractSyncAction(String name)
    {
        super(name);
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        doSync(event);
    }

    /**
     * Do selected synchronization.
     *
     * @param event parent event or null.
     */
    public synchronized void doSync(ActionEvent event)
    {
        if (!GlobalController.SINGLETON.canSynchronize()) return;

        enableSyncActions(false);
        recordSentimentsConfig();

        try
        {
            Frame owner = UifUtilities.findOwnerFrame(event == null ? null : event.getSource());

            IProgressListener listener =
                ProgressDialog.createListenerDialog(owner, getProcessName());

            AbstractSynchronization sync = getSynchronizationModule(GlobalModel.SINGLETON);
            AbstractSynchronization.Stats stats = sync.doSynchronization(listener, true);

            if (!stats.hasFailed()) GlobalController.SINGLETON.getFeatureManager().registerSync();

            if (SentimentsFeature.isAvailable() && sentimentsConfigChanged())
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        RecalculateAction.perform(false);
                    }
                });
            }
        } finally
        {
            enableSyncActions(true);
        }
    }

    /**
     * Returns module for synchronization processing.
     *
     * @param model model we will be synchronizing.
     *
     * @return module.
     */
    protected abstract AbstractSynchronization getSynchronizationModule(GlobalModel model);

    /**
     * Returns the name of synchronization process.
     *
     * @return name.
     */
    protected abstract String getProcessName();

    private void enableSyncActions(boolean b)
    {
        SyncFullAction.getInstance().setEnabled(b);
        SyncInAction.getInstance().setEnabled(b);
        SyncOutAction.getInstance().setEnabled(b);
    }

    /**
     * Records the state of sentiments configuration.
     */
    private void recordSentimentsConfig()
    {
        SentimentsConfig.syncOut(sentimentsConfig);
    }

    /**
     * Compares current sentiments configuration to saved.
     *
     * @return TRUE if changed.
     */
    private boolean sentimentsConfigChanged()
    {
        Map cfg = new HashMap();
        SentimentsConfig.syncOut(cfg);

        try
        {
            if (cfg.size() != sentimentsConfig.size()) return true;
            Set<Map.Entry<Object, Object>> entries = cfg.entrySet();
            for (Map.Entry<Object, Object> entry : entries)
            {
                Object key  = entry.getKey();
                byte[] val1 = (byte[])entry.getValue();
                byte[] val2 = (byte[])sentimentsConfig.get(key);

                if (val2 == null) return true;
                if (!Arrays.equals(val1, val2)) return true;
            }
        } catch (Exception e)
        {
            return true;
        }

        return false;
    }
}
