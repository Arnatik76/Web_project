# Thinking


# Searching


# Responding



# MangaVerse Frontend

## Overview

**MangaVerse** is a frontend project designed to provide users with an interactive platform to explore and read manga. The application features a responsive design, dynamic content loading, and category-based filtering. Users can browse popular manga, explore genres, and interact with a visually appealing interface.

This project is built using **HTML**, **CSS**, and **JavaScript**, and it communicates with a backend API to fetch manga data dynamically.

---

## Features

- **Dynamic Manga Catalog**: Displays a list of popular manga fetched from the backend.
- **Genre-Based Filtering**: Allows users to filter manga by genres such as Action, Romance, Fantasy, and Comedy.
- **Smooth Scrolling**: Horizontal scrolling for manga lists with smooth animations.
- **Responsive Design**: Optimized for various screen sizes.
- **Interactive UI**: Buttons and navigation links for seamless user interaction.
- **Social Links**: Footer includes links to social media profiles of the creators.

---

## Project Structure

```
MangaVerse/
├── index.html       # Main HTML file
├── styles.css       # CSS file for styling
├── script.js        # JavaScript file for functionality
├── images/          # Folder for images (e.g., icons, manga covers)
└── favicon.ico      # Favicon for the website
```

---

## Installation and Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/mangaverse.git
   ```

2. Navigate to the project directory:
   ```bash
   cd mangaverse
   ```

3. Open the `index.html` file in your browser to view the application:
   ```bash
   open index.html
   ```

4. Ensure the backend API is running on `http://localhost:8080` for dynamic content to load.

---

## Usage

### Popular Manga
- The homepage displays a list of popular manga fetched from the backend API.
- Use the left (`←`) and right (`→`) scroll buttons to navigate through the manga list.

### Genre Filtering
- Click on any genre button (e.g., Action, Romance, Fantasy, Comedy) to fetch and display manga specific to that genre.

### Footer
- The footer contains social media links and credits for the creators.

---

## API Integration

The frontend communicates with the backend API to fetch manga data. Below are the API endpoints used:

1. **Fetch All Manga**:
   - **Endpoint**: `GET http://localhost:8080/api/manga/all`
   - **Description**: Fetches a list of all popular manga.
   - **Response**: JSON array of manga objects.

2. **Fetch Manga by Genre**:
   - **Endpoint**: `GET http://localhost:8080/api/manga/genre?genre=<genre>`
   - **Description**: Fetches manga filtered by the specified genre.
   - **Response**: JSON array of manga objects.

---

## Technologies Used

- **HTML5**: For structuring the web page.
- **CSS3**: For styling and layout.
- **JavaScript (ES6)**: For dynamic content and interactivity.
- **XMLHttpRequest & Fetch API**: For making API calls to the backend.

---

## How It Works

1. **Dynamic Content Loading**:
   - On page load, the script fetches popular manga from the backend and dynamically generates cards for each manga.

2. **Genre Filtering**:
   - When a genre button is clicked, the script sends a request to the backend to fetch manga of the selected genre and updates the UI accordingly.

3. **Smooth Scrolling**:
   - Horizontal scrolling for the manga grid is implemented using JavaScript with smooth behavior.

4. **Error Handling**:
   - If the API request fails, an error message is logged in the console.

---

## Future Enhancements

- Add a search bar for users to search manga by title.
- Implement user authentication for personalized features.
- Add a "Read Later" or "Favorites" feature.
- Improve error handling and display user-friendly error messages.
- Enhance responsiveness for smaller devices.

---

## Credits

- **Creators**:
  - Jalgas Arnat
  - Almassov Aidyn
- **Social Links**:
  - [Instagram - Jalgas Arnat](https://www.instagram.com/zhalgas_arnat/)
  - [Instagram - Almassov Aidyn](https://www.instagram.com/keshegiai/)

---

## License

This project is licensed under the MIT License. Feel free to use, modify, and distribute it as per the license terms.
