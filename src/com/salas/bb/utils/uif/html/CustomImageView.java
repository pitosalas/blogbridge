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
// $Id: CustomImageView.java,v 1.24 2008/04/09 06:07:11 spyromus Exp $
//

package com.salas.bb.utils.uif.html;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.imageblocker.ImageBlocker;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.images.ImageFetcher;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Customized version of image view. We assign tweaked URLStreamHandler to image URL to unblock
 * the GUI during image loading.
 *
 * Author of original class taken as a base from Sun SDK is Scott Violet.
 */
public class CustomImageView extends View
{
    private static final Logger LOG = Logger.getLogger(CustomImageView.class.getName());

    /**
     * If true, when some of the bits are available a repaint is done.
     * <p/>
     * This is set to false as swing does not offer a repaint that takes a
     * delay. If this were true, a bunch of immediate repaints would get
     * generated that end up significantly delaying the loading of the image
     * (or anything else going on for that matter).
     */
    private static boolean sIsInc;
    /**
     * Repaint delay when some of the bits are available.
     */
    private static int sIncRate = 100;
    /**
     * Icon used while the image is being loaded.
     */
    private static Icon sPendingImageIcon;
    /**
     * Icon used if the image could not be found.
     */
    private static Icon sMissingImageIcon;

    /**
     * Document property for image cache.
     */
    private static final String IMAGE_CACHE_PROPERTY = "imageCache";

    // Height/width to use before we know the real size, these should at least
    // the size of <code>sMissingImageIcon</code> and
    // <code>sPendingImageIcon</code>
    private static final int DEFAULT_WIDTH = 0;
    private static final int DEFAULT_HEIGHT = 0;

    // Show no images for pending / missing pictures
    private static final boolean NO_STATE_IMAGES = true;

    /**
     * Link border to use if other is not specified.
     */
    private static final int LINK_BORDER = 0;

    // Bitmask values
    private static final int LOADING_FLAG = 1;
    private static final int LINK_FLAG = 2;
    private static final int WIDTH_FLAG = 4;
    private static final int HEIGHT_FLAG = 8;
    private static final int RELOAD_FLAG = 16;
    private static final int RELOAD_IMAGE_FLAG = 32;
    private static final int SYNC_LOAD_FLAG = 64;

    private AttributeSet    attr;
    private Image           image;
    private int             width;
    private int             height;

    /**
     * Bitmask containing some of the above bitmask values. Because the
     * image loading notification can happen on another thread access to
     * this is synchronized (at least for modifying it).
     */
    private int             state;
    private Container       container;
    private Rectangle       fBounds;
    private Color           borderColor;

    // Size of the border, the insets contains this valid. For example, if
    // the HSPACE attribute was 4 and BORDER 2, leftInset would be 6.
    private short           borderSize;

    // Insets, obtained from the painter.
    protected short         leftInset;
    protected short         rightInset;
    protected short         topInset;
    protected short         bottomInset;

    /**
     * We don't directly implement ImageObserver, instead we use an instance
     * that calls back to us.
     */
    private ImageObserver   imageObserver;

    /**
     * Used for alt text. Will be non-null if the image couldn't be found,
     * and there is valid alt text.
     */
    private View            altView;

    /**
     * Alignment along the vertical (Y) axis.
     */
    private float           vAlign;
    private URLStreamHandler handler;

    /**
     * Creates a new view that represents an IMG element.
     *
     * @param elem the element to create a view for
     */
    public CustomImageView(Element elem)
    {
        super(elem);
        fBounds = new Rectangle();
        imageObserver = new ImageHandler();
        state = RELOAD_FLAG | RELOAD_IMAGE_FLAG;
        handler = new CustomHtmlUrlStreamHandler();
    }

    /**
     * Returns the text to display if the image can't be loaded. This is
     * obtained from the Elements attribute set with the attribute name
     * <code>HTML.Attribute.ALT</code>.
     *
     * @return alternative text.
     */
    public String getAltText()
    {
        return (String)getElement().getAttributes().getAttribute(HTML.Attribute.ALT);
    }

