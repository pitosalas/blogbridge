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
// $Id: WizardUIHelper.java,v 1.3 2006/05/31 08:55:21 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uif.util.Modes;
import com.salas.bb.utils.i18n.Strings;

import java.awt.event.ActionListener;

/**
 * Set of wizard-related utilities.
 */
final class WizardUIHelper
{
    /**
     * Hidden utilit class constructor.
     */
    private WizardUIHelper()
    {
    }

    /**
     * Creates 'previous' button.
     *
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createPreviousButton(ActionListener listener)
    {
        return createButton(Strings.message("installer.prev"), listener);
    }

    /**
     * Creates 'next' button.
     *
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createNextButton(ActionListener listener)
    {
        return createButton(Strings.message("installer.next"), listener);
    }

    /**
     * Creates 'cancel' button.
     *
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createCancelButton(ActionListener listener)
    {
        return createButton(Strings.message("installer.cancel"), listener);
    }

    /**
     * Creates 'finish' button.
     *
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createFinishButton(ActionListener listener)
    {
        return createButton(Strings.message("installer.finish"), listener);
    }

    /**
     * Creates button.
     *
     * @param caption   button caption.
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createButton(String caption, ActionListener listener)
    {
        int mnemoIndex = caption == null ? -1 : caption.indexOf('&');
        char mnemo = mnemoIndex == -1 ? (char)-1 : caption.charAt(mnemoIndex + 1);

        if (mnemoIndex != -1) caption = caption.replaceAll("&", "");

        return createButton(caption, mnemo, listener);
    }

    /**
     * Creates button.
     *
     * @param caption   button caption.
     * @param mnemonic  mnemonic char.
     * @param listener  button listener.
     *
     * @return button object.
     */
    static UIFButton createButton(String caption, char mnemonic, ActionListener listener)
    {
        final UIFButton button = new UIFButton(caption);

        button.setMnemonic(mnemonic);
        button.setOpaqueMode(Modes.LAF_NON_AQUA);
        button.addActionListener(listener);

        return button;
    }
}
