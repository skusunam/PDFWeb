package com.adp.ds.pdfweb

import net.noerd.prequel.DatabaseConfig
import net.noerd.prequel.SQLFormatterImplicits._
import net.noerd.prequel.ResultSetRowImplicits._
import org.slf4j.LoggerFactory

object SigningRequestsRepository {
  val database = DatabaseConfig(
    driver = "org.postgresql.Driver",
    jdbcURL = "jdbc:postgresql://localhost/dev?user=pdfweb&password=foobar"
  )

  def init() {
    SigningRequestsRepository.database.transaction {
      tx => {
        tx.execute("CREATE TABLE IF NOT EXISTS requests(id SERIAL PRIMARY KEY, sourceId VARCHAR(255), dealId VARCHAR(255), originalFileName VARCHAR(1000))")
        tx.execute("CREATE TABLE IF NOT EXISTS pages(id SERIAL PRIMARY KEY, requestId INT REFERENCES requests(id), width FLOAT, height FLOAT, path VARCHAR(1000))")
        tx.execute("CREATE TABLE IF NOT EXISTS signingblocks(id SERIAL PRIMARY KEY, requestId INT REFERENCES requests(id), page INT, xlocation FLOAT, ylocation FLOAT, width FLOAT, height FLOAT, role VARCHAR(255))")
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
        val requestId = tx.selectInt("INSERT INTO requests (sourceId, dealId, originalFileName) VALUES (?, ?, ?) RETURNING id", request.id, request.dealId, request.originalFileName)
        logger.info(s"Created request with Id ${requestId}")
        for (page <- request.document.pages) {
          val pageId = tx.selectInt("INSERT INTO pages (requestId, width, height, path) VALUES (?, ?, ?, ?) RETURNING id", requestId, page.width, page.height, page.path)
          page.id = pageId
        }
        for ((page, block) <- request.document.pages.flatMap(p => p.signingBlocks.map((p, _)))) {
          block.pageId = page.id
          tx.execute("INSERT INTO signingblocks(requestId, page, xlocation, ylocation, width, height, role) VALUES (?, ?, ?, ?, ?, ?, ?)", requestId, block.pageId, block.xLocation, block.yLocation, block.width, block.height, block.role)
        }
    }
  }

  def getDeals() = {
    SigningRequestsRepository.database.transaction {
      tx =>
        val blocksWithRequestId = tx.select("SELECT id, page, xlocation, ylocation, width, height, role, requestId FROM signingblocks") {
          r => (new SigningBlock {
            id = r
            pageId = r
            xLocation = r
            yLocation = r
            width = r
            height = r
            role = r
          }, r: Int)
        }

        val pagesWithRequestId = tx.select("SELECT id, width, height, path, requestId FROM pages") {
          r => (new SignablePage {
            id = r
            width = r
            height = r
            path = r
          }, r: Int)
        }

        val groupedBlocks = blocksWithRequestId.groupBy(kv => kv._2)
        val groupedPages = pagesWithRequestId.groupBy(kv => kv._2)
        val requestPages = for ((requestId, blocks) <- groupedBlocks;
                                pages = groupedPages(requestId).map(kv => kv._1);
                                page <- pages
        ) yield
          new SignablePage {
            id = page.id
            width = page.width
            height = page.height
            path = page.path
            signingBlocks = blocks.map(kv => kv._1).toArray
          }
        new Deal {
          documents = Array(new SignableDocument {
            pages = requestPages.toArray
          })
        }
    }
  }
}