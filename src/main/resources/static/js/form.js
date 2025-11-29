const pageType = document.body.dataset.page;

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

        const selectedType = row.querySelector('.attType')?.innerText?.trim();
        if (selectedType === '연차') {
            row.dataset.planStart = '';
            row.dataset.planEnd = '';
            row.dataset.planType = '';
        }

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
            halfType: row.querySelector('.halfType')?.value || '', // 반차 select

            //ETC PAGE FIELDS
            planType: row.querySelector('.planType')?.innerText?.trim() || "",
            newShiftType: row.querySelector('.newShiftType')?.value || "",
            startDate: row.querySelector('.startDate')?.value || "",
            endDate: row.querySelector('.endDate')?.value || "",
            isTodayRequest: row.querySelector('.isTodayRequest')?.value || "",
            balanceDay: row.querySelector('.balanceDay')?.innerText || ''
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

    for (const row of rows) {

        if (pageType === "general") {
            // 사유 검증
            if (!row.reason) {
                alert('사유를 선택해주세요.');
                return;
            }
            if (!row.reasonDetail || row.reasonDetail.trim() === '') {
                alert('사유내용을 입력해주세요.');
                return;
            }
            if (row.attType !== '반차' && row.attType !== '조퇴') {

                if (!row.startTime || !row.endTime) {
                    alert('시작시간과 종료시간을 입력해주세요.');
                    return;
                }
                // 시간을 분 단위로 변환
                const [startHour, startMin] = row.startTime.split(':').map(Number);
                const [endHour, endMin] = row.endTime.split(':').map(Number);


                let startTotalMin = startHour * 60 + startMin;
                let endTotalMin = endHour * 60 + endMin;


                // 익일 체크 시 +24시간(1440분)
                console.log('startNextDay:', row.startNextDay, 'endNextDay:', row.endNextDay);
                if (row.startNextDay) startTotalMin += 1440;
                if (row.endNextDay) endTotalMin += 1440;

                // 근무시간 계산
                const diffMin = endTotalMin - startTotalMin;

                // 종료시간이 시작시간 이전인지 체크
                if (diffMin <= 0) {
                    alert('종료시간은 시작시간 이후여야 합니다.');
                    return;
                }

                // 30분 단위 체크
                if (diffMin % 30 !== 0) {
                    alert('근무시간은 30분 단위로 입력해주세요.');
                    return;
                }
            }

        }

        // etc 페이지 전용 검증
        if (pageType === "etc") {

            if (!row.newShiftType) {
                alert("변경근무를 선택하세요.");
                return;
            }

            // if (!row.startDate || !row.endDate) {
            if (!row.endDate) {
                alert("종료일을 선택하세요.");
                return;
            }

            if (!row.reason || row.reason.trim() === "") {
                alert("사유를 입력하세요.");
                return;
            }

            if (!row.isTodayRequest) {
                alert("신청 시각을 선택하세요.");
                return;
            }
        }
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

    // general/etc 결정
    const listName = (pageType === "etc") ? "etcList" : "attList";
    // 선택된 행 데이터 추가
    rows.forEach((row, idx) => {
        Object.entries(row).forEach(([key, value]) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = `${listName}[${idx}].${key}`;
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

    // general/etc 결정
    const listName = (pageType === "etc") ? "etcList" : "attList";
    // 최소 데이터만 전송
    rows.forEach((row, idx) => {
        const requestIdInput = document.createElement('input');
        requestIdInput.type = 'hidden';
        requestIdInput.name = `${listName}[${idx}].requestId`;
        requestIdInput.value = row.requestId;
        form.appendChild(requestIdInput);

    });

    document.body.appendChild(form);
    form.submit();

    setTimeout(() => (window.__submitting = false), 2000);
}
