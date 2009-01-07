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
// $Id: ProgressDialog.java,v 1.10 2006/05/31 10:39:45 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.ComponentsFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Synchronization progress and outcome dialog. Dialog is automatically shown when process started.
 * It has OK button which is disabled until the end of the processing.
 * <p/>
 * The typical sequence is:
 * <pre>
 *   IProgressListener listener = createListenerDialog(mainFrame);
 *
 *   listener.processStarted("Loading some stuff...", 3);
 *   listener.processStep("Step 1");
 *   listener.processStep("Step 2");
 *   listener.processStep("Step 3");
 *   listener.processFinished("During the process we made...");
 * </pre>
 */
public final class ProgressDialog extends AbstractDialog implements IProgressListener
{
    private static final String COLS = "150dlu:grow";
    private static final String ROWS = "75dlu:grow, 2dlu, pref, 2dlu, pref, 14dlu";
    private static final FormLayout LAYOUT = new FormLayout(COLS, ROWS);
    private static final CellConstraints CC = new CellConstraints();

    private static final String THREAD_NAME = "ProgressDialog.processStarted";

    private JButton         btnOk;
    private JTextArea       taInfo;
    private JLabel          lbCurrentProcess;
    private JProgressBar    pbProgress;

    /**
     * Flag showing TRUE when process started.
     */
    private volatile boolean processStarted = false;

    /**
     * Current step number.
     */
    private volatile int currentStep = -1;

    /**
     * Creates dialog.
     *
     * @param owner owner frame.
     * @param title title of the dialog.
     */
    private ProgressDialog(Frame owner, String title)
    {
        super(owner, title);
        setModal(true);

        initComponents();
    }

    /**
     * Creates progress dialog and returns listener.
     *
     * @param owner owner of the dialog.
     * @param title title of the dialog.
     *
     * @return listener interface.
     */
    public static IProgressListener createListenerDialog(Frame owner, String title)
    {
        return new ProgressDialog(owner, title);
    }

    /**
     * Builds content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildMainPanel(), BorderLayout.CENTER);
        content.add(buildButtonsBar(), BorderLayout.SOUTH);

        return content;
    }

    // Initialize components
    private synchronized void initComponents()
    {
        taInfo = ComponentsFactory.createWrappedMultilineLabel(Constants.EMPTY_STRING);

        lbCurrentProcess = new JLabel();
        pbProgress = new JProgressBar();
        btnOk = createOKButton(true);
    }

    // Main panel
    private Component buildMainPanel()
    {
        JPanel main = new JPanel(LAYOUT);

        main.add(taInfo, CC.xy(1, 1, "f, t"));
        main.add(lbCurrentProcess, CC.xy(1, 3));
        main.add(pbProgress, CC.xy(1, 5));

        return main;
    }

    // Buttons
    private Component buildButtonsBar()
    {
        return ButtonBarFactory.buildOKBar(btnOk);
    }

    // Resizes component using our policy
    protected void resizeHook(JComponent component)
    {
        Resizer.REVERSE_SQRT.resize(component);
    }

    /**
     * Set the cursor image to a specified cursor.
     *
     * @param cursor cursor.
     */
    public void setCursor(Cursor cursor)
    {
        super.setCursor(cursor);
        taInfo.setCursor(cursor);
    }

    // ---------------------------------------------------------------------------------------------
    // IProgressListener implementation
    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when step of overall process started.
     *
     * @param stepName step name.
     */
    public synchronized void processStep(final String stepName)
    {
        if (!processStarted) processStarted(Strings.message("service.sync.process.started"), -1);

        currentStep++;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                lbCurrentProcess.setText(stepName);
            }
        });
    }

    /** Invoked when started step is completed. */
    public void processStepCompleted()
    {
        final int finishedStep = currentStep;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                pbProgress.setValue(finishedStep);
            }
        });
    }

    /**
     * Invoked when process started.
     *
     * @param information information to display.
     * @param steps       steps in the process.
     */
    public synchronized void processStarted(final String information, final int steps)
    {
        taInfo.setText(information);
        if (steps > 0)
        {
            pbProgress.setIndeterminate(false);
            pbProgress.setMinimum(0);
            pbProgress.setMaximum(steps);
        } else
        {
            pbProgress.setIndeterminate(true);
        }

        processStarted = true;
        currentStep = 0;
        btnOk.setEnabled(false);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Thread thread = new Thread(THREAD_NAME)
        {
            public void run()
            {
                open();
            }
        };

        thread.start();
    }

    /**
     * Invoked when process finished.
     *
     * @param information information to display or NULL to leave previous message.
     */
    public synchronized void processFinished(final String information)
    {
        processStarted = false;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                lbCurrentProcess.setText(Strings.message("service.sync.completed"));

                if (information != null) taInfo.setText(information);
                btnOk.setEnabled(true);
                setCursor(null);
            }
        });
    }
}
