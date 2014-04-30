package com.adp.ds.pdfweb

import org.slf4j.LoggerFactory
import resource.managed
import org.springframework.boot.SpringApplication
import akka.actor.{Props, ActorSystem}
import java.io.File

object Main {


  val logger = LoggerFactory.getLogger(Main.getClass)


  def main(args: Array[String]) {
    val system = ActorSystem("system")
    val repository = new SigningRequestsRepository
    system.actorOf(Props(new SigningProcessor(repository, new File("data/requests"))))
    SpringApplication.run(new PDFWebConfiguration().getClass, args:_*)
  }
}
