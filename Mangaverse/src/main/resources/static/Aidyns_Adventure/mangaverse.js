document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');
    const genreMangaGrid = document.getElementById('genre-manga-grid');
    const scrollLeftBtn = document.getElementById('scroll-left');
    const scrollRightBtn = document.getElementById('scroll-right');

    const actionBtn = document.getElementById('action-btn');
    const romanceBtn = document.getElementById('romance-btn');
    const comedyBtn = document.getElementById('comedy-btn');
    const fantasyBtn = document.getElementById('fantasy-btn');

    console.log(document.getElementById('genre-manga-grid'));


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

    fetchPopularManga();

    async function fetchGenreManga(genre) {
        try {
            genreMangaGrid.innerHTML = '';

            const response = await fetch(`http://localhost:8080/api/manga/genre?genre=${genre}`);

            if (!response.ok) {
                throw new Error(`Error fetching manga: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();

            data.forEach((manga) => {
                const card = document.createElement('div');
                card.classList.add('genre-manga-card');
                card.innerHTML = `
                    <img src="${manga.imageUrl}" alt="${manga.title}">
                    <h3>${manga.title}</h3>
                    <a href="#" class="btn">Read Now</a>
                `;
                genreMangaGrid.appendChild(card);
            });
        } catch (error) {
            console.error(error);
        }
    }

    actionBtn.addEventListener('click', () => {
        fetchGenreManga('Action');
    });

    romanceBtn.addEventListener('click', () => {
        fetchGenreManga('Romance');
    });

    comedyBtn.addEventListener('click', () => {
        fetchGenreManga('Comedy');
    });

    fantasyBtn.addEventListener('click', () => {
        fetchGenreManga('Fantasy');
    });


    // Scroll functionality
    scrollLeftBtn.addEventListener('click', () => {
        mangaGrid.scrollBy({left: -300, behavior: 'smooth'});
    });

    scrollRightBtn.addEventListener('click', () => {
        mangaGrid.scrollBy({left: 300, behavior: 'smooth'});
    });
});