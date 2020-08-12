package br.ce.wcaquino.utils;

import io.restassured.RestAssured;

public class BarrigaUtils {
	
	public static Integer getIdDaContaPeloNome(String nome) {
		return RestAssured.get("/contas?nome="+nome).then().extract().path("id[0]");
	}
	
	public static Integer getIdDaMovimentacaoPelaDescricao(String descricao) {
		return RestAssured.get("/transacoes?descricao="+descricao).then().extract().path("id[0]");
	}
}
