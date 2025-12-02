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

/* 선택된 사번 리스트 가져오기 */
function getSelectedEmpData() {
    return Array.from(document.querySelectorAll(".rowCheck:checked"))
        .map(cb => ({
            empCode: cb.dataset.empcode,
            empDept: cb.dataset.department
        }));
}

/* 신청 버튼 처리 */
function submitWithAction(url, userDept) {

    const selected = getSelectedEmpData();
    if (selected.length === 0) {
        alert("근태신청할 부서원을 선택하십시오.");
        return;
    }

    // 본인 부서 확인
    const invalid = selected.some(emp => emp.empDept !== userDept);
    if (invalid) {
        alert("하위 부서 직원은 근태신청할 수 없습니다.");
        return;
    }

    const empCodes = selected.map(emp => emp.empCode).join(",");
    const workDate = document.querySelector("input[name='workDate']").value;

    // GET 방식 → URL에 파라미터 붙여서 이동
    const finalUrl = `${url}?empCodes=${encodeURIComponent(empCodes)}&workDate=${workDate}`;

    // DEBUG
    console.log("===== 요청 URL =====");
    console.log(finalUrl);
    console.log("====================");

    window.location.href = finalUrl;
}

// userDept 서버에서 세팅해야 함
const userDept = /*[[${loginUser.department}]]*/ 'G1510'; // 예시

// 일반근태신청 → /att/general/search
document.getElementById("btnGoGeneral").onclick = () =>
    submitWithAction("/att/general/search", userDept);

// 기타근태신청 → /att/etc/search
document.getElementById("btnGoEtc").onclick = () =>
    submitWithAction("/att/etc/search", userDept);
