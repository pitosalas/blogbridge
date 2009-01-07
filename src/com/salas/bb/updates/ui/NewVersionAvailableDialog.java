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
// $Id: NewVersionAvailableDialog.java,v 1.13 2006/05/31 10:53:16 spyromus Exp $
//

package com.salas.bb.updates.ui;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.component.UIFButton;
import com.salas.bb.updates.CheckResult;
import com.salas.bb.updates.Location;
import com.salas.bb.utils.uif.*;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Dialog to display information on new version available.
 */
public class NewVersionAvailableDialog extends AbstractDialog
{
    private static final MessageFormat FMT_SINGLE_SELECTION =
        new MessageFormat(Strings.message("updates.click.download.to.save.the.new.version.0.mb.to.your.desktop"));
    private static final MessageFormat FMT_MULTI_SELECTION =
        new MessageFormat(Strings.message("updates.click.download.to.save.selected.files.0.mb.to.your.desktop"));

    private CheckResult         checkResult;
    private Location[]          applicableDownloads;
    private boolean             mainPackageAvailable;

    private PackagesTableModel  packagesModel;
    private JTable              packagesTable;

    private JLabel              lbDownloadSuggestion;
    private JCheckBox           chShowAllDownloads;
    private UIFButton           btnDownload;
    private Location mainPackage;

    /**
     * Creates dialog.
     *
     * @param frame parent frame.
     */
    public NewVersionAvailableDialog(Frame frame)
    {
        super(frame, Strings.message("updates.new.version.dialog.title"), true);
    }

