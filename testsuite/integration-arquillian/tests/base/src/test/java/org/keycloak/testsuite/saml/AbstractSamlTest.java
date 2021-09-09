package org.keycloak.testsuite.saml;

import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.testsuite.AbstractAuthTest;
import org.keycloak.testsuite.util.SamlClient;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.keycloak.testsuite.util.Matchers.isSamlResponse;
import static org.keycloak.testsuite.util.ServerURLs.AUTH_SERVER_PORT;
import static org.keycloak.testsuite.util.ServerURLs.AUTH_SERVER_SCHEME;
import static org.keycloak.testsuite.util.ServerURLs.AUTH_SERVER_SSL_REQUIRED;
import static org.keycloak.testsuite.utils.io.IOUtil.loadRealm;

/**
 * @author mhajas
 */
public abstract class AbstractSamlTest extends AbstractAuthTest {
    public static final String REALM_NAME = "demo";
    public static final String REALM_PRIVATE_KEY = "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";
    public static final String REALM_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1TQ+eqetMEggj6Z9svPM6G2UzIY+Buz5H1PMEa7DQy1EPiMHj4HyAQwuYwjE4tUppwVd6LuDgJe+dEWE5vz0rw08NuTYM/C5e/Qhb+X+Ipz8xQVsEWe9h3O3eWJyDMJinrB3i7arzygGArY8q1z+inivYWOKBQzxra+YK5fdgZ1pFIbBVDRFVkDKNZwcJwsmct5oBwvssu5Lj+kat9GPsTxqmlR05n6W//lGh40dQdxaLKLiV0/QMAcYfoHXUmXmS9/vG4GhibfBCAYamnum0ss6h9y3F6VBQNHg1SXID/vl/THPZsSzb+tdNBopIV8nYx7Gbvtnb/NIvT5TbPg1K2iGzFQOhBg7lmZpiFG4vG6WOs9Yxh2buK4zBBDOtG2ufnEgzFbrARQxdnOXxFptlJ3YxGAjHN4e/BMAt7QP24+wnmHVGfHieBLEqyJnuh3chX5ZKlEaiM5KLYuC5IY4XXl4N70IHQUTteIFZN+8fbHqd1saaDmPHDc/N8HT7FgSnpDCbqJv/6ya38OIiKzWywQxHGS0ZyGGWpACv7cqG0iXcoe/16scFTjuVXXKgBTwdE8O6WykcmYVKmLHobesiRmMfeZxLE/6heJJRp/6wH3pHaXWiSveb+RvePKBTiBBaGmr4+bzcU8P61CEFHADG0NIXkSNd3pnUW7bGO5p2xcCAwEAAQ==";
    public static final String REALM_SIGNING_CERTIFICATE = "MIIElzCCAn8CBgFJGQdcCzANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDEwRkZW1vMB4XDTE0MTAxNjEyNTQxM1oXDTI0MTAxNjEyNTU1M1owDzENMAsGA1UEAxMEZGVtbzCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANU0PnqnrTBIII+mfbLzzOhtlMyGPgbs+R9TzBGuw0MtRD4jB4+B8gEMLmMIxOLVKacFXei7g4CXvnRFhOb89K8NPDbk2DPwuXv0IW/l/iKc/MUFbBFnvYdzt3licgzCYp6wd4u2q88oBgK2PKtc/op4r2FjigUM8a2vmCuX3YGdaRSGwVQ0RVZAyjWcHCcLJnLeaAcL7LLuS4/pGrfRj7E8appUdOZ+lv/5RoeNHUHcWiyi4ldP0DAHGH6B11Jl5kvf7xuBoYm3wQgGGpp7ptLLOofctxelQUDR4NUlyA/75f0xz2bEs2/rXTQaKSFfJ2Mexm77Z2/zSL0+U2z4NStohsxUDoQYO5ZmaYhRuLxuljrPWMYdm7iuMwQQzrRtrn5xIMxW6wEUMXZzl8RabZSd2MRgIxzeHvwTALe0D9uPsJ5h1Rnx4ngSxKsiZ7od3IV+WSpRGojOSi2LguSGOF15eDe9CB0FE7XiBWTfvH2x6ndbGmg5jxw3PzfB0+xYEp6Qwm6ib/+smt/DiIis1ssEMRxktGchhlqQAr+3KhtIl3KHv9erHBU47lV1yoAU8HRPDulspHJmFSpix6G3rIkZjH3mcSxP+oXiSUaf+sB96R2l1okr3m/kb3jygU4gQWhpq+Pm83FPD+tQhBRwAxtDSF5EjXd6Z1Fu2xjuadsXAgMBAAEwDQYJKoZIhvcNAQELBQADggIBADh+ZYGKHrY6ZWTo1Hao7JS7O5KT9S0RSJI3Xs7FTrEethvzwHIebxytcVOjOZVuV/WaHDLlcCAdWlDOsN4wozAdBehpQdDgfpAgx06J+j1G9lZgJF+ChVa3rg+qUNobTyuTCYBYjM8vmte+ZWgshMH82aGDyOSM70q0qe+FndBqkVqAig49t3HtBfH+pqxdMEyhhzogMDlja1/WhfWM6YF4PCe7vx/nhxkbqdAv/WMA/8TC8hFa16cG09PZ77y32P6tvlFqfTZuyEpIeKT0nEVOOUnzbknzivX6bWoi/ivjZM04jzg7VLK3XxhmwQaNE3zaNTcA3aSRH4ToZBjWPzv1YMgxv1tHNfiYHvLQjrVXdu8rA/6eJHql/GFLxDDGUyavs/58hy6DdLuLxnn2IOTIWn5B2VcBy9aKfC43zOY7elkHzqa1eLNdSyeGJUH/J6+kIKvlZPbk329StvEyDf214Stmf/12GsjF3UnO0+zqyQiCGTkTzNIrRqe05KfJ9tvBfxDGuPgZWOptY0zEgbBG8Y5os/0UkcLhSQFF9CZ9/siUH1lEulS+Le2gO8fjX/ZyRLrOOxCBwxkr9bL+0EFPoFG6weU9AdRED3EURGUQHXUBxwgMTrG4AQodMz9FOxu+jcm2iqkbahM1iRbFeSk9ikvQQIhb7Ykl9+5+Nvzs";

