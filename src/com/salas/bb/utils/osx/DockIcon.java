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
// $Id $
//

package com.salas.bb.utils.osx;

import com.jgoodies.uif.util.SystemUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dock Icon for Mac OS X.
 */
public class DockIcon
{
    private static final Logger LOG = Logger.getLogger(DockIcon.class.getName());

    private static final String MSG_ERR_FAILED_INIT = "Failed to initialize Dock Icon manager.";
    private static final String MSG_ERR_FAILED_SET_ICON = "Failed to set Dock Icon.";

    // Global constants
    private static final int CENTER_ALIGNMENT = 2;
    private static final int NS_COMPOSITE_SOURCE_OVER = 2;

    // Configuration
    private static final int FONT_SIZE = 24;
    private static final int BADGE_RADIUS = 20;

    // Initialization status
    private static boolean initialized = false;
    private static boolean initializationFailed;

    // Global objects
    private static Object nsApplication;
    private static Object nsIcon;

    // Current counter
    private static int current = 0;

    // Cocoa classes
    private static Class classNSBezierPath;
    private static Class classNSMutableParagraphStyle;
    private static Class classNSData;

    // Cocoa class constructors
    private static Constructor nsAttributedString_c_s_a;
    private static Constructor nsData_c;
    private static Constructor nsDictionary_c_k_v;
    private static Constructor nsImage_c_data;
    private static Constructor nsImage_c_size;
    private static Constructor nsPoint_c_x_y;
    private static Constructor nsRect_c_x_y_w_h;

    // Cocoa class methods
    private static Method nsApplication_setApplicationIconImage;
    private static Method nsBezierPath_fill;
    private static Method nsBezierPath_moveToPoint;
    private static Method nsBezierPath_appendBezierPathWithArcFromPoint;
    private static Method nsBezierPath_lineToPoint;
    private static Method nsColor_setFill;
    private static Method nsColor_redColor;
    private static Method nsColor_whiteColor;
    private static Method nsFont_controlContentFontOfSize;
    private static Method nsGraphics_drawAttributedString;
    private static Method nsGraphics_sizeOfAttributedString;
    private static Method nsImage_size;
    private static Method nsImage_lockFocus;
    private static Method nsImage_unlockFocus;
    private static Method nsImage_compositeToPoint;
    private static Method nsMutableParagraphStyle_setAlignment;
    private static Method nsSize_height;
    private static Method nsSize_width;

    // We keep these references to avoid them being collected
    // see http://wiki.java.net/bin/view/Mac/Jobjc_lookupObjCObject
    private static Object biAttrs;
    private static Object biColorWhite;
    private static Object biFont;
    private static Object biParaStyle;