    /**
     * Creates header.
     *
     * @return header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("updates.new.version.dialog.title"),
            Strings.message("updates.new.version.dialog.header"));
    }

    /**
     * Builds dialog content.
     *
     * @return content component.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonBar(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds dialog main panel.
     *
     * @return content component.
     */
    private Component buildMainPanel()
    {
        boolean severalPackagesAvailable = applicableDownloads.length > 1;

        BBFormBuilder builder = new BBFormBuilder("250dlu");

        builder.append(getWordingComponent(), 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("updates.new.version.changes"), 1);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu");
        builder.append(getChangesTableComponent(), 1, CellConstraints.FILL, CellConstraints.FILL);

        // Show "Show all downloads" checkbox if several packages available.
        builder.appendRelatedComponentsGapRow(2);
        if (severalPackagesAvailable || (applicableDownloads.length == 1 && !mainPackageAvailable))
        {
            final Component detailsPanel = createPackagesDetailsPanel();

            if (mainPackageAvailable)
            {
                builder.append(chShowAllDownloads);
            } else
            {
                builder.append(Strings.message("updates.new.version.availble.downloads"), 1);
            }

            builder.appendRelatedComponentsGapRow(2);
            builder.append(detailsPanel);

            detailsPanel.setVisible(!mainPackageAvailable);

            chShowAllDownloads.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    detailsPanel.setVisible(chShowAllDownloads.isSelected());
                    pack();
                }
            });
        }

        builder.append(lbDownloadSuggestion, 1);
        builder.appendUnrelatedComponentsGapRow();

        return builder.getPanel();
    }

    /**
     * Creates panel with package details.
     *
     * @return panel with packages details.
     */
    private Component createPackagesDetailsPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("0:grow");

        builder.appendRow("50dlu");
        builder.append(getPackagesTableComponent(), 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.appendRelatedComponentsGapRow();

        return builder.getPanel();
    }

    /**
     * Creates icon component.
     *
     * @return component.
     */
    private Component getIconComponent()
    {
        return new JLabel(IconSource.getIcon("logo.small.icon"));
    }

    /**
     * Creates component with new version wording.
     *
     * @return component.
     */
    private Component getWordingComponent()
    {
        CheckResult result = getCheckResult();

        return ComponentsFactory.createWrappedMultilineLabel(
            MessageFormat.format(Strings.message("updates.new.version.wording"),
                new Object[] { result.getRecentVersion() }));
    }

    /**
     * Creates changes table component.
     *
     * @return table.
     */
    private Component getChangesTableComponent()
    {
        CheckResult result = getCheckResult();

        ChangesTableModel model = new ChangesTableModel(result.getChanges());
        JTable table = new JTable(model);
        table.setShowGrid(false);
        table.setTableHeader(null);
        UifUtilities.setTableColWidth(table, 0, 20);
        table.setDefaultRenderer(Integer.class, new ChangeTypeRenderer());
        table.setEnabled(false);

        JScrollPane pane = new JScrollPane(table);
        pane.getViewport().setBackground(table.getBackground());

        return pane;
    }

    /**
     * Returns packages table.
     *
     * @return component.
     */
    private Component getPackagesTableComponent()
    {
        JScrollPane pane = new JScrollPane(packagesTable);
        pane.getViewport().setBackground(packagesTable.getBackground());

        return pane;
    }

    /**
     * Returns check result.
     *
     * @return check result.
     */
    private CheckResult getCheckResult()
    {
        return checkResult;
    }

    /**
     * Builds dialog buttons bar.
     *
     * @return content component.
     */
    private Component buildButtonBar()
    {
        UIFButton laterButton = createCancelButton();
        laterButton.setText(Strings.message("updates.new.version.later"));

        return ButtonBarFactory.buildRightAlignedBar(laterButton, btnDownload);
    }

    /**
     * Shows dialog.
     *
     * @param aCheckResult result of updates check with new version info.
     *
     * @throws NullPointerException if check result isn't specified.
     */
    public void open(CheckResult aCheckResult)
    {
        if (aCheckResult == null) throw new NullPointerException(Strings.error("updates.unspecified.check.result"));

        checkResult = aCheckResult;
        Map allLocations = checkResult.getLocations();
        applicableDownloads = selectApplicableDownloads(allLocations);

        List applicableTypes = Location.getApplicableTypes();
        String mainPackageType = (String)applicableTypes.get(0);
        mainPackage = (Location)allLocations.get(mainPackageType);
        mainPackageAvailable =  mainPackage != null;

        initComponents();
        pack();

        super.open();
    }

    private void initComponents()
    {
        lbDownloadSuggestion = new JLabel();

        chShowAllDownloads = ComponentsFactory.createCheckBox(Strings.message("updates.new.version.show.all"));

        btnDownload = createOKButton(true);
        btnDownload.setText(Strings.message("updates.new.version.download"));
        btnDownload.setEnabled(getCheckResult().getLocations().size() > 0);

        packagesModel = new PackagesTableModel(applicableDownloads);
        packagesModel.addTableModelListener(new DownloadsSelectionListener());

        packagesTable = new JTable(packagesModel);
        packagesTable.setShowGrid(false);
        packagesTable.setTableHeader(null);
        UifUtilities.setTableColWidth(packagesTable, 0, 20);

        if (mainPackage != null) packagesModel.selectPackage(mainPackage);
        onDownloadsSelectionChange();
    }

    /**
     * Handles window events depending on the state of the <code>defaultCloseOperation</code>
     * property.
     *
     * @see #setDefaultCloseOperation
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED)
        {
            // Ensure that the dialog is correctly sized on opening
            pack();
        }
    }

    /**
     * Returns the list of applicable downloads.
     *
     * @param allLocations all available downloads.
     *
     * @return applicable list.
     */
    private static Location[] selectApplicableDownloads(Map allLocations)
    {
        Location[] locations = (Location[])allLocations.values().toArray(new Location[0]);

        return Location.selectApplicable(locations);
    }

    /**
     * Dialog resizing hook.
     *
     * @param content dialog content.
     */
    protected void resizeHook(JComponent content)
    {
//        Resizer.ONE2ONE.resizeDialogContent(content);
        super.resizeHook(content);
    }

    /**
     * Accepts only if some packages selected for download.
     */
    public void doAccept()
    {
        if (isPackagesSelected())
        {
            super.doAccept();
        } else
        {
            JOptionPane.showMessageDialog(this,
                Strings.message("updates.new.version.please.select.at.least.one.package"),
                Strings.message("updates.new.version.dialog.title"),
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Returns <code>TRUE</code> if some packages selected.
     *
     * @return <code>TRUE</code> if some packages selected.
     */
    private boolean isPackagesSelected()
    {
        return getSelectedLocations().length > 0;
    }

    /**
     * Returns list of selected locations for download.
     *
     * @return list of selected locations for download.
     */
    public Location[] getSelectedLocations()
    {
        return packagesModel.getSelectedLocations();
    }

    private void onDownloadsSelectionChange()
    {
        Location[] selected = getSelectedLocations();
        boolean nothingSelected = selected.length == 0;

        String msg;

        if (nothingSelected)
        {
            msg = applicableDownloads.length > 0
                ? Strings.message("updates.new.version.validation.nothing.selected")
                : Strings.message("updates.new.version.validation.nothing.available");
        } else
        {
            MessageFormat format;
            if (selected.length == 1)
            {
                format = FMT_SINGLE_SELECTION;
            } else
            {
                format = FMT_MULTI_SELECTION;
            }

            msg = format.format(new Object[] { new Float(calculateTotalSizeInMegs(selected)) });
        }

        btnDownload.setEnabled(!nothingSelected);
        lbDownloadSuggestion.setText(msg);
    }

    /**
     * Returns cummulative size of downloads in megabytes.
     *
     * @param locations locations to sum.
     *
     * @return megabytes.
     */
    private static double calculateTotalSizeInMegs(Location[] locations)
    {
        long bytes = 0;

        for (int i = 0; i < locations.length; i++)
        {
            Location location = locations[i];
            bytes += location.getSize();
        }

        return bytes / 1024 / 1024;
    }

    /**
     * Listener of downloads selections changes.
     */
    private class DownloadsSelectionListener implements TableModelListener
    {
        /**
         * This fine grain notification tells listeners the exact range of cells, rows, or columns that
         * changed.
         */
        public void tableChanged(TableModelEvent e)
        {
            onDownloadsSelectionChange();
        }
    }
}