    public static final String SAML_ASSERTION_CONSUMER_URL_SALES_POST = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/sales-post/saml";
    public static final String SAML_CLIENT_ID_SALES_POST = "http://localhost:8280/sales-post/";

    public static final String SAML_CLIENT_ID_ECP_SP = "http://localhost:8280/ecp-sp/";
    public static final String SAML_ASSERTION_CONSUMER_URL_ECP_SP = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/ecp-sp/saml";

    public static final String SAML_ASSERTION_CONSUMER_URL_SALES_POST2 = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/sales-post2/saml";
    public static final String SAML_CLIENT_ID_SALES_POST2 = "http://localhost:8280/sales-post2/";

    public static final String SAML_URL_SALES_POST_SIG = "http://localhost:8080/sales-post-sig/";
    public static final String SAML_CLIENT_ID_SALES_POST_SIG = "http://localhost:8280/sales-post-sig/";
    public static final String SAML_ASSERTION_CONSUMER_URL_SALES_POST_SIG = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/sales-post-sig/";

    public static final String SAML_CLIENT_ID_SALES_POST_ASSERTION_AND_RESPONSE_SIG = "http://localhost:8280/sales-post-assertion-and-response-sig/";
    public static final String SAML_ASSERTION_CONSUMER_URL_SALES_POST_ASSERTION_AND_RESPONSE_SIG = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/sales-post-assertion-and-response-sig/";

