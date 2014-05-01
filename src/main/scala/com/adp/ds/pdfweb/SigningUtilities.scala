package com.adp.ds.pdfweb

import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import java.awt.Color
import java.io.{InputStream, File, FileOutputStream, FileWriter}
import org.apache.pdfbox.util.ImageIOUtil
import org.slf4j.LoggerFactory
import resource.managed
import scala.collection.JavaConversions._

class SigningUtilities(inputStream: InputStream) {
  val logger = LoggerFactory.getLogger(classOf[SigningUtilities])

  implicit def toFile(path: String) = new File(path)

  val scale = 96.0f / 72.0f
  logger.debug(s"Reading file")
  val document = PDDocument.load(inputStream)

  def close() {
    document.close()
  }

  def processSigningPDF(request: SigningRequest, baseDirectory: File) {
    val baseName = s"${baseDirectory.getAbsolutePath}${File.separator}request"
    request.originalFileName = s"${baseName}_orig.pdf"
    document.save(request.originalFileName)
    writeHtml(baseName)
    for (((page, reqPage), idx) <- document.getDocumentCatalog.getAllPages.map(_.asInstanceOf[PDPage]) zip request.document.pages zip Range(1, Int.MaxValue)) {
      val fileName = s"$baseName$idx.png"
      writeImage(page, fileName)
      reqPage.path = fileName
      reqPage.width = page.getMediaBox.getWidth
      reqPage.height = page.getMediaBox.getHeight
    }
  }

  def addBlocks(signatures: Seq[SigningBlock]) = {
    for (signature <- signatures) {
      val page = document.getDocumentCatalog.getAllPages()(signature.pageIdx).asInstanceOf[PDPage]
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
    logger.info(s"Writing to file $fileName")
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

  def writeImage(page: PDPage, outFile: String) {
    val img = page.convertToImage
    logger.info(s"Writing to file $outFile")
    for (os <- managed(new FileOutputStream(outFile))) {
      ImageIOUtil.writeImage(img, "png", os)
    }
  }

  def adjustSignatureLines(signatureLines:Seq[SignatureLine], block:SigningBlock) = {
    val minY = signatureLines.flatMap(s=>Vector(s.ly, s.my)).min
    val minX = signatureLines.flatMap(s=>Vector(s.lx, s.mx)).min
    val realignedSignature = for(line <- signatureLines) yield new SignatureLine{
      lx = line.lx - minX
      mx = line.mx - minX
      ly = line.ly - minY
      my = line.my - minY
    }
    val maxY = realignedSignature.flatMap(s=>Vector(s.ly, s.my)).max
    val maxX = realignedSignature.flatMap(s=>Vector(s.lx, s.mx)).max
    val ratioX = block.width / maxX
    val ratioY = block.height / maxY
    val ratio = Math.min(ratioX, ratioY)
    logger.info(s"Zooming ${ratio}x given signing block ${block.width}x${block.height} and signature ${maxX}x${maxY}")
    for(line <- realignedSignature) yield new SignatureLine{
      lx = Math.floor(line.lx * ratio).toInt
      mx = Math.floor(line.mx * ratio).toInt
      ly = Math.floor(line.ly * ratio).toInt
      my = Math.floor(line.my * ratio).toInt
    }
  }
  def applySignature(signatureBlocks: Seq[SigningBlock], signatureLines: Seq[SignatureLine]) = {
    logger.info("Applying signature")

    val pages = document.getDocumentCatalog.getAllPages()
    for (block <- signatureBlocks;
         page <- pages.map(_.asInstanceOf[PDPage]).zip(Range(0, Int.MaxValue)).filter{case (p, i)=> i == block.pageIdx}.map{case (p,_) => p};
         cs <- managed(new PDPageContentStream(document, page, true, true))) {
      val adjustedLines = adjustSignatureLines(signatureLines, block)
      val blockZeroY = page.getMediaBox.getHeight - block.yLocation
      cs.setStrokingColor(Color.black)
      for (line <- adjustedLines) {
        cs.drawLine(block.xLocation + line.lx, blockZeroY - line.ly, block.xLocation + line.mx, blockZeroY - line.my)
      }
    }
  }
}
