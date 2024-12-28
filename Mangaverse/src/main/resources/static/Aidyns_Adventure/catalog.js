document.addEventListener('DOMContentLoaded', () => {
    const mangaGrid = document.getElementById('manga-grid');

    function fetchAllManga() {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', 'http://localhost:8080/api/manga/all', true);

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

    fetchAllManga();
});