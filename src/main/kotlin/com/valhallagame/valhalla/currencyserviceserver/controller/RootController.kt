package com.valhallagame.valhalla.currencyserviceserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.valhallagame.common.JS
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
class RootController {
    @RequestMapping(path = ["/"], method = [RequestMethod.GET])
    fun ping(): ResponseEntity<JsonNode> {
       return JS.message(HttpStatus.OK, "pong")
    }
}
