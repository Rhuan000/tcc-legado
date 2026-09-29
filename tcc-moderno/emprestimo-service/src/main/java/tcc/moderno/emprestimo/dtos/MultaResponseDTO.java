package tcc.moderno.emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class MultaResponseDTO {

    private final double valor;

    @JsonCreator
    public MultaResponseDTO(@JsonProperty("valor") double valor) {
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MultaResponseDTO that)) return false;
        return Double.compare(valor, that.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
