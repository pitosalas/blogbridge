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
// $Id: FloatParagraphView.java,v 1.14 2006/06/08 04:44:38 kyank Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.*;
import javax.swing.text.html.ParagraphView;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Customized version of paragraph view. Enhanced to detect and paint left/right-aligned
 * (floated) images in the margins.
 *
 * Author of original class taken as a base from Sun SDK is Timothy Prinzing.
 */
public class FloatParagraphView extends UnjustifiedParagraphView
{
    /**
     * Flag to indicate that this paragraph contains floated images that
     * protrude below its bottom edge.
     */
    private boolean passForwardFloats = false;

    private FloatMarginManager fmm = new FloatMarginManager();

    /**
     * The set of <code>FloatableImageView</code>s that are positioned in the margin of this
     * paragraph. These are painted whenever this paragraph is painted.
     */
    private Set intrudingFloats = new HashSet();

    private int justification;
    private float lineSpacing;

    /**
     * Creates paragraph view with support for floated images.
     * @param elem paragraph element
     */
    public FloatParagraphView(Element elem)
    {
        super(elem);
        strategy = new FloatFlowStrategy();
    }

    /**
     * Renders using the given rendering surface and area on that
     * surface. This is implemented to delgate to the superclass
     * for most painting, then paint any floating images in the
     * paragraph.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     * @see javax.swing.text.View#paint
     */
    public void paint(Graphics g, Shape a)
    {
        super.paint(g, a);
        paintFloatingImages(g, a);
    }

    /**
     * Paints floating images in the margins of the paragraph.
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     */
    protected void paintFloatingImages(Graphics g, Shape a)
    {
        // Paint intruding floated images
        Iterator it = intrudingFloats.iterator();
        while (it.hasNext())
        {
            FloatableImageView fiv = (FloatableImageView)it.next();
            paintFloatingImage(g, a, fiv, this);
        }
    }

    /**
     * Paints a floated image.
     * @param g the rendering surface to use
     * @param a the allocated area of the current <code>FloatParagraphView</code>
     * @param iv the view for the floated image
     * @param fpv the current <code>FloatParagraphView</code>, which is initiating this paint
     */
    static void paintFloatingImage(Graphics g, Shape a, FloatableImageView iv,
                                   FloatParagraphView fpv)
    {
        Rectangle r = a.getBounds();

        // Obtain index of fpv in parent
        View parent = fpv.getParent();
        int n = parent.getViewCount();
        int thisIndex;
        for (thisIndex = 0; thisIndex < n; thisIndex++)
        {
            if (parent.getView(thisIndex) == fpv)
            {
                break;
            }
        }

        // Convert allocated area of fpv to allocated area of host paragraph
        // Assumes X_AXIS allocation remains constant across subsequent FloatParagraphViews
        FloatParagraphView hostFpv = (FloatParagraphView)iv.getParent().getParent();
        while (fpv != hostFpv)
        {
            fpv = (FloatParagraphView)parent.getView(--thisIndex);
            r.y -= fpv.getPreferredSpan(Y_AXIS);
        }
        r.height = (int)fpv.getPreferredSpan(Y_AXIS);

        // Adjust rendering area for vertical offset within host paragraph
        Shape rowAllocation = hostFpv.getChildAllocation(
                hostFpv.getViewIndexAtPosition(iv.getStartOffset()), r);
        r.y = rowAllocation.getBounds().y;

        // Adjust rendering area for assigned float position
        r.y += iv.vFloatPos;
        if (iv.getLayoutType() == FloatableImageView.FLOAT_LEFT)
        {
            r.x += iv.hFloatPos;
        } else
        {
            r.x += r.width - iv.hFloatPos - iv.getPreferredSpan(X_AXIS, true);
        }

        r.width = (int)iv.getPreferredSpan(X_AXIS, true);
        r.height = (int)iv.getPreferredSpan(Y_AXIS, true);

        iv.paint(g, r, true);
    }

    /**
     * Create a <code>View</code> that should be used to hold a row's worth of children in a flow.
     * Adjusts the left and right margins for any floated images present.
     *
     * @return a new <code>FloatRow</code>
     */
    protected View createRow()
    {
        return new FloatRow(getElement());
    }

    /**
     * Fetch the constraining span to flow against for
     * the given child index. This has been re-implemented
     * because <code>Row</code> was also re-implemented, so the superclass'
     * version of this method does not use the right <code>Row</code> class.
     *
     * @param index the child view (row) to fetch
     *
     * @return the span in pixels
     */
    public int getFlowSpan(int index)
    {
        View child = getView(index);
        int adjust = 0;
        if (child instanceof FloatRow)
        {
            FloatRow row = (FloatRow) child;
            adjust = row.getLeftInset() + row.getRightInset();
        }
        return Math.max(0, layoutSpan - adjust);
    }

