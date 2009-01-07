package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.general.DateAfterCO;
import com.salas.bb.domain.query.general.IDates;
import com.salas.bb.domain.query.general.LongLessCO;
import com.salas.bb.domain.query.general.LongGreaterCO;
import junit.framework.TestCase;

/**
 * This suite contains acceptance tests for criteria and query validation.
 */
public class TestValidationAcceptance extends TestCase
{
    private ICriteria criteria;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        criteria = new Query().addCriteria();
    }

    /**
     * Tests catching of "after today" clause by validator.
     */
    public void testValidatingAfterToday()
    {
        criteria.setProperty(ArticleDateProperty.INSTANCE);
        criteria.setComparisonOperation(DateAfterCO.INSTANCE);
        criteria.setValue(IDates.VALUE_TODAY);

        assertNotNull(ArticleDateProperty.ERR_FUTURE, criteria.validate());
    }

    /**
     * Tests catching of "less than 1 star" criteria.
     */
    public void testValidatingStarzLessThanOne()
    {
        criteria.setProperty(FeedStarzProperty.INSTANCE);
        criteria.setComparisonOperation(LongLessCO.INSTANCE);
        criteria.setValue("1");

        assertNotNull(FeedStarzProperty.ERR_LESS_1_STAR, criteria.validate());
    }

    /**
     * Tests catching of "greater than 5 starz" criteria.
     */
    public void testValidatingStarzMoreThanFive()
    {
        criteria.setProperty(FeedStarzProperty.INSTANCE);
        criteria.setComparisonOperation(LongGreaterCO.INSTANCE);
        criteria.setValue("5");

        assertNotNull(FeedStarzProperty.ERR_MORE_5_STARZ, criteria.validate());
    }
}
