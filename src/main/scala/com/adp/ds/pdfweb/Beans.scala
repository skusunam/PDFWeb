package com.adp.ds.pdfweb

import scala.beans.BeanInfo
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import java.io.File


@BeanInfo class SigningBlock {
  @JsonProperty var page = 1
  @JsonProperty var width = 0f
  @JsonProperty var height = 0f
  @JsonProperty var xLocation = 0f
  @JsonProperty var yLocation = 0f
  @JsonProperty var role = ""
}

@BeanInfo class SignablePage {
  @JsonProperty var imageUrl = ""
  @JsonProperty var signingBlocks = Array[SigningBlock]()
}

@BeanInfo class SignableDocument {
  @JsonProperty var title = ""
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
  @JsonProperty var document:SignableDocument = null
  @JsonProperty var baseDirectory:File = null
}
@BeanInfo class SignatureLine {
  @JsonProperty var lx = 0
  @JsonProperty var ly = 0
  @JsonProperty var mx = 0
  @JsonProperty var my = 0
}