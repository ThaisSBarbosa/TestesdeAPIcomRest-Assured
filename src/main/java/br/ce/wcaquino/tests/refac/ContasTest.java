package br.ce.wcaquino.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.utils.BarrigaUtils;

public class ContasTest extends BaseTest{
	
	@Test
	public void deveIncluirContaComSucesso() {
		
		//criaçao de uma conta
		given()
			.body("{\"nome\": \"Conta inserida\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is("Conta inserida"))
		;
	}
	
	@Test
	public void deveAlterarContaComSucesso() {

		Integer CONTA_ID = BarrigaUtils.getIdDaContaPeloNome("Conta para alterar");
		
		//alterar uma conta
		given()
			.body("{\"nome\": \"Conta alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome", is("Conta alterada"))
		;
	}
	
	@Test
	public void naoDeveIncluirContaComNomeRepetido() {
		
		//criaçao de uma conta
		given()
			.body("{\"nome\": \"Conta mesmo nome\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}
}
