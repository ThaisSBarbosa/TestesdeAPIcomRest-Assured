package br.ce.waquino.rest.tests;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;

public class BarrigaTest extends BaseTest {
	
	private String TOKEN;
	
	@Before
	public void login() {
		
		Map<String, String> login = new HashMap<String, String>();
		login.put("email", "thais.barbosa@atomicsolutions.com.br");
		login.put("senha", "Proton");
				
		//login na api e extraçao do token
		TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token");
	}
	
	@Test
	public void naoDeveAcessarAPISemToken() {
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	
	@Test
	public void deveIncluirContaComSucesso() {
		
		//criaçao de uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT é bearer
			.body("{\"nome\": \"Nova Conta via API\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is("Nova Conta via API"))
		;
	}
	
	@Test
	public void deveAlterarContaComSucesso() {
				
		//alterar uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT é bearer
			.body("{\"nome\": \"Nova Conta via API Nome Alteradoo\"}")
		.when()
			.put("/contas/233449")
		.then()
			.statusCode(200)
			.body("nome", is("Nova Conta via API Nome Alteradoo"))
		;
	}
	
	@Test
	public void naoDeveIncluirContaComNomeRepetido() {
		
		//criaçao de uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT é bearer
			.body("{\"nome\": \"Nova Conta via API Nome Alteradoo\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}
	
	@Test
	public void deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		
		//criaçao de uma movimentaçao
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
		.log().all()
			.statusCode(201)
		;
	}
	
	@Test
	public void deveValidarCamposObrigatoriosNaMovimentacao() {
		
		given()
			.header("Authorization", "JWT " + TOKEN)
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
	public void naoDeveCadastrarMovimentacaoFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao("23/04/2021");
		
		//criaçao de uma movimentaçao
		given()
			.header("Authorization", "JWT " + TOKEN)
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
	public void naoDeveRemoverContaComMovimentacao() {
				
		//deletar uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.delete("/contas/233449")
		.then()
			.statusCode(500)
			.body("name", is("error"))    //devolve só 1 item e nao uma lista
			.body("constraint", is("transacoes_conta_id_foreign"))
			.body("detail", is("Key (id)=(233449) is still referenced from table \"transacoes\"."))
		;
	}
	
	@Test
	public void deveCalcularSaldoDasContas() {
				
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == 233449}.saldo", is("40.00"))
		;
	}
	
	@Test
	public void deveRemoverUmaMovimentacao() {
				
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.delete("/transacoes/208883")
		.then()
			.statusCode(204)
		;
	}
	
	private Movimentacao getMovimentacaoValida() {
		
		Movimentacao movimentacao = new Movimentacao();
//		movimentacao.setId(id);
		movimentacao.setConta_id(233449);
//		movimentacao.setUsuario_id(usuario_id);
		movimentacao.setDescricao("Esmola");
		movimentacao.setTipo("REC");
		movimentacao.setEnvolvido("Seu Madruga");
		movimentacao.setValor(10.00f);
		movimentacao.setData_transacao("10/07/2020");
		movimentacao.setData_pagamento("12/07/2020");
		movimentacao.setStatus(true);
		
		return movimentacao;
	}
	
}