    /**
     * Fetch the location along the flow axis that the
     * flow span will start at. This has been re-implemented
     * because <code>Row</code> was also re-implemented, so the superclass'
     * version of this method does not use the right <code>Row</code> class.
     *
     * @param index the child view (row) to fetch
     *
     * @return the start position in pixels
     */
    public int getFlowStart(int index)
    {
        View child = getView(index);
        int adjust = 0;
        if (child instanceof FloatRow)
        {
            FloatRow row = (FloatRow) child;
            adjust = row.getLeftInset();
        }
        return (int)getTabBase() + adjust;
    }

    /**
     * Sets the amount of line spacing.
     * @param ls the line spacing as a factor of line height
     */
    protected void setLineSpacing(float ls)
    {
        super.setLineSpacing(ls);
        lineSpacing = ls;
    }

    /**
     * Gets the amount of line spacing.
     * @return the line spacing as a factor of line height
     */
    protected float getLineSpacing()
    {
        return lineSpacing;
    }

    /**
     * Set the type of justification.
     * @param j one of <code>StyleConstants.ALIGN_LEFT</code>,
     * <code>StyleConstants.ALIGN_RIGHT</code>, etc.
     *
     * @see javax.swing.text.StyleConstants
     */
    protected void setJustification(int j)
    {
        super.setJustification(j);
        justification = j;
    }

    /**
     * Gets the type of justification.
     * @return one of <code>StyleConstants.ALIGN_LEFT</code>,
     * <code>StyleConstants.ALIGN_RIGHT</code>, etc.
     *
     * @see javax.swing.text.StyleConstants
     */
    protected int getJustification()
    {
        return justification;
    }

    /**
     * Gets the index of the child view (<code>FloatRow</code>) at the given document position.
     * Must be implemented here to give access to classes in this package that need it, like
     * <code>FloatableImageView</code>.
     *
     * @param pos The document position.
     * @return The child view (<code>FloatRow</code>) for the given document position.
     */
    protected int getViewIndexAtPosition(int pos)
    {
        return super.getViewIndexAtPosition(pos);
    }

    /**
     * Strategy for maintaining the physical form of the flow.
     * Takes into account floated images in the margins when performing layout.
     */
    public static class FloatFlowStrategy extends FlowStrategy
    {
        /**
         * Creates all <code>Row</code>s and their contents from scratch whenever the paragraph's
         * layout is invalidated.
         * @param fv the <code>FlowView</code> to lay out
         */
        public void layout(FlowView fv)
        {
            if (fv instanceof FloatParagraphView)
            {
                FloatParagraphView pv = (FloatParagraphView)fv;


                // Get previous and next siblings
                View parent = pv.getParent();
                int n = parent.getViewCount();
                View prevSibling = null;
                View nextSibling = null;
                int thisIndex;
                for (thisIndex = 0; thisIndex < n; thisIndex++)
                {
                    if (parent.getView(thisIndex) == pv)
                    {
                        if (thisIndex > 0) prevSibling = parent.getView(thisIndex - 1);
                        if (thisIndex < n - 1) nextSibling = parent.getView(thisIndex + 1);
                        break;
                    }
                }

                // If the previous sibling is a FloatParagraphView, copy its float manager
                // to obtain any floats intruding from above.
                pv.intrudingFloats.clear();
                if (prevSibling != null && prevSibling instanceof FloatParagraphView)
                {
                    FloatParagraphView prevPara = (FloatParagraphView)prevSibling;
                    pv.fmm = prevPara.fmm.copy();

                    // Take responsibility for painting any active floating images
                    Iterator it = pv.fmm.floating.iterator();
                    while (it.hasNext())
                    {
                        FloatMarginManager.FloatSpec spec = (FloatMarginManager.FloatSpec)it.next();
                        pv.intrudingFloats.add(spec.iv);
                    }

                    // Advance the float state to allow for top margin of paragraph
                    pv.fmm.moveDown(pv.getTopInset(), pv);
                } else
                {
                    // Set up an empty float state
                    pv.fmm.clear();
                    // Assumption: the span only changes at the start of a paragraph with no
                    // floats intruding on it from above. We may have to change this so that span
                    // is re-evaluated whenever a margin is cleared of floats by fmm.moveDown(),
                    // especially if we implement support for floats through lists and blockquotes.
                    pv.fmm.setSpan(pv.layoutSpan);
                }

                // Perform the layout
                super.layout(fv);

                // Advance the float state to allow for bottom margin of paragraph
                pv.fmm.moveDown(pv.getBottomInset(), pv);

                // Check if this paragraph contained or now contains floats that intruded on
                // following paragraph or not.
                boolean wasPassForward = pv.passForwardFloats;
                pv.passForwardFloats = !pv.fmm.isEmpty();

                // Invalidate following paragraph if appropriate
                if (nextSibling != null && nextSibling instanceof FloatParagraphView &&
                    (wasPassForward || pv.passForwardFloats))
                {
                    ((ParagraphView)nextSibling).layoutChanged(X_AXIS);
                    ((ParagraphView)nextSibling).layoutChanged(Y_AXIS);
                }

                // If last paragraph, expand bottom margin of this paragraph to allow for floating
                // images to paint fully
                if (nextSibling == null || !(nextSibling instanceof FloatParagraphView) &&
                        pv.passForwardFloats)
                {
                    FloatRow additionalRow = (FloatRow)pv.createRow();

                    // Reset insets to allow bottom margin
                    short bottom = 0;

                    while (!pv.fmm.isEmpty())
                    {
                        short dist = (short)pv.fmm.getRemainingActiveLength();
                        pv.fmm.moveDown(dist, pv);
                        bottom += dist;
                    }

                    additionalRow.setInsets((short)0, (short)0, bottom, (short)0);

                    pv.append(additionalRow);
                }
            } else super.layout(fv);
        }

