
// 📌전체 체크박스 제어
function initCheckAll(masterSelector, itemSelector) {
    const master = document.querySelector(masterSelector);
    if (!master) return;

    master.addEventListener('change', e => {
        const checked = e.target.checked;
        document.querySelectorAll(itemSelector).forEach(cb => cb.checked = checked);
    });
}

// ⛏️선택된 행 데이터 수집
function collectSelectedRows() {
    return Array.from(document.querySelectorAll('.rowCheck:checked')).map(cb => {
        const row = cb.closest('tr');
        return {
            empNo: row.querySelector('.empNo').innerText,
            //name: row.querySelector('.name').innerText,
            //position: row.querySelector('.position').innerText,
            //department: row.querySelector('.department').innerText,
            workType: row.querySelector('.workType')?.innerText || '',
            reason: row.querySelector('.reason')?.value || '',
            reasonDetail: row.querySelector('.reasonDetail')?.value || '',
            startTime: row.querySelector('.startTime')?.value || '',
            endTime: row.querySelector('.endTime')?.value || ''
            //expectedHours: row.querySelector('.expectedHours').innerText || '',
            //status: row.querySelector('.status').innerText || '',
            //applicant: row.querySelector('.applicant').innerText || ''
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

// 📋저장 요청 (POST form)
function submitSave(actionUrl, workDate) {
    const rows = collectSelectedRows();
    if (rows.length === 0) {
        alert('선택된 행이 없습니다.');
        return;
    }

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
}

// 🗑️삭제 요청 (POST form)
function submitDelete(actionUrl, workDate) {
    const rows = collectSelectedRows();
    if (rows.length === 0) {
        alert('삭제할 행을 선택하세요.');
        return;
    }

    if (!confirm('선택된 근태 신청을 삭제하시겠습니까?')) {
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = actionUrl;

    // 날짜 추가
    const dateInput = document.createElement('input');
    dateInput.type = 'hidden';
    dateInput.name = 'workDate';
    dateInput.value = workDate;
    form.appendChild(dateInput);

    // 근무유형 + 근무일 + 사번만 전송
    rows.forEach((row, idx) => {
        const empNoInput = document.createElement('input');
        empNoInput.type = 'hidden';
        empNoInput.name = `attList[${idx}].empNo`;
        empNoInput.value = row.empNo;
        form.appendChild(empNoInput);

        const typeInput = document.createElement('input');
        typeInput.type = 'hidden';
        typeInput.name = `attList[${idx}].workType`;
        typeInput.value = row.workType;
        form.appendChild(typeInput);

        const dateHidden = document.createElement('input');
        dateHidden.type = 'hidden';
        dateHidden.name = `attList[${idx}].workDate`;
        dateHidden.value = workDate;
        form.appendChild(dateHidden);
    });

    document.body.appendChild(form);
    form.submit();
}
