package com.adp.ds.pdfweb
import org.slf4j.LoggerFactory
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import java.io.{FileWriter, FileOutputStream, File}
import resource.managed
import scala.collection.JavaConversions._
import org.apache.pdfbox.util.ImageIOUtil

object Main extends App {
  implicit def toFile(path:String) = new File(path)

  val logger = LoggerFactory.getLogger(Main.getClass)
  val pdfFile = "testFile.pdf"
  logger.debug(s"Reading file ${pdfFile.getAbsolutePath}")

  val document = PDDocument.load(pdfFile)
  for((obj, idx) <- document.getDocumentCatalog.getAllPages zip Range(1,Int.MaxValue)){
    val page = obj.asInstanceOf[PDPage]
    val baseName = s"${pdfFile.getAbsolutePath.getParent}/${pdfFile.getName.split('.')(0)}$idx"
    writeImage(page, baseName)
    writeHtml(page, baseName)
  }

  def writeHtml(page: PDPage, baseName: String) {
    val scale = 96.0 / 72.0
    val width = page.getMediaBox.getWidth * scale
    val height = page.getMediaBox.getHeight * scale
    logger.debug(s"Writing to file $baseName.html")
    for (writer <- managed(new FileWriter(s"$baseName.html"))) {
      writer.write(
        s"""<html><head></head><body>
           <img src="${baseName.getName}.png" style="width:${width}px;height:${height}px" />
           </body></html>
        """.stripMargin)
    }
  }

  def writeImage(page: PDPage, baseName: String) {
    val img = page.convertToImage
    val outFile = s"$baseName.png"
    logger.debug(s"Writing to file $outFile")
    for (os <- managed(new FileOutputStream(outFile))) {
      ImageIOUtil.writeImage(img, "png", os)
    }
  }
}