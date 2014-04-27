package com.adp.ds.pdfweb

import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import java.awt.Color
import java.io.{File, FileOutputStream, FileWriter}
import org.apache.pdfbox.util.ImageIOUtil
import org.slf4j.LoggerFactory
import resource.managed
import scala.collection.JavaConversions._
import com.google.gson.Gson
import java.awt.image.BufferedImage
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg

class PDFUtilities(pdfFile:String) {
  val logger = LoggerFactory.getLogger(classOf[PDFUtilities])

  implicit def toFile(path: String) = new File(path)

  val scale = 96.0f / 72.0f
  logger.debug(s"Reading file ${pdfFile.getAbsolutePath}")
  val document = PDDocument.load(pdfFile)

  def close() {
    document.close()
  }
  def processSigningPDF(signatures: Array[SigningBlock]) {
      addBlocks(signatures)
      val baseName = s"${pdfFile.getAbsolutePath.getParent}${File.separator}${pdfFile.getName.split('.')(0)}"
      writeHtml(baseName)
      for ((page, idx) <- document.getDocumentCatalog.getAllPages.map(_.asInstanceOf[PDPage]) zip Range(1, Int.MaxValue)) {
        val pageBaseName = s"$baseName$idx"
        writeImage(page, pageBaseName)
      }
  }

  def addBlocks(signatures: Seq[SigningBlock]) = {
    for (signature <- signatures) {
      val page = document.getDocumentCatalog.getAllPages()(signature.page - 1).asInstanceOf[PDPage]
      val height = page.getMediaBox.getHeight
      for (cs <- managed(new PDPageContentStream(document, page, true, true))) {
        cs.setStrokingColor(Color.red)
        cs.addRect(signature.xLocation, height - signature.height - signature.yLocation, signature.width, signature.height)
        cs.stroke()
        cs.beginText()
        cs.moveTextPositionByAmount(100, 100)
        cs.setNonStrokingColor(Color.black)
        cs.drawString("Hello, world")
        cs.endText()
      }
    }
    document
  }

  def writeHtml(baseName: String) {
    val fileName = s"$baseName.html"
    logger.debug(s"Writing to file $fileName")
    for (writer <- managed(new FileWriter(s"$fileName"))) {
      writer.write("<html><head></head><body>")
      for ((page, idx) <- document.getDocumentCatalog.getAllPages.map(_.asInstanceOf[PDPage]) zip Range(1, Int.MaxValue)) {
        val width = page.getMediaBox.getWidth * scale
        val height = page.getMediaBox.getHeight * scale

        writer.write( s"""<div><img src="${baseName.getName}$idx.png" style="width:${width}px;height:${height}px;" /></div>""")
      }
      writer.write("</body></html>")
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

  def applySignature(signatureBlocks:Seq[SigningBlock], signature:String){
    val image = convertJsonToImage(signature)
    val ximage = new PDJpeg(document, image)
    val pages = document.getDocumentCatalog.getAllPages()
    for(block <- signatureBlocks;
        page = pages(block.page - 1).asInstanceOf[PDPage];
        cs <- managed(new PDPageContentStream(document,page, true, true))){
      cs.drawImage(ximage, block.xLocation, page.getMediaBox.getHeight - block.yLocation - block.height)
    }
  }

  def convertJsonToImage(jsonString: String) = {
    val gson = new Gson()
    val signatureLines = gson.fromJson(jsonString, classOf[Array[SignatureLine]])
    val offscreenImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB)
    val g2 = offscreenImage.createGraphics()
    g2.setColor(Color.white)
    g2.fillRect(0, 0, 200, 50)
    g2.setPaint(Color.black)
    for (line <- signatureLines) {
      g2.drawLine(line.lx, line.ly, line.mx, line.my)
    }
    g2.dispose()
    offscreenImage
  }
}
