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
// $Id: ComponentsFactory.java,v 1.12 2008/06/26 13:41:58 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uifextras.util.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Factory of components.
 */
public final class ComponentsFactory
{
    /**
     * Hidden utility class constructor.
     */
    private ComponentsFactory()
    {
    }

    /**
     * Creates a scrollable instructions text box wrapped with scroll pane.
     *
     * @param text text.
     *
     * @return component.
     */
    public static JComponent createInstructionsBox(String text)
    {
        JTextArea instructions = createInstructionsArea(text);

        return new JScrollPane(instructions);
    }

    /**
     * Creates an instructions area.
     *
     * @param text text to initialize area with.
     *
     * @return area.
     */
    public static JTextArea createInstructionsArea(String text)
    {
        JTextArea instructions = new JTextArea(text);
        instructions.setWrapStyleWord(true);
        instructions.setLineWrap(true);
        instructions.setTabSize(4);
        Color background = instructions.getBackground();
        instructions.setEditable(false);
        instructions.setBackground(background);
        UifUtilities.smallerFont(instructions);
        instructions.setMargin(new Insets(10, 10, 10, 10));

        return instructions;
    }

    /**
     * Creates radio-button with mnemonized text.
     *
     * @param textWithMnemonic text with mnemonic mark.
     *
     * @return created radio-button.
     */
    public static JRadioButton createRadioButton(String textWithMnemonic)
    {
        return createRadioButton(textWithMnemonic, null);
    }

    /**
     * Creates radio-button with mnemonized text.
     *
     * @param textWithMnemonic  text with mnemonic mark.
     * @param model             model to use for radio-button.
     *
     * @return created radio-button.
     */
    public static JRadioButton createRadioButton(String textWithMnemonic, ButtonModel model)
    {
        JRadioButton button = new JRadioButton();

        if (model != null) button.setModel(model);

        setTextAndMnemonic(button, textWithMnemonic);

        return button;
    }

    /**
     * Creates check-box with mnemonized text.
     *
     * @param textWithMnemonic text with mnemonic mark.
     *
     * @return created check-box.
     */
    public static JCheckBox createCheckBox(String textWithMnemonic)
    {
        return createCheckBox(textWithMnemonic, null);
    }

    /**
     * Creates check-box with mnemonized text.
     *
     * @param textWithMnemonic text with mnemonic mark.
     * @param model             model of check box.
     *
     * @return created check-box.
     */
    public static JCheckBox createCheckBox(String textWithMnemonic, ButtonModel model)
    {
        JCheckBox checkBox = new JCheckBox();

        if (model != null) checkBox.setModel(model);

        setTextAndMnemonic(checkBox, textWithMnemonic);

        return checkBox;
    }

    /**
     * Creates check-box with mnemonized text.
     *
     * @param textWithMnemonic  text with mnemonic mark.
     * @param bean              bean to bind this check box to.
     * @param propertyName      name of the bean property.
     * @param trigger           channel to trigger the commit operation.
     *
     * @return created check-box.
     */
    public static JCheckBox createCheckBox(String textWithMnemonic, Object bean, String propertyName,
                                           ValueModel trigger)
    {
        return createCheckBox(textWithMnemonic, new ToggleButtonAdapter(
            new BufferedValueModel(new PropertyAdapter(bean, propertyName), trigger)));
    }

    /**
     * Creates button with mnemonized text.
     *
     * @param textWithMnemonic text with mnemonic mark.
     *
     * @return created button.
     */
    public static JButton createButton(String textWithMnemonic)
    {
        JButton button = new JButton();

        setTextAndMnemonic(button, textWithMnemonic);

        return button;
    }

    /**
     * Creates label with mnemonized text.
     *
     * @param textWithMnemonic text with mnemonic mark.
     *
     * @return created label.
     */
    public static JLabel createLabel(String textWithMnemonic)
    {
        MnemonicHolder holder = findMnemonic(textWithMnemonic);

        JLabel label = new JLabel(holder.text);
        if (holder.mnemonicIndex != -1)
        {
            label.setDisplayedMnemonic(holder.mnemonicChar);
            label.setDisplayedMnemonicIndex(holder.mnemonicIndex);
        }

        return label;
    }

    /**
     * Sets the mnemonic to existing button.
     *
     * @param button            button.
     * @param textWithMenmonic  text with mnemonic.
     *
     * @return button.
     */
    public static AbstractButton setTextAndMnemonic(AbstractButton button, String textWithMenmonic)
    {
        setTextAndMnemonic(button, findMnemonic(textWithMenmonic));

        return button;
    }

    /**
     * Creates wrapped multi-line label with font and cursor similar to other labels.
     *
     * @param text text to initialize with.
     *
     * @return label.
     */
    public static JTextArea createWrappedMultilineLabel(String text)
    {
        final JTextArea mlLabel = UIFactory.createWrappedMultilineLabel(text);

        final JLabel label = new JLabel();
        mlLabel.setFont(label.getFont());
        mlLabel.setCursor(label.getCursor());

        return mlLabel;
    }

