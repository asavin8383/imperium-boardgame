package controllers.helpers;

import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import org.springframework.data.domain.Sort;

/**
 * Creation date: 03.06.2019
 * Author: asavin
 * Построитель выражений сортировки для постраничных запросов
 */
public class SortingHelper {

    /**
     * Возвращает сортировку по направлению и имени столбца
     * Если направление - null, то возвращает ASC
     * Если имя столбца - null, то возвращает Sort.unsorted()
     * @param sortingDirection направление сортировки
     * @param sortingColumn имя столбца сортировки
     * @return сортировка
     */
    public static Sort createSorting(SortingDirection sortingDirection, String sortingColumn){
        if(sortingDirection == null){
            sortingDirection = SortingDirection.ASC;
        }
        if(sortingColumn == null){
            return Sort.unsorted();
        }
        Sort sort = Sort.by(sortingColumn);
        switch (sortingDirection) {
            case ASC:  {
                return sort.ascending();
            }
            case DESC: {
                return sort.descending();
            }
            default: {
                throw new AS_15_8_PPT_Exception("Error creatin sorting expression! Value is not supported: " + sortingDirection);
            }

        }
    }
}
