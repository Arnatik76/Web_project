document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');
    const scrollLeftBtn = document.getElementById('scroll-left');
    const scrollRightBtn = document.getElementById('scroll-right');

    const genreMangaGrid = document.getElementById('genre-manga-grid');
    const actionBtn = document.getElementById('action-btn');
    const romanceBtn = document.getElementById('romance-btn');
    const comedyBtn = document.getElementById('comedy-btn');
    const fantasyBtn = document.getElementById('fantasy-btn');

    console.log(document.getElementById('genre-manga-grid'));


    function fetchPopularManga() {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', 'http://localhost:8080/api/manga/popular', true);

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) { // Запрос завершён
                if (xhr.status === 200) { // Успешный ответ
                    try {
                        const data = JSON.parse(xhr.responseText);

                        // Populate manga grid
                        data.forEach((manga) => {
                            const card = document.createElement('div');
                            card.classList.add('manga-card');
                            card.innerHTML = `
                            <img src="${manga.imageUrl}" alt="${manga.title}">
                            <a href="#" class="btn">${manga.title}</a>
                        `;
                            mangaGrid.appendChild(card);
                        });
                    } catch (error) {
                        console.error('Ошибка при парсинге JSON:', error);
                    }
                } else {
                    console.error(`Ошибка при получении манги: ${xhr.status} ${xhr.statusText}`);
                }
            }
        };

        xhr.onerror = function () {
            console.error('Произошла ошибка при выполнении запроса.');
        };

        xhr.send();
    }


    fetchPopularManga();

    function fetchGenreManga(genre) {
        genreMangaGrid.innerHTML = ''; // Очищаем содержимое перед загрузкой

        const xhr = new XMLHttpRequest();
        xhr.open('GET', `http://localhost:8080/api/manga/genre?genre=${encodeURIComponent(genre)}`, true);

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) { // Запрос завершён
                if (xhr.status === 200) { // Успешный ответ
                    try {
                        const data = JSON.parse(xhr.responseText);

                        data.forEach((manga) => {
                            const card = document.createElement('div');
                            card.classList.add('genre-manga-card');
                            card.innerHTML = `          
                            <img src="${manga.imageUrl}" alt="${manga.title}">
                            <a href="#" class="btn">${manga.title}</a>
                            <div class="info1">
                                <p>| author: ${manga.author}</p>
                            </div>
                            <div class="info2">
                                <p>${manga.description}</p>
                            </div>
                        `;
                            genreMangaGrid.appendChild(card);
                        });
                    } catch (error) {
                        console.error('Ошибка при парсинге JSON:', error);
                    }
                } else {
                    console.error(`Ошибка при получении манги: ${xhr.status} ${xhr.statusText}`);
                }
            }
        };

        xhr.onerror = function () {
            console.error('Произошла ошибка при выполнении запроса.');
        };

        xhr.send();
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

document.querySelectorAll('.info2').forEach(info2 => {
    const originalText = info2.dataset.title;
    if (originalText.length > 250) {
        info2.textContent = originalText.substring(0, 250) + '...';
    }
});