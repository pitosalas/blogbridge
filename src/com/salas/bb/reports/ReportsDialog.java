// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: ReportsDialog.java,v 1.8 2008/04/01 12:51:27 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swingworker.SwingWorker;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ProgressPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Reports dialog.
 */
class ReportsDialog extends AbstractDialog
{
    /** Provider of the report data. */
    private final IReportDataProvider provider;
    /** Click callback reports can use. */
    private final IClickCallback clickCallback;

    /** Progress panel that is used to indicate when the report is being created. */
    private final ProgressPanel progressPanel = new ProgressPanel(Strings.message("collecting.data"));

    /** The container with the page. */
    private final JScrollPane pageContainer;
    /** Report selector. */
    private final JComboBox cbReport;

    /**
     * Creates the dialog.
     *
     * @param frame         parent frame.
     * @param provider      provider of report data.
     * @param clickCallback click callback reports can use.
     */
    public ReportsDialog(Frame frame, IReportDataProvider provider, IClickCallback clickCallback)
    {
        super(frame);
        this.provider = provider;
        this.clickCallback = clickCallback;

        pageContainer = new JScrollPane();
        cbReport = new JComboBox();
        cbReport.addItemListener(new ItemListener()
        {
            /**
             * Invoked when a report is selected.
             *
             * @param e event.
             */
            public void itemStateChanged(ItemEvent e)
            {
                onReportSelected((IReport)cbReport.getSelectedItem());
            }
        });

        cbReport.addItem(new ArticlesByHourReport());
        cbReport.addItem(new ArticlesByDayReport());
        cbReport.addItem(new MostVisitedGuidesReport());
        cbReport.addItem(new MostReadGuidesReport());
        cbReport.addItem(new MostPinnedGuidesReport());
        cbReport.addItem(new MostVisitedFeedsReport());
        cbReport.addItem(new MostReadFeedsReport());
        cbReport.addItem(new MostPinnedFeedsReport());
        cbReport.addItem(new NoTrafficVisibleFeedsReport());
        cbReport.addItem(new FeedsInMultipleGuidesReport());

        // Set dialog attributes
        pageContainer.setPreferredSize(new Dimension(500, 300));
    }

    /**
     * Builds the content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildControlPanel(), BorderLayout.NORTH);
        panel.add(pageContainer, BorderLayout.CENTER);
        panel.add(buildButtonBar(), BorderLayout.SOUTH);

        return panel;
    }

    private JComponent buildButtonBar()
    {
        JButton btnReset = new JButton(new ResetAction());
        JPanel bar = ButtonBarFactory.buildHelpBar(btnReset, createCloseButton(true));
        bar.setBorder(Borders.createEmptyBorder(
            LayoutStyle.getCurrent().getDialogMarginY(), Sizes.dluX(0),
            Sizes.dluY(0), Sizes.dluX(0)));

        return bar;
    }

    /**
     * Creates a control panel.
     *
     * @return panel.
     */
    private Component buildControlPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, p, 0:grow");
        builder.append(Strings.message("report.choose.report"), cbReport);
        builder.appendRelatedComponentsGapRow();
        return builder.getPanel();
    }

    /**
     * Invoked when the report is selected.
     *
     * @param report report.
     */
    private void onReportSelected(IReport report)
    {
        if (!report.isDataInitialized())
        {
            showProgressPage();
            startCollectingData(report);
        } else
        {
            showReportPage(report);
        }
    }

    /**
     * Shows the progress page.
     */
    private void showProgressPage()
    {
        pageContainer.setViewportView(progressPanel);

        // TODO Disable the selector
    }

    /**
     * Shows the report page.
     *
     * @param report page.
     */
    private void showReportPage(IReport report)
    {
        report.layoutView();
        pageContainer.setViewportView(report.getReportPage());

        // TODO: Enable the selector
    }

    /**
     * Starts the worker for the report, and shows the report page
     * upon completion.
     *
     * @param report report to process.
     */
    private void startCollectingData(final IReport report)
    {
        SwingWorker<Object, Integer> worker = new SwingWorker<Object, Integer>()
        {
            /**
             * Invoked in the background thread.
             *
             * @return the result.
             *
             * @throws Exception if anything goes wrong.
             */
            protected Object doInBackground()
                throws Exception
            {
                report.setClickCallback(clickCallback);
                report.initializeData(provider);
                return null;
            }

            /** Invoked in EDT when processing is finished. */
            @Override
            protected void done()
            {
                showReportPage(report);
            }
        };
        worker.execute();
    }

    /** Resets the data. */
    private class ResetAction extends AbstractAction
    {
        /** Creates an action. */
        public ResetAction()
        {
            super(Strings.message("report.reset.data"));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            provider.reset();
            doClose();
        }
    }
}
