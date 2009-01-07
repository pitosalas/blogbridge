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
// $Id: PostToBlogAction.java,v 1.30 2008/03/31 15:29:14 spyromus Exp $
//

package com.salas.bb.remixfeeds;

import com.salas.bb.core.FeatureManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.feed.FeedLinkPostToBlogAction;
import com.salas.bb.remixfeeds.editor.AbstractPostEditor;
import com.salas.bb.remixfeeds.editor.PostEditor;
import com.salas.bb.remixfeeds.editor.PostEditorAdv;
import com.salas.bb.remixfeeds.prefs.BloggingPreferences;
import com.salas.bb.remixfeeds.prefs.TargetBlog;
import com.salas.bb.remixfeeds.templates.Template;
import com.salas.bb.remixfeeds.templates.Templates;
import com.salas.bb.remixfeeds.type.*;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.views.settings.RenderingManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * Opens the post edit dialog for currently selected article and then,
 * upon confirmation, posts it to blog.
 */
public class PostToBlogAction extends AbstractAction implements PropertyChangeListener
{
    private static PostToBlogAction instanceForArticle;
    private static PostToBlogAction instanceForFeed;
    private static PostToBlogAction instanceForGuide;
    private static ActionSelector actionSelector;

    private final IType type;

    /**
     * Hidden singleton constructor.
     *
     * @param type type of the action.
     */
    protected PostToBlogAction(IType type)
    {
        this.type = type;

        update_();

        FeatureManager fm = GlobalController.SINGLETON.getFeatureManager();
        fm.addPropertyChangeListener(FeatureManager.PROP_PTB_ENABLED, this);
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized ActionSelector getActionSelector()
    {
        if (actionSelector == null) actionSelector = new ActionSelector();
        return actionSelector;
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized PostToBlogAction getInstanceForArticle()
    {
        if (instanceForArticle == null) instanceForArticle = new PostToBlogAction(new ArticleType());
        return instanceForArticle;
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized PostToBlogAction getInstanceForFeed()
    {
        if (instanceForFeed == null) instanceForFeed = new PostToBlogAction(new FeedType());
        return instanceForFeed;
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized PostToBlogAction getInstanceForGuide()
    {
        if (instanceForGuide == null) instanceForGuide = new PostToBlogAction(new GuideType());
        return instanceForGuide;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        BloggingPreferences bloggingPreferences = GlobalModel.SINGLETON.getUserPreferences().getBloggingPreferences();
        if (type.isAvailable() && bloggingPreferences.getBlogsCount() > 0)
        {
            GlobalController controller = GlobalController.SINGLETON;

            TargetBlog targetBlog = bloggingPreferences.getDefaultBlog();

            String templateName = e.getActionCommand();
            if (!Templates.isExisting(templateName))
            {
                templateName = targetBlog.getTemplateName();

                // See if the user wants to change the template dynamically
                if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0 && type.isTemplateChangeSupported())
                {
                    // Ask for mode in a popup menu and repeat the event
                    MainFrame frame = controller.getMainFrame();
                    Point point = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(point, frame);

                    JPopupMenu menu = new JPopupMenu();
                    Collection<Template> templates = Templates.getUserTemplates().values();
                    for (Template template : templates)
                    {
                        menu.add(new PostToBlogTemplateAction(template.getName()));
                    }
                    menu.show(frame, (int)point.getX(), (int)point.getY());

                    return;
                }
            }

            AbstractPostEditor editor;
            MainFrame frame = controller.getMainFrame();
            FeatureManager fm = controller.getFeatureManager();
            boolean isRichEditor = bloggingPreferences.isRichEditor() || !fm.isPtbAdvanced();
            if (fm.isPtbAdvanced())
            {
                PostEditorAdv edAdv = new PostEditorAdv(frame, isRichEditor);
                java.util.List<TargetBlog> blogs = bloggingPreferences.getBlogs();
                edAdv.setTargetBlogs(blogs.toArray(new TargetBlog[blogs.size()]),
                    new TargetBlog[] { targetBlog });

                editor = edAdv;
            } else
            {
                PostEditor edNor = new PostEditor(frame, isRichEditor);
                edNor.setTargetBlog(targetBlog);

                editor = edNor;
            }

            // Get template
            Template template = Templates.getByName(templateName);

            PostData data = type.getPostData(template);
            if (data.text == null) return;

            editor.setPostTitle(data.title);
            editor.setPostText(data.text);
            editor.setSourceArticle(data.sourceArticle);
            editor.setEditorFont(RenderingManager.getArticleBodyFont());
            editor.open();
        }
    }

    public static void update()
    {
        if (instanceForArticle != null) instanceForArticle.update_();
        if (instanceForFeed != null) instanceForFeed.update_();
        if (instanceForGuide != null) instanceForGuide.update_();
    }

    /** Updates the state of this action. */
    protected void update_()
    {
        setEnabled(type.isAvailable());
    }

    /**
     * Invoked when the status of PTB-enable feature changes.
     *
     * @param evt property event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        update();
        FeedLinkPostToBlogAction.update();
    }

    /** The action for the template popup menu. Calls the main action with encoded mode. */
    class PostToBlogTemplateAction extends AbstractAction
    {
        private final String templateName;

        /**
         * Defines an <code>Action</code> object with a default
         * description string and default icon.
         *
         * @param name the name of the template.
         */
        public PostToBlogTemplateAction(String name)
        {
            super(name);
            templateName = name;
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent e)
        {
            PostToBlogAction.this.actionPerformed(new ActionEvent(e.getSource(), e.getID(), templateName));
        }
    }

    /** Selects the action according to where the focus is. */
    private static class ActionSelector extends AbstractAction
    {
        /**
         * Invoked when action is performed.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            // Find current focus
            KeyboardFocusManager keyFocusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Component current = keyFocusManager.getFocusOwner();

            // Pass the event to someone 
            MainFrame mf = GlobalController.SINGLETON.getMainFrame();
            if (mf.getGudiesPanel().getFocusableComponent() == current)
            {
                instanceForGuide.actionPerformed(e);
            } else if (mf.getFeedsPanel().getFeedsList() == current)
            {
                instanceForFeed.actionPerformed(e);
            } else
            {
                instanceForArticle.actionPerformed(e);
            }
        }
    }
}
