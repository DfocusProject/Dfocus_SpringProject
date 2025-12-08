// 부서 트리(조직도) 열고닫기 구조
function toggleNode(element) {
    const children = element.nextElementSibling;
    if (!children) return;

    const icon = element.querySelector('.toggle-icon');

    if (children && children.classList.contains('children')) {
        children.classList.toggle('hide');
        if (icon) {
            icon.classList.toggle('expanded');
        }
    }
}
