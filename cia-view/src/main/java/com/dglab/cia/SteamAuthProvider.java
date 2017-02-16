package com.dglab.cia;

import org.apache.commons.lang3.StringUtils;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author doc
 */
@Controller
public class SteamAuthProvider implements AuthenticationProvider {
    private static final String OPENID_IDENTIFIER = "openid_identifier";
    private static ConsumerManager manager = null;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }

    static {
        manager = new ConsumerManager();
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/login")
    public String steamLogin(HttpServletRequest httpReq, HttpServletResponse httpResp) throws Exception {
        String returnToUrl = "http://127.0.0.1/openid-verify";
        List<DiscoveryInformation> discoveries = manager.discover("http://steamcommunity.com/openid");
        DiscoveryInformation discovered = manager.associate(discoveries);
        httpReq.getSession().setAttribute("openid-disc", discovered);

        AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

        return "redirect:" + authReq.getDestinationUrl(true);
    }

    @GetMapping("/openid-verify")
    public String steamVerify(HttpServletRequest httpReq) throws Exception {
        ParameterList parameterList = new ParameterList(httpReq.getParameterMap());
        DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute("openid-disc");

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = httpReq.getRequestURL();
        String queryString = httpReq.getQueryString();

        if (StringUtils.isNotEmpty(queryString))
            receivingURL.append("?").append(httpReq.getQueryString());

        VerificationResult verification = manager.verify(receivingURL.toString(), parameterList, discovered);
        Identifier verified = verification.getVerifiedId();

        if (verified != null) {
            Authentication authentication = new PreAuthenticatedAuthenticationToken(
                    verified.getIdentifier(),
                    "",
                    AuthorityUtils.createAuthorityList("ROLE_USER")
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        return "redirect:/tournament";
    }

}