    public static final String SAML_CLIENT_SALES_POST_SIG_PRIVATE_KEY = "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";
    public static final String SAML_CLIENT_SALES_POST_SIG_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjEbuLYH0pkdaRtkhBp41PgpLl/GOge9sF7u6IqOqn9iWojg/GhFzk7YflaGJUY5J2kIiaoH1oRNy7PqmMIGZLUsl/zVPIQLVH7yVNLAzLpnxtQqidVdVkO8xeNan5WTE+NgAfoUELSSBXn/tFnmT4NnahnOngzD+vH3h6tOaheI6OXbCln4OTcw/tJgyaWJE05vLfcQKbdd4uPCykDmDDqgEgTUHZ+d1kZpqsQ2q/4Ry04wrYjMwglNTdd8wc8xRvjJNkZ9AGMOcWRV2KadB6PwoVJNl9fxp2JVID+xZIvj9nTNVE8296pUStWNVvT23WKt1SasmkpPaZQa1WLCSHF+dyZVSgL+XH9w22MQaGgywp3Vef9Hstw2SXnEz2317vXbbvvw3IPBt5VSTOVJI/AOIF1yHHvzGlExUPrB4onfppwPlt1O2bObxzv86Py335aXVPI/WvFleBy9Io5ZPZLyL/r+AeLD9i4cwEoHg9cogw/OgoJXN85SuVVpH/SVgtVmbHjn1hiAraooc0WWa/1ysYmUurVlEl7SnICbNiBq6+sibB4D6GyHRA5sEeRUEcNDJdg+8PWROxIN/zwrROCVg9piGHkSkFTgidLvY8w0XnmZ5ppY0opcbH0PfDnaxZK+Yq3ouMEB4vNk1rxvkjqJ2+QU01KoPuIOZUSreG+cCAwEAAQ==";
    public static final PrivateKey SAML_CLIENT_SALES_POST_SIG_PRIVATE_KEY_PK;
    public static final PublicKey SAML_CLIENT_SALES_POST_SIG_PUBLIC_KEY_PK;

