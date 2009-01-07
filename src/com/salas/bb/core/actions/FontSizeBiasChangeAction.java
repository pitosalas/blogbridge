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
// $Id: FontSizeBiasChangeAction.java,v 1.6 2006/01/08 04:42:25 kyank Exp $
//

package com.salas.bb.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.salas.bb.core.GlobalModel;

/**
 * Action to manipulate Font Size Bias. Initial implementation applies to Article only.
 */
public class FontSizeBiasChangeAction extends AbstractAction
{
    private int biasChangeFactor;
    
    /**
     * Creates an action with given amount of bias change.
     *
     * @param chgAmt If zero it means reset <code>biasChangeFactor</code> back to zero,
     *               otherwise bias is changed by <code>chgAmt</code>.
     */
    public FontSizeBiasChangeAction(int chgAmt)
    {
       biasChangeFactor = chgAmt;
    }
      
    /**
     * Change the fontSizeBias by telling the factor to <code>GlobalModel</code> which in
     * turn will tell the rest.
     *
     * @param event action event.
     */
    public void actionPerformed(ActionEvent event)
    {
        GlobalModel.SINGLETON.getGlobalRenderingSettings().setArticleFontBias(biasChangeFactor);
    }
}
