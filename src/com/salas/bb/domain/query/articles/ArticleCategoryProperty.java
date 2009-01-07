package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.query.AbstractProperty;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.domain.query.general.StringNotContainsCO;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.i18n.Strings;

/**
 * Property, representing the category of an article.
 */
public class ArticleCategoryProperty extends AbstractProperty
{
    /** Instance of this property. */
    public static final ArticleCategoryProperty INSTANCE = new ArticleCategoryProperty();

    /**
     * Creates property.
     */
    protected ArticleCategoryProperty()
    {
        super(Strings.message("query.property.article.category"), "article-category", PropertyType.STRING, "",
            new IComparisonOperation[]
            {
                StringContainsCO.INSTANCE, StringNotContainsCO.INSTANCE
            });
    }

    /**
     * Compares the corresponding property of the target object to the value using specific
     * comparison operation.
     *
     * @param target    target object.
     * @param operation comparison operation, supported by this property.
     * @param value     value to compare against.
     *
     * @return TRUE if the object matches the criteria.
     *
     * @throws NullPointerException     if target, operation or value are NULL's.
     * @throws ClassCastException       if target is not supported.
     * @throws IllegalArgumentException if operation is not supported.
     */
    public boolean match(Object target, IComparisonOperation operation, String value)
    {
        IArticle article = (IArticle)target;
        String categories = article == null ? null : article.getSubject();

        return operation.match(categories, value.toUpperCase());
    }
}
