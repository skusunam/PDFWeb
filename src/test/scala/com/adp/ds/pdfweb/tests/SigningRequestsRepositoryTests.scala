package com.adp.ds.pdfweb.tests

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import com.adp.ds.pdfweb._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class SigningRequestsRepositoryTests extends FunSuite with BeforeAndAfterEach {
  val repo = new SigningRequestsRepository()
  override def beforeEach() {
    SigningRequestsRepository.database.transaction{tx => {
      tx.execute("TRUNCATE TABLE pages")
      tx.execute("TRUNCATE TABLE signingblocks")
      tx.execute("DELETE FROM requests")
    }}
  }

  test("Should be able to insert into the repository") {
    repo.addRequest(new SigningRequest{
      id = "bob"
      dealId = "mydealid"
      originalFileName = "myfileName.pdf"
      document = new SignableDocument {
        title = "Interesting ID"
        pages = Array(new SignablePage {
          id = 123
          width = 640
          height = 480
          signingBlocks = Array(new SigningBlock{
            xLocation = 123
            yLocation = 456
            width = 276
            height = 30
            role = "Buyer"
          })
        })
      }
    })

    assert(SigningRequestsRepository.database.transaction(_.selectInt("SELECT COUNT(*) FROM requests")) === 1)
    assert(SigningRequestsRepository.database.transaction(_.selectInt("SELECT COUNT(*) FROM signingblocks")) === 1)
    assert(SigningRequestsRepository.database.transaction(_.selectInt("SELECT COUNT(*) FROM pages")) === 1)
  }
}
