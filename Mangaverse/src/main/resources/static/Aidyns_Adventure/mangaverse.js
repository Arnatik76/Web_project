document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');
    const genreMangaGrid = document.getElementById('genre-manga-grid');
    const scrollLeftBtn = document.getElementById('scroll-left');
    const scrollRightBtn = document.getElementById('scroll-right');

    async function fetchPopularManga() {
        try {
            const response = await fetch('http://localhost:8080/api/manga/popular');

            if (!response.ok) {
                throw new Error(`Error fetching manga: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();

            // Populate manga grid
            data.forEach((manga) => {
                const card = document.createElement('div');
                card.classList.add('manga-card');
                card.innerHTML = `
                    <img src="${manga.imageUrl}" alt="${manga.title}">
                    <h3>${manga.title}</h3>
                    <a href="#" class="btn">Read Now</a>
                `;
                mangaGrid.appendChild(card);
            });
        } catch (error) {
            console.error(error);
        }
    }

    async function fetchActionGenre() {
        try {
            const response = await fetch('http://localhost:8080/api/manga/genre?genre=Action');

            if (!response.ok) {
                throw new Error(`Error fetching manga: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();

            // Populate manga grid
            data.forEach((manga) => {
                const genreCard = document.createElement('div');
                genreCard.classList.add('genre-manga-card');
                genreCard.innerHTML = `
                    <img src="${manga.imageUrl}" alt="${manga.title}">
                    <h3>${manga.title}</h3>
                    <a href="#" class="btn">Read Now</a>
                `;
                genreMangaGrid.appendChild(genreCard);
            });
        } catch (error) {
            console.error(error);
        }
    }

    // Fetch and display manga on page load
    fetchPopularManga();
    fetchActionGenre()

    // Scroll functionality
    scrollLeftBtn.addEventListener('click', () => {
        mangaGrid.scrollBy({left: -300, behavior: 'smooth'});
    });

    scrollRightBtn.addEventListener('click', () => {
        mangaGrid.scrollBy({left: 300, behavior: 'smooth'});
    });
});