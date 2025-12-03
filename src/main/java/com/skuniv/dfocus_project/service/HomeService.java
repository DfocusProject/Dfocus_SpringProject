package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.dto.home.AnnualLeaveDto;
import com.skuniv.dfocus_project.dto.home.CommuteDto;
import com.skuniv.dfocus_project.dto.home.DurationDto;
import com.skuniv.dfocus_project.mapper.AttMapper;
import com.skuniv.dfocus_project.mapper.HomeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final HomeMapper homeMapper;
    private final AttMapper attMapper;
    private final AttService attService;

    public CommuteDto getLatestCommuteRecord(String username) {
        return homeMapper.getLatestCommuteRecord(username);
    }

    public AnnualLeaveDto getAnnualLeaveRecord(String username) {
        // 1. 기존 연차 잔여/사용 데이터 가져오기
        AnnualLeaveDto annualLeaveDto = homeMapper.getAnnualLeaveRecord(username, Year.now());

        // 2. 연차 요청 리스트 가져오기 (start_date ~ end_date 포함)
        List<DurationDto> requestedDayList = attMapper.getExpectedDay(username, Year.now());
        double expectedDay = 0.0;

        // 3. 각 요청의 기간을 1일 단위로 순회
        for (DurationDto requestedDay : requestedDayList) {
            if(requestedDay != null) {
                LocalDate start = requestedDay.getStartDate();
                LocalDate end = requestedDay.getEndDate();

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    // 휴일이나 제외 날짜가 아니면 계산
                    if (!attMapper.isHoliday(username, date)) {
                        expectedDay += 1.0;
                    }
                }
            }
        }
        // 반차 요청 개수 가져오기
        Double halfTypeCount = attMapper.getHalfRequestedCount(username, Year.now());
        expectedDay += halfTypeCount;
        // 6. 계산된 예정 연차 세팅
        annualLeaveDto.setExpectedDay(expectedDay);

        return annualLeaveDto;
    }


    public List<Double> getWeeklyWorkedHours(String empCode) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Double> dailyHours = new ArrayList<>(Collections.nCopies(7, 0.0));

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            List<TimeRange> ranges = attService.getFinalWorkRanges(empCode, date);

            double hours = 0.0;

            for (TimeRange r : ranges) {
                LocalDateTime start = r.getStartDateTime();
                LocalDateTime end = r.getEndDateTime();
                if (start != null && end != null) {
                    hours += Duration.between(start, end).toMinutes() / 60.0;
                }
            }

            dailyHours.set(i, hours);
        }

        return dailyHours;
    }

}