    /**
     * Performs initialization.
     */
    private static void init()
    {
        initializationFailed = true;

        // Save current icon to paint badges over it
        try
        {
            // Classes
            Class classNSApplication = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSApplication");
            classNSBezierPath = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSBezierPath");
            Class classNSImage = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSImage");
            Class classNSColor = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSColor");
            Class classNSFont = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSFont");
            Class classNSGraphics = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSGraphics");
            classNSMutableParagraphStyle = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSMutableParagraphStyle");
            Class classNSSize = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSSize");
            Class classNSPoint = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSPoint");
            Class classNSRect = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSRect");
            Class classNSDictionary = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSDictionary");
            Class classNSAttributedString = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSAttributedString");

            classNSData = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSData");

            // Methods
            nsApplication_setApplicationIconImage = classNSApplication.getMethod(
                "setApplicationIconImage", new Class[] { classNSImage });
            nsBezierPath_fill = classNSBezierPath.getMethod("fill", new Class[0]);
            nsBezierPath_moveToPoint = classNSBezierPath.getMethod("moveToPoint", new Class[] { classNSPoint });
            nsBezierPath_lineToPoint = classNSBezierPath.getMethod("lineToPoint", new Class[] { classNSPoint });
            nsBezierPath_appendBezierPathWithArcFromPoint = classNSBezierPath.getMethod("appendBezierPathWithArcFromPoint",
                new Class[] { classNSPoint, classNSPoint, Float.TYPE });
            nsColor_setFill = classNSColor.getMethod("setFill", new Class[0]);
            nsColor_redColor = classNSColor.getMethod("redColor", new Class[0]);
            nsColor_whiteColor = classNSColor.getMethod("whiteColor", new Class[0]);
            nsFont_controlContentFontOfSize = classNSFont.getMethod("controlContentFontOfSize",
                new Class[] { Float.TYPE });
            nsGraphics_drawAttributedString = classNSGraphics.getMethod("drawAttributedString",
                new Class[] { classNSAttributedString, classNSRect });
            nsGraphics_sizeOfAttributedString = classNSGraphics.getMethod("sizeOfAttributedString",
                new Class[] { classNSAttributedString });
            Method nsImage_imageNamed = classNSImage.getMethod("imageNamed", new Class[] { String.class });
            nsImage_size = classNSImage.getMethod("size", new Class[0]);
            nsImage_lockFocus = classNSImage.getMethod("lockFocus", new Class[0]);
            nsImage_unlockFocus = classNSImage.getMethod("unlockFocus", new Class[0]);
            nsImage_compositeToPoint = classNSImage.getMethod("compositeToPoint",
                new Class[] { classNSPoint, Integer.TYPE });
            nsMutableParagraphStyle_setAlignment = classNSMutableParagraphStyle.getMethod("setAlignment",
                new Class[] { Integer.TYPE });
            nsSize_height = classNSSize.getMethod("height", new Class[0]);
            nsSize_width = classNSSize.getMethod("width", new Class[0]);

            // Constructors
            nsAttributedString_c_s_a = classNSAttributedString.getConstructor(new Class[] {
                String.class, classNSDictionary });
            nsData_c = classNSData.getConstructor(new Class[] { byte[].class });
            nsDictionary_c_k_v = classNSDictionary.getConstructor(new Class[] { Object[].class, Object[].class });
            nsImage_c_data = classNSImage.getConstructor(new Class[] { classNSData });
            nsImage_c_size = classNSImage.getConstructor(new Class[] { classNSSize });
            nsPoint_c_x_y = classNSPoint.getConstructor(new Class[] { Float.TYPE, Float.TYPE });
            nsRect_c_x_y_w_h = classNSRect.getConstructor(new Class[] {
                Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE });

            // Get application instance
            Method nsApplication_sharedApplication = classNSApplication.getMethod("sharedApplication", new Class[0]);
            nsApplication = nsApplication_sharedApplication.invoke(null, new Object[0]);

            // Get main icon image
            nsIcon = nsImage_imageNamed.invoke(null, new Object[] { "NSApplicationIcon" });

            // Construct badge inscription attributes object
            createBadgeInscriptionAttrs();

            initializationFailed = false;
        } catch (Exception e)
        {
            LOG.log(Level.WARNING, MSG_ERR_FAILED_INIT, e);
        } finally
        {
            initialized = true;
        }
    }

