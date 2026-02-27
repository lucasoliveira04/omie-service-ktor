package com.omie.dto.omieApi

data class OmieRequestDto(
    val call: String,
    val app_key: String,
    val app_secret: String,
    val param: List<OmieParam>
)
data class OmieContaReceberDTO(
    val codigo_lancamento_integracao: String,
    val codigo_cliente_fornecedor: Long,
    val data_vencimento: String,
    val valor_documento: Double,
    val codigo_categoria: String,
    val data_previsao: String,
    val id_conta_corrente: Long,
)
data class OmieParam(
    val lote: Int,
    val conta_receber_cadastro: List<OmieContaReceberDTO>
)