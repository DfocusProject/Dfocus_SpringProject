let lastAppliedType = null;

// 🔹 근무유형별 테이블 처리 (조회 이후에만 작동)
function handleWorkTypeChange(selectedType) {
    if (selectedType === lastAppliedType) return;
    lastAppliedType = selectedType;

    const table = document.querySelector('#attTable');
    if (!table) return;

    const thead = table.querySelector('thead');
    const rows = table.querySelectorAll('tbody tr');
    if (!thead || rows.length === 0) return;

    const startColIndex = 11;
    const endColIndex = 12;
    let halfHeader = thead.querySelector('th.half-col');

    // ✅ 초기화
    rows.forEach(row => {
        const startInput = row.querySelector('.startTime');
        const endInput = row.querySelector('.endTime');
        if (!startInput || !endInput) return;

        startInput.disabled = false;
        endInput.disabled = false;
        startInput.style.display = '';
        endInput.style.display = '';

        // 기존 반차 요소 제거
        const exist = row.querySelector('.halfType');
        if (exist) exist.remove();

        const startTd = row.querySelector(`td:nth-child(${startColIndex})`);
        const endTd = row.querySelector(`td:nth-child(${endColIndex})`);
        if (startTd) startTd.style.display = '';
        if (endTd) endTd.style.display = '';
    });

    if (halfHeader) halfHeader.remove();
    table.querySelectorAll('td.half-col').forEach(td => td.remove());

    // 🔸 근무유형별 처리
    switch (selectedType) {
        case '연장':
            rows.forEach(row => {
                const startInput = row.querySelector('.startTime');
                const endInput = row.querySelector('.endTime');
                const planEnd = row.dataset.planEnd || '';
                const savedEnd = row.dataset.reqEnd || '';
                startInput.value = planEnd;       // ✅ 퇴근시간
                endInput.value = savedEnd || '';  // ✅ 저장값 또는 없음
                startInput.disabled = true;
                endInput.disabled = false;
            });
            break;

        case '조출':
            rows.forEach(row => {
                const startInput = row.querySelector('.startTime');
                const endInput = row.querySelector('.endTime');
                const planStart = row.dataset.planStart || '';
                const savedStart = row.dataset.reqStart || '';
                startInput.value = savedStart || ''; // ✅ 저장값 또는 없음
                endInput.value = planStart;          // ✅ 출근시간
                startInput.disabled = false;
                endInput.disabled = true;
            });
            break;


        case '조퇴': {
            const startTh = thead.querySelector(`th:nth-child(${startColIndex})`);
            const endTh = thead.querySelector(`th:nth-child(${endColIndex})`);

            if (startTh) startTh.style.display = '';
            if (endTh) endTh.style.display = 'none';

            rows.forEach(row => {
                const startInput = row.querySelector('.startTime');
                const endInput = row.querySelector('.endTime');
                const startTd = row.querySelector(`td:nth-child(${startColIndex})`);
                const endTd = row.querySelector(`td:nth-child(${endColIndex})`);

                if (startInput) {
                    startInput.disabled = false;
                    startInput.style.display = '';
                }
                if (endInput) {
                    endInput.value = '';
                    endInput.disabled = true;
                    endInput.style.display = 'none';
                }
                if (endTd) endTd.style.display = 'none';
                if (startTd) startTd.style.display = '';
            });
            break;
        }

        case '반차': {
            const startTh = thead.querySelector(`th:nth-child(${startColIndex})`);
            const endTh = thead.querySelector(`th:nth-child(${endColIndex})`);
            if (startTh) startTh.style.display = 'none';
            if (endTh) endTh.style.display = 'none';

            let halfTh = thead.querySelector('th.half-th');
            if (!halfTh) {
                const reasonDetailTh = thead.querySelector('th:nth-child(10)');
                halfTh = document.createElement('th');
                halfTh.className = 'half-th';
                halfTh.textContent = '반차구분';
                reasonDetailTh.after(halfTh);
            } else {
                halfTh.style.display = '';
            }

            const expectedTh = thead.querySelector('th.highlight');
            if (expectedTh && halfTh) {
                halfTh.after(expectedTh);
            }

            rows.forEach(row => {
                const startTd = row.querySelector(`td:nth-child(${startColIndex})`);
                const endTd = row.querySelector(`td:nth-child(${endColIndex})`);
                if (startTd) startTd.style.display = 'none';
                if (endTd) endTd.style.display = 'none';

                let halfTd = row.querySelector('td.half-col');
                if (!halfTd) {
                    halfTd = document.createElement('td');
                    halfTd.className = 'half-col';
                    const sel = document.createElement('select');
                    sel.className = 'halfType form-select';
                    sel.name = 'halfType';
                    sel.required = true;
                    sel.innerHTML = `
    <option value="">--선택--</option>
    <option value="morningOff" ${row.dataset.halfType === '전반차' ? 'selected' : ''}>전반차</option>
    <option value="afternoonOff" ${row.dataset.halfType === '후반차' ? 'selected' : ''}>후반차</option>`;
                    halfTd.appendChild(sel);

                    const reasonDetailTd = Array.from(row.querySelectorAll('td'))
                        .find(td => td.querySelector('.reasonDetail'));
                    reasonDetailTd.after(halfTd);
                } else {
                    halfTd.style.display = '';
                }

                const expectedTd = row.querySelector('td.highlight');
                if (expectedTd && halfTd) {
                    halfTd.after(expectedTd);
                }
            });
            break;
        }

        case '휴일':
        case '외출':
            rows.forEach(row => {
                const startInput = row.querySelector('.startTime');
                const endInput = row.querySelector('.endTime');

                const savedStart = row.dataset.reqStart || '';
                const savedEnd = row.dataset.reqEnd || '';

                startInput.value = savedStart;
                endInput.value = savedEnd;

                startInput.disabled = false;
                endInput.disabled = false;
            });
            break;

    }

    handleNextDayCheckboxes(selectedType, rows);
}