        /**
         * Fills a row with as many sub-views as will fit. Updates the floating
         * image indentation state if neccessary.
         *
         * @param fv the <code>FlowView</code> for which to lay out a row.
         * @param rowIndex the index of the row view to lay out.
         * @param pos the document position to begin the row.
         * @return The document position at the end of the row.
         */
        protected int layoutRow(FlowView fv, int rowIndex, int pos)
        {
            FloatRow row = (FloatRow)fv.getView(rowIndex);

            // Get margins for floating images
            // Normally we would get the default insets using getLeftInset, etc., but
            // implementation of these in Row adds to the default values for line height
            // and first line indent without removing these again on setInset. To work around
            // this, we simply assume zero insets on all sides.
            short left = (short)((FloatParagraphView)fv).fmm.usedLeft;
            short right = (short)((FloatParagraphView)fv).fmm.usedRight;
            row.setInsets((short)0, left, (short)0, right);

            // Perform layout tasks in superclass
            int endPos = super.layoutRow(fv, rowIndex, pos);

            FloatParagraphView fpv = (FloatParagraphView)fv;

            // Process any floating images in the new row
            processFloatingImages(fpv, row);

            // Add bottom margin to row if it ends with a clearing break
            int bottom = 0;
            int dist;
            if (row.isClearLeft())
            {
                while (!fpv.fmm.isEmpty(LEFT))
                {
                    dist = fpv.fmm.getRemainingActiveLength(LEFT);
                    if (dist == 0) break;
                    fpv.fmm.moveDown(dist, fpv);
                    bottom += dist;
                }
            }
            if (row.isClearRight())
            {
                while (!fpv.fmm.isEmpty(RIGHT))
                {
                    dist = fpv.fmm.getRemainingActiveLength(RIGHT);
                    if (dist == 0) break;
                    fpv.fmm.moveDown(dist, fpv);
                    bottom += dist;
                }
            }
            row.setInsets((short)0, left, (short)bottom, right);

            return endPos;
        }

        /**
         * Adjusts the margins of the parent paragraph view in response to any floated images
         * in the current row. Floated images at the very start of the row intrude on the
         * margins of this row. Other floated images intrude beginning with the next row.
         * This distinction contravenes CSS 2 and CSS2.1, but it's also what every browser out
         * there does.
         * @param fv the paragraph view
         * @param row the row view
         */
        private void processFloatingImages(FloatParagraphView fv, Row row)
        {
            int n = row.getViewCount();
            boolean isLeadingImage = true;
            int rowHeight = row.calculateMinorAxisRequirements(Y_AXIS, null).preferred;
            for (int i = 0; i < n; i++)
            {
                View v = row.getView(i);
                if (v instanceof FloatableImageView && ((FloatableImageView)v).isFloat())
                {
                    FloatableImageView iv = (FloatableImageView)v;
                    int margin = iv.getLayoutType() == FloatableImageView.FLOAT_LEFT ? LEFT : RIGHT;
                    // Update float state
                    fv.fmm.addFloat(
                            (int)iv.getPreferredSpan(X_AXIS, true),
                            (int)iv.getPreferredSpan(Y_AXIS, true),
                            margin, !isLeadingImage, iv, fv);
                } else if (isLeadingImage)
                {
                    isLeadingImage = false;
                }
            }
            fv.fmm.moveDown(rowHeight, fv);
        }
    }

