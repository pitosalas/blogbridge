/**
 *
 */
package com.salas.bb.domain;

import com.salas.bb.utils.i18n.Strings;

/**
 * Feed type.
 */
public class FeedType
{
    /** Numeric type of text. */
    public static final int TYPE_TEXT    = 0;
    /** Numeric type of image. */
    public static final int TYPE_IMAGE   = 1;
    /** Numeric type of link. */
    public static final int TYPE_LINK    = 2;
    /** Numeric type of Twitter feed. */
    public static final int TYPE_TWITTER = 3;

    /** Traditional text feed. */
    public static final FeedType TEXT       = new FeedType(TYPE_TEXT, Strings.message("feed.type.text"));
    /** Images feed (flickr, engadget ...). */
    public static final FeedType IMAGE      = new FeedType(TYPE_IMAGE, Strings.message("feed.type.images"));
    /** Links feed (del.icio.us, simpy ...). */
    public static final FeedType LINK       = new FeedType(TYPE_LINK, Strings.message("feed.type.links"));
    /** Twitter feed */
    public static final FeedType TWITTER    = new FeedType(TYPE_TWITTER, Strings.message("feed.type.twitter"));

    private final int type;
    private final String typeName;

    /**
     * Hidden enumeration constructor.
     *
     * @param aType     type descriminator.
     * @param aTypeName type name.
     */
    private FeedType(int aType, String aTypeName)
    {
        type = aType;
        typeName = aTypeName;
    }

    /**
     * Returns type descriminator.
     *
     * @return type descriminator.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Converts type descriminator into type object.
     *
     * @param type type descriminator.
     *
     * @return object.
     *
     * @throws IllegalArgumentException when type is unknown.
     */
    public static FeedType toObject(int type)
    {
        FeedType obj;

        switch (type)
        {
            case TYPE_TEXT:
                obj = TEXT;
                break;
            case TYPE_IMAGE:
                obj = IMAGE;
                break;
            case TYPE_LINK:
                obj = LINK;
                break;
            case TYPE_TWITTER:
                obj = TWITTER;
                break;
            default:
                throw new IllegalArgumentException(Strings.error("unsupported.feed.type"));
        }

        return obj;
    }

    /**
     * Returns string representation of this type object.
     *
     * @return string representation.
     */
    public String toString()
    {
        return typeName;
    }

    /**
     * Compares type to the other type object.
     *
     * @param o other type.
     *
     * @return <code>TRUE</code> if equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FeedType feedType = (FeedType)o;

        return type == feedType.type;
    }

    /**
     * Returns hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return type;
    }

    /**
     * Returns all available feed types.
     *
     * @return types.
     */
    public static FeedType[] getAllTypes()
    {
        return new FeedType[] { TEXT, IMAGE, TWITTER };
    }
}
