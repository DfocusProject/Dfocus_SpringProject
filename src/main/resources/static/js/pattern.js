document.addEventListener("DOMContentLoaded", () => {
    const topBtn = document.getElementById("topActionBtn");
    const registerBtn = document.getElementById("registerPatternBtn");
    const saveBtn = document.getElementById("savePatternBtn");
    const deleteBtn = document.getElementById("deletePatternBtn");
    const modal = document.getElementById("patternModal");
    const modalCancelBtn = document.getElementById("modalCancelBtn");
    const modalConfirmBtn = document.getElementById("modalConfirmBtn");
    const modalPatternName = document.getElementById("modalPatternName");
    const modalPatternDesc = document.getElementById("modalPatternDesc");

    // 코드 일괄 입력
    const bulkInputContainer = document.getElementById("bulkInputContainer");
    const bulkCodeInput = document.getElementById("bulkCodeInput");
    const applyBulkCodeBtn = document.getElementById("applyBulkCodeBtn");

    // 요일별 입력
    const dayOfWeekSelect = document.getElementById("dayOfWeekSelect");
    const dayCodeInput = document.getElementById("dayCodeInput");
    const applyDayCodeBtn = document.getElementById("applyDayCodeBtn");

    let rowAdded = false;
    let editingRow = null;

    // 근태코드 패널 색상 적용
    document.querySelectorAll('.code-table tbody tr').forEach(row => {
        const codeCell = row.cells[0];
        if (codeCell) codeCell.classList.add(`code-${codeCell.textContent.trim()}`);
    });

    // 라디오 상태 변화 반응
    document.querySelectorAll('input[name="selectedPattern"]').forEach(radio => {
        radio.addEventListener("mousedown", (e) => {

            // 편집 중이 아니라면 아무 제한 없음
            if (!editingRow) return;

            const clickedRow = radio.closest("tr");

            // 이미 편집 중인 행의 라디오를 클릭한 경우는 허용
            if (clickedRow === editingRow) {
                return;
            }

            // 다른 행을 클릭한 경우만 차단
            e.preventDefault();
            alert("이미 편집 중인 패턴이 있습니다. 편집을 저장하거나 취소하세요.");
            return false;
        });
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

        // 코드 일괄 입력 표시
        bulkInputContainer.style.display = "flex";
        bulkCodeInput.value = "";
        dayOfWeekSelect.value = "";
        dayCodeInput.value = "";

        // 저장 버튼 표시
        saveBtn.style.display = "block";
        registerBtn.style.display = "none";
        topBtn.style.display = "none";
        deleteBtn.style.display = "none";
    });

    // 전체 코드 기입 버튼 클릭
    applyBulkCodeBtn.addEventListener("click", () => {
        const code = bulkCodeInput.value.trim();

        if (!code) {
            alert("코드를 입력해주세요.");
            bulkCodeInput.focus();
            return;
        }

        if (!editingRow) {
            alert("편집 중인 패턴이 없습니다.");
            return;
        }

        // 선택된 행의 모든 input에 값 설정
        const inputs = editingRow.querySelectorAll('.code-cell input[type="text"]');
        inputs.forEach(input => {
            input.value = code;
        });

        alert(`모든 셀에 "${code}" 코드가 입력되었습니다.`);
        bulkCodeInput.value = "";
    });

    // 요일별 코드 기입 버튼 클릭
    applyDayCodeBtn.addEventListener("click", () => {
        const dayOfWeek = dayOfWeekSelect.value;
        const code = dayCodeInput.value.trim();

        if (!dayOfWeek) {
            alert("요일을 선택해주세요.");
            dayOfWeekSelect.focus();
            return;
        }

        if (!code) {
            alert("코드를 입력해주세요.");
            dayCodeInput.focus();
            return;
        }

        if (!editingRow) {
            alert("편집 중인 패턴이 없습니다.");
            return;
        }

        // 헤더에서 요일 정보 가져오기
        const headers = document.querySelectorAll('#patternTable thead th.date-header');
        const dayIndices = []; // 해당 요일의 컬럼 인덱스 저장

        headers.forEach((header, index) => {
            const fullDate = header.getAttribute('data-full-date');
            if (fullDate) {
                // LocalDate를 파싱하여 요일 구하기
                const date = new Date(fullDate);
                // JavaScript의 getDay(): 0(일)~6(토)
                // 우리 select value: 1(월)~7(일)
                let jsDay = date.getDay(); // 0=일, 1=월, ..., 6=토
                let ourDay = jsDay === 0 ? 7 : jsDay; // 7=일, 1=월, ..., 6=토

                if (ourDay.toString() === dayOfWeek) {
                    dayIndices.push(index);
                }
            }
        });

        // 해당 요일의 input들에 코드 적용
        const inputs = editingRow.querySelectorAll('.code-cell input[type="text"]');
        let count = 0;

        dayIndices.forEach(colIndex => {
            const inputIndex = colIndex;
            if (inputIndex >= 0 && inputIndex < inputs.length) {
                inputs[inputIndex].value = code;
                count++;
            }
        });

        const dayNames = ['', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일', '일요일'];
        alert(`${dayNames[dayOfWeek]} ${count}개 셀에 "${code}" 코드가 입력되었습니다.`);

        dayCodeInput.value = "";
    });

    // 저장 버튼 클릭
    saveBtn.addEventListener("click", () => {
        if (!editingRow) return;

        const patternName = document.getElementById("savePatternName").value;

        if (!patternName) {
            alert("패턴명이 누락되었습니다.");
            return;
        }

        // 숨기기
        bulkInputContainer.style.display = "none";

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

        // 숨기기
        bulkInputContainer.style.display = "none";
    }

    // 삭제 버튼
    deleteBtn.addEventListener("click", () => {
        const selected = document.querySelector('input[name="selectedPattern"]:checked');

        if (!selected) {
            alert("삭제할 패턴을 선택해주세요.");
            return;
        }

        if (!confirm("정말 삭제하시겠습니까?")) return;

        // BE로 보낼 데이터 설정
        document.getElementById("deletePatternInput").value = selected.value;
        // 실제 삭제 요청 전송
        document.getElementById("deletePatternForm").submit();

        alert("삭제되었습니다.");
    });
});