    /**
     * Data structure used to track images floating in the margins of paragraphs.
     * Allows you to advance down the major axis and have floats drop out of significance.
     * Will find the best position for a new floating image given the other floating images
     * currently in effect.
     */
    protected static class FloatMarginManager
    {
        /**
         * Active floats.
         */
        private LinkedList floating;

        /**
         * Floats waiting for space to free up.
         */
        private LinkedList waiting;

        /**
         * The available span, when no floats are active.
         */
        private int span = Integer.MAX_VALUE;

        /**
         * The amount of span currently occupied by left floats.
         */
        private int usedLeft = 0;

        /**
         * The amount of span currently occupied by right floats.
         */
        private int usedRight = 0;

        /**
         * Create an empty <code>FloatMarginManager</code>.
         */
        protected FloatMarginManager()
        {
            floating = new LinkedList();
            waiting = new LinkedList();
        }

        /**
         * Sets the available span when no floats are intruding.
         * @param newSpan The available span in pixels.
         */
        protected void setSpan(int newSpan)
        {
            span = newSpan;
        }

        /**
         * Adds a floating image block, either to begin at the current position, or
         * to wait for space to be positioned.
         *
         * @param size amount of the margin the floating block consumes.
         * @param length length of the floating block (i.e. image height).
         * @param alignment margin to float in (<code>RIGHT</code> or <code>LEFT</code>)
         * @param mustWait if <code>true</code>, this image will not be assigned a space in the
         *        margin until at least the next row.
         * @param iv the <code>FloatableImageView</code> for the image wanting to be floated
         * @param fpv the <code>FloatParagraphView</code> that will be responsible for painting
         *        the <code>FloatableImageView</code> if it is added to the margin immediately.
         *        This may be <code>null</code> if <code>mustWait</code> is <code>true</code>.
         */
        protected void addFloat(int size, int length, int alignment, boolean mustWait,
                                FloatableImageView iv, FloatParagraphView fpv)
        {
            FloatSpec fs = new FloatSpec(size, length, alignment, iv);
            iv.vFloatPos = 0;

            if (!mustWait && getRemainingSpan() >= size)
            {
                if (alignment == LEFT)
                {
                    iv.hFloatPos = usedLeft;
                    usedLeft += size;
                } else if (alignment == RIGHT)
                {
                    iv.hFloatPos = usedRight;
                    usedRight += size;
                }
                // Added first because floats expire like a LIFO queue
                floating.addFirst(fs);
                fpv.intrudingFloats.add(iv);
            } else
            {
                // Added last because floats become active like a FIFO queue
                waiting.addLast(fs);
            }
        }

        /**
         * Gets the amount of span still available between current floats.
         * @return The available span in pixels.
         */
        protected int getRemainingSpan()
        {
            return span - usedLeft - usedRight;
        }

        /**
         * Gets the current amount of the margin occupied by floating image blocks.
         *
         * @param margin The margin to check (<code>LEFT</code> or <code>RIGHT</code>)
         * @return the size of the margin due to floating blocks in pixels
         */
        protected int getMarginSize(int margin)
        {
            int m = 0;
            if (margin == LEFT)
            {
                m = usedLeft;
            } else if (margin == RIGHT)
            {
                m = usedRight;
            }
            return m;
        }

        /**
         * Calculates the remaining document length before all currently active floating blocks end.
         * This does not take into account floats that are waiting for space, so effectively this
         * gives the maximum distance before a new float comes into effect, or all floats are
         * finished.
         *
         * @return remaining document length with objects in the margin
         */
        protected int getRemainingActiveLength()
        {
            int maxLength = 0;
            Iterator it = floating.iterator();
            while (it.hasNext())
            {
                FloatSpec fs = (FloatSpec)it.next();
                maxLength = Math.max(maxLength, fs.length);
            }
            return maxLength;
        }

        /**
         * Calculates the remaining document length before all currently active floating blocks in
         * the specified margin end.
         * This does not take into account floats that are waiting for space, so effectively this
         * gives the maximum distance before a new float comes into effect, or all floats are
         * finished.
         *
         * @param margin the margin to measure the remaining distance in
         * @return remaining document length with objects in the margin
         */
        protected int getRemainingActiveLength(int margin)
        {
            int maxLength = 0;
            Iterator it = floating.iterator();
            while (it.hasNext())
            {
                FloatSpec fs = (FloatSpec)it.next();
                if (fs.alignment == margin)
                {
                    maxLength = Math.max(maxLength, fs.length);
                }
            }
            return maxLength;
        }

