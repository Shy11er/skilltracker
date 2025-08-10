package net.brekker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Описывает возвращаемый результат если получена ошибка")
public class ExceptionResponseDto implements Serializable {
    @Schema(description = "Текст ошибки")
    private String message;
}
