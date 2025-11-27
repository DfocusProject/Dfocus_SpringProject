package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.mapper.AccountMapper;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmpService {
    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;
    private final AccountMapper accountMapper;

    public String removeFromDept(List<String> empCodes) {
        for(String empCode : empCodes) {
            String role = empMapper.getEmpRole(empCode);
            if(role.equals("LEADER")) {
                return "리더는 부서에서 삭제할 수 없습니다.";
            }
        }
        empMapper.deleteEmpFromDept(empCodes);
        return "부서 내 사원 삭제가 처리되었습니다.";
    }

    @Transactional
    public String setDeptLeader(List<String> empCodes, String deptCode) {

        if(empCodes.size() != 1) {
            return "리더는 한 명만 지정 가능합니다";
        }
        String role = empMapper.getEmpRole(empCodes.get(0));
        if(role.equals("ADMIN")){
            return "관리자는 리더로 설정할 수 없습니다";
        }
        else if(role.equals("LEADER")){
            return "해당 사원은 이미 리더로 지정되어 있습니다";
        }
        String currentLeaderEmpCode = deptMapper.getLeaderByDeptCode(deptCode);

        // 2. 현재 리더가 있으면 USER로 변경
        if(currentLeaderEmpCode != null && !currentLeaderEmpCode.isEmpty()) {
            accountMapper.updateRole(currentLeaderEmpCode, "USER");
        }

        deptMapper.updateDeptLeader(empCodes.get(0), deptCode);
        accountMapper.updateRole(empCodes.get(0), "LEADER");
        return "리더로 지정되었습니다";
    }

    public String moveDept(List<String> empCodes, String newDeptCode) {
        for(String empCode : empCodes) {
            String role = empMapper.getEmpRole(empCode);
            if(role.equals("LEADER")) {
                return "리더는 부서 이동이 불가합니다.";
            }
        }
        empMapper.updateDeptCode(empCodes, newDeptCode);
        return "부서 이동이 완료되었습니다.";
    }
    public List<EmpDto> getEmpListNoDept() {
        return empMapper.findEmpNoDept();
    }

    public void assignEmployeesToDept(List<String> empCodes, String deptCode) {
        empMapper.assignDept(empCodes, deptCode);
    }

    public void setPattern(String deptCode, String patternCode) {
        empMapper.setPattern(deptCode, patternCode);
    }

    public List<AttEmpViewDto> getAttEmpList(String attType, LocalDate workDate, List<String> empCode, String deptName) {
        return empMapper.findAttEmpList(attType, workDate, empCode, deptName);
    }

    public List<AttEmpViewDto> getEtcAttEmpList(String planType, LocalDate workDate, String searchEmpCode, String deptName) {
        return empMapper.findEtcAttEmpList(planType, workDate, searchEmpCode, deptName);
    }

    public void setPesonalPattern(List<String> empCodes, String patternCode) {
        for(String empCode : empCodes){
            empMapper.setPersonalPattern(empCode, patternCode);
        }
    }

    public void setEmpToOff(List<String> empCodes, String leaveType){
        for(String empCode : empCodes){
            if(leaveType.equals("WK")){
                empMapper.updateEmpStatus(empCode, "WORK");
                empMapper.setPersonalPattern(empCode, null);
            }
            else if(leaveType.equals("BL")){
                empMapper.updateEmpStatus(empCode, "OFF");
                empMapper.setPersonalPattern(empCode, "휴직");
            }
            else{
                empMapper.updateEmpStatus(empCode, "OFF");
                empMapper.setPersonalPattern(empCode, "육아휴직");
            }
        }
    }
}