        /**
         * Advances the major axis by the specified distance. Some active floats may
         * expire as they no longer intrude at this position, while elements waiting for float
         * space may become active floats.
         *
         * @param distance distance of document to advance
         * @param fpv the <code>FloatParagraphView</code> that will be responsible for painting any
         *        floating images that take effect as a result of this call.
         */
        protected void moveDown(int distance, FloatParagraphView fpv)
        {
            Iterator it;
            FloatSpec currentSpec;

            do
            {
                // If there are no floats to expire, we'll just move the requested distance
                int minDistance = distance;

                // Find out if we can move a shorter distance and get an innermost float to expire
                it = floating.iterator();
                boolean foundInnermostLeftFloat = false;
                boolean foundInnerMostRightFloat = false;
                while (it.hasNext() && !(foundInnermostLeftFloat && foundInnerMostRightFloat))
                {
                    currentSpec = (FloatSpec)it.next();
                    if (currentSpec.alignment == LEFT && !foundInnermostLeftFloat)
                    {
                        foundInnermostLeftFloat = true;
                        if (currentSpec.length < minDistance)
                        {
                            minDistance = currentSpec.length;
                        }
                    } else if (currentSpec.alignment == RIGHT && !foundInnerMostRightFloat)
                    {
                        foundInnerMostRightFloat = true;
                        if (currentSpec.length < minDistance)
                        {
                            minDistance = currentSpec.length;
                        }
                    }
                }

                // Move the discovered distance
                it = floating.iterator();
                boolean leftExpiring = true;
                boolean rightExpiring = true;
                while (it.hasNext())
                {
                    currentSpec = (FloatSpec)it.next();
                    currentSpec.length -= minDistance;
                    // Identify floats eligible for expiration
                    if (currentSpec.length <= 0)
                    {
                        // Expire only innermost floats
                        if (currentSpec.alignment == LEFT && leftExpiring)
                        {
                            usedLeft -= currentSpec.size;
                            it.remove();
                        } else if (currentSpec.alignment == RIGHT && rightExpiring)
                        {
                            usedRight -= currentSpec.size;
                            it.remove();
                        }
                    } else
                    {
                        if (currentSpec.alignment == LEFT)
                        {
                            leftExpiring = false;
                        } else if (currentSpec.alignment == RIGHT)
                        {
                            rightExpiring = false;
                        }
                    }
                }

                // Activate any eligible waiting floats
                it = waiting.iterator();
                while (it.hasNext())
                {
                    currentSpec = (FloatSpec)it.next();
                    // Increment vertical positions of all waiting floats
                    currentSpec.iv.vFloatPos += minDistance;
                    // If the waiting float will fit, or if there are no active floats
                    if (currentSpec.size <= getRemainingSpan() || floating.size() == 0)
                    {
                        if (currentSpec.alignment == LEFT)
                        {
                            currentSpec.iv.hFloatPos = usedLeft;
                            usedLeft += currentSpec.size;
                        } else if (currentSpec.alignment == RIGHT)
                        {
                            currentSpec.iv.hFloatPos = usedRight;
                            usedRight += currentSpec.size;
                        }
                        // Added first because floats expire like a LIFO queue
                        floating.addFirst(currentSpec);
                        fpv.intrudingFloats.add(currentSpec.iv);
                        it.remove();
                    }
                }

                // Decrease distance remaining by amount moved
                distance -= minDistance;
            }
            while (distance > 0);
        }

        /**
         * Resets this object to its initial (empty) state, but keeps the same <code>span</code>
         * value.
         */
        protected void clear()
        {
            floating.clear();
            waiting.clear();
            usedLeft = 0;
            usedRight = 0;
        }

        /**
         * Checks if there are currently any active (or waiting) images.
         * @return True if there are no active or waiting floated images.
         */
        protected boolean isEmpty()
        {
            // If none floating, then we can assume none waiting
            return floating.isEmpty();
        }

        /**
         * Checks if there are currently any active (or waiting) images in the specified margin.
         * @param margin The margin to check. <code>LEFT</code> or <code>RIGHT</code>.
         * @return True if there are no active or waiting floated images.
         */
        protected boolean isEmpty(int margin)
        {
            return isEmpty(margin, false);
        }

        /**
         * Checks if there are currently any active (or waiting) images in the specified margin.
         * @param margin The margin to check. <code>LEFT</code> or <code>RIGHT</code>.
         * @return True if there are no active or waiting floated images.
         */
        protected boolean isEmpty(int margin, boolean onlyFloating)
        {
            Iterator it = floating.iterator();
            while (it.hasNext())
            {
                if (((FloatSpec)it.next()).alignment == margin)
                {
                    return false;
                }
            }
            if (onlyFloating) return true;
            
            it = waiting.iterator();
            while (it.hasNext())
            {
                if (((FloatSpec)it.next()).alignment == margin)
                {
                    return false;
                }
            }
            return true;
        }

