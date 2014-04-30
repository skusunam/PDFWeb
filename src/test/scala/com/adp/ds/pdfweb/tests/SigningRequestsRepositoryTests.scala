package com.adp.ds.pdfweb.tests

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import com.adp.ds.pdfweb._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.io.File

@RunWith(classOf[JUnitRunner])
class SigningRequestsRepositoryTests extends FunSuite with BeforeAndAfterEach {
  val repo = new SigningRequestsRepository()
  override def beforeEach() {
    SigningRequestsRepository.database.transaction{tx => {
      tx.execute("TRUNCATE TABLE signingblocks")
      tx.execute("TRUNCATE TABLE requests")
    }}
  }

  test("Should be able to insert into the repository") {
    repo.addRequest(new SigningRequest{
      id = "bob"
      dealId = "mydealid"
      baseDirectory = new File("data/testrequests")
      document = new SignableDocument {
        title = "Interesting ID"
        pages = Array(new SignablePage {
          signingBlocks = Array(new SigningBlock{
            page = 1
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
  }
}
