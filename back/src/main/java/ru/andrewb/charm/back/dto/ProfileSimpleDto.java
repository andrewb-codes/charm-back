package ru.andrewb.charm.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Short profile DTO used in recommendations")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileSimpleDto {
    @ToString.Include
    @Schema(description = "Profile id", example = "2")
    Long id;

    @ToString.Include
    @Schema(description = "Name", example = "Elena")
    String name;

    @ToString.Include
    @Schema(description = "Surname", example = "Sidorova")
    String surname;

    @ToString.Include
    @Schema(description = "Calculated age", example = "26")
    Integer age;

    @Schema(description = "Profile description", example = "I am Java Dev")
    String about;

    @Schema(description = "Stored profile photo filename", example = "avatar.jpg")
    String photo;
}