    /**
     * Return a URL for the image source,
     * or null if it could not be determined.
     *
     * @return URL of image.
     */
    public URL getImageURL()
    {
        URL imageURL = null;

        String src = (String)getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
        if (src != null)
        {
            src = src.replaceAll(" ", "%20");
            URL reference = ((HTMLDocument)getDocument()).getBase();
            try
            {
                imageURL = new URL(reference, src, handler);
            } catch (MalformedURLException e)
            {
                imageURL = null;
            }

            // If image URL is blocked, never return it.
            if (ImageBlocker.isBlocked(imageURL)) imageURL = null;
        }

        return imageURL;
    }

    /**
     * Returns the icon to use if the image couldn't be found.
     *
     * @return icon of absent image.
     */
    public Icon getNoImageIcon()
    {
        Icon icon = null;

        if (!NO_STATE_IMAGES)
        {
            synchronized (CustomImageView.class)
            {
                if (sMissingImageIcon == null) sMissingImageIcon = ResourceUtils.getIcon("html.image.missing.icon");
                icon = sMissingImageIcon;
            }
        }

        return icon;
    }

    /**
     * Returns the icon to use while in the process of loading the image.
     *
     * @return icon of loading image.
     */
    public Icon getLoadingImageIcon()
    {
        Icon icon = null;

        if (!NO_STATE_IMAGES)
        {
            synchronized (CustomImageView.class)
            {
                if (sPendingImageIcon == null) sPendingImageIcon = ResourceUtils.getIcon("html.image.pending.icon");
                icon = sPendingImageIcon;
            }
        }

        return icon;
    }

    /**
     * Returns the image to render.
     *
     * @return image.
     */
    public Image getImage()
    {
        sync();
        return image;
    }

    /**
     * Sets how the image is loaded. If <code>newValue</code> is true,
     * the image we be loaded when first asked for, otherwise it will
     * be loaded asynchronously. The default is to not load synchronously,
     * that is to load the image asynchronously.
     *
     * @param newValue ...
     */
    public void setLoadsSynchronously(boolean newValue)
    {
        synchronized (this)
        {
            if (newValue)
            {
                state |= SYNC_LOAD_FLAG;
            } else
            {
                state = (state | SYNC_LOAD_FLAG) ^ SYNC_LOAD_FLAG;
            }
        }
    }

    /**
     * Returns true if the image should be loaded when first asked for.
     *
     * @return ...
     */
    public boolean getLoadsSynchronously()
    {
        return ((state & SYNC_LOAD_FLAG) != 0);
    }

    /**
     * Convenience method to get the StyleSheet.
     * @return stylesheet.
     */
    protected StyleSheet getStyleSheet()
    {
        HTMLDocument doc = (HTMLDocument)getDocument();
        return doc.getStyleSheet();
    }

    /**
     * Fetches the attributes to use when rendering.  This is
     * implemented to multiplex the attributes specified in the
     * model with a StyleSheet.
     *
     * @return atrributes.
     */
    public AttributeSet getAttributes()
    {
        sync();
        return attr;
    }

    /**
     * For images the tooltip text comes from text specified with the
     * <code>ALT</code> attribute. This is overriden to return
     * <code>getAltText</code>.
     *
     * @see javax.swing.text.JTextComponent#getToolTipText
     */
    public String getToolTipText(float x, float y, Shape allocation)
    {
        return getAltText();
    }

