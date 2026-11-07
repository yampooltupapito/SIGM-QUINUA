package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum CategoriaCostoArbitrio {
    COSTO_DIRECTO_MANO_OBRA("Costo Directo: Mano de Obra y Personal Operativo"),
    COSTO_DIRECTO_MATERIALES("Costo Directo: Materiales, Combustible e Insumos"),
    DEPRECIACION_MAQUINARIA_EQUIPOS("Depreciación: Maquinaria, Vehículos y Equipos"),
    COSTO_INDIRECTO_OPERATIVO("Costo Indirecto: Mantenimiento y Servicios Operativos"),
    GASTOS_ADMINISTRATIVOS_GESTION("Gastos Administrativos y Gestión del Servicio");

    private final String descripcion;

    CategoriaCostoArbitrio(String descripcion) {
        this.descripcion = descripcion;
    }
}
