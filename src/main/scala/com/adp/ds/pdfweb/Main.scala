package com.adp.ds.pdfweb

import org.slf4j.LoggerFactory
import resource.managed
import org.springframework.boot.SpringApplication

object Main {


  val logger = LoggerFactory.getLogger(Main.getClass)


  def main(args: Array[String]) {
    SpringApplication.run(new PDFWebConfiguration().getClass, args:_*)
  }


}
