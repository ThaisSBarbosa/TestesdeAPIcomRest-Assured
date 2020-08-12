package br.ce.wcaquino.rest.tests;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.utils.DateUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {
	
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
	@BeforeClass
	public static void login() {
		
		Map<String, String> login = new HashMap<String, String>();
		login.put("email", "thais.barbosa@atomicsolutions.com.br");
		login.put("senha", "Proton");
				
		//login na api e extraçao do token
		String TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token");
		
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
	}
	
	@Test
	public void t01_deveIncluirContaComSucesso() {
		
		//criaçao de uma conta
		CONTA_ID = given()
			.body("{\"nome\": \""+CONTA_NAME+"\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is(CONTA_NAME))
			.extract().path("id");
		;
	}
	
	@Test
	public void t02_deveAlterarContaComSucesso() {
				
		//alterar uma conta
		given()
			.body("{\"nome\": \""+CONTA_NAME+" Alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome", is(CONTA_NAME+" Alterada"))
		;
	}
	
	@Test
	public void t03_naoDeveIncluirContaComNomeRepetido() {
		
		//criaçao de uma conta
		given()
			.body("{\"nome\": \""+CONTA_NAME+" Alterada\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}
	
	@Test
	public void t04_deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		
		//criaçao de uma movimentaçao
		MOV_ID = given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
		.log().all()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
	@Test
	public void t05_deveValidarCamposObrigatoriosNaMovimentacao() {
		
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
	public void t06_naoDeveCadastrarMovimentacaoFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao(DateUtils.getDataDiferencaDias(2));
		
		//criaçao de uma movimentaçao
		given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
		.log().all()
			.statusCode(400)
			.body("$", hasSize(1))      //quantos erros vieram  
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
	}
	
	@Test
	public void t07_naoDeveRemoverContaComMovimentacao() {
				
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
	public void t08_deveCalcularSaldoDasContas() {
				
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("10.00"))
		;
	}
	
	@Test
	public void t09_deveRemoverUmaMovimentacao() {
				
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
	}
	
	@Test
	public void t10_naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	
	private Movimentacao getMovimentacaoValida() {
		
		Movimentacao movimentacao = new Movimentacao();
//		movimentacao.setId(id);
		movimentacao.setConta_id(CONTA_ID);
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
