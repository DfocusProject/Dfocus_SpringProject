    document.addEventListener("DOMContentLoaded", () => {
    const topBtn = document.getElementById("topActionBtn");
    const registerBtn = document.getElementById("registerPatternBtn");
    const saveBtn = document.getElementById("savePatternBtn");
    const modal = document.getElementById("patternModal");
    const modalCancelBtn = document.getElementById("modalCancelBtn");
    const modalConfirmBtn = document.getElementById("modalConfirmBtn");
    const modalPatternName = document.getElementById("modalPatternName");
    const modalPatternDesc = document.getElementById("modalPatternDesc");
    let rowAdded = false;
    let editingRow = null;

    // 근태코드 패널 색상 적용
    document.querySelectorAll('.code-table tbody tr').forEach(row => {
    const codeCell = row.cells[0];
    if (codeCell) codeCell.classList.add(`code-${codeCell.textContent.trim()}`);
});

    // 모달 열기
    topBtn.addEventListener("click", () => {
    if (!rowAdded) {
    modal.classList.add("active");
    modalPatternName.focus();
}
});

    // 모달 닫기 (취소 버튼)
    modalCancelBtn.addEventListener("click", () => {
    modal.classList.remove("active");
    modalPatternName.value = "";
    modalPatternDesc.value = "";
});

    // 모달 닫기 (배경 클릭)
    modal.addEventListener("click", (e) => {
    if (e.target === modal) {
    modal.classList.remove("active");
    modalPatternName.value = "";
    modalPatternDesc.value = "";
}
});

    // 모달 확인 버튼
    modalConfirmBtn.addEventListener("click", () => {
    const patternName = modalPatternName.value.trim();

    if (!patternName) {
    alert("패턴명을 입력해주세요.");
    modalPatternName.focus();
    return;
}

    document.getElementById("patternNameInput").value = patternName;
    document.getElementById("patternDescInput").value = modalPatternDesc.value.trim();

    document.getElementById("createPatternForm").submit();
    rowAdded = true;
});

    // Enter 키로 확인
    modalPatternDesc.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
    modalConfirmBtn.click();
}
});

    // 패턴 등록 버튼 클릭
    registerBtn.addEventListener("click", () => {
    const selectedRadio = document.querySelector('input[name="selectedPattern"]:checked');

    if (!selectedRadio) {
    alert("패턴을 선택해주세요.");
    return;
}

    const patternName = selectedRadio.value;
    const row = selectedRadio.closest('tr');

    // 이미 편집 중인 행이 있으면 되돌리기
    if (editingRow && editingRow !== row) {
    restoreRow(editingRow);
}

    editingRow = row;

    // 패턴명 저장
    document.getElementById("savePatternName").value = patternName;

    // 코드 셀들을 input으로 변경
    const codeCells = row.querySelectorAll('.code-cell');
    codeCells.forEach(cell => {
    const currentValue = cell.textContent.trim();
    const date = cell.getAttribute('data-date');

    // 기존 색상 클래스 제거
    cell.className = 'code-cell editable-cell';

    cell.innerHTML = `<input type="text"
                                         autocomplete="off"
                                         name="dateCodeMap[${date}]"
                                         value="${currentValue}"
                                         data-date="${date}">`;
});

    // 저장 버튼 표시
    saveBtn.style.display = "block";
    registerBtn.style.display = "none";
});

    // 저장 버튼 클릭
    saveBtn.addEventListener("click", () => {
    if (!editingRow) return;

    const patternName = document.getElementById("savePatternName").value;

    if (!patternName) {
    alert("패턴명이 누락되었습니다.");
    return;
}

    // 폼 제출
    document.getElementById("patternSaveForm").submit();
});

    // 행 복원 함수
    function restoreRow(row) {
    const codeCells = row.querySelectorAll('.code-cell');
    codeCells.forEach(cell => {
    if (cell.classList.contains('editable-cell')) {
    const input = cell.querySelector('input');
    const value = input ? input.value.trim() : '';
    cell.classList.remove('editable-cell');

    // 색상 클래스 다시 적용
    if (value) {
    cell.classList.add(`code-${value}`);
}
    cell.innerHTML = value;
}
});
}
});