        /**
         * Performs a deep copy of this object.
         * @return A copy of this object, containing copies of its elements.
         */
        public FloatMarginManager copy()
        {
            FloatMarginManager clone = new FloatMarginManager();
            clone.span = span;
            clone.usedLeft = usedLeft;
            clone.usedRight = usedRight;
            Iterator it = floating.iterator();
            while (it.hasNext())
            {
                clone.floating.addLast(((FloatSpec)it.next()).copy());
            }
            it = waiting.iterator();
            while (it.hasNext())
            {
                clone.waiting.addLast(((FloatSpec)it.next()).copy());
            }
            return clone;
        }

        /**
         * A record of a single floated element's requested space.
         */
        static class FloatSpec implements Cloneable
        {
            /**
             * The amount of the margin this float will consume.
             */
            private int size;

            /**
             * The vertical distance along the margin this float will consume.
             */
            private int length;

            /**
             * Which margin this float will consume. <code>LEFT</code> or <code>RIGHT</code>.
             */
            private int alignment;

            /**
             * The <code>FloatableImageView</code> responsible for painting the floated image.
             */
            private FloatableImageView iv;

            /**
             * Creates a new <code>FloatSpec</code> with the specified parameters.
             * @param size the amount of margin this float will consume
             * @param length the vertical distance along the margin this float will consume
             * @param alignment which margin this float will consume (<code>LEFT</code> or
             *                  <code>RIGHT</code>)
             * @param iv the <code>FloatableImageView</code> responsible for painting the floated
             *           image
             */
            FloatSpec(int size, int length, int alignment, FloatableImageView iv)
            {
                this.size = size;
                this.length = length;
                this.alignment = alignment;
                this.iv = iv;
            }

            /**
             * Performs a shallow copy of this <code>FloatSpec</code>.
             * @return A shallow copy of this object (i.e. the <code>iv</code> property will point
             *         to the same instance).
             */
            public Object copy()
            {
                Object clone = null;
                try
                {
                    clone = super.clone();
                } catch (CloneNotSupportedException ex)
                {
                    // Should never happen
                }
                return clone;
            }
        }
    }

    /**
     * A <code>Row</code> that may have a floated image in its margin.
     */
    class FloatRow extends Row
    {
        /**
         * Creates a <code>FloatRow</code>.
         * @param elem the paragraph element responsible for this row
         */
        FloatRow(Element elem)
        {
            super(elem);
        }

        /**
         * Performs layout for the major axis of the box (i.e. the
         * axis that it represents). Zeroes the spans of right-aligned images at the
         * start of the row, adjusting subsequent offsets.
         *
         * @param targetSpan the total span given to the view, which
         *                   would be used to layout the children
         * @param axis       the axis being layed out
         * @param offsets    the offsets from the origin of the view for
         *                   each of the child views; this is a return value and is
         *                   filled in by the implementation of this method
         * @param spans      the span of each child view; this is a return
         *                   value and is filled in by the implementation of this method
         */
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans)
        {
            super.layoutMajorAxis(targetSpan, axis, offsets, spans);

            // Find right-floated floated images at the start of the row
            int n = getViewCount();
            for (int i = 0; i < n; i++)
            {
                View v = getView(i);
                if (v instanceof FloatableImageView)
                {
                    FloatableImageView fiv = (FloatableImageView)v;
                    if (!fiv.isFloat()) break;
                    if (fiv.getLayoutType() == FloatableImageView.FLOAT_RIGHT)
                    {
                        // Reduce the offset of subsequent views in the row
                        for (int j = i + 1; j < n; j++)
                        {
                            offsets[j] -= spans[i];
                        }
                        // Set the span of the right-floated image to zero
                        spans[i] = 0;
                    }
                } else break;
            }
        }

        /**
         * Checks if we should clear the left margin after this row.
         *
         * @return true if following rows should begin after any left floats
         */
        boolean isClearLeft()
        {
            FloatBrView brView = getLastFloatBrView();
            return brView != null && brView.isClearLeft();
        }

        /**
         * Checks if we should clear the right margin after this row.
         *
         * @return true if following rows should begin after any right floats
         */
        boolean isClearRight()
        {
            FloatBrView brView = getLastFloatBrView();
            return brView != null && brView.isClearRight();
        }

        /**
         * Returns the <code>FloatBrView</code> that terminates this row, if any.
         *
         * @return the last view in this row if it's a <code>FloatBrView</code>, or null if it's not
         */
        FloatBrView getLastFloatBrView()
        {
            int n = getViewCount();
            if (n > 0)
            {
                View lastView = getView(getViewCount() - 1);
                if (lastView instanceof FloatBrView)
                {
                    return (FloatBrView)lastView;
                }
            }
            return null;
        }

