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
// $Id: SyncFullAction.java,v 1.4 2006/05/31 10:39:45 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.i18n.Strings;

/**
 * Merges information stored on server with current informat and then saves it
 * back making everything in sync.
 */
public final class SyncFullAction extends AbstractSyncAction
{
    private static SyncFullAction instance;
    private static final String PROC_NAME = Strings.message("service.sync.full");

    /**
     * Hidden singleton constructor.
     */
    private SyncFullAction()
    {
        super(Strings.message("service.sync.action.synchronize.now"));
        setEnabled(false);
    }

    /**
     * Returns instance of action.
     *
     * @return instance.
     */
    public static SyncFullAction getInstance()
    {
        if (instance == null) instance = new SyncFullAction();
        return instance;
    }

    /**
     * Returns module for synchronization processing.
     *
     * @param model model we will be synchronizing.
     *
     * @return module.
     */
    protected AbstractSynchronization getSynchronizationModule(GlobalModel model)
    {
        return new SyncFull(model);
    }

    /**
     * Returns the name of synchronization process.
     *
     * @return name.
     */
    protected String getProcessName()
    {
        return PROC_NAME;
    }
}
