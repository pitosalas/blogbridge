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
// $Id: Dialog.java,v 1.26 2007/09/11 18:59:49 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.jgoodies.binding.adapter.*;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.search.ResultGroup;
import com.salas.bb.search.ResultGroupPanel;
import com.salas.bb.search.ResultItem;
import com.salas.bb.search.ResultsList;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swingworker.SwingWorker;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.ProgressPanel;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.mainframe.StarsSelectionComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * What's Hot dialog box.
 */
public class Dialog extends AbstractDialog
{
    private static final String PROP_WH_STARZ = "wh.starz";
    private static final String PROP_WH_UNREAD_ONLY = "wh.unreadOnly";
    private static final String PROP_WH_TIME_OPTION = "wh.timeOption";

    /** Minimum dialog size. */
    private static final Dimension  MIN_SIZE = new Dimension(500, 350);

    /** Group separator background gradient colors. */
    private static final Color GROUP_COLOR_1 = Color.decode("#eebb6b");
    private static final Color GROUP_COLOR_2 = Color.decode("#f3b149");

    private ResultsList             itemsList;
    private ListModel               model;

    private StarsSelectionComponent starz;
    private JCheckBox               chOnlyUnread;
    private JComboBox               cbTimeOptions;

    /** Progress panel. Used to show the progress of the search. */
    private ProgressPanel           pnlProgress;
    /** Content panel to update with new views. */
    private JScrollPane             scrollPanel;
    /** Setup button. */
    private JButton                 btnSetup;

    /** Guides set. */
    private final GuidesSet guidesSet;

