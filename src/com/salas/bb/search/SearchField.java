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
// $Id: SearchField.java,v 1.5 2006/03/16 15:32:46 spyromus Exp $
//

package com.salas.bb.search;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.util.Timer;

/**
 * Search field with tpye selector. It fires action event with the search
 * string as command every time it sees that the search should be performed.
 */
public class SearchField extends JTextField
{
    /** The delay in ms from the last key press before the text-changed event is fired. */
    private static final int PRE_FIRE_DELAY = 750;

    private String lastFiredText;
    private java.util.Timer timer;

    /**
     * Creates search field.
     */
    public SearchField()
    {
        enableEvents(AWTEvent.KEY_EVENT_MASK);

        addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                resetTimer();
            }
        });
    }

    /**
     * Key events processor.
     *
     * @param e event.
     */
    protected void processKeyEvent(KeyEvent e)
    {
        super.processKeyEvent(e);

        int id = e.getID();
        if (id == KeyEvent.KEY_PRESSED)
        {
            resetTimer();
        } else if (id == KeyEvent.KEY_RELEASED)
        {
            String txt = getText();
            if (!txt.equals(lastFiredText))
            {
                lastFiredText = txt;
                setTimer();
            }
        }
    }

    /**
     * Sets the timer that will fire search text entered even once expired.
     */
    private void setTimer()
    {
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        fireActionPerformed();
                    }
                });
            }
        }, PRE_FIRE_DELAY);
    }

    /**
     * Stops the timer.
     */
    private void resetTimer()
    {
        if (timer != null) timer.cancel();
    }

    /**
     * Invoked to process the key bindings for <code>ks</code> as the result of the
     * <code>KeyEvent</code> <code>e</code>. This obtains the appropriate <code>InputMap</code>,
     * gets the binding, gets the action from the <code>ActionMap</code>, and then (if the action is
     * found and the component is enabled) invokes <code>notifyAction</code> to notify the action.
     *
     * @param ks        the <code>KeyStroke</code> queried
     * @param e         the <code>KeyEvent</code>
     * @param condition the condition.
     * @param pressed   true if the key is pressed
     *
     * @return true if there was a binding to an action, and the action was enabled
     *
     * @since 1.3
     */
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
    {
        // This way we disable global shortcuts when typing in the search field
        super.processKeyBinding(ks, e, condition, pressed);
        return true;
    }
}
