document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');
    const scrollLeftBtn = document.getElementById('scroll-left');
    const scrollRightBtn = document.getElementById('scroll-right');
    const genreMangaGrid = document.getElementById('genre-manga-grid');
    const actionCategoryBtn = document.getElementById('action-category-btn');
    const romanceCategoryBtn = document.getElementById('romance-category-btn');
    const comedyCategoryBtn = document.getElementById('comedy-category-btn');
    const fantasyCategoryBtn = document.getElementById('fantasy-category-btn');

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

// Добавляем обработчики событий для кнопок категорий
    actionCategoryBtn.addEventListener('click', () => {
        fetchGenreManga('Action');
    });

    romanceCategoryBtn.addEventListener('click', () => {
        fetchGenreManga('Romance');
    });

    fantasyCategoryBtn.addEventListener('click', () => {
        fetchGenreManga('Fantasy');
    });

    comedyCategoryBtn.addEventListener('click', () => {
        fetchGenreManga('Comedy');
    });

    ajaxUtils.sendGetRequest = function (requestUrl, responseHandler) {
        var request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            handleResponse(request, responseHandler);
        };
        request.open("GET", requestUrl, true);
        request.send(null); // for POST only
    };


    // Scroll functionality
    scrollLeftBtn.addEventListener('click', () => {
        mangaGrid.scrollBy({left: -300, behavior: 'smooth'});
    });

    scrollRightBtn.addEventListener('click', () => {
    mangaGrid.scrollBy({left: 300, behavior: 'smooth'});
    });
});
