// 부서 트리(조직도) 열고닫기 구조
function toggleNode(element) {
    const children = element.nextElementSibling;
    if (!children) return;

    const icon = element.querySelector('.toggle-icon');

    if (children && children.classList.contains('children')) {
        children.classList.toggle('show');
        icon.classList.toggle('expanded');
    }
}

// 부서 패턴 지정/변경
const setPatternBtn = document.getElementById('setPatternBtn');
const patternSelect = document.getElementById('patternSelect');

if (setPatternBtn) {
    setPatternBtn.addEventListener('click', () => {
        patternSelect.style.display = patternSelect.style.display === 'none' ? 'block' : 'none';
    });
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


// 부서에서 삭제, 리더 지정 → 제출 전에 확인창 추가
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
            msg = "부서 내 사원을 삭제하시겠습니까?";
        } else if (btn.value === "setLeader") {
            msg = "부서 내 리더로 지정하시겠습니까?";
        } else if (btn.value === "moveDept") {
            const deptName = document.getElementById("newDept").selectedOptions[0].textContent;
            msg = `부서 내 사원을 ${deptName} 부서로 이동하시겠습니까?`;
        } else if (btn.value === "setPersonalPattern") {
            const patternName = document.getElementById("personalPatternCode").value;
            msg = `부서 내 사원에게 패턴(${patternName})을 지정하시겠습니까?`;
        } else if (btn.value === "changeLeave") {
            msg = "부서 내 사원을 휴직 변경 하시겠습니까?";
        }

        if (msg !== "" && !confirm(msg)) {
            e.preventDefault();
        }
    });

});

//다른 부서로 이동
const moveBtn = document.getElementById('moveDeptBtn');
const moveSelect = document.getElementById('moveDeptSelect');

moveBtn.addEventListener('click', () => {
    if (!hasCheckedEmployees()) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }
    moveSelect.style.display = moveSelect.style.display === 'none' ? 'block' : 'none';
});

// 개별 근태 패턴 지정
const personalBtn = document.getElementById('setPersonalPattern');
const personalSelect = document.getElementById('personalPatternSelect');
const personalEmpInputs = document.getElementById('personalEmpInputs');

personalBtn.addEventListener('click', () => {
    // 체크된 사원 여부 확인
    const checkedEmps = Array.from(document.querySelectorAll('input[name="empCodes"]:checked'))
        .map(cb => cb.value);

    if (checkedEmps.length === 0) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }

    // 기존 hidden input 초기화
    personalEmpInputs.innerHTML = '';

    // 선택된 사원 hidden input 생성
    checkedEmps.forEach(empCode => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'empCodes';
        input.value = empCode;
        personalEmpInputs.appendChild(input);
    });

    personalSelect.style.display = 'block';
});

// 휴직 변경
const changeLeaveBtn = document.getElementById('changeLeave');
const leaveChangeSelect = document.getElementById('leaveChangeSelect');

changeLeaveBtn.addEventListener('click', () => {
    if (!hasCheckedEmployees()) {
        alert("먼저 사원을 선택해주세요.");
        return;
    }

    leaveChangeSelect.style.display =
        leaveChangeSelect.style.display === 'none' ? 'block' : 'none';
});
