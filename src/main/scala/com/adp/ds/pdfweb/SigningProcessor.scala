package com.adp.ds.pdfweb

import akka.actor.Actor
import org.slf4j.LoggerFactory
import java.io.{ByteArrayInputStream, File}
import java.util.UUID
import resource._
import scala.io.Source

class SigningProcessor(repo: SigningRequestsRepository, requestsBaseDirectory: File) extends Actor {
  val logger = LoggerFactory.getLogger(classOf[SigningProcessor])
  logger.info(s"Creating directory $requestsBaseDirectory")
  requestsBaseDirectory.mkdirs()

  def receive = {
    case ProcessSigningRequest(request) =>
      logger.info("Processing signing request")
      val suffix = UUID.randomUUID().toString
      val requestBaseDirectory = new File(s"${requestsBaseDirectory}${File.separator}$suffix")
      requestBaseDirectory.mkdir()
      val utils = new SigningUtilities(new ByteArrayInputStream(request.document.contents))
      utils.processSigningPDF(request, requestBaseDirectory)
      repo.addRequest(request)
    case ProcessSignedBlock(signedBlock) =>
      logger.info("Processing signed block")
      val deals = repo.getDeals()
      for(deal <-deals; document <- deal.documents if document.pages.filter(p=>p.signingBlocks.exists(b=>b.id == signedBlock.signingBlockId)).length > 0;
         origFilePath <- repo.getOrigPDFPath(document.originalRequestId)){
        val origRequestPathFile = new File(origFilePath)
        val signedPDFName = s"${origRequestPathFile.getParentFile.getAbsolutePath}${File.separator}request_signed.pdf"
        logger.info(s"Signing document into ${signedPDFName}")
        for (source <- managed(Source.fromFile(origFilePath)(scala.io.Codec.ISO8859))) {
          val bytes = source.map(_.toByte).toArray
          val utils = new SigningUtilities(new ByteArrayInputStream(bytes))
          utils.applySignature(document.pages.flatMap(p=>p.signingBlocks).filter(b=>b.id == signedBlock.signingBlockId), signedBlock.signatureLines)
          utils.document.save(new File(signedPDFName))
        }
      }
  }
}
