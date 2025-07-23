package br.com.planejeisofwarehouse.communitycenterapi.Application.Enum

enum class ResourceType(val points: Int) {
    MEDICO(4),
    VOLUNTARIO(3),
    SUPRIMENTOS_MEDICOS(7),
    VEICULO_DE_TRANSPORTE (5),
    CESTA_BASICA(2);

    companion object fromString {
        fun fromString(string: String): ResourceType? {
            return entries.find { it.name.equals(string, ignoreCase = true) }
        }
    }
}