# Mangaverse

Mangaverse is a Spring Boot application for managing a collection of manga. It provides RESTful APIs for adding, updating, deleting, and retrieving manga information.

---

## Features

- **Add New Manga**: Add new manga entries to the database.
- **Update Manga**: Edit existing manga details.
- **Delete Manga**: Remove manga entries.
- **Retrieve Manga by ID**: Get detailed information about specific manga.
- **Search Manga**: Search for manga by title, author, genre, or description.
- **Popular Manga**: Fetch manga based on their ratings.
- **Manga by Genre**: Retrieve manga grouped by genres.

---

## Technologies Used

- **Java**
- **Spring Boot**
- **Spring Data JDBC**
- **PostgreSQL**
- **Maven**

---

## Getting Started

### Prerequisites

Ensure you have the following installed on your system:

- **Java**: Version 17 or higher
- **Maven**: Version 3.6.0 or higher
- **PostgreSQL**

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Arnatik76/mangaverse.git
   cd mangaverse
   ```

2. Set up the PostgreSQL database:
   ```sql
   CREATE DATABASE mangaverse;
   ```

3. Update the `application.properties` file with your PostgreSQL credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mangaverse
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

---

## API Endpoints

### Add New Manga

- **URL**: `/api/manga/new`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "genre": "Shonen",
    "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
    "rating": 4.5,
    "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
  }
  ```
- **Response**:
  ```json
  {
    "id": 1,
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "genre": "Shonen",
    "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
    "rating": 4.5,
    "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
  }
  ```

### Update Manga

- **URL**: `/api/manga/update`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "id": 1,
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "genre": "Shonen",
    "description": "Updated description",
    "rating": 4.6,
    "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
  }
  ```
- **Response**:
  ```json
  {
    "id": 1,
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "genre": "Shonen",
    "description": "Updated description",
    "rating": 4.6,
    "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
  }
  ```

### Delete Manga

- **URL**: `/api/manga/delete/{mangaId}`
- **Method**: `DELETE`
- **Response**: `204 No Content`

### Get Manga by ID

- **URL**: `/api/manga/{id}`
- **Method**: `GET`
- **Response**:
  ```json
  {
    "id": 1,
    "title": "One Piece",
    "author": "Eiichiro Oda",
    "genre": "Shonen",
    "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
    "rating": 4.5,
    "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
  }
  ```

### Search Manga

- **URL**: `/api/manga/search`
- **Method**: `GET`
- **Query Parameters**: `query`
- **Response**:
  ```json
  [
    {
      "id": 1,
      "title": "One Piece",
      "author": "Eiichiro Oda",
      "genre": "Shonen",
      "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
      "rating": 4.5,
      "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
    }
  ]
  ```

### Get Popular Manga

- **URL**: `/api/manga/popular`
- **Method**: `GET`
- **Response**:
  ```json
  [
    {
      "id": 1,
      "title": "One Piece",
      "author": "Eiichiro Oda",
      "genre": "Shonen",
      "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
      "rating": 4.5,
      "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
    }
  ]
  ```

### Get Manga by Genre

- **URL**: `/api/manga/genre/{genre}`
- **Method**: `GET`
- **Response**:
  ```json
  [
    {
      "id": 1,
      "title": "One Piece",
      "author": "Eiichiro Oda",
      "genre": "Shonen",
      "description": "Monkey D. Luffy is a young pirate with a dream: to prove the legend true and be the Pirate King.",
      "rating": 4.5,
      "imageUrl": "http://localhost:8080/images/one_piece_cover_page.jpg"
    }
  ]
  ```

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

