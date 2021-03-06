package com.adp.ds.pdfweb

import org.springframework.web.bind.annotation._
import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import akka.actor.ActorRef
import java.io.File

@Controller
@ResponseBody
class PDFController @Autowired()(signingProcessor: ActorRef, repo: SigningRequestsRepository) {
  val logger = LoggerFactory.getLogger(classOf[PDFController])
  val assetsPrefix = new File("data/testrepository").getAbsolutePath
  val assetsUrlPrefix = "/assets"

  @RequestMapping(value = Array("/api/deals", "/deals"), method = Array(RequestMethod.GET))
  def deals() = {
    val deals = repo.getDeals().toArray
    for (deal <- deals;
         page <- deal.documents.flatMap(d => d.pages)) {
      page.imageUrl = page.path.replace(assetsPrefix, assetsUrlPrefix)
    }
    deals
  }

  @RequestMapping(value = Array("/api/signingrequests", "/signingrequests"), method = Array(RequestMethod.POST))
  def signingrequests(@RequestBody request: SigningRequest) = {
    signingProcessor ! ProcessSigningRequest(request)
    "Posted"
  }


  /**
   * Example JSON
[{"lx":33,"ly":10,"mx":33,"my":9},{"lx":32,"ly":9,"mx":33,"my":10},{"lx":29,"ly":9,"mx":32,"my":9},{"lx":23,"ly":9,"mx":29,"my":9},{"lx":18,"ly":15,"mx":23,"my":9},{"lx":16,"ly":17,"mx":18,"my":15},{"lx":16,"ly":19,"mx":16,"my":17},{"lx":16,"ly":20,"mx":16,"my":19},{"lx":16,"ly":22,"mx":16,"my":20},{"lx":21,"ly":27,"mx":16,"my":22},{"lx":28,"ly":29,"mx":21,"my":27},{"lx":35,"ly":31,"mx":28,"my":29},{"lx":40,"ly":32,"mx":35,"my":31},{"lx":42,"ly":34,"mx":40,"my":32},{"lx":43,"ly":36,"mx":42,"my":34},{"lx":43,"ly":39,"mx":43,"my":36},{"lx":36,"ly":43,"mx":43,"my":39},{"lx":26,"ly":48,"mx":36,"my":43},{"lx":18,"ly":51,"mx":26,"my":48},{"lx":14,"ly":52,"mx":18,"my":51},{"lx":13,"ly":52,"mx":14,"my":52},{"lx":49,"ly":24,"mx":49,"my":23},{"lx":50,"ly":23,"mx":49,"my":24},{"lx":52,"ly":23,"mx":50,"my":23},{"lx":53,"ly":24,"mx":52,"my":23},{"lx":57,"ly":28,"mx":53,"my":24},{"lx":58,"ly":34,"mx":57,"my":28},{"lx":59,"ly":39,"mx":58,"my":34},{"lx":59,"ly":42,"mx":59,"my":39},{"lx":59,"ly":46,"mx":59,"my":42},{"lx":59,"ly":48,"mx":59,"my":46},{"lx":58,"ly":49,"mx":59,"my":48},{"lx":57,"ly":49,"mx":58,"my":49},{"lx":56,"ly":48,"mx":57,"my":49},{"lx":56,"ly":46,"mx":56,"my":48},{"lx":56,"ly":43,"mx":56,"my":46},{"lx":56,"ly":35,"mx":56,"my":43},{"lx":57,"ly":32,"mx":56,"my":35},{"lx":59,"ly":29,"mx":57,"my":32},{"lx":60,"ly":27,"mx":59,"my":29},{"lx":64,"ly":23,"mx":60,"my":27},{"lx":65,"ly":22,"mx":64,"my":23},{"lx":67,"ly":22,"mx":65,"my":22},{"lx":79,"ly":24,"mx":79,"my":23},{"lx":79,"ly":25,"mx":79,"my":24},{"lx":79,"ly":27,"mx":79,"my":25},{"lx":79,"ly":29,"mx":79,"my":27},{"lx":79,"ly":33,"mx":79,"my":29},{"lx":79,"ly":34,"mx":79,"my":33},{"lx":79,"ly":35,"mx":79,"my":34},{"lx":79,"ly":36,"mx":79,"my":35},{"lx":79,"ly":39,"mx":79,"my":36},{"lx":79,"ly":43,"mx":79,"my":39},{"lx":79,"ly":45,"mx":79,"my":43},{"lx":80,"ly":45,"mx":79,"my":45},{"lx":75,"ly":11,"mx":75,"my":10},{"lx":76,"ly":10,"mx":75,"my":11},{"lx":77,"ly":10,"mx":76,"my":10},{"lx":78,"ly":10,"mx":77,"my":10},{"lx":79,"ly":10,"mx":78,"my":10},{"lx":80,"ly":10,"mx":79,"my":10},{"lx":81,"ly":9,"mx":80,"my":10},{"lx":82,"ly":7,"mx":81,"my":9},{"lx":83,"ly":5,"mx":82,"my":7},{"lx":83,"ly":3,"mx":83,"my":5},{"lx":83,"ly":1,"mx":83,"my":3},{"lx":82,"ly":0,"mx":83,"my":1},{"lx":80,"ly":-1,"mx":82,"my":0},{"lx":79,"ly":-1,"mx":80,"my":-1},{"lx":75,"ly":-1,"mx":79,"my":-1},{"lx":72,"ly":2,"mx":75,"my":-1},{"lx":71,"ly":3,"mx":72,"my":2},{"lx":70,"ly":5,"mx":71,"my":3},{"lx":70,"ly":8,"mx":70,"my":5},{"lx":70,"ly":9,"mx":70,"my":8},{"lx":70,"ly":10,"mx":70,"my":9},{"lx":70,"ly":11,"mx":70,"my":10},{"lx":71,"ly":11,"mx":70,"my":11},{"lx":74,"ly":11,"mx":71,"my":11},{"lx":75,"ly":11,"mx":74,"my":11},{"lx":76,"ly":12,"mx":75,"my":11},{"lx":77,"ly":12,"mx":76,"my":12},{"lx":80,"ly":12,"mx":77,"my":12},{"lx":81,"ly":12,"mx":80,"my":12},{"lx":82,"ly":12,"mx":81,"my":12},{"lx":90,"ly":23,"mx":90,"my":22},{"lx":91,"ly":22,"mx":90,"my":23},{"lx":92,"ly":22,"mx":91,"my":22},{"lx":93,"ly":22,"mx":92,"my":22},{"lx":94,"ly":22,"mx":93,"my":22},{"lx":95,"ly":25,"mx":94,"my":22},{"lx":95,"ly":29,"mx":95,"my":25},{"lx":95,"ly":34,"mx":95,"my":29},{"lx":95,"ly":38,"mx":95,"my":34},{"lx":95,"ly":42,"mx":95,"my":38},{"lx":95,"ly":45,"mx":95,"my":42},{"lx":95,"ly":44,"mx":95,"my":45},{"lx":95,"ly":42,"mx":95,"my":44},{"lx":95,"ly":39,"mx":95,"my":42},{"lx":100,"ly":31,"mx":95,"my":39},{"lx":104,"ly":26,"mx":100,"my":31},{"lx":108,"ly":23,"mx":104,"my":26},{"lx":110,"ly":23,"mx":108,"my":23},{"lx":111,"ly":23,"mx":110,"my":23},{"lx":112,"ly":23,"mx":111,"my":23},{"lx":113,"ly":23,"mx":112,"my":23},{"lx":114,"ly":25,"mx":113,"my":23},{"lx":116,"ly":30,"mx":114,"my":25},{"lx":116,"ly":34,"mx":116,"my":30},{"lx":116,"ly":38,"mx":116,"my":34},{"lx":116,"ly":40,"mx":116,"my":38},{"lx":116,"ly":41,"mx":116,"my":40},{"lx":117,"ly":42,"mx":116,"my":41},{"lx":117,"ly":43,"mx":117,"my":42},{"lx":117,"ly":44,"mx":117,"my":43},{"lx":118,"ly":44,"mx":117,"my":44},{"lx":118,"ly":45,"mx":118,"my":44},{"lx":129,"ly":26,"mx":129,"my":25},{"lx":129,"ly":28,"mx":129,"my":26},{"lx":129,"ly":29,"mx":129,"my":28},{"lx":129,"ly":30,"mx":129,"my":29},{"lx":129,"ly":36,"mx":129,"my":30},{"lx":129,"ly":37,"mx":129,"my":36},{"lx":129,"ly":38,"mx":129,"my":37},{"lx":129,"ly":39,"mx":129,"my":38},{"lx":130,"ly":39,"mx":129,"my":39},{"lx":130,"ly":9,"mx":130,"my":8},{"lx":132,"ly":8,"mx":130,"my":9},{"lx":133,"ly":8,"mx":132,"my":8},{"lx":134,"ly":8,"mx":133,"my":8},{"lx":136,"ly":8,"mx":134,"my":8},{"lx":138,"ly":7,"mx":136,"my":8},{"lx":139,"ly":5,"mx":138,"my":7},{"lx":140,"ly":1,"mx":139,"my":5},{"lx":140,"ly":-1,"mx":140,"my":1},{"lx":140,"ly":-2,"mx":140,"my":-1},{"lx":129,"ly":1,"mx":129,"my":0},{"lx":128,"ly":2,"mx":129,"my":1},{"lx":128,"ly":5,"mx":128,"my":2},{"lx":128,"ly":6,"mx":128,"my":5},{"lx":128,"ly":8,"mx":128,"my":6},{"lx":128,"ly":13,"mx":128,"my":8},{"lx":129,"ly":14,"mx":128,"my":13},{"lx":130,"ly":15,"mx":129,"my":14},{"lx":131,"ly":15,"mx":130,"my":15},{"lx":140,"ly":28,"mx":140,"my":27},{"lx":140,"ly":30,"mx":140,"my":28},{"lx":141,"ly":32,"mx":140,"my":30},{"lx":142,"ly":34,"mx":141,"my":32},{"lx":143,"ly":36,"mx":142,"my":34},{"lx":146,"ly":38,"mx":143,"my":36},{"lx":147,"ly":38,"mx":146,"my":38},{"lx":148,"ly":37,"mx":147,"my":38},{"lx":148,"ly":35,"mx":148,"my":37},{"lx":149,"ly":31,"mx":148,"my":35},{"lx":151,"ly":22,"mx":149,"my":31},{"lx":153,"ly":16,"mx":151,"my":22},{"lx":153,"ly":11,"mx":153,"my":16},{"lx":154,"ly":9,"mx":153,"my":11},{"lx":168,"ly":21,"mx":168,"my":20},{"lx":168,"ly":22,"mx":168,"my":21},{"lx":168,"ly":24,"mx":168,"my":22},{"lx":168,"ly":25,"mx":168,"my":24},{"lx":168,"ly":30,"mx":168,"my":25},{"lx":168,"ly":33,"mx":168,"my":30},{"lx":168,"ly":36,"mx":168,"my":33},{"lx":169,"ly":36,"mx":168,"my":36},{"lx":170,"ly":36,"mx":169,"my":36},{"lx":172,"ly":36,"mx":170,"my":36},{"lx":175,"ly":32,"mx":172,"my":36},{"lx":176,"ly":29,"mx":175,"my":32},{"lx":176,"ly":26,"mx":176,"my":29},{"lx":176,"ly":24,"mx":176,"my":26},{"lx":176,"ly":21,"mx":176,"my":24},{"lx":176,"ly":19,"mx":176,"my":21},{"lx":176,"ly":21,"mx":176,"my":19},{"lx":177,"ly":23,"mx":176,"my":21},{"lx":178,"ly":27,"mx":177,"my":23},{"lx":180,"ly":30,"mx":178,"my":27},{"lx":182,"ly":36,"mx":180,"my":30},{"lx":182,"ly":37,"mx":182,"my":36},{"lx":183,"ly":40,"mx":182,"my":37},{"lx":184,"ly":40,"mx":183,"my":40},{"lx":193,"ly":16,"mx":193,"my":15},{"lx":193,"ly":17,"mx":193,"my":16},{"lx":193,"ly":18,"mx":193,"my":17},{"lx":195,"ly":18,"mx":193,"my":18},{"lx":196,"ly":19,"mx":195,"my":18},{"lx":199,"ly":22,"mx":196,"my":19},{"lx":196,"ly":36,"mx":196,"my":35},{"lx":194,"ly":37,"mx":196,"my":36},{"lx":192,"ly":38,"mx":194,"my":37},{"lx":187,"ly":39,"mx":192,"my":38},{"lx":185,"ly":39,"mx":187,"my":39},{"lx":73,"ly":48,"mx":73,"my":47},{"lx":74,"ly":47,"mx":73,"my":48},{"lx":77,"ly":47,"mx":74,"my":47},{"lx":82,"ly":47,"mx":77,"my":47},{"lx":97,"ly":47,"mx":82,"my":47},{"lx":104,"ly":47,"mx":97,"my":47},{"lx":113,"ly":47,"mx":104,"my":47},{"lx":122,"ly":47,"mx":113,"my":47},{"lx":125,"ly":47,"mx":122,"my":47},{"lx":129,"ly":47,"mx":125,"my":47},{"lx":133,"ly":47,"mx":129,"my":47},{"lx":138,"ly":47,"mx":133,"my":47},{"lx":142,"ly":47,"mx":138,"my":47},{"lx":143,"ly":47,"mx":142,"my":47},{"lx":148,"ly":47,"mx":143,"my":47},{"lx":150,"ly":47,"mx":148,"my":47},{"lx":152,"ly":47,"mx":150,"my":47},{"lx":153,"ly":47,"mx":152,"my":47},{"lx":158,"ly":47,"mx":153,"my":47},{"lx":159,"ly":47,"mx":158,"my":47},{"lx":161,"ly":47,"mx":159,"my":47},{"lx":162,"ly":47,"mx":161,"my":47}]
   * @param body
   * @return
   */
  @RequestMapping(value = Array("/api/signedBlock", "/signedBlock"), method = Array(RequestMethod.POST))
  def post(@RequestBody body: SignedBlock) = {
    signingProcessor ! ProcessSignedBlock(body)
    "Document signing has been queued"
  }

}
