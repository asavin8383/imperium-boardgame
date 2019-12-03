package repositories.helper;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

/**
 * Created by san
 * Date: 02.12.2019
 */
public class OrderRandom extends Order
{
    protected final static String RANDOM = "random()"; //random() generates a random number on PostgreSQL
    private static final long serialVersionUID = 3815371067383546907L;

    public OrderRandom()
    {
        super(null,true);
    }

    @Override
    public String toSqlString(Criteria arg0, CriteriaQuery arg1) throws HibernateException
    {
        return this.toString();
    }

    @Override
    public String toString()
    {
        return RANDOM;
    }
}
