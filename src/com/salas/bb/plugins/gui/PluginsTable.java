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
// $Id: PluginsTable.java,v 1.6 2007/04/06 09:57:35 spyromus Exp $
//

package com.salas.bb.plugins.gui;

import com.salas.bb.plugins.domain.Package;
import com.salas.bb.utils.uif.CheckBoxList;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Plug-ins list table.
 */
public class PluginsTable extends CheckBoxList
{
    private List<Package>       installed;
    private List<Package>       selected;

    private PackagesListModel   packages;

    /**
     * Creates table.
     */
    public PluginsTable()
    {
        packages = new PackagesListModel();
        setModel(packages);
    }

    // ------------------------------------------------------------------------
    // Interface
    // ------------------------------------------------------------------------

    /**
     * Sets the list of installed packages.
     *
     * @param packages installed packages.
     */
    public void setInstalledPackages(List<Package> packages)
    {
        installed = packages;
        updateList();
    }

    /**
     * Sets the list of selected packages.
     *
     * @param packages selected packages.
     */
    public void setSelectedPackages(List<Package> packages)
    {
        selected = packages;
        updateList();
    }

    /**
     * Returns the list of selected packages.
     *
     * @return selected packages.
     */
    public List<Package> getSelectedPackages()
    {
        List<Package> pkgs = new ArrayList<Package>();

        for (PackageCheckBox pkgbox : packages)
        {
            if (pkgbox.isSelected()) pkgs.add(pkgbox.getSource());
        }

        return pkgs;
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    /** Adds newly installed items, removes missing, changes the selection. */
    private void updateList()
    {
        if (installed == null)
        {
            packages.clear();
        } else
        {
            // Add new items
            for (Package pkg : installed)
            {
                boolean found = false;
                for (PackageCheckBox pcb : packages)
                {
                    if (pcb.getSource().equals(pkg))
                    {
                        found = true;
                        break;
                    }
                }

                if (!found) packages.add(new PackageCheckBox(pkg));
            }

            // Remove missing
            List<PackageCheckBox> toRemove = new ArrayList<PackageCheckBox>();
            for (PackageCheckBox pcb : packages)
            {
                boolean found = false;
                for (Package pkg : installed)
                {
                    if (pcb.equals(pkg))
                    {
                        found = true;
                        break;
                    }
                }

                if (!found) toRemove.add(pcb);
            }
            packages.removeAll(toRemove);
        }

        // Change the selection
        if (selected == null)
        {
            for (PackageCheckBox pcb : packages)
            {
                pcb.setSelected(false);
            }
        } else
        {
            for (PackageCheckBox pcb : packages)
            {
                if (selected.contains(pcb.getSource())) pcb.setSelected(true);
            }
        }
    }

    /**
     * Returns highlighted package.
     *
     * @return package or <code>NULL</code>.
     */
    public Package getHighlightedPackage()
    {
        PackageCheckBox pcb = (PackageCheckBox)getSelectedValue();
        return pcb == null ? null : pcb.getSource();
    }

    /**
     * Selects a package by its file name.
     *
     * @param file file name.
     */
    public void selectPackage(File file)
    {
        String name = file.getName();
        for (PackageCheckBox pcb : packages)
        {
            if (pcb.getSource().getFileName().equals(name))
            {
                pcb.setSelected(true);
                setSelectedIndex(packages.indexOf(pcb));
                return;
            }
        }
    }

    /**
     * Package checkbox.
     */
    private static class PackageCheckBox extends JCheckBox
    {
        private final Package source;

        /**
         * Creates a package checkbox.
         *
         * @param source source package.
         */
        public PackageCheckBox(Package source)
        {
            this.source = source;
            setText(source.getName());
        }

        /**
         * Returns package.
         *
         * @return package.
         */
        public Package getSource()
        {
            return source;
        }

        @Override
        public String toString()
        {
            return source.getName();
        }

        /**
         * Compares this object to another package or package checkbox.
         *
         * @param o object.
         *
         * @return <code>TRUE</code> if represent the same package.
         */
        public boolean equals(Object o)
        {
            if (this == o) return true;

            Package thatSource = o instanceof Package ? (Package)o : ((PackageCheckBox)o).getSource();

            return source != null ? source.equals(thatSource) : thatSource == null;
        }

        /**
         * Returns the hash code.
         *
         * @return hash code.
         */
        public int hashCode()
        {
            return (source != null ? source.hashCode() : 0);
        }

        /**
         * Checkbox comparator.
         */
        public static class PCBComparator implements Comparator<PackageCheckBox>
        {
            /**
             * Compares two objects.
             *
             * @param o1    first.
             * @param o2    second.
             *
             * @return a negative integer, zero, or a positive integer as the
             * 	       first argument is less than, equal to, or greater than the
             *	       second.
             */
            public int compare(PackageCheckBox o1, PackageCheckBox o2)
            {
                return o1.getText().compareTo(o2.getText());
            }
        }
    }

    /**
     * Packages list model firing necessary events on updates.
     */
    private static class PackagesListModel extends ArrayList<PackageCheckBox> implements ListModel
    {
        private final static Comparator<PackageCheckBox> comparator = new PackageCheckBox.PCBComparator();

        @Override
        public boolean add(PackageCheckBox o)
        {
            int i = Collections.binarySearch(this, o, comparator);
            if (i < 0)
            {
                i = -i - 1;
                super.add(i, o);
                fireIntervalAdded(i, i);
            }

            return i < 0;
        }


        @Override
        public PackageCheckBox remove(int index)
        {
            PackageCheckBox el = super.remove(index);
            fireIntervalRemoved(index, index);
            return el;
        }

        @Override
        public void clear()
        {
            int s = size();
            super.clear();

            if (s > 0) fireIntervalRemoved(0, s);
        }

        // --------------------------------------------------------------------
        // Implementation
        // --------------------------------------------------------------------

        private EventListenerList ell = new EventListenerList();

        /**
         * Returns the length of the list.
         *
         * @return the length of the list
         */
        public int getSize()
        {
            return this.size();
        }

        /**
         * Returns the value at the specified index.
         *
         * @param index the requested index
         *
         * @return the value at <code>index</code>
         */
        public Object getElementAt(int index)
        {
            return get(index);
        }

        /**
         * Adds a listener to the list that's notified each time a change
         * to the data model occurs.
         *
         * @param l the <code>ListDataListener</code> to be added
         */
        public void addListDataListener(ListDataListener l)
        {
            ell.add(ListDataListener.class, l);
        }

        /**
         * Removes a listener from the list that's notified each time a
         * change to the data model occurs.
         *
         * @param l the <code>ListDataListener</code> to be removed
         */
        public void removeListDataListener(ListDataListener l)
        {
            ell.remove(ListDataListener.class, l);
        }

        /**
         * Fires interval added event.
         *
         * @param i0 index 0.
         * @param i1 index 1.
         */
        private void fireIntervalAdded(int i0, int i1)
        {
            ListDataEvent e = null;
            ListDataListener[] ldls = ell.getListeners(ListDataListener.class);
            for (ListDataListener ldl : ldls)
            {
                if (e == null) e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i0, i1);
                ldl.intervalAdded(e);
            }
        }

        /**
         * Fires interval removed event.
         *
         * @param i0 index 0.
         * @param i1 index 1.
         */
        private void fireIntervalRemoved(int i0, int i1)
        {
            ListDataEvent e = null;
            ListDataListener[] ldls = ell.getListeners(ListDataListener.class);
            for (ListDataListener ldl : ldls)
            {
                if (e == null) e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i0, i1);
                ldl.intervalRemoved(e);
            }
        }

    }
}