    static {
        try {
            KeyFactory kfRsa = KeyFactory.getInstance("RSA");
            SAML_CLIENT_SALES_POST_SIG_PRIVATE_KEY_PK = kfRsa.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(SAML_CLIENT_SALES_POST_SIG_PRIVATE_KEY)));
            SAML_CLIENT_SALES_POST_SIG_PUBLIC_KEY_PK = kfRsa.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(SAML_CLIENT_SALES_POST_SIG_PUBLIC_KEY)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    // Set date to past; then: openssl req -x509 -newkey rsa:1024 -keyout key.pem -out cert.pem -days 1 -nodes -subj '/CN=http:\/\/localhost:8080\/sales-post-sig\/'
    public static final String SAML_CLIENT_SALES_POST_SIG_EXPIRED_PRIVATE_KEY = "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";
    public static final String SAML_CLIENT_SALES_POST_SIG_EXPIRED_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAi1edzzOyMoKuUIiRmvarv51G651NkAlB18tGwnibjeqDPI3cfUFtamiz5xMUd24GaqaDgLw05hn44fjEZuqGDKcQYXArc1LLlyc5Qkc1aPzTB7wu1lJvG6vntCGMrm2jbQ6sjClDeMpZLQ/YqWGMWDvD89LqB8neBoXXMaBq+j1fG9eFzYfSa3dlbarpUnFPAjSMos4WvU2p49QcoeQ9+v1l0AAvOFxswBSzMARFLAFMNwolDr95EeEqTzR1yatkig4553qsMGb7whKK8BwngbeFRSKf8KyI0NalKhiNqNrncS8Batyv95VLqWvLq9WZ1MuQNybXEN4abNOkNXxIWceK055yh+y9W/3sdwmMCIWNALSQk05aGe8cwY6wrG6B901ZqsxXgn1GcUQ/3jUfIIqZR4eVnUnd0Rr9fQroMnghye01dTfwGY/J9DodNtQEeVj7Tc8oOn+XyXgEScBqbdQULbt1ZJuIQ2AQkz+iT3pNTc6icX8ntxXvI37ldzVwAxJcMd2lJTxg9LXDCK3OX3wEBXPuUZ6lWq1dve0SKAx83v/Oq1mrxHOpVaakYEIYLb+xocOoBId+SiRNq343sU8sl7Fmvj24fvw2gOiIUjs0YqEpTSnh223TDDEoVtih5DqufRCwlj6iZ/P5jM+4zj1R9nUmKBB8geF0VjHrVYcCAwEAAQ==";
    public static final String SAML_CLIENT_SALES_POST_SIG_EXPIRED_CERTIFICATE = "MIIE3DCCAsQCCQD5Ys1ttDYXujANBgkqhkiG9w0BAQsFADAwMS4wLAYDVQQDDCVodHRwOi8vbG9jYWxob3N0OjgwODAvc2FsZXMtcG9zdC1zaWcvMB4XDTE2MDgyOTA4NTIzM1oXDTE2MDgzMDA4NTIzM1owMDEuMCwGA1UEAwwlaHR0cDovL2xvY2FsaG9zdDo4MDgwL3NhbGVzLXBvc3Qtc2lnLzCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAItXnc8zsjKCrlCIkZr2q7+dRuudTZAJQdfLRsJ4m43qgzyN3H1BbWpos+cTFHduBmqmg4C8NOYZ+OH4xGbqhgynEGFwK3NSy5cnOUJHNWj80we8LtZSbxur57QhjK5to20OrIwpQ3jKWS0P2KlhjFg7w/PS6gfJ3gaF1zGgavo9XxvXhc2H0mt3ZW2q6VJxTwI0jKLOFr1NqePUHKHkPfr9ZdAALzhcbMAUszAERSwBTDcKJQ6/eRHhKk80dcmrZIoOOed6rDBm+8ISivAcJ4G3hUUin/CsiNDWpSoYjaja53EvAWrcr/eVS6lry6vVmdTLkDcm1xDeGmzTpDV8SFnHitOecofsvVv97HcJjAiFjQC0kJNOWhnvHMGOsKxugfdNWarMV4J9RnFEP941HyCKmUeHlZ1J3dEa/X0K6DJ4IcntNXU38BmPyfQ6HTbUBHlY+03PKDp/l8l4BEnAam3UFC27dWSbiENgEJM/ok96TU3OonF/J7cV7yN+5Xc1cAMSXDHdpSU8YPS1wwitzl98BAVz7lGepVqtXb3tEigMfN7/zqtZq8RzqVWmpGBCGC2/saHDqASHfkokTat+N7FPLJexZr49uH78NoDoiFI7NGKhKU0p4dtt0wwxKFbYoeQ6rn0QsJY+omfz+YzPuM49UfZ1JigQfIHhdFYx61WHAgMBAAEwDQYJKoZIhvcNAQELBQADggIBABFUtDezjdLW5wEmerESThzGKNDol55OI+pttCe+YCrjAn0Z5p/RieDyeUz3dnSMuCx0nQKj67f5uo6F73En2j+2vg6y+LiheLIkuLf3patwJTRIu8NmuaMoBVlioOHX2Fb5685EYY5TokSQU7PrUC2XTEfPXcWbFRvLmn16PdrwIjWcbjlNqo6Vg/CbdBHibeA5gddMuB858Vwz7hT1XCrVIcyr/Ye05GKcBXnzikD3IMZa9U3SfHqH9totMsTWodXROcQNnYsyV7y92DsuHXXcaKnsp6BaPrAtuMfQ/fAIWtkLUQOHqMAsg4Uu2UNiLBl4nioP9qs2vIcTlKEgvwJKb3bDuYrbwxaXeR1DmEEKhJipSGS5movmCyT6md39oRwY46X1jDKbk+aKCalVNo0NpOs+PI4jF5cj8NV+PkKzXuBJClfsDgU9qO24ZKtGXHGitb/P4Hb4kVveS+p1Stc9CpnU0mhMp3ATMeTDKlitQ0k2Uzwq2V7IMORzjr0ejPkE+gqURE7yIKnyDwomKRmmmMkjHVpdn5XMg/SIRbzUaaars8wBXygM8ByjAauRjSV4UfFP/0MNy3pNrPivMRCH0buy9ew4KkFI7W3KHrIE6gLH/L2JMRnsh22aAu8pw5BwE1xQYi3W+EztYU+HjeE3nNPlvxkwUSrLpA1XsCJ0";

