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
// $Id: FloatableImageView.java,v 1.10 2008/04/09 06:07:11 spyromus Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.View;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import java.awt.*;

/**
 * Adds support for left/right floated images. These images do not appear
 * in the flow (i.e. they are invisible), but are painted by the enclosing
 * <code>FloatParagraphView</code>.
 *
 * @see FloatParagraphView
 */
public class FloatableImageView extends CustomImageView
{
    /**
     * Inline layout type constant.
     */
    public static final int INLINE = 0;
    /**
     * Left-aligned layout type constant.
     */
    public static final int FLOAT_LEFT = 1;
    /**
     * Right-aligned layout type constant.
     */
    public static final int FLOAT_RIGHT = 2;

    /**
     * The internal alignment state for this image (one of the static constants, e.g.
     * <code>INLINE</code>).
     */
    private int hAlign;

    /**
     * The horizontal distance from the margin of the floated image.
     * Ignored for non-floating images.
     */
    protected int hFloatPos = 0;

    /**
     * The vertical distance of the floated image from its assigned margin.
     * Ignored for non-floating images.
     */
    protected int vFloatPos = 0;

    /**
     * Creates a new view that represents an IMG element.
     *
     * @param elem the element to create a view for
     */
    public FloatableImageView(Element elem)
    {
        super(elem);

        // Add default 3px margins if element is floated and has no HSPACE/VSPACE
        MutableAttributeSet attr = (MutableAttributeSet)elem.getAttributes();
        Object alignment = attr.getAttribute(HTML.Attribute.ALIGN);

        AttributeSet cssAttr = getAttributes();
        Object cssFloat = cssAttr.getAttribute(CSS.Attribute.FLOAT);

        if ("left".equals(alignment) || "right".equals(alignment) ||
                "left".equals(cssFloat) || "right".equals(cssFloat))
        {
            if (getIntAttr(HTML.Attribute.HSPACE, -1) < 0)
            {
                attr.addAttribute(HTML.Attribute.HSPACE, "3");
            }
            if (getIntAttr(HTML.Attribute.VSPACE, -1) < 0)
            {
                attr.addAttribute(HTML.Attribute.VSPACE, "3");
            }
        }
    }


    /**
     * Update any cached values that come from attributes. Updates <code>layoutType</code>
     * property from HTML <code>align</code> attribute and CSS <code>float</code> property.
     */
    protected void setPropertiesFromAttributes()
    {
        super.setPropertiesFromAttributes();

        AttributeSet attr = getElement().getAttributes();
        Object alignment = attr.getAttribute(HTML.Attribute.ALIGN);

        AttributeSet cssAttr = getAttributes();
        Object cssFloat = cssAttr.getAttribute(CSS.Attribute.FLOAT);

        hAlign = INLINE;
        if (alignment != null)
        {
            alignment = alignment.toString();
            if ("left".equals(alignment))
            {
                hAlign = FLOAT_LEFT;
            } else if ("right".equals(alignment))
            {
                hAlign = FLOAT_RIGHT;
            }
        } else if (cssFloat != null)
        {
            cssFloat = cssFloat.toString();
            if ("left".equals(cssFloat))
            {
                hAlign = FLOAT_LEFT;
            } else if ("right".equals(cssFloat))
            {
                hAlign = FLOAT_RIGHT;
            }
        }

        // Apply margin rules
        rightInset  += getIntAttr(CSS.Attribute.MARGIN_RIGHT, 0);
        leftInset   += getIntAttr(CSS.Attribute.MARGIN_LEFT, 0);
        bottomInset += getIntAttr(CSS.Attribute.MARGIN_BOTTOM, 0);
        topInset    += getIntAttr(CSS.Attribute.MARGIN_TOP, 0);
    }

