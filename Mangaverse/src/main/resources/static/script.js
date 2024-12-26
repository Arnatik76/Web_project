document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');
    const scrollLeftBtn = document.getElementById('scroll-left');
    const scrollRightBtn = document.getElementById('scroll-right');

    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://localhost:8080/api/manga/all', true);
    xhr.onreadystatechange = () => {
        if (xhr.readyState === 4 && xhr.status === 200) {
            const data = JSON.parse(xhr.responseText);
            data.forEach(manga => {
                const card = document.createElement('div');
                card.classList.add('manga-card');
                card.innerHTML = `
                    <img src="${manga.imageUrl}" alt="${manga.title}">
                    <h3>${manga.title}</h3>
                    <a href="#" class="btn">Read Now</a>
                `;
                mangaGrid.appendChild(card);
            });
        } else if (xhr.readyState === 4) {
            console.error('Error fetching manga:', xhr.status, xhr.statusText);
        }
    };
    xhr.send();

    // Scroll functionality
    scrollLeftBtn.addEventListener('click', () => {
    mangaGrid.scrollBy({left: -300, behavior: 'smooth'});
    });

    scrollRightBtn.addEventListener('click', () => {
    mangaGrid.scrollBy({left: 300, behavior: 'smooth'});
    });
});