    public static final String SAML_ASSERTION_CONSUMER_URL_SALES_POST_ENC =  AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/sales-post-enc/saml";
    public static final String SAML_CLIENT_ID_SALES_POST_ENC = "http://localhost:8280/sales-post-enc/";
    public static final String SAML_CLIENT_SALES_POST_ENC_PRIVATE_KEY = "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";
    public static final String SAML_CLIENT_SALES_POST_ENC_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA4lqbOuec+11V6HkeiavS6cyUIDo/5A05PGbtaFmgG7CPhXjS8bJrAePsH+GRbTofC4P8g98pbD7cKvy7cuTPYVCl6as8hzZmHsZm/VZeq2qTJDgHwMrSG/IlCPm0j2zEQ+dVKeIi+nXN+y26foup8SbkeGBt5XpnlZuNzo3WSwCuzKED2Rw+0kvvvC+Cn8NSE5Vdzsfe3uF+jWJBN27usBAVEA6ROnQUHqYfjn7vRWnfMqDelvA0J4agNLADekK/mSlB5W7ICf0ziDup15js78nPB7GUDTGXtYDB6fGmB5s+MXGsVaR+jQbNqPmm25Gfiw0MZdjxUdEsRKo8f7WYbOV3pbSxzpymC3VKEYcJn8PopkCRs/uCodEoex5PRwREGOhTTXRzQn6vpRpco6p3zyZqtScEjCS6yB556erGY0edLZ9Lptfjmq4AtzbZM2FIUy08BgtRYRvEn75SNNyinwYmZkiNIrEitD3TbkvvtYI1iNj3WoA5XuzKyCW6Udg/nNj7UBQVB1G75o1TiZiGn5uomTAJXOpavL/dLd5HrvGtfffo58BMBjftddKjY7yuzrVG0F09wHVwwgkdHAozVMc/sEYILd0MFDqGqWyhFV6EbVA+BA41vn5zJYrpR1UHG6RbdyJtu7IDSueJbfIsk9ulHVnZQhn8Rpp8zc8A+V8CAwEAAQ==";

    public static final String SAML_CLIENT_ID_EMPLOYEE_2 = "http://localhost:8280/employee2/";
    public static final String SAML_CLIENT_ID_EMPLOYEE_SIG = "http://localhost:8280/employee-sig/";

    public static final String SAML_BROKER_ALIAS = "saml-broker";

    protected final AtomicReference<NameIDType> nameIdRef = new AtomicReference<>();
    protected final AtomicReference<String> sessionIndexRef = new AtomicReference<>();

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        testRealms.add(loadRealm("/adapter-test/keycloak-saml/testsaml.json"));
    }

    @Override
    protected boolean modifyRealmForSSL() {
        return true;
    }

    protected AuthnRequestType createLoginRequestDocument(String issuer, String assertionConsumerURL, String realmName) {
        return SamlClient.createLoginRequestDocument(issuer, assertionConsumerURL, getAuthServerSamlEndpoint(realmName));
    }

    protected URI getAuthServerSamlEndpoint(String realm) throws IllegalArgumentException, UriBuilderException {
        return RealmsResource
                .protocolUrl(UriBuilder.fromUri(getAuthServerRoot()))
                .build(realm, SamlProtocol.LOGIN_PROTOCOL);
    }

    protected URI getAuthServerBrokerSamlEndpoint(String realm, String identityProviderAlias) throws IllegalArgumentException, UriBuilderException {
        return RealmsResource
                .realmBaseUrl(UriBuilder.fromUri(getAuthServerRoot()))
                .path("broker/{idp-name}/endpoint")
                .build(realm, identityProviderAlias);
    }

    protected URI getAuthServerRealmBase(String realm) throws IllegalArgumentException, UriBuilderException {
        return RealmsResource
                .realmBaseUrl(UriBuilder.fromUri(getAuthServerRoot()))
                .build(realm);
    }

    protected URI getSamlBrokerUrl(String realmName) {
        return URI.create(getAuthServerRealmBase(realmName).toString() + "/broker/" + SAML_BROKER_ALIAS + "/endpoint");
    }

    protected SAML2Object extractNameIdAndSessionIndexAndTerminate(SAML2Object so) {
        assertThat(so, isSamlResponse(JBossSAMLURIConstants.STATUS_SUCCESS));
        ResponseType loginResp1 = (ResponseType) so;
        final AssertionType firstAssertion = loginResp1.getAssertions().get(0).getAssertion();
        assertThat(firstAssertion, org.hamcrest.Matchers.notNullValue());
        assertThat(firstAssertion.getSubject().getSubType().getBaseID(), instanceOf(NameIDType.class));

        NameIDType nameId = (NameIDType) firstAssertion.getSubject().getSubType().getBaseID();
        AuthnStatementType firstAssertionStatement = (AuthnStatementType) firstAssertion.getStatements().iterator().next();

        nameIdRef.set(nameId);
        sessionIndexRef.set(firstAssertionStatement.getSessionIndex());

        return null;
    }
}
