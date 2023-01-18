package com.example.projectlfg

import DBEventsInformation

class UserInformation {

    // class instead of data for firebase operation

    var name: String? = ""
    var email: String? = ""
    var imageuri: String? = ""
    var uid: String? = ""
    var friendList: ArrayList<String>? = null
    var eventsinfolist =ArrayList<DBEventsInformation>();


    constructor()

    constructor(name: String, email: String, uri: String, uid:String) {
        this.name = name
        this.email = email
        this.imageuri = uri
        this.uid = uid
    }

}
