package com.adp.ds.pdfweb

import akka.actor.Actor
import org.slf4j.LoggerFactory
import java.io.{ByteArrayInputStream, InputStream, File}
import java.util.UUID

class SigningProcessor(repo:SigningRequestsRepository, requestsBaseDirectory:File) extends Actor {
  val logger = LoggerFactory.getLogger(classOf[SigningProcessor])
  logger.info(s"Creating directory ${requestsBaseDirectory}")
  requestsBaseDirectory.mkdirs()
  def receive = {
    case ProcessSigningRequest(request: SigningRequest) =>
      logger.info("Processing signing request")
      val suffix = UUID.randomUUID().toString
      val requestBaseDirectory = s"${requestsBaseDirectory}${File.separator}${suffix}"
      request.baseDirectory = new File(requestBaseDirectory)
      new File(requestBaseDirectory).mkdir()
      val utils = new SigningUtilities(new ByteArrayInputStream(request.document.contents))
      utils.processSigningPDF(request.document.pages.flatMap(_.signingBlocks), requestBaseDirectory)
      repo.addRequest(request)
  }
}
