// 📌 전체 체크박스
function initCheckAll(masterSelector, itemSelector) {
    const master = document.querySelector(masterSelector);
    if (!master) return;

    master.addEventListener('change', e => {
        document.querySelectorAll(itemSelector).forEach(cb => {
            cb.checked = e.target.checked;
        });
    });
}

// 📌 선택된 행 데이터 수집
function collectSelectedRows() {
    return Array.from(document.querySelectorAll('.rowCheck:checked')).map(cb => {
        const row = cb.closest('tr');

        return {
            empCode: row.querySelector('.empCode')?.innerText?.trim() || "",
            newShiftType: row.querySelector('.newShiftType')?.value || "",
            startDate: row.querySelector('.startDate')?.value || "",
            endDate: row.querySelector('.endDate')?.value || "",
            reason: row.querySelector('.reason')?.value || "",
            isTodayRequest: row.querySelector('.isTodayRequest')?.value || ""
        };
    });
}

// 📌 조회
function submitSearch(formSelector) {
    const form = document.querySelector(formSelector);
    if (!form) {
        alert("조회 폼이 없습니다.");
        return;
    }
    form.submit();
}

// // 📌 저장/상신 공통 처리
// function submitApply(actionUrl) {
//     const rows = collectSelectedRows();
//
//     if (rows.length === 0) {
//         alert("선택된 행이 없습니다.");
//         return;
//     }
//
//     // 필수값 체크
//     for (const row of rows) {
//         if (!row.newShiftType) {
//             alert("변경근무를 선택하세요.");
//             return;
//         }
//         if (!row.startDate || !row.endDate) {
//             alert("시작일과 종료일을 입력하세요.");
//             return;
//         }
//         if (!row.reason) {
//             alert("사유를 입력하세요.");
//             return;
//         }
//         if (!row.isTodayRequest) {
//             alert("신청시각을 선택하세요.");
//             return;
//         }
//     }
//
//     if (!confirm("처리하시겠습니까?")) return;
//
//     const form = document.createElement('form');
//     form.method = 'post';
//     form.action = actionUrl;
//
//     rows.forEach((row, idx) => {
//         for (const [key, val] of Object.entries(row)) {
//             const input = document.createElement('input');
//             input.type = 'hidden';
//             input.name = `etcList[${idx}].${key}`;
//             input.value = val;
//             form.appendChild(input);
//         }
//     });
//
//     document.body.appendChild(form);
//     form.submit();
// }
//
// // 📌 삭제/상신취소 → empCode만 전송
// function submitCancel(actionUrl) {
//     const rows = collectSelectedRows();
//
//     if (rows.length === 0) {
//         alert("행을 선택하세요.");
//         return;
//     }
//
//     if (!confirm("삭제(또는 상신취소) 하시겠습니까?")) return;
//
//     const form = document.createElement('form');
//     form.method = 'post';
//     form.action = actionUrl;
//
//     rows.forEach((row, idx) => {
//         const input = document.createElement('input');
//         input.type = 'hidden';
//         input.name = `etcList[${idx}].empCode`;
//         input.value = row.empCode;
//         form.appendChild(input);
//     });
//
//     document.body.appendChild(form);
//     form.submit();
// }

// // 📌 선택된 행에 입력창 값 일괄 적용
// function applyValuesToSelectedRows() {
//     const newShiftType = document.getElementById("newShiftType")?.value || "";
//     const startDate = document.getElementById("startDate")?.value || "";
//     const endDate = document.getElementById("endDate")?.value || "";
//     const reason = document.getElementById("reason")?.value || "";
//     const isTodayRequest = document.getElementById("isTodayRequest")?.value || "";
//
//     const rows = document.querySelectorAll('.rowCheck:checked');
//
//     if (rows.length === 0) {
//         alert("적용할 행을 선택하세요.");
//         return;
//     }
//
//     rows.forEach(cb => {
//         const row = cb.closest("tr");
//
//         if (newShiftType) row.querySelector(".newShiftType").value = newShiftType;
//         if (startDate) row.querySelector(".startDate").value = startDate;
//         if (endDate) row.querySelector(".endDate").value = endDate;
//         if (reason) row.querySelector(".reason").value = reason;
//         if (isTodayRequest) row.querySelector(".isTodayRequest").value = isTodayRequest;
//     });
// }

// 적용 버튼 // 입력되는 값
document.getElementById("btnApplyToRow").addEventListener("click", function () {

    // 상단 입력값 가져오기
    const newShiftType = document.getElementById("newShiftType").value;
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;
    const reason = document.getElementById("reason").value;
    const isTodayRequest = document.getElementById("isTodayRequest").value;

    // 입력값 검증
    if (!newShiftType || !startDate || !endDate || !reason || !isTodayRequest) {
        alert("모든 값을 입력해야 적용할 수 있습니다.");
        return;
    }

    // 체크된 행 찾기
    const checkedRows = document.querySelectorAll(".rowCheck:checked");

    if (checkedRows.length === 0) {
        alert("적용할 행을 선택해주세요.");
        return;
    }

    checkedRows.forEach(rowCheckbox => {
        const row = rowCheckbox.closest("tr");

        // 테이블의 각 셀에 값 적용
        row.querySelector(".newShiftType").value = newShiftType;
        row.querySelector(".startDate").value = startDate;
        row.querySelector(".endDate").value = endDate;
        row.querySelector(".reason").value = reason;
        row.querySelector(".isTodayRequest").value = isTodayRequest;
    });

    alert("선택된 행에 적용되었습니다.");
});

// 📌 초기화
document.addEventListener('DOMContentLoaded', () => {

    initCheckAll('#checkAll', '.rowCheck');

    document.getElementById('btnSearchEtc')?.addEventListener('click', () => {
        submitSearch('#searchForm');
    });

    document.getElementById('btnApplyToRow')?.addEventListener('click', () => {
        applyValuesToSelectedRows();
    });

    document.getElementById('btnSave')?.addEventListener('click', () => {
        const workDate = document.getElementById("startDate")?.value;
        submitApply('/etc/save', workDate);
    });

    document.getElementById('btnRequest')?.addEventListener('click', () => {
        const workDate = document.getElementById("startDate")?.value;
        submitApply('/etc/request', workDate);
    });

    document.getElementById('btnDelete')?.addEventListener('click', () => {
        const workDate = document.getElementById("startDate")?.value;
        submitCancel('/etc/delete', workDate);
    });

    document.getElementById('btnRequestCancel')?.addEventListener('click', () => {
        const workDate = document.getElementById("startDate")?.value;
        submitCancel('/etc/requestCancel', workDate);
    });
});

// 시작일 (오늘, 내일)
document.addEventListener("DOMContentLoaded", () => {

    // 시작일 초기값 = 오늘
    const today = new Date();
    const todayStr = today.toISOString().split("T")[0];
    const tomorrowStr = new Date(today.getTime() + 24 * 60 * 60 * 1000).toISOString().split("T")[0];

    const startDateInput = document.getElementById("startDate");
    if (startDateInput) {
        startDateInput.value = todayStr;   // 초기값 오늘
        startDateInput.min = todayStr;     // 오늘 이전 선택 불가
        startDateInput.max = tomorrowStr;  // 내일까지 가능
    }

    // 조회 후 테이블 행들에도 적용
    const applyStartDateLimit = () => {
        document.querySelectorAll(".startDate").forEach(input => {
            input.min = todayStr;
            input.max = tomorrowStr;
        });
    };
    applyStartDateLimit();
});