    /**
     * Creates search dialog.
     *
     * @param owner     dialog's parent frame.
     * @param engine    engine to use.
     * @param set       guides set to pick the guides from.
     * @param listener  selection listener.
     */
    public Dialog(Frame owner, Engine engine, final GuidesSet set, ActionListener listener)
    {
        super(owner, Strings.message("whatshot.dialog.title"));
        guidesSet = set;

        pnlProgress = new ProgressPanel(Strings.message("whatshot.inprogress"));
        scrollPanel = buildResultsPanel();

        setModal(false);

        // Only unread selector
        Preferences prefs = Application.getUserPreferences();
        ValueModel mdlOnlyUnread = new PreferencesAdapter(prefs, PROP_WH_UNREAD_ONLY, false);
        chOnlyUnread = ComponentsFactory.createCheckBox(Strings.message("whatshot.unreadonly"),
            new ToggleButtonAdapter(mdlOnlyUnread));

        // Time options
        ValueModel mdlTimeOption = new AbstractConverter(
            new PreferencesAdapter(prefs, PROP_WH_TIME_OPTION, TimeOption.THIS_WEEK.getCode()))
        {
            public Object convertFromSubject(Object o)
            {
                return TimeOption.fromCode((Integer)o);
            }

            public void setValue(Object o)
            {
                TimeOption to = (TimeOption)o;
                subject.setValue(to.getCode());
            }
        };
        cbTimeOptions = new JComboBox(new ComboBoxAdapter(TimeOption.OPTIONS, mdlTimeOption));

        // Starz selector
        ValueModel mdlStarz = new PreferencesAdapter(prefs, PROP_WH_STARZ, 1);
        starz = new StarsSelectionComponent(new BoundedRangeAdapter(mdlStarz, 0, 1, 5));

        // Make toolbar fonts smaller
        UifUtilities.smallerFont(chOnlyUnread);
        UifUtilities.smallerFont(cbTimeOptions);

        // Setup button
        btnSetup = new JButton(Strings.message("whatshot.setup"));
        btnSetup.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                SetupDialog sd = new SetupDialog(GlobalModel.SINGLETON.getUserPreferences());
                sd.open();
            }
        });

        UifUtilities.smallerFont(btnSetup);

        // Results model and list
        model = new ListModel(engine, mdlStarz, mdlOnlyUnread, mdlTimeOption);
        onSetupChange();

        itemsList = new ResultsList(model)
        {
            @Override
            protected ResultGroupPanel createGroupPanel(ResultGroup group)
            {
                return new ResultGroupPanel(group.getName(), group.getName(),
                    GROUP_COLOR_1, GROUP_COLOR_2);
            }
        };
        itemsList.addActionListener(listener);

        // Start populating
        SwingWorker scanner = model.scan();
        scanner.addPropertyChangeListener(new ScannerListener());
        scanner.execute();
    }

    /** Release resources before closing. */
    public void close()
    {
        // Stop link resolution immediately
        model.stopLinkResolution();

        itemsList = null;
        super.close();
        getContentPane().removeAll();
    }

    /**
     * Sets the dialog's resizable state. By default dialogs are non-resizable; subclasses may
     * override.
     */
    protected void setResizable()
    {
        setResizable(true);
    }

    /**
     * Creates main content pane.
     *
     * @return the dialog's main content without header and border.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildTopBar(), BorderLayout.NORTH);
        panel.add(scrollPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates top bar with progress indicator, results count and search field.
     *
     * @return top bar component.
     */
    private Component buildTopBar()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 5px, p, 5px, p, 5px:grow, p");

        builder.append(starz);
        builder.append(cbTimeOptions);

        if (SystemUtils.IS_OS_MAC)
        {
            CellConstraints cc = new CellConstraints();
            JPanel p1 = new JPanel(new FormLayout("p", "p"));
            p1.add(chOnlyUnread, cc.xy(1, 1));
            builder.append(p1);

            p1 = new JPanel(new FormLayout("p", "p, 4px"));
            p1.add(btnSetup, cc.xy(1, 1));
            builder.append(p1);
        } else
        {
            builder.append(chOnlyUnread);
            builder.append(btnSetup);
        }

        builder.appendUnrelatedComponentsGapRow();

        return builder.getPanel();
    }

    /**
     * Creates results panel with results list and controls.
     *
     * @return results panel component.
     */
    private JScrollPane buildResultsPanel()
    {
        JScrollPane sp = new JScrollPane();
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /**
     * Returns currently selected item.
     *
     * @return item.
     */
    public ResultItem getSelectedItem()
    {
        return itemsList == null ? null : itemsList.getSelectedItem();
    }

    /**
     * Invoked when user changes the setup.
     */
    private void onSetupChange()
    {
        UserPreferences prefs = GlobalModel.SINGLETON.getUserPreferences();

        // Find the target guide
        IGuide guide = null;
        String title = prefs.getWhTargetGuide();
        if (StringUtils.isNotEmpty(title))
        {
            Collection<IGuide> guides = guidesSet.findGuidesByTitle(title);
            if (guides.size() > 0) guide = guides.iterator().next();
        }

        model.setSetup(prefs.getWhIgnore(), prefs.isWhNoSelfLinks(), prefs.isWhSuppressSameSourceLinks(), guide);
    }

    /**
     * Resizes the specified component. This method is called during the build process and enables
     * subclasses to achieve a better aspect ratio, by applying a resizer, e.g. the
     * <code>Resizer</code>.
     *
     * @param component the component to be resized
     */
    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.ONE2ONE.fromWidth(MIN_SIZE.width));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Scanner listener.
     */
    private class ScannerListener implements PropertyChangeListener
    {
        /**
         * Invoked when the state or progress changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String prop = evt.getPropertyName();
            if ("state".equals(prop)) onState((SwingWorker.StateValue)evt.getNewValue()); else
            if ("progress".equals(prop)) onProgress((Integer)evt.getNewValue());
        }

        /**
         * Invoked when the scanner state changes.
         *
         * @param state new state.
         */
        private void onState(SwingWorker.StateValue state)
        {
            Component set = null;

            switch (state)
            {
                case STARTED:
                    // Show the progress panel
                    set = pnlProgress;
                    break;
                case DONE:
                    // Show the results
                    set = itemsList;
                    break;
            }

            if (set != null) scrollPanel.setViewportView(set);
        }

        /**
         * Invoked when the scanner progress changes.
         *
         * @param progress [0 - 100].
         */
        private void onProgress(int progress)
        {
            pnlProgress.setProgress(progress);
        }
    }

    /**
     * Setup dialog.
     */
    private class SetupDialog extends AbstractDialog
    {
        private JComboBox cbGuides;
        private JTextArea taIgnorePatterns;
        private JCheckBox chDontCountSelfLinks;
        private JCheckBox chSuppressSameSource;

        /**
         * Creates the dialog.
         *
         * @param prefs user preferences to manipulate.
         */
        public SetupDialog(UserPreferences prefs)
        {
            super(Dialog.this, Strings.message("whatshot.dialog.title") + " - " + Strings.message("whatshot.setup"));

            taIgnorePatterns = new JTextArea();
            taIgnorePatterns.setLineWrap(false);
            taIgnorePatterns.setDocument(new DocumentAdapter(new BufferedValueModel(new PropertyAdapter(
                prefs, UserPreferences.PROP_WH_IGNORE), getTriggerChannel())));

            chDontCountSelfLinks = ComponentsFactory.createCheckBox(
                Strings.message("whatshot.setup.no.self.links"),
                prefs, UserPreferences.PROP_WH_NOSELFLINKS, getTriggerChannel());
            chSuppressSameSource = ComponentsFactory.createCheckBox(
                Strings.message("whatshot.setup.no.same.source"),
                prefs, UserPreferences.PROP_WH_SUPPRESS_SAME_SOURCE_LINKS, getTriggerChannel());

            // Guide selector
            final Map<String, FGuide> guidesMap = new HashMap<String, FGuide>();
            Vector<FGuide> guides = new Vector<FGuide>();
            FGuide allGuides = new FGuide("", Strings.message("whatshot.setup.all.guides"));
            guides.add(allGuides);
            guidesMap.put(allGuides.key, allGuides);
            int cnt = guidesSet.getGuidesCount();
            for (int i = 0; i < cnt; i++)
            {
                String t = guidesSet.getGuideAt(i).getTitle();
                FGuide fg = new FGuide(t);

                guides.add(fg);
                guidesMap.put(fg.key, fg);
            }

            ValueModel mdlGuides = new AbstractConverter(
                new PropertyAdapter(prefs, UserPreferences.PROP_WH_TARGET_GUIDE))
            {
                public Object convertFromSubject(Object o)
                {
                    return guidesMap.get(o);
                }

                public void setValue(Object o)
                {
                    FGuide guide = (FGuide)o;
                    subject.setValue(guide.key);
                }
            };
            cbGuides = new JComboBox(new ComboBoxAdapter(guides, mdlGuides));

            // Listening for setup changes
            getTriggerChannel().addValueChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (ValueHolder.PROPERTYNAME_VALUE.equals(evt.getPropertyName()))
                    {
                        if ((Boolean)evt.getNewValue()) onSetupChange();
                    }
                }
            });
        }

        /**
         * Builds the dialog content.
         *
         * @return panel.
         */
        protected JComponent buildContent()
        {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(buildMainPanel(), BorderLayout.CENTER);
            panel.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
            return panel;
        }

        /**
         * Builds main panel.
         *
         * @return panel.
         */
        private Component buildMainPanel()
        {
            BBFormBuilder builder = new BBFormBuilder("min(p;200dlu)");
            builder.setDefaultDialogBorder();

            builder.append(new JLabel(Strings.message("whatshot.source.guides")));
            builder.append(cbGuides);

            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(new JLabel(Strings.message("whatshot.ignore.links")));
            builder.appendRelatedComponentsGapRow(2);
            builder.appendRow("100dlu");
            builder.append(new JScrollPane(taIgnorePatterns), 1, CellConstraints.FILL, CellConstraints.FILL);

            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(chDontCountSelfLinks);
            builder.append(chSuppressSameSource);

            return builder.getPanel();
        }

        @Override
        public void doAccept()
        {
            // Set the change time
            long time = DateUtils.localToUTC(System.currentTimeMillis());
            GlobalModel.SINGLETON.getUserPreferences().setWhSettingsChangeTime(time);

            super.doAccept();
        }

        /**
         * Helper class to hold a guide key / title.
         */
        private class FGuide
        {
            private String key;
            private String title;

            /**
             * Creates a guide holder.
             *
             * @param title title.
             */
            private FGuide(String title)
            {
                this(title, title);
            }

            /**
             * Creates a guide holder.
             *
             * @param key   key.
             * @param title title.
             */
            private FGuide(String key, String title)
            {
                this.key = key;
                this.title = title;
            }

            @Override
            public String toString()
            {
                return title;
            }
        }
    }
}