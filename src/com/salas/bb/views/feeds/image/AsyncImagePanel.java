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
// $Id: AsyncImagePanel.java,v 1.20 2007/11/19 08:24:48 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.images.ImageFetcher;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.net.URL;

/**
 * Image icon with asynchronous load of data.
 */
class AsyncImagePanel extends JComponent
{
    private static final int INDICATION_BORDER  = 0;
    private static final int INDICATION_FADE    = 1;
    private static final int INDICATION_FRAME   = 2;
    private static final int INDICATION_TYPE    = INDICATION_FRAME;

    private static final int STATUS_INCEPTION   = -1;
    private static final int STATUS_LOADING     = 0;
    private static final int STATUS_LOADED      = 1;
    private static final int STATUS_FAILED      = 2;

    private static final AlphaComposite COMPOSITE_MODE_1 =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
    private static final AlphaComposite COMPOSITE_MODE_2 =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    private final URL           imageURL;
    private final ImageHandler  observer;
    private int                 maxWidth;
    private int                 maxHeight;

    private Image               image;
    private Image               rescaledImage;
    private Image               badge;

    private int                 status;

    private boolean             secondMode;
    private boolean             overrideFirstMode;

    /**
     * Creates component.
     *
     * @param aImageURL     URL to load data from.
     * @param aWidth        max width of image.
     * @param aHeight       max height of image.
     * @param aBorder       border to wrap picture with.
     * @param aSecondMode   <code>TRUE</code> to switch to second mode.
     *
     * @throws NullPointerException if URL is not specified.
     */
    public AsyncImagePanel(URL aImageURL, int aWidth, int aHeight, Border aBorder,
        boolean aSecondMode)
    {
        if (aImageURL == null) throw new NullPointerException(Strings.error("unspecified.url"));

        imageURL = aImageURL;
        image = null;
        rescaledImage = null;
        observer = new ImageHandler();

        secondMode = aSecondMode;
        overrideFirstMode = false;

        status = STATUS_INCEPTION;
        setBorder(aBorder);
        setImageSize(new Dimension(aWidth, aHeight));
    }

    /**
     * Switches display into second mode.
     *
     * @param aSecondMode second mode.
     */
    public void setSecondMode(boolean aSecondMode)
    {
        if (secondMode != aSecondMode)
        {
            secondMode = aSecondMode;
            repaint();
        }
    }

    /**
     * Sets a badge to paint over the image.
     *
     * @param badge badge to paint.
     */
    public void setBadge(Image badge)
    {
        this.badge = badge;
        repaint();
    }

    /**
     * Sets new image size and repaints everything.
     *
     * @param dim dimension.
     */
    public void setImageSize(Dimension dim)
    {
        maxWidth = dim.width;
        maxHeight = dim.height;

        int width = maxWidth;
        int height = maxHeight;
        Border border = getBorder();

        if (border != null)
        {
            Insets borderInsets = border.getBorderInsets(this);
            width += borderInsets.left + borderInsets.right;
            height += borderInsets.top + borderInsets.bottom;
        }

        setPreferredSize(new Dimension(width, height));

        if (STATUS_LOADED == status)
        {
            rescaledImage = null;
            loadImage();
        }
    }

    /**
     * Starts loading image.
     */
    private void loadImage()
    {
        status = STATUS_LOADING;
        image = ImageFetcher.load(imageURL);
        if (image.getHeight(observer) != -1) status = STATUS_LOADED;
    }

    /**
     * Calls the UI delegate's paint method, if the UI delegate is non-<code>null</code>.  We pass
     * the delegate a copy of the <code>Graphics</code> object to protect the rest of the paint code
     * from irrevocable changes (for example, <code>Graphics.translate</code>). <p> If you override
     * this in a subclass you should not make permanent changes to the passed in
     * <code>Graphics</code>. For example, you should not alter the clip <code>Rectangle</code> or
     * modify the transform. If you need to do these operations you may find it easier to create a
     * new <code>Graphics</code> from the passed in <code>Graphics</code> and manipulate it.
     * Further, if you do not invoker super's implementation you must honor the opaque property,
     * that is if this component is opaque, you must completely fill in the background in a
     * non-opaque color. If you do not honor the opaque property you will likely see visual
     * artifacts.
     *
     * @param g the <code>Graphics</code> object to protect
     *
     * @see #paint
     * @see javax.swing.plaf.ComponentUI
     */
    protected void paintComponent(Graphics g)
    {
        if (status == STATUS_INCEPTION) loadImage();

        if (INDICATION_TYPE == INDICATION_FADE && g instanceof Graphics2D)
        {
            ((Graphics2D)g).setComposite(!overrideFirstMode && secondMode
                ? COMPOSITE_MODE_2 : COMPOSITE_MODE_1);
        }

        switch (status)
        {
            case STATUS_LOADING:
                paintLoading(g);
                break;
            case STATUS_LOADED:
                paintLoaded(g);
                break;
            default:
                paintFailed(g);
                break;
        }
    }

    /** Paint loading state. */
    private void paintLoading(Graphics g)
    {
        g.setColor(Color.LIGHT_GRAY);
        paintPlaceholder(g);
    }

    /** Paint failed state. */
    private void paintFailed(Graphics g)
    {
        g.setColor(Color.GRAY);
        paintPlaceholder(g);
    }

