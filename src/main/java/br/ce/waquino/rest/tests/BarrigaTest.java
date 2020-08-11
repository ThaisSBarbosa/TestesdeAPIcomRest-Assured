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
				
		//login na api e extra�ao do token
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
		
		//cria�ao de uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT � bearer
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
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT � bearer
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
		
		//cria�ao de uma conta
		given()
			.header("Authorization", "JWT " + TOKEN)         // forma de enviar o token// nas apis mais recentes em vez de JWT � bearer
			.body("{\"nome\": \"Nova Conta via API Nome Alteradoo\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("J� existe uma conta com esse nome!"))
		;
	}
	
	@Test
	public void deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		
		//cria�ao de uma movimenta�ao
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
			.body("msg", hasItems("Data da Movimenta��o � obrigat�rio",
								  "Data do pagamento � obrigat�rio",
								  "Descri��o � obrigat�rio",
								  "Interessado � obrigat�rio",
								  "Valor � obrigat�rio",
								  "Valor deve ser um n�mero",
								  "Conta � obrigat�rio",
								  "Situa��o � obrigat�rio"))
		;
	}
	
	@Test
	public void naoDeveCadastrarMovimentacaoFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao("23/04/2021");
		
		//cria�ao de uma movimenta�ao
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
		.log().all()
			.statusCode(400)
			.body("$", hasSize(1))      //quantos erros vieram  
			.body("msg", hasItem("Data da Movimenta��o deve ser menor ou igual � data atual"))
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
			.body("name", is("error"))    //devolve s� 1 item e nao uma lista
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
