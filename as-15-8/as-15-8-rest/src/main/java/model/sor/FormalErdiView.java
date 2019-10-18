package model.sor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FormalErdiView {

    private Long id;

    private String erdiId;

    private ContentResource exampleResource;

    private Long initContentVersionId;

    private ContentInfo contentInfo;

}
