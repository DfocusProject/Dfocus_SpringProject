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

