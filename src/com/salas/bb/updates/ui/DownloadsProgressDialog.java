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
// $Id: DownloadsProgressDialog.java,v 1.11 2006/12/13 19:59:28 spyromus Exp $
//

package com.salas.bb.updates.ui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.updates.Location;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.IStreamProgressListener;
import com.salas.bb.utils.net.URLInputStream;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Downloads progress dialog.
 */
public class DownloadsProgressDialog extends AbstractDialog
{
    private IStreamProgressListener progressListener;
    private Location[]              locations;
    private int                     currentDownload;
    private long                    currentRead;
    private long                    currentTotal;
    private long                    overallTotal;
    private long                    overallRead;

    private JProgressBar            prOverall;
    private JLabel                  lbCurrent;
    private JProgressBar            prCurrent;
    private JButton                 btnCancel;
    private JButton                 btnExit;
    private JTextArea               lbReport;
    private JPanel                  pnlReport;
    private JCheckBox               chStartInstallation;
    private boolean                 finished;
    private boolean                 exitBlogBridge;

    private Runnable actInstall;
    private Runnable actCancel;
    private Runnable actExit;

    /**
     * Creates downloads progress dialog.
     *
     * @param frame         parent frame.
     * @param aLocations    list of locations which are going to be downloaded.
     */
    public DownloadsProgressDialog(Frame frame, Location[] aLocations)
    {
        super(frame, Strings.message("updates.downloading.dialog.title"), false);

        locations = aLocations;
        currentDownload = -1;
        progressListener = new ProgressListener();
        finished = false;
        exitBlogBridge = false;

        overallRead = 0;
        overallTotal = 0;
        for (int i = 0; i < aLocations.length; i++) overallTotal += aLocations[i].getSize();

        initComponents();
        pack();

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Returns <code>TRUE</code> if a user chose to exit BB.
     *
     * @return <code>TRUE</code> to exit.
     */
    public boolean isExitBlogBridge()
    {
        return exitBlogBridge;
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

        if (e.getID() == WindowEvent.WINDOW_OPENED) pack();
    }

    /**
     * Opens the dialog.
     *
     * @param installAction installation code.
     * @param cancelAction  cancelling code.
     * @param exitAction    exiting code.
     */
    public void open(Runnable installAction, Runnable cancelAction, Runnable exitAction)
    {
        actInstall = installAction;
        actCancel = cancelAction;
        actExit = exitAction;

        super.open();
    }

    /**
     * Creates page content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(createMainPanel(), BorderLayout.CENTER);
        panel.add(createButtonBar(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates main panel.
     *
     * @return main panel.
     */
    private Component createMainPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("max(p;200dlu)");
        builder.setDefaultDialogBorder();

        String msg = Strings.message(SystemUtils.IS_OS_MAC
            ? "updates.downloading.wording.mac"
            : "updates.downloading.wording");

        builder.appendRow("pref:grow");
        builder.append(ComponentsFactory.createWrappedMultilineLabel(msg), 1);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("updates.downloading.overall.progress"), 1);
        builder.append(prOverall);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(lbCurrent);
        builder.append(prCurrent);
        builder.append(pnlReport);
        builder.append(chStartInstallation);
        builder.appendUnrelatedComponentsGapRow(2);

