package com.adp.ds.pdfweb

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class SimpleCORSFilter extends Filter {
  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    val response = res.asInstanceOf[HttpServletResponse]
    response.setHeader("Access-Control-Allow-Origin", "*")
    response.setHeader("Access-Control-Allow-Methods", "*")
    response.setHeader("Access-Control-Max-Age", "3600")
    response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type,Content-Length,Date,Server,X-Application-Context")
    chain.doFilter(req, res)
  }

  def init(filterConfig: FilterConfig) {}

  def destroy() {}

}