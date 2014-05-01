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
        tx.execute("CREATE TABLE IF NOT EXISTS pages(id SERIAL PRIMARY KEY, idx INT, requestId INT REFERENCES requests(id), width FLOAT, height FLOAT, path VARCHAR(1000))")
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
        logger.info(s"Created request with Id $requestId")
        for ((page, idx) <- request.document.pages zip Range(0, Int.MaxValue)) {
          val pageId = tx.selectInt("INSERT INTO pages (idx, requestId, width, height, path) VALUES (?, ?, ?, ?, ?) RETURNING id", idx, requestId, page.width, page.height, page.path)
          page.id = pageId
        }
        for ((page, block) <- request.document.pages.flatMap(p => p.signingBlocks.map((p, _)))) {
          block.pageIdx = page.idx
          tx.execute("INSERT INTO signingblocks(requestId, page, xlocation, ylocation, width, height, role) VALUES (?, ?, ?, ?, ?, ?, ?)", requestId, block.pageIdx, block.xLocation, block.yLocation, block.width, block.height, block.role)
        }
    }
  }

  def getOrigPDFPath(requestId: String) = {
    SigningRequestsRepository.database.transaction {
      tx => tx.select("SELECT originalFileName FROM requests WHERE sourceId = ?", requestId) {
        r => r:String
      }
    }
  }

  def getDeals() = {
    SigningRequestsRepository.database.transaction {
      tx =>
        val blocksWithRequestId = tx.select("SELECT id, page, xlocation, ylocation, width, height, role, requestId FROM signingblocks") {
          r => (new SigningBlock {
            id = r
            pageIdx = r
            xLocation = r
            yLocation = r
            width = r
            height = r
            role = r
          }, r: Int)
        }

        val pagesWithRequestId = tx.select("SELECT id, idx, width, height, path, requestId FROM pages") {
          r => (new SignablePage {
            id = r
            idx = r
            width = r
            height = r
            path = r
          }, r: Int)
        }

        val requests = tx.select("SELECT sourceId, dealId, originalFileName,id FROM requests") {
          r => (new SigningRequest {
            id = r
            dealId = r
            originalFileName = r
            document = new SignableDocument()
          }, r: Int)
        }

        val groupedBlocks = blocksWithRequestId.groupBy(kv => kv._2)
        val groupedPages = pagesWithRequestId.groupBy(kv => kv._2)
        val groupedRequests = requests.groupBy(kv => kv._1.dealId)
        for ((requestDealId, requestInfos) <- groupedRequests)
        yield new Deal {
          dealId = requestDealId
          documents = (for ((request, requestId) <- requestInfos) yield new SignableDocument {
            pages = (for ((page, _) <- groupedPages(requestId))
            yield
              new SignablePage {
                id = page.id
                width = page.width
                height = page.height
                path = page.path
                signingBlocks = (for ((block, _) <- groupedBlocks(requestId) if block.pageIdx == page.idx) yield block).toArray
              }).toArray
            originalRequestId = request.id
          }).toArray
        }
    }
  }
}