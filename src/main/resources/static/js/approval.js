document.addEventListener("DOMContentLoaded", () => {

    // 좌측 row 클릭 → 상세보기
    document.querySelectorAll(".doc-row").forEach(row => {
        row.addEventListener("click", () => {
            const requestId = row.querySelector(".requestId").value;

            document.querySelectorAll(".doc-row").forEach(r => r.classList.remove("active"));
            row.classList.add("active");

            fetch(`/approval/detail/${requestId}`)
                .then(res => res.text())
                .then(html => {
                    document.querySelector("#detail-area").innerHTML = html;
                    attachDetailFormHandlers();
                })
                .catch(err => console.error("DETAIL LOAD ERROR:", err));
        });
    });

    // 탭 처리
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const target = btn.dataset.tab;
            tabContents.forEach(tc => tc.classList.toggle('active', tc.id === target));
        });
    });

    // 일괄 승인/반려
    document.querySelector(".bulk-approval").addEventListener("click", () => bulkAction("approve"));
    document.querySelector(".bulk-rejection").addEventListener("click", () => bulkAction("reject"));

});

// 단건 승인/반려 form 처리
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

// 일괄 승인/반려
function bulkAction(actionType) {
    const activeTab = document.querySelector(".tab-content.active");
    yzyz

    if (checkedRows.length === 0) {
        alert("선택된 문서가 없습니다.");
        return;
    }

    const requestIds = Array.from(checkedRows).map(r => r.closest("tr").querySelector(".requestId").value);
    const reasonSelect = document.querySelector(".bulk-buttons .reason");
    const reason = actionType === "reject" ? reasonSelect.value : null;

    if (actionType === "reject" && (!reason || reason === "")) {
        alert("반려 사유를 선택해주세요.");
        return;
    }

    fetch(`/approval/${actionType}/bulk`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(actionType === "reject" ? { requestIds, reason } : requestIds)
    }).then(() => {
        alert(actionType === "approve" ? "일괄 승인 완료" : "일괄 반려 완료");
        location.reload();
    });
}