        return builder.getPanel();
    }

    /** Initializes components. */
    private void initComponents()
    {
        lbReport = ComponentsFactory.createWrappedMultilineLabel("");

        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.appendRelatedComponentsGapRow(1);
        builder.append(lbReport);
        pnlReport = builder.getPanel();
        pnlReport.setVisible(false);

        prOverall = new JProgressBar();
        lbCurrent = new JLabel();
        prCurrent = new JProgressBar();

        prOverall.setMinimum(0);
        prOverall.setMaximum(100);
        prOverall.setValue(0);

        prCurrent.setMinimum(0);
        prCurrent.setMaximum(100);

        chStartInstallation = ComponentsFactory.createCheckBox(Strings.message("updates.downloading.close.blogbridge.and.install.new.version"));
        chStartInstallation.setVisible(false);

        nextDownload();
    }

    /**
     * Creates button bar.
     *
     * @return button bar.
     */
    private Component createButtonBar()
    {
        btnExit = createOKButton(false);
        btnExit.setText(Strings.message("updates.downloading.exit"));
        btnExit.setVisible(false);
        btnCancel = createCancelButton();

        ButtonBarBuilder builder = new ButtonBarBuilder();

        builder.getPanel().setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);
        builder.addGlue();
        builder.addFixed(btnExit);
        builder.addRelatedGap();
        builder.addFixed(btnCancel);

        return builder.getPanel();
    }

    /**
     * Returns progress listener which is going to listen for downloads.
     *
     * @return listener.
     */
    public IStreamProgressListener getProgressListener()
    {
        return progressListener;
    }

    /**
     * Switches controls to
     */
    public void nextDownload()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                currentDownload++;

                if (currentDownload < locations.length)
                {
                    resetCurrentProgress();
                    setCurrentLabel(locations[currentDownload].getDescription());
                } else
                {
                    prCurrent.setValue(100);
                    btnCancel.setText(Strings.message("updates.downloading.close"));
                }
            }
        });
    }

    /**
     * Resets the position of currently downloaded file progress.
     */
    private void resetCurrentProgress()
    {
        prCurrent.setValue(0);
        currentRead = 0;
    }

    /**
     * Sets the downloading details label.
     *
     * @param aDescription description.
     */
    private void setCurrentLabel(String aDescription)
    {
        lbCurrent.setText(MessageFormat.format(
            Strings.message("updates.downloading.status"), new Object[] { aDescription }));
    }

    /**
     * Invoked when downloading starts. Can be invoked several time per
     * single download.
     *
     * @param aLength length of resource.
     */
    private void onDownloadStarted(final long aLength)
    {
        currentTotal = aLength;
    }

    /**
     * Invoked when download finishes with error.
     */
    private void onDownloadFailed()
    {
        onDownloadFinished();
    }

    /**
     * Invoked when download finishes successfully.
     */
    private void onDownloadFinished()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                prCurrent.setValue(prCurrent.getMaximum());
            }
        });
    }

    /**
     * Invoked when some bytes have been read.
     *
     * @param aBytes number of bytes read.
     */
    private void onRead(final long aBytes)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                overallRead += aBytes;
                currentRead += aBytes;
                prCurrent.setValue((int)(currentRead * 100 / currentTotal));
                prOverall.setValue((int)(overallRead * 100 / overallTotal));
            }
        });
    }

    public void doAccept()
    {
        super.doAccept();
        actExit.run();
    }

    public synchronized void doCancel()
    {
        if (finished)
        {
            super.doAccept();
            if (shouldStartInstallation()) actInstall.run();
        } else
        {
            super.doCancel();
            actCancel.run();
        }
    }

    /**
     * Reports complete download status.
     *
     * @param aSuccessful   list of successful downloads.
     * @param aFailed       list of failed downloads.
     */
    public synchronized void allDone(List aSuccessful, List aFailed)
    {
        String msg;

        int sucSize = aSuccessful.size();
        int failSize = aFailed.size();

        if (sucSize > 0)
        {
            msg = MessageFormat.format(Strings.message("updates.downloading.successfully.downloaded.0.to.your.desktop"),
                new Object[] { (sucSize == 1)
                    ? Strings.message("updates.downloading.package")
                    : MessageFormat.format(Strings.message("updates.downloading.0.packages"),
                        new Object[] { new Integer(sucSize) }) });
        } else msg = "";

        if (failSize > 0)
        {
            msg += MessageFormat.format(Strings.message("updates.downloading.failed.to.download.0.packages"),
                new Object[] { new Integer(failSize) });
        }

        lbReport.setText(msg);
        pnlReport.setVisible(true);

        Location mainPackage = Location.getMainPackage(aSuccessful);
        if (SystemUtils.IS_OS_WINDOWS && mainPackage != null)
        {
            chStartInstallation.setVisible(true);
        } else
        {
            btnExit.setVisible(true);
        }

        pack();

        finished = true;
    }

    /**
     * Returns <code>TRUE</code> if user is willing to start installation.
     *
     * @return <code>TRUE</code> if user is willing to start installation.
     */
    public boolean shouldStartInstallation()
    {
        return chStartInstallation.isSelected();
    }

    /**
     * Stream progress listener which is used to update dialog progress bars.
     */
    private class ProgressListener implements IStreamProgressListener
    {
        /**
         * Indicates the the source is being connected.
         *
         * @param source source.
         */
        public void connecting(URLInputStream source)
        {
        }

        /**
         * Indicates that the source has been successfully connected.
         *
         * @param source source.
         * @param length length of the resource (-1 if unknown).
         */
        public void connected(URLInputStream source, long length)
        {
            onDownloadStarted(length);
        }

        /**
         * Indicates that there's an error happened during reading of stream.
         * (We already did attempts to recover).
         *
         * @param source source.
         * @param ex     cause of error.
         */
        public void errored(URLInputStream source, IOException ex)
        {
            onDownloadFailed();
        }

        /**
         * Indicates that the stream has been finished.
         *
         * @param source source.
         */
        public void finished(URLInputStream source)
        {
            onDownloadFinished();
        }

        /**
         * Indicates that some bytes has been read.
         *
         * @param source source.
         * @param bytes  bytes.
         */
        public void read(URLInputStream source, long bytes)
        {
            onRead(bytes);
        }
    }
}
