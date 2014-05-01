package com.adp.ds.pdfweb

import scala.beans.BeanInfo
import com.fasterxml.jackson.annotation.JsonProperty


@BeanInfo class SigningBlock {
  @JsonProperty var id = 0
  @JsonProperty var pageIdx = 0
  @JsonProperty var width = 0f
  @JsonProperty var height = 0f
  @JsonProperty var xLocation = 0f
  @JsonProperty var yLocation = 0f
  @JsonProperty var role = ""
}

@BeanInfo class SignablePage {
  @JsonProperty var id = 0
  @JsonProperty var idx = 0
  @JsonProperty var imageUrl = ""
  @JsonProperty var path = ""
  @JsonProperty var signingBlocks = Array[SigningBlock]()
  @JsonProperty var width = 0f
  @JsonProperty var height = 0f
}

@BeanInfo class SignableDocument {
  @JsonProperty var title = ""
  @JsonProperty var originalRequestId = ""
  @JsonProperty var pages = Array[SignablePage]()
  @JsonProperty var contents = Array[Byte]()
}

@BeanInfo class Deal {
  @JsonProperty var dealId = ""
  @JsonProperty var documents = Array[SignableDocument]()
}

@BeanInfo class SigningRequest {
  @JsonProperty var id = ""
  @JsonProperty var dealId = ""
  @JsonProperty var document: SignableDocument = null
  @JsonProperty var originalFileName: String = null
}

@BeanInfo class SignatureLine {
  @JsonProperty var lx = 0
  @JsonProperty var ly = 0
  @JsonProperty var mx = 0
  @JsonProperty var my = 0
}

@BeanInfo class SignedBlock {
  @JsonProperty var signingBlockId = 0
  @JsonProperty var signatureLines: Array[SignatureLine] = null
}