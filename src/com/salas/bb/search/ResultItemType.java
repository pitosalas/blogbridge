/**
 * 
 */

package com.salas.bb.search;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;

/**
 * Search result item type.
 */
public class ResultItemType
{
    /** Guide. */
    public static final ResultItemType GUIDE =
        new ResultItemType(0, Strings.message("search.itemtype.guides"), "search.guide.icon");

    /** Feed. */
    public static final ResultItemType FEED =
        new ResultItemType(1, Strings.message("search.itemtype.feeds"), "search.feed.icon");

    /** Article. */
    public static final ResultItemType ARTICLE =
        new ResultItemType(2, Strings.message("search.itemtype.article"), "search.article.icon");

    /** Picture. */
    public static final ResultItemType PICTURE =
        new ResultItemType(3, Strings.message("search.itemtype.picture"), "search.picture.icon");

    /** Number of types. */
    public static final int COUNT = 4;

    private final int order;
    private final String name;
    private final Icon icon;

    /**
     * Creates item type object.
     *
     * @param anOrder   order.
     * @param aName     item name.
     * @param aIconName icon resource name.
     */
    private ResultItemType(int anOrder, String aName, String aIconName)
    {
        order = anOrder;
        name = aName;
        icon = ResourceUtils.getIcon(aIconName);
    }

    /**
     * Returns type name.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns order.
     *
     * @return order.
     */
    public int getOrder()
    {
        return order;
    }

    /**
     * Returns the icon corresponding to this type.
     *
     * @return icon.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns string representation.
     *
     * @return string representation.
     */
    public String toString()
    {
        return name;
    }
}
