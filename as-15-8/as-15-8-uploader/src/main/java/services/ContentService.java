package services;

import checkUnits.CheckUnitType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import model.enums.BlockType;
import model.scheme.ContentInfo;
import model.scheme.ContentResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ContentInfoRepository;
import repositories.ContentRepository;
import repositories.ContentResourcesRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentInfoRepository infoRepository;
    private final ContentResourcesRepository resourceRepository;

    @Transactional
    public Page<ContentView> getRelevantContent(Pageable page) {
        Page<ContentView> contentPage = contentRepository.findRelevant(page);
        contentPage.getContent().forEach(view -> {
            view.setContentInfo(infoRepository.findOneByContentVersion_IdAndContent_Id(
                    view.getContentVersionId(), view.getId()));

            BlockType blockType = view.getContentInfo().getBlockType();
            List<String> resourceTypes = getResourceTypesFor(blockType);
            view.setExampleResource(resourceRepository.findOneByContentAndVersionAndTypeDsc(
                    view.getId(), view.getContentVersionId(), resourceTypes));

        });
        return contentPage;
    }

/*    @Async("asyncExecutor")
    public ResponseEntity<?> update() {
//        log.info("@Async: " +
//                SecurityContextHolder.getContext()
//                .getAuthentication().getPrincipal());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) { }
        service.update();
        ResponseEntity.ok();
        return ResponseEntity.ok().build();
    }*/

    /* @GetMapping(path = "/update")
    public Callable<Void> update() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) { }
                return service.update();
            }
        };
    }*/

    /*@GetMapping(path = "/update")
    public DeferredResult<ResponseEntity<?>> update() {
        DeferredResult<ResponseEntity<?>> output = new DeferredResult<>();

        ForkJoinPool.commonPool().submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            output.setResult(ResponseEntity.ok().build());
        });

        return output;
    }*/

    public static List<String> getResourceTypesFor(BlockType blockType) {
        // todo bi map
        switch (blockType) {
            case IP:
                return Arrays
                        .stream(CheckUnitType.values())
                        .map(CheckUnitType::toString)
                        .filter(type -> type.contains("ip"))
                        .collect(Collectors.toList());
            case DOMAIN:
            case DOMAIN_MASK:
                return Collections.singletonList(CheckUnitType.DOMAIN.toString().toLowerCase());
            default:
                return Collections.singletonList(CheckUnitType.URL.toString().toLowerCase());
        }
    }

    @Data
    @NoArgsConstructor
    public static class ContentView {

        private Long id;

        private String erdiId;

        private Long contentVersionId;

        private ContentInfo contentInfo;

        private ContentResources exampleResource;

        @SuppressWarnings("unused")
        public ContentView(Long id, String erdiId,
                           Long contentVersionId) {
            this.id = id;
            this.erdiId = erdiId;
            this.contentVersionId = contentVersionId;
        }

    }

}
