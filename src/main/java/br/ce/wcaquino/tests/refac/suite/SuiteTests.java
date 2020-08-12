package br.ce.wcaquino.tests.refac.suite;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.tests.refac.AuthTest;
import br.ce.wcaquino.tests.refac.ContasTest;
import br.ce.wcaquino.tests.refac.MovimentacaoTest;
import br.ce.wcaquino.tests.refac.SaldoTest;
import io.restassured.RestAssured;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	//executa nesta ordem
	ContasTest.class,
	MovimentacaoTest.class,
	SaldoTest.class,
	AuthTest.class
})
public class SuiteTests extends BaseTest{
	
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
		RestAssured.get("/reset").then().statusCode(200);  //resetar toda vez que executa
	}

}
