/**
 * 📦 form.js - 근태 공통 처리 스크립트
 * 버튼 이벤트 등록 / 폼 전송 / 체크박스 관리
 */

// 📌전체 체크박스 제어
function initCheckAll(masterSelector, itemSelector) {
    const master = document.querySelector(masterSelector);
    if (!master) return;

    master.addEventListener('change', e => {
        const checked = e.target.checked;
        document.querySelectorAll(itemSelector).forEach(cb => (cb.checked = checked));
    });
}

// ⛏️선택된 행 데이터 수집
function collectSelectedRows() {
    return Array.from(document.querySelectorAll('.rowCheck:checked')).map(cb => {
        const row = cb.closest('tr');
        return {
            empCode: row.querySelector('.empCode').innerText,
            attType: row.querySelector('.attType')?.innerText || '',
            reason: row.querySelector('.reason')?.value || '',
            reasonDetail: row.querySelector('.reasonDetail')?.value || '',
            startTime: row.querySelector('.startTime')?.value || '',
            endTime: row.querySelector('.endTime')?.value || '',
            startNextDay: row.querySelector('.startNextDay')?.checked || false,
            endNextDay: row.querySelector('.endNextDay')?.checked || false,
            requestId: row.querySelector('.requestId')?.value || '',
            halfType: row.querySelector('.halfType')?.value || ''  // 반차 select
        };
    });
}

// 🔖조회 (GET 폼 전송)
function submitSearch(formSelector) {
    const form = document.querySelector(formSelector);
    if (!form) {
        alert('조회 폼이 존재하지 않습니다.');
        return;
    }
    form.submit();
}

// 📋저장/상신 요청 (POST form)
function submitApply(actionUrl, workDate) {
    const rows = collectSelectedRows();
    if (rows.length === 0) {
        alert('선택된 행이 없습니다.');
        return;
    }

    // ✅ 필수값 검증 추가
    for (const row of rows) {
        if (!row.reason) {
            alert('사유를 선택해주세요.');
            return;
        }
        if (!row.reasonDetail || row.reasonDetail.trim() === '') {
            alert('사유내용을 입력해주세요.');
            return;
        }

        // // 🔸 근무유형별로 필수 항목 다르게 체크
        // if (row.attType === '반차') {
        //     if (!row.halfType) {
        //         alert('반차 구분을 선택해주세요.');
        //         return;
        //     }
        // } else {
        //     if (!row.startTime) {
        //         alert('시작시간을 입력해주세요.');
        //         return;
        //     }
        //     if (!row.endTime && row.attType !== '조퇴') {
        //         // 조퇴는 종료시간 없음
        //         alert('종료시간을 입력해주세요.');
        //         return;
        //     }
        // }
    }

    // ✅ actionUrl에 따라 다른 confirm 메시지
    let message = '';
    if (actionUrl.includes('/save')) {
        message = '선택된 근태 정보를 저장하시겠습니까?';
    } else if (actionUrl.includes('/request')) {
        message = '선택된 근태 신청을 상신하시겠습니까?';
    } else {
        message = '처리를 진행하시겠습니까?';
    }

    if (!confirm(message)) {
        return;
    }

    // 중복 전송 방지
    if (window.__submitting) return;
    window.__submitting = true;

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = actionUrl;

    // 날짜 추가
    const dateInput = document.createElement('input');
    dateInput.type = 'hidden';
    dateInput.name = 'workDate';
    dateInput.value = workDate;
    form.appendChild(dateInput);

    // 선택된 행 데이터 추가
    rows.forEach((row, idx) => {
        Object.entries(row).forEach(([key, value]) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = `attList[${idx}].${key}`;
            input.value = value;
            form.appendChild(input);
        });
    });

    document.body.appendChild(form);
    form.submit();

    // 2초 후 다시 전송 가능
    setTimeout(() => (window.__submitting = false), 2000);
}

// 🗑️삭제/상신취소 요청 (POST form)
function submitCancel(actionUrl, workDate) {
    const rows = collectSelectedRows();
    if (rows.length === 0) {
        alert('삭제할 행을 선택하세요.');
        return;
    }

    if (!confirm('선택된 근태 신청을 삭제(또는 취소)하시겠습니까?')) {
        return;
    }

    if (window.__submitting) return;
    window.__submitting = true;

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = actionUrl;

    // 날짜 추가
    const dateInput = document.createElement('input');
    dateInput.type = 'hidden';
    dateInput.name = 'workDate';
    dateInput.value = workDate;
    form.appendChild(dateInput);

    // 최소 데이터만 전송
    rows.forEach((row, idx) => {
        const requestIdInput = document.createElement('input');
        requestIdInput.type = 'hidden';
        requestIdInput.name = `attList[${idx}].requestId`;
        requestIdInput.value = row.requestId;
        form.appendChild(requestIdInput);

    });

    document.body.appendChild(form);
    form.submit();

    setTimeout(() => (window.__submitting = false), 2000);
}
