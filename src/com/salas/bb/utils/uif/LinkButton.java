/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2011 by R. Pito Salas
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: R. Pito Salas
 * mailto:pitosalas@users.sourceforge.net
 * More information: about BlogBridge
 * http://www.blogbridge.com
 * http://sourceforge.net/projects/blogbridge
 */

package com.salas.bb.utils.uif;

import com.jgoodies.uif.util.ResourceUtils;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: alg Date: 6/18/11 Time: 20:49 To change this template use File | Settings | File
 * Templates.
 */
public class LinkButton extends LinkLabel
{
    public LinkButton(String iconResource)
    {
        this(ResourceUtils.getIcon(iconResource));
    }

    public LinkButton(ImageIcon icon)
    {
        super();
        setIcon(icon);
    }
}
