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
// $Id: GuidesPanel.java,v 1.73 2007/10/04 09:55:06 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uifextras.util.UIFactory;
import com.salas.bb.core.*;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.dnd.DNDList;
import com.salas.bb.utils.dnd.DNDListContext;
import com.salas.bb.utils.dnd.IDNDObject;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;
import com.salas.bb.views.mainframe.UnreadButton;
import com.salas.bb.views.settings.RenderingManager;
import com.salas.bb.views.settings.RenderingSettingsNames;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays icons for each of the different channel guides, allowing the user to pick which one they
 * want to work with.
 */
public class GuidesPanel extends CoolInternalFrame
{
    private static final Logger LOG = Logger.getLogger(GuidesPanel.class.getName());

    private final ScrollListAction scrollUpAction = new ScrollListAction(-1, "gl.up.icon");
    private final ScrollListAction scrollDownAction = new ScrollListAction(+1, "gl.down.icon");

    private GuidesList              guidesList;
    private GuideListCellRenderer   cellRenderer;
    private GuidesSet               model;

    // Flag of callback selection. When set firing of guide selection event isn't required.
    // This happens when we have programatical selection of guide and don't wish to get in
    // endless loop when list fires selection event and model asks it to select guide once
    // again.
    private boolean callbackSelection;

    private UnreadController unreadController;

    // Action to call on double-click over some cell
    private Action onDoubleClickAction;
    private GuidesListModel guidesListModel;

    /**
     * Constructs guides panel.
     */
    public GuidesPanel()
    {
        super(Strings.message("panel.guides"));
        setSubtitle(" ");
    }

    /**
     * Sets new on-double-click action.
     *
     * @param action action.
     */
    public void setOnDoubleClickAction(Action action)
    {
        this.onDoubleClickAction = action;
    }

