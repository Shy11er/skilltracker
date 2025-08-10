package net.brekker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Описывает ошибку с указанием поля и текста ошибки")
public class Violation implements Serializable {
    @Schema(description = "Ошибочное поле")
    private String fieldName;

    @Schema(description = "Описание ошибки")
    private String message;
}

