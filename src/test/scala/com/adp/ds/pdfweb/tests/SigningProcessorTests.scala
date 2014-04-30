package com.adp.ds.pdfweb.tests

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import akka.actor._
import com.adp.ds.pdfweb._
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.io.Source
import com.adp.ds.pdfweb.ProcessSigningRequest
import akka.actor.Terminated
import resource.managed

@RunWith(classOf[JUnitRunner])
class SigningProcessorTests extends FunSuite with BeforeAndAfterEach {
  new File("data/testrepository").delete()


  var system = ActorSystem("system")
  val actor = system.actorOf(Props(new SigningProcessor(new SigningRequestsRepository(), new File("data/testrepository"))))
  system.actorOf(Props(new Actor {
    context.watch(actor)

    def receive = {
      case Terminated(ref) => system.shutdown()
    }
  }))

  override def beforeEach() {
    SigningRequestsRepository.database.transaction {
      tx => {
        tx.execute("TRUNCATE TABLE signingblocks")
        tx.execute("TRUNCATE TABLE requests")
      }
    }
  }

  test("Should be able to post request") {
    for (source <- managed(Source.fromFile("testFile.pdf")(scala.io.Codec.ISO8859))) {
      val bytes = source.map(_.toByte).toArray
      actor ! ProcessSigningRequest(new SigningRequest {
        id = "myrequest"
        dealId = "mydealid"
        document = new SignableDocument {
          title = "MyDocument"
          contents = bytes
          pages = Array(new SignablePage {
            signingBlocks = Array(
              new SigningBlock {
                page = 1
                width = 257
                height = 27
                xLocation = 63
                yLocation = 618
                role = "buyer"
              },
              new SigningBlock {
                page = 1
                width = 257
                height = 27
                xLocation = 63
                yLocation = 663
                role = "cobuyer"
              }
            )
          })
        }
      })
      actor ! PoisonPill
      system.awaitTermination(5 seconds)

      SigningRequestsRepository.database.transaction {
        tx =>
          assert(SigningRequestsRepository.database.transaction(_.selectInt("SELECT COUNT(*) FROM requests")) === 1)

          assert(SigningRequestsRepository.database.transaction(_.selectInt("SELECT COUNT(*) FROM signingblocks")) === 2)
      }
    }

  }
}
