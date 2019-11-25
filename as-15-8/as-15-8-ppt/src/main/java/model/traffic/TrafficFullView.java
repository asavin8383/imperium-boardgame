package model.traffic;

import lombok.Data;
import model.enums.TrafficUnitType;

@Data
public class TrafficFullView {

    private Long id;

    private String name;

    private ErdiTrafficUnit formalErdiUnit;

    private ErdiTrafficUnit customErdiUnit;

    private SearchQueryTrafficUnit searchPhraseUnit;

    //private List<SearchQueryTrafficUnit> searchTemplates;



    public void setUnit(TrafficUnit trafficUnit) {
        TrafficUnitType type = trafficUnit.getType();
        if (type == null)
            return;

        switch (type) {
            case FORMAL:
                setFormalErdiUnit((ErdiTrafficUnit) trafficUnit);
                break;
            case CUSTOM:
                setCustomErdiUnit((ErdiTrafficUnit) trafficUnit);
                break;
            case PHRASE:
                setSearchPhraseUnit((SearchQueryTrafficUnit) trafficUnit);
                break;
            /*case TEMPLATE:
                if (searchTemplates == null)
                    searchTemplates = new ArrayList<>();
                searchTemplates.add((SearchQueryTrafficUnit) trafficUnit);
                break;*/
            case DYNAMIC:
                throw new UnsupportedOperationException("DYNAMIC not implemented");
        }
    }

}
