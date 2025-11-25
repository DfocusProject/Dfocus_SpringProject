document.addEventListener("DOMContentLoaded", () => {

    // 좌측 상세보기 클릭
    document.addEventListener("click", function (e) {
        const row = e.target.closest(".doc-row");
        if (row) {
            const requestIdInput = row.querySelector(".requestId");
            if (!requestIdInput) return;
            const requestId = requestIdInput.value;

            loadDetail(requestId);

            document.querySelectorAll(".doc-row").forEach(r => r.classList.remove("active"));
            row.classList.add("active");
        }
    });

    // 탭 전환
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const target = btn.dataset.tab;
            document.querySelectorAll(".tab-content").forEach(tc =>
                tc.classList.toggle('active', tc.id === target)
            );

            updateActionsVisibility();
            document.querySelector("#detail-area").innerHTML =
                '<div class="empty-msg">문서를 선택하면 상세내용이 표시됩니다.</div>';
        });
    });

    // 체크박스 클릭
    document.addEventListener("change", function(e) {
        const tab1 = document.querySelector("#tab1");
        if (!tab1) return;

        // 전체 체크박스 클릭
        if (e.target.id === "checkAll1") {
            const checked = e.target.checked;
            tab1.querySelectorAll(".rowCheck").forEach(chk => chk.checked = checked);
        }

        // row 체크박스 클릭 → 전체 체크박스 상태 갱신
        if (e.target.classList.contains("rowCheck")) {
            const all = tab1.querySelectorAll(".rowCheck");
            const allChecked = Array.from(all).every(c => c.checked);
            const checkAll = document.querySelector("#checkAll1");
            if (checkAll) checkAll.checked = allChecked;
        }
    });

    // 단일/일괄 승인/반려
    document.addEventListener("click", function(e) {

        // 일괄 승인
        if (e.target.classList.contains("bulk-approval")) {
            const ids = getCheckedrequestId();
            if (ids.length === 0) { alert("체크된 문서가 없습니다."); return; }
            sendApprovalRequest("approve", ids);
        }

        // 일괄 반려
        if (e.target.classList.contains("bulk-rejection")) {
            const ids = getCheckedrequestId();
            if (ids.length === 0) { alert("체크된 문서가 없습니다."); return; }

            const reasonSelect = document.querySelector(".bulk-buttons .reason");
            if (!reasonSelect || !reasonSelect.value) { alert("반려 사유를 선택하세요."); return; }

            sendApprovalRequest("reject", ids, reasonSelect.value);
        }
    });

});

// 상세 로드
function loadDetail(requestId) {
    fetch(`/approval/detail/${requestId}`)
        .then(res => res.text())
        .then(html => {
            document.querySelector("#detail-area").innerHTML = html;

            attachDetailFormHandlers();
            updateActionsVisibility();
            hideActionsIfNoTabs();
        })
        .catch(err => console.error("DETAIL LOAD ERROR:", err));
}

// 단일 승인/반려 처리
function attachDetailFormHandlers() {
    const approveForm = document.querySelector("#detail-area form[action$='/approve']");
    const rejectForm = document.querySelector("#detail-area form[action$='/reject']");

    if (approveForm && !approveForm.dataset.bound) {
        approveForm.dataset.bound = true;
        approveForm.addEventListener("submit", function(e) {
            e.preventDefault();
            const requestId = approveForm.querySelector("input[name='requestId']").value;
            sendApprovalRequest("approve", [requestId]);  // 배열로 전달
        });
    }

    if (rejectForm && !rejectForm.dataset.bound) {
        rejectForm.dataset.bound = true;
        rejectForm.addEventListener("submit", function(e) {
            e.preventDefault();
            const requestId = rejectForm.querySelector("input[name='requestId']").value;
            const reasonInput = rejectForm.querySelector("input[name='reason']");
            if (!reasonInput || !reasonInput.value) { alert("반려 사유를 입력하세요."); return; }

            sendApprovalRequest("reject", [requestId], reasonInput.value);  // 배열로 전달
        });
    }
}

// 체크된 requestId 가져오기
function getCheckedrequestId() {
    const tab1 = document.querySelector("#tab1");
    if (!tab1) return [];
    return [...tab1.querySelectorAll(".rowCheck:checked")].map(chk => chk.value);
}

// form POST로 단일/일괄 승인/반려 처리
function sendApprovalRequest(type, requestId, reason) {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = `/approval/${type}`;  // approve / reject

    requestId.forEach(id => {
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = "requestId";
        input.value = id;
        form.appendChild(input);
    });

    if (reason) {
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = "reason";
        input.value = reason;
        form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
}

// 탭에 따라 actions(승인/반려 버튼 영역) 숨김
function updateActionsVisibility() {
    const activeTab = document.querySelector(".tab-btn.active");
    const actions = document.querySelector("#detail-area .actions");
    if (!activeTab || !actions) return;
    actions.style.display = activeTab.dataset.tab === "tab1" ? "block" : "none";
}

// 페이지에 탭이 없는 경우(detail만 표시하는 페이지)
function hideActionsIfNoTabs() {
    const hasTabs = document.querySelectorAll(".tab-btn").length > 0;
    const actions = document.querySelector("#detail-area .actions");
    if (!hasTabs && actions) actions.style.display = "none";
}
