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
// $Id: NoFlickerSplashWrapper.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.splash.ImageSplash;
import com.jgoodies.uif.splash.SplashProvider;
import com.salas.bb.utils.RTUtils;

import java.awt.*;

/**
 * <p>A wrapper for <code>com.jgoodies.uif.splash.ImageSplash</code> which is
 * removing flickering when note or progress changes.</p>
 * 
 * <p>The reason of flickering is default lightweigt component's <code>update(Graphics)</code>
 * method which clears the background before <code>paint(Graphics)</code> method invocation.</p>
 * 
 * <p>Other solution is to ask JGoodies author to override
 * <code>ImageSplash.update(Graphics)</code>: i.e. <code>void update(Graphics g) {paint(g);}</code>
 * </p>
 * 
 * TODO remove after ImageSplash.update will be overriden
 */
public class NoFlickerSplashWrapper extends Window implements SplashProvider
{
    private ImageSplash imageSplash;
    
    /**
     * Creates ImageSplashWrapper and initializes it with wrapped ImageSplash
     * size/location values.
     * 
     * @param anImageSplash     image splash screen to wrap.
     */
    public NoFlickerSplashWrapper(ImageSplash anImageSplash)
    {
        super(anImageSplash.getOwner());
        
        imageSplash = anImageSplash;
        
        setSize(imageSplash.getSize());
        setLocation(imageSplash.getLocation());

        RTUtils.callIfPresent(this, "setAlwaysOnTop", true);
    }

    /**
     * Invoked to open splash.
     * 
     * @see SplashProvider#openSplash()
     */
    public void openSplash()
    {
        setVisible(true);
    }

    /**
     * Invoked to close splash &amp; release resources.
     * 
     * @see SplashProvider#closeSplash()
     */
    public void closeSplash()
    {
        dispose();
    }

    /**
     * Sets the note.
     * 
     * @param note splash note.
     *
     * @see SplashProvider#setNote(java.lang.String)
     */
    public void setNote(String note)
    {
        imageSplash.setNote(note);
        repaint();
    }

    /**
     * Sets the progress.
     * 
     * @param percent progress percent value (0 <= ... <= 100).
     *
     * @see SplashProvider#setProgress(int)
     */
    public void setProgress(int percent)
    {
        imageSplash.setProgress(percent);
        repaint();
    }

    /**
     * Paints the container.
     * 
     * @param g the specified Graphics window.
     */
    public void paint(Graphics g)
    {
        imageSplash.paint(g);
    }

    /**
     * Updates the container.
     * 
     * @param g the specified Graphics window.
     */
    public void update(Graphics g)
    {
        paint(g);
    }
}