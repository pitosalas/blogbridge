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
// $Id: InstallationProgressDialog.java,v 1.6 2006/07/12 11:33:54 spyromus Exp $
//

package com.salas.bb.installation;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

/**
 * Installation progress dialog shows how installation is going.
 */
public class InstallationProgressDialog extends AbstractDialog
{
    private Object watcher;
    private String[] steps;
    private StepPanel current = null;

    private JButton btnFunction = createCancelButton();
    private ArrayList stepPanels = new ArrayList();

    /**
     * Creates dialog.
     *
     * @param aSteps        list of steps to indicate.
     * @param aWatcher      object which will be locked while dialog is opened.
     */
    public InstallationProgressDialog(String[] aSteps, Object aWatcher)
    {
        super((Frame)null);
        setModal(false);

        watcher = aWatcher;
        steps = aSteps;
    }

    /**
     * Creates the panel's header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("installation.progress.dialog.title"),
            Strings.message("installation.progress.dialog.header"));
    }

    /**
     * Builds content.
     */
    protected JComponent buildContent()
    {
        FormLayout layout = new FormLayout("15dlu, 150dlu:grow, 15dlu",
            "100dlu:grow, 2dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();

        builder.add(buildStepsPanel(), cc.xy(2, 1));
        builder.add(buildButtonBar(), cc.xyw(1, 3, 3));

        return builder.getPanel();
    }

    /**
     * Builds panel of steps.
     */
    private JPanel buildStepsPanel()
    {
        JPanel panel = new JPanel(new GridLayout(steps.length, 1));

        for (int i = 0; i < steps.length; i++)
        {
            final StepPanel stepPanel = new StepPanel(steps[i]);
            panel.add(stepPanel);
            stepPanels.add(stepPanel);
        }

        if (steps.length > 0) current = (StepPanel)stepPanels.get(0);

        return panel;
    }

    /**
     * Builds button bar.
     */
    private JPanel buildButtonBar()
    {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        bar.add(btnFunction);
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return bar;
    }

    /**
     * Marks current step as failed and procedes to the next one.
     */
    public void failedStep()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                makeStep(false);
            }
        });
    }

    /**
     * Marks current step as finished and procedes to the next one.
     */
    public void succeedStep()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                makeStep(true);
            }
        });
    }

    /**
     * Marks current step as finished and procedes to the next one.
     */
    private void makeStep(boolean succeed)
    {
        int next = 0;
        if (current != null)
        {
            current.setFinished(true, succeed);
            next = stepPanels.indexOf(current) + 1;
        }

        current = (next < stepPanels.size()) ? (StepPanel)stepPanels.get(next) : null;
    }

    /**
     * Opens dialog.
     */
    public void open()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                InstallationProgressDialog.super.open();
            }
        });
    }

    /**
     * Closes dialog and notifies watcher about it.
     */
    public void close()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                InstallationProgressDialog.super.close();
            }
        });

        synchronized (watcher)
        {
            watcher.notifyAll();
        }
    }

    /**
     * Makes button have 'OK' caption instead of 'Cancel'.
     */
    public void finish()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                btnFunction.setText("OK");
                btnFunction.setMnemonic('O');
            }
        });
    }

    /**
     * Pannel of the single step.
     */
    private static class StepPanel extends JPanel
    {
        private static final Border ICON_BORDER = BorderFactory.createEmptyBorder(5, 0, 5, 10);

        private static final Icon ICON_UNFINISHED =
            ResourceUtils.getIcon(ResourceID.ICON_INSTALLER_STEP_UNFINISHED);

        private static final Icon ICON_FINISHED =
            ResourceUtils.getIcon(ResourceID.ICON_INSTALLER_STEP_FINISHED);

        private static final Icon ICON_FAILED =
            ResourceUtils.getIcon(ResourceID.ICON_INSTALLER_STEP_FAILED);

        private JLabel lbIcon = new JLabel();
        private JLabel lbText = new JLabel();

        /**
         * Creates a new <code>JPanel</code> with a double buffer and a flow layout.
         *
         * @param text step text.
         */
        public StepPanel(String text)
        {
            setFinished(false, false);
            lbText.setText(text);

            lbIcon.setBorder(ICON_BORDER);

            setLayout(new BorderLayout());
            add(lbIcon, BorderLayout.WEST);
            add(lbText, BorderLayout.CENTER);
        }

        /**
         * Sets the state of current step.
         *
         * @param b <code>true</code> to mark step as finished.
         * @param successfully shows if finish was successful.
         */
        public void setFinished(boolean b, boolean successfully)
        {
            lbIcon.setIcon(b ? successfully ? ICON_FINISHED : ICON_FAILED : ICON_UNFINISHED);
            lbText.setEnabled(b);
        }
    }
}
