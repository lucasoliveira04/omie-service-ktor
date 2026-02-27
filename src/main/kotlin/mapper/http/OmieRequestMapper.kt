package com.omie.mapper.http

import com.omie.dto.messageSQS.receiver.FaturaDto
import com.omie.dto.omieApi.OmieContaReceberDTO
import com.omie.dto.omieApi.OmieParam
import com.omie.dto.omieApi.OmieRequestDto

class OmieRequestMapper(
    private val appKey: String,
    private val appSecret: String
) {
    fun mapFaturasToRequest(
        faturas: List<FaturaDto>,
        loteNumber: Int
    ): OmieRequestDto {

        val contas = faturas.map {

            OmieContaReceberDTO(
                codigo_lancamento_integracao = it.codigoLancamentoIntegracao,
                codigo_cliente_fornecedor = it.codigoCliente,
                data_vencimento = it.dataVencimento,
                valor_documento = it.valor,
                codigo_categoria = it.codigoCategoria,
                data_previsao = it.dataPrevisao,
                id_conta_corrente = it.idContaCorrente
            )
        }

        val param = OmieParam(
            lote = loteNumber,
            conta_receber_cadastro = contas
        )

        return OmieRequestDto(
            call = "IncluirContaReceberPorLote",
            app_key = appKey,
            app_secret = appSecret,
            param = listOf(param)
        )
    }
}