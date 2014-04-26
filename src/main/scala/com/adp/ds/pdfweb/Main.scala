package com.adp.ds.pdfweb
import org.slf4j.LoggerFactory
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import java.io.{FileOutputStream, File}
import resource.managed
import scala.collection.JavaConversions._
import org.apache.pdfbox.util.ImageIOUtil

object Main extends App {
  val logger = LoggerFactory.getLogger("chapters.introduction.HelloWorld1")
  //val pdfFile = new File("c:/temp/bills.pdf")
  val pdfFile = new File("c:/temp/Used Motor Vehicle Llimited Warranty.pdf")
  logger.debug(s"Converting file ${pdfFile.getAbsolutePath}")

  val document = PDDocument.load(pdfFile)
  for((obj, idx) <- document.getDocumentCatalog.getAllPages zip Range(1,Int.MaxValue)){
    val page = obj.asInstanceOf[PDPage]
    val img = page.convertToImage
    val outFile = new File(s"${pdfFile.getAbsolutePath}${idx}.png")
    logger.debug(s"Writing to file ${outFile.getAbsolutePath}")
    for(os <- managed(new FileOutputStream(outFile))){
      ImageIOUtil.writeImage(img, "png", os)
    }
  }
  println("Done")
}