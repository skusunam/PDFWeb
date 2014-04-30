package com.adp.ds.pdfweb

import net.noerd.prequel.DatabaseConfig
import net.noerd.prequel.SQLFormatterImplicits._
import net.noerd.prequel.ResultSetRowImplicits._
import org.slf4j.LoggerFactory
import java.sql.SQLException

object SigningRequestsRepository {
  val database = DatabaseConfig(
    driver = "org.postgresql.Driver",
    jdbcURL = "jdbc:postgresql://localhost/dev?user=pdfweb&password=foobar"
  )

  def init() {
    SigningRequestsRepository.database.transaction {
      tx => {
        tx.execute("CREATE TABLE IF NOT EXISTS requests(id SERIAL PRIMARY KEY, sourceId VARCHAR(255), dealId VARCHAR(255), basedirectory VARCHAR(1000))")
        tx.execute("CREATE TABLE IF NOT EXISTS signingblocks(id SERIAL PRIMARY KEY, requestId INT, page INT, xlocation FLOAT, ylocation FLOAT, width FLOAT, height FLOAT, role VARCHAR(255))")
      }
    }
  }

  init()
}

class SigningRequestsRepository {
  val logger = LoggerFactory.getLogger(classOf[SigningRequestsRepository])

  def addRequest(request: SigningRequest) = {
    SigningRequestsRepository.database.transaction {
      tx =>
        val requestId = tx.selectInt("INSERT INTO requests (sourceId, dealId, basedirectory) VALUES (?, ?, ?) RETURNING id", request.id, request.dealId, request.baseDirectory.getAbsolutePath)
        logger.info(s"Created request with Id ${requestId}")
        for (block <- request.document.pages.flatMap(_.signingBlocks)) {
          tx.execute("INSERT INTO signingblocks(requestId, page, xlocation, ylocation, width, height, role) VALUES (?, ?, ?, ?, ?, ?, ?)", requestId, block.page, block.xLocation, block.yLocation, block.width, block.height, block.role)
        }
    }
  }
}