// 🔹 익일 체크박스 표시/숨김
function handleNextDayCheckboxes(selectedType, rows) {
    rows.forEach(row => {
        const startNext = row.querySelector('.startNextDay');
        const endNext = row.querySelector('.endNextDay');

        if (['연장','조출','휴일','외출'].includes(selectedType)) {
            if (startNext) startNext.closest('label').style.display = '';
            if (endNext) endNext.closest('label').style.display = '';
        } else {
            if (startNext) { startNext.closest('label').style.display = 'none'; startNext.checked = false; }
            if (endNext) { endNext.closest('label').style.display = 'none'; endNext.checked = false; }
        }
    });
}

// 🔹 초기화 및 이벤트 등록
document.addEventListener('DOMContentLoaded', () => {
    initCheckAll('#checkAll', '.rowCheck');

    const attType = document.querySelector('#searchForm select[name="attType"]');
    const workDateInput = document.querySelector('#workDate');

    document.getElementById('btnSearch')?.addEventListener('click', () => {
        submitSearch('#searchForm');
        if (attType?.value) handleWorkTypeChange(attType.value);
    });

    document.getElementById('btnSave')?.addEventListener('click', () => submitApply('/att/save', workDateInput.value));
    document.getElementById('btnDelete')?.addEventListener('click', () => submitCancel('/att/delete', workDateInput.value));
    document.getElementById('btnRequest')?.addEventListener('click', () => submitApply('/att/request', workDateInput.value));
    document.getElementById('btnRequestCancel')?.addEventListener('click', () => submitCancel('/att/requestCancel', workDateInput.value));

    // 페이지 로드 후, 테이블 데이터가 있으면 선택된 근무유형 적용
    const hasTableData = document.querySelector('#attTable tbody tr');
    if (hasTableData && attType?.value) {
        handleWorkTypeChange(attType.value);
    }
});
