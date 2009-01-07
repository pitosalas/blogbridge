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
// $Id: TipOfTheDay.java,v 1.15 2006/07/07 11:04:09 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.binding.adapter.PreferencesAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;

/**
 * Class for working with tips of the day.
 */
public final class TipOfTheDay
{
    /** What is guide topic key. */
    public static final String TIP_WHAT_IS_GUIDE    = "tip.whatisguide";

    /** What is feed topic key. */
    public static final String TIP_WHAT_IS_FEED     = "tip.whatisfeed";

    /** What is article topic key. */
    public static final String TIP_WHAT_IS_ARTICLE  = "tip.whatisarticle";

    /** Statz filter topic key. */
    public static final String TIP_STARZ_FILTER     = "tip.starzFilter";

    /** Starz setting topic key. */
    public static final String TIP_STARZ_SETTINGS   = "tip.starzSettings";

    /** Keyboard shortcuts topic key. */
    public static final String TIP_KEYBOARD_SHORTCUTS = "tip.keyboardShortcuts";

    /**
     * Hidden constructor of utility class.
     */
    private TipOfTheDay()
    {
    }

    /**
     * Opens standard "Tip Of The Day" dialog with random tip. Respects disabling of tips in
     * preferences.
     */
    public static void showRandomTip()
    {
        showRandomTip(false);
    }

    /**
     * Opens standard "Tip Of The Day" dialog with random tip. Respects disabling of tips in
     * preferences.
     *
     * @param force force showing the tip.
     */
    public static void showRandomTip(boolean force)
    {
        if (force || isRandomEnabled())
        {
            String tipsIndexPath = Application.getConfiguration().getTipIndexPath();
            CustomTipOfTheDayDialog dialog =
                new CustomTipOfTheDayDialog(Application.getDefaultParentFrame(), tipsIndexPath);

            dialog.open();
        }
    }

    /**
     * Shows the named tip. If the tip is proactive then it's checked if this tip is disabled by
     * user.
     *
     * @param tipName   name of the tip.
     * @param proactive TRUE if proactive.
     *
     * @see TIP_WHAT_IS_GUIDE
     */
    public static void showNamedTip(String tipName, boolean proactive)
    {
        TipDialog dialog = TipDialog.createNamedTip(tipName, proactive);
        if (dialog != null) dialog.open();
    }

