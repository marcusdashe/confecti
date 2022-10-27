package com.confbeve.app.controller

import com.confbeve.app.dto.LoginDTO
import com.confbeve.app.dto.Message
import com.confbeve.app.dto.RegisterDTO
import com.confbeve.app.model.User
import com.confbeve.app.service.UserService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie
import java.util.Date
import javax.crypto.SecretKey


@RestController
@RequestMapping("api")
class AuthController(private val userService: UserService) {
    companion object {
        private val key: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        private val secretString : String = Encoders.BASE64.encode(key.getEncoded())
    }
    @PostMapping("register")
    fun register(@RequestBody body : RegisterDTO): ResponseEntity<User> {
        val user = User()
        user.name = body.name
        user.email = body.email
        user.password = body.password
        return ResponseEntity.ok(this.userService.save(user))
    }

    @PostMapping("login")
    fun login(@RequestBody body : LoginDTO, response: HttpServletResponse) : ResponseEntity<Any>{
        val user = this.userService.findByEmail(body.email)
            ?: return ResponseEntity.badRequest().body(Message("User not found"))

        if(!user.comparePassword(body.password)){
            return ResponseEntity.badRequest().body(Message("Invalid Password"))
        }

        val issuer = user.id.toString()

        val jwt = Jwts.builder().setIssuer(issuer).setExpiration(Date(System.currentTimeMillis() + 60 * 24 * 1000)) // a day
            .signWith(SignatureAlgorithm.HS512, secretString).compact()
        var cookie = Cookie("jwt", jwt)
        cookie.isHttpOnly = true

        response.addCookie(cookie)
        return ResponseEntity.ok(Message("Success"))
    }

    @GetMapping("user")
    fun user(@CookieValue("jwt") jwt: String?): ResponseEntity<Any>{
        try{
            if(jwt === null){
                return ResponseEntity.status(401).body(Message("Unauthenticated jwt is null"))
            }
//            val body = Jwts.parser().setSigningKey(secretString).parseClaimsJwt(jwt).body
            val body = Jwts.parser().setSigningKey(secretString).parseClaimsJws(jwt).getBody()
            return ResponseEntity.ok(this.userService.getById(body.issuer.toInt()))

        } catch(ex: Exception){
            return ResponseEntity.status(401).body(Message("Unauthenticated Oooops exception!"))
        }
    }

    @PostMapping("logout")
    fun logout(response: HttpServletResponse) : ResponseEntity<Any>{
        val cookie = Cookie("jwt", "")
        cookie.maxAge = 0
        response.addCookie(cookie)
        return ResponseEntity.ok(Message("Success"))
    }

    @PostMapping("/file/upload", consumes = [ MediaType.MULTIPART_FORM_DATA_VALUE])
    fun handleFileUpload(@RequestPart("files") filess: List<MultipartFile>?):ResponseEntity<Any>{
        return ResponseEntity.ok(Message("Uploaded Successfully"))
    }
}