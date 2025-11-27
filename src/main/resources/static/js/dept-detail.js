// 부서 패턴 지정/변경
const setPatternBtn = document.getElementById('setPatternBtn');
const patternSelect = document.getElementById('patternSelect');
const patternForm = document.getElementById('patternForm');

if (setPatternBtn) {
    setPatternBtn.addEventListener('click', () => {
        // 패널 보이기
        patternSelect.style.display = 'block';
        // 변경하기 버튼 숨김
        setPatternBtn.style.display = 'none';
    });
}

// 저장 버튼 클릭 시
if (patternForm) {
    patternForm.addEventListener('submit', (e) => {
        // submit 전에 선택값 확인
        const selectedPattern = patternForm.patternCode.value;
        if (!selectedPattern) {
            e.preventDefault();
            alert('패턴을 선택해주세요.');
            return;
        }

        // form은 그대로 submit → 서버로 patternCode와 deptCode 전송
        // submit 후 다시 버튼 보여주려면 다음처럼 가능
        // submit 후 다시 버튼 보여주려면 다음처럼 가능
        patternForm.addEventListener('submit', () => {
            setPatternBtn.style.display = 'inline-block';
            patternSelect.style.display = 'none';
        });
    });
}


// 세 개 패널 모두 닫는 함수
const movePanel = document.getElementById('moveDeptSelect');
const personalPanel = document.getElementById('personalPatternSelect');
const leavePanel = document.getElementById('leaveChangeSelect');

function closeAllPanels() {
    movePanel.style.display = 'none';
    personalPanel.style.display = 'none';
    leavePanel.style.display = 'none';
}

// 공통 함수: 체크된 사원이 있는지 확인
function hasCheckedEmployees() {
    return document.querySelectorAll('input[name="empCodes"]:checked').length > 0;
}

// 부서에서 삭제, 리더로 지정 (form전에 검사)
document.querySelectorAll('button[type="submit"][name="action"]').forEach(btn => {
    if (btn.value === "delete" || btn.value === "setLeader") {
        btn.addEventListener('click', (e) => {
            if (!hasCheckedEmployees()) {
                e.preventDefault();
                alert("먼저 사원을 선택해주세요.");
            }
        });
    }
});


document.querySelectorAll('button[type="submit"][name="action"]').forEach(btn => {

    btn.addEventListener('click', (e) => {

        // 사원 미선택 검사
        if (!hasCheckedEmployees()) {
            e.preventDefault();
            alert("먼저 사원을 선택해주세요.");
            return;
        }

        // 액션별 확인창 메시지
        let msg = "";
        if (btn.value === "delete") {
            msg = "부서 내 사원을 [삭제]하시겠습니까?";
        } else if (btn.value === "setLeader") {
            msg = "부서 내 [리더]로 지정하시겠습니까?";
        } else if (btn.value === "moveDept") {
            const deptName = document.getElementById("newDept").selectedOptions[0].textContent;
            msg = `부서 내 사원을 [${deptName}] 부서로 이동하시겠습니까?`;
        } else if (btn.value === "setPersonalPattern") {
            const patternName = document.getElementById("personalPatternCode").value;
            msg = `부서 내 사원에게 근무패턴:[${patternName}]을 지정하시겠습니까?`;
        } else if (btn.value === "changeLeave") {
            msg = "부서 내 사원을 [휴직 변경] 하시겠습니까?";
        }

        if (msg !== "" && !confirm(msg)) {
            e.preventDefault();
        }
    });

});

//다른 부서로 이동
const moveBtn = document.getElementById('moveDeptBtn');

moveBtn.addEventListener('click', () => {
    if (!hasCheckedEmployees()) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }

    const isOpen = movePanel.style.display === 'block'; // 현재 열려있는지

    closeAllPanels(); // 일단 모두 닫고

    // 이미 열려있던 경우 → 닫힌 상태 유지 (토글)
    if (!isOpen) {
        movePanel.style.display = 'block';
    }
});

// 개별 근태 패턴 지정
const personalBtn = document.getElementById('setPersonalPattern');
const personalEmpInputs = document.getElementById('personalEmpInputs');

personalBtn.addEventListener('click', () => {
    const checkedEmps = Array.from(document.querySelectorAll('input[name="empCodes"]:checked'))
        .map(cb => cb.value);

    if (checkedEmps.length === 0) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }

    // 히든 input 생성
    personalEmpInputs.innerHTML = '';
    checkedEmps.forEach(empCode => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'empCodes';
        input.value = empCode;
        personalEmpInputs.appendChild(input);
    });

    const isOpen = personalPanel.style.display === 'block';

    closeAllPanels();

    if (!isOpen) {
        personalPanel.style.display = 'block';
    }
});

// 휴직 변경 버튼
const changeLeaveBtn = document.getElementById('changeLeave');
const leaveEmpInputs = document.getElementById('leaveEmpInputs');

changeLeaveBtn.addEventListener('click', () => {
    const checkedEmps = Array.from(document.querySelectorAll('input[name="empCodes"]:checked'))
        .map(cb => cb.value);

    if (checkedEmps.length === 0) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }

    leaveEmpInputs.innerHTML = '';
    checkedEmps.forEach(empCode => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'empCodes'; // 서버에서 List<String>으로 바인딩 가능
        input.value = empCode;
        leaveEmpInputs.appendChild(input);
    });

    const isOpen = leavePanel.style.display === 'block';

    closeAllPanels();

    if (!isOpen) {
        leavePanel.style.display = 'block';
    }
});