    /**
     * Convenience method for getting an integer attribute from the element's
     * <code>AttributeSet</code>.
     *
     * @param name The <code>HTML.Attribute</code> constant to fetch.
     * @param deflt The default value to return.
     *
     * @return The integer value of the HTML attribute <code>name</code>, or <code>deflt</code>
     * if attribute value could not be converted to into, or if it has no value.
     */
    private int getIntAttr(HTML.Attribute name, int deflt)
    {
        AttributeSet attr = getElement().getAttributes();
        if (attr.isDefined(name))  // does not check parents!
        {
            int i;
            String val = attr.getAttribute(name).toString();
            if (val == null)
            {
                i = deflt;
            } else
            {
                try
                {
                    i = Math.max(0, Integer.parseInt(val));
                } catch (NumberFormatException x)
                {
                    i = deflt;
                }
            }
            return i;
        } else return deflt;
    }

    /**
     * Returns the layout type for the image.
     * @return <code>FLOAT_LEFT</code>, <code>FLOAT_RIGHT</code>, or <code>INLINE</code>
     */
    public int getLayoutType()
    {
        return hAlign;
    }

    /**
     * Is this a floating (left/right-aligned) image?
     * @return true if floating
     */
    public boolean isFloat()
    {
        return getLayoutType() != INLINE;
    }

    /**
     * Paints this <code>View</code>.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     */
    public void paint(Graphics g, Shape a)
    {
        paint(g, a, false);
    }

    /**
     * Paints this <code>View</code>. If image is floated, the image will not
     * actually be painted unless <code>paintFloat</code> is true.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     * @param paintFloat even if the image is floated, actually paint it
     */
    public void paint(Graphics g, Shape a, boolean paintFloat)
    {
        if (isFloat() && !paintFloat) return;
        super.paint(g, a);
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either <code>X_AXIS</code> or <code>Y_AXIS</code>
     * @return   the span the view would like to be rendered into;
     *           typically the view is told to render into the span
     *           that is returned, although there is no guarantee;
     *           the parent may choose to resize or break the view
     */
    public float getPreferredSpan(int axis)
    {
        return getPreferredSpan(axis, false);
    }

    /**
     * Gets preferred size along a given axis for the image. If image
     * is floated, the preferred span will be zero unless <code>paintFloat</code>
     * is true or the image is at the start of its row (in which case it occupies
     * space on this row).
     *
     * @param axis may be either <code>X_AXIS</code> or <code>Y_AXIS</code>
     * @param paintFloat If true and image is a floater, return the
     *                   dimension of the image inline (zero). If false,
     *                   return the actual image dimension.
     * @return   the span the view would like to be rendered into;
     *           typically the view is told to render into the span
     *           that is returned, although there is no guarantee;
     *           the parent may choose to resize or break the view.
     */
    public float getPreferredSpan(int axis, boolean paintFloat)
    {
        View v;

        if (!isFloat() || paintFloat) return super.getPreferredSpan(axis);
        if (axis == View.Y_AXIS) return 0;

        View lv = getParent(); // The logical view
        View fv = lv == null ? null : lv.getParent(); // The flow (paragraph) view

        if (fv != null && fv instanceof FloatParagraphView)
        {
            int rowIndex = ((FloatParagraphView)fv).getViewIndexAtPosition(getStartOffset());
            View row = rowIndex < 0 ? null : fv.getView(rowIndex);

            // If this view has already been placed in a row
            if (row != null)
            {
                // Find out if this view is among any floated images at the start the row
                for (int i = 0; true; i++)
                {
                    v = row.getView(i);
                    if (v == this || !(v instanceof FloatableImageView) ||
                            !((FloatableImageView)v).isFloat())
                    {
                        break;
                    }
                }
                if (v != this)
                {
                    // This image won't appear in this row, so its span for this row is 0
                    return 0;
                }
            } else if (fv.getViewCount() > 0)
            // If this view is looking to be placed in a row
            {
                // Find the last row (the one currently being layed out)
                row = fv.getView(fv.getViewCount() - 1);

                // Find out if the row contains anything other than floated images so far
                int n = row.getViewCount();
                for (int i = 0; i < n; i++)
                {
                    v = row.getView(i);
                    if (!(v instanceof FloatableImageView) || !((FloatableImageView)v).isFloat())
                    {
                        // This image won't appear in this row, so its span for this row is 0
                        return 0;
                    }
                }
            }
        }
        // This image will appear in this row
        return super.getPreferredSpan(axis);
    }
}