        /**
         * Determines if a point falls before an allocated region. Takes into
         * account that this row may begin with a left-floated image.
         * @param x the X coordinate >= 0
         * @param y the Y coordinate >= 0
         * @param innerAlloc the allocated region; this is the area inside of the insets
         * @return true if the point lies before the region else false
         */
        protected boolean isBefore(int x, int y, Rectangle innerAlloc)
        {
            if (getViewCount() > 0 && getView(0) instanceof FloatableImageView)
            {
                FloatableImageView iv = (FloatableImageView)getView(0);
                int imgWidth = (int)iv.getPreferredSpan(getAxis(), true);
                if (iv.getLayoutType() == FloatableImageView.FLOAT_LEFT)
                {
                    x -= imgWidth;
                }
            }
            return super.isBefore(x, y, innerAlloc);
        }

        /**
         * Determines if a point falls after an allocated region. Takes into
         * account that this row may begin with a left-floated image.
         * @param x the X coordinate >= 0
         * @param y the Y coordinate >= 0
         * @param innerAlloc the allocated region; this is the area inside of the insets
         * @return true if the point lies after the region else false
         */
        protected boolean isAfter(int x, int y, Rectangle innerAlloc)
        {
            if (getViewCount() > 0 && getView(0) instanceof FloatableImageView)
            {
                FloatableImageView iv = (FloatableImageView)getView(0);
                int imgWidth = (int) iv.getPreferredSpan(getAxis(), true);
                if (iv.getLayoutType() == FloatableImageView.FLOAT_LEFT)
                {
                    x -= imgWidth;
                }
            }
            return super.isAfter(x, y, innerAlloc);
        }

        /**
         * Gets the top inset. Redeclared so that it's accessible to containing class.
         *
         * @return the top inset
         * @see FloatParagraphView#createRow
         */
        protected short getTopInset()
        {
            return super.getTopInset();
        }

        /**
         * Gets the left inset. Redeclared so that it's accessible to containing class.
         *
         * @return the left inset
         * @see FloatParagraphView#createRow
         */
        protected short getLeftInset()
        {
            return super.getLeftInset();
        }

        /**
         * Gets the right inset. Redeclared so that it's accessible to containing class.
         *
         * @return the right inset
         * @see FloatParagraphView#createRow
         */
        protected short getRightInset()
        {
            return super.getRightInset();
        }

        /**
         * Gets the bottom inset. Redeclared so that it's accessible to containing class.
         *
         * @return the bottom inset
         * @see FloatParagraphView#createRow
         */
        protected short getBottomInset()
        {
            return super.getBottomInset();
        }

