package analysis;

public interface StubAnalysisResult {

    String getFinalUrl();

    String getStubUrl();

    Integer getPageSize();

    Integer getKeyWordsCount();

    Integer getLinkCount();

    Integer getDomainNameCount();

    void setStubScoreInfo(String info);
}
