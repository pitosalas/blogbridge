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
// $Id: AbstractFeedDisplay.java,v 1.49 2008/04/08 08:06:19 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.sentiments.ArticleFilterProtector;
import com.salas.bb.utils.IdentityList;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.JumplessViewport;
import com.salas.bb.views.INavigationModes;
import static com.salas.bb.views.feeds.IFeedDisplayConstants.MODE_FULL;
import static com.salas.bb.views.feeds.IFeedDisplayConstants.MODE_MINIMAL;
import com.salas.bb.views.feeds.html.ArticlesGroup;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of {@link IFeedDisplay} which is capable of returning
 * itself was the renderable component.
 */
public abstract class AbstractFeedDisplay extends JPanel
    implements IFeedDisplay
{
    private static final Logger LOG = Logger.getLogger(AbstractFeedDisplay.class.getName());
    private boolean popupTriggered;

    /**
     * Selection mode show how the article selection event has to be interpreted.
     * The mode changes depending on the ctrl/shift status during the mouse clicks
     * over the article displays. (CTRL - TOGGLE, SHIFT - RANGE, no modifier - SINGLE). 
     */
    protected enum SelectionMode { SINGLE, TOGGLE, RANGE }

    private final List<IFeedDisplayListener>    listeners;
    private final IFeedDisplayConfig            config;
    protected final ArticlesGroup[]             groups;
    protected final FeedDisplayModel            model;
    protected final NoContentPanel              noContentPanel;

    /** Page model to update when page changes. */
    private final ValueModel pageModel;

    protected JViewport                         viewport;
    protected URL                               hoveredLink;

    /** Leading selection of the selected displays list. It's always present in the list. */
    protected IArticleDisplay                   selectedDisplay;
    /** All selected displays. */
    protected List<IArticleDisplay>             selectedDisplays;

    /** Indicates whether current article(s)Selected event originates here. */
    private boolean articleSelectionSource;

    /**
     * Abstract view.
     *
     * @param aConfig display configuration.
     * @param pageModel page model to update when page changes.
     * @param pageCountModel page model with the number of pages (updated by the FeedDisplayModel).
     */
    protected AbstractFeedDisplay(IFeedDisplayConfig aConfig, ValueModel pageModel, ValueModel pageCountModel)
    {
        this.pageModel = pageModel;
        listeners = new CopyOnWriteArrayList<IFeedDisplayListener>();
        config = aConfig;

        if (aConfig != null)
        {
            config.setListener(new ConfigListener());

            model = new FeedDisplayModel(pageCountModel);
            onFilterChange();
            model.addListener(new ModelListener());

            // Init groups
            groups = new ArticlesGroup[model.getGroupsCount()];
            for (int i = 0; i < model.getGroupsCount(); i++)
            {
                groups[i] = new ArticlesGroup(model.getGroupName(i), config.getGroupPopupAdapter());
                groups[i].setFont(config.getGroupDividerFont());
            }

            updateGroupsSettings();
            updateSortingOrder();

            selectedDisplays = new IdentityList<IArticleDisplay>();
            selectedDisplay = null;
            hoveredLink = null;

            // No content panel
            noContentPanel = new NoContentPanel(getNoContentPanelMessage());
            noContentPanel.setBackground(config.getDisplayBGColor());
            updateNoContentPanel();
        } else
        {
            model = null;
            groups = null;
            noContentPanel = null;
        }
    }

    /**
     * Converts the mouse event into the selection mode. It analyzes the modifiers and
     * decides on the mode.
     *
     * @param e event.
     *
     * @return mode.
     */
    protected static SelectionMode eventToMode(MouseEvent e)
    {
        SelectionMode mode = SelectionMode.SINGLE;

        int mod = e.getModifiersEx();
        int ctrl = MouseEvent.CTRL_DOWN_MASK;
        int shift = MouseEvent.SHIFT_DOWN_MASK;

        if ((mod & ctrl) == ctrl)
        {
            mode = SelectionMode.TOGGLE;
        } else if ((mod & shift) == shift)
        {
            mode = SelectionMode.RANGE;
        }

        return mode;
    }

    /**
     * Sets the view page.
     *
     * @param page page.
     */
    public void setPage(int page)
    {
        if (model != null)
        {
            model.setPage(page);
            pageModel.setValue(page);
            scrollToTop();
        }
    }

    /**
     * Sets the view page size (in articles).
     *
     * @param size size of the page.
     */
    public void setPageSize(int size)
    {
        if (model != null) model.setPageSize(size);
    }

    /** Updates the sorting order of the list. */
    private void updateSortingOrder()
    {
        IFeed feed = model.getFeed();
        if (feed != null && feed.getAscendingSorting() != null)
        {
            setAscending(feed.getAscendingSorting());
        } else setAscending(config.isAscendingSorting());
    }

    /**
     * Returns <code>TRUE</code> during firing the article(s) selection event, so that it's possible to learn if it's
     * the source of this event. This is particularily useful when it's necessary to skip sending the event back to the
     * display.
     *
     * @return <code>TRUE</code> if current event has come from this component.
     */
    public boolean isArticleSelectionSource()
    {
        return articleSelectionSource;
    }

    /**
     * Updates the contents of the no-content panel.
     */
    private void updateNoContentPanel()
    {
        if (noContentPanel == null || config == null) return;

        boolean visible = !config.showEmptyGroups() && !model.hasVisibleArticles();
        noContentPanel.setVisible(visible);
        if (visible) noContentPanel.setMessage(getNoContentPanelMessage());
    }

    /**
     * Returns message to show when there's no articles to display.
     *
     * @return the message.
     */
    protected String getNoContentPanelMessage()
    {
        return null;
    }

    /**
     * Converts rectrangle of view port to no-content panel bounds.
     *
     * @param r rectangle.
     *
     * @return bounds rectangle.
     */
    protected Rectangle rectToNoContentBounds(Rectangle r)
    {
        return r;
    }

    /**
     * Returns the message to show in the no content panel.
     *
     * @return the message.
     */
//    protected abstract String getNoContentMessage();

    /**
     * Releases all links and resources and prepares itself to be garbage collected.
     */
    public void prepareForDismiss()
    {
        if (model != null) model.prepareToDismiss();
        if (config != null) config.setListener(null);
        setViewport(null);
    }

    /**
     * Sets the viewport which will be used for showing this component.
     *
     * @param aViewport viewport.
     */
    public void setViewport(JViewport aViewport)
    {
        viewport = aViewport;
        if (noContentPanel != null) noContentPanel.setViewport(aViewport);
    }

    /**
     * Returns displayable feed view component.
     *
     * @return displayable feed view component.
     */
    public JComponent getComponent()
    {
        return this;
    }

    /**
     * Adds listener.
     *
     * @param l listener.
     */
    public void addListener(IFeedDisplayListener l)
    {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Removes listener.
     *
     * @param l listener.
     */
    public void removeListener(IFeedDisplayListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fire article selected event.
     *
     * @param lead              lead article.
     * @param selectedArticles  all selected articles.
     */
    protected void fireArticleSelected(IArticle lead, IArticle[] selectedArticles)
    {
        for (IFeedDisplayListener l : listeners) l.articleSelected(lead, selectedArticles);
    }

    /**
     * Fire link hovered event.
     *
     * @param link link.
     */
    protected void fireLinkHovered(URL link)
    {
        for (IFeedDisplayListener l : listeners) l.linkHovered(link);
    }

    /**
     * Fire link clicked event.
     *
     * @param link link.
     */
    protected void fireLinkClicked(URL link)
    {
        for (IFeedDisplayListener l : listeners) l.linkClicked(link);
    }

    /**
     * Fire feed jump link clicked event.
     *
     * @param feed feed.
     */
    protected void fireFeedJumpLinkClicked(IFeed feed)
    {
        for (IFeedDisplayListener l : listeners) l.feedJumpLinkClicked(feed);
    }

    /**
     * Fire the zoom-in event.
     */
    protected void fireZoomIn()
    {
        for (IFeedDisplayListener l : listeners) l.onZoomIn();
    }

    /**
     * Fire the zoom-out event.
     */
    protected void fireZoomOut()
    {
        for (IFeedDisplayListener l : listeners) l.onZoomOut();
    }

    /**
     * Get display configuration.
     *
     * @return configuration.
     */
    public IFeedDisplayConfig getConfig()
    {
        return config;
    }

    /** Requests focus for this display. */
    public void focus()
    {
        requestFocusInWindow();
    }

    /**
     * Request focus for the list component.
     *
     * @return <code>FALSE</code> if focusing is guaranteed to fail.
     */
    public boolean requestFocusInWindow()
    {
        boolean focused = true;

        if ((selectedDisplay == null && !this.selectFirstArticle(getConfig().getViewMode())) ||
            !selectedDisplay.focus())
        {
            focused = super.requestFocusInWindow();
        }

        return focused;
    }

    /**
     * Finds the display to remove.
     *
     * @param aArticle      article displayed.
     * @param aGroup        group reported by a model.
     * @param aIndexInGroup index within the reported group.
     *
     * @return display or <code>NULL</code>.
     */
    private IArticleDisplay findDisplay(IArticle aArticle, int aGroup, int aIndexInGroup)
    {
        IArticleDisplay display;

        int index = getDisplayIndex(aGroup, aIndexInGroup);
        Component cmp = index < getComponentCount() ? getComponent(index) : null;
        display = cmp == null || !(cmp instanceof IArticleDisplay) ? null : (IArticleDisplay)cmp;

        // Check if correct display is found
        if (display != null && display.getArticle() != aArticle)
        {
            getLogger().severe(MessageFormat.format(
                Strings.error("ui.wrong.article.has.been.found"),
                aGroup, aIndexInGroup));
            display = null;
        }

        // Plan B -- looking for an article display using direct iteration
        if (display == null)
        {
            getLogger().severe(MessageFormat.format(
                Strings.error("ui.missing.display"),
                index, aGroup, aIndexInGroup));

            display = findArticleDisplay(aArticle);
        }

        // If display is not found -- we are in trouble!
        if (display == null) getLogger().severe(Strings.error("ui.display.was.not.found"));

        return display;
    }

    /**
     * Finds article display directly among all article components.
     *
     * @param aArticle article we are looking for.
     *
     * @return display component or <code>NULL</code>.
     */
    protected IArticleDisplay findArticleDisplay(IArticle aArticle)
    {
        IArticleDisplay aDisplay = null;

        for (int i = 0; aDisplay == null && i < getComponentCount(); i++)
        {
            Component cmp = getComponent(i);
            if (cmp instanceof IArticleDisplay)
            {
                IArticleDisplay dsp = (IArticleDisplay)cmp;
                if (dsp.getArticle() == aArticle) aDisplay = dsp;
            }
        }

        return aDisplay;
    }

    /**
     * Returns current logger.
     *
     * @return logger object.
     */
    protected abstract Logger getLogger();

    /**
     * Orders to select next article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectNextArticle(int mode)
    {
        return selectNextArticle(mode, selectedDisplay);
    }

    /**
     * Orders to select next article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectFirstArticle(int mode)
    {
        boolean selected = selectNextArticle(mode, null);
        if (selected) ensureSelectedViewDisplayed();
        return selected;
    }

    /**
     * Orders to select previous article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectPreviousArticle(int mode)
    {
        return selectPreviousArticle(selectedDisplay, mode);
    }

    /**
     * Orders to select last article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectLastArticle(int mode)
    {
        boolean selected = selectPreviousArticle(null, mode);
        if (selected) ensureSelectedViewDisplayed();
        return selected;
    }

    /**
     * Sets the feed which is required to be displayed.
     *
     * @param feed the feed.
     */
    public void setFeed(IFeed feed)
    {
        selectDisplay(null, false, SelectionMode.SINGLE);
        model.setFeed(feed);
        updateSortingOrder();

        if (viewport != null)
        {
            if (viewport instanceof JumplessViewport) ((JumplessViewport)viewport).resetStoredPosition();
            scrollTo(new Rectangle(viewport.getWidth(), viewport.getHeight()));
        }
    }


    /**
     * Returns currently selected text in currently selected article.
     *
     * @return text.
     */
    public String getSelectedText()
    {
        return null;
    }

    /**
     * Repaints all highlights in all visible articles.
     */
    public void repaintHighlights()
    {
        Iterator it = new ArticleDisplayIterator();
        while (it.hasNext())
        {
            IArticleDisplay display = (IArticleDisplay)it.next();
            display.updateHighlights();
        }
    }

    /**
     * Repaints all sentiments color codes.
     */
    public void repaintSentimentsColorCodes()
    {
        Iterator it = new ArticleDisplayIterator();
        while (it.hasNext())
        {
            IArticleDisplay display = (IArticleDisplay)it.next();
            display.updateColorCode();
        }
    }

    /**
     * Selects the next display after given.
     *
     * @param mode              mode of selection.
     * @param currentDisplay    currently selected display or <code>NULL</code>.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    private boolean selectNextArticle(int mode, IArticleDisplay currentDisplay)
    {
        boolean selected = false;

        IArticleDisplay display = findNextDisplay(currentDisplay, mode);
        if (display != null)
        {
            selectDisplay(display, true, SelectionMode.SINGLE);
            selected = true;
        } else
        {
            // See if there are more pages
            int pages = model.getPagesCount();
            int page = model.getPage();
            if (page < pages - 1)
            {
                // Go to the next page and select the first article
                setPage(page + 1);
                selected = selectFirstArticle(mode);
            }
        }

        return selected;
    }

    /**
     * Selects the previous display after given.
     *
     * @param mode              mode of selection.
     * @param currentDisplay    currently selected display or <code>NULL</code>.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    private boolean selectPreviousArticle(IArticleDisplay currentDisplay, int mode)
    {
        boolean selected = false;

        IArticleDisplay display = findPrevDisplay(currentDisplay, mode);
        if (display != null)
        {
            selectDisplay(display, true, SelectionMode.SINGLE);
            selected = true;
        } else
        {
            // See if there are more pages before this one
            int page = model.getPage();
            if (page > 0)
            {
                // Go to the next page and select the first article
                setPage(page - 1);
                selected = selectLastArticle(mode);
            }
        }

        return selected;
    }

    /**
     * Changes currently selected display. Depending on the mode of selection the result will
     * be different.
     * <p/>
     * For the {@link SelectionMode#RANGE} mode, articles between the {@link #selectedDisplay} and
     * this new display are selected inclusively and the {@link #selectedDisplay} is assigned
     * this new display.
     * <p/>
     * For the {@link SelectionMode#TOGGLE} mode, the article is toggled selected and makes it
     * in and out of the selected list. When the article is selected, it becomes the new
     * {@link #selectedDisplay}, when deselected the closest (if any) becomes.
     * <p/>
     * For the {@link SelectionMode#SINGLE} mode, the only article is selected and present in
     * the {@link #selectedDisplays} list.
     *
     * @param display       new display selection.
     * @param forceScroll   <code>TRUE</code> to force scrolling even when some link is hovered.
     * @param mode          mode of the article display selection.
     */
    protected void selectDisplay(IArticleDisplay display, boolean forceScroll, SelectionMode mode)
    {
        boolean fireEvent = selectDisplayWithoutEvent(display, forceScroll, mode);

        if (fireEvent)
        {
            try
            {
                // Mark us as the source of the event
                articleSelectionSource = true;

                fireArticleSelected(getSelectedArticle(), getSelectedArticles());
            } finally
            {
                // Release the flag
                articleSelectionSource = false;
            }
        }
    }

    /**
     * Returns all selected articles.
     *
     * @return articles.
     */
    private IArticle[] getSelectedArticles()
    {
        IArticle[] articles = new IArticle[selectedDisplays.size()];

        int i = 0;
        for (IArticleDisplay display : selectedDisplays)
        {
            articles[i++] = display.getArticle();
        }

        return articles;
    }

    /**
     * Changes currently selected display and says whether it's desired to fire event or no.
     *
     * @param display       display to select.
     * @param forceScroll   <code>TRUE</code> to force scrolling even when some link is hovered.
     * @param mode          mode of the article display selection.
     *
     * @return <code>TRUE</code> if the selection was changed and the event is preferred.
     *
     * @see #selectDisplay More info on the modes
     */
    protected boolean selectDisplayWithoutEvent(IArticleDisplay display, boolean forceScroll, SelectionMode mode)
    {
        boolean fireEvent;

        if (mode == SelectionMode.SINGLE)
        {
            fireEvent = processSingleSelectionMode(display, forceScroll);
        } else if (mode == SelectionMode.TOGGLE)
        {
            fireEvent = processToggleSelectionMode(display);
        } else
        {
            // Range display selection mode

            if (selectedDisplay == null || selectedDisplay == display)
            {
                fireEvent = processSingleSelectionMode(display, forceScroll);
            } else
            {
                // Find view indexes of the components
                int newLeadIndex = indexOf(display.getComponent());
                int oldLeadIndex = indexOf(selectedDisplay.getComponent());

                if (newLeadIndex == -1 || oldLeadIndex == -1)
                {
                    // Revert to the simple toggle mode in case of unexpected results
                    fireEvent = processToggleSelectionMode(display);
                } else
                {
                    // Get all currently selected articles in a temp array
                    IArticleDisplay[] current = selectedDisplays.toArray(new IArticleDisplay[selectedDisplays.size()]);
                    selectedDisplays.clear();

                    // Find the min and max
                    int min = Math.min(oldLeadIndex, newLeadIndex);
                    int max = Math.max(oldLeadIndex, newLeadIndex);

                    // Start filling with new displays
                    for (int i = min; i <= max; i++)
                    {
                        Component comp = getComponent(i);
                        if (comp instanceof IArticleDisplay)
                        {
                            IArticleDisplay disp = (IArticleDisplay)comp;
                            disp.setSelected(true);
                            selectedDisplays.add(disp);
                        }
                    }

                    // Walk through the past displays and deselect all that are not in the
                    // present list
                    for (IArticleDisplay disp : current)
                    {
                        if (!selectedDisplays.contains(disp)) disp.setSelected(false);
                    }

                    // Select a new lead
                    selectedDisplay = display;

                    fireEvent = true;
                }
            }
        }

        // If there's some display selected, request the focus
        // NOTE: Maybe we need to request it all the time?
        if (selectedDisplay != null) requestFocusInWindow();

        return fireEvent;
    }

    private boolean processToggleSelectionMode(IArticleDisplay display)
    {
        boolean fireEvent;// Toggle the display mode
        boolean displayIsSelected = selectedDisplays.contains(display);

        // Toggle the display selection state
        display.setSelected(!displayIsSelected);

        if (displayIsSelected)
        {
            // Display is selected, we deselect it
            selectedDisplays.remove(display);

            // If the display is current lead selection, we need to find another one
            if (display == selectedDisplay)
            {
                // Select the first display as a lead
                selectedDisplay = selectedDisplays.size() == 0 ? null : selectedDisplays.get(0);
            }
        } else
        {
            // Display is not selected, we select it and make our lead
            selectedDisplays.add(display);
            selectedDisplay = display;
        }

        // We need to fire the updates
        fireEvent = true;
        return fireEvent;
    }

    private boolean processSingleSelectionMode(IArticleDisplay display, boolean forceScroll)
    {
        boolean fireEvent;
        fireEvent = selectedDisplays.size() > 1 || selectedDisplay != display;

        // Set all selected articles deselected and remove them from the selected list
        // except for our new selection
        for (IArticleDisplay disp : selectedDisplays) if (disp != display) disp.setSelected(false);

        // Clear displays and add the new selection
        selectedDisplays.clear();
        if (display != null) selectedDisplays.add(display);

        if (display != selectedDisplay)
        {
            selectedDisplay = display;
            if (selectedDisplay != null)
            {
                selectedDisplay.setSelected(true);

                if (forceScroll || hoveredLink == null) ensureSelectedViewDisplayed();
            }
        }
        return fireEvent;
    }

    /**
     * Finds index of the component.
     *
     * @param component component.
     *
     * @return index or <code>-1</code> if component wasn't found.
     */
    private int indexOf(Component component)
    {
        int index = -1;

        Component[] components = getComponents();
        for (int i = 0; index == -1 && i < components.length; i++)
        {
            if (components[i] == component) index = i;
        }

        return index;
    }

    /**
     * Finds next view with a properties fitting the mode.
     *
     * @param currentDisplay   current view.
     * @param aMode         mode.
     *
     * @return next view or <code>NULL</code>.
     *
     * @see com.salas.bb.views.INavigationModes#MODE_NORMAL
     * @see com.salas.bb.views.INavigationModes#MODE_UNREAD
     */
    private IArticleDisplay findNextDisplay(IArticleDisplay currentDisplay, int aMode)
    {
        IArticleDisplay nextDisplay = null;

        int currentIndex = currentDisplay == null ? -1 : indexOf(currentDisplay.getComponent());
        for (int i = currentIndex + 1; nextDisplay == null && i < getComponentCount(); i++)
        {
            Component comp = getComponent(i);
            if (comp instanceof IArticleDisplay)
            {
                IArticleDisplay display = (IArticleDisplay)comp;
                if (display.getComponent().isVisible() &&
                    fitsMode(display.getArticle(), aMode))
                {
                    nextDisplay = display;
                }
            }
        }

        return nextDisplay;
    }

    /**
     * Finds previous view with a properties fitting the mode.
     *
     * @param currentDisplay   current view.
     * @param aMode         mode.
     *
     * @return previous view or <code>NULL</code>.
     *
     * @see com.salas.bb.views.INavigationModes#MODE_NORMAL
     * @see com.salas.bb.views.INavigationModes#MODE_UNREAD
     */
    private IArticleDisplay findPrevDisplay(IArticleDisplay currentDisplay, int aMode)
    {
        IArticleDisplay prevDisplay = null;

        int currentIndex = currentDisplay == null
            ? getComponentCount()
            : indexOf(currentDisplay.getComponent());

        for (int i = currentIndex - 1; prevDisplay == null && i >= 0; i--)
        {
            Component comp = getComponent(i);
            if (comp instanceof IArticleDisplay)
            {
                IArticleDisplay display = (IArticleDisplay)comp;
                if (display.getComponent().isVisible() &&
                    fitsMode(display.getArticle(), aMode))
                {
                    prevDisplay = (IArticleDisplay)comp;
                }
            }
        }

        return prevDisplay;
    }

    /**
     * Called when model reports that there is no articles to display.
     */
    private void onArticlesRemoved()
    {
        Component[] components = getComponents();
        for (Component component : components)
        {
            if (component instanceof ArticlesGroup)
            {
                ((ArticlesGroup)component).unregisterAll();
            } else if (component instanceof IArticleDisplay)
            {
                remove(component);

                IArticleDisplay display = ((IArticleDisplay)component);
                IArticle article = display.getArticle();
                article.removeListener(display.getArticleListener());
            }
        }
        updateNoContentPanel();

        // When page changes, we scroll to the top
        scrollToTop();
    }

    private void scrollToTop()
    {
        Rectangle rect = getVisibleRect();
        rect.y = 0;
        scrollTo(rect);
    }

    /**
     * Called when model has another article added to some group.
     *
     * @param aArticle      added article.
     * @param aGroup        group the article was added to.
     * @param aIndexInGroup index in the group.
     */
    private void onArticleAdded(IArticle aArticle, int aGroup, int aIndexInGroup)
    {
        updateNoContentPanel();

        IArticleDisplay display = createNewArticleDisplay(aArticle);
        display.addHyperlinkListener(new LinkListener());

        Component component = display.getComponent();
        component.setVisible(false);
        int index = getDisplayIndex(aGroup, aIndexInGroup);
        try
        {
            add(component, index);
            groups[aGroup].register(display);

            aArticle.addListener(display.getArticleListener());
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, "Failed to add article at: " + index +
                " (group=" + aGroup +
                ", ingroup=" + aIndexInGroup +
                ", groupIndex=" + indexOf(groups[aGroup]) +
                ", components=" + getComponentCount() + ")");
        }
    }

    /**
     * Called when model has lost the article and it should no longer be displayed.
     *
     * @param aArticle      removed article.
     * @param aGroup        group it was.
     * @param aIndexInGroup index in group which was occupied with it.
     */
    private void onArticleRemoved(IArticle aArticle, int aGroup, int aIndexInGroup)
    {
        IArticleDisplay display = findDisplay(aArticle, aGroup, aIndexInGroup);
        if (display == null) return;

        Component dispComponent = display.getComponent();
        boolean wasVisible = dispComponent.isVisible();
        dispComponent.setVisible(false);

        if (selectedDisplay != null)
        {
            if (selectedDisplay == display)
            {
                // TODO: we probably don't want to reset whole selection if the leading (or any other) display gets removed
                selectDisplay(null, false, SelectionMode.SINGLE);
            } else if (wasVisible)
            {
                Rectangle boundsDis = dispComponent.getBounds();
                Rectangle boundsView = viewport.getViewRect();

                int delta = boundsView.y - boundsDis.y;
                if (delta > 0)
                {
                    boundsView.y -= delta;
                    scrollTo(boundsView);
                }
            }
        }

        remove(dispComponent);
        groups[aGroup].unregister(display);

        aArticle.removeListener(display.getArticleListener());
        updateNoContentPanel();
    }

    /**
     * Creates new article display for addition to the display.
     *
     * @param aArticle article to create display for.
     *
     * @return display.
     */
    protected abstract IArticleDisplay createNewArticleDisplay(IArticle aArticle);

    /**
     * Sets the hovered link.
     *
     * @param link link.
     */
    private void setHoveredHyperLink(URL link)
    {
        if (hoveredLink == link) return;

        hoveredLink = link;
        fireLinkHovered(hoveredLink);
    }

    /**
     * Sets the order of sorting. Default is descending (latest first).
     *
     * @param asc   <code>TRUE</code> for ascending order, <code>FALSE</code> for descending.
     */
    public void setAscending(boolean asc)
    {
        model.setAscending(asc);

        // Change the name of groups after reverting the order
        for (int i = 0; i < groups.length; i++)
        {
            groups[i].setName(model.getGroupName(i));
        }
    }

    /**
     * Scan through all displays and switch their collapse/expand statuses.
     *
     * @param aCollapsing collapse.
     */
    protected void collapseAll(boolean aCollapsing)
    {
        if (model.getArticlesCount() > 0)
        {
            for (int i = 0; i < getComponentCount(); i++)
            {
                Component component = getComponent(i);
                if (component instanceof IArticleDisplay)
                {
                    IArticleDisplay display = (IArticleDisplay)component;
                    display.setCollapsed(aCollapsing);
                }
            }

            if (aCollapsing) requestFocus(); else requestFocusInWindow();
        }
    }

    /**
     * Cycles view mode forward.
     */
    public void cycleViewModeForward()
    {
        cycleViewMode(true, true);
    }

    /**
     * Cycles view mode backward.
     */
    public void cycleViewModeBackward()
    {
        cycleViewMode(true, false);
    }

    protected void cycleViewMode(boolean global, boolean forward)
    {
        int cvm = 0;
        if (selectedDisplay != null) {
            cvm = selectedDisplay.getViewMode();
        } else {
            IArticleDisplay display = findNextDisplay(null, INavigationModes.MODE_NORMAL);
            cvm = (display != null) ? display.getViewMode() : config.getViewMode();
        }

        int nvm = cvm + (forward ? 1 : -1);
        if (nvm < MODE_MINIMAL) nvm = MODE_FULL; else
        if (nvm > MODE_FULL) nvm = MODE_MINIMAL;

        if (global)
        {
            Iterator<IArticleDisplay> it = new ArticleDisplayIterator();
            while (it.hasNext()) it.next().setViewMode(nvm);
        } else if (selectedDisplay != null)
        {
            selectedDisplay.setViewMode(nvm);
        }
    }

    /**
     * If there's display selected, collapses or expands it (switches).
     *
     * @param aCollapsing <code>TRUE</code> to collapse.
     */
    protected void collapseSelected(boolean aCollapsing)
    {
        if (selectedDisplays.size() > 0)
        {
            for (IArticleDisplay display : selectedDisplays) display.setCollapsed(aCollapsing);
            if (aCollapsing) requestFocus(); else requestFocusInWindow();
        }
    }

    /**
     * Updates settings of all groups.
     */
    private void updateGroupsSettings()
    {
        for (ArticlesGroup group : groups)
        {
            group.setCanBeVisible(config.showGroups());
            group.setVisibleIfEmpty(config.showEmptyGroups());
        }

        updateNoContentPanel();
    }

    /**
     * Scrolls to make rectangle visible.
     *
     * @param rect rectangle.
     */
    private void scrollTo(final Rectangle rect)
    {
        Container parent = getParent();
        if (parent != null && parent instanceof IScrollContoller)
        {
            ((IScrollContoller)parent).scrollTo(rect);
        } else scrollRectToVisible(rect);
    }

    /**
     * Returns index of article which is inside the group at specified index.
     *
     * @param aGroup        group index.
     * @param aIndexInGroup index within the group.
     *
     * @return article index.
     */
    private int getDisplayIndex(int aGroup, int aIndexInGroup)
    {
        return indexOf(groups[aGroup]) + aIndexInGroup + 1;
    }

    /**
     * Returns selected article.
     *
     * @return selected article or <code>NULL</code>.
     */
    private IArticle getSelectedArticle()
    {
        return selectedDisplay == null ? null : selectedDisplay.getArticle();
    }

    /**
     * Makes a finest adjustment to show a selected article display in the most gentle way.
     */
    private void ensureSelectedViewDisplayed()
    {
        Rectangle portRect = viewport.getViewRect();
        Component component = selectedDisplay.getComponent();
        Rectangle rect = component.getBounds();

        boolean includesGroup = false;
        if (config.showGroups())
        {
            int index = indexOf(component);
            if (index > 0 && getComponent(index - 1) instanceof ArticlesGroup)
            {
                // This is the first article in a group -- let's display its group as well
                component = getComponent(index - 1);
                Rectangle groupRect = component.getBounds();
                rect.setBounds(groupRect.x, groupRect.y, rect.width,
                    rect.height + (rect.y - groupRect.y));

                includesGroup = true;
            }
        }

        int portY = (int)portRect.getY();
        int portH = (int)portRect.getHeight();
        int viewY = (int)rect.getY();
        int viewH = (int)rect.getHeight();

        int portB = portY + portH;
        int viewB = viewY + viewH;

        boolean invisible = viewB < portY || viewY > portB;
        boolean coveringPort = !invisible && viewY <= portY && viewB >= portB;

        if (coveringPort)
        {
            rect = null;
        } else if (invisible || viewH > portH || viewY < portY || viewB > portB)
        {
            if (!includesGroup) rect.y = Math.max(rect.y - 10, 0);
            rect.width = viewport.getWidth();
            rect.height = viewport.getHeight();
        }

        if (viewport instanceof JumplessViewport)
        {
            JumplessViewport jvp = (JumplessViewport)viewport;
            jvp.resetStoredPosition();
        }

        if (rect != null) scrollTo(rect);
    }

    /**
     * Returns <code>TRUE</code> if article fits conditions of the mode.
     *
     * @param aArticle  article.
     * @param aMode     mode.
     *
     * @return <code>TRUE</code> if article fits conditions of the mode.
     *
     * @see com.salas.bb.views.INavigationModes#MODE_NORMAL
     * @see com.salas.bb.views.INavigationModes#MODE_UNREAD
     */
    private boolean fitsMode(IArticle aArticle, int aMode)
    {
        return aMode == INavigationModes.MODE_NORMAL ||
            (aMode == INavigationModes.MODE_UNREAD && !aArticle.isRead());
    }

    /**
     * Orders view to select and show article if it can be visible.
     *
     * @param article article to select.
     */
    public void selectArticle(IArticle article)
    {
        int newPage = model.ensureArticleVisibility(article);
        if (newPage != -1) pageModel.setValue(newPage);
        
        final IArticleDisplay display = findArticleDisplay(article);
        if (display != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    selectDisplayWithoutEvent(display, true, SelectionMode.SINGLE);
                }
            });
        }
    }

    /**
     * Repaints article text if is currently in the given mode.
     *
     * @param briefMode <code>TRUE</code> for brief mode, otherwise -- full mode.
     */
    public void repaintIfInMode(boolean briefMode)
    {
        // Does nothing by default
    }

    /**
     * Model listener.
     */
    private class ModelListener implements IFeedDisplayModelListener
    {
        /** Invoked when all articles removed from the model as result of feeds change. */
        public void articlesRemoved()
        {
            onArticlesRemoved();
        }

        /**
         * Invoked when model receives the event about new article addition.
         *
         * @param article      new article.
         * @param group        group index this article was assigned to.
         * @param indexInGroup index inside the group.
         */
        public void articleAdded(IArticle article, int group, int indexInGroup)
        {
            onArticleAdded(article, group, indexInGroup);
        }

        /**
         * Invoked when model receives the event about article removal.
         *
         * @param article      deleted article.
         * @param group        group index this article was assigned to.
         * @param indexInGroup index inside the group.
         */
        public void articleRemoved(IArticle article, int group, int indexInGroup)
        {
            onArticleRemoved(article, group, indexInGroup);
        }
    }

    /**
     * Article views iterator.
     */
    protected class ArticleDisplayIterator implements Iterator<IArticleDisplay>
    {
        private final Component[] components;
        private int nextView;

        /**
         * Creates iterator.
         */
        public ArticleDisplayIterator()
        {
            components = getComponents();
            nextView = findNextView(-1);
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other words, returns
         * <tt>true</tt> if <tt>next</tt> would return an element rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext()
        {
            return nextView != -1;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         *
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
         */
        public IArticleDisplay next()
        {
            IArticleDisplay display = null;
            if (nextView != -1)
            {
                display = (IArticleDisplay)components[nextView];
                nextView = findNextView(nextView);
            }

            return display;
        }

        /**
         * Finds next view index given current view index.
         *
         * @param aViewIndex current view index.
         *
         * @return next view index or <code>-1</code> if there's no view.
         */
        private int findNextView(int aViewIndex)
        {
            int next = -1;

            for (int i = aViewIndex + 1; next == -1 && i < components.length; i++)
            {
                Component comp = components[i];
                if (comp instanceof IArticleDisplay) next = i;
            }

            return next;
        }

        /**
         * Unsupported.
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not supported by
         *                                       this Iterator.
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Mouse Event processing
    // ---------------------------------------------------------------------------------------------

    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        Object component = getComponentForMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_PRESSED:
                popupTriggered = false;
                if (component instanceof IArticleDisplay)
                {
                    IArticleDisplay articleDisplay = (IArticleDisplay)component;
                    // Note that isRightMouseButton may not be very well suited for all systems
                    // It should match the popup dialog guesture
                    if (!e.isPopupTrigger() || !selectedDisplays.contains(articleDisplay))
                    {
                        selectDisplay(articleDisplay, false, eventToMode(e));
                    }

                    MouseListener popup = (hoveredLink != null) ? getLinkPopupAdapter() : getViewPopupAdapter();
                    if (popup != null) popup.mousePressed(e);

                    popupTriggered = e.isPopupTrigger();
                } else requestFocus();
                break;

            case MouseEvent.MOUSE_RELEASED:
                if (component instanceof IArticleDisplay)
                {
                    MouseListener popup = (hoveredLink != null) ? getLinkPopupAdapter() : getViewPopupAdapter();
                    if (popup != null) popup.mouseReleased(e);
                }
                break;

            case MouseEvent.MOUSE_CLICKED:
                if (SwingUtilities.isLeftMouseButton(e) && component instanceof IArticleDisplay)
                {
                    URL link = null;
                    IArticle article = null;
                    if (hoveredLink != null)
                    {
                        link = hoveredLink;
                    } else if (e.getClickCount() == 2)
                    {
                        article = ((IArticleDisplay)component).getArticle();
                        link = article.getLink();
                    }

                    if (link != null && !popupTriggered) fireLinkClicked(link);
                    if (article != null)
                    {
                        GlobalModel model = GlobalModel.SINGLETON;
                        GlobalController.readArticles(true, model.getSelectedGuide(), model.getSelectedFeed(), article);
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Returns the component the user clicked on.
     *
     * @param e event.
     *
     * @return component.
     */
    protected Object getComponentForMouseEvent(MouseEvent e)
    {
        return e.getSource();
    }

    /**
     * Returns the view popup adapter.
     *
     * @return view popup adapter.
     */
    protected MouseListener getViewPopupAdapter() { return null; }

    /**
     * Returns the link popup adapter.
     *
     * @return link popup adapter.
     */
    protected MouseListener getLinkPopupAdapter() { return null; }

    /**
     * Forwards the mouse wheel event higher to a parent.
     * @param e event to forward.
     */
    private void forwardMouseWheelHigher(MouseWheelEvent e)
    {
        int newX, newY;

        newX = e.getX() + getX(); // Coordinates take into account at least
        newY = e.getY() + getY(); // the cursor's position relative to this
                                  // Component (e.getX()), and this Component's
                                  // position relative to its parent.

        Container parent = getParent();
        if (parent == null) return;

        // Fix coordinates to be relative to new event source
        newX += parent.getX();
        newY += parent.getY();

        // Change event to be from new source, with new x,y
        MouseWheelEvent newMWE = new MouseWheelEvent(parent, e.getID(), e.getWhen(),
            e.getModifiers(), newX, newY, e.getClickCount(), e.isPopupTrigger(),
            e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());

        parent.dispatchEvent(newMWE);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e)
    {
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 && e.getScrollAmount() != 0)
        {
            // Zooming in / out
            boolean in = e.getWheelRotation() > 0;

            if (in) fireZoomIn(); else fireZoomOut();
        } else forwardMouseWheelHigher(e);
    }
    
    // ---------------------------------------------------------------------------------------------
    // Hyperlink support
    // ---------------------------------------------------------------------------------------------

    /**
     * Listener of all hyper-link related events.
     */
    private class LinkListener implements HyperlinkListener
    {
        /**
         * Called when a hypertext link is updated.
         *
         * @param e the event responsible for the update
         */
        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            HyperlinkEvent.EventType type = e.getEventType();
            if (type != HyperlinkEvent.EventType.ACTIVATED)
            {
                URL link = (type == HyperlinkEvent.EventType.ENTERED) ? e.getURL() : null;
                setHoveredHyperLink(link);

                JComponent textPane = (JComponent)e.getSource();
                String tooltip = getHoveredLinkTooltip(link, textPane);
                textPane.setToolTipText(tooltip);
            }
        }
    }

    /**
     * Returns tool-tip for a give link.
     *
     * @param link          link.
     * @param textPane      pane requesting the tooltip.
     *
     * @return tool-tip text.
     */
    protected String getHoveredLinkTooltip(URL link, JComponent textPane)
    {
        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // Configuration changes
    // ---------------------------------------------------------------------------------------------

    /**
     * Listens to configuration changes and takes appropriate actions.
     */
    protected class ConfigListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that has
         *            changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            onConfigPropertyChange(evt.getPropertyName());
        }
    }

    /**
     * Invoked when config property changes.
     *
     * @param name name of the property.
     */
    protected void onConfigPropertyChange(String name)
    {
        if (IFeedDisplayConfig.THEME.equals(name))
        {
            onThemeChange();
        } else if (IFeedDisplayConfig.FILTER.equals(name))
        {
            if (ArticleFilterProtector.canSwitchTo(config.getFilter())) onFilterChange();
        } else if (IFeedDisplayConfig.MODE.equals(name))
        {
            onViewModeChange();
        } else if (IFeedDisplayConfig.SORT_ORDER.equals(name))
        {
            updateSortingOrder();
        } else if (IFeedDisplayConfig.GROUPS_VISIBLE.equals(name) ||
            IFeedDisplayConfig.EMPTY_GROUPS_VISIBLE.equals(name))
        {
            updateGroupsSettings();
        } else if (IFeedDisplayConfig.FONT_BIAS.equals(name))
        {
            onFontBiasChange();
        }
    }

    /**
     * Called when filter changes.
     */
    protected void onFilterChange()
    {
        model.setFilter(config.getFilter());
        updateNoContentPanel();
    }

    /**
     * Called when theme changes.
     */
    protected void onThemeChange()
    {
        if (noContentPanel != null) noContentPanel.setBackground(config.getDisplayBGColor());

        Iterator it = new ArticleDisplayIterator();
        while (it.hasNext()) ((IArticleDisplay)it.next()).onThemeChange();

        for (ArticlesGroup group : groups) group.setFont(config.getGroupDividerFont());
    }

    /**
     * Invoked on view mode change.
     */
    protected void onViewModeChange()
    {
        Iterator it = new ArticleDisplayIterator();
        while (it.hasNext()) ((IArticleDisplay)it.next()).onViewModeChange();
    }

    /**
     * Invoked on font bias change.
     */
    private void onFontBiasChange()
    {
        Iterator it = new ArticleDisplayIterator();
        while (it.hasNext()) ((IArticleDisplay)it.next()).onFontBiasChange();
    }

    /**
     * Content panel.
     */
    private class NoContentPanel extends JPanel
    {
        private final ViewportSizeMonitor monitor;
        private final JLabel lbMessage;

        private JViewport viewport;

        /**
         * Creates panel with the given message.
         *
         * @param aMessage message.
         */
        public NoContentPanel(String aMessage)
        {
            setLayout(new FormLayout("5dlu, center:p:grow, 5dlu", "5dlu:grow, p, 5dlu:grow"));
            CellConstraints cc = new CellConstraints();

            lbMessage = new JLabel(aMessage);
            add(lbMessage, cc.xy(2, 2));

            monitor = new ViewportSizeMonitor();
        }

        /**
         * Sets the background color of this component.
         *
         * @param bg the desired background <code>Color</code>
         */
        public void setBackground(Color bg)
        {
            super.setBackground(bg);
            if (lbMessage != null) lbMessage.setBackground(bg);
        }

        /**
         * Registers viewport to follow.
         *
         * @param aViewport viewport.
         */
        public void setViewport(JViewport aViewport)
        {
            if (viewport != null) viewport.removeComponentListener(monitor);
            viewport = aViewport;
            if (viewport != null)
            {
                onViewportResize();
                viewport.addComponentListener(monitor);
            }
        }

        /**
         * Called when the viewport has been resized and this component
         * size requires updates.
         */
        private void onViewportResize()
        {
            Rectangle viewRect = rectToNoContentBounds(viewport.getViewRect());
            Dimension size = new Dimension(viewRect.width, viewRect.height);

            setMinimumSize(size);
            setPreferredSize(size);
            setBounds(viewRect);
        }

        /**
         * Sets the message.
         *
         * @param msg message.
         */
        public void setMessage(String msg)
        {
            lbMessage.setText(msg);
        }

        /**
         * Monitors changes in view port size and make the size of this component
         * match.
         */
        private class ViewportSizeMonitor extends ComponentAdapter
        {
            /** Invoked when the component's size changes. */
            public void componentResized(ComponentEvent e)
            {
                onViewportResize();
            }
        }
    }
}
