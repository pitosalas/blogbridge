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
// $Id: ImageFeedDisplayConfig.java,v 1.4 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.settings;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.uif.ShadowBorder;
import com.salas.bb.views.feeds.image.IImageFeedDisplayConfig;

import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseListener;
import java.net.URL;

/**
 * Image feed display configuration.
 */
public class ImageFeedDisplayConfig extends AbstractFeedDisplayConfig
    implements IImageFeedDisplayConfig
{
    private final static Border READ    = new ShadowBorder(Color.decode("#b0b0b0"));
    private final static Border UNREAD  = new ShadowBorder(Color.decode("#b0b0b0"));

    /**
     * Returns global background color.
     *
     * @return background color.
     */
    public Color getGlobalBGColor()
    {
        return getItemBGColor(false);
    }

    /**
     * Returns background color of individual item.
     *
     * @param selected <code>TRUE</code> to return selected item background.
     *
     * @return color.
     */
    public Color getItemBGColor(boolean selected)
    {
        return selected
            ? RenderingManager.getSelectedArticleBackground()
            : RenderingManager.getArticleBodyBackground();
    }

    /**
     * Returns the font should be used for rendering of the author name.
     *
     * @return font.
     */
    public Font getAuthorFont()
    {
        return RenderingManager.getArticleDateFont();
    }

    /**
     * Returns URL to image telling that there's no image available.
     *
     * @return URL.
     */
    public URL getNoImageURL()
    {
        return ResourceUtils.getURL("resources/no-img-110x165.gif");
    }

    /**
     * Returns border for picture.
     *
     * @param read <code>TRUE</code> to return border for "read" picture.
     *
     * @return border.
     */
    public Border getPictureBorder(boolean read)
    {
        return read ? READ : UNREAD;
    }

    /**
     * Returns adapter which is listening to the mouse events (press/release/click) from the views.
     * Useful for context menus.
     *
     * @return popup adapter.
     */
    public MouseListener getViewPopupAdapter()
    {
        return GlobalController.SINGLETON.getMainFrame().getImageDisplayPopupAdapter();
    }
}
