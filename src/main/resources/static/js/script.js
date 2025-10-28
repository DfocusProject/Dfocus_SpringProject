window.addEventListener('DOMContentLoaded', () => {
    loadPage('home.html'); // 기본 페이지 로드
    setupDropdowns();      // 드롭다운 초기화
});

// ✅ 사이드바 고정형이므로 아래 함수는 더 이상 사용 안 함
/*
function openNav() {
  document.getElementById("sideBar").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
}
function closeNav() {
  document.getElementById("sideBar").style.width = "0";
  document.getElementById("main").style.marginLeft = "0";
}
*/

// 페이지 로드 함수
function loadPage(page, element = null) {
    const mainContent = document.getElementById('main-content');
    mainContent.style.opacity = '0';

    fetch(page)
        .then(res => {
            if (!res.ok) throw new Error('페이지를 불러올 수 없습니다.');
            return res.text();
        })
        .then(html => {
            setTimeout(() => {
                mainContent.innerHTML = html;
                mainContent.style.opacity = '1';
                if (element) highlightActiveMenu(element); // ✅ 강조처리
            }, 200);
        })
        .catch(err => {
            mainContent.innerHTML = `<p style="color:red;">${err.message}</p>`;
            mainContent.style.opacity = '1';
        });
}

// 드롭다운 설정
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

// 클릭된 메뉴 강조
function highlightActiveMenu(element) {
    document.querySelectorAll('.sidebar a, .dropdown-btn').forEach(el => {
        el.classList.remove('active-menu');
    });
    element.classList.add('active-menu');
}
