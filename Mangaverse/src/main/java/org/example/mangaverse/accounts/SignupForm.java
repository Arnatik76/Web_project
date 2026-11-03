package org.example.mangaverse.accounts;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sign-up form. A JavaBean (not a record) because th:field binds through getters and setters. */
public class SignupForm {

    @NotBlank(message = "Как к вам обращаться?")
    @Size(max = 60, message = "Не длиннее 60 символов")
    private String name = "";

    @NotBlank(message = "Нужен email")
    @Email(message = "Похоже, в адресе опечатка")
    @Size(max = 254, message = "Слишком длинный адрес")
    private String email = "";

    @NotBlank(message = "Придумайте пароль")
    @Size(min = 8, max = 72, message = "Пароль — от 8 до 72 символов")
    private String password = "";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
