package com.adp.ds.pdfweb

object SigningRole extends Enumeration {
  val Buyer, Cobuyer, FIManager = Value
}
case class SigningBlock(page:Int, width:Float, height:Float, xLocation:Float, yLocation:Float, role:SigningRole.Value)
case class SigningRequest(body:Array[Byte], signingBlocks:Array[SigningBlock])