    /**
     * Update any cached values that come from attributes.
     */
    protected void setPropertiesFromAttributes()
    {
        StyleSheet sheet = getStyleSheet();
        this.attr = sheet.getViewAttributes(this);

        // Gutters
        borderSize = (short)getIntAttr(HTML.Attribute.BORDER, isLink() ? LINK_BORDER : 0);
        if (borderSize == 0 && image == null) borderSize = 1;

        rightInset = (short)(getIntAttr(HTML.Attribute.HSPACE, 0) + borderSize);
        leftInset = rightInset;
        bottomInset = (short)(getIntAttr(HTML.Attribute.VSPACE, 0) + borderSize);
        topInset = bottomInset;

        borderColor = ((StyledDocument)getDocument()).getForeground(getAttributes());

        AttributeSet attrs = getElement().getAttributes();

        // Alignment.
        // PENDING: This needs to be changed to support the CSS versions
        // when conversion from ALIGN to VERTICAL_ALIGN is complete.
        Object alignment = attrs.getAttribute(HTML.Attribute.ALIGN);

        vAlign = 1.0f;
        if (alignment != null)
        {
            alignment = alignment.toString();
            if ("top".equals(alignment))
            {
                vAlign = 0f;
            } else if ("middle".equals(alignment))
            {
                vAlign = .5f;
            }
        }

        AttributeSet anchorAttr = (AttributeSet)attrs.getAttribute(HTML.Tag.A);
        if (anchorAttr != null && anchorAttr.isDefined(HTML.Attribute.HREF))
        {
            synchronized (this)
            {
                state |= LINK_FLAG;
            }
        } else
        {
            synchronized (this)
            {
                state = (state | LINK_FLAG) ^ LINK_FLAG;
            }
        }
    }

    /**
     * Establishes the parent view for this view.
     * Seize this moment to cache the AWT Container I'm in.
     *
     * @param parent parent.
     */
    public void setParent(View parent)
    {
        View oldParent = getParent();
        super.setParent(parent);
        container = (parent != null) ? getContainer() : null;
        if (oldParent != parent)
        {
            synchronized (this)
            {
                state |= RELOAD_FLAG;
            }
        }
    }

