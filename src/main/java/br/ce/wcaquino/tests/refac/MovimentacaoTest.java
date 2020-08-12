package br.ce.wcaquino.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.text.DecimalFormat;
import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.Movimentacao;
import br.ce.wcaquino.utils.BarrigaUtils;
import br.ce.wcaquino.utils.DateUtils;

public class MovimentacaoTest extends BaseTest{
		
	@Test
	public void deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		DecimalFormat df =  new DecimalFormat();
	    df.setMinimumFractionDigits(2);
		
		//criaçao de uma movimentaçao
		given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
//		.log().all()
			.statusCode(201)
			.body("conta_id", is(movimentacao.getConta_id()))
			.body("descricao", is(movimentacao.getDescricao()))
			.body("tipo", is(movimentacao.getTipo()))
			.body("envolvido", is(movimentacao.getEnvolvido()))
			.body("valor", is((df.format(movimentacao.getValor())).toString().replace(',', '.')))
			.body("status", is(movimentacao.getStatus()))
		;
	}
	
	@Test
	public void naoDeveCadastrarMovimentacaoFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao(DateUtils.getDataDiferencaDias(2));
		
		//criaçao de uma movimentaçao
		given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
//		.log().all()
			.statusCode(400)
			.body("$", hasSize(1))      //quantos erros vieram  
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
	}
	
	@Test
	public void deveValidarCamposObrigatoriosNaMovimentacao() {
		
		given()
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(8))         //$ raiz
//				.body("msg.size()", is(8))
			.body("msg", hasItems("Data da Movimentação é obrigatório",
								  "Data do pagamento é obrigatório",
								  "Descrição é obrigatório",
								  "Interessado é obrigatório",
								  "Valor é obrigatório",
								  "Valor deve ser um número",
								  "Conta é obrigatório",
								  "Situação é obrigatório"))
		;
	}
	
	@Test
	public void naoDeveRemoverContaComMovimentacao() {
		
		Integer CONTA_ID = BarrigaUtils.getIdDaContaPeloNome("Conta com movimentacao");
		
		//deletar uma conta
		given()
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("name", is("error"))    //devolve só 1 item e nao uma lista
			.body("constraint", is("transacoes_conta_id_foreign"))
			.body("detail", is("Key (id)=("+CONTA_ID+") is still referenced from table \"transacoes\"."))
		;
	}
	
	@Test
	public void deveRemoverUmaMovimentacao() {
		
		Integer MOV_ID = BarrigaUtils.getIdDaMovimentacaoPelaDescricao("Movimentacao para exclusao");
		
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
	}

	private Movimentacao getMovimentacaoValida() {
		
		Movimentacao movimentacao = new Movimentacao();
//		movimentacao.setId(id);
		movimentacao.setConta_id(BarrigaUtils.getIdDaContaPeloNome("Conta para movimentacoes"));
//		movimentacao.setUsuario_id(usuario_id);
		movimentacao.setDescricao("Esmola");
		movimentacao.setTipo("REC");
		movimentacao.setEnvolvido("Seu Madruga");
		movimentacao.setValor(10.00f);
		movimentacao.setData_transacao(DateUtils.getDataDiferencaDias(-1));
		movimentacao.setData_pagamento(DateUtils.getDataDiferencaDias(5));
		movimentacao.setStatus(true);
		
		return movimentacao;
	}
	

}
