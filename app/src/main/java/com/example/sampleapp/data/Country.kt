package com.example.sampleapp.data

import com.google.gson.annotations.SerializedName

class Country {
    @SerializedName("name")
    var name: Name? = null

    @SerializedName("flags")
    var flags: Flags? = null

    @SerializedName("capital")
    var capital: List<String>? = null

    class Name {
        @SerializedName("common")
        var common: String? = null
    }

    class Flags {
        @SerializedName("png")
        var png: String? = null

    }
}
