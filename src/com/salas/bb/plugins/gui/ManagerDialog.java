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
// $Id: ManagerDialog.java,v 1.12 2007/04/06 10:23:26 spyromus Exp $
//

package com.salas.bb.plugins.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.plugins.Manager;
import com.salas.bb.plugins.domain.IPlugin;
import com.salas.bb.plugins.domain.Package;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Packages / plug-ins manager dialog box.
 */
public class ManagerDialog extends AbstractDialog
{
    private PluginsTable tblPackages;
    private JButton btnUninstall;
    private JButton btnRescan;
    private JButton btnInstall;
    private UIFButton btnClose;
    private JTextArea taDescription;

    /**
     * Creates the dialog.
     *
     * @param frame parent frame.
     */
    public ManagerDialog(Frame frame)
    {
        super(frame, Strings.message("plugin.manager.dialog.title"));
    }

    @Override
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("plugin.manager.dialog.title"),
            Strings.message("plugin.manager.dialog.header"),
            getIcon());
    }

    /**
     * Returns the icon.
     *
     * @return icon.
     */
    public static ImageIcon getIcon()
    {
        return IconSource.getIcon("plugin.manager.icon");
    }

    /**
     * Builds main content area.
     *
     * @return the panel.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButonsBar(), BorderLayout.SOUTH);

        // Finally, select the first plug-in
        if (!highlightFirstIfPresent())
        {
            onSelectionChange();
        }

        return panel;
    }

    /**
     * Highlights the first package if present.
     *
     * @return <code>TRUE</code> if highlighted.
     */
    private boolean highlightFirstIfPresent()
    {
        boolean present = tblPackages.getModel().getSize() > 0;
        if (present) tblPackages.setSelectedIndex(0);
        return present;
    }

    /**
     * Returns buttons bar.
     *
     * @return bar.
     */
    private Component buildButonsBar()
    {
        btnRescan = new JButton(new RescanAction());
        btnInstall = new JButton(new InstallAction());
        btnUninstall = new JButton(new UninstallAction());
        btnClose = createCloseButton(false);

        BBFormBuilder b = new BBFormBuilder("0:grow, p, 7dlu, p, 2dlu, p, 7dlu, p");
        b.nextColumn();
        b.append(btnRescan);
        b.append(btnInstall);
        b.append(btnUninstall);
        b.append(btnClose);

        JPanel p = b.getPanel();
        p.setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);

        return p;
    }

    /**
     * Returns main panel.
     *
     * @return panel.
     */
    private Component buildMainPanel()
    {
        tblPackages = new PluginsTable();
        tblPackages.setInstalledPackages(Manager.getInstalledPackages());
        tblPackages.setSelectedPackages(Manager.getEnabledPackages());
        tblPackages.addListSelectionListener(new PluginTableListener());

        taDescription = new JTextArea();
        Color bg = taDescription.getBackground();
        taDescription.setEditable(false);
        taDescription.setBackground(bg);
        UifUtilities.smallerFont(taDescription);

        BBFormBuilder b = new BBFormBuilder("p:grow");
        b.setDefaultDialogBorder();

        b.append(Strings.message("plugin.manager.plugins"), 1);
        b.appendRelatedComponentsGapRow(2);
        b.appendRow("100dlu:grow");
        b.append(new JScrollPane(tblPackages), 1, CellConstraints.FILL, CellConstraints.FILL);

        b.appendUnrelatedComponentsGapRow(2);
        b.append(Strings.message("plugin.manager.details"), 1);
        b.appendRelatedComponentsGapRow(2);
        b.appendRow("50dlu");
        b.append(new JScrollPane(taDescription), 1, CellConstraints.FILL, CellConstraints.FILL);

        JTextArea lbNote = ComponentsFactory.createWrappedMultilineLabel(Strings.message("plugin.manager.warning"));
        UifUtilities.smallerFont(lbNote);
        b.append(lbNote);
        b.appendRelatedComponentsGapRow();

        return b.getPanel();
    }

    @Override
    protected void resizeHook(JComponent component)
    {
        Resizer.ONE2ONE.resizeDialogContent(component);
    }

    /**
     * Shows the dialog with the list of enabled packages provided.
     *
     * @return <code>TRUE</code> if something has changed. 
     */
    public boolean openDialog()
    {
        java.util.List<Package> before = Manager.getEnabledPackages();
        super.open();
        java.util.List<Package> after = tblPackages.getSelectedPackages();

        boolean changed = isChanged(before, after);
        if (changed) Manager.setEnabledPackages(after);

        return changed;
    }

    /**
     * Returns <code>TRUE</code> if the list of packages has changed.
     *
     * @param before    before the action.
     * @param after     after the action.
     *
     * @return <code>TRUE</code> if the list of packages has changed.
     */
    private static boolean isChanged(java.util.List<Package> before, java.util.List<Package> after)
    {
        if (after == null) return false;
        if (before == null) return true;

        boolean changed = true;
        if (before.size() == after.size())
        {
            changed = false;
            for (Package pack : after)
            {
                if (!before.contains(pack))
                {
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }

    /**
     * Enables / disables components.
     *
     * @param en <code>TRUE</code> to enable.
     */
    private void enableComponents(boolean en)
    {
        btnClose.setEnabled(en);
        btnInstall.setEnabled(en);
        btnRescan.setEnabled(en);
        btnUninstall.setEnabled(en && (tblPackages.getSelectedIndex() != -1));

        tblPackages.setEnabled(en);
        taDescription.setEnabled(en);
    }

    /**
     * Calls the {@link #enableComponents(boolean)} from EDT.
     *
     * @param en <code>TRUE</code> to enable.
     */
    private void enableComponentsEDT(final boolean en)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                enableComponents(en);
            }
        });
    }
    // ------------------------------------------------------------------------
    // Listeners & Actions
    // ------------------------------------------------------------------------

    /**
     * Creates executor service if necessary and submits a task.
     *
     * @param task task to start.
     */
    private synchronized void backgroundTask(Runnable task)
    {
        Thread th = new Thread(task, task.getClass().getName());
        th.setDaemon(true);
        th.start();
    }

    /**
     * Invoked when the selection state of the plug-in table changes.
     */
    private void onSelectionChange()
    {
        Package pckg = tblPackages.getHighlightedPackage();
        btnUninstall.setEnabled(pckg != null);

        String descr = null;
        if (pckg != null)
        {
            // Collect plug-in stats
            Map<String, Integer> cnts = new HashMap<String, Integer>();
            for (IPlugin plugin : pckg)
            {
                String typeName = plugin.getTypeName();
                Integer cnt = cnts.get(typeName);
                if (cnt == null) cnt = 0;
                cnts.put(typeName, cnt + 1);
            }

            // Build the list
            int i = 0;
            String[] contents = new String[cnts.size()];
            for (Map.Entry<String, Integer> entry : cnts.entrySet())
            {
                String type = entry.getKey();
                int cnt = entry.getValue();

                contents[i++] = type + (cnt > 1 ? " (" + cnt + ")" : "");
            }

            // Create the description text
            descr = MessageFormat.format("{0}\n\nAuthor: {1} {2}\nContents: {3}",
                pckg.getDescription(), pckg.getAuthor(), pckg.getEmail(), StringUtils.join(contents, ", "));
        }

        taDescription.setText(descr);
    }

    /** Reloads the list of packages. */
    private void reloadPackagesList()
    {
        final java.util.List<Package> packages = Manager.reloadInstalledPackages();
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                tblPackages.setInstalledPackages(packages);
            }
        });
    }

    /**
     * Listener of the list selection.
     */
    private class PluginTableListener implements ListSelectionListener
    {
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public void valueChanged(ListSelectionEvent e)
        {
            onSelectionChange();
        }
    }

    /**
     * Rescans the directory of plug-ins.
     */
    private class RescanAction extends AbstractAction
    {
        /**
         * Creates action.
         */
        public RescanAction()
        {
            super(Strings.message("plugin.manager.rescan"));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event object.
         */
        public void actionPerformed(ActionEvent e)
        {
            backgroundTask(new RescanTask());
        }

        /** The task itself. */
        private class RescanTask implements Runnable
        {
            /**
             * Called when the task is staring.
             */
            public void run()
            {
                enableComponentsEDT(false);

                try
                {
                    reloadPackagesList();
                } finally
                {
                    enableComponentsEDT(true);
                }
            }
        }
    }

    /** Install action shows the file picker and then installs the selected file. */
    private class InstallAction extends AbstractAction
    {
        /**
         * Creates the action.
         */
        public InstallAction()
        {
            super(Strings.message("plugin.manager.install"));
        }

        /**
         * Invoked when the action is performed.
         *
         * @param e event object.
         */
        public void actionPerformed(ActionEvent e)
        {
            Preferences prefs = Preferences.userNodeForPackage(ManagerDialog.class);
            String lastPath = prefs.get("last.path", null);

            JFileChooser chooser = new JFileChooser();
            if (lastPath != null) chooser.setCurrentDirectory(new File(lastPath));

            chooser.setFileFilter(new FileFilter()
            {
                public boolean accept(File f)
                {
                    return f != null && (f.isDirectory() || (f.isFile() && f.getName().matches(".*\\.(jar|zip)\\s*$")));
                }

                public String getDescription()
                {
                    return "Plug-in Packages";
                }
            });
            int res = chooser.showOpenDialog(ManagerDialog.this);

            if (res == JFileChooser.APPROVE_OPTION)
            {
                prefs.put("last.path", chooser.getCurrentDirectory().getAbsolutePath());
                backgroundTask(new InstallTask(chooser.getSelectedFile()));
            }
        }

        /** Installation task. */
        private class InstallTask implements Runnable
        {
            private final File file;

            /**
             * Creates the task.
             *
             * @param file file to install.
             */
            public InstallTask(File file)
            {
                this.file = file;
            }

            /** Invoked when the task runs. */
            public void run()
            {
                enableComponentsEDT(false);

                try
                {
                    final String error = Manager.install(file);
                    if (error != null)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                JOptionPane.showMessageDialog(ManagerDialog.this, error,
                                    (String)InstallAction.this.getValue(Action.NAME),
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } else
                    {
                        reloadPackagesList();

                        // Select package
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                tblPackages.selectPackage(file);
                            }
                        });
                    }
                } finally
                {
                    enableComponentsEDT(true);
                }
            }
        }
    }

    /** Uninstall action that uninstalls the highlighted packages. */
    private class UninstallAction extends AbstractAction
    {
        /**
         * Creates the action.
         */
        public UninstallAction()
        {
            super(Strings.message("plugin.manager.uninstall"));
        }

        /**
         * Invoked when action occurs.
         *
         * @param e event object.
         */
        public void actionPerformed(ActionEvent e)
        {
            Package pkg = tblPackages.getHighlightedPackage();
            if (pkg != null)
            {
                Manager.uninstall(pkg);
                reloadPackagesList();
                highlightFirstIfPresent();
            }
        }
    }
}
