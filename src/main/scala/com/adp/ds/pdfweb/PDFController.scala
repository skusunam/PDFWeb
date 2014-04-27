package com.adp.ds.pdfweb

import org.springframework.web.bind.annotation.{RequestMethod, ResponseBody, RequestParam, RequestMapping}
import org.springframework.stereotype.Controller

@Controller
@RequestMapping(Array("/pdf"))
@ResponseBody
class PDFController {
  @RequestMapping(method = Array(RequestMethod.GET))
  def index(@RequestParam name:String) = "Hello, " + name
}