    /**
     * Sets application icon from the name of the resource.
     *
     * @param resource resource.
     */
    public static void setApplicationIcon(String resource)
    {
        if (!SystemUtils.IS_OS_MAC || initializationFailed) return;

        // Initialize if not initialized yet
        if (!initialized) init();

        // Prepares image
        if (resource != null)
        {
            try {
                byte[] resourceBytes = OSXSupport.loadResourceBytes(resource);
                if (resourceBytes != null)
                {
                    Object data = nsData_c.newInstance(new Object[]{ resourceBytes});
                    Object image = nsImage_c_data.newInstance(new Object[]{ data });

                    if (image != null)
                    {
                        nsIcon = image;
                        setApplicationIcon(image);
                    }
                }
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_ERR_FAILED_SET_ICON, e);
            }
        }

    }

    /**
     * Sets an icon in the dock to this image.
     *
     * @param nsImage image.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void setApplicationIcon(Object nsImage)
            throws IllegalAccessException, InvocationTargetException
    {
        nsApplication_setApplicationIconImage.invoke(nsApplication, new Object[] { nsImage });
    }

    /**
     * Shows some counter in the icon badge.
     *
     * @param cnt counter to show. If it's <= 0, the badge is not visible.
     */
    public static synchronized void setBadgeCounter(int cnt)
    {
        if (!SystemUtils.IS_OS_MAC || initializationFailed) return;

        // If there's nothing to change, return
        if (current == cnt || (current == 0 && cnt < 0)) return;

        // Initialize if not initialized yet
        if (!initialized) init();

        current = cnt;
        try
        {
            renderAndShow();
        } catch (Exception e)
        {
            initializationFailed = true;
            LOG.log(Level.WARNING, "Failed to paint the icon", e);
        }
    }

    /**
     * Renders new badge and shows it.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static void renderAndShow()
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        Object newImage = nsIcon;

        if (current > 0)
        {
            // Create new image of the icon size
            Object size = getSize(nsIcon);
            newImage = createNSImage(size);

            // Create text inscription
            Object asText = createBadgeInscription();
            Object asTextSize = sizeOfAttributedString(asText);

            // Calculate badge size -----------------------------------------------
            float r = BADGE_RADIUS;
            float d = r * 2;

            // middle part
            float textWidth = getWidth(asTextSize);
            float widthThreshold = d - FONT_SIZE;
            float middlePart = Math.max(0, textWidth - widthThreshold);

            float badgeW = d + middlePart;
            float badgeH = d;

            float iconH = getHeight(size);
            float iconW = getWidth(size);

            // offsets
            float xo = iconW - badgeW;
            float yo = iconH - badgeH;

            // Start rendering ----------------------------------------------------
            lockFocus(newImage);

            // Create badge pad
            Object circle = classNSBezierPath.newInstance();
            moveToPoint(circle, xo + r, yo);
            appendBezierPathWithArcFromPoint(circle, point(xo, yo), point(xo, yo + r), r);
            appendBezierPathWithArcFromPoint(circle, point(xo, yo + d), point(xo + r, yo + d), r);
            if (middlePart > 0) lineToPoint(circle, xo + r + middlePart, yo + d);
            appendBezierPathWithArcFromPoint(circle, point(xo + badgeW, yo + d), point(xo + badgeW, yo + r), r);
            appendBezierPathWithArcFromPoint(circle, point(xo + badgeW, yo), point(xo + r + middlePart, yo), r);
            if (middlePart > 0) lineToPoint(circle, xo + r, yo);

            // pad
            compositeToPoint(nsIcon, point(0, 0), new Integer(NS_COMPOSITE_SOURCE_OVER));
            setRedColorFill();
            fillBezierPath(circle);

            // inscription
            Object rect = rect(xo, yo + (badgeH - FONT_SIZE) / 1.5f, badgeW, FONT_SIZE);
            nsGraphics_drawAttributedString.invoke(null, new Object[] { asText, rect });
            unlockFocus(newImage);
        }

        // Set the image ------------------------------------------------------
        setApplicationIcon(newImage);
    }

    /**
     * Draws line from current point to some other point.
     *
     * @param nsBezierPath path.
     * @param x x coordinate.
     * @param y y coordinate.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static void lineToPoint(Object nsBezierPath, float x, float y)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        nsBezierPath_lineToPoint.invoke(nsBezierPath, new Object[] { point(x, y) });
    }

    /**
     * Draws arc from current point to from / to point pair.
     *
     * @param nsBezierPath  path.
     * @param nsPointFrom   from-point.
     * @param nsPointTo     to-point.
     * @param radius        radius.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void appendBezierPathWithArcFromPoint(Object nsBezierPath, Object nsPointFrom, Object nsPointTo,
                                                         float radius)
        throws IllegalAccessException, InvocationTargetException
    {
        nsBezierPath_appendBezierPathWithArcFromPoint.invoke(nsBezierPath, new Object[] {
            nsPointFrom, nsPointTo, new Float(radius) });
    }

    /**
     * Gets the size of the string.
     *
     * @param nsAttributedString string.
     *
     * @return NSSize object.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static Object sizeOfAttributedString(Object nsAttributedString)
        throws IllegalAccessException, InvocationTargetException
    {
        return nsGraphics_sizeOfAttributedString.invoke(null, new Object[] { nsAttributedString });
    }

    /**
     * Creates inscription for the badge.
     *
     * @return NSAttributedString object.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static Object createBadgeInscription()
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return nsAttributedString_c_s_a.newInstance(new Object[] { Integer.toString(current), biAttrs });
    }

    /**
     * Creates badge inscription attributes.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static void createBadgeInscriptionAttrs()
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        biColorWhite = nsColor_whiteColor.invoke(null, new Object[0]);
        biFont = nsFont_controlContentFontOfSize.invoke(null, new Object[] { new Float(FONT_SIZE) });
        biParaStyle = classNSMutableParagraphStyle.newInstance();
        nsMutableParagraphStyle_setAlignment.invoke(biParaStyle, new Object[] { new Integer(CENTER_ALIGNMENT) });

        Object[] values = new Object[] { biColorWhite, biFont, biParaStyle };
        Object[] keys = new Object[] { "NSColor", "NSFont", "NSParagraphStyle" };
        biAttrs = nsDictionary_c_k_v.newInstance(new Object[] { values, keys });
    }

    /**
     * Creates NSPoint object.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     *
     * @return object.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static Object point(float x, float y)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return nsPoint_c_x_y.newInstance(new Object[] { new Float(x), new Float(y) });
    }

    /**
     * Moves current position to the point.
     *
     * @param nsBezierPath  path.
     * @param x x coordinate.
     * @param y y coordinate.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static void moveToPoint(Object nsBezierPath, float x, float y)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        nsBezierPath_moveToPoint.invoke(nsBezierPath, new Object[] { point(x, y) });
    }

    /**
     * Fills bezier path with color.
     *
     * @param nsBezierPath path.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void fillBezierPath(Object nsBezierPath)
        throws IllegalAccessException, InvocationTargetException
    {
        nsBezierPath_fill.invoke(nsBezierPath, new Object[0]);
    }

    /**
     * Sets current color to red.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void setRedColorFill()
        throws IllegalAccessException, InvocationTargetException
    {
        Object redColor = nsColor_redColor.invoke(null, new Object[0]);
        nsColor_setFill.invoke(redColor, new Object[0]);
    }

    /**
     * Creates NSRect object.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param w width.
     * @param h height.
     *
     * @return NSRect.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static Object rect(float x, float y, float w, float h)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return nsRect_c_x_y_w_h.newInstance(new Object[] { new Float(x), new Float(y), new Float(w), new Float(h) });
    }

    /**
     * Composites to the point.
     *
     * @param nsImage NSImage.
     * @param nsPoint point.
     * @param intMode composite mode.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void compositeToPoint(Object nsImage, Object nsPoint, Object intMode)
        throws IllegalAccessException, InvocationTargetException
    {
        nsImage_compositeToPoint.invoke(nsImage, new Object[] { nsPoint, intMode });
    }

    /**
     * Returns the height component of the NSSize object.
     *
     * @param nsSize size.
     *
     * @return height.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static float getHeight(Object nsSize)
        throws IllegalAccessException, InvocationTargetException
    {
        return ((Float)nsSize_height.invoke(nsSize, new Object[0])).floatValue();
    }

    /**
     * Returns the width component of the NSSize object.
     *
     * @param nsSize size.
     *
     * @return width.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static float getWidth(Object nsSize)
        throws IllegalAccessException, InvocationTargetException
    {
        return ((Float)nsSize_width.invoke(nsSize, new Object[0])).floatValue();
    }

    /**
     * Locks painting focus.
     *
     * @param nsImage image.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void lockFocus(Object nsImage)
        throws IllegalAccessException, InvocationTargetException
    {
        nsImage_lockFocus.invoke(nsImage, new Object[0]);
    }

    /**
     * Unlocks painting focus.
     *
     * @param nsImage image.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static void unlockFocus(Object nsImage)
        throws IllegalAccessException, InvocationTargetException
    {
        nsImage_unlockFocus.invoke(nsImage, new Object[0]);
    }

    /**
     * Creates NSImage object.
     *
     * @param nsSize size.
     *
     * @return object.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     * @throws InstantiationException       instantiation exception.
     */
    private static Object createNSImage(Object nsSize)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return nsImage_c_size.newInstance(new Object[] { nsSize });
    }

    /**
     * Returns the size of an NSImage object.
     *
     * @param nsImage image.
     *
     * @return size.
     *
     * @throws IllegalAccessException       illegal access.
     * @throws InvocationTargetException    invocation exception.
     */
    private static Object getSize(Object nsImage)
        throws IllegalAccessException, InvocationTargetException
    {
        return nsImage_size.invoke(nsImage, new Object[0]);
    }
}
