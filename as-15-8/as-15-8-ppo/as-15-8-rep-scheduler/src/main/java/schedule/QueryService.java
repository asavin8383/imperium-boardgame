package schedule;

/**
 * Фасад для создания отчета
 *
 * User: asinjavin
 * Date: 11.10.2019
 * Time: 15:40
 */
public interface QueryService
{
    void beforeAll();
    void beforeEach(long rep_id);

    void refreshDatamart();
}
