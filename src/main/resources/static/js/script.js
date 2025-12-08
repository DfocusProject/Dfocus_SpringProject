window.addEventListener('DOMContentLoaded', () => {
    setupDropdowns();
});

function setupDropdowns() {
    const dropdowns = document.getElementsByClassName("dropdown-btn");
    for (let i = 0; i < dropdowns.length; i++) {
        dropdowns[i].addEventListener("click", function() {
            this.classList.toggle("active");
            const content = this.nextElementSibling;
            content.style.display = content.style.display === "block" ? "none" : "block";
        });
    }
}
window.addEventListener('beforeunload', function () {
    try {
        if (navigator.sendBeacon) {
            navigator.sendBeacon('/clearSearchSession'); // POST 요청으로 서버 세션 삭제
        }
    } catch (e) {
        // 서버가 없거나 오류 발생 시 무시
        console.log('Session clear request failed:', e);
    }
});