    /**
     * Sets text and mnemonic to button.
     *
     * @param button    button.
     * @param holder    holder of mnemonic information.
     */
    private static void setTextAndMnemonic(AbstractButton button, MnemonicHolder holder)
    {
        button.setText(holder.text);
        if (holder.mnemonicIndex != -1)
        {
            button.setMnemonic(holder.mnemonicChar);
            button.setDisplayedMnemonicIndex(holder.mnemonicIndex);
        }
    }

    /**
     * Finds mnemonic mark in text, records it and removes.
     *
     * @param textWithMnemonic  text with mnemonic mark.
     *
     * @return results of search (text without mark and marked char).
     */
    private static MnemonicHolder findMnemonic(String textWithMnemonic)
    {
        MnemonicHolder holder = new MnemonicHolder();

        holder.text = textWithMnemonic;
        holder.mnemonicChar = (char)0;
        holder.mnemonicIndex = -1;

        if (textWithMnemonic != null)
        {
            int index = textWithMnemonic.indexOf('&');
            if (index != -1)
            {
                holder.mnemonicIndex = index;
                holder.mnemonicChar = textWithMnemonic.charAt(index + 1);

                int len = textWithMnemonic.length();

                StringBuffer sb = new StringBuffer(len - 1);
                sb.append(textWithMnemonic.substring(0, index));
                sb.append(textWithMnemonic.substring(index + 1, len));

                holder.text = sb.toString();
            }
        }

        return holder;
    }

    /**
     * Creates separator with collapsable icon to the left from label and connects it to
     * the given component. The clicking on icon controls visibility of the component.
     *
     * @param textWithMnemonic  text with mnemonic mark.
     * @param comp              component visibility of which to control.
     *
     * @return separator read for the form.
     */
    public static JComponent createCollapsibleSeparator(String textWithMnemonic,
        final JComponent comp)
    {
        MnemonicHolder holder = findMnemonic(textWithMnemonic);

        CollapseControlLabel label = new CollapseControlLabel(holder.text);
        if (holder.mnemonicIndex != -1)
        {
            label.setDisplayedMnemonic(holder.mnemonicChar);
            label.setDisplayedMnemonicIndex(holder.mnemonicIndex);
        }

        if (comp != null)
        {
            comp.setVisible(!label.isCollapsed());

            label.addPropertyChangeListener("collapsed", new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    boolean collapsed = (Boolean)evt.getNewValue();
                    comp.setVisible(!collapsed);
                }
            });
        }

        return label;
    }

    /**
     * Holder for de-mnemonized text and mnemonic index.
     */
    private static class MnemonicHolder
    {
        private String  text;
        private char    mnemonicChar;
        private int     mnemonicIndex;
    }

    /**
     * Special label with small collapsing icon to the left from label.
     * When the collapsing state changes it fires "collapsed" property change event.
     */
    private static class CollapseControlLabel extends JLabel
    {
        private static final Icon ICON_COL_OUT = ResourceUtils.getIcon("ccl.col.out");
        private static final Icon ICON_COL_OVR = ResourceUtils.getIcon("ccl.col.ovr");
        private static final Icon ICON_EXP_OUT = ResourceUtils.getIcon("ccl.exp.out");
        private static final Icon ICON_EXP_OVR = ResourceUtils.getIcon("ccl.exp.ovr");

        private static final boolean DEFAULT_COLLAPSED = true;

        private boolean collapsed;
        private boolean over;

        public CollapseControlLabel(String text)
        {
            this(text, DEFAULT_COLLAPSED);
        }

        public CollapseControlLabel(String text, boolean aCollapsed)
        {
            super(text);
            setCollapsed(aCollapsed);

            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        public void setCollapsed(boolean aCollapsed)
        {
            if (collapsed != aCollapsed)
            {
                collapsed = aCollapsed;
                updateCollapseIcon();
                firePropertyChange("collapsed", !collapsed, collapsed);
            }
        }

        public boolean isCollapsed()
        {
            return collapsed;
        }

        private void updateCollapseIcon()
        {
            Icon icon;
            if (collapsed)
            {
                icon = over ? ICON_COL_OVR : ICON_COL_OUT;
            } else
            {
                icon = over ? ICON_EXP_OVR : ICON_EXP_OUT;
            }

            setIcon(icon);
        }

        protected void processEvent(AWTEvent e)
        {
            int id = e.getID();

            switch (id)
            {
                case MouseEvent.MOUSE_ENTERED:
                    over = true;
                    updateCollapseIcon();
                    break;
                case MouseEvent.MOUSE_EXITED:
                    over = false;
                    updateCollapseIcon();
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    setCollapsed(!collapsed);
                    break;
                default:
                    super.processEvent(e);
                    break;
            }
        }
    }
}
