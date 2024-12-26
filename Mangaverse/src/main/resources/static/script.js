let currentSlide = 0;

document.addEventListener('DOMContentLoaded', () => {
    fetch('http://localhost:8080/api/manga/all')
        .then(response => response.json())
        .then(data => {
            const slidesContainer = document.getElementById('slides');
            data.forEach(manga => {
                const slide = document.createElement('div');
                slide.className = 'slide';
                slide.innerHTML = `<img src="${manga.imageUrl}" alt="${manga.title}">`;
                slidesContainer.appendChild(slide);
            });
        });
});

function moveSlide(direction) {
    const slides = document.querySelectorAll('.slide');
    currentSlide = (currentSlide + direction + slides.length) % slides.length;
    const offset = -currentSlide * 100;
    document.getElementById('slides').style.transform = `translateX(${offset}%)`;
}