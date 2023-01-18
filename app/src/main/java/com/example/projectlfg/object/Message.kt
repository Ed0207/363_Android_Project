package com.example.projectlfg

class Message {

    var msg: String? = null
    var sender: String? = null
    var imguri:String?=""
    constructor()

    constructor(msg: String, sender: String){
        this.msg = msg
        this.sender = sender
    }
}