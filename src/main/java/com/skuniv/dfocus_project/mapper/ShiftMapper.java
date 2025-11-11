package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.Shift;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ShiftMapper {
    Shift selectShift(@Param("shiftCode") String shiftCode);
    List<Map<String, Object>> selectWeeklyShiftCodes(
            @Param("empCode") String empCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
