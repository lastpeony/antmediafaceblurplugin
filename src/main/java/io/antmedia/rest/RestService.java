package io.antmedia.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.antmedia.plugin.BlurTechnique;
import io.antmedia.plugin.Utils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import io.antmedia.plugin.TensorflowPlugin;
import io.antmedia.rest.model.Result;
import io.swagger.annotations.ApiParam;

@Component
@Path("/v2/tensorflow")
public class RestService {

	@Context
	protected ServletContext servletContext;

	/*
	 * Start object detection for the given stream id
	 */
	@POST
	@Path("/{streamId}/start")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response start(@PathParam("streamId") String streamId, 
			@ApiParam(value = "To create image files instead of realtime demonstrations.", required = false) @QueryParam("offline") boolean offline,
						  @ApiParam(value = "Bluring technique used. 0 for Convolution or 1 for Gaussian blur.", required = false, defaultValue = "0") @QueryParam("blurTechnique") int blurTechnique,
						  @ApiParam(value = "Strength of bluring.", required = false, defaultValue = "1") @QueryParam("blurFactor") int blurFactor) {
		TensorflowPlugin app = getPluginApp();
		boolean result = false;
		if(blurTechnique == BlurTechnique.CONVOLUTION_BLUR.ordinal()){
			result = app.startDetection(streamId, !offline, BlurTechnique.CONVOLUTION_BLUR, Utils.getBlurFactor(blurFactor));
		}else{
			nu.pattern.OpenCV.loadLocally();
			result = app.startDetection(streamId, !offline, BlurTechnique.GAUSSIAN_BLUR, Utils.getBlurFactor(blurFactor));
		}

		return Response.status(Status.OK).entity(new Result(result)).build();
	}
		
	/*
	 * Stop object detection for the given stream id
	 */
	@POST
	@Path("/{streamId}/stop")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stop(@PathParam("streamId") String streamId) {
		TensorflowPlugin app = getPluginApp();
		boolean result = app.stopDetection(streamId);

		return Response.status(Status.OK).entity(new Result(result)).build();
	}
	
	private TensorflowPlugin getPluginApp() {
		ApplicationContext appCtx = (ApplicationContext) servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return (TensorflowPlugin) appCtx.getBean("plugin.tensorflow");
	}
}
