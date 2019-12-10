package services;

import helpers.CriteriaHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import repositories.RobotRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class RobotService {

    private final RobotRepository robotRepository;

    private final EntityManager em;

    public Page<Robot> get(Pageable pageable){
        return robotRepository.findAll(pageable);
    }

    public Page<Robot> getByQuery(Pageable pageable, String query){

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Robot> mainQuery = cb.createQuery(Robot.class);
        Root<Robot> mainRoot = mainQuery.from(Robot.class);
        List<Predicate> predicates = new ArrayList<>();

        if (!StringUtils.isEmpty(query)) {
            List<SingularAttribute<Robot, ?>> likeAttrs = new ArrayList<>();
            likeAttrs.add(Robot_.id);
            likeAttrs.add(Robot_.origId);
            likeAttrs.add(Robot_.name);
            likeAttrs.add(Robot_.origName);

            String likeQuery = "%" + query.toLowerCase() + "%";
            for (SingularAttribute<Robot, ?> attr : likeAttrs) {
                predicates.add(cb.like(cb.lower(mainRoot.get(attr).as(String.class)), likeQuery));
            }

            predicates.add(cb.and(
                    cb.equal(mainRoot.get(Robot_.type), RobotType.PS),
                    cb.like(cb.literal("ПС".toLowerCase()), likeQuery)
                    ));
            predicates.add(cb.and(
                    cb.equal(mainRoot.get(Robot_.type), RobotType.PASD),
                    cb.like(cb.literal("ПАСД".toLowerCase()), likeQuery)
            ));

            predicates.add(cb.and(
                    cb.lessThan(mainRoot.get(Robot_.origId), 0L),
                    cb.like(cb.literal("АС 15.8".toLowerCase()), likeQuery)
            ));
            predicates.add(cb.and(
                    cb.greaterThanOrEqualTo(mainRoot.get(Robot_.origId), 0L),
                    cb.like(cb.literal("ППП РА".toLowerCase()), likeQuery)
            ));

            predicates.add(cb.and(
                    cb.equal(mainRoot.get(Robot_.STATUS), RobotStatus.WORK),
                    cb.like(cb.literal("Работает".toLowerCase()), likeQuery)
            ));
            predicates.add(cb.and(
                    cb.equal(mainRoot.get(Robot_.STATUS), RobotStatus.OUT_OF_WORK),
                    cb.like(cb.literal("Не работает".toLowerCase()), likeQuery)
            ));


            Predicate predicate = cb.or(predicates.toArray(new Predicate[0]));

            mainQuery.where(predicate);
        }

        mainQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), mainRoot, cb));
        return CriteriaHelper.createPage(em, mainQuery, pageable);
    }

    public Optional<Robot> findById(Long id){
        return robotRepository.findById(id);
    }

    public Robot edit(Robot robot, Robot newRobot){
        log.info("Сохранение робота: {}", robot);

        if(robot == null)
            throw new IllegalArgumentException("Робот не найден по ID");

        robot.setName(newRobot.getName());
        robot.setAccessTool(newRobot.getAccessTool());
        robot.setModificationDate(LocalDateTime.now());
        robot.setStatus(newRobot.getStatus());

        robot.getRobotProperties().clear();
        for(RobotProperty prop : newRobot.getRobotProperties()) {
            prop.setRobot(robot);
            robot.getRobotProperties().add(prop);
        }
        robot = robotRepository.save(robot);
        log.info("Робот успешно сохранен: id {}, name {}", robot.getId(), robot.getName());
        return robot;
    }

    public void delete(Robot robot){
        if(robot.getId() < 0)
            robotRepository.delete(robot);
    }
}
