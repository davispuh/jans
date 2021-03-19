/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.as.server.ws.rs;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.jans.as.model.common.IntrospectionResponse;
import io.jans.as.model.uma.UmaTestUtil;
import io.jans.as.model.uma.wrapper.Token;
import io.jans.as.server.BaseTest;
import io.jans.as.server.model.uma.TUma;
import io.jans.as.server.util.ServerUtil;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 17/09/2013
 */

public class IntrospectionWebServiceEmbeddedTest extends BaseTest {

	@ArquillianResource
	private URI url;

	private static Token authorization;
	private static Token tokenToIntrospect;

	@Test
	@Parameters({ "authorizePath", "tokenPath", "umaUserId", "umaUserSecret", "umaPatClientId", "umaPatClientSecret",
			"umaRedirectUri" })
	public void requestAuthorization(String authorizePath, String tokenPath, String umaUserId, String umaUserSecret,
			String umaPatClientId, String umaPatClientSecret, String umaRedirectUri) {
		authorization = TUma.requestPat(url, authorizePath, tokenPath, umaUserId, umaUserSecret, umaPatClientId,
				umaPatClientSecret, umaRedirectUri);
		UmaTestUtil.assert_(authorization);
	}

	@Test(dependsOnMethods = "requestAuthorization")
	@Parameters({ "authorizePath", "tokenPath", "umaUserId", "umaUserSecret", "umaPatClientId", "umaPatClientSecret",
			"umaRedirectUri" })
	public void requestTokenToIntrospect(String authorizePath, String tokenPath, String umaUserId, String umaUserSecret,
			String umaPatClientId, String umaPatClientSecret, String umaRedirectUri) {
		tokenToIntrospect = TUma.requestPat(url, authorizePath, tokenPath, umaUserId, umaUserSecret, umaPatClientId,
				umaPatClientSecret, umaRedirectUri);
		UmaTestUtil.assert_(tokenToIntrospect);
	}

	@Test(dependsOnMethods = "requestTokenToIntrospect")
	@Parameters({ "introspectionPath" })
	public void introspection(final String introspectionPath) throws Exception {
		Builder request = ResteasyClientBuilder.newClient().target(url.toString() + introspectionPath).request();

		request.header("Accept", "application/json");
		request.header("Authorization", "Bearer " + authorization.getAccessToken());
		Response response = request.post(Entity.form(new Form("token", tokenToIntrospect.getAccessToken())));

		String entity = response.readEntity(String.class);
		showResponse("introspection", response, entity);

		assertEquals(response.getStatus(), 200);
		try {
			final IntrospectionResponse t = ServerUtil.createJsonMapper().readValue(entity,
					IntrospectionResponse.class);
			assertTrue(t != null && t.isActive());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
