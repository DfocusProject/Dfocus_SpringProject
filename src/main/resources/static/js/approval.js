// public/js/approval.js

document.addEventListener("DOMContentLoaded", () => {

    // 모든 문서 row 클릭 이벤트 부여
    document.querySelectorAll(".doc-row").forEach(row => {
        row.addEventListener("click", () => {
            const requestId = row.querySelector(".requestId").value;

            // 선택 강조
            document.querySelectorAll(".doc-row").forEach(r => r.classList.remove("active"));
            row.classList.add("active");

            // AJAX 로 상세 조회
            fetch(`/approval/detail/${requestId}`)
                .then(res => res.text())
                .then(html => {
                    document.querySelector("#detail-area").innerHTML = html;
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

            tabContents.forEach(tc => {
                tc.classList.toggle('active', tc.id === target);
            });
        });
    });

});