    /**
     * Binds the proactive tip of the day to a specific tab of the tabbed pane.
     * The tip will be displayed when user enters associated tab.
     *
     * @param pane      pane to bind to.
     * @param targetTab tab to associate with.
     * @param tipName   tip name.
     */
    public static void bind(final JTabbedPane pane, final Component targetTab, final String tipName)
    {
        pane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if (pane.getSelectedComponent() == targetTab) showNamedTip(tipName, true);
            }
        });
    }

    /**
     * Returns TRUE if random tips are enabled.
     *
     * @return TRUE if random tips are enabled.
     */
    private static boolean isRandomEnabled()
    {
        return CustomTipOfTheDayDialog.isShowingTips();
    }

    /**
     * Mouse adapter calling tip of the day dialog on click event.
     */
    public static class TipMouseAdapter extends MouseAdapter
    {
        private String tipName;
        private boolean proactive;

        /**
         * Creates mouse adapter for the tip.
         *
         * @param aTipName      tip name.
         * @param aProactive    if tip is proactive.
         */
        public TipMouseAdapter(String aTipName, boolean aProactive)
        {
            tipName = aTipName;
            proactive = aProactive;
        }

        /**
         * Invoked when mouse button pressed.
         *
         * @param e event.
         */
        public void mousePressed(MouseEvent e)
        {
            TipOfTheDay.showNamedTip(tipName, proactive);
        }
    }

    /**
     * Dialog for showing command and proactive tips.
     */
    private static final class TipDialog extends AbstractDialog
    {
        private static final Logger LOG = Logger.getLogger(TipDialog.class.getName());

        private JEditorPane htmlPane;

        private String tipName;
        private boolean proactive;
        private JCheckBox chDoNotShow;

        /**
         * Constructs the dialog for the given ownwer and path to the tip index file.
         */
        private TipDialog(Frame owner, String aTipName, boolean aProactive)
        {
            super(owner, Strings.message("ui.tip.of.the.day"));

            tipName = aTipName;
            proactive = aProactive;
        }

        /**
         * Creates tip of the day dialog showing tip with a given name (associated in
         * Resource.properties with particular file).
         *
         * @param tipName   name of the tip.
         * @param proactive TRUE if proactive dialog (could be disabled by user).
         *
         * @return dialog.
         */
        public static TipDialog createNamedTip(String tipName, boolean proactive)
        {
            boolean enabled = !proactive ||
                Application.getUserPreferences().getBoolean(tipName, true);

            return !enabled ? null : new TipDialog(Application.getDefaultParentFrame(), tipName,
                proactive);
        }

        /**
         * Builds and returns the dialog's content.
         */
        protected JComponent buildContent()
        {
            JPanel content = new JPanel(new BorderLayout());
            content.add(buildMainPanel(), BorderLayout.CENTER);
            content.add(buildButtonBar(), BorderLayout.SOUTH);

            showTip();

            return content;
        }

        /**
         * Builds and returns the main panel.
         */
        private JComponent buildMainPanel()
        {
            htmlPane = new JEditorPane();
            htmlPane.setEditable(false);
            htmlPane.setFocusable(false);
            htmlPane.setMargin(new Insets(0, 10, 0, 5));
            htmlPane.setPreferredSize(Resizer.DEFAULT.fromWidth(320));

            LinkListener.attachToPane(htmlPane);

            return new JScrollPane(htmlPane);
        }

        /**
         * Creates and returns the button bar, which consists of: CheckBox, glue, Back, Next, and
         * Close buttons.
         */
        private JComponent buildButtonBar()
        {
            if (proactive)
            {
                chDoNotShow = ComponentsFactory.createCheckBox(Strings.message("do.not.show.this.dialog.again"));
                chDoNotShow.setSelected(false);
            }

            ButtonBarBuilder builder = new ButtonBarBuilder();
            builder.setDefaultButtonBarGapBorder();
            if (proactive) builder.addFixed(chDoNotShow);
            builder.addUnrelatedGap();
            builder.addGlue();
            builder.addUnrelatedGap();
            builder.addGridded(createCloseButton(true));
            return builder.getPanel();
        }

        /**
         * Use the default resizer to get an aesthetic aspect ratio.
         */
        protected void resizeHook(JComponent component)
        {
            Resizer.DEFAULT.resizeDialogContent(component);
        }

        /**
         * Opens dialog.
         */
        public void showTip()
        {
            URL tipURL = ResourceUtils.getURL(ResourceUtils.getString(tipName));
            showTip(tipURL);
        }

        private void showTip(URL tipURL)
        {
            try
            {
                htmlPane.setPage(tipURL);
            } catch (IOException e)
            {
                if (tipURL != null) LOG.warning(MessageFormat.format(
                    Strings.error("failed.to.show.tip"), new Object[] { tipURL }));
            }
        }

        public void open()
        {
            super.open();

            if (proactive && chDoNotShow.isSelected())
            {
                Application.getUserPreferences().putBoolean(tipName, false);
            }
        }
    }

    /**
     * Custom tip of the day dialog which support clicking on links.
     */
    private static class CustomTipOfTheDayDialog extends AbstractDialog
    {
        private static final String IS_SHOWING_KEY = "tipOfTheDay.isShowing";
        private static final Boolean IS_SHOWING_DEFAULT = Boolean.TRUE;

        private static final String TIP_INDEX_KEY = "tipOfTheDay.index";
        private static final Integer TIP_INDEX_DEFAULT = new Integer(0);

        private static final Logger LOGGER = Logger.getLogger("CustomTipOfTheDayDialog");

        private static PreferencesAdapter showingTipsModel;
        private static PreferencesAdapter tipIndexModel;

        private JEditorPane htmlPane;
        private java.util.List tipPaths;

        // Instance  Creation ****************************************************

        /**
         * Constructs the dialog for the given ownwer and path to the tip index file.
         */
        public CustomTipOfTheDayDialog(Frame owner, String tipIndexPath)
        {
            super(owner, Strings.message("ui.tip.of.the.day"));
            tipIndexModel = new PreferencesAdapter(Application.getUserPreferences(),
                    TIP_INDEX_KEY,
                    TIP_INDEX_DEFAULT);
            readTipPaths(tipIndexPath);
        }


        // Static Access to the showingTips Property  *****************************

        /**
         * Answers if we shall show tips at application startup.
         */
        public static boolean isShowingTips()
        {
            return showingTipsModel().getBoolean();
        }

        /**
         * Sets if we shall show tips at application startup.
         *
         * @param b true to enable the tips, false to disable
         */
        public static void setShowingTips(boolean b)
        {
            showingTipsModel().setBoolean(b);
        }

        /**
         * Returns an adapter for the boolean property 'showingTips' that is backed by the user
         * preferences.
         *
         * @return a <code>PreferencesAdapter</code> for the 'showingTips' property.
         */
        public static PreferencesAdapter showingTipsModel()
        {
            if (showingTipsModel == null)
            {
                showingTipsModel = new PreferencesAdapter(Application.getUserPreferences(),
                        IS_SHOWING_KEY,
                        IS_SHOWING_DEFAULT);
            }
            return showingTipsModel;
        }

        // Building *************************************************************

        /**
         * Builds and returns the dialog's content.
         */
        protected JComponent buildContent()
        {
            JPanel content = new JPanel(new BorderLayout());
            content.add(buildMainPanel(), BorderLayout.CENTER);
            content.add(buildButtonBar(), BorderLayout.SOUTH);
            return content;
        }

        /**
         * Builds and returns the main panel.
         */
        private JComponent buildMainPanel()
        {
            htmlPane = new JEditorPane();
            htmlPane.setEditable(false);
            htmlPane.setFocusable(false);
            htmlPane.setMargin(new Insets(0, 10, 0, 5));
            htmlPane.setPreferredSize(Resizer.DEFAULT.fromWidth(320));

            goToNextTip();
            LinkListener.attachToPane(htmlPane);

            return new JScrollPane(htmlPane);
        }

        /**
         * Creates and returns the button bar, which consists of: CheckBox, glue, Back, Next, and
         * Close buttons.
         */
        private JComponent buildButtonBar()
        {
            JCheckBox showTipsCheckBox = new JCheckBox(Strings.message("ui.tip.of.the.day.show.on.startup"));
            showTipsCheckBox.setModel(createShowTipsButtonModel(showingTipsModel()));

            ButtonBarBuilder builder = new ButtonBarBuilder();
            builder.setDefaultButtonBarGapBorder();
            builder.addFixed(showTipsCheckBox);
            builder.addUnrelatedGap();
            builder.addGlue();
            builder.addGridded(createBackButton());
            builder.addGridded(createNextButton());
            builder.addUnrelatedGap();
            builder.addGridded(createCloseButton(true));
            return builder.getPanel();
        }

        /**
         * Use the default resizer to get an aesthetic aspect ratio.
         */
        protected void resizeHook(JComponent component)
        {
            Resizer.DEFAULT.resizeDialogContent(component);
        }

        /**
         * Creates and answers a <code>ButtonModel</code> for the show tips check box, using the
         * given <code>ValueModel</code>.
         */
        private ButtonModel createShowTipsButtonModel(ValueModel model)
        {
            return new ToggleButtonAdapter(buffer(model));
        }

        /**
         * Creates and returns the back button.
         */
        private JButton createBackButton()
        {
            JButton button = new JButton(Strings.message("ui.tip.of.the.day.back"));
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    goToPreviousTip();
                }
            });
            return button;
        }

        /**
         * Creates and returns the next button.
         */
        private JButton createNextButton()
        {
            JButton button = new JButton(Strings.message("ui.tip.of.the.day.next"));
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    goToNextTip();
                }
            });
            return button;
        }

        // Behavior ***************************************************************

        /**
         * Performs the close operation.
         */
        public void close()
        {
            doApply();
            super.close();
        }

        /**
         * Loads and displays the next tip.
         */
        private void goToNextTip()
        {
            int tipIndex = tipIndexModel.getInt();
            if (++tipIndex >= tipPaths.size())
                tipIndex = 0;
            tipIndexModel.setInt(tipIndex);
            showTip((String)tipPaths.get(tipIndex));
        }

        /**
         * Loads and displays the previous tip.
         */
        private void goToPreviousTip()
        {
            int tipIndex = tipIndexModel.getInt();
            if (--tipIndex < 0) tipIndex = tipPaths.size() - 1;
            tipIndexModel.setInt(tipIndex);
            showTip((String)tipPaths.get(tipIndex));
        }

        // Misc *******************************************************************

        /**
         * Answers the directory name for the given file name.
         */
        private String getDirectoryName(String filename)
        {
            char zipSeparator = '/';
            int lastSeparatorIndex = filename.lastIndexOf(zipSeparator);

            return (lastSeparatorIndex == -1) ? "" : filename.substring(0, lastSeparatorIndex + 1);
        }

        /**
         * Reads and parses the tip index file to get all contained paths.
         */
        private void readTipPaths(String filename)
        {
            tipPaths = new ArrayList();
            String directoryName = getDirectoryName(filename);
            InputStream in = null;
            try
            {
                in = ResourceUtils.getInputStream(filename);
                if (null == in) return;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                try
                {
                    String line;
                    do
                    {
                        line = reader.readLine();
                        if (line != null)
                        {
                            line = line.trim();
                            if (line.length() > 0)
                                tipPaths.add(directoryName + line);
                        }
                    } while (line != null);
                    reader.close();
                } catch (IOException e)
                {
                    LOGGER.log(Level.WARNING, MessageFormat.format(
                        Strings.error("failed.to.read.tips.index"),
                        new Object[] { filename }), e);
                }
            } finally
            {
                try
                {
                    if (in != null) in.close();
                } catch (IOException e1)
                {
                    LOGGER.log(Level.WARNING, Strings.error("failed.to.close.file"), e1);
                }
            }
        }

        /**
         * Shows the tip for the specified tip path.
         */
        private void showTip(String tipPath)
        {
            URL tipURL = ResourceUtils.getURL(tipPath);
            try
            {
                htmlPane.setPage(tipURL);
            } catch (IOException e)
            {
                if (tipURL != null) LOGGER.warning(MessageFormat.format(
                    Strings.error("failed.to.show.tip"), new Object[] { tipURL }));
            }
        }
    }

    /**
     * Listens for clicks on urls and loads new pages to the pane.
     */
    private static final class LinkListener implements HyperlinkListener
    {
        private JEditorPane pane;

        /**
         * Creates listener for a pane.
         *
         * @param aPane pane to update.
         */
        private LinkListener(JEditorPane aPane)
        {
            pane = aPane;
        }

        /**
         * Attaches listener to the pane.
         *
         * @param pane pane to attach to.
         */
        public static void attachToPane(JEditorPane pane)
        {
            pane.addHyperlinkListener(new LinkListener(pane));
        }

        /**
         * Called when hyperlink status updated.
         *
         * @param e event object.
         */
        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
            try
            {
                pane.setPage(e.getURL());
            } catch (IOException e1)
            {
                // Not a problem.
            }
        }
    }
}
