package com.adp.ds.pdfweb

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import akka.actor.{Props, ActorSystem}
import java.io.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.config.annotation.{WebMvcConfigurerAdapter, ResourceHandlerRegistry}

@Configuration
@ComponentScan
@EnableAutoConfiguration
class PDFWebConfiguration extends WebMvcConfigurerAdapter {
  @Bean def getActorSystem = ActorSystem("mysystem")

  @Bean def getSigningRequestsRepository = new SigningRequestsRepository()

  @Autowired
  @Bean def getSigningProcessor(system: ActorSystem) = system.actorOf(Props(new SigningProcessor(new SigningRequestsRepository(), new File("data/testrepository"))))

  override def addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("/assets/**")
      .addResourceLocations("file:/Users/halimf/Documents/work/PDFWeb/data/testrepository/")
  }
}
