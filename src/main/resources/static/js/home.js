// 현재 시간 업데이트
function updateTime() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    document.getElementById('currentTime').textContent = `${hours}:${minutes}:${seconds}`;
}

updateTime();
setInterval(updateTime, 1000);

// 출근 처리
function checkIn() {
    const now = new Date();
    const time = now.getHours().toString().padStart(2, '0') + ':' +
        now.getMinutes().toString().padStart(2, '0');

    if (confirm(time + '에 출근 처리하시겠습니까?')) {
        // 서버 요청
        // fetch('/api/attendance/checkin', { method: 'POST' })
        //     .then(response => response.json())
        //     .then(data => { location.reload(); });
        alert('출근 처리되었습니다.');
        location.reload();
    }
}

// 퇴근 처리
function checkOut() {
    const now = new Date();
    const time = now.getHours().toString().padStart(2, '0') + ':' +
        now.getMinutes().toString().padStart(2, '0');

    if (confirm(time + '에 퇴근 처리하시겠습니까?')) {
        // 서버 요청
        // fetch('/api/attendance/checkout', { method: 'POST' })
        //     .then(response => response.json())
        //     .then(data => { location.reload(); });
        alert('퇴근 처리되었습니다. 수고하셨습니다!');
        location.reload();
    }
}

// 할일 체크박스 토글
function toggleTask(checkbox) {
    if (checkbox.style.background) {
        checkbox.style.background = '';
        checkbox.style.border = '2px solid #bdc3c7';
        checkbox.parentElement.style.opacity = '1';
    } else {
        checkbox.style.background = '#27ae60';
        checkbox.style.border = '2px solid #27ae60';
        checkbox.parentElement.style.opacity = '0.6';

        // 서버에 완료 상태 전송
        // const taskId = checkbox.parentElement.dataset.taskId;
        // fetch(`/api/tasks/${taskId}/complete`, { method: 'POST' });
    }
}
