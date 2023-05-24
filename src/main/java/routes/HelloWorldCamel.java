package routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import jakarta.enterprise.context.ApplicationScoped;
import process.ConvertDbToCsv;
import process.LinesAggStrategy;
import process.TrimHeaderAndTrailer;

@ApplicationScoped
public class HelloWorldCamel extends RouteBuilder{

    @Override
    public void configure() throws Exception {

        restConfiguration()
        .port(8080)
        .bindingMode(RestBindingMode.auto);

        rest("/boomi/utilities/v1")
        .produces("text/plain")
        .get("/version").to("direct:version")
        .post("/dbtocsv").type(String.class).to("direct:convertDBtoCSVProfile");

        from("direct:convertDBtoCSVProfile")
        .routeId("DB_PROFILE_TO_CSV_PROFILE")
        .process(new TrimHeaderAndTrailer())
        .log(LoggingLevel.DEBUG,"Data after Trim: ${body}")
        .split(body().tokenize("#"), new LinesAggStrategy())
        .process(new ConvertDbToCsv())
        .end()
        .log(LoggingLevel.INFO,"Data: ${body}"); 

        from("direct:version")
        .routeId("VERSION_ROUTE")
        .setBody(constant("version is 1.0"))
        .log(LoggingLevel.INFO,"get version route: ${body}");

 
    }
    
}
