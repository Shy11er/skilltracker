package net.brekker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "Описывает ошибки при нарушении работы с полями")
public class ValidationErrorResponseDto implements Serializable {
    @Schema(description = "Список ошибок")
    private List<Violation> violations;
}
