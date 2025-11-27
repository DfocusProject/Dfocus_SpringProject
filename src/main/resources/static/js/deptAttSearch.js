/* 체크박스 전체 선택 */
document.getElementById("checkAll1").addEventListener("change", function () {
    const checked = this.checked;
    document.querySelectorAll(".rowCheck").forEach(cb => cb.checked = checked);
});

/* 개별 체크 시 전체 체크 갱신 */
document.querySelectorAll(".rowCheck").forEach(cb => {
    cb.addEventListener("change", () => {
        const all = document.querySelectorAll(".rowCheck");
        const checked = document.querySelectorAll(".rowCheck:checked");
        document.getElementById("checkAll1").checked = all.length === checked.length;
    });
});

/* 선택된 사번과 부서 모으기 */
function getSelectedEmpData() {
    return Array.from(document.querySelectorAll(".rowCheck:checked"))
        .map(cb => ({
            empCode: cb.dataset.empcode,
            empDept: cb.dataset.department  // 여기에 데이터-부서 속성 추가 필요
        }));
}

/* 신청 버튼 처리 */
function submitWithAction(actionUrl, userDept) {
    const selected = getSelectedEmpData();
    if (selected.length === 0) {
        alert("근태신청할 부서원을 선택하십시오.");
        return;
    }

    // 본인 부서 체크
    const invalid = selected.some(emp => emp.empDept !== userDept);
    if (invalid) {
        alert("하위 부서 직원은 근태신청할 수 없습니다.");
        return;
    }

    const empCodes = selected.map(emp => emp.empCode).join(",");
    const form = document.getElementById("attForm");
    form.action = actionUrl;
    document.getElementById("empCodes").value = empCodes;
    form.submit();
}

// userDept는 서버에서 로그인한 사용자 부서로 동적으로 넣어줘야 함
const userDept = /*[[${loginUser.department}]]*/ 'DEV'; // 예시

document.getElementById("btnGoGeneral").onclick = () => submitWithAction("/att/general", userDept);
document.getElementById("btnGoEtc").onclick = () => submitWithAction("/att/etc", userDept);
