package com.adp.ds.pdfweb
import org.slf4j.{Logger, LoggerFactory}
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import java.io.File
import scala.collection.JavaConversions._
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

object Main extends App {
  val logger = LoggerFactory.getLogger("chapters.introduction.HelloWorld1")
  val pdfName = "c:/temp/Used Motor Vehicle Llimited Warranty.pdf"
  logger.debug(s"Converting file ${pdfName}")

  val document = PDDocument.load(new File(pdfName))
  for((obj, idx) <- document.getDocumentCatalog.getAllPages zip Range(1,Int.MaxValue)){
    val page = obj.asInstanceOf[PDPage]
    val img = page.convertToImage
    val fileName = s"c:/temp/image${idx}.png"
    logger.debug(s"Writing to file ${fileName}")
    ImageIO.write(img, "png", new File(fileName))
  }
  println("Done")
}