    /** Paint image placeholder. */
    private void paintPlaceholder(Graphics g)
    {
        Border border = getBorder();
        Insets insets = border.getBorderInsets(this);

        int il = insets.left;
        int it = insets.top;

        g.fillRect(il, it, maxWidth, maxHeight);
        getBorder().paintBorder(this, g, il, it, maxWidth, maxHeight);

        int imgWidth = maxWidth;
        int imgHeight = maxHeight;

        paintBadge(g, imgWidth, imgHeight);
    }

    /**
     * Paints badge when present.
     *
     * @param g         graphics context.
     * @param imgWidth  image width.
     * @param imgHeight image height.
     */
    private void paintBadge(Graphics g, int imgWidth, int imgHeight)
    {
        if (badge != null)
        {
            int bwidth = badge.getWidth(null);
            int bheight = badge.getHeight(null);

            if (bwidth > 0 && bheight > 0 && imgWidth > bwidth && imgHeight > bheight)
            {
                Border border = getBorder();
                Insets insets = border.getBorderInsets(this);

                // Badge is valid
                int x = maxWidth / 2 + imgWidth / 2 - bwidth - 2;
                int y = maxHeight / 2 + imgHeight / 2 - bheight - 2;

                g.drawImage(badge, insets.left + x, insets.top + y, bwidth, bheight, null);
            }
        }
    }

    /** Paint loaded state. */
    private void paintLoaded(Graphics g)
    {
        if (rescaledImage == null)
        {
            rescaledImage = rescaleImage(image);
            if (rescaledImage != null) image = null;
        }

        if (rescaledImage != null)
        {
            int width = rescaledImage.getWidth(observer);
            int height = rescaledImage.getHeight(observer);

            if (width == -1 || height == -1)
            {
                paintLoading(g);
            } else
            {
                Border border = getBorder();
                Insets insets = border.getBorderInsets(this);

                int picX = (maxWidth - width) / 2 + insets.left;
                int picY = (maxHeight - height) / 2 + insets.top;

                g.drawImage(rescaledImage, picX, picY, width, height, observer);

                if (INDICATION_TYPE == INDICATION_BORDER && g instanceof Graphics2D)
                {
                    ((Graphics2D)g).setComposite(!overrideFirstMode && secondMode
                        ? COMPOSITE_MODE_2 : COMPOSITE_MODE_1);
                }

                border.paintBorder(this, g, picX, picY, width, height);

                if (INDICATION_TYPE == INDICATION_FRAME && !secondMode)
                {
                    g.setColor(Color.WHITE);
                    g.drawRect(picX + 1, picY + 1, width - 2, height - 2);
                    g.setColor(Color.BLACK);
                    g.drawRect(picX, picY, width, height);
                }

                paintBadge(g, width, height);
            }
        }
    }

    /**
     * Rescales an image to fit the desired dimensions.
     *
     * @param src source image.
     *
     * @return destination image.
     */
    private Image rescaleImage(Image src)
    {
        Image dest = null;

        int imgWidth = src.getWidth(observer);
        if (imgWidth != -1)
        {
            int imgHeight = src.getHeight(observer);

            if (imgHeight != -1)
            {
                int picWidth = imgWidth;
                int picHeight = imgHeight;

                if (picWidth > maxWidth || picHeight > maxHeight)
                {
                    picWidth = imgWidth > imgHeight ? maxWidth : (imgWidth * maxHeight / imgHeight);
                    picHeight = imgWidth > imgHeight ? (imgHeight * maxWidth / imgWidth) : maxHeight;
                }

                dest = src.getScaledInstance(picWidth, picHeight, Image.SCALE_SMOOTH);
            }
        }

        return dest;
    }

    /**
     * Paints the component's border. <p> If you override this in a subclass you should not make
     * permanent changes to the passed in <code>Graphics</code>. For example, you should not alter
     * the clip <code>Rectangle</code> or modify the transform. If you need to do these operations
     * you may find it easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it.
     *
     * @param g the <code>Graphics</code> context in which to paint
     *
     * @see #paint
     * @see #setBorder
     */
    protected void paintBorder(Graphics g)
    {
        // No border painting required.
    }

    /**
     * Catch mouse pointer over the image and repaint it with normal mode.
     *
     * @param e mouse event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_ENTERED:
                overrideFirstMode = true;
                repaint();
                break;

            case MouseEvent.MOUSE_EXITED:
                overrideFirstMode = false;
                repaint();
                break;

            default:
                break;
        }
    }

    /**
     * Keeps an eye on loading of images.
     */
    private class ImageHandler implements ImageObserver
    {
        /**
         * Invoked when loading of image brought some new bits or has been finished.
         *
         * @param img       the image being observed.
         * @param infoflags the bitwise inclusive OR of the following flags:  <code>WIDTH</code>,
         *                  <code>HEIGHT</code>, <code>PROPERTIES</code>, <code>SOMEBITS</code>,
         *                  <code>FRAMEBITS</code>, <code>ALLBITS</code>, <code>ERROR</code>,
         *                  <code>ABORT</code>.
         * @param x         the <i>x</i> coordinate.
         * @param y         the <i>y</i> coordinate.
         * @param width     the width.
         * @param height    the height.
         *
         * @return <code>false</code> if the infoflags indicate that the image is completely loaded;
         *         <code>true</code> otherwise.
         */
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
        {
            boolean loaded = (infoflags & (ALLBITS | ERROR | ABORT)) != 0;

            if (loaded)
            {
                status = (infoflags & ALLBITS) != 0 ? STATUS_LOADED : STATUS_FAILED;
                repaint(0);
            }

            return !loaded;
        }
    }
}
