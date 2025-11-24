document.addEventListener("DOMContentLoaded", () => {

    // 좌측 row 클릭 → 상세보기
    document.addEventListener("click", function (e) {
        const row = e.target.closest(".doc-row");
        if (row) {
            const requestId = row.querySelector(".requestId").value;
            loadDetail(requestId);

            document.querySelectorAll(".doc-row").forEach(r => r.classList.remove("active"));
            row.classList.add("active");
        }
    });

    // 탭 처리
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {

            document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            const target = btn.dataset.tab;
            document.querySelectorAll(".tab-content").forEach(tc =>
                tc.classList.toggle('active', tc.id === target)
            );

            // 탭에 따라 actions 숨기기/보이기
            updateActionsVisibility();

            // 상세 초기화
            document.querySelector("#detail-area").innerHTML =
                '<div class="empty-msg">문서를 선택하면 상세내용이 표시됩니다.</div>';
        });
    });

});


// 상세 로드 Ajax
function loadDetail(requestId) {
    fetch(`/approval/detail/${requestId}`)
        .then(res => res.text())
        .then(html => {
            document.querySelector("#detail-area").innerHTML = html;

            attachDetailFormHandlers();
            updateActionsVisibility(); /* 상세 로드된 후에 반드시 실행해야 정상 작동함 */
            hideActionsIfNoTabs();
        })
        .catch(err => console.error("DETAIL LOAD ERROR:", err));
}


// 승인/반려 단건 처리
function attachDetailFormHandlers() {
    const approveForm = document.querySelector("#detail-area form[action$='/approve']");
    const rejectForm = document.querySelector("#detail-area form[action$='/reject']");

    if (approveForm) {
        approveForm.addEventListener("submit", e => {
            e.preventDefault();
            const requestId = approveForm.querySelector("[name='requestId']").value;

            fetch("/approval/approve", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `requestId=${requestId}`
            }).then(() => {
                alert("승인 완료");
                location.reload();
            });
        });
    }

    if (rejectForm) {
        rejectForm.addEventListener("submit", e => {
            e.preventDefault();
            const requestId = rejectForm.querySelector("[name='requestId']").value;
            const reason = rejectForm.querySelector("[name='reason']").value;

            fetch("/approval/reject", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `requestId=${requestId}&reason=${encodeURIComponent(reason)}`
            }).then(() => {
                alert("반려 완료");
                location.reload();
            });
        });
    }
}


// 탭에 따라 actions(승인/반려 버튼 영역) 숨기기
function updateActionsVisibility() {
    const activeTab = document.querySelector(".tab-btn.active");
    const actions = document.querySelector("#detail-area .actions");

    if (!activeTab || !actions) return;

    if (activeTab.dataset.tab === "tab1") {
        actions.style.display = "block";  // 결재할 문서
    } else {
        actions.style.display = "none";   // 승인됨 / 반려됨
    }
}

// 페이지에 탭이 없는 경우(detail만 표시하는 페이지)
function hideActionsIfNoTabs() {
    const hasTabs = document.querySelectorAll(".tab-btn").length > 0;
    const actions = document.querySelector("#detail-area .actions");

    if (!hasTabs && actions) {
        actions.style.display = "none";   // 승인/반려 버튼 숨김
    }
}