        /**
         * Sets the insets. Redeclared so that it's accessible to containing class.
         *
         * @see FloatParagraphView#createRow
         */
        protected void setInsets(short top, short left, short bottom, short right)
        {
            super.setInsets(top, left, bottom, right);
        }
    }

    /**
     * Internally created view that has the purpose of holding
     * the views that represent the children of the paragraph
     * that have been arranged in rows.
     *
     * Reimplemention of package-private inner class in
     * javax.swing.text.ParagraphView.
     *
     * @see javax.swing.text.ParagraphView.Row
     */
    class Row extends BoxView
    {
        /**
         * Creates a row.
         * @param elem the paragraph element responsible for this row
         */
        Row(Element elem)
        {
            super(elem, View.X_AXIS);
        }

        /**
         * This is reimplemented to do nothing since the
         * paragraph fills in the row with its needed
         * children.
         *
         * @param f the <code>ViewFactory</code> to (not) use
         */
        protected void loadChildren(ViewFactory f)
        {
        }

        /**
         * Fetches the attributes to use when rendering.  This view
         * isn't directly responsible for an element so it returns
         * the outer classes attributes.
         *
         * @return the <code>AttributeSet</code> from the parent view
         */
        public AttributeSet getAttributes()
        {
            View p = getParent();
            return (p != null) ? p.getAttributes() : null;
        }

        /**
         * Determines the desired alignment for this view along an axis. This is implemented to
         * give the total alignment needed to position the children with the alignment points
         * lined up along the axis orthoginal to the axis that is being tiled. The axis being
         * tiled will request to be centered (i.e. 0.5f).
         * @param axis may be either <code>View.X_AXIS</code> or <code>View.Y_AXIS</code>
         * @return the desired alignment >= 0.0f && &lt;= 1.0f; this should be a value between 0.0
         * and 1.0 where 0 indicates alignment at the origin and 1.0 indicates alignment to the
         * full span away from the origin; an alignment of 0.5 would be the center of the view
         */
        public float getAlignment(int axis)
        {
            if (axis == View.X_AXIS)
            {
                switch (justification)
                {
                    case StyleConstants.ALIGN_LEFT:
                        return 0;
                    case StyleConstants.ALIGN_RIGHT:
                        return 1;
                    case StyleConstants.ALIGN_CENTER:
                    case StyleConstants.ALIGN_JUSTIFIED:
                        return 0.5f;
                    default:
                }
            }
            return super.getAlignment(axis);
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.  This is
         * implemented to let the superclass find the position along
         * the major axis and the allocation of the row is used
         * along the minor axis, so that even though the children
         * are different heights they all get the same caret height.
         *
         * @param pos the position to convert
         * @param a   the allocated region to render into
         * @param b   the bias
         * @return the bounding box of the given position
         * @throws BadLocationException if the given position does not represent a
         *                              valid location in the associated document
         * @see View#modelToView
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException
        {
            Rectangle r = a.getBounds();
            View v = getViewAtPosition(pos, r);
            if ((v != null) && (!v.getElement().isLeaf()))
            {
                // Don't adjust the height if the view represents a branch.
                return super.modelToView(pos, a, b);
            }
            r = a.getBounds();
            int height = r.height;
            int y = r.y;
            Shape loc = super.modelToView(pos, a, b);
            r = loc.getBounds();
            r.height = height;
            r.y = y;
            return r;
        }

        /**
         * Fetches the portion of the model for which this view is responsible.
         *
         * @return the starting offset into the model >= 0
         */
        public int getStartOffset()
        {
            int offs = Integer.MAX_VALUE;
            int n = getViewCount();
            for (int i = 0; i < n; i++)
            {
                View v = getView(i);
                offs = Math.min(offs, v.getStartOffset());
            }
            return offs;
        }

        /**
         * Fetches the portion of the model for which this view is responsible.
         *
         * @return the ending offset into the model >= 0
         */
        public int getEndOffset()
        {
            int offs = 0;
            int n = getViewCount();
            for (int i = 0; i < n; i++)
            {
                View v = getView(i);
                offs = Math.max(offs, v.getEndOffset());
            }
            return offs;
        }

        /**
         * Perform layout for the minor axis of the box (i.e. the
         * axis orthoginal to the axis that it represents).  The results
         * of the layout should be placed in the given arrays which represent
         * the allocations to the children along the minor axis.
         *
         * This is implemented to do a baseline layout of the children
         * by calling <code>BoxView.baselineLayout</code>.
         *
         * @param targetSpan the total span given to the view, which
         *                   whould be used to layout the children.
         * @param axis       the axis being layed out.
         * @param offsets    the offsets from the origin of the view for
         *                   each of the child views.  This is a return value and is
         *                   filled in by the implementation of this method.
         * @param spans      the span of each child view.  This is a return
         *                   value and is filled in by the implementation of this method.
         */
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans)
        {
            baselineLayout(targetSpan, axis, offsets, spans);
        }

        /**
         * Calculates the size requirements for the minor axis <code>axis</code>.
         * @param axis The minor axis (<code>Y_AXIS</code> in LTR/RTL text).
         * @param r The <code>SizeRequirements</code> object to return. If null, a new object will
         * be created.
         * @return The size requirements for the minor axis.
         */
        protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                                  SizeRequirements r)
        {
            return baselineRequirements(axis, r);
        }

        /**
         * Fetches the child view index representing the given position in
         * the model.
         *
         * @param pos the position >= 0
         * @return index of the view representing the given position, or
         *         -1 if no view represents that position
         */
        protected int getViewIndexAtPosition(int pos)
        {
            // This is expensive, but are views are not necessarily layed
            // out in model order.
            if (pos < getStartOffset() || pos >= getEndOffset()) return -1;
            for (int counter = getViewCount() - 1; counter >= 0; counter--)
            {
                View v = getView(counter);
                if (pos >= v.getStartOffset() &&
                        pos < v.getEndOffset())
                {
                    return counter;
                }
            }
            return -1;
        }

        /**
         * Gets the left inset.
         *
         * @return the inset
         */
        protected short getLeftInset()
        {
            View parentView;
            int adjustment = 0;
            if ((parentView = getParent()) != null)
            { //use firstLineIdent for the first row
                if (this == parentView.getView(0))
                {
                    adjustment = firstLineIndent;
                }
            }
            return (short)(super.getLeftInset() + adjustment);
        }

        /**
         * Gets the bottom inset.
         *
         * @return the inset
         */
        protected short getBottomInset()
        {
            SizeRequirements minorRequest = calculateMinorAxisRequirements(Y_AXIS, null);

            return (short)(super.getBottomInset() +
                    ((minorRequest != null) ? minorRequest.preferred : 0) * lineSpacing);
        }
    }
}