    /**
     * Setups list of guides.
     *
     * @param popupAdapter popup to use.
     */
    public void setupGuidesList(MouseListener popupAdapter)
    {
        model = GlobalModel.SINGLETON.getGuidesSet();

        guidesListModel = GlobalController.SINGLETON.getGuidesListModel();
        guidesList = new GuidesList(guidesListModel);

        GuideDisplayModeManager.getInstance().addListener(new IDisplayModeManagerListener()
        {
            public void onClassColorChanged(int cl, Color oldColor, Color newColor)
            {
                guidesList.repaint();
                if (oldColor == null || newColor == null)
                {
                    IGuide sel = GlobalModel.SINGLETON.getSelectedGuide();
                    if (sel != null) guidesList.setSelectedValue(sel, false);
                }
            }
        });

        // set up the UnreadController to manage the unread button
        unreadController = new UnreadController(guidesList, guidesListModel);

        cellRenderer = new GuideListCellRenderer(unreadController);
        guidesList.setCellRenderer(cellRenderer);

        // set to 0 to prevent computing width by renderer preferred size
        guidesList.setFixedCellWidth(0);

        // Order of the next two lines is MANDATORY.
        // Selection of guide MUST precede opening of conext menu in case of
        // right mouse button click.

        GuideMouseListener mouseListener = new GuideMouseListener();
        guidesList.addMouseListener(mouseListener);
        guidesList.addMouseMotionListener(mouseListener);
        guidesList.addMouseListener(popupAdapter);

        guidesList.addListSelectionListener(new GuideSelectionListener());
        guidesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        guidesList.addPropertyChangeListener(DNDList.PROP_DRAGGING, new DraggingListListener());
        guidesList.setDropTarget(new URLDropTarget(new URLDropListener()));

        // Remove key listener from the list
        UifUtilities.removeTypeSelectionListener(guidesList);
        
        onListColorsUpdate();

        JScrollPane scrollPane = UIFactory.createStrippedScrollPane(guidesList);
        scrollPane.setPreferredSize(new Dimension(5, 1));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.addMouseWheelListener(new GuideMouseWheelListener());

        // Recalculate buttons each time the viewport changes position
        scrollPane.getViewport().addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                reviewActionsState();
            }
        });

        // Build the guideSubPantel, which consists of the scrollable list of Guides, and the two
        // buttons. Add the resulting guideSubPantel to the CoolFrame as the conent.

        JPanel guideSubPanel = new JPanel();
        JPanel buttons = new JPanel(new GridLayout());
        final JButton btnUp = new JButton(scrollUpAction);
        final JButton btnDown = new JButton(scrollDownAction);
        buttons.add(btnUp);
        buttons.add(btnDown);

        guideSubPanel.setLayout(new BorderLayout());
        guideSubPanel.add(scrollPane, BorderLayout.CENTER);
        guideSubPanel.add(buttons, BorderLayout.SOUTH);
        guideSubPanel.setBorder(BorderFactory.createEmptyBorder());
        guideSubPanel.setPreferredSize(new Dimension(90, -1));
        guideSubPanel.setMinimumSize(new Dimension(90, 0));

        setContent(guideSubPanel);

        GlobalController.SINGLETON.addControllerListener(new ContollerListener());

        reviewActionsState();

        // setup listener of resizing events to review state of scrolling actions
        addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                reviewActionsState();
            }
        });

        // Update layout if unread button is enabled/disabled in preferences
        RenderingManager.addPropertyChangeListener(
            new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    String prop = evt.getPropertyName();
                    if (RenderingSettingsNames.IS_UNREAD_IN_GUIDES_SHOWING.equals(prop) ||
                        RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING.equals(prop) ||
                        RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING.equals(prop))
                    {
                        updateGuidesListLayout();
                    } else if (RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES.equals(prop))
                    {
                        onIconSizeChange();
                    }
                }
            });

        RenderingManager.addPropertyChangeListener(
            RenderingSettingsNames.THEME,
            new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    onListColorsUpdate();
                }
            });
    }

    /**
     * Updates the list color according to the theme and starts list repainting.
     */
    private void onListColorsUpdate()
    {
        Color background = RenderingManager.getFeedsListBackground(false);
        guidesList.setBackground(background);
        guidesList.repaint();
    }

    /**
     * Checks what scrolling actions should be enabled and what - disabled basing on what's
     * currently visible.
     */
    void reviewActionsState()
    {
        int first = getFirstFullyVisibleIndex();
        int last = getLastFullyVisibleIndex();
        int size = guidesList.getModel().getSize();

        scrollUpAction.setEnabled(first > 0);
        scrollDownAction.setEnabled(last < size - 1);
    }

    /**
     * Returns first index which is fully visible.
     * 
     * @return index in list.
     */
    private int getFirstFullyVisibleIndex()
    {
        int index = guidesList.getFirstVisibleIndex();
        final ListModel mdl = guidesList.getModel();
        if (mdl != null && index != -1)
        {
            Rectangle r = guidesList.getVisibleRect();
            Rectangle c = guidesList.getCellBounds(index, index);
            if (r.y > c.y && index < mdl.getSize() - 1) index++;
        }

        return index;
    }

    /**
     * Returns last index which is fully visible.
     * 
     * @return index in list.
     */
    private int getLastFullyVisibleIndex()
    {
        int index = guidesList.getLastVisibleIndex();
        if (index != -1)
        {
            Rectangle r = guidesList.getVisibleRect();
            Rectangle c = guidesList.getCellBounds(index, index);
            if (r.y + r.height < c.y + c.height && index > 0) index--;
        }

        return index;
    }

    /**
     * Ensures that the guide at the provided index is visible.
     *
     * @param index     index of the guide that should be visible.
     */
    public void ensureIndexIsVisible(int index)
    {
        guidesList.ensureIndexIsVisible(index);
    }

    /**
     * Returns guides list.
     *
     * @return guides list component.
     */
    public GuidesList getGuidesList()
    {
        return guidesList;
    }

    /**
     * Returns component which can get keyboard focus.
     * 
     * @return focusable component.
     */
    public JComponent getFocusableComponent()
    {
        return guidesList;
    }

    /**
     * Selects guide at specified index.
     * 
     * @param index index to select guide at. -1 to clear selection.
     */
    public void selectGuide(int index)
    {
        if (index == -1)
        {
            guidesList.clearSelection();
        } else
        {
            guidesList.setSelectedIndex(index);
        }
    }


    /**
     * @return The UnreadController for the Guides panel.
     */
    public UnreadController getUnreadController()
    {
        return unreadController;
    }

    /**
     * Update the layout of the guides list cells.
     */
    private void updateGuidesListLayout()
    {
        cellRenderer.updateRendererAndLayout();
        rescaleAndRepaintListCells();
    }

    /**
     * Invoked when the size of guides list icons change.
     */
    private void onIconSizeChange()
    {
        cellRenderer.onIconSizeChange();
        rescaleAndRepaintListCells();
    }

    /**
     * Rescales and updates component.
     */
    private void rescaleAndRepaintListCells()
    {
        guidesList.setFixedCellHeight(cellRenderer.getRequiredHeight());
        guidesList.repaint();
        unreadController.resetAttachment();
    }

    /**
     * Listens for URL drops and invokes reading lists addition and guides creation.
     */
    private class URLDropListener implements IURLDropTargetListener
    {
        /**
         * Called when valid URL is dropped to the target.
         *
         * @param url       URL dropped.
         * @param location  mouse pointer location.
         */
        public void urlDropped(URL url, Point location)
        {
            if (GlobalController.SINGLETON.checkForNewSubscription()) return;

            GlobalModel mdl = GlobalModel.SINGLETON;
            final GlobalController controller = GlobalController.SINGLETON;
            IGuide guide = null;

            int index = guidesList.locationToIndex(location);
            if (index != -1)
            {
                guide = mdl.getGuidesSet().getGuideAt(index);
            }

            final DirectFeed feed = controller.createDirectFeed(guide, url);
            if (feed != null)
            {
                // EDT !!!
                if (guide != mdl.getSelectedGuide()) controller.selectGuide(guide, false);

                // We do this to be sure that all events connected to addition of
                // the feed to the guide are successfully processed.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        controller.selectFeed(feed);
                    }
                });
            }
        }
    }

    /**
     * Listens to mouse wheel commands and scrolls the list vertically.
     */
    private static class GuideMouseWheelListener implements MouseWheelListener
    {
        /**
         * Invoked when the mouse wheel is rotated.
         *
         * @see java.awt.event.MouseWheelEvent
         */
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            JScrollPane pane = (JScrollPane)e.getSource();
            JScrollBar scrollBar = pane.getVerticalScrollBar();

            int direction = e.getWheelRotation() < 0 ? -1 : 1;
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
            {
                scrollByUnits(scrollBar, direction, e.getScrollAmount());
            } else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
            {
                scrollByBlock(scrollBar, direction);
            }
        }

        private void scrollByBlock(JScrollBar scrollBar, int direction)
        {
            int oldValue = scrollBar.getValue();
            int blockIncrement = scrollBar.getBlockIncrement();
            int delta = blockIncrement * direction;
            int newValue = oldValue + delta;

            // Check for overflow.
            if (delta > 0 && newValue < oldValue)
            {
                newValue = scrollBar.getMaximum();
            } else if (delta < 0 && newValue > oldValue)
            {
                newValue = scrollBar.getMinimum();
            }
            scrollBar.setValue(newValue);
        }

        /**
         * Method for scrolling by a unit increment.
         *
         * @param scrollbar scrollbar component.
         * @param direction scrolling direction.
         * @param units     scrolling units.
         */
        private void scrollByUnits(JScrollBar scrollbar, int direction, int units)
        {
            // This method is called from BasicScrollPaneUI to implement wheel
            // scrolling, as well as from scrollByUnit().
            int delta = direction * units * scrollbar.getUnitIncrement(direction);
            int oldValue = scrollbar.getValue();
            int newValue = oldValue + delta;

            // Check for overflow.
            if (delta > 0 && newValue < oldValue)
            {
                newValue = scrollbar.getMaximum();
            } else if (delta < 0 && newValue > oldValue)
            {
                newValue = scrollbar.getMinimum();
            }
            scrollbar.setValue(newValue);
        }
    }

    /**
     * Performs selection of item in list with right mouse button.
     */
    private class GuideMouseListener extends MouseAdapter implements MouseMotionListener
    {
        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        public void mousePressed(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                final int index = guidesList.locationToIndex(e.getPoint());
                if (index >= 0 && guidesList.getCellBounds(index, index).contains(e.getPoint()) &&
                    !guidesList.isSelectedIndex(index)) guidesList.setSelectedIndex(index);
            }
        }

        /**
         * Invoked when the mouse enters a component.
         */
        public void mouseEntered(MouseEvent e)
        {
            if (DNDListContext.isDragging())
            {
                final int index = guidesList.locationToIndex(e.getPoint());
                guidesList.setSelectedIndex(index);
            }
        }

        /**
         * Invoked on drag, but we ignore it.
         */
        public void mouseDragged(MouseEvent e)
        {
        }

        /**
         * Invoked on mouse moved.  Make sure unread button is
         * attached to row mouse is over.
         * @param e MouseEvent info  
         */
        public void mouseMoved(MouseEvent e)
        {
            boolean showUnread = RenderingManager.isShowUnreadInGuides();

            if (!showUnread) return;

            final int index = guidesList.locationToIndex(e.getPoint());
            if (index >= 0) unreadController.attachButton(index, false);
        }


        /**
         * Invoked when mouse is clicked.
         */
        public void mouseClicked(MouseEvent e)
        {
            if (onDoubleClickAction != null && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
            {
                onDoubleClickAction.actionPerformed(new ActionEvent(guidesList, 0, null));
            }
        }
    }

    /**
     * Listens for selections on the guides list and notifies the rest application.
     */
    private class GuideSelectionListener implements ListSelectionListener
    {
        /**
         * Called whenever the value of the selection changes.
         * 
         * @param e the event that characterizes the change.
         */
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting() && !callbackSelection)
            {
                // Find out last selected guide index
                IGuide prevGuide = GlobalModel.SINGLETON.getSelectedGuide();
                int oldIndex = prevGuide == null ? -1 : indexOf(guidesList.getModel(), prevGuide);

                // Find out new selection index
                int selIndex = ListSelectionManager.evaluateSelectionIndex(guidesList, oldIndex);

                // Get new selected guide and update models
                ListModel model = guidesList.getModel();
                IGuide guide = selIndex == -1 ? null : (IGuide)model.getElementAt(selIndex);

                if (DNDListContext.isDragging())
                {
                    DNDListContext.setDestination(guide);
                } else
                {
                    GlobalController.SINGLETON.selectGuideAndFeed(guide);

                    // Hack to repaint list cells after selection tricks
                    guidesList.repaint();
                }
            }
        }

        private int indexOf(ListModel aModel, Object obj)
        {
            int index = -1;
            int count = aModel.getSize();
            for (int i = 0; index == -1 && i < count; i++)
            {
                if (aModel.getElementAt(i) == obj) index = i;
            }

            return index;
        }
    }

    /**
     * Listener of programmatical selections.
     */
    private class ContollerListener extends ControllerAdapter
    {
        /**
         * Invoked after application changes the guide.
         * 
         * @param guide guide to with we have switched.
         */
        public void guideSelected(final IGuide guide)
        {
            if (UifUtilities.isEDT())
            {
                guideSelectedEDT(guide);
            } else SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    guideSelectedEDT(guide);
                }
            });
        }

        /**
         * Invoked after application changes the guide.
         *
         * @param guide guide to with we have switched.
         */
        private void guideSelectedEDT(IGuide guide)
        {
            if (guide == null)
            {
                guidesList.clearSelection();
            } else
            {
                int index = guidesListModel.indexOf(guide);
                if (index >= 0)
                {
                    if (!guidesList.isSelectedIndex(index))
                    {
                        callbackSelection = true;
                        guidesList.setSelectedIndex(index);
                        callbackSelection = false;
                    }
                    guidesList.ensureIndexIsVisible(index);
                }

                // Selection occurs after the guides list is all set up,
                // so we declare the UI initialized.
                unreadController.setUIInitialized();
            }
        }
    }

    /**
     * Basic scroll action.
     */
    private class ScrollListAction extends AbstractAction
    {
        private int scrollStep;

        /**
         * Constructs scroll action.
         * 
         * @param step number of rows to scroll (positive - down, negative - up).
         * @param iconName name of the icon in resources.
         */
        public ScrollListAction(int step, String iconName)
        {
            super(Constants.EMPTY_STRING, ResourceUtils.getIcon(iconName));
            this.scrollStep = step;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            final int size = guidesList.getModel().getSize();
            if (scrollStep > 0)
            {
                // scroll down
                int lastVisible = getLastFullyVisibleIndex();
                if (lastVisible < size - 1)
                {
                    int nextVisible = lastVisible + scrollStep;
                    if (nextVisible >= size) nextVisible = size - 1;
                    guidesList.ensureIndexIsVisible(nextVisible);
                }
            } else
            {
                // scroll up
                int firstVisible = getFirstFullyVisibleIndex();
                if (firstVisible > 0)
                {
                    // step is negative here so '- -' = '+'
                    int nextVisible = firstVisible + scrollStep;
                    if (nextVisible < 0) nextVisible = 0;
                    guidesList.ensureIndexIsVisible(nextVisible);
                }
            }

            reviewActionsState();
        }
    }

    /**
     * Listens for dragging mode changes of the list and updates global context.
     */
    private class DraggingListListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that
         *            has changed.
         */
        public void propertyChange(final PropertyChangeEvent evt)
        {
            boolean isDraggingFinished = !(Boolean)evt.getNewValue();
            if (isDraggingFinished)
            {
                DNDList source = DNDListContext.getSource();
                IDNDObject object = DNDListContext.getObject();
                int insertPosition = source.getInsertPosition();
                Object[] guidesI = object.getItems();

                if (insertPosition >= 0 && guidesI.length > 0)
                {
                    IGuide currentSelection = GlobalModel.SINGLETON.getSelectedGuide();
                    int oldSelectionIndex = currentSelection == null
                        ? -1 : model.indexOf(currentSelection);

                    boolean selectedGuideMoved = false;

                    // We need to translate insert position into guide set coordinates
                    if (insertPosition < guidesListModel.getSize())
                    {
                        IGuide after = (IGuide)guidesListModel.getElementAt(insertPosition);
                        insertPosition = model.indexOf(after);
                    } else if (guidesListModel.getSize() > 0)
                    {
                        IGuide before = (IGuide)guidesListModel.getElementAt(insertPosition - 1);
                        insertPosition = model.indexOf(before) + 1;
                    } else insertPosition = 0;

                    // Move selected guides now
                    int index = insertPosition;
                    for (int i = 0; i < guidesI.length; i++)
                    {
                        IGuide guide = (IGuide)guidesI[guidesI.length - i - 1];

                        int currentIndex = model.indexOf(guide);
                        if (currentIndex < index) index--;
                        selectedGuideMoved |= currentIndex == oldSelectionIndex;

                        GlobalController.SINGLETON.moveGuide(guide, index);
                    }

                    int currentSelectionIndex = currentSelection == null
                        ? -1 : guidesListModel.indexOf(currentSelection);

                    ListSelectionModel selModel = source.getSelectionModel();
                    if (selectedGuideMoved)
                    {
                        selModel.setSelectionInterval(index, index + guidesI.length - 1);
                        selModel.addSelectionInterval(currentSelectionIndex, currentSelectionIndex);
                        selModel.setLeadSelectionIndex(currentSelectionIndex);
                    } else if (currentSelectionIndex != -1)
                    {
                        selModel.setSelectionInterval(currentSelectionIndex, currentSelectionIndex);
                    } else
                    {
                        selModel.clearSelection();
                    }
                }
            }
        }
    }

    /**
     * The UnreadController manages the unread button.
     * There is a single "live" instance of each, which is moved into place on whatever
     * row the user has moused on.  We take care to remove or reset the buttons if the
     * list contents changes, so that it's not left in an obsolete position in the list. 
     */
    static class UnreadController extends ComponentAdapter
        implements ActionListener, IDisplayModeManagerListener
    {
        private static final String TOOL_TIP_MSG_SINGLE = Strings.message("panel.guides.unread.one");
        private static final String TOOL_TIP_MSG_MANY = Strings.message("panel.guides.unread.many");

        private final JList         guidesList;
        private final GuidesListModel guidesListModel;
        private final UnreadButton  unreadButton;

        /** The row which the unread button is attached to, or -1. */
        private int                 attachedRow;
        /** The Guide which the unread button is attached to, or null. */
        private IGuide              attachedGuide;

        /** Set when the UI has been initialized. */
        private boolean             uiInitialized;

        /** Set when an update of the unread buttons has been queued. */
        private boolean             updateScheduled;

        /** Guides whose unread counts must be updated. */
        private final Set<IGuide> unreadDirtyGuides = new HashSet<IGuide>();

        /**
         * Constructs as on the given FeedsPanel.
         *
         * @param list  guides list.
         * @param model guides list model.
         */
        UnreadController(JList list, GuidesListModel model)
        {
            guidesList = list;
            guidesListModel = model;
            unreadButton = new UnreadButton();
            unreadButton.initToolTipMessage(TOOL_TIP_MSG_SINGLE, TOOL_TIP_MSG_MANY);
            attachedRow = -1;
            uiInitialized = false;
            updateScheduled = false;

            attachListeners();
        }

        /**
         * Adds listeners so that we are notified of changes to the list, and can track
         * the button press on the unread button.
         */
        void attachListeners()
        {
            GlobalController.SINGLETON.addDomainListener(new DomainListener());
            FeedDisplayModeManager.getInstance().addListener(this);
            unreadButton.addActionListener(this);
            guidesList.addComponentListener(this);

            // Listen to unread data feeds deselection and update the counters when it happens.
            // It may come that some feed with unread articles becomes invisible.
            GlobalController.SINGLETON.addControllerListener(new UnreadDataFeedDeselectionMonitor()
            {
                /**
                 * Invoked when unread data feed deselection detected.
                 */
                protected void unreadDataFeedDeselected()
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            updateAttachments();
                        }
                    });
                }
            });

            // User preferences will affect unread counts, if the starz filter changes
            // or if feeds below the threshhold are changed to show or hide.
            // As a simple and conservative response, just repaint the guide list
            // entirely, and update the unread button.
            GlobalModel.SINGLETON.getUserPreferences().addPropertyChangeListener(
                new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (!UserPreferences.FEED_VISIBILITY_PROPERTIES.contains(evt.getPropertyName())) return;

                        updateAttachments();
                    }
                });

            // Changes to feed display colors will affect unread counts if the
            // "hidden" color option is used to hide or show feeds.
            // For simplicity, just repaint all the guides entirely and update the
            // unread button.
            FeedDisplayModeManager.getInstance().addListener(
                new IDisplayModeManagerListener()
                {
                    public void onClassColorChanged(int feedClass, Color oldColor, Color newColor)
                    {
                        updateAttachments();
                    }
                });

        }

        private void updateAttachments()
        {
            guidesList.repaint();
            resetAttachment();
        }

        /**
         * Compute the unread statistics for the given guide, i.e. the number of unread feeds in
         * that guide.
         * 
         * @param guide
         *        the guide to calc.
         * 
         * @return number of unread feeds in that guide.
         */
        static int calcUnreadStats(IGuide guide)
        {
            int count = 0;

            IFeed[] feeds = GlobalModel.SINGLETON.getVisibleFeeds(guide);
            for (IFeed feed : feeds) if (feed.getUnreadArticlesCount() > 0) count++;

            return count;
        }

        /**
         * Calculate the unread counts for a guide if the UI has been initialized.
         * Otherwise, defer the calculation by scheduling a later update.
         * This allows the Guides pane to initially come up more quickly by avoid
         * computing the unread stats for all the guides. 
         * @param guide The guide to calculate.
         * @return Unread count for the guide, or 0 if it's deferred.
         */
        int deferCalcUnreadStats(IGuide guide)
        {
            if (!uiInitialized)
            {
                unreadUpdateNeeded(guide);
                return 0;
            } else
            {
                return calcUnreadStats(guide);
            }
        }

        /**
         * Move the buttons into place on the given row of the list.
         * Does nothing if we're already attached at that position.
         *
         * @param row index of row to attach to.
         * @param forceUpdate <code>TRUE</code> to force button update even if already in place
         */
        void attachButton(int row, boolean forceUpdate)
        {
            IGuide guide = (IGuide)guidesList.getModel().getElementAt(row);

            boolean sameButton = (row == attachedRow && guide == attachedGuide);

            if (sameButton && !forceUpdate) return;

            attachedGuide = guide;
            attachedRow = row;
            int unreadCount = calcUnreadStats(attachedGuide);

            GuideListCellRenderer cellRenderer =
                ((GuideListCellRenderer)guidesList.getCellRenderer());

            Rectangle r = guidesList.getCellBounds(row, row);
            Dimension unreadButtonSize = unreadButton.getSize();
            int unreadButtonYOffs = cellRenderer.getUnreadButtonYOffset();
            r.x = r.getSize().width - unreadButtonSize.width;
            r.x -= GuideListCellRenderer.CELL_MARGIN_RIGHT + 1; // +1 for border insets
            r.y += GuideListCellRenderer.CELL_MARGIN_TOP + 1 + unreadButtonYOffs; // +1 for border
            r.setSize(unreadButtonSize);

            Rectangle oldBounds = unreadButton.getBounds();

            // If it's the same button at the same position, then update it;
            // this preserves the mouse state of the button.  Otherwise,
            // reset it.
            if (sameButton && oldBounds.equals(r))
            {
                unreadButton.update(unreadCount);
            } else
            {
                unreadButton.setBounds(r);
                unreadButton.init(unreadCount);
            }
            guidesList.add(unreadButton);

            // Register the object this button is attached to (for event)
            unreadButton.setAttachedToObject(attachedGuide);
        }

        /**
         * Detach the buttons from the component hierarchy, effectively hiding them.
         */
        void detachButton()
        {
            if (unreadButton.getParent() != null)
            {
                Rectangle r = unreadButton.getBounds();
                guidesList.remove(unreadButton);
                guidesList.repaint(r);
            }
            attachedGuide = null;
            attachedRow = -1;
        }

        /**
         * Validate that the buttons are attached in the proper position.
         * If the buttons were attached to a row that no longer corresponds to
         * that feed, then just detach the buttons; they'll get reattached when the mouse
         * is moved. Otherwise, do an attachButtons again, which ensures that their
         * position and unread info is up-to-date.
         */
        void resetAttachment()
        {
            boolean showUnread = RenderingManager.isShowUnreadInGuides();

            if (attachedRow >= 0)
            {
                int row = attachedRow;
                IGuide guide = attachedGuide;

                // update attachment
                ListModel mdl = guidesList.getModel();
                if (showUnread && mdl.getSize() > row && mdl.getElementAt(row) == guide)
                {
                    attachButton(row, true);
                } else
                {
                    detachButton();
                }
            }
        }

        /**
         * The list has been resized - - reset button attachment.
         *
         * @param e ComponentEvent info.
         *
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
        public void componentResized(ComponentEvent e)
        {
            resetAttachment();
        }

        /**
         * Handle a press of the unread button.
         *
         * @param e ActionEvent info.
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            IGuide guide = (IGuide)e.getSource();

            if (guide != null)
            {
                // Mark visible feeds as read, but don't touch feeds
                // not currently shown.
                GlobalController.readGuides(true, guide);

                // Explicitly detach buttons. This really should be handled by the listener
                // events telling us something has changed, but currently those only fire for
                // feeds in the selected guide, while the button could be pressed on any guide.
                if (attachedGuide == guide) detachButton();
            }
        }

        /**
         * The guide's unread count needs to be updated. Add it to a list, and of needed, schedule
         * an update in the EDT.
         *
         * @param guide the guide whose unread count has changed.
         */
        private void unreadUpdateNeeded(IGuide guide)
        {
            if (guide == null)
            {
                LOG.log(Level.SEVERE, Strings.error("unspecified.guide"), new Exception("Dump"));
                return;
            }

            synchronized (unreadDirtyGuides)
            {
                if (!updateScheduled && uiInitialized) scheduleUpdate();
                unreadDirtyGuides.add(guide);
            }
        }

        /**
         * Marks the initial UI display completed -- i.e., the user can see the guides, feeds and
         * initial articles. At this point we schedule an update for the guide's unread counts,
         * which we had previously deferred so that the initial display would appear more quickly.
         */
        private void setUIInitialized()
        {
            if (!uiInitialized)
            {
                uiInitialized = true;

                synchronized (unreadDirtyGuides)
                {
                    if (!unreadDirtyGuides.isEmpty() && !updateScheduled)
                    {
                        scheduleUpdate();
                    }
                }
            }
        }

        /**
         * Schedule the update of the dirty (deferred) guide
         * unread counts, which will occur in the EDT thread.  
         */
        private void scheduleUpdate()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    IGuide[] tempDirtyGuides;

                    // Copy to temp array to reduce contention and
                    // avoid deadlocks
                    synchronized (unreadDirtyGuides)
                    {
                        tempDirtyGuides = unreadDirtyGuides.toArray(new IGuide[unreadDirtyGuides.size()]);
                        unreadDirtyGuides.clear();
                        updateScheduled = false;
                    }

                    boolean showUnread = RenderingManager.isShowUnreadInGuides();
                    if (!showUnread) return;

                    for (IGuide guide : tempDirtyGuides)
                    {
                        int row = guidesListModel.indexOf(guide);

                        if (row >= 0)
                        {
                            // Repaint the affected row.
                            Rectangle r = guidesList.getCellBounds(row, row);
                            guidesList.repaint(r);
                            // If this has the button attached to it,
                            // force an update of it so that the number
                            // it displays is in synch.
                            if (row == attachedRow && guide == attachedGuide)
                            {
                                attachButton(row, true);
                            }
                        } else detachButton();
                    }
                }
            });
            updateScheduled = true;
        }

        /**
         * Invoked when color for feeds of some class changes.
         *
         * @param feedClass feed class.
         * @param oldColor  old color value.
         * @param newColor  new color value.
         */
        public void onClassColorChanged(int feedClass, Color oldColor, Color newColor)
        {
            // If some feed have an opportunity to become visible or invisible
            // we should refresh guides unread counts
            if (oldColor == null || newColor == null)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();

                        for (int i = 0; i < set.getGuidesCount(); i++)
                            unreadUpdateNeeded(set.getGuideAt(i));
                    }
                });
            }
        }

        /**
         * A subclass for handling the DomainListener events that
         * we subscribe to in order to update the guide unread counts. 
         */
        private class DomainListener extends DomainAdapter
        {
            /**
             * Feed property changed. We look for "unread" property changes.
             *  
             * @param feed  feed that changed.
             * @param property property of the article.
             * @param oldValue old property value.
             * @param newValue new property value.
             */
            public void propertyChanged(IFeed feed, String property,
                    Object oldValue, Object newValue)
            {
                // We should think of catching all the situations when:
                // a) feed has changes in number of unread articles
                // b) feed appears and disappears in the list
                if (IFeed.PROP_UNREAD_ARTICLES_COUNT.equals(property) ||
                    DirectFeed.PROP_DISABLED.equals(property))
                {
                    IGuide[] parentGuides = feed.getParentGuides();
                    for (IGuide parentGuide : parentGuides) unreadUpdateNeeded(parentGuide);
                }
            }

            /**
             * Guide added.  Update its unread count.
             * 
             * @param set           guides set.
             * @param guide         added guide.
             * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
             */
            public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
            {
                unreadUpdateNeeded(guide);
            }

            /**
             * Invoked when the guide has been removed from the set. Doesn't affect unread counts.
             * 
             * @param set guides set.
             * @param guide removed guide.
             * @param index old guide index.
             */
            public void guideRemoved(GuidesSet set, IGuide guide, int index)
            {
                detachButton();
            }

            /**
             * Invoked when new feed has been added to the guide. That may cause the guide's
             * unread count to change, so update it.
             * 
             * @param guide parent guide.
             * @param feed added feed.
             */
            public void feedAdded(IGuide guide, IFeed feed)
            {
                unreadUpdateNeeded(guide);
            }

            /**
             * Invoked when the feed has been removed from the guide.
             * May cause the guide's unread count to change.
             *
             * @param event feed removal event.
             */
            public void feedRemoved(FeedRemovedEvent event)
            {
                unreadUpdateNeeded(event.getGuide());
            }
        }
    }
}