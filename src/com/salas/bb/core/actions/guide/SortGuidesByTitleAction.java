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
// $Id: SortGuidesByTitleAction.java,v 1.6 2007/09/07 13:15:19 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.views.GuidesList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Sorts guides by their titles.
 */
public final class SortGuidesByTitleAction extends AbstractAction
{
    private static SortGuidesByTitleAction instance;

    /** Hidden singleton constructor. */
    private SortGuidesByTitleAction()
    {
        // Activated by ActionsMonitor upon initialization completion
        setEnabled(false);
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized SortGuidesByTitleAction getInstance()
    {
        if (instance == null) instance = new SortGuidesByTitleAction();

        return instance;
    }

    /** Invoked when an action occurs. */
    public void actionPerformed(ActionEvent e)
    {
        GlobalModel model = GlobalModel.SINGLETON;
        GlobalController controller = GlobalController.SINGLETON;
        GuidesList guidesList = controller.getMainFrame().getGudiesPanel().getGuidesList();

        GuidesSet set = model.getGuidesSet();

        IGuide selectedGuide = model.getSelectedGuide();

        sortGuidesSet(set);

        if (selectedGuide != null)
        {
            int index = controller.getGuidesListModel().indexOf(selectedGuide);
            if (index != -1) guidesList.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    /**
     * Sorts the guides set.
     *
     * @param set set.
     */
    static void sortGuidesSet(GuidesSet set)
    {
        int count = set.getGuidesCount();
        if (count > 1)
        {
            IGuide[] newList = new IGuide[count];

            // Resort guides
            for (int i = 0; i < count; i++) newList[i] = set.getGuideAt(i);
            Arrays.sort(newList, new GuidesComparator());

            // Move to new positions
            for (int i = count -  1; i >= 0; i--)
            {
                set.relocateGuide(newList[i], i);
            }
        }
    }

    /**
     * Compares guides' titles.
     */
    private static class GuidesComparator implements Comparator
    {
        /**
         * Compares two guides by their titles.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as the first argument is less than,
         *         equal to, or greater than the second.
         *
         * @throws ClassCastException if the arguments' types prevent them from being compared by this
         *                            Comparator.
         */
        public int compare(Object o1, Object o2)
        {
            IGuide g1 = (IGuide)o1;
            IGuide g2 = (IGuide)o2;

            String t1 = g1 == null ? null : g1.getTitle();
            String t2 = g2 == null ? null : g2.getTitle();

            return t1 == null
                ? t2 == null ? 0 : -1
                : t2 == null ? 1 : t1.compareToIgnoreCase(t2);
        }
    }
}
