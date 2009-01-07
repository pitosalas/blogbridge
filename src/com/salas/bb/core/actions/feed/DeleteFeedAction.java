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
// $Id: DeleteFeedAction.java,v 1.18 2008/04/09 04:38:11 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.Resources;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for deletion of currently selected feed.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class DeleteFeedAction extends ThreadedAction
{
    /** Default. Delete mode. */
    public static final int DELETE    = 0;
    /** Hides selected feed. */
    public static final int DISABLE   = 1;
    /** Unhides selected feed. */
    public static final int ENABLE    = 2;

    private static DeleteFeedAction instance;

    private String nameDelete;
    private String nameDisable;
    private String nameEnable;

    private int mode;

    /**
     * Hidden constructor of singleton class.
     */
    private DeleteFeedAction()
    {
        setEnabled(false);

        nameDelete = Strings.message("deletefeed.delete");
        nameDisable = Strings.message("deletefeed.disable");
        nameEnable = Strings.message("deletefeed.enable");

        setMode(DELETE);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance.
     */
    public static synchronized DeleteFeedAction getInstance()
    {
        if (instance == null) instance = new DeleteFeedAction();
        return instance;
    }

    /**
     * Sets current mode of operation.
     *
     * @param aMode mode.
     *
     * @see #DELETE
     * @see #DISABLE
     * @see #ENABLE
     */
    public void setMode(int aMode)
    {
        mode = aMode;
        updateNameKey();
    }

    /**
     * Sets the value.
     *
     * @param key       key.
     * @param newValue  value.
     */
    public void putValue(String key, Object newValue)
    {
        if ("name".equalsIgnoreCase(key))
        {
            String titles = newValue.toString();
            int mark = titles.indexOf('~');
            int mark2 = titles.indexOf('~', mark + 1);

            if (mark != -1 && mark2 != -1)
            {
                nameDelete = titles.substring(0, mark);
                nameDisable = titles.substring(mark + 1, mark2);
                nameEnable = titles.substring(mark2 + 1);
            }

            updateNameKey();
        } else super.putValue(key, newValue);
    }

    /** Updates the label in the menu. */
    private void updateNameKey()
    {
        String name;

        switch (mode)
        {
            case DISABLE:
                name = nameDisable;
                break;
            case ENABLE:
                name = nameEnable;
                break;
            default:
                name = nameDelete;
        }

        super.putValue("Name", name);
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        IFeed[] selectedFeeds = GlobalController.SINGLETON.getSelectedFeeds();
        for (IFeed feed : selectedFeeds)
        {
            switch (mode)
            {
                case DISABLE:
                    // We can mark as disabled any direct feeds
                    if (feed instanceof DirectFeed)
                    {
                        ((DirectFeed)feed).setDisabled(true);
                    }
                    break;
                case ENABLE:
                    // We can mark as enabled any direct feeds
                    if (feed instanceof DirectFeed)
                    {
                        ((DirectFeed)feed).setDisabled(false);
                    }
                    break;
                default:
                    boolean cont;

                    // We can't delete dynamic direct feeds
                    IGuide guide = GlobalModel.SINGLETON.getSelectedGuide();
                    List<IGuide> otherGuides = getOtherParentGuides(feed, guide);

                    boolean noQuestions = ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0);
                    if (otherGuides.size() > 0)
                    {
                        String guidesList = "\n";
                        int i = 0;
                        for (i = 0; i < 3 && i < otherGuides.size(); i++)
                        {
                            IGuide g = otherGuides.get(i);
                            guidesList += "    - " + StringUtils.excerpt(g.getTitle(), 30) + "\n";
                        }
                        if (i < otherGuides.size()) guidesList += "    - \u2026\n"; 

                        String btnFromThisOnly = Strings.message("deletefeed.dialog.this.only");
                        String btnFromAllGuides = Strings.message("deletefeed.dialog.everywhere");
                        String btnCancel = "Cancel";
                        Object[] options = new Object[] { btnFromThisOnly, btnFromAllGuides, btnCancel };

                        int res = JOptionPane.showOptionDialog(
                            GlobalController.SINGLETON.getMainFrame(),
                            MessageFormat.format(
                                Strings.message("deletefeed.dialog.text.multiple.instances"),
                                    otherGuides.size(), guidesList),
                            Strings.message("deletefeed.dialog.title"),
                            -1,
                            JOptionPane.INFORMATION_MESSAGE,
                            Resources.getLargeApplicationIcon(),
                            options,
                            btnFromThisOnly);

                        if (res > -1 && options[res] == btnFromAllGuides)
                        {
                            for (IGuide otherGuide : otherGuides) otherGuide.remove(feed);
                        }

                        cont = (options[res] != btnCancel);
                    } else
                    {
                        int res = noQuestions ? JOptionPane.YES_OPTION : JOptionPane.showConfirmDialog(
                            GlobalController.SINGLETON.getMainFrame(),
                            Strings.message("deletefeed.dialog.text.one.instance"),
                            Strings.message("deletefeed.dialog.title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            Resources.getLargeApplicationIcon());

                        cont = res == JOptionPane.YES_OPTION;
                    }

                    if (cont) guide.remove(feed);
            }
        }

        // Switch mode HIDE->UNHIDE, UNHIDE->HIDE
        setMode(mode == DISABLE ? ENABLE : mode == ENABLE ? DISABLE : DELETE);
    }

    /**
     * Returns the list of other guides having direct connection with this feed.
     *
     * @param feed  the feed.
     * @param guide the main guide.
     *
     * @return other parent guides.
     */
    private static List<IGuide> getOtherParentGuides(IFeed feed, IGuide guide)
    {
        List<IGuide> otherGuides = new ArrayList<IGuide>();

        IGuide[] parentGuides = feed.getParentGuides();
        for (IGuide parentGuide : parentGuides)
        {
            if (parentGuide != guide && parentGuide.hasDirectLinkWith(feed))
            {
                otherGuides.add(parentGuide);
            }
        }

        return otherGuides;
    }
}
