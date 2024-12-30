package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^\\S+$")
    private String login;
    private String name;
    @Past
    @NotNull
    private LocalDate birthday;
    @JsonIgnore
    private Set<Integer> friends = new HashSet<>();
    public boolean checkEmail() {
        return email.matches(
                "^[\\w-.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$"
        );
    }
    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }
}
