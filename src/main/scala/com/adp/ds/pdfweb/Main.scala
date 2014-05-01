package com.adp.ds.pdfweb

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import akka.actor.{Props, ActorSystem}
import java.io.File

object Main {


  val logger = LoggerFactory.getLogger(Main.getClass)


  def main(args: Array[String]) {
    SpringApplication.run(new PDFWebConfiguration().getClass, args: _*)
  }
}
