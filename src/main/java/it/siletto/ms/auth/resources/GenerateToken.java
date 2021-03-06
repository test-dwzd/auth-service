package it.siletto.ms.auth.resources;

import io.dropwizard.jersey.caching.CacheControl;
import it.siletto.ms.auth.AuthServiceApp;
import it.siletto.ms.auth.dto.TokenResponseDTO;
import it.siletto.ms.auth.service.CypherService;
import it.siletto.ms.base.cors.Cors;
import it.siletto.ms.base.resources.BaseResource;
import it.siletto.ms.identity.model.Identity;
import it.siletto.ms.identity.service.IdentityDAO;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;

@Path("/token")
@Produces(MediaType.APPLICATION_JSON)
public class GenerateToken extends BaseResource {

	protected static final long EXPIRE_WINDOW = 1000 * 60 * 60 * 24;

	@Inject
	protected CypherService cryptService;
	
	@Inject
	protected IdentityDAO identityDao;
	
	@GET
	@Timed
	@Path("generate")
	@CacheControl(noCache = true)
	@Cors
	public TokenResponseDTO generate(@QueryParam("username") String username, @QueryParam("password") String password) throws Exception {
		
		Identity user = identityDao.getUser(username, password, AuthServiceApp.getConfig().getRealm());
		
		TokenResponseDTO ret = new TokenResponseDTO();
		
		if(user == null){
			ret.setStatus(TokenResponseDTO.STATUS_KO);
		}else{
			
			long expire = new Date().getTime() + EXPIRE_WINDOW;
			String token = cryptService.generateToken(AuthServiceApp.getConfig().getPublicKeyFile(), user.getUsername(), user.getRoles(), expire);
			
			ret.setStatus(TokenResponseDTO.STATUS_OK);
			ret.setToken(token);
			ret.setExpires(expire);
		}
		
		return ret;

	}

}
