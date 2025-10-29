function toggleNode(element) {
    const children = element.nextElementSibling;
    if (!children) return;

    const icon = element.querySelector('.toggle-icon');

    if (children && children.classList.contains('children')) {
        children.classList.toggle('show');
        icon.classList.toggle('expanded');
    }
}