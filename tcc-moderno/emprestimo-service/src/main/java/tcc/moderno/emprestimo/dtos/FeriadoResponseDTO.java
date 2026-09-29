package tcc.moderno.emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

public final class FeriadoResponseDTO {

    private final LocalDate date;
    private final String name;
    private final String type;

    @JsonCreator
    public FeriadoResponseDTO(@JsonProperty("date") LocalDate date,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type) {
        this.date = date;
        this.name = name;
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FeriadoResponseDTO that)) return false;
        return Objects.equals(date, that.date)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, name, type);
    }
}
