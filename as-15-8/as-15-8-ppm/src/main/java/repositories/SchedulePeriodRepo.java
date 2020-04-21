package repositories;

import model.Schedule;
import model.SchedulePeriod;
import model.enums.SchedulePeriodState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Repository
public interface SchedulePeriodRepo extends JpaRepository<SchedulePeriod, Long> {

    List<SchedulePeriod> findAllByScheduleAndSchedulePeriodStateAndAndStartTimeBefore(Schedule schedule, SchedulePeriodState schedulePeriodState, LocalTime time);

    @Query(value =
            "with periods as ( " +
                "select p.schedule_id, p.start_time, p.end_time, s.max_workers_count " +
                "from schedule.schedule_periods p " +
                "join schedule.schedules s " +
                    "on p.schedule_id = s.id and " +
                    "s.status in ('PLANNED', 'RUNNING') and " +
                    "s.planned_date = :plannedDate " +
            "), " +
            "times as ( " +
                "select distinct p1.start_time from periods p1 " +
                "union " +
                "select distinct p2.end_time from periods p2 " +
            "), " +
            "intervals as ( " +
                "select t1.start_time, min(t2.start_time) end_time from times t1 " +
                "join times t2 on t1.start_time < t2.start_time " +
                "group by t1.start_time " +
            ") " +
            "select intervals.start_time, intervals.end_time, sum(coalesce(p.max_workers_count, 0)) workers_count, string_agg(schedule_id::character varying, ',') as schedule_ids " +
            "from intervals " +
            "join periods p " +
            "    on (intervals.start_time < p.end_time and intervals.end_time > p.start_time) " +
            "group by intervals.start_time, intervals.end_time " +
            "order by start_time", nativeQuery = true)
    List<String[]> findAllPeriodsWorkersCount(@Param("plannedDate") LocalDate plannedDate);

    @Query(value = "with periods as (" +
        "                select p.start_time, p.end_time, s.max_workers_count " +
        "                from schedule.schedule_periods p " +
        "                join schedule.schedules s " +
        "                    on p.schedule_id = s.id and " +
        "                    s.status in ('PLANNED', 'RUNNING') and " +
        "                    s.planned_date = :plannedDate " +
        "            ), " +
        "            times as ( " +
        "                select distinct p1.start_time from periods p1 " +
        "                union " +
        "                select distinct p2.end_time from periods p2 " +
        "            ), " +
        "            intervals as ( " +
        "                select t1.start_time, min(t2.start_time) end_time from times t1 " +
        "                join times t2 on t1.start_time < t2.start_time " +
        "                group by t1.start_time " +
        "            ) " +
        "            select coalesce(max(workers_count), 0) from ( " +
        "            select intervals.start_time, intervals.end_time, sum(coalesce(p.max_workers_count, 0)) workers_count " +
        "            from intervals " +
        "            join periods p " +
        "                on (intervals.start_time < p.end_time and intervals.end_time > p.start_time) " +
        "            group by intervals.start_time, intervals.end_time " +
        "            order by start_time) all_day where start_time <= :endTime and end_time >= :startTime", nativeQuery = true)
    int getBusyWorkersCount(@Param("plannedDate") LocalDate plannedDate, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