    /**
     * Invoked when the Elements attributes have changed. Recreates the image.
     *
     * @param e ...
     * @param a ...
     * @param f ...
     */
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f)
    {
        super.changedUpdate(e, a, f);

/*

// This is part of the original implementation and it resets the image every time some
// attribute of the element (?) has changed.
// We don't need this as we don't change the attributes of image IMG tags at run-time.
//
// The negative side of this call is that it produces weird effects in conjunction with
// caching. Empty images with correct dimensions are written because of two consequential
// caching attempts (yes, I can't explain the details).

        synchronized (this)
        {
            state |= RELOAD_FLAG | RELOAD_IMAGE_FLAG;
        }

        // Assume the worst.
        preferenceChanged(null, true, true);
*/
    }

    /**
     * Paints the View.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     * @see javax.swing.text.View#paint
     */
    public void paint(Graphics g, Shape a)
    {
        sync();

        Rectangle rect = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();

        Image anImage = getImage();
        Rectangle clip = g.getClipBounds();

        fBounds.setBounds(rect);
        paintHighlights(g, a);
        paintBorder(g, rect);
        if (clip != null)
        {
            g.clipRect(rect.x + leftInset, rect.y + topInset,
                    rect.width - leftInset - rightInset,
                    rect.height - topInset - bottomInset);
        }

        boolean brokenImage = true;
        if (anImage != null)
        {
            try
            {
                if (!hasPixels(anImage))
                {
                    // No pixels yet, use the default
                    Icon icon = getLoadingImageIcon();

                    if (icon != null)
                    {
                        icon.paintIcon(getContainer(), g, rect.x + leftInset, rect.y + topInset);
                    }
                } else
                {
                    // Draw the anImage
                    g.drawImage(anImage, rect.x + leftInset, rect.y + topInset,
                            width, height, imageObserver);
                }
                brokenImage = false;
            } catch (RuntimeException e)
            {
                image = null;
                LOG.log(Level.WARNING, Strings.error("img.failed.to.paint.image"), e);
            }
        }

        if (brokenImage)
        {
            Icon icon = getNoImageIcon();

            if (icon != null)
            {
                icon.paintIcon(getContainer(), g, rect.x + leftInset, rect.y + topInset);
            }

            View view = getAltView();
            // Paint the view representing the alt text, if its non-null
            if (view != null && ((state & WIDTH_FLAG) == 0 || width > DEFAULT_WIDTH))
            {
                // Assume layout along the y direction
                Rectangle altRect = new Rectangle(
                    rect.x + leftInset + DEFAULT_WIDTH, rect.y + topInset,
                    rect.width - leftInset - rightInset - DEFAULT_WIDTH,
                    rect.height - topInset - bottomInset);

                view.paint(g, altRect);
            }
        }

        if (clip != null)
        {
            // Reset clip.
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }
    }

    private void paintHighlights(Graphics g, Shape shape)
    {
        if (container instanceof JTextComponent)
        {
            JTextComponent tc = (JTextComponent)container;
            Highlighter h = tc.getHighlighter();
            if (h instanceof LayeredHighlighter)
            {
                ((LayeredHighlighter)h).paintLayeredHighlights(g, getStartOffset(),
                    getEndOffset(), shape, tc, this);
            }
        }
    }

    private void paintBorder(Graphics g, Rectangle rect)
    {
        Color color = borderColor;

        if (borderSize > 0 && color != null)
        {
            int xOffset = leftInset - borderSize;
            int yOffset = topInset - borderSize;
            g.setColor(color);
            for (int counter = 0; counter < borderSize; counter++)
            {
                g.drawRect(rect.x + xOffset + counter,
                        rect.y + yOffset + counter,
                        rect.width - counter - counter - xOffset - xOffset - 1,
                        rect.height - counter - counter - yOffset - yOffset - 1);
            }
        }
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either X_AXIS or Y_AXIS
     * @return the span the view would like to be rendered into;
     *         typically the view is told to render into the span
     *         that is returned, although there is no guarantee;
     *         the parent may choose to resize or break the view
     */
    public float getPreferredSpan(int axis)
    {
        sync();

        // If the attributes specified a width/height, always use it!
        if (axis == View.X_AXIS && (state & WIDTH_FLAG) == WIDTH_FLAG)
        {
            getPreferredSpanFromAltView(axis);
            return width + leftInset + rightInset;
        }
        if (axis == View.Y_AXIS && (state & HEIGHT_FLAG) == HEIGHT_FLAG)
        {
            getPreferredSpanFromAltView(axis);
            return height + topInset + bottomInset;
        }

        Image anImage = getImage();

        if (anImage != null)
        {
            switch (axis)
            {
                case View.X_AXIS:
                    return width + leftInset + rightInset;
                case View.Y_AXIS:
                    return height + topInset + bottomInset;
                default:
                    throw new IllegalArgumentException(MessageFormat.format(
                        Strings.error("img.invalid.axis"), axis));
            }
        } else
        {
            View view = getAltView();
            float retValue = 0f;

            if (view != null)
            {
                retValue = view.getPreferredSpan(axis);
            }
            switch (axis)
            {
                case View.X_AXIS:
                    return retValue + (float)(width + leftInset + rightInset);
                case View.Y_AXIS:
                    return retValue + (float)(height + topInset + bottomInset);
                default:
                    throw new IllegalArgumentException(MessageFormat.format(
                        Strings.error("img.invalid.axis"), axis));
            }
        }
    }

    /**
     * Determines the desired alignment for this view along an
     * axis.  This is implemented to give the alignment to the
     * bottom of the icon along the y axis, and the default
     * along the x axis.
     *
     * @param axis may be either X_AXIS or Y_AXIS
     * @return the desired alignment; this should be a value
     *         between 0.0 and 1.0 where 0 indicates alignment at the
     *         origin and 1.0 indicates alignment to the full span
     *         away from the origin; an alignment of 0.5 would be the
     *         center of the view
     */
    public float getAlignment(int axis)
    {
        switch (axis)
        {
            case View.Y_AXIS:
                return vAlign;
            default:
                return super.getAlignment(axis);
        }
    }

    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert
     * @param a   the allocated region to render into
     * @param b   position bias.
     *
     * @return the bounding box of the given position
     *
     * @throws javax.swing.text.BadLocationException if the given position does not represent a
     *                              valid location in the associated document
     *
     * @see javax.swing.text.View#modelToView
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException
    {
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        if ((pos >= p0) && (pos <= p1))
        {
            Rectangle r = a.getBounds();
            if (pos == p1)
            {
                r.x += r.width;
            }
            r.width = 0;
            return r;
        }
        return null;
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param a the allocated region to render into
     * @param bias position bias.
     *
     * @return the location within the model that best represents the
     *         given point of view
     * @see javax.swing.text.View#viewToModel
     */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias)
    {
        Rectangle alloc = (Rectangle)a;
        if (x < alloc.x + alloc.width)
        {
            bias[0] = Position.Bias.Forward;
            return getStartOffset();
        }
        bias[0] = Position.Bias.Backward;
        return getEndOffset();
    }

    /**
     * Sets the size of the view.  This should cause
     * layout of the view if it has any layout duties.
     *
     * @param aWidth  the aWidth >= 0
     * @param aHeight the aHeight >= 0
     */
    public void setSize(float aWidth, float aHeight)
    {
        sync();

        if (getImage() == null)
        {
            View view = getAltView();

            if (view != null)
            {
                view.setSize(Math.max(0f, aWidth - (float)(DEFAULT_WIDTH + leftInset + rightInset)),
                        Math.max(0f, aHeight - (float)(topInset + bottomInset)));
            }
        }
    }

    /**
     * Returns true if this image within a link?
     * @return <code>TRUE</code> if image is within a link.
     */
    private boolean isLink()
    {
        return ((state & LINK_FLAG) == LINK_FLAG);
    }

    /**
     * Returns true if the passed in anImage has a non-zero width and height.
     * @param anImage image.
     * @return <code>TRUE</code> if has non-zero width and height.
     */
    private boolean hasPixels(Image anImage)
    {
        return anImage != null &&
            (anImage.getHeight(imageObserver) > 0) &&
            (anImage.getWidth(imageObserver) > 0);
    }

    /**
     * Returns the preferred span of the View used to display the alt text,
     * or 0 if the view does not exist.
     *
     * @param axis axis.
     *
     * @return preferred span.
     */
    private float getPreferredSpanFromAltView(int axis)
    {
        if (getImage() == null)
        {
            View view = getAltView();

            if (view != null)
            {
                return view.getPreferredSpan(axis);
            }
        }
        return 0f;
    }

    /**
     * Request that this view be repainted.
     * Assumes the view is still at its last-drawn location.
     *
     * @param delay maximum delay before repaint.
     */
    private void repaint(long delay)
    {
        if (container != null && fBounds != null)
        {
            container.repaint(delay, fBounds.x, fBounds.y, fBounds.width,
                    fBounds.height);
        }
    }

    /**
     * Convenience method for getting an integer attribute from the elements
     * AttributeSet. Automatically strips off 'px' suffix of attribute value,
     * if present.
     *
     * @param name attribute name.
     * @param deflt default value.
     *
     * @return value.
     */
    protected int getIntAttr(Object name, int deflt)
    {
        AttributeSet attrs = getElement().getAttributes();
        if (attrs.isDefined(name))
        {
            int i;
            String val = attrs.getAttribute(name).toString();
            if (val == null)
            {
                i = deflt;
            } else
            {
                try
                {
                    val = val.endsWith("px") ? val.substring(0, val.length() - 2) : val;
                    i = Math.max(0, Integer.parseInt(val));
                } catch (NumberFormatException x)
                {
                    i = deflt;
                }
            }
            return i;
        } else
            return deflt;
    }

    /**
     * Makes sure the necessary properties and image is loaded.
     */
    private void sync()
    {
        int s = state;
        if ((s & RELOAD_IMAGE_FLAG) != 0)
        {
            refreshImage();
        }
        s = state;
        if ((s & RELOAD_FLAG) != 0)
        {
            synchronized (this)
            {
                state = (state | RELOAD_FLAG) ^ RELOAD_FLAG;
            }
            setPropertiesFromAttributes();
        }
    }

    /**
     * Loads the image and updates the size accordingly. This should be
     * invoked instead of invoking <code>loadImage</code> or
     * <code>updateImageSize</code> directly.
     */
    private void refreshImage()
    {
        synchronized (this)
        {
            // clear out width/height/realoadimage flag and set loading flag
            state = (state | LOADING_FLAG | RELOAD_IMAGE_FLAG | WIDTH_FLAG |
                    HEIGHT_FLAG) ^ (WIDTH_FLAG | HEIGHT_FLAG |
                    RELOAD_IMAGE_FLAG);
            image = null;
            width = 0;
            height = 0;
        }

        try
        {
            // Load the image
            loadImage();

            // And update the size params
            updateImageSize();
        } finally
        {
            synchronized (this)
            {
                // Clear out state in case someone threw an exception.
                state = (state | LOADING_FLAG) ^ LOADING_FLAG;
            }
        }
    }

    /**
     * Loads the image from the URL <code>getImageURL</code>. This should
     * only be invoked from <code>refreshImage</code>.
     */
    private void loadImage()
    {
        URL src = getImageURL();
        Image newImage = null;
        if (src != null)
        {
            Dictionary cache = (Dictionary)getDocument().
                    getProperty(IMAGE_CACHE_PROPERTY);
            if (cache != null)
            {
                newImage = (Image)cache.get(src);
            } else
            {
                if (System.getProperty(Strings.error("img.no.images")) == null)
                {
                    newImage = ImageFetcher.load(src);
                }

//                if (newImage != null && getLoadsSynchronously())
//                {
//                    // Force the image to be loaded by using an ImageIcon.
//                    ImageIcon ii = new ImageIcon();
//                    ii.setImage(newImage);
//                }
            }
        }
        image = newImage;
    }

    /**
     * Recreates and reloads the image.  This should
     * only be invoked from <code>refreshImage</code>.
     */
    private void updateImageSize()
    {
        int newWidth;
        int newHeight;
        int newState = 0;
        Image newImage = getImage();

        if (newImage != null)
        {
            // Get the width/height and set the state ivar before calling
            // anything that might cause the image to be loaded, and thus the
            // ImageHandler to be called.
            newWidth = getIntAttr(HTML.Attribute.WIDTH, -1);
            if (newWidth > 0)
            {
                newState |= WIDTH_FLAG;
            }
            newHeight = getIntAttr(HTML.Attribute.HEIGHT, -1);
            if (newHeight > 0)
            {
                newState |= HEIGHT_FLAG;
            }

            if (newWidth <= 0)
            {
                newWidth = newImage.getWidth(imageObserver);
                if (newWidth <= 0)
                {
                    newWidth = DEFAULT_WIDTH;
                }
            }

            if (newHeight <= 0)
            {
                newHeight = newImage.getHeight(imageObserver);
                if (newHeight <= 0)
                {
                    newHeight = DEFAULT_HEIGHT;
                }
            }

            // Make sure the image starts loading:
            if ((newState & (WIDTH_FLAG | HEIGHT_FLAG)) != 0)
            {
                Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth,
                        newHeight,
                        imageObserver);
            } else
            {
                Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1,
                        imageObserver);
            }

            boolean createText = false;
            synchronized (this)
            {
                // If imageloading failed, other thread may have called
                // ImageLoader which will null out image, hence we check
                // for it.
                if (image != null)
                {
                    if ((newState & WIDTH_FLAG) == WIDTH_FLAG || width == 0)
                    {
                        width = newWidth;
                    }
                    if ((newState & HEIGHT_FLAG) == HEIGHT_FLAG ||
                            height == 0)
                    {
                        height = newHeight;
                    }
                } else
                {
                    createText = true;
                    if ((newState & WIDTH_FLAG) == WIDTH_FLAG)
                    {
                        width = newWidth;
                    }
                    if ((newState & HEIGHT_FLAG) == HEIGHT_FLAG)
                    {
                        height = newHeight;
                    }
                }
                state = state | newState;
                state = (state | LOADING_FLAG) ^ LOADING_FLAG;
            }
            if (createText)
            {
                // Only reset if this thread determined image is null
                updateAltTextView();
            }
        } else
        {
            width = DEFAULT_HEIGHT;
            height = DEFAULT_HEIGHT;
            updateBorderForNoImage();
            updateAltTextView();
        }
    }

    /**
     * Updates the view representing the alt text.
     */
    private void updateAltTextView()
    {
        String text = getAltText();

        if (text != null)
        {
            ImageLabelView newView;

            newView = new ImageLabelView(getElement(), text);
            synchronized (this)
            {
                altView = newView;
            }
        }
    }

    /**
     * Returns the view to use for alternate text. This may be null.
     * @return alternative view.
     */
    private View getAltView()
    {
        View view;

        synchronized (this)
        {
            view = altView;
        }
        if (view != null && view.getParent() == null)
        {
            view.setParent(getParent());
        }
        return view;
    }

    /**
     * Invokes <code>preferenceChanged</code> on the event displatching
     * thread.
     */
    private void safePreferenceChanged()
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            Document doc = getDocument();
            try
            {
                if (doc instanceof AbstractDocument)
                {
                    ((AbstractDocument)doc).readLock();
                }
                preferenceChanged(null, true, true);
            } finally
            {
                if (doc instanceof AbstractDocument)
                {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    safePreferenceChanged();
                }
            });
        }
    }

    /**
     * Invoked if no image is found, in which case a default border is
     * used if one isn't specified.
     */
    private void updateBorderForNoImage()
    {
        if (borderSize == 0)
        {
            borderSize = 1;
            leftInset += borderSize;
            rightInset += borderSize;
            bottomInset += borderSize;
            topInset += borderSize;
        }
    }


    /**
     * ImageHandler implements the ImageObserver to correctly update the
     * display as new parts of the image become available.
     */
    private class ImageHandler implements ImageObserver
    {
        // This can come on any thread. If we are in the process of reloading
        // the image and determining our state (loading == true) we don't fire
        // preference changed, or repaint, we just reset the fWidth/fHeight as
        // necessary and return. This is ok as we know when loading finishes
        // it will pick up the new height/width, if necessary.
        public boolean imageUpdate(Image img, int flags, int x, int y,
                                   int newWidth, int newHeight)
        {
            if (image == null || image != img)
            {
                return false;
            }

            // Bail out if there was an error:
            if ((flags & (ABORT | ERROR)) != 0)
            {
                repaint(0);
                synchronized (CustomImageView.this)
                {
                    if (image == img)
                    {
                        // Be sure image hasn't changed since we don't
                        // initialy synchronize
                        image = null;

                        if ((state & WIDTH_FLAG) != WIDTH_FLAG) width = DEFAULT_WIDTH;
                        if ((state & HEIGHT_FLAG) != HEIGHT_FLAG) height = DEFAULT_HEIGHT;

                        // No image, use a default border.
                        updateBorderForNoImage();
                    }
                    if ((state & LOADING_FLAG) == LOADING_FLAG)
                    {
                        // No need to resize or repaint, still in the process
                        // of loading.
                        return false;
                    }
                }

                updateAltTextView();
                safePreferenceChanged();
                return false;
            }

            // Resize image if necessary:
            short changed = 0;
            if ((flags & ImageObserver.HEIGHT) != 0 &&
                !getElement().getAttributes().isDefined(HTML.Attribute.HEIGHT))
            {
                changed |= ImageObserver.HEIGHT;
            }

            if ((flags & ImageObserver.WIDTH) != 0 &&
                !getElement().getAttributes().isDefined(HTML.Attribute.WIDTH))
            {
                changed |= ImageObserver.WIDTH;
            }

            synchronized (CustomImageView.this)
            {
                if (image != img) return false;

                if ((changed & ImageObserver.WIDTH) != 0 &&
                    (state & WIDTH_FLAG) == 0)
                {
                    width = newWidth;
                }

                if ((changed & ImageObserver.HEIGHT) != 0 &&
                    (state & HEIGHT_FLAG) == 0)
                {
                    height = newHeight;
                }

                if ((state & LOADING_FLAG) == LOADING_FLAG)
                {
                    // No need to resize or repaint, still in the process of
                    // loading.
                    return true;
                }
            }

            if (changed != 0)
            {
                // May need to resize myself, asynchronously:
                safePreferenceChanged();
                return true;
            }

            // Repaint when done or when new pixels arrive:
            if ((flags & (FRAMEBITS | ALLBITS)) != 0)
            {
                // Make a static copy of image
                image = prepareStaticCopy(image);

                repaint(0);
            } else if ((flags & SOMEBITS) != 0 && sIsInc)
            {
                repaint(sIncRate);
            }

            return (flags & (ALLBITS | FRAMEBITS)) == 0;
        }

        /**
         * Prepares static copy of image to avoid continuous loading requests on repaint().
         *
         * @param anImage   source image.
         *
         * @return copy or original image.
         */
        private Image prepareStaticCopy(Image anImage)
        {
            Image result = anImage;
            int width = anImage.getWidth(null);
            int height = anImage.getHeight(null);

            if (width != -1 && height != -1)
            {
/*
                BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics g = copy.createGraphics();
                g.drawImage(anImage, 0, 0, null);

                result = copy;
*/
            }

            return result;
        }
    }


    /**
     * ImageLabelView is used if the image can't be loaded, and
     * the attribute specified an alt attribute. It overriden a handle of
     * methods as the text is hardcoded and does not come from the document.
     */
    private static class ImageLabelView extends InlineView
    {
        private Segment segment;
        private Color fg;

        ImageLabelView(Element e, String text)
        {
            super(e);
            reset(text);
        }

        public void reset(String text)
        {
            segment = new Segment(text.toCharArray(), 0, text.length());
        }

        public void paint(Graphics g, Shape a)
        {
            // Don't use supers paint, otherwise selection will be wrong
            // as our start/end offsets are fake.
            GlyphPainter painter = getGlyphPainter();

            if (painter != null)
            {
                g.setColor(getForeground());
                painter.paint(this, g, a, getStartOffset(), getEndOffset());
            }
        }

        public Segment getText(int p0, int p1)
        {
            if (p0 < 0 || p1 > segment.array.length)
            {
                throw new RuntimeException(Strings.error("img.imageview.stale.view"));
            }
            segment.offset = p0;
            segment.count = p1 - p0;
            return segment;
        }

        public int getStartOffset()
        {
            return 0;
        }

        public int getEndOffset()
        {
            return segment.array.length;
        }

        public View breakView(int axis, int p0, float pos, float len)
        {
            // Don't allow a break
            return this;
        }

        public Color getForeground()
        {
            View parent;
            if (fg == null && (parent = getParent()) != null)
            {
                Document doc = getDocument();
                AttributeSet attrs = parent.getAttributes();

                if (attrs != null && (doc instanceof StyledDocument))
                {
                    fg = ((StyledDocument)doc).getForeground(attrs);
                }
            }
            return fg;
        }
